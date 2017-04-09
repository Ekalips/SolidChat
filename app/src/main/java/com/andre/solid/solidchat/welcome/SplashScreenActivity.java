package com.andre.solid.solidchat.welcome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.andre.solid.solidchat.auth.LoginActivity;
import com.andre.solid.solidchat.main.MainActivity;
import com.andre.solid.solidchat.user.User;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        if (User.getInstance().getName().isEmpty()){
            intent = new Intent(this, LoginActivity.class);
        }
        else
            intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
