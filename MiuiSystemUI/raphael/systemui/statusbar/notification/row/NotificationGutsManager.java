package com.android.systemui.statusbar.notification.row;

import android.app.INotificationManager;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.settingslib.notification.ConversationIconFactory;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.NotificationLifetimeExtender;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.StatusBarStateControllerImpl;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.row.AppOpsInfo;
import com.android.systemui.statusbar.notification.row.NotificationConversationInfo;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.NotificationInfo;
import com.android.systemui.statusbar.notification.row.PriorityOnboardingDialogController;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;
import javax.inject.Provider;

public class NotificationGutsManager implements Dumpable, NotificationLifetimeExtender {
    private final AccessibilityManager mAccessibilityManager;
    private final Handler mBgHandler;
    private final BubbleController mBubbleController;
    private final Provider<PriorityOnboardingDialogController.Builder> mBuilderProvider;
    private final ChannelEditorDialogController mChannelEditorDialogController;
    private final Context mContext;
    private final CurrentUserContextTracker mContextTracker;
    private final DeviceProvisionedController mDeviceProvisionedController = ((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class));
    private NotificationMenuRowPlugin.MenuItem mGutsMenuItem;
    private final HighPriorityProvider mHighPriorityProvider;
    @VisibleForTesting
    protected String mKeyToRemoveOnGutsClosed;
    private final LauncherApps mLauncherApps;
    private NotificationListContainer mListContainer;
    private final NotificationLockscreenUserManager mLockscreenUserManager = ((NotificationLockscreenUserManager) Dependency.get(NotificationLockscreenUserManager.class));
    private final Handler mMainHandler;
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private NotificationActivityStarter mNotificationActivityStarter;
    private NotificationGuts mNotificationGutsExposed;
    private NotificationLifetimeExtender.NotificationSafeToRemoveCallback mNotificationLifetimeFinishedCallback;
    private final INotificationManager mNotificationManager;
    private OnSettingsClickListener mOnSettingsClickListener;
    private Runnable mOpenRunnable;
    private NotificationPresenter mPresenter;
    private final ShortcutManager mShortcutManager;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController = ((StatusBarStateController) Dependency.get(StatusBarStateController.class));
    private final UiEventLogger mUiEventLogger;
    private final VisualStabilityManager mVisualStabilityManager;

    public interface OnSettingsClickListener {
        void onSettingsClick(String str);
    }

    public NotificationGutsManager(Context context, VisualStabilityManager visualStabilityManager, Lazy<StatusBar> lazy, Handler handler, Handler handler2, AccessibilityManager accessibilityManager, HighPriorityProvider highPriorityProvider, INotificationManager iNotificationManager, LauncherApps launcherApps, ShortcutManager shortcutManager, ChannelEditorDialogController channelEditorDialogController, CurrentUserContextTracker currentUserContextTracker, Provider<PriorityOnboardingDialogController.Builder> provider, BubbleController bubbleController, UiEventLogger uiEventLogger) {
        this.mContext = context;
        this.mVisualStabilityManager = visualStabilityManager;
        this.mStatusBarLazy = lazy;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mAccessibilityManager = accessibilityManager;
        this.mHighPriorityProvider = highPriorityProvider;
        this.mNotificationManager = iNotificationManager;
        this.mLauncherApps = launcherApps;
        this.mShortcutManager = shortcutManager;
        this.mContextTracker = currentUserContextTracker;
        this.mBuilderProvider = provider;
        this.mChannelEditorDialogController = channelEditorDialogController;
        this.mBubbleController = bubbleController;
        this.mUiEventLogger = uiEventLogger;
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationListContainer notificationListContainer, NotificationInfo.CheckSaveListener checkSaveListener, OnSettingsClickListener onSettingsClickListener) {
        this.mPresenter = notificationPresenter;
        this.mListContainer = notificationListContainer;
        this.mOnSettingsClickListener = onSettingsClickListener;
    }

    public void setNotificationActivityStarter(NotificationActivityStarter notificationActivityStarter) {
        this.mNotificationActivityStarter = notificationActivityStarter;
    }

