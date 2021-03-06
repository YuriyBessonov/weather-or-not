package com.warinator.app.weatherornot.util;

import android.content.Context;

import java.util.Calendar;
import java.util.Date;

import xyz.matteobattilana.library.Common.Constants;

/**
 * Утилитный класс общего назначения
 */
public class Util {
    private Util() {
    }

    //Получить id ресурса иконки погоды по имени иконки
    public static int getIconResId(String iconName, Context context) {
        return context.getResources().getIdentifier(String.format("_%s", iconName),
                "drawable", context.getPackageName());
    }

    //Получить id ресурса фона погоды по имени иконки
    public static int getBgrResId(String iconName, Context context) {
        return context.getResources().getIdentifier(String.format("b%s", iconName),
                "drawable", context.getPackageName());
    }

    //Узнать, является ли дата сегодняшней
    public static boolean dateIsToday(Date date) {
        if (date == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar today = Calendar.getInstance();
        if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                && calendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
            return true;
        }
        return false;
    }

    //Получить время суток
    public static TimeOfDay getTimeOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 6) {
            return TimeOfDay.NIGHT;
        } else if (hour >= 6 && hour < 12) {
            return TimeOfDay.MORNING;
        } else if (hour >= 12 && hour < 18) {
            return TimeOfDay.AFTERNOON;
        } else {
            return TimeOfDay.EVENING;
        }
    }

    //Перевести гектопаскали в мм рт. ст.
    public static float hPaToMmHg(float hPa) {
        return hPa * 0.75006f;
    }

    public static Constants.weatherStatus getWeatherStatus(int weatherCode) {
        int code = weatherCode / 100;
        if (code == 2 || code == 3 || code == 5) {
            return Constants.weatherStatus.RAIN;
        } else if (code == 6) {
            return Constants.weatherStatus.SNOW;
        } else {
            return Constants.weatherStatus.SUN;
        }
    }

    //Время суток
    public enum TimeOfDay {
        MORNING,
        AFTERNOON,
        EVENING,
        NIGHT
    }
}
