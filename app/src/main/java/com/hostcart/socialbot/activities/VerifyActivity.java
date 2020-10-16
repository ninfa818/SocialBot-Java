package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.main.MainActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.PermissionsUtil;
import com.hostcart.socialbot.utils.ServiceHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.Util;
import com.hostcart.socialbot.views.InputLayout;

import java.util.concurrent.TimeUnit;

import in.aabhasjindal.otptextview.OTPListener;
import in.aabhasjindal.otptextview.OtpTextView;
import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class VerifyActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 159;

    private LinearLayout llt_send, llt_result;
    private InputLayout ilt_phone;
    private Button btn_send;
    private TextView lbl_detail, lbl_resend;
    private ProgressDialog dialog;

    private OTPListener otpListener = new OTPListener() {
        @Override
        public void onInteractionListener() { }

        @Override
        public void onOTPComplete(String otp) {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            onCompleteVerify(credential);
        }
    };

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verifyPhoneNumberCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            onCompleteVerify(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            dialog.dismiss();
            Util.showSnackbar(VerifyActivity.this, e.getMessage());
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            dialog.dismiss();
            Util.showSnackbar(VerifyActivity.this, getString(R.string.toast_sent_code));

            verificationId = s;
            isResult = true;
            setResultView();
        }
    };

    private boolean isResult = false;
    private String verificationId;

    private FirebaseAuth mAuth;


    @SuppressLint("SetTextI18n")
    private void initWithEvent() {
        btn_send.setOnClickListener(v -> {
            if (ilt_phone.getInputText().length() == 0) {
                Toast.makeText(VerifyActivity.this, R.string.toast_phone_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            lbl_detail.setText(getString(R.string.verify_code_detail) + "\n" + ilt_phone.getResultText());
            String str_phone = ilt_phone.getResultText();

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    str_phone,        // Phone number to verify
                    60,                 // Timeout duration
                    TimeUnit.SECONDS,   // Unit of timeout
                    this,               // Activity (for callback binding)
                    verifyPhoneNumberCallback);
        });
        lbl_resend.setOnClickListener(v -> {
            isResult = false;
            setResultView();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_verify_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_verify_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        mAuth = FirebaseAuth.getInstance();
        requestPermissions();
    }

    private void initWithView() {
        dialog = ProgressDialog.show(this, "", getString(R.string.alt_connect_server));
        dialog.dismiss();

        llt_send = findViewById(R.id.llt_verify_send);
        llt_result = findViewById(R.id.llt_verify_result);
        isResult = false;

        ilt_phone = findViewById(R.id.ilt_verify_phone);

        btn_send = findViewById(R.id.btn_verify_send);

        lbl_detail = findViewById(R.id.lbl_verify_detail);
        lbl_resend = findViewById(R.id.lbl_verify_resend);

        OtpTextView otp_code = findViewById(R.id.otp_verify);
        otp_code.setOtpListener(otpListener);


        setResultView();
    }

    private void setResultView() {
        if (isResult) {
            llt_send.setVisibility(View.GONE);
            llt_result.setVisibility(View.VISIBLE);
        } else {
            llt_send.setVisibility(View.VISIBLE);
            llt_result.setVisibility(View.GONE);
        }
    }

    private void saveUserInfo(UserInfo userInfo) {
        SharedPreferencesManager.saveMyPhoto(userInfo.getPhoto());
        SharedPreferencesManager.saveMyUsername(userInfo.getName());
        SharedPreferencesManager.saveSurname(userInfo.getSurname());
        SharedPreferencesManager.saveEmail(userInfo.getEmail());
        SharedPreferencesManager.saveBirthday(userInfo.getBirthDate());
        SharedPreferencesManager.saveMyStatus(userInfo.getStatus());
        SharedPreferencesManager.savePhoneNumber(userInfo.getPhone());
        SharedPreferencesManager.saveMyThumbImg(userInfo.getThumbImg());

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.createInstance(this);
        Phonenumber.PhoneNumber numberProto;
        try {
            numberProto = phoneUtil.parse(FireManager.getPhoneNumber(), "");
            String countryCode = phoneUtil.getRegionCodeForNumber(numberProto);
            SharedPreferencesManager.saveCountryCode(countryCode);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        ServiceHelper.fetchUserGroupsAndBroadcasts(this);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions, PERMISSION_REQUEST_CODE);
    }

    private void onCompleteVerify(PhoneAuthCredential credential) {
        dialog.show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startTheActivity();
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Util.showSnackbar(VerifyActivity.this, task.getException().getMessage());
                        }
                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> dialog.dismiss());
    }

    private void startTheActivity() {
        FireManager.addFriendsToRealm(this);
        if (SharedPreferencesManager.isUserInfoSaved()) {
            dialog.dismiss();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            if( FireManager.getUid() != null ) {
                FireManager.getUserInfoByUid(FireManager.getUid(), new FireManager.userInfoListener() {
                    @Override
                    public void onFound(UserInfo userInfo) {
                        dialog.dismiss();

                        saveUserInfo(userInfo);

                        boolean isFirst = SharedPreferencesManager.getFlagFirst();
                        if (isFirst) {
                            Intent intent = new Intent(VerifyActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(VerifyActivity.this, InformationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onNotFound() {
                        dialog.dismiss();

                        SharedPreferencesManager.savePhoneNumber(FireManager.getPhoneNumber());
                        Intent intent = new Intent(VerifyActivity.this, InformationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }
    }

    private void showAlertDialog() {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogDark);
            builder.setTitle(R.string.missing_permissions)
                    .setMessage(R.string.you_have_to_grant_permissions)
                    .setNegativeButton(R.string.ok, (dialogInterface, i) -> requestPermissions())
                    .setNeutralButton(R.string.no_close_the_app, (dialogInterface, i) -> finish());
            builder.show();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogLight);
            builder.setTitle(R.string.missing_permissions)
                    .setMessage(R.string.you_have_to_grant_permissions)
                    .setNegativeButton(R.string.ok, (dialogInterface, i) -> requestPermissions())
                    .setNeutralButton(R.string.no_close_the_app, (dialogInterface, i) -> finish());
            builder.show();
        }
    }

    private void exitDialog() {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogDark);
            dialog.setIcon(R.mipmap.ic_launcher_round);
            dialog.setTitle(R.string.app_name);
            dialog.setMessage(getString(R.string.alert_onback));
            dialog.setNegativeButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            });
            dialog.setNeutralButton(getResources().getString(R.string.no), (dialogInterface, i) -> { });
            dialog.show();
        } else {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this, R.style.AlertDialogLight);
            dialog.setIcon(R.mipmap.ic_launcher_round);
            dialog.setTitle(R.string.app_name);
            dialog.setMessage(getString(R.string.alert_onback));
            dialog.setNegativeButton(getResources().getString(R.string.yes), (dialogInterface, i) -> {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            });
            dialog.setNeutralButton(getResources().getString(R.string.no), (dialogInterface, i) -> { });
            dialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            initWithView();
            initWithEvent();
        } else {
            showAlertDialog();
        }
    }

    @Override
    public void onBackPressed() {
        exitDialog();
    }

}
