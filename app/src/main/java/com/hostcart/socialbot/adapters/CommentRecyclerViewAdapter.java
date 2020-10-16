package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.Comment;
import com.hostcart.socialbot.model.Review;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.views.ReplyCommentView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentViewHolder> {

    private Context mContext;
    private List<Comment> mComments;
    private String mPostId;

    public CommentRecyclerViewAdapter(Context mContext, List<Comment> mComments, String postUid) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.mPostId = postUid;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new CommentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.row_comment_dark, parent, false));
        } else {
            return new CommentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.row_comment_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = mComments.get(position);

        if( comment == null ) return;

        if (!comment.getPhotoUrl().equals("")) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(BitmapUtils.encodeImageAsBytes(comment.getPhotoUrl()))
                    .into(holder.user_image);
        }

        holder.username_text.setText(comment.getUserName());
        holder.content_text.setText(comment.getContent());

        long currentTime = Long.parseLong(comment.getTime());
        Date currentDate = new Date(currentTime);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        holder.time_text.setText(df.format(currentDate));

        holder.llt_comment_view.setOnClickListener(v -> holder.llt_comment.setVisibility(View.VISIBLE));
        holder.llt_reply.setOnClickListener(v -> {
            if (comment.getUserid().equals(FireManager.getUid())) {
                Toast.makeText(mContext, R.string.toast_yours, Toast.LENGTH_SHORT).show();
                return;
            }
            onAddReplyComment(comment);
        });

        List<Review> reviews = new ArrayList<>(comment.getReviews());
        for (ImageView img_review: holder.lst_reviews) {
            img_review.setVisibility(View.GONE);
        }
        if (reviews.size() == 0) {
            holder.lst_reviews.get(0).setVisibility(View.VISIBLE);
            holder.review_text.setText(mContext.getString(R.string.normal_no_review));
        } else {
            holder.review_text.setText(String.valueOf(reviews.size()));
            boolean[] flags = new boolean[] {
                    false, false, false, false, false, false
            };
            for (Review review: reviews) {
                flags[Integer.parseInt(review.getType())] = true;
            }
            for (int i = 1; i < holder.lst_reviews.size(); i++) {
                ImageView img_review = holder.lst_reviews.get(i);
                if (flags[i - 1]) {
                    img_review.setVisibility(View.VISIBLE);
                }
            }
        }

        holder.llt_comment.setVisibility(View.GONE);
        for (int i = 0; i < holder.lst_set_reviews.size(); i++) {
            ImageView img_set = holder.lst_set_reviews.get(i);
            int finalI = i;
            img_set.setOnClickListener(v -> updateReview(holder, comment, finalI));
        }

        holder.llt_comment_reply.removeAllViews();
        for (Comment reply: comment.getReplies()) {
            ReplyCommentView commentView = new ReplyCommentView(mContext, reply);
            holder.llt_comment_reply.addView(commentView);
        }
    }

    private void onAddReplyComment(Comment comment) {
        showEditTextDialog(mContext.getString(R.string.hint_reply), text -> {
            Comment replyComment = new Comment();
            replyComment.setPhotoUrl(SharedPreferencesManager.getThumbImg());
            replyComment.setContent(text);
            replyComment.setUserName(SharedPreferencesManager.getUserName());
            replyComment.setTime(String.format(Locale.US, "%d", System.currentTimeMillis()));
            replyComment.setUserid(FireManager.getUid());

            comment.addReplies(replyComment);
            notifyDataSetChanged();
            FireConstants.postsRef.child(mPostId).child("postComments").child(comment.getTime()).child("replies").setValue(comment.getReplies());
        });
    }

    private void updateReview(CommentViewHolder holder, Comment comment, int type) {
        holder.llt_comment.setVisibility(View.GONE);

        if (comment.getUserid().equals(FireManager.getUid())) {
            Toast.makeText(mContext, R.string.toast_yours, Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = new Review();
        review.setTime("");
        review.setType(String.valueOf(type));
        review.setUserid(FireManager.getUid());

        int index = -1;
        for (int i = 0; i < comment.getReviews().size(); i++) {
            Review reviewCell = comment.getReviews().get(i);
            if (reviewCell.getUserid().equals(FireManager.getUid())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            comment.updateReviews(index, review);
        } else {
            comment.addReviews(review);
        }

        notifyDataSetChanged();
        FireConstants.postsRef.child(mPostId).child("postComments").child(comment.getTime()).child("reviews").setValue(comment.getReviews());
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView user_image;
        TextView username_text;
        TextView content_text;
        TextView time_text;
        TextView review_text;
        LinearLayout llt_comment, llt_comment_view, llt_reply, llt_comment_reply;

        private List<ImageView> lst_reviews = new ArrayList<>();
        private List<ImageView> lst_set_reviews = new ArrayList<>();

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.user_image);
            username_text = itemView.findViewById(R.id.comment_username);
            content_text = itemView.findViewById(R.id.comment_content);
            time_text = itemView.findViewById(R.id.comment_time);
            review_text = itemView.findViewById(R.id.comment_review);
            llt_comment = itemView.findViewById(R.id.llt_comment_set);
            llt_comment_view = itemView.findViewById(R.id.llt_comment_view);
            llt_reply = itemView.findViewById(R.id.llt_comment_reply);
            llt_comment_reply = itemView.findViewById(R.id.llt_reply);

            ImageView img_review_0 = itemView.findViewById(R.id.img_comment_review_0);
            ImageView img_review_1 = itemView.findViewById(R.id.img_comment_review_1);
            ImageView img_review_2 = itemView.findViewById(R.id.img_comment_review_2);
            ImageView img_review_3 = itemView.findViewById(R.id.img_comment_review_3);
            ImageView img_review_4 = itemView.findViewById(R.id.img_comment_review_4);
            ImageView img_review_5 = itemView.findViewById(R.id.img_comment_review_5);
            ImageView img_review_6 = itemView.findViewById(R.id.img_comment_review_6);
            lst_reviews.add(img_review_0);
            lst_reviews.add(img_review_1);
            lst_reviews.add(img_review_2);
            lst_reviews.add(img_review_3);
            lst_reviews.add(img_review_4);
            lst_reviews.add(img_review_5);
            lst_reviews.add(img_review_6);

            ImageView img_set_0 = itemView.findViewById(R.id.img_set_review_0);
            ImageView img_set_1 = itemView.findViewById(R.id.img_set_review_1);
            ImageView img_set_2 = itemView.findViewById(R.id.img_set_review_2);
            ImageView img_set_3 = itemView.findViewById(R.id.img_set_review_3);
            ImageView img_set_4 = itemView.findViewById(R.id.img_set_review_4);
            ImageView img_set_5 = itemView.findViewById(R.id.img_set_review_5);
            lst_set_reviews.add(img_set_0);
            lst_set_reviews.add(img_set_1);
            lst_set_reviews.add(img_set_2);
            lst_set_reviews.add(img_set_3);
            lst_set_reviews.add(img_set_4);
            lst_set_reviews.add(img_set_5);
        }

    }

    private void showEditTextDialog(String message, EditTextDialogListener listener) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(mContext, R.style.AlertDialogDark);
            EmojiconEditText edittext = new EmojiconEditText(mContext);
            alert.setMessage(message);
            alert.setView(edittext);
            alert.setNegativeButton(R.string.ok, (dialog, whichButton) -> {
                if (listener != null)
                    listener.onOk(edittext.getText().toString());
            });
            alert.setNeutralButton(R.string.cancel, null);
            alert.show();
        } else {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(mContext, R.style.AlertDialogLight);
            EmojiconEditText edittext = new EmojiconEditText(mContext);
            alert.setMessage(message);
            alert.setView(edittext);
            alert.setNegativeButton(R.string.ok, (dialog, whichButton) -> {
                if (listener != null)
                    listener.onOk(edittext.getText().toString());
            });
            alert.setNeutralButton(R.string.cancel, null);
            alert.show();
        }
    }

    public interface EditTextDialogListener {
        void onOk(String text);
    }

}
