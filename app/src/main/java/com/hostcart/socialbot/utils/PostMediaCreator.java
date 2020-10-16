package com.hostcart.socialbot.utils;

import com.hostcart.socialbot.model.Post;
import com.hostcart.socialbot.model.constants.StatusType;

import java.util.Date;

public class PostMediaCreator {

    public static PostMedia createImagePost(String imagePath) {
        String thumbImg = BitmapUtils.decodeImage(imagePath, false);
        PostMedia postMedia = new PostMedia(FireManager.getUid(), null, null, 0,
                thumbImg, imagePath, new Date().getTime(), 1);

        return postMedia;
    }

    public static PostMedia createVideoPost( String videoPath ) {
        String thumbImg = BitmapUtils.generateVideoThumbAsBase64(videoPath);
        long mediaLengthInMillis = Util.getMediaLengthInMillis(MyApp.context(), videoPath);

        PostMedia postMedia = new PostMedia(FireManager.getUid(), null, null, mediaLengthInMillis,
                thumbImg, videoPath, new Date().getTime(), 2);

        return postMedia;
    }

    public static PostMedia createYoutubePost(String youtubePath) {
        String[] ids = youtubePath.split("/");
        return new PostMedia(FireManager.getUid(), null, youtubePath, 0,
                ids[ids.length - 1], "", new Date().getTime(), 3);
    }

    public static Post createLocationPost( String latlng ) {
        return null;
    }
}
