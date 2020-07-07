package com.android.keyguard;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.util.Log;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.widget.LockPatternUtils;

public class KeyguardSecurityModel {
    private final Context mContext;
    private final boolean mIsPukScreenAvailable = this.mContext.getResources().getBoolean(17891458);
    private LockPatternUtils mLockPatternUtils;

    public enum SecurityMode {
        Invalid,
        None,
        Pattern,
        Password,
        PIN,
        SimPin,
        SimPuk
    }

    public KeyguardSecurityModel(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    /* access modifiers changed from: package-private */
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    public SecurityMode getSecurityMode(int i) {
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        if (this.mIsPukScreenAvailable && SubscriptionManager.isValidSubscriptionId(instance.getNextSubIdForState(IccCardConstants.State.PUK_REQUIRED))) {
            return SecurityMode.SimPuk;
        }
        if (SubscriptionManager.isValidSubscriptionId(instance.getNextSubIdForState(IccCardConstants.State.PIN_REQUIRED))) {
            return SecurityMode.SimPin;
        }
        int activePasswordQuality = this.mLockPatternUtils.getActivePasswordQuality(i);
        Log.v("KeyguardSecurityModel", "getSecurityMode security=" + activePasswordQuality);
        if (activePasswordQuality == 0) {
            return SecurityMode.None;
        }
        if (activePasswordQuality == 65536) {
            return SecurityMode.Pattern;
        }
        if (activePasswordQuality == 131072 || activePasswordQuality == 196608) {
            return SecurityMode.PIN;
        }
        if (activePasswordQuality == 262144 || activePasswordQuality == 327680 || activePasswordQuality == 393216 || activePasswordQuality == 524288) {
            return SecurityMode.Password;
        }
        throw new IllegalStateException("Unknown security quality:" + activePasswordQuality);
    }
}
