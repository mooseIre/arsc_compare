package com.android.systemui.statusbar.phone;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import android.service.dreams.IDreamManager;
import android.util.Log;
import android.util.MiuiMultiWindowUtils;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
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
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import dagger.Lazy;
import java.util.concurrent.Executor;

public class MiuiStatusBarNotificationActivityStarter extends StatusBarNotificationActivityStarter {
    public MiuiStatusBarNotificationActivityStarter(Context context, CommandQueue commandQueue, Handler handler, Handler handler2, Executor executor, NotificationEntryManager notificationEntryManager, NotifPipeline notifPipeline, NotifCollection notifCollection, HeadsUpManagerPhone headsUpManagerPhone, ActivityStarter activityStarter, NotificationClickNotifier notificationClickNotifier, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, KeyguardManager keyguardManager, IDreamManager iDreamManager, BubbleController bubbleController, Lazy<AssistManager> lazy, NotificationRemoteInputManager notificationRemoteInputManager, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, ShadeController shadeController, KeyguardStateController keyguardStateController, NotificationInterruptStateProvider notificationInterruptStateProvider, LockPatternUtils lockPatternUtils, StatusBarRemoteInputCallback statusBarRemoteInputCallback, ActivityIntentHelper activityIntentHelper, FeatureFlags featureFlags, MetricsLogger metricsLogger, StatusBarNotificationActivityStarterLogger statusBarNotificationActivityStarterLogger, StatusBar statusBar, NotificationPresenter notificationPresenter, NotificationPanelViewController notificationPanelViewController, ActivityLaunchAnimator activityLaunchAnimator) {
        super(context, commandQueue, handler, handler2, executor, notificationEntryManager, notifPipeline, notifCollection, headsUpManagerPhone, activityStarter, notificationClickNotifier, statusBarStateController, statusBarKeyguardViewManager, keyguardManager, iDreamManager, bubbleController, lazy, notificationRemoteInputManager, notificationGroupManager, notificationLockscreenUserManager, shadeController, keyguardStateController, notificationInterruptStateProvider, lockPatternUtils, statusBarRemoteInputCallback, activityIntentHelper, featureFlags, metricsLogger, statusBarNotificationActivityStarterLogger, statusBar, notificationPresenter, notificationPanelViewController, activityLaunchAnimator);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.StatusBarNotificationActivityStarter
    public void startNotificationIntent(PendingIntent pendingIntent, Intent intent, NotificationEntry notificationEntry, View view, boolean z, boolean z2) {
        ActivityOptions activityOptions = MiuiMultiWindowUtils.getActivityOptions(this.mContext, pendingIntent.getCreatorPackage());
        boolean z3 = activityOptions != null;
        Log.i("MiuiMultiWindowUtils", "startNotificationIntent: " + HeadsUpUtil.isClickedHeadsUpNotification(view));
        if (!HeadsUpUtil.isClickedHeadsUpNotification(view) || z3) {
            try {
                pendingIntent.send(this.mContext, 0, intent, null, null, null, activityOptions != null ? activityOptions.toBundle() : StatusBar.getActivityOptions(null));
                this.mLogger.logStartNotificationIntent(notificationEntry.getKey(), pendingIntent);
            } catch (PendingIntent.CanceledException e) {
                this.mLogger.logSendingIntentFailed(e);
            }
        } else {
            super.startNotificationIntent(pendingIntent, intent, notificationEntry, view, z, z2);
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationActivityStarter
    public void startHistoryIntent(boolean z) {
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiStatusBarNotificationActivityStarter$a_g4gvpeq50l4NdGw98GmMoHic */

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return MiuiStatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$2$MiuiStatusBarNotificationActivityStarter();
            }
        }, null, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$2 */
    public /* synthetic */ boolean lambda$startHistoryIntent$2$MiuiStatusBarNotificationActivityStarter() {
        AsyncTask.execute(new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiStatusBarNotificationActivityStarter$qacJoUvB8BJQHjYvoj9Rb4sgpus */

            public final void run() {
                MiuiStatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$1$MiuiStatusBarNotificationActivityStarter();
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$1 */
    public /* synthetic */ void lambda$startHistoryIntent$1$MiuiStatusBarNotificationActivityStarter() {
        TaskStackBuilder.create(this.mContext).addNextIntent(MiuiNotificationSectionsManager.Companion.intent4NotificationControlCenterSettings()).startActivities(null, UserHandle.CURRENT);
        if (shouldCollapse()) {
            this.mMainThreadHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiStatusBarNotificationActivityStarter$_hUcnxDSXis59CS060JgJuCbSc */

                public final void run() {
                    MiuiStatusBarNotificationActivityStarter.this.lambda$startHistoryIntent$0$MiuiStatusBarNotificationActivityStarter();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startHistoryIntent$0 */
    public /* synthetic */ void lambda$startHistoryIntent$0$MiuiStatusBarNotificationActivityStarter() {
        this.mCommandQueue.animateCollapsePanels(2, true);
    }
}
