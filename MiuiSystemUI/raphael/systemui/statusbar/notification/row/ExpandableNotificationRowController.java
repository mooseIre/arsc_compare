package com.android.systemui.statusbar.notification.row;

import android.app.PendingIntent;
import android.util.Log;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.util.time.SystemClock;
import java.util.Objects;

public class ExpandableNotificationRowController {
    private final ActivatableNotificationViewController mActivatableNotificationViewController;
    private final boolean mAllowLongPress;
    private final String mAppName;
    private final SystemClock mClock;
    private final ExpandableNotificationRow.ExpansionLogger mExpansionLogger = new ExpandableNotificationRow.ExpansionLogger() {
        /* class com.android.systemui.statusbar.notification.row.$$Lambda$ExpandableNotificationRowController$7PRoCjf2CPB0eC3liBvfR80zWU */

        @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.ExpansionLogger
        public final void logNotificationExpansion(String str, boolean z, boolean z2) {
            ExpandableNotificationRowController.this.logNotificationExpansion(str, z, z2);
        }
    };
    private final FalsingManager mFalsingManager;
    private final HeadsUpManager mHeadsUpManager;
    private final KeyguardBypassController mKeyguardBypassController;
    private final NotificationMediaManager mMediaManager;
    private final NotificationGroupManager mNotificationGroupManager;
    private final NotificationGutsManager mNotificationGutsManager;
    private final String mNotificationKey;
    private final NotificationLogger mNotificationLogger;
    private final ExpandableNotificationRow.OnAppOpsClickListener mOnAppOpsClickListener;
    private Runnable mOnDismissRunnable;
    private final ExpandableNotificationRow.OnExpandClickListener mOnExpandClickListener;
    private final PeopleNotificationIdentifier mPeopleNotificationIdentifier;
    private final PluginManager mPluginManager;
    private final RowContentBindStage mRowContentBindStage;
    private final StatusBarStateController mStatusBarStateController;
    private final ExpandableNotificationRow mView;

    static /* synthetic */ boolean lambda$init$0() {
        return false;
    }

