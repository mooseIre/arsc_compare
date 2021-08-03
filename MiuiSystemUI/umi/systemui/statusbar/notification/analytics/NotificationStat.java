package com.android.systemui.statusbar.notification.analytics;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import codeinjection.CodeInjection;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.MiuiNotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.PushEvents;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.notification.unimportant.FoldTool;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl;
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
import com.miui.systemui.events.EnqueueEvent;
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
import com.miui.systemui.util.Md5;
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
import java.util.stream.Stream;
import org.json.JSONArray;

public class NotificationStat extends NotificationStatWithPlugin {
    private int mBatteryLevel;
    private Context mContext;
    private MiuiNotificationEntryManager mEntryManager;
    private EventTracker mEventTracker;
    private NotificationGroupManager mGroupManager;
    private HeadsUpManagerPhone mHeadsUpManager;
    private KeyguardStateController mKeyguardStateController;
    private NotificationPanelStat mPanelStat;
    private NotificationSettingsManager mSettingsManager;

    public NotificationStat(Context context, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, HeadsUpManagerPhone headsUpManagerPhone, StatusBarStateController statusBarStateController, KeyguardStateController keyguardStateController, EventTracker eventTracker, NotificationSettingsManager notificationSettingsManager, BatteryController batteryController) {
        this.mContext = context;
        this.mEntryManager = (MiuiNotificationEntryManager) notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mKeyguardStateController = keyguardStateController;
        this.mEventTracker = eventTracker;
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
        this.mSettingsManager = notificationSettingsManager;
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.notification.analytics.NotificationStat.AnonymousClass2 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onNotificationAdded(NotificationEntry notificationEntry) {
                NotificationStat.this.onArrive(notificationEntry, false);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPostEntryUpdated(NotificationEntry notificationEntry) {
                NotificationStat.this.onArrive(notificationEntry, true);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryInflated(NotificationEntry notificationEntry) {
                NotificationStat.this.handleEntryInflatedEvent(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryReinflated(NotificationEntry notificationEntry) {
                NotificationStat.this.handleEntryInflatedEvent(notificationEntry);
            }
        });
        this.mBatteryLevel = ((MiuiBatteryControllerImpl) batteryController).getBatteryLevel();
        batteryController.addCallback(new BatteryController.BatteryStateChangeCallback() {
            /* class com.android.systemui.statusbar.notification.analytics.NotificationStat.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
            public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
                NotificationStat.this.mBatteryLevel = i;
            }
        });
    }

    public void onArrive(NotificationEntry notificationEntry, boolean z) {
        handleEnqueueEvent(notificationEntry.getSbn(), z);
    }

    public void onPanelExpanded(boolean z, boolean z2, int i, int i2) {
        String str;
        if (this.mPanelStat == null) {
            this.mPanelStat = new NotificationPanelStat(this.mContext, this.mEventTracker);
            if (z) {
                str = "lockscreen";
            } else {
                str = CommonUtil.getTopActivityPkg(this.mContext);
            }
            this.mPanelStat.start(str, z2, i, i2);
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
        String notifSource = getNotifSource(notificationEntry, true);
        handleClickEvent(notificationEntry, notifSource);
        handleVisibleEventWhenClick(notificationEntry, notifSource);
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
        handleVisibleEventWhenRemove(notificationEntry, getNotifSource(notificationEntry));
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
            List<NotificationEntry> list3 = (List) this.mEntryManager.getFinalVisibleNotifications().stream().filter(new Predicate(list2) {
                /* class com.android.systemui.statusbar.notification.analytics.$$Lambda$NotificationStat$S3GDR4uAa3tOSJNzVzXiQJDHmI */
                public final /* synthetic */ List f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return this.f$0.contains(((NotificationEntry) obj).getKey());
                }
            }).collect(Collectors.toList());
            if (!list3.isEmpty()) {
                handleVisibleEvent(list3, getNotifSource(z, z2, null), true);
            }
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleEntryInflatedEvent(NotificationEntry notificationEntry) {
        if (notificationEntry != null && !NotificationUtil.isFoldEntrance(notificationEntry.getSbn()).booleanValue()) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            int notifId = getNotifId(notificationEntry);
            long notifTsId = getNotifTsId(notificationEntry);
            String generateTextId = generateTextId(sbn);
            String resolvePushMsgId = resolvePushMsgId(sbn);
            String notifPkg = getNotifPkg(notificationEntry);
            String notifTargetPkg = getNotifTargetPkg(notificationEntry);
            boolean containsBigPic = containsBigPic(sbn);
            boolean containCustomView = containCustomView(sbn);
            boolean containCustomAction = containCustomAction(sbn);
            int i = sbn.getNotification().priority;
            int i2 = this.mBatteryLevel;
            String channelId = getChannelId(notificationEntry.getSbn());
            int i3 = sbn.getNotification().flags;
            String pushUid = getPushUid(sbn);
            boolean isFold = NotificationUtil.isFold(notificationEntry.getSbn());
            int foldReason = NotificationUtil.getFoldReason(notificationEntry.getSbn());
            this.mEventTracker.track(new EnqueueEvent(notifId, notifTsId, generateTextId, resolvePushMsgId, notifPkg, notifTargetPkg, containsBigPic, containCustomView, containCustomAction, i, i2, channelId, i3, pushUid, isFold ? "fold" : CodeInjection.MD5, (!isFold || !FoldTool.INSTANCE.isAnalyzeBySdk(foldReason)) ? "OTHER" : "UNIMPORTANT", foldReason));
        }
    }

    private void handleEnqueueEvent(ExpandedNotification expandedNotification, boolean z) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sbn", expandedNotification);
        hashMap.put("update", Boolean.valueOf(z));
        onPluginEvent(this.mContext, "notification_enqueue", hashMap);
    }

