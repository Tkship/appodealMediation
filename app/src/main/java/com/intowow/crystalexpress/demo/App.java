package com.intowow.crystalexpress.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.intowow.sdk.I2WAPI;


public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        final boolean IS_SUPPORT_LIFECYCLE_CALLBACKS =
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
        App mInstance = null;
        Log.d("APP","-------------Application 1 ");
        MultiDex.install(this);
        //  Initilize intowow sdk
        //
        boolean isTestMode = false; // Set true to enable test mode
        boolean isOpenVerboseLog = true; // Set true to enable verbose log
        Log.d("APP","-------------Application I2WAPI.init isTestMode = " + isTestMode);
        I2WAPI.init(this, isTestMode, isOpenVerboseLog);

    }
    /*final boolean IS_SUPPORT_LIFECYCLE_CALLBACKS =
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH);
    App mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("APP","-------------Application onCreate");
        mInstance = this;
        MultiDex.install(this);

        if(IS_SUPPORT_LIFECYCLE_CALLBACKS) {
            mInstance.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                    //  TODO
                    //  Initilize intowow sdk
                    //
                    boolean isTestMode = false; // Set true to enable test mode
                    boolean isOpenVerboseLog = true; // Set true to enable verbose log
                    Log.d("APP","-------------Application I2WAPI.init isTestMode = " + isTestMode);
                    I2WAPI.init(activity.getApplicationContext(), isTestMode, isOpenVerboseLog);

                }

                @Override
                public void onActivityStarted(Activity activity) {
                }

                @Override
                public void onActivityResumed(Activity activity) {

                    //  TODO
                    //  Notify sdk that Activity has already in resume status
                    //
                    I2WAPI.onActivityResume(activity.getApplicationContext());

                }

                @Override
                public void onActivityPaused(Activity activity) {

                    //  TODO
                    //  Notify sdk that Activity has already in pause status
                    //
                    I2WAPI.onActivityPause(activity.getApplicationContext());

                }

                @Override
                public void onActivityStopped(Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity,
                                                        Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                }
            });
        }
    }*/
}
