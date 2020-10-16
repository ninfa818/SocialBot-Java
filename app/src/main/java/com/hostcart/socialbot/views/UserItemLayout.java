package com.hostcart.socialbot.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;


public class UserItemLayout extends LinearLayout {

    private ImageView img_avatar;
    private TextView lbl_name, lbl_info;
    private Button btn_action;
    private ImageView img_message, img_call, img_vcall, img_delete;

    private UserInfo userModel;
    private boolean isMessage, isCall, isVCall, isDelete;
    private String str_button;

    private UserItemCallback userItemCallback;

    private void initWithEvent() {
        btn_action.setOnClickListener(v -> {
            if (!str_button.equals("ADD")) return;
            userItemCallback.onActionCallback(userModel);
        });

        img_message.setOnClickListener(v -> userItemCallback.onMessageCallback(userModel));

        img_call.setOnClickListener(v -> userItemCallback.onVoiceCallback(userModel));

        img_vcall.setOnClickListener(v -> userItemCallback.onVideoCallback(userModel));

        img_delete.setOnClickListener(v -> userItemCallback.onDeleteCallback(userModel));
    }

    public UserItemLayout(Context context, UserInfo userModel, boolean isMessage, boolean isCall, boolean isVCall, boolean isDelete) {
        super(context);

        this.userModel = userModel;
        this.isMessage = isMessage;
        this.isCall = isCall;
        this.isVCall = isVCall;
        this.isDelete = isDelete;

        setOrientation(LinearLayout.HORIZONTAL);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            LayoutInflater.from(context).inflate(R.layout.ui_user_item_dark, this, true);
        } else {
            LayoutInflater.from(context).inflate(R.layout.ui_user_item_light, this, true);
        }

        initUIView();
        initWithEvent();
    }

    private void initUIView() {
        img_avatar = findViewById(R.id.img_user_avatar);
        lbl_name = findViewById(R.id.lbl_user_name);
        lbl_info = findViewById(R.id.lbl_user_info);

        btn_action = findViewById(R.id.btn_user_action);

        img_message = findViewById(R.id.img_user_message);
        img_call = findViewById(R.id.img_user_call);
        img_vcall = findViewById(R.id.img_user_vcall);
        img_delete = findViewById(R.id.img_user_delete);

        initWithData();
    }

    private void initWithData() {
        if (!isMessage) {
            img_message.setVisibility(GONE);
        }

        if (!isCall) {
            img_call.setVisibility(GONE);
        }

        if (!isVCall) {
            img_vcall.setVisibility(GONE);
        }

        if (!isDelete) {
            img_delete.setVisibility(GONE);
        }

        Glide.with(getContext())
                .asBitmap()
                .load(BitmapUtils.encodeImageAsBytes(userModel.getThumbImg()))
                .into(img_avatar);
        lbl_name.setText(userModel.getName());
        lbl_info.setText(userModel.getStatus());
        str_button = "ADD";

        String jsonFriend = SharedPreferencesManager.getFriendsListJsonString();
        if(!jsonFriend.equals("")) {
            Gson gson = new Gson();
            TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
            List<UserInfo> friendList = gson.fromJson(jsonFriend, token.getType());
            for (UserInfo user: friendList) {
                if (user.getUid().equals(userModel.getUid())) {
                    str_button = "FRIEND";
                    break;
                }
            }
        }

        String jsonAdded = SharedPreferencesManager.getAddedMeListJsonString();
        if (!jsonAdded.equals("")) {
            Gson gson = new Gson();
            TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
            List<UserInfo> invitedUsers = gson.fromJson(jsonAdded, token.getType());
            for (UserInfo user: invitedUsers) {
                if (user.getUid().equals(userModel.getUid())) {
                    str_button = "ADDED";
                    break;
                }
            }
        }

        if (userModel.getUid().equals(FireManager.getUid())) {
            str_button = "ME";
        }

        btn_action.setText(str_button);
    }

    public String getButtonStr() {
        return str_button;
    }

    public void setButtonStr(String value) {
        str_button = value;
        btn_action.setText(str_button);
    }

    public void setUserItemCallback(UserItemCallback userItemCallback) {
        this.userItemCallback = userItemCallback;
    }

    public interface UserItemCallback {
        default void onMessageCallback(UserInfo userModel) {}
        default void onVoiceCallback(UserInfo userModel) {}
        default void onVideoCallback(UserInfo userModel) {}
        default void onDeleteCallback(UserInfo userModel) {}
        default void onActionCallback(UserInfo userModel) {}
    }

}
