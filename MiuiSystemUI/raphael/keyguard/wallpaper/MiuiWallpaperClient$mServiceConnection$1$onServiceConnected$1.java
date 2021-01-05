package com.android.keyguard.wallpaper;

import android.util.Log;
import android.widget.RemoteViews;
import com.miui.miwallpaper.IMiuiKeyguardWallpaperCallback;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Job;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1 extends IMiuiKeyguardWallpaperCallback.Stub {
    final /* synthetic */ MiuiWallpaperClient$mServiceConnection$1 this$0;

    MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1(MiuiWallpaperClient$mServiceConnection$1 miuiWallpaperClient$mServiceConnection$1) {
        this.this$0 = miuiWallpaperClient$mServiceConnection$1;
    }

    public void onRemoteViewChange(@Nullable RemoteViews remoteViews, @Nullable RemoteViews remoteViews2) {
        String tag = this.this$0.this$0.getTAG();
        Log.d(tag, "onRemoteViewChange MainRemote:" + remoteViews + "  FullScreenRemote:" + remoteViews2);
        Job unused = BuildersKt__Builders_commonKt.launch$default(this.this$0.this$0.mUiScope, (CoroutineContext) null, (CoroutineStart) null, new MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1(remoteViews, remoteViews2, (Continuation) null), 3, (Object) null);
    }
}
