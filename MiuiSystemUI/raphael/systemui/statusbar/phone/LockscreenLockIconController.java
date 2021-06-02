package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.biometrics.BiometricSourceType;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0021R$string;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.Optional;
import java.util.function.Consumer;

public class LockscreenLockIconController {
    private final AccessibilityController mAccessibilityController;
    private final View.AccessibilityDelegate mAccessibilityDelegate = new View.AccessibilityDelegate() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass7 */

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
            boolean isFingerprintDetectionRunning = LockscreenLockIconController.this.mKeyguardUpdateMonitor.isFingerprintDetectionRunning();
            boolean isUnlockingWithBiometricAllowed = LockscreenLockIconController.this.mKeyguardUpdateMonitor.isUnlockingWithBiometricAllowed(true);
            if (isFingerprintDetectionRunning && isUnlockingWithBiometricAllowed) {
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, LockscreenLockIconController.this.mResources.getString(C0021R$string.accessibility_unlock_without_fingerprint)));
                accessibilityNodeInfo.setHintText(LockscreenLockIconController.this.mResources.getString(C0021R$string.accessibility_waiting_for_fingerprint));
            } else if (LockscreenLockIconController.this.getState() == 2) {
                accessibilityNodeInfo.setClassName(LockIcon.class.getName());
                accessibilityNodeInfo.setContentDescription(LockscreenLockIconController.this.mResources.getString(C0021R$string.accessibility_scanning_face));
            }
        }
    };
    private boolean mBlockUpdates;
    private boolean mBouncerShowingScrimmed;
    private final ConfigurationController mConfigurationController;
    private final ConfigurationController.ConfigurationListener mConfigurationListener = new ConfigurationController.ConfigurationListener() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass3 */
        private int mDensity;

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onThemeChanged() {
            if (LockscreenLockIconController.this.mLockIcon != null) {
                TypedArray obtainStyledAttributes = LockscreenLockIconController.this.mLockIcon.getContext().getTheme().obtainStyledAttributes(null, new int[]{C0009R$attr.wallpaperTextColor}, 0, 0);
                int color = obtainStyledAttributes.getColor(0, -1);
                obtainStyledAttributes.recycle();
                LockscreenLockIconController.this.mLockIcon.onThemeChange(color);
            }
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            ViewGroup.LayoutParams layoutParams;
            if (LockscreenLockIconController.this.mLockIcon != null && (layoutParams = LockscreenLockIconController.this.mLockIcon.getLayoutParams()) != null) {
                layoutParams.width = LockscreenLockIconController.this.mLockIcon.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_lock_width);
                layoutParams.height = LockscreenLockIconController.this.mLockIcon.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_lock_height);
                LockscreenLockIconController.this.mLockIcon.setLayoutParams(layoutParams);
                LockscreenLockIconController.this.update(true);
            }
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onLocaleListChanged() {
            if (LockscreenLockIconController.this.mLockIcon != null) {
                LockscreenLockIconController.this.mLockIcon.setContentDescription(LockscreenLockIconController.this.mLockIcon.getResources().getText(C0021R$string.accessibility_unlock_button));
                LockscreenLockIconController.this.update(true);
            }
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onConfigChanged(Configuration configuration) {
            int i = configuration.densityDpi;
            if (i != this.mDensity) {
                this.mDensity = i;
                LockscreenLockIconController.this.update();
            }
        }
    };
    private final DockManager.DockEventListener mDockEventListener = new DockManager.DockEventListener() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$LockscreenLockIconController$YwLkB4yDF5Gwcj5NX5hNSw8eA7E */
    };
    private final Optional<DockManager> mDockManager;
    private boolean mDocked;
    private final HeadsUpManagerPhone mHeadsUpManagerPhone;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardIndicationController mKeyguardIndicationController;
    private boolean mKeyguardJustShown;
    private final KeyguardStateController.Callback mKeyguardMonitorCallback = new KeyguardStateController.Callback() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass6 */

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            boolean z = LockscreenLockIconController.this.mKeyguardShowing;
            LockscreenLockIconController lockscreenLockIconController = LockscreenLockIconController.this;
            lockscreenLockIconController.mKeyguardShowing = lockscreenLockIconController.mKeyguardStateController.isShowing();
            boolean z2 = false;
            if (!z && LockscreenLockIconController.this.mKeyguardShowing && LockscreenLockIconController.this.mBlockUpdates) {
                LockscreenLockIconController.this.mBlockUpdates = false;
                z2 = true;
            }
            if (!z && LockscreenLockIconController.this.mKeyguardShowing) {
                LockscreenLockIconController.this.mKeyguardJustShown = true;
            }
            LockscreenLockIconController.this.update(z2);
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardFadingAwayChanged() {
            if (!LockscreenLockIconController.this.mKeyguardStateController.isKeyguardFadingAway() && LockscreenLockIconController.this.mBlockUpdates) {
                LockscreenLockIconController.this.mBlockUpdates = false;
                LockscreenLockIconController.this.update(true);
            }
        }

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onUnlockedChanged() {
            LockscreenLockIconController.this.update();
        }
    };
    private boolean mKeyguardShowing;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private int mLastState;
    private LockIcon mLockIcon;
    private final LockPatternUtils mLockPatternUtils;
    private final LockscreenGestureLogger mLockscreenGestureLogger;
    private final NotificationWakeUpCoordinator mNotificationWakeUpCoordinator;
    private View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass1 */

        public void onViewAttachedToWindow(View view) {
            LockscreenLockIconController.this.mStatusBarStateController.addCallback(LockscreenLockIconController.this.mSBStateListener);
            LockscreenLockIconController.this.mConfigurationController.addCallback(LockscreenLockIconController.this.mConfigurationListener);
            LockscreenLockIconController.this.mNotificationWakeUpCoordinator.addListener(LockscreenLockIconController.this.mWakeUpListener);
            LockscreenLockIconController.this.mKeyguardUpdateMonitor.registerCallback(LockscreenLockIconController.this.mUpdateMonitorCallback);
            LockscreenLockIconController.this.mKeyguardStateController.addCallback(LockscreenLockIconController.this.mKeyguardMonitorCallback);
            LockscreenLockIconController.this.mDockManager.ifPresent(new Consumer() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$LockscreenLockIconController$1$lAN9mjl0bP11onyNXKoQAiuwbo4 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    LockscreenLockIconController.AnonymousClass1.this.lambda$onViewAttachedToWindow$0$LockscreenLockIconController$1((DockManager) obj);
                }
            });
            LockscreenLockIconController lockscreenLockIconController = LockscreenLockIconController.this;
            lockscreenLockIconController.mSimLocked = lockscreenLockIconController.mKeyguardUpdateMonitor.isSimPinSecure();
            LockscreenLockIconController.this.mConfigurationListener.onThemeChanged();
            LockscreenLockIconController.this.update();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onViewAttachedToWindow$0 */
        public /* synthetic */ void lambda$onViewAttachedToWindow$0$LockscreenLockIconController$1(DockManager dockManager) {
            dockManager.addListener(LockscreenLockIconController.this.mDockEventListener);
        }

        public void onViewDetachedFromWindow(View view) {
            LockscreenLockIconController.this.mStatusBarStateController.removeCallback(LockscreenLockIconController.this.mSBStateListener);
            LockscreenLockIconController.this.mConfigurationController.removeCallback(LockscreenLockIconController.this.mConfigurationListener);
            LockscreenLockIconController.this.mNotificationWakeUpCoordinator.removeListener(LockscreenLockIconController.this.mWakeUpListener);
            LockscreenLockIconController.this.mKeyguardUpdateMonitor.removeCallback(LockscreenLockIconController.this.mUpdateMonitorCallback);
            LockscreenLockIconController.this.mKeyguardStateController.removeCallback(LockscreenLockIconController.this.mKeyguardMonitorCallback);
            LockscreenLockIconController.this.mDockManager.ifPresent(new Consumer() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$LockscreenLockIconController$1$33uhHOghx_czm01x2awmcBSkdM */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    LockscreenLockIconController.AnonymousClass1.this.lambda$onViewDetachedFromWindow$1$LockscreenLockIconController$1((DockManager) obj);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onViewDetachedFromWindow$1 */
        public /* synthetic */ void lambda$onViewDetachedFromWindow$1$LockscreenLockIconController$1(DockManager dockManager) {
            dockManager.removeListener(LockscreenLockIconController.this.mDockEventListener);
        }
    };
    private final Resources mResources;
    private final StatusBarStateController.StateListener mSBStateListener = new StatusBarStateController.StateListener() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass2 */

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozingChanged(boolean z) {
            LockscreenLockIconController.this.setDozing(z);
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onPulsingChanged(boolean z) {
            LockscreenLockIconController.this.setPulsing(z);
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozeAmountChanged(float f, float f2) {
            if (LockscreenLockIconController.this.mLockIcon != null) {
                LockscreenLockIconController.this.mLockIcon.setDozeAmount(f2);
            }
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            LockscreenLockIconController.this.setStatusBarState(i);
        }
    };
    private final ShadeController mShadeController;
    private boolean mShowingLaunchAffordance;
    private boolean mSimLocked;
    private int mStatusBarState = 0;
    private final StatusBarStateController mStatusBarStateController;
    private boolean mTransientBiometricsError;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass5 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSimStateChanged(int i, int i2, int i3) {
            LockscreenLockIconController lockscreenLockIconController = LockscreenLockIconController.this;
            lockscreenLockIconController.mSimLocked = lockscreenLockIconController.mKeyguardUpdateMonitor.isSimPinSecure();
            LockscreenLockIconController.this.update();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            LockscreenLockIconController.this.update();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            LockscreenLockIconController.this.update();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            LockscreenLockIconController.this.update();
        }
    };
    private boolean mWakeAndUnlockRunning;
    private final NotificationWakeUpCoordinator.WakeUpListener mWakeUpListener = new NotificationWakeUpCoordinator.WakeUpListener() {
        /* class com.android.systemui.statusbar.phone.LockscreenLockIconController.AnonymousClass4 */

        @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
        public void onPulseExpansionChanged(boolean z) {
        }

        @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
        public void onFullyHiddenChanged(boolean z) {
            if (LockscreenLockIconController.this.mKeyguardBypassController.getBypassEnabled() && LockscreenLockIconController.this.updateIconVisibility()) {
                LockscreenLockIconController.this.update();
            }
        }
    };

    public LockscreenLockIconController(LockscreenGestureLogger lockscreenGestureLogger, KeyguardUpdateMonitor keyguardUpdateMonitor, LockPatternUtils lockPatternUtils, ShadeController shadeController, AccessibilityController accessibilityController, KeyguardIndicationController keyguardIndicationController, StatusBarStateController statusBarStateController, ConfigurationController configurationController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, DockManager dockManager, KeyguardStateController keyguardStateController, Resources resources, HeadsUpManagerPhone headsUpManagerPhone) {
        this.mLockscreenGestureLogger = lockscreenGestureLogger;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mLockPatternUtils = lockPatternUtils;
        this.mShadeController = shadeController;
        this.mAccessibilityController = accessibilityController;
        this.mKeyguardIndicationController = keyguardIndicationController;
        this.mStatusBarStateController = statusBarStateController;
        this.mConfigurationController = configurationController;
        this.mNotificationWakeUpCoordinator = notificationWakeUpCoordinator;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mDockManager = dockManager == null ? Optional.empty() : Optional.of(dockManager);
        this.mKeyguardStateController = keyguardStateController;
        this.mResources = resources;
        this.mHeadsUpManagerPhone = headsUpManagerPhone;
        this.mKeyguardIndicationController.setLockIconController(this);
    }

    public void attach(LockIcon lockIcon) {
        this.mLockIcon = lockIcon;
        lockIcon.setVisibility(8);
        this.mLockIcon.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$LockscreenLockIconController$w6uFCwNQV4Mtc7oy2mEXXG52_I */

            public final void onClick(View view) {
                LockscreenLockIconController.this.handleClick(view);
            }
        });
        this.mLockIcon.setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$LockscreenLockIconController$LslFmHw3JlLgJluLcqL2mxJusEk */

            public final boolean onLongClick(View view) {
                return LockscreenLockIconController.this.handleLongClick(view);
            }
        });
        this.mLockIcon.setAccessibilityDelegate(this.mAccessibilityDelegate);
        if (this.mLockIcon.isAttachedToWindow()) {
            this.mOnAttachStateChangeListener.onViewAttachedToWindow(this.mLockIcon);
        }
        this.mLockIcon.addOnAttachStateChangeListener(this.mOnAttachStateChangeListener);
        setStatusBarState(this.mStatusBarStateController.getState());
    }

    public void onScrimVisibilityChanged(Integer num) {
        if (this.mWakeAndUnlockRunning && num.intValue() == 0) {
            this.mWakeAndUnlockRunning = false;
            update();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPulsing(boolean z) {
        update();
    }

    public void onBiometricAuthModeChanged(boolean z, boolean z2) {
        if (z) {
            this.mWakeAndUnlockRunning = true;
        }
        if (z2 && this.mKeyguardBypassController.getBypassEnabled() && canBlockUpdates()) {
            this.mBlockUpdates = true;
        }
        update();
    }

    public void onShowingLaunchAffordanceChanged(Boolean bool) {
        this.mShowingLaunchAffordance = bool.booleanValue();
        update();
    }

    public void setBouncerShowingScrimmed(boolean z) {
        this.mBouncerShowingScrimmed = z;
        if (this.mKeyguardBypassController.getBypassEnabled()) {
            update();
        }
    }

    public void onBouncerPreHideAnimation() {
        update();
    }

    public void setTransientBiometricsError(boolean z) {
        this.mTransientBiometricsError = z;
        update();
    }

    /* access modifiers changed from: private */
    public boolean handleLongClick(View view) {
        this.mLockscreenGestureLogger.write(191, 0, 0);
        this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_LOCK_TAP);
        this.mKeyguardIndicationController.showTransientIndication(C0021R$string.keyguard_indication_trust_disabled);
        this.mKeyguardUpdateMonitor.onLockIconPressed();
        this.mLockPatternUtils.requireCredentialEntry(KeyguardUpdateMonitor.getCurrentUser());
        return true;
    }

    /* access modifiers changed from: private */
    public void handleClick(View view) {
        if (this.mAccessibilityController.isAccessibilityEnabled()) {
            this.mShadeController.animateCollapsePanels(0, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update() {
        update(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void update(boolean z) {
        LockIcon lockIcon;
        int state = getState();
        boolean z2 = this.mLastState != state || z;
        if (this.mBlockUpdates && canBlockUpdates()) {
            z2 = false;
        }
        if (z2 && (lockIcon = this.mLockIcon) != null) {
            lockIcon.update(state, this.mStatusBarStateController.isPulsing(), this.mStatusBarStateController.isDozing(), this.mKeyguardJustShown);
        }
        this.mLastState = state;
        this.mKeyguardJustShown = false;
        updateIconVisibility();
        updateClickability();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getState() {
        if ((this.mKeyguardStateController.canDismissLockScreen() || !this.mKeyguardStateController.isShowing() || this.mKeyguardStateController.isKeyguardGoingAway() || this.mKeyguardStateController.isKeyguardFadingAway()) && !this.mSimLocked) {
            return 1;
        }
        if (this.mTransientBiometricsError) {
            return 3;
        }
        return (!this.mKeyguardUpdateMonitor.isFaceDetectionRunning() || this.mStatusBarStateController.isPulsing()) ? 0 : 2;
    }

    private boolean canBlockUpdates() {
        return this.mKeyguardShowing || this.mKeyguardStateController.isKeyguardFadingAway();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDozing(boolean z) {
        update();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStatusBarState(int i) {
        this.mStatusBarState = i;
        updateIconVisibility();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean updateIconVisibility() {
        boolean z = (this.mStatusBarStateController.isDozing() && (!this.mStatusBarStateController.isPulsing() || this.mDocked)) || this.mWakeAndUnlockRunning || this.mShowingLaunchAffordance;
        if (this.mKeyguardBypassController.getBypassEnabled() && !this.mBouncerShowingScrimmed && ((this.mHeadsUpManagerPhone.isHeadsUpGoingAway() || this.mHeadsUpManagerPhone.hasPinnedHeadsUp() || this.mStatusBarState == 1) && !this.mNotificationWakeUpCoordinator.getNotificationsFullyHidden())) {
            z = true;
        }
        LockIcon lockIcon = this.mLockIcon;
        if (lockIcon == null) {
            return false;
        }
        return lockIcon.updateIconVisibility(!z);
    }

    private void updateClickability() {
        if (this.mAccessibilityController != null) {
            boolean z = true;
            boolean z2 = this.mKeyguardStateController.isMethodSecure() && this.mKeyguardStateController.canDismissLockScreen();
            boolean isAccessibilityEnabled = this.mAccessibilityController.isAccessibilityEnabled();
            LockIcon lockIcon = this.mLockIcon;
            if (lockIcon != null) {
                lockIcon.setClickable(isAccessibilityEnabled);
                LockIcon lockIcon2 = this.mLockIcon;
                if (!z2 || isAccessibilityEnabled) {
                    z = false;
                }
                lockIcon2.setLongClickable(z);
                this.mLockIcon.setFocusable(this.mAccessibilityController.isAccessibilityEnabled());
            }
        }
    }
}
