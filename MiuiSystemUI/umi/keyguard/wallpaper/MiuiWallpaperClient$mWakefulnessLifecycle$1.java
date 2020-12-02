package com.android.keyguard.wallpaper;

import android.os.RemoteException;
import android.util.Log;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.miui.miwallpaper.IMiuiKeyguardWallpaperService;

/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient$mWakefulnessLifecycle$1 implements WakefulnessLifecycle.Observer {
    final /* synthetic */ MiuiWallpaperClient this$0;

    MiuiWallpaperClient$mWakefulnessLifecycle$1(MiuiWallpaperClient miuiWallpaperClient) {
        this.this$0 = miuiWallpaperClient;
    }

    public void onStartedWakingUp() {
        try {
            IMiuiKeyguardWallpaperService access$getMWallpaperService$p = this.this$0.mWallpaperService;
            if (access$getMWallpaperService$p != null) {
                access$getMWallpaperService$p.onStartedWakingUp();
            } else {
                this.this$0.bindService();
            }
        } catch (RemoteException e) {
            String tag = this.this$0.getTAG();
            Log.e(tag, "onStartedWakingUp: " + e.getMessage());
        }
    }

    public void onStartedGoingToSleep() {
        try {
            IMiuiKeyguardWallpaperService access$getMWallpaperService$p = this.this$0.mWallpaperService;
            if (access$getMWallpaperService$p != null) {
                access$getMWallpaperService$p.onStartedGoingToSleep();
            } else {
                this.this$0.bindService();
            }
        } catch (RemoteException e) {
            String tag = this.this$0.getTAG();
            Log.e(tag, "onStartedGoingToSleep: " + e.getMessage());
        }
    }
}
