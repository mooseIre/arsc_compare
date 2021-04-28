package com.android.systemui.media;

import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.stack.MediaHeaderView;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class KeyguardMediaController {
    private final KeyguardBypassController bypassController;
    private final MediaHost mediaHost;
    private final NotificationLockscreenUserManager notifLockscreenUserManager;
    private final SysuiStatusBarStateController statusBarStateController;
    private MediaHeaderView view;
    private Function1<? super Boolean, Unit> visibilityChangedListener;

    public KeyguardMediaController(MediaHost mediaHost2, KeyguardBypassController keyguardBypassController, SysuiStatusBarStateController sysuiStatusBarStateController, NotificationLockscreenUserManager notificationLockscreenUserManager) {
        Intrinsics.checkParameterIsNotNull(mediaHost2, "mediaHost");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "notifLockscreenUserManager");
        this.mediaHost = mediaHost2;
        this.bypassController = keyguardBypassController;
        this.statusBarStateController = sysuiStatusBarStateController;
        this.notifLockscreenUserManager = notificationLockscreenUserManager;
        sysuiStatusBarStateController.addCallback(new StatusBarStateController.StateListener(this) {
            /* class com.android.systemui.media.KeyguardMediaController.AnonymousClass1 */
            final /* synthetic */ KeyguardMediaController this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                this.this$0.updateVisibility();
            }
        });
    }

    public final void setVisibilityChangedListener(Function1<? super Boolean, Unit> function1) {
        this.visibilityChangedListener = function1;
    }

    public final MediaHeaderView getView() {
        return this.view;
    }

    public final void attach(MediaHeaderView mediaHeaderView) {
        Intrinsics.checkParameterIsNotNull(mediaHeaderView, "mediaView");
        this.view = mediaHeaderView;
        this.mediaHost.addVisibilityChangeListener(new KeyguardMediaController$attach$1(this));
        this.mediaHost.setExpansion(0.0f);
        this.mediaHost.setShowsOnlyActiveMedia(false);
        this.mediaHost.setFalsingProtectionNeeded(true);
        this.mediaHost.init(2);
        mediaHeaderView.setContentView(this.mediaHost.getHostView());
        updateVisibility();
    }

    private final void updateVisibility() {
        Function1<? super Boolean, Unit> function1;
        int i = 0;
        boolean z = true;
        this.mediaHost.setExpansion(this.statusBarStateController.getState() == 1 ? 0.0f : 1.0f);
        if (!this.mediaHost.getVisible() || this.bypassController.getBypassEnabled() || !this.notifLockscreenUserManager.shouldShowLockscreenNotifications()) {
            z = false;
        }
        MediaHeaderView mediaHeaderView = this.view;
        int visibility = mediaHeaderView != null ? mediaHeaderView.getVisibility() : 8;
        if (!z) {
            i = 8;
        }
        MediaHeaderView mediaHeaderView2 = this.view;
        if (mediaHeaderView2 != null) {
            mediaHeaderView2.setVisibility(i);
        }
        if (visibility != i && (function1 = this.visibilityChangedListener) != null) {
            function1.invoke(Boolean.valueOf(z));
        }
    }
}
