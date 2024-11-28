package com.kelyandev.fluxbiz.Settings.Account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.Auth.ReauthenticationFragment;
import com.kelyandev.fluxbiz.R;

public class AccountSettingsFragment extends PreferenceFragmentCompat {

    private FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();

        Preference logoutPreference = findPreference("logout");
        Preference changeUsername = findPreference("username");
        Preference changeMail = findPreference("mail");

        if (logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(preference -> {
                logoutUser();
                return true;
            });
        }
        if (changeMail != null) {
            changeMail.setOnPreferenceClickListener(preference -> {
                showReauthenticationFragment();
                return true;
            });
        }
        if (changeUsername != null) {
            changeUsername.setOnPreferenceClickListener(preference -> {
                showChangeUsernameFragment();
                return true;
            });
        }
    }

    /**
     * Function to show the fragment used for Reauthentication
     */
    private void showReauthenticationFragment() {
        ReauthenticationFragment reauthFragment = new ReauthenticationFragment();

        reauthFragment.setReauthenticationListener(new ReauthenticationFragment.OnReauthenticationListener() {
            @Override
            public void OnReauthenticationSuccess() {
                Toast.makeText(requireContext(), "Réauthentification réussie.", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
                showChangeEmailFragment();
            }

            @Override
            public void OnReauthenticationFailure() {
                Toast.makeText(requireContext(), "Réauthentification échouée. Suppression annulée.", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.settings_container, reauthFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Function to show the fragment to change username
     */
    private void showChangeUsernameFragment () {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.settings_container, new ChangeUsernameFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Function to show the fragment to change Email
     */
    private void showChangeEmailFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.settings_container, new ChangeEmailFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Function to log out the user from the app
     */
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getActivity(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finishAffinity();
    }
}
