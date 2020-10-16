package com.hostcart.socialbot.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.Comment;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReplyCommentView extends LinearLayout {

    private Comment reply;

    public ReplyCommentView(Context context, Comment comment) {
        super(context);

        this.reply = comment;

        setOrientation(LinearLayout.HORIZONTAL);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            LayoutInflater.from(context).inflate(R.layout.row_comment_reply_dark, this, true);
        } else {
            LayoutInflater.from(context).inflate(R.layout.row_comment_reply_light, this, true);
        }

        init();
    }

    private void init() {
        CircleImageView img_user = findViewById(R.id.user_image);
        TextView lbl_user = findViewById(R.id.comment_username);
        TextView lbl_content = findViewById(R.id.comment_content);
        TextView lbl_time = findViewById(R.id.comment_time);

        if (!reply.getPhotoUrl().equals("")) {
            Glide.with(getContext())
                    .asBitmap()
                    .load(BitmapUtils.encodeImageAsBytes(reply.getPhotoUrl()))
                    .into(img_user);
        }

        lbl_user.setText(reply.getUserName());
        lbl_content.setText(reply.getContent());
        long currentTime = Long.parseLong(reply.getTime());
        Date currentDate = new Date(currentTime);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        lbl_time.setText(df.format(currentDate));
    }

}
