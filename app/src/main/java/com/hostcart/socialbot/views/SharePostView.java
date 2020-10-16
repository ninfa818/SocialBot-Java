package com.hostcart.socialbot.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.fragments.PostFragment;
import com.hostcart.socialbot.model.Post;
import com.hostcart.socialbot.utils.KeyboardHelper;
import com.hostcart.socialbot.utils.PostManager;

import de.hdodenhof.circleimageview.CircleImageView;
import io.codetail.animation.SupportAnimator;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class SharePostView extends RelativeLayout implements View.OnClickListener {

    private CardView cardView;
    private CircleImageView photoView;
    private TextView usernameView;
    private EditText contentEdit;
    private Button sharenowButton;
    private Context mContext;
    private Post mPost;
    private PostFragment fragment;

    private boolean isOpen;

    public SharePostView(Context context) {
        super(context);

        init(context);
    }

    public SharePostView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public SharePostView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init( Context context ) {
        mContext = context;

        View view = View.inflate(context, R.layout.post_share_item, null);

        final LayoutParams lp = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        view.setLayoutParams(lp);

        addView(view);

        cardView = view.findViewById(R.id.cardview_sharepost);
        photoView = view.findViewById(R.id.share_photo);
        usernameView = view.findViewById(R.id.share_username);
        contentEdit = view.findViewById(R.id.share_content_edit);
        sharenowButton = view.findViewById(R.id.sharenow_button);

        cardView.setVisibility(GONE);

        sharenowButton.setOnClickListener(this);
    }

    public void setPhotoAndName( String uri, String username ) {

        usernameView.setText(username);

    }

    @Override
    public void onClick(View v) {
        // hide
        hide(v);
        // process
//        if( mPost == null ) return;
//        PostManager.createRepost(contentEdit.getText().toString(), mPost, fragment);
    }

    private void animate(View v) {

        int w = cardView.getWidth();
        int h = cardView.getHeight();

        int finalRadius = (int) Math.hypot(w, h);


        //get attachment button x coordinates to start the animation from
        int cx = (int) v.getX();

        int cy = cardView.getBottom();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {


            SupportAnimator animator =
                    io.codetail.animation.ViewAnimationUtils.createCircularReveal(cardView, cx, cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(1500);

            SupportAnimator animator_reverse = animator.reverse();

            //if it's not open ,open it
            if (!isOpen) {
                cardView.setVisibility(View.VISIBLE);
                animator.start();
                isOpen = true;
            } else {
                //if otherwise close it
                animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                    @Override
                    public void onAnimationStart() {

                    }

                    @Override
                    public void onAnimationEnd() {
                        cardView.setVisibility(View.INVISIBLE);
                        isOpen = false;

                    }

                    @Override
                    public void onAnimationCancel() {

                    }

                    @Override
                    public void onAnimationRepeat() {

                    }
                });
                animator_reverse.start();

            }
        } else {
            if (!isOpen) {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(cardView, cx, cy, 0, finalRadius);
                cardView.setVisibility(View.VISIBLE);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        /*bounceAnimations();*/
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                anim.start();
                isOpen = true;

            } else {
                Animator anim = android.view.ViewAnimationUtils.createCircularReveal(cardView, cx, cy, finalRadius, 0);
                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        cardView.setVisibility(View.INVISIBLE);
                        isOpen = false;
                    }
                });
                anim.start();

            }
        }
    }

    public void setData(Post post, PostFragment fragment) {
        mPost = post;
        this.fragment = fragment;
    }

    public void show(View v, String uri, String username) {
        cardView.setVisibility(View.VISIBLE);
        Glide.with(mContext)
                .asBitmap()
                .load(Uri.parse(uri))
                .into(photoView);
        usernameView.setText(username);
        //animate(v);
    }

    public void hide( View v ) {
        cardView.setVisibility(View.GONE);
        KeyboardHelper.hideSoftKeyboard(this.getContext(), v);
        isOpen = true;
        //animate(v);
    }
}
