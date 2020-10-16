package com.hostcart.socialbot.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.jetbrains.annotations.NotNull;


public class ForwardPostFragment extends BaseFragment {

    public EditText post_text;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            view = inflater.inflate(R.layout.fragment_forward_post_dark, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_forward_post_light, container, false);
        }
        initView(view);
        return view;
    }

    private void initView(View view) {
        post_text = view.findViewById(R.id.post_text);
    }

    @Override
    public boolean showAds() {
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}
