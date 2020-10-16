package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.views.UserItemLayout;

import java.util.List;
import java.util.UUID;

public class AddUserAdapter extends BaseAdapter {

    private Context context;
    private List<UserInfo> userModels;


    public AddUserAdapter(Context context, List<UserInfo> userModels) {
        this.context = context;
        this.userModels = userModels;
    }

    @Override
    public int getCount() {
        return userModels.size();
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
        UserInfo userModel = userModels.get(position);

        UserItemLayout userItemLayout = new UserItemLayout(context, userModel, false, false, false, false);
        userItemLayout.setUserItemCallback(new UserItemLayout.UserItemCallback() {
            @Override
            public void onActionCallback(UserInfo userModel) {
                if(userItemLayout.getButtonStr().equals("ADD") ) {
                    FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent")
                            .child(userModel.getUid()).setValue(UUID.randomUUID().toString())
                            .addOnCompleteListener(task -> FireConstants.friendRequestRef.child(userModel.getUid()).child("received")
                            .child(FireManager.getUid()).setValue(UUID.randomUUID().toString()).addOnCompleteListener(task1 -> {
                                userItemLayout.setButtonStr("ADDED");
                            }));
                } else {
                    FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent")
                            .child(userModel.getUid()).removeValue()
                            .addOnCompleteListener(task -> FireConstants.friendRequestRef.child(userModel.getUid()).child("received")
                            .child(FireManager.getUid()).removeValue().addOnCompleteListener(task12 -> {
                                userItemLayout.setButtonStr("ADD");
                            }));
                }
            }
        });

        return userItemLayout;
    }

}
