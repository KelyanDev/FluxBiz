package com.kelyandev.fluxbiz;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
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
import com.kelyandev.fluxbiz.Settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiperefreshlayout;
    private BizAdapter bizAdapter;
    private List<Biz> bizList;
    private FirebaseFirestore db;
    private DrawerLayout drawerLayout;
    private ImageButton button, navButton;
    private BroadcastReceiver logoutReceiver;

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

        // Database Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Recycler View
        recyclerView = findViewById(R.id.recyclerViewBiz);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiperefreshlayout = findViewById(R.id.swipeRefreshLayout);
        swiperefreshlayout.setColorSchemeColors(
                getResources().getColor(R.color.my_light_primary)
        );

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

        int colorOnPrimary = typedValue.data;

        swiperefreshlayout.setProgressBackgroundColorSchemeColor(colorOnPrimary);

        // Swipe Refresh Layout
        swiperefreshlayout.setOnRefreshListener(this::refreshRecyclerViewData);


        bizList = new ArrayList<>();

        bizAdapter = new BizAdapter(bizList, currentUser.getUid());
        recyclerView.setAdapter(bizAdapter);

        // Database connexion
        db = FirebaseFirestore.getInstance();

        loadDataFromFirestore();

        // Create Biz Button
        button = findViewById(R.id.buttonLog);

        button.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CreateBizActivity.class));
        });

        // Nav bar
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.navbarButton);

        navButton.setOnClickListener(v -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.user_name);

        String displayName = currentUser.getDisplayName();
        if (displayName != null) {
            navUsername.setText(displayName);
        } else {
            navUsername.setText("");
        }


        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent profileIntent = new Intent(MainActivity.this, ProfilActivity.class);
                startActivity(profileIntent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void refreshRecyclerViewData() {
        loadDataFromFirestore();
        swiperefreshlayout.setRefreshing(false);
    }

    /**
     * Function to load Bizzes from Firestore
     */
    private void loadDataFromFirestore() {
        swiperefreshlayout.setRefreshing(true);

        db.collection("bizs")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore Error", "Listen failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        bizList.clear();

                        int[] loadedCount = {0};

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
                                    }

                                    loadedCount[0]++;

                                    if (loadedCount[0] == queryDocumentSnapshots.size()) {
                                        bizList.sort((b1, b2) -> {
                                            Log.w("Sorting process", "Score 1: " + b1.calculateScore() + " - Score 2: " + b2.calculateScore());
                                            return Double.compare(b2.calculateScore(), b1.calculateScore());
                                        });

                                        bizAdapter.notifyDataSetChanged();
                                        Log.w("Sorting process", "Biz list sorted correctly");

                                        swiperefreshlayout.setRefreshing(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w("Realtime Database", "Failed to read like count", error.toException());
                                    swiperefreshlayout.setRefreshing(false);
                                }
                            });
                        }

                        Log.d("Firestore data", "Document fetched " + bizList.size());
                    } else {
                        Log.d("Firestore Data", "No Documents found");
                        swiperefreshlayout.setRefreshing(false);
                    }
                });

    }

}