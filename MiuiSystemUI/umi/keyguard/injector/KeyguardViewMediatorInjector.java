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
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBar;
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

    public final void preHideKeyguard() {
        ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).setWallpaperAsTarget(true);
        if (MiuiKeyguardUtils.isTopActivityLauncher(this.mContext)) {
            ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).onKeyguardGoingAway(true, true);
        }
        this.mContext.sendBroadcastAsUser(this.FINGER_FAST_UNLOCK_INTENT, UserHandle.CURRENT);
        keyguardGoingAway();
        ((StatusBar) Dependency.get(StatusBar.class)).setKeyguardTransparent();
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
            ActivityTaskManager.getService().keyguardGoingAway(i);
            Slog.i("KeyguardViewMediator", "call fw keyguardGoingAway: flags = " + i);
        } catch (RemoteException e) {
            Log.e("KeyguardViewMediator", "Error while calling WindowManager", e);
        }
    }
}
