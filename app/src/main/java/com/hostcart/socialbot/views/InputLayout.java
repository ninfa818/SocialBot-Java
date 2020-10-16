package com.hostcart.socialbot.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;


public class InputLayout extends LinearLayout {

    private int resourceID;
    private String hintStr;
    private String inputStr;
    private boolean isEditable;
    private boolean isCcp;

    private InputLayoutCallback layoutCallback;

    private EditText txt_input;
    private ImageView img_edit, img_header;
    private CountryCodePicker ccp_country;

    private void initWithEvent() {
        img_edit.setOnClickListener(v -> {
            boolean isEdit = txt_input.isEnabled();
            txt_input.setEnabled(!isEdit);
            if (isEdit) {
                String result = txt_input.getText().toString();
                if (result.length() == 0) {
                    Toast.makeText(getContext(), getResources().getString(R.string.toast_input_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (layoutCallback != null) {
                    layoutCallback.onFinishedEditable(inputStr, ccp_country.getFullNumber());
                }
            } else {
                if (layoutCallback != null) {
                    layoutCallback.onStartedEditable();
                }
            }
        });
    }

    public InputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.InputLayout);
        this.resourceID = arr.getResourceId(R.styleable.InputLayout_src, 0);
        this.hintStr = arr.getString(R.styleable.InputLayout_hint);
        if (hintStr == null) {
            hintStr = "";
        }
        this.inputStr = arr.getString(R.styleable.InputLayout_inputType);
        if (inputStr == null) {
            inputStr = "";
        }
        this.isEditable = arr.getBoolean(R.styleable.InputLayout_editable, false);
        this.isCcp = arr.getBoolean(R.styleable.InputLayout_isccp, false);
        arr.recycle();

        setOrientation(LinearLayout.HORIZONTAL);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            LayoutInflater.from(context).inflate(R.layout.ui_input_layout_dark, this, true);
        } else {
            LayoutInflater.from(context).inflate(R.layout.ui_input_layout_light, this, true);
        }

        initUIView();
        initWithEvent();
    }

    private void initUIView() {
        img_header = findViewById(R.id.img_input_header);
        img_edit = findViewById(R.id.img_input_edit);
        txt_input = findViewById(R.id.txt_input);
        ccp_country = findViewById(R.id.ccp_input);

        initWithData();
    }

    private void initWithData() {
        img_header.setImageResource(resourceID);

        txt_input.setHint(hintStr);
        switch (inputStr) {
            case "Number" :
                txt_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case "Email" :
                txt_input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "Phone" :
                txt_input.setInputType(InputType.TYPE_CLASS_PHONE);
                break;
            case "Pass" :
                txt_input.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
        }

        if (isEditable) {
            txt_input.setEnabled(false);
            img_edit.setVisibility(VISIBLE);
        } else {
            txt_input.setEnabled(true);
            img_edit.setVisibility(GONE);
        }

        if (isCcp) {
            ccp_country.setVisibility(VISIBLE);
        } else {
            ccp_country.setVisibility(GONE);
        }
    }

    public String getInputText() {
        return txt_input.getText().toString();
    }

    public void setInputText(String str) {
        txt_input.setText(str);
    }

    public String getResultText() {
        return ccp_country.getSelectedCountryCodeWithPlus() + txt_input.getText().toString();
    }

    public void setInputTextEnable(boolean enable) {
        txt_input.setEnabled(enable);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            img_edit.setVisibility(VISIBLE);
        } else {
            img_edit.setVisibility(GONE);
        }
    }

    public void setHintStr(String hint) {
        hintStr = hint;
        txt_input.setHint(hintStr);
    }

    public void setInputLayoutCallback(InputLayoutCallback layoutCallback) {
        this.layoutCallback = layoutCallback;
    }

    public interface InputLayoutCallback {
        void onFinishedEditable(String text, String country);
        void onStartedEditable();
    }

}
