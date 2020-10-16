package com.hostcart.socialbot.utils;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hostcart.socialbot.job.DeleteStatusJob;
import com.hostcart.socialbot.model.TextStatus;
import com.hostcart.socialbot.model.constants.StatusType;
import com.hostcart.socialbot.model.realms.Status;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.model.realms.UserStatuses;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import io.realm.RealmResults;

public class StatusManager {
    private static List<String> currentDownloadStatusOperations = new ArrayList<>();

    public static void downloadVideoStatus(final String id, String url, final File file, final OnStatusDownloadComplete onComplete) {
        //prevent duplicates download
        if (currentDownloadStatusOperations.contains(id))
            return;

        currentDownloadStatusOperations.add(id);
        FireConstants.storageRef.child(url)
                .getFile(file)
                .addOnCompleteListener(task -> {
                    if (currentDownloadStatusOperations.contains(id))
                        currentDownloadStatusOperations.remove(currentDownloadStatusOperations);
                    if (task.isSuccessful()) {
                        RealmHelper.getInstance().setLocalPathForVideoStatus(id, file.getPath());
                        if (onComplete != null)
                            onComplete.onComplete(file.getPath());
                    } else {
                        if (onComplete != null)
                            onComplete.onComplete(null);
                    }
                });
    }


    public static void deleteStatus(final String statusId, int statusType, final DeleteStatus onComplete) {
        FireConstants.getMyStatusRef(statusType).child(statusId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (onComplete != null) {
                    onComplete.onComplete(task.isSuccessful(), statusId);
                }
                if (task.isSuccessful()) {
                    RealmHelper.getInstance().deleteStatus(FireManager.getUid(), statusId);
                }
            }
        });
    }


    public static void uploadStatus(final String filePath, int statusType, final boolean isVideo, final UploadStatusCallback uploadStatusCallback) {
        final String fileName = Util.getFileNameFromPath(filePath);

        final Status status;
        if (isVideo)
            status = StatusCreator.createVideoStatus(filePath);
        else
            status = StatusCreator.createImageStatus(filePath);

        FireManager.getRef(FireManager.STATUS_TYPE, fileName).putFile(Uri.fromFile(new File(filePath))).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> uploadTask) {
                if (uploadTask.isSuccessful()) {
                    if (!isVideo) {
                        uploadTask.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                final Uri uri = task.getResult().normalizeScheme();
                                status.setContent(String.valueOf(uri));
                                FireConstants.getMyStatusRef(statusType)
                                        .child(status.getStatusId()).updateChildren(status.toMap()).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                RealmHelper.getInstance().saveStatus(FireManager.getUid(), status);
                                                DeleteStatusJob.schedule(status.getUserId(), status.getStatusId());
                                            }

                                            uploadStatusCallback.onComplete(task1.isSuccessful());
                                        });
                            } else {
                                uploadStatusCallback.onComplete(false);
                            }
                        });

                    } else {
                        final String filePathBucket = uploadTask.getResult().getStorage().getPath();
                        status.setContent(String.valueOf(filePathBucket));
                        FireConstants.getMyStatusRef(statusType)
                                .child(status.getStatusId()).updateChildren(status.toMap()).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        RealmHelper.getInstance().saveStatus(FireManager.getUid(), status);
                                        DeleteStatusJob.schedule(status.getUserId(), status.getStatusId());
                                    }
                                    uploadStatusCallback.onComplete(task.isSuccessful());

                                });
                    }
                } else {
                    if (uploadStatusCallback != null)
                        uploadStatusCallback.onComplete(false);
                }
            }
        });
    }

    public static void uploadTextStatus(TextStatus textStatus, final UploadStatusCallback uploadStatusCallback) {
        Status status = StatusCreator.createTextStatus(textStatus);
        FireConstants.getMyStatusRef(StatusType.TEXT)
                .child(status.getStatusId()).updateChildren(status.toMap()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        RealmHelper.getInstance().saveStatus(FireManager.getUid(), status);
                        DeleteStatusJob.schedule(status.getUserId(), status.getStatusId());
                    }
                    uploadStatusCallback.onComplete(task.isSuccessful());

                });

    }

    public static void getStatusList( LoadStatus loadStatus ) {
        RealmResults<User> users = RealmHelper.getInstance().getListOfUsers();

        for( User user : users ) {
            getMyStatus(user.getUid(), loadStatus, users.size());
//                        String uid = (String) sub.getValue();
//                        UserStatuses userStatuses = RealmHelper.getInstance().getUserStatuses(uid);
//                        for( int i=0; i<userStatuses.getStatuses().size(); i++ )
//                            RealmHelper.getInstance().deleteStatus(uid, userStatuses.getStatuses().get(i).getStatusId());
        }
//        FireConstants.uidByPhone.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if( dataSnapshot.getValue() != null ) {
//                    for( DataSnapshot sub : dataSnapshot.getChildren() ) {
//                        getMyStatus((String) sub.getValue(), loadStatus, (int)dataSnapshot.getChildrenCount());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                int kkk = 0;
//            }
//        });
    }

    static int count = 0;
    public static void getMyStatus( String uid, LoadStatus loadStatus, int childCount ) {
        FireConstants.statusRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    String key = dataSnapshot.getKey();
                    HashMap<String,Object> statusList = (HashMap<String,Object>) dataSnapshot.getValue();
                    for(Map.Entry<String, Object> entry : statusList.entrySet()) {
                        String statusId = entry.getKey();
                        Object value = entry.getValue();

                        Status status = new Status();
                        status.setStatusId(statusId);
                        status.setUserId(key);
                        if( value instanceof HashMap ) {
                            HashMap<String, Object> valuesMap =(HashMap) value;
                            for(Map.Entry<String, Object> ventry :valuesMap.entrySet()) {
                                String vkey = ventry.getKey();
                                Object vvalue = ventry.getValue();

                                switch (vkey) {
                                    case "content":
                                        status.setContent(vvalue.toString());
                                        break;
                                    case "duration":
                                        status.setDuration(Integer.parseInt(vvalue.toString()));
                                        break;
                                    case "thumbImg":
                                        status.setThumbImg(vvalue.toString());
                                        break;
                                    case "timestamp":
                                        status.setTimestamp(Long.parseLong(vvalue.toString()));
                                        break;
                                    case "type":
                                        status.setType(Integer.parseInt(vvalue.toString()));
                                        break;
                                }
                            }
                        }
                        RealmHelper.getInstance().saveStatus(key, status);
                    }
                    count ++;
                    UserStatuses userStatuses = RealmHelper.getInstance().getUserStatuses(uid);
                    if( count == childCount )
                        loadStatus.onComplete(userStatuses);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public interface LoadStatus {
        void onComplete(UserStatuses userStatuses);
    }

    public interface DeleteStatus {
        void onComplete(boolean isSuccessful, String id);
    }

    public interface UploadStatusCallback {
        void onComplete(boolean isSuccessful);
    }

    public interface OnStatusDownloadComplete {
        void onComplete(String path);
    }
}
