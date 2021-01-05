package com.android.systemui.statusbar.policy;

public interface KeyguardStateController extends CallbackController<Callback> {

    public interface Callback {
        void onKeyguardFadingAwayChanged() {
        }

        void onKeyguardShowingChanged() {
        }

        void onUnlockedChanged() {
        }
    }

    long calculateGoingToFullShadeDelay();

    boolean canDismissLockScreen();

    long getKeyguardFadingAwayDelay();

    long getKeyguardFadingAwayDuration();

    boolean isBypassFadingAnimation() {
        return false;
    }

    boolean isFaceAuthEnabled() {
        return false;
    }

    boolean isKeyguardFadingAway();

    boolean isKeyguardGoingAway();

    boolean isLaunchTransitionFadingAway();

    boolean isMethodSecure();

    boolean isOccluded();

    boolean isShowing();

    void notifyKeyguardDoneFading() {
    }

    void notifyKeyguardFadingAway(long j, long j2, boolean z) {
    }

    void notifyKeyguardGoingAway(boolean z) {
    }

    void notifyKeyguardState(boolean z, boolean z2) {
    }

    void setLaunchTransitionFadingAway(boolean z) {
    }

    boolean isUnlocked() {
        return !isShowing() || canDismissLockScreen();
    }

    long getShortenedFadingAwayDuration() {
        if (isBypassFadingAnimation()) {
            return getKeyguardFadingAwayDuration();
        }
        return getKeyguardFadingAwayDuration() / 2;
    }
}
