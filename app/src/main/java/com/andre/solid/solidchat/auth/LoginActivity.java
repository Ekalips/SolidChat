package com.andre.solid.solidchat.auth;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.databinding.ActivityLoginBinding;
import com.andre.solid.solidchat.main.MainActivity;
import com.andre.solid.solidchat.stuff.Utils;
import com.andre.solid.solidchat.user.User;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    ObservableField<String> newNameField = new ObservableField<>("");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setNameField(newNameField);

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToSetName();
            }
        });
    }

    private void tryToSetName() {
        if (newNameField.get().trim().isEmpty()) {
            Utils.showToastMessage(R.string.error_enter_name);
        } else {
            User.getInstance().setName(newNameField.get());
            launchMainActivity();
        }
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
