package com.android.systemui.statusbar.policy;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

/* compiled from: DriveModeControllerImpl.kt */
public final class DriveModeControllerImpl$observe$1 extends ContentObserver {
    final /* synthetic */ DriveModeControllerImpl this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    DriveModeControllerImpl$observe$1(DriveModeControllerImpl driveModeControllerImpl, Handler handler) {
        super(handler);
        this.this$0 = driveModeControllerImpl;
    }

    public void onChange(boolean z) {
        Log.d("DriveModeController", "Drive mode change detected.");
        this.this$0.mUIExecutor.execute(new DriveModeControllerImpl$observe$1$onChange$1(this));
    }
}
