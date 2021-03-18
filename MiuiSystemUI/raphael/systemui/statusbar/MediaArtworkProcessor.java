package com.android.systemui.statusbar;

import android.graphics.Bitmap;
import android.graphics.Point;

/* compiled from: MediaArtworkProcessor.kt */
public final class MediaArtworkProcessor {
    private Bitmap mArtworkCache;
    private final Point mTmpSize = new Point();

    /* JADX WARNING: Removed duplicated region for block: B:54:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x0116  */
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final android.graphics.Bitmap processArtwork(@org.jetbrains.annotations.NotNull android.content.Context r9, @org.jetbrains.annotations.NotNull android.graphics.Bitmap r10) {
        /*
        // Method dump skipped, instructions count: 282
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.MediaArtworkProcessor.processArtwork(android.content.Context, android.graphics.Bitmap):android.graphics.Bitmap");
    }

    public final void clearCache() {
        Bitmap bitmap = this.mArtworkCache;
        if (bitmap != null) {
            bitmap.recycle();
        }
        this.mArtworkCache = null;
    }
}
