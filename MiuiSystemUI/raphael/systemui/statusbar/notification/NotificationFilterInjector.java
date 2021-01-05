package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.KeyguardNotificationController;
import com.android.systemui.statusbar.notification.policy.UsbNotificationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.DebugConfig;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NotificationFilterInjector {
    private static final boolean DEBUG = DebugConfig.DEBUG_NOTIFICATION;

    public static boolean shouldFilterOut(NotificationEntry notificationEntry) {
        Class cls = NotificationSettingsManager.class;
        if (((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).isActive()) {
            return true;
        }
        String packageName = notificationEntry.getSbn().getPackageName();
        String channelId = notificationEntry.getSbn().getNotification().getChannelId();
        if ((notificationEntry.getSbn().getNotification().flags & 64) != 0 && ((NotificationSettingsManager) Dependency.get(cls)).hideForegroundNotification(packageName, channelId)) {
            if (DEBUG) {
                Log.d("NotificationFilterInjector", "filter out foreground " + packageName + ":" + channelId);
            }
            return true;
        } else if ((notificationEntry.getSbn().getNotification().flags & 2) != 0 && notificationEntry.getSbn().getId() == 0 && TextUtils.equals("android", notificationEntry.getSbn().getOpPkg()) && ((NotificationSettingsManager) Dependency.get(cls)).hideAlertWindowNotification(notificationEntry.getSbn().getTag())) {
            if (DEBUG) {
                Log.d("NotificationFilterInjector", "filter out alert window " + packageName + ":" + channelId);
            }
            return true;
        } else if (((UsbNotificationController) Dependency.get(UsbNotificationController.class)).needDisableUsbNotification(notificationEntry.getSbn())) {
            if (DEBUG) {
                Log.d("NotificationFilterInjector", "filter out usb " + packageName + ":" + channelId);
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
            if (MediaDataManagerKt.isMediaNotification(notificationEntry.getSbn())) {
                if (DEBUG) {
                    Log.d("NotificationFilterInjector", "show media on keyguard " + notificationEntry.getKey());
                }
                return false;
            } else if (!NotificationSettingsHelper.alwaysShowKeyguardNotifications() && notificationEntry.getSbn().hasShownAfterUnlock()) {
                if (DEBUG) {
                    Log.d("NotificationFilterInjector", "has shown " + notificationEntry.getKey());
                }
                return true;
            } else if (!notificationEntry.getSbn().canShowOnKeyguard()) {
                if (DEBUG) {
                    Log.d("NotificationFilterInjector", "can not show onKeyguard " + notificationEntry.getKey());
                }
                return true;
            }
        }
        return false;
    }

    public static void handleRefreshRequest(Context context, Intent intent, NotificationEntryManager notificationEntryManager) {
        String stringExtra = intent.getStringExtra("app_packageName");
        String stringExtra2 = intent.getStringExtra("messageId");
        intent.getStringExtra("change_importance");
        String stringExtra3 = intent.getStringExtra("channel_id");
        if (intent.getBooleanExtra("com.miui.app.ExtraStatusBarManager.extra_forbid_notification", false)) {
            filterNotification(context, stringExtra, notificationEntryManager);
            if (!TextUtils.equals(intent.getSender(), context.getPackageName())) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onBlock(stringExtra, stringExtra3, stringExtra2);
            }
        }
    }

    public static void handleRemoveNotificationRequest(Intent intent, NotificationEntryManager notificationEntryManager) {
        Class cls = KeyguardNotificationController.class;
        int intExtra = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_key", 0);
        int intExtra2 = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_click", 0);
        if (intExtra == 0) {
            Log.d("NotificationFilterInjector", "keycode == 0 CLEAR_KEYGUARD_NOTIFICATION");
            ((KeyguardNotificationController) Dependency.get(cls)).clear();
            return;
        }
        ((KeyguardNotificationController) Dependency.get(cls)).remove(intExtra);
        for (NotificationEntry next : notificationEntryManager.getActiveNotificationsForCurrentUser()) {
            if (intExtra == next.getKey().hashCode()) {
                ExpandedNotification sbn = next.getSbn();
                Log.d("NotificationFilterInjector", "keycode = " + intExtra + "; click = " + intExtra2 + "; pkg = " + sbn.getPackageName() + "; id = " + sbn.getId());
                if (intExtra2 == 1) {
                    next.getRow().callOnClick();
                } else {
                    notificationEntryManager.performRemoveNotification(sbn, 2);
                }
            }
        }
    }

    private static void filterNotification(Context context, String str, NotificationEntryManager notificationEntryManager) {
        ((List) notificationEntryManager.getAllNotifs().stream().filter(new Predicate(str) {
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.equals(((NotificationEntry) obj).getSbn().getPackageName());
            }
        }).collect(Collectors.toList())).forEach(new Consumer(context, notificationEntryManager) {
            public final /* synthetic */ Context f$0;
            public final /* synthetic */ NotificationEntryManager f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                NotificationFilterInjector.filterNotification(this.f$0, ((NotificationEntry) obj).getSbn(), this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public static boolean filterNotification(Context context, ExpandedNotification expandedNotification, NotificationEntryManager notificationEntryManager) {
        boolean z;
        Class cls = NotificationSettingsManager.class;
        String packageName = expandedNotification.getPackageName();
        String channelId = expandedNotification.getNotification().getChannelId();
        if ((!expandedNotification.isSubstituteNotification() || !NotificationSettingsHelper.isNotificationsBanned(context, packageName)) && (((expandedNotification.getNotification().flags & 64) == 0 || !((NotificationSettingsManager) Dependency.get(cls)).hideForegroundNotification(packageName, channelId)) && (((expandedNotification.getNotification().flags & 2) == 0 || expandedNotification.getId() != 0 || !TextUtils.equals("android", expandedNotification.getOpPkg()) || !((NotificationSettingsManager) Dependency.get(cls)).hideAlertWindowNotification(expandedNotification.getTag())) && !((UsbNotificationController) Dependency.get(UsbNotificationController.class)).needDisableUsbNotification(expandedNotification) && ((expandedNotification.getNotification().flags & 268435456) == 0 || (!packageName.equalsIgnoreCase("com.mediatek.selfregister") && !packageName.equalsIgnoreCase("com.mediatek.deviceregister")))))) {
            z = false;
        } else {
            z = true;
        }
        if (z) {
            notificationEntryManager.performRemoveNotification(expandedNotification, 7);
        }
        if (z) {
            Log.d("NotificationFilterInjector", String.format("filter Notification key=%s", new Object[]{expandedNotification.getKey()}));
        }
        return z;
    }
}
