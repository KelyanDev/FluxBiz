package com.kelyandev.fluxbiz.Settings.Account;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.kelyandev.fluxbiz.R;

public class ChangeUsernameFragment extends Fragment {

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private Button buttonConfirm;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedinstance) {
        View view = inflater.inflate(R.layout.fragment_change_username, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        String currentUsername = currentUser.getDisplayName();

        EditText newUsername = view.findViewById(R.id.editTextNewUsername);
        EditText ancientUsername = view.findViewById(R.id.editTextUsername);
        buttonConfirm = view.findViewById(R.id.buttonNext);

        // Text managing
        int disabledColor = new EditText(getContext()).getHintTextColors().getDefaultColor();
        ancientUsername.setTextColor(disabledColor);
        ancientUsername.setText(currentUsername);

        newUsername.setText(currentUsername);

        buttonConfirm.setOnClickListener(v -> {
            String username = newUsername.getText().toString().trim();
            buttonConfirm.setEnabled(false);

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(getContext(), "Veuillez entrer un nouveau nom d'utilisateur", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
                return;
            }

            updateUsername(currentUsername, username);
        });

        return view;
    }

    /**
     * Function to update the user's username
     * @param username The ancient username in case a problem is encountered
     * @param newUsername The new username to use
     */
    private void updateUsername(String username, String newUsername) {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            buttonConfirm.setEnabled(true);
            return;
        }

        currentUser.updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(newUsername)
                        .build()
        ).addOnCompleteListener(profileTask -> {
            if (profileTask.isSuccessful()) {
                String userId = currentUser.getUid();

                firestore.collection("users")
                        .document(userId)
                        .update("lastUsernameChange", com.google.firebase.Timestamp.now())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateBizMessages(userId, username, newUsername);
                            } else if (task.getException().getMessage().contains("PERMISSION_DENIED")) {
                                Toast.makeText(getContext(), "Vous avez déjà mis à jour votre nom d'utilisateur récemment", Toast.LENGTH_SHORT).show();
                                manageBizUpdateErrors(username);
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Erreur lors de la mise à jour de votre profil", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
            }
        });
    }

    /**
     * Function to update the user's bizzes, and change their associated username
     * @param userId The user's ID
     * @param username The ancient username in case a problem is encountered
     * @param newUsername The new username to use
     */
    private void updateBizMessages(String userId, String username, String newUsername) {
        firestore.collection("bizs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            WriteBatch batch = firestore.batch();
                            querySnapshot.getDocuments().forEach(document -> {
                                batch.update(document.getReference(), "username", newUsername);

                            });

                            batch.commit()
                                    .addOnCompleteListener(batchTask -> {
                                        if (batchTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Nom d'utilisateur mis à jour avec succès.", Toast.LENGTH_SHORT).show();
                                            requireActivity().getSupportFragmentManager().popBackStack();
                                        } else {
                                            Toast.makeText(getContext(), "Erreur lors de la mise à jour de vos Bizs.", Toast.LENGTH_SHORT).show();
                                            manageBizUpdateErrors(username);
                                        }
                                    });

                        }
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la récupération de vos Bizs", Toast.LENGTH_SHORT).show();
                        manageBizUpdateErrors(username);
                    }
                });
    }

    /**
     * Function to revert the username in case of a problem
     * @param username The ancient username to revert to
     */
    private void manageBizUpdateErrors(String username) {
        currentUser.updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
        ).addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful()) {
                buttonConfirm.setEnabled(true);
            } else {
                Toast.makeText(getContext(), "Erreur de mise à jour de votre profil. Veuillez contacter l'administrateur", Toast.LENGTH_LONG).show();
                buttonConfirm.setEnabled(true);
            }
        });
    }
}
