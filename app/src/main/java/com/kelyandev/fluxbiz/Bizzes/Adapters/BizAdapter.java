package com.kelyandev.fluxbiz.Bizzes.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.kelyandev.fluxbiz.BizConversationActivity;
import com.kelyandev.fluxbiz.Bizzes.CommentBizActivity;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;
import com.kelyandev.fluxbiz.Profile.ProfilActivity;
import com.kelyandev.fluxbiz.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Adapter to manage and display the Biz list in a RecyclerView
 */
public class BizAdapter extends RecyclerView.Adapter<BizAdapter.BizViewHolder> {

    private List<Biz> bizList;
    private String currentUserId;

    /**
     * Constructor of BizAdapter
     * @param bizList list of Biz objects
     * @param currentUserId ID of the current user
     */
    public BizAdapter(List<Biz> bizList, String currentUserId) {
        this.bizList = bizList;
        this.currentUserId = currentUserId;
    }

    /**
     * Updates the new data in the Adapter.
     * This function will compare the difference between the old biz list and the new one.
     * After comparing the multiple items of the lists, the function will tell the adapter which data needs to be modified.
     * @param newBizList The new biz list to update the data
     */
    public void updateData(List<Biz> newBizList) {
        List<Biz> safeNewBizList = new ArrayList<>(newBizList);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return bizList.size();
            }

