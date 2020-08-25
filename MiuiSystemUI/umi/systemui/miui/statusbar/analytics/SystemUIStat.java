package com.android.systemui.miui.statusbar.analytics;

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
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import com.android.internal.os.SomeArgs;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.miui.analytics.AnalyticsWrapper;
import com.xiaomi.stat.MiStatParams;
import com.xiaomi.stat.c.b;
import java.util.ArrayList;
import java.util.Map;

public class SystemUIStat {
    private static boolean DEBUG = Constants.DEBUG;
    private Handler mBgHandler = new WorkHandler(this.mBgThread.getLooper());
    private HandlerThread mBgThread;
    /* access modifiers changed from: private */
    public Context mContext;

    public SystemUIStat(Context context) {
        this.mContext = context;
        HandlerThread handlerThread = new HandlerThread("SystemUIStat", 10);
        this.mBgThread = handlerThread;
        handlerThread.start();
    }

    /* access modifiers changed from: package-private */
    public HandlerThread getBgThread() {
        return this.mBgThread;
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

    public void onClickQSTile(String str, boolean z, int i) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onClickQSTile();
        handleTrackQSTileClick(str, z, i);
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
        miStatParams.putInt("use_control_panel", Analytics$ControlCenterEvent.getUseControlPanel(this.mContext));
        miStatParams.putInt("expandable_under_lock_screen", Analytics$ControlCenterEvent.getExpandableUnderLockscreen(this.mContext));
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

    static void log(String str, Object... objArr) {
        if (DEBUG) {
            Log.d("SystemUIStat", String.format(str, objArr));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x001c, code lost:
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
            int r2 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()
            java.lang.String r3 = "sysui_qs_tiles"
            java.lang.String r1 = android.provider.Settings.Secure.getStringForUser(r1, r3, r2)
            boolean r2 = android.text.TextUtils.isEmpty(r1)
            if (r2 != 0) goto L_0x0026
            java.lang.String r2 = ","
            java.lang.String[] r1 = r1.split(r2)
            if (r1 == 0) goto L_0x0026
            int r1 = r1.length
            goto L_0x0027
        L_0x0026:
            r1 = 0
        L_0x0027:
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

    public void handleFreeformEvent() {
        trackEvent("freeform_notification");
    }

    public void handleFreeformEventSpeed() {
        trackEvent("freeform_notification_way_speed");
    }

    public void handleFreeformEventDistance() {
        trackEvent("freeform_notification_way_distance");
    }
}
