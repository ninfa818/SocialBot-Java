package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.PostMedia;

import java.util.List;

public class WallPostGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<PostMedia> mediaList;

    WallPostGridViewAdapter(Context context, List<PostMedia> mediaList){
        this.context = context;
        this.mediaList = mediaList;
    }

    @Override
    public int getCount() {
        if (mediaList == null) {
            return 0;
        }
        if (mediaList.size() > 4) {
            return 4;
        } else {
            return mediaList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mediaList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        WallPostGridViewAdapter.ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_grid_image, null);
            viewHolder = new WallPostGridViewAdapter.ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.imageItem);
            viewHolder.playView = convertView.findViewById(R.id.play_images);
            viewHolder.playView.setVisibility(View.GONE);
            viewHolder.lblMore = convertView.findViewById(R.id.lbl_more);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (WallPostGridViewAdapter.ViewHolder)convertView.getTag();
        }

        Glide.with(context)
            .load(BitmapUtils.encodeImageAsBytes(mediaList.get(position).getThumbImg()))
            .into(viewHolder.imageView);

        if (mediaList.get(position).getType() == 2) {
            viewHolder.playView.setVisibility(View.VISIBLE);
        }

        if (position == 3 && mediaList.size() > 4) {
            viewHolder.lblMore.setVisibility(View.VISIBLE);
            viewHolder.lblMore.setText("+ More " + (mediaList.size() - 4));
        }

        return convertView;
    }

    class ViewHolder{
        ImageView imageView;
        ImageView playView;
        TextView lblMore;
    }
}
