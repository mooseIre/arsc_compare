package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import com.android.systemui.keyguard.KeyguardViewMediator;

public class MiuiSmartCoverHelper {
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mHideLockForLid;
    private final BroadcastReceiver mSmartCoverReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("miui.intent.action.SMART_COVER".equals(intent.getAction())) {
                boolean z = !intent.getBooleanExtra("is_smart_cover_open", false);
                if (MiuiSmartCoverHelper.this.mViewMediator.isSecure()) {
                    boolean unused = MiuiSmartCoverHelper.this.mHideLockForLid = false;
                } else if (z) {
                    boolean unused2 = MiuiSmartCoverHelper.this.mHideLockForLid = true;
                } else {
                    if (MiuiSmartCoverHelper.this.mViewMediator.isShowingAndNotOccluded() && !MiuiSmartCoverHelper.this.mViewMediator.isSimLockedOrMissing()) {
                        MiuiSmartCoverHelper.this.mViewMediator.keyguardDone();
                    }
                    boolean unused3 = MiuiSmartCoverHelper.this.mHideLockForLid = false;
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public KeyguardViewMediator mViewMediator;

    public MiuiSmartCoverHelper(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mViewMediator = keyguardViewMediator;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.SMART_COVER");
        this.mContext.registerReceiverAsUser(this.mSmartCoverReceiver, UserHandle.ALL, intentFilter, "android.permission.DEVICE_POWER", (Handler) null);
    }

    public boolean isHideLockForLid() {
        return this.mHideLockForLid;
    }
}
