package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.job.SetStatusSeenJob;
import com.hostcart.socialbot.model.TextStatus;
import com.hostcart.socialbot.model.constants.StatusType;
import com.hostcart.socialbot.model.realms.Status;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.StatusManager;
import com.hostcart.socialbot.utils.TimeHelper;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmResults;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import me.zhanghai.android.systemuihelper.SystemUiHelper;
import ooo.oxo.library.widget.PullBackLayout;

import static com.hostcart.socialbot.utils.FontUtil.isFontExists;

public class ViewStatusActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener, PullBackLayout.Callback {

    String userId;
    StoriesProgressView storiesProgressView;
    private ImageView image;

    private VideoView videoView;
    private CircleImageView profileImage;
    private TextView tvUsername;
    private TextView tvStatusTime;
    private ImageButton backButton;
    private ConstraintLayout root;
    private TextView tvStatus;

    MediaPlayer.OnPreparedListener onPreparedListener;
    MediaPlayer.OnErrorListener onErrorListener;
    private ProgressBar progressBar;
    private StatusManager.OnStatusDownloadComplete onStatusDownloadComplete;

    private int counter = 0;

    //image story duration 7 seconds
    public static final long IMAGE_STORY_DURATION = 7000L;
    //text story duration 6 seconds
    private static final long TEXT_STORY_DURATION = 6000L;

    long pressTime = 0L;
    long limit = 500L;
    RealmResults<Status> statuses;


