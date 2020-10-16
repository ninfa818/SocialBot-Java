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
import com.hostcart.socialbot.activities.MoreWebActivity;
import com.hostcart.socialbot.model.MoreSectionModel;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;

public class MoreSectionAdapter extends BaseAdapter {

    private Context mContext;
    private List<MoreSectionModel> mDatas;

    public MoreSectionAdapter(Context context, List<MoreSectionModel> datas) {
        mContext = context;
        mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MoreSectionModel model = mDatas.get(position);

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_more_light, null);
        } else {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_more_dark, null);
        }

        ImageView img_icon = convertView.findViewById(R.id.img_icon);
        Glide.with(mContext)
                .asBitmap()
                .load(model.icon)
                .into(img_icon);
        TextView lbl_title = convertView.findViewById(R.id.lbl_title);
        lbl_title.setText(model.name);

        convertView.setOnClickListener(v -> {
            AppUtils.gSelMoreSection = model;
            AppUtils.showOtherActivity(mContext, MoreWebActivity.class, 0);
        });

        return convertView;
    }
}
