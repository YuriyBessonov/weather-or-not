package com.warinator.app.weatherornot.network;

import com.warinator.app.weatherornot.model.pojo.CurrentWeather;
import com.warinator.app.weatherornot.model.pojo.WeatherForecast;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API погоды
 */
public interface WeatherApi {
    String CURRENT_WEATHER = "weather?units=metric&lang=ru";
    String FORECAST = "forecast?units=metric&lang=ru";

    @GET(CURRENT_WEATHER)//Получить текущую погоду в указанном городе
    Observable<CurrentWeather> getCurrent(@Query("id") int cityId, @Query("APPID") String apiKey);

    @GET(FORECAST)//Получить прогноз погоды для указанного города через каждые 3 часа на 5 дней
    Observable<WeatherForecast> getForecast(@Query("id") int cityId, @Query("APPID") String apiKey);

}
