package com.kelyandev.fluxbiz.Settings.Security;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kelyandev.fluxbiz.Auth.ForgottenPassActivity;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.R;

public class ChangePasswordFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Button buttonConfirm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedinstance) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        EditText ancientPassword = view.findViewById(R.id.editTextAncientPassword);
        EditText newPassword = view.findViewById(R.id.editTextNewPassword);
        EditText confirmNewPassword = view.findViewById(R.id.editTextConfirmPassword);

        buttonConfirm = view.findViewById(R.id.buttonNext);
        TextView forgotten = view.findViewById(R.id.textViewForgottenPassword);

        forgotten.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ForgottenPassActivity.class));
        });

        buttonConfirm.setOnClickListener(v -> {
            String lastPassword = ancientPassword.getText().toString().trim();
            String password = newPassword.getText().toString().trim();
            String confirmPassword = confirmNewPassword.getText().toString().trim();
            buttonConfirm.setEnabled(false);

            if (TextUtils.isEmpty(lastPassword) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(getContext(),"Merci de remplir tous les champs", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(),"Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(),"Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
                return;
            }

            reauthenticate(lastPassword, password);
        });

        return view;
    }

    /**
     * Function to reauthenticate the user before updating his password
     * @param currentPassword His current password for the authentication
     * @param newPassword The new password to use
     */
    private void reauthenticate(String currentPassword, String newPassword) {
        if (currentUser != null && currentUser.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);

            currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updatePassword(newPassword);
                } else {
                    Toast.makeText(getContext(), "Réauthentification échouée. Vérifiez votre mot de passe actuel", Toast.LENGTH_SHORT).show();
                    buttonConfirm.setEnabled(true);
                }
            });
        } else {
            Toast.makeText(getContext(), "Erreur: Utilisateur non connecté ou email indisponible.", Toast.LENGTH_SHORT).show();
            buttonConfirm.setEnabled(true);
        }
    }

    /**
     * Function to change the user's password
     * @param newPassword The new password to use
     */
    private void updatePassword(String newPassword) {
        currentUser.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Mot de passe mis à jour avec succès.", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Erreur lors de la mise à jour du mot de passe", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
            }
        });
    }
}
