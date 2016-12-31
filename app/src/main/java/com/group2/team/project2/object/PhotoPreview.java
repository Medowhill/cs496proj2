package com.group2.team.project2.object;

import android.graphics.Bitmap;

/**
 * Created by q on 2016-12-31.
 */

public class PhotoPreview {

    private Bitmap bitmap;
    private String time;

    public PhotoPreview(Bitmap bitmap, String time) {
        this.bitmap = bitmap;
        this.time = time;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getTime() {
        return time;
    }
}
