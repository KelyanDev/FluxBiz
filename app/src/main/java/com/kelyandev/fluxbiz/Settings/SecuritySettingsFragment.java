package com.kelyandev.fluxbiz.Settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.R;

public class SecuritySettingsFragment extends PreferenceFragmentCompat {

    private FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.security_preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();


        Preference delAccountPreference = findPreference("delAccount");
        if (delAccountPreference != null) {
            delAccountPreference.setOnPreferenceClickListener(preference -> {
                showDeleteAccountConfirmation();
                return true;
            });
        }
    }

    private void showDeleteAccountConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Suppression du compte");

        final EditText passwordInput = new EditText(requireContext());
        passwordInput.setHint("Entrez votre mot de passe");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(passwordInput);

        builder.setPositiveButton("Confirmer", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (!password.isEmpty()) {
                deleteAccount(password);
            } else {
                Toast.makeText(requireContext(),"Mot de passe requis", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteAccount(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            AuthCredential credential= EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.delete().addOnCompleteListener(deleteTask -> {
                                if (deleteTask.isSuccessful()) {
                                    Toast.makeText(requireContext(), "Compte supprimé avec succès.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    requireActivity().finish();
                                } else {
                                    Toast.makeText(requireContext(), "Erreur lors de la suppression, veuillez réessayer.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la réauthentification.", Toast.LENGTH_SHORT).show();
                }
            }) ;
        } else {
            Toast.makeText(requireContext(), "Aucun utilisateur connecté.", Toast.LENGTH_SHORT).show();
        }
    }
}
