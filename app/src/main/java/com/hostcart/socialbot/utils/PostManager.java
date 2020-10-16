package com.hostcart.socialbot.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostManager {

    public static String findVideo( String uri ) {
        String filePath = uri;
        if( uri.indexOf("+") != -1 ) {
            filePath = uri.substring(uri.indexOf("+")+1);
        }
        return filePath;
    }

    public static int getCommentCount( HashMap<String,Object> comments ) {
        if( comments == null ) return 0;
        return comments.size();
    }

    private static List<String> currentDownloadPostOperations = new ArrayList<>();

    public static void downloadVideoPost(final String id, String url, final File file, final OnPostDownloadComplete onComplete) {
        if (currentDownloadPostOperations.contains(id))
            return;

        currentDownloadPostOperations.add(id);
        FireConstants.storageRef.child(url)
                .getFile(file)
                .addOnCompleteListener(task -> {
                    if (currentDownloadPostOperations.contains(id))
                        currentDownloadPostOperations.remove(currentDownloadPostOperations);

                    if (task.isSuccessful()) {
                        if (onComplete != null)
                            onComplete.onComplete(file.getPath());
                    } else {
                        if (onComplete != null)
                            onComplete.onComplete(null);
                    }
                });
    }

    public interface OnPostDownloadComplete {
        void onComplete(String path);
    }

}
