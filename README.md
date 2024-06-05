# Weather Application 2

## Overview
This is a simple weather application that allows users to input a city name and fetch its current weather information. The application displays the temperature, humidity, and weather description.


## ✨ Features

- Input city name to fetch weather data
- Display geo-coded address of the current location
- Display current system time
- Display weather information including temperature, humidity, and weather description
- Save the last searched city name using SharedPreferences


## ⚙️ Installation

1. Clone the repository:

- git clone https://github.com/Dulara-Kavindu/WeatherApp2

2. Open the project through Android Studio.

3. Configure API Key:

- Open MainActivity.java.
- Find the line where the API key is required: Call<OneCallResponse> call = service.getCurrentWeather(latitude, longitude, "minutely,hourly,daily,alerts", "YOUR_API_KEY”, "metric");
- Replace "YOUR_API_KEY" with your actual OpenWeatherMap API key.

4.Sync Project with Gradle Files:

- Go to File > Sync Project with Gradle Files.
- Run the Application

5.The application requires the following permissions:

- ACCESS_FINE_LOCATION: To get the current location of the device.
- INTERNET: To fetch weather data from the OpenWeatherMap API.
- ACCESS_NETWORK_STATE: To check network connectivity.

## 🚀 Usage

- Open the app on your device.
- Enter city name in textbox
- Press the "Fetch Weather" button to get the current weather information for the city you entered.
- The app will display the temperature, humidity, and weather information.
