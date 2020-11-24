package com.android.keyguard.wallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardWallpaperControllerImpl.kt */
public final class MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ MiuiKeyguardWallpaperControllerImpl this$0;

    MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1(MiuiKeyguardWallpaperControllerImpl miuiKeyguardWallpaperControllerImpl) {
        this.this$0 = miuiKeyguardWallpaperControllerImpl;
    }

    public void onReceive(@Nullable Context context, @Nullable Intent intent) {
        if (intent != null && Intrinsics.areEqual((Object) intent.getAction(), (Object) "miui.intent.action.LOCK_WALLPAPER_CHANGED") && Intrinsics.areEqual((Object) "com.miui.miwallpaper", (Object) intent.getSender())) {
            this.this$0.mWallpaperJsonString = intent.getStringExtra(MiuiKeyguardWallpaperControllerImpl.KEY_WALLPAPER_INFO);
            this.this$0.mIsWallpaperColorLight = intent.getBooleanExtra(MiuiKeyguardWallpaperControllerImpl.KEY_IS_WALLPAPER_COLOR_LIGHT, false);
            this.this$0.mWallpaperBlurColor = intent.getIntExtra(MiuiKeyguardWallpaperControllerImpl.KEY_WALLPAPER_BLUR_COLOR, -1);
            String access$getTAG$p = this.this$0.TAG;
            Log.d(access$getTAG$p, "onWallpaperChange ColorLight(!darkStyle):" + this.this$0.mIsWallpaperColorLight + " mWallpaperBlurColor:" + this.this$0.mWallpaperBlurColor + " mWallpaperInfo:" + this.this$0.mWallpaperJsonString);
            Iterator it = this.this$0.mWallpaperChangeCallbacks.iterator();
            while (it.hasNext()) {
                ((IMiuiKeyguardWallpaperController.IWallpaperChangeCallback) it.next()).onWallpaperChange(this.this$0.mIsWallpaperColorLight);
            }
        }
    }
}
