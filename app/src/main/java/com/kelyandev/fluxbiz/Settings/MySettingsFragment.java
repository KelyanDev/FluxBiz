package com.kelyandev.fluxbiz.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.R;

public class MySettingsFragment extends PreferenceFragmentCompat {

    private FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();

        Preference logoutPreference = findPreference("logout");
        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                logoutUser();
                return true;
            });
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }
}
