package com.android.systemui.plugins.statusbar;

import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = StateListener.class)
@ProvidesInterface(version = 1)
public interface StatusBarStateController {
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    public interface StateListener {
        public static final int VERSION = 1;

        void onDozeAmountChanged(float f, float f2) {
        }

        void onDozingChanged(boolean z) {
        }

        void onStateChanged(int i) {
        }

        void onStatePostChange() {
        }

        void onStatePreChange(int i, int i2) {
        }
    }

    void addCallback(StateListener stateListener);

    float getDozeAmount();

    int getState();

    boolean isDozing();

    void removeCallback(StateListener stateListener);
}
