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
import com.kelyandev.fluxbiz.MainActivity;
import com.kelyandev.fluxbiz.R;
import android.content.Intent;
import android.widget.Toast;


/**
 * Activity used by users to log in their FluzBiz account
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button loginButton;
    private TextView viewRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        viewRegister = findViewById(R.id.textViewRegister);
        loginButton = findViewById(R.id.buttonLogin);
        mAuth = FirebaseAuth.getInstance();

        viewRegister.setOnClickListener( view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        loginButton.setOnClickListener(view -> loginUser());
    }

    /**
     * Function to log in an existing user in FluzBiz
     */
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Votre email est requise pour vous connecter.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vous devez indiquer votre mot de passe");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(LoginActivity.this, "Connexion réussie", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Connexion échouée" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
