package com.example.hotfix;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import io.flutter.view.FlutterMain;

import android.app.Activity;
import androidx.annotation.CallSuper;
import androidx.multidex.MultiDex;

import io.flutter.FlutterInjector;

/**
 * Flutter implementation of {@link android.app.Application}, managing application-level global
 * initializations.
 *
 * <p>Using this {@link android.app.Application} is not required when using APIs in the package
 * {@code io.flutter.embedding.android} since they self-initialize on first use.
 */
public class MyApplication extends Application {
    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();
        FlutterInjector.instance().flutterLoader().startInitialization(this);
        FlutterMain.startInitialization(this);
        // 修改Bugly appID
        Bugly.init(this, "edd0f6faa3", true);
    }

    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // 初始化 Tinker
        MultiDex.install(base);
        Beta.installTinker();
    }
}