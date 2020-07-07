package com.android.keyguard;

import android.content.Context;
import com.android.internal.widget.LockPatternUtils;

public class AbstractStrongAuthTracker extends LockPatternUtils.StrongAuthTracker {
    public AbstractStrongAuthTracker(Context context) {
        super(context);
    }

    public boolean isFingerprintAllowedForUser(int i) {
        return isBiometricAllowedForUser(false, i);
    }
}
