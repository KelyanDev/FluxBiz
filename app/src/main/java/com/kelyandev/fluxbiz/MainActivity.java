package com.kelyandev.fluxbiz;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kelyandev.fluxbiz.Bizzes.Adapters.BizAdapter;
import com.kelyandev.fluxbiz.Auth.LoginActivity;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;
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
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

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

        loadDataFromFirestore();

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