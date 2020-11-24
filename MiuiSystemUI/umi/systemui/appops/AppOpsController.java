package com.android.systemui.appops;

public interface AppOpsController {

    public interface Callback {
        void onActiveStateChanged(int i, int i2, String str, boolean z);
    }

    void addCallback(int[] iArr, Callback callback);
}
