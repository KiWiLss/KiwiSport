package com.winding.kiwisport;

import android.app.Application;
import android.content.Context;

/**
 * Created by 刘少帅 on 2017/11/1
 */

public class MyApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}
