package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.Post;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.views.PostGridView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostRecyclerViewAdapter extends RecyclerView.Adapter<MyPostRecyclerViewAdapter.BasicViewHolder> {

    private final List<Post> mValues;
    User user = RealmHelper.getInstance().getUser(FireManager.getUid());


    public MyPostRecyclerViewAdapter(List<Post> items) {
        mValues = items;
    }

    @Override
    public int getItemCount() {
        if (mValues == null)
            return 0;
        return mValues.size();
    }

    @NonNull
    @Override
    public BasicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wall_post_dark, parent, false);
        return new BasicViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull BasicViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        return -1;
    }

    public static class BasicViewHolder extends RecyclerView.ViewHolder {

        TextView postIdView;
        CircleImageView postUserImage;
        TextView postUserNameView;
        TextView postTimeView;
        ImageButton postOptionsButton;
        TextView postContentView;
        LinearLayout postLikeLayout;
        LinearLayout postCommentLayout;
        ImageView postShareImage;
        TextView postLikes;
        TextView postComments;

        SingleMediaHolder single;
        MultipleMediaHolder muliple;
        public GPSLocationHolder location;

        private Post post;

        BasicViewHolder(@NonNull View itemView, Context context) {
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
            postLikes = itemView.findViewById(R.id.post_likes);
            postComments = itemView.findViewById(R.id.post_comments);

            single = new SingleMediaHolder(itemView);
            muliple = new MultipleMediaHolder(itemView);
            location = new GPSLocationHolder(itemView, context);
        }

        public Post getPost() {
            return post;
        }

        public void setPost(Post post) {
            this.post = post;
        }
    }

    public static class SingleMediaHolder {
        RelativeLayout singleMediaLayout;
        ImageView postImage;
        ImageView playImage;

        SingleMediaHolder(@NonNull View itemView) {
            singleMediaLayout = itemView.findViewById(R.id.single_media_layout);
            postImage = itemView.findViewById(R.id.post_image);
            playImage = itemView.findViewById(R.id.play_image);
        }
    }

    public static class MultipleMediaHolder {
        PostGridView postGridView;

        MultipleMediaHolder(@NonNull View itemView) {
            postGridView = itemView.findViewById(R.id.postedit_gridview);
            postGridView.setFocusable(true);
        }
    }

    public static class GPSLocationHolder implements OnMapReadyCallback {
        RelativeLayout postLocationLayout;
        MapView mapView;

        GoogleMap mGoogleMap;
        LatLng mMapLocation;

        Context latContext;

        GPSLocationHolder(@NonNull View itemView, Context context) {
            latContext = context;

            postLocationLayout = itemView.findViewById(R.id.post_location_layout);
            mapView = itemView.findViewById(R.id.map_view);

            mapView.onCreate(null);
            mapView.getMapAsync(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mGoogleMap = googleMap;

            MapsInitializer.initialize(latContext);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // If we have mapView data, update the mapView content.
            if (mMapLocation != null) {
                updateMapContents();
            }
        }

        void updateMapContents() {
            mGoogleMap.clear();
            mGoogleMap.addMarker(new MarkerOptions().position(mMapLocation));

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mMapLocation, 17f);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }

}
