package com.kelyandev.fluxbiz;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import com.kelyandev.fluxbiz.Auth.Devices.DeviceManager;

import java.util.UUID;

public class MyApp extends Application {

    private static MyApp instance;
    private boolean isUserValid = false;
    private boolean isUserCheckComplete = false;
    private Runnable onUserCheckComplete;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseFirestore.setLoggingEnabled(true);

        initFirebase();
        checkSession();
        manageTheme();
    }

    /**
     * Initialize Firebase configs
     */
    private void initFirebase() {
        FirebaseApp.initializeApp(this);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();

        firestore.setFirestoreSettings(settings);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
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

    /**
     * Check if the user is connected, and if his device is known.
     */
    private void checkSession() {
        Log.d("UserVerificationProcess", "Session check started");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            isUserCheckComplete = true;
            return;
        }

        String deviceId = getSafeDeviceId(this);

        DeviceManager.checkDeviceValidity(user.getUid(), deviceId, new DeviceManager.DeviceCheckCallback() {
            @Override
            public void onDeviceValid(boolean isValid) {
                isUserValid = isValid;
                isUserCheckComplete = true;
                if (onUserCheckComplete != null) {
                    onUserCheckComplete.run();
                }
            }
        });
    }

    /**
     * Gets the app instance
     * @return The app instance
     */
    public static MyApp getInstance() {
        return instance;
    }

    /**
     * Checks if the user is valid
     * @return True if he is connected and his device is known, false if not.
     */
    public boolean isUserValid() {
        return isUserValid;
    }

    /**
     * Change the state of the user once connected
     * @param isValid The change of the user
     */
    public void setUserValid(boolean isValid) {
        this.isUserValid = isValid;
    }

    /**
     * Checks if the user has been fully checked
     * @return If the user check is completed.
     */
    public boolean isUserCheckComplete() {
        return isUserCheckComplete;
    }

    /**
     * Gets the device ID from the preferences; Generates one if none exist
     * @param context The context
     * @return The device's Id
     */
    private String getSafeDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String deviceId = prefs.getString("device_id", null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }


    /**
     * Changes the status of the running user check
     * @param callback The callback function to execute
     */
    public void setOnUserCheckComplete(Runnable callback) {
        this.onUserCheckComplete = callback;

        if (isUserCheckComplete && callback != null) {
            callback.run();
        }
    }

}
