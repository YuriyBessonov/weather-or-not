package com.warinator.app.weatherornot.util;

import java.lang.reflect.Field;

/**
 * Created by Warinator on 10.07.2017.
 */

public class Util {
    private Util(){}

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
