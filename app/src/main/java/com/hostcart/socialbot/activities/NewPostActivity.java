package com.hostcart.socialbot.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.cjt2325.cameralibrary.ResultCodes;
import com.codekidlabs.storagechooser.StorageChooser;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.ExpandableContact;
import com.hostcart.socialbot.placespicker.Place;
import com.hostcart.socialbot.placespicker.PlacesPickerActivity;
import com.hostcart.socialbot.adapters.NewPostGridViewAdapter;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.model.constants.MessageType;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.ContactUtils;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FileFilter;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.Glide4Engine;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.KeyboardHelper;
import com.hostcart.socialbot.utils.PostMedia;
import com.hostcart.socialbot.utils.PostMediaCreator;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.Util;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.hostcart.socialbot.activities.ChatActivity.MAX_SELECTABLE;

public class NewPostActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int LOCATION_TYPE = 2;
    public static final int IMGANDVID_TYPE = 0;
    public static final int ONLYTEXT_TYPE = 1;

    private static final int POSTTYPE_IMAGE = 1;
    private static final int POSTTYPE_VIDEO = 2;

    private static final int CAMERA_REQUEST = 4659;
    private static final int PICK_GALLERY_REQUEST = 4815;
    private static final int PICK_LOCATION_REQUEST = 7125;
    private static final int PICK_CONTACT_REQUEST = 5491;
    private static final int PICK_NUMBERS_FOR_CONTACT_REQUEST = 5517;

    private static int MAX_FILE_SIZE = 40000;

    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog progressDialog;
    private EditText postText;
    private MapView mapView;
    private GoogleMap mGoogleMap;
    private LatLng mMapLocation;
    private GridView imagesPreview;

    private String latlng = "";
    private ArrayList<String> imagesPaths;
    private Posts post;
    private NewPostGridViewAdapter adapter;

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_new_post_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_new_post_light);
        }

        coordinatorLayout = findViewById(R.id.newPostCoordinator);
        progressDialog = new ProgressDialog(NewPostActivity.this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        postText = findViewById(R.id.post_text);
        postText.requestFocus();
        KeyboardHelper.openSoftKeyboard(this, postText);

        mapView = findViewById(R.id.mapView);

        mapView.onCreate(null);
        mapView.getMapAsync(this);
        // Mou
        // Mou
        ImageButton galleryImages = findViewById(R.id.gallery_images);
        ImageButton cameraImage = findViewById(R.id.camera_image);
        ImageButton locationImage = findViewById(R.id.location_image);
        ImageButton docImage = findViewById(R.id.document_image);
        ImageButton attachImage = findViewById(R.id.attachment_image);
        //

        imagesPreview = findViewById(R.id.gridview_images_preview);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float width = displayMetrics.widthPixels / displayMetrics.density;
        int IMAGE_DIMENSION = 100;
        int columns = (int) width/ IMAGE_DIMENSION;
        imagesPreview.setNumColumns(columns);
        imagesPreview.setOnItemLongClickListener((parent, view, position, id) -> {
            imagesPaths.remove(position);
            adapter.notifyDataSetChanged();
            return false;
        });

        imagesPaths = new ArrayList<>();

        if (AppUtils.gEditPost) {
            post = AppUtils.gPosts;
            importDataFromPost(post);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Post");
        } else {
            post = new Posts();
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.new_post);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        galleryImages.setOnClickListener(v -> pickImages());
        cameraImage.setOnClickListener(v -> startCamera());
        locationImage.setOnClickListener(v -> pickLocation());
        docImage.setOnClickListener(v -> pickFile());
        attachImage.setOnClickListener(v -> { });
    }

    private void startCamera() {
        startActivityForResult(new Intent(NewPostActivity.this, CameraActivity.class), CAMERA_REQUEST);
    }

    private void pickLocation() {
        startActivityForResult(new Intent(this, PlacesPickerActivity.class), PICK_LOCATION_REQUEST);
    }

    private void pickFile() {
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(NewPostActivity.this)
                .withFragmentManager(getFragmentManager())
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .disableMultiSelect()
                .build();
        chooser.show();
        chooser.setOnSelectListener(path -> {
            File file = new File(path);
            int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
            String fileExtension = Util.getFileExtensionFromPath(path);

            if (file_size > MAX_FILE_SIZE) {
                Toast.makeText(NewPostActivity.this, R.string.file_is_too_big, Toast.LENGTH_SHORT).show();

            } else if (!FileFilter.isOkExtension(fileExtension)) {
                Toast.makeText(NewPostActivity.this, R.string.type_not_supported, Toast.LENGTH_SHORT).show();
            } else {
//                sendFile(path);
            }
        });
    }

    private void pickContact() {
        new MultiContactPicker.Builder(NewPostActivity.this)
                .handleColor(ContextCompat.getColor(NewPostActivity.this, R.color.colorPrimary))
                .bubbleColor(ContextCompat.getColor(NewPostActivity.this, R.color.colorPrimary))
                .showPickerForResult(PICK_CONTACT_REQUEST);
    }

    private void pickImages() {
        Matisse.from(NewPostActivity.this)
                .choose(MimeType.of(MimeType.MP4, MimeType.THREEGPP, MimeType.THREEGPP2
                        , MimeType.JPEG, MimeType.BMP, MimeType.PNG, MimeType.GIF))
                .countable(true)
                .maxSelectable(MAX_SELECTABLE)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new Glide4Engine())
                .forResult(PICK_GALLERY_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getMenuInflater().inflate(R.menu.menu_new_post_dark, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_new_post_light, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.post_items) {
            postBlog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void postBlog() {
        int post_type;
        if (AppUtils.gEditPost) {
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Editing post!");
            progressDialog.show();
            post_type = post.getPostType();
        } else {
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Creating post!");
            progressDialog.show();

            if( latlng.equals("") ) {
                if(imagesPaths != null && imagesPaths.size() > 0)
                    post_type = IMGANDVID_TYPE;
                else
                    post_type = ONLYTEXT_TYPE;
            } else {
                post_type = LOCATION_TYPE;
            }
        }

        createPost(
                postText.getText().toString(),
                post_type,
                imagesPaths,
                latlng,
                this
        );
    }

    private String createImagePath(String imagePath, boolean fromCamera) {
        int type = MessageType.SENT_IMAGE;

        File file = DirManager.generateFile(type);
        String fileExtension = Util.getFileExtensionFromPath(imagePath);

        if (fileExtension.equals("gif")) {
            try {
                FileUtils.copyFile(imagePath, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            BitmapUtils.compressImage(imagePath, file);
        }
        if (fromCamera) {
            FileUtils.deleteFile(imagePath);
        }

        return file.getPath();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( requestCode == CAMERA_REQUEST && resultCode != ResultCodes.CAMERA_ERROR_STATE ) {
            if (resultCode == ResultCodes.IMAGE_CAPTURE_SUCCESS) {
                String path = data.getStringExtra(IntentUtils.EXTRA_PATH_RESULT);

                path = createImagePath(path, true);
                setMultimedia(path);
            } else if (resultCode == ResultCodes.VIDEO_RECORD_SUCCESS) {
                String path = data.getStringExtra(IntentUtils.EXTRA_PATH_RESULT);
                setMultimedia(path);
            }
        } else if( requestCode == PICK_GALLERY_REQUEST && resultCode == RESULT_OK ) {
            List<String> mPaths = Matisse.obtainPathResult(data);
            for (String mPath : mPaths) {
                if (!FileUtils.isFileExists(mPath)) {
                    Toast.makeText(NewPostActivity.this, R.string.image_video_not_found, Toast.LENGTH_SHORT).show();
                    continue;
                }

                if (FileUtils.isPickedVideo(mPath)) {
                    setMultimedia(mPath);
                } else {
                    String path = createImagePath(mPath, false);
                    setMultimedia(path);
                }
            }
        } else if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            List<ContactResult> results = MultiContactPicker.obtainResult(data);
            List<ExpandableContact> contactNameList = ContactUtils.getContactsFromContactResult(results);
            Intent intent = new Intent(this, SelectContactNumbersActivity.class);
            intent.putParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST, (ArrayList<? extends Parcelable>) contactNameList);
            startActivityForResult(intent, PICK_NUMBERS_FOR_CONTACT_REQUEST);
        } else if (requestCode == PICK_NUMBERS_FOR_CONTACT_REQUEST && resultCode == RESULT_OK) {
            List<ExpandableContact> selectedContacts = data.getParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST);
//            sendContacts(selectedContacts);
        } else if (requestCode == PICK_LOCATION_REQUEST && resultCode == RESULT_OK) {
            Place place = data.getParcelableExtra(Place.EXTRA_PLACE);
            LatLng ll = place.getLatLng();
            latlng = String.format("%s,%s", String.valueOf(ll.latitude),
                    String.valueOf(ll.longitude));

            mapView.setVisibility(View.VISIBLE);
            setMapLocation(ll);
        }
    }

    private void setMultimedia( String path ) {
        imagesPaths.add(path);

        if( adapter == null ) {
            adapter = new NewPostGridViewAdapter(NewPostActivity.this,imagesPaths);
            imagesPreview.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }

    private void importDataFromPost( Posts post ) {
        postText.setText(post.getPostText());
        switch (post.getPostType()) {
            case IMGANDVID_TYPE:
                imagesPreview.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.GONE);
                previewPost(post, IMGANDVID_TYPE);
                break;
            case ONLYTEXT_TYPE:
                break;
            case LOCATION_TYPE:
                imagesPreview.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);
                previewPost(post, LOCATION_TYPE);
                break;
        }
    }

    private void previewPost(Posts post, int type) {
        if (type == LOCATION_TYPE) {
            String[] latlong =  post.getPostLocation().split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            setMapLocation(new LatLng(latitude, longitude));
        } else {
            HashMap<String,Object> hashMap = post.getPostMedias();
            if (hashMap != null) {
                for( Map.Entry<String,Object> entry: hashMap.entrySet() ) {
                    HashMap<String,Object> value = (HashMap<String,Object>)entry.getValue();
                    for( Map.Entry<String,Object> ventry : value.entrySet() ) {
                        String subKey = ventry.getKey();
                        Object subValue = ventry.getValue();
                        if( subKey.equals("localPath") ) {
                            setMultimedia(subValue.toString());
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        MapsInitializer.initialize(this);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        if (mMapLocation != null) {
            updateMapContents();
        }
    }

    private void setMapLocation(LatLng location) {
        mMapLocation = location;
        if (mGoogleMap != null) {
            updateMapContents();
        }
    }

    protected void updateMapContents() {
        mGoogleMap.clear();
        mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation, 17f);
        mGoogleMap.moveCamera(cameraUpdate);
    }

    // Create Post
    public void createPost(String postTxt, int postType, List<String> imagesPaths, String latlng, Object caller) {
        switch (postType) {
            case IMGANDVID_TYPE:
                createMediaPost(postTxt, postType, imagesPaths, caller);
                break;
            case ONLYTEXT_TYPE:
                createTextPost(postTxt, postType, caller);
                break;
            case LOCATION_TYPE:
                createLocationPosts(postTxt, postType, latlng, caller);
                break;
        }
    }

    private void saveMedia( String key, List<String> mediapaths, Object caller ) {
        List<Integer> types = new ArrayList<>();
        List<PostMedia> postMedias = new ArrayList<>();

        for( String mediapath: mediapaths ) {
            PostMedia postMedia;
            String extension = Util.getFileExtensionFromPath(mediapath);
            if( extension.equals("jpg") ) {
                types.add(POSTTYPE_IMAGE);
                postMedia = PostMediaCreator.createImagePost(mediapath);
            }
            else {
                types.add(POSTTYPE_VIDEO);
                postMedia = PostMediaCreator.createVideoPost(mediapath);
            }
            postMedias.add(postMedia);

            final String fileName = Util.getFileNameFromPath(mediapath);
            FireManager.getRef(FireManager.POST_TYPE, fileName).putFile(Uri.fromFile(new File(mediapath))).addOnCompleteListener(uploadTask -> {
                if( uploadTask.isSuccessful() ) {
                    uploadTask.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                        if( task.isSuccessful() ) {
                            final Uri uri = task.getResult().normalizeScheme();
                            PostMedia temp = postMedias.get(mediapaths.indexOf(mediapath));
                            temp.setContent(String.valueOf(uri));

                            String sub_key = FireConstants.postsRef.child(key).child("posMedias").push().getKey();
                            setMedias(temp, sub_key);

                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("content", temp.getContent());
                            hashMap.put("duration", temp.getDuration());
                            hashMap.put("localPath", temp.getLocalPath());
                            hashMap.put("thumbImg", temp.getThumbImg());
                            hashMap.put("timestamp", temp.getTimestamp());
                            hashMap.put("type", temp.getType());
                            hashMap.put("userId", temp.getUserId());

                            FireConstants.postsRef
                                    .child(key).child("postMedias").child(sub_key).updateChildren(hashMap)
                                    .addOnCompleteListener(task1 -> {
                                        PostMedia last = postMedias.get(mediapaths.size()-1);
                                        if( temp.equals(last) ) {
                                            setNewPostMedias();
                                            gotoNewPostActivity(caller);
                                        }
                                    });
                        }
                    });
                }
            }).addOnFailureListener(e -> Snackbar.make(((NewPostActivity) caller)
                    .getCoordinatorLayout(),e.getMessage(),2500)
                    .setActionTextColor(Color.RED).show());
        }
    }

    private static void gotoNewPostActivity( Object caller ) {
        Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Succesfully added post",2500).setActionTextColor(Color.GREEN).show();
        Handler handler = new Handler();
        handler.postDelayed(((NewPostActivity) caller)::onBackPressed, 3500);
        ((NewPostActivity) caller).getProgressDialog().dismiss();
    }

    public HashMap<String,Object> createPostHashMap( String key, String postText, int type, String latlong) {
        setNewPost(key, postText, type, latlong);
        HashMap<String,Object> data = new HashMap<>();
        if(!postText.equals("") && !postText.equals(null))
            data.put("postText",post.getPostText());
        data.put("postId", post.getPostId());
        data.put("postUid", post.getPostUid());
        data.put("postName", post.getPostName());
        data.put("postPhotoUrl", post.getPostPhotoUrl());
        data.put("postShares",post.getPostShares());
        data.put("postIsShared",post.getPostIsShared());
        data.put("postTime", post.getPostTime());
        data.put("postType", post.getPostType());
        if( latlong != null && !latlong.equals("") )
            data.put("postLocation", post.getPostLocation());

        return data;
    }

    private void createMediaPost(String text, int postType, List<String> mediapaths, Object caller) {
        String key = FireConstants.postsRef.push().getKey();
        if (AppUtils.gEditPost) {
            key = post.getPostId();
        }
        HashMap<String,Object> data = createPostHashMap(key, text, postType, null);
        String finalKey = key;
        FireConstants.postsRef
                .child(key)
                .setValue(data).addOnCompleteListener(task -> {
            if( task.isSuccessful() ) {
                saveMedia(finalKey, mediapaths, caller);
            }
        });
    }

    private void createTextPost( String text, int postType, Object caller ) {
        String key = FireConstants.postsRef.push().getKey();
        if (AppUtils.gEditPost) {
            key = post.getPostId();
        }
        HashMap<String,Object> data = createPostHashMap(key, text, postType, null);
        FireConstants.postsRef
                .child(key)
                .updateChildren(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Handler handler = new Handler();
                if (AppUtils.gEditPost) {
                    Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Successful updated post",2500).setActionTextColor(Color.GREEN).show();
                } else {
                    Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Successful added post",2500).setActionTextColor(Color.GREEN).show();
                }
                handler.postDelayed(((NewPostActivity) caller)::onBackPressed, 3500);
                ((NewPostActivity) caller).getProgressDialog().dismiss();
            } else {
                ((NewPostActivity) caller).getProgressDialog().dismiss();
                Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Text must be provided!",2500).setActionTextColor(Color.RED).show();
            }
        });
    }

    private void createLocationPosts( String text, int postType, String latlng, Object caller ) {
        String key = FireConstants.postsRef.push().getKey();
        if (AppUtils.gEditPost) {
            key = post.getPostId();
        }
        HashMap<String,Object> data = createPostHashMap(key, text, postType, latlng);
        FireConstants.postsRef
                .child(key)
                .updateChildren(data).addOnCompleteListener(task -> {
            if( task.isSuccessful() ) {
                Handler handler = new Handler();
                if (AppUtils.gEditPost) {
                    Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Successful updated post",2500).setActionTextColor(Color.GREEN).show();
                } else {
                    Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Successful added post",2500).setActionTextColor(Color.GREEN).show();
                }

                handler.postDelayed(this::onBackPressed, 3500);
                ((NewPostActivity) caller).getProgressDialog().dismiss();
            } else {
                ((NewPostActivity) caller).getProgressDialog().dismiss();
                Snackbar.make(((NewPostActivity) caller).getCoordinatorLayout(),"Location must be provided!",2500).setActionTextColor(Color.RED).show();
            }
        });
    }

    private void setNewPost( String key, String postText, int type, String latlong) {
        post.setPostId(key);
        post.setPostUid(FireManager.getUid());
        post.setPostIsShared(false);
        post.setPostShares(0);
        post.setPostText(postText);
        post.setPostName(SharedPreferencesManager.getUserName());
        post.setPostPhotoUrl(SharedPreferencesManager.getThumbImg());
        post.setPostType(type);
        post.setPostTime(new Date().getTime());
        post.setPostIsLiked(false);
        post.setPostLikes(0);
        if( latlong != null && !latlong.equals("") )
            post.setPostLocation(latlong);
    }

    Set<Map<String,Object>> mapSet = new HashSet<>();
    private void setMedias( PostMedia postMedia, String sKey ) {
        Map<String,Object> ssHashMap = new HashMap<>();
        ssHashMap.put("content", postMedia.getContent());
        ssHashMap.put("duration", postMedia.getDuration());
        ssHashMap.put("localPath", postMedia.getLocalPath());
        ssHashMap.put("thumbImg", postMedia.getThumbImg());
        ssHashMap.put("timestamp", postMedia.getTimestamp());
        ssHashMap.put("type", postMedia.getType());
        ssHashMap.put("userId", postMedia.getUserId());

        Map<String,Object> sHashMap = new HashMap<>();
        sHashMap.put(sKey, ssHashMap);

        mapSet.add(sHashMap);
    }

    private void setNewPostMedias() {
        HashMap<String,Object> hashMap = new HashMap<>();
        for( Map<String,Object> map : mapSet ) {
            String[] keyArray = new String[map.keySet().size()];
            map.keySet().toArray(keyArray);
            String key = keyArray[0];
            hashMap.put(key, map.get(key));
        }
        post.setPostMedias(hashMap);
    }

}
