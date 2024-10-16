package com.kelyandev.fluxbiz.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kelyandev.fluxbiz.Models.Biz;
import com.kelyandev.fluxbiz.R;

import java.util.List;

public class BizAdapter extends RecyclerView.Adapter<BizAdapter.BizViewHolder> {

    private List<Biz> bizList;

    public BizAdapter(List<Biz> bizList) {
        this.bizList = bizList;
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
        public ImageButton buttonLike;

        public BizViewHolder(@NonNull View itemview) {
            super(itemview);
            buttonLike = itemview.findViewById(R.id.buttonLike);
            contentTextView = itemview.findViewById(R.id.textViewBizContent);
            usernameTextView = itemview.findViewById(R.id.textViewBizUsername);
            likeCountTextView = itemview.findViewById(R.id.like_count);
            timeTextView = itemview.findViewById(R.id.textViewBizTime);
        }
    }
}
