package com.android.keyguard.wallpaper;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.miui.miwallpaper.IMiuiKeyguardWallpaperService;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient$mServiceConnection$1 implements ServiceConnection {
    final /* synthetic */ MiuiWallpaperClient this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiWallpaperClient$mServiceConnection$1(MiuiWallpaperClient miuiWallpaperClient) {
        this.this$0 = miuiWallpaperClient;
    }

    public void onServiceConnected(@NotNull ComponentName componentName, @NotNull IBinder iBinder) {
        Intrinsics.checkParameterIsNotNull(componentName, "name");
        Intrinsics.checkParameterIsNotNull(iBinder, "service");
        Log.d(this.this$0.getTAG(), "on MiuiKeyguardWallpaperRemoteStateService connected");
        try {
            this.this$0.mWallpaperService = IMiuiKeyguardWallpaperService.Stub.asInterface(iBinder);
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.this$0.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.bindSystemUIProxy(new MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1(this));
            } else {
                Log.d(this.this$0.getTAG(), "mWallpaperService == null");
            }
            this.this$0.mBinding = true;
        } catch (RemoteException e) {
            String tag = this.this$0.getTAG();
            Log.e(tag, "onServiceConnected: " + e.getMessage());
        }
    }

    public void onServiceDisconnected(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "name");
        this.this$0.bindService();
        this.this$0.mWallpaperService = null;
        this.this$0.mBinding = false;
    }
}
