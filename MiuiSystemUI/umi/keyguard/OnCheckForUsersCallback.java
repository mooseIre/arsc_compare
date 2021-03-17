package com.android.keyguard;

public interface OnCheckForUsersCallback {
    void onChecked(boolean z, int i, int i2);

    default void onEarlyMatched() {
    }
}
