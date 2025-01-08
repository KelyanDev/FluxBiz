package com.kelyandev.fluxbiz.Bizzes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.kelyandev.fluxbiz.Bizzes.CommentBizActivity;
import com.kelyandev.fluxbiz.Bizzes.Models.Biz;
import com.kelyandev.fluxbiz.Bizzes.Models.Reply;
import com.kelyandev.fluxbiz.Profile.ProfilActivity;
import com.kelyandev.fluxbiz.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {
    private List<Reply> replyList;
    private String currentUserId;

    /**
     * Constructor of ReplyAdapter
     * @param replyList The list containing the replies
     * @param currentUserId The current logged in user's Id
     */
    public ReplyAdapter(List<Reply> replyList, String currentUserId) {
        this.replyList = replyList;
        this.currentUserId = currentUserId;
    }

    /**
     * Updates the new data in the Adapter.
     * This function will compare the difference between the old reply list and the new one.
     * After comparing the multiple items of the lists, the function will tell the adapter which data needs to be modified.
     * @param newReplyList The new reply list to update the data
     */
    public void updateData(List<Reply> newReplyList) {
        List<Reply> safeNewReplyList = new ArrayList<>(newReplyList);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return replyList.size();
            }

            @Override
            public int getNewListSize() {
                return newReplyList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return replyList.get(oldItemPosition).getId().equals(newReplyList.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return replyList.get(oldItemPosition).equals(newReplyList.get(newItemPosition));
            }
        });
        replyList.clear();
        replyList.addAll(safeNewReplyList);

        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Create a new instance of ReplyViewHolder when needed
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return A new instance of ReplyViewHolder
     */
    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reply, parent, false);
        return new ReplyAdapter.ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyAdapter.ReplyViewHolder holder, int position) {
        Reply reply = replyList.get(position);

        holder.content.setText(reply.getContent());
        holder.author.setText(reply.getAuthor());
        holder.likeCount.setText(String.valueOf(reply.getLikes()));
        holder.rebizCount.setText(String.valueOf(reply.getRebizzes()));
        holder.replyCount.setText(String.valueOf(reply.getReplies()));
        holder.time.setText(reply.getFormattedDate());
        setStyledMentions(holder.replyingTextView, "En réponse à @" + reply.getReplyTo());

        // Manage the click on the profile picture
        holder.viewProfil.setOnClickListener(v -> {
            Context context = holder.viewProfil.getContext();
            Intent intent = new Intent(context, ProfilActivity.class);
            intent.putExtra("userId", reply.getUserId());
            intent.putExtra("username", reply.getAuthor());
            context.startActivity(intent);
        });

        // Manage the click on the comment button
        holder.buttonReply.setOnClickListener(v -> {
            Context context = holder.buttonReply.getContext();
            Intent intent = new Intent(context, CommentBizActivity.class);
            intent.putExtra("bizUsername", reply.getAuthor());
            intent.putExtra("bizContent", reply.getContent());
            intent.putExtra("bizId", reply.getId());
            context.startActivity(intent);
        });

        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/").getReference("likesRef").child(reply.getId());

        holder.buttonOptions.setOnClickListener(v -> showBottomSheetDialog(holder.itemView.getContext(), reply));

        // Like state managing
        likesRef.child("userRefs").child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                holder.buttonLike.setSelected(isLiked);

                if (isLiked) {
                    int primaryColor = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary);
                    holder.likeCount.setTextColor(primaryColor);
                } else {
                    TypedValue typedValue = new TypedValue();
                    holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                    int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                    holder.likeCount.setTextColor(defaultTextColor);
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

                reply.incrementLikes();
                holder.likeCount.setText(String.valueOf(reply.getLikes()));

                int primaryColor = MaterialColors.getColor(holder.itemView, com.google.android.material.R.attr.colorPrimary);
                holder.likeCount.setTextColor(primaryColor);

            } else {
                likesRef.child("userRefs").child(currentUserId).removeValue();
                likesRef.child("likeCount").setValue(ServerValue.increment(-1));

                reply.decrementLikes();
                holder.likeCount.setText(String.valueOf(reply.getLikes()));

                TypedValue typedValue = new TypedValue();
                holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                holder.likeCount.setTextColor(defaultTextColor);
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
                    holder.replyCount.setTextColor(shareColor);
                } else {
                    TypedValue typedValue = new TypedValue();
                    holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                    int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                    holder.replyCount.setTextColor(defaultTextColor);
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

                reply.incrementRebizzes();
                holder.replyCount.setText(String.valueOf(reply.getRebizzes()));

                int shareColor = holder.itemView.getContext().getColor(R.color.share_color);
                holder.replyCount.setTextColor(shareColor);

            } else {
                likesRef.child("rebizRefs").child(currentUserId).removeValue();
                likesRef.child("rebizCount").setValue(ServerValue.increment(-1));

                reply.decrementRebizzes();
                holder.replyCount.setText(String.valueOf(reply.getRebizzes()));

                TypedValue typedValue = new TypedValue();
                holder.itemView.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                int defaultTextColor = holder.itemView.getContext().getResources().getColor(typedValue.resourceId, holder.itemView.getContext().getTheme());
                holder.replyCount.setTextColor(defaultTextColor);
            }
        });
    }

    /**
     * Return the total number of items in the Reply list
     * @return The size of the reply list
     */
    @Override
    public int getItemCount() {
        return replyList.size();
    }

    /**
     * View holder class for replies, holding references to each UI elements
     */
    public static class ReplyViewHolder extends RecyclerView.ViewHolder {
        TextView content, author, likeCount, rebizCount, replyCount, time, replyingTextView;
        public ImageButton buttonLike, buttonRebiz, buttonReply, viewProfil, buttonOptions;

        /**
         * Constructor for ReplyViewHolder
         * @param itemview The root view of each item in the recycler view
         */
        public ReplyViewHolder(@NonNull View itemview) {
            super(itemview);
            content = itemview.findViewById(R.id.ReplyContent);
            author = itemview.findViewById(R.id.ReplyAuthor);
            likeCount = itemview.findViewById(R.id.like_count);
            rebizCount = itemview.findViewById(R.id.comment_count);
            replyCount = itemview.findViewById(R.id.rebiz_count);
            time = itemview.findViewById(R.id.ReplyTime);
            replyingTextView = itemview.findViewById(R.id.ReplyMention);

            buttonLike = itemview.findViewById(R.id.buttonLike);
            buttonRebiz = itemview.findViewById(R.id.buttonRebiz);
            buttonReply = itemview.findViewById(R.id.buttonComment);
            viewProfil = itemview.findViewById(R.id.ReplyAuthorProfile);
            buttonOptions = itemview.findViewById(R.id.imageButtonOptions);
        }
    }

    /**
     * Function to change the color of the @username in the answering section
     * @param answerTextView The TextView of the answer
     * @param answerText The TextView's text
     */
    private void setStyledMentions(TextView answerTextView, String answerText) {
        SpannableString spannable = new SpannableString(answerText);

        Pattern mentionPattern = Pattern.compile("@\\w+");
        Matcher matcher = mentionPattern.matcher(answerText);

        int mentionColor = ContextCompat.getColor(answerTextView.getContext(), R.color.my_light_primary);

        while (matcher.find()) {
            spannable.setSpan(
                    new ForegroundColorSpan(mentionColor),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        answerTextView.setText(spannable);
    }

    /**
     * Displays a bottom sheet dialog with options for the Reply item
     * @param context The context of the current activity
     * @param reply The Reply object for which options will be shown
     */
    private void showBottomSheetDialog(Context context, Reply reply) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_options_owned, null);

        LinearLayout deleteOption = bottomSheetView.findViewById(R.id.option_delete);
        LinearLayout cancelOption = bottomSheetView.findViewById(R.id.option_cancel);

        if (reply.getUserId().equals(currentUserId)) {
            deleteOption.setVisibility(View.VISIBLE);
            deleteOption.setOnClickListener(v -> {
                deleteReply(reply, context);
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
     * Deletes a Reply from both Firestore and Realtime Database, then updates the adapter
     * @param reply The Reply object to be deleted
     */
    private void deleteReply(Reply reply, Context context) {
        DatabaseReference likesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("likesRef").child(reply.getId());

        Map<String, Object> deletedData = new HashMap<>();
        deletedData.put("isDeleted", true);
        deletedData.put("author", reply.getAuthor());
        deletedData.put("time", System.currentTimeMillis());
        deletedData.put("parentId", reply.getParentId());

        FirebaseFirestore.getInstance().collection("replies").document(reply.getId())
                .set(deletedData)
                .addOnSuccessListener(aVoid -> {
                    likesRef.removeValue().addOnSuccessListener(aVoid2 -> {
                        replyList.remove(reply);
                        updateParentRepliesCount(reply.getParentId());
                        Toast.makeText(context, "Commentaire supprimé avec succès", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Update the reply count of the reply's parent
     * @param parentBizId The reply parent's Id
     */
    private void updateParentRepliesCount(String parentBizId) {
        DatabaseReference repliesRef = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/").getReference("likesRef").child(parentBizId);

        repliesRef.child("replyCount").setValue(ServerValue.increment(-1));
    }
}
