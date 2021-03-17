package com.android.systemui.statusbar.notification.init;

import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.NotificationClicker;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationsControllerImpl_Factory implements Factory<NotificationsControllerImpl> {
    private final Provider<NotificationClicker.Builder> clickerBuilderProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<NotificationEntryManager> entryManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<NotificationGroupAlertTransferHelper> groupAlertTransferHelperProvider;
    private final Provider<NotificationGroupManager> groupManagerProvider;
    private final Provider<HeadsUpController> headsUpControllerProvider;
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<HeadsUpViewBinder> headsUpViewBinderProvider;
    private final Provider<NotifPipelineInitializer> newNotifPipelineProvider;
    private final Provider<NotifBindPipelineInitializer> notifBindPipelineInitializerProvider;
    private final Provider<NotifPipeline> notifPipelineProvider;
    private final Provider<NotificationListener> notificationListenerProvider;
    private final Provider<NotificationRowBinderImpl> notificationRowBinderProvider;
    private final Provider<RemoteInputUriController> remoteInputUriControllerProvider;
    private final Provider<TargetSdkResolver> targetSdkResolverProvider;

    public NotificationsControllerImpl_Factory(Provider<FeatureFlags> provider, Provider<NotificationListener> provider2, Provider<NotificationEntryManager> provider3, Provider<NotifPipeline> provider4, Provider<TargetSdkResolver> provider5, Provider<NotifPipelineInitializer> provider6, Provider<NotifBindPipelineInitializer> provider7, Provider<DeviceProvisionedController> provider8, Provider<NotificationRowBinderImpl> provider9, Provider<RemoteInputUriController> provider10, Provider<NotificationGroupManager> provider11, Provider<NotificationGroupAlertTransferHelper> provider12, Provider<HeadsUpManager> provider13, Provider<HeadsUpController> provider14, Provider<HeadsUpViewBinder> provider15, Provider<NotificationClicker.Builder> provider16) {
        this.featureFlagsProvider = provider;
        this.notificationListenerProvider = provider2;
        this.entryManagerProvider = provider3;
        this.notifPipelineProvider = provider4;
        this.targetSdkResolverProvider = provider5;
        this.newNotifPipelineProvider = provider6;
        this.notifBindPipelineInitializerProvider = provider7;
        this.deviceProvisionedControllerProvider = provider8;
        this.notificationRowBinderProvider = provider9;
        this.remoteInputUriControllerProvider = provider10;
        this.groupManagerProvider = provider11;
        this.groupAlertTransferHelperProvider = provider12;
        this.headsUpManagerProvider = provider13;
        this.headsUpControllerProvider = provider14;
        this.headsUpViewBinderProvider = provider15;
        this.clickerBuilderProvider = provider16;
    }

    @Override // javax.inject.Provider
    public NotificationsControllerImpl get() {
        return provideInstance(this.featureFlagsProvider, this.notificationListenerProvider, this.entryManagerProvider, this.notifPipelineProvider, this.targetSdkResolverProvider, this.newNotifPipelineProvider, this.notifBindPipelineInitializerProvider, this.deviceProvisionedControllerProvider, this.notificationRowBinderProvider, this.remoteInputUriControllerProvider, this.groupManagerProvider, this.groupAlertTransferHelperProvider, this.headsUpManagerProvider, this.headsUpControllerProvider, this.headsUpViewBinderProvider, this.clickerBuilderProvider);
    }

    public static NotificationsControllerImpl provideInstance(Provider<FeatureFlags> provider, Provider<NotificationListener> provider2, Provider<NotificationEntryManager> provider3, Provider<NotifPipeline> provider4, Provider<TargetSdkResolver> provider5, Provider<NotifPipelineInitializer> provider6, Provider<NotifBindPipelineInitializer> provider7, Provider<DeviceProvisionedController> provider8, Provider<NotificationRowBinderImpl> provider9, Provider<RemoteInputUriController> provider10, Provider<NotificationGroupManager> provider11, Provider<NotificationGroupAlertTransferHelper> provider12, Provider<HeadsUpManager> provider13, Provider<HeadsUpController> provider14, Provider<HeadsUpViewBinder> provider15, Provider<NotificationClicker.Builder> provider16) {
        return new NotificationsControllerImpl(provider.get(), provider2.get(), provider3.get(), DoubleCheck.lazy(provider4), provider5.get(), DoubleCheck.lazy(provider6), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get());
    }

    public static NotificationsControllerImpl_Factory create(Provider<FeatureFlags> provider, Provider<NotificationListener> provider2, Provider<NotificationEntryManager> provider3, Provider<NotifPipeline> provider4, Provider<TargetSdkResolver> provider5, Provider<NotifPipelineInitializer> provider6, Provider<NotifBindPipelineInitializer> provider7, Provider<DeviceProvisionedController> provider8, Provider<NotificationRowBinderImpl> provider9, Provider<RemoteInputUriController> provider10, Provider<NotificationGroupManager> provider11, Provider<NotificationGroupAlertTransferHelper> provider12, Provider<HeadsUpManager> provider13, Provider<HeadsUpController> provider14, Provider<HeadsUpViewBinder> provider15, Provider<NotificationClicker.Builder> provider16) {
        return new NotificationsControllerImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16);
    }
}
