package com.android.systemui.statusbar.notification.analytics;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.PushEvents;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.BlockEvent;
import com.miui.systemui.events.CancelAllEvent;
import com.miui.systemui.events.CancelEvent;
import com.miui.systemui.events.ClearAllMode;
import com.miui.systemui.events.ClickEvent;
import com.miui.systemui.events.ExpansionEvent;
import com.miui.systemui.events.MenuOpenEvent;
import com.miui.systemui.events.NotifSource;
import com.miui.systemui.events.SetConfigEvent;
import com.miui.systemui.events.VisibleEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.json.JSONArray;

public class NotificationStat {
    private Context mContext;
    private NotificationEntryManager mEntryManager;
    private EventTracker mEventTracker;
    private NotificationGroupManager mGroupManager;
    private HeadsUpManagerPhone mHeadsUpManager;
    private KeyguardStateController mKeyguardStateController;

    public NotificationStat(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, HeadsUpManagerPhone headsUpManagerPhone, StatusBarStateController statusBarStateController, KeyguardStateController keyguardStateController, EventTracker eventTracker) {
        this.mContext = context;
        this.mEntryManager = notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mKeyguardStateController = keyguardStateController;
        this.mEventTracker = eventTracker;
    }

    public void onClick(NotificationEntry notificationEntry) {
        handleClickEvent(notificationEntry);
        handleVisibleEvent(notificationEntry.getKey(), getNotifSource(notificationEntry));
    }

    public void onExpansionChanged(String str, boolean z, boolean z2) {
        NotificationEntry notifEntry = getNotifEntry(str);
        if ("com.miui.systemAdSolution".equals(notifEntry.getSbn().getOpPkg()) || "com.miui.msa.global".equals(notifEntry.getSbn().getOpPkg())) {
            handleExpansionChangedEvent(notifEntry, z, z2);
        }
    }

    public void onRemove(NotificationEntry notificationEntry) {
        ArrayList<NotificationEntry> children;
        int notifIndex = getNotifIndex(notificationEntry);
        onRemoveSingle(notificationEntry, notifIndex);
        if (this.mGroupManager.isSummaryOfGroup(notificationEntry.getSbn()) && (children = this.mGroupManager.getChildren(notificationEntry.getSbn())) != null) {
            for (NotificationEntry onRemoveSingle : children) {
                onRemoveSingle(onRemoveSingle, notifIndex);
                notifIndex++;
            }
        }
    }

    private void onRemoveSingle(NotificationEntry notificationEntry, int i) {
        handleCancelEvent(notificationEntry, i);
        handleVisibleEvent(notificationEntry.getKey(), getNotifSource(notificationEntry));
        AdTracker.trackRemove(this.mContext, notificationEntry);
    }

    public void onRemoveAll(int i) {
        handleCancelAllEvent(i);
    }

    public void onBlock(NotificationEntry notificationEntry) {
        sendBlockNotificationEvent(notificationEntry.getSbn().getPackageName(), PushEvents.getADId(notificationEntry.getSbn().getNotification()));
        handleBlockEvent(notificationEntry);
    }

    public void onBlock(String str, String str2, String str3) {
        if (!"com.miui.systemAdSolution".equals(str) && !"com.miui.msa.global".equals(str)) {
            str3 = null;
        }
        sendBlockNotificationEvent(str, str3);
        handleBlockEvent(str, str2);
    }

    public void onOpenMenu(NotificationEntry notificationEntry) {
        handleMenuOpenEvent(notificationEntry);
    }

    public void onSetConfig(NotificationEntry notificationEntry) {
        handleSetConfigEvent(notificationEntry);
    }

