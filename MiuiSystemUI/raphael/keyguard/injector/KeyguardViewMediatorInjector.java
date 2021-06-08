package com.android.keyguard.injector;

import android.app.ActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import java.util.ArrayList;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardViewMediatorInjector.kt */
public final class KeyguardViewMediatorInjector {
    private final Intent FINGER_FAST_UNLOCK_INTENT = new Intent("com.android.finger.fast.unlock");
    private final int KEYGUARD_GOING_AWAY_FLAG_EXIT_FOR_APP = 8;
    private final int KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS = 2;
    @NotNull
    private final BroadcastDispatcher mBroadcastDispatcher;
    @NotNull
    private final Context mContext;
    private boolean mKeyguardExitFromFw;
    private final BroadcastReceiver mShowPasswordScreenReceiver = new KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1(this);
    @Nullable
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));

    public KeyguardViewMediatorInjector(@NotNull Context context, @NotNull BroadcastDispatcher broadcastDispatcher, @Nullable StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "mBroadcastDispatcher");
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    @Nullable
    public final StatusBarKeyguardViewManager getMStatusBarKeyguardViewManager() {
        return this.mStatusBarKeyguardViewManager;
    }

    public final void setup() {
        registerShowPasswordScreenBroadcast();
        ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).sendShowUnlockScreenBroadcast();
        ((KeyguardSensorInjector) Dependency.get(KeyguardSensorInjector.class)).setupSensors();
    }

    private final void registerShowPasswordScreenBroadcast() {
        IntentFilter intentFilter = new IntentFilter("xiaomi.intent.action.SHOW_SECURE_KEYGUARD");
        BroadcastDispatcher broadcastDispatcher = this.mBroadcastDispatcher;
        BroadcastReceiver broadcastReceiver = this.mShowPasswordScreenReceiver;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        BroadcastDispatcher.registerReceiver$default(broadcastDispatcher, broadcastReceiver, intentFilter, null, userHandle, 4, null);
    }

    public final void unblockScreenOn(@NotNull ArrayList<IKeyguardStateCallback> arrayList) {
        Intrinsics.checkParameterIsNotNull(arrayList, "keyguardStateCallbacks");
        Log.d("miui_face", "unblockScreenOn");
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            Parcel obtain = Parcel.obtain();
            Parcel obtain2 = Parcel.obtain();
            try {
                obtain.writeInterfaceToken("com.android.internal.policy.IKeyguardStateCallback");
                arrayList.get(size).asBinder().transact(255, obtain, obtain2, 1);
                obtain2.readException();
            } catch (RemoteException e) {
                Log.e("miui_face", "something wrong when unblock screen on");
                e.printStackTrace();
            } catch (Throwable th) {
                obtain.recycle();
                obtain2.recycle();
                throw th;
            }
            obtain.recycle();
            obtain2.recycle();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x001a, code lost:
        if (((com.android.keyguard.KeyguardUpdateMonitor) r0).isDeviceInteractive() == false) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x003e, code lost:
        if (((com.android.keyguard.KeyguardUpdateMonitor) r0).isDeviceInteractive() != false) goto L_0x0040;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void preHideKeyguard() {
        /*
            r3 = this;
            android.content.Context r0 = r3.mContext
            boolean r0 = com.android.keyguard.utils.MiuiKeyguardUtils.isTopActivityLauncher(r0)
            java.lang.String r1 = "Dependency.get(KeyguardUpdateMonitor::class.java)"
            r2 = 1
            if (r0 != 0) goto L_0x001c
            java.lang.Class<com.android.keyguard.KeyguardUpdateMonitor> r0 = com.android.keyguard.KeyguardUpdateMonitor.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            com.android.keyguard.KeyguardUpdateMonitor r0 = (com.android.keyguard.KeyguardUpdateMonitor) r0
            boolean r0 = r0.isDeviceInteractive()
            if (r0 != 0) goto L_0x0027
        L_0x001c:
            java.lang.Class<com.android.keyguard.wallpaper.MiuiWallpaperClient> r0 = com.android.keyguard.wallpaper.MiuiWallpaperClient.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            com.android.keyguard.wallpaper.MiuiWallpaperClient r0 = (com.android.keyguard.wallpaper.MiuiWallpaperClient) r0
            r0.onKeyguardGoingAway(r2, r2)
        L_0x0027:
            android.content.Context r0 = r3.mContext
            boolean r0 = com.android.keyguard.utils.MiuiKeyguardUtils.isTopActivityLauncher(r0)
            if (r0 != 0) goto L_0x0040
            java.lang.Class<com.android.keyguard.KeyguardUpdateMonitor> r0 = com.android.keyguard.KeyguardUpdateMonitor.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            com.android.keyguard.KeyguardUpdateMonitor r0 = (com.android.keyguard.KeyguardUpdateMonitor) r0
            boolean r0 = r0.isDeviceInteractive()
            if (r0 == 0) goto L_0x004b
        L_0x0040:
            java.lang.Class<com.android.keyguard.MiuiFastUnlockController> r0 = com.android.keyguard.MiuiFastUnlockController.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            com.android.keyguard.MiuiFastUnlockController r0 = (com.android.keyguard.MiuiFastUnlockController) r0
            r0.setWallpaperAsTarget(r2)
        L_0x004b:
            android.content.Context r0 = r3.mContext
            android.content.Intent r1 = r3.FINGER_FAST_UNLOCK_INTENT
            android.os.UserHandle r2 = android.os.UserHandle.CURRENT
            r0.sendBroadcastAsUser(r1, r2)
            r3.keyguardGoingAway()
            java.lang.Class<com.android.systemui.statusbar.phone.StatusBar> r3 = com.android.systemui.statusbar.phone.StatusBar.class
            java.lang.Object r3 = com.android.systemui.Dependency.get(r3)
            com.android.systemui.statusbar.phone.StatusBar r3 = (com.android.systemui.statusbar.phone.StatusBar) r3
            r3.setKeyguardTransparent()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.injector.KeyguardViewMediatorInjector.preHideKeyguard():void");
    }

    public final void keyguardGoingAway() {
        Object obj = Dependency.get(MiuiFastUnlockController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(MiuiFastU…ckController::class.java)");
        if (((MiuiFastUnlockController) obj).isFastUnlock()) {
            doKeyguardGoingAway();
        } else {
            this.mUiOffloadThread.submit(new KeyguardViewMediatorInjector$keyguardGoingAway$1(this), 2);
        }
    }

    /* access modifiers changed from: private */
    public final void doKeyguardGoingAway() {
        int i = MiuiKeyguardUtils.isTopActivityLauncher(this.mContext) ? this.KEYGUARD_GOING_AWAY_FLAG_NO_WINDOW_ANIMATIONS : this.KEYGUARD_GOING_AWAY_FLAG_EXIT_FOR_APP;
        try {
            this.mKeyguardExitFromFw = false;
            ActivityTaskManager.getService().keyguardGoingAway(i);
            Slog.i("KeyguardViewMediator", "call fw keyguardGoingAway: flags = " + i);
        } catch (RemoteException e) {
            Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
        }
    }

    public final void setKeyguardExitFromFw() {
        this.mKeyguardExitFromFw = true;
    }

    public final boolean getKeyguardExitFromFw() {
        return this.mKeyguardExitFromFw;
    }
}
