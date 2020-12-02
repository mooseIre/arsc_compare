package com.android.systemui.statusbar.phone;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.biometrics.BiometricSourceType;
import android.util.AttributeSet;
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
    /* access modifiers changed from: private */
    public boolean mBlockUpdates;
    private boolean mBouncerShowingScrimmed;
    /* access modifiers changed from: private */
    public final ConfigurationController mConfigurationController;
    /* access modifiers changed from: private */
    public final ConfigurationController.ConfigurationListener mConfigurationListener = new ConfigurationController.ConfigurationListener() {
        private int mDensity;

        public void onThemeChanged() {
            if (LockscreenLockIconController.this.mLockIcon != null) {
                TypedArray obtainStyledAttributes = LockscreenLockIconController.this.mLockIcon.getContext().getTheme().obtainStyledAttributes((AttributeSet) null, new int[]{C0009R$attr.wallpaperTextColor}, 0, 0);
                int color = obtainStyledAttributes.getColor(0, -1);
                obtainStyledAttributes.recycle();
                LockscreenLockIconController.this.mLockIcon.onThemeChange(color);
            }
        }

        public void onDensityOrFontScaleChanged() {
            ViewGroup.LayoutParams layoutParams;
            if (LockscreenLockIconController.this.mLockIcon != null && (layoutParams = LockscreenLockIconController.this.mLockIcon.getLayoutParams()) != null) {
                layoutParams.width = LockscreenLockIconController.this.mLockIcon.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_lock_width);
                layoutParams.height = LockscreenLockIconController.this.mLockIcon.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_lock_height);
                LockscreenLockIconController.this.mLockIcon.setLayoutParams(layoutParams);
                LockscreenLockIconController.this.update(true);
            }
        }

        public void onLocaleListChanged() {
            if (LockscreenLockIconController.this.mLockIcon != null) {
                LockscreenLockIconController.this.mLockIcon.setContentDescription(LockscreenLockIconController.this.mLockIcon.getResources().getText(C0021R$string.accessibility_unlock_button));
                LockscreenLockIconController.this.update(true);
            }
        }

        public void onConfigChanged(Configuration configuration) {
            int i = configuration.densityDpi;
            if (i != this.mDensity) {
                this.mDensity = i;
                LockscreenLockIconController.this.update();
            }
        }
    };
    /* access modifiers changed from: private */
    public final DockManager.DockEventListener mDockEventListener = new DockManager.DockEventListener() {
    };
    /* access modifiers changed from: private */
    public final Optional<DockManager> mDockManager;
    private boolean mDocked;
    private final HeadsUpManagerPhone mHeadsUpManagerPhone;
    /* access modifiers changed from: private */
    public final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardIndicationController mKeyguardIndicationController;
    /* access modifiers changed from: private */
    public boolean mKeyguardJustShown;
    /* access modifiers changed from: private */
    public final KeyguardStateController.Callback mKeyguardMonitorCallback = new KeyguardStateController.Callback() {
        public void onKeyguardShowingChanged() {
            boolean access$2100 = LockscreenLockIconController.this.mKeyguardShowing;
            LockscreenLockIconController lockscreenLockIconController = LockscreenLockIconController.this;
            boolean unused = lockscreenLockIconController.mKeyguardShowing = lockscreenLockIconController.mKeyguardStateController.isShowing();
            boolean z = false;
            if (!access$2100 && LockscreenLockIconController.this.mKeyguardShowing && LockscreenLockIconController.this.mBlockUpdates) {
                boolean unused2 = LockscreenLockIconController.this.mBlockUpdates = false;
                z = true;
            }
            if (!access$2100 && LockscreenLockIconController.this.mKeyguardShowing) {
                boolean unused3 = LockscreenLockIconController.this.mKeyguardJustShown = true;
            }
            LockscreenLockIconController.this.update(z);
        }

        public void onKeyguardFadingAwayChanged() {
            if (!LockscreenLockIconController.this.mKeyguardStateController.isKeyguardFadingAway() && LockscreenLockIconController.this.mBlockUpdates) {
                boolean unused = LockscreenLockIconController.this.mBlockUpdates = false;
                LockscreenLockIconController.this.update(true);
            }
        }

        public void onUnlockedChanged() {
            LockscreenLockIconController.this.update();
        }
    };
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    /* access modifiers changed from: private */
    public final KeyguardStateController mKeyguardStateController;
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private int mLastState;
    /* access modifiers changed from: private */
    public LockIcon mLockIcon;
    private final LockPatternUtils mLockPatternUtils;
    private final LockscreenGestureLogger mLockscreenGestureLogger;
    /* access modifiers changed from: private */
    public final NotificationWakeUpCoordinator mNotificationWakeUpCoordinator;
    private View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        public void onViewAttachedToWindow(View view) {
            LockscreenLockIconController.this.mStatusBarStateController.addCallback(LockscreenLockIconController.this.mSBStateListener);
            LockscreenLockIconController.this.mConfigurationController.addCallback(LockscreenLockIconController.this.mConfigurationListener);
            LockscreenLockIconController.this.mNotificationWakeUpCoordinator.addListener(LockscreenLockIconController.this.mWakeUpListener);
            LockscreenLockIconController.this.mKeyguardUpdateMonitor.registerCallback(LockscreenLockIconController.this.mUpdateMonitorCallback);
            LockscreenLockIconController.this.mKeyguardStateController.addCallback(LockscreenLockIconController.this.mKeyguardMonitorCallback);
            LockscreenLockIconController.this.mDockManager.ifPresent(new Consumer() {
                public final void accept(Object obj) {
                    LockscreenLockIconController.AnonymousClass1.this.lambda$onViewAttachedToWindow$0$LockscreenLockIconController$1((DockManager) obj);
                }
            });
            LockscreenLockIconController lockscreenLockIconController = LockscreenLockIconController.this;
            boolean unused = lockscreenLockIconController.mSimLocked = lockscreenLockIconController.mKeyguardUpdateMonitor.isSimPinSecure();
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
    /* access modifiers changed from: private */
    public final Resources mResources;
    /* access modifiers changed from: private */
    public final StatusBarStateController.StateListener mSBStateListener = new StatusBarStateController.StateListener() {
        public void onDozingChanged(boolean z) {
            LockscreenLockIconController.this.setDozing(z);
        }

        public void onPulsingChanged(boolean z) {
            LockscreenLockIconController.this.setPulsing(z);
        }

        public void onDozeAmountChanged(float f, float f2) {
            if (LockscreenLockIconController.this.mLockIcon != null) {
                LockscreenLockIconController.this.mLockIcon.setDozeAmount(f2);
            }
        }

        public void onStateChanged(int i) {
            LockscreenLockIconController.this.setStatusBarState(i);
        }
    };
    private final ShadeController mShadeController;
    private boolean mShowingLaunchAffordance;
    /* access modifiers changed from: private */
    public boolean mSimLocked;
    private int mStatusBarState = 0;
    /* access modifiers changed from: private */
    public final StatusBarStateController mStatusBarStateController;
    private boolean mTransientBiometricsError;
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onSimStateChanged(int i, int i2, int i3) {
            LockscreenLockIconController lockscreenLockIconController = LockscreenLockIconController.this;
            boolean unused = lockscreenLockIconController.mSimLocked = lockscreenLockIconController.mKeyguardUpdateMonitor.isSimPinSecure();
            LockscreenLockIconController.this.update();
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            LockscreenLockIconController.this.update();
        }

        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            LockscreenLockIconController.this.update();
        }

        public void onStrongAuthStateChanged(int i) {
            LockscreenLockIconController.this.update();
        }
    };
    private boolean mWakeAndUnlockRunning;
    /* access modifiers changed from: private */
    public final NotificationWakeUpCoordinator.WakeUpListener mWakeUpListener = new NotificationWakeUpCoordinator.WakeUpListener() {
        public void onPulseExpansionChanged(boolean z) {
        }

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
            public final void onClick(View view) {
                LockscreenLockIconController.this.handleClick(view);
            }
        });
        this.mLockIcon.setOnLongClickListener(new View.OnLongClickListener() {
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
    public void setPulsing(boolean z) {
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
    public void update() {
        update(false);
    }

    /* access modifiers changed from: private */
    public void update(boolean z) {
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
    public int getState() {
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
    public void setDozing(boolean z) {
        update();
    }

    /* access modifiers changed from: private */
    public void setStatusBarState(int i) {
        this.mStatusBarState = i;
        updateIconVisibility();
    }

    /* access modifiers changed from: private */
    public boolean updateIconVisibility() {
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