    private void handleVisibleEventWhenRemove(NotificationEntry notificationEntry, String str) {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(notificationEntry);
        if (notificationEntry.isSummaryWithChildren()) {
            for (ExpandableNotificationRow expandableNotificationRow : notificationEntry.getRow().getAttachedChildren()) {
                if (expandableNotificationRow.getEntry().isVisual) {
                    arrayList.add(expandableNotificationRow.getEntry());
                }
            }
        }
        handleVisibleEvent(arrayList, str);
    }

    private void handleVisibleEventWhenClick(NotificationEntry notificationEntry, String str) {
        if (NotifSource.FLOAT.name().equalsIgnoreCase(str) && notificationEntry.isTopLevelChild()) {
            ArrayList arrayList = new ArrayList(1);
            arrayList.add(notificationEntry);
            handleVisibleEvent(arrayList, str);
        }
    }

    private void handleVisibleEvent(List<NotificationEntry> list, String str) {
        handleVisibleEvent(list, str, false);
    }

    private void handleVisibleEvent(List<NotificationEntry> list, String str, boolean z) {
        List list2 = (List) list.stream().map(new Function(z, str) {
            /* class com.android.systemui.statusbar.notification.analytics.$$Lambda$NotificationStat$Yi_37fmON2NUw2aTaWz2DoCRAA */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return NotificationStat.this.lambda$handleVisibleEvent$2$NotificationStat(this.f$1, this.f$2, (NotificationEntry) obj);
            }
        }).collect(Collectors.toList());
        if (list2.size() > 0) {
            this.mEventTracker.track(new VisibleEvent(str, list2, getScreenOrientation()));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleVisibleEvent$2 */
    public /* synthetic */ Map lambda$handleVisibleEvent$2$NotificationStat(boolean z, String str, NotificationEntry notificationEntry) {
        long currentTimeMillis = System.currentTimeMillis() - notificationEntry.getSbn().seeTime;
        int notifIndex = getNotifIndex(notificationEntry);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("source", z ? recalculateNotifSource(str, notificationEntry) : str);
        hashMap.put("index", Integer.valueOf(notifIndex));
        hashMap.put("visualPosition", Integer.valueOf(notificationEntry.visualPosition));
        hashMap.put("duration", Long.valueOf(currentTimeMillis));
        hashMap.put("sbn", notificationEntry.getSbn());
        hashMap.put("expanded", Boolean.valueOf(notificationEntry.getRow().isExpanded(true)));
        HashMap hashMap2 = new HashMap();
        hashMap2.put("ts_id", Long.valueOf(getNotifTsId(notificationEntry)));
        hashMap2.put("duration", Long.valueOf(System.currentTimeMillis() - notificationEntry.getSbn().seeTime));
        hashMap2.put("index", Integer.valueOf(getNotifIndex(notificationEntry)));
        if (notificationEntry.getSbn().getNotification().isGroupSummary()) {
            hashMap2.put("items", new JSONArray((Collection) getChildrenPostTime(notificationEntry)).toString());
            hashMap.put("item_sbns", getChildrenSbn(notificationEntry));
        } else {
            List<Long> entranceChildrenIds = getEntranceChildrenIds(notificationEntry);
            if (!entranceChildrenIds.isEmpty()) {
                hashMap2.put("items", new JSONArray((Collection) entranceChildrenIds).toString());
                hashMap.put("item_ts_ids", entranceChildrenIds);
            }
        }
        notificationEntry.getSbn().seeTime = 0;
        notificationEntry.getSbn().mSeenByPanel = str.equals(NotifSource.PANEL.name());
        hashMap2.put("is_group", Integer.valueOf(getIsNotificationGrouped(notificationEntry)));
        hashMap2.put("is_priority", Integer.valueOf(getIsPriority(notificationEntry)));
        hashMap2.put("mipush_class", Integer.valueOf(getMipushClass(notificationEntry)));
        hashMap2.put("is_section", Integer.valueOf(inImportantSection(notificationEntry)));
        hashMap2.put("visualPosition", Integer.valueOf(notificationEntry.visualPosition));
        onPluginEvent(this.mContext, "notification_visible", hashMap);
        return hashMap2;
    }

    private List<Long> getChildrenPostTime(NotificationEntry notificationEntry) {
        return (List) getChildrenStream(notificationEntry).map($$Lambda$NotificationStat$Br2VQCRwfb3_peyZko2_ir2kkgc.INSTANCE).collect(Collectors.toList());
    }

    private List<ExpandedNotification> getChildrenSbn(NotificationEntry notificationEntry) {
        return (List) getChildrenStream(notificationEntry).map($$Lambda$NotificationStat$r2tCs72MHgwIZy_YYk117do5p9U.INSTANCE).collect(Collectors.toList());
    }

    private Stream<NotificationEntry> getChildrenStream(NotificationEntry notificationEntry) {
        if (this.mGroupManager.isSummaryOfGroup(notificationEntry.getSbn())) {
            return this.mGroupManager.getChildren(notificationEntry.getSbn()).stream();
        }
        return Stream.empty();
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

    private void handleClickEvent(NotificationEntry notificationEntry, String str) {
        boolean equals = "com.miui.notification".equals(notificationEntry.getSbn().getOpPkg());
        String notifPkg = getNotifPkg(notificationEntry);
        String notifTargetPkg = getNotifTargetPkg(notificationEntry);
        long notifTsId = getNotifTsId(notificationEntry);
        int notifIndex = getNotifIndex(notificationEntry);
        boolean notifClearable = getNotifClearable(notificationEntry);
        int notifIndex2 = getNotifIndex(notificationEntry);
        String tag = equals ? notificationEntry.getSbn().getTag() : CodeInjection.MD5;
        int isNotificationGrouped = getIsNotificationGrouped(notificationEntry);
        NotificationPanelStat notificationPanelStat = this.mPanelStat;
        ClickEvent clickEvent = new ClickEvent(notifPkg, notifTargetPkg, notifTsId, notifIndex, notifClearable, str, notifIndex2, tag, isNotificationGrouped, notificationPanelStat == null ? -1 : notificationPanelStat.getPanelSlidingTimes(), getIsPriority(notificationEntry), getMipushClass(notificationEntry), inImportantSection(notificationEntry), getCategory(notificationEntry.getSbn()), getChannelId(notificationEntry.getSbn()));
        this.mEventTracker.track(clickEvent);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("index", Integer.valueOf(clickEvent.getIndex()));
        hashMap.put("source", clickEvent.getSource());
        hashMap.put("sbn", notificationEntry.getSbn());
        onPluginEvent(this.mContext, "notification_click", hashMap);
    }

    private void handleExpansionChangedEvent(NotificationEntry notificationEntry, boolean z, boolean z2) {
        ExpansionEvent expansionEvent = new ExpansionEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), getNotifIndex(notificationEntry), z, z2);
        this.mEventTracker.track(expansionEvent);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("index", Integer.valueOf(expansionEvent.getIndex()));
        hashMap.put("user_action", Boolean.valueOf(z));
        hashMap.put("expanded", Boolean.valueOf(z2));
        hashMap.put("sbn", notificationEntry.getSbn());
        onPluginEvent(this.mContext, "notification_expansion_changed", hashMap);
    }

