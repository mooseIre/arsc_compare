package com.android.systemui.statusbar;

import android.graphics.Bitmap;
import android.graphics.Point;

/* compiled from: MediaArtworkProcessor.kt */
public final class MediaArtworkProcessor {
    private Bitmap mArtworkCache;
    private final Point mTmpSize = new Point();

    /* JADX WARNING: Removed duplicated region for block: B:55:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x00f9  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0101  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0116  */
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final android.graphics.Bitmap processArtwork(@org.jetbrains.annotations.NotNull android.content.Context r9, @org.jetbrains.annotations.NotNull android.graphics.Bitmap r10) {
        /*
            r8 = this;
            java.lang.String r0 = "inBitmap"
            java.lang.String r1 = "context"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r9, r1)
            java.lang.String r1 = "artwork"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r10, r1)
            android.graphics.Bitmap r1 = r8.mArtworkCache
            if (r1 == 0) goto L_0x0011
            return r1
        L_0x0011:
            android.renderscript.RenderScript r1 = android.renderscript.RenderScript.create(r9)
            android.renderscript.Element r2 = android.renderscript.Element.U8_4(r1)
            android.renderscript.ScriptIntrinsicBlur r2 = android.renderscript.ScriptIntrinsicBlur.create(r1, r2)
            r3 = 0
            android.view.Display r9 = r9.getDisplay()     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            if (r9 == 0) goto L_0x0029
            android.graphics.Point r4 = r8.mTmpSize     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            r9.getSize(r4)     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
        L_0x0029:
            android.graphics.Rect r9 = new android.graphics.Rect     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r4 = r10.getWidth()     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r5 = r10.getHeight()     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            r6 = 0
            r9.<init>(r6, r6, r4, r5)     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            android.graphics.Point r4 = r8.mTmpSize     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r4 = r4.x     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r4 = r4 / 6
            android.graphics.Point r8 = r8.mTmpSize     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r8 = r8.y     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r8 = r8 / 6
            int r8 = java.lang.Math.max(r4, r8)     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            android.util.MathUtils.fitRect(r9, r8)     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r8 = r9.width()     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            int r9 = r9.height()     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            r4 = 1
            android.graphics.Bitmap r8 = android.graphics.Bitmap.createScaledBitmap(r10, r8, r9, r4)     // Catch:{ IllegalArgumentException -> 0x00e7, all -> 0x00e3 }
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r0)     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.graphics.Bitmap$Config r9 = r8.getConfig()     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.graphics.Bitmap$Config r4 = android.graphics.Bitmap.Config.ARGB_8888     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            if (r9 == r4) goto L_0x0075
            android.graphics.Bitmap$Config r9 = android.graphics.Bitmap.Config.ARGB_8888     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.graphics.Bitmap r9 = r8.copy(r9, r6)     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            r8.recycle()     // Catch:{ IllegalArgumentException -> 0x0071, all -> 0x006d }
            r8 = r9
            goto L_0x0075
        L_0x006d:
            r8 = move-exception
            r1 = r3
            goto L_0x0107
        L_0x0071:
            r8 = move-exception
            r0 = r3
            goto L_0x00ea
        L_0x0075:
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r0)     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            int r9 = r8.getWidth()     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            int r0 = r8.getHeight()     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.graphics.Bitmap$Config r4 = android.graphics.Bitmap.Config.ARGB_8888     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.graphics.Bitmap r9 = android.graphics.Bitmap.createBitmap(r9, r0, r4)     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.renderscript.Allocation$MipmapControl r0 = android.renderscript.Allocation.MipmapControl.MIPMAP_NONE     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            r4 = 2
            android.renderscript.Allocation r0 = android.renderscript.Allocation.createFromBitmap(r1, r8, r0, r4)     // Catch:{ IllegalArgumentException -> 0x00dc, all -> 0x00d6 }
            android.renderscript.Allocation r1 = android.renderscript.Allocation.createFromBitmap(r1, r9)     // Catch:{ IllegalArgumentException -> 0x00d3, all -> 0x00cf }
            r4 = 1103626240(0x41c80000, float:25.0)
            r2.setRadius(r4)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            r2.setInput(r0)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            r2.forEach(r1)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            r1.copyTo(r9)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            androidx.palette.graphics.Palette$Swatch r10 = com.android.systemui.statusbar.notification.MediaNotificationProcessor.findBackgroundSwatch((android.graphics.Bitmap) r10)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            android.graphics.Canvas r4 = new android.graphics.Canvas     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            r4.<init>(r9)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            java.lang.String r5 = "swatch"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r10, r5)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            int r10 = r10.getRgb()     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            r5 = 178(0xb2, float:2.5E-43)
            int r10 = com.android.internal.graphics.ColorUtils.setAlphaComponent(r10, r5)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            r4.drawColor(r10)     // Catch:{ IllegalArgumentException -> 0x00cd, all -> 0x00cb }
            if (r0 == 0) goto L_0x00bf
            r0.destroy()
        L_0x00bf:
            if (r1 == 0) goto L_0x00c4
            r1.destroy()
        L_0x00c4:
            r2.destroy()
            r8.recycle()
            return r9
        L_0x00cb:
            r9 = move-exception
            goto L_0x00d1
        L_0x00cd:
            r9 = move-exception
            goto L_0x00df
        L_0x00cf:
            r9 = move-exception
            r1 = r3
        L_0x00d1:
            r3 = r0
            goto L_0x00d8
        L_0x00d3:
            r9 = move-exception
            r1 = r3
            goto L_0x00df
        L_0x00d6:
            r9 = move-exception
            r1 = r3
        L_0x00d8:
            r7 = r9
            r9 = r8
            r8 = r7
            goto L_0x0107
        L_0x00dc:
            r9 = move-exception
            r0 = r3
            r1 = r0
        L_0x00df:
            r7 = r9
            r9 = r8
            r8 = r7
            goto L_0x00eb
        L_0x00e3:
            r8 = move-exception
            r9 = r3
            r1 = r9
            goto L_0x0107
        L_0x00e7:
            r8 = move-exception
            r9 = r3
            r0 = r9
        L_0x00ea:
            r1 = r0
        L_0x00eb:
            java.lang.String r10 = "MediaArtworkProcessor"
            java.lang.String r4 = "error while processing artwork"
            android.util.Log.e(r10, r4, r8)     // Catch:{ all -> 0x0105 }
            if (r0 == 0) goto L_0x00f7
            r0.destroy()
        L_0x00f7:
            if (r1 == 0) goto L_0x00fc
            r1.destroy()
        L_0x00fc:
            r2.destroy()
            if (r9 == 0) goto L_0x0104
            r9.recycle()
        L_0x0104:
            return r3
        L_0x0105:
            r8 = move-exception
            r3 = r0
        L_0x0107:
            if (r3 == 0) goto L_0x010c
            r3.destroy()
        L_0x010c:
            if (r1 == 0) goto L_0x0111
            r1.destroy()
        L_0x0111:
            r2.destroy()
            if (r9 == 0) goto L_0x0119
            r9.recycle()
        L_0x0119:
            throw r8
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
