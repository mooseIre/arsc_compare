package com.android.systemui.statusbar.phone;

import android.app.ActivityTaskManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.dreams.IDreamManager;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.EventLog;
import android.view.RemoteAnimationAdapter;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationClickNotifier;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.Lazy;
import java.util.Objects;
import java.util.concurrent.Executor;

public class StatusBarNotificationActivityStarter implements NotificationActivityStarter {
    private final ActivityIntentHelper mActivityIntentHelper;
    private final ActivityLaunchAnimator mActivityLaunchAnimator;
    private final ActivityStarter mActivityStarter;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final Handler mBackgroundHandler;
    private final BubbleController mBubbleController;
    private final NotificationClickNotifier mClickNotifier;
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private final IDreamManager mDreamManager;
    /* access modifiers changed from: private */
    public final NotificationEntryManager mEntryManager;
    private final FeatureFlags mFeatureFlags;
    private final NotificationGroupManager mGroupManager;
    /* access modifiers changed from: private */
    public final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mIsCollapsingToShowActivityOverLockscreen;
    private final KeyguardManager mKeyguardManager;
    private final KeyguardStateController mKeyguardStateController;
    private final LockPatternUtils mLockPatternUtils;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private final StatusBarNotificationActivityStarterLogger mLogger;
    private final Handler mMainThreadHandler;
    private final MetricsLogger mMetricsLogger;
    /* access modifiers changed from: private */
    public final NotifCollection mNotifCollection;
    /* access modifiers changed from: private */
    public final NotifPipeline mNotifPipeline;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    /* access modifiers changed from: private */
    public final NotificationPanelViewController mNotificationPanel;
    private final NotificationPresenter mPresenter;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final ShadeController mShadeController;
    private final StatusBar mStatusBar;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarRemoteInputCallback mStatusBarRemoteInputCallback;
    private final StatusBarStateController mStatusBarStateController;
    private final Executor mUiBgExecutor;