    public ExpandableNotificationRowController(ExpandableNotificationRow expandableNotificationRow, ActivatableNotificationViewController activatableNotificationViewController, NotificationMediaManager notificationMediaManager, PluginManager pluginManager, SystemClock systemClock, String str, String str2, KeyguardBypassController keyguardBypassController, NotificationGroupManager notificationGroupManager, RowContentBindStage rowContentBindStage, NotificationLogger notificationLogger, HeadsUpManager headsUpManager, ExpandableNotificationRow.OnExpandClickListener onExpandClickListener, StatusBarStateController statusBarStateController, NotificationGutsManager notificationGutsManager, boolean z, Runnable runnable, FalsingManager falsingManager, PeopleNotificationIdentifier peopleNotificationIdentifier) {
        this.mView = expandableNotificationRow;
        this.mActivatableNotificationViewController = activatableNotificationViewController;
        this.mMediaManager = notificationMediaManager;
        this.mPluginManager = pluginManager;
        this.mClock = systemClock;
        this.mAppName = str;
        this.mNotificationKey = str2;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mNotificationGroupManager = notificationGroupManager;
        this.mRowContentBindStage = rowContentBindStage;
        this.mNotificationLogger = notificationLogger;
        this.mHeadsUpManager = headsUpManager;
        this.mOnExpandClickListener = onExpandClickListener;
        this.mStatusBarStateController = statusBarStateController;
        this.mNotificationGutsManager = notificationGutsManager;
        this.mOnDismissRunnable = runnable;
        Objects.requireNonNull(notificationGutsManager);
        this.mOnAppOpsClickListener = new ExpandableNotificationRow.OnAppOpsClickListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$oy9pBf4KjrW7ZRpgHkpOCIaDYlg */

            @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.OnAppOpsClickListener
            public final boolean onClick(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                return NotificationGutsManager.this.openGuts(view, i, i2, menuItem);
            }
        };
        this.mAllowLongPress = z;
        this.mFalsingManager = falsingManager;
        this.mPeopleNotificationIdentifier = peopleNotificationIdentifier;
    }

    public void init() {
        this.mActivatableNotificationViewController.init();
        this.mView.initialize(this.mAppName, this.mNotificationKey, this.mExpansionLogger, this.mKeyguardBypassController, this.mNotificationGroupManager, this.mHeadsUpManager, this.mRowContentBindStage, this.mOnExpandClickListener, this.mMediaManager, this.mOnAppOpsClickListener, this.mFalsingManager, this.mStatusBarStateController, this.mPeopleNotificationIdentifier);
        this.mView.setOnDismissRunnable(this.mOnDismissRunnable);
        this.mView.setDescendantFocusability(393216);
        if (this.mAllowLongPress) {
            this.mView.setLongPressListener(new ExpandableNotificationRow.LongPressListener() {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$ExpandableNotificationRowController$_ms1NM7u5Ae7j4XaboX7D03mges */

                @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.LongPressListener
                public final boolean onLongPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                    return ExpandableNotificationRowController.this.lambda$init$1$ExpandableNotificationRowController(view, i, i2, menuItem);
                }
            });
        }
        if (NotificationRemoteInputManager.ENABLE_REMOTE_INPUT) {
            this.mView.setDescendantFocusability(131072);
        }
        this.mView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            /* class com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController.AnonymousClass1 */

            public void onViewAttachedToWindow(View view) {
                ExpandableNotificationRowController.this.mView.getEntry().setInitializationTime(ExpandableNotificationRowController.this.mClock.elapsedRealtime());
                ExpandableNotificationRowController.this.mPluginManager.addPluginListener((PluginListener) ExpandableNotificationRowController.this.mView, NotificationMenuRowPlugin.class, false);
            }

            public void onViewDetachedFromWindow(View view) {
                ExpandableNotificationRowController.this.mPluginManager.removePluginListener(ExpandableNotificationRowController.this.mView);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$init$1 */
    public /* synthetic */ boolean lambda$init$1$ExpandableNotificationRowController(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        if (this.mView.isSummaryWithChildren()) {
            this.mView.expandNotification();
            return true;
        }
        StatusBar statusBar = (StatusBar) Dependency.get(StatusBar.class);
        boolean z = false;
        if (statusBar.isKeyguardShowing()) {
            statusBar.dismissKeyguardThenExecute($$Lambda$ExpandableNotificationRowController$A0pf9AyYTRXRQWgbW6MeQ5pFtak.INSTANCE, null, false);
            return true;
        }
        ExpandedNotification sbn = this.mView.getEntry().getSbn();
        if (sbn.getLongPressIntent() != null) {
            z = true;
        }
        if (z) {
            Log.d("NotificationLongPress", sbn.getKey() + " LongPressJump");
            PendingIntent longPressIntent = sbn.getLongPressIntent();
            PendingIntent pendingIntent = sbn.getNotification().contentIntent;
            sbn.getNotification().contentIntent = longPressIntent;
            this.mView.performClick();
            sbn.getNotification().contentIntent = pendingIntent;
            return true;
        }
        ((ModalController) Dependency.get(ModalController.class)).tryAnimEnterModal(this.mView);
        return true;
    }

    public void init(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.initialize(this.mAppName, this.mNotificationKey, this.mExpansionLogger, this.mKeyguardBypassController, this.mNotificationGroupManager, this.mHeadsUpManager, this.mRowContentBindStage, this.mOnExpandClickListener, this.mMediaManager, this.mOnAppOpsClickListener, this.mFalsingManager, this.mStatusBarStateController, this.mPeopleNotificationIdentifier);
    }

    /* access modifiers changed from: private */
    public void logNotificationExpansion(String str, boolean z, boolean z2) {
        this.mNotificationLogger.onExpansionChanged(str, z, z2);
    }

    public void setOnDismissRunnable(Runnable runnable) {
        this.mOnDismissRunnable = runnable;
        this.mView.setOnDismissRunnable(runnable);
    }
}
