package com.android.keyguard;

import android.graphics.ImageDecoder;

/* renamed from: com.android.keyguard.-$$Lambda$KeyguardCameraView$8$wT9TMxtptpjuOCqjBWpG9gwqjpw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KeyguardCameraView$8$wT9TMxtptpjuOCqjBWpG9gwqjpw implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$KeyguardCameraView$8$wT9TMxtptpjuOCqjBWpG9gwqjpw INSTANCE = new $$Lambda$KeyguardCameraView$8$wT9TMxtptpjuOCqjBWpG9gwqjpw();

    private /* synthetic */ $$Lambda$KeyguardCameraView$8$wT9TMxtptpjuOCqjBWpG9gwqjpw() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setAllocator(1);
    }
}
