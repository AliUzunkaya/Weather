package com.example.weather.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class WeatherController {

    @GetMapping("/weather")
    public ResponseEntity<String> getWeather(@RequestParam String city) throws JSONException {
        String apiKey = "0cb2da01df89710c70d599a94e7d3e35";
        String apiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&units=metric&lang=tr";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        // API yanıtını işleyerek bugünden itibaren toplamda 5 günün hava durumu verilerini al
        String weatherData = processFiveDayWeatherData(response);

        return ResponseEntity.ok(weatherData);
    }

    private String processFiveDayWeatherData(String jsonResponse) throws JSONException {
        StringBuilder weatherData = new StringBuilder();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM");
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray weatherList = jsonObject.getJSONArray("list");

        // İlk günü bugün olarak ayarlamak için bugünün tarihini alın
        Date today = new Date();
        String todayFormatted = dateFormat.format(today);

        String currentDay = "";
        double minTemperature = Double.MAX_VALUE;
        double maxTemperature = Double.MIN_VALUE;
        int dayCounter = 0;

        for (int i = 0; i < weatherList.length(); i++) {
            JSONObject weatherInfo = weatherList.getJSONObject(i);
            long timestamp = weatherInfo.getLong("dt") * 1000;
            Date date = new Date(timestamp);
            String formattedDate = dateFormat.format(date);

            // Her yeni gün için bir gün sayacını artırın
            if (!formattedDate.equals(currentDay)) {
                if (!currentDay.isEmpty()) {
                    String data = currentDay + ": Min " + minTemperature + "°C, Max " + maxTemperature + "°C";
                    weatherData.append(data).append("\n");
                    dayCounter++;

                    if (dayCounter >= 5) {
                        break; // Toplamda 5 günü geçtikten sonra döngüden çık
                    }
                }

                currentDay = formattedDate;
                minTemperature = Double.MAX_VALUE;
                maxTemperature = Double.MIN_VALUE;
            }

            JSONObject mainInfo = weatherInfo.getJSONObject("main");
            double temperature = mainInfo.getDouble("temp");
            if (temperature < minTemperature) {
                minTemperature = temperature;
            }
            if (temperature > maxTemperature) {
                maxTemperature = temperature;
            }
        }

        // Son günün verisini ekleyin
        if (!currentDay.isEmpty() && dayCounter < 5) {
            String data = currentDay + ": Min " + minTemperature + "°C, Max " + maxTemperature + "°C";
            weatherData.append(data).append("\n");
        }

        return weatherData.toString();
    }
}
