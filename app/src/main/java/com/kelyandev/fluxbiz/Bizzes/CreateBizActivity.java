package com.kelyandev.fluxbiz.Bizzes;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.Bizzes.Circle.SmallProgressCircleView;
import com.kelyandev.fluxbiz.R;

import java.util.HashMap;
import java.util.Map;


/**
 * Activity used by logged in users to post a new Biz
 */
public class CreateBizActivity extends AppCompatActivity {

    private EditText bizContent;
    private ImageButton cancel;
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
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, imeInsets.bottom));

            return insets;
        });

        // Objects Instantiation
        // Auth and Database instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Biz content instances
        bizContent = findViewById(R.id.editTextBizContent);
        buttonSend = findViewById(R.id.buttonSendBiz);
        cancel = findViewById(R.id.buttonCancel);
        // Progress Circle instance
        SmallProgressCircleView progressCircle = findViewById(R.id.smallProgressCircle);
        progressCircle.setMaxChars(300);

        // Force the Keyboard to appear on the initial load of the activity
        bizContent.requestFocus();
        bizContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (bizContent.hasFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(bizContent, InputMethodManager.SHOW_IMPLICIT);
                    }
                    bizContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        buttonSend.setEnabled(false);

        bizContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    buttonSend.setEnabled(false);
                } else {
                    buttonSend.setEnabled(true);
                }

                progressCircle.setCurrentChars(charSequence.length());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        cancel.setOnClickListener( view -> {
            finish();
        });

        getCurrentUsername();

        buttonSend.setOnClickListener(view -> sendBiz());
    }

    /**
     * Gets the username of the current FluzBiz's user
     */
    private void getCurrentUsername() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            currentUsername = currentUser.getDisplayName();
        } else {
            Toast.makeText(CreateBizActivity.this,"Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create the Biz inside both Firestore and Realtime Database
     */
    private void sendBiz() {
        String bizText = bizContent.getText().toString().trim();
        buttonSend.setEnabled(false);

        if (!bizText.isEmpty() && currentUsername != null) {
            FirebaseUser currentUser = auth.getCurrentUser();

            Map<String, Object> biz = new HashMap<>();
            biz.put("content", bizText);
            biz.put("time",System.currentTimeMillis());
            biz.put("username", currentUsername);
            biz.put("userId", currentUser.getUid());
            biz.put("isDeleted", false);

            db.collection("bizs")
                    .add(biz)
                    .addOnSuccessListener(documentReference -> {
                        String bizId = documentReference.getId();

                        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/").getReference("likesRef").child(bizId);

                        Map<String, Object> likeData = new HashMap<>();
                        likeData.put("likeCount", 0);
                        likeData.put("userRef", new HashMap<String, Boolean>());
                        likeData.put("rebizCount", 0);
                        likeData.put("replyCount", 0);

                        likesRef.setValue(likeData).addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateBizActivity.this,"Biz envoyé", Toast.LENGTH_SHORT).show();
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(CreateBizActivity.this,"Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
                            buttonSend.setEnabled(true);
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(CreateBizActivity.this,"Erreur lors de l'envoi", Toast.LENGTH_SHORT).show();
                        buttonSend.setEnabled(true);
                    });
        } else if (currentUsername == null) {
            Toast.makeText(CreateBizActivity.this,"Nom d'utilisateur introuvable", Toast.LENGTH_SHORT).show();
            buttonSend.setEnabled(true);
        } else {
            Toast.makeText(CreateBizActivity.this,"Biz vide", Toast.LENGTH_SHORT).show();
            buttonSend.setEnabled(true);
        }
    }
}