package com.android.systemui.vendor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserHandle;
import com.android.systemui.Dependency;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: HeadsetPolicy.kt */
public final class HeadsetPolicy {
    private final Context mContext;
    private final BroadcastReceiver mIntentReceiver;
    private final PowerManager mPowerManager;

    public HeadsetPolicy(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.mContext = context;
        Object systemService = context.getSystemService("power");
        if (systemService != null) {
            this.mPowerManager = (PowerManager) systemService;
            this.mIntentReceiver = new HeadsetPolicy$mIntentReceiver$1(this);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.os.PowerManager");
    }

    public final void start() {
        Object obj = Dependency.get(Dependency.BG_LOOPER);
        if (obj != null) {
            Handler handler = new Handler((Looper) obj);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.HEADSET_PLUG");
            this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, null, handler);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.os.Looper");
    }
}
