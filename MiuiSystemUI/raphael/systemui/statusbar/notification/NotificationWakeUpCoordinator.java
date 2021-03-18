package com.android.systemui.statusbar.notification;

import android.animation.ObjectAnimator;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.PanelExpansionListener;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationWakeUpCoordinator.kt */
public final class NotificationWakeUpCoordinator implements OnHeadsUpChangedListener, StatusBarStateController.StateListener, PanelExpansionListener {
    private final KeyguardBypassController bypassController;
    private boolean collapsedEnoughToHide;
    private final DozeParameters dozeParameters;
    private boolean fullyAwake;
    private float mDozeAmount;
    private final Set<NotificationEntry> mEntrySetToClearWhenFinished = new LinkedHashSet();
    private final HeadsUpManager mHeadsUpManager;
    private float mLinearDozeAmount;
    private float mLinearVisibilityAmount;
    private final NotificationWakeUpCoordinator$mNotificationVisibility$1 mNotificationVisibility = new NotificationWakeUpCoordinator$mNotificationVisibility$1("notificationVisibility");
    private float mNotificationVisibleAmount;
    private boolean mNotificationsVisible;
    private boolean mNotificationsVisibleForExpansion;
    private NotificationStackScrollLayout mStackScroller;
    private float mVisibilityAmount;
    private ObjectAnimator mVisibilityAnimator;
    private Interpolator mVisibilityInterpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
    private boolean notificationsFullyHidden;
    private boolean pulseExpanding;
    private boolean pulsing;
    private int state = 1;
    private final StatusBarStateController statusBarStateController;
    private final ArrayList<WakeUpListener> wakeUpListeners = new ArrayList<>();
    private boolean wakingUp;
    private boolean willWakeUp;

    /* compiled from: NotificationWakeUpCoordinator.kt */
    public interface WakeUpListener {
        default void onFullyHiddenChanged(boolean z) {
        }

        default void onPulseExpansionChanged(boolean z) {
        }
    }

    public final void setIconAreaController(@NotNull NotificationIconAreaController notificationIconAreaController) {
        Intrinsics.checkParameterIsNotNull(notificationIconAreaController, "<set-?>");
    }

    public NotificationWakeUpCoordinator(@NotNull HeadsUpManager headsUpManager, @NotNull StatusBarStateController statusBarStateController2, @NotNull KeyguardBypassController keyguardBypassController, @NotNull DozeParameters dozeParameters2) {
        Intrinsics.checkParameterIsNotNull(headsUpManager, "mHeadsUpManager");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(dozeParameters2, "dozeParameters");
        this.mHeadsUpManager = headsUpManager;
        this.statusBarStateController = statusBarStateController2;
        this.bypassController = keyguardBypassController;
        this.dozeParameters = dozeParameters2;
        this.mHeadsUpManager.addListener(this);
        this.statusBarStateController.addCallback(this);
        addListener(new WakeUpListener(this) {
            /* class com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.AnonymousClass1 */
            final /* synthetic */ NotificationWakeUpCoordinator this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
            public void onFullyHiddenChanged(boolean z) {
                if (z && this.this$0.mNotificationsVisibleForExpansion) {
                    this.this$0.setNotificationsVisibleForExpansion(false, false, false);
                }
            }
        });
    }

    public final void setFullyAwake(boolean z) {
        this.fullyAwake = z;
    }

    public final void setWakingUp(boolean z) {
        this.wakingUp = z;
        setWillWakeUp(false);
        if (z) {
            if (this.mNotificationsVisible && !this.mNotificationsVisibleForExpansion && !this.bypassController.getBypassEnabled()) {
                NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
                if (notificationStackScrollLayout != null) {
                    notificationStackScrollLayout.wakeUpFromPulse();
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
                    throw null;
                }
            }
            if (this.bypassController.getBypassEnabled() && !this.mNotificationsVisible) {
                updateNotificationVisibility(shouldAnimateVisibility(), false);
            }
        }
    }

    public final void setWillWakeUp(boolean z) {
        if (!z || this.mDozeAmount != 0.0f) {
            this.willWakeUp = z;
        }
    }

    public final void setPulsing(boolean z) {
        this.pulsing = z;
        if (z) {
            updateNotificationVisibility(shouldAnimateVisibility(), false);
        }
    }

