package com.kelyandev.fluxbiz;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import com.google.firebase.firestore.Source;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kelyandev.fluxbiz.Bizzes.Adapters.BizAdapter;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.Bizzes.CreateBizActivity;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;
import com.kelyandev.fluxbiz.Profile.ProfilActivity;
import com.kelyandev.fluxbiz.Settings.SettingsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiperefreshlayout;
    private BizAdapter bizAdapter;
    private List<Biz> bizList;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;
    private DrawerLayout drawerLayout;
    private ImageButton button, navButton;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private boolean synchronizing = false;

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

        // Managing Firebase remote config
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        fetchRemoteConfig();

        // Database Auth / Firebase instances
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
                ContextCompat.getColor(this, R.color.my_light_primary)
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
        rdb = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/");

        loadDataFromCache();

        // Create Biz Button
        button = findViewById(R.id.buttonLog);

        button.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CreateBizActivity.class)));

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

        // Managing items in the drawer layout
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_settings) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            } else if (item.getItemId() == R.id.nav_profile) {
                Intent profileIntent = new Intent(MainActivity.this, ProfilActivity.class);
                startActivity(profileIntent);
            } else if (item.getItemId() == R.id.nav_help) {
                openSupportPage();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * Function to force the recyclerView to refresh
     */
    private void refreshRecyclerViewData() {
        syncFeedWithServers();
    }

    /**
     * Function to load Bizzes from Cache
     */
    private void loadDataFromCache() {
        db.collection("bizs")
                .orderBy("time", Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d("Firestore Cache", "Data loaded successfully");
                        updateBizList(task.getResult().getDocuments());
                    } else {
                        Log.d("Firestore Cache", "No data found in cache");
                        syncFeedWithServers();
                    }
                });
    }

    /**
     * Function to load Bizzes from Firestore
     */
    private void syncFeedWithServers() {
        swiperefreshlayout.setRefreshing(true);
        synchronizing = true;

        db.collection("bizs")
                .whereEqualTo("isDeleted", false)
                .orderBy("time", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        updateBizList(task.getResult().getDocuments());
                    } else {
                        Log.d("Firestore", "Failed to fetch data", task.getException());
                    }
                });
    }

    /**
     * Function to load data from RealtimeDatabase
     * @param newBizList The new bizzes list to be loaded
     */
    private void loadRealtimeDatabaseLikes(List<Biz> newBizList) {
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger totalTasks = new AtomicInteger(newBizList.size() * 3);

        Log.d("RealtimeDatabase", "Loading new bizzes data");

        for (Biz biz: newBizList) {
            DatabaseReference interactionRef = rdb.getReference("likesRef").child(biz.getId());

            ValueEventListener likeListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        biz.setLikes(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        finalizeBizListUpdate(newBizList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("RealtimeDatabase", "Failed to read like count", error.toException());
                }
            };

            ValueEventListener rebizListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        biz.setRebizzes(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        finalizeBizListUpdate(newBizList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("RealtimeDatabase", "Failed to read rebiz count", error.toException());
                }
            };

            ValueEventListener replyListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        biz.setReplies(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        finalizeBizListUpdate(newBizList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("RealtimeDatabase", "Failed to read like count", error.toException());
                }
            };

            interactionRef.child("likeCount").addListenerForSingleValueEvent(likeListener);
            interactionRef.child("rebizCount").addListenerForSingleValueEvent(rebizListener);
            interactionRef.child("replyCount").addListenerForSingleValueEvent(replyListener);

            if (synchronizing) {
                interactionRef.keepSynced(true);
            }
        }
    }

    /**
     * Function to update the Biz list
     * @param documents The different Bizzes
     */
    private void updateBizList(List<DocumentSnapshot> documents) {
        List<Biz> newBizList = new ArrayList<>();
        Log.d("UpdateBizList", "Updating biz list...");
        for (DocumentSnapshot document : documents) {
            String id = document.getId();
            Biz existingBiz = findBizById(id);

            if (existingBiz != null) {
                if (!Objects.equals(existingBiz.getContent(), document.getString("content"))) {
                    existingBiz.setContent(document.getString("content"));
                }
                newBizList.add(existingBiz);
            } else {
                String content = document.getString("content");
                long time = document.getLong("time");
                String username = document.getString("username");
                String userId = document.getString("userId");

                Biz biz = new Biz(id, content, time, username, 0, 0, 0, userId);
                newBizList.add(biz);
            }
        }

        loadRealtimeDatabaseLikes(newBizList);
    }

    /**
     * Finalize the update process: calculate scores, sort the new list, and give it to the adapter
     * @param newBizList The new biz list loaded from Firestore
     */
    private void finalizeBizListUpdate(List<Biz> newBizList) {
        for (Biz biz: newBizList) {
            biz.calculateScore();
        }
        newBizList.sort((b1, b2) -> Double.compare(b2.getScore(), b1.getScore()));
        swiperefreshlayout.setRefreshing(false);
        bizAdapter.updateData(newBizList);

        if (!synchronizing) {
            syncFeedWithServers();
        } else {
            synchronizing = false;
        }
    }

    /**
     * Checks if a Biz already exists in the user's feed
     * @param id The Biz's ID
     * @return Return the biz if it exists, and null if it doesn't
     */
    private Biz findBizById(String id) {
        for (Biz biz: bizList) {
            if (biz.getId().equals(id)) {
                return biz;
            }
        }
        return null;
    }

    /**
     * Function to open the support page on my Github repo
     */
    private void openSupportPage() {
        String supportUrl = "https://github.com/KelyanDev/FluxBiz/issues/new?body=&labels=help";
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(supportUrl));
        startActivity(browserIntent);
    }

    /**
     * Function to fetch the config from Firebase Remote Config
     */
    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String latestAppVersion = mFirebaseRemoteConfig.getString("latest_app_version");
                        Log.d("RemoteConfig", "Latest app version: " + latestAppVersion);

                        checkForAppUpdate(latestAppVersion);
                    } else {
                        Log.d("RemoteConfig", "Couldn't get parameters");
                    }
                });
    }

    /**
     * Function to check if the app is currently running on the latest version available
     * @param latestAppVersion The latest app version
     */
    private void checkForAppUpdate(String latestAppVersion) {
        String currentAppVersion = getCurrentAppVersion();

        if (isNewVersionRequired(currentAppVersion, latestAppVersion)) {
            showUpdateDialog();
        }
    }

    /**
     * Function to get the app version currently running
     * @return The current app version
     */
    private String getCurrentAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "0.0.0";
        }
    }

    /**
     * Function to compare the version currently running and the latest version available
     * @param currentVersion The version the app is running on
     * @param minimumVersion The latest version available for the app
     * @return True if the latest version available is different from the current version, False if it is not
     */
    private boolean isNewVersionRequired(String currentVersion, String minimumVersion) {
        return currentVersion.compareTo(minimumVersion) < 0;
    }

    /**
     * Function to show the dialog if there is a new version available.
     */
    private void showUpdateDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Mise à jour disponible")
                .setMessage("Une nouvelle version de l'application est disponible. Merci d'installer la dernière version")
                .setPositiveButton("Mettre à jour", null)
                .setCancelable(false)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KelyanDev/FluxBiz/releases"));
            startActivity(intent);
        });
    }

}