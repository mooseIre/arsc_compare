package com.android.systemui.miui.statusbar.notification;

import android.graphics.ImageDecoder;

/* renamed from: com.android.systemui.miui.statusbar.notification.-$$Lambda$NotificationUtil$YfnAuhF3RlXedltGO2nV-YLyxuE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationUtil$YfnAuhF3RlXedltGO2nVYLyxuE implements ImageDecoder.OnHeaderDecodedListener {
    public static final /* synthetic */ $$Lambda$NotificationUtil$YfnAuhF3RlXedltGO2nVYLyxuE INSTANCE = new $$Lambda$NotificationUtil$YfnAuhF3RlXedltGO2nVYLyxuE();

    private /* synthetic */ $$Lambda$NotificationUtil$YfnAuhF3RlXedltGO2nVYLyxuE() {
    }

    public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
        imageDecoder.setMutableRequired(true);
    }
}
