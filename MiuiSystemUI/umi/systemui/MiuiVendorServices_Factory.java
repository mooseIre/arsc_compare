package com.android.systemui;

import android.content.Context;
import com.android.keyguard.fod.policy.MiuiGxzwPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationAlertController;
import com.android.systemui.statusbar.notification.policy.NotificationCountLimitPolicy;
import com.android.systemui.statusbar.notification.policy.NotificationDynamicFpsController;
import com.android.systemui.statusbar.policy.MiuiHeadsUpPolicy;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class MiuiVendorServices_Factory implements Factory<MiuiVendorServices> {
    private final Provider<Context> contextProvider;
    private final Provider<MiuiGxzwPolicy> mMiuiGxzwPolicyProvider;
    private final Provider<MiuiHeadsUpPolicy> mMiuiHeadsUpPolicyProvider;
    private final Provider<MiuiNotificationShadePolicy> mMiuiNotificationShadePolicyProvider;
    private final Provider<NotificationAlertController> mNotifAlertControllerProvider;
    private final Provider<NotificationCountLimitPolicy> mNotifCountLimitPolicyProvider;
    private final Provider<NotificationDynamicFpsController> mNotifDynamicFpsControllerProvider;
    private final Provider<MiuiWallpaperZoomOutService> mWallpaperZoomOutServiceProvider;

    public MiuiVendorServices_Factory(Provider<Context> provider, Provider<MiuiWallpaperZoomOutService> provider2, Provider<MiuiHeadsUpPolicy> provider3, Provider<MiuiGxzwPolicy> provider4, Provider<NotificationAlertController> provider5, Provider<NotificationDynamicFpsController> provider6, Provider<NotificationCountLimitPolicy> provider7, Provider<MiuiNotificationShadePolicy> provider8) {
        this.contextProvider = provider;
        this.mWallpaperZoomOutServiceProvider = provider2;
        this.mMiuiHeadsUpPolicyProvider = provider3;
        this.mMiuiGxzwPolicyProvider = provider4;
        this.mNotifAlertControllerProvider = provider5;
        this.mNotifDynamicFpsControllerProvider = provider6;
        this.mNotifCountLimitPolicyProvider = provider7;
        this.mMiuiNotificationShadePolicyProvider = provider8;
    }

    public MiuiVendorServices get() {
        return provideInstance(this.contextProvider, this.mWallpaperZoomOutServiceProvider, this.mMiuiHeadsUpPolicyProvider, this.mMiuiGxzwPolicyProvider, this.mNotifAlertControllerProvider, this.mNotifDynamicFpsControllerProvider, this.mNotifCountLimitPolicyProvider, this.mMiuiNotificationShadePolicyProvider);
    }

    public static MiuiVendorServices provideInstance(Provider<Context> provider, Provider<MiuiWallpaperZoomOutService> provider2, Provider<MiuiHeadsUpPolicy> provider3, Provider<MiuiGxzwPolicy> provider4, Provider<NotificationAlertController> provider5, Provider<NotificationDynamicFpsController> provider6, Provider<NotificationCountLimitPolicy> provider7, Provider<MiuiNotificationShadePolicy> provider8) {
        MiuiVendorServices miuiVendorServices = new MiuiVendorServices(provider.get());
        MiuiVendorServices_MembersInjector.injectMWallpaperZoomOutService(miuiVendorServices, provider2.get());
        MiuiVendorServices_MembersInjector.injectMMiuiHeadsUpPolicy(miuiVendorServices, provider3.get());
        MiuiVendorServices_MembersInjector.injectMMiuiGxzwPolicy(miuiVendorServices, provider4.get());
        MiuiVendorServices_MembersInjector.injectMNotifAlertController(miuiVendorServices, provider5.get());
        MiuiVendorServices_MembersInjector.injectMNotifDynamicFpsController(miuiVendorServices, provider6.get());
        MiuiVendorServices_MembersInjector.injectMNotifCountLimitPolicy(miuiVendorServices, provider7.get());
        MiuiVendorServices_MembersInjector.injectMMiuiNotificationShadePolicy(miuiVendorServices, provider8.get());
        return miuiVendorServices;
    }

    public static MiuiVendorServices_Factory create(Provider<Context> provider, Provider<MiuiWallpaperZoomOutService> provider2, Provider<MiuiHeadsUpPolicy> provider3, Provider<MiuiGxzwPolicy> provider4, Provider<NotificationAlertController> provider5, Provider<NotificationDynamicFpsController> provider6, Provider<NotificationCountLimitPolicy> provider7, Provider<MiuiNotificationShadePolicy> provider8) {
        return new MiuiVendorServices_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}
