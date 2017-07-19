package com.warinator.app.weatherornot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.warinator.app.weatherornot.R;
import com.warinator.app.weatherornot.model.pojo.City;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LocationActivity extends AppCompatActivity {

    public static final String RESULT_CITY_ID = "city_id";
    private static final String ARG_CITY_NAME = "city_name";
    @BindView(R.id.lv_cities)
    ListView lvCities;
    @BindView(R.id.et_location)
    TextInputEditText etLocation;
    @BindView(R.id.la_location_input)
    TextInputLayout laLocationInput;
    @BindView(R.id.btn_stop_search)
    Button btnStopSearch;
    private List<City> mCityList;
    private List<String> mCityNamesList;
    private String mCityName;
    private ArrayAdapter mAdapter;
    private Disposable mSearchDisposable;
    private boolean mIsSearching;

    public static Intent newIntent(Context context, String cityName) {
        Intent intent = new Intent(context, LocationActivity.class);
        intent.putExtra(ARG_CITY_NAME, cityName);
        return intent;
    }

    @OnClick(R.id.btn_search)
    void searchCity() {
        String queryName =
                etLocation.getText().toString();
        if (!queryName.isEmpty()) {
            mCityList.clear();
            mCityNamesList.clear();
            mAdapter.notifyDataSetChanged();
            setSearching(true);
            findCity(queryName);
        }
    }

    @OnClick(R.id.btn_back)
    void back() {
        finish();
    }

    private void setSearching(boolean isSearching) {
        mIsSearching = isSearching;
        btnStopSearch.setVisibility(isSearching ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.btn_stop_search)
    public void stopSearching() {
        setSearching(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ButterKnife.bind(this);

        if (getIntent() != null) {
            mCityName = getIntent().getStringExtra(ARG_CITY_NAME);
        }
        if (mCityName == null) {
            mCityName = "";
        }
        etLocation.setText(mCityName);
        mCityList = new ArrayList<>();
        mCityNamesList = new ArrayList<>();

        mAdapter = new ArrayAdapter<>(this, R.layout.item_city, mCityNamesList);
        lvCities.setAdapter(mAdapter);
        lvCities.setOnItemClickListener((parent, view, position, id) -> {
            setSearching(false);
            Intent intent = new Intent();
            intent.putExtra(RESULT_CITY_ID, mCityList.get(position).getId());
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    public void findCity(final String name) {
        mIsSearching = true;
        if (mSearchDisposable != null && !mSearchDisposable.isDisposed()) {
            mSearchDisposable.dispose();
        }
        mSearchDisposable = Observable.create((ObservableOnSubscribe<City>) emitter -> {
            Gson gson = new Gson();
            JsonReader jsonReader;
            InputStream inputStream = getResources().openRawResource(R.raw.cities);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            jsonReader = new JsonReader(reader);
            try {
                jsonReader.beginArray();
                while (jsonReader.hasNext() && mIsSearching) {
                    City city = gson.fromJson(jsonReader, City.class);
                    if (city.getName() != null && city.getName().equals(name)) {
                        emitter.onNext(city);
                    }
                }
                jsonReader.endArray();
                jsonReader.close();
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<City>() {
                    @Override
                    public void onNext(@NonNull City city) {
                        mCityList.add(city);
                        mCityNamesList.add(String.format(Locale.getDefault(),
                                getString(R.string.format_location),
                                city.getName(), city.getCountry(),
                                city.getCoord().getLat(), city.getCoord().getLon()));
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        setSearching(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        setSearching(false);
                        String message = mCityList.isEmpty() ?
                                getString(R.string.no_cities_found_with_such_name) :
                                getString(R.string.searching_completed);
                        Toast.makeText(LocationActivity.this, message,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (mSearchDisposable != null && !mSearchDisposable.isDisposed()) {
            mSearchDisposable.dispose();
        }
        super.onDestroy();
    }
}
