package com.andre.solid.solidchat.welcome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.andre.solid.solidchat.auth.LoginActivity;
import com.andre.solid.solidchat.main.MainActivity;
import com.andre.solid.solidchat.user.User;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            decideAndStartActivity();
        else
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE);
    }

    private void decideAndStartActivity() {
        Intent intent;
        if (User.getInstance().getName().isEmpty()) {
            intent = new Intent(this, LoginActivity.class);
        } else
            intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isSucceed = true;
        for (int res :
                grantResults) {
            isSucceed = isSucceed && res == PackageManager.PERMISSION_GRANTED;
        }

        if (isSucceed)
            decideAndStartActivity();
    }
}
