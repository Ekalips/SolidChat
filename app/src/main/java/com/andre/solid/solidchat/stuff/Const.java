package com.andre.solid.solidchat.stuff;

import android.os.Environment;

import com.andre.solid.solidchat.MyApplication;

import java.io.File;

/**
 * Created by lantain on 09.04.17.
 */

public class Const {
    public static final String APP_NAME = "SolidChat";
    public static final String DEFAULT_STORAGE_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + APP_NAME + File.separator;

}
