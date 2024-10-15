package com.kelyandev.fluxbiz.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        holder.buttonLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isLiked = !holder.buttonLike.isSelected();
                holder.buttonLike.setSelected(isLiked);
            }
        });
        holder.contentTextView.setText(biz.getContent());
        holder.usernameTextView.setText(biz.getUsername());
        holder.likeCountTextView.setText(String.valueOf(biz.getLikes()));
    }

    @Override
    public int getItemCount() {
        return bizList.size();
    }

    static class BizViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView, usernameTextView, likeCountTextView;
        public ImageButton buttonLike;

        public BizViewHolder(@NonNull View itemview) {
            super(itemview);
            buttonLike = itemview.findViewById(R.id.buttonLike);
            contentTextView = itemview.findViewById(R.id.textViewBizContent);
            usernameTextView = itemview.findViewById(R.id.textViewBizUsername);
            likeCountTextView = itemview.findViewById(R.id.like_count);
        }
    }
}