    //on touch listener, when user holds his thumb it will pause the story,and when he release it will resume
    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener onTouchListener = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pause();
                return false;
            case MotionEvent.ACTION_UP:
                return resume();
        }

        return false;
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_view_status_dark);
        } else {
            setContentView(R.layout.activity_view_status_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        userId = getIntent().getStringExtra(IntentUtils.UID);
        statuses = RealmHelper.getInstance().getUserStatuses(userId).getFilteredStatuses();
        User user;

        if (userId.equals(FireManager.getUid()))
            user = SharedPreferencesManager.getCurrentUser();
        else
            user = RealmHelper.getInstance().getUser(userId);

        new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY).hide();

        initViews();

        //set stories durations
        storiesProgressView.setStoriesCountWithDurations(getDurations());
        storiesProgressView.setStoriesListener(this);

        //onVideo prepared listener
        onPreparedListener = mp -> {
            image.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            storiesProgressView.start(counter);
            videoView.start();
        };

        onErrorListener = (mediaPlayer, i, i1) -> {
            Toast.makeText(ViewStatusActivity.this, R.string.error_playing_this, Toast.LENGTH_SHORT).show();
            return true;
        };

        initStatusDownloadCompleteCallback();

        loadStatus(statuses.get(0));
        storiesProgressView.startStories(counter);


        // bind reverse view
        //play the previous story (onClick)
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(v -> storiesProgressView.reverse(counter));

        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        //play the next story (onClick)
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(v -> storiesProgressView.skip(counter));
        skip.setOnTouchListener(onTouchListener);


        //back button in toolbar onClick
        backButton.setOnClickListener(view -> finish());

        PullBackLayout pullBackLayout = findViewById(R.id.pull);
        pullBackLayout.setCallback(this);

        setUserInfo(user);
    }

    @Override
    protected void onDestroy() {
        videoView.stopPlayback();
        videoView.setOnPreparedListener(null);
        videoView.setOnErrorListener(null);
        storiesProgressView.destroy();
        super.onDestroy();
    }

    private boolean resume() {
        long now = System.currentTimeMillis();

        //resume video if needed
        if (statuses.get(counter).getType() == StatusType.VIDEO && !videoView.isPlaying()) {
            videoView.start();
        }
        storiesProgressView.resume();
        return limit < now - pressTime;
    }


    private void pause() {
        pressTime = System.currentTimeMillis();
        storiesProgressView.pause();
        //pause video if needed
        if (statuses.get(counter).getType() == StatusType.VIDEO && videoView.isPlaying()) {
            videoView.pause();
        }
    }


    private void initStatusDownloadCompleteCallback() {
        if (onStatusDownloadComplete == null) {
            onStatusDownloadComplete = path -> {
                if (path != null) {
                    playVideo(path);
                }
            };
        }
    }

    private void initViews() {
        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        videoView = findViewById(R.id.video_view);
        profileImage = findViewById(R.id.profile_image);
        tvUsername = findViewById(R.id.tv_username);
        tvStatusTime = findViewById(R.id.tv_status_time);
        backButton = findViewById(R.id.back_button);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);
        root = findViewById(R.id.root);
        storiesProgressView.setStoriesCount(statuses.size());
    }

    @Override
    public void onNext() {
        videoView.stopPlayback();
        videoView.setOnPreparedListener(null);
        videoView.setOnErrorListener(null);
        onStatusDownloadComplete = null;
        if (counter + 1 > statuses.size() - 1)
            return;
        counter++;
        loadStatus(statuses.get(counter));
    }

    @Override
    public void onPrev() {

        videoView.stopPlayback();
        videoView.setOnPreparedListener(null);
        videoView.setOnErrorListener(null);
        onStatusDownloadComplete = null;
        if ((counter - 1) < 0) return;
        counter--;
        loadStatus(statuses.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    private void loadStatus(final Status status) {
        setStatusTime(status.getTimestamp());
        storiesProgressView.setCurrent(counter);
        videoView.setVisibility(View.GONE);
        image.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        image.setVisibility(View.VISIBLE);
        tvStatus.setVisibility(View.GONE);
        root.setBackgroundColor(Color.BLACK);

        //load thumb blurred image while loading original image or video
        if (status.getType() == StatusType.IMAGE || status.getType() == StatusType.VIDEO)
            image.setImageBitmap(BitmapUtils.simpleBlur(this, BitmapUtils.encodeImage(status.getThumbImg())));

        if (status.getType() == StatusType.IMAGE) {
            loadImage(status);
        } else if (status.getType() == StatusType.VIDEO) {
            loadVideo(status);
        } else if (status.getType() == StatusType.TEXT) {
            progressBar.setVisibility(View.GONE);
            tvStatus.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            loadTextStatus(status.getTextStatus());
        }

        //set status as seen
        if (!status.isSeen()) {
            RealmHelper.getInstance().setStatusAsSeen(status.getStatusId());
            //check if all statuses are seen and save it
            if (status.getStatusId().equals(statuses.last().getStatusId()))
                RealmHelper.getInstance().setAllStatusesAsSeen(userId);
        }

        //Schedule a job to update status count on Firebase
        if (!status.getUserId().equals(FireManager.getUid()) && !status.isSeenCountSent()) {
            SetStatusSeenJob.schedule(userId, status.getStatusId());
        }
    }

    private void loadTextStatus(TextStatus textStatus) {
        root.setBackgroundColor(Color.parseColor(textStatus.getBackgroundColor()));
        String fontName = textStatus.getFontName();

        if (isFontExists(fontName)) {
            tvStatus.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/" + fontName));
        }

        tvStatus.setText(textStatus.getText());
        storiesProgressView.start(counter);
    }


    private void loadImage(Status status) {
        //if this status by this user load it locally ,otherwise load it from server and cache it
        String url = status.getLocalPath() == null ? status.getContent() : status.getLocalPath();

        Glide.with(this).load(url)
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        progressBar.setVisibility(View.GONE);
                        storiesProgressView.start(counter);
                        image.setImageDrawable(resource);
                    }
                });
    }

    private void loadVideo(Status status) {
        //if the video is not exists download it
        if (status.getLocalPath() == null) {
            downloadStatusVideo(status);
        } else {
            //if the video is exists in device play it
            if (FileUtils.isFileExists(status.getLocalPath())) {
                playVideo(status.getLocalPath());
            } else {
                //otherwise download it
                downloadStatusVideo(status);
            }
        }
    }


    private void downloadStatusVideo(Status status) {
        initStatusDownloadCompleteCallback();
        File statusFile = DirManager.getReceivedStatusFile(status.getStatusId(), status.getType());
        StatusManager.downloadVideoStatus(status.getStatusId(), status.getContent(), statusFile, onStatusDownloadComplete);
    }

    private void playVideo(String path) {
        videoView.requestFocus();
        videoView.setVideoURI(Uri.parse(path));
        videoView.setVisibility(View.VISIBLE);
        videoView.setOnPreparedListener(onPreparedListener);
        videoView.setOnErrorListener(onErrorListener);
    }


    //get statuses durations
    private long[] getDurations() {
        long[] array = new long[statuses.size()];
        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            //if it's an image set its duration to IMAGE_STORY_DURATION
            if (status.getType() == StatusType.IMAGE) {
                array[i] = IMAGE_STORY_DURATION;
            } else if (status.getType() == StatusType.TEXT) {
                array[i] = TEXT_STORY_DURATION;
            } else {
                //if it's a video set its duration to the video duration
                array[i] = status.getDuration();
            }
        }
        return array;
    }

    @Override
    public void onPullStart() {

    }

    @Override
    public void onPull(float v) {

    }

    @Override
    public void onPullCancel() {

    }

    //when the user swipes vertically exit the activity
    @Override
    public void onPullComplete() {
        finish();
    }

    //set status time
    private void setStatusTime(long timestamp) {
        tvStatusTime.setText(TimeHelper.getStatusTime(timestamp));
    }

    //set user image and user info
    private void setUserInfo(User user) {
        Glide.with(this).asBitmap().load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).into(profileImage);
        if (user.getUid().equals(FireManager.getUid()))
            tvUsername.setText(getResources().getString(R.string.you));
        else
            tvUsername.setText(user.getUserName());
    }

}
