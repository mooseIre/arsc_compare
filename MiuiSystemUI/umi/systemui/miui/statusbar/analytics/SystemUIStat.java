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
import com.xiaomi.stat.c.b;
import java.util.ArrayList;
import java.util.HashMap;
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
                StatManager.trackGenericEvent((String) someArgs.arg1, (Map) someArgs.arg2);
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
        HashMap hashMap = null;
        if (map != null && map.size() > 0) {
            hashMap = new HashMap();
            for (Map.Entry next : map.entrySet()) {
                hashMap.put((String) next.getKey(), next.getValue());
            }
        }
        if (hashMap != null) {
            trackEvent(str, hashMap);
        } else {
            trackEvent(str);
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

    public void handlePhoneStatusEvent() {
        HashMap hashMap = new HashMap();
        hashMap.put("is_dual_card", Integer.valueOf(Analytics$PhoneStatusEvent.getIsDualCardValue()));
        hashMap.put("alarm_set", Integer.valueOf(Analytics$PhoneStatusEvent.getIsAlarmSetValue()));
        hashMap.put("mute_turned_on", Integer.valueOf(Analytics$PhoneStatusEvent.getIsMuteTurnedOnValue(this.mContext)));
        hashMap.put("wifi_turned_on", Integer.valueOf(Analytics$PhoneStatusEvent.getIsWifiTurnedOnValue(this.mContext)));
        hashMap.put("bluetooth_turned_on", Integer.valueOf(Analytics$PhoneStatusEvent.getIsBluetoothTurnedOnValue()));
        hashMap.put("auto_brightness_turned_on", Integer.valueOf(Analytics$PhoneStatusEvent.getIsAutoBrightnessTurnedOnValue(this.mContext)));
        hashMap.put("gps_turned_on", Integer.valueOf(Analytics$PhoneStatusEvent.getIsGpsTurnedOnValue(this.mContext)));
        hashMap.put("rotation_lock_turned_on", Integer.valueOf(Analytics$PhoneStatusEvent.getIsRotationLockTurnedOnValue(this.mContext)));
        hashMap.put("is_full_screen", Integer.valueOf(Analytics$PhoneStatusEvent.getIsFullScreen(this.mContext)));
        hashMap.put("is_notch_screen", Integer.valueOf(Analytics$PhoneStatusEvent.getIsNotchScreen()));
        trackEvent("status_bar_phone_status", hashMap);
    }

    public void handleNotchEvent() {
        if (Constants.IS_NOTCH) {
            String str = Build.VERSION.SDK_INT < 28 ? "force_black" : "force_black_v2";
            HashMap hashMap = new HashMap();
            hashMap.put(str, "" + Settings.Global.getInt(this.mContext.getContentResolver(), str, -1));
            trackEvent("notch", hashMap);
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
            HashMap hashMap = new HashMap();
            hashMap.put("system_navigation_way", z2 ? "gesture" : "nav_bar");
            String str2 = "on";
            hashMap.put("double_check_for_the_gesture", Settings.Global.getInt(this.mContext.getContentResolver(), "show_mistake_touch_toast", 1) != 0 ? str2 : "off");
            if (Settings.Global.getInt(this.mContext.getContentResolver(), "show_gesture_appswitch_feature", 0) != 0) {
                str = str2;
            } else {
                str = "off";
            }
            hashMap.put("go_back_to_previous_app", str);
            if (!isRightHand()) {
                str2 = "off";
            }
            hashMap.put("mirror_buttons", str2);
            trackEvent("fullscreen_settings_state", hashMap);
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
        HashMap hashMap = new HashMap();
        hashMap.put("prompt_state", Analytics$StatusBarPromptEvent.getPromptState(str));
        trackEvent("show_status_bar_prompt", hashMap);
    }

    public void handleClickStatusBarPromptEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("prompt_state", Analytics$StatusBarPromptEvent.getPromptState(str));
        trackEvent("click_status_bar_prompt", hashMap);
    }

    public void handleControlCenterEvent(String str) {
        trackEvent(str);
    }

    public void handleControlCenterQuickTileEvent(String str, String str2) {
        HashMap hashMap = new HashMap();
        hashMap.put("quick_tile_spec", str2);
        trackEvent(str, hashMap);
    }

    private void trackEvent(String str) {
        trackEvent(str, (HashMap<String, Object>) null);
    }

    private void trackEvent(String str, HashMap<String, Object> hashMap) {
        if (!Constants.IS_INTERNATIONAL) {
            if (hashMap == null) {
                hashMap = new HashMap<>();
                hashMap.put(str, 0);
            }
            log("trackEvent eventName=%s params=%s", str, hashMap.toString());
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = hashMap;
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
            java.util.HashMap r0 = new java.util.HashMap
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
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            java.lang.String r2 = "system_qs_tile_added"
            r0.put(r2, r1)
            java.lang.String r1 = "state_qs_tile"
            r4.trackEvent(r1, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.statusbar.analytics.SystemUIStat.handleQSTileStateEvent():void");
    }

    public void handleTrackQSTileClick(String str, boolean z, int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("qs_tile_name", str);
        hashMap.put("qs_tile_position", z ? "qs_edit" : "qs_panel");
        hashMap.put("qs_tile_index", Integer.valueOf(i));
        trackEvent("click_qs_tile", hashMap);
    }

    public void handleTrackQSTileSecondaryClick(String str, int i, boolean z) {
        HashMap hashMap = new HashMap();
        hashMap.put("qs_tile_name", str);
        hashMap.put("qs_tile_index", Integer.valueOf(i));
        hashMap.put("qs_tile_switch_state", z ? "on" : "off");
        trackEvent("secondary_click_qs_tile", hashMap);
    }

    public void handleTrackQSTileLongClick(String str, int i) {
        HashMap hashMap = new HashMap();
        hashMap.put("qs_tile_name", str);
        hashMap.put("qs_tile_index", Integer.valueOf(i));
        trackEvent("long_click_qs_tile", hashMap);
    }

    public void handleSlideBrightnessBarEvent(int i, int i2, boolean z) {
        HashMap hashMap = new HashMap();
        hashMap.put("before_brightness_value", Integer.valueOf(i));
        hashMap.put("after_brightness_value", Integer.valueOf(i2));
        hashMap.put("auto_brightness_turned_on", z ? "on" : "off");
        trackEvent("slide_brightness_bar", hashMap);
    }

    public void handleClickShortcutEvent(String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("shortcut", str);
        trackEvent("click_notification_bar_shortcut", hashMap);
    }

    public void handleQSDetailExitEvent(String str, boolean z, boolean z2, String str2) {
        HashMap hashMap = new HashMap();
        hashMap.put("qs_tile_name", str);
        hashMap.put("click_item", Integer.valueOf(z ? 1 : 0));
        hashMap.put("click_switch", Integer.valueOf(z2 ? 1 : 0));
        hashMap.put("exit_mode", str2);
        trackEvent("event_qs_detail_exit", hashMap);
    }

    public void handleQSEditExitEvent(boolean z, boolean z2, String str) {
        HashMap hashMap = new HashMap();
        hashMap.put("click_reset", Integer.valueOf(z ? 1 : 0));
        hashMap.put("qs_tile_move", Integer.valueOf(z2 ? 1 : 0));
        hashMap.put("exit_mode", str);
        trackEvent("event_qs_edit_exit", hashMap);
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
