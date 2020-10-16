package com.hostcart.socialbot.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.CommentRecyclerViewAdapter;
import com.hostcart.socialbot.model.Comment;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.model.Review;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.KeyboardHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class CommentsActivity extends AppCompatActivity {

    private CommentRecyclerViewAdapter mAdapter;
    private LinearLayout llt_content;
    private EmojiconEditText msg_text;

    private List<ImageView> postReviews = new ArrayList<>();

    private List<Comment> mComments = new ArrayList<>();
    private Posts mPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_comments_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_comments_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if( getSupportActionBar() != null )
            getSupportActionBar().hide();

        mPost = AppUtils.gPosts;

        ImageView img_review0 = findViewById(R.id.img_review_0);
        ImageView img_review1 = findViewById(R.id.img_review_1);
        ImageView img_review2 = findViewById(R.id.img_review_2);
        ImageView img_review3 = findViewById(R.id.img_review_3);
        ImageView img_review4 = findViewById(R.id.img_review_4);
        ImageView img_review5 = findViewById(R.id.img_review_5);
        ImageView img_review6 = findViewById(R.id.img_review_6);
        postReviews.add(img_review0);
        postReviews.add(img_review1);
        postReviews.add(img_review2);
        postReviews.add(img_review3);
        postReviews.add(img_review4);
        postReviews.add(img_review5);
        postReviews.add(img_review6);

        TextView like_show_text = findViewById(R.id.like_show_text);


        for (ImageView img_review: postReviews) {
            img_review.setVisibility(View.GONE);
        }

        if (mPost.getReviews().size() == 0) {
            postReviews.get(0).setVisibility(View.VISIBLE);
            like_show_text.setText(getString(R.string.normal_no_review));
        } else {
            like_show_text.setText(String.valueOf(mPost.getReviews().size()));
            boolean[] flags = new boolean[] {
                    false, false, false, false, false, false
            };
            for (Review review: mPost.getReviews()) {
                flags[Integer.parseInt(review.getType())] = true;
            }
            for (int i = 1; i < postReviews.size(); i++) {
                ImageView img_review = postReviews.get(i);
                if (flags[i - 1]) {
                    img_review.setVisibility(View.VISIBLE);
                }
            }
        }

        llt_content = findViewById(R.id.activity_content);

        // message
        Button send_view = findViewById(R.id.send_btn);
        send_view.setVisibility(View.INVISIBLE);
        send_view.setOnClickListener(v -> {
            if( msg_text.getText().length() < 2 ) {
                Snackbar.make(llt_content, "You must type at least 2 letters", 2500).setActionTextColor(Color.RED).show();
                return;
            }
            if (mPost.getPostUid().equals(FireManager.getUid())) {
                Snackbar.make(llt_content, "This post is yours.", 2500).setActionTextColor(Color.RED).show();
                return;
            }

            // hide keyboard
            KeyboardHelper.hideSoftKeyboard(CommentsActivity.this, msg_text);

            Comment comment = new Comment();
            comment.setPhotoUrl(SharedPreferencesManager.getThumbImg());
            comment.setContent(msg_text.getText().toString());
            comment.setUserName(SharedPreferencesManager.getUserName());
            comment.setTime(String.format(Locale.US, "%d", System.currentTimeMillis()));
            comment.setUserid(FireManager.getUid());

            msg_text.setText("");

            FireConstants.postsRef.child(mPost.getPostId()).child("postComments").child(comment.getTime()).setValue(comment);
        });

        // click like image
        ImageView like_click_image = findViewById(R.id.like_click_image);
        like_click_image.setOnClickListener(v -> {
            if (mPost.getReviews().size() > 0) {
                AppUtils.showOtherActivity(CommentsActivity.this, LikeUsersActivity.class, 0);
            } else {
                Toast.makeText(CommentsActivity.this, R.string.normal_no_review, Toast.LENGTH_SHORT).show();
            }
        });

        msg_text = findViewById(R.id.msg_text);
        KeyboardHelper.openSoftKeyboard(this, msg_text.findFocus());
        msg_text.requestFocus();
        msg_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if( count > 0 )
                    send_view.setVisibility(View.VISIBLE);
                else send_view.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RecyclerView comment_recyclerview = findViewById(R.id.comment_recyclerview);
        mAdapter = new CommentRecyclerViewAdapter(this, mComments, mPost.getPostId());
        comment_recyclerview.setAdapter(mAdapter);
        comment_recyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mAdapter.notifyDataSetChanged();

        initWithData();
    }

    private void initWithData() {
        FireConstants.postsRef.child(mPost.getPostId()).child("postComments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if( snapshot.getValue() != null ) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if( mComments != null && mComments.size() > 0 ) {
                        boolean isContain = false;
                        for( int i=0; i< mComments.size(); i++ ) {
                            if(mComments.get(i).getTime().equals(comment.getTime())) {
                                isContain = true;
                                break;
                            }
                        }
                        if (!isContain ) {
                            mComments.add(comment);
                        }
                    } else {
                        mComments.add(comment);
                    }
                    sortComments();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Comment comment = snapshot.getValue(Comment.class);
                if( mComments != null && mComments.size() > 0 ) {
                    for( int i=0; i< mComments.size(); i++ ) {
                        if (mComments.get(i).getTime().equals(comment.getTime())) {
                            mComments.set(i, comment);
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sortComments() {
        Collections.sort(mComments, (o1, o2) -> o2.getTime().compareTo(o1.getTime()));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
