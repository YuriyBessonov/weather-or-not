package com.warinator.app.weatherornot.model.realm_model;

import com.warinator.app.weatherornot.model.pojo.CurrentWeather;
import com.warinator.app.weatherornot.model.pojo.WeatherConditions;
import com.warinator.app.weatherornot.model.pojo.WeatherForecast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Warinator on 16.07.2017.
 */

public class StoredWeather extends RealmObject {
    public static final String ID = "id";
    public static final int ID_CURRENT_WEATHER = 0;
    @PrimaryKey
    private int id;
    private String mCityName;
    private long mDateTime;
    private float mTemperature;
    private int mWeatherCode;
    private String mDescription;
    private String mIcon;
    private float mWindDegrees;
    private float mWindSpeed;
    private float mPressure;
    private float mHumidity;

    public static StoredWeather fromCurrentWeather(CurrentWeather currentWeather) {
        StoredWeather weather = new StoredWeather();
        weather.setId(ID_CURRENT_WEATHER);
        weather.setCityName(currentWeather.getName());
        weather.setDateTime(currentWeather.getDt() *
                TimeUnit.SECONDS.toMillis(1));
        weather.setTemperature(currentWeather.getMain().getTemp());
        weather.setDescription(currentWeather.getWeather().get(0).getDescription());
        weather.setIcon(currentWeather.getWeather().get(0).getIcon());
        weather.setWeatherCode(currentWeather.getWeather().get(0).getId());
        return weather;
    }

    public static List<StoredWeather> fromWeatherForecast(WeatherForecast forecast) {
        List<WeatherConditions> conditionsList = forecast.getList();
        List<StoredWeather> storedForecast = new ArrayList<>();
        int i = 1;
        for (WeatherConditions conditions : conditionsList) {
            StoredWeather weather = new StoredWeather();
            weather.setId(i++);
            weather.setCityName(forecast.getCity().getName());
            weather.setDateTime(conditions.getDt() *
                    TimeUnit.SECONDS.toMillis(1));
            weather.setTemperature(conditions.getMain().getTemp());
            weather.setDescription(conditions.getWeather().get(0).getDescription());
            weather.setIcon(conditions.getWeather().get(0).getIcon());
            weather.setWeatherCode(conditions.getWeather().get(0).getId());
            weather.setWindDegrees(conditions.getWind().getDeg());
            weather.setWindSpeed(conditions.getWind().getSpeed());
            weather.setPressure(conditions.getMain().getPressure());
            weather.setHumidity(conditions.getMain().getHumidity());
            storedForecast.add(weather);
        }
        return storedForecast;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }

    public long getDateTime() {
        return mDateTime;
    }

    public void setDateTime(long dateTime) {
        mDateTime = dateTime;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public void setTemperature(float temperature) {
        mTemperature = temperature;
    }

    public int getWeatherCode() {
        return mWeatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        mWeatherCode = weatherCode;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getIcon() {
        return mIcon;
    }

    public void setIcon(String icon) {
        mIcon = icon;
    }

    public float getWindDegrees() {
        return mWindDegrees;
    }

    public void setWindDegrees(float windDegrees) {
        mWindDegrees = windDegrees;
    }

    public float getWindSpeed() {
        return mWindSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        mWindSpeed = windSpeed;
    }

    public float getPressure() {
        return mPressure;
    }

    public void setPressure(float pressure) {
        this.mPressure = pressure;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public void setHumidity(float humidity) {
        mHumidity = humidity;
    }
}
