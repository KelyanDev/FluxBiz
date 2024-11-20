package com.kelyandev.fluxbiz.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.kelyandev.fluxbiz.R;


public class ForgottenPassActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private Button sendMail;
    private TextView viewLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgotten);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        viewLogin = findViewById(R.id.textViewLogin);
        sendMail = findViewById(R.id.buttonSendMail);
        mAuth = FirebaseAuth.getInstance();

        viewLogin.setOnClickListener( view -> {
            finish();
        });


        sendMail.setOnClickListener(view -> sendMailForPass());
    }

    /**
     * Send reinitialisation mail to user
     */
    private void sendMailForPass() {
        sendMail.setEnabled(false);
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Merci de renseigner l'email de votre compte");
            sendMail.setEnabled(true);
            return;
        }

        if (isEmailValid(email)) {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Email de réinitialisation envoyé", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Erreur lors de l'envoi du mail de réinitialisation", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Email invalide. Veuillez rentrer une email valide", Toast.LENGTH_SHORT).show();
        }


    }

    /**
     * Function to verify if the mail is valid
     * @param email The email to verify
     * @return True if the email is valid, false if it is not
     */
    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
