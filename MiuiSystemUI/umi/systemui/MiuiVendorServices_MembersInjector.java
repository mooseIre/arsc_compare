package com.android.systemui;

import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.systemui.recents.MiuiRecentProxy;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import com.android.systemui.vendor.OrientationPolicy;

public final class MiuiVendorServices_MembersInjector {
    public static void injectMWallpaperZoomOutService(MiuiVendorServices miuiVendorServices, MiuiWallpaperZoomOutService miuiWallpaperZoomOutService) {
        miuiVendorServices.mWallpaperZoomOutService = miuiWallpaperZoomOutService;
    }

    public static void injectMMiuiHeadsUpPolicy(MiuiVendorServices miuiVendorServices, MiuiHeadsUpPolicy miuiHeadsUpPolicy) {
        miuiVendorServices.mMiuiHeadsUpPolicy = miuiHeadsUpPolicy;
    }

    public static void injectMMiuiGxzwPolicy(MiuiVendorServices miuiVendorServices, MiuiGxzwPolicy miuiGxzwPolicy) {
        miuiVendorServices.mMiuiGxzwPolicy = miuiGxzwPolicy;
    }

    public static void injectMNotifAlertController(MiuiVendorServices miuiVendorServices, NotificationAlertController notificationAlertController) {
        miuiVendorServices.mNotifAlertController = notificationAlertController;
    }

    public static void injectMNotifDynamicFpsController(MiuiVendorServices miuiVendorServices, NotificationDynamicFpsController notificationDynamicFpsController) {
        miuiVendorServices.mNotifDynamicFpsController = notificationDynamicFpsController;
    }

    public static void injectMNotifCountLimitPolicy(MiuiVendorServices miuiVendorServices, NotificationCountLimitPolicy notificationCountLimitPolicy) {
        miuiVendorServices.mNotifCountLimitPolicy = notificationCountLimitPolicy;
    }

    public static void injectMMiuiNotificationShadePolicy(MiuiVendorServices miuiVendorServices, MiuiNotificationShadePolicy miuiNotificationShadePolicy) {
        miuiVendorServices.mMiuiNotificationShadePolicy = miuiNotificationShadePolicy;
    }

    public static void injectMMiuiRecentProxy(MiuiVendorServices miuiVendorServices, MiuiRecentProxy miuiRecentProxy) {
        miuiVendorServices.mMiuiRecentProxy = miuiRecentProxy;
    }

    public static void injectMOrientationPolicy(MiuiVendorServices miuiVendorServices, OrientationPolicy orientationPolicy) {
        miuiVendorServices.mOrientationPolicy = orientationPolicy;
    }

    public static void injectMNotificationNavigationCoordinator(MiuiVendorServices miuiVendorServices, NotificationPanelNavigationBarCoordinator notificationPanelNavigationBarCoordinator) {
        miuiVendorServices.mNotificationNavigationCoordinator = notificationPanelNavigationBarCoordinator;
    }
}
