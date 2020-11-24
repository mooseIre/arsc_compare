package com.android.keyguard;

import android.content.ComponentName;
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
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import dagger.Lazy;

public class MiuiDozeServiceHost extends DozeServiceHost {
    /* access modifiers changed from: private */
    public final AodCallback mAodCallback = new AodCallback();
    private boolean mAodEnable;
    /* access modifiers changed from: private */
    public IMiuiAodService mAodService;
    private boolean mAodServiceBinded = false;
    private boolean mAodUsingSuperWallpaperStyle;
    private final Context mContext = SystemUIApplication.getContext();
    Runnable mDozingChanged = new Runnable() {
        public final void run() {
            MiuiDozeServiceHost.this.updateDozing();
        }
    };
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    Runnable mNotifyKeycodeGoto = $$Lambda$MiuiDozeServiceHost$lo2VTvaOLCM7yAae_Klyzmiik.INSTANCE;
    private final PowerManager mPowerManager;
    private final boolean mSupportAod;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IMiuiAodService unused = MiuiDozeServiceHost.this.mAodService = IMiuiAodService.Stub.asInterface(iBinder);
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

    public void startDozing() {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiDozeServiceHost(DozeLog dozeLog, PowerManager powerManager, WakefulnessLifecycle wakefulnessLifecycle, SysuiStatusBarStateController sysuiStatusBarStateController, DeviceProvisionedController deviceProvisionedController, HeadsUpManagerPhone headsUpManagerPhone, BatteryController batteryController, ScrimController scrimController, Lazy<BiometricUnlockController> lazy, KeyguardViewMediator keyguardViewMediator, Lazy<AssistManager> lazy2, DozeScrimController dozeScrimController, KeyguardUpdateMonitor keyguardUpdateMonitor, VisualStabilityManager visualStabilityManager, PulseExpansionHandler pulseExpansionHandler, NotificationShadeWindowController notificationShadeWindowController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, LockscreenLockIconController lockscreenLockIconController) {
        super(dozeLog, powerManager, wakefulnessLifecycle, sysuiStatusBarStateController, deviceProvisionedController, headsUpManagerPhone, batteryController, scrimController, lazy, keyguardViewMediator, lazy2, dozeScrimController, keyguardUpdateMonitor, visualStabilityManager, pulseExpansionHandler, notificationShadeWindowController, notificationWakeUpCoordinator, lockscreenLockIconController);
        final KeyguardUpdateMonitor keyguardUpdateMonitor2 = keyguardUpdateMonitor;
        boolean supportAod = supportAod(this.mContext);
        this.mSupportAod = supportAod;
        this.mPowerManager = powerManager;
        if (supportAod) {
            AnonymousClass1 r3 = new KeyguardUpdateMonitorCallback() {
                public void onSimStateChanged(int i, int i2, int i3) {
                    super.onSimStateChanged(i, i2, i3);
                    MiuiDozeServiceHost.this.onSimPinSecureChanged(keyguardUpdateMonitor2.isSimPinSecure());
                }

                public void onUserSwitchComplete(int i) {
                    super.onUserSwitchComplete(i);
                    MiuiDozeServiceHost.this.disconnectAodService();
                    MiuiDozeServiceHost.this.startAndBindAodService();
                }
            };
            this.mKeyguardUpdateMonitorCallback = r3;
            keyguardUpdateMonitor2.registerCallback(r3);
            ((SettingsObserver) Dependency.get(SettingsObserver.class)).addCallback(new SettingsObserver.Callback() {
                public final void onContentChanged(String str, String str2) {
                    MiuiDozeServiceHost.this.lambda$new$0$MiuiDozeServiceHost(str, str2);
                }
            }, 1, MiuiKeyguardUtils.AOD_MODE, "aod_using_super_wallpaper");
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

    public void initialize(StatusBar statusBar, NotificationIconAreaController notificationIconAreaController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, NotificationShadeWindowViewController notificationShadeWindowViewController, NotificationPanelViewController notificationPanelViewController, View view) {
        super.initialize(statusBar, notificationIconAreaController, statusBarKeyguardViewManager, notificationShadeWindowViewController, notificationPanelViewController, view);
        checkAodService();
        addCallback(new DozeHost.Callback() {
            public void onDozeSuppressedChanged(boolean z) {
                MiuiDozeServiceHost.this.sendCommand("suppressAmbientDisplay", z ? 1 : 0, (Bundle) null);
            }
        });
    }

    public void stopDozing() {
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
    public void startAndBindAodService() {
        if (this.mSupportAod) {
            this.mHandler.post(new Runnable() {
                public final void run() {
                    MiuiDozeServiceHost.this.lambda$startAndBindAodService$1$MiuiDozeServiceHost();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAndBindAodService$1 */
    public /* synthetic */ void lambda$startAndBindAodService$1$MiuiDozeServiceHost() {
        Intent intent = new Intent("com.miui.aod.MiuiAodService");
        intent.setPackage("com.miui.aod");
        this.mAodServiceBinded = this.mContext.bindServiceAsUser(intent, this.serviceConnection, 1, UserHandle.CURRENT);
        Log.d("MiuiDozeServiceHost", "is service connected: " + this.mAodServiceBinded);
    }

    /* access modifiers changed from: private */
    public void disconnectAodService() {
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

    private class AodCallback extends IMiuiAodCallback.Stub {
        public void onDozeStateChanged(int i) {
        }

        public void onExtendPulse() {
        }

        public void setAnimateWakeup(boolean z) {
        }

        private AodCallback() {
        }

        public void onDozingRequested(boolean z) {
            Log.i("MiuiDozeServiceHost", "onDozingRequested: " + z);
            boolean unused = MiuiDozeServiceHost.this.mDozingRequested = z;
            MiuiDozeServiceHost.this.mHandler.removeCallbacks(MiuiDozeServiceHost.this.mDozingChanged);
            MiuiDozeServiceHost.this.mHandler.postAtFrontOfQueue(MiuiDozeServiceHost.this.mDozingChanged);
        }

        public void notifyKeycodeGoto() {
            MiuiDozeServiceHost.this.mHandler.postAtFrontOfQueue(MiuiDozeServiceHost.this.mNotifyKeycodeGoto);
        }
    }

    static /* synthetic */ void lambda$new$2() {
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().notifyKeycodeGoto();
        }
    }

    public static boolean supportAod(Context context) {
        return context.getResources().getBoolean(17891423);
    }
}
