package com.andre.solid.solidchat.user;

import android.content.Context;
import android.content.SharedPreferences;

import com.andre.solid.solidchat.MyApplication;
import com.andre.solid.solidchat.data.QuickQuestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lantain on 08.04.17.
 */

public class User {
    private static final String USER_PREF_DATA = "user_data";

    private static User instance;
    private static SharedPreferences preferences;
    private String name;
    private String image;
    private String mac;

    List<QuickQuestion> quickQuestions = new ArrayList<>();
    public User(String name, String mac, String image) {
        this.name = name;
        this.mac = mac;
        this.image = image;
    }

    public static User getInstance() {
        if (preferences == null)
            preferences = MyApplication.get().getSharedPreferences(USER_PREF_DATA, Context.MODE_PRIVATE);
        if (instance == null)
            instance = new User(preferences.getString(Keys.name.toString(), ""),
                    preferences.getString(Keys.mac.toString(), ""),
                    preferences.getString(Keys.image.toString(), ""));
        return instance;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        preferences.edit().putString(Keys.name.toString(), name).apply();
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        preferences.edit().putString(Keys.image.toString(), image).apply();
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
        preferences.edit().putString(Keys.mac.toString(), mac).apply();
    }

    public List<QuickQuestion> getQuickQuestions() {
        return quickQuestions;
    }

    public void setQuickQuestions(List<QuickQuestion> quickQuestions) {
        this.quickQuestions = quickQuestions;
    }

    enum Keys {
        name, image, mac,questions;
    }
}
