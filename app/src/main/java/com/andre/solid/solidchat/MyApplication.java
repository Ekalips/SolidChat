package com.andre.solid.solidchat;

import android.app.Application;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import org.greenrobot.eventbus.EventBus;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by lantain on 08.04.17.
 */

public class MyApplication extends Application {

    private static final String TAG = MyApplication.class.getSimpleName();
    private static MyApplication applicationInstance;
    public static String CONTENT_AUTHORITY;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationInstance = this;
        CONTENT_AUTHORITY = getPackageName() + ".provider";
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);


        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build());

        EventBus.builder().sendNoSubscriberEvent(true).installDefaultEventBus();


    }

    public static MyApplication get() {
        return applicationInstance;
    }
}

