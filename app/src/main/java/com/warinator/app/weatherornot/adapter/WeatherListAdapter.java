package com.warinator.app.weatherornot.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.warinator.app.weatherornot.GlideApp;
import com.warinator.app.weatherornot.R;
import com.warinator.app.weatherornot.model.WeatherConditions;
import com.warinator.app.weatherornot.util.FormatUtil;
import com.warinator.app.weatherornot.util.Util;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Warinator on 11.07.2017.
 */

public class WeatherListAdapter extends RecyclerView.Adapter<WeatherListAdapter.ViewHolder> {

    private List<WeatherConditions> mWeatherList;
    private Context mContext;

    public WeatherListAdapter(Context context, List<WeatherConditions> weatherList){
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
        WeatherConditions weather = mWeatherList.get(position);
        Date date = new Date(weather.getDt()*1000);
        //holder.laWeatherItem.setBackgroundColor(Util.getTimeOfDayColor(date, mContext));
        holder.tvTime.setTextColor(Util.getTimeOfDayColor(date, mContext));
        holder.tvDate.setText(FormatUtil.getFormattedDate(date, mContext));
        holder.tvTime.setText(FormatUtil.getFormattedTime(date));
        holder.tvDescription.setText(weather.getWeather().get(0).getDescription());
        holder.tvTemperature.setText(FormatUtil
                .getFormattedTemperature(weather.getMain().getTemp()));
        int iconResId = Util.getIconResId(weather.getWeather().get(0).getIcon(),mContext);
        GlideApp.with(mContext)
                .load(iconResId)
                .transition(withCrossFade())
                .into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return mWeatherList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_date)
        TextView tvDate;
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_weather_descr)
        TextView tvDescription;
        @BindView(R.id.tv_weather_deg)
        TextView tvTemperature;
        @BindView(R.id.iv_weather_icon)
        ImageView ivIcon;
        @BindView(R.id.la_weather_item)
        RelativeLayout laWeatherItem;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
