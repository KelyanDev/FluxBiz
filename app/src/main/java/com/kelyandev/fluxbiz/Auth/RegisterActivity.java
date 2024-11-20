package com.kelyandev.fluxbiz.Auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.MainActivity;
import com.kelyandev.fluxbiz.R;
import android.content.Intent;
import android.widget.Toast;

/**
 * Activity used by new users to create a FluzBiz account
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword;
    private Button registerButton;
    private TextView viewLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewLogin = findViewById(R.id.textViewLogin);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        registerButton = findViewById(R.id.buttonRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        viewLogin.setOnClickListener( view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        registerButton.setOnClickListener(view -> registerUser());
    }

    /**
     * Function to register the user in Firebase
     */
    private void registerUser() {
        registerButton.setEnabled(false);
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Votre email est requise");
            registerButton.setEnabled(true);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Le mot de passe est requis");
            registerButton.setEnabled(true);
            return;
        }
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Votre nom d'utilisateur est requis");
            registerButton.setEnabled(true);
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            registerButton.setEnabled(true);
            return;
        }

        isEmailValid(email);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    user.sendEmailVerification()
                            .addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(this, "Email de vérification envoyé", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "L'email de vérification n'a pas pu être envoyé", Toast.LENGTH_SHORT).show();
                                }
                            });
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();


                    user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Votre compte a bien été créé.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erreur lors de la création du compte :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            registerButton.setEnabled(true);
                        }
                    });
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Erreur lors de la création du compte :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                registerButton.setEnabled(true);
            }
        });
    }

    /**
     * Function to verify if the mail is valid
     * @param email The email to verify
     * @return True if the email is valid, false if it is not
     */
    private boolean isEmailValid(String email) {
        return email != null & Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
