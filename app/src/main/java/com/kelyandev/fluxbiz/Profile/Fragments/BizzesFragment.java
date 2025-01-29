package com.kelyandev.fluxbiz.Profile.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.kelyandev.fluxbiz.Bizzes.Adapters.BizAdapter;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;
import com.kelyandev.fluxbiz.Bizzes.Models.Reply;
import com.kelyandev.fluxbiz.Profile.ProfilActivity;
import com.kelyandev.fluxbiz.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BizzesFragment extends Fragment {

    private RecyclerView recyclerView;
    private BizAdapter bizAdapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private List<Biz> bizList = new ArrayList<>();
    private ProfilActivity activity;
    private TextView emptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        // Objects instantiation
        // --
        // Progress Bar
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.EmptyListText);
        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewBizs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        activity = (ProfilActivity) getActivity();
        String userId = activity.getUserId();

        db = FirebaseFirestore.getInstance();
        bizList = activity.getBizList();

        bizAdapter = new BizAdapter(bizList, userId);
        recyclerView.setAdapter(bizAdapter);

        if (bizList.isEmpty()) {
            loadFirestoreData(userId);
        } else {
            updateAdapterData(bizList);
        }

        return view;
    }

    /**
     * Loads the user's bizzes data from Firestore
     * @param userId The user's ID
     */
    private void loadFirestoreData(String userId) {
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
                        List<Biz> newBizList = new ArrayList<>();
                        for (DocumentSnapshot document: queryDocumentSnapshots.getDocuments()) {
                            Biz biz = new Biz(
                                    document.getId(),
                                    document.getString("content"),
                                    document.getLong("time"),
                                    document.getString("username"),
                                    0, 0, 0,
                                    userId
                            );
                            newBizList.add(biz);
                        }
                        if (newBizList.isEmpty()) {
                            progressBar.setVisibility(View.GONE);
                            emptyText.setVisibility(View.VISIBLE);
                        } else {
                            loadDataFromDatabase(newBizList);
                        }
                    }
                });
    }

    /**
     * Loads the user's bizzes data from Realtime Database
     */
    private void loadDataFromDatabase(List<Biz> newBizList) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("likesRef");

        AtomicInteger completedTasks = new AtomicInteger(0);
        AtomicInteger totalTasks = new AtomicInteger(newBizList.size() * 3);

        for (Biz biz: newBizList) {
            DatabaseReference interactionRef = likesRef.child(biz.getId());

            ValueEventListener likeListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        biz.setLikes(snapshot.getValue(Integer.class));
                    }
                    if (completedTasks.incrementAndGet() == totalTasks.get()) {
                        activity.setBizList(newBizList);
                        updateAdapterData(newBizList);
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
                        activity.setBizList(newBizList);
                        updateAdapterData(newBizList);
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
                        activity.setBizList(newBizList);
                        updateAdapterData(newBizList);
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
     * Updates the adapter's data if the new biz list is not empty
     * @param newBizList The new biz list
     */
    private void updateAdapterData(List<Biz> newBizList) {
        if (newBizList != null) {
            bizAdapter.updateData(newBizList);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
