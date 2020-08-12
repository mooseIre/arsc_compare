package com.android.systemui.plugins;

import android.net.Uri;
import android.view.MotionEvent;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.io.PrintWriter;

@ProvidesInterface(version = 1)
public interface FalsingManager {
    public static final int VERSION = 1;

    void dump(PrintWriter printWriter);

    boolean isClassiferEnabled();

    boolean isFalseTouch();

    boolean isReportingEnabled();

    boolean isUnlockingDisabled();

    void onAffordanceSwipingAborted();

    void onAffordanceSwipingStarted(boolean z);

    void onBouncerHidden();

    void onBouncerShown();

    void onCameraHintStarted();

    void onCameraOn();

    void onExpansionFromPulseStopped();

    void onLeftAffordanceHintStarted();

    void onLeftAffordanceOn();

    void onNotificationActive();

    void onNotificationDismissed();

    void onNotificationDoubleTap(boolean z, float f, float f2);

    void onNotificatonStartDismissing();

    void onNotificatonStartDraggingDown();

    void onNotificatonStopDismissing();

    void onNotificatonStopDraggingDown();

    void onQsDown();

    void onScreenOff();

    void onScreenOnFromTouch();

    void onScreenTurningOn();

    void onStartExpandingFromPulse();

    void onSucccessfulUnlock();

    void onTouchEvent(MotionEvent motionEvent, int i, int i2);

    void onTrackingStarted(boolean z);

    void onTrackingStopped();

    void onUnlockHintStarted();

    Uri reportRejectedTouch();

    void setNotificationExpanded();

    void setQsExpanded(boolean z);

    void setShowingAod(boolean z);

    boolean shouldEnforceBouncer();
}
