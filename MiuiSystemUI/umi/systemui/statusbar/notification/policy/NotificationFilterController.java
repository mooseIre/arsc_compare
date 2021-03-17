package com.android.systemui.statusbar.notification.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.plugins.NotificationListenerController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.NotificationFilterController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.DebugConfig;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NotificationFilterController {
    private static final boolean DEBUG = DebugConfig.DEBUG_NOTIFICATION;
    private final NotificationEntryManager mEntryManager;
    private final NotificationSettingsManager mSettingsManager;

    public void start() {
    }

    public NotificationFilterController(Context context, NotificationListener notificationListener, NotificationEntryManager notificationEntryManager, NotificationSettingsManager notificationSettingsManager, BroadcastDispatcher broadcastDispatcher) {
        this.mEntryManager = notificationEntryManager;
        this.mSettingsManager = notificationSettingsManager;
        notificationListener.onPluginConnected((NotificationListenerController) new NotificationListenerController() {
            /* class com.android.systemui.statusbar.notification.policy.NotificationFilterController.AnonymousClass1 */

            @Override // com.android.systemui.plugins.NotificationListenerController
            public void onListenerConnected(NotificationListenerController.NotificationProvider notificationProvider) {
            }

            @Override // com.android.systemui.plugins.NotificationListenerController
            public StatusBarNotification[] getActiveNotifications(StatusBarNotification[] statusBarNotificationArr) {
                return (statusBarNotificationArr == null || statusBarNotificationArr.length == 0) ? statusBarNotificationArr : (StatusBarNotification[]) Arrays.stream(statusBarNotificationArr).filter(new Predicate() {
                    /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationFilterController$1$XmSgMLR7UpebWnSxn8CIGx_z5rQ */

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return NotificationFilterController.AnonymousClass1.this.lambda$getActiveNotifications$0$NotificationFilterController$1((StatusBarNotification) obj);
                    }
                }).toArray($$Lambda$NotificationFilterController$1$e4t_htKeCYj4iaLS9q0WMaQzhp4.INSTANCE);
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$getActiveNotifications$0 */
            public /* synthetic */ boolean lambda$getActiveNotifications$0$NotificationFilterController$1(StatusBarNotification statusBarNotification) {
                return !NotificationFilterController.this.filterOut(statusBarNotification);
            }

            static /* synthetic */ StatusBarNotification[] lambda$getActiveNotifications$1(int i) {
                return new StatusBarNotification[i];
            }

            @Override // com.android.systemui.plugins.NotificationListenerController
            public boolean onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
                return NotificationFilterController.this.filterOut(statusBarNotification);
            }
        }, context);
        broadcastDispatcher.registerReceiver(new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.notification.policy.NotificationFilterController.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                NotificationFilterController.this.removeBannedNotifications(context, intent);
            }
        }, new IntentFilter("com.miui.app.ExtraStatusBarManager.action_refresh_notification"), null, UserHandle.ALL);
        broadcastDispatcher.registerReceiver(new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.notification.policy.NotificationFilterController.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                NotificationFilterController.this.removeKeyguardNotifications(intent);
            }
        }, new IntentFilter("com.miui.app.ExtraStatusBarManager.action_remove_keyguard_notification"), null, UserHandle.ALL);
    }

    public static boolean shouldFilterOut(NotificationEntry notificationEntry) {
        if (((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).isActive()) {
            return true;
        }
        if (((UsbNotificationController) Dependency.get(UsbNotificationController.class)).needDisableUsbNotification(notificationEntry.getSbn())) {
            if (DEBUG) {
                Log.d("NotificationFilterController", "filter out usb notification.");
            }
            return true;
        } else if (DEBUG && NotificationUtil.isSystemNotification(notificationEntry.getSbn())) {
            return true;
        } else {
            Context context = SystemUIApplication.getContext();
            String packageName = notificationEntry.getSbn().getPackageName();
            boolean isSubstituteNotification = notificationEntry.getSbn().isSubstituteNotification();
            boolean isNotificationsBanned = NotificationSettingsHelper.isNotificationsBanned(context, packageName);
            if (!isSubstituteNotification || !isNotificationsBanned) {
                return shouldFilterOutKeyguard(notificationEntry);
            }
            ((NotificationEntryManager) Dependency.get(NotificationEntryManager.class)).performRemoveNotification(notificationEntry.getSbn(), 7);
            Log.d("NotificationFilterController", String.format("filter Notification banned substitute key=%s", notificationEntry.getKey()));
            return true;
        }
    }

    private static boolean shouldFilterOutKeyguard(NotificationEntry notificationEntry) {
        if (((KeyguardStateController) Dependency.get(KeyguardStateController.class)).isShowing()) {
            return !shouldShowOnKeyguard(notificationEntry);
        }
        return false;
    }

    public static boolean shouldShowOnKeyguard(NotificationEntry notificationEntry) {
        if (MediaDataManagerKt.isMediaNotification(notificationEntry.getSbn())) {
            if (DEBUG) {
                Log.d("NotificationFilterController", "show media on keyguard " + notificationEntry.getKey());
            }
            return true;
        } else if (!NotificationSettingsHelper.alwaysShowKeyguardNotifications() && notificationEntry.getSbn().hasShownAfterUnlock()) {
            if (DEBUG) {
                Log.d("NotificationFilterController", "has shown " + notificationEntry.getKey());
            }
            return false;
        } else if (notificationEntry.getSbn().canShowOnKeyguard()) {
            return true;
        } else {
            if (DEBUG) {
                Log.d("NotificationFilterController", "can not show onKeyguard " + notificationEntry.getKey());
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean filterOut(StatusBarNotification statusBarNotification) {
        String packageName = statusBarNotification.getPackageName();
        String channelId = statusBarNotification.getNotification().getChannelId();
        if ((statusBarNotification.getNotification().flags & 64) != 0 && this.mSettingsManager.hideForegroundNotification(packageName, channelId)) {
            if (DEBUG) {
                Log.d("NotificationFilterController", "filter out foreground " + packageName + ":" + channelId);
            }
            return true;
        } else if ((statusBarNotification.getNotification().flags & 2) == 0 || statusBarNotification.getId() != 0 || !TextUtils.equals("android", statusBarNotification.getOpPkg()) || !this.mSettingsManager.hideAlertWindowNotification(statusBarNotification.getTag())) {
            return false;
        } else {
            if (DEBUG) {
                Log.d("NotificationFilterController", "filter out alert window " + packageName + ":" + channelId);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeBannedNotifications(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("app_packageName");
        String stringExtra2 = intent.getStringExtra("messageId");
        intent.getStringExtra("change_importance");
        String stringExtra3 = intent.getStringExtra("channel_id");
        if (intent.getBooleanExtra("com.miui.app.ExtraStatusBarManager.extra_forbid_notification", false)) {
            removeNotifications(stringExtra, stringExtra3);
            if (!TextUtils.equals(intent.getSender(), context.getPackageName())) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onBlock(stringExtra, stringExtra3, stringExtra2);
            }
        }
    }

    private void removeNotifications(String str, String str2) {
        ((List) this.mEntryManager.getAllNotifs().stream().filter(new Predicate(str) {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationFilterController$vizTXKc5abyTxxyyz6pEk9EKss */
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.equals(((NotificationEntry) obj).getSbn().getPackageName());
            }
        }).filter(new Predicate(str2) {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationFilterController$ckOIaSUtpeD_dTPkUqtIBuOxEs */
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return NotificationFilterController.lambda$removeNotifications$1(this.f$0, (NotificationEntry) obj);
            }
        }).collect(Collectors.toList())).forEach(new Consumer() {
            /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationFilterController$Px5xWo4BZZ0gfHkvdc258lrBZLo */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationFilterController.this.lambda$removeNotifications$2$NotificationFilterController((NotificationEntry) obj);
            }
        });
    }

    static /* synthetic */ boolean lambda$removeNotifications$1(String str, NotificationEntry notificationEntry) {
        return TextUtils.isEmpty(str) || TextUtils.equals(str, notificationEntry.getChannel().getId());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeNotifications$2 */
    public /* synthetic */ void lambda$removeNotifications$2$NotificationFilterController(NotificationEntry notificationEntry) {
        Log.d("NotificationFilterController", String.format("filter Notification key=%s", notificationEntry.getKey()));
        this.mEntryManager.performRemoveNotification(notificationEntry.getSbn(), 7);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeKeyguardNotifications(Intent intent) {
        int intExtra = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_key", 0);
        int intExtra2 = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_click", 0);
        if (intExtra == 0) {
            Log.d("NotificationFilterController", "keycode == 0 CLEAR_KEYGUARD_NOTIFICATION");
            ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).clear();
            return;
        }
        ((KeyguardNotificationController) Dependency.get(KeyguardNotificationController.class)).remove(intExtra);
        for (NotificationEntry notificationEntry : this.mEntryManager.getActiveNotificationsForCurrentUser()) {
            if (intExtra == notificationEntry.getKey().hashCode()) {
                ExpandedNotification sbn = notificationEntry.getSbn();
                Log.d("NotificationFilterController", "keycode = " + intExtra + "; click = " + intExtra2 + "; pkg = " + sbn.getPackageName() + "; id = " + sbn.getId());
                if (intExtra2 == 1) {
                    notificationEntry.getRow().callOnClick();
                } else {
                    this.mEntryManager.performRemoveNotification(sbn, 2);
                }
            }
        }
    }
}
