package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.os.UserManager;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.MiuiAlarmController$MiuiAlarmChangeCallback;
import com.android.systemui.statusbar.policy.MiuiAlarmControllerImpl;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.time.DateFormatUtil;
import java.util.concurrent.Executor;
import miui.securityspace.XSpaceUserHandle;

public class MiuiPhoneStatusBarPolicy extends PhoneStatusBarPolicy implements MiuiAlarmController$MiuiAlarmChangeCallback {
    private byte mBluetoothFlowState = 0;
    protected Context mContext;
    private int mCurrentProfileId;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if ("android.media.extra.AUDIO_MIC_PLUG_STATE".equals(intent.getAction())) {
                MiuiPhoneStatusBarPolicy.this.updateMicphonePlug(intent);
            }
        }
    };
    private boolean mManagedProfileIconVisible = false;
    private boolean mManagedProfileInQuietMode = false;
    private boolean mRingerVisible;
    private final ContentObserver mSecondSpaceStatusIconObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.statusbar.phone.MiuiPhoneStatusBarPolicy.AnonymousClass1 */

        public void onChange(boolean z) {
            MiuiPhoneStatusBarPolicy miuiPhoneStatusBarPolicy = MiuiPhoneStatusBarPolicy.this;
            miuiPhoneStatusBarPolicy.mSecondSpaceStatusIconVisible = MiuiSettings.Global.isOpenSecondSpaceStatusIcon(miuiPhoneStatusBarPolicy.mContext.getContentResolver());
            MiuiPhoneStatusBarPolicy.this.updateManagedProfile();
        }
    };
    private boolean mSecondSpaceStatusIconVisible;
    protected final String mSlotBluetoothBattery;
    protected final String mSlotMicphone;
    protected final String mSlotSyncActive;
    StatusBarManager mStatusBarManager;
    MiuiDripLeftStatusBarIconControllerImpl miuiDripLeftStatusBarIconController;

    public MiuiPhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController, CommandQueue commandQueue, BroadcastDispatcher broadcastDispatcher, Executor executor, Resources resources, CastController castController, HotspotController hotspotController, BluetoothController bluetoothController, NextAlarmController nextAlarmController, UserInfoController userInfoController, RotationLockController rotationLockController, DataSaverController dataSaverController, ZenModeController zenModeController, DeviceProvisionedController deviceProvisionedController, KeyguardStateController keyguardStateController, LocationController locationController, SensorPrivacyController sensorPrivacyController, IActivityManager iActivityManager, AlarmManager alarmManager, UserManager userManager, RecordingController recordingController, TelecomManager telecomManager, int i, SharedPreferences sharedPreferences, DateFormatUtil dateFormatUtil, RingerModeTracker ringerModeTracker) {
        super(statusBarIconController, commandQueue, broadcastDispatcher, executor, resources, castController, hotspotController, bluetoothController, nextAlarmController, userInfoController, rotationLockController, dataSaverController, zenModeController, deviceProvisionedController, keyguardStateController, locationController, sensorPrivacyController, iActivityManager, alarmManager, userManager, recordingController, telecomManager, i, sharedPreferences, dateFormatUtil, ringerModeTracker);
        this.mContext = context;
        this.mSlotBluetoothBattery = "bluetooth_handsfree_battery";
        this.mSlotSyncActive = "sync_active";
        this.mSlotMicphone = "micphone";
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.miuiDripLeftStatusBarIconController = (MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("open_second_space_status_icon"), false, this.mSecondSpaceStatusIconObserver);
        this.mSecondSpaceStatusIconObserver.onChange(false);
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarPolicy
    public void miuiInit() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.extra.AUDIO_MIC_PLUG_STATE");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, this.mHandler);
        this.mIconController.setIcon(this.mSlotZen, C0013R$drawable.stat_sys_quiet_mode, null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotSyncActive, C0013R$drawable.stat_sys_sync, null);
        this.mIconController.setIconVisibility(this.mSlotSyncActive, false);
        this.mIconController.setIcon(this.mSlotLocation, C0013R$drawable.stat_sys_gps_on, this.mResources.getString(C0021R$string.accessibility_location_active));
        this.mIconController.setIconVisibility(this.mSlotLocation, false);
        this.mIconController.setIcon(this.mSlotRotate, C0013R$drawable.stat_sys_rotate_portrait, null);
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
        this.mIconController.setIcon(this.mSlotHotspot, C0013R$drawable.stat_sys_wifi_ap_on, this.mResources.getString(C0021R$string.accessibility_status_bar_hotspot));
        this.mIconController.setIconVisibility(this.mSlotHotspot, false);
        this.mIconController.setIcon(this.mSlotVolume, C0013R$drawable.stat_sys_ringer_vibrate, this.mContext.getString(C0021R$string.accessibility_ringer_vibrate));
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        this.mIconController.setIcon(this.mSlotHeadset, C0013R$drawable.stat_sys_headset, this.mResources.getString(C0021R$string.accessibility_status_bar_headset));
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
        this.mIconController.setIcon(this.mSlotMicphone, C0013R$drawable.stat_sys_micphone, null);
        this.mIconController.setIconVisibility(this.mSlotMicphone, false);
        this.miuiDripLeftStatusBarIconController.setIcon(this.mSlotVolume, C0013R$drawable.stat_sys_ringer_vibrate, this.mContext.getString(C0021R$string.accessibility_ringer_vibrate));
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotVolume, false);
        this.miuiDripLeftStatusBarIconController.setIcon(this.mSlotZen, C0013R$drawable.stat_sys_quiet_mode, null);
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotZen, false);
        initVloumeZen();
        this.miuiDripLeftStatusBarIconController.setIcon(this.mSlotHeadset, C0013R$drawable.stat_sys_headset, this.mResources.getString(C0021R$string.accessibility_status_bar_headset));
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotHeadset, false);
        this.miuiDripLeftStatusBarIconController.setIcon(this.mSlotAlarmClock, C0013R$drawable.stat_sys_alarm, this.mResources.getString(C0021R$string.status_bar_alarm));
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.miuiDripLeftStatusBarIconController.setIcon(this.mSlotMicphone, C0013R$drawable.stat_sys_micphone, null);
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotMicphone, false);
        this.mStatusBarManager.setIcon("mute", C0013R$drawable.stat_notify_call_mute, 0, null);
        this.mStatusBarManager.setIconVisibility("mute", false);
        this.mStatusBarManager.setIcon("speakerphone", C0013R$drawable.stat_sys_speakerphone, 0, null);
        this.mStatusBarManager.setIconVisibility("speakerphone", false);
        this.mStatusBarManager.setIcon("call_record", C0013R$drawable.stat_sys_call_record, 0, null);
        this.mStatusBarManager.setIconVisibility("call_record", false);
        ((MiuiAlarmControllerImpl) Dependency.get(MiuiAlarmControllerImpl.class)).addCallback((MiuiAlarmController$MiuiAlarmChangeCallback) this);
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothInoutStateChange(String str) {
        updateBluetooth(str);
    }

    @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
    public void onBluetoothBatteryChange(Intent intent) {
        updateBluetoothHandsfreeBattery(intent);
    }

    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarPolicy
    public void updateBluetooth(String str) {
        int i;
        Log.d("MiuiPhoneStatusBarPolicy", "updateBluetooth: action = " + str);
        int i2 = C0013R$drawable.stat_sys_data_bluetooth;
        String string = this.mResources.getString(C0021R$string.accessibility_quick_settings_bluetooth_on);
        BluetoothController bluetoothController = this.mBluetooth;
        boolean z = false;
        if (bluetoothController != null) {
            boolean isBluetoothEnabled = bluetoothController.isBluetoothEnabled();
            boolean isBluetoothConnected = this.mBluetooth.isBluetoothConnected();
            if (!isBluetoothEnabled) {
                this.mIconController.setIconVisibility(this.mSlotBluetoothBattery, false);
            }
            if (isBluetoothConnected) {
                i2 = C0013R$drawable.stat_sys_data_bluetooth_connected;
                string = this.mResources.getString(C0021R$string.accessibility_bluetooth_connected);
            }
            if ("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_START".equals(str)) {
                this.mBluetoothFlowState = (byte) (this.mBluetoothFlowState | 1);
            } else if ("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_END".equals(str)) {
                this.mBluetoothFlowState = (byte) (this.mBluetoothFlowState & -2);
            } else if ("com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_START".equals(str)) {
                this.mBluetoothFlowState = (byte) (this.mBluetoothFlowState | 2);
            } else if ("com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_END".equals(str)) {
                this.mBluetoothFlowState = (byte) (this.mBluetoothFlowState & -3);
            }
            byte b = this.mBluetoothFlowState;
            if (b == 1) {
                i = C0013R$drawable.stat_sys_data_bluetooth_in;
            } else if (b == 2) {
                i = C0013R$drawable.stat_sys_data_bluetooth_out;
            } else {
                if (b == 3) {
                    i = C0013R$drawable.stat_sys_data_bluetooth_inout;
                }
                Log.d("MiuiPhoneStatusBarPolicy", "updateBluetooth: BluetoothFlowState = " + ((int) this.mBluetoothFlowState));
                z = isBluetoothEnabled;
            }
            i2 = i;
            Log.d("MiuiPhoneStatusBarPolicy", "updateBluetooth: BluetoothFlowState = " + ((int) this.mBluetoothFlowState));
            z = isBluetoothEnabled;
        }
        this.mIconController.setIcon(this.mSlotBluetooth, i2, string);
        this.mIconController.setIconVisibility(this.mSlotBluetooth, z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarPolicy
    public void profileChanged(int i) {
        this.mCurrentProfileId = i;
        updateManagedProfile();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarPolicy
    public void updateManagedProfile() {
        this.mUiBgExecutor.execute(new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiPhoneStatusBarPolicy$YLvW0eaWPNgXkJGDF9CFm2822yk */

            public final void run() {
                MiuiPhoneStatusBarPolicy.this.lambda$updateManagedProfile$1$MiuiPhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateManagedProfile$1 */
    public /* synthetic */ void lambda$updateManagedProfile$1$MiuiPhoneStatusBarPolicy() {
        try {
            boolean isManagedProfile = this.mUserManager.isManagedProfile(ActivityTaskManager.getService().getLastResumedActivityUserId());
            boolean z = true;
            boolean z2 = (this.mCurrentUserId == 0 || this.mCurrentUserId == UserSwitcherController.getMaintenanceModeId() || !this.mSecondSpaceStatusIconVisible) ? false : true;
            if (this.mCurrentUserId != 0 || !XSpaceUserHandle.isXSpaceUserId(this.mCurrentProfileId)) {
                z = false;
            }
            Log.d("MiuiPhoneStatusBarPolicy", "updateManagedProfile: secondSpace = " + z2);
            this.mHandler.post(new Runnable(z, isManagedProfile, z2) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiPhoneStatusBarPolicy$jlZWxWXhPRdh80FQ2dWSJJ1ZjU */
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ boolean f$2;
                public final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    MiuiPhoneStatusBarPolicy.this.lambda$updateManagedProfile$0$MiuiPhoneStatusBarPolicy(this.f$1, this.f$2, this.f$3);
                }
            });
        } catch (RemoteException e) {
            Log.w("MiuiPhoneStatusBarPolicy", "updateManagedProfile: ", e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateManagedProfile$0 */
    public /* synthetic */ void lambda$updateManagedProfile$0$MiuiPhoneStatusBarPolicy(boolean z, boolean z2, boolean z3) {
        boolean z4 = false;
        if (!z) {
            if (z2 && !this.mKeyguardStateController.isShowing()) {
                this.mIconController.setIcon(this.mSlotManagedProfile, C0013R$drawable.stat_sys_managed_profile_status, this.mContext.getString(C0021R$string.accessibility_managed_profile));
            } else if (this.mManagedProfileInQuietMode) {
                this.mIconController.setIcon(this.mSlotManagedProfile, C0013R$drawable.stat_sys_managed_profile_status_off, this.mContext.getString(C0021R$string.accessibility_managed_profile));
            } else if (z3) {
                z4 = !this.mKeyguardStateController.isShowing();
                this.mIconController.setIcon(this.mSlotManagedProfile, C0013R$drawable.stat_sys_managed_profile_not_owner_user, this.mContext.getString(C0021R$string.accessibility_managed_profile));
            }
            z4 = true;
        }
        if (this.mManagedProfileIconVisible != z4) {
            this.mIconController.setIconVisibility(this.mSlotManagedProfile, z4);
            this.mManagedProfileIconVisible = z4;
        }
    }

    /* access modifiers changed from: protected */
    public void updateBluetoothHandsfreeBattery(Intent intent) {
        boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.show_bluetooth_handsfree_battery", true);
        Log.d("MiuiPhoneStatusBarPolicy", "updateBluetoothHandsfreeBattery: show = " + booleanExtra);
        if (!booleanExtra) {
            this.mIconController.setIconVisibility(this.mSlotBluetoothBattery, false);
            return;
        }
        int intExtra = intent.getIntExtra("android.intent.extra.bluetooth_handsfree_battery_level", 0) + 1;
        Log.d("MiuiPhoneStatusBarPolicy", "updateBluetoothHandsfreeBattery: level = " + intExtra);
        switch (intExtra) {
            case 1:
            case 2:
                this.mIconController.setIcon(this.mSlotBluetoothBattery, C0013R$drawable.stat_sys_bluetooth_handsfree_battery_1, this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_handsfree_battery_level, Integer.valueOf(intExtra * 10)));
                break;
            case 3:
            case 4:
                this.mIconController.setIcon(this.mSlotBluetoothBattery, C0013R$drawable.stat_sys_bluetooth_handsfree_battery_2, this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_handsfree_battery_level, Integer.valueOf(intExtra * 10)));
                break;
            case 5:
            case 6:
                this.mIconController.setIcon(this.mSlotBluetoothBattery, C0013R$drawable.stat_sys_bluetooth_handsfree_battery_3, this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_handsfree_battery_level, Integer.valueOf(intExtra * 10)));
                break;
            case 7:
            case 8:
                this.mIconController.setIcon(this.mSlotBluetoothBattery, C0013R$drawable.stat_sys_bluetooth_handsfree_battery_4, this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_handsfree_battery_level, Integer.valueOf(intExtra * 10)));
                break;
            default:
                this.mIconController.setIcon(this.mSlotBluetoothBattery, C0013R$drawable.stat_sys_bluetooth_handsfree_battery_5, this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_handsfree_battery_level, Integer.valueOf(intExtra * 10)));
                break;
        }
        this.mIconController.setIconVisibility(this.mSlotBluetoothBattery, true);
    }

    /* access modifiers changed from: protected */
    public void updateMicphonePlug(Intent intent) {
        boolean z = intent.getIntExtra("state", 0) != 0;
        Log.d("MiuiPhoneStatusBarPolicy", "updateMicphonePlug: intent = " + intent + " connected = " + z);
        if (z) {
            this.mIconController.setIconVisibility(this.mSlotMicphone, true);
            this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotMicphone, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotMicphone, false);
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotMicphone, false);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onVibrateChanged(boolean z) {
        updateVolumeZen();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarPolicy
    public void updateVolumeZen() {
        String str;
        int i;
        Log.d("MiuiPhoneStatusBarPolicy", "updateZenAndRinger: zenVisible = " + this.mZenVisible + " ringerVisible = " + this.mRingerVisible);
        if (this.mZenVisible) {
            this.mIconController.setIconVisibility(this.mSlotZen, true);
            this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotZen, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotZen, false);
            this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotZen, false);
        }
        if (!this.mRingerVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, false);
            this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotVolume, false);
            return;
        }
        if (this.mZenController.isVibrateOn()) {
            i = C0013R$drawable.stat_sys_ringer_vibrate;
            str = this.mContext.getString(C0021R$string.accessibility_ringer_vibrate);
        } else {
            i = C0013R$drawable.stat_sys_ringer_silent;
            str = this.mContext.getString(C0021R$string.accessibility_ringer_silent);
        }
        this.mIconController.setIcon(this.mSlotVolume, i, str);
        this.mIconController.setIconVisibility(this.mSlotVolume, true);
        this.miuiDripLeftStatusBarIconController.setIcon(this.mSlotVolume, i, str);
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotVolume, true);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController.Callback
    public void onZenOrRingerChanged(boolean z, boolean z2) {
        this.mZenVisible = z;
        this.mRingerVisible = z2;
        updateVolumeZen();
    }

    /* access modifiers changed from: protected */
    public void updateLocation(Intent intent) {
        boolean z;
        String action = intent.getAction();
        int i = 0;
        boolean booleanExtra = intent.getBooleanExtra("enabled", false);
        if (!action.equals("android.location.GPS_FIX_CHANGE") || !booleanExtra) {
            action.equals("android.location.GPS_ENABLED_CHANGE");
            z = false;
        } else {
            i = C0013R$drawable.stat_sys_gps_on;
            z = true;
        }
        if (i != 0) {
            this.mIconController.setIcon(this.mSlotLocation, i, null);
        }
        this.mIconController.setIconVisibility(this.mSlotLocation, z);
    }

    @Override // com.android.systemui.statusbar.policy.LocationController.LocationChangeCallback
    public void onLocationStatusChanged(Intent intent) {
        updateLocation(intent);
    }

    /* access modifiers changed from: protected */
    public void initVloumeZen() {
        this.mRingerVisible = this.mZenController.isRingerModeOn();
        this.mZenVisible = this.mZenController.isZenModeOn();
        updateVolumeZen();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PhoneStatusBarPolicy
    public void updateHeadsetPlug(Intent intent) {
        int i;
        int i2;
        int i3;
        boolean z = intent.getIntExtra("state", 0) != 0;
        boolean z2 = intent.getIntExtra("microphone", 0) != 0;
        Log.d("MiuiPhoneStatusBarPolicy", "updateHeadsetPlug: intent = " + intent + " connected = " + z + " hasMic = " + z2);
        if (z) {
            Resources resources = this.mResources;
            if (z2) {
                i = C0021R$string.accessibility_status_bar_headset;
            } else {
                i = C0021R$string.accessibility_status_bar_headphones;
            }
            String string = resources.getString(i);
            StatusBarIconController statusBarIconController = this.mIconController;
            String str = this.mSlotHeadset;
            if (z2) {
                i2 = C0013R$drawable.stat_sys_headset;
            } else {
                i2 = C0013R$drawable.stat_sys_headset_without_mic;
            }
            statusBarIconController.setIcon(str, i2, string);
            this.mIconController.setIconVisibility(this.mSlotHeadset, true);
            MiuiDripLeftStatusBarIconControllerImpl miuiDripLeftStatusBarIconControllerImpl = this.miuiDripLeftStatusBarIconController;
            String str2 = this.mSlotHeadset;
            if (z2) {
                i3 = C0013R$drawable.stat_sys_headset;
            } else {
                i3 = C0013R$drawable.stat_sys_headset_without_mic;
            }
            miuiDripLeftStatusBarIconControllerImpl.setIcon(str2, i3, string);
            this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotHeadset, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotHeadset, false);
    }

    @Override // com.android.systemui.statusbar.policy.MiuiAlarmController$MiuiAlarmChangeCallback
    public void onNextAlarmChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, z);
        this.miuiDripLeftStatusBarIconController.setIconVisibility(this.mSlotAlarmClock, z);
    }
}
