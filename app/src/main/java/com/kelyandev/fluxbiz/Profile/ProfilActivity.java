package com.kelyandev.fluxbiz.Profile;

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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;
import com.kelyandev.fluxbiz.Bizzes.Models.Reply;
import com.kelyandev.fluxbiz.Profile.Adapter.ProfilePagerAdapter;
import com.kelyandev.fluxbiz.R;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activity in which the current user gets to see his Bizzes
 */
public class ProfilActivity extends AppCompatActivity {

    private List<Biz> bizList;
    private List<Reply> replyList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String userId;
    private boolean isBizLoaded, isReplyLoaded;
    private ViewPager2 viewPager;
    private final Object dataLock = new Object();

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

        // Instantiation of all items
        // --
        // Pager View
        bizList = new ArrayList<>();
        replyList = new ArrayList<>();
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager2);
        ProfilePagerAdapter adapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);


        // Association of the tabLayout and the viewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Posts");
            } else {
                tab.setText("Réponses");
            }
        }).attach();

        // Firebase Instances
        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        String displayName = getIntent().getStringExtra("username");

        if (userId == null) {
            userId = mAuth.getCurrentUser().getUid();
        }
        if (displayName == null) {
            displayName = mAuth.getCurrentUser().getDisplayName();
        }

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
     * Gets the bizList
     * @return The loaded biz list
     */
    public List<Biz> getBizList() {
        synchronized (dataLock) {
            return new ArrayList<>(bizList);
        }
    }

    /**
     * Sets a new biz list for the user's profile
     * @param newBizList The new biz list
     */
    public void setBizList(List<Biz> newBizList) {
        bizList = newBizList;
    }

    /**
     * Gets the replyList
     * @return The loaded reply list
     */
    public List<Reply> getReplyList() {
        synchronized (dataLock) {
            return new ArrayList<>(replyList);
        }
    }

    /**
     * Sets a new reply list for the user's profile
     * @param newReplyList The new reply list
     */
    public void setReplyList(List<Reply> newReplyList) {
        replyList = newReplyList;
    }

    /**
     * Gets the current user's ID
     * @return The current user's ID
     */
    public String getUserId() {
        return userId;
    }
}
