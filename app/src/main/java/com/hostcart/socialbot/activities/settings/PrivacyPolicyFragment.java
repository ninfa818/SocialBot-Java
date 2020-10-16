package com.hostcart.socialbot.activities.settings;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;


/**
 * Created by Devlomi on 25/03/2018.
 */

public class PrivacyPolicyFragment extends PreferenceFragment {
    private TextView tvPrivacyPolicy;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            view = inflater.inflate(R.layout.privacy_policy_fragment_dark, container, false);
        } else {
            view = inflater.inflate(R.layout.privacy_policy_fragment_light, container, false);
        }
        initViews(view);
        getHtml(tvPrivacyPolicy);
        return view;
    }

    private void getHtml( TextView textView){
        String html = getResources().getString(R.string.privacy_policy_html);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(html));
        }
    }

    private void initViews(View view) {
        tvPrivacyPolicy = view.findViewById(R.id.tv_privacy_policy);
    }


}

