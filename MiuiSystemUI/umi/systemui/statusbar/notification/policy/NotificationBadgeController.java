package com.android.systemui.statusbar.notification.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.NotificationBadgeController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NotificationBadgeController {
    BroadcastDispatcher mBroadcastDispatcher;
    Context mContext;
    NotificationEntryManager mEntryManager;
    NotificationGroupManager mGroupManager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.notification.policy.NotificationBadgeController.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            boolean booleanExtra = intent.getBooleanExtra("com.miui.extra_update_request_first_time", false);
            Log.d("NotifBadge", "recevie broadbcast ACTION_APPLICATION_MESSAGE_QUERY, requestFirstTime=" + booleanExtra);
            if (booleanExtra) {
                new ArrayList(NotificationBadgeController.this.mEntryManager.getVisibleNotifications()).stream().filter(new Predicate(ConcurrentHashMap.newKeySet()) {
                    /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationBadgeController$1$0mL3CghvQ1p7y9g_5i3XGE7mrBc */
                    public final /* synthetic */ Set f$0;

                    {
                        this.f$0 = r1;
                    }

                    @Override // java.util.function.Predicate
                    public final boolean test(Object obj) {
                        return this.f$0.add(((NotificationEntry) obj).getSbn().getPackageName());
                    }
                }).forEach(new Consumer() {
                    /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationBadgeController$1$OV2_G18o0KzMKTB_MqN4Xkr5sk */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        NotificationBadgeController.AnonymousClass1.this.lambda$onReceive$1$NotificationBadgeController$1((NotificationEntry) obj);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$1 */
        public /* synthetic */ void lambda$onReceive$1$NotificationBadgeController$1(NotificationEntry notificationEntry) {
            NotificationBadgeController.this.updateAppBadgeNum(notificationEntry.getSbn());
        }
    };

    public NotificationBadgeController(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mEntryManager = notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
        this.mBroadcastDispatcher = broadcastDispatcher;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.APPLICATION_MESSAGE_QUERY");
        this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter, null, UserHandle.ALL);
        new CurrentUserTracker(this.mBroadcastDispatcher) {
            /* class com.android.systemui.statusbar.notification.policy.NotificationBadgeController.AnonymousClass2 */

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                Intent intent = new Intent("android.intent.action.APPLICATION_MESSAGE_QUERY");
                intent.putExtra("com.miui.extra_update_request_first_time", true);
                NotificationBadgeController.this.mContext.sendBroadcast(intent);
            }
        }.startTracking();
    }

    public void updateAppBadgeNum(ExpandedNotification expandedNotification) {
        int i;
        CharSequence charSequence;
        String str;
        int identifier = expandedNotification.getUser().getIdentifier();
        String packageName = expandedNotification.getPackageName();
        CharSequence messageClassName = getMessageClassName(expandedNotification);
        boolean canShowBadge = NotificationSettingsHelper.canShowBadge(expandedNotification);
        int i2 = 0;
        if (canShowBadge) {
            List<NotificationEntry> list = (List) this.mEntryManager.getVisibleNotifications().stream().filter(new Predicate() {
                /* class com.android.systemui.statusbar.notification.policy.$$Lambda$NotificationBadgeController$ZhLIvBsM7w4Mj28WNhyGMzN0h7w */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return NotificationBadgeController.lambda$updateAppBadgeNum$0(ExpandedNotification.this, (NotificationEntry) obj);
                }
            }).collect(Collectors.toList());
            if (NotificationUtil.isMissedCallNotification(expandedNotification)) {
                for (NotificationEntry notificationEntry : list) {
                    if (NotificationUtil.isMissedCallNotification(notificationEntry.getSbn()) && needStatBadgeNum(notificationEntry.getSbn())) {
                        i2 += notificationEntry.getSbn().getMessageCount();
                    }
                }
                charSequence = ".activities.TwelveKeyDialer";
                i = i2;
                str = "com.android.contacts";
                updateAppBadgeNum(str, charSequence, i, identifier, canShowBadge);
            }
            for (NotificationEntry notificationEntry2 : list) {
                if (notificationEntry2.getSbn().getPackageName().equals(packageName) && TextUtils.equals(getMessageClassName(notificationEntry2.getSbn()), messageClassName) && needStatBadgeNum(notificationEntry2.getSbn())) {
                    i2 += notificationEntry2.getSbn().getMessageCount();
                }
            }
        }
        charSequence = messageClassName;
        i = i2;
        str = packageName;
        updateAppBadgeNum(str, charSequence, i, identifier, canShowBadge);
    }

    static /* synthetic */ boolean lambda$updateAppBadgeNum$0(ExpandedNotification expandedNotification, NotificationEntry notificationEntry) {
        return expandedNotification.getPackageName().equals(notificationEntry.getSbn().getPackageName()) && UserHandle.isSameUser(expandedNotification.getUid(), notificationEntry.getSbn().getUid());
    }

    private void updateAppBadgeNum(String str, CharSequence charSequence, int i, int i2, boolean z) {
        String str2;
        if (str == null) {
            str2 = "";
        } else {
            str2 = str + "/" + ((Object) charSequence);
        }
        Intent intent = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
        intent.putExtra("android.intent.extra.update_application_message_text", i > 0 ? String.valueOf(i) : null);
        intent.putExtra("android.intent.extra.update_application_component_name", str2);
        intent.putExtra("userId", i2);
        intent.putExtra("targetPkg", charSequence);
        intent.putExtra("miui.intent.extra.application_show_corner", z);
        intent.setPackage("com.miui.home");
        Log.d("NotifBadge", "update app badge num: " + str2 + ",num=" + i + ",isAllowed=" + z + ",userId=" + i2);
        if (i2 == -1) {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    private CharSequence getMessageClassName(ExpandedNotification expandedNotification) {
        CharSequence messageClassName = expandedNotification.getMessageClassName();
        return messageClassName == null ? "" : messageClassName;
    }

    private boolean needStatBadgeNum(ExpandedNotification expandedNotification) {
        if (!"com.android.systemui".equals(expandedNotification.getPackageName()) && !MediaDataManagerKt.isMediaNotification(expandedNotification) && !NotificationUtil.hasProgressbar(expandedNotification) && !this.mGroupManager.isSummaryOfGroup(expandedNotification)) {
            return expandedNotification.isClearable();
        }
        return false;
    }

    public boolean needRestatBadgeNum(ExpandedNotification expandedNotification, ExpandedNotification expandedNotification2) {
        if (expandedNotification == null || expandedNotification2 == null) {
            return false;
        }
        return (expandedNotification.getMessageCount() != expandedNotification2.getMessageCount()) || needStatBadgeNum(expandedNotification) != needStatBadgeNum(expandedNotification2) || !TextUtils.equals(expandedNotification.getTargetPackageName(), expandedNotification2.getTargetPackageName());
    }
}
