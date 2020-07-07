package com.android.systemui.recents.model;

import android.graphics.Bitmap;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;

public class ThumbnailData {
    public boolean isAccessLocked;
    public boolean isDeterminedWhetherBlur;
    public Bitmap thumbnail;
    public ActivityManager$TaskThumbnailInfo thumbnailInfo;
}
