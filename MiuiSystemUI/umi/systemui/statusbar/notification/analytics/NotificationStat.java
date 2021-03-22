package com.android.systemui.statusbar.notification.analytics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.PushEvents;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.BlockEvent;
import com.miui.systemui.events.CancelAllEvent;
import com.miui.systemui.events.CancelEvent;
import com.miui.systemui.events.ClearAllMode;
import com.miui.systemui.events.ClickAllowNotificationEvent;
import com.miui.systemui.events.ClickEvent;
import com.miui.systemui.events.ClickMoreEvent;
import com.miui.systemui.events.ClickSetUnimportant;
import com.miui.systemui.events.ClickSnoozeDialog;
import com.miui.systemui.events.ExitModalEvent;
import com.miui.systemui.events.ExpansionEvent;
import com.miui.systemui.events.FloatAutoCollapseEvent;
import com.miui.systemui.events.FloatManualCollapseEvent;
import com.miui.systemui.events.GroupCollapseEvent;
import com.miui.systemui.events.GroupExpandEvent;
import com.miui.systemui.events.MediaStrokeEvent;
import com.miui.systemui.events.MenuOpenEvent;
import com.miui.systemui.events.ModalDialogCancelEvent;
import com.miui.systemui.events.ModalDialogConfirmEvent;
import com.miui.systemui.events.NotifSource;
import com.miui.systemui.events.NotificationPanelSlideEvent;
import com.miui.systemui.events.SetConfigEvent;
import com.miui.systemui.events.SnoozeToastClick;
import com.miui.systemui.events.SnoozeToastVisible;
import com.miui.systemui.events.VisibleEvent;
import com.miui.systemui.util.CommonUtil;
import java.util.ArrayList;
import java.util.Collection;
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
    private NotificationPanelStat mPanelStat;

    public NotificationStat(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, HeadsUpManagerPhone headsUpManagerPhone, StatusBarStateController statusBarStateController, KeyguardStateController keyguardStateController, EventTracker eventTracker, NotificationPanelStat notificationPanelStat) {
        this.mContext = context;
        this.mEntryManager = notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mKeyguardStateController = keyguardStateController;
        this.mEventTracker = eventTracker;
        this.mPanelStat = notificationPanelStat;
        notificationGroupManager.addOnGroupChangeListener(new NotificationGroupManager.OnGroupChangeListener() {
            /* class com.android.systemui.statusbar.notification.analytics.NotificationStat.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupExpansionChanged(ExpandableNotificationRow expandableNotificationRow, boolean z) {
                if (z) {
                    NotificationStat.this.onGroupExpand(expandableNotificationRow.getEntry(), NotificationStat.this.mGroupManager.getChildren(expandableNotificationRow.getEntry().getSbn()).size());
                } else {
                    NotificationStat.this.onGroupCollapse(expandableNotificationRow.getEntry(), NotificationStat.this.mGroupManager.getChildren(expandableNotificationRow.getEntry().getSbn()).size());
                }
            }
        });
    }

    public void onPanelExpanded(boolean z, boolean z2, int i) {
        String str;
        if (this.mPanelStat == null) {
            this.mPanelStat = new NotificationPanelStat(this.mContext, this.mEventTracker);
            if (z) {
                str = "lockscreen";
            } else {
                str = CommonUtil.getTopActivityPkg(this.mContext);
            }
            this.mPanelStat.start(str, z2, i);
        }
    }

    public void onPanelCollapsed(boolean z, int i) {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.end(z, i);
            this.mPanelStat = null;
        }
    }

    public void onClick(NotificationEntry notificationEntry) {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markClick();
        }
        handleClickEvent(notificationEntry);
        handleVisibleEvent(notificationEntry.getKey(), getNotifSource(notificationEntry));
    }

    public void onExpansionChanged(String str, boolean z, boolean z2) {
        NotificationEntry notifEntry = getNotifEntry(str);
        if (notifEntry != null) {
            if ("com.miui.systemAdSolution".equals(notifEntry.getSbn().getOpPkg()) || "com.miui.msa.global".equals(notifEntry.getSbn().getOpPkg())) {
                handleExpansionChangedEvent(notifEntry, z, z2);
            }
        }
    }

    public void onRemove(NotificationEntry notificationEntry) {
        ArrayList<NotificationEntry> children;
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markRemove();
        }
        int notifIndex = getNotifIndex(notificationEntry);
        onRemoveSingle(notificationEntry, notifIndex);
        if (this.mGroupManager.isSummaryOfGroup(notificationEntry.getSbn()) && (children = this.mGroupManager.getChildren(notificationEntry.getSbn())) != null) {
            for (NotificationEntry notificationEntry2 : children) {
                onRemoveSingle(notificationEntry2, notifIndex);
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
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markRemoveAll();
            this.mPanelStat.markRemove();
        }
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

    public void onOpenQSPanel() {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markOpenQSPanel();
        }
    }

    public void onClickQSTitle() {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markClickQS();
        }
    }

    public void onSlideBrightnessBar() {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markChangeBrightness();
        }
    }

    public void onMediaStroke(String str) {
        handleMediaStrokeEvent(str);
    }

    public void onGroupExpand(NotificationEntry notificationEntry, int i) {
        handleGroupExpandEvent(notificationEntry, i);
    }

    public void onGroupCollapse(NotificationEntry notificationEntry, int i) {
        handleGroupCollapseEvent(notificationEntry, i);
    }

    public void onFloatAutoCollapse(NotificationEntry notificationEntry) {
        handleFloatAutoCollapseEvent(notificationEntry);
    }

    public void onFloatManualCollapse(NotificationEntry notificationEntry, boolean z) {
        handleFloatManualCollapse(notificationEntry, !z ? 1 : 0);
    }

    public void onClickAllowNotification(NotificationEntry notificationEntry) {
        handleClickAllowNotificationEvent(notificationEntry);
    }

    public void onClickSetUnimportant(NotificationEntry notificationEntry) {
        handleClickSetUnimportantEvent(notificationEntry);
    }

    public void onClickMore(NotificationEntry notificationEntry) {
        handleClickMoreEvent(notificationEntry);
    }

    public void onExitModal(NotificationEntry notificationEntry, String str) {
        handleExitModalEvent(notificationEntry, str);
    }

    public void onModalDialogConfirm(NotificationEntry notificationEntry, String str) {
        handleModalDialogConfirmEvent(notificationEntry, str);
    }

    public void onModalDialogCancel(NotificationEntry notificationEntry, String str, String str2) {
        handleModalDialogCancelEvent(notificationEntry, str, str2);
    }

    public void onNotificationPanelSliding(String str, String str2) {
        handleNotificationPanelSlidingEvent(str, str2);
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.markSlidingTimes();
        }
    }

    public void onHomePressed() {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.onHomePressed();
        }
    }

    public void onBackPressed() {
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        if (notificationPanelStat != null) {
            notificationPanelStat.onBackPressed();
        }
    }

    public void onSnoozeToastVisible() {
        handleSnoozeToastVisibleEvent();
    }

    public void onSnoozeToastClick() {
        handleSnoozeToastClickEvent();
    }

    public void onSnoozeDialogClick(int i) {
        handleSnoozeDialogClick(i);
    }

    public void logVisibilityChanges(List<String> list, List<String> list2, boolean z, boolean z2) {
        list.forEach(new Consumer() {
            /* class com.android.systemui.statusbar.notification.analytics.$$Lambda$NotificationStat$h58jkTJPtvADAYsXdsJA1kVW9lE */

            @Override // java.util.function.Consumer
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
        handleVisibleEvent(arrayList, str2);
    }

    private void handleVisibleEvent(List<String> list, String str) {
        List list2 = (List) this.mEntryManager.getVisibleNotifications().stream().filter(new Predicate(list) {
            /* class com.android.systemui.statusbar.notification.analytics.$$Lambda$NotificationStat$57_sNF2n7Ax5LcAxQDxEiKpAB4Y */
            public final /* synthetic */ List f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return this.f$0.contains(((NotificationEntry) obj).getKey());
            }
        }).map(new Function() {
            /* class com.android.systemui.statusbar.notification.analytics.$$Lambda$NotificationStat$sqwH11aQ0BBdkw0RiRU2q_c1VZ8 */

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NotificationStat.this.lambda$handleVisibleEvent$2$NotificationStat((NotificationEntry) obj);
            }
        }).collect(Collectors.toList());
        if (list2.size() > 0) {
            this.mEventTracker.track(new VisibleEvent(str, list2, getScreenOrientation()));
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
            hashMap.put("items", new JSONArray((Collection) getChildrenPostTime(notificationEntry)).toString());
        } else {
            List<Long> entranceChildrenIds = getEntranceChildrenIds(notificationEntry);
            if (!entranceChildrenIds.isEmpty()) {
                hashMap.put("items", new JSONArray((Collection) entranceChildrenIds).toString());
            }
        }
        notificationEntry.getSbn().seeTime = 0;
        hashMap.put("is_group", Integer.valueOf(getIsNotificationGrouped(notificationEntry)));
        hashMap.put("is_priority", Integer.valueOf(getIsPriority(notificationEntry)));
        hashMap.put("mipush_class", Integer.valueOf(getMipushClass(notificationEntry)));
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
        for (long j : longArray) {
            arrayList.add(Long.valueOf(j));
        }
        return arrayList;
    }

    private void handleClickEvent(NotificationEntry notificationEntry) {
        boolean equals = "com.miui.notification".equals(notificationEntry.getSbn().getOpPkg());
        EventTracker eventTracker = this.mEventTracker;
        String notifPkg = getNotifPkg(notificationEntry);
        String notifTargetPkg = getNotifTargetPkg(notificationEntry);
        long notifTsId = getNotifTsId(notificationEntry);
        int notifIndex = getNotifIndex(notificationEntry);
        boolean notifClearable = getNotifClearable(notificationEntry);
        String notifSource = getNotifSource(notificationEntry);
        int notifIndex2 = getNotifIndex(notificationEntry);
        String tag = equals ? notificationEntry.getSbn().getTag() : "";
        int isNotificationGrouped = getIsNotificationGrouped(notificationEntry);
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        eventTracker.track(new ClickEvent(notifPkg, notifTargetPkg, notifTsId, notifIndex, notifClearable, notifSource, notifIndex2, tag, isNotificationGrouped, notificationPanelStat == null ? -1 : notificationPanelStat.getPanelSlidingTimes(), getIsPriority(notificationEntry), getMipushClass(notificationEntry)));
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
        this.mEventTracker.track(new CancelEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), i, getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry), getIsNotificationGrouped(notificationEntry), getIsPriority(notificationEntry), getMipushClass(notificationEntry)));
    }

    private void handleCancelAllEvent(int i) {
        this.mEventTracker.track(new CancelAllEvent(ClearAllMode.CLEAR_ALL.name(), i, 1, NotifSource.PANEL.name()));
    }

    private void handleMenuOpenEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new MenuOpenEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry), getIsNotificationGrouped(notificationEntry), getIsPriority(notificationEntry), getMipushClass(notificationEntry)));
    }

    private void handleSetConfigEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new SetConfigEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), -1, NotificationUtil.getBucket(), "panel"));
    }

    private void handleGroupExpandEvent(NotificationEntry notificationEntry, int i) {
        this.mEventTracker.track(new GroupExpandEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), i));
    }

    private void handleGroupCollapseEvent(NotificationEntry notificationEntry, int i) {
        this.mEventTracker.track(new GroupCollapseEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), i));
    }

    private void handleFloatAutoCollapseEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new FloatAutoCollapseEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry), getScreenOrientation()));
    }

    private void handleFloatManualCollapse(NotificationEntry notificationEntry, int i) {
        this.mEventTracker.track(new FloatManualCollapseEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry), i, getScreenOrientation()));
    }

    private void handleClickAllowNotificationEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new ClickAllowNotificationEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry)));
    }

    private void handleClickSetUnimportantEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new ClickSetUnimportant(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry)));
    }

    private void handleClickMoreEvent(NotificationEntry notificationEntry) {
        this.mEventTracker.track(new ClickMoreEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry)));
    }

    private void handleMediaStrokeEvent(String str) {
        this.mEventTracker.track(new MediaStrokeEvent(str));
    }

    private void handleExitModalEvent(NotificationEntry notificationEntry, String str) {
        this.mEventTracker.track(new ExitModalEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry), str));
    }

    private void handleModalDialogConfirmEvent(NotificationEntry notificationEntry, String str) {
        this.mEventTracker.track(new ModalDialogConfirmEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry), str));
    }

    private void handleModalDialogCancelEvent(NotificationEntry notificationEntry, String str, String str2) {
        this.mEventTracker.track(new ModalDialogCancelEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifStyle(notificationEntry), str, str2));
    }

    private void handleNotificationPanelSlidingEvent(String str, String str2) {
        this.mEventTracker.track(new NotificationPanelSlideEvent(str, str2));
    }

    private void handleSnoozeToastVisibleEvent() {
        this.mEventTracker.track(new SnoozeToastVisible());
    }

    private void handleSnoozeToastClickEvent() {
        this.mEventTracker.track(new SnoozeToastClick());
    }

    private void handleSnoozeDialogClick(int i) {
        this.mEventTracker.track(new ClickSnoozeDialog(i));
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

    private int getIsNotificationGrouped(NotificationEntry notificationEntry) {
        return (notificationEntry.isChildInGroup() || notificationEntry.getSbn().getNotification().isGroupSummary()) ? 1 : 0;
    }

    private int getIsPriority(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getNotification().extras.getInt("is_priority", -1);
    }

    private int getMipushClass(NotificationEntry notificationEntry) {
        Bundle bundle = notificationEntry.getSbn().getNotification().extras;
        if (!bundle.containsKey("mipush_class")) {
            return -1;
        }
        int i = bundle.getInt("mipush_class");
        if (i < 1 || i > 8) {
            return 0;
        }
        return i;
    }

    private NotificationEntry getNotifEntry(String str) {
        return this.mEntryManager.getActiveNotificationUnfiltered(str);
    }

    private int getScreenOrientation() {
        return this.mContext.getResources().getConfiguration().orientation;
    }
}
