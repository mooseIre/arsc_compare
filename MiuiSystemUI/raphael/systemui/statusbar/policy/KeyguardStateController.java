package com.android.systemui.statusbar.policy;

public interface KeyguardStateController extends CallbackController<Callback> {

    public interface Callback {
        default void onKeyguardFadingAwayChanged() {
        }

        default void onKeyguardShowingChanged() {
        }

        default void onUnlockedChanged() {
        }
    }

    long calculateGoingToFullShadeDelay();

    boolean canDismissLockScreen();

    long getKeyguardFadingAwayDelay();

    long getKeyguardFadingAwayDuration();

    default boolean isBypassFadingAnimation() {
        return false;
    }

    default boolean isFaceAuthEnabled() {
        return false;
    }

    boolean isKeyguardFadingAway();

    boolean isKeyguardGoingAway();

    boolean isLaunchTransitionFadingAway();

    boolean isMethodSecure();

    boolean isOccluded();

    boolean isShowing();

    default void notifyKeyguardDoneFading() {
    }

    default void notifyKeyguardFadingAway(long j, long j2, boolean z) {
    }

    default void notifyKeyguardGoingAway(boolean z) {
    }

    default void notifyKeyguardState(boolean z, boolean z2) {
    }

    default void setLaunchTransitionFadingAway(boolean z) {
    }

    default boolean isUnlocked() {
        return !isShowing() || canDismissLockScreen();
    }

    default long getShortenedFadingAwayDuration() {
        if (isBypassFadingAnimation()) {
            return getKeyguardFadingAwayDuration();
        }
        return getKeyguardFadingAwayDuration() / 2;
    }
}
