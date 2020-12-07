package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IActivityManager;
import android.app.SynchronousUserSwitchObserver;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.notification.ZenModeConfig;
import android.telecom.TelecomManager;
import android.util.Log;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SensorPrivacyController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.time.DateFormatUtil;
import java.util.concurrent.Executor;

public class PhoneStatusBarPolicy implements BluetoothController.Callback, CommandQueue.Callbacks, RotationLockController.RotationLockControllerCallback, DataSaverController.Listener, ZenModeController.Callback, DeviceProvisionedController.DeviceProvisionedListener, KeyguardStateController.Callback, LocationController.LocationChangeCallback, RecordingController.RecordingStateChangeCallback {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("PhoneStatusBarPolicy", 3);
    protected BluetoothController mBluetooth;
    protected final BroadcastDispatcher mBroadcastDispatcher;
    protected final CastController mCast;
    protected final CommandQueue mCommandQueue;
    protected int mCurrentUserId;
    protected boolean mCurrentUserSetup;
    protected final DataSaverController mDataSaver;
    protected final int mDisplayId;
    protected final Handler mHandler = new Handler();
    protected final HotspotController mHotspot;
    private final HotspotController.Callback mHotspotCallback = new HotspotController.Callback() {
        public void onHotspotChanged(boolean z, int i) {
            PhoneStatusBarPolicy phoneStatusBarPolicy = PhoneStatusBarPolicy.this;
            phoneStatusBarPolicy.mIconController.setIconVisibility(phoneStatusBarPolicy.mSlotHotspot, z);
        }

        public void onHotspotChanged(boolean z, int i, int i2) {
            PhoneStatusBarPolicy phoneStatusBarPolicy = PhoneStatusBarPolicy.this;
            phoneStatusBarPolicy.mIconController.setIconVisibility(phoneStatusBarPolicy.mSlotHotspot, z);
        }
    };
    protected final IActivityManager mIActivityManager;
    protected final StatusBarIconController mIconController;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r8, android.content.Intent r9) {
            /*
                r7 = this;
                java.lang.String r8 = r9.getAction()
                int r0 = r8.hashCode()
                r1 = 5
                r2 = 4
                r3 = 3
                r4 = 2
                r5 = 1
                r6 = 0
                switch(r0) {
                    case -1676458352: goto L_0x0044;
                    case -1238404651: goto L_0x003a;
                    case -864107122: goto L_0x0030;
                    case -229777127: goto L_0x0026;
                    case 1051344550: goto L_0x001c;
                    case 1051477093: goto L_0x0012;
                    default: goto L_0x0011;
                }
            L_0x0011:
                goto L_0x004e
            L_0x0012:
                java.lang.String r0 = "android.intent.action.MANAGED_PROFILE_REMOVED"
                boolean r8 = r8.equals(r0)
                if (r8 == 0) goto L_0x004e
                r8 = r2
                goto L_0x004f
            L_0x001c:
                java.lang.String r0 = "android.telecom.action.CURRENT_TTY_MODE_CHANGED"
                boolean r8 = r8.equals(r0)
                if (r8 == 0) goto L_0x004e
                r8 = r5
                goto L_0x004f
            L_0x0026:
                java.lang.String r0 = "android.intent.action.SIM_STATE_CHANGED"
                boolean r8 = r8.equals(r0)
                if (r8 == 0) goto L_0x004e
                r8 = r6
                goto L_0x004f
            L_0x0030:
                java.lang.String r0 = "android.intent.action.MANAGED_PROFILE_AVAILABLE"
                boolean r8 = r8.equals(r0)
                if (r8 == 0) goto L_0x004e
                r8 = r4
                goto L_0x004f
            L_0x003a:
                java.lang.String r0 = "android.intent.action.MANAGED_PROFILE_UNAVAILABLE"
                boolean r8 = r8.equals(r0)
                if (r8 == 0) goto L_0x004e
                r8 = r3
                goto L_0x004f
            L_0x0044:
                java.lang.String r0 = "android.intent.action.HEADSET_PLUG"
                boolean r8 = r8.equals(r0)
                if (r8 == 0) goto L_0x004e
                r8 = r1
                goto L_0x004f
            L_0x004e:
                r8 = -1
            L_0x004f:
                if (r8 == 0) goto L_0x0074
                if (r8 == r5) goto L_0x0068
                if (r8 == r4) goto L_0x0062
                if (r8 == r3) goto L_0x0062
                if (r8 == r2) goto L_0x0062
                if (r8 == r1) goto L_0x005c
                goto L_0x007a
            L_0x005c:
                com.android.systemui.statusbar.phone.PhoneStatusBarPolicy r7 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.this
                r7.updateHeadsetPlug(r9)
                goto L_0x007a
            L_0x0062:
                com.android.systemui.statusbar.phone.PhoneStatusBarPolicy r7 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.this
                r7.updateManagedProfile()
                goto L_0x007a
            L_0x0068:
                com.android.systemui.statusbar.phone.PhoneStatusBarPolicy r7 = com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.this
                java.lang.String r8 = "android.telecom.extra.CURRENT_TTY_MODE"
                int r8 = r9.getIntExtra(r8, r6)
                r7.updateTTY(r8)
                goto L_0x007a
            L_0x0074:
                java.lang.String r7 = "rebroadcastOnUnlock"
                boolean r7 = r9.getBooleanExtra(r7, r6)
            L_0x007a:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.AnonymousClass6.onReceive(android.content.Context, android.content.Intent):void");
        }
    };
    protected final KeyguardStateController mKeyguardStateController;
    protected final LocationController mLocationController;
    protected boolean mManagedProfileIconVisible = false;
    /* access modifiers changed from: private */
    public AlarmManager.AlarmClockInfo mNextAlarm;
    private final NextAlarmController.NextAlarmChangeCallback mNextAlarmCallback = new NextAlarmController.NextAlarmChangeCallback() {
        public void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo) {
            AlarmManager.AlarmClockInfo unused = PhoneStatusBarPolicy.this.mNextAlarm = alarmClockInfo;
            PhoneStatusBarPolicy.this.updateAlarm();
        }
    };
    protected final NextAlarmController mNextAlarmController;
    protected final DeviceProvisionedController mProvisionedController;
    protected final RecordingController mRecordingController;
    private Runnable mRemoveCastIconRunnable = new Runnable() {
        public void run() {
            if (PhoneStatusBarPolicy.DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateCast: hiding icon NOW");
            }
            PhoneStatusBarPolicy phoneStatusBarPolicy = PhoneStatusBarPolicy.this;
            phoneStatusBarPolicy.mIconController.setIconVisibility(phoneStatusBarPolicy.mSlotCast, false);
        }
    };
    protected final Resources mResources;
    protected final RotationLockController mRotationLockController;
    protected final SensorPrivacyController mSensorPrivacyController;
    protected final String mSlotAlarmClock;
    protected final String mSlotBluetooth;
    protected final String mSlotCast;
    protected final String mSlotDataSaver;
    protected final String mSlotHeadset;
    protected final String mSlotHotspot;
    protected final String mSlotLocation;
    protected final String mSlotManagedProfile;
    protected final String mSlotRotate;
    protected final String mSlotScreenRecord;
    protected final String mSlotSensorsOff;
    protected final String mSlotTty;
    protected final String mSlotVolume;
    protected final String mSlotZen;
    protected final TelecomManager mTelecomManager;
    protected final Executor mUiBgExecutor;
    protected final UserInfoController mUserInfoController;
    protected final UserManager mUserManager;
    private final SynchronousUserSwitchObserver mUserSwitchListener = new SynchronousUserSwitchObserver() {
        /* access modifiers changed from: private */
        /* renamed from: lambda$onUserSwitching$0 */
        public /* synthetic */ void lambda$onUserSwitching$0$PhoneStatusBarPolicy$1() {
            PhoneStatusBarPolicy.this.mUserInfoController.reloadUserInfo();
        }

        public void onUserSwitching(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                public final void run() {
                    PhoneStatusBarPolicy.AnonymousClass1.this.lambda$onUserSwitching$0$PhoneStatusBarPolicy$1();
                }
            });
        }

        public void onUserSwitchComplete(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.mHandler.post(new Runnable() {
                public final void run() {
                    PhoneStatusBarPolicy.AnonymousClass1.this.lambda$onUserSwitchComplete$1$PhoneStatusBarPolicy$1();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onUserSwitchComplete$1 */
        public /* synthetic */ void lambda$onUserSwitchComplete$1$PhoneStatusBarPolicy$1() {
            PhoneStatusBarPolicy.this.mCurrentUserId = ActivityManager.getCurrentUser();
            PhoneStatusBarPolicy.this.updateAlarm();
            PhoneStatusBarPolicy.this.updateManagedProfile();
        }

        public void onForegroundProfileSwitch(int i) throws RemoteException {
            PhoneStatusBarPolicy.this.profileChanged(i);
        }
    };
    protected final ZenModeController mZenController;
    protected boolean mZenVisible;

    /* access modifiers changed from: private */
    public void updateAlarm() {
    }

    /* access modifiers changed from: protected */
    public abstract void miuiInit();

    /* access modifiers changed from: protected */
    public abstract void profileChanged(int i);

    public abstract void updateBluetooth(String str);

    /* access modifiers changed from: protected */
    public abstract void updateHeadsetPlug(Intent intent);

    /* access modifiers changed from: protected */
    public abstract void updateManagedProfile();

    /* access modifiers changed from: protected */
    public abstract void updateVolumeZen();

    public PhoneStatusBarPolicy(StatusBarIconController statusBarIconController, CommandQueue commandQueue, BroadcastDispatcher broadcastDispatcher, Executor executor, Resources resources, CastController castController, HotspotController hotspotController, BluetoothController bluetoothController, NextAlarmController nextAlarmController, UserInfoController userInfoController, RotationLockController rotationLockController, DataSaverController dataSaverController, ZenModeController zenModeController, DeviceProvisionedController deviceProvisionedController, KeyguardStateController keyguardStateController, LocationController locationController, SensorPrivacyController sensorPrivacyController, IActivityManager iActivityManager, AlarmManager alarmManager, UserManager userManager, RecordingController recordingController, TelecomManager telecomManager, int i, SharedPreferences sharedPreferences, DateFormatUtil dateFormatUtil, RingerModeTracker ringerModeTracker) {
        this.mIconController = statusBarIconController;
        this.mCommandQueue = commandQueue;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mResources = resources;
        this.mCast = castController;
        this.mHotspot = hotspotController;
        this.mBluetooth = bluetoothController;
        this.mNextAlarmController = nextAlarmController;
        this.mUserInfoController = userInfoController;
        this.mIActivityManager = iActivityManager;
        this.mUserManager = userManager;
        this.mRotationLockController = rotationLockController;
        this.mDataSaver = dataSaverController;
        this.mZenController = zenModeController;
        this.mProvisionedController = deviceProvisionedController;
        this.mKeyguardStateController = keyguardStateController;
        this.mLocationController = locationController;
        this.mSensorPrivacyController = sensorPrivacyController;
        this.mRecordingController = recordingController;
        this.mUiBgExecutor = executor;
        this.mTelecomManager = telecomManager;
        this.mSlotCast = resources.getString(17041387);
        this.mSlotHotspot = resources.getString(17041394);
        this.mSlotBluetooth = resources.getString(17041385);
        this.mSlotTty = resources.getString(17041411);
        this.mSlotZen = resources.getString(17041415);
        this.mSlotVolume = resources.getString(17041412);
        this.mSlotAlarmClock = resources.getString(17041383);
        this.mSlotManagedProfile = resources.getString(17041397);
        this.mSlotRotate = resources.getString(17041404);
        this.mSlotHeadset = resources.getString(17041393);
        this.mSlotDataSaver = resources.getString(17041391);
        this.mSlotLocation = resources.getString(17041396);
        this.mSlotSensorsOff = resources.getString(17041407);
        this.mSlotScreenRecord = resources.getString(17041405);
        this.mDisplayId = i;
    }

    public void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("android.telecom.action.CURRENT_TTY_MODE_CHANGED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mIntentReceiver, intentFilter, this.mHandler, UserHandle.ALL);
        try {
            this.mIActivityManager.registerUserSwitchObserver(this.mUserSwitchListener, "PhoneStatusBarPolicy");
        } catch (RemoteException unused) {
        }
        updateTTY();
        updateBluetooth((String) null);
        this.mIconController.setIcon(this.mSlotAlarmClock, C0013R$drawable.stat_sys_alarm, this.mResources.getString(C0021R$string.status_bar_alarm));
        this.mIconController.setIconVisibility(this.mSlotAlarmClock, false);
        this.mIconController.setIcon(this.mSlotZen, C0013R$drawable.stat_sys_dnd, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotZen, false);
        this.mIconController.setIcon(this.mSlotVolume, C0013R$drawable.stat_sys_ringer_vibrate, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotVolume, false);
        this.mIconController.setIcon(this.mSlotCast, C0013R$drawable.stat_sys_cast, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotCast, false);
        this.mIconController.setIcon(this.mSlotManagedProfile, C0013R$drawable.stat_sys_managed_profile_status, this.mResources.getString(C0021R$string.accessibility_managed_profile));
        this.mIconController.setIconVisibility(this.mSlotManagedProfile, this.mManagedProfileIconVisible);
        this.mIconController.setIcon(this.mSlotDataSaver, C0013R$drawable.stat_sys_data_saver, this.mResources.getString(C0021R$string.accessibility_data_saver_on));
        this.mIconController.setIconVisibility(this.mSlotDataSaver, false);
        this.mIconController.setIcon(this.mSlotLocation, 17303177, this.mResources.getString(C0021R$string.accessibility_location_active));
        this.mIconController.setIconVisibility(this.mSlotLocation, false);
        this.mIconController.setIcon(this.mSlotSensorsOff, C0013R$drawable.stat_sys_sensors_off, this.mResources.getString(C0021R$string.accessibility_sensors_off_active));
        this.mIconController.setIconVisibility(this.mSlotSensorsOff, this.mSensorPrivacyController.isSensorPrivacyEnabled());
        this.mIconController.setIcon(this.mSlotScreenRecord, C0013R$drawable.stat_sys_screen_record, (CharSequence) null);
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
        miuiInit();
        this.mBluetooth.addCallback(this);
        this.mProvisionedController.addCallback(this);
        this.mZenController.addCallback(this);
        this.mHotspot.addCallback(this.mHotspotCallback);
        this.mNextAlarmController.addCallback(this.mNextAlarmCallback);
        this.mDataSaver.addCallback(this);
        this.mKeyguardStateController.addCallback(this);
        this.mLocationController.addCallback(this);
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    public void onConfigChanged(ZenModeConfig zenModeConfig) {
        updateVolumeZen();
    }

    public void onBluetoothDevicesChanged() {
        updateBluetooth((String) null);
    }

    public void onBluetoothStateChange(boolean z) {
        updateBluetooth((String) null);
    }

    private final void updateTTY() {
        TelecomManager telecomManager = this.mTelecomManager;
        if (telecomManager == null) {
            updateTTY(0);
        } else {
            updateTTY(telecomManager.getCurrentTtyMode());
        }
    }

    /* access modifiers changed from: private */
    public final void updateTTY(int i) {
        boolean z = i != 0;
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: enabled: " + z);
        }
        if (z) {
            if (DEBUG) {
                Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY on");
            }
            this.mIconController.setIcon(this.mSlotTty, C0013R$drawable.stat_sys_tty_mode, this.mResources.getString(C0021R$string.accessibility_tty_enabled));
            this.mIconController.setIconVisibility(this.mSlotTty, true);
            return;
        }
        if (DEBUG) {
            Log.v("PhoneStatusBarPolicy", "updateTTY: set TTY off");
        }
        this.mIconController.setIconVisibility(this.mSlotTty, false);
    }

    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        if (this.mDisplayId == i) {
            updateManagedProfile();
        }
    }

    public void onKeyguardShowingChanged() {
        updateManagedProfile();
    }

    public void onUserSetupChanged() {
        DeviceProvisionedController deviceProvisionedController = this.mProvisionedController;
        boolean isUserSetup = deviceProvisionedController.isUserSetup(deviceProvisionedController.getCurrentUser());
        if (this.mCurrentUserSetup != isUserSetup) {
            this.mCurrentUserSetup = isUserSetup;
            updateAlarm();
        }
    }

    public void onRotationLockStateChanged(boolean z, boolean z2) {
        boolean isCurrentOrientationLockPortrait = RotationLockTile.isCurrentOrientationLockPortrait(this.mRotationLockController, this.mResources);
        if (z) {
            if (isCurrentOrientationLockPortrait) {
                this.mIconController.setIcon(this.mSlotRotate, C0013R$drawable.stat_sys_rotate_portrait, this.mResources.getString(C0021R$string.accessibility_rotation_lock_on_portrait));
            } else {
                this.mIconController.setIcon(this.mSlotRotate, C0013R$drawable.stat_sys_rotate_landscape, this.mResources.getString(C0021R$string.accessibility_rotation_lock_on_landscape));
            }
            this.mIconController.setIconVisibility(this.mSlotRotate, true);
            return;
        }
        this.mIconController.setIconVisibility(this.mSlotRotate, false);
    }

    public void onDataSaverChanged(boolean z) {
        this.mIconController.setIconVisibility(this.mSlotDataSaver, z);
    }

    public void onLocationActiveChanged(boolean z) {
        updateLocation();
    }

    private void updateLocation() {
        if (this.mLocationController.isLocationActive()) {
            this.mIconController.setIconVisibility(this.mSlotLocation, true);
        } else {
            this.mIconController.setIconVisibility(this.mSlotLocation, false);
        }
    }

    public void onCountdown(long j) {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: countdown " + j);
        }
        int floorDiv = (int) Math.floorDiv(j + 500, 1000);
        int i = C0013R$drawable.stat_sys_screen_record;
        String num = Integer.toString(floorDiv);
        if (floorDiv == 1) {
            i = C0013R$drawable.stat_sys_screen_record_1;
        } else if (floorDiv == 2) {
            i = C0013R$drawable.stat_sys_screen_record_2;
        } else if (floorDiv == 3) {
            i = C0013R$drawable.stat_sys_screen_record_3;
        }
        this.mIconController.setIcon(this.mSlotScreenRecord, i, num);
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, true);
        this.mIconController.setIconAccessibilityLiveRegion(this.mSlotScreenRecord, 2);
    }

    public void onCountdownEnd() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: hiding icon during countdown");
        }
        this.mHandler.post(new Runnable() {
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onCountdownEnd$2$PhoneStatusBarPolicy();
            }
        });
        this.mHandler.post(new Runnable() {
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onCountdownEnd$3$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCountdownEnd$2 */
    public /* synthetic */ void lambda$onCountdownEnd$2$PhoneStatusBarPolicy() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCountdownEnd$3 */
    public /* synthetic */ void lambda$onCountdownEnd$3$PhoneStatusBarPolicy() {
        this.mIconController.setIconAccessibilityLiveRegion(this.mSlotScreenRecord, 0);
    }

    public void onRecordingStart() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: showing icon");
        }
        this.mIconController.setIcon(this.mSlotScreenRecord, C0013R$drawable.stat_sys_screen_record, this.mResources.getString(C0021R$string.screenrecord_ongoing_screen_only));
        this.mHandler.post(new Runnable() {
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onRecordingStart$4$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onRecordingStart$4 */
    public /* synthetic */ void lambda$onRecordingStart$4$PhoneStatusBarPolicy() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, true);
    }

    public void onRecordingEnd() {
        if (DEBUG) {
            Log.d("PhoneStatusBarPolicy", "screenrecord: hiding icon");
        }
        this.mHandler.post(new Runnable() {
            public final void run() {
                PhoneStatusBarPolicy.this.lambda$onRecordingEnd$5$PhoneStatusBarPolicy();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onRecordingEnd$5 */
    public /* synthetic */ void lambda$onRecordingEnd$5$PhoneStatusBarPolicy() {
        this.mIconController.setIconVisibility(this.mSlotScreenRecord, false);
    }
}
