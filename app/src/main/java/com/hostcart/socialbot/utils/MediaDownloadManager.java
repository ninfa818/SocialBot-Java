package com.hostcart.socialbot.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class MediaDownloadManager {

    public static void downloadMedia(String url, final File file, final OnComplete onComplete) {
        StorageReference storageReference = FireConstants.postsStorageRef.child(url);
        storageReference
                .getFile(file)
                .addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            onComplete.onComplete(true);
                        } else {

                        }
                    }
                });

    }

    public interface OnComplete {
        void onComplete(boolean isSuccess);
    }

}
