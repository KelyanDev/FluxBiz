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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.MainActivity;
import com.kelyandev.fluxbiz.R;
import android.content.Intent;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


/**
 * Activity used by users to log in their FluzBiz account
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button loginButton;
    private TextView viewRegister, viewForgotten;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseReference usernamerefs;

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
        viewForgotten = findViewById(R.id.textViewForgottenPassword);
        loginButton = findViewById(R.id.buttonLogin);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usernamerefs = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("usernames");

        viewRegister.setOnClickListener( view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        viewForgotten.setOnClickListener( view -> {
            startActivity(new Intent(LoginActivity.this, ForgottenPassActivity.class));
        });

        loginButton.setOnClickListener(view -> loginUser());
    }

    /**
     * Logs in an existing user in FluzBiz
     */
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        loginButton.setEnabled(false);

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Votre email est requise pour vous connecter.");
            loginButton.setEnabled(true);
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vous devez indiquer votre mot de passe");
            loginButton.setEnabled(true);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    usernamerefs.child(user.getUid()).get().addOnCompleteListener(usernameTask -> {
                        if (usernameTask.isSuccessful() && usernameTask.getResult().exists()) {
                            proceedToMainActivity();
                        } else {
                            handleFirstLogin(user);
                        }
                    });
                } else if (user != null) {
                    Toast.makeText(this, "Votre email n'est pas vérifiée", Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    user.sendEmailVerification();
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(true);
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "Ce compte n'existe pas.", Toast.LENGTH_SHORT).show();
                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Identifiant ou mot de passe incorrect(s). Réessayez", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erreur: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Handle the first login for the user, updating their username in RealtimeDatabase and Firestore
     * @param user The authenticated user
     */
    private void handleFirstLogin(FirebaseUser user) {
        String userId = user.getUid();
        String username = user.getDisplayName();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Aucun nom d'utilisateur trouvé pour ce compte.", Toast.LENGTH_SHORT).show();
            loginButton.setEnabled(true);
            return;
        }

        usernamerefs.child(userId).setValue(username).addOnCompleteListener(rtTask -> {
            if (rtTask.isSuccessful()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("lastUsernameChange", com.google.firebase.Timestamp.now());

                db.collection("users").document(userId).set(userMap)
                        .addOnCompleteListener(fsTask -> {
                            if (fsTask.isSuccessful()) {
                                proceedToMainActivity();
                            } else {
                                Toast.makeText(this, "Erreur lors de l'enregistrement dans Firestore", Toast.LENGTH_SHORT).show();
                                loginButton.setEnabled(true);
                            }
                        });
            } else {
                Toast.makeText(this, "Erreur lors de l'enregistrement dans Firestore", Toast.LENGTH_SHORT).show();
                loginButton.setEnabled(true);
            }
        });
    }

    /**
     * Proceed to main activity after successful login
     */
    private void proceedToMainActivity() {
        Toast.makeText(this, "Connexion réussie", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
