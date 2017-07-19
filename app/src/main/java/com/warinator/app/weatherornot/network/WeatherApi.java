package com.warinator.app.weatherornot.network;

import com.warinator.app.weatherornot.model.pojo.CurrentWeather;
import com.warinator.app.weatherornot.model.pojo.WeatherForecast;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Warinator on 10.07.2017.
 */

public interface WeatherApi {
    String CURRENT_WEATHER = "weather?units=metric&lang=ru";
    String FORECAST = "forecast?units=metric&lang=ru";

    @GET(CURRENT_WEATHER)
    Observable<CurrentWeather> getCurrent(@Query("id") int cityId, @Query("APPID") String apiKey);

    @GET(FORECAST)
    Observable<WeatherForecast> getForecast(@Query("id") int cityId, @Query("APPID") String apiKey);

}
