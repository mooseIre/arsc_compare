package com.android.keyguard;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.miui.Shell;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.DozeHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeScrimController;
import com.android.systemui.statusbar.phone.DozeServiceHost;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.LockscreenLockIconController;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowViewController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.miui.aod.IMiuiAodCallback;
import com.miui.aod.IMiuiAodService;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import dagger.Lazy;

public class MiuiDozeServiceHost extends DozeServiceHost {
    private final AodCallback mAodCallback = new AodCallback();
    private boolean mAodEnable;
    private IMiuiAodService mAodService;
    private boolean mAodServiceBinded = false;
    private boolean mAodUsingSuperWallpaperStyle;
    private final Context mContext = SystemUIApplication.getContext();
    private DeviceProvisionedController mDeviceProvisionedController;
    Runnable mDozingChanged = new Runnable() {
        /* class com.android.keyguard.$$Lambda$pTlDRfd3UGChuOoJMce0_IvYrU */

        public final void run() {
            MiuiDozeServiceHost.this.updateDozing();
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    Runnable mNotifyKeycodeGoto = $$Lambda$MiuiDozeServiceHost$dJI1FLLD1uCJOZIbgk7HpTtrZxI.INSTANCE;
    private final PowerManager mPowerManager;
    private final boolean mSupportAod;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        /* class com.android.keyguard.MiuiDozeServiceHost.AnonymousClass4 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MiuiDozeServiceHost.this.mAodService = IMiuiAodService.Stub.asInterface(iBinder);
            if (MiuiDozeServiceHost.this.mAodService != null) {
                try {
                    MiuiDozeServiceHost.this.mAodService.registerCallback(MiuiDozeServiceHost.this.mAodCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Log.i("MiuiDozeServiceHost", "连接Service 成功");
        }

        public void onServiceDisconnected(ComponentName componentName) {
            MiuiDozeServiceHost.this.disconnectAodService();
            Log.e("MiuiDozeServiceHost", "连接Service 失败");
            MiuiDozeServiceHost.this.startAndBindAodService();
        }
    };

    @Override // com.android.systemui.doze.DozeHost, com.android.systemui.statusbar.phone.DozeServiceHost
    public void startDozing() {
    }

    public MiuiDozeServiceHost(DozeLog dozeLog, PowerManager powerManager, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, DeviceProvisionedController deviceProvisionedController, HeadsUpManagerPhone headsUpManagerPhone, BatteryController batteryController, ScrimController scrimController, Lazy<BiometricUnlockController> lazy, KeyguardViewMediator keyguardViewMediator, Lazy<AssistManager> lazy2, DozeScrimController dozeScrimController, final KeyguardUpdateMonitor keyguardUpdateMonitor, VisualStabilityManager visualStabilityManager, PulseExpansionHandler pulseExpansionHandler, NotificationShadeWindowController notificationShadeWindowController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, LockscreenLockIconController lockscreenLockIconController, final SettingsManager settingsManager) {
        super(dozeLog, powerManager, wakefulnessLifecycle, sysuiStatusBarStateController, deviceProvisionedController, headsUpManagerPhone, batteryController, scrimController, lazy, keyguardViewMediator, lazy2, dozeScrimController, keyguardUpdateMonitor, visualStabilityManager, pulseExpansionHandler, notificationShadeWindowController, notificationWakeUpCoordinator, lockscreenLockIconController);
        boolean supportAod = supportAod(this.mContext);
        this.mSupportAod = supportAod;
        this.mPowerManager = powerManager;
        this.mDeviceProvisionedController = deviceProvisionedController;
        if (supportAod) {
            AnonymousClass1 r3 = new KeyguardUpdateMonitorCallback() {
                /* class com.android.keyguard.MiuiDozeServiceHost.AnonymousClass1 */

                @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
                public void onSimStateChanged(int i, int i2, int i3) {
                    super.onSimStateChanged(i, i2, i3);
                    MiuiDozeServiceHost.this.onSimPinSecureChanged(keyguardUpdateMonitor.isSimPinSecure());
                }

                @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
                public void onUserSwitchComplete(int i) {
                    super.onUserSwitchComplete(i);
                    MiuiDozeServiceHost.this.disconnectAodService();
                    MiuiDozeServiceHost.this.startAndBindAodService();
                }
            };
            this.mKeyguardUpdateMonitorCallback = r3;
            keyguardUpdateMonitor.registerCallback(r3);
            ((SettingsObserver) Dependency.get(SettingsObserver.class)).addCallback(new SettingsObserver.Callback() {
                /* class com.android.keyguard.$$Lambda$MiuiDozeServiceHost$_QlERT10AMyGNFXe8IGYFygq5o */

                @Override // com.miui.systemui.SettingsObserver.Callback
                public final void onContentChanged(String str, String str2) {
                    MiuiDozeServiceHost.this.lambda$new$0$MiuiDozeServiceHost(str, str2);
                }
            }, 1, MiuiKeyguardUtils.AOD_MODE, "aod_using_super_wallpaper");
            this.mDeviceProvisionedController.addCallback(new DeviceProvisionedController.DeviceProvisionedListener() {
                /* class com.android.keyguard.MiuiDozeServiceHost.AnonymousClass2 */

                @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
                public void onDeviceProvisionedChanged() {
                    ContentResolver contentResolver = MiuiDozeServiceHost.this.mContext.getContentResolver();
                    boolean z = false;
                    if (Settings.Global.getInt(contentResolver, "device_provisioned", 0) != 0) {
                        z = true;
                    }
                    if (z) {
                        Settings.Global.putInt(contentResolver, "new_device_after_support_notification_animation", 1);
                        settingsManager.refreshWakeupForNotificationValue();
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiDozeServiceHost(String str, String str2) {
        if (MiuiKeyguardUtils.AOD_MODE.equals(str)) {
            this.mAodEnable = MiuiTextUtils.parseBoolean(str2, false);
        } else if ("aod_using_super_wallpaper".equals(str)) {
            this.mAodUsingSuperWallpaperStyle = MiuiTextUtils.parseBoolean(str2, false);
        }
        updateDozeAfterScreenOff();
    }

    @Override // com.android.systemui.statusbar.phone.DozeServiceHost
    public void initialize(StatusBar statusBar, NotificationIconAreaController notificationIconAreaController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, NotificationShadeWindowViewController notificationShadeWindowViewController, NotificationPanelViewController notificationPanelViewController, View view) {
        super.initialize(statusBar, notificationIconAreaController, statusBarKeyguardViewManager, notificationShadeWindowViewController, notificationPanelViewController, view);
        checkAodService();
        addCallback(new DozeHost.Callback() {
            /* class com.android.keyguard.MiuiDozeServiceHost.AnonymousClass3 */

            @Override // com.android.systemui.doze.DozeHost.Callback
            public void onDozeSuppressedChanged(boolean z) {
                MiuiDozeServiceHost.this.sendCommand("suppressAmbientDisplay", z ? 1 : 0, null);
            }
        });
    }

    @Override // com.android.systemui.doze.DozeHost, com.android.systemui.statusbar.phone.DozeServiceHost
    public void stopDozing() {
        super.stopDozing();
        checkAodService();
        IMiuiAodService iMiuiAodService = this.mAodService;
        if (iMiuiAodService != null) {
            try {
                iMiuiAodService.stopDozing();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void onGxzwIconChanged(boolean z) {
        Shell.setRuntimeSharedValue("KEYGUARD_GXZW_ICON_SHOWN", z ? 0 : 1);
        if (this.mSupportAod) {
            checkAodService();
            IMiuiAodService iMiuiAodService = this.mAodService;
            if (iMiuiAodService != null) {
                try {
                    iMiuiAodService.onGxzwIconChanged(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fireAodState(boolean z) {
        if (this.mSupportAod) {
            checkAodService();
            IMiuiAodService iMiuiAodService = this.mAodService;
            if (iMiuiAodService != null) {
                try {
                    iMiuiAodService.fireAodState(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void fireFingerprintPressed(boolean z) {
        if (this.mSupportAod) {
            checkAodService();
            IMiuiAodService iMiuiAodService = this.mAodService;
            if (iMiuiAodService != null) {
                try {
                    iMiuiAodService.fireFingerprintPressed(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onSimPinSecureChanged(boolean z) {
        if (this.mSupportAod) {
            checkAodService();
            IMiuiAodService iMiuiAodService = this.mAodService;
            if (iMiuiAodService != null) {
                try {
                    iMiuiAodService.onSimPinSecureChanged(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onKeyguardTransparent() {
        IMiuiAodService iMiuiAodService;
        if (this.mSupportAod && (iMiuiAodService = this.mAodService) != null) {
            try {
                iMiuiAodService.onKeyguardTransparent();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCommand(String str, int i, Bundle bundle) {
        if (this.mSupportAod) {
            checkAodService();
            IMiuiAodService iMiuiAodService = this.mAodService;
            if (iMiuiAodService != null) {
                try {
                    iMiuiAodService.sendCommand(str, i, bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkAodService() {
        if (this.mSupportAod && this.mAodService == null) {
            startAndBindAodService();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAndBindAodService() {
        if (this.mSupportAod) {
            this.mHandler.post(new Runnable() {
                /* class com.android.keyguard.$$Lambda$MiuiDozeServiceHost$A9RXwPWz12fOmNfsF5EE0_JYVk */

                public final void run() {
                    MiuiDozeServiceHost.this.lambda$startAndBindAodService$2$MiuiDozeServiceHost();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAndBindAodService$2 */
    public /* synthetic */ void lambda$startAndBindAodService$2$MiuiDozeServiceHost() {
        Intent intent = new Intent("com.miui.aod.MiuiAodService");
        intent.setPackage("com.miui.aod");
        this.mAodServiceBinded = this.mContext.bindServiceAsUser(intent, this.serviceConnection, 1, UserHandle.CURRENT);
        Log.d("MiuiDozeServiceHost", "is service connected: " + this.mAodServiceBinded);
        if (!this.mAodServiceBinded) {
            this.mHandler.postDelayed(new Runnable(intent) {
                /* class com.android.keyguard.$$Lambda$MiuiDozeServiceHost$nxh3_4ZcNn5fNtNVuf41CaSJCy0 */
                public final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiDozeServiceHost.this.lambda$startAndBindAodService$1$MiuiDozeServiceHost(this.f$1);
                }
            }, 500);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAndBindAodService$1 */
    public /* synthetic */ void lambda$startAndBindAodService$1$MiuiDozeServiceHost(Intent intent) {
        this.mAodServiceBinded = this.mContext.bindServiceAsUser(intent, this.serviceConnection, 1, UserHandle.CURRENT);
        Log.d("MiuiDozeServiceHost", "is service retry connected: " + this.mAodServiceBinded);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnectAodService() {
        IMiuiAodService iMiuiAodService = this.mAodService;
        if (iMiuiAodService != null) {
            try {
                iMiuiAodService.unregisterCallback();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.mAodService = null;
            this.mContext.unbindService(this.serviceConnection);
        }
        this.mAodCallback.onDozingRequested(false);
    }

    private void updateDozeAfterScreenOff() {
        boolean z = !this.mSupportAod || !this.mAodEnable || !this.mAodUsingSuperWallpaperStyle;
        Log.i("MiuiDozeServiceHost", "updateDozeAfterScreenOff: " + z);
        this.mPowerManager.setDozeAfterScreenOff(z);
    }

    /* access modifiers changed from: private */
    public class AodCallback extends IMiuiAodCallback.Stub {
        @Override // com.miui.aod.IMiuiAodCallback
        public void onDozeStateChanged(int i) {
        }

        @Override // com.miui.aod.IMiuiAodCallback
        public void onExtendPulse() {
        }

        @Override // com.miui.aod.IMiuiAodCallback
        public void setAnimateWakeup(boolean z) {
        }

        private AodCallback() {
        }

        @Override // com.miui.aod.IMiuiAodCallback
        public void onDozingRequested(boolean z) {
            Log.i("MiuiDozeServiceHost", "onDozingRequested: " + z);
            ((DozeServiceHost) MiuiDozeServiceHost.this).mDozingRequested = z;
            MiuiDozeServiceHost.this.mHandler.removeCallbacks(MiuiDozeServiceHost.this.mDozingChanged);
            MiuiDozeServiceHost.this.mHandler.postAtFrontOfQueue(MiuiDozeServiceHost.this.mDozingChanged);
        }

        @Override // com.miui.aod.IMiuiAodCallback
        public void notifyKeycodeGoto() {
            MiuiDozeServiceHost.this.mHandler.postAtFrontOfQueue(MiuiDozeServiceHost.this.mNotifyKeycodeGoto);
        }
    }

    static /* synthetic */ void lambda$new$3() {
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().notifyKeycodeGoto();
        }
    }

    public static boolean supportAod(Context context) {
        return context.getResources().getBoolean(17891423);
    }
}
