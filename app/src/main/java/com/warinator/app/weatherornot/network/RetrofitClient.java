package com.warinator.app.weatherornot.network;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Warinator on 10.07.2017.
 */

public class RetrofitClient {
    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static WeatherApi weatherApi;

    public static WeatherApi getWeatherApi(){
        if (weatherApi == null){
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .build();
           weatherApi = retrofit.create(WeatherApi.class);
        }
        return weatherApi;
    }

}
