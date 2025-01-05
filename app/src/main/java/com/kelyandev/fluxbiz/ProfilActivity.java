package com.kelyandev.fluxbiz;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kelyandev.fluxbiz.Bizzes.Adapters.BizAdapter;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity in which the current user gets to see his Bizzes
 */
public class ProfilActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BizAdapter bizAdapter;
    private List<Biz> bizList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recycler View
        recyclerView = findViewById(R.id.recyclerViewBizs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bizList = new ArrayList<>();

        // Firebase Instances
        db = FirebaseFirestore.getInstance();

        String userId = getIntent().getStringExtra("userId");
        String displayName = getIntent().getStringExtra("username");

        if (userId == null) {
            userId = mAuth.getCurrentUser().getUid();
        }
        if (displayName == null) {
            displayName = mAuth.getCurrentUser().getDisplayName();
        }

        bizAdapter = new BizAdapter(bizList, userId);
        recyclerView.setAdapter(bizAdapter);

        loadUserDataFromFirestore(userId);

        // Username
        TextView profileUsername = findViewById(R.id.textViewUsername);

        if (displayName != null) {
            profileUsername.setText(displayName);
        } else {
            profileUsername.setText("");
        }

        // Back Arrow
        ImageButton backButton = findViewById(R.id.imageButtonBack);
        backButton.setOnClickListener(view -> {
            finish();
        });

    }

    /**
     * Gets the user's Biz from Firestore and Realtime Database
     */
    private void loadUserDataFromFirestore(String userId) {
        db.collection("bizs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore Error", "Listen failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        bizList.clear();
                        int[] loadedCount = {0};

                        for (DocumentSnapshot document: queryDocumentSnapshots.getDocuments()) {
                            String id = document.getId();
                            String content = document.getString("content");
                            long time = document.getLong("time");
                            String username = document.getString("username");

                            Biz biz = new Biz(id, content, time, username, 0, 0, 0, userId);
                            bizList.add(biz);

                            DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("likesRef").child(id);

                            likesRef.child("likeCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        int likeCount  = snapshot.getValue(Integer.class);
                                        biz.setLikes(likeCount);
                                    }

                                    loadedCount[0]++;

                                    if (loadedCount[0] == queryDocumentSnapshots.size()) {
                                        bizList.sort((b1, b2) -> {
                                            Log.w("Sorting process", "Score 1: " + b1.getScore() + " - Score 2: " + b2.getScore());
                                            return Double.compare(b2.getScore(), b1.getScore());
                                        });

                                        bizAdapter.notifyDataSetChanged();
                                        Log.w("Sorting process", "Biz feed sorted correctly");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w("Realtime Database", "Failed to read like count", error.toException());
                                }
                            });
                        }

                        int bizCount = bizList.size();
                        TextView bizCountTextView = findViewById(R.id.biz_count);

                        String bizCountText = ("Bizs: " + bizCount);
                        bizCountTextView.setText(bizCountText);

                    } else {
                        Log.d("Firestore Data", "No documents found");
                    }
                });
    }
}