            @Override
            public int getNewListSize() {
                return newBizList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                boolean answer = bizList.get(oldItemPosition).getId()
                        .equals(newBizList.get(newItemPosition).getId());
                Log.d("BizAdapterDiffResult", "Are Items the same: " + answer);
                return bizList.get(oldItemPosition).getId()
                        .equals(newBizList.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                boolean answer = bizList.get(oldItemPosition)
                        .equals(newBizList.get(newItemPosition));
                Log.d("BizAdapterDiffResult", "Are contents the same: " + answer);
                return bizList.get(oldItemPosition)
                        .equals(newBizList.get(newItemPosition));
            }
        });
        bizList.clear();
        bizList.addAll(safeNewBizList);

        diffResult.dispatchUpdatesTo(this);
    }


    /**
     * Create a new instance of BizViewHolder when needed
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A new instance of BizViewHolder
     */
    @NonNull
    @Override
    public BizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_biz, parent, false);
        return new BizViewHolder(view);
    }


    /**
     * Binds the Biz data to the views in BizViewHolder
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BizViewHolder holder, int position) {
        Biz biz = bizList.get(position);

        Log.d("BizAdapter View holder", "I just got called to bind a view !");
        Log.d("BizAdapter View holder", "Biz's ID: " + biz.getId());


        holder.contentTextView.setText(biz.getContent());
        holder.usernameTextView.setText(biz.getAuthor());
        holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));
        holder.shareCountTextView.setText(String.valueOf(biz.getRebizzes()));
        holder.replyCounTextView.setText(String.valueOf(biz.getReplies()));
        holder.timeTextView.setText(biz.getFormattedDate());

        holder.rebizedLayout.setVisibility(View.GONE);

        updateRebizInfo(biz, holder.rebizedLayout, holder.textViewRebized);

        // Manage the click on the profile picture
        holder.viewProfil.setOnClickListener(v -> {
            Context context = holder.viewProfil.getContext();
            Intent intent = new Intent(context, ProfilActivity.class);
            intent.putExtra("userId", biz.getUserId());
            intent.putExtra("username", biz.getAuthor());
            context.startActivity(intent);
        });

        // Manage the click on the comment button
        holder.buttonComment.setOnClickListener(v -> {
            Context context = holder.buttonComment.getContext();
            Intent intent = new Intent(context, CommentBizActivity.class);
            intent.putExtra("bizUsername", biz.getAuthor());
            intent.putExtra("bizContent", biz.getContent());
            intent.putExtra("bizId", biz.getId());
            context.startActivity(intent);
        });

        // Manage the click on the Biz
        holder.contentTextView.setOnClickListener(v -> {
            Context context = holder.contentTextView.getContext();
            Intent intent = new Intent(context, BizConversationActivity.class);
            intent.putExtra("bizId", biz.getId());
            intent.putExtra("bizContent", biz.getContent());
            intent.putExtra("bizUsername", biz.getAuthor());
            intent.putExtra("bizTime", biz.getTime());
            intent.putExtra("authorId", biz.getUserId());
            context.startActivity(intent);
        });

        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/").getReference("likesRef").child(biz.getId());

        holder.buttonOptions.setOnClickListener(v -> showBottomSheetDialog(holder.itemView.getContext(), biz));

        // Like state managing
        likesRef.child("userRefs").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                holder.buttonLike.setSelected(isLiked);

                if (isLiked) {
                    int primaryColor = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary);
                    holder.likeCountTextView.setTextColor(primaryColor);
                } else {
                    TypedValue typedValue = new TypedValue();
                    holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                    int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                    holder.likeCountTextView.setTextColor(defaultTextColor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        holder.buttonLike.setOnClickListener(v -> {
           boolean isLiked = !holder.buttonLike.isSelected();
           holder.buttonLike.setSelected(isLiked);

           if (isLiked) {
               likesRef.child("userRefs").child(currentUserId).setValue(true);
               likesRef.child("likeCount").setValue(ServerValue.increment(1));

               biz.incrementLikes();
               holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));

               int primaryColor = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary);
               holder.likeCountTextView.setTextColor(primaryColor);

           } else {
               likesRef.child("userRefs").child(currentUserId).removeValue();
               likesRef.child("likeCount").setValue(ServerValue.increment(-1));

               biz.decrementLikes();
               holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));

               TypedValue typedValue = new TypedValue();
               holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
               int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
               holder.likeCountTextView.setTextColor(defaultTextColor);
           }
        });

        // Rebiz state managing
        likesRef.child("rebizRefs").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isRebizzed = snapshot.exists();
                holder.buttonRebiz.setSelected(isRebizzed);

                if (isRebizzed) {
                    int shareColor = holder.itemView.getContext().getColor(R.color.share_color);
                    holder.shareCountTextView.setTextColor(shareColor);
                } else {
                    TypedValue typedValue = new TypedValue();
                    holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                    int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                    holder.shareCountTextView.setTextColor(defaultTextColor);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        holder.buttonRebiz.setOnClickListener(v -> {
            boolean isRebizzed = !holder.buttonRebiz.isSelected();
            holder.buttonRebiz.setSelected(isRebizzed);

            if (isRebizzed) {
                likesRef.child("rebizRefs").child(currentUserId).setValue(true);
                likesRef.child("rebizCount").setValue(ServerValue.increment(1));

                biz.incrementRebiz();
                holder.shareCountTextView.setText(String.valueOf(biz.getRebizzes()));

                int shareColor = holder.itemView.getContext().getColor(R.color.share_color);
                holder.shareCountTextView.setTextColor(shareColor);

            } else {
                likesRef.child("rebizRefs").child(currentUserId).removeValue();
                likesRef.child("rebizCount").setValue(ServerValue.increment(-1));

                biz.decrementRebiz();
                holder.shareCountTextView.setText(String.valueOf(biz.getRebizzes()));

                TypedValue typedValue = new TypedValue();
                holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                holder.shareCountTextView.setTextColor(defaultTextColor);
            }
        });
    }

    /**
     * Return the total number of items in the Biz list
     * @return The size of the Biz list
     */
    @Override
    public int getItemCount() {
        return bizList.size();
    }


    /**
     * ViewHolder class for Biz, holding references to each UI element
     */
    public static class BizViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView, usernameTextView, likeCountTextView, timeTextView, shareCountTextView, textViewRebized, replyCounTextView;
        public ImageButton buttonLike, buttonOptions, viewProfil, buttonRebiz, buttonComment;
        LinearLayout rebizedLayout;


        /**
         * Constructor for BizViewHolder
         * @param itemview The root view of each item in the RecyclerView
         */
        public BizViewHolder(@NonNull View itemview) {
            super(itemview);
            buttonLike = itemview.findViewById(R.id.buttonLike);
            buttonRebiz = itemview.findViewById(R.id.buttonRebiz);
            buttonComment = itemview.findViewById(R.id.buttonComment);
            buttonOptions = itemview.findViewById(R.id.imageButtonOptions);
            contentTextView = itemview.findViewById(R.id.textViewBizContent);
            usernameTextView = itemview.findViewById(R.id.textViewBizUsername);
            likeCountTextView = itemview.findViewById(R.id.like_count);
            shareCountTextView = itemview.findViewById(R.id.rebiz_count);
            replyCounTextView = itemview.findViewById(R.id.comment_count);
            timeTextView = itemview.findViewById(R.id.textViewBizTime);
            viewProfil = itemview.findViewById(R.id.imageViewProfile);

            textViewRebized = itemview.findViewById(R.id.textViewRebiz);
            rebizedLayout = itemview.findViewById(R.id.Rebized);
        }
    }


    /**
     * Displays a bottom sheet dialog with options for the Biz item
     * @param context The context of the current activity
     * @param biz The Biz object for which options will be shown
     */
    private void showBottomSheetDialog(Context context, Biz biz) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_options_owned, null);

        LinearLayout deleteOption = bottomSheetView.findViewById(R.id.option_delete);
        LinearLayout cancelOption = bottomSheetView.findViewById(R.id.option_cancel);

        if (biz.getUserId().equals(currentUserId)) {
            deleteOption.setVisibility(View.VISIBLE);
            deleteOption.setOnClickListener(v -> {
                deleteBiz(biz);
                bottomSheetDialog.dismiss();
            });
        } else {
            deleteOption.setVisibility(View.GONE);
        }

        cancelOption.setVisibility(View.VISIBLE);
        cancelOption.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }


    /**
     * Deletes a Biz from both Firestore and Realtime Database, then updates the adapter
     * @param biz The Biz object to be deleted
     */
    private void deleteBiz(Biz biz) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("likesRef").child(biz.getId());

        Map<String, Object> deletedData = new HashMap<>();
        deletedData.put("isDeleted", true);
        deletedData.put("author", biz.getAuthor());
        deletedData.put("time", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("bizs")
                .document(biz.getId())
                .set(deletedData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    likesRef.removeValue()
                        .addOnSuccessListener(aVoid2 -> {
                            bizList.remove(biz);
                            updateData(bizList);
                        }).addOnFailureListener(e -> Log.w("Biz deletion", "Error deleting biz from Database"));
                }).addOnFailureListener(e -> Log.w("Biz Deletion", "Error deleting biz from Firestore", e));
    }

    /**
     * Update the Rebiz status of a Biz
     * @param biz The biz
     * @param rebizedLayout The Biz rebized layout
     * @param textViewRebiz The Biz textView for Rebizzes
     */
    @SuppressLint("SetTextI18n")
    private void updateRebizInfo(Biz biz, LinearLayout rebizedLayout, TextView textViewRebiz) {
        DatabaseReference rebizRefs = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("likesRef")
                .child(biz.getId())
                .child("rebizRefs");

        rebizRefs.limitToFirst(2).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                long rebizCount = biz.getRebizzes();

                if (rebizCount == 0) {
                    rebizedLayout.setVisibility(View.GONE);
                    return;
                }

                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                if (rebizCount == 1 && iterator.hasNext()) {
                    String userId = iterator.next().getKey();
                    fetchUsername(userId, username -> textViewRebiz.setText(username + " a reposté"));
                } else if (rebizCount == 2 && iterator.hasNext()) {
                    String firstUserId = iterator.next().getKey();
                    String secondUserId = iterator.hasNext() ? iterator.next().getKey() : null;

                    if (secondUserId != null) {
                        fetchUsername(firstUserId, firstUsername ->
                            fetchUsername(secondUserId, secondUsername ->
                                    textViewRebiz.setText(firstUsername + " et " + secondUsername + " ont reposté")
                            )
                        );
                    }
                } else {
                    textViewRebiz.setText("Plusieurs utilisateurs ont reposté");
                }

                rebizedLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Function to recover the username using the user's Id
     * @param userId The user's Id
     * @param callback The callback to manage the username
     */
    private void fetchUsername(String userId, OnUsernameFetched callback) {
        DatabaseReference usernameRefs = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("usernames")
                .child(userId);

        usernameRefs.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String username = task.getResult().getValue(String.class);
                callback.onUsernameFetched(username);
            } else {
                callback.onUsernameFetched("Utilisateur inconnu");
            }
        });
    }

    /**
     * Interface to manage the callback once the username is recovered
     */
    private interface OnUsernameFetched {
        void onUsernameFetched(String username);
    }
}
