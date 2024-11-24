package com.kelyandev.fluxbiz.Settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.kelyandev.fluxbiz.R;

public class RootSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

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
