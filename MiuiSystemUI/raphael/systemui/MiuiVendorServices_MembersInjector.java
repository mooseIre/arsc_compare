package com.android.systemui;

import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.systemui.recents.MiuiFullScreenGestureProxy;
import com.android.systemui.recents.MiuiRecentProxy;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.notification.policy.NotificationFilterController;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import com.android.systemui.statusbar.policy.MiuiStatusBarConfigurationListener;
import com.android.systemui.vendor.HeadsetPolicy;
import com.android.systemui.vendor.OrientationPolicy;
import com.miui.systemui.display.OLEDScreenHelper;

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

    public static void injectMNotificationFilterController(MiuiVendorServices miuiVendorServices, NotificationFilterController notificationFilterController) {
        miuiVendorServices.mNotificationFilterController = notificationFilterController;
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

    public static void injectMPerformanceTools(MiuiVendorServices miuiVendorServices, PerformanceTools performanceTools) {
        miuiVendorServices.mPerformanceTools = performanceTools;
    }

    public static void injectMNotificationNavigationCoordinator(MiuiVendorServices miuiVendorServices, NotificationPanelNavigationBarCoordinator notificationPanelNavigationBarCoordinator) {
        miuiVendorServices.mNotificationNavigationCoordinator = notificationPanelNavigationBarCoordinator;
    }

    public static void injectMHeadsetPolicy(MiuiVendorServices miuiVendorServices, HeadsetPolicy headsetPolicy) {
        miuiVendorServices.mHeadsetPolicy = headsetPolicy;
    }

    public static void injectMMiuiFullScreenGestureProxy(MiuiVendorServices miuiVendorServices, MiuiFullScreenGestureProxy miuiFullScreenGestureProxy) {
        miuiVendorServices.mMiuiFullScreenGestureProxy = miuiFullScreenGestureProxy;
    }

    public static void injectMOledScreenHelper(MiuiVendorServices miuiVendorServices, OLEDScreenHelper oLEDScreenHelper) {
        miuiVendorServices.mOledScreenHelper = oLEDScreenHelper;
    }

    public static void injectMMiuiChargeManager(MiuiVendorServices miuiVendorServices, MiuiChargeManager miuiChargeManager) {
        miuiVendorServices.mMiuiChargeManager = miuiChargeManager;
    }

    public static void injectMNotificationEntryManager(MiuiVendorServices miuiVendorServices, NotificationEntryManager notificationEntryManager) {
        miuiVendorServices.mNotificationEntryManager = notificationEntryManager;
    }

    public static void injectMMiuiFaceUnlockManager(MiuiVendorServices miuiVendorServices, MiuiFaceUnlockManager miuiFaceUnlockManager) {
        miuiVendorServices.mMiuiFaceUnlockManager = miuiFaceUnlockManager;
    }

    public static void injectMMiuiStatusBarConfigurationListener(MiuiVendorServices miuiVendorServices, MiuiStatusBarConfigurationListener miuiStatusBarConfigurationListener) {
        miuiVendorServices.mMiuiStatusBarConfigurationListener = miuiStatusBarConfigurationListener;
    }
}
