package com.andre.solid.solidchat.stuff;

import android.support.annotation.StringRes;
import android.widget.Toast;

import com.andre.solid.solidchat.MyApplication;

/**
 * Created by lantain on 08.04.17.
 */

public class Utils {

    public static void showToastMessage(@StringRes int messageRes){
        Toast.makeText(MyApplication.get(),messageRes,Toast.LENGTH_SHORT).show();
    }

    public static void showToastMessage(String message){
        Toast.makeText(MyApplication.get(),message,Toast.LENGTH_SHORT).show();
    }
}
