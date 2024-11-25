package com.kelyandev.fluxbiz.Auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kelyandev.fluxbiz.R;

import javax.annotation.Nullable;

/**
 * Fragment used whenever the app need to reauthenticate an user
 * This fragment provide all the tools to reauthenticate the user in Firebase, to let them make changes in their accounts
 */
public class ReauthenticationFragment extends Fragment {

    private FirebaseAuth mAuth;

    /**
     * OnReauthenticationListener
     * Interface to listen for auth events
     */
    public interface OnReauthenticationListener {
        /**
         * Called when the Auth finished successfully
         */
        void OnReauthenticationSuccess();

        /**
         * Called when the Auth fails
         */
        void OnReauthenticationFailure();
    }

    private OnReauthenticationListener reauthenticationListener;

    /**
     * Define the listener for the events of Authentication
     * @param listener The listener used to listen to Auth events
     */
    public void setReauthenticationListener(OnReauthenticationListener listener) {
        this.reauthenticationListener = listener;
    }

    /**
     * Create and returns the view associated to this fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The view created for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reauth, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        EditText passwordInput = view.findViewById(R.id.editTextPassword);
        Button buttonNext = view.findViewById(R.id.buttonNext);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        buttonCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        buttonNext.setOnClickListener(v -> {
            String password = passwordInput.getText().toString().trim();
            buttonNext.setEnabled(false);

            if (password.isEmpty()) {
                Toast.makeText(getContext(), "Veuillez entrer votre mot de passe", Toast.LENGTH_SHORT).show();
                buttonNext.setEnabled(true);
                return;
            }

            if (currentUser != null && currentUser.getEmail() != null) {
                AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
                currentUser.reauthenticate(credential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Réauthentification réussie", Toast.LENGTH_SHORT).show();
                                if (reauthenticationListener != null) {
                                    reauthenticationListener.OnReauthenticationSuccess();
                                }
                            } else {
                                Toast.makeText(getContext(), "Réauthentification échouée", Toast.LENGTH_SHORT).show();
                                if (reauthenticationListener != null) {
                                    reauthenticationListener.OnReauthenticationFailure();
                                    buttonNext.setEnabled(true);
                                }
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Erreur: utilisateur introuvable", Toast.LENGTH_SHORT).show();
                buttonNext.setEnabled(true);
            }
        });

        return view;
    }
}
