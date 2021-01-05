package com.android.systemui.statusbar.notification.init;

import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationClicker;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationListController;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.TargetSdkResolver;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinderImpl;
import com.android.systemui.statusbar.notification.collection.init.NotifPipelineInitializer;
import com.android.systemui.statusbar.notification.interruption.HeadsUpController;
import com.android.systemui.statusbar.notification.interruption.HeadsUpViewBinder;
import com.android.systemui.statusbar.notification.row.NotifBindPipelineInitializer;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.NotificationGroupAlertTransferHelper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Optional;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationsControllerImpl.kt */
public final class NotificationsControllerImpl implements NotificationsController {
    private final NotificationClicker.Builder clickerBuilder;
    private final DeviceProvisionedController deviceProvisionedController;
    private final NotificationEntryManager entryManager;
    private final FeatureFlags featureFlags;
    private final NotificationGroupAlertTransferHelper groupAlertTransferHelper;
    private final NotificationGroupManager groupManager;
    private final HeadsUpController headsUpController;
    private final HeadsUpManager headsUpManager;
    private final HeadsUpViewBinder headsUpViewBinder;
    private final Lazy<NotifPipelineInitializer> newNotifPipeline;
    private final NotifBindPipelineInitializer notifBindPipelineInitializer;
    private final Lazy<NotifPipeline> notifPipeline;
    private final NotificationListener notificationListener;
    private final NotificationRowBinderImpl notificationRowBinder;
    private final RemoteInputUriController remoteInputUriController;
    private final TargetSdkResolver targetSdkResolver;

