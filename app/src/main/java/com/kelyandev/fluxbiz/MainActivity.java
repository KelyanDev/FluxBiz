package com.kelyandev.fluxbiz;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageButton;

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
import com.kelyandev.fluxbiz.Adapters.BizAdapter;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.Models.Biz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BizAdapter bizAdapter;
    private List<Biz> bizList;
    private Button button;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewBiz);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bizList = new ArrayList<>();

        bizAdapter = new BizAdapter(bizList);
        recyclerView.setAdapter(bizAdapter);

        db = FirebaseFirestore.getInstance();

        db.collection("bizs")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore Error", "Listen failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        bizList.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String id = document.getId();
                            String content = document.getString("content");
                            long time = document.getLong("time");
                            String username = document.getString("username");
                            String userId = document.getString("userId");

                            Biz biz = new Biz(id, content, time, username, 0, userId);
                            bizList.add(biz);

                            DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                                    .getReference("likesRef").child(id);

                            likesRef.child("likeCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        int likeCount = snapshot.getValue(Integer.class);
                                        biz.setLikes(likeCount);
                                        bizAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w("Realtime Database", "Failed to read like count", error.toException());
                                }
                            });
                        }

                        Collections.sort(bizList, new Comparator<Biz>() {
                            @Override
                            public int compare(Biz b1, Biz b2) {
                                return Double.compare(b2.calculateScore(), b1.calculateScore());
                            }
                        });

                        bizAdapter.notifyDataSetChanged();
                        Log.d("Firestore data", "Document fetched " + bizList.size());
                    } else {
                        Log.d("Firestore Data", "No Documents found");
                    }
                });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        button = findViewById(R.id.buttonLog);

        button.setOnClickListener( view -> {
            startActivity(new Intent(MainActivity.this, CreateBizActivity.class));
        });

        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }
}