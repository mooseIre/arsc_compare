package com.android.systemui.statusbar.notification.policy;

import android.os.Handler;
import android.util.Log;
import kotlin.jvm.internal.Intrinsics;
import miui.process.ForegroundInfo;
import miui.process.IForegroundWindowListener;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager$mWindowListener$1 extends IForegroundWindowListener.Stub {
    final /* synthetic */ Handler $handler;
    final /* synthetic */ AppMiniWindowManager this$0;

    AppMiniWindowManager$mWindowListener$1(AppMiniWindowManager appMiniWindowManager, Handler handler) {
        this.this$0 = appMiniWindowManager;
        this.$handler = handler;
    }

    public void onForegroundWindowChanged(@NotNull ForegroundInfo foregroundInfo) {
        Intrinsics.checkParameterIsNotNull(foregroundInfo, "foregroundInfo");
        String str = foregroundInfo.mForegroundPackageName;
        Log.d("AppMiniWindowManager", "onForegroundWindowChanged: " + str);
        this.this$0.mTopWindowPackage = str;
        AppMiniWindowManager appMiniWindowManager = this.this$0;
        Intrinsics.checkExpressionValueIsNotNull(str, "foregroundPackage");
        appMiniWindowManager.fireOneshotListenersForPackages(str);
        this.$handler.post(new AppMiniWindowManager$mWindowListener$1$onForegroundWindowChanged$1(this));
    }
}
