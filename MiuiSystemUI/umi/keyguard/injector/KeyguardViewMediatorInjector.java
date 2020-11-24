package com.android.keyguard.injector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.policy.IKeyguardStateCallback;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardViewMediatorInjector.kt */
public final class KeyguardViewMediatorInjector {
    @NotNull
    private final BroadcastDispatcher mBroadcastDispatcher;
    @NotNull
    private final Context mContext;
    private final BroadcastReceiver mShowPasswordScreenReceiver = new KeyguardViewMediatorInjector$mShowPasswordScreenReceiver$1(this);
    @Nullable
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;

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
        BroadcastDispatcher.registerReceiver$default(broadcastDispatcher, broadcastReceiver, intentFilter, (Executor) null, userHandle, 4, (Object) null);
    }

    public final void sendKeyguardScreenOnBroadcast() {
        this.mContext.sendBroadcast(new Intent("com.android.systemui.SCREEN_ON"));
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
}
