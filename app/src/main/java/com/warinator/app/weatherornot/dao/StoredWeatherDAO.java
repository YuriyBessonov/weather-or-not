package com.warinator.app.weatherornot.dao;

import com.warinator.app.weatherornot.model.realm_model.StoredWeather;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.warinator.app.weatherornot.model.realm_model.StoredWeather.ID_CURRENT_WEATHER;

/**
 * Created by Warinator on 18.07.2017.
 */

public class StoredWeatherDAO {

    public void storeForecast(final List<StoredWeather> forecast) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RealmResults<StoredWeather> result =
                    realm1.where(StoredWeather.class)
                            .greaterThan(StoredWeather.ID, ID_CURRENT_WEATHER).findAll();
            result.deleteAllFromRealm();
            realm1.copyToRealm(forecast);
        });
        realm.close();
    }

    public void storeWeather(final StoredWeather currentWeather) {
        if (currentWeather.getId() != ID_CURRENT_WEATHER) {
            throw new IllegalArgumentException(
                    "У сохраняемой текущей погоды id должен быть равен " + ID_CURRENT_WEATHER);
        }
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(currentWeather));
        realm.close();
    }

    public List<StoredWeather> getForecast() {
        Realm realm = Realm.getDefaultInstance();
        List<StoredWeather> weatherList =
                realm.copyFromRealm(realm.where(StoredWeather.class)
                        .greaterThan(StoredWeather.ID, ID_CURRENT_WEATHER).findAll());
        realm.close();
        return weatherList;
    }

    public StoredWeather getWeather() {
        Realm realm = Realm.getDefaultInstance();
        StoredWeather currentWeather =
                realm.copyFromRealm(realm.where(StoredWeather.class)
                        .equalTo(StoredWeather.ID, ID_CURRENT_WEATHER).findAll().first());
        realm.close();
        return currentWeather;
    }
}
