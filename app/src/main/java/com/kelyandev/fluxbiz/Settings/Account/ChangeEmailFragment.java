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
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kelyandev.fluxbiz.R;

import javax.annotation.Nullable;

/**
 * Fragment used by an user to change his mail address
 */
public class ChangeEmailFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_email, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        EditText newEmailInput = view.findViewById(R.id.editTextNewMail);
        Button buttonConfirm = view.findViewById(R.id.buttonNext);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        buttonCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });


        buttonConfirm.setOnClickListener(v -> {
            String newEmail = newEmailInput.getText().toString().trim();
            buttonConfirm.setEnabled(false);

            if (TextUtils.isEmpty(newEmail)) {
                Toast.makeText(getContext(), "Veuillez rentrer votre nouvelle adresse mail", Toast.LENGTH_SHORT).show();
                buttonConfirm.setEnabled(true);
                return;
            }

            if (currentUser != null) {
                currentUser.verifyBeforeUpdateEmail(newEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Adresse Mail mise à jour avec succès", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Erreur lors de la mise à jour de l'adresse mail", Toast.LENGTH_SHORT).show();
                        buttonConfirm.setEnabled(true);
                    }
                });
            }
        });
        return view;
    }
}
