package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import com.android.systemui.keyguard.KeyguardViewMediator;

public class MiuiSmartCoverHelper {
    private Context mContext;
    private boolean mHideLockForLid;
    private final BroadcastReceiver mSmartCoverReceiver = new BroadcastReceiver() {
        /* class com.android.keyguard.MiuiSmartCoverHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("miui.intent.action.SMART_COVER".equals(intent.getAction())) {
                boolean z = !intent.getBooleanExtra("is_smart_cover_open", false);
                if (MiuiSmartCoverHelper.this.mViewMediator.isSecure()) {
                    MiuiSmartCoverHelper.this.mHideLockForLid = false;
                } else if (z) {
                    MiuiSmartCoverHelper.this.mHideLockForLid = true;
                } else {
                    if (MiuiSmartCoverHelper.this.mViewMediator.isShowingAndNotOccluded() && !MiuiSmartCoverHelper.this.mViewMediator.isSimLockedOrMissing()) {
                        MiuiSmartCoverHelper.this.mViewMediator.keyguardDone();
                    }
                    MiuiSmartCoverHelper.this.mHideLockForLid = false;
                }
            }
        }
    };
    private KeyguardViewMediator mViewMediator;

    public MiuiSmartCoverHelper(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mViewMediator = keyguardViewMediator;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.SMART_COVER");
        this.mContext.registerReceiverAsUser(this.mSmartCoverReceiver, UserHandle.ALL, intentFilter, "android.permission.DEVICE_POWER", null);
    }

    public boolean isHideLockForLid() {
        return this.mHideLockForLid;
    }
}
