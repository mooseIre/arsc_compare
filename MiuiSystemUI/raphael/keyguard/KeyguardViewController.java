package com.android.keyguard;

import android.os.Bundle;
import android.view.ViewRootImpl;

public interface KeyguardViewController {
    boolean bouncerIsOrWillBeShowing();

    void dismissAndCollapse();

    ViewRootImpl getViewRootImpl();

    void hide(long j, long j2);

    boolean isBouncerShowing();

    boolean isShowing();

    void keyguardGoingAway();

    void notifyKeyguardAuthenticated(boolean z);

    default void onFinishedGoingToSleep() {
    }

    default void onScreenTurnedOn() {
    }

    default void onScreenTurningOn() {
    }

    default void onStartedGoingToSleep() {
    }

    default void onStartedWakingUp() {
    }

    void reset(boolean z);

    void setKeyguardGoingAwayState(boolean z);

    void setNeedsInput(boolean z);

    void setOccluded(boolean z, boolean z2);

    void show(Bundle bundle);

    void showBouncer(boolean z);

    void startPreHideAnimation(Runnable runnable);
}
