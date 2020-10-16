package com.hostcart.socialbot.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.CallingActivity;
import com.hostcart.socialbot.model.constants.FireCallType;

public class PerformCall {
    Activity context;

    public PerformCall(Activity context) {
        this.context = context;
    }

    //this will check for call requirements then open the Calling Activity
    public void performCall(final boolean isVideo, final String uid) {
        if (!NetworkHelper.isConnected(context)) {
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        if (MyApp.isIsCallActive()){
            Toast.makeText(context, R.string.there_is_active_call_currently, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isVideo && !PermissionsUtil.hasVideoCallPermissions(context)) {
            Toast.makeText(context, R.string.missing_permissions, Toast.LENGTH_SHORT).show();
            return;
        } else if (!isVideo && !PermissionsUtil.hasVoiceCallPermissions(context)) {
            Toast.makeText(context, R.string.missing_permissions, Toast.LENGTH_SHORT).show();
            return;
        }

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context, R.style.AlertDialogDark);
            int message = isVideo ? R.string.video_call_confirmation : R.string.voice_call_confirmation;
            dialog.setMessage(message);
            dialog.setNeutralButton(R.string.no, null)
                    .setNegativeButton(R.string.yes, (dialogInterface, i) -> {
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage(context.getResources().getString(R.string.loading));
                        progressDialog.show();
                        FireManager.isUserBlocked(uid, isBlocked -> {
                            progressDialog.dismiss();
                            if (isBlocked) {
                                Util.showSnackbar(context, context.getResources().getString(R.string.error_calling));
                            } else {
                                Intent callScreen = new Intent(context, CallingActivity.class);
                                callScreen.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.OUTGOING);
                                callScreen.putExtra(IntentUtils.ISVIDEO, isVideo);
                                callScreen.putExtra(IntentUtils.UID, uid);
                                context.startActivity(callScreen);
                            }
                        });
                    });
            dialog.show();
        } else {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context, R.style.AlertDialogLight);
            int message = isVideo ? R.string.video_call_confirmation : R.string.voice_call_confirmation;
            dialog.setMessage(message);
            dialog.setNeutralButton(R.string.no, null)
                    .setNegativeButton(R.string.yes, (dialogInterface, i) -> {
                        final ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setMessage(context.getResources().getString(R.string.loading));
                        progressDialog.show();
                        FireManager.isUserBlocked(uid, isBlocked -> {
                            progressDialog.dismiss();
                            if (isBlocked) {
                                Util.showSnackbar(context, context.getResources().getString(R.string.error_calling));
                            } else {
                                Intent callScreen = new Intent(context, CallingActivity.class);
                                callScreen.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.OUTGOING);
                                callScreen.putExtra(IntentUtils.ISVIDEO, isVideo);
                                callScreen.putExtra(IntentUtils.UID, uid);
                                context.startActivity(callScreen);
                            }
                        });
                    });
            dialog.show();
        }
    }

}
