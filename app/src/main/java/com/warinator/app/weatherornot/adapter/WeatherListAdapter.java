package com.warinator.app.weatherornot.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warinator.app.weatherornot.GlideApp;
import com.warinator.app.weatherornot.R;
import com.warinator.app.weatherornot.activity.WeatherDetailsActivity;
import com.warinator.app.weatherornot.model.realm_model.StoredWeather;
import com.warinator.app.weatherornot.util.FormatUtil;
import com.warinator.app.weatherornot.util.Util;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * Адаптер прогноза погоды
 */
public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {

    private List<StoredWeather> mWeatherList;
    private Context mContext;

    public WeatherListAdapter(Context context, List<StoredWeather> weatherList) {
        mWeatherList = weatherList;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StoredWeather weather = mWeatherList.get(position);
        Date date = new Date(weather.getDateTime());

        int timeColor = R.color.colorGreyDark;
        switch (Util.getTimeOfDay(date)) {
            case MORNING:
                timeColor = R.color.colorMorning;
                break;
            case AFTERNOON:
                timeColor = R.color.colorAfternoon;
                break;
            case EVENING:
                timeColor = R.color.colorEvening;
                break;
            case NIGHT:
                timeColor = R.color.colorNight;
                break;
        }

        timeColor = ContextCompat.getColor(mContext, timeColor);
        holder.tvTime.setTextColor(timeColor);
        holder.tvDate.setText(FormatUtil.getFormattedDate(date, mContext));
        holder.tvTime.setText(FormatUtil.getFormattedTime(date));
        holder.tvTemperature.setText(FormatUtil
                .getFormattedTemperature(weather.getTemperature()));
        int iconResId = Util.getIconResId(weather.getIcon(), mContext);
        GlideApp.with(mContext)
                .load(iconResId)
                .transition(withCrossFade())
                .into(holder.ivIcon);
        holder.mIconResId = iconResId;

        String transitionName = String.format(Locale.getDefault(),
                "_%s%d", weather.getIcon(), position);
        ViewCompat.setTransitionName(holder.ivIcon, transitionName);
    }

    @Override
    public int getItemCount() {
        return mWeatherList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_weather_deg)
        TextView tvTemperature;
        @BindView(R.id.iv_weather_icon)
        ImageView ivIcon;
        @BindView(R.id.la_weather_item)
        RelativeLayout laWeatherItem;
        @BindView(R.id.la_card_weather_root)
        FrameLayout laRoot;
        private int mIconResId;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            laWeatherItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            StoredWeather weather = mWeatherList.get(getAdapterPosition());
            WeatherDetailsActivity.IntentBuilder builder = new WeatherDetailsActivity
                    .IntentBuilder(mContext, weather.getDateTime(), weather.getTemperature())
                    .description(weather.getDescription())
                    .humidity(weather.getHumidity())
                    .pressure(weather.getPressure())
                    .windDegrees(weather.getWindDegrees())
                    .windSpeed(weather.getWindSpeed())
                    .weatherCode(weather.getWeatherCode())
                    .icon(mIconResId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.transitionName(ivIcon.getTransitionName());
            }

            Intent intent = builder.build();

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    (Activity) mContext, ivIcon, ViewCompat.getTransitionName(ivIcon));
            mContext.startActivity(intent, options.toBundle());
        }
    }
}