    public final boolean getNotificationsFullyHidden() {
        return this.notificationsFullyHidden;
    }

    private final void setNotificationsFullyHidden(boolean z) {
        if (this.notificationsFullyHidden != z) {
            this.notificationsFullyHidden = z;
            Iterator<WakeUpListener> it = this.wakeUpListeners.iterator();
            while (it.hasNext()) {
                it.next().onFullyHiddenChanged(z);
            }
        }
    }

    public final boolean getCanShowPulsingHuns() {
        boolean z = this.pulsing;
        if (!this.bypassController.getBypassEnabled()) {
            return z;
        }
        boolean z2 = z || ((this.wakingUp || this.willWakeUp || this.fullyAwake) && this.statusBarStateController.getState() == 1);
        if (this.collapsedEnoughToHide) {
            return false;
        }
        return z2;
    }

    public final void setStackScroller(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "stackScroller");
        this.mStackScroller = notificationStackScrollLayout;
        this.pulseExpanding = notificationStackScrollLayout.isPulseExpanding();
        notificationStackScrollLayout.setOnPulseHeightChangedListener(new NotificationWakeUpCoordinator$setStackScroller$1(this));
    }

    public final boolean isPulseExpanding() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            return notificationStackScrollLayout.isPulseExpanding();
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    public final void setNotificationsVisibleForExpansion(boolean z, boolean z2, boolean z3) {
        this.mNotificationsVisibleForExpansion = z;
        updateNotificationVisibility(z2, z3);
        if (!z && this.mNotificationsVisible) {
            this.mHeadsUpManager.releaseAllImmediately();
        }
    }

    public final void addListener(@NotNull WakeUpListener wakeUpListener) {
        Intrinsics.checkParameterIsNotNull(wakeUpListener, "listener");
        this.wakeUpListeners.add(wakeUpListener);
    }

    public final void removeListener(@NotNull WakeUpListener wakeUpListener) {
        Intrinsics.checkParameterIsNotNull(wakeUpListener, "listener");
        this.wakeUpListeners.remove(wakeUpListener);
    }

    private final void updateNotificationVisibility(boolean z, boolean z2) {
        boolean z3 = false;
        if ((this.mNotificationsVisibleForExpansion || this.mHeadsUpManager.hasNotifications()) && getCanShowPulsingHuns()) {
            z3 = true;
        }
        if (z3 || !this.mNotificationsVisible || ((!this.wakingUp && !this.willWakeUp) || this.mDozeAmount == 0.0f)) {
            setNotificationsVisible(z3, z, z2);
        }
    }

    private final void setNotificationsVisible(boolean z, boolean z2, boolean z3) {
        if (this.mNotificationsVisible != z) {
            this.mNotificationsVisible = z;
            ObjectAnimator objectAnimator = this.mVisibilityAnimator;
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            if (z2) {
                notifyAnimationStart(z);
                startVisibilityAnimation(z3);
                return;
            }
            setVisibilityAmount(z ? 1.0f : 0.0f);
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozeAmountChanged(float f, float f2) {
        if (!updateDozeAmountIfBypass()) {
            if (!(f == 1.0f || f == 0.0f)) {
                float f3 = this.mLinearDozeAmount;
                if (f3 == 0.0f || f3 == 1.0f) {
                    notifyAnimationStart(this.mLinearDozeAmount == 1.0f);
                }
            }
            setDozeAmount(f, f2);
        }
    }

    public final void setDozeAmount(float f, float f2) {
        boolean z = f != this.mLinearDozeAmount;
        this.mLinearDozeAmount = f;
        this.mDozeAmount = f2;
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.setDozeAmount(f2);
            updateHideAmount();
            if (z && f == 0.0f) {
                setNotificationsVisible(false, false, false);
                setNotificationsVisibleForExpansion(false, false, false);
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        updateDozeAmountIfBypass();
        if (this.bypassController.getBypassEnabled() && i == 1 && this.state == 2 && (!this.statusBarStateController.isDozing() || shouldAnimateVisibility())) {
            setNotificationsVisible(true, false, false);
            setNotificationsVisible(false, true, false);
        }
        this.state = i;
    }

    @Override // com.android.systemui.statusbar.phone.PanelExpansionListener
    public void onPanelExpansionChanged(float f, boolean z) {
        boolean z2 = f <= 0.9f;
        if (z2 != this.collapsedEnoughToHide) {
            boolean canShowPulsingHuns = getCanShowPulsingHuns();
            this.collapsedEnoughToHide = z2;
            if (canShowPulsingHuns && !getCanShowPulsingHuns()) {
                updateNotificationVisibility(true, true);
                this.mHeadsUpManager.releaseAllImmediately();
            }
        }
    }

    private final boolean updateDozeAmountIfBypass() {
        if (!this.bypassController.getBypassEnabled()) {
            return false;
        }
        float f = 1.0f;
        if (this.statusBarStateController.getState() == 0 || this.statusBarStateController.getState() == 2) {
            f = 0.0f;
        }
        setDozeAmount(f, f);
        return true;
    }

    private final void startVisibilityAnimation(boolean z) {
        Interpolator interpolator;
        float f = this.mNotificationVisibleAmount;
        float f2 = 0.0f;
        if (f == 0.0f || f == 1.0f) {
            if (this.mNotificationsVisible) {
                interpolator = Interpolators.TOUCH_RESPONSE;
            } else {
                interpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
            }
            this.mVisibilityInterpolator = interpolator;
        }
        if (this.mNotificationsVisible) {
            f2 = 1.0f;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, this.mNotificationVisibility, f2);
        ofFloat.setInterpolator(Interpolators.LINEAR);
        long j = (long) 500;
        if (z) {
            j = (long) (((float) j) / 1.5f);
        }
        ofFloat.setDuration(j);
        ofFloat.start();
        this.mVisibilityAnimator = ofFloat;
    }

    /* access modifiers changed from: private */
    public final void setVisibilityAmount(float f) {
        this.mLinearVisibilityAmount = f;
        this.mVisibilityAmount = this.mVisibilityInterpolator.getInterpolation(f);
        handleAnimationFinished();
        updateHideAmount();
    }

    private final void handleAnimationFinished() {
        if (this.mLinearDozeAmount == 0.0f || this.mLinearVisibilityAmount == 0.0f) {
            Iterator<T> it = this.mEntrySetToClearWhenFinished.iterator();
            while (it.hasNext()) {
                it.next().setHeadsUpAnimatingAway(false);
            }
            this.mEntrySetToClearWhenFinished.clear();
        }
    }

    public final float getWakeUpHeight() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            return notificationStackScrollLayout.getWakeUpHeight();
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    private final void updateHideAmount() {
        float min = Math.min(1.0f - this.mLinearVisibilityAmount, this.mLinearDozeAmount);
        float min2 = Math.min(1.0f - this.mVisibilityAmount, this.mDozeAmount);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.setHideAmount(min, min2);
            setNotificationsFullyHidden(min == 1.0f);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    private final void notifyAnimationStart(boolean z) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.notifyHideAnimationStart(!z);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        if (z) {
            setNotificationsVisible(false, false, false);
        }
    }

    public final float setPulseHeight(float f) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        if (notificationStackScrollLayout != null) {
            float pulseHeight = notificationStackScrollLayout.setPulseHeight(f);
            if (this.bypassController.getBypassEnabled()) {
                return 0.0f;
            }
            return pulseHeight;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mStackScroller");
        throw null;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(@NotNull NotificationEntry notificationEntry, boolean z) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        boolean shouldAnimateVisibility = shouldAnimateVisibility();
        if (!z) {
            if (!(this.mLinearDozeAmount == 0.0f || this.mLinearVisibilityAmount == 0.0f)) {
                if (notificationEntry.isRowDismissed()) {
                    shouldAnimateVisibility = false;
                } else if (!this.wakingUp && !this.willWakeUp) {
                    notificationEntry.setHeadsUpAnimatingAway(true);
                    this.mEntrySetToClearWhenFinished.add(notificationEntry);
                }
            }
        } else if (this.mEntrySetToClearWhenFinished.contains(notificationEntry)) {
            this.mEntrySetToClearWhenFinished.remove(notificationEntry);
            notificationEntry.setHeadsUpAnimatingAway(false);
        }
        updateNotificationVisibility(shouldAnimateVisibility, false);
    }

    private final boolean shouldAnimateVisibility() {
        return this.dozeParameters.getAlwaysOn() && !this.dozeParameters.getDisplayNeedsBlanking();
    }
}
