package com.hostcart.socialbot.views.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hostcart.socialbot.R;

public class IgnoreBatteryDialog extends MaterialAlertDialogBuilder {
    Context context;
    private OnDialogClickListener onDialogClickListener;

    public void setOnDialogClickListener(OnDialogClickListener onDialogClickListener) {
        this.onDialogClickListener = onDialogClickListener;
    }

    public IgnoreBatteryDialog(Context context) {
        super(context, R.style.AlertDialogDark);
        this.context = context;
    }

    @Override
    public AlertDialog show() {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_ignore_battery, null);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        AppCompatCheckBox checkBox = view.findViewById(R.id.chb_dont_show);
        setView(view);

        String message = context.getString(R.string.ignore_battery_dialog_message, context.getString(R.string.app_name));
        tvMessage.setText(message);
        setNegativeButton(R.string.cancel, (dialog, which) -> {
            if (onDialogClickListener != null)
                onDialogClickListener.onCancelClick(checkBox.isChecked());
        });

        setPositiveButton(R.string.ok, (dialog, which) -> {
            if (onDialogClickListener != null)
                onDialogClickListener.onOk();
        });
        return super.show();
    }

    public interface OnDialogClickListener {
        void onCancelClick(boolean checkBoxChecked);
        void onOk();
    }

}
