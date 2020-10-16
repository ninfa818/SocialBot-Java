package com.hostcart.socialbot.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.CommentsActivity;
import com.hostcart.socialbot.activities.EnlargyActivity;
import com.hostcart.socialbot.activities.MyProfileActivity;
import com.hostcart.socialbot.activities.NewPostActivity;
import com.hostcart.socialbot.activities.UserProfileActivity;
import com.hostcart.socialbot.fragments.PostFragment;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.model.Review;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.PostManager;
import com.hostcart.socialbot.utils.PostMedia;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.views.PostGridView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private final int TYPE_IMGVIDEO = 0;
    private final int TYPE_TEXT = 1;
    private final int TYPE_LOCATION = 2;

    private List<Posts> posts;
    private Context context;
    private PostFragment fragment;

    public PostsAdapter(List<Posts> posts, PostFragment postFragment) {
        this.posts = posts;
        this.fragment = postFragment;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wall_post_dark, parent, false), parent.getContext());
        } else {
            return new PostViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wall_post_light, parent, false), parent.getContext());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Posts post = posts.get(position);
        if( post == null ) return;
        switch (post.getPostType()) {
            case TYPE_IMGVIDEO:
                onBindImgVideoPost(holder, post);
                break;
            case TYPE_TEXT:
                onBindTextPost(holder, post);
                break;
            case TYPE_LOCATION:
                onBindLocationPost(holder, post);
                break;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull PostViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    private void onBindImgVideoPost(PostViewHolder holder, Posts post ) {
        onBindSameObject(holder, post);
        holder.locationRayout.setVisibility(View.GONE);
        HashMap<String,Object> hashMap = post.getPostMedias();
        if( hashMap == null ) return;

        List<PostMedia> postMedias = new ArrayList<>();
        if( hashMap.entrySet().size() > 1 ) {
            holder.singleRayout.setVisibility(View.GONE);
            holder.multipleMediaView.setVisibility(View.VISIBLE);
            postMedias.addAll(getPostMedia(hashMap));

            WallPostGridViewAdapter wallAdapter = new WallPostGridViewAdapter(
                    holder.multipleMediaView.getContext(), postMedias);

            holder.multipleMediaView.setAdapter(wallAdapter);
            wallAdapter.notifyDataSetChanged();

            holder.multipleMediaView.setOnItemClickListener((parent, view, position, id) -> {
                PostMedia postMedia = postMedias.get(position);
                if( postMedia != null ) {
                    Intent intent = new Intent(context, EnlargyActivity.class);
                    AppUtils.gPosts = post;
                    AppUtils.gPostMedia = postMedia;
                    context.startActivity(intent);
                }
            });
        } else {
            holder.singleRayout.setVisibility(View.VISIBLE);
            holder.multipleMediaView.setVisibility(View.GONE);
            postMedias.addAll(getPostMedia(hashMap));
            if (postMedias.get(0).getType() == 3) {
                String url = "https://img.youtube.com/vi/"  + postMedias.get(0).getThumbImg() + "/mqdefault.jpg";
                Glide.with(context)
                        .load(url)
                        .into(holder.singleThumbImage);
            } else {
                Glide.with(context)
                        .asBitmap()
                        .load(BitmapUtils.encodeImageAsBytes(postMedias.get(0).getThumbImg()))
                        .into(holder.singleThumbImage);
            }

            if( postMedias.get(0).getType() > 1 )
                holder.videoIconImage.setVisibility(View.VISIBLE);
            else
                holder.videoIconImage.setVisibility(View.GONE);

            holder.singleThumbImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, EnlargyActivity.class);
                AppUtils.gPosts = post;
                AppUtils.gPostMedia = postMedias.get(0);
                context.startActivity(intent);
            });
        }
    }

    private List<PostMedia> getPostMedia( HashMap<String,Object> hashMap) {
        List<PostMedia> postMediaList = new ArrayList<>();

        for(Map.Entry<String, Object> entry :hashMap.entrySet()) {
            PostMedia postMedia = new PostMedia();
            HashMap<String,Object> value = (HashMap<String,Object>)entry.getValue();
            for(Map.Entry<String, Object> ventry :value.entrySet()) {
                String sub_key = ventry.getKey();
                Object sub_value = ventry.getValue();

                switch (sub_key) {
                    case "content":
                        postMedia.setContent(sub_value.toString());
                        break;
                    case "duration":
                        postMedia.setDuration(Long.parseLong(sub_value.toString()));
                        break;
                    case "timestamp":
                        postMedia.setTimestamp(Long.parseLong(sub_value.toString()));
                        break;
                    case "type":
                        postMedia.setType(Integer.parseInt(sub_value.toString()));
                        break;
                    case "thumbImg":
                        postMedia.setThumbImg(sub_value.toString());
                        break;
                    case "userId":
                        postMedia.setUserId(sub_value.toString());
                        break;
                    case "localPath":
                        postMedia.setLocalPath(sub_value.toString());
                        break;
                }
            }
            postMediaList.add(postMedia);
        }
        return postMediaList;
    }

    private void onBindTextPost(PostViewHolder holder, Posts post ) {
        onBindSameObject(holder, post);

        holder.locationRayout.setVisibility(View.GONE);
        holder.singleRayout.setVisibility(View.GONE);
        holder.multipleMediaView.setVisibility(View.GONE);
    }

    private void onBindLocationPost(PostViewHolder holder, Posts post ) {
        onBindSameObject(holder, post);

        holder.locationRayout.setVisibility(View.VISIBLE);
        holder.singleRayout.setVisibility(View.GONE);
        holder.multipleMediaView.setVisibility(View.GONE);

        String[] latlong =  post.getPostLocation().split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);

        holder.setMapLocation(new LatLng(latitude, longitude));
    }

    private void onBindSameObject(PostViewHolder holder, Posts post ) {
        Glide.with(context)
                .asBitmap()
                .load(BitmapUtils.encodeImageAsBytes(post.getPostPhotoUrl()))
                .into(holder.postUserImage);

        // user name
        holder.postUserNameView.setText(post.getPostName());
        FireManager.getUserInfoByUid(post.getPostUid(), new FireManager.userInfoListener() {
            @Override
            public void onFound(UserInfo userInfo) {
                holder.postUserNameView.setText(userInfo.getName() + " " + userInfo.getSurname());
            }

            @Override
            public void onNotFound() {

            }
        });

        // post time
        String pt_time = convertMilisecToDate(post.getPostTime());
        holder.postTimeView.setText(pt_time);

        // post content
        if( post.getPostText() != null && !post.getPostText().equals("") ) {
            holder.postContentView.setVisibility(View.VISIBLE);
            holder.postContentView.setText(post.getPostText());
        } else {
            holder.postContentView.setVisibility(View.GONE);
        }

        // post likes
        holder.postComments.setText(String.valueOf(PostManager.getCommentCount(post.getPostComments())));
        holder.postLikeLayout.setOnClickListener(v -> holder.llt_set.setVisibility(View.VISIBLE));
        for (int i = 0; i < holder.lst_set_reviews.size(); i++) {
            ImageView img_set = holder.lst_set_reviews.get(i);
            int finalI = i;
            img_set.setOnClickListener(v -> {
                holder.llt_set.setVisibility(View.GONE);
                updateReview(finalI, post);
            });
        }
        for (ImageView img_show: holder.lst_show_reviews) {
            img_show.setVisibility(View.GONE);
        }
        if (post.getReviews().size() == 0) {
            holder.lst_show_reviews.get(0).setVisibility(View.VISIBLE);
            holder.postReview.setText(context.getString(R.string.normal_no_review));
        } else {
            holder.postReview.setText(String.valueOf(post.getReviews().size()));
            boolean[] flags = new boolean[] {
                    false, false, false, false, false, false
            };
            for (Review review: post.getReviews()) {
                flags[Integer.parseInt(review.getType())] = true;
            }
            for (int i = 1; i < holder.lst_show_reviews.size(); i++) {
                ImageView img_review = holder.lst_show_reviews.get(i);
                if (flags[i - 1]) {
                    img_review.setVisibility(View.VISIBLE);
                }
            }
        }

        // click post comment
        holder.postCommentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(fragment.getContext(), CommentsActivity.class);
            AppUtils.gPosts = post;
            fragment.startActivityForResult(intent, 2222);
        });

        // click post share
        holder.postShareImage.setOnClickListener(v -> sharePost(post));

        // post option
        holder.postOptionsButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(holder.postOptionsButton.getContext(),
                    holder.postOptionsButton);
            popupMenu.getMenuInflater().inflate(R.menu.menu_post_actions,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> handleMenuClick(item,holder.postOptionsButton.getContext(), post));

            if(FireManager.getUid().equals(post.getPostUid()))
                popupMenu.show();
        });

        holder.llt_user_avatar.setOnClickListener(v -> {
            if (post.getPostUid().equals(FireManager.getUid())) {
                AppUtils.showOtherActivity(fragment.getContext(), MyProfileActivity.class, 0);
            } else {
                AppUtils.gUid = post.getPostUid();
                AppUtils.showOtherActivity(fragment.getContext(), UserProfileActivity.class, 0);
            }
        });
    }

    private void updateReview(int index, Posts post) {
        if (post.getPostUid().equals(FireManager.getUid())) {
            Toast.makeText(context, R.string.toast_yours, Toast.LENGTH_SHORT).show();
            return;
        }
        Review postReview = new Review();
        postReview.setUserid(FireManager.getUid());
        postReview.setType(String.valueOf(index));
        postReview.setTime("");

        int setIndex = -1;
        for (int i = 0; i < post.getReviews().size(); i++) {
            Review review = post.getReviews().get(i);
            if (review.getUserid().equals(FireManager.getUid())) {
                setIndex = i;
                break;
            }
        }
        if (setIndex != -1) {
            post.updateReview(setIndex, postReview);
        } else {
            post.addReview(postReview);
        }

        notifyDataSetChanged();
        FireConstants.postsRef.child(post.getPostId()).child("reviews").setValue(post.getReviews());
    }

    private void sharePost(Posts post) {
        int type = post.getPostType();
        switch (type) {
            case TYPE_IMGVIDEO:
                shareMedia(post);
                break;
            case TYPE_TEXT:
                shareText(post.getPostText());
                break;
            case TYPE_LOCATION:
                shareLocation(post.getPostLocation());
                break;
        }
    }

    private void shareLocation(String latlong) {
        String uri = "https://www.google.com/maps/?q=" + latlong ;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,  uri);
        context.startActivity(Intent.createChooser(sharingIntent, "Share in..."));
    }

    private void shareText(String description) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, description);
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private int count = 0;
    private void shareMedia(Posts post) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "SocialBot Sharing ...");
        if (!post.getPostText().isEmpty()) {
            intent.putExtra(Intent.EXTRA_TEXT, post.getPostText());
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("*/*");

        ArrayList<Uri> files = new ArrayList<>();
        count = 0;

        ProgressDialog dialog = ProgressDialog.show(context, "", context.getString(R.string.dia_download_media));
        for (int i = 0; i < getPostMedia(post.getPostMedias()).size(); i++) {
            PostMedia postDataModel = getPostMedia(post.getPostMedias()).get(i);
            if (postDataModel.getType() == 3) {
                dialog.dismiss();
                shareText(postDataModel.getContent());
                break;
            }
            downloadPostMediaFile(postDataModel, post.getPostId(), i, new PostMediaDownloadCallback() {
                @Override
                public void onSuccess(String file_path) {
                    String[] splitUrl = file_path.split("/");
                    String fileName = splitUrl[splitUrl.length - 1];
                    String path = Environment.getExternalStorageDirectory().toString() + "/Socialbot/Post/" +fileName;
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                    count++;
                    if (count == getPostMedia(post.getPostMedias()).size()) {
                        dialog.dismiss();

                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                        context.startActivity(intent);
                    }
                }

                @Override
                public void onFailed(String error) {
                    count++;
                    if (count == getPostMedia(post.getPostMedias()).size()) {
                        dialog.dismiss();

                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }

    private void downloadPostMediaFile(PostMedia postMedia,String postid, int index, PostMediaDownloadCallback callback) {
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

        final File localFile = new File(rootPath, postid + "_" + index + type);
        if (localFile.exists()) {
            callback.onSuccess(localFile.getAbsolutePath());
            return;
        }

        storageRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> callback.onSuccess(localFile.getAbsolutePath()))
                .addOnFailureListener(e -> callback.onFailed(e.getMessage()));
    }

    public interface PostMediaDownloadCallback {
        void onSuccess(String file_path);
        void onFailed(String error);
    }

    private boolean handleMenuClick(MenuItem item, Context context, Posts post) {
        switch(item.getItemId()) {
            case R.id.delete_post :
                if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                    new MaterialAlertDialogBuilder(context, R.style.AlertDialogDark)
                            .setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this post?")
                            .setNegativeButton("Yes", (dialog, which) -> deletePost(post))
                            .setNeutralButton("No", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    new MaterialAlertDialogBuilder(context, R.style.AlertDialogDark)
                            .setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this post?")
                            .setNegativeButton("Yes", (dialog, which) -> deletePost(post))
                            .setNeutralButton("No", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                break;
            case R.id.edit_post :
                AppUtils.gPosts = post;
                AppUtils.gEditPost = true;
                Intent i = new Intent(fragment.getContext(), NewPostActivity.class);
                fragment.startActivity(i);
                break;
        }
        return false;
    }

    private void deletePost( Posts post ) {
        FireConstants.postsRef.child(post.getPostId()).removeValue().addOnCompleteListener(task -> {
            if( task.isSuccessful() )
                posts.remove(post);
        });
    }

    private String convertMilisecToDate( long milisec ) {
        Date currentDate = new Date(milisec);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        return df.format(currentDate);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        TextView postIdView;
        CircleImageView postUserImage;
        TextView postUserNameView;
        TextView postTimeView;
        ImageButton postOptionsButton;
        TextView postContentView;
        LinearLayout postLikeLayout;
        LinearLayout postCommentLayout;
        ImageView postShareImage;
        TextView postComments;

        // single media
        RelativeLayout singleRayout;
        ImageView singleThumbImage;
        ImageView videoIconImage;

        // multiple medias
        PostGridView multipleMediaView;

        // location
        RelativeLayout locationRayout;
        MapView postMapView;
        GoogleMap mGoogleMap;
        LatLng mMapLocation;
        Context mContext;

        LinearLayout llt_set, llt_user_avatar;
        TextView postReview;
        List<ImageView> lst_show_reviews = new ArrayList<>();
        List<ImageView> lst_set_reviews = new ArrayList<>();

        PostViewHolder(@NonNull View itemView, Context context) {
            super(itemView);

            postIdView = itemView.findViewById(R.id.post_id);
            postUserImage = itemView.findViewById(R.id.post_userimg);
            postUserNameView = itemView.findViewById(R.id.post_userfullname);
            postTimeView = itemView.findViewById(R.id.post_time);
            postOptionsButton = itemView.findViewById(R.id.post_options);
            postContentView = itemView.findViewById(R.id.post_text);
            postLikeLayout = itemView.findViewById(R.id.like_layout);
            postCommentLayout = itemView.findViewById(R.id.comment_layout);
            postShareImage = itemView.findViewById(R.id.post_share);
            postComments = itemView.findViewById(R.id.post_comments);

            ImageView img_show_review0 = itemView.findViewById(R.id.img_review_0);
            ImageView img_show_review1 = itemView.findViewById(R.id.img_review_1);
            ImageView img_show_review2 = itemView.findViewById(R.id.img_review_2);
            ImageView img_show_review3 = itemView.findViewById(R.id.img_review_3);
            ImageView img_show_review4 = itemView.findViewById(R.id.img_review_4);
            ImageView img_show_review5 = itemView.findViewById(R.id.img_review_5);
            ImageView img_show_review6 = itemView.findViewById(R.id.img_review_6);
            lst_show_reviews.add(img_show_review0);
            lst_show_reviews.add(img_show_review1);
            lst_show_reviews.add(img_show_review2);
            lst_show_reviews.add(img_show_review3);
            lst_show_reviews.add(img_show_review4);
            lst_show_reviews.add(img_show_review5);
            lst_show_reviews.add(img_show_review6);

            ImageView img_set_review0 = itemView.findViewById(R.id.img_set_review_0);
            ImageView img_set_review1 = itemView.findViewById(R.id.img_set_review_1);
            ImageView img_set_review2 = itemView.findViewById(R.id.img_set_review_2);
            ImageView img_set_review3 = itemView.findViewById(R.id.img_set_review_3);
            ImageView img_set_review4 = itemView.findViewById(R.id.img_set_review_4);
            ImageView img_set_review5 = itemView.findViewById(R.id.img_set_review_5);
            lst_set_reviews.add(img_set_review0);
            lst_set_reviews.add(img_set_review1);
            lst_set_reviews.add(img_set_review2);
            lst_set_reviews.add(img_set_review3);
            lst_set_reviews.add(img_set_review4);
            lst_set_reviews.add(img_set_review5);

            llt_set = itemView.findViewById(R.id.llt_review_set);
            llt_set.setVisibility(View.GONE);
            postReview = itemView.findViewById(R.id.post_review);

            llt_user_avatar = itemView.findViewById(R.id.llt_user_avatar);

            // single media
            singleRayout = itemView.findViewById(R.id.single_media_layout);
            singleThumbImage = itemView.findViewById(R.id.post_image);
            videoIconImage = itemView.findViewById(R.id.play_image);

            // multiple medias
            multipleMediaView = itemView.findViewById(R.id.postedit_gridview);
            multipleMediaView.setFocusable(true);

            // location
            locationRayout = itemView.findViewById(R.id.post_location_layout);
            postMapView = itemView.findViewById(R.id.post_map_view);
            postMapView.onCreate(null);
            postMapView.getMapAsync(this);

            mContext = context;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            MapsInitializer.initialize(mContext);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            if (mMapLocation != null) {
                updateMapContents();
            }
        }

        void setMapLocation(LatLng location) {
            mMapLocation = location;

            if (mGoogleMap != null) {
                updateMapContents();
            }
        }

        private void updateMapContents() {
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation));
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation, 17f);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

}
