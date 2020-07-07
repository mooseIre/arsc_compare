package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.AlarmManager;
import android.app.AppGlobals;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.app.SynchronousUserSwitchObserverCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.content.pm.UserInfoCompat;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.notification.Condition;
import android.service.notification.StatusBarNotification;
import android.service.notification.ZenModeConfig;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.DockedStackExistsListener;
import com.android.systemui.SystemUI;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.ApplicationInfoHelper;
import com.xiaomi.stat.MiStat;
import java.util.Iterator;
import java.util.function.Consumer;
import miui.securityspace.XSpaceUserHandle;
import miui.util.AudioManagerHelper;

public class PhoneStatusBarPolicy implements BluetoothController.Callback, CommandQueue.Callbacks, RotationLockController.RotationLockControllerCallback, DataSaverController.Listener, LocationController.LocationChangeCallback, ZenModeController.Callback, DeviceProvisionedController.DeviceProvisionedListener, KeyguardMonitor.Callback {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    public static final int LOCATION_STATUS_ACQUIRING_ICON_ID = (Constants.SUPPORT_DUAL_GPS ? R.drawable.stat_sys_dual_gps_acquiring : R.drawable.stat_sys_gps_acquiring);
    public static final int LOCATION_STATUS_ON_ICON_ID = ((Build.VERSION.SDK_INT > 28 || !Constants.SUPPORT_DUAL_GPS) ? R.drawable.stat_sys_gps_on : R.drawable.stat_sys_dual_gps_on);
    private final String ACTION_MICPHONE_PLUG = "android.media.extra.AUDIO_MIC_PLUG_STATE";
    private final AlarmManager mAlarmManager;
    private BluetoothController mBluetooth;
    private byte mBluetoothFlowState;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final ArraySet<Pair<String, Integer>> mCurrentNotifs = new ArraySet<>();
    /* access modifiers changed from: private */
    public int mCurrentProfileId;
    /* access modifiers changed from: private */
    public int mCurrentUserId;
    private boolean mCurrentUserSetup;
    private final DataSaverController mDataSaver;
    /* access modifiers changed from: private */
    public boolean mDockedStackExists;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public final StatusBarIconController mIconController;
    private BroadcastReceiver mIntentReceiver;
    /* access modifiers changed from: private */
    public final KeyguardMonitor mKeyguardMonitor;
    private final LocationController mLocationController;
    /* access modifiers changed from: private */
    public boolean mManagedProfileIconVisible;
    /* access modifiers changed from: private */
    public boolean mManagedProfileInQuietMode;
    private final NextAlarmController mNextAlarm;
    private final NextAlarmController.NextAlarmChangeCallback mNextAlarmCallback;
    private final DeviceProvisionedController mProvisionedController;
    private Runnable mRemoveCastIconRunnable;
    private final RotationLockController mRotationLockController;
    private final ContentObserver mSecondSpaceStatusIconObsever;
    /* access modifiers changed from: private */
    public boolean mSecondSpaceStatusIconVisible;
    private final StatusBarManager mService;
    private final String mSlotAlarmClock;
    private final String mSlotBluetooth;
    private final String mSlotBluetoothBattery;
    private final String mSlotCallrecord;
    /* access modifiers changed from: private */
    public final String mSlotCast;
    private final String mSlotDataSaver;
    private final String mSlotHeadset;
    private final String mSlotLocation;
    /* access modifiers changed from: private */
    public final String mSlotManagedProfile;
    private final String mSlotMicphone;
    private final String mSlotMute;
    private final String mSlotQuiet;
    private final String mSlotRotate;
    private final String mSlotSpeakerphone;
    private final String mSlotSyncActive;
    private final String mSlotTty;
    private final String mSlotVolume;
    private final String mSlotZen;
    private final SystemServicesProxy.TaskStackListener mTaskListener;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));
    /* access modifiers changed from: private */
    public int mUserIdLegacy;
    /* access modifiers changed from: private */
    public final UserInfoController mUserInfoController;
    /* access modifiers changed from: private */
    public final UserManager mUserManager;
    private final SynchronousUserSwitchObserverCompat mUserSwitchListener;
    private boolean mVolumeVisible;
    private final ZenModeController mZenController;

    private final void updateVolumeZen() {
    }

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onConditionsChanged(Condition[] conditionArr) {
    }

    public void onConfigChanged(ZenModeConfig zenModeConfig) {
    }

    public void onDeviceProvisionedChanged() {
    }

    public void onEffectsSupressorChanged() {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void onLocationSettingsChanged(boolean z) {
    }

    public void onManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
    }

    public void onNextAlarmChanged() {
    }

    public void onUserSwitched() {
    }

    public void onZenAvailableChanged(boolean z) {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    public PhoneStatusBarPolicy(Context context, StatusBarIconController statusBarIconController) {
        this.mManagedProfileIconVisible = false;
        this.mManagedProfileInQuietMode = false;
        this.mSecondSpaceStatusIconObsever = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                PhoneStatusBarPolicy phoneStatusBarPolicy = PhoneStatusBarPolicy.this;
                boolean unused = phoneStatusBarPolicy.mSecondSpaceStatusIconVisible = MiuiSettings.Global.isOpenSecondSpaceStatusIcon(phoneStatusBarPolicy.mContext.getContentResolver());
                PhoneStatusBarPolicy.this.updateManagedProfile();
            }
        };
        this.mBluetoothFlowState = 0;
        this.mUserSwitchListener = new SynchronousUserSwitchObserverCompat() {
            public void onUserSwitching(int i) throws RemoteException {
                PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                    public void run() {
                        PhoneStatusBarPolicy.this.mUserInfoController.reloadUserInfo();
                    }
                });
            }

            public void onUserSwitchComplete(final int i) throws RemoteException {
                PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                    public void run() {
                        int unused = PhoneStatusBarPolicy.this.mUserIdLegacy = i;
                        int unused2 = PhoneStatusBarPolicy.this.mCurrentUserId = ActivityManager.getCurrentUser();
                        PhoneStatusBarPolicy.this.updateQuietState();
                        PhoneStatusBarPolicy.this.updateManagedProfile();
                        PhoneStatusBarPolicy.this.updateForegroundInstantApps();
                    }
                });
            }

            public void onForegroundProfileSwitch(int i) throws RemoteException {
                int unused = PhoneStatusBarPolicy.this.mUserIdLegacy = i;
                PhoneStatusBarPolicy.this.profileChanged(i);
            }
        };
        this.mNextAlarmCallback = new NextAlarmController.NextAlarmChangeCallback() {
            public void onNextAlarmChanged(boolean z) {
                PhoneStatusBarPolicy.this.updateAlarm(z);
            }
        };
        this.mTaskListener = new SystemServicesProxy.TaskStackListener() {
            public void onTaskStackChanged() {
                PhoneStatusBarPolicy.this.updateForegroundInstantApps();
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals("android.media.RINGER_MODE_CHANGED") || action.equals("android.media.VIBRATE_SETTING_CHANGED")) {
                    if (!MiuiSettings.SilenceMode.isSupported) {
                        PhoneStatusBarPolicy.this.updateVolume();
                    }
                } else if (action.equals("android.telecom.action.CURRENT_TTY_MODE_CHANGED")) {
                    PhoneStatusBarPolicy.this.updateTTY(intent);
                } else if (action.equals("android.intent.action.MANAGED_PROFILE_AVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_UNAVAILABLE") || action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                    PhoneStatusBarPolicy.this.updateQuietState();
                    PhoneStatusBarPolicy.this.updateManagedProfile();
                } else if (action.equals("android.intent.action.HEADSET_PLUG")) {
                    PhoneStatusBarPolicy.this.updateHeadsetPlug(intent);
                } else if (action.equals("android.media.extra.AUDIO_MIC_PLUG_STATE")) {
                    PhoneStatusBarPolicy.this.updateMicphonePlug(intent);
                } else if ("android.intent.action.BLUETOOTH_HANDSFREE_BATTERY_CHANGED".equals(action)) {
                    PhoneStatusBarPolicy.this.updateBluetoothHandsfreeBattery(intent);
                }
            }
        };
        this.mRemoveCastIconRunnable = new Runnable() {
            public void run() {
                if (PhoneStatusBarPolicy.DEBUG) {
                    Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
                }
                PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotCast, false);
            }
        };
        this.mContext = context;
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mIconController = statusBarIconController;
        this.mService = (StatusBarManager) context.getSystemService("statusbar");
        this.mBluetooth = (BluetoothController) Dependency.get(BluetoothController.class);
        this.mNextAlarm = (NextAlarmController) Dependency.get(NextAlarmController.class);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mUserInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mRotationLockController = (RotationLockController) Dependency.get(RotationLockController.class);
        this.mDataSaver = (DataSaverController) Dependency.get(DataSaverController.class);
        this.mZenController = (ZenModeController) Dependency.get(ZenModeController.class);
        this.mProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
        this.mKeyguardMonitor = (KeyguardMonitor) Dependency.get(KeyguardMonitor.class);
        this.mLocationController = (LocationController) Dependency.get(LocationController.class);
        this.mSlotCast = "cast";
        this.mSlotBluetooth = "bluetooth";
        this.mSlotTty = "tty";
        this.mSlotZen = "zen";
        this.mSlotVolume = "volume";
        this.mSlotAlarmClock = "alarm_clock";
        this.mSlotManagedProfile = "managed_profile";
        this.mSlotRotate = "rotate";
        this.mSlotHeadset = "headset";
        this.mSlotMicphone = "micphone";
        this.mSlotDataSaver = "data_saver";
        this.mSlotLocation = MiStat.Param.LOCATION;
        this.mSlotSyncActive = "sync_active";
        this.mSlotQuiet = "quiet";
        this.mSlotMute = "mute";
        this.mSlotSpeakerphone = "speakerphone";
        this.mSlotCallrecord = "call_record";
        this.mSlotBluetoothBattery = "bluetooth_handsfree_battery";
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        intentFilter.addAction("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.media.extra.AUDIO_MIC_PLUG_STATE");
        intentFilter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        intentFilter.addAction("android.intent.action.BLUETOOTH_HANDSFREE_BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mContext.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, (String) null, this.mHandler);
        try {
            ActivityManagerCompat.registerUserSwitchObserver(this.mUserSwitchListener, "PhoneStatusBarPolicy");
        } catch (RemoteException unused) {
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("open_second_space_status_icon"), false, this.mSecondSpaceStatusIconObsever);
        this.mSecondSpaceStatusIconObsever.onChange(false);
        this.mIconController.setIcon(this.mSlotTty, R.drawable.stat_sys_tty_mode, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotTty, false);
        updateBluetooth((String) null);
        this.mIconController.setIcon(this.mSlotAlarmClock, R.drawable.stat_sys_alarm, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotVolume, R.drawable.stat_sys_ringer_vibrate, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        updateVolume();
        this.mIconController.setIcon(this.mSlotCast, R.drawable.stat_sys_cast, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotCast, false);
        this.mIconController.setIcon(this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status, this.mContext.getString(R.string.accessibility_managed_profile));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, R.drawable.stat_sys_data_saver, context.getString(R.string.accessibility_data_saver_on));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        this.mIconController.setIcon(this.mSlotSyncActive, R.drawable.stat_sys_sync_active, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotSyncActive, false);
        this.mService.setIcon(this.mSlotMute, R.drawable.stat_notify_call_mute, 0, (String) null);
        this.mService.setIconVisibility(this.mSlotMute, false);
        this.mService.setIcon(this.mSlotSpeakerphone, R.drawable.stat_sys_speakerphone, 0, (String) null);
        this.mService.setIconVisibility(this.mSlotSpeakerphone, false);
        this.mService.setIcon(this.mSlotCallrecord, R.drawable.stat_sys_call_record, 0, (String) null);
        this.mService.setIconVisibility(this.mSlotCallrecord, false);
        this.mIconController.setIcon(this.mSlotQuiet, R.drawable.stat_sys_quiet_mode, (CharSequence) null);
        if (MiuiSettings.SilenceMode.getZenMode(this.mContext) == 1) {
            setQuietMode(true);
            this.mIconController.setIconVisibility(this.mSlotVolume, false);
            this.mVolumeVisible = false;
        } else {
            setQuietMode(false);
        }
        this.mRotationLockController.addCallback(this);
        this.mBluetooth.addCallback(this);
        this.mProvisionedController.addCallback(this);
        this.mZenController.addCallback(this);
        this.mNextAlarm.addCallback(this.mNextAlarmCallback);
        this.mDataSaver.addCallback(this);
        this.mKeyguardMonitor.addCallback(this);
        this.mLocationController.addCallback(this);
        ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
        SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mTaskListener);
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        for (StatusBarNotification statusBarNotification : notificationManager.getActiveNotifications()) {
            if (statusBarNotification.getId() == 7) {
                notificationManager.cancel(statusBarNotification.getTag(), statusBarNotification.getId());
            }
        }
        DockedStackExistsListener.register(new Consumer() {
            public final void accept(Object obj) {
                PhoneStatusBarPolicy.this.lambda$new$0$PhoneStatusBarPolicy((Boolean) obj);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$PhoneStatusBarPolicy(Boolean bool) {
        this.mDockedStackExists = bool.booleanValue();
        updateForegroundInstantApps();
    }

    public void onZenChanged(int i) {
        updateVolumeZen();
    }

    public void onLocationActiveChanged(boolean z) {
        if (Build.VERSION.SDK_INT <= 28) {
            return;
        }
        if (this.mLocationController.isLocationActive()) {
            this.mIconController.setIcon(this.mSlotLocation, LOCATION_STATUS_ON_ICON_ID, (CharSequence) null);
            this.mIconController.setIconVisibility(this.mSlotLocation, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotLocation, false);
    }

    public void onLocationStatusChanged(Intent intent) {
        updateLocation(intent);
    }

    private void updateLocation(Intent intent) {
        String action = intent.getAction();
        int i = 0;
        boolean booleanExtra = intent.getBooleanExtra("enabled", false);
        boolean z = true;
        if (action.equals("android.location.GPS_FIX_CHANGE") && booleanExtra) {
            i = LOCATION_STATUS_ON_ICON_ID;
        } else if (!action.equals("android.location.GPS_ENABLED_CHANGE") || booleanExtra) {
            i = LOCATION_STATUS_ACQUIRING_ICON_ID;
        } else {
            z = false;
        }
        if (i != 0) {
            this.mIconController.setIcon(this.mSlotLocation, i, (CharSequence) null);
        }
        this.mIconController.setIconVisibility(this.mSlotLocation, z);
    }

    /* access modifiers changed from: private */
    public void updateAlarm(boolean z) {
        boolean z2 = true;
        this.mIconController.setIcon(this.mSlotAlarmClock, this.mZenController.getZen() == 2 ? R.drawable.stat_sys_alarm_dim : R.drawable.stat_sys_alarm, this.mContext.getString(R.string.accessibility_quick_settings_alarm_on));
        StatusBarIconController statusBarIconController = this.mIconController;
        String str = this.mSlotAlarmClock;
        if (!this.mCurrentUserSetup || !z) {
            z2 = false;
        }
        statusBarIconController.setIconVisibility(str, z2);
    }

    /* access modifiers changed from: private */
    public final void updateVolume() {
        String str;
        int i;
        boolean isSilentEnabled = AudioManagerHelper.isSilentEnabled(this.mContext);
        if (AudioManagerHelper.isVibrateEnabled(this.mContext)) {
            i = R.drawable.stat_sys_ringer_vibrate;
            str = this.mContext.getString(R.string.accessibility_ringer_vibrate);
        } else {
            i = R.drawable.stat_sys_ringer_silent;
            str = this.mContext.getString(R.string.accessibility_ringer_silent);
        }
        if (isSilentEnabled) {
            this.mIconController.setIcon(this.mSlotVolume, i, str);
        }
        if (isSilentEnabled != this.mVolumeVisible) {
            this.mIconController.setIconVisibility(this.mSlotVolume, isSilentEnabled);
            this.mVolumeVisible = isSilentEnabled;
        }
    }

    public void setQuietMode(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotQuiet, z);
    }

    public void updateSilentModeIcon() {
        if (MiuiSettings.SilenceMode.getZenMode(this.mContext) == 1) {
            setQuietMode(true);
            this.mIconController.setIconVisibility(this.mSlotVolume, false);
            this.mVolumeVisible = false;
            return;
        }
        setQuietMode(false);
        updateVolume();
    }

    public void onBluetoothDevicesChanged() {
        updateBluetooth((String) null);
    }

    public void onBluetoothStateChange(boolean z) {
        updateBluetooth((String) null);
    }

    public void onBluetoothStatePhoneChange() {
        BluetoothController bluetoothController = this.mBluetooth;
        if (bluetoothController != null) {
            boolean isBluetoothEnabled = bluetoothController.isBluetoothEnabled();
            boolean isBluetoothPhoneConnected = this.mBluetooth.isBluetoothPhoneConnected();
            if (isBluetoothEnabled) {
                this.mIconController.setIconVisibility(this.mSlotBluetoothBattery, isBluetoothPhoneConnected || this.mBluetooth.getProfileConnectionState());
                Log.d("PhoneStatusBarPolicy", "onBluetoothStatePhoneChange bluetoothBattery bluetoothPhoneEnableConnected = " + isBluetoothPhoneConnected + " ProfileConnectionState = " + this.mBluetooth.getProfileConnectionState());
            }
        }
    }

    public void onBluetoothInoutStateChange(String str) {
        updateBluetooth(str);
    }

    private final void updateBluetooth(String str) {
        boolean z;
        String string = this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_on);
        BluetoothController bluetoothController = this.mBluetooth;
        int i = R.drawable.stat_sys_data_bluetooth;
        if (bluetoothController != null) {
            z = bluetoothController.isBluetoothEnabled();
            boolean isBluetoothConnected = this.mBluetooth.isBluetoothConnected();
            if (!z) {
                this.mIconController.setIconVisibility(this.mSlotBluetoothBattery, false);
                Log.d("PhoneStatusBarPolicy", "updateBluetooth bluetoothEnabled = " + z + " bluetoothBattery visible=false");
            }
            if (isBluetoothConnected) {
                i = R.drawable.stat_sys_data_bluetooth_connected;
                string = this.mContext.getString(R.string.accessibility_bluetooth_connected);
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
                i = R.drawable.stat_sys_data_bluetooth_in;
            } else if (b == 2) {
                i = R.drawable.stat_sys_data_bluetooth_out;
            } else if (b == 3) {
                i = R.drawable.stat_sys_data_bluetooth_inout;
            }
        } else {
            z = false;
        }
        this.mIconController.setIcon(this.mSlotBluetooth, i, string);
        this.mIconController.setIconVisibility(this.mSlotBluetooth, z);
    }

    /* access modifiers changed from: private */
    public final void updateTTY(Intent intent) {
        boolean z = intent.getIntExtra("android.telecom.intent.extra.CURRENT_TTY_MODE", 0) != 0;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + z);
        }
        if (z) {
            if (DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
            }
            this.mIconController.setIcon(this.mSlotTty, R.drawable.stat_sys_tty_mode, this.mContext.getString(R.string.accessibility_tty_enabled));
            this.mIconController.setIconVisibility(this.mSlotTty, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
        }
        this.mIconController.setIconVisibility(this.mSlotTty, false);
    }

    /* access modifiers changed from: private */
    public void updateQuietState() {
        this.mManagedProfileInQuietMode = false;
        for (UserInfo userInfo : this.mUserManager.getEnabledProfiles(KeyguardUpdateMonitor.getCurrentUser())) {
            if (userInfo.isManagedProfile() && UserInfoCompat.isQuietModeEnabled(userInfo)) {
                this.mManagedProfileInQuietMode = true;
                return;
            }
        }
    }

    public void profileChanged(int i) {
        this.mCurrentProfileId = i;
        updateManagedProfile();
    }

    /* access modifiers changed from: private */
    public void updateManagedProfile() {
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                try {
                    final boolean isManagedProfile = UserManagerCompat.isManagedProfile(PhoneStatusBarPolicy.this.mUserManager, ActivityManagerCompat.getLastResumedActivityUserId(PhoneStatusBarPolicy.this.mUserIdLegacy));
                    final boolean z = true;
                    final boolean z2 = PhoneStatusBarPolicy.this.mCurrentUserId != 0 && PhoneStatusBarPolicy.this.mSecondSpaceStatusIconVisible;
                    if (PhoneStatusBarPolicy.this.mCurrentUserId != 0 || !XSpaceUserHandle.isXSpaceUserId(PhoneStatusBarPolicy.this.mCurrentProfileId)) {
                        z = false;
                    }
                    PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                        public void run() {
                            boolean z = false;
                            if (!z) {
                                if (isManagedProfile && !PhoneStatusBarPolicy.this.mKeyguardMonitor.isShowing()) {
                                    PhoneStatusBarPolicy.this.mIconController.setIcon(PhoneStatusBarPolicy.this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status, PhoneStatusBarPolicy.this.mContext.getString(R.string.accessibility_managed_profile));
                                } else if (PhoneStatusBarPolicy.this.mManagedProfileInQuietMode) {
                                    PhoneStatusBarPolicy.this.mIconController.setIcon(PhoneStatusBarPolicy.this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_status_off, PhoneStatusBarPolicy.this.mContext.getString(R.string.accessibility_managed_profile));
                                } else if (z2) {
                                    z = !PhoneStatusBarPolicy.this.mKeyguardMonitor.isShowing();
                                    PhoneStatusBarPolicy.this.mIconController.setIcon(PhoneStatusBarPolicy.this.mSlotManagedProfile, R.drawable.stat_sys_managed_profile_not_owner_user, PhoneStatusBarPolicy.this.mContext.getString(R.string.accessibility_managed_profile));
                                }
                                z = true;
                            }
                            if (PhoneStatusBarPolicy.this.mManagedProfileIconVisible != z) {
                                PhoneStatusBarPolicy.this.mIconController.setIconVisibility(PhoneStatusBarPolicy.this.mSlotManagedProfile, z);
                                boolean unused = PhoneStatusBarPolicy.this.mManagedProfileIconVisible = z;
                            }
                        }
                    });
                } catch (RemoteException e) {
                    Log.w("PhoneStatusBarPolicy", "updateManagedProfile: ", e);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateForegroundInstantApps() {
        final NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService(NotificationManager.class);
        final ArraySet arraySet = new ArraySet(this.mCurrentNotifs);
        final IPackageManager packageManager = AppGlobals.getPackageManager();
        this.mCurrentNotifs.clear();
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                try {
                    if (ActivityManagerCompat.getFocusedStackId() == 1) {
                        PhoneStatusBarPolicy.this.checkStack(1, 1, 0, arraySet, notificationManager, packageManager);
                    }
                    if (PhoneStatusBarPolicy.this.mDockedStackExists) {
                        PhoneStatusBarPolicy.this.checkStack(3, 3, 0, arraySet, notificationManager, packageManager);
                    }
                } catch (Exception unused) {
                }
                Iterator it = arraySet.iterator();
                while (it.hasNext()) {
                    Pair pair = (Pair) it.next();
                    notificationManager.cancelAsUser((String) pair.first, 7, new UserHandle(((Integer) pair.second).intValue()));
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void checkStack(int i, int i2, int i3, ArraySet<Pair<String, Integer>> arraySet, NotificationManager notificationManager, IPackageManager iPackageManager) {
        try {
            ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(i, i2, i3);
            int userId = ActivityManagerCompat.getUserId(stackInfo);
            if (stackInfo == null) {
                return;
            }
            if (stackInfo.topActivity != null) {
                String packageName = stackInfo.topActivity.getPackageName();
                if (!hasNotif(arraySet, packageName, userId)) {
                    ApplicationInfoHelper.postEphemeralNotificationIfNeeded(this.mContext, packageName, userId, iPackageManager.getApplicationInfo(packageName, 8192, userId), notificationManager, stackInfo.taskIds[stackInfo.taskIds.length - 1], this.mCurrentNotifs);
                }
            }
        } catch (Exception unused) {
        }
    }

    private boolean hasNotif(ArraySet<Pair<String, Integer>> arraySet, String str, int i) {
        Pair pair = new Pair(str, Integer.valueOf(i));
        if (!arraySet.remove(pair)) {
            return false;
        }
        this.mCurrentNotifs.add(pair);
        return true;
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
        updateManagedProfile();
        updateForegroundInstantApps();
    }

    public void onKeyguardShowingChanged() {
        updateManagedProfile();
        updateForegroundInstantApps();
    }

    public void onUserSetupChanged() {
        DeviceProvisionedController deviceProvisionedController = this.mProvisionedController;
        boolean isUserSetup = deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser());
        if (this.mCurrentUserSetup != isUserSetup) {
            this.mCurrentUserSetup = isUserSetup;
            updateQuietState();
        }
    }

    public void preloadRecentApps() {
        updateForegroundInstantApps();
    }

    public void onRotationLockStateChanged(boolean z, boolean z2) {
        boolean isCurrentOrientationLockPortrait = RotationLockTile.isCurrentOrientationLockPortrait(this.mRotationLockController, this.mContext);
        if (z) {
            if (isCurrentOrientationLockPortrait) {
                this.mIconController.setIcon(this.mSlotRotate, R.drawable.stat_sys_rotate_portrait, this.mContext.getString(R.string.accessibility_rotation_lock_on_portrait));
            } else {
                this.mIconController.setIcon(this.mSlotRotate, R.drawable.stat_sys_rotate_landscape, this.mContext.getString(R.string.accessibility_rotation_lock_on_landscape));
            }
            this.mIconController.setIconVisibility(this.mSlotRotate, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
    }

    /* access modifiers changed from: private */
    public void updateHeadsetPlug(Intent intent) {
        boolean z = intent.getIntExtra("state", 0) != 0;
        boolean z2 = intent.getIntExtra("microphone", 0) != 0;
        Log.d("PhoneStatusBarPolicy", "intent=" + intent + "  connected=" + z + "  hasMic=" + z2);
        if (z) {
            this.mIconController.setIcon(this.mSlotHeadset, z2 ? R.drawable.stat_sys_headset : R.drawable.stat_sys_headset_without_mic, this.mContext.getString(z2 ? R.string.accessibility_status_bar_headset : R.string.accessibility_status_bar_headphones));
            this.mIconController.setIconVisibility(this.mSlotHeadset, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotHeadset, false);
    }

    /* access modifiers changed from: private */
    public void updateMicphonePlug(Intent intent) {
        boolean z = intent.getIntExtra("state", 0) != 0;
        Log.d("PhoneStatusBarPolicy", "intent=" + intent + "  connected=" + z);
        if (z) {
            this.mIconController.setIcon(this.mSlotMicphone, R.drawable.stat_sys_micphone, this.mContext.getString(R.string.accessibility_status_bar_micphone));
            this.mIconController.setIconVisibility(this.mSlotMicphone, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotMicphone, false);
    }

    public void onDataSaverChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, z);
    }

    /* access modifiers changed from: private */
    public final void updateBluetoothHandsfreeBattery(Intent intent) {
        boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.show_bluetooth_handsfree_battery", true);
        if (!booleanExtra) {
            this.mService.setIconVisibility(this.mSlotBluetoothBattery, false);
            Log.d("PhoneStatusBarPolicy", "updateBluetoothHandsfreeBattery visibile=" + booleanExtra);
            return;
        }
        int intExtra = intent.getIntExtra("android.intent.extra.bluetooth_handsfree_battery_level", 0) + 1;
        this.mService.setIcon(this.mSlotBluetoothBattery, R.drawable.stat_sys_bluetooth_handsfree_battery, intExtra, this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_handsfree_battery_level, new Object[]{Integer.valueOf(intExtra * 10)}));
        this.mService.setIconVisibility(this.mSlotBluetoothBattery, true);
        Log.d("PhoneStatusBarPolicy", "updateBluetoothHandsfreeBattery visibile= true");
    }
}
