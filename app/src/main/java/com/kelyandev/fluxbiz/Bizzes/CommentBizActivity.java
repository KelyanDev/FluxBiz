package com.kelyandev.fluxbiz.Bizzes;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.Bizzes.Circle.SmallProgressCircleView;
import com.kelyandev.fluxbiz.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentBizActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseDatabase rdb;
    private FirebaseAuth mAuth;
    private String currentUsername;
    private ImageButton buttonCancel;
    private Button buttonComment;
    private EditText commentContent;
    private TextView originalContentView, originalUsernameView;
    private View spacerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment_biz);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right,
                    Math.max(systemBars.bottom, imeInsets.bottom));

            return insets;
        });

        // View to handle the total scrollable height
        spacerView = findViewById(R.id.spacerView);
        // Authentification to get the user's ID
        mAuth = FirebaseAuth.getInstance();
        // Original Biz & Comment views
        commentContent = findViewById(R.id.CommentContent);
        originalUsernameView = findViewById(R.id.BizUsername);
        originalContentView = findViewById(R.id.BizContent);
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        // Buttons
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonComment = findViewById(R.id.buttonSendComment);
        // Progress Circle
        SmallProgressCircleView progressCircle = findViewById(R.id.smallProgressCircle);
        progressCircle.setMaxChars(300);

        // Force the keyboard to appear once activity is created
        commentContent.requestFocus();
        commentContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (commentContent.hasFocus()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.showSoftInput(commentContent, InputMethodManager.SHOW_IMPLICIT);
                    }
                    commentContent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        // Get the instance of Firestore
        db = FirebaseFirestore.getInstance();
        rdb = FirebaseDatabase.getInstance("https://fluxbiz-data-default-rtdb.europe-west1.firebasedatabase.app/");

        // Receive the username, as well as the content of the original Biz
        String username = getIntent().getStringExtra("bizUsername");
        String originalContent = getIntent().getStringExtra("bizContent");
        String bizId = getIntent().getStringExtra("bizId");

        // Adapt the answer textView, to change the color of the @username
        TextView answerTextView = findViewById(R.id.answeringTextView);
        String answerText = "En réponse à @" + username ;
        setStyledMentions(answerTextView, answerText);

        originalUsernameView.setText(username);
        originalContentView.setText(originalContent);

        // Manage the scroll view to limit how far an user can scroll
        manageFirstLoading(scrollView);

        // Manage the buttons
        buttonComment.setEnabled(false);
        buttonCancel.setOnClickListener(view -> {
            finish();
        });

        // Listener for the comment's changes
        commentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().isEmpty()) {
                    buttonComment.setEnabled(false);
                } else {
                    buttonComment.setEnabled(true);
                }

                progressCircle.setCurrentChars(charSequence.length());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        buttonComment.setOnClickListener(view -> sendComment(bizId,username));

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
     * Function to manage the different steps when initially loading the activity
     * @param scrollView The scroll view to manage
     */
    private void manageFirstLoading(NestedScrollView scrollView) {
        // Manage the bottom view of the scrollView to adapt the height of the view
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int bizHeight = originalContentView.getHeight();
                int bizUserHeight = originalUsernameView.getHeight();
                int commentHeight = commentContent.getLineHeight();
                int spacerHeight = spacerView.getHeight();

                spacerView.getLayoutParams().height = spacerHeight + bizHeight + bizUserHeight + commentHeight + 10;
                spacerView.requestLayout();
            }
        });

        // Manage the scroll to initially load the user as far down as possible
        new Handler().postDelayed(() -> scrollView.scrollTo(0, scrollView.getChildAt(0).getHeight()), 100);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < 0) {
                    scrollView.scrollTo(0,0);
                }

                int maxScroll = scrollView.getChildAt(0).getMeasuredHeight() - scrollView.getMeasuredHeight();
                if (scrollY > maxScroll) {
                    scrollView.scrollTo(0, maxScroll);
                }
            }
        });
    }

    /**
     * Gets the username of the current FluzBiz's user
     */
    private void getCurrentUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUsername = currentUser.getDisplayName();
        } else {
            Toast.makeText(this,"Vous n'êtes pas connecté", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds the comment to Firestore
     * @param parentBizId The original biz's ID
     * @param replyToUser The username of the parent's author
     */
    private void sendComment(String parentBizId, String replyToUser) {
        buttonComment.setEnabled(false);
        String commentText = commentContent.getText().toString().trim();

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Le commentaire ne peut pas être vide", Toast.LENGTH_SHORT).show();
            buttonComment.setEnabled(true);
            return;
        }

        CollectionReference commentsCollection = db.collection("replies");
        getCurrentUsername();

        Map<String, Object> comment = new HashMap<>();
        comment.put("text", commentText);
        comment.put("time", System.currentTimeMillis());
        comment.put("userId", mAuth.getUid());
        comment.put("author", currentUsername);
        comment.put("parentBizId", parentBizId);
        comment.put("replyToUser", replyToUser);
        comment.put("isDeleted", false);

        commentsCollection.add(comment)
                .addOnSuccessListener(documentReference -> {
                    String replyId = documentReference.getId();

                    updateParentRepliesCount(parentBizId);

                    manageRealtimeDatabase(replyId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Le commentaire n'a pas pu être ajouté", Toast.LENGTH_SHORT).show();
                   buttonComment.setEnabled(false);
                });

    }

    /**
     * Update the reply count of the parent Biz / comment
     * @param parentBizId The Original Biz's ID
     */
    private void updateParentRepliesCount(String parentBizId) {
        DatabaseReference repliesRef = rdb.getReference("likesRef").child(parentBizId);

        repliesRef.child("replyCount").setValue(ServerValue.increment(1));
    }

    /**
     * Manage the initialisation of the reply's counters
     * @param replyId The reply's ID
     */
    private void manageRealtimeDatabase(String replyId) {
        DatabaseReference reference = rdb.getReference("likesRef").child(replyId);

        Map<String, Object> replyData = new HashMap<>();
        replyData.put("likeCount", 0);
        replyData.put("rebizCount", 0);
        replyData.put("replyCount", 0);

        reference.setValue(replyData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Commentaire ajouté avec succès", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erreur lors de l'initialisation des compteurs", Toast.LENGTH_SHORT).show();
            buttonComment.setEnabled(true);
        });
    }
}
