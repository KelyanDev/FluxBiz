package com.kelyandev.fluxbiz.Settings.Accessibility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.Auth.ReauthenticationFragment;
import com.kelyandev.fluxbiz.R;

public class AccessibilitySettingsFragment extends PreferenceFragmentCompat {
    private static final String PREFS_NAME = "settings_pref";
    private static final String THEME_PREF_KEY = "theme";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.accessibility_preferences, rootKey);

        Preference theme = findPreference("theme");

        if (theme != null) {
            theme.setOnPreferenceChangeListener((preference, newValue) -> {
                String themeValue = (String) newValue;
                adaptTheme(themeValue);
                return true;
            });
        }
    }

    private void adaptTheme(String themeValue) {
        requireActivity();
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(THEME_PREF_KEY, themeValue).apply();

        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        requireActivity().recreate();
    }
}
