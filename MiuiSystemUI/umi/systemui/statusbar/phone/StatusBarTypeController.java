package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.policy.CallbackController;

public interface StatusBarTypeController extends CallbackController<StatusBarTypeChangeListener> {

    public enum CutoutType {
        NONE,
        NOTCH,
        NARROW_NOTCH,
        DRIP,
        HOLE
    }

    public interface StatusBarTypeChangeListener {
        void onCutoutTypeChanged();
    }

    CutoutType getCutoutType();

    boolean hasCutout();
}
