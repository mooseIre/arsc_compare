package com.android.systemui.biometrics;

public interface DialogViewCallback {
    void onErrorShown();

    void onNegativePressed();

    void onPositivePressed();

    void onTryAgainPressed();

    void onUserCanceled();
}
