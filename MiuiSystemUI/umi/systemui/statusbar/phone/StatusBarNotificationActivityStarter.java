package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
    protected final ActivityStarter mActivityStarter;
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final Handler mBackgroundHandler;
    private final BubbleController mBubbleController;
    private final NotificationClickNotifier mClickNotifier;
    protected final CommandQueue mCommandQueue;
    protected final Context mContext;
    private final IDreamManager mDreamManager;
    private final NotificationEntryManager mEntryManager;
    private final FeatureFlags mFeatureFlags;
    private final NotificationGroupManager mGroupManager;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mIsCollapsingToShowActivityOverLockscreen;
    private final KeyguardManager mKeyguardManager;
    private final KeyguardStateController mKeyguardStateController;
    private final LockPatternUtils mLockPatternUtils;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    protected final StatusBarNotificationActivityStarterLogger mLogger;
    protected final Handler mMainThreadHandler;
    private final MetricsLogger mMetricsLogger;
    private final NotifCollection mNotifCollection;
    private final NotifPipeline mNotifPipeline;
    private final NotificationInterruptStateProvider mNotificationInterruptStateProvider;
    private final NotificationPanelViewController mNotificationPanel;
    private final NotificationPresenter mPresenter;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private final ShadeController mShadeController;
    private final StatusBar mStatusBar;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarRemoteInputCallback mStatusBarRemoteInputCallback;
    private final StatusBarStateController mStatusBarStateController;
    private final Executor mUiBgExecutor;

    protected StatusBarNotificationActivityStarter(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger, StatusBar statusBar, NotificationPresenter notificationPresenter, NotificationPanelViewController notificationPanelViewController, ActivityLaunchAnimator activityLaunchAnimator) {
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
                /* class com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.AnonymousClass1 */

                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        } else {
            this.mNotifPipeline.addCollectionListener(new NotifCollectionListener() {
                /* class com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.AnonymousClass2 */

                @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
                public void onEntryAdded(NotificationEntry notificationEntry) {
                    StatusBarNotificationActivityStarter.this.handleFullScreenIntent(notificationEntry);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
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
                $$Lambda$StatusBarNotificationActivityStarter$Pyeef5xkti2nTtS5zKZgWAnZicA r12 = new ActivityStarter.OnDismissAction(statusBarNotification, expandableNotificationRow, controller, pendingIntent, z, isOccluded, z3) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$Pyeef5xkti2nTtS5zKZgWAnZicA */
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

                    @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                    public final boolean onDismiss() {
                        return StatusBarNotificationActivityStarter.this.lambda$onNotificationClicked$0$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
                    }
                };
                if (z3) {
                    this.mIsCollapsingToShowActivityOverLockscreen = true;
                    r12.onDismiss();
                    return;
                }
                this.mActivityStarter.dismissKeyguardThenExecute(r12, null, z2);
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
    public boolean lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification r13, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r14, com.android.systemui.statusbar.RemoteInputController r15, android.app.PendingIntent r16, boolean r17, boolean r18, boolean r19) {
        /*
        // Method dump skipped, instructions count: 122
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.lambda$onNotificationClicked$0(android.service.notification.StatusBarNotification, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.RemoteInputController, android.app.PendingIntent, boolean, boolean, boolean):boolean");
    }

    /* access modifiers changed from: private */
    /* renamed from: handleNotificationClickAfterPanelCollapsed */
    public void lambda$handleNotificationClickAfterKeyguardDismissed$1(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, RemoteInputController remoteInputController, PendingIntent pendingIntent, boolean z, boolean z2, NotificationEntry notificationEntry, boolean z3) {
        this.mLogger.logHandleClickAfterPanelCollapsed(statusBarNotification.getKey());
        String key = statusBarNotification.getKey();
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        if (z) {
            int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
            if (this.mLockPatternUtils.isSeparateProfileChallengeEnabled(identifier) && this.mKeyguardManager.isDeviceLocked(identifier) && this.mStatusBarRemoteInputCallback.startWorkChallengeIfNecessary(identifier, pendingIntent.getIntentSender(), key)) {
                collapseOnMainThread();
                return;
            }
        }
        NotificationEntry entry = expandableNotificationRow.getEntry();
        CharSequence charSequence = !TextUtils.isEmpty(entry.remoteInputText) ? entry.remoteInputText : null;
        Intent putExtra = (TextUtils.isEmpty(charSequence) || remoteInputController.isSpinning(key)) ? null : new Intent().putExtra("android.remoteInputDraft", charSequence.toString());
        boolean canBubble = entry.canBubble();
        if (canBubble) {
            this.mLogger.logExpandingBubble(key);
            expandBubbleStackOnMainThread(entry);
        } else {
            startNotificationIntent(pendingIntent, putExtra, entry, expandableNotificationRow, z2, z, z3);
        }
        if (z || canBubble) {
            this.mAssistManagerLazy.get().hideAssist();
        }
        if (shouldCollapse()) {
            collapseOnMainThread();
        }
        this.mClickNotifier.onNotificationClick(key, NotificationVisibility.obtain(key, entry.getRanking().getRank(), getVisibleNotificationsCount(), true, NotificationLogger.getNotificationLocation(entry)));
        if (!canBubble) {
            if (notificationEntry != null) {
                removeNotification(notificationEntry);
            }
            if (shouldAutoCancel(statusBarNotification) || this.mRemoteInputManager.isNotificationKeptForRemoteInputHistory(key)) {
                removeNotification(expandableNotificationRow.getEntry());
            }
        }
        this.mIsCollapsingToShowActivityOverLockscreen = false;
    }

    private void expandBubbleStackOnMainThread(NotificationEntry notificationEntry) {
        if (Looper.getMainLooper().isCurrentThread()) {
            this.mBubbleController.expandStackAndSelectBubble(notificationEntry);
        } else {
            this.mMainThreadHandler.post(new Runnable(notificationEntry) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$_h_OdrtdsD1DAoz8Z6fGvw_e1JY */
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

    /* access modifiers changed from: protected */
    public void startNotificationIntent(PendingIntent pendingIntent, Intent intent, NotificationEntry notificationEntry, View view, boolean z, boolean z2, boolean z3) {
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
        this.mMainThreadHandler.post(new Runnable(pendingIntent.sendAndReturnResult(this.mContext, 0, intent, null, null, null, StatusBar.getActivityOptions(launchAnimation)), z2) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$Y8JbBTzeL9ap2ze1W8GEAmENrw */
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

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void startNotificationGutsIntent(Intent intent, int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(intent, expandableNotificationRow, i) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$isT7KSHgVoiV5FyhsRVwbFw5RM */
            public final /* synthetic */ Intent f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return StatusBarNotificationActivityStarter.this.lambda$startNotificationGutsIntent$7$StatusBarNotificationActivityStarter(this.f$1, this.f$2, this.f$3);
            }
        }, null, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startNotificationGutsIntent$7 */
    public /* synthetic */ boolean lambda$startNotificationGutsIntent$7$StatusBarNotificationActivityStarter(Intent intent, ExpandableNotificationRow expandableNotificationRow, int i) {
        AsyncTask.execute(new Runnable(intent, expandableNotificationRow, i) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$xP8kim0YLPcFXI52PtjDJXYHzZo */
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
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$ViMRehyVVsXvPyoP1D3vy85ebw0 */
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
                /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$hxUPIAWvV2tDwN2Innktj57KZU */

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
    /* access modifiers changed from: public */
    private void handleFullScreenIntent(NotificationEntry notificationEntry) {
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
                    /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$m3KhJtChxE56Qa7kgUUXb_Dvg */

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
                if (this.mStatusBarStateController.getState() == 0) {
                    this.mShadeController.collapsePanel();
                }
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

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
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
            /* class com.android.systemui.statusbar.phone.$$Lambda$XDmf1V0qHGBRkxV63RRNIpOXuQ */

            public final void run() {
                ShadeController.this.collapsePanel();
            }
        });
    }

    /* access modifiers changed from: protected */
    public boolean shouldCollapse() {
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
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarNotificationActivityStarter$dfG4musWipZgmv_OJpU2CnBV14 */
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
            /* class com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.AnonymousClass3 */

            public void run() {
                int i;
                if (StatusBarNotificationActivityStarter.this.mHeadsUpManager.isAlerting(notificationEntry.getKey())) {
                    i = 1;
                } else {
                    i = StatusBarNotificationActivityStarter.this.mNotificationPanel.hasPulsingNotifications() ? 2 : 3;
                }
                NotifCollection notifCollection = StatusBarNotificationActivityStarter.this.mNotifCollection;
                NotificationEntry notificationEntry = notificationEntry;
                notifCollection.dismissNotification(notificationEntry, new DismissedByUserStats(i, 1, NotificationVisibility.obtain(notificationEntry.getKey(), notificationEntry.getRanking().getRank(), StatusBarNotificationActivityStarter.this.mNotifPipeline.getShadeListCount(), true, NotificationLogger.getNotificationLocation(notificationEntry))));
            }
        } : new Runnable() {
            /* class com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter.AnonymousClass4 */

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

        public MiuiStatusBarNotificationActivityStarter build() {
            return new MiuiStatusBarNotificationActivityStarter(this.mContext, this.mCommandQueue, this.mMainThreadHandler, this.mBackgroundHandler, this.mUiBgExecutor, this.mEntryManager, this.mNotifPipeline, this.mNotifCollection, this.mHeadsUpManager, this.mActivityStarter, this.mClickNotifier, this.mStatusBarStateController, this.mStatusBarKeyguardViewManager, this.mKeyguardManager, this.mDreamManager, this.mBubbleController, this.mAssistManagerLazy, this.mRemoteInputManager, this.mGroupManager, this.mLockscreenUserManager, this.mShadeController, this.mKeyguardStateController, this.mNotificationInterruptStateProvider, this.mLockPatternUtils, this.mRemoteInputCallback, this.mActivityIntentHelper, this.mFeatureFlags, this.mMetricsLogger, this.mLogger, this.mStatusBar, this.mNotificationPresenter, this.mNotificationPanelViewController, this.mActivityLaunchAnimator);
        }
    }
}
