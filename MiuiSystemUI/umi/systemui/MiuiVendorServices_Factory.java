package com.android.systemui;

import android.content.Context;
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
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiVendorServices_Factory implements Factory<MiuiVendorServices> {
    private final Provider<Context> contextProvider;
    private final Provider<HeadsetPolicy> mHeadsetPolicyProvider;
    private final Provider<MiuiChargeManager> mMiuiChargeManagerProvider;
    private final Provider<MiuiFaceUnlockManager> mMiuiFaceUnlockManagerProvider;
    private final Provider<MiuiFullScreenGestureProxy> mMiuiFullScreenGestureProxyProvider;
    private final Provider<MiuiGxzwPolicy> mMiuiGxzwPolicyProvider;
    private final Provider<MiuiHeadsUpPolicy> mMiuiHeadsUpPolicyProvider;
    private final Provider<MiuiNotificationShadePolicy> mMiuiNotificationShadePolicyProvider;
    private final Provider<MiuiRecentProxy> mMiuiRecentProxyProvider;
    private final Provider<MiuiStatusBarConfigurationListener> mMiuiStatusBarConfigurationListenerProvider;
    private final Provider<NotificationAlertController> mNotifAlertControllerProvider;
    private final Provider<NotificationCountLimitPolicy> mNotifCountLimitPolicyProvider;
    private final Provider<NotificationDynamicFpsController> mNotifDynamicFpsControllerProvider;
    private final Provider<NotificationEntryManager> mNotificationEntryManagerProvider;
    private final Provider<NotificationFilterController> mNotificationFilterControllerProvider;
    private final Provider<NotificationPanelNavigationBarCoordinator> mNotificationNavigationCoordinatorProvider;
    private final Provider<OLEDScreenHelper> mOledScreenHelperProvider;
    private final Provider<OrientationPolicy> mOrientationPolicyProvider;
    private final Provider<PerformanceTools> mPerformanceToolsProvider;
    private final Provider<MiuiWallpaperZoomOutService> mWallpaperZoomOutServiceProvider;

    public MiuiVendorServices_Factory(Provider<Context> provider, Provider<MiuiWallpaperZoomOutService> provider2, Provider<MiuiHeadsUpPolicy> provider3, Provider<MiuiGxzwPolicy> provider4, Provider<NotificationFilterController> provider5, Provider<NotificationAlertController> provider6, Provider<NotificationDynamicFpsController> provider7, Provider<NotificationCountLimitPolicy> provider8, Provider<MiuiNotificationShadePolicy> provider9, Provider<MiuiRecentProxy> provider10, Provider<OrientationPolicy> provider11, Provider<PerformanceTools> provider12, Provider<NotificationPanelNavigationBarCoordinator> provider13, Provider<HeadsetPolicy> provider14, Provider<MiuiFullScreenGestureProxy> provider15, Provider<OLEDScreenHelper> provider16, Provider<MiuiChargeManager> provider17, Provider<NotificationEntryManager> provider18, Provider<MiuiFaceUnlockManager> provider19, Provider<MiuiStatusBarConfigurationListener> provider20) {
        this.contextProvider = provider;
        this.mWallpaperZoomOutServiceProvider = provider2;
        this.mMiuiHeadsUpPolicyProvider = provider3;
        this.mMiuiGxzwPolicyProvider = provider4;
        this.mNotificationFilterControllerProvider = provider5;
        this.mNotifAlertControllerProvider = provider6;
        this.mNotifDynamicFpsControllerProvider = provider7;
        this.mNotifCountLimitPolicyProvider = provider8;
        this.mMiuiNotificationShadePolicyProvider = provider9;
        this.mMiuiRecentProxyProvider = provider10;
        this.mOrientationPolicyProvider = provider11;
        this.mPerformanceToolsProvider = provider12;
        this.mNotificationNavigationCoordinatorProvider = provider13;
        this.mHeadsetPolicyProvider = provider14;
        this.mMiuiFullScreenGestureProxyProvider = provider15;
        this.mOledScreenHelperProvider = provider16;
        this.mMiuiChargeManagerProvider = provider17;
        this.mNotificationEntryManagerProvider = provider18;
        this.mMiuiFaceUnlockManagerProvider = provider19;
        this.mMiuiStatusBarConfigurationListenerProvider = provider20;
    }

    @Override // javax.inject.Provider
    public MiuiVendorServices get() {
        return provideInstance(this.contextProvider, this.mWallpaperZoomOutServiceProvider, this.mMiuiHeadsUpPolicyProvider, this.mMiuiGxzwPolicyProvider, this.mNotificationFilterControllerProvider, this.mNotifAlertControllerProvider, this.mNotifDynamicFpsControllerProvider, this.mNotifCountLimitPolicyProvider, this.mMiuiNotificationShadePolicyProvider, this.mMiuiRecentProxyProvider, this.mOrientationPolicyProvider, this.mPerformanceToolsProvider, this.mNotificationNavigationCoordinatorProvider, this.mHeadsetPolicyProvider, this.mMiuiFullScreenGestureProxyProvider, this.mOledScreenHelperProvider, this.mMiuiChargeManagerProvider, this.mNotificationEntryManagerProvider, this.mMiuiFaceUnlockManagerProvider, this.mMiuiStatusBarConfigurationListenerProvider);
    }

    public static MiuiVendorServices provideInstance(Provider<Context> provider, Provider<MiuiWallpaperZoomOutService> provider2, Provider<MiuiHeadsUpPolicy> provider3, Provider<MiuiGxzwPolicy> provider4, Provider<NotificationFilterController> provider5, Provider<NotificationAlertController> provider6, Provider<NotificationDynamicFpsController> provider7, Provider<NotificationCountLimitPolicy> provider8, Provider<MiuiNotificationShadePolicy> provider9, Provider<MiuiRecentProxy> provider10, Provider<OrientationPolicy> provider11, Provider<PerformanceTools> provider12, Provider<NotificationPanelNavigationBarCoordinator> provider13, Provider<HeadsetPolicy> provider14, Provider<MiuiFullScreenGestureProxy> provider15, Provider<OLEDScreenHelper> provider16, Provider<MiuiChargeManager> provider17, Provider<NotificationEntryManager> provider18, Provider<MiuiFaceUnlockManager> provider19, Provider<MiuiStatusBarConfigurationListener> provider20) {
        MiuiVendorServices miuiVendorServices = new MiuiVendorServices(provider.get());
        MiuiVendorServices_MembersInjector.injectMWallpaperZoomOutService(miuiVendorServices, provider2.get());
        MiuiVendorServices_MembersInjector.injectMMiuiHeadsUpPolicy(miuiVendorServices, provider3.get());
        MiuiVendorServices_MembersInjector.injectMMiuiGxzwPolicy(miuiVendorServices, provider4.get());
        MiuiVendorServices_MembersInjector.injectMNotificationFilterController(miuiVendorServices, provider5.get());
        MiuiVendorServices_MembersInjector.injectMNotifAlertController(miuiVendorServices, provider6.get());
        MiuiVendorServices_MembersInjector.injectMNotifDynamicFpsController(miuiVendorServices, provider7.get());
        MiuiVendorServices_MembersInjector.injectMNotifCountLimitPolicy(miuiVendorServices, provider8.get());
        MiuiVendorServices_MembersInjector.injectMMiuiNotificationShadePolicy(miuiVendorServices, provider9.get());
        MiuiVendorServices_MembersInjector.injectMMiuiRecentProxy(miuiVendorServices, provider10.get());
        MiuiVendorServices_MembersInjector.injectMOrientationPolicy(miuiVendorServices, provider11.get());
        MiuiVendorServices_MembersInjector.injectMPerformanceTools(miuiVendorServices, provider12.get());
        MiuiVendorServices_MembersInjector.injectMNotificationNavigationCoordinator(miuiVendorServices, provider13.get());
        MiuiVendorServices_MembersInjector.injectMHeadsetPolicy(miuiVendorServices, provider14.get());
        MiuiVendorServices_MembersInjector.injectMMiuiFullScreenGestureProxy(miuiVendorServices, provider15.get());
        MiuiVendorServices_MembersInjector.injectMOledScreenHelper(miuiVendorServices, provider16.get());
        MiuiVendorServices_MembersInjector.injectMMiuiChargeManager(miuiVendorServices, provider17.get());
        MiuiVendorServices_MembersInjector.injectMNotificationEntryManager(miuiVendorServices, provider18.get());
        MiuiVendorServices_MembersInjector.injectMMiuiFaceUnlockManager(miuiVendorServices, provider19.get());
        MiuiVendorServices_MembersInjector.injectMMiuiStatusBarConfigurationListener(miuiVendorServices, provider20.get());
        return miuiVendorServices;
    }

    public static MiuiVendorServices_Factory create(Provider<Context> provider, Provider<MiuiWallpaperZoomOutService> provider2, Provider<MiuiHeadsUpPolicy> provider3, Provider<MiuiGxzwPolicy> provider4, Provider<NotificationFilterController> provider5, Provider<NotificationAlertController> provider6, Provider<NotificationDynamicFpsController> provider7, Provider<NotificationCountLimitPolicy> provider8, Provider<MiuiNotificationShadePolicy> provider9, Provider<MiuiRecentProxy> provider10, Provider<OrientationPolicy> provider11, Provider<PerformanceTools> provider12, Provider<NotificationPanelNavigationBarCoordinator> provider13, Provider<HeadsetPolicy> provider14, Provider<MiuiFullScreenGestureProxy> provider15, Provider<OLEDScreenHelper> provider16, Provider<MiuiChargeManager> provider17, Provider<NotificationEntryManager> provider18, Provider<MiuiFaceUnlockManager> provider19, Provider<MiuiStatusBarConfigurationListener> provider20) {
        return new MiuiVendorServices_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20);
    }
}
