package com.userinfosetting;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * SharePreference工具类，保存配置信息
 */
public class SpUtil {
    private static final String TAG = "SpUtil";
    public static boolean isIntelligentNoPic = false;

    private static SharedPreferences getSp(Context context) {
        return context.getSharedPreferences("config", Context.MODE_PRIVATE);
    }

    //-----------------------------------boolean----------------------------------------//
    public static void putBoolean(Context context, String key, boolean value) {
        getSp(context).edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }


    public static boolean getBoolean(Context context, String key, boolean defaultVal) {
        return getSp(context).getBoolean(key, defaultVal);
    }


    //-----------------------------------String----------------------------------------//
    public static void putString(Context context, String key, String value) {
        getSp(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key) {
        return getString(context, key, "");
    }


    public static String getString(Context context, String key, String defaultVal) {
        return getSp(context).getString(key, defaultVal);
    }

    //-----------------------------------Long----------------------------------------//
    public static void putLong(Context context, String key, long value) {
        getSp(context).edit().putLong(key, value).commit();
    }

    public static long getLong(Context context, String key) {
        return getLong(context, key, 0);
    }


    public static long getLong(Context context, String key, long defaultVal) {
        return getSp(context).getLong(key, defaultVal);
    }

    //-----------------------------------int----------------------------------------//
    public static void putInt (Context context, String key , int value){
        getSp(context).edit().putInt(key ,value).commit();

    }

    public static int getInt (Context context , String key){
        return getInt(context , key , 0);
    }


    public static int getInt (Context context , String key , int defaultVal){
        return getSp(context).getInt(key , defaultVal);
    }

}
