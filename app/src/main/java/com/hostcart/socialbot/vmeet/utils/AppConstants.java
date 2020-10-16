package com.hostcart.socialbot.vmeet.utils;

import android.content.Context;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class AppConstants {

    public static final String USER_INFO = "user_info";

    public static final String INTENT_BEAN = "BeanData";
    public static final String INTENT_ID = "ID";

    public static String NAME = "NAME";
    public static String MEETING_ID = "MEETING_ID";

    public static final String IMAGE_DIRECTORY_NAME = "VMeet";
    public static final String Storage_Path = "Images/";

    public static class Table {
        public static String MEETING_HISTORY = "MeetingHistory";
        public static String SCHEDULE = "Schedule";
    }

    public static class DateFormats {

        public static String DATE_FORMAT_DD_MMM_YYYY = "dd MMM yyyy";
        public static String DATE_FORMAT_DASH = "dd-MM-yyyy";

        public static String TIME_FORMAT_12 = "hh:mm a";
        public static String TIME_FORMAT_24 = "HH:mm";

        public static String DATETIME_FORMAT_24 = "dd-MM-yyyy HH:mm:ss";
        public static String DATETIME_FORMAT_12 = "dd-MM-yyyy hh:mm a";
    }

    public static boolean checkDateisFuture(String selectedDate){
        SimpleDateFormat myFormat = new SimpleDateFormat(DateFormats.DATE_FORMAT_DASH, Locale.getDefault());
        String strSelectedDate = selectedDate;

        try {
            Date date1 = myFormat.parse(strSelectedDate);
            Date date2 = myFormat.parse(SharedObjects.getTodaysDate(AppConstants.DateFormats.DATE_FORMAT_DASH));

            if (date2.before(date1)) {
                return true ;
            }else if (date2.equals(date1)) {
                return true ;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void showSnackBar(String message, View view) {
        final Snackbar snackBar = Snackbar.make(view,message, Snackbar.LENGTH_SHORT);
        snackBar.show();
    }

    public static void showAlertDialog(String Msg, Context context) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogDark);
            materialAlertDialogBuilder.setMessage(Msg);
            materialAlertDialogBuilder.setCancelable(false)
                    .setNegativeButton(context.getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            materialAlertDialogBuilder.show();
        } else {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogLight);
            materialAlertDialogBuilder.setMessage(Msg);
            materialAlertDialogBuilder.setCancelable(false)
                    .setNegativeButton(context.getResources().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
            materialAlertDialogBuilder.show();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String getMeetingCode() {

        String meetingCode = "";

        String randomString = getRandomString(9);
        int size = 3;
        for (int start = 0; start < randomString.length(); start += size) {
            meetingCode += randomString.substring(start, Math.min(randomString.length(), start + size)) + "-";
        }
        return meetingCode.substring(0, meetingCode.length() - 1);
    }

    private static final String ALLOWED_CHARACTERS = "qwertyuiopasdfghjklzxcvbnm";

    private static String getRandomString(final int sizeOfRandomString) {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

}
