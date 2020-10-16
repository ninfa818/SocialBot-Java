package com.hostcart.socialbot.utils;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Environment;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.MoreSectionModel;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.vmeet.utils.AppConstants;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppUtils {

    public static String THEME_DARK = "dark";
    public static String THEME_LIGHT = "light";

    public static String gUid = "";

    public static Posts gPosts = new Posts();
    public static PostMedia gPostMedia = new PostMedia();
    public static boolean gEditPost = false;
    public static MoreSectionModel gSelMoreSection = new MoreSectionModel();

    public static void showOtherActivity (Context context, Class<?> cls, int direction) {
        Intent myIntent = new Intent(context, cls);
        ActivityOptions options;
        switch (direction) {
            case 0:
                options = ActivityOptions.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_left);
                context.startActivity(myIntent, options.toBundle());
                break;
            case 1:
                options = ActivityOptions.makeCustomAnimation(context, R.anim.slide_in_left, R.anim.slide_out_right);
                context.startActivity(myIntent, options.toBundle());
                break;
            default:
                context.startActivity(myIntent);
                break;
        }
    }

    private static Translate translate;
    public static void getTranslateService(Context context) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = context.getResources().openRawResource(R.raw.credentials)) {

            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static String translate(Context context, String language, String message) {
        String code = "en";

        Resources res = context.getResources();
        String[] languages = res.getStringArray(R.array.language_array);
        String[] codes = res.getStringArray(R.array.code_array);

        for( int i=0; i<languages.length; i++ ) {
            if( language.equals(languages[i]) ) {
                code = codes[i];
                break;
            }
        }

        Translation translation = translate.translate(message, Translate.TranslateOption.targetLanguage(code), Translate.TranslateOption.model("base"));

        return translation.getTranslatedText();
    }

    public static void onDownloadFileResponse(String postid, String mediaUrl, String type, DownloadResponse downlaodResponse) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(mediaUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                downlaodResponse.onFailedListener(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }

                File rootPath = new File(Environment.getExternalStorageDirectory(), "Socialbot/Post/");
                if(!rootPath.exists()) {
                    boolean rootable = rootPath.mkdirs();
                    if (!rootable) {
                        downlaodResponse.onFailedListener("Permission Denied.");
                        return;
                    }
                }

                File localFile = new File(rootPath, postid + "_" + 0 + type);
                if (!localFile.exists()) {
                    try {
                        FileOutputStream out = new FileOutputStream(localFile);
                        out.write(response.body().bytes());
                    } catch (Exception e) {
                        downlaodResponse.onFailedListener(e.getMessage());
                    }
                }
                downlaodResponse.onSuccessListener(localFile.getAbsolutePath());
            }
        });
    }

    public interface DownloadResponse {
        void onSuccessListener(String mediaUri);
        void onFailedListener(String error);
    }

}
