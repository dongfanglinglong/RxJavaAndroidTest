package com.dongfang.rx;

import android.app.Application;

import com.dongfang.rx.config.AppConfig;

/**
 * Created by dongfang on 16/4/17.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.CACHE_DIR = getCacheDir();
    }
}
