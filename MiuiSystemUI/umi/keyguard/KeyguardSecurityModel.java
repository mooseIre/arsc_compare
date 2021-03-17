package com.android.keyguard;

import android.content.Context;
import android.telephony.SubscriptionManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import java.util.function.Supplier;

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

    KeyguardSecurityModel(Context context) {
        this.mContext = context;
        this.mLockPatternUtils = new LockPatternUtils(context);
    }

    /* access modifiers changed from: package-private */
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
    }

    public SecurityMode getSecurityMode(int i) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if (this.mIsPukScreenAvailable && SubscriptionManager.isValidSubscriptionId(keyguardUpdateMonitor.getNextSubIdForState(3))) {
            return SecurityMode.SimPuk;
        }
        if (SubscriptionManager.isValidSubscriptionId(keyguardUpdateMonitor.getUnlockedSubIdForState(2))) {
            return SecurityMode.SimPin;
        }
        int intValue = ((Integer) DejankUtils.whitelistIpcs(new Supplier(i) {
            /* class com.android.keyguard.$$Lambda$KeyguardSecurityModel$wVA2_YbUv0Q_IYsNKsPxtYAIjp0 */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardSecurityModel.this.lambda$getSecurityMode$0$KeyguardSecurityModel(this.f$1);
            }
        })).intValue();
        if (intValue == 0) {
            return SecurityMode.None;
        }
        if (intValue == 65536) {
            return SecurityMode.Pattern;
        }
        if (intValue == 131072 || intValue == 196608) {
            return SecurityMode.PIN;
        }
        if (intValue == 262144 || intValue == 327680 || intValue == 393216 || intValue == 524288) {
            return SecurityMode.Password;
        }
        throw new IllegalStateException("Unknown security quality:" + intValue);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getSecurityMode$0 */
    public /* synthetic */ Integer lambda$getSecurityMode$0$KeyguardSecurityModel(int i) {
        return Integer.valueOf(this.mLockPatternUtils.getActivePasswordQuality(i));
    }
}
