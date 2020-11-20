package com.hostcart.socialbot.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.hostcart.socialbot.R;

public class PrivacyDialog extends Dialog {

    private PrivacyDialogListener privacyDialogListener;

    public PrivacyDialog(@NonNull Context context) {
        super(context);

        setContentView(R.layout.dialog_privacy);

        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        setTitle(null);
        setCanceledOnTouchOutside(true);

        initUIView();
    }

    private void initUIView() {
        WebView webView = findViewById(R.id.wbv_content);
        webView.loadData(getContext().getString(R.string.privacy_policy_html), "text/html", "UTF-8");
        Button btn_accept = findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(v -> {
            dismiss();
            privacyDialogListener.onClick();
        });
    }

    public void setPrivacyDialogListener(PrivacyDialogListener privacyDialogListener) {
        this.privacyDialogListener = privacyDialogListener;
    }

    public interface PrivacyDialogListener {
        void onClick();
    }

}
