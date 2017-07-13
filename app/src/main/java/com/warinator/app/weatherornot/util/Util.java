package com.warinator.app.weatherornot.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.warinator.app.weatherornot.R;

import java.util.Calendar;
import java.util.Date;

import xyz.matteobattilana.library.Common.Constants;

/**
 * Created by Warinator on 10.07.2017.
 */

public class Util {
    private Util(){}

    public static int getIconResId(String iconName, Context context) {
       return context.getResources().getIdentifier(String.format("_%s", iconName),
               "drawable", context.getPackageName());
    }

    public static int getBgrResId(String bgrName, Context context) {
        return context.getResources().getIdentifier(String.format("b%s", bgrName),
                "drawable", context.getPackageName());
    }

    public static boolean dateIsToday(Date date){
        if (date == null){
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar today = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && calendar.get(Calendar.DATE) == today.get(Calendar.DATE)){
            return true;
        }
        return false;
    }



    public static int getTimeOfDayColor(Date date, Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 6){
            return ContextCompat.getColor(context, R.color.colorNight);
        }
        else if (hour >= 6 && hour < 12){
            return ContextCompat.getColor(context, R.color.colorMorning);
        }
        else if (hour >= 12 && hour < 18 ){
            return ContextCompat.getColor(context, R.color.colorDay);
        }
        else {
            return ContextCompat.getColor(context, R.color.colorEvening);
        }
    }

    public static float hPaToMmHg(float hPa){
        return hPa*0.75006f;
    }

    public static Constants.weatherStatus getWeatherStatus(int weatherCode){
        int code = weatherCode / 100;
        if (code == 2 || code == 3 || code == 5){
            return Constants.weatherStatus.RAIN;
        }
        else if (code == 6){
            return Constants.weatherStatus.SNOW;
        }
        else {
            return Constants.weatherStatus.SUN;
        }
    }
}
