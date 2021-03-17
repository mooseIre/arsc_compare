package com.android.keyguard.wallpaper;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.util.Iterator;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardWallpaperControllerImpl.kt */
public final class MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ MiuiKeyguardWallpaperControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1(MiuiKeyguardWallpaperControllerImpl miuiKeyguardWallpaperControllerImpl) {
        this.this$0 = miuiKeyguardWallpaperControllerImpl;
    }

    public void onReceive(@Nullable Context context, @Nullable Intent intent) {
        if (intent != null && Intrinsics.areEqual(intent.getAction(), "miui.intent.action.LOCK_WALLPAPER_CHANGED") && Intrinsics.areEqual("com.miui.miwallpaper", intent.getSender())) {
            checkForSuperWallpaper();
            this.this$0.mWallpaperJsonString = intent.getStringExtra(MiuiKeyguardWallpaperControllerImpl.KEY_WALLPAPER_INFO);
            this.this$0.mIsWallpaperColorLight = intent.getBooleanExtra(MiuiKeyguardWallpaperControllerImpl.KEY_IS_WALLPAPER_COLOR_LIGHT, false);
            this.this$0.mWallpaperBlurColor = intent.getIntExtra(MiuiKeyguardWallpaperControllerImpl.KEY_WALLPAPER_BLUR_COLOR, -1);
            String str = this.this$0.TAG;
            Log.d(str, "onWallpaperChange ColorLight(!darkStyle):" + this.this$0.mIsWallpaperColorLight + " mWallpaperBlurColor:" + this.this$0.mWallpaperBlurColor + " mWallpaperInfo:" + this.this$0.mWallpaperJsonString);
            Iterator it = this.this$0.mWallpaperChangeCallbacks.iterator();
            while (it.hasNext()) {
                ((IMiuiKeyguardWallpaperController.IWallpaperChangeCallback) it.next()).onWallpaperChange(this.this$0.mIsWallpaperColorLight);
            }
        }
    }

    private final void checkForSuperWallpaper() {
        Object obj = Dependency.get(UserSwitcherController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(UserSwitcherController::class.java)");
        Object systemService = ((UserSwitcherController) obj).getContextForUser().getSystemService("wallpaper");
        if (systemService != null) {
            WallpaperInfo wallpaperInfo = ((WallpaperManager) systemService).getWallpaperInfo();
            if (wallpaperInfo != null && wallpaperInfo.getServiceInfo() != null) {
                try {
                    this.this$0.mIsSuperWallpaper = wallpaperInfo.getServiceInfo().metaData.getBoolean("is_super_wallpaper");
                } catch (Exception e) {
                    String str = this.this$0.TAG;
                    Log.e(str, "isSuperWallpaper wallpaperInfo.getServiceInfo() fail:" + e.getCause());
                }
            }
        } else {
            throw new TypeCastException("null cannot be cast to non-null type android.app.WallpaperManager");
        }
    }
}
