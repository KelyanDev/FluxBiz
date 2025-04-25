package com.kelyandev.fluxbiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.kelyandev.fluxbiz.Auth.LoginActivity;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d("UserVerificationProcess", "SplashActivity Loaded !");

        MyApp app = MyApp.getInstance();

        if (app.isUserCheckComplete()) {
            handleRedirection(app.isUserValid());
            return;
        }

        app.setOnUserCheckComplete(() -> {
            boolean isValid = app.isUserValid();
            runOnUiThread(() -> handleRedirection(isValid));
        });
    }

    /**
     * Handle the redirection after the initial loading
     * @param isValidSession Is the session valid or not
     */
    private void handleRedirection(boolean isValidSession) {
        Intent intent = new Intent(this, isValidSession ? MainActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
