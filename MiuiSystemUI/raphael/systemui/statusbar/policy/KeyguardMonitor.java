package com.android.systemui.statusbar.policy;

public interface KeyguardMonitor extends CallbackController<Callback> {

    public interface Callback {
        void onKeyguardShowingChanged();
    }

    boolean canSkipBouncer();

    long getKeyguardFadingAwayDelay();

    long getKeyguardFadingAwayDuration();

    boolean isKeyguardFadingAway();

    boolean isKeyguardGoingAway();

    boolean isSecure();

    boolean isShowing();

    boolean needSkipVolumeDialog();
}
