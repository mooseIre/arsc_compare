package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.Handler;
import android.view.accessibility.AccessibilityManager;

public class AccessibilityManagerWrapper implements CallbackController<AccessibilityManager.AccessibilityServicesStateChangeListener> {
    private final AccessibilityManager mAccessibilityManager;

    public AccessibilityManagerWrapper(Context context) {
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
    }

    public void addCallback(AccessibilityManager.AccessibilityServicesStateChangeListener accessibilityServicesStateChangeListener) {
        this.mAccessibilityManager.addAccessibilityServicesStateChangeListener(accessibilityServicesStateChangeListener, (Handler) null);
    }

    public void removeCallback(AccessibilityManager.AccessibilityServicesStateChangeListener accessibilityServicesStateChangeListener) {
        this.mAccessibilityManager.removeAccessibilityServicesStateChangeListener(accessibilityServicesStateChangeListener);
    }

    public int getRecommendedTimeoutMillis(int i, int i2) {
        return this.mAccessibilityManager.getRecommendedTimeoutMillis(i, i2);
    }
}
