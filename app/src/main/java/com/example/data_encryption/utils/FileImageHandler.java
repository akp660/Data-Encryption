package com.example.data_encryption.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class FileImageHandler {

    private static String fileName = "DEFAULTFILE";

    public static void setDefaultFileData(Activity activity, boolean value){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(fileName, value);
        editor.apply();
    }


    public static boolean getDefaultFilePresence(Activity activity){
        SharedPreferences sharedPreferences = activity.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(fileName, false);
    }

}
