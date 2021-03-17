package com.android.keyguard.wallpaper;

import android.os.RemoteException;
import android.util.Log;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.miui.miwallpaper.IMiuiKeyguardWallpaperService;

/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient$mWakefulnessLifecycle$1 implements WakefulnessLifecycle.Observer {
    final /* synthetic */ MiuiWallpaperClient this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiWallpaperClient$mWakefulnessLifecycle$1(MiuiWallpaperClient miuiWallpaperClient) {
        this.this$0 = miuiWallpaperClient;
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedWakingUp() {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.this$0.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onStartedWakingUp();
            } else {
                this.this$0.bindService();
            }
        } catch (RemoteException e) {
            String tag = this.this$0.getTAG();
            Log.e(tag, "onStartedWakingUp: " + e.getMessage());
        }
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedGoingToSleep() {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.this$0.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onStartedGoingToSleep();
            } else {
                this.this$0.bindService();
            }
        } catch (RemoteException e) {
            String tag = this.this$0.getTAG();
            Log.e(tag, "onStartedGoingToSleep: " + e.getMessage());
        }
    }
}
