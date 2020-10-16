package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.PostsAdapter;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.model.Review;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.PostManager;
import com.hostcart.socialbot.utils.PostMedia;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EnlargyActivity extends AppCompatActivity {

    private int currentApiVersion;
    private boolean isDisableUI;
    private MediaController mediaController;
    private MediaPlayer.OnErrorListener onErrorListener;

    private ImageView enlarge_image;
    private VideoView enlarge_video;
    private ProgressBar progressBar;

    private PostManager.OnPostDownloadComplete onPostDownloadComplete;
    private List<ImageView> postReviews = new ArrayList<>();


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_enlargy_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_enlargy_light);
        }

        // full screen
        currentApiVersion = Build.VERSION.SDK_INT;
        final int layoutFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(layoutFlags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(layoutFlags);
                }
            });
        }

        onErrorListener = (mediaPlayer, i, i1) -> {
            if (progressBar.getVisibility() != View.GONE) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(EnlargyActivity.this, R.string.error_playing_this, Toast.LENGTH_SHORT).show();
            return true;
        };

        Posts mPost = AppUtils.gPosts;
        PostMedia media = AppUtils.gPostMedia;

        if( mPost == null || media == null )
            return;

        // show image
        enlarge_image = findViewById(R.id.enlarge_image);
        enlarge_video = findViewById(R.id.enlarg_video);
        WebView enlarg_webview = findViewById(R.id.enlarg_webview);

        progressBar = findViewById(R.id.video_progress);

        switch (media.getType()) {
            case 1:
                progressBar.setVisibility(View.GONE);
                enlarge_image.setVisibility(View.VISIBLE);
                enlarge_video.setVisibility(View.GONE);
                enlarg_webview.setVisibility(View.GONE);

                loadImage(media);
                break;
            case 2:
                progressBar.setVisibility(View.VISIBLE);
                enlarge_image.setVisibility(View.GONE);
                enlarge_video.setVisibility(View.VISIBLE);
                enlarg_webview.setVisibility(View.GONE);

                mediaController = new MediaController(this);
                mediaController.setAnchorView(enlarge_video);

                loadVideo(media);
                break;
            case 3:
                progressBar.setVisibility(View.GONE);
                enlarge_image.setVisibility(View.GONE);
                enlarge_video.setVisibility(View.GONE);
                enlarg_webview.setVisibility(View.VISIBLE);
                enlarg_webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
                String loadData = "<html><body style='margin:0px;padding:0px;'>\n" +
                        "        <script type='text/javascript' src='http://www.youtube.com/iframe_api'></script><script type='text/javascript'>\n" +
                        "                var player;\n" +
                        "        function onYouTubeIframeAPIReady()\n" +
                        "        {player=new YT.Player('playerId',{events:{onReady:onPlayerReady}})}\n" +
                        "        function onPlayerReady(event){player.setVolume(100);player.playVideo();}\n" +
                        "        </script>\n" +
                        "        <iframe id='playerId' type='text/html' width='100%' height='100%'\n" +
                        "        src='https://www.youtube.com/embed/" + media.getThumbImg() + "?enablejsapi=1&rel=0&playsinline=1&autoplay=1&showinfo=0&autohide=1&modestbranding=1' frameborder='0'>\n" +
                        "        </body></html>";
                enlarg_webview.loadData(loadData, "text/html", "utf-8");
                enlarg_webview.getSettings().setJavaScriptEnabled(true);
                enlarg_webview.setWebViewClient(new WebViewClient(){
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        view.loadUrl("javascript:onYouTubeIframeAPIReady();");
                    }
                });
                break;
        }

        enlarge_video.setOnCompletionListener(mp -> { });
        enlarge_video.setOnPreparedListener(mp -> progressBar.setVisibility(View.GONE));

        enlarge_image.setOnClickListener(v -> isDisableUI = !isDisableUI);

        // UI Layout
        TextView show_like_text = findViewById(R.id.show_like_text);
        TextView show_comment_text = findViewById(R.id.show_comment_text);

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

        for (ImageView img_review: postReviews) {
            img_review.setVisibility(View.GONE);
        }
        if (mPost.getReviews().size() == 0) {
            postReviews.get(0).setVisibility(View.VISIBLE);
            show_like_text.setText(getString(R.string.normal_no_review));
        } else {
            show_like_text.setText(String.valueOf(mPost.getReviews().size()));
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

        // show comment
        int comments = PostManager.getCommentCount(mPost.getPostComments());
        show_comment_text.setText(String.format(Locale.US, "%d Comments", comments));

        LinearLayout click_like = findViewById(R.id.click_like);
        click_like.setOnClickListener(v -> {

        });

        // click comment
        LinearLayout click_comment = findViewById(R.id.click_comment);
        click_comment.setOnClickListener(v -> {
            Intent intent = new Intent(EnlargyActivity.this, CommentsActivity.class);
            startActivity(intent);
        });

        // click share
        LinearLayout click_share = findViewById(R.id.click_share);
        click_share.setOnClickListener(v -> shareMedia());
    }

    private void shareMedia() {
        if (AppUtils.gPostMedia.getType() == 3) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
            intent.putExtra(Intent.EXTRA_TEXT, AppUtils.gPostMedia.getContent());
            startActivity(Intent.createChooser(intent, "Share Via"));
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putExtra(Intent.EXTRA_SUBJECT, "SocialBot Sharing ...");
            if (!AppUtils.gPosts.getPostText().isEmpty()) {
                intent.putExtra(Intent.EXTRA_TEXT, AppUtils.gPosts.getPostText());
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("*/*");
            ArrayList<Uri> files = new ArrayList<>();
            downloadPostMediaFile(AppUtils.gPostMedia, AppUtils.gPosts.getPostId(), new PostsAdapter.PostMediaDownloadCallback() {
                @Override
                public void onSuccess(String file_path) {
                    String[] splitUrl = file_path.split("/");
                    String fileName = splitUrl[splitUrl.length - 1];
                    String path = Environment.getExternalStorageDirectory().toString() + "/Socialbot/Post/" +fileName;
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                }

                @Override
                public void onFailed(String error) { }
            });
        }
    }

    private void downloadPostMediaFile(PostMedia postMedia, String postid, PostsAdapter.PostMediaDownloadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(postMedia.getContent());

        String type;
        if (postMedia.getType() == 2) {
            type = ".mp4";
        } else {
            type = ".jpg";
        }

        File rootPath = new File(Environment.getExternalStorageDirectory(), "Socialbot/Post/");
        if(!rootPath.exists()) {
            boolean rootable = rootPath.mkdirs();
            if (!rootable) {
                callback.onFailed("Permission Denied.");
                return;
            }
        }

        long tsLong = System.currentTimeMillis()/1000;
        final File localFile = new File(rootPath, postid + "_" + tsLong + type);
        if (localFile.exists()) {
            callback.onSuccess(localFile.getAbsolutePath());
            return;
        }

        storageRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> callback.onSuccess(localFile.getAbsolutePath()))
                .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
    }

    private void loadImage( PostMedia media ) {
        String url;
        if( media.getUserId().equals(FireManager.getUid()) )
            url = media.getLocalPath() == null ? media.getContent() : media.getLocalPath();
        else
            url = media.getContent();

        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(enlarge_image);
    }

    private void loadVideo( PostMedia media ) {
        String localPath;

        if( media.getUserId().equals(FireManager.getUid()) )
            localPath = media.getLocalPath();
        else
            localPath = DirManager.getReceivedPostFile(media.getLocalPath()).getAbsolutePath();

        if( localPath == null ) {
            downloadVideo(media);
        } else {
            if (FileUtils.isFileExists(localPath)) {
                playVideo(localPath);
            } else {
                downloadVideo(media);
            }
        }
    }

    private void downloadVideo( PostMedia media ) {
        initStatusDownloadCompleteCallback();
        File postFile = DirManager.getReceivedPostFile(media.getLocalPath());
        PostManager.downloadVideoPost(media.getMediaId(), media.getContent(), postFile, onPostDownloadComplete);
    }

    private void playVideo( String path ) {
        progressBar.setVisibility(View.GONE);
        enlarge_video.setMediaController(mediaController);
        enlarge_video.requestFocus();
        enlarge_video.setVideoURI(Uri.parse(path));
        enlarge_video.setOnErrorListener(onErrorListener);
        enlarge_video.start();
    }

    private void initStatusDownloadCompleteCallback() {
        if (onPostDownloadComplete == null) {
            onPostDownloadComplete = path -> {
                if (path != null) {
                    playVideo(path);
                }
            };
        }
    }

    @Override
    protected void onDestroy() {
        enlarge_video.stopPlayback();
        enlarge_video.setOnPreparedListener(null);
        enlarge_video.setOnErrorListener(null);

        super.onDestroy();
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

}
