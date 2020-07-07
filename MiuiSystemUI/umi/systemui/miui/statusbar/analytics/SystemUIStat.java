package com.android.systemui.miui.statusbar.analytics;

import android.app.NotificationChannelCompat;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import com.android.internal.os.SomeArgs;
import com.android.systemui.AdTracker;
import com.android.systemui.Constants;
import com.android.systemui.SystemUI;
import com.android.systemui.Util;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.PushEvents;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.StatusBar;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.MiStatParams;
import com.xiaomi.stat.c.b;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SystemUIStat {
    private static boolean DEBUG = Constants.DEBUG;
    private Handler mBgHandler;
    /* access modifiers changed from: private */
    public Context mContext;
    private PanelExpandSession mCurrentBarSession = null;

    public void onArrive(ExpandedNotification expandedNotification) {
    }

    public SystemUIStat(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread("SystemUIStat", 10);
        handlerThread.start();
        this.mBgHandler = new WorkHandler(handlerThread.getLooper());
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
                SystemUIStat.this.log(aDBlock.adId, new Object[0]);
                Intent intent = new Intent("miui.intent.adblock");
                intent.setPackage("com.miui.systemAdSolution");
                intent.putExtra("adid", aDBlock.adId);
                SystemUIStat.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
            } else if (i == 2001) {
                SomeArgs someArgs = (SomeArgs) message.obj;
                Object obj = someArgs.arg4;
                if (obj != null) {
                    AnalyticsWrapper.trackPlainTextEvent((String) someArgs.arg1, (String) obj);
                    return;
                }
                Object obj2 = someArgs.arg2;
                if (obj2 != null) {
                    AnalyticsWrapper.trackEvent((String) someArgs.arg1, (String) obj2);
                } else {
                    AnalyticsWrapper.trackEvent((String) someArgs.arg1, (MiStatParams) someArgs.arg3);
                }
            } else if (i == 2002) {
                AnalyticsWrapper.setUserProperty((MiStatParams) message.obj);
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
        handleNotiVisibleEvent(expandedNotification.getKey());
        AdTracker.trackRemove(this.mContext, entry);
    }

    public void onRemoveAll(boolean z, List<NotificationData.Entry> list, boolean z2) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markRemoveAll();
            this.mCurrentBarSession.markRemove();
            this.mCurrentBarSession.removeAllNotifications(z, list.size(), z2);
            handleCancelAllNotiEvent(this.mCurrentBarSession.cancelAllNotiEvent, list.stream().map($$Lambda$SystemUIStat$4yQkEBi0C_T1qgydx0AUvJ0g7c.INSTANCE));
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
        handleNotiClickEvent(expandedNotification, i, Analytics$NotiEvent.getSource(z, z2));
        handleNotiVisibleEvent(expandedNotification.getKey());
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

    public void onClickQSTile(String str, boolean z, int i) {
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markClickQS();
        }
        handleTrackQSTileClick(str, z, i);
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

    public void logNotificationVisibilityChanges(List<String> list, List<String> list2) {
        log("logNotificationVisibilityChanges newlyVisible=" + list + ", noLongerVisible=" + list2, new Object[0]);
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.logNotificationVisibilityChanges(list);
        }
        list.forEach(new Consumer() {
            public final void accept(Object obj) {
                SystemUIStat.this.lambda$logNotificationVisibilityChanges$1$SystemUIStat((String) obj);
            }
        });
        if (!list2.isEmpty()) {
            handleNotiVisibleEvent(list2);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$logNotificationVisibilityChanges$1 */
    public /* synthetic */ void lambda$logNotificationVisibilityChanges$1$SystemUIStat(String str) {
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

    public void reportFullScreenEventAnonymous(String str, Map<String, String> map) {
        Object[] objArr = new Object[2];
        objArr[0] = str;
        objArr[1] = map != null ? map.toString() : "null params";
        log("reportFullScreenEventAnonymous eventName=%s params=%s", objArr);
        MiStatParams miStatParams = null;
        if (map != null && map.size() > 0) {
            miStatParams = new MiStatParams();
            for (Map.Entry next : map.entrySet()) {
                miStatParams.putString((String) next.getKey(), (String) next.getValue());
            }
        }
        if (miStatParams != null) {
            trackEvent(str, miStatParams);
        } else {
            trackEvent(str);
        }
    }

    public void onPanelExpanded(boolean z, boolean z2, List<NotificationData.Entry> list) {
        String str;
        log("onPanelExpanded", new Object[0]);
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
        log("onPanelAnimationEnd", new Object[0]);
        PanelExpandSession panelExpandSession = this.mCurrentBarSession;
        if (panelExpandSession != null) {
            panelExpandSession.markAnimationEnd();
        }
    }

    public void onPanelCollapsed(boolean z, boolean z2, List<NotificationData.Entry> list) {
        log("onPanelCollapsed", new Object[0]);
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
            MiStatParams miStatParams = new MiStatParams();
            miStatParams.putString("current_page", analytics$ExpandEvent.currentPage);
            miStatParams.putString("expand_mode", analytics$ExpandEvent.expandMode.name());
            miStatParams.putInt("notifications_count", analytics$ExpandEvent.notificationsCount);
            trackEvent(analytics$ExpandEvent.getEventName(), miStatParams);
        }
    }

    private void handleCollapseEvent(Analytics$CollapseEvent analytics$CollapseEvent) {
        if (analytics$CollapseEvent != null) {
            MiStatParams miStatParams = new MiStatParams();
            miStatParams.putString("collapse_mode", analytics$CollapseEvent.collapseMode.name());
            miStatParams.putInt("notifications_count", analytics$CollapseEvent.notificationsCount);
            miStatParams.putInt("is_qs_expanded", analytics$CollapseEvent.isQsExpanded);
            miStatParams.putInt("is_click_qs_toggle", analytics$CollapseEvent.isClickQsToggle);
            miStatParams.putInt("is_slide_brightness_bar", analytics$CollapseEvent.isSlideBrightnessBar);
            miStatParams.putInt("is_slide_notification_bar", analytics$CollapseEvent.isSlideNotificationBar);
            miStatParams.putInt("is_delete_notification", analytics$CollapseEvent.isDeleteNotification);
            miStatParams.putInt("residence_time", (int) analytics$CollapseEvent.residenceTime);
            miStatParams.putString("fist_notification_action", analytics$CollapseEvent.fistNotificationAction.name());
            miStatParams.putInt("fist_notification_action_duration", (int) analytics$CollapseEvent.fistNotificationActionDuration);
            miStatParams.putInt("notification_visible_count", analytics$CollapseEvent.notificationVisibleCount);
            trackEvent(analytics$CollapseEvent.getEventName(), miStatParams);
        }
    }

    public void handleSettingsStatusEvent() {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("notifications_enabled_count", Analytics$SettingsStatusEvent.getAppsCount(this.mContext, false));
        miStatParams.putInt("notifications_banned_count", Analytics$SettingsStatusEvent.getAppsCount(this.mContext, true));
        miStatParams.putInt("show_notification_icon", Analytics$SettingsStatusEvent.getShowNotificationIconValue(this.mContext));
        miStatParams.putInt("show_network_speed", Analytics$SettingsStatusEvent.getShowNetworkSpeedValue(this.mContext));
        miStatParams.putInt("show_carrier_under_keyguard", Analytics$SettingsStatusEvent.getShowCarrierUnderKeyguardValue(this.mContext));
        miStatParams.putString("custom_carrier", Analytics$SettingsStatusEvent.getCustomCarrierValue(this.mContext));
        miStatParams.putString("battery_indicator", Analytics$SettingsStatusEvent.getBatteryIndicator(this.mContext));
        miStatParams.putInt("toggle_collapse_after_clicked", Analytics$SettingsStatusEvent.getToggleCollapseAfterClickedValue(this.mContext));
        miStatParams.putInt("expandable_under_keyguard", Analytics$SettingsStatusEvent.getExpandableUnderKeyguardValue(this.mContext));
        miStatParams.putString("notification_shortcut", Analytics$SettingsStatusEvent.getNotificationShortcut(this.mContext));
        miStatParams.putString("notification_style", Analytics$SettingsStatusEvent.getNotificationStyle(this.mContext));
        miStatParams.putInt("bucket", Analytics$SettingsStatusEvent.getBucket(this.mContext));
        miStatParams.putInt("notification_fold", Analytics$SettingsStatusEvent.getUserFold(this.mContext));
        miStatParams.putInt("notification_aggregate", Analytics$SettingsStatusEvent.getUserAggregate(this.mContext));
        miStatParams.putInt("use_control_panel", Analytics$SettingsStatusEvent.getUseControlPanel(this.mContext));
        miStatParams.putInt("expand_selected_info", Analytics$SettingsStatusEvent.getExpandSelectedInfo(this.mContext));
        trackEvent("status_bar_settings_status", miStatParams);
    }

    public void handlePhoneStatusEvent() {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("is_dual_card", Analytics$PhoneStatusEvent.getIsDualCardValue());
        miStatParams.putInt("alarm_set", Analytics$PhoneStatusEvent.getIsAlarmSetValue());
        miStatParams.putInt("mute_turned_on", Analytics$PhoneStatusEvent.getIsMuteTurnedOnValue(this.mContext));
        miStatParams.putInt("wifi_turned_on", Analytics$PhoneStatusEvent.getIsWifiTurnedOnValue(this.mContext));
        miStatParams.putInt("bluetooth_turned_on", Analytics$PhoneStatusEvent.getIsBluetoothTurnedOnValue());
        miStatParams.putInt("auto_brightness_turned_on", Analytics$PhoneStatusEvent.getIsAutoBrightnessTurnedOnValue(this.mContext));
        miStatParams.putInt("gps_turned_on", Analytics$PhoneStatusEvent.getIsGpsTurnedOnValue(this.mContext));
        miStatParams.putInt("rotation_lock_turned_on", Analytics$PhoneStatusEvent.getIsRotationLockTurnedOnValue(this.mContext));
        miStatParams.putInt("is_full_screen", Analytics$PhoneStatusEvent.getIsFullScreen(this.mContext));
        miStatParams.putInt("is_notch_screen", Analytics$PhoneStatusEvent.getIsNotchScreen());
        trackEvent("status_bar_phone_status", miStatParams);
    }

    public void handleNotchEvent() {
        if (Constants.IS_NOTCH) {
            String str = Build.VERSION.SDK_INT < 28 ? "force_black" : "force_black_v2";
            MiStatParams miStatParams = new MiStatParams();
            miStatParams.putString(str, "" + Settings.Global.getInt(this.mContext.getContentResolver(), str, -1));
            trackEvent("notch", miStatParams);
        }
    }

    public void handleToggleFullscreenSettingStateEvent() {
        boolean z;
        String str;
        try {
            z = IWindowManagerCompat.hasNavigationBar(IWindowManager.Stub.asInterface(ServiceManager.getService("window")), ContextCompat.getDisplayId(this.mContext));
        } catch (RemoteException unused) {
            z = true;
        }
        if (z) {
            boolean z2 = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
            MiStatParams miStatParams = new MiStatParams();
            miStatParams.putString("system_navigation_way", z2 ? "gesture" : "nav_bar");
            String str2 = "on";
            miStatParams.putString("double_check_for_the_gesture", Settings.Global.getInt(this.mContext.getContentResolver(), "show_mistake_touch_toast", 1) != 0 ? str2 : "off");
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "show_gesture_appswitch_feature", 0) != 0) {
                str = str2;
            } else {
                str = "off";
            }
            miStatParams.putString("go_back_to_previous_app", str);
            if (!isRightHand()) {
                str2 = "off";
            }
            miStatParams.putString("mirror_buttons", str2);
            trackEvent("fullscreen_settings_state", miStatParams);
        }
    }

    private boolean isRightHand() {
        ArrayList screenKeyOrder = MiuiSettings.System.getScreenKeyOrder(this.mContext);
        if (screenKeyOrder == null || screenKeyOrder.size() <= 0 || ((Integer) screenKeyOrder.get(0)).intValue() != 2) {
            return false;
        }
        return true;
    }

    private void handleNotiVisibleEvent(String str) {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(str);
        handleNotiVisibleEvent((List<String>) arrayList);
    }

    private void handleNotiVisibleEvent(List<String> list) {
        JSONArray jSONArray = new JSONArray();
        list.forEach(new Consumer(((StatusBar) SystemUI.getComponent(this.mContext, StatusBar.class)).getNotificationData(), jSONArray) {
            public final /* synthetic */ NotificationData f$1;
            public final /* synthetic */ JSONArray f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                SystemUIStat.this.lambda$handleNotiVisibleEvent$2$SystemUIStat(this.f$1, this.f$2, (String) obj);
            }
        });
        try {
            if (jSONArray.length() > 0) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("source", "panel");
                jSONObject.put("items", jSONArray);
                trackEvent("notification_visible", jSONObject);
            }
        } catch (JSONException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleNotiVisibleEvent$2 */
    public /* synthetic */ void lambda$handleNotiVisibleEvent$2$SystemUIStat(NotificationData notificationData, JSONArray jSONArray, String str) {
        NotificationData.Entry entry = notificationData.get(str);
        if (entry != null && entry.seeTime != 0) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("ts_id", entry.notification.getPostTime());
                jSONObject.put("duration", System.currentTimeMillis() - entry.seeTime);
                jSONObject.put("index", notificationData.indexOf(entry) + 1);
                if (entry.notification.getNotification().isGroupSummary()) {
                    jSONObject.put("items", getChildrenPostTime(entry.row));
                }
                jSONArray.put(jSONObject);
            } catch (JSONException unused) {
            }
            entry.seeTime = 0;
        }
    }

    private List<Long> getChildrenPostTime(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow == null || expandableNotificationRow.getChildrenContainer() == null || expandableNotificationRow.getChildrenContainer().getNotificationChildren() == null) {
            return null;
        }
        return (List) expandableNotificationRow.getChildrenContainer().getNotificationChildren().stream().map($$Lambda$SystemUIStat$J9bUnh0618MhdBwwnfyV94jOEo.INSTANCE).collect(Collectors.toList());
    }

    public void handleNotiClickEvent(ExpandedNotification expandedNotification, int i, String str) {
        MiStatParams miStatParam = Analytics$NotiEvent.getMiStatParam(expandedNotification);
        miStatParam.putString("source", str);
        boolean z = true;
        miStatParam.putInt("index", i + 1);
        if (!AnalyticsWrapper.sSupprotAggregate || !"com.miui.notification".equals(expandedNotification.getBasePkg())) {
            z = false;
        }
        if (z) {
            miStatParam.putString("entrance_type", expandedNotification.getTag());
        }
        trackEvent("notification_click", miStatParam);
    }

    public void handleExpansionChangedEvent(ExpandedNotification expandedNotification, boolean z, boolean z2, int i) {
        MiStatParams miStatParam = Analytics$NotiEvent.getMiStatParam(expandedNotification);
        miStatParam.putInt("index", i + 1);
        miStatParam.putBoolean("user_action", z);
        miStatParam.putBoolean("expanded", z2);
        trackEvent("notification_expansion_changed", miStatParam);
    }

    public void handleNotiBlockEvent(String str, String str2) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("pkg", str);
        miStatParams.putString("channel_id", str2);
        miStatParams.putString("source", "settings");
        trackEvent("notification_block", miStatParams);
    }

    public void handleNotiBlockEvent(ExpandedNotification expandedNotification, NotificationChannelCompat notificationChannelCompat, int i) {
        MiStatParams miStatParam = Analytics$NotiEvent.getMiStatParam(expandedNotification);
        miStatParam.putString("source", "panel");
        miStatParam.putInt("index", i + 1);
        miStatParam.putString("channel_id", Analytics$NotiEvent.getChannelValue(notificationChannelCompat));
        trackEvent("notification_block", miStatParam);
    }

    public void handleNotiCancelEvent(ExpandedNotification expandedNotification, String str, int i) {
        MiStatParams miStatParam = Analytics$NotiEvent.getMiStatParam(expandedNotification);
        miStatParam.putInt("index", i + 1);
        miStatParam.putString("source", str);
        trackEvent("notification_cancel", miStatParam);
    }

    public void handleCancelAllNotiEvent(Analytics$CancelAllNotiEvent analytics$CancelAllNotiEvent, Stream<ExpandedNotification> stream) {
        if (analytics$CancelAllNotiEvent != null) {
            try {
                JSONArray jSONArray = new JSONArray();
                stream.forEach(new Consumer(jSONArray) {
                    public final /* synthetic */ JSONArray f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void accept(Object obj) {
                        this.f$0.put(((ExpandedNotification) obj).getPostTime());
                    }
                });
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("clear_all_mode", analytics$CancelAllNotiEvent.clearAllMode.name());
                jSONObject.put("notifications_count", analytics$CancelAllNotiEvent.notificationsCount);
                jSONObject.put("is_slide_notification_bar", analytics$CancelAllNotiEvent.isSlideNotificationBar);
                jSONObject.put("items", jSONArray);
                jSONObject.put("source", "panel");
                trackEvent("notification_cancel_all", jSONObject);
            } catch (JSONException unused) {
            }
        }
    }

    public void handleNotiOpenMenuEvent(ExpandedNotification expandedNotification, int i) {
        MiStatParams miStatParam = Analytics$NotiEvent.getMiStatParam(expandedNotification);
        miStatParam.putInt("index", i + 1);
        miStatParam.putString("source", "panel");
        trackEvent("notification_open_menu", miStatParam);
    }

    public void handleNotiSetConfigEvent(ExpandedNotification expandedNotification) {
        MiStatParams miStatParam = Analytics$NotiEvent.getMiStatParam(expandedNotification);
        miStatParam.putInt(MiStat.Param.VALUE, -1);
        miStatParam.putInt("bucket", Analytics$SettingsStatusEvent.getBucket(this.mContext));
        miStatParam.putString("source", "panel");
        trackEvent("notification_set_config", miStatParam);
    }

    public void handleShowStatusBarPromptEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("prompt_state", Analytics$StatusBarPromptEvent.getPromptState(str));
        trackEvent("show_status_bar_prompt", miStatParams);
    }

    public void handleClickStatusBarPromptEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("prompt_state", Analytics$StatusBarPromptEvent.getPromptState(str));
        trackEvent("click_status_bar_prompt", miStatParams);
    }

    public void handleControlCenterEvent(String str) {
        trackEvent(str, "control_center_event");
    }

    public void handleControlCenterQuickTileEvent(String str, String str2) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("quick_tile_spec", str2);
        trackEvent(str, "control_center_event", miStatParams, (String) null);
    }

    private void trackEvent(String str) {
        trackEvent(str, (String) null, (MiStatParams) null, (String) null);
    }

    private void trackEvent(String str, String str2) {
        log("trackEvent eventName=%s eventGroup=%s", str, str2);
        trackEvent(str, str2, (MiStatParams) null, (String) null);
    }

    private void trackEvent(String str, MiStatParams miStatParams) {
        miStatParams.putLong("ts", System.currentTimeMillis());
        log("trackEvent eventName=%s params=%s", str, miStatParams.toJsonString());
        trackEvent(str, (String) null, miStatParams, (String) null);
    }

    private void trackEvent(String str, JSONObject jSONObject) throws JSONException {
        jSONObject.put("ts", System.currentTimeMillis());
        log("trackEvent eventName=%s params=%s", str, jSONObject.toString());
        trackEvent(str, (String) null, (MiStatParams) null, jSONObject.toString());
    }

    private void trackEvent(String str, String str2, MiStatParams miStatParams, String str3) {
        if (!Constants.IS_INTERNATIONAL) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = str2;
            obtain.arg3 = miStatParams;
            obtain.arg4 = str3;
            this.mBgHandler.obtainMessage(b.m, obtain).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void log(String str, Object... objArr) {
        if (DEBUG) {
            Log.d("SystemUIStat", String.format(str, objArr));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0019, code lost:
        r1 = r1.split(",");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleQSTileStateEvent() {
        /*
            r4 = this;
            com.xiaomi.stat.MiStatParams r0 = new com.xiaomi.stat.MiStatParams
            r0.<init>()
            android.content.Context r1 = r4.mContext
            android.content.ContentResolver r1 = r1.getContentResolver()
            java.lang.String r2 = "sysui_qs_tiles"
            r3 = -2
            java.lang.String r1 = android.provider.Settings.Secure.getStringForUser(r1, r2, r3)
            boolean r2 = android.text.TextUtils.isEmpty(r1)
            if (r2 != 0) goto L_0x0023
            java.lang.String r2 = ","
            java.lang.String[] r1 = r1.split(r2)
            if (r1 == 0) goto L_0x0023
            int r1 = r1.length
            goto L_0x0024
        L_0x0023:
            r1 = 0
        L_0x0024:
            java.lang.String r2 = "system_qs_tile_added"
            r0.putInt(r2, r1)
            java.lang.String r1 = "state_qs_tile"
            r4.trackEvent((java.lang.String) r1, (com.xiaomi.stat.MiStatParams) r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.statusbar.analytics.SystemUIStat.handleQSTileStateEvent():void");
    }

    public void handleTrackQSTileClick(String str, boolean z, int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("qs_tile_name", str);
        miStatParams.putString("qs_tile_position", z ? "qs_edit" : "qs_panel");
        miStatParams.putInt("qs_tile_index", i);
        trackEvent("click_qs_tile", miStatParams);
    }

    public void handleTrackQSTileSecondaryClick(String str, int i, boolean z) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("qs_tile_name", str);
        miStatParams.putInt("qs_tile_index", i);
        miStatParams.putString("qs_tile_switch_state", z ? "on" : "off");
        trackEvent("secondary_click_qs_tile", miStatParams);
    }

    public void handleTrackQSTileLongClick(String str, int i) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("qs_tile_name", str);
        miStatParams.putInt("qs_tile_index", i);
        trackEvent("long_click_qs_tile", miStatParams);
    }

    public void handleSlideBrightnessBarEvent(int i, int i2, boolean z) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("before_brightness_value", i);
        miStatParams.putInt("after_brightness_value", i2);
        miStatParams.putString("auto_brightness_turned_on", z ? "on" : "off");
        trackEvent("slide_brightness_bar", miStatParams);
    }

    public void handleClickShortcutEvent(String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("shortcut", str);
        trackEvent("click_notification_bar_shortcut", miStatParams);
    }

    public void handleQSDetailExitEvent(String str, boolean z, boolean z2, String str2) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putString("qs_tile_name", str);
        miStatParams.putInt("click_item", z ? 1 : 0);
        miStatParams.putInt("click_switch", z2 ? 1 : 0);
        miStatParams.putString("exit_mode", str2);
        trackEvent("event_qs_detail_exit", miStatParams);
    }

    public void handleQSEditExitEvent(boolean z, boolean z2, String str) {
        MiStatParams miStatParams = new MiStatParams();
        miStatParams.putInt("click_reset", z ? 1 : 0);
        miStatParams.putInt("qs_tile_move", z2 ? 1 : 0);
        miStatParams.putString("exit_mode", str);
        trackEvent("event_qs_edit_exit", miStatParams);
    }
}
