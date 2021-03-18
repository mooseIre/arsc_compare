package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import com.android.systemui.media.MediaDataFilter;
import com.android.systemui.media.MediaTimeoutListener;
import com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import com.android.systemui.statusbar.notification.zen.ZenModeViewController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationSwipeCallback.kt */
public final class MiuiNotificationSwipeCallback extends NotificationCallbackWrapper {
    private final MediaDataFilter mediaManager;
    private final MediaTimeoutListener mediaTimeoutListener;
    private final ZenModeViewController zenModeViewController;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationSwipeCallback(@NotNull NotificationSwipeHelper.NotificationCallback notificationCallback, @NotNull MediaTimeoutListener mediaTimeoutListener2, @NotNull MediaDataFilter mediaDataFilter, @NotNull ZenModeViewController zenModeViewController2) {
        super(notificationCallback);
        Intrinsics.checkParameterIsNotNull(notificationCallback, "base");
        Intrinsics.checkParameterIsNotNull(mediaTimeoutListener2, "mediaTimeoutListener");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter, "mediaManager");
        Intrinsics.checkParameterIsNotNull(zenModeViewController2, "zenModeViewController");
        this.mediaTimeoutListener = mediaTimeoutListener2;
        this.mediaManager = mediaDataFilter;
        this.zenModeViewController = zenModeViewController2;
    }

    @Override // com.android.systemui.SwipeHelper.Callback, com.android.systemui.statusbar.notification.stack.NotificationCallbackWrapper
    public boolean canChildBeDragged(@NotNull View view, int i) {
        Intrinsics.checkParameterIsNotNull(view, "animView");
        if (MiuiNotificationSwipeCallbackKt.isPersistentNotificationRow(view)) {
            return false;
        }
        if (!(view instanceof MiuiMediaHeaderView)) {
            return super.canChildBeDragged(view, i);
        }
        if (!((MiuiMediaHeaderView) view).canMediaScrollHorizontally(i)) {
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.SwipeHelper.Callback
    public boolean canChildBeDismissedInDirection(@Nullable View view, boolean z) {
        return canChildBeDismissed(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback, com.android.systemui.statusbar.notification.stack.NotificationCallbackWrapper
    public boolean canChildBeDismissed(@Nullable View view) {
        if (view instanceof MiuiMediaHeaderView) {
            return !this.mediaTimeoutListener.hasPlayingMedia();
        }
        if (view instanceof ZenModeView) {
            return ((ZenModeView) view).getCanSwipe();
        }
        return super.canChildBeDismissed(view);
    }

    @Override // com.android.systemui.SwipeHelper.Callback, com.android.systemui.statusbar.notification.stack.NotificationCallbackWrapper
    public void onChildDismissed(@Nullable View view) {
        super.onChildDismissed(view);
        if (view instanceof MiuiMediaHeaderView) {
            this.mediaManager.onSwipeToDismiss();
        }
        if (view instanceof ZenModeView) {
            this.zenModeViewController.onSwipeToDismiss();
        }
    }
}
