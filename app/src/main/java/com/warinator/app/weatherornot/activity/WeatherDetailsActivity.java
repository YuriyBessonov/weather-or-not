package com.warinator.app.weatherornot.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warinator.app.weatherornot.R;
import com.warinator.app.weatherornot.util.FormatUtil;
import com.warinator.app.weatherornot.util.Util;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.matteobattilana.library.Common.Constants;
import xyz.matteobattilana.library.WeatherView;

public class WeatherDetailsActivity extends AppCompatActivity {

    private static final String ARG_DATE_TIME = "date_time";
    private static final String ARG_TEMPERATURE = "temperature";
    private static final String ARG_WIND_DEGREES = "wind_degrees";
    private static final String ARG_WIND_SPEED = "wind_speed";
    private static final String ARG_ICON = "icon";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_HUMIDITY = "humidity";
    private static final String ARG_PRESSURE = "pressure";
    private static final String ARG_WEATHER_CODE = "weather_code";
    private static final String ARG_TRANSITION_NAME = "transition_name";

    @BindView(R.id.tb_weather_details)
    Toolbar toolbar;
    @BindView(R.id.iv_weather_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_time)
    TextView tvTime;
    @BindView(R.id.tv_date)
    TextView tvDate;
    @BindView(R.id.tv_weather_deg)
    TextView tvTemperature;
    @BindView(R.id.tv_weather_descr)
    TextView tvDescription;
    @BindView(R.id.tv_wind)
    TextView tvWind;
    @BindView(R.id.tv_humidity)
    TextView tvHumidity;
    @BindView(R.id.tv_pressure)
    TextView tvPressure;
    @BindView(R.id.la_root)
    RelativeLayout laRoot;

    @BindView(R.id.la_weather_main)
    RelativeLayout laWeatherMain;
    @BindView(R.id.cv_weather_details)
    CardView cvWeatherDetails;
    @BindView(R.id.weather_view)
    WeatherView weatherView;

    private String mTransitonName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.forecast_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTransitonName = bundle.getString(ARG_TRANSITION_NAME);
                ivIcon.setTransitionName(mTransitonName);
            }
            populate(bundle);
        }
        setupAnimations();
    }

    private void populate(Bundle bundle){
        ivIcon.setImageResource(bundle.getInt(ARG_ICON));
        Date date = new Date( bundle.getLong(ARG_DATE_TIME)
                *TimeUnit.SECONDS.toMillis(1));
        tvTime.setText(FormatUtil.getFormattedTime(date));
        tvDate.setText(FormatUtil.getFormattedDate(date, this));
        tvDescription.setText(bundle.getString(ARG_DESCRIPTION));
        tvTemperature.setText(FormatUtil.getFormattedTemperature(
                bundle.getFloat(ARG_TEMPERATURE)));

        String windDirection = FormatUtil
                .getWindDirection(bundle.getFloat(ARG_WIND_DEGREES),this);
        int windSpeed = Math.round(bundle.getFloat(ARG_WIND_SPEED));
        tvWind.setText(String.format(Locale.getDefault(), getString(R.string.format_wind),
                windDirection, windSpeed));
        int humidity = Math.round(bundle.getFloat(ARG_HUMIDITY));
        tvHumidity.setText(String.format(Locale.getDefault(),
                getString(R.string.format_percent_int), humidity ));
        int pressure = Math.round(Util.hPaToMmHg(bundle.getFloat(ARG_PRESSURE)));
        tvPressure.setText(String.format(Locale.getDefault(),
                    getString(R.string.format_pressure),pressure));
        Constants.weatherStatus status =
                Util.getWeatherStatus(bundle.getInt(ARG_WEATHER_CODE));
        weatherView.setWeather(status);
        if (status == Constants.weatherStatus.SUN){
            weatherView.stopAnimation();
        }
        else {
            weatherView.startAnimation();
        }

    }

    private void setupAnimations(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide(Gravity.BOTTOM);
            slide.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(slide);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class IntentBuilder {
        private Intent mIntent;

        public IntentBuilder(Context context, long dateTimeMs, float temperature){
            mIntent = new Intent(context, WeatherDetailsActivity.class);
            mIntent.putExtra(ARG_DATE_TIME, dateTimeMs);
            mIntent.putExtra(ARG_TEMPERATURE, temperature);
        }

        public IntentBuilder windDegrees(float degrees){
            mIntent.putExtra(ARG_WIND_DEGREES, degrees);
            return this;
        }

        public IntentBuilder windSpeed(float speed){
            mIntent.putExtra(ARG_WIND_SPEED, speed);
            return this;
        }

        public IntentBuilder icon(int iconRes){
            mIntent.putExtra(ARG_ICON, iconRes);
            return this;
        }

        public IntentBuilder description(String description){
            mIntent.putExtra(ARG_DESCRIPTION, description);
            return this;
        }

        public IntentBuilder humidity(float humidity){
            mIntent.putExtra(ARG_HUMIDITY, humidity);
            return this;
        }

        public IntentBuilder pressure(float pressure){
            mIntent.putExtra(ARG_PRESSURE, pressure);
            return this;
        }

        public IntentBuilder transitionName(String transitionName){
            mIntent.putExtra(ARG_TRANSITION_NAME, transitionName);
            return this;
        }

        public IntentBuilder weatherCode(int weatherCode){
            mIntent.putExtra(ARG_WEATHER_CODE, weatherCode);
            return this;
        }

        public Intent build(){
            return mIntent;
        }
    }
}
