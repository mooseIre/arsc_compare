package com.android.systemui.statusbar.notification;

import android.content.res.Configuration;
import android.view.ViewTreeObserver;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeControllerExt;
import com.android.systemui.statusbar.policy.ConfigurationController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationPanelNavigationBarCoordinator.kt */
public final class NotificationPanelNavigationBarCoordinator implements CommandQueue.Callbacks, ConfigurationController.ConfigurationListener {
    private NavigationBarView barView;
    @NotNull
    private final CommandQueue commandQueue;
    @NotNull
    private final ConfigurationController configurationController;
    private int disable1;
    private int lastNavigationBarMode = -1;
    @NotNull
    private final LightBarController lightBarController;
    @NotNull
    private final int[] location = new int[2];
    private boolean navBarDarkMode;
    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new NotificationPanelNavigationBarCoordinator$onGlobalLayoutListener$1(this);
    private int orientation = 1;

    public NotificationPanelNavigationBarCoordinator(@NotNull CommandQueue commandQueue2, @NotNull ConfigurationController configurationController2, @NotNull LightBarController lightBarController2) {
        Intrinsics.checkParameterIsNotNull(commandQueue2, "commandQueue");
        Intrinsics.checkParameterIsNotNull(configurationController2, "configurationController");
        Intrinsics.checkParameterIsNotNull(lightBarController2, "lightBarController");
        this.commandQueue = commandQueue2;
        this.configurationController = configurationController2;
        this.lightBarController = lightBarController2;
    }

    public static final /* synthetic */ NavigationBarView access$getBarView$p(NotificationPanelNavigationBarCoordinator notificationPanelNavigationBarCoordinator) {
        NavigationBarView navigationBarView = notificationPanelNavigationBarCoordinator.barView;
        if (navigationBarView != null) {
            return navigationBarView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("barView");
        throw null;
    }

    public final boolean getNavBarDarkMode() {
        return this.navBarDarkMode;
    }

    @NotNull
    public final int[] getLocation() {
        return this.location;
    }

    public final void start() {
        this.configurationController.addCallback(this);
        this.commandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(@Nullable Configuration configuration) {
        super.onConfigChanged(configuration);
        this.orientation = configuration != null ? configuration.orientation : this.orientation;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        super.disable(i, i2, i3, z);
        this.disable1 = i2;
    }

    public final void setNavigationBarView(@NotNull NavigationBarView navigationBarView) {
        Intrinsics.checkParameterIsNotNull(navigationBarView, "barView");
        NavigationBarView navigationBarView2 = this.barView;
        if (navigationBarView2 != null) {
            if (navigationBarView2 != null) {
                ViewTreeObserver viewTreeObserver = navigationBarView2.getViewTreeObserver();
                if (viewTreeObserver != null) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this.onGlobalLayoutListener);
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("barView");
                throw null;
            }
        }
        this.barView = navigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getViewTreeObserver().addOnGlobalLayoutListener(this.onGlobalLayoutListener);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("barView");
            throw null;
        }
    }

    public final void switchNavigationBarModeIfNeed(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "stackScrollLayout");
        if (this.barView != null && !NavigationModeControllerExt.INSTANCE.getMIsFsgMode()) {
            boolean z = false;
            if (this.orientation != 2) {
                float bottomMostNotificationBottom = notificationStackScrollLayout.getBottomMostNotificationBottom();
                int[] iArr = this.location;
                if (bottomMostNotificationBottom > ((float) iArr[1]) && iArr[1] > 0) {
                    z = true;
                }
                if (z != this.navBarDarkMode) {
                    m21switch(z);
                }
            } else if (this.lastNavigationBarMode != -1) {
                m21switch(false);
            }
        }
    }

    /* renamed from: switch  reason: not valid java name */
    private final void m21switch(boolean z) {
        if (z) {
            updateNavigationBarMode(1);
            this.lastNavigationBarMode = this.lightBarController.getNavigationBarMode();
        } else {
            updateNavigationBarMode(this.lastNavigationBarMode);
            this.lastNavigationBarMode = -1;
        }
        this.navBarDarkMode = z;
    }

    private final void updateNavigationBarMode(int i) {
        NavigationBarView navigationBarView = this.barView;
        if (navigationBarView != null) {
            navigationBarView.getBarTransitions().transitionTo(i, true);
            NavigationBarView navigationBarView2 = this.barView;
            if (navigationBarView2 != null) {
                navigationBarView2.setDisabledFlags(this.disable1);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("barView");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("barView");
            throw null;
        }
    }
}
