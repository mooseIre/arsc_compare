package com.android.systemui.statusbar.notification.collection.inflation;

import android.content.Context;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.icon.MiuiIconManager;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.notification.row.RowInflaterTask;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotificationRowBinderImpl_Factory implements Factory<NotificationRowBinderImpl> {
    private final Provider<Context> contextProvider;
    private final Provider<ExpandableNotificationRowComponent.Builder> expandableNotificationRowComponentBuilderProvider;
    private final Provider<MiuiIconManager> iconManagerProvider;
    private final Provider<LowPriorityInflationHelper> lowPriorityInflationHelperProvider;
    private final Provider<NotifBindPipeline> notifBindPipelineProvider;
    private final Provider<NotificationInterruptStateProvider> notificationInterruptionStateProvider;
    private final Provider<NotificationLockscreenUserManager> notificationLockscreenUserManagerProvider;
    private final Provider<NotificationMessagingUtil> notificationMessagingUtilProvider;
    private final Provider<NotificationRemoteInputManager> notificationRemoteInputManagerProvider;
    private final Provider<RowContentBindStage> rowContentBindStageProvider;
    private final Provider<RowInflaterTask> rowInflaterTaskProvider;

    public NotificationRowBinderImpl_Factory(Provider<Context> provider, Provider<NotificationMessagingUtil> provider2, Provider<NotificationRemoteInputManager> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotifBindPipeline> provider5, Provider<RowContentBindStage> provider6, Provider<NotificationInterruptStateProvider> provider7, Provider<RowInflaterTask> provider8, Provider<ExpandableNotificationRowComponent.Builder> provider9, Provider<MiuiIconManager> provider10, Provider<LowPriorityInflationHelper> provider11) {
        this.contextProvider = provider;
        this.notificationMessagingUtilProvider = provider2;
        this.notificationRemoteInputManagerProvider = provider3;
        this.notificationLockscreenUserManagerProvider = provider4;
        this.notifBindPipelineProvider = provider5;
        this.rowContentBindStageProvider = provider6;
        this.notificationInterruptionStateProvider = provider7;
        this.rowInflaterTaskProvider = provider8;
        this.expandableNotificationRowComponentBuilderProvider = provider9;
        this.iconManagerProvider = provider10;
        this.lowPriorityInflationHelperProvider = provider11;
    }

    @Override // javax.inject.Provider
    public NotificationRowBinderImpl get() {
        return provideInstance(this.contextProvider, this.notificationMessagingUtilProvider, this.notificationRemoteInputManagerProvider, this.notificationLockscreenUserManagerProvider, this.notifBindPipelineProvider, this.rowContentBindStageProvider, this.notificationInterruptionStateProvider, this.rowInflaterTaskProvider, this.expandableNotificationRowComponentBuilderProvider, this.iconManagerProvider, this.lowPriorityInflationHelperProvider);
    }

    public static NotificationRowBinderImpl provideInstance(Provider<Context> provider, Provider<NotificationMessagingUtil> provider2, Provider<NotificationRemoteInputManager> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotifBindPipeline> provider5, Provider<RowContentBindStage> provider6, Provider<NotificationInterruptStateProvider> provider7, Provider<RowInflaterTask> provider8, Provider<ExpandableNotificationRowComponent.Builder> provider9, Provider<MiuiIconManager> provider10, Provider<LowPriorityInflationHelper> provider11) {
        return new NotificationRowBinderImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8, provider9.get(), provider10.get(), provider11.get());
    }

    public static NotificationRowBinderImpl_Factory create(Provider<Context> provider, Provider<NotificationMessagingUtil> provider2, Provider<NotificationRemoteInputManager> provider3, Provider<NotificationLockscreenUserManager> provider4, Provider<NotifBindPipeline> provider5, Provider<RowContentBindStage> provider6, Provider<NotificationInterruptStateProvider> provider7, Provider<RowInflaterTask> provider8, Provider<ExpandableNotificationRowComponent.Builder> provider9, Provider<MiuiIconManager> provider10, Provider<LowPriorityInflationHelper> provider11) {
        return new NotificationRowBinderImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }
}
