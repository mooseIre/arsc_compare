package com.android.systemui.statusbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.statusbar.notification.row.dagger.NotificationRowComponent;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.util.InjectionInflationController;

public class SuperStatusBarViewFactory {
    private final Context mContext;
    private final InjectionInflationController mInjectionInflationController;
    private final LockscreenLockIconController mLockIconController;
    private final NotificationRowComponent.Builder mNotificationRowComponentBuilder;
    private NotificationShadeWindowView mNotificationShadeWindowView;
    private NotificationShelf mNotificationShelf;
    private StatusBarWindowView mStatusBarWindowView;

    public SuperStatusBarViewFactory(Context context, InjectionInflationController injectionInflationController, NotificationRowComponent.Builder builder, LockscreenLockIconController lockscreenLockIconController) {
        this.mContext = context;
        this.mInjectionInflationController = injectionInflationController;
        this.mNotificationRowComponentBuilder = builder;
        this.mLockIconController = lockscreenLockIconController;
    }

    public NotificationShadeWindowView getNotificationShadeWindowView() {
        NotificationShadeWindowView notificationShadeWindowView = this.mNotificationShadeWindowView;
        if (notificationShadeWindowView != null) {
            return notificationShadeWindowView;
        }
        NotificationShadeWindowView notificationShadeWindowView2 = (NotificationShadeWindowView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mContext)).inflate(C0017R$layout.super_notification_shade, (ViewGroup) null);
        this.mNotificationShadeWindowView = notificationShadeWindowView2;
        if (notificationShadeWindowView2 != null) {
            LockIcon lockIcon = (LockIcon) notificationShadeWindowView2.findViewById(C0015R$id.lock_icon);
            if (lockIcon != null) {
                this.mLockIconController.attach(lockIcon);
            }
            return this.mNotificationShadeWindowView;
        }
        throw new IllegalStateException("R.layout.super_notification_shade could not be properly inflated");
    }

    public StatusBarWindowView getStatusBarWindowView() {
        StatusBarWindowView statusBarWindowView = this.mStatusBarWindowView;
        if (statusBarWindowView != null) {
            return statusBarWindowView;
        }
        StatusBarWindowView statusBarWindowView2 = (StatusBarWindowView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mContext)).inflate(C0017R$layout.super_status_bar, (ViewGroup) null);
        this.mStatusBarWindowView = statusBarWindowView2;
        if (statusBarWindowView2 != null) {
            return statusBarWindowView2;
        }
        throw new IllegalStateException("R.layout.super_status_bar could not be properly inflated");
    }

    public NotificationShelf getNotificationShelf(ViewGroup viewGroup) {
        NotificationShelf notificationShelf = this.mNotificationShelf;
        if (notificationShelf != null) {
            return notificationShelf;
        }
        NotificationShelf notificationShelf2 = (NotificationShelf) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mContext)).inflate(C0017R$layout.status_bar_notification_shelf, viewGroup, false);
        this.mNotificationShelf = notificationShelf2;
        this.mNotificationRowComponentBuilder.activatableNotificationView(notificationShelf2).build().getActivatableNotificationViewController().init();
        NotificationShelf notificationShelf3 = this.mNotificationShelf;
        if (notificationShelf3 != null) {
            return notificationShelf3;
        }
        throw new IllegalStateException("R.layout.status_bar_notification_shelf could not be properly inflated");
    }
}
