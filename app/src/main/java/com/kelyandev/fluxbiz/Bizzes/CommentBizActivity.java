package com.kelyandev.fluxbiz.Bizzes;

import static androidx.core.util.TypedValueCompat.dpToPx;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kelyandev.fluxbiz.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommentBizActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseDatabase rdb;
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // View to handle the total scrollable height
        spacerView = findViewById(R.id.spacerView);
        // Original Biz & Comment views
        commentContent = findViewById(R.id.CommentContent);
        originalUsernameView = findViewById(R.id.BizUsername);
        originalContentView = findViewById(R.id.BizContent);
        NestedScrollView scrollView = findViewById(R.id.scrollView);
        // Buttons
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonComment = findViewById(R.id.buttonSendComment);

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

        // Receive the username, as well as the content of the original Biz
        String username = getIntent().getStringExtra("bizUsername");
        String originalContent = getIntent().getStringExtra("bizContent");

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
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        buttonComment.setOnClickListener(view -> sendComment());

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


    private void sendComment() {}
}
