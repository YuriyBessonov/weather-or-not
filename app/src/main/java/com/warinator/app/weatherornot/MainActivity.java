package com.warinator.app.weatherornot;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.warinator.app.weatherornot.model.CurrentWeather;
import com.warinator.app.weatherornot.network.RetrofitClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    private AppBarLayout mAppBarLayout;
    private Disposable mWeatherDisposable;
    private String mTitle = " ";
    @BindView(R.id.tb_main)
    Toolbar mToolbar;
    @BindView(R.id.la_collapsing_toolbar)
    CollapsingToolbarLayout mToolbarLayout;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");

        mAppBarLayout = (AppBarLayout)findViewById(R.id.la_app_bar_layout);
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

        refreshWeather();
    }

    public void refreshWeather(){
        Observable<CurrentWeather> call = RetrofitClient.getWeatherApi().getCurrent("Novosibirsk");
        if (isNetworkConnected()){
            if (mWeatherDisposable != null && !mWeatherDisposable.isDisposed()){
                mWeatherDisposable.dispose();
            }
            mWeatherDisposable = call.observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<CurrentWeather>(){
                        @Override
                        public void onNext(@NonNull CurrentWeather currentWeather) {
                            //TODO: изменить дату обновления
                            //TODO: сохранить погоду в БД
                            String temperature = String.format(Locale.getDefault(),"%+d",
                                    Math.round(currentWeather.getMain().getTemp()));
                            String conditions = currentWeather.getWeather().get(0).getDescription();
                            tvWeatherDescr.setText(conditions);
                            tvWeatherDeg.setText(temperature);
                            tvLocation.setText(currentWeather.getName());
                            DateFormat timeFormat = new SimpleDateFormat("HH:mm",Locale.getDefault());
                            tvUpdated.setText(timeFormat.format(Calendar.getInstance().getTime()));

                            int iconResId = getResources().getIdentifier(String.format("b%s",
                                    currentWeather.getWeather().get(0).getIcon()), "drawable", getPackageName());
                            ivToolbarBgr.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, iconResId));

                            iconResId = getResources().getIdentifier(String.format("_%s",
                                    currentWeather.getWeather().get(0).getIcon()), "drawable", getPackageName());
                            ivWeatherIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, iconResId));

                            mTitle = String.format(Locale.getDefault(),"%s, %s", temperature, conditions);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(MainActivity.this,"Не удалось обновить погоду", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
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
