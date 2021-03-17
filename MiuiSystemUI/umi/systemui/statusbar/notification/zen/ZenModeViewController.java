package com.android.systemui.statusbar.notification.zen;

import android.content.Intent;
import android.service.notification.ZenModeConfig;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.ZenModeController;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ZenModeViewController.kt */
public final class ZenModeViewController implements ZenModeController.Callback {
    private final KeyguardBypassController bypassController;
    private boolean manuallyDismissed;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private final SysuiStatusBarStateController statusBarStateController;
    @Nullable
    private ZenModeView view;
    @Nullable
    private Function2<? super Boolean, ? super Boolean, Unit> visibilityChangedListener;
    private final ZenModeController zenModeController;

    public ZenModeViewController(@NotNull ZenModeController zenModeController2, @NotNull KeyguardBypassController keyguardBypassController, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager) {
        Intrinsics.checkParameterIsNotNull(zenModeController2, "zenModeController");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
        this.zenModeController = zenModeController2;
        this.bypassController = keyguardBypassController;
        this.statusBarStateController = sysuiStatusBarStateController;
        this.notifLockscreenUserManager = notificationLockscreenUserManager;
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

    public final void setVisibilityChangedListener(@Nullable Function2<? super Boolean, ? super Boolean, Unit> function2) {
        this.visibilityChangedListener = function2;
    }

    @Nullable
    public final ZenModeView getView() {
        return this.view;
    }

    public final void attach(@NotNull ZenModeView zenModeView) {
        Intrinsics.checkParameterIsNotNull(zenModeView, "zenModeView");
        this.view = zenModeView;
        zenModeView.setController(this);
        updateVisibility();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updateVisibility() {
        boolean shouldBeVisible = shouldBeVisible();
        ZenModeView zenModeView = this.view;
        int i = 8;
        int visibility = zenModeView != null ? zenModeView.getVisibility() : 8;
        if (shouldBeVisible) {
            i = 0;
        }
        ZenModeView zenModeView2 = this.view;
        if (zenModeView2 != null) {
            zenModeView2.setVisibility(i);
        }
        ZenModeView zenModeView3 = this.view;
        if (zenModeView3 != null) {
            zenModeView3.loadOrReleaseContent(i);
        }
        if (visibility != i) {
            if (i == 0) {
                ZenModeView zenModeView4 = this.view;
                if (zenModeView4 != null) {
                    zenModeView4.resetTranslation();
                }
                ZenModeView zenModeView5 = this.view;
                if (zenModeView5 != null) {
                    zenModeView5.resetContentText();
                }
            }
            Function2<? super Boolean, ? super Boolean, Unit> function2 = this.visibilityChangedListener;
            if (function2 != null) {
                function2.invoke(Boolean.valueOf(shouldBeVisible), Boolean.valueOf(this.manuallyDismissed));
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
