package com.hostcart.socialbot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.constants.MessageType;
import com.hostcart.socialbot.model.constants.NetworkType;
import com.hostcart.socialbot.model.realms.User;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Devlomi on 01/02/2017.
 */

public class SharedPreferencesManager {

    //this will contains the app preferences
    private static SharedPreferences mSharedPref;
    private static SharedPreferences mAddedPref;
    private static SharedPreferences mRemainPref;
    private static SharedPreferences mFriendsPref;
    private static SharedPreferences mPostPref;

    private static SharedPreferences mThemePref;

    //get key of auto download from settings shared
    private static String key_autodownload_roaming;
    private static String key_autodownload_wifi;
    private static String key_autodownload_cellular;

    //check what user is enabled (video,audio,images) for every state (wifi,cellular,roaming)
    private static Set<String> defaultWifiSet;
    private static Set<String> defaultCellularSet;
    private static Set<String> defaultRoamingSet;

    synchronized public static void init(Context context) {
        if (mSharedPref == null)
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if( mAddedPref == null )
            mAddedPref = context.getSharedPreferences("InvitedList", Context.MODE_PRIVATE);

        if( mRemainPref == null )
            mRemainPref = context.getSharedPreferences("RemainList", Context.MODE_PRIVATE);

        if( mFriendsPref == null )
            mFriendsPref = context.getSharedPreferences("FriendsList", Context.MODE_PRIVATE);

        if( mPostPref == null )
            mPostPref = context.getSharedPreferences("PostList", Context.MODE_PRIVATE);

        if( mThemePref == null )
            mThemePref = context.getSharedPreferences("Theme", Context.MODE_PRIVATE);

        if (defaultWifiSet == null)
            defaultWifiSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.autodownload_wifi_defaults)));

        if (defaultCellularSet == null)
            defaultCellularSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.autodownload_cellular_defaults)));

        if (defaultRoamingSet == null)
            defaultRoamingSet = new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.autodownload_roaming_defaults)));

        if (key_autodownload_wifi == null) {
            key_autodownload_wifi = context.getResources().getString(R.string.key_autodownload_wifi);
        }

        if (key_autodownload_cellular == null) {
            key_autodownload_cellular = context.getResources().getString(R.string.key_autodownload_cellular);
        }
        if (key_autodownload_roaming == null) {
            key_autodownload_roaming = context.getResources().getString(R.string.key_autodownload_roaming);
        }

    }

    public static boolean getFlagPrivacy() {
        return mSharedPref.getBoolean("isprivacy", false);
    }

    public static void setFlagPrivacy(boolean flag) {
        mSharedPref.edit().putBoolean("isprivacy", flag).apply();
    }

    public static boolean getFlagFirst() {
        return mSharedPref.getBoolean("isfirst", false);
    }

    public static void setFlagFirst(boolean flag) {
        mSharedPref.edit().putBoolean("isfirst", flag).apply();
    }

    public static String getThemeMode() {
        return mThemePref.getString("thememode", AppUtils.THEME_DARK);
    }

    public static void setThemeMode(String themeMode) {
        mThemePref.edit().putString("thememode", themeMode).apply();
    }

    public static void setContactSynced(boolean isSynced) {
        mSharedPref.edit().putBoolean("isSynced", isSynced).apply();
    }

    public static boolean isContactSynced() {
        return mSharedPref.getBoolean("isSynced", false);
    }

    public static boolean isEnterIsSend() {
        return mSharedPref.getBoolean("enter_is_send", false);
    }

    //this will return if user has enabled auto download for certain network type and for certain media type
    static boolean canDownload(int mediaType, int availableNetworkType) {
        if (mediaType == MessageType.RECEIVED_VOICE_MESSAGE) return true;
        String value = String.valueOf(getTypeValueByMediaType(mediaType));

        switch (availableNetworkType) {
            case NetworkType.WIFI:
                Set<String> stringSetWifi = mSharedPref.getStringSet(key_autodownload_wifi, defaultWifiSet);
                if (stringSetWifi.contains(value))
                    return true;

                break;
            case NetworkType.DATA:
                Set<String> stringSetData = mSharedPref.getStringSet(key_autodownload_cellular, defaultCellularSet);
                if (stringSetData.contains(value))
                    return true;
                break;
            case NetworkType.ROAMING:
                Set<String> stringSetRoaming = mSharedPref.getStringSet(key_autodownload_roaming, defaultRoamingSet);
                if (stringSetRoaming.contains(value))
                    return true;
                break;
        }
        return false;
    }

    private static int getTypeValueByMediaType(int mediaType) {
        switch (mediaType) {
            case MessageType.RECEIVED_IMAGE:
                return 0;
            case MessageType.RECEIVED_AUDIO:
                return 1;
            case MessageType.RECEIVED_VIDEO:
                return 2;

            default:
                return 3;
        }
    }

    // save post list
    static void savePostListJsonString(String postListJsonString) {
        mPostPref.edit().putString("posts", postListJsonString).apply();
    }

    static String getPostListJsonString() {
        return mPostPref.getString("posts", "");
    }

    // save added me user list
    public static void saveAddedMeListJsonString(String addedmeListJsonString) {
        mAddedPref.edit().putString("addedme", addedmeListJsonString).apply();
    }

    public static String getAddedMeListJsonString() {
        return mAddedPref.getString("addedme", "");
    }

    public static void saveRemailListJsonString(String remainListJsonString) {
        mRemainPref.edit().putString("remain", remainListJsonString).apply();
    }

    public static String getRemainListJsonString() {
        return mRemainPref.getString("remain", "");
    }

    public static void saveFriendsListJsonString(String friendsListJsonString) {
        mFriendsPref.edit().putString("friends", friendsListJsonString).apply();
    }

    public static String getFriendsListJsonString() {
        return mFriendsPref.getString("friends", "");
    }

    //save user status locally to show it his profile
    public static void saveMyStatus(String status) {
        mSharedPref.edit().putString("status", status).apply();
    }

    //save user username locally to show it his profile
    public static void saveMyUsername(String username) {
        mSharedPref.edit().putString("username", username).apply();
    }

    //save user photo path locally to show it his profile
    public static void saveMyPhoto(String path) {
        mSharedPref.edit().putString("user_image", path).apply();
    }

    //save user phone number locally to show it his profile
    public static void savePhoneNumber(String phoneNumber) {
        mSharedPref.edit().putString("phone_number", phoneNumber).apply();
    }

    public static void saveSurname(String surname) {
        mSharedPref.edit().putString("surname", surname).apply();
    }

    public static void saveEmail(String email) {
        mSharedPref.edit().putString("email", email).apply();
    }

    public static void saveBirthday(String birthday) {
        mSharedPref.edit().putString("birthday", birthday).apply();
    }

    public static void saveGender(String gender) {
        mSharedPref.edit().putString("gender", gender).apply();
    }

    public static void saveLanguage(String language) {
        mSharedPref.edit().putString("lang", language).apply();
    }

    public static String getUserName() {
        return mSharedPref.getString("username", "");
    }

    public static String getStatus() {
        return mSharedPref.getString("status", "");
    }

    public static String getPhoneNumber() {
        return mSharedPref.getString("phone_number", "");
    }

    public static String getMyPhoto() {
        return mSharedPref.getString("user_image", "");
    }

    public static String getSurname() {
        return mSharedPref.getString("surname", "");
    }

    public static String getEmail() {
        return mSharedPref.getString("email", "");
    }

    public static String getBirthday() {
        return mSharedPref.getString("birthday", "");
    }

    public static String getGender() { return mSharedPref.getString("gender", "Male"); }

    public static String getLanguage() { return mSharedPref.getString("lang", "English");}

    //check if user enabled vibration for notifications
    static boolean isVibrateEnabled() {
        return mSharedPref.getBoolean("notifications_new_message_vibrate", false);
    }

    //get notification ringtone
    static Uri getRingtone() {
        return Uri.parse(mSharedPref.getString("notifications_new_message_ringtone", "content://settings/system/notification_sound"));
    }

    //check if user enabled notifications
    static boolean isNotificationEnabled() {
        return mSharedPref.getBoolean("notifications_new_message", true);
    }

    //check if user info is saved when launch the app for first time
    public static boolean isUserInfoSaved() {
        return mSharedPref.getBoolean("is_userInfo_saved", false);
    }

    public static void setUserInfoSaved(boolean bool) {
        mSharedPref.edit().putBoolean("is_userInfo_saved", bool).apply();
    }

    //save country code to use it late when formatting the numbers
    public static void saveCountryCode(String phoneNumber) {
        mSharedPref.edit().putString("ccode", phoneNumber).apply();
    }

    public static void saveMyThumbImg(String thumbImg) {
        mSharedPref.edit().putString("thumbImg", thumbImg).apply();
    }

    public static String getThumbImg() {
        return mSharedPref.getString("thumbImg", "");
    }

    static String getCountryCode() {
        return mSharedPref.getString("ccode", "");
    }

    // set notification token as saved to prevent resending it to server
    public static void setTokenSaved(boolean bool) {
        mSharedPref.edit().putBoolean("token_sent", bool).apply();
    }

    public static boolean isTokenSaved() {
        return mSharedPref.getBoolean("token_sent", false);
    }

    public static User getCurrentUser() {
        User user = new User();
        user.setUid(FireManager.getUid());
        user.setThumbImg(SharedPreferencesManager.getThumbImg());
        user.setPhoto("");
        user.setPhone(SharedPreferencesManager.getPhoneNumber());
        user.setStatus(SharedPreferencesManager.getStatus());
        user.setUserName(SharedPreferencesManager.getUserName());
        user.setUserLocalPhoto(SharedPreferencesManager.getMyPhoto());
        return user;
    }

    public static void setFetchUserGroupsSaved(boolean b) {
        mSharedPref.edit().putBoolean("fetch_user_groups_saved", b).apply();
    }

    public static boolean isFetchedUserGroups() {
        return mSharedPref.getBoolean("fetch_user_groups_saved", false);
    }

    public static void setAppVersionSaved(boolean b) {
        mSharedPref.edit().putBoolean("is_app_ver_saved", b).apply();
    }

    public static boolean isAppVersionSaved() {
        return mSharedPref.getBoolean("is_app_ver_saved", false);
    }

    static void setLastSeenState(int lastSeenState) {
        mSharedPref.edit().putInt("lastSeenState", lastSeenState).apply();
    }

    public static int getLastSeenState() {
        if (mSharedPref == null) return 0;
        return mSharedPref.getInt("lastSeenState", 0);
    }

    public static void setSinchConfigured(boolean value) {
        mSharedPref.edit().putBoolean("sinchConfigured", value).apply();
    }

    public static boolean isSinchConfigured() {
        return mSharedPref.getBoolean("sinchConfigured", false);
    }

    public static void setWallpaperPath(String value) {
        mSharedPref.edit().putString("wallpaperPath", value).apply();
    }

    public static String getWallpaperPath() {
        return mSharedPref.getString("wallpaperPath", "");
    }

    static void setLastBackup(long value) {
        mSharedPref.edit().putLong("lastBackup", value).apply();
    }

    public static long getLastBackup() {
        return mSharedPref.getLong("lastBackup", -1);
    }

    //used to determine whether the UID,and Phone number were saved
    public static void setCurrentUserInfoSaved(boolean value) {
        mSharedPref.edit().putBoolean("currentUserInfoSaved", value).apply();
    }

    public static boolean isCurrentUserInfoSaved() {
        return mSharedPref.getBoolean("currentUserInfoSaved", false);
    }

    public static void setDoNotShowBatteryOptimizationAgain(boolean value) {
        mSharedPref.edit().putBoolean("doNotShowBatteryOptimizationAgain", value).apply();
    }

    public static boolean isDoNotShowBatteryOptimizationAgain() {
        return mSharedPref.getBoolean("doNotShowBatteryOptimizationAgain", false);
    }

}
