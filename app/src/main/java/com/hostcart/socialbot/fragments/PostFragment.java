package com.hostcart.socialbot.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.cjt2325.cameralibrary.ResultCodes;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.ViewStatusActivity;
import com.hostcart.socialbot.adapters.PostsAdapter;
import com.hostcart.socialbot.adapters.UserStatusRecyclerViewAdapter;
import com.hostcart.socialbot.interfaces.StatusFragmentCallbacks;
import com.hostcart.socialbot.model.Post;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.model.TextStatus;
import com.hostcart.socialbot.model.constants.MessageType;
import com.hostcart.socialbot.model.constants.StatusType;
import com.hostcart.socialbot.model.realms.Status;
import com.hostcart.socialbot.model.realms.UserStatuses;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.ImageEditorRequest;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.MyApp;
import com.hostcart.socialbot.utils.NetworkHelper;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.StatusManager;
import com.hostcart.socialbot.utils.Util;
import com.hostcart.socialbot.views.SharePostView;
import com.hostcart.socialbot.views.TextViewWithShapeBackground;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.zhihu.matisse.Matisse;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmResults;

import static android.app.Activity.RESULT_OK;

public class PostFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    //max duration for status video time (30sec)
    private static final int MAX_STATUS_VIDEO_TIME = 30;

    private List<Posts> postsList = new ArrayList<>();
    private PostsAdapter adapter;

    private View fragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView myBackground;
    private TextViewWithShapeBackground myTextStatusBackground;
    private CircleImageView myPhoto;
    private TextView myStoryTitle;
    private RelativeLayout myStatusRelayout;

    // Stories
    private UserStatuses myStatuses;
    private RecyclerView statusRecyclerView;
    private RealmResults<UserStatuses> statusesList;

    private StatusFragmentCallbacks callbacks;

    public PostFragment() {
    }

    @Override
    public boolean showAds() {
        return false;
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            fragment = inflater.inflate(R.layout.fragment_post_list_dark, container, false);
        } else {
            fragment = inflater.inflate(R.layout.fragment_post_list_light, container, false);
        }

        swipeRefreshLayout = fragment.findViewById(R.id.posts_swipe_container);
        ImageView backgroundImage = fragment.findViewById(R.id.status_back_image);
        CircleImageView plusCircle = fragment.findViewById(R.id.plus_circle);

        initMyStatusView();

        String img_url = SharedPreferencesManager.getMyPhoto();
        Glide.with(getContext())
                .asBitmap()
                .load(img_url)
                .into(backgroundImage);

        plusCircle.setOnClickListener(v -> showCreateStoryDialog(getActivity()));
        statusesList = RealmHelper.getInstance().getAllStatuses();

        // My Statuses
        initMyStatuses();

        // statuses
        statusRecyclerView = fragment.findViewById(R.id.story_recyclerview);
        initStatusAdapter();

        // post
        RecyclerView postsRecyclerView = fragment.findViewById(R.id.posts_recyclerview);
        postsRecyclerView.setHasFixedSize(true);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        adapter = new PostsAdapter(postsList, this);
        postsRecyclerView.setAdapter(adapter);
        fetchPost();

        SharePostView sharePostView = fragment.findViewById(R.id.sharePostView);

        swipeRefreshLayout.setOnRefreshListener(this);
        return fragment;
    }

    private void showCreateStoryDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_layout);

        ImageView textStory = dialog.findViewById(R.id.textstatus_image);
        textStory.setOnClickListener(v -> {
            if (callbacks != null)
                callbacks.openTextStatus();
            dialog.dismiss();
        });

        ImageView cameraStory = dialog.findViewById(R.id.medistatus_image);
        cameraStory.setOnClickListener(v -> {
            if (callbacks != null)
                callbacks.openCamera();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void initStatusAdapter() {
        UserStatusRecyclerViewAdapter statusAdapter = new UserStatusRecyclerViewAdapter(getContext(), statusesList);
        statusRecyclerView.setAdapter(statusAdapter);
        statusRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                RecyclerView.HORIZONTAL, false));
        statusAdapter.notifyDataSetChanged();
    }

    private void initMyStatusView() {
        myBackground = fragment.findViewById(R.id.back_image);
        myTextStatusBackground = fragment.findViewById(R.id.text_status);
        myPhoto = fragment.findViewById(R.id.user_circle);
        myStoryTitle = fragment.findViewById(R.id.name_text);
        myStatusRelayout = fragment.findViewById(R.id.status_relayout);

        myStatusRelayout.setVisibility(View.GONE);
        myStatusRelayout.setOnClickListener(v -> {
            if (myStatuses != null && !myStatuses.getFilteredStatuses().isEmpty()) {
                Intent intent = new Intent(getActivity(), ViewStatusActivity.class);
                intent.putExtra(IntentUtils.UID, myStatuses.getUserId());
                startActivity(intent);
            }
        });
    }

    private void initMyStatuses() {
        myStatuses = RealmHelper.getInstance().getUserStatuses(FireManager.getUid());
    }

    private void setMyStatus() {
        if (myStatuses == null)
            initMyStatuses();

        if( myStatuses != null
                && !myStatuses.getFilteredStatuses().isEmpty() ) {
            myStatusRelayout.setVisibility(View.VISIBLE);
            Status lastStatus = myStatuses.getStatuses().last();
            if (lastStatus.getType() == StatusType.IMAGE || lastStatus.getType() == StatusType.VIDEO) {
                myTextStatusBackground.setVisibility(View.GONE);
                myBackground.setVisibility(View.VISIBLE);
                Glide.with(getActivity()).asBitmap().load(BitmapUtils.encodeImageAsBytes(lastStatus.getThumbImg())).into(myBackground);
            } else if( lastStatus.getType() == StatusType.TEXT ) {
                myTextStatusBackground.setVisibility(View.VISIBLE);
                myBackground.setVisibility(View.GONE);
                TextStatus textStatus = lastStatus.getTextStatus();
                myTextStatusBackground.setText(textStatus.getText());
                myTextStatusBackground.setShapeColor(Color.parseColor(textStatus.getBackgroundColor()));
            }
            Glide.with(getActivity()).asBitmap().load(BitmapUtils.encodeImageAsBytes(SharedPreferencesManager.getThumbImg())).into(myPhoto);

            myStoryTitle.setText("Your story");
        } else {
            myStatusRelayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void onQueryTextChange(String newText) {
        super.onQueryTextChange(newText);
    }

    @Override
    public void onSearchClose() {
        super.onSearchClose();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        isSelf = true;
        callbacks = (StatusFragmentCallbacks) context;

        if (!(context instanceof OnListFragmentInteractionListener)) {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private boolean isSelf = false;
    @Override
    public void onResume() {
        super.onResume();
        setMyStatus();
        if( isSelf ) {
            isSelf = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Post item);
    }

    /* Manage Stories */
    public void onCameraActivityResult(int resultCode, Intent data) {
        if (resultCode != ResultCodes.CAMERA_ERROR_STATE) {
            if (resultCode == ResultCodes.IMAGE_CAPTURE_SUCCESS) {
                String path = data.getStringExtra(IntentUtils.EXTRA_PATH_RESULT);
                ImageEditorRequest.open(getActivity(), path);

            } else if (resultCode == ResultCodes.VIDEO_RECORD_SUCCESS) {
                String path = data.getStringExtra(IntentUtils.EXTRA_PATH_RESULT);
                uploadVideoStatus(path);
            } else if (resultCode == ResultCodes.PICK_IMAGE_FROM_CAMERA) {
                List<String> mPaths = Matisse.obtainPathResult(data);
                for (String mPath : mPaths) {
                    if (!FileUtils.isFileExists(mPath)) {
                        Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.image_video_not_found), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (FileUtils.isPickedVideo(mPaths.get(0))) {
                    long mediaLengthInMillis = Util.getMediaLengthInMillis(getContext(), mPaths.get(0));
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaLengthInMillis);
                    if (seconds <= MAX_STATUS_VIDEO_TIME) {
                        for (String mPath : mPaths) {
                            uploadVideoStatus(mPath);
                        }
                    } else {
                        Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.video_length_is_too_long), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mPaths.size() == 1)
                        ImageEditorRequest.open(getActivity(), mPaths.get(0));
                    else
                        for (String path : mPaths) {
                            uploadImageStatus(path);
                        }
                }
            }
        }
    }

    private void uploadVideoStatus(String path) {
        if (!NetworkHelper.isConnected(MyApp.context())) {
            Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(getActivity(), R.string.uploading_status, Toast.LENGTH_SHORT).show();
        StatusManager.uploadStatus(path, StatusType.VIDEO, true, isSuccessful -> {
            if (isSuccessful) {
                setMyStatus();
                Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.status_uploaded), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.error_uploading_status), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadImageStatus(String path) {
        if (!NetworkHelper.isConnected(MyApp.context())) {
            Toast.makeText(MyApp.context(), MyApp.context().getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(MyApp.context(), MyApp.context().getResources().getString(R.string.uploading_status), Toast.LENGTH_SHORT).show();
        String mPath = compressImage(path);
        StatusManager.uploadStatus(mPath, StatusType.IMAGE, false, isSuccessful -> {
            if (isSuccessful) {
                setMyStatus();
                Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.status_uploaded), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), MyApp.context().getResources().getString(R.string.error_uploading_status), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //compress image when user chooses an image from gallery
    private String compressImage(String imagePath) {
        File file = DirManager.generateFile(MessageType.SENT_IMAGE);
        BitmapUtils.compressImage(imagePath, file);

        return file.getPath();
    }

    public void onImageEditSuccess(@NotNull String imagePath) {
        uploadImageStatus(imagePath);
    }

    public void onTextStatusResult(TextStatus textStatus) {
        if (!NetworkHelper.isConnected(MyApp.context())) {
            Toast.makeText(MyApp.context(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MyApp.context(), R.string.uploading_status, Toast.LENGTH_SHORT).show();
            StatusManager.uploadTextStatus(textStatus, isSuccessful -> {
                if (isSuccessful)
                    setMyStatus();
            });
        }
    }

    //fetch status when user swipes to this page
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && callbacks != null)
            callbacks.fetchStatuses();
    }

    private void fetchPost() {
        FireConstants.postsRef.orderByKey().limitToLast(50).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if( dataSnapshot.getValue() != null ) {
                    Posts posts = dataSnapshot.getValue(Posts.class);
                    if (posts.getPostId() == null) {
                        return;
                    }
                    if( postsList != null && postsList.size() > 0) {
                        boolean isContain = false;
                        for( int i=0; i< postsList.size(); i++ ) {
                            if(postsList.get(i).getPostId().equals(posts.getPostId())) {
                                isContain = true;
                                break;
                            }
                        }
                        if( !isContain ) {
                            postsList.add(0, posts);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        postsList.add(0, posts);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Posts posts = dataSnapshot.getValue(Posts.class);
                if( postsList != null && postsList.size() > 0 ) {
                    for( int i=0; i< postsList.size(); i++ ) {
                        if (postsList.get(i).getPostId().equals(posts.getPostId())) {
                            postsList.set(i, posts);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Posts posts = dataSnapshot.getValue(Posts.class);
                if( postsList != null && postsList.size() > 0 ) {
                    int selIndex = 0;
                    for( int i=0; i< postsList.size(); i++ ) {
                        if (postsList.get(i).getPostId().equals(posts.getPostId())) {
                            selIndex = i;
                            break;
                        }
                    }
                    postsList.remove(selIndex);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if( requestCode == 2222 ) { // comment activity
            if( resultCode == RESULT_OK ) {
                Posts post = (Posts)data.getSerializableExtra("postt");
                for( int i=0; i<postsList.size(); i++ ) {
                    if( postsList.get(i).getPostId().equals(post.getPostId()) ) {
                        postsList.get(i).setPostComments(post.getPostComments());
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        }
    }

    public void setNewPost( @Nullable Intent data ) { // new post
        if( data == null ) return;
        Posts post = (Posts)data.getSerializableExtra("newpost");
        postsList.remove(0);
        postsList.add(0, post);
    }

}
