package com.kelyandev.fluxbiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kelyandev.fluxbiz.Bizzes.Adapters.ReplyAdapter;
import com.kelyandev.fluxbiz.Bizzes.CommentBizActivity;
import com.kelyandev.fluxbiz.Bizzes.Models.Reply;
import com.kelyandev.fluxbiz.Profile.ProfilActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class BizConversationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReplyAdapter replyAdapter;
    private ImageButton arrowBack;
    private String bizId, bizContent, bizUsername, userId, bizAuthorId, parentId;
    private long bizTime;
    private int bizLike, bizRebiz;
    private FirebaseFirestore db;
    private FirebaseDatabase rdb;
    private DatabaseReference bizRef;
    private FirebaseAuth mAuth;
    private List<Reply> replyList;
    private TextView bizTextUsername, bizTextContent, bizTextTime, likeCount, rebizCount, emptyText;
    private ImageButton rebizButton, likeButton, commentButton, profilButton;
    private boolean isReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_biz_conversation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Instantiation of all items
        // --
        // Button to leave activity
        arrowBack = findViewById(R.id.backArrow);
        // original Biz parts
        bizId = getIntent().getStringExtra("bizId");
        bizContent = getIntent().getStringExtra("bizContent");
        bizUsername = getIntent().getStringExtra("bizUsername");
        bizTime = getIntent().getLongExtra("bizTime", System.currentTimeMillis());
        bizAuthorId = getIntent().getStringExtra("authorId");
        bizLike = 0;
        bizRebiz = 0;
        isReply = getIntent().getBooleanExtra("isReply", false);
        if (isReply) {
            parentId = getIntent().getStringExtra("parentId");
        }
        // Layout original biz
        bizTextUsername = findViewById(R.id.originalBizUsername);
        bizTextContent = findViewById(R.id.originalBizContent);
        bizTextTime = findViewById(R.id.originalBizTime);
        rebizCount = findViewById(R.id.rebiz_count);
        likeCount = findViewById(R.id.like_count);
        profilButton = findViewById(R.id.imageViewProfile);
        // Original Biz buttons
        rebizButton = findViewById(R.id.buttonRebiz);
        likeButton = findViewById(R.id.buttonLike);
        commentButton = findViewById(R.id.buttonComment);
        // Firestore & Realtime Database instance
        db = FirebaseFirestore.getInstance();
        rdb = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/");
        bizRef = rdb.getReference("likesRef").child(bizId);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        // Replies recycler view layout
        emptyText = findViewById(R.id.emptyReplyList);
        recyclerView = findViewById(R.id.commentFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        replyList = new ArrayList<>();
        replyAdapter = new ReplyAdapter(replyList,userId);
        recyclerView.setAdapter(replyAdapter);

        // Initial loading steps
        manageOriginalBiz();
        loadOriginalBizCounts();
        loadRepliesFromFirestore();

        // Buttons click listeners
        // --
        // Button to finish the activity
        arrowBack.setOnClickListener(v -> {
            finish();
        });

        // Button to comment the original Biz
        commentButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommentBizActivity.class);
            intent.putExtra("bizUsername", bizUsername);
            intent.putExtra("bizContent", bizContent);
            intent.putExtra("bizId", bizId);
            startActivity(intent);
        });

        // Button to go to the author's profile
        profilButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfilActivity.class);
            intent.putExtra("userId", bizAuthorId);
            intent.putExtra("username", bizUsername);
            startActivity(intent);
        });

        // Buttons to manage when a user like / rebiz the original post
        likeButton.setOnClickListener(v -> manageLikeAction());
        rebizButton.setOnClickListener(v -> manageRebizAction());

    }

    /**
     * Function to add the original biz's content and username to the View
     */
    private void manageOriginalBiz() {
        bizTextUsername.setText(bizUsername);
        bizTextContent.setText(bizContent);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd MMM yyyy", Locale.getDefault());
        String formattedTime = dateFormat.format(new Date(bizTime));
        bizTextTime.setText(formattedTime);
    }

    /**
     * Load the counters of the original Biz from Realtime Database
     * Counter's textView will be changed once data loaded
     */
    private void loadOriginalBizCounts() {
        bizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("likeCount").exists()) {
                    bizLike = snapshot.child("likeCount").getValue(Integer.class);
                }
                if (snapshot.child("rebizCount").exists()) {
                    bizRebiz = snapshot.child("rebizCount").getValue(Integer.class);
                }

                boolean isLiked = snapshot.child("userRefs").child(userId).exists();
                boolean isRebizzed = snapshot.child("rebizRefs").child(userId).exists();

                likeButton.setSelected(isLiked);
                rebizButton.setSelected(isRebizzed);

                likeCount.setText(String.valueOf(bizLike));
                rebizCount.setText(String.valueOf(bizRebiz));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("RealtimeDatabase", "Failed to read like count", error.toException());
            }
        });
    }

    /**
     * Manage the action of adding / removing a like to the original Biz
     */
    private void manageLikeAction() {
        bizRef.child("likeCount").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                bizLike = task.getResult().getValue(Integer.class);

                boolean isLiked = !likeButton.isSelected();
                likeButton.setSelected(isLiked);

                if (isLiked) {
                    bizRef.child("userRefs").child(userId).setValue(true);
                    bizRef.child("likeCount").setValue(ServerValue.increment(1));

                    bizLike += 1;
                    likeCount.setText(String.valueOf(bizLike));
                } else {
                    bizRef.child("userRefs").child(userId).removeValue();
                    bizRef.child("likeCount").setValue(ServerValue.increment(-1));

                    bizLike -= 1;
                    likeCount.setText(String.valueOf(bizLike));
                }
            }
        });

    }

    /**
     * Manage the action of adding / removing a rebiz to the original Biz
     */
    private void manageRebizAction() {
        bizRef.child("rebizCount").get().addOnCompleteListener(task -> {
            bizRebiz = task.getResult().getValue(Integer.class);

            boolean isRebizzed = !rebizButton.isSelected();
            rebizButton.setSelected(isRebizzed);

            if (isRebizzed) {
                bizRef.child("rebizRefs").child(userId).setValue(true);
                bizRef.child("rebizCount").setValue(ServerValue.increment(1)).addOnSuccessListener(aVoid -> {

                    bizRebiz += 1;
                    rebizCount.setText(String.valueOf(bizRebiz));
                });
            } else {
                bizRef.child("rebizRefs").child(userId).removeValue();
                bizRef.child("rebizCount").setValue(ServerValue.increment(-1)).addOnSuccessListener(aVoid -> {

                    bizRebiz -= 1;
                    rebizCount.setText(String.valueOf(bizRebiz));
                });
            }
        });
    }

    /**
     * Loads the replies from Firestore
     */
    private void loadRepliesFromFirestore() {
        db.collection("replies")
                .whereEqualTo("parentBizId", bizId)
                .whereEqualTo("isDeleted", false)
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        updateReplyList(task.getResult().getDocuments());
                    } else {
                        Log.d("Firestore", "Failed to fetch data from servers", task.getException());
                    }
                });
    }

    /**
     * Load replies data from RealtimeDatabase
     */
    private void loadRealtimeDatabaseData(List<Reply> newReplyList) {
        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger totalTasks = new AtomicInteger(newReplyList.size() * 3);

        for (Reply reply : newReplyList) {
            DatabaseReference interactionRef = rdb.getReference("likesRef").child(reply.getId());

            ValueEventListener likeListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        reply.setLikes(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        finalizeReplyListUpdate(newReplyList);
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
                        reply.setRebizzes(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        finalizeReplyListUpdate(newReplyList);
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
                        reply.setReplies(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        finalizeReplyListUpdate(newReplyList);
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
        }
    }

    /**
     * Update the Reply list
     * @param documents The different replies
     */
    private void updateReplyList(List<DocumentSnapshot> documents) {
        List<Reply> newReplyList = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            String id = document.getId();

            String content = document.getString("text");
            long time = document.getLong("time");
            String author = document.getString("author");
            String userId = document.getString("userId");
            String replyToUser = document.getString("replyToUser");

            Reply reply = new Reply(id, content, time, author, 0, 0, 0, userId, replyToUser, bizId);
            newReplyList.add(reply);
        }
        if (newReplyList.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
        } else {
            loadRealtimeDatabaseData(newReplyList);
        }
    }

    /**
     * Finalize the update process: calculate scores, sort the list, and notify the adapter
     * @param newReplyList The new reply list to load in the adapter
     */
    private void finalizeReplyListUpdate(List<Reply> newReplyList) {
        for (Reply reply: newReplyList) {
            reply.calculateScore();
        }
        replyList.sort((b1, b2) -> Double.compare(b2.getScore(), b1.getScore()));
        replyAdapter.updateData(newReplyList);
    }
}
