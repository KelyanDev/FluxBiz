package com.kelyandev.fluxbiz.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.Models.Biz;
import com.kelyandev.fluxbiz.R;

import java.util.List;

public class BizAdapter extends RecyclerView.Adapter<BizAdapter.BizViewHolder> {

    private List<Biz> bizList;
    private String currentUserId;

    public BizAdapter(List<Biz> bizList, String currentUserId) {
        this.bizList = bizList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public BizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_biz, parent, false);
        return new BizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BizViewHolder holder, int position) {
        Biz biz = bizList.get(position);

        holder.contentTextView.setText(biz.getContent());
        holder.usernameTextView.setText(biz.getUsername());
        holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));
        holder.timeTextView.setText(biz.getFormattedDate());

        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/").getReference("likesRef").child(biz.getId());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likesRef.child("userRefs").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                holder.buttonLike.setSelected(isLiked);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        holder.buttonOptions.setOnClickListener(v -> showBottomSheetDialog(holder.itemView.getContext(), biz));

        holder.buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLiked = !holder.buttonLike.isSelected();
                holder.buttonLike.setSelected(isLiked);

                DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/").getReference("likesRef").child(biz.getId());
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if (isLiked) {
                    likesRef.child("userRefs").child(userId).setValue(true);
                    likesRef.child("likeCount").setValue(biz.getLikes() + 1);

                    biz.incrementLikes();
                    holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));

                } else {
                    likesRef.child("userRefs").child(userId).removeValue();
                    likesRef.child("likeCount").setValue(biz.getLikes() - 1);

                    biz.decrementLikes();
                    holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return bizList.size();
    }

    static class BizViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView, usernameTextView, likeCountTextView, timeTextView;
        public ImageButton buttonLike, buttonOptions;

        public BizViewHolder(@NonNull View itemview) {
            super(itemview);
            buttonLike = itemview.findViewById(R.id.buttonLike);
            buttonOptions = itemview.findViewById(R.id.imageButtonOptions);
            contentTextView = itemview.findViewById(R.id.textViewBizContent);
            usernameTextView = itemview.findViewById(R.id.textViewBizUsername);
            likeCountTextView = itemview.findViewById(R.id.like_count);
            timeTextView = itemview.findViewById(R.id.textViewBizTime);
        }
    }

    private void showBottomSheetDialog(Context context, Biz biz) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_options_owned, null);

        TextView deleteOption = bottomSheetView.findViewById(R.id.option_delete);
        TextView cancelOption = bottomSheetView.findViewById(R.id.option_cancel);

        if (biz.getUserId().equals(currentUserId)) {
            deleteOption.setVisibility(View.VISIBLE);
            deleteOption.setOnClickListener(v -> {
                deleteBiz(biz);
                bottomSheetDialog.dismiss();
            });
        }

        cancelOption.setVisibility(View.VISIBLE);
        cancelOption.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void deleteBiz(Biz biz) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("likesRef").child(biz.getId());

        FirebaseFirestore.getInstance().collection("bizs")
                .document(biz.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    likesRef.removeValue()
                        .addOnSuccessListener(aVoid2 -> {
                            bizList.remove(biz);
                            notifyDataSetChanged();
                        }).addOnFailureListener(e -> Log.w("Biz deletion", "Error deleting biz from Database"));
                }).addOnFailureListener(e -> Log.w("Biz Deletion", "Error deleting biz from Firestore", e));
    }
}