    public NotificationsControllerImpl(@NotNull FeatureFlags featureFlags2, @NotNull NotificationListener notificationListener2, @NotNull NotificationEntryManager notificationEntryManager, @NotNull Lazy<NotifPipeline> lazy, @NotNull TargetSdkResolver targetSdkResolver2, @NotNull Lazy<NotifPipelineInitializer> lazy2, @NotNull NotifBindPipelineInitializer notifBindPipelineInitializer2, @NotNull DeviceProvisionedController deviceProvisionedController2, @NotNull NotificationRowBinderImpl notificationRowBinderImpl, @NotNull RemoteInputUriController remoteInputUriController2, @NotNull NotificationGroupManager notificationGroupManager, @NotNull NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper, @NotNull HeadsUpManager headsUpManager2, @NotNull HeadsUpController headsUpController2, @NotNull HeadsUpViewBinder headsUpViewBinder2, @NotNull NotificationClicker.Builder builder) {
        FeatureFlags featureFlags3 = featureFlags2;
        NotificationListener notificationListener3 = notificationListener2;
        NotificationEntryManager notificationEntryManager2 = notificationEntryManager;
        Lazy<NotifPipeline> lazy3 = lazy;
        TargetSdkResolver targetSdkResolver3 = targetSdkResolver2;
        Lazy<NotifPipelineInitializer> lazy4 = lazy2;
        NotifBindPipelineInitializer notifBindPipelineInitializer3 = notifBindPipelineInitializer2;
        DeviceProvisionedController deviceProvisionedController3 = deviceProvisionedController2;
        NotificationRowBinderImpl notificationRowBinderImpl2 = notificationRowBinderImpl;
        RemoteInputUriController remoteInputUriController3 = remoteInputUriController2;
        NotificationGroupManager notificationGroupManager2 = notificationGroupManager;
        NotificationGroupAlertTransferHelper notificationGroupAlertTransferHelper2 = notificationGroupAlertTransferHelper;
        HeadsUpManager headsUpManager3 = headsUpManager2;
        HeadsUpController headsUpController3 = headsUpController2;
        Intrinsics.checkParameterIsNotNull(featureFlags3, "featureFlags");
        Intrinsics.checkParameterIsNotNull(notificationListener3, "notificationListener");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager2, "entryManager");
        Intrinsics.checkParameterIsNotNull(lazy3, "notifPipeline");
        Intrinsics.checkParameterIsNotNull(targetSdkResolver3, "targetSdkResolver");
        Intrinsics.checkParameterIsNotNull(lazy4, "newNotifPipeline");
        Intrinsics.checkParameterIsNotNull(notifBindPipelineInitializer3, "notifBindPipelineInitializer");
        Intrinsics.checkParameterIsNotNull(deviceProvisionedController3, "deviceProvisionedController");
        Intrinsics.checkParameterIsNotNull(notificationRowBinderImpl2, "notificationRowBinder");
        Intrinsics.checkParameterIsNotNull(remoteInputUriController3, "remoteInputUriController");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager2, "groupManager");
        Intrinsics.checkParameterIsNotNull(notificationGroupAlertTransferHelper2, "groupAlertTransferHelper");
        Intrinsics.checkParameterIsNotNull(headsUpManager3, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(headsUpController3, "headsUpController");
        Intrinsics.checkParameterIsNotNull(headsUpViewBinder2, "headsUpViewBinder");
        NotificationClicker.Builder builder2 = builder;
        Intrinsics.checkParameterIsNotNull(builder2, "clickerBuilder");
        this.featureFlags = featureFlags3;
        this.notificationListener = notificationListener3;
        this.entryManager = notificationEntryManager2;
        this.notifPipeline = lazy3;
        this.targetSdkResolver = targetSdkResolver3;
        this.newNotifPipeline = lazy4;
        this.notifBindPipelineInitializer = notifBindPipelineInitializer3;
        this.deviceProvisionedController = deviceProvisionedController3;
        this.notificationRowBinder = notificationRowBinderImpl2;
        this.remoteInputUriController = remoteInputUriController3;
        this.groupManager = notificationGroupManager2;
        this.groupAlertTransferHelper = notificationGroupAlertTransferHelper2;
        this.headsUpManager = headsUpManager3;
        this.headsUpController = headsUpController3;
        this.headsUpViewBinder = headsUpViewBinder2;
        this.clickerBuilder = builder2;
    }

    public void initialize(@NotNull StatusBar statusBar, @NotNull NotificationPresenter notificationPresenter, @NotNull NotificationListContainer notificationListContainer, @NotNull NotificationActivityStarter notificationActivityStarter, @NotNull NotificationRowBinderImpl.BindRowCallback bindRowCallback) {
        Intrinsics.checkParameterIsNotNull(statusBar, "statusBar");
        Intrinsics.checkParameterIsNotNull(notificationPresenter, "presenter");
        Intrinsics.checkParameterIsNotNull(notificationListContainer, "listContainer");
        Intrinsics.checkParameterIsNotNull(notificationActivityStarter, "notificationActivityStarter");
        Intrinsics.checkParameterIsNotNull(bindRowCallback, "bindRowCallback");
        this.notificationListener.registerAsSystemService();
        new NotificationListController(this.entryManager, notificationListContainer, this.deviceProvisionedController).bind();
        this.notificationRowBinder.setNotificationClicker(this.clickerBuilder.build(Optional.of(statusBar), notificationActivityStarter));
        this.notificationRowBinder.setUpWithPresenter(notificationPresenter, notificationListContainer, bindRowCallback);
        this.headsUpViewBinder.setPresenter(notificationPresenter);
        this.notifBindPipelineInitializer.initialize();
        if (this.featureFlags.isNewNotifPipelineEnabled()) {
            this.newNotifPipeline.get().initialize(this.notificationListener, this.notificationRowBinder, notificationListContainer);
        }
        if (this.featureFlags.isNewNotifPipelineRenderingEnabled()) {
            TargetSdkResolver targetSdkResolver2 = this.targetSdkResolver;
            NotifPipeline notifPipeline2 = this.notifPipeline.get();
            Intrinsics.checkExpressionValueIsNotNull(notifPipeline2, "notifPipeline.get()");
            targetSdkResolver2.initialize(notifPipeline2);
            return;
        }
        this.targetSdkResolver.initialize(this.entryManager);
        this.remoteInputUriController.attach(this.entryManager);
        this.groupAlertTransferHelper.bind(this.entryManager, this.groupManager);
        this.headsUpManager.addListener(this.groupManager);
        this.headsUpManager.addListener(this.groupAlertTransferHelper);
        this.headsUpController.attach(this.entryManager, this.headsUpManager);
        this.groupManager.setHeadsUpManager(this.headsUpManager);
        this.groupAlertTransferHelper.setHeadsUpManager(this.headsUpManager);
        this.entryManager.attach(this.notificationListener);
    }

    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr, boolean z) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        if (z) {
            this.entryManager.dump(printWriter, "  ");
        }
        this.groupManager.dump(fileDescriptor, printWriter, strArr);
    }

    public void requestNotificationUpdate(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "reason");
        this.entryManager.updateNotifications(str);
    }

    public void resetUserExpandedStates() {
        for (NotificationEntry resetUserExpansion : this.entryManager.getVisibleNotifications()) {
            resetUserExpansion.resetUserExpansion();
        }
    }

    public void setNotificationSnoozed(@NotNull StatusBarNotification statusBarNotification, @NotNull NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(snoozeOption, "snoozeOption");
        if (snoozeOption.getSnoozeCriterion() != null) {
            NotificationListener notificationListener2 = this.notificationListener;
            String key = statusBarNotification.getKey();
            SnoozeCriterion snoozeCriterion = snoozeOption.getSnoozeCriterion();
            Intrinsics.checkExpressionValueIsNotNull(snoozeCriterion, "snoozeOption.snoozeCriterion");
            notificationListener2.snoozeNotification(key, snoozeCriterion.getId());
            return;
        }
        this.notificationListener.snoozeNotification(statusBarNotification.getKey(), ((long) (snoozeOption.getMinutesToSnoozeFor() * 60)) * ((long) 1000));
    }

    public int getActiveNotificationsCount() {
        return this.entryManager.getActiveNotificationsCount();
    }

    public void setNotificationSnoozed(@NotNull StatusBarNotification statusBarNotification, int i) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        this.notificationListener.snoozeNotification(statusBarNotification.getKey(), ((long) (i * 60 * 60)) * ((long) 1000));
    }
}
