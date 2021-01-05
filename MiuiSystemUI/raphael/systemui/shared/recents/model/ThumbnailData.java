package com.android.systemui.shared.recents.model;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

public class ThumbnailData {
    public final Bitmap thumbnail;

    public ThumbnailData(ActivityManager.TaskSnapshot taskSnapshot) {
        GraphicBuffer snapshot = taskSnapshot.getSnapshot();
        if (snapshot == null || (((long) snapshot.getUsage()) & 256) == 0) {
            Log.e("ThumbnailData", "Unexpected snapshot without USAGE_GPU_SAMPLED_IMAGE: " + snapshot);
            Point taskSize = taskSnapshot.getTaskSize();
            Bitmap createBitmap = Bitmap.createBitmap(taskSize.x, taskSize.y, Bitmap.Config.ARGB_8888);
            this.thumbnail = createBitmap;
            createBitmap.eraseColor(-16777216);
        } else {
            this.thumbnail = Bitmap.wrapHardwareBuffer(snapshot, taskSnapshot.getColorSpace());
        }
        new Rect(taskSnapshot.getContentInsets());
        taskSnapshot.getOrientation();
        taskSnapshot.getRotation();
        taskSnapshot.isLowResolution();
        this.thumbnail.getWidth();
        int i = taskSnapshot.getTaskSize().x;
        taskSnapshot.isRealSnapshot();
        taskSnapshot.isTranslucent();
        taskSnapshot.getWindowingMode();
        taskSnapshot.getSystemUiVisibility();
        taskSnapshot.getId();
    }
}