    private StatusBarNotificationActivityStarter(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger, StatusBar statusBar, NotificationPresenter notificationPresenter, NotificationPanelViewController notificationPanelViewController, ActivityLaunchAnimator activityLaunchAnimator) {
        this.mContext = context;
        this.mCommandQueue = commandQueue;
        this.mMainThreadHandler = handler;
        this.mBackgroundHandler = handler2;
        this.mUiBgExecutor = executor;
        this.mEntryManager = notificationEntryManager;
        this.mNotifPipeline = notifPipeline;
        this.mNotifCollection = notifCollection;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mActivityStarter = activityStarter;
        this.mClickNotifier = notificationClickNotifier;
        this.mStatusBarStateController = statusBarStateController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mKeyguardManager = keyguardManager;
        this.mDreamManager = iDreamManager;
        this.mBubbleController = bubbleController;
        this.mAssistManagerLazy = lazy;
        this.mRemoteInputManager = notificationRemoteInputManager;
        this.mGroupManager = notificationGroupManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mShadeController = shadeController;
        this.mKeyguardStateController = keyguardStateController;
        this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
        this.mLockPatternUtils = lockPatternUtils;
        this.mStatusBarRemoteInputCallback = statusBarRemoteInputCallback;
        this.mActivityIntentHelper = activityIntentHelper;
        this.mFeatureFlags = featureFlags;
        this.mMetricsLogger = metricsLogger;
        this.mLogger = statusBarNotificationActivityStarterLogger;
        this.mStatusBar = statusBar;
        this.mPresenter = notificationPresenter;
        this.mNotificationPanel = notificationPanelViewController;
        this.mActivityLaunchAnimator = activityLaunchAnimator;
        if (!featureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
                public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        } else {
            this.mNotifPipeline.addCollectionListener(new NotifCollectionListener() {
                public void onEntryAdded(NotificationEntry notificationEntry) {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        }
    }

    public void onNotificationClicked(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow) {
        PendingIntent pendingIntent;
        this.mLogger.logStartingActivityFromClick(statusBarNotification.getKey());
        RemoteInputController controller = this.mRemoteInputManager.getController();
        if (!controller.isRemoteInputActive(expandableNotificationRow.getEntry()) || TextUtils.isEmpty(expandableNotificationRow.getActiveRemoteInputText())) {
            Notification notification = statusBarNotification.getNotification();
            PendingIntent pendingIntent2 = notification.contentIntent;
            if (pendingIntent2 != null) {
                pendingIntent = pendingIntent2;
            } else {
                pendingIntent = notification.fullScreenIntent;
            }
            boolean isBubble = expandableNotificationRow.getEntry().isBubble();
            if (pendingIntent != null || isBubble) {
                boolean z = pendingIntent != null && pendingIntent.isActivity() && !isBubble;
                boolean z2 = z && this.mActivityIntentHelper.wouldLaunchResolverActivity(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId());
                boolean isOccluded = this.mStatusBar.isOccluded();
                boolean z3 = this.mKeyguardStateController.isShowing() && pendingIntent != null && this.mActivityIntentHelper.wouldShowOverLockscreen(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId());
                $$Lambda$StatusBarNotificationActivityStarter$Pyeef5xkti2nTtS5zKZgWAnZicA r1 = new ActivityStarter.OnDismissAction(statusBarNotification, expandableNotificationRow, controller, pendingIntent, z, isOccluded, z3) {
                    public final /* synthetic */ StatusBarNotification f$1;
                    public final /* synthetic */ ExpandableNotificationRow f$2;
                    public final /* synthetic */ RemoteInputController f$3;
                    public final /* synthetic */ PendingIntent f$4;
                    public final /* synthetic */ boolean f$5;
                    public final /* synthetic */ boolean f$6;
                    public final /* synthetic */ boolean f$7;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                        this.f$6 = r7;
                        this.f$7 = r8;
                    }

                    public final boolean onDismiss() {
                        return StatusBarNotificationActivityStarter.this.lambda$onNotificationClicked$0$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
                    }
                };
                if (z3) {
                    this.mIsCollapsingToShowActivityOverLockscreen = true;
                    r1.onDismiss();
                    return;
                }
                this.mActivityStarter.dismissKeyguardThenExecute(r1, (Runnable) null, z2);
                return;
            }
            this.mLogger.logNonClickableNotification(statusBarNotification.getKey());
            return;
        }
        controller.closeRemoteInputs();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x002b, code lost:
        if (shouldAutoCancel(r0.getSbn()) != false) goto L_0x002f;
     */
    /* renamed from: handleNotificationClickAfterKeyguardDismissed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification r12, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r13, com.android.systemui.statusbar.RemoteInputController r14, android.app.PendingIntent r15, boolean r16, boolean r17, boolean r18) {
        /*
            r11 = this;
            r9 = r11
            r2 = r12
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r0 = r9.mLogger
            java.lang.String r1 = r12.getKey()
            r0.logHandleClickAfterKeyguardDismissed(r1)
            r3 = r13
            r11.removeHUN(r13)
            boolean r0 = shouldAutoCancel(r12)
            if (r0 == 0) goto L_0x002e
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r9.mGroupManager
            boolean r0 = r0.isOnlyChildInGroup(r12)
            if (r0 == 0) goto L_0x002e
            com.android.systemui.statusbar.phone.NotificationGroupManager r0 = r9.mGroupManager
            com.android.systemui.statusbar.notification.collection.NotificationEntry r0 = r0.getLogicalGroupSummary(r12)
            com.android.systemui.statusbar.notification.ExpandedNotification r1 = r0.getSbn()
            boolean r1 = shouldAutoCancel(r1)
            if (r1 == 0) goto L_0x002e
            goto L_0x002f
        L_0x002e:
            r0 = 0
        L_0x002f:
            r8 = r0
            com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7mfSGy2G6exE-3cGRoA3iww8GIU r10 = new com.android.systemui.statusbar.phone.-$$Lambda$StatusBarNotificationActivityStarter$7mfSGy2G6exE-3cGRoA3iww8GIU
            r0 = r10
            r1 = r11
            r2 = r12
            r3 = r13
            r4 = r14
            r5 = r15
            r6 = r16
            r7 = r17
            r0.<init>(r2, r3, r4, r5, r6, r7, r8)
            r0 = 1
            if (r18 == 0) goto L_0x004d
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.addPostCollapseAction(r10)
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.collapsePanel(r0)
            goto L_0x006d
        L_0x004d:
            com.android.systemui.statusbar.policy.KeyguardStateController r1 = r9.mKeyguardStateController
            boolean r1 = r1.isShowing()
            if (r1 == 0) goto L_0x0068
            com.android.systemui.statusbar.phone.StatusBar r1 = r9.mStatusBar
            boolean r1 = r1.isOccluded()
            if (r1 == 0) goto L_0x0068
            com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager r1 = r9.mStatusBarKeyguardViewManager
            r1.addAfterKeyguardGoneRunnable(r10)
            com.android.systemui.statusbar.phone.ShadeController r1 = r9.mShadeController
            r1.collapsePanel()
            goto L_0x006d
        L_0x0068:
            android.os.Handler r1 = r9.mBackgroundHandler
            r1.postAtFrontOfQueue(r10)
        L_0x006d:
            com.android.systemui.statusbar.phone.NotificationPanelViewController r1 = r9.mNotificationPanel
            boolean r1 = r1.isFullyCollapsed()
            r0 = r0 ^ r1
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.RemoteInputController, android.app.PendingIntent, boolean, boolean, boolean):boolean");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0077  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00a1  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00c0  */
    /* renamed from: handleNotificationClickAfterPanelCollapsed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void lambda$handleNotificationClickAfterKeyguardDismissed$1(android.service.notification.StatusBarNotification r13, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r14, com.android.systemui.statusbar.RemoteInputController r15, android.app.PendingIntent r16, boolean r17, boolean r18, com.android.systemui.statusbar.notification.collection.NotificationEntry r19) {
        /*
            r12 = this;
            r7 = r12
            r8 = r19
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r0 = r7.mLogger
            java.lang.String r1 = r13.getKey()
            r0.logHandleClickAfterPanelCollapsed(r1)
            java.lang.String r9 = r13.getKey()
            android.app.IActivityManager r0 = android.app.ActivityManager.getService()     // Catch:{ RemoteException -> 0x0017 }
            r0.resumeAppSwitches()     // Catch:{ RemoteException -> 0x0017 }
        L_0x0017:
            if (r17 == 0) goto L_0x0041
            android.os.UserHandle r0 = r16.getCreatorUserHandle()
            int r0 = r0.getIdentifier()
            com.android.internal.widget.LockPatternUtils r1 = r7.mLockPatternUtils
            boolean r1 = r1.isSeparateProfileChallengeEnabled(r0)
            if (r1 == 0) goto L_0x0041
            android.app.KeyguardManager r1 = r7.mKeyguardManager
            boolean r1 = r1.isDeviceLocked(r0)
            if (r1 == 0) goto L_0x0041
            com.android.systemui.statusbar.phone.StatusBarRemoteInputCallback r1 = r7.mStatusBarRemoteInputCallback
            android.content.IntentSender r2 = r16.getIntentSender()
            boolean r0 = r1.startWorkChallengeIfNecessary(r0, r2, r9)
            if (r0 == 0) goto L_0x0041
            r12.collapseOnMainThread()
            return
        L_0x0041:
            com.android.systemui.statusbar.notification.collection.NotificationEntry r10 = r14.getEntry()
            java.lang.CharSequence r0 = r10.remoteInputText
            boolean r0 = android.text.TextUtils.isEmpty(r0)
            r1 = 0
            if (r0 != 0) goto L_0x0051
            java.lang.CharSequence r0 = r10.remoteInputText
            goto L_0x0052
        L_0x0051:
            r0 = r1
        L_0x0052:
            boolean r2 = android.text.TextUtils.isEmpty(r0)
            if (r2 != 0) goto L_0x0070
            r2 = r15
            boolean r2 = r15.isSpinning(r9)
            if (r2 != 0) goto L_0x0070
            android.content.Intent r1 = new android.content.Intent
            r1.<init>()
            java.lang.String r0 = r0.toString()
            java.lang.String r2 = "android.remoteInputDraft"
            android.content.Intent r0 = r1.putExtra(r2, r0)
            r2 = r0
            goto L_0x0071
        L_0x0070:
            r2 = r1
        L_0x0071:
            boolean r11 = r10.canBubble()
            if (r11 == 0) goto L_0x0080
            com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarterLogger r0 = r7.mLogger
            r0.logExpandingBubble(r9)
            r12.expandBubbleStackOnMainThread(r10)
            goto L_0x008c
        L_0x0080:
            r0 = r12
            r1 = r16
            r3 = r10
            r4 = r14
            r5 = r18
            r6 = r17
            r0.startNotificationIntent(r1, r2, r3, r4, r5, r6)
        L_0x008c:
            if (r17 != 0) goto L_0x0090
            if (r11 == 0) goto L_0x009b
        L_0x0090:
            dagger.Lazy<com.android.systemui.assist.AssistManager> r0 = r7.mAssistManagerLazy
            java.lang.Object r0 = r0.get()
            com.android.systemui.assist.AssistManager r0 = (com.android.systemui.assist.AssistManager) r0
            r0.hideAssist()
        L_0x009b:
            boolean r0 = r12.shouldCollapse()
            if (r0 == 0) goto L_0x00a4
            r12.collapseOnMainThread()
        L_0x00a4:
            int r0 = r12.getVisibleNotificationsCount()
            android.service.notification.NotificationListenerService$Ranking r1 = r10.getRanking()
            int r1 = r1.getRank()
            com.android.internal.statusbar.NotificationVisibility$NotificationLocation r2 = com.android.systemui.statusbar.notification.logging.NotificationLogger.getNotificationLocation(r10)
            r3 = 1
            com.android.internal.statusbar.NotificationVisibility r0 = com.android.internal.statusbar.NotificationVisibility.obtain(r9, r1, r0, r3, r2)
            com.android.systemui.statusbar.NotificationClickNotifier r1 = r7.mClickNotifier
            r1.onNotificationClick(r9, r0)
            if (r11 != 0) goto L_0x00da
            if (r8 == 0) goto L_0x00c5
            r12.removeNotification(r8)
        L_0x00c5:
            boolean r0 = shouldAutoCancel(r13)
            if (r0 != 0) goto L_0x00d3
            com.android.systemui.statusbar.NotificationRemoteInputManager r0 = r7.mRemoteInputManager
            boolean r0 = r0.isNotificationKeptForRemoteInputHistory(r9)
            if (r0 == 0) goto L_0x00da
        L_0x00d3:
            com.android.systemui.statusbar.notification.collection.NotificationEntry r0 = r14.getEntry()
            r12.removeNotification(r0)
        L_0x00da:
            r0 = 0
            r7.mIsCollapsingToShowActivityOverLockscreen = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.lambda$handleNotificationClickAfterKeyguardDismissed$1(android.service.notification.StatusBarNotification, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.RemoteInputController, android.app.PendingIntent, boolean, boolean, com.android.systemui.statusbar.notification.collection.NotificationEntry):void");
    }

    private void expandBubbleStackOnMainThread(NotificationEntry notificationEntry) {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
        } else {
            this.mMainThreadHandler.post(new Runnable(notificationEntry) {
                public final /* synthetic */ NotificationEntry f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    StatusBarNotificationActivityStarter.this.lambda$expandBubbleStackOnMainThread$2$StatusBarNotificationActivityStarter(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$expandBubbleStackOnMainThread$2 */
    public /* synthetic */ void lambda$expandBubbleStackOnMainThread$2$StatusBarNotificationActivityStarter(NotificationEntry notificationEntry) {
        this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
    }

    private void startNotificationIntent(PendingIntent pendingIntent, Intent intent, NotificationEntry notificationEntry, View view, boolean z, boolean z2) {
        RemoteAnimationAdapter launchAnimation = this.mActivityLaunchAnimator.getLaunchAnimation(view, z);
        this.mLogger.logStartNotificationIntent(notificationEntry.getKey(), pendingIntent);
        if (launchAnimation != null) {
            try {
                ActivityTaskManager.getService().registerRemoteAnimationForNextActivityStart(pendingIntent.getCreatorPackage(), launchAnimation);
            } catch (PendingIntent.CanceledException | RemoteException e) {
                this.mLogger.logSendingIntentFailed(e);
                return;
            }
        }
        this.mMainThreadHandler.post(new Runnable(pendingIntent.sendAndReturnResult(this.mContext, 0, intent, (PendingIntent.OnFinished) null, (Handler) null, (String) null, StatusBar.getActivityOptions(launchAnimation)), z2) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startNotificationIntent$3$StatusBarNotificationActivityStarter(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationIntent$3 */
    public /* synthetic */ void lambda$startNotificationIntent$3$StatusBarNotificationActivityStarter(int i, boolean z) {
        this.mActivityLaunchAnimator.setLaunchResult(i, z);
    }

    public void startNotificationGutsIntent(Intent intent, int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(intent, expandableNotificationRow, i) {
            public final /* synthetic */ Intent f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final boolean onDismiss() {
                return StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$7$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3);
            }
        }, (Runnable) null, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$7 */
    public /* synthetic */ boolean lambda$startNotificationGutsIntent$7$StatusBarNotificationActivityStarter(Intent intent, ExpandableNotificationRow expandableNotificationRow, int i) {
        AsyncTask.execute(new Runnable(intent, expandableNotificationRow, i) {
            public final /* synthetic */ Intent f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$6$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$6 */
    public /* synthetic */ void lambda$startNotificationGutsIntent$6$StatusBarNotificationActivityStarter(Intent intent, ExpandableNotificationRow expandableNotificationRow, int i) {
        this.mMainThreadHandler.post(new Runnable(TaskStackBuilder.create(this.mContext).addNextIntentWithParentStack(intent).startActivities(StatusBar.getActivityOptions(this.mActivityLaunchAnimator.getLaunchAnimation(expandableNotificationRow, this.mStatusBar.isOccluded())), new UserHandle(UserHandle.getUserId(i))), expandableNotificationRow) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$4$StatusBarNotificationActivityStarter(this.f$1, this.f$2);
            }
        });
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() {
                public final void run() {
                    StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$5$StatusBarNotificationActivityStarter();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$4 */
    public /* synthetic */ void lambda$startNotificationGutsIntent$4$StatusBarNotificationActivityStarter(int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityLaunchAnimator.setLaunchResult(i, true);
        removeHUN(expandableNotificationRow);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$5 */
    public /* synthetic */ void lambda$startNotificationGutsIntent$5$StatusBarNotificationActivityStarter() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }

    public void startHistoryIntent(boolean z) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final boolean onDismiss() {
                return StatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$10$StatusBarNotificationActivityStarter(this.f$1);
            }
        }, (Runnable) null, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$10 */
    public /* synthetic */ boolean lambda$startHistoryIntent$10$StatusBarNotificationActivityStarter(boolean z) {
        AsyncTask.execute(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$9$StatusBarNotificationActivityStarter(this.f$1);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$9 */
    public /* synthetic */ void lambda$startHistoryIntent$9$StatusBarNotificationActivityStarter(boolean z) {
        Intent intent;
        if (z) {
            intent = new Intent("android.settings.NOTIFICATION_HISTORY");
        } else {
            intent = new Intent("android.settings.NOTIFICATION_SETTINGS");
        }
        TaskStackBuilder addNextIntent = TaskStackBuilder.create(this.mContext).addNextIntent(new Intent("android.settings.NOTIFICATION_SETTINGS"));
        if (z) {
            addNextIntent.addNextIntent(intent);
        }
        addNextIntent.startActivities((Bundle) null, UserHandle.CURRENT);
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() {
                public final void run() {
                    StatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$8$StatusBarNotificationActivityStarter();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$8 */
    public /* synthetic */ void lambda$startHistoryIntent$8$StatusBarNotificationActivityStarter() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }

    private void removeHUN(ExpandableNotificationRow expandableNotificationRow) {
        String key = expandableNotificationRow.getEntry().getSbn().getKey();
        HeadsUpManagerPhone headsUpManagerPhone = this.mHeadsUpManager;
        if (headsUpManagerPhone != null && headsUpManagerPhone.isAlerting(key)) {
            if (this.mPresenter.isPresenterFullyCollapsed()) {
                HeadsUpUtil.setIsClickedHeadsUpNotification(expandableNotificationRow, true);
            }
            this.mHeadsUpManager.removeNotification(key, true);
        }
    }

    /* access modifiers changed from: private */
    public void handleFullScreenIntent(NotificationEntry notificationEntry) {
        if (!this.mNotificationInterruptStateProvider.shouldLaunchFullScreenIntentWhenAdded(notificationEntry)) {
            return;
        }
        if (shouldSuppressFullScreenIntent(notificationEntry)) {
            this.mLogger.logFullScreenIntentSuppressedByDnD(notificationEntry.getKey());
        } else if (notificationEntry.getImportance() < 4) {
            this.mLogger.logFullScreenIntentNotImportantEnough(notificationEntry.getKey());
        } else {
            if (NotificationUtil.isInCallNotification(notificationEntry.getSbn())) {
                this.mUiBgExecutor.execute(new Runnable() {
                    public final void run() {
                        StatusBarNotificationActivityStarter.this.lambda$handleFullScreenIntent$11$StatusBarNotificationActivityStarter();
                    }
                });
            }
            PendingIntent pendingIntent = notificationEntry.getSbn().getNotification().fullScreenIntent;
            this.mLogger.logSendingFullScreenIntent(notificationEntry.getKey(), pendingIntent);
            try {
                EventLog.writeEvent(36002, notificationEntry.getKey());
                pendingIntent.send();
                notificationEntry.notifyFullScreenIntentLaunched();
                this.mMetricsLogger.count("note_fullscreen", 1);
            } catch (PendingIntent.CanceledException unused) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleFullScreenIntent$11 */
    public /* synthetic */ void lambda$handleFullScreenIntent$11$StatusBarNotificationActivityStarter() {
        try {
            this.mDreamManager.awaken();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isCollapsingToShowActivityOverLockscreen() {
        return this.mIsCollapsingToShowActivityOverLockscreen;
    }

    private static boolean shouldAutoCancel(StatusBarNotification statusBarNotification) {
        int i = statusBarNotification.getNotification().flags;
        return (i & 16) == 16 && (i & 64) == 0;
    }

    private void collapseOnMainThread() {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mShadeController.collapsePanel();
            return;
        }
        Handler handler = this.mMainThreadHandler;
        ShadeController shadeController = this.mShadeController;
        Objects.requireNonNull(shadeController);
        handler.post(new Runnable() {
            public final void run() {
                ShadeController.this.collapsePanel();
            }
        });
    }

    private boolean shouldCollapse() {
        return this.mStatusBarStateController.getState() != 0 || !this.mActivityLaunchAnimator.isAnimationPending();
    }

    private boolean shouldSuppressFullScreenIntent(NotificationEntry notificationEntry) {
        if (this.mPresenter.isDeviceInVrMode()) {
            return true;
        }
        return notificationEntry.shouldSuppressFullScreenIntent();
    }

    private void removeNotification(NotificationEntry notificationEntry) {
        this.mMainThreadHandler.post(new Runnable(notificationEntry) {
            public final /* synthetic */ NotificationEntry f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBarNotificationActivityStarter.this.lambda$removeNotification$12$StatusBarNotificationActivityStarter(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeNotification$12 */
    public /* synthetic */ void lambda$removeNotification$12$StatusBarNotificationActivityStarter(NotificationEntry notificationEntry) {
        Runnable createRemoveRunnable = createRemoveRunnable(notificationEntry);
        if (this.mPresenter.isCollapsing()) {
            this.mShadeController.addPostCollapseAction(createRemoveRunnable);
        } else {
            createRemoveRunnable.run();
        }
    }

    private int getVisibleNotificationsCount() {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return this.mNotifPipeline.getShadeListCount();
        }
        return this.mEntryManager.getActiveNotificationsCount();
    }

    private Runnable createRemoveRunnable(final NotificationEntry notificationEntry) {
        return this.mFeatureFlags.isNewNotifPipelineRenderingEnabled() ? new Runnable() {
            public void run() {
                int i;
                if (StatusBarNotificationActivityStarter.this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                    i = 1;
                } else {
                    i = StatusBarNotificationActivityStarter.this.mNotificationPanel.hasPulsingNotifications() ? 2 : 3;
                }
                NotifCollection access$400 = StatusBarNotificationActivityStarter.this.mNotifCollection;
                NotificationEntry notificationEntry = notificationEntry;
                access$400.dismissNotification(notificationEntry, new DismissedByUserStats(i, 1, NotificationVisibility.obtain(notificationEntry.getKey(), notificationEntry.getRanking().getRank(), StatusBarNotificationActivityStarter.this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry))));
            }
        } : new Runnable() {
            public void run() {
                StatusBarNotificationActivityStarter.this.mEntryManager.performRemoveNotification(notificationEntry.getSbn(), 1);
            }
        };
    }

    public static class Builder {
        private final ActivityIntentHelper mActivityIntentHelper;
        private ActivityLaunchAnimator mActivityLaunchAnimator;
        private final ActivityStarter mActivityStarter;
        private final Lazy<AssistManager> mAssistManagerLazy;
        private final Handler mBackgroundHandler;
        private final BubbleController mBubbleController;
        private final NotificationClickNotifier mClickNotifier;
        private final CommandQueue mCommandQueue;
        private final Context mContext;
        private final IDreamManager mDreamManager;
        private final NotificationEntryManager mEntryManager;
        private final FeatureFlags mFeatureFlags;
        private final NotificationGroupManager mGroupManager;
        private final HeadsUpManagerPhone mHeadsUpManager;
        private final KeyguardManager mKeyguardManager;
        private final KeyguardStateController mKeyguardStateController;
        private final LockPatternUtils mLockPatternUtils;
        private final NotificationLockscreenUserManager mLockscreenUserManager;
        private final StatusBarNotificationActivityStarterLogger mLogger;
        private final Handler mMainThreadHandler;
        private final MetricsLogger mMetricsLogger;
        private final NotifCollection mNotifCollection;
        private final NotifPipeline mNotifPipeline;
        private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
        private NotificationPanelViewController mNotificationPanelViewController;
        private NotificationPresenter mNotificationPresenter;
        private final StatusBarRemoteInputCallback mRemoteInputCallback;
        private final NotificationRemoteInputManager mRemoteInputManager;
        private final ShadeController mShadeController;
        private StatusBar mStatusBar;
        private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
        private final StatusBarStateController mStatusBarStateController;
        private final Executor mUiBgExecutor;

        public Builder(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger) {
            this.mContext = context;
            this.mCommandQueue = commandQueue;
            this.mMainThreadHandler = handler;
            this.mBackgroundHandler = handler2;
            this.mUiBgExecutor = executor;
            this.mEntryManager = notificationEntryManager;
            this.mNotifPipeline = notifPipeline;
            this.mNotifCollection = notifCollection;
            this.mHeadsUpManager = headsUpManagerPhone;
            this.mActivityStarter = activityStarter;
            this.mClickNotifier = notificationClickNotifier;
            this.mStatusBarStateController = statusBarStateController;
            this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
            this.mKeyguardManager = keyguardManager;
            this.mDreamManager = iDreamManager;
            this.mBubbleController = bubbleController;
            this.mAssistManagerLazy = lazy;
            this.mRemoteInputManager = notificationRemoteInputManager;
            this.mGroupManager = notificationGroupManager;
            this.mLockscreenUserManager = notificationLockscreenUserManager;
            this.mShadeController = shadeController;
            this.mKeyguardStateController = keyguardStateController;
            this.mNotificationInterruptStateProvider = notificationInterruptStateProvider;
            this.mLockPatternUtils = lockPatternUtils;
            this.mRemoteInputCallback = statusBarRemoteInputCallback;
            this.mActivityIntentHelper = activityIntentHelper;
            this.mFeatureFlags = featureFlags;
            this.mMetricsLogger = metricsLogger;
            this.mLogger = statusBarNotificationActivityStarterLogger;
        }

        public Builder setStatusBar(StatusBar statusBar) {
            this.mStatusBar = statusBar;
            return this;
        }

        public Builder setNotificationPresenter(NotificationPresenter notificationPresenter) {
            this.mNotificationPresenter = notificationPresenter;
            return this;
        }

        public Builder setActivityLaunchAnimator(ActivityLaunchAnimator activityLaunchAnimator) {
            this.mActivityLaunchAnimator = activityLaunchAnimator;
            return this;
        }

        public Builder setNotificationPanelViewController(NotificationPanelViewController notificationPanelViewController) {
            this.mNotificationPanelViewController = notificationPanelViewController;
            return this;
        }

        public StatusBarNotificationActivityStarter build() {
            return new StatusBarNotificationActivityStarter(this.mContext, this.mCommandQueue, this.mMainThreadHandler, this.mBackgroundHandler, this.mUiBgExecutor, this.mEntryManager, this.mNotifPipeline, this.mNotifCollection, this.mHeadsUpManager, this.mActivityStarter, this.mClickNotifier, this.mStatusBarStateController, this.mStatusBarKeyguardViewManager, this.mKeyguardManager, this.mDreamManager, this.mBubbleController, this.mAssistManagerLazy, this.mRemoteInputManager, this.mGroupManager, this.mLockscreenUserManager, this.mShadeController, this.mKeyguardStateController, this.mNotificationInterruptStateProvider, this.mLockPatternUtils, this.mRemoteInputCallback, this.mActivityIntentHelper, this.mFeatureFlags, this.mMetricsLogger, this.mLogger, this.mStatusBar, this.mNotificationPresenter, this.mNotificationPanelViewController, this.mActivityLaunchAnimator);
        }
    }
}
