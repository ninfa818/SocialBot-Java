package com.hostcart.socialbot.views.dialogs;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hostcart.socialbot.R;

public class SetGroupTitleDialog extends MaterialAlertDialogBuilder {
    //onClickListener callback
    private OnFragmentInteractionListener mListener;
    private Context context;
    private EditText etGroupName;
    private TextView tvEtLength;
    private String name;
    private String hint;
    private String title;

    public SetGroupTitleDialog(Context context, String groupTitle) {
        super(context, R.style.AlertDialogDark);
        this.context = context;
        this.name = groupTitle;
    }

    @Override
    public AlertDialog show() {
        //if this is true then show the checkbox (Delete from phone)
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_group_title, null);
        etGroupName = view.findViewById(R.id.et_group_title);
        tvEtLength = view.findViewById(R.id.tv_et_length);

        etGroupName.setText(name);
        if (hint != null)
            etGroupName.setHint(hint);
        setView(view);

        etGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                tvEtLength.setText(String.valueOf(25 - s.toString().length()));

            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }
        });

        if (title == null)
            setTitle(R.string.group_title);
        else
            setTitle(title);

        setPositiveButton(R.string.ok, (dialog, which) -> {
            mListener.onPositiveClick(etGroupName.getText().toString().trim());
        });
        setNegativeButton(R.string.cancel, null);

        //show the dialog
        return super.show();
    }

    public void setmListener(OnFragmentInteractionListener mListener) {
        this.mListener = mListener;
    }

    public void setEditTextHint(String broadcast_hint) {
        hint = broadcast_hint;
    }

    public void setDialogTitle(String title) {
        this.title = title;
    }

    public interface OnFragmentInteractionListener {
        void onPositiveClick(String groupTitle);
    }

}