    public void onDensityOrFontScaleChanged(NotificationEntry notificationEntry) {
        setExposedGuts(notificationEntry.getGuts());
        bindGuts(notificationEntry.getRow());
    }

    private void startAppNotificationSettingsActivity(String str, int i, NotificationChannel notificationChannel, ExpandableNotificationRow expandableNotificationRow) {
        Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", str);
        intent.putExtra("app_uid", i);
        if (notificationChannel != null) {
            Bundle bundle = new Bundle();
            intent.putExtra(":settings:fragment_args_key", notificationChannel.getId());
            bundle.putString(":settings:fragment_args_key", notificationChannel.getId());
            intent.putExtra(":settings:show_fragment_args", bundle);
        }
        this.mNotificationActivityStarter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
    }

    private void startAppDetailsSettingsActivity(String str, int i, NotificationChannel notificationChannel, ExpandableNotificationRow expandableNotificationRow) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", str, null));
        intent.putExtra("android.provider.extra.APP_PACKAGE", str);
        intent.putExtra("app_uid", i);
        if (notificationChannel != null) {
            intent.putExtra(":settings:fragment_args_key", notificationChannel.getId());
        }
        this.mNotificationActivityStarter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void startAppOpsSettingsActivity(String str, int i, ArraySet<Integer> arraySet, ExpandableNotificationRow expandableNotificationRow) {
        if (arraySet.contains(24)) {
            if (arraySet.contains(26) || arraySet.contains(27)) {
                startAppDetailsSettingsActivity(str, i, null, expandableNotificationRow);
                return;
            }
            Intent intent = new Intent("android.settings.MANAGE_APP_OVERLAY_PERMISSION");
            intent.setData(Uri.fromParts("package", str, null));
            this.mNotificationActivityStarter.startNotificationGutsIntent(intent, i, expandableNotificationRow);
        } else if (arraySet.contains(26) || arraySet.contains(27)) {
            Intent intent2 = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
            intent2.putExtra("android.intent.extra.PACKAGE_NAME", str);
            this.mNotificationActivityStarter.startNotificationGutsIntent(intent2, i, expandableNotificationRow);
        }
    }

    private void startConversationSettingsActivity(int i, ExpandableNotificationRow expandableNotificationRow) {
        this.mNotificationActivityStarter.startNotificationGutsIntent(new Intent("android.settings.CONVERSATION_SETTINGS"), i, expandableNotificationRow);
    }

