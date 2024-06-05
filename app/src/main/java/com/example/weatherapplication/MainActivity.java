package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView addressTextView, systemTimeTextView, weatherInfoTextView;
    private EditText cityNameEditText;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WeatherPrefs";
    private static final String KEY_CITY_NAME = "cityName";

    private Handler timeHandler;
    private Runnable timeRunnable;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameEditText = findViewById(R.id.city_name);
        Button fetchButton = findViewById(R.id.fetch_button);
        addressTextView = findViewById(R.id.address);
        systemTimeTextView = findViewById(R.id.system_time);
        weatherInfoTextView = findViewById(R.id.weather_info);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load the last searched city name
        String lastCityName = sharedPreferences.getString(KEY_CITY_NAME, "");
        if (!lastCityName.isEmpty()) {
            cityNameEditText.setText(lastCityName);
            fetchWeatherDataByCityName(lastCityName);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getLastLocation();
        }

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityNameEditText.getText().toString();
                fetchWeatherDataByCityName(cityName);
                saveCityName(cityName);
            }
        });

        // Initialize the Handler and Runnable to update the system time
        timeHandler = new Handler(Looper.getMainLooper());
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateSystemTime();
                timeHandler.postDelayed(this, 1000); // Update every second
            }
        };

        timeHandler.post(timeRunnable); // Start updating time
    }

    private void saveCityName(String cityName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CITY_NAME, cityName);
        editor.apply();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            getAddress(latitude, longitude);
                            fetchWeatherData(latitude, longitude);
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void getAddress(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                addressTextView.setText("Current Address: " + address.getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fetchWeatherData(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        String apiKey = "5ec5b6390b700e5194cd8420cbef9037"; // Replace with your actual API key
        Call<OneCallResponse> call = service.getCurrentWeather(latitude, longitude, "minutely,hourly,daily,alerts", apiKey, "metric");

        call.enqueue(new Callback<OneCallResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<OneCallResponse> call, @NonNull Response<OneCallResponse> response) {
                if (response.isSuccessful()) {
                    OneCallResponse oneCallResponse = response.body();
                    if (oneCallResponse != null) {
                        OneCallResponse.Current current = oneCallResponse.getCurrent();
                        weatherInfoTextView.setText("Temp: " + current.getTemp() +
                                "°C\nHumidity: " + current.getHumidity() + "%" +
                                "\nDescription: " + current.getWeather().get(0).getDescription());
                    } else {
                        weatherInfoTextView.setText("No weather data available");
                        Log.e(TAG, "Weather response is null");
                    }
                } else {
                    weatherInfoTextView.setText("Response not successful: " + response.message());
                    Log.e(TAG, "Response not successful: " + response.message());
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<OneCallResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                weatherInfoTextView.setText("Failed to retrieve weather data: " + t.getMessage());
                Log.e(TAG, "Failed to retrieve weather data", t);
            }
        });
    }

    private void fetchWeatherDataByCityName(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        String apiKey = "5ec5b6390b700e5194cd8420cbef9037"; // Replace with your actual API key
        Call<WeatherResponse> call = service.getWeatherByCityName(cityName, apiKey, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        WeatherResponse.Main main = weatherResponse.getMain();
                        WeatherResponse.Weather weather = weatherResponse.getWeather().get(0);
                        weatherInfoTextView.setText("Temp: " + main.getTemp() +
                                "°C\nHumidity: " + main.getHumidity() + "%" +
                                "\nDescription: " + weather.getDescription());
                    } else {
                        weatherInfoTextView.setText("No weather data available");
                        Log.e(TAG, "Weather response is null");
                    }
                } else {
                    weatherInfoTextView.setText("Response not successful: " + response.message());
                    Log.e(TAG, "Response not successful: " + response.message());
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
                weatherInfoTextView.setText("Failed to retrieve weather data: " + t.getMessage());
                Log.e(TAG, "Failed to retrieve weather data", t);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateSystemTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = dateFormat.format(new Date());
        systemTimeTextView.setText("System Time: " + currentTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(timeRunnable); // Stop updating time when activity is destroyed
    }
}
