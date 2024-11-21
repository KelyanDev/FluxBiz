package com.kelyandev.fluxbiz.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.R;

public class RootSettingsFragment extends PreferenceFragmentCompat {

    private FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();


        Preference accountPreference = findPreference("account");
        Preference securityPreference = findPreference("security");

        if (accountPreference != null) {
            accountPreference.setOnPreferenceClickListener(preference -> {
                navigateToFragment(new AccountSettingsFragment());
                return true;
            });
        }
        if (securityPreference != null) {
            securityPreference.setOnPreferenceClickListener(preference -> {
                navigateToFragment(new SecuritySettingsFragment());
                return true;
            });
        }
    }

    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
