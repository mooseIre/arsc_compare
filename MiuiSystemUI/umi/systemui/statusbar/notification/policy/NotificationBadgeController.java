package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.media.MediaDataManagerKt;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NotificationBadgeController {
    Context mContext;
    NotificationEntryManager mEntryManager;
    NotificationGroupManager mGroupManager;

    public NotificationBadgeController(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager) {
        this.mContext = context;
        this.mEntryManager = notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
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
            str2 = str + "/" + charSequence;
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
        if ((expandedNotification.getMessageCount() != expandedNotification2.getMessageCount()) || needStatBadgeNum(expandedNotification) != needStatBadgeNum(expandedNotification2) || !TextUtils.equals(expandedNotification.getTargetPackageName(), expandedNotification2.getTargetPackageName())) {
            return true;
        }
        return false;
    }
}
