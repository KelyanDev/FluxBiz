package com.kelyandev.fluxbiz.Settings.Security;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.Auth.ReauthenticationFragment;
import com.kelyandev.fluxbiz.R;

public class SecuritySettingsFragment extends PreferenceFragmentCompat {

    private FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.security_preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();

        Preference delAccountPreference = findPreference("delAccount");
        Preference changePassword = findPreference("password");

        if (delAccountPreference != null) {
            delAccountPreference.setOnPreferenceClickListener(preference -> {
                showReauthenticationFragment();
                return true;
            });
        }
        if (changePassword != null) {
            changePassword.setOnPreferenceClickListener(preference -> {
                showChangePasswordFragment();
                return true;
            });
        }
    }

    /**
     * Function to show the Fragment used to manage Reauthentication
     */
    private void showReauthenticationFragment() {
        ReauthenticationFragment reauthFragment = new ReauthenticationFragment();

        reauthFragment.setReauthenticationListener(new ReauthenticationFragment.OnReauthenticationListener() {
            @Override
            public void OnReauthenticationSuccess() {
                deleteAccount();
            }

            @Override
            public void OnReauthenticationFailure() {
                Toast.makeText(requireContext(), "Réauthentification échouée. Suppression annulée.", Toast.LENGTH_SHORT).show();
            }
        });

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.settings_container, reauthFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Function to delete account from Firebase
     */
    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.delete().addOnCompleteListener(deleteTask -> {
                if (deleteTask.isSuccessful()) {
                    Toast.makeText(requireContext(), "Compte supprimé avec succès.", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la suppression, veuillez réessayer.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(requireContext(), "Erreur lors de la réauthentification.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Function to show the fragment to change the password
     */
    private void showChangePasswordFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.settings_container, new ChangePasswordFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Function to redirect the user to the Login activity once his account is deleted
     */
    private void redirectToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