    public void logVisibilityChanges(List<String> list, List<String> list2, boolean z, boolean z2) {
        list.forEach(new Consumer() {
            public final void accept(Object obj) {
                NotificationStat.this.lambda$logVisibilityChanges$0$NotificationStat((String) obj);
            }
        });
        if (!list2.isEmpty()) {
            handleVisibleEvent(list2, getNotifSource(z, z2));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$logVisibilityChanges$0 */
    public /* synthetic */ void lambda$logVisibilityChanges$0$NotificationStat(String str) {
        NotificationEntry activeNotificationUnfiltered = this.mEntryManager.getActiveNotificationUnfiltered(str);
        if (activeNotificationUnfiltered != null) {
            AdTracker.trackShow(this.mContext, activeNotificationUnfiltered);
        }
    }

    private void sendBlockNotificationEvent(String str, String str2) {
        if ("com.miui.systemAdSolution".equals(str) && !TextUtils.isEmpty(str2)) {
            Intent intent = new Intent("miui.intent.adblock");
            intent.setPackage("com.miui.systemAdSolution");
            intent.putExtra("adid", str2);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    private void handleVisibleEvent(String str, String str2) {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(str);
        handleVisibleEvent((List<String>) arrayList, str2);
    }

    private void handleVisibleEvent(List<String> list, String str) {
        List list2 = (List) this.mEntryManager.getVisibleNotifications().stream().filter(new Predicate(list) {
            public final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.contains(((NotificationEntry) obj).getKey());
            }
        }).map(new Function() {
            public final Object apply(Object obj) {
                return NotificationStat.this.lambda$handleVisibleEvent$2$NotificationStat((NotificationEntry) obj);
            }
        }).collect(Collectors.toList());
        if (list2.size() > 0) {
            this.mEventTracker.track(new VisibleEvent(str, list2));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleVisibleEvent$2 */
    public /* synthetic */ Map lambda$handleVisibleEvent$2$NotificationStat(NotificationEntry notificationEntry) {
        HashMap hashMap = new HashMap();
        hashMap.put("ts_id", Long.valueOf(getNotifTsId(notificationEntry)));
        hashMap.put("duration", Long.valueOf(System.currentTimeMillis() - notificationEntry.getSbn().seeTime));
        hashMap.put("index", Integer.valueOf(getNotifIndex(notificationEntry)));
        if (notificationEntry.getSbn().getNotification().isGroupSummary()) {
            hashMap.put("items", new JSONArray(getChildrenPostTime(notificationEntry)).toString());
        } else {
            List<Long> entranceChildrenIds = getEntranceChildrenIds(notificationEntry);
            if (!entranceChildrenIds.isEmpty()) {
                hashMap.put("items", new JSONArray(entranceChildrenIds).toString());
            }
        }
        notificationEntry.getSbn().seeTime = 0;
        return hashMap;
    }

    private List<Long> getChildrenPostTime(NotificationEntry notificationEntry) {
        if (this.mGroupManager.isSummaryOfGroup(notificationEntry.getSbn())) {
            return (List) this.mGroupManager.getChildren(notificationEntry.getSbn()).stream().map($$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc.INSTANCE).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<Long> getEntranceChildrenIds(NotificationEntry notificationEntry) {
        long[] longArray = notificationEntry.getSbn().getNotification().extras.getLongArray("miui.inner_notifications");
        if (!("com.miui.notification".equals(notificationEntry.getSbn().getOpPkg()) && longArray != null)) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(longArray.length);
        for (long valueOf : longArray) {
            arrayList.add(Long.valueOf(valueOf));
        }
        return arrayList;
    }

    private void handleClickEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new ClickEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifIndex(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry), "com.miui.notification".equals(notificationEntry.getSbn().getOpPkg()) ? notificationEntry.getSbn().getTag() : ""));
    }

    private void handleExpansionChangedEvent(NotificationEntry notificationEntry, boolean z, boolean z2) {
        this.mEventTracker.track(new ExpansionEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifIndex(notificationEntry), getNotifClearable(notificationEntry), getNotifIndex(notificationEntry), z, z2));
    }

    private void handleBlockEvent(String str, String str2) {
        this.mEventTracker.track(new BlockEvent(str, str, -1, -1, false, NotifSource.SETTINGS.name(), -1, str2));
    }

    private void handleBlockEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new BlockEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifIndex(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry), notificationEntry.getSbn().getNotification().getChannelId()));
    }

    private void handleCancelEvent(NotificationEntry notificationEntry, int i) {
        this.mEventTracker.track(new CancelEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), i, getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry)));
    }

    private void handleCancelAllEvent(int i) {
        this.mEventTracker.track(new CancelAllEvent(ClearAllMode.CLEAR_ALL.name(), i, 1, NotifSource.PANEL.name()));
    }

    private void handleMenuOpenEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new MenuOpenEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry)));
    }

    private void handleSetConfigEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new SetConfigEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), -1, NotificationUtil.getBucket(), "panel"));
    }

    private String getNotifPkg(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getOpPkg();
    }

    private String getNotifTargetPkg(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getPackageName();
    }

    private long getNotifTsId(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getPostTime();
    }

    private String getNotifStyle(NotificationEntry notificationEntry) {
        String string = notificationEntry.getSbn().getNotification().extras.getString("android.template");
        if (TextUtils.isEmpty(string)) {
            return "Normal";
        }
        int lastIndexOf = string.lastIndexOf("$");
        return lastIndexOf > 0 ? string.substring(lastIndexOf + 1) : "Unknown";
    }

    private boolean getNotifClearable(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().isClearable();
    }

    private int getNotifIndex(NotificationEntry notificationEntry) {
        return this.mEntryManager.getVisibleNotifications().indexOf(notificationEntry) + 1;
    }

    private String getNotifSource(NotificationEntry notificationEntry) {
        return getNotifSource(this.mHeadsUpManager.isAlerting(notificationEntry.getKey()), this.mKeyguardStateController.isShowing());
    }

    private String getNotifSource(boolean z, boolean z2) {
        if (z) {
            return NotifSource.FLOAT.name();
        }
        if (z2) {
            return NotifSource.KEYGUARD.name();
        }
        return NotifSource.PANEL.name();
    }

    private NotificationEntry getNotifEntry(String str) {
        return this.mEntryManager.getActiveNotificationUnfiltered(str);
    }
}
