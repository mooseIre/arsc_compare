package com.android.systemui.statusbar.notification.zen;

import android.content.Intent;
import android.service.notification.ZenModeConfig;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationViewController;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.ZenModeController;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ZenModeViewController.kt */
public final class ZenModeViewController implements ZenModeController.Callback {
    private final NotificationRowComponent.Builder builder;
    private final KeyguardBypassController bypassController;
    private boolean manuallyDismissed;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private final SysuiStatusBarStateController statusBarStateController;
    @Nullable
    private ZenModeView view;
    private ActivatableNotificationViewController viewController;
    @Nullable
    private Function1<? super Boolean, Boolean> visibilityChangedListener;
    private final ZenModeController zenModeController;

    public final void setNotificationSectionsManager(@Nullable MiuiNotificationSectionsManager miuiNotificationSectionsManager) {
    }

    public ZenModeViewController(@NotNull ZenModeController zenModeController2, @NotNull KeyguardBypassController keyguardBypassController, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull NotificationRowComponent.Builder builder2) {
        Intrinsics.checkParameterIsNotNull(zenModeController2, "zenModeController");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(builder2, "builder");
        this.zenModeController = zenModeController2;
        this.bypassController = keyguardBypassController;
        this.statusBarStateController = sysuiStatusBarStateController;
        this.notifLockscreenUserManager = notificationLockscreenUserManager;
        this.builder = builder2;
        sysuiStatusBarStateController.addCallback(new StatusBarStateController.StateListener(this) {
            /* class com.android.systemui.statusbar.notification.zen.ZenModeViewController.AnonymousClass1 */
            final /* synthetic */ ZenModeViewController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                this.this$0.updateVisibility();
            }
        });
        this.zenModeController.addCallback(this);
    }

    public final void setVisibilityChangedListener(@Nullable Function1<? super Boolean, Boolean> function1) {
        this.visibilityChangedListener = function1;
    }

    @Nullable
    public final ZenModeView getView() {
        return this.view;
    }

    public final void attach(@NotNull ZenModeView zenModeView) {
        Intrinsics.checkParameterIsNotNull(zenModeView, "zenModeView");
        this.view = zenModeView;
        if (zenModeView != null) {
            zenModeView.setController(this);
        }
        NotificationRowComponent build = this.builder.activatableNotificationView(this.view).build();
        Intrinsics.checkExpressionValueIsNotNull(build, "builder.activatableNotificationView(view).build()");
        ActivatableNotificationViewController activatableNotificationViewController = build.getActivatableNotificationViewController();
        Intrinsics.checkExpressionValueIsNotNull(activatableNotificationViewController, "builder.activatableNotif…otificationViewController");
        this.viewController = activatableNotificationViewController;
        if (activatableNotificationViewController != null) {
            activatableNotificationViewController.init();
            updateVisibility();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("viewController");
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updateVisibility() {
        ZenModeView zenModeView;
        Boolean invoke;
        boolean shouldBeVisible = shouldBeVisible();
        ZenModeView zenModeView2 = this.view;
        int i = zenModeView2 != null ? zenModeView2.mVisibility : 8;
        boolean z = false;
        int i2 = shouldBeVisible ? 0 : 8;
        if (i != i2) {
            if (i2 == 0) {
                ZenModeView zenModeView3 = this.view;
                if (zenModeView3 != null) {
                    zenModeView3.doAfterAnim(i2);
                }
                ZenModeView zenModeView4 = this.view;
                if (zenModeView4 != null) {
                    zenModeView4.resetTranslation();
                }
                ZenModeView zenModeView5 = this.view;
                if (zenModeView5 != null) {
                    zenModeView5.resetContentText();
                }
            } else {
                ZenModeView zenModeView6 = this.view;
                if (zenModeView6 != null) {
                    zenModeView6.mVisibility = i2;
                }
            }
            Function1<? super Boolean, Boolean> function1 = this.visibilityChangedListener;
            if (!(function1 == null || (invoke = function1.invoke(Boolean.valueOf(shouldBeVisible))) == null)) {
                z = invoke.booleanValue();
            }
            if (i2 == 8 && (!z || this.manuallyDismissed)) {
                ZenModeView zenModeView7 = this.view;
                if (zenModeView7 != null) {
                    zenModeView7.doAfterAnim(i2);
                }
            } else if (i2 == 0 && !z && (zenModeView = this.view) != null) {
                zenModeView.resetScaleAndAlpha();
            }
        }
    }

    private final boolean shouldBeVisible() {
        return (this.statusBarStateController.getState() == 1 || this.statusBarStateController.getState() == 2 || this.statusBarStateController.getState() == 3) && isDndOn() && !this.manuallyDismissed && !this.bypassController.getBypassEnabled() && this.notifLockscreenUserManager.shouldShowLockscreenNotifications();
    }

    public final void onSwipeToDismiss() {
        this.manuallyDismissed = true;
        updateVisibility();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onConfigChanged(@Nullable ZenModeConfig zenModeConfig) {
        this.manuallyDismissed = false;
        updateVisibility();
    }

    private final boolean isDndOn() {
        return this.zenModeController.getZen() != 0;
    }

    public final void setZenOff() {
        this.zenModeController.setZen(0, null, "ZenModeViewController");
    }

    public final void jump2Settings() {
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(new Intent("android.settings.SOUND_SETTINGS"), 0);
    }
}