    private boolean bindGuts(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.ensureGutsInflated();
        return bindGuts(expandableNotificationRow, this.mGutsMenuItem);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean bindGuts(ExpandableNotificationRow expandableNotificationRow, NotificationMenuRowPlugin.MenuItem menuItem) {
        ExpandedNotification sbn = expandableNotificationRow.getEntry().getSbn();
        expandableNotificationRow.setGutsView(menuItem);
        expandableNotificationRow.setTag(sbn.getPackageName());
        expandableNotificationRow.getGuts().setClosedListener(new NotificationGuts.OnGutsClosedListener(expandableNotificationRow, sbn) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$lbHSFb83h5SRmJTPUlzactX7_1Q */
            public final /* synthetic */ ExpandableNotificationRow f$1;
            public final /* synthetic */ StatusBarNotification f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.OnGutsClosedListener
            public final void onGutsClosed(NotificationGuts notificationGuts) {
                NotificationGutsManager.this.lambda$bindGuts$0$NotificationGutsManager(this.f$1, this.f$2, notificationGuts);
            }
        });
        View gutsView = menuItem.getGutsView();
        try {
            if (gutsView instanceof NotificationSnooze) {
                initializeSnoozeView(expandableNotificationRow, (NotificationSnooze) gutsView);
                return true;
            } else if (gutsView instanceof AppOpsInfo) {
                initializeAppOpsInfo(expandableNotificationRow, (AppOpsInfo) gutsView);
                return true;
            } else if (gutsView instanceof NotificationInfo) {
                initializeNotificationInfo(expandableNotificationRow, (NotificationInfo) gutsView);
                return true;
            } else if (gutsView instanceof NotificationConversationInfo) {
                initializeConversationNotificationInfo(expandableNotificationRow, (NotificationConversationInfo) gutsView);
                return true;
            } else if (!(gutsView instanceof PartialConversationInfo)) {
                return true;
            } else {
                initializePartialConversationNotificationInfo(expandableNotificationRow, (PartialConversationInfo) gutsView);
                return true;
            }
        } catch (Exception e) {
            Log.e("NotificationGutsManager", "error binding guts", e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindGuts$0 */
    public /* synthetic */ void lambda$bindGuts$0$NotificationGutsManager(ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification, NotificationGuts notificationGuts) {
        expandableNotificationRow.onGutsClosed();
        if (!notificationGuts.willBeRemoved() && !expandableNotificationRow.isRemoved()) {
            this.mListContainer.onHeightChanged(expandableNotificationRow, !this.mPresenter.isPresenterFullyCollapsed());
        }
        if (this.mNotificationGutsExposed == notificationGuts) {
            this.mNotificationGutsExposed = null;
            this.mGutsMenuItem = null;
        }
        String key = statusBarNotification.getKey();
        if (key.equals(this.mKeyToRemoveOnGutsClosed)) {
            this.mKeyToRemoveOnGutsClosed = null;
            NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback = this.mNotificationLifetimeFinishedCallback;
            if (notificationSafeToRemoveCallback != null) {
                notificationSafeToRemoveCallback.onSafeToRemove(key);
            }
        }
    }

    private void initializeSnoozeView(ExpandableNotificationRow expandableNotificationRow, NotificationSnooze notificationSnooze) {
        NotificationGuts guts = expandableNotificationRow.getGuts();
        ExpandedNotification sbn = expandableNotificationRow.getEntry().getSbn();
        notificationSnooze.setSnoozeListener(this.mListContainer.getSwipeActionHelper());
        notificationSnooze.setStatusBarNotification(sbn);
        notificationSnooze.setSnoozeOptions(expandableNotificationRow.getEntry().getSnoozeCriteria());
        guts.setHeightChangedListener(new NotificationGuts.OnHeightChangedListener(expandableNotificationRow) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$xtHxMW6jrIgJGugFgxSSg6aT080 */
            public final /* synthetic */ ExpandableNotificationRow f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.OnHeightChangedListener
            public final void onHeightChanged(NotificationGuts notificationGuts) {
                NotificationGutsManager.this.lambda$initializeSnoozeView$1$NotificationGutsManager(this.f$1, notificationGuts);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializeSnoozeView$1 */
    public /* synthetic */ void lambda$initializeSnoozeView$1$NotificationGutsManager(ExpandableNotificationRow expandableNotificationRow, NotificationGuts notificationGuts) {
        this.mListContainer.onHeightChanged(expandableNotificationRow, expandableNotificationRow.isShown());
    }

    private void initializeAppOpsInfo(ExpandableNotificationRow expandableNotificationRow, AppOpsInfo appOpsInfo) {
        NotificationGuts guts = expandableNotificationRow.getGuts();
        ExpandedNotification sbn = expandableNotificationRow.getEntry().getSbn();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, sbn.getUser().getIdentifier());
        $$Lambda$NotificationGutsManager$wyMAwZ08iUSa7KG3DScd2mF0ZVk r4 = new AppOpsInfo.OnSettingsClickListener(sbn, guts, expandableNotificationRow) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$wyMAwZ08iUSa7KG3DScd2mF0ZVk */
            public final /* synthetic */ StatusBarNotification f$1;
            public final /* synthetic */ NotificationGuts f$2;
            public final /* synthetic */ ExpandableNotificationRow f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.systemui.statusbar.notification.row.AppOpsInfo.OnSettingsClickListener
            public final void onClick(View view, String str, int i, ArraySet arraySet) {
                NotificationGutsManager.this.lambda$initializeAppOpsInfo$2$NotificationGutsManager(this.f$1, this.f$2, this.f$3, view, str, i, arraySet);
            }
        };
        if (!expandableNotificationRow.getEntry().mActiveAppOps.isEmpty()) {
            appOpsInfo.bindGuts(packageManagerForUser, r4, sbn, this.mUiEventLogger, expandableNotificationRow.getEntry().mActiveAppOps);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializeAppOpsInfo$2 */
    public /* synthetic */ void lambda$initializeAppOpsInfo$2$NotificationGutsManager(StatusBarNotification statusBarNotification, NotificationGuts notificationGuts, ExpandableNotificationRow expandableNotificationRow, View view, String str, int i, ArraySet arraySet) {
        this.mUiEventLogger.logWithInstanceId(NotificationAppOpsEvent.NOTIFICATION_APP_OPS_SETTINGS_CLICK, statusBarNotification.getUid(), statusBarNotification.getPackageName(), statusBarNotification.getInstanceId());
        this.mMetricsLogger.action(1346);
        notificationGuts.resetFalsingCheck();
        startAppOpsSettingsActivity(str, i, arraySet, expandableNotificationRow);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void initializeNotificationInfo(ExpandableNotificationRow expandableNotificationRow, NotificationInfo notificationInfo) throws Exception {
        $$Lambda$NotificationGutsManager$Q50_8sHdIRaYdx4NmoW9bex_4o r13;
        NotificationGuts guts = expandableNotificationRow.getGuts();
        ExpandedNotification sbn = expandableNotificationRow.getEntry().getSbn();
        String packageName = sbn.getPackageName();
        UserHandle user = sbn.getUser();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier());
        $$Lambda$NotificationGutsManager$5sbilrrQIt_lf8k9ZdwNLnjs r14 = new NotificationInfo.OnAppSettingsClickListener(guts, sbn, expandableNotificationRow) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$5sbilrrQIt_lf8k9ZdwNLnjs */
            public final /* synthetic */ NotificationGuts f$1;
            public final /* synthetic */ StatusBarNotification f$2;
            public final /* synthetic */ ExpandableNotificationRow f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.systemui.statusbar.notification.row.NotificationInfo.OnAppSettingsClickListener
            public final void onClick(View view, Intent intent) {
                NotificationGutsManager.this.lambda$initializeNotificationInfo$3$NotificationGutsManager(this.f$1, this.f$2, this.f$3, view, intent);
            }
        };
        if (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) {
            r13 = new NotificationInfo.OnSettingsClickListener(guts, sbn, packageName, expandableNotificationRow) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$Q50_8sHdIRaYdx4NmoW9bex_4o */
                public final /* synthetic */ NotificationGuts f$1;
                public final /* synthetic */ StatusBarNotification f$2;
                public final /* synthetic */ String f$3;
                public final /* synthetic */ ExpandableNotificationRow f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationInfo.OnSettingsClickListener
                public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                    NotificationGutsManager.this.lambda$initializeNotificationInfo$4$NotificationGutsManager(this.f$1, this.f$2, this.f$3, this.f$4, view, notificationChannel, i);
                }
            };
        } else {
            r13 = null;
        }
        notificationInfo.bindNotification(packageManagerForUser, this.mNotificationManager, this.mVisualStabilityManager, this.mChannelEditorDialogController, packageName, expandableNotificationRow.getEntry().getChannel(), expandableNotificationRow.getUniqueChannels(), expandableNotificationRow.getEntry(), r13, r14, this.mUiEventLogger, this.mDeviceProvisionedController.isDeviceProvisioned(), expandableNotificationRow.getIsNonblockable(), this.mHighPriorityProvider.isHighPriority(expandableNotificationRow.getEntry()));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializeNotificationInfo$3 */
    public /* synthetic */ void lambda$initializeNotificationInfo$3$NotificationGutsManager(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow, View view, Intent intent) {
        this.mMetricsLogger.action(206);
        notificationGuts.resetFalsingCheck();
        this.mNotificationActivityStarter.startNotificationGutsIntent(intent, statusBarNotification.getUid(), expandableNotificationRow);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializeNotificationInfo$4 */
    public /* synthetic */ void lambda$initializeNotificationInfo$4$NotificationGutsManager(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        this.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        this.mOnSettingsClickListener.onSettingsClick(statusBarNotification.getKey());
        startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void initializePartialConversationNotificationInfo(ExpandableNotificationRow expandableNotificationRow, PartialConversationInfo partialConversationInfo) throws Exception {
        $$Lambda$NotificationGutsManager$_QQQs9zfP_ZSHrXlaEFU7rOxG50 r12;
        NotificationGuts guts = expandableNotificationRow.getGuts();
        ExpandedNotification sbn = expandableNotificationRow.getEntry().getSbn();
        String packageName = sbn.getPackageName();
        UserHandle user = sbn.getUser();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier());
        if (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) {
            r12 = new NotificationInfo.OnSettingsClickListener(guts, sbn, packageName, expandableNotificationRow) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$_QQQs9zfP_ZSHrXlaEFU7rOxG50 */
                public final /* synthetic */ NotificationGuts f$1;
                public final /* synthetic */ StatusBarNotification f$2;
                public final /* synthetic */ String f$3;
                public final /* synthetic */ ExpandableNotificationRow f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationInfo.OnSettingsClickListener
                public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                    NotificationGutsManager.this.lambda$initializePartialConversationNotificationInfo$5$NotificationGutsManager(this.f$1, this.f$2, this.f$3, this.f$4, view, notificationChannel, i);
                }
            };
        } else {
            r12 = null;
        }
        partialConversationInfo.bindNotification(packageManagerForUser, this.mNotificationManager, this.mChannelEditorDialogController, packageName, expandableNotificationRow.getEntry().getChannel(), expandableNotificationRow.getUniqueChannels(), expandableNotificationRow.getEntry(), r12, this.mDeviceProvisionedController.isDeviceProvisioned(), expandableNotificationRow.getIsNonblockable());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializePartialConversationNotificationInfo$5 */
    public /* synthetic */ void lambda$initializePartialConversationNotificationInfo$5$NotificationGutsManager(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        this.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        this.mOnSettingsClickListener.onSettingsClick(statusBarNotification.getKey());
        startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void initializeConversationNotificationInfo(ExpandableNotificationRow expandableNotificationRow, NotificationConversationInfo notificationConversationInfo) throws Exception {
        $$Lambda$NotificationGutsManager$l1uQ6jkAF_qnuYpLc8JtAuLHmo r16;
        NotificationGuts guts = expandableNotificationRow.getGuts();
        NotificationEntry entry = expandableNotificationRow.getEntry();
        ExpandedNotification sbn = entry.getSbn();
        String packageName = sbn.getPackageName();
        UserHandle user = sbn.getUser();
        PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(this.mContext, user.getIdentifier());
        $$Lambda$NotificationGutsManager$SxOqoV1fAYXT8YXSboDhbPUKcNY r7 = new NotificationConversationInfo.OnSnoozeClickListener(sbn) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$SxOqoV1fAYXT8YXSboDhbPUKcNY */
            public final /* synthetic */ StatusBarNotification f$1;

            {
                this.f$1 = r2;
            }
        };
        $$Lambda$NotificationGutsManager$sCU8ga9RgRp89PSTE4vP2xOWPYM r15 = new NotificationConversationInfo.OnConversationSettingsClickListener(sbn, expandableNotificationRow) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$sCU8ga9RgRp89PSTE4vP2xOWPYM */
            public final /* synthetic */ StatusBarNotification f$1;
            public final /* synthetic */ ExpandableNotificationRow f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // com.android.systemui.statusbar.notification.row.NotificationConversationInfo.OnConversationSettingsClickListener
            public final void onClick() {
                NotificationGutsManager.this.lambda$initializeConversationNotificationInfo$8$NotificationGutsManager(this.f$1, this.f$2);
            }
        };
        if (!user.equals(UserHandle.ALL) || this.mLockscreenUserManager.getCurrentUserId() == 0) {
            r16 = new NotificationConversationInfo.OnSettingsClickListener(guts, sbn, packageName, expandableNotificationRow) {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$l1uQ6jkAF_qnuYpLc8JtAuLHmo */
                public final /* synthetic */ NotificationGuts f$1;
                public final /* synthetic */ StatusBarNotification f$2;
                public final /* synthetic */ String f$3;
                public final /* synthetic */ ExpandableNotificationRow f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                @Override // com.android.systemui.statusbar.notification.row.NotificationConversationInfo.OnSettingsClickListener
                public final void onClick(View view, NotificationChannel notificationChannel, int i) {
                    NotificationGutsManager.this.lambda$initializeConversationNotificationInfo$9$NotificationGutsManager(this.f$1, this.f$2, this.f$3, this.f$4, view, notificationChannel, i);
                }
            };
        } else {
            r16 = null;
        }
        Context context = this.mContext;
        notificationConversationInfo.bindNotification(this.mShortcutManager, packageManagerForUser, this.mNotificationManager, this.mVisualStabilityManager, packageName, entry.getChannel(), entry, entry.getBubbleMetadata(), r16, r7, new ConversationIconFactory(context, this.mLauncherApps, packageManagerForUser, IconDrawableFactory.newInstance(context, false), this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.notification_guts_conversation_icon_size)), this.mContextTracker.getCurrentUserContext(), this.mBuilderProvider, this.mDeviceProvisionedController.isDeviceProvisioned(), this.mMainHandler, this.mBgHandler, r15, this.mBubbleController);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializeConversationNotificationInfo$8 */
    public /* synthetic */ void lambda$initializeConversationNotificationInfo$8$NotificationGutsManager(StatusBarNotification statusBarNotification, ExpandableNotificationRow expandableNotificationRow) {
        startConversationSettingsActivity(statusBarNotification.getUid(), expandableNotificationRow);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initializeConversationNotificationInfo$9 */
    public /* synthetic */ void lambda$initializeConversationNotificationInfo$9$NotificationGutsManager(NotificationGuts notificationGuts, StatusBarNotification statusBarNotification, String str, ExpandableNotificationRow expandableNotificationRow, View view, NotificationChannel notificationChannel, int i) {
        this.mMetricsLogger.action(205);
        notificationGuts.resetFalsingCheck();
        this.mOnSettingsClickListener.onSettingsClick(statusBarNotification.getKey());
        startAppNotificationSettingsActivity(str, i, notificationChannel, expandableNotificationRow);
    }

    public void closeAndSaveGuts(boolean z, boolean z2, boolean z3, int i, int i2, boolean z4) {
        NotificationGuts notificationGuts = this.mNotificationGutsExposed;
        if (notificationGuts != null) {
            notificationGuts.removeCallbacks(this.mOpenRunnable);
            this.mNotificationGutsExposed.closeControls(z, z3, i, i2, z2);
        }
        if (z4) {
            this.mListContainer.resetExposedMenuView(false, true);
        }
    }

    public NotificationGuts getExposedGuts() {
        return this.mNotificationGutsExposed;
    }

    public void setExposedGuts(NotificationGuts notificationGuts) {
        this.mNotificationGutsExposed = notificationGuts;
    }

    public boolean openGuts(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        if (!(menuItem.getGutsView() instanceof NotificationGuts.GutsContent) || !((NotificationGuts.GutsContent) menuItem.getGutsView()).needsFalsingProtection()) {
            return lambda$openGuts$10(view, i, i2, menuItem);
        }
        StatusBarStateController statusBarStateController = this.mStatusBarStateController;
        if (statusBarStateController instanceof StatusBarStateControllerImpl) {
            ((StatusBarStateControllerImpl) statusBarStateController).setLeaveOpenOnKeyguardHide(true);
        }
        this.mStatusBarLazy.get().executeRunnableDismissingKeyguard(new Runnable(view, i, i2, menuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$FMcYcqx6JxHHSZHrMeuI2W1DjZY */
            public final /* synthetic */ View f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ NotificationMenuRowPlugin.MenuItem f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                NotificationGutsManager.this.lambda$openGuts$11$NotificationGutsManager(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        }, null, false, true, true);
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$openGuts$11 */
    public /* synthetic */ void lambda$openGuts$11$NotificationGutsManager(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        this.mMainHandler.post(new Runnable(view, i, i2, menuItem) {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotificationGutsManager$PjWrLLW0p5MpvIfRIh3E86B4Vc */
            public final /* synthetic */ View f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ NotificationMenuRowPlugin.MenuItem f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                NotificationGutsManager.this.lambda$openGuts$10$NotificationGutsManager(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    /* renamed from: openGutsInternal */
    public boolean lambda$openGuts$10(View view, final int i, final int i2, final NotificationMenuRowPlugin.MenuItem menuItem) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        if (view.getWindowToken() == null) {
            Log.e("NotificationGutsManager", "Trying to show notification guts, but not attached to window");
            return false;
        }
        final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        view.performHapticFeedback(0);
        if (expandableNotificationRow.areGutsExposed()) {
            closeAndSaveGuts(false, false, true, -1, -1, true);
            return false;
        }
        expandableNotificationRow.ensureGutsInflated();
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        this.mNotificationGutsExposed = guts;
        if (!bindGuts(expandableNotificationRow, menuItem) || guts == null) {
            return false;
        }
        guts.setVisibility(4);
        AnonymousClass1 r7 = new Runnable() {
            /* class com.android.systemui.statusbar.notification.row.NotificationGutsManager.AnonymousClass1 */

            public void run() {
                if (expandableNotificationRow.getWindowToken() == null) {
                    Log.e("NotificationGutsManager", "Trying to show notification guts in post(), but not attached to window");
                    return;
                }
                guts.setVisibility(0);
                boolean z = NotificationGutsManager.this.mStatusBarStateController.getState() == 1 && !NotificationGutsManager.this.mAccessibilityManager.isTouchExplorationEnabled();
                NotificationGuts notificationGuts = guts;
                boolean z2 = !expandableNotificationRow.isBlockingHelperShowing();
                int i = i;
                int i2 = i2;
                ExpandableNotificationRow expandableNotificationRow = expandableNotificationRow;
                Objects.requireNonNull(expandableNotificationRow);
                notificationGuts.openControls(z2, i, i2, z, new Runnable() {
                    /* class com.android.systemui.statusbar.notification.row.$$Lambda$IONSGD9gxXDD_zwBcDGw5yfu2Rc */

                    public final void run() {
                        ExpandableNotificationRow.this.onGutsOpened();
                    }
                });
                expandableNotificationRow.closeRemoteInput();
                NotificationGutsManager.this.mListContainer.onHeightChanged(expandableNotificationRow, true);
                NotificationGutsManager.this.mGutsMenuItem = menuItem;
            }
        };
        this.mOpenRunnable = r7;
        guts.post(r7);
        return true;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setCallback(NotificationLifetimeExtender.NotificationSafeToRemoveCallback notificationSafeToRemoveCallback) {
        this.mNotificationLifetimeFinishedCallback = notificationSafeToRemoveCallback;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        return (notificationEntry == null || this.mNotificationGutsExposed == null || notificationEntry.getGuts() == null || this.mNotificationGutsExposed != notificationEntry.getGuts() || this.mNotificationGutsExposed.isLeavebehind()) ? false : true;
    }

    @Override // com.android.systemui.statusbar.NotificationLifetimeExtender
    public void setShouldManageLifetime(NotificationEntry notificationEntry, boolean z) {
        if (z) {
            this.mKeyToRemoveOnGutsClosed = notificationEntry.getKey();
            if (Log.isLoggable("NotificationGutsManager", 3)) {
                Log.d("NotificationGutsManager", "Keeping notification because it's showing guts. " + notificationEntry.getKey());
                return;
            }
            return;
        }
        String str = this.mKeyToRemoveOnGutsClosed;
        if (str != null && str.equals(notificationEntry.getKey())) {
            this.mKeyToRemoveOnGutsClosed = null;
            if (Log.isLoggable("NotificationGutsManager", 3)) {
                Log.d("NotificationGutsManager", "Notification that was kept for guts was updated. " + notificationEntry.getKey());
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationGutsManager state:");
        printWriter.print("  mKeyToRemoveOnGutsClosed: ");
        printWriter.println(this.mKeyToRemoveOnGutsClosed);
    }
}
