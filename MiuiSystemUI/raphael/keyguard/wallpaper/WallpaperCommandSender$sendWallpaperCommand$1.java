package com.android.keyguard.wallpaper;

import android.app.WallpaperManager;
import android.os.Bundle;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: WallpaperCommandSender.kt */
final class WallpaperCommandSender$sendWallpaperCommand$1 implements Runnable {
    final /* synthetic */ String $action;
    final /* synthetic */ Bundle $bundle;
    final /* synthetic */ WallpaperCommandSender this$0;

    WallpaperCommandSender$sendWallpaperCommand$1(WallpaperCommandSender wallpaperCommandSender, String str, Bundle bundle) {
        this.this$0 = wallpaperCommandSender;
        this.$action = str;
        this.$bundle = bundle;
    }

    public final void run() {
        WallpaperManager access$getMWallpaperManager$p = this.this$0.mWallpaperManager;
        NotificationShadeWindowView access$getMNotificationShadeWindowView$p = this.this$0.mNotificationShadeWindowView;
        if (access$getMNotificationShadeWindowView$p != null) {
            access$getMWallpaperManager$p.sendWallpaperCommand(access$getMNotificationShadeWindowView$p.getWindowToken(), this.$action, 0, 0, 0, this.$bundle);
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
