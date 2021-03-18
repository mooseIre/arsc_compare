package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.View;
import android.view.ViewParent;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.ActionClickLogger;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.Objects;

public class StatusBarRemoteInputCallback implements NotificationRemoteInputManager.Callback, CommandQueue.Callbacks, StatusBarStateController.StateListener {
    private final ActionClickLogger mActionClickLogger;
    private final ActivityIntentHelper mActivityIntentHelper;
    private final ActivityStarter mActivityStarter;
    protected BroadcastReceiver mChallengeReceiver = new ChallengeReceiver();
    private final CommandQueue mCommandQueue;
    private final Context mContext;
    private int mDisabled2;
    private final NotificationGroupManager mGroupManager;
    private KeyguardManager mKeyguardManager;
    private final KeyguardStateController mKeyguardStateController;
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private Handler mMainHandler = new Handler();
    private View mPendingRemoteInputView;
    private View mPendingWorkRemoteInputView;
    private final ShadeController mShadeController;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final SysuiStatusBarStateController mStatusBarStateController;

    public StatusBarRemoteInputCallback(Context context, NotificationGroupManager notificationGroupManager, NotificationLockscreenUserManager notificationLockscreenUserManager, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, ActivityStarter activityStarter, ShadeController shadeController, CommandQueue commandQueue, ActionClickLogger actionClickLogger) {
        this.mContext = context;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mShadeController = shadeController;
        context.registerReceiverAsUser(this.mChallengeReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.DEVICE_LOCKED_CHANGED"), null, null);
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mKeyguardStateController = keyguardStateController;
        SysuiStatusBarStateController sysuiStatusBarStateController = (SysuiStatusBarStateController) statusBarStateController;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mActivityStarter = activityStarter;
        sysuiStatusBarStateController.addCallback(this);
        this.mKeyguardManager = (KeyguardManager) context.getSystemService(KeyguardManager.class);
        this.mCommandQueue = commandQueue;
        commandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mActionClickLogger = actionClickLogger;
        this.mActivityIntentHelper = new ActivityIntentHelper(this.mContext);
        this.mGroupManager = notificationGroupManager;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        boolean z = this.mPendingRemoteInputView != null;
        if (i != 0) {
            return;
        }
        if ((this.mStatusBarStateController.leaveOpenOnKeyguardHide() || z) && !this.mStatusBarStateController.isKeyguardRequested()) {
            if (z) {
                Handler handler = this.mMainHandler;
                View view = this.mPendingRemoteInputView;
                Objects.requireNonNull(view);
                handler.post(new Runnable(view) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$au9TYywfgPbmO65RQz_jg33Qz0 */
                    public final /* synthetic */ View f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        this.f$0.callOnClick();
                    }
                });
            }
            this.mPendingRemoteInputView = null;
        }
    }

    @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.Callback
    public void onLockedRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view) {
        if (!expandableNotificationRow.isPinned()) {
            this.mStatusBarStateController.setLeaveOpenOnKeyguardHide(true);
        }
        this.mStatusBarKeyguardViewManager.showBouncer(true);
        this.mPendingRemoteInputView = view;
    }

    /* access modifiers changed from: protected */
    public void onWorkChallengeChanged() {
        this.mLockscreenUserManager.updatePublicMode();
        if (this.mPendingWorkRemoteInputView != null && !this.mLockscreenUserManager.isAnyProfilePublicMode()) {
            this.mShadeController.postOnShadeExpanded(new Runnable() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarRemoteInputCallback$R1k7Wh1xlxjAMn9HjU1lr6mXXE */

                public final void run() {
                    StatusBarRemoteInputCallback.this.lambda$onWorkChallengeChanged$2$StatusBarRemoteInputCallback();
                }
            });
            this.mShadeController.instantExpandNotificationsPanel();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWorkChallengeChanged$2 */
    public /* synthetic */ void lambda$onWorkChallengeChanged$2$StatusBarRemoteInputCallback() {
        View view = this.mPendingWorkRemoteInputView;
        if (view != null) {
            ViewParent parent = view.getParent();
            while (!(parent instanceof ExpandableNotificationRow)) {
                if (parent != null) {
                    parent = parent.getParent();
                } else {
                    return;
                }
            }
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) parent;
            ViewParent parent2 = expandableNotificationRow.getParent();
            if (parent2 instanceof NotificationStackScrollLayout) {
                expandableNotificationRow.makeActionsVisibile();
                expandableNotificationRow.post(new Runnable((NotificationStackScrollLayout) parent2, expandableNotificationRow) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarRemoteInputCallback$L_R5DgtrNavZQt2DnmfrB_93PMA */
                    public final /* synthetic */ NotificationStackScrollLayout f$1;
                    public final /* synthetic */ ExpandableNotificationRow f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        StatusBarRemoteInputCallback.this.lambda$onWorkChallengeChanged$1$StatusBarRemoteInputCallback(this.f$1, this.f$2);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWorkChallengeChanged$1 */
    public /* synthetic */ void lambda$onWorkChallengeChanged$1$StatusBarRemoteInputCallback(NotificationStackScrollLayout notificationStackScrollLayout, ExpandableNotificationRow expandableNotificationRow) {
        $$Lambda$StatusBarRemoteInputCallback$Pf9b4xR3WdydZqpSHpd3WHttUBw r0 = new Runnable(notificationStackScrollLayout) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarRemoteInputCallback$Pf9b4xR3WdydZqpSHpd3WHttUBw */
            public final /* synthetic */ NotificationStackScrollLayout f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                StatusBarRemoteInputCallback.this.lambda$onWorkChallengeChanged$0$StatusBarRemoteInputCallback(this.f$1);
            }
        };
        if (notificationStackScrollLayout.scrollTo(expandableNotificationRow)) {
            notificationStackScrollLayout.setFinishScrollingCallback(r0);
        } else {
            r0.run();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onWorkChallengeChanged$0 */
    public /* synthetic */ void lambda$onWorkChallengeChanged$0$StatusBarRemoteInputCallback(NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mPendingWorkRemoteInputView.callOnClick();
        this.mPendingWorkRemoteInputView = null;
        notificationStackScrollLayout.setFinishScrollingCallback(null);
    }

    @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.Callback
    public void onMakeExpandedVisibleForRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view) {
        if (this.mKeyguardStateController.isShowing()) {
            onLockedRemoteInput(expandableNotificationRow, view);
            return;
        }
        if (expandableNotificationRow.isChildInGroup() && !expandableNotificationRow.areChildrenExpanded()) {
            this.mGroupManager.toggleGroupExpansion(expandableNotificationRow.getEntry().getSbn());
        }
        expandableNotificationRow.setUserExpanded(true);
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        Objects.requireNonNull(view);
        privateLayout.setOnExpandedVisibleListener(new Runnable(view) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$MVkYf3BuVxXy7rxrXvHR4SUXEU */
            public final /* synthetic */ View f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.performClick();
            }
        });
    }

    @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.Callback
    public void onLockedWorkRemoteInput(int i, ExpandableNotificationRow expandableNotificationRow, View view) {
        this.mCommandQueue.animateCollapsePanels();
        startWorkChallengeIfNecessary(i, null, null);
        this.mPendingWorkRemoteInputView = view;
    }

    /* access modifiers changed from: package-private */
    public boolean startWorkChallengeIfNecessary(int i, IntentSender intentSender, String str) {
        this.mPendingWorkRemoteInputView = null;
        Intent createConfirmDeviceCredentialIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, i);
        if (createConfirmDeviceCredentialIntent == null) {
            return false;
        }
        Intent intent = new Intent("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        intent.putExtra("android.intent.extra.INTENT", intentSender);
        intent.putExtra("android.intent.extra.INDEX", str);
        intent.setPackage(this.mContext.getPackageName());
        createConfirmDeviceCredentialIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getBroadcast(this.mContext, 0, intent, 1409286144).getIntentSender());
        try {
            ActivityManager.getService().startConfirmDeviceCredentialIntent(createConfirmDeviceCredentialIntent, (Bundle) null);
            return true;
        } catch (RemoteException unused) {
            return true;
        }
    }

    @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.Callback
    public boolean shouldHandleRemoteInput(View view, PendingIntent pendingIntent) {
        return (this.mDisabled2 & 4) != 0;
    }

    @Override // com.android.systemui.statusbar.NotificationRemoteInputManager.Callback
    public boolean handleRemoteViewClick(View view, PendingIntent pendingIntent, NotificationRemoteInputManager.ClickHandler clickHandler) {
        if (!pendingIntent.isActivity()) {
            return clickHandler.handleClick();
        }
        this.mActionClickLogger.logWaitingToCloseKeyguard(pendingIntent);
        this.mActivityStarter.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction(pendingIntent, clickHandler) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarRemoteInputCallback$n1nyYEfnirLBe7S11WLW5h0H_EI */
            public final /* synthetic */ PendingIntent f$1;
            public final /* synthetic */ NotificationRemoteInputManager.ClickHandler f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
            public final boolean onDismiss() {
                return StatusBarRemoteInputCallback.this.lambda$handleRemoteViewClick$3$StatusBarRemoteInputCallback(this.f$1, this.f$2);
            }
        }, null, this.mActivityIntentHelper.wouldLaunchResolverActivity(pendingIntent.getIntent(), this.mLockscreenUserManager.getCurrentUserId()));
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleRemoteViewClick$3 */
    public /* synthetic */ boolean lambda$handleRemoteViewClick$3$StatusBarRemoteInputCallback(PendingIntent pendingIntent, NotificationRemoteInputManager.ClickHandler clickHandler) {
        this.mActionClickLogger.logKeyguardGone(pendingIntent);
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        return clickHandler.handleClick() && this.mShadeController.closeShadeIfOpen();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == this.mContext.getDisplayId()) {
            this.mDisabled2 = i3;
        }
    }

    protected class ChallengeReceiver extends BroadcastReceiver {
        protected ChallengeReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            if ("android.intent.action.DEVICE_LOCKED_CHANGED".equals(action) && intExtra != StatusBarRemoteInputCallback.this.mLockscreenUserManager.getCurrentUserId() && StatusBarRemoteInputCallback.this.mLockscreenUserManager.isCurrentProfile(intExtra)) {
                StatusBarRemoteInputCallback.this.onWorkChallengeChanged();
            }
        }
    }
}
