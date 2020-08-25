package com.android.systemui.miui.statusbar.analytics;

import android.app.NotificationChannelCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.internal.os.SomeArgs;
import com.android.systemui.AdTracker;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.Util;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.PushEvents;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.StatusBar;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.c.b;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;

public class NotificationStat {
    private Handler mBgHandler;
    /* access modifiers changed from: private */
    public Context mContext;
    private PanelExpandSession mCurrentBarSession = new PanelExpandSession();

    public void onArrive(ExpandedNotification expandedNotification) {
    }

    static {
        boolean z = Constants.DEBUG;
    }

    public NotificationStat(Context context) {
        this.mContext = context;
        this.mBgHandler = new WorkHandler(((SystemUIStat) Dependency.get(SystemUIStat.class)).getBgThread().getLooper());
    }

    private final class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 1002) {
                ADBlock aDBlock = (ADBlock) message.obj;
                SystemUIStat.log(aDBlock.adId, new Object[0]);
                Intent intent = new Intent("miui.intent.adblock");
                intent.setPackage("com.miui.systemAdSolution");
                intent.putExtra("adid", aDBlock.adId);
                NotificationStat.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            } else if (i == 2001) {
                SomeArgs someArgs = (SomeArgs) message.obj;
                StatManager.track((String) someArgs.arg1, (Map) someArgs.arg2, (List) someArgs.arg3);
            }
        }
    }

    public void onRemove(ExpandableNotificationRow expandableNotificationRow, int i, boolean z, boolean z2) {
        List<ExpandableNotificationRow> notificationChildren;
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markRemove();
        }
        String source = Analytics$NotiEvent.getSource(z, z2);
        onRemoveSingle(expandableNotificationRow.getEntry(), source, i);
        if (expandableNotificationRow.isSummaryWithChildren() && (notificationChildren = expandableNotificationRow.getNotificationChildren()) != null) {
            for (ExpandableNotificationRow entry : notificationChildren) {
                i++;
                onRemoveSingle(entry.getEntry(), source, i);
            }
        }
    }

    private void onRemoveSingle(NotificationData.Entry entry, String str, int i) {
        ExpandedNotification expandedNotification = entry.notification;
        handleNotiCancelEvent(expandedNotification, str, i);
        handleNotiVisibleEvent(expandedNotification.getKey(), str);
        AdTracker.trackRemove(this.mContext, entry);
    }

    public void onRemoveAll(boolean z, List<NotificationData.Entry> list, boolean z2) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markRemoveAll();
            this.mCurrentBarSession.markRemove();
            this.mCurrentBarSession.removeAllNotifications(z, list.size(), z2);
            handleCancelAllNotiEvent(this.mCurrentBarSession.cancelAllNotiEvent, list.stream().map($$Lambda$NotificationStat$_IIprGwTR7K4_tE0XlZYhl83GV4.INSTANCE));
        }
    }

    public void onBlock(ExpandedNotification expandedNotification, NotificationChannelCompat notificationChannelCompat, int i) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markBlock();
        }
        sendBlockNotificationEvent(expandedNotification.getPackageName(), PushEvents.getADId(expandedNotification.getNotification()));
        handleNotiBlockEvent(expandedNotification, notificationChannelCompat, i);
    }

    public void onBlock(String str, String str2, String str3) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markBlock();
        }
        if (!"com.miui.systemAdSolution".equals(str) && !"com.miui.msa.global".equals(str)) {
            str3 = null;
        }
        sendBlockNotificationEvent(str, str3);
        handleNotiBlockEvent(str, str2);
    }

    public void onClick(ExpandedNotification expandedNotification, boolean z, boolean z2, int i) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markClick();
        }
        String source = Analytics$NotiEvent.getSource(z, z2);
        handleNotiClickEvent(expandedNotification, i, source);
        handleNotiVisibleEvent(expandedNotification.getKey(), source);
    }

    public void onExpansionChanged(ExpandedNotification expandedNotification, boolean z, boolean z2) {
        if ("com.miui.systemAdSolution".equals(expandedNotification.getBasePkg()) || "com.miui.msa.global".equals(expandedNotification.getBasePkg())) {
            handleExpansionChangedEvent(expandedNotification, z, z2, ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getNotificationData().indexOf(expandedNotification));
        }
    }

    public void onScrollMore() {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.scrollMore();
        }
    }

    public void onClickQSTile() {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markClickQS();
        }
    }

    public void onOpenQSPanel() {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.openQSPanel();
        }
    }

    public void logNotificationLongPress(String str) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markNotiLongPress();
        }
    }

    public void logNotificationSwipeLeft(String str) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markNotiSwipeLeft();
        }
    }

    public void logNotificationSwipeRight(String str) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markNotiSwipeRight();
        }
    }

    public void logNotificationVisibilityChanges(List<String> list, List<String> list2, boolean z, boolean z2) {
        SystemUIStat.log("logNotificationVisibilityChanges newlyVisible=" + list + ", noLongerVisible=" + list2 + "float=" + z + "keyguard=" + z2, new Object[0]);
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.logNotificationVisibilityChanges(list);
        }
        list.forEach(new Consumer() {
            public final void accept(Object obj) {
                NotificationStat.this.lambda$logNotificationVisibilityChanges$1$NotificationStat((String) obj);
            }
        });
        if (!list2.isEmpty()) {
            handleNotiVisibleEvent(list2, Analytics$NotiEvent.getSource(z, z2));
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$logNotificationVisibilityChanges$1 */
    public /* synthetic */ void lambda$logNotificationVisibilityChanges$1$NotificationStat(String str) {
        NotificationData.Entry entry = ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getNotificationData().get(str);
        if (entry != null) {
            AdTracker.trackShow(this.mContext, entry);
        }
    }

    private void sendBlockNotificationEvent(String str, String str2) {
        if ("com.miui.systemAdSolution".equals(str)) {
            ADBlock aDBlock = new ADBlock();
            aDBlock.adId = str2;
            sendADBlockEvent(aDBlock);
        }
    }

    private void sendADBlockEvent(ADBlock aDBlock) {
        if (!TextUtils.isEmpty(aDBlock.adId)) {
            this.mBgHandler.obtainMessage(b.b, aDBlock).sendToTarget();
        }
    }

    public void onPanelExpanded(boolean z, boolean z2, List<NotificationData.Entry> list) {
        String str;
        SystemUIStat.log("onPanelExpanded", new Object[0]);
        if (this.mCurrentBarSession == null) {
            this.mCurrentBarSession = new PanelExpandSession();
            if (z) {
                str = "lockscreen";
            } else {
                str = Util.getTopActivityPkg(this.mContext);
            }
            this.mCurrentBarSession.start(str, z2, list.size());
            handleExpandEvent(this.mCurrentBarSession.expandEvent);
        }
    }

    public void onPanelAnimationEnd() {
        SystemUIStat.log("onPanelAnimationEnd", new Object[0]);
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markAnimationEnd();
        }
    }

    public void onPanelCollapsed(boolean z, boolean z2, List<NotificationData.Entry> list) {
        SystemUIStat.log("onPanelCollapsed", new Object[0]);
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.end(z2, list.size());
            handleCollapseEvent(this.mCurrentBarSession.collapseEvent);
            this.mCurrentBarSession = null;
        }
    }

    public void onBackPressed() {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.onBackPressed();
        }
    }

    public void onHomePressed() {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.onHomePressed();
        }
    }

    private void handleExpandEvent(Analytics$ExpandEvent analytics$ExpandEvent) {
        if (analytics$ExpandEvent != null) {
            HashMap hashMap = new HashMap();
            hashMap.put("current_page", analytics$ExpandEvent.currentPage);
            hashMap.put("expand_mode", analytics$ExpandEvent.expandMode.name());
            hashMap.put("notifications_count", Integer.valueOf(analytics$ExpandEvent.notificationsCount));
            trackEvent(analytics$ExpandEvent.getEventName(), hashMap);
        }
    }

    private void handleCollapseEvent(Analytics$CollapseEvent analytics$CollapseEvent) {
        if (analytics$CollapseEvent != null) {
            HashMap hashMap = new HashMap();
            hashMap.put("collapse_mode", analytics$CollapseEvent.collapseMode.name());
            hashMap.put("notifications_count", Integer.valueOf(analytics$CollapseEvent.notificationsCount));
            hashMap.put("is_qs_expanded", Integer.valueOf(analytics$CollapseEvent.isQsExpanded));
            hashMap.put("is_click_qs_toggle", Integer.valueOf(analytics$CollapseEvent.isClickQsToggle));
            hashMap.put("is_slide_brightness_bar", Integer.valueOf(analytics$CollapseEvent.isSlideBrightnessBar));
            hashMap.put("is_slide_notification_bar", Integer.valueOf(analytics$CollapseEvent.isSlideNotificationBar));
            hashMap.put("is_delete_notification", Integer.valueOf(analytics$CollapseEvent.isDeleteNotification));
            hashMap.put("residence_time", Integer.valueOf((int) analytics$CollapseEvent.residenceTime));
            hashMap.put("fist_notification_action", analytics$CollapseEvent.fistNotificationAction.name());
            hashMap.put("fist_notification_action_duration", Integer.valueOf((int) analytics$CollapseEvent.fistNotificationActionDuration));
            hashMap.put("notification_visible_count", Integer.valueOf(analytics$CollapseEvent.notificationVisibleCount));
            trackEvent(analytics$CollapseEvent.getEventName(), hashMap);
        }
    }

    public void handleSettingsStatusEvent() {
        HashMap hashMap = new HashMap();
        hashMap.put("notifications_enabled_count", Integer.valueOf(Analytics$SettingsStatusEvent.getAppsCount(this.mContext, false)));
        hashMap.put("notifications_banned_count", Integer.valueOf(Analytics$SettingsStatusEvent.getAppsCount(this.mContext, true)));
        hashMap.put("show_notification_icon", Integer.valueOf(Analytics$SettingsStatusEvent.getShowNotificationIconValue(this.mContext)));
        hashMap.put("show_network_speed", Integer.valueOf(Analytics$SettingsStatusEvent.getShowNetworkSpeedValue(this.mContext)));
        hashMap.put("show_carrier_under_keyguard", Integer.valueOf(Analytics$SettingsStatusEvent.getShowCarrierUnderKeyguardValue(this.mContext)));
        hashMap.put("custom_carrier", Analytics$SettingsStatusEvent.getCustomCarrierValue(this.mContext));
        hashMap.put("battery_indicator", Analytics$SettingsStatusEvent.getBatteryIndicator(this.mContext));
        hashMap.put("toggle_collapse_after_clicked", Integer.valueOf(Analytics$SettingsStatusEvent.getToggleCollapseAfterClickedValue(this.mContext)));
        hashMap.put("expandable_under_keyguard", Integer.valueOf(Analytics$SettingsStatusEvent.getExpandableUnderKeyguardValue(this.mContext)));
        hashMap.put("notification_shortcut", Analytics$SettingsStatusEvent.getNotificationShortcut(this.mContext));
        hashMap.put("notification_style", Analytics$SettingsStatusEvent.getNotificationStyle(this.mContext));
        hashMap.put("bucket", Integer.valueOf(Analytics$SettingsStatusEvent.getBucket(this.mContext)));
        hashMap.put("notification_fold", Integer.valueOf(Analytics$SettingsStatusEvent.getUserFold(this.mContext)));
        hashMap.put("notification_aggregate", Integer.valueOf(Analytics$SettingsStatusEvent.getUserAggregate(this.mContext)));
        hashMap.put("use_control_panel", Integer.valueOf(Analytics$ControlCenterEvent.getUseControlPanel(this.mContext)));
        hashMap.put("expandable_under_lock_screen", Integer.valueOf(Analytics$ControlCenterEvent.getExpandableUnderLockscreen(this.mContext)));
        hashMap.put("expand_selected_info", Integer.valueOf(Analytics$SettingsStatusEvent.getExpandSelectedInfo(this.mContext)));
        trackEvent("status_bar_settings_status", hashMap);
    }

    private void handleNotiVisibleEvent(String str, String str2) {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(str);
        handleNotiVisibleEvent((List<String>) arrayList, str2);
    }

    private void handleNotiVisibleEvent(List<String> list, String str) {
        NotificationData notificationData = ((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getNotificationData();
        List list2 = (List) list.stream().map(new Function() {
            public final Object apply(Object obj) {
                return NotificationData.this.get((String) obj);
            }
        }).filter($$Lambda$NotificationStat$4lZ4qzexFgm7Xg6LxQlKPPwt0Q.INSTANCE).map(new Function(notificationData) {
            public final /* synthetic */ NotificationData f$1;

            {
                this.f$1 = r2;
            }

            public final Object apply(Object obj) {
                return NotificationStat.this.lambda$handleNotiVisibleEvent$4$NotificationStat(this.f$1, (NotificationData.Entry) obj);
            }
        }).collect(Collectors.toList());
        if (list2.size() > 0) {
            HashMap hashMap = new HashMap();
            hashMap.put("source", str);
            trackEvent("notification_visible", hashMap, list2);
        }
    }

    static /* synthetic */ boolean lambda$handleNotiVisibleEvent$3(NotificationData.Entry entry) {
        return (entry == null || entry.seeTime == 0) ? false : true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleNotiVisibleEvent$4 */
    public /* synthetic */ Map lambda$handleNotiVisibleEvent$4$NotificationStat(NotificationData notificationData, NotificationData.Entry entry) {
        HashMap hashMap = new HashMap();
        hashMap.put("ts_id", Long.valueOf(entry.notification.getPostTime()));
        hashMap.put("duration", Long.valueOf(System.currentTimeMillis() - entry.seeTime));
        hashMap.put("index", Integer.valueOf(notificationData.indexOf(entry) + 1));
        if (entry.notification.getNotification().isGroupSummary()) {
            hashMap.put("items", new JSONArray(getChildrenPostTime(entry.row)).toString());
        } else {
            List<Long> entranceChildrenIds = getEntranceChildrenIds(entry.notification);
            if (!entranceChildrenIds.isEmpty()) {
                hashMap.put("items", new JSONArray(entranceChildrenIds).toString());
            }
        }
        entry.seeTime = 0;
        return hashMap;
    }

    private List<Long> getChildrenPostTime(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow == null || expandableNotificationRow.getChildrenContainer() == null || expandableNotificationRow.getChildrenContainer().getNotificationChildren() == null) {
            return Collections.emptyList();
        }
        return (List) expandableNotificationRow.getChildrenContainer().getNotificationChildren().stream().map($$Lambda$NotificationStat$v9xbYIxdp1VCGQLT5IUgWvjzpSY.INSTANCE).collect(Collectors.toList());
    }

    private List<Long> getEntranceChildrenIds(ExpandedNotification expandedNotification) {
        long[] longArray = expandedNotification.getNotification().extras.getLongArray("miui.inner_notifications");
        if (!("com.miui.notification".equals(expandedNotification.getBasePkg()) && longArray != null)) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList(longArray.length);
        for (long valueOf : longArray) {
            arrayList.add(Long.valueOf(valueOf));
        }
        return arrayList;
    }

    private void handleNotiClickEvent(ExpandedNotification expandedNotification, int i, String str) {
        Map<String, Object> statParam = Analytics$NotiEvent.getStatParam(expandedNotification);
        statParam.put("source", str);
        boolean z = true;
        statParam.put("index", Integer.valueOf(i + 1));
        if (!AnalyticsWrapper.sSupprotAggregate || !"com.miui.notification".equals(expandedNotification.getBasePkg())) {
            z = false;
        }
        if (z) {
            statParam.put("entrance_type", expandedNotification.getTag());
        }
        trackEvent("notification_click", statParam);
    }

    private void handleExpansionChangedEvent(ExpandedNotification expandedNotification, boolean z, boolean z2, int i) {
        Map<String, Object> statParam = Analytics$NotiEvent.getStatParam(expandedNotification);
        statParam.put("index", Integer.valueOf(i + 1));
        statParam.put("user_action", Boolean.valueOf(z));
        statParam.put("expanded", Boolean.valueOf(z2));
        trackEvent("notification_expansion_changed", statParam);
    }

    private void handleNotiBlockEvent(String str, String str2) {
        HashMap hashMap = new HashMap();
        hashMap.put("pkg", str);
        hashMap.put("channel_id", str2);
        hashMap.put("source", "settings");
        trackEvent("notification_block", hashMap);
    }

    private void handleNotiBlockEvent(ExpandedNotification expandedNotification, NotificationChannelCompat notificationChannelCompat, int i) {
        Map<String, Object> statParam = Analytics$NotiEvent.getStatParam(expandedNotification);
        statParam.put("source", "panel");
        statParam.put("index", Integer.valueOf(i + 1));
        statParam.put("channel_id", Analytics$NotiEvent.getChannelValue(notificationChannelCompat));
        trackEvent("notification_block", statParam);
    }

    private void handleNotiCancelEvent(ExpandedNotification expandedNotification, String str, int i) {
        Map<String, Object> statParam = Analytics$NotiEvent.getStatParam(expandedNotification);
        statParam.put("index", Integer.valueOf(i + 1));
        statParam.put("source", str);
        trackEvent("notification_cancel", statParam);
    }

    private void handleCancelAllNotiEvent(Analytics$CancelAllNotiEvent analytics$CancelAllNotiEvent, Stream<ExpandedNotification> stream) {
        if (analytics$CancelAllNotiEvent != null) {
            HashMap hashMap = new HashMap();
            hashMap.put("clear_all_mode", analytics$CancelAllNotiEvent.clearAllMode.name());
            hashMap.put("notifications_count", Integer.valueOf(analytics$CancelAllNotiEvent.notificationsCount));
            hashMap.put("is_slide_notification_bar", Integer.valueOf(analytics$CancelAllNotiEvent.isSlideNotificationBar));
            hashMap.put("source", "panel");
            trackEvent("notification_cancel_all", hashMap, (List) stream.map($$Lambda$NotificationStat$Dv6rsKP2EKuIIPOTf3NzGnBvqQ8.INSTANCE).collect(Collectors.toList()));
        }
    }

    public void handleNotiOpenMenuEvent(ExpandedNotification expandedNotification, int i) {
        Map<String, Object> statParam = Analytics$NotiEvent.getStatParam(expandedNotification);
        statParam.put("index", Integer.valueOf(i + 1));
        statParam.put("source", "panel");
        trackEvent("notification_open_menu", statParam);
    }

    public void handleNotiSetConfigEvent(ExpandedNotification expandedNotification) {
        Map<String, Object> statParam = Analytics$NotiEvent.getStatParam(expandedNotification);
        statParam.put(MiStat.Param.VALUE, -1);
        statParam.put("bucket", Integer.valueOf(Analytics$SettingsStatusEvent.getBucket(this.mContext)));
        statParam.put("source", "panel");
        trackEvent("notification_set_config", statParam);
    }

    private void trackEvent(String str, Map<String, Object> map) {
        trackEvent(str, map, (List) null);
    }

    private void trackEvent(String str, Map<String, Object> map, List list) {
        if (!Constants.IS_INTERNATIONAL) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = map;
            obtain.arg3 = list;
            this.mBgHandler.obtainMessage(b.m, obtain).sendToTarget();
        }
    }
}
