package com.android.systemui.miui.statusbar.phone;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

public interface IMiuiStatusBarPrompt {
    boolean blockClickAction();

    Context getContext();

    void handleClickAction();

    void hideReturnToInCallScreenButton();

    void hideReturnToRecorderView();

    void hideSafePayStatusBar();

    void hideSosStatusBar();

    void makeReturnToInCallScreenButtonGone();

    void makeReturnToInCallScreenButtonVisible();

    void showReturnToDriveMode(boolean z);

    void showReturnToDriveModeView(boolean z, boolean z2);

    void showReturnToInCall(boolean z);

    void showReturnToInCallScreenButton(String str, long j);

    void showReturnToMulti(boolean z);

    void showReturnToRecorderView(String str, boolean z, long j);

    void showReturnToRecorderView(boolean z);

    void showReturnToSafeBar(boolean z);

    void showReturnToSosBar(boolean z);

    void showSafePayStatusBar(int i, Bundle bundle);

    void showSosStatusBar();

    void updateSosImageDark(boolean z, Rect rect, float f);

    void updateStateViews(String str);

    void updateTouchArea(boolean z, int i) {
    }
}
