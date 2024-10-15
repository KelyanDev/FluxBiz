package com.kelyandev.fluxbiz;

import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateBizActivity extends AppCompatActivity {

    private EditText bizContent;
    private TextView cancel;
    private Button buttonSend;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_biz);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bizContent = findViewById(R.id.editTextBizContent);
        buttonSend = findViewById(R.id.buttonSendBiz);
        cancel = findViewById(R.id.textViewCancel);

        cancel.setOnClickListener( view -> {
            finish();
        });

        getCurrentUsername();

        buttonSend.setOnClickListener(view -> sendBiz());
    }

    private void getCurrentUsername() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUsername = documentSnapshot.getString("username");
                } else {
                    Toast.makeText(CreateBizActivity.this, "Nom d'utilisateur introuvable", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(CreateBizActivity.this,"Erreur lors de la récupération de votre nom d'utilisateur", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(CreateBizActivity.this,"Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBiz() {
        String bizText = bizContent.getText().toString().trim();

        if (!bizText.isEmpty() && currentUsername != null) {
            FirebaseUser currentUser = auth.getCurrentUser();

            Map<String, Object> biz = new HashMap<>();
            biz.put("content", bizText);
            biz.put("time",System.currentTimeMillis());
            biz.put("username", currentUsername);
            biz.put("userId", currentUser.getUid());
            biz.put("likeCount", 0);

            db.collection("bizs")
                    .add(biz)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(CreateBizActivity.this,"Biz envoyé", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(CreateBizActivity.this,"Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
                    });
        } else if (currentUsername == null) {
            Toast.makeText(CreateBizActivity.this,"Nom d'utilisateur introuvable", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CreateBizActivity.this,"Biz vide", Toast.LENGTH_SHORT).show();
        }
    }
}