package com.kelyandev.fluxbiz.Settings;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.kelyandev.fluxbiz.R;
import com.kelyandev.fluxbiz.Settings.Account.AccountSettingsFragment;
import com.kelyandev.fluxbiz.Settings.Security.SecuritySettingsFragment;

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

    /**
     * Function to navigate to a fragment
     * @param fragment The fragment to navigate to
     */
    private void navigateToFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
