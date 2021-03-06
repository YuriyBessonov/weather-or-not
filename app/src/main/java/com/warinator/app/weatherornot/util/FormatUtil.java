package com.warinator.app.weatherornot.util;

import android.content.Context;

import com.warinator.app.weatherornot.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Утилитный класс для задач форматирования
 */
public class FormatUtil {
    private FormatUtil() {
    }

    //Получить строку для даты в формате [день недели], [день месяца] [название месяца]
    //или [день недели], ["сегодня"] для сегодняшнего дня
    public static String getFormattedDate(Date date, Context context) {
        if (!Util.dateIsToday(date)) {
            SimpleDateFormat sdf = new SimpleDateFormat("E, d MMMM", Locale.getDefault());
            return sdf.format(date);
        } else {
            return String.format("%s, %s", new SimpleDateFormat("E",
                            Locale.getDefault()).format(date),
                    context.getString(R.string.today));
        }

    }

    //Получить строку для времени в формате [ЧЧ:ММ]
    public static String getFormattedTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    //Получить строку для целой части температуры с указанием знака
    public static String getFormattedTemperature(float temperature) {
        return String.format(Locale.getDefault(), "%+d",
                Math.round(temperature));
    }

    //Получить строку для направления ветра в градусах
    public static String getWindDirection(float windDegree, Context context) {
        String[] directions = context.getResources().
                getStringArray(R.array.wind_directions);
        int ind = (int) ((windDegree / 22.5) + .5);
        return directions[ind % directions.length];
    }
}
