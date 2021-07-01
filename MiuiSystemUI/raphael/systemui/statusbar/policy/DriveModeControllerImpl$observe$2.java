package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DriveModeControllerImpl.kt */
public final class DriveModeControllerImpl$observe$2 extends BroadcastReceiver {
    final /* synthetic */ DriveModeControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    DriveModeControllerImpl$observe$2(DriveModeControllerImpl driveModeControllerImpl) {
        this.this$0 = driveModeControllerImpl;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        String action = intent.getAction();
        if (intent.getData() != null) {
            if (Intrinsics.areEqual("android.intent.action.PACKAGE_ADDED", action)) {
                if (!this.this$0.mIsDriveModeAvailable) {
                    Uri data = intent.getData();
                    if (data == null) {
                        Intrinsics.throwNpe();
                        throw null;
                    } else if (Intrinsics.areEqual("com.xiaomi.drivemode", data.getSchemeSpecificPart())) {
                        this.this$0.mIsDriveModeAvailable = true;
                    }
                }
            } else if (Intrinsics.areEqual("android.intent.action.PACKAGE_REMOVED", action) && this.this$0.mIsDriveModeAvailable) {
                Uri data2 = intent.getData();
                if (data2 == null) {
                    Intrinsics.throwNpe();
                    throw null;
                } else if (Intrinsics.areEqual(data2.getSchemeSpecificPart(), "com.xiaomi.drivemode")) {
                    this.this$0.mIsDriveModeAvailable = false;
                    this.this$0.leaveDriveMode();
                }
            }
        }
    }
}
