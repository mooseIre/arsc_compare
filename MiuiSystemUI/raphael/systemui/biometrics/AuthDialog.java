package com.android.systemui.biometrics;

import android.os.Bundle;
import android.view.WindowManager;

public interface AuthDialog {
    void animateToCredentialUI();

    void dismissFromSystemServer();

    void dismissWithoutCallback(boolean z);

    String getOpPackageName();

    boolean isAllowDeviceCredentials();

    void onAuthenticationFailed(String str);

    void onAuthenticationSucceeded();

    void onError(String str);

    void onHelp(String str);

    void onSaveState(Bundle bundle);

    void show(WindowManager windowManager, Bundle bundle);
}
