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
import com.android.systemui.broadcast.BroadcastDispatcher;
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
import java.util.concurrent.Executor;
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
            public void onListenerConnected(NotificationListenerController.NotificationProvider notificationProvider) {
            }

            public StatusBarNotification[] getActiveNotifications(StatusBarNotification[] statusBarNotificationArr) {
                return (statusBarNotificationArr == null || statusBarNotificationArr.length == 0) ? statusBarNotificationArr : (StatusBarNotification[]) Arrays.stream(statusBarNotificationArr).filter(new Predicate() {
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

            public boolean onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
                return NotificationFilterController.this.filterOut(statusBarNotification);
            }
        }, context);
        broadcastDispatcher.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NotificationFilterController.this.removeBannedNotifications(context, intent);
            }
        }, new IntentFilter("com.miui.app.ExtraStatusBarManager.action_refresh_notification"), (Executor) null, UserHandle.ALL);
        broadcastDispatcher.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NotificationFilterController.this.removeKeyguardNotifications(intent);
            }
        }, new IntentFilter("com.miui.app.ExtraStatusBarManager.action_remove_keyguard_notification"), (Executor) null, UserHandle.ALL);
    }

    public static boolean shouldFilterOut(NotificationEntry notificationEntry) {
        if (((UsbNotificationController) Dependency.get(UsbNotificationController.class)).needDisableUsbNotification(notificationEntry.getSbn())) {
            if (DEBUG) {
                Log.d("NotificationFilterController", "filter out usb notification.");
            }
            return true;
        } else if (!DEBUG || !NotificationUtil.isSystemNotification(notificationEntry.getSbn())) {
            return shouldFilterOutKeyguard(notificationEntry);
        } else {
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
    public boolean filterOut(StatusBarNotification statusBarNotification) {
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
    public void removeBannedNotifications(Context context, Intent intent) {
        String stringExtra = intent.getStringExtra("app_packageName");
        String stringExtra2 = intent.getStringExtra("messageId");
        intent.getStringExtra("change_importance");
        String stringExtra3 = intent.getStringExtra("channel_id");
        if (intent.getBooleanExtra("com.miui.app.ExtraStatusBarManager.extra_forbid_notification", false)) {
            removeNotifications(stringExtra);
            if (!TextUtils.equals(intent.getSender(), context.getPackageName())) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onBlock(stringExtra, stringExtra3, stringExtra2);
            }
        }
    }

    private void removeNotifications(String str) {
        ((List) this.mEntryManager.getAllNotifs().stream().filter(new Predicate(str) {
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.equals(((NotificationEntry) obj).getSbn().getPackageName());
            }
        }).collect(Collectors.toList())).forEach(new Consumer() {
            public final void accept(Object obj) {
                NotificationFilterController.this.lambda$removeNotifications$1$NotificationFilterController((NotificationEntry) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$removeNotifications$1 */
    public /* synthetic */ void lambda$removeNotifications$1$NotificationFilterController(NotificationEntry notificationEntry) {
        Log.d("NotificationFilterController", String.format("filter Notification key=%s", new Object[]{notificationEntry.getKey()}));
        this.mEntryManager.performRemoveNotification(notificationEntry.getSbn(), 7);
    }

    /* access modifiers changed from: private */
    public void removeKeyguardNotifications(Intent intent) {
        Class cls = KeyguardNotificationController.class;
        int intExtra = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_key", 0);
        int intExtra2 = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_click", 0);
        if (intExtra == 0) {
            Log.d("NotificationFilterController", "keycode == 0 CLEAR_KEYGUARD_NOTIFICATION");
            ((KeyguardNotificationController) Dependency.get(cls)).clear();
            return;
        }
        ((KeyguardNotificationController) Dependency.get(cls)).remove(intExtra);
        for (NotificationEntry next : this.mEntryManager.getActiveNotificationsForCurrentUser()) {
            if (intExtra == next.getKey().hashCode()) {
                ExpandedNotification sbn = next.getSbn();
                Log.d("NotificationFilterController", "keycode = " + intExtra + "; click = " + intExtra2 + "; pkg = " + sbn.getPackageName() + "; id = " + sbn.getId());
                if (intExtra2 == 1) {
                    next.getRow().callOnClick();
                } else {
                    this.mEntryManager.performRemoveNotification(sbn, 2);
                }
            }
        }
    }
}
