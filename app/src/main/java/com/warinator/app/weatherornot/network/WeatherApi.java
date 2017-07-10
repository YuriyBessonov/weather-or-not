package com.warinator.app.weatherornot.network;

import com.warinator.app.weatherornot.model.CurrentWeather;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Warinator on 10.07.2017.
 */

public interface WeatherApi {
    String CURRENT_WEATHER = "weather?units=metric&lang=ru&APPID=ee149d0c39bbb291d9b5e1ce23a16301";

    @GET(CURRENT_WEATHER)
    Observable<CurrentWeather> getCurrent(@Query("q") String city);
}
