package com.android.systemui.media;

import android.graphics.ImageDecoder;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MediaDataManager.kt */
final class MediaDataManager$loadBitmapFromUri$1 implements ImageDecoder.OnHeaderDecodedListener {
    public static final MediaDataManager$loadBitmapFromUri$1 INSTANCE = new MediaDataManager$loadBitmapFromUri$1();

    MediaDataManager$loadBitmapFromUri$1() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        Intrinsics.checkExpressionValueIsNotNull(imageDecoder, "decoder");
        imageDecoder.setMutableRequired(true);
    }
}
