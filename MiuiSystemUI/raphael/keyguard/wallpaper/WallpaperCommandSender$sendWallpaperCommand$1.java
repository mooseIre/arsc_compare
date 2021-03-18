package com.android.keyguard.wallpaper;

import android.app.WallpaperManager;
import android.os.Bundle;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: WallpaperCommandSender.kt */
public final class WallpaperCommandSender$sendWallpaperCommand$1 implements Runnable {
    final /* synthetic */ String $action;
    final /* synthetic */ Bundle $bundle;
    final /* synthetic */ WallpaperCommandSender this$0;

    WallpaperCommandSender$sendWallpaperCommand$1(WallpaperCommandSender wallpaperCommandSender, String str, Bundle bundle) {
        this.this$0 = wallpaperCommandSender;
        this.$action = str;
        this.$bundle = bundle;
    }

    public final void run() {
        WallpaperManager wallpaperManager = this.this$0.mWallpaperManager;
        NotificationShadeWindowView notificationShadeWindowView = this.this$0.mNotificationShadeWindowView;
        if (notificationShadeWindowView != null) {
            wallpaperManager.sendWallpaperCommand(notificationShadeWindowView.getWindowToken(), this.$action, 0, 0, 0, this.$bundle);
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
