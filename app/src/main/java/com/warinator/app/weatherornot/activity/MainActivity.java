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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.warinator.app.weatherornot.GlideApp;
import com.warinator.app.weatherornot.R;
import com.warinator.app.weatherornot.adapter.WeatherListAdapter;
import com.warinator.app.weatherornot.model.CurrentWeather;
import com.warinator.app.weatherornot.model.WeatherConditions;
import com.warinator.app.weatherornot.model.WeatherForecast;
import com.warinator.app.weatherornot.network.RetrofitClient;
import com.warinator.app.weatherornot.util.FormatUtil;
import com.warinator.app.weatherornot.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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
import xyz.matteobattilana.library.Common.Constants;
import xyz.matteobattilana.library.WeatherView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int NETWORK_TIMEOUT = 15;//сек.
    private static final int REQUEST_CODE_LOCATION = 1;//сек.
    private static final int DEFAULT_CITY_ID = 1496747;//Новосибирск

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
    private List<WeatherConditions> mWeatherConditionsList;
    private int mCityID = DEFAULT_CITY_ID;
    private String mCityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");
        tvToolbarTitle.setVisibility(View.INVISIBLE);

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
        mWeatherConditionsList = new ArrayList<>();
        mWeatherListAdapter = new WeatherListAdapter(this,mWeatherConditionsList);
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
        mCityID = data.getIntExtra(LocationActivity.RESULT_CITY_ID,
                DEFAULT_CITY_ID);
        startRefreshing();
    }

    public void refreshWeather(){
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
                            //TODO: изменить дату обновления
                            //TODO: сохранить погоду в БД
                            String temperature = FormatUtil
                                    .getFormattedTemperature(currentWeather.getMain().getTemp());
                            String conditions = currentWeather.getWeather().get(0).getDescription();
                            tvWeatherDescr.setText(conditions);
                            tvWeatherDeg.setText(temperature);
                            mCityName = currentWeather.getName();
                            tvLocation.setText(mCityName);
                            tvUpdated.setText(FormatUtil
                                    .getFormattedTime(Calendar.getInstance().getTime()));

                            int iconResId = Util.getIconResId(
                                    currentWeather.getWeather().get(0).getIcon(), MainActivity.this);
                            GlideApp.with(MainActivity.this)
                                    .load(iconResId)
                                    .transition(withCrossFade())
                                    .into(ivWeatherIcon);

                            int bgrResId = Util.getBgrResId(currentWeather.getWeather().get(0).getIcon(),
                                    MainActivity.this);
                            GlideApp.with(MainActivity.this)
                                    .load(bgrResId)
                                    .transition(withCrossFade())
                                    .into(ivToolbarBgr);

                            mTitle = String.format(Locale.getDefault(),"%s, %s", temperature, conditions);
                            tvToolbarTitle.setText(mTitle);
                            Constants.weatherStatus status = Util.
                                    getWeatherStatus(currentWeather.getWeather().get(0).getId());
                            weatherView.setWeather(status);
                            if (status == Constants.weatherStatus.SUN){
                                weatherView.stopAnimation();
                            }
                            else {
                                weatherView.startAnimation();
                            }

                            return RetrofitClient.getWeatherApi().getForecast(mCityID);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .subscribeWith(new DisposableObserver<WeatherForecast>(){
                        @Override
                        public void onNext(@NonNull WeatherForecast forecast) {
                            laSwipeRefresh.setRefreshing(false);
                            mWeatherConditionsList.clear();
                            mWeatherConditionsList.addAll(forecast.getList());
                            mWeatherListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            laSwipeRefresh.setRefreshing(false);
                            Toast.makeText(MainActivity.this,"Не удалось обновить погоду", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                        }
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
