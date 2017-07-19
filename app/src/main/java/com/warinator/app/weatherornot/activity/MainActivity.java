package com.warinator.app.weatherornot.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.warinator.app.weatherornot.GlideApp;
import com.warinator.app.weatherornot.R;
import com.warinator.app.weatherornot.adapter.WeatherListAdapter;
import com.warinator.app.weatherornot.dao.StoredWeatherDAO;
import com.warinator.app.weatherornot.model.pojo.CurrentWeather;
import com.warinator.app.weatherornot.model.pojo.WeatherForecast;
import com.warinator.app.weatherornot.model.realm_model.StoredWeather;
import com.warinator.app.weatherornot.network.RetrofitClient;
import com.warinator.app.weatherornot.util.FormatUtil;
import com.warinator.app.weatherornot.util.PrefUtil;
import com.warinator.app.weatherornot.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import xyz.matteobattilana.library.Common.Constants;
import xyz.matteobattilana.library.WeatherView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.warinator.app.weatherornot.util.PrefUtil.DEFAULT_CITY_ID;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int NETWORK_TIMEOUT = 15;//сек.
    private static final int REQUEST_CODE_LOCATION = 1;//сек.

    @BindView(R.id.tb_main)
    Toolbar mToolbar;
    @BindView(R.id.la_collapsing_toolbar)
    CollapsingToolbarLayout mToolbarLayout;
    @BindView(R.id.la_app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.tv_weather_descr)
    TextView tvWeatherDescr;
    @BindView(R.id.tv_weather_deg)
    TextView tvWeatherDeg;
    @BindView(R.id.tv_location)
    TextView tvLocation;
    @BindView(R.id.tv_updated)
    TextView tvUpdated;
    @BindView(R.id.iv_weather_icon)
    ImageView ivWeatherIcon;
    @BindView(R.id.iv_toolbar_bgr)
    ImageView ivToolbarBgr;
    @BindView(R.id.rv_weather_list)
    RecyclerView rvWeatherList;
    @BindView(R.id.weather_view)
    WeatherView weatherView;
    @BindView(R.id.tv_toolbar_title)
    TextView tvToolbarTitle;
    @BindView(R.id.la_swipe_refresh)
    SwipeRefreshLayout laSwipeRefresh;

    private Disposable mWeatherDisposable;
    private String mTitle = " ";
    private WeatherListAdapter mWeatherListAdapter;
    private List<StoredWeather> mForecast;
    private int mCityID;
    private String mCityName = "";
    private StoredWeatherDAO mStoredWeatherDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");
        tvToolbarTitle.setVisibility(View.INVISIBLE);

        Realm.init(this);

        mStoredWeatherDAO = new StoredWeatherDAO();

        mCityID = new PrefUtil(this).getCityID();

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private boolean mIsTitleShow = false;
            private int mScrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset >= 0){
                    laSwipeRefresh.setEnabled(true);
                }
                else if (!laSwipeRefresh.isRefreshing())
                {
                    laSwipeRefresh.setEnabled(false);
                }
                if (mScrollRange == -1) {
                    mScrollRange = appBarLayout.getTotalScrollRange();
                }
                if (mScrollRange + verticalOffset == 0) {
                    tvToolbarTitle.setVisibility(View.VISIBLE);
                    mIsTitleShow = true;
                } else if(mIsTitleShow) {
                    tvToolbarTitle.setVisibility(View.INVISIBLE);
                    mIsTitleShow = false;
                }
            }
        });

        rvWeatherList.setLayoutManager(new LinearLayoutManager(this));
        mForecast = new ArrayList<>();
        mWeatherListAdapter = new WeatherListAdapter(this,mForecast);
        rvWeatherList.setAdapter(mWeatherListAdapter);
        rvWeatherList.setItemAnimator(new DefaultItemAnimator());

        laSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWeather();
            }
        });

        mWeatherDisposable = new CompositeDisposable();
        refreshWeather();

    }

    @Override
    protected void onDestroy() {
        if (mWeatherDisposable != null && !mWeatherDisposable.isDisposed()){
            mWeatherDisposable.dispose();
        }
        super.onDestroy();
    }

    @OnClick(R.id.iv_refresh)
    public void startRefreshing(){
        laSwipeRefresh.setEnabled(true);
        laSwipeRefresh.setRefreshing(true);
        refreshWeather();
    }

    @OnClick(R.id.iv_location)
    public void pickLocation(){
        Intent intent = LocationActivity.newIntent(this, mCityName);
        startActivityForResult(intent, REQUEST_CODE_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        int newCityId = data.getIntExtra(LocationActivity.RESULT_CITY_ID, DEFAULT_CITY_ID);
        if (mCityID != newCityId){
            mCityID = newCityId;
            new PrefUtil(this).setCityID(mCityID);
            startRefreshing();
        }
    }

    public void saveWeather(StoredWeather currentWeather){
        mStoredWeatherDAO.storeWeather(currentWeather);
    }

    public void saveWeather(List<StoredWeather> forecast){
        mStoredWeatherDAO.storeForecast(forecast);
    }

    public void restoreWeather(){
        Observable.fromCallable(new Callable<StoredWeather>() {
            @Override
            public StoredWeather call() throws Exception {
                return mStoredWeatherDAO.getWeather();
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .concatMap(new Function<StoredWeather, Observable<List<StoredWeather>>>() {
            @Override
            public Observable<List<StoredWeather>> apply(@NonNull StoredWeather current) throws Exception {
                List<StoredWeather> allWeather = new ArrayList<>();
                allWeather.add(current);
                allWeather.addAll(mStoredWeatherDAO.getForecast());
                return Observable.just(allWeather);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<List<StoredWeather>>() {
            @Override
            public void onNext(@NonNull List<StoredWeather> allWeather) {
                populateCurrentWeather(allWeather.get(0));
                populateForecast(allWeather.subList(1, allWeather.size()));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e(getString(R.string.app_name), getClass().getSimpleName(), e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    public void populateCurrentWeather(StoredWeather currentWeather){
        String temperature = FormatUtil
                .getFormattedTemperature(currentWeather.getTemperature());
        String conditions = currentWeather.getDescription();
        tvWeatherDescr.setText(conditions);
        tvWeatherDeg.setText(temperature);
        mCityName = currentWeather.getCityName();
        tvLocation.setText(mCityName);
        tvUpdated.setText(FormatUtil
                .getFormattedTime(new Date(currentWeather.getDateTime())));

        int iconResId = Util.getIconResId(
                currentWeather.getIcon(), MainActivity.this);
        GlideApp.with(MainActivity.this)
                .load(iconResId)
                .transition(withCrossFade())
                .into(ivWeatherIcon);

        int bgrResId = Util.getBgrResId(currentWeather.getIcon(),
                MainActivity.this);
        GlideApp.with(MainActivity.this)
                .load(bgrResId)
                .transition(withCrossFade())
                .into(ivToolbarBgr);

        mTitle = String.format(Locale.getDefault(), "%s, %s", temperature, conditions);
        tvToolbarTitle.setText(mTitle);
        Constants.weatherStatus status = Util.
                getWeatherStatus(currentWeather.getWeatherCode());
        weatherView.setWeather(status);
        if (status == Constants.weatherStatus.SUN){
            weatherView.stopAnimation();
        }
        else {
            weatherView.startAnimation();
        }
    }

    public void populateForecast(List<StoredWeather> forecast){
        laSwipeRefresh.setRefreshing(false);
        mForecast.clear();
        mForecast.addAll(forecast);
        mWeatherListAdapter.notifyDataSetChanged();
    }

    public void refreshWeather(){
        Observable<CurrentWeather> currentWeatherObs =
                RetrofitClient.getWeatherApi().getCurrent(mCityID);
        if (isNetworkConnected()){
            if (mWeatherDisposable != null){
                mWeatherDisposable.dispose();
            }
            mWeatherDisposable = currentWeatherObs
                    .observeOn(Schedulers.io())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .concatMap(new Function<CurrentWeather, Observable<StoredWeather>>() {
                        @Override
                        public Observable<StoredWeather> apply(@NonNull CurrentWeather currentWeather)
                                throws Exception {
                            StoredWeather current = StoredWeather.fromCurrentWeather(currentWeather);
                            saveWeather(current);
                            return Observable.just(current);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .concatMap(new Function<StoredWeather, Observable<WeatherForecast>>() {
                        @Override
                        public Observable<WeatherForecast> apply(@NonNull StoredWeather current)
                                throws Exception {
                            populateCurrentWeather(current);
                            return RetrofitClient.getWeatherApi().getForecast(mCityID);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .observeOn(Schedulers.io())
                    .concatMap(new Function<WeatherForecast, Observable<List<StoredWeather>>>() {
                        @Override
                        public Observable<List<StoredWeather>> apply(@NonNull WeatherForecast weatherForecast) throws Exception {
                            List<StoredWeather> forecast = StoredWeather.fromWeatherForecast(weatherForecast);
                            saveWeather(forecast);
                            return Observable.just(forecast);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<List<StoredWeather>>(){
                        @Override
                        public void onNext(@NonNull List<StoredWeather> forecast) {
                            populateForecast(forecast);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            laSwipeRefresh.setRefreshing(false);
                            Toast.makeText(MainActivity.this,"Не удалось обновить погоду", Toast.LENGTH_SHORT).show();
                            Log.e(getString(R.string.app_name), getClass().getSimpleName(), e);
                            restoreWeather();
                        }

                        @Override
                        public void onComplete() {}
                    });
        }
        else {
            laSwipeRefresh.setRefreshing(false);
            Toast.makeText(MainActivity.this,
                    R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
            restoreWeather();
        }
    }

    public void refreshWeatherOld(){
        Observable<CurrentWeather> currentWeatherObs =
                RetrofitClient.getWeatherApi().getCurrent(mCityID);
        if (isNetworkConnected()){
            if (mWeatherDisposable != null){
                mWeatherDisposable.dispose();
            }
            mWeatherDisposable = currentWeatherObs
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .concatMap(new Function<CurrentWeather, Observable<WeatherForecast>>() {
                        @Override
                        public Observable<WeatherForecast> apply(@NonNull CurrentWeather currentWeather) throws Exception {
                            //TODO: сохранить погоду в БД


                            return RetrofitClient.getWeatherApi().getForecast(mCityID);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<WeatherForecast>(){
                        @Override
                        public void onNext(@NonNull WeatherForecast forecast) {

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            laSwipeRefresh.setRefreshing(false);
                            Toast.makeText(MainActivity.this,"Не удалось обновить погоду", Toast.LENGTH_SHORT).show();
                            Log.e(getString(R.string.app_name), "", e);
                        }

                        @Override
                        public void onComplete() {}
                    });
        }
        else {
            laSwipeRefresh.setRefreshing(false);
            Toast.makeText(MainActivity.this,
                    R.string.not_connected_to_internet, Toast.LENGTH_SHORT).show();
            //TODO: загрузить погоду из БД
        }

    }

    //проверить наличие подключения к интернету
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    @Override
    public void onRefresh() {
        refreshWeather();
    }

}