    private void handleBlockEvent(String str, String str2) {
        this.mEventTracker.track(new BlockEvent(str, str, -1, -1, false, NotifSource.SETTINGS.name(), -1, str2));
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("index", -1);
        hashMap.put("source", NotifSource.SETTINGS.name());
        hashMap.put("channel_id", str2);
        onPluginEvent(this.mContext, "notification_block", hashMap);
    }

    private void handleBlockEvent(NotificationEntry notificationEntry) {
        BlockEvent blockEvent = new BlockEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifIndex(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry), getChannelId(notificationEntry.getSbn()));
        this.mEventTracker.track(blockEvent);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("index", Integer.valueOf(blockEvent.getIndex()));
        hashMap.put("sbn", notificationEntry.getSbn());
        hashMap.put("source", blockEvent.getSource());
        hashMap.put("channel_id", blockEvent.getChannelId());
        onPluginEvent(this.mContext, "notification_block", hashMap);
    }

    private void handleCancelEvent(NotificationEntry notificationEntry, int i) {
        CancelEvent cancelEvent = new CancelEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), i, getIsNotificationGrouped(notificationEntry), getIsPriority(notificationEntry), getMipushClass(notificationEntry), inImportantSection(notificationEntry), getCategory(notificationEntry.getSbn()), getChannelId(notificationEntry.getSbn()));
        this.mEventTracker.track(cancelEvent);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("index", Integer.valueOf(cancelEvent.getIndex()));
        hashMap.put("sbn", notificationEntry.getSbn());
        hashMap.put("source", cancelEvent.getSource());
        onPluginEvent(this.mContext, "notification_cancel", hashMap);
    }

    private void handleCancelAllEvent(int i) {
        NotifSource notifSource;
        EventTracker eventTracker = this.mEventTracker;
        String name = ClearAllMode.CLEAR_ALL.name();
        if (FoldManager.Companion.isShowingUnimportant()) {
            notifSource = NotifSource.UNIMPORTANT;
        } else {
            notifSource = NotifSource.PANEL;
        }
        eventTracker.track(new CancelAllEvent(name, i, 1, notifSource.name()));
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("notifications_count", Integer.valueOf(i));
        onPluginEvent(this.mContext, "notification_cancel_all", hashMap);
    }

    private void handleMenuOpenEvent(NotificationEntry notificationEntry) {
        MenuOpenEvent menuOpenEvent = new MenuOpenEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), getNotifSource(notificationEntry), getNotifIndex(notificationEntry), getIsNotificationGrouped(notificationEntry), getIsPriority(notificationEntry), getMipushClass(notificationEntry), inImportantSection(notificationEntry));
        this.mEventTracker.track(menuOpenEvent);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("index", Integer.valueOf(menuOpenEvent.getIndex()));
        hashMap.put("sbn", notificationEntry.getSbn());
        hashMap.put("source", menuOpenEvent.getSource());
        onPluginEvent(this.mContext, "notification_open_menu", hashMap);
    }

    private void handleSetConfigEvent(NotificationEntry notificationEntry) {
        SetConfigEvent setConfigEvent = new SetConfigEvent(getNotifPkg(notificationEntry), getNotifTargetPkg(notificationEntry), getNotifTsId(notificationEntry), getNotifStyle(notificationEntry), getNotifClearable(notificationEntry), -1, NotificationUtil.getBucket(), NotifSource.PANEL.name());
        this.mEventTracker.track(setConfigEvent);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sbn", notificationEntry.getSbn());
        hashMap.put("config_value", -1);
        hashMap.put("bucket", Integer.valueOf(setConfigEvent.getBucket()));
        hashMap.put("source", setConfigEvent.getSource());
        onPluginEvent(this.mContext, "notification_set_config", hashMap);
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

    private int getNotifId(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().getId();
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
        return this.mEntryManager.getFinalVisibleNotifications().indexOf(notificationEntry) + 1;
    }

    private String getNotifSource(NotificationEntry notificationEntry) {
        return getNotifSource(notificationEntry, false);
    }

    private String getNotifSource(NotificationEntry notificationEntry, boolean z) {
        String notifSource = getNotifSource(this.mHeadsUpManager.isAlerting(notificationEntry.getKey()), this.mKeyguardStateController.isShowing(), notificationEntry);
        return (!z || !NotifSource.PANEL.name().equalsIgnoreCase(notifSource) || !notificationEntry.isVisualInFloat) ? notifSource : NotifSource.FLOAT.name();
    }

    private String getNotifSource(boolean z, boolean z2, NotificationEntry notificationEntry) {
        if (z) {
            return NotifSource.FLOAT.name();
        }
        if (z2) {
            return NotifSource.KEYGUARD.name();
        }
        if (isEntryFold(notificationEntry)) {
            return NotifSource.UNIMPORTANT.name();
        }
        return NotifSource.PANEL.name();
    }

    private String recalculateNotifSource(String str, NotificationEntry notificationEntry) {
        return isEntryFold(notificationEntry) ? NotifSource.UNIMPORTANT.name() : str;
    }

    private boolean isEntryFold(NotificationEntry notificationEntry) {
        return notificationEntry != null && NotificationUtil.isFold(notificationEntry.getSbn());
    }

    private int getIsNotificationGrouped(NotificationEntry notificationEntry) {
        return (notificationEntry.isChildInGroup() || notificationEntry.getSbn().getNotification().isGroupSummary()) ? 1 : 0;
    }

    private int getIsPriority(NotificationEntry notificationEntry) {
        String notifPkg = getNotifPkg(notificationEntry);
        if (isImportantPkg(notifPkg)) {
            return 1;
        }
        if (notifPkg.equals("com.xiaomi.xmsf")) {
            return notificationEntry.getSbn().getNotification().extras.getInt("is_priority", -1);
        }
        return -1;
    }

    private int getMipushClass(NotificationEntry notificationEntry) {
        String notifPkg = getNotifPkg(notificationEntry);
        if (isImportantPkg(notifPkg)) {
            return 1;
        }
        if (notifPkg.equals("com.xiaomi.xmsf")) {
            Bundle bundle = notificationEntry.getSbn().getNotification().extras;
            if (bundle.containsKey("mipush_class")) {
                int i = bundle.getInt("mipush_class");
                if (i < 1 || i > 8) {
                    return 0;
                }
                return i;
            }
        }
        return -1;
    }

    private NotificationEntry getNotifEntry(String str) {
        return this.mEntryManager.getActiveNotificationUnfiltered(str);
    }

    private int getScreenOrientation() {
        return this.mContext.getResources().getConfiguration().orientation;
    }

    private boolean isImportantPkg(String str) {
        return this.mSettingsManager.isImportantWhitelist(str);
    }

    private int inImportantSection(NotificationEntry notificationEntry) {
        return notificationEntry.getSbn().isImportant() ? 1 : 0;
    }

    private String getChannelId(ExpandedNotification expandedNotification) {
        String channelId = expandedNotification.getNotification().getChannelId();
        return channelId != null ? channelId : CodeInjection.MD5;
    }

    private String generateTextId(ExpandedNotification expandedNotification) {
        Notification notification = expandedNotification.getNotification();
        return Md5.getMd5Digest(NotificationUtil.resolveTitle(notification).toString() + ((Object) NotificationUtil.resolveText(notification)));
    }

    private String resolvePushMsgId(ExpandedNotification expandedNotification) {
        return NotificationUtil.resolvePushMsgId(expandedNotification.getNotification());
    }

    private boolean containsBigPic(ExpandedNotification expandedNotification) {
        return NotificationUtil.containsBigPic(expandedNotification.getNotification());
    }

    private boolean containCustomView(ExpandedNotification expandedNotification) {
        return NotificationUtil.containCustomView(expandedNotification.getNotification());
    }

    private boolean containCustomAction(ExpandedNotification expandedNotification) {
        return expandedNotification.getNotification().actions != null && expandedNotification.getNotification().actions.length > 0;
    }

    private String getPushUid(ExpandedNotification expandedNotification) {
        return NotificationUtil.getPushUid(expandedNotification.getNotification());
    }

    private String getCategory(ExpandedNotification expandedNotification) {
        return (!NotificationUtil.isFold(expandedNotification) || !FoldTool.INSTANCE.isAnalyzeBySdk(NotificationUtil.getFoldReason(expandedNotification))) ? "OTHER" : "UNIMPORTANT";
    }
}
