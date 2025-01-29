package com.kelyandev.fluxbiz.Profile.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kelyandev.fluxbiz.Bizzes.Adapters.ReplyAdapter;
import com.kelyandev.fluxbiz.Bizzes.Models.Reply;
import com.kelyandev.fluxbiz.Profile.ProfilActivity;
import com.kelyandev.fluxbiz.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RepliesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReplyAdapter replyAdapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private List<Reply> replyList = new ArrayList<>();
    private ProfilActivity activity;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_replies, container, false);

        // Objects instantiation
        // --
        // Progress Bar
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.EmptyListText);
        // Recycler View
        recyclerView = view.findViewById(R.id.recyclerViewReplies);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        activity = (ProfilActivity) getActivity();
        String userId = activity.getUserId();

        db = FirebaseFirestore.getInstance();
        replyList = activity.getReplyList();

        replyAdapter = new ReplyAdapter(replyList, userId);
        recyclerView.setAdapter(replyAdapter);

        if (replyList.isEmpty()) {
            loadFirestoreData(userId);
        } else {
            updateAdapterData(replyList);
        }

        return view;
    }

    /**
     * Gets the user's Replies from Firestore
     * @param userId The user's ID
     */
    public void loadFirestoreData(String userId) {
        db.collection("replies")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("Firestore Error", "Listen failed", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Reply> newReplyList = new ArrayList<>();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Reply reply = new Reply(
                                    document.getId(),
                                    document.getString("text"),
                                    document.getLong("time"),
                                    document.getString("author"),
                                    0, 0, 0,
                                    userId,
                                    document.getString("replyToUser"),
                                    document.getString("parentBizId")
                            );
                            newReplyList.add(reply);
                        }
                        Log.d("RepliesFragment", "ReplyList size: " + newReplyList.size());
                        if (newReplyList.isEmpty()) {
                            Log.d("RepliesFragment", "Nothing was found. Putting empty text instead");
                            progressBar.setVisibility(View.GONE);
                            emptyText.setVisibility(View.VISIBLE);
                        } else {
                            loadDataFromDatabase(newReplyList);
                        }
                    }
                });
    }

    /**
     * Load the data from RealtimeDatabase
     * @param newReplyList The new reply list
     */
    public void loadDataFromDatabase(List<Reply> newReplyList) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("likesRef");

        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger totalTasks = new AtomicInteger(newReplyList.size() * 3);

        for (Reply reply: newReplyList) {
            DatabaseReference interactionRef = likesRef.child(reply.getId());

            ValueEventListener likeListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        reply.setLikes(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        activity.setReplyList(newReplyList);
                        updateAdapterData(newReplyList);
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
                        activity.setReplyList(newReplyList);
                        updateAdapterData(newReplyList);
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
                        activity.setReplyList(newReplyList);
                        updateAdapterData(newReplyList);
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
     * Updates the adapter data if the new reply list isn't empty
     * @param newReplyList The new reply list
     */
    private void updateAdapterData(List<Reply> newReplyList) {
        if (newReplyList != null) {
            replyAdapter.updateData(newReplyList);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
