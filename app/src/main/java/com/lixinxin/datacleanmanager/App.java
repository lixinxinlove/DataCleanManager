package com.lixinxin.datacleanmanager;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    public static Context app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
