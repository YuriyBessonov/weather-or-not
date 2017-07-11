package com.warinator.app.weatherornot.activity;

import android.content.Context;
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
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final int NETWORK_TIMEOUT = 15;//сек.
    private CompositeDisposable mWeatherDisposable;
    private String mTitle = " ";
    private WeatherListAdapter mWeatherListAdapter;
    private List<WeatherConditions> mWeatherConditionsList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private boolean mIsTitleShow = false;
            private int mScrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (mScrollRange == -1) {
                    mScrollRange = appBarLayout.getTotalScrollRange();
                }
                if (mScrollRange + verticalOffset == 0) {
                    mToolbarLayout.setTitle(mTitle);
                    mIsTitleShow = true;
                } else if(mIsTitleShow) {
                    mToolbarLayout.setTitle(" ");
                    mIsTitleShow = false;
                }
            }
        });

        rvWeatherList.setLayoutManager(new LinearLayoutManager(this));
        mWeatherConditionsList = new ArrayList<>();
        mWeatherListAdapter = new WeatherListAdapter(this,mWeatherConditionsList);
        rvWeatherList.setAdapter(mWeatherListAdapter);
        rvWeatherList.setItemAnimator(new DefaultItemAnimator());

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

    public void refreshWeather(){
        Observable<CurrentWeather> currentWeatherObs = RetrofitClient.getWeatherApi().getCurrent("Novosibirsk");
        Observable<WeatherForecast> forecastObs = RetrofitClient.getWeatherApi().getForecast("Novosibirsk");
        if (isNetworkConnected()){
            if (mWeatherDisposable != null && !mWeatherDisposable.isDisposed()){
                mWeatherDisposable.dispose();
                mWeatherDisposable = new CompositeDisposable();
            }
            mWeatherDisposable.add(
                    currentWeatherObs.observeOn(AndroidSchedulers.mainThread())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .subscribeWith(new DisposableObserver<CurrentWeather>(){
                        @Override
                        public void onNext(@NonNull CurrentWeather currentWeather) {
                            //TODO: изменить дату обновления
                            //TODO: сохранить погоду в БД
                            String temperature = FormatUtil.getFormattedTemperature(currentWeather.getMain().getTemp());
                            String conditions = currentWeather.getWeather().get(0).getDescription();
                            tvWeatherDescr.setText(conditions);
                            tvWeatherDeg.setText(temperature);
                            tvLocation.setText(currentWeather.getName());
                            tvUpdated.setText(FormatUtil.getFormattedTime(Calendar.getInstance().getTime()));

                            int iconResId = Util.getIconResId(currentWeather.getWeather().get(0).getIcon(),
                                    MainActivity.this);
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
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(MainActivity.this,"Не удалось обновить текущую погоду", Toast.LENGTH_SHORT).show();
                            Log.d("WEATHER","NOW",e);
                        }

                        @Override
                        public void onComplete() {}
                    }));

            mWeatherDisposable.add(forecastObs.observeOn(AndroidSchedulers.mainThread())
                    .timeout(NETWORK_TIMEOUT, TimeUnit.SECONDS)
                    .subscribeWith(new DisposableObserver<WeatherForecast>(){
                        @Override
                        public void onNext(@NonNull WeatherForecast forecast) {
                            mWeatherConditionsList.clear();
                            mWeatherConditionsList.addAll(forecast.getList());
                            mWeatherListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(MainActivity.this,"Не удалось обновить прогноз", Toast.LENGTH_SHORT).show();
                            Log.d("WEATHER","FORECAST",e);
                        }

                        @Override
                        public void onComplete() {}
                    }));
        }
        else {
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
