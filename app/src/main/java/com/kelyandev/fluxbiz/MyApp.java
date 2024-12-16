package com.kelyandev.fluxbiz;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.FirebaseDatabase;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        manageTheme();
    }

    /**
     * Function to manage the chosen color theme in the app preferences.
     */
    private void manageTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = prefs.getString("theme", "system");

        int newNightMode;
        switch (theme) {
            case "light":
                newNightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                newNightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case "system":
            default:
                newNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        if (AppCompatDelegate.getDefaultNightMode() != newNightMode) {
            AppCompatDelegate.setDefaultNightMode(newNightMode);
        }
    }
}
