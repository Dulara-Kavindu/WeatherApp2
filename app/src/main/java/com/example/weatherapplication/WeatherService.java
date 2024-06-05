package com.example.weatherapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("data/3.0/onecall")
    Call<OneCallResponse> getCurrentWeather(@Query("lat") double lat,
                                            @Query("lon") double lon,
                                            @Query("exclude") String exclude,
                                            @Query("appid") String apiKey,
                                            @Query("units") String units);

    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeatherByCityName(@Query("q") String cityName,
                                               @Query("appid") String apiKey,
                                               @Query("units") String units);
}
