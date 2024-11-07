package com.kelyandev.fluxbiz.Auth;

import android.os.Bundle;
import android.text.TextUtils;
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
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Votre email est requise");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Le mot de passe est requis");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Votre nom d'utilisateur est requis");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build();

                    user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Votre compte a bien été créé", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erreur lors de la création du compte :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Erreur lors de la création du compte :" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
