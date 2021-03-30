package com.android.keyguard.injector;

import android.content.Context;
import android.content.Intent;
import android.hardware.biometrics.BiometricManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.keyguard.IPhoneSignalController;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiBleUnlockHelper;
import com.android.keyguard.MiuiDozeServiceHost;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyguardUpdateMonitorInjector.kt */
public final class KeyguardUpdateMonitorInjector implements SuperSaveModeController.SuperSaveModeChangeListener {
    private MiuiBleUnlockHelper.BLEUnlockState mBLEUnlockState;
    private final BiometricManager mBiometricManager;
    private boolean mChargeAnimationShowing;
    @NotNull
    private final Context mContext;
    private boolean mDisableFingerprintListenState;
    private int mFaceUnlockMode;
    private int mFingerprintMode;
    private boolean mKeyguardOccluded;
    private boolean mKeyguardShowing;
    private boolean mKeyguardShowingAndOccluded;
    private boolean mSimLocked;
    private final SuperSaveModeController mSuperSaveModeController;
    private String mUnlockWay;
    private String mWakeupReason;

    public KeyguardUpdateMonitorInjector(@NotNull Context context, @NotNull SuperSaveModeController superSaveModeController) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(superSaveModeController, "mSuperSaveModeController");
        this.mContext = context;
        this.mSuperSaveModeController = superSaveModeController;
        if (superSaveModeController != null) {
            superSaveModeController.addCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
            this.mBiometricManager = (BiometricManager) this.mContext.getSystemService(BiometricManager.class);
            this.mWakeupReason = "none";
            this.mUnlockWay = "none";
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    public final boolean isOwnerUser() {
        return KeyguardUpdateMonitor.getCurrentUser() == 0;
    }

    @Override // com.android.systemui.controlcenter.policy.SuperSaveModeController.SuperSaveModeChangeListener
    public void onSuperSaveModeChange(boolean z) {
        forEachCallback(new KeyguardUpdateMonitorInjector$onSuperSaveModeChange$1(z));
    }

    private final void forEachCallback(Function1<? super MiuiKeyguardUpdateMonitorCallback, Unit> function1) {
        Object obj = Dependency.get(KeyguardUpdateMonitor.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(KeyguardUpdateMonitor::class.java)");
        ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> callbacks = ((KeyguardUpdateMonitor) obj).getCallbacks();
        Intrinsics.checkExpressionValueIsNotNull(callbacks, "Dependency.get(KeyguardU…or::class.java).callbacks");
        ArrayList arrayList = new ArrayList();
        Iterator<T> it = callbacks.iterator();
        while (it.hasNext()) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = (KeyguardUpdateMonitorCallback) it.next().get();
            if (keyguardUpdateMonitorCallback != null) {
                arrayList.add(keyguardUpdateMonitorCallback);
            }
        }
        ArrayList<MiuiKeyguardUpdateMonitorCallback> arrayList2 = new ArrayList();
        for (Object obj2 : arrayList) {
            if (obj2 instanceof MiuiKeyguardUpdateMonitorCallback) {
                arrayList2.add(obj2);
            }
        }
        for (MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback : arrayList2) {
            function1.invoke(miuiKeyguardUpdateMonitorCallback);
        }
    }

    public final void onMagazineResourceInited() {
        forEachCallback(KeyguardUpdateMonitorInjector$onMagazineResourceInited$1.INSTANCE);
    }

    public final void handleLockScreenMagazinePreViewVisibilityChanged(boolean z) {
        forEachCallback(new KeyguardUpdateMonitorInjector$handleLockScreenMagazinePreViewVisibilityChanged$1(z));
    }

    private final void handleKeyguardOccludedChanged(boolean z) {
        forEachCallback(new KeyguardUpdateMonitorInjector$handleKeyguardOccludedChanged$1(z));
    }

    public final void handleKeyguardShowingChanged(boolean z) {
        forEachCallback(new KeyguardUpdateMonitorInjector$handleKeyguardShowingChanged$1(z));
    }

    public final void handleLockWallpaperProviderChanged() {
        forEachCallback(KeyguardUpdateMonitorInjector$handleLockWallpaperProviderChanged$1.INSTANCE);
    }

    public final void handleChargeAnimationShowingChanged(boolean z, boolean z2) {
        if (this.mChargeAnimationShowing != z) {
            this.mChargeAnimationShowing = z;
            forEachCallback(new KeyguardUpdateMonitorInjector$handleChargeAnimationShowingChanged$1(z, z2));
        }
    }

    public final boolean isChargeAnimationShowing() {
        return this.mChargeAnimationShowing;
    }

    public final boolean isKeyguardShowing() {
        return this.mKeyguardShowing;
    }

    public final boolean isKeyguardOccluded() {
        return this.mKeyguardOccluded;
    }

    public final void setKeyguardShowingAndOccluded(@NotNull TaskStackChangeListener taskStackChangeListener, boolean z, boolean z2, @NotNull MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback) {
        Intrinsics.checkParameterIsNotNull(taskStackChangeListener, "mTaskStackListener");
        Intrinsics.checkParameterIsNotNull(miuiKeyguardUpdateMonitorCallback, "keyguardUpdateMonitorCallback");
        this.mKeyguardShowingAndOccluded = z && z2;
        if (z2 != this.mKeyguardOccluded) {
            this.mKeyguardOccluded = z2;
            handleKeyguardOccludedChanged(z2);
            notifyAodOccludChanged(z2);
        }
        if (this.mKeyguardShowing != z) {
            this.mKeyguardShowing = z;
            handleKeyguardShowingChanged(z);
        }
        if (this.mKeyguardShowingAndOccluded) {
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(taskStackChangeListener);
            ActivityManagerWrapper.getInstance().registerTaskStackListener(taskStackChangeListener);
            return;
        }
        ActivityManagerWrapper.getInstance().unregisterTaskStackListener(taskStackChangeListener);
    }

    private final void notifyAodOccludChanged(boolean z) {
        if (((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isWallpaperSupportsAmbientMode()) {
            ((MiuiDozeServiceHost) Dependency.get(MiuiDozeServiceHost.class)).sendCommand("keyguard_occluded", z ? 1 : 0, null);
        }
    }

    public final void setFingerprintMode(int i) {
        if (this.mFingerprintMode != i) {
            this.mFingerprintMode = i;
        }
    }

    public final boolean isFingerprintUnlock() {
        int i = this.mFingerprintMode;
        return i == 7 || i == 1 || i == 2 || i == 8;
    }

    public final void setFaceUnlockMode(int i) {
        this.mFaceUnlockMode = i;
    }

    public final boolean isFaceUnlock() {
        int i = this.mFaceUnlockMode;
        return i == 7 || i == 8;
    }

    public final void sendUpdates(@NotNull KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback, boolean z) {
        Intrinsics.checkParameterIsNotNull(keyguardUpdateMonitorCallback, "callback");
        if (keyguardUpdateMonitorCallback instanceof MiuiKeyguardUpdateMonitorCallback) {
            MiuiKeyguardUpdateMonitorCallback miuiKeyguardUpdateMonitorCallback = (MiuiKeyguardUpdateMonitorCallback) keyguardUpdateMonitorCallback;
            miuiKeyguardUpdateMonitorCallback.onKeyguardOccludedChanged(z);
            miuiKeyguardUpdateMonitorCallback.onKeyguardShowingChanged(this.mKeyguardShowing);
            miuiKeyguardUpdateMonitorCallback.onLockWallpaperProviderChanged();
            if (MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST) {
                Object obj = Dependency.get(IPhoneSignalController.class);
                Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(IPhoneSignalController::class.java)");
                miuiKeyguardUpdateMonitorCallback.onPhoneSignalChanged(((IPhoneSignalController) obj).isSignalAvailable());
            }
        }
    }

    public final void onRegionChanged(@NotNull ArrayList<WeakReference<KeyguardUpdateMonitorCallback>> arrayList) {
        Intrinsics.checkParameterIsNotNull(arrayList, "mCallbacks");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            KeyguardUpdateMonitorCallback keyguardUpdateMonitorCallback = arrayList.get(i).get();
            if (keyguardUpdateMonitorCallback != null && (keyguardUpdateMonitorCallback instanceof MiuiKeyguardUpdateMonitorCallback)) {
                ((MiuiKeyguardUpdateMonitorCallback) keyguardUpdateMonitorCallback).onRegionChanged();
            }
        }
    }

    public final void setBLEUnlockState(@NotNull MiuiBleUnlockHelper.BLEUnlockState bLEUnlockState) {
        Intrinsics.checkParameterIsNotNull(bLEUnlockState, "state");
        this.mBLEUnlockState = bLEUnlockState;
    }

    public final boolean isBleUnlockSuccess() {
        return this.mBLEUnlockState == MiuiBleUnlockHelper.BLEUnlockState.SUCCEED;
    }

    public final void reportSuccessfulStrongAuthUnlockAttempt() {
        BiometricManager biometricManager = this.mBiometricManager;
        if (biometricManager != null) {
            biometricManager.resetLockout(null);
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().reportSuccessfulStrongAuthUnlockAttempt();
        }
    }

    public final boolean shouldListenForFingerprintWhenUnlocked() {
        Object obj = Dependency.get(MiuiFaceUnlockManager.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(MiuiFaceUnlockManager::class.java)");
        return ((MiuiFaceUnlockManager) obj).isFaceUnlockSuccessAndStayScreen() || ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isBleUnlockSuccess();
    }

    public final void notifyDrawnWhenScreenOn(@NotNull IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        Intrinsics.checkParameterIsNotNull(iKeyguardDrawnCallback, "callback");
        Trace.beginSection("KeyguardViewMediator#notifyDrawn");
        screenTurnedOnCallback(iKeyguardDrawnCallback);
        Trace.endSection();
    }

    private final void screenTurnedOnCallback(IKeyguardDrawnCallback iKeyguardDrawnCallback) {
        long screenOnDelyTime = ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).getScreenOnDelyTime();
        Parcel obtain = Parcel.obtain();
        Parcel obtain2 = Parcel.obtain();
        try {
            obtain.writeInterfaceToken("com.android.internal.policy.IKeyguardDrawnCallback");
            obtain.writeLong(screenOnDelyTime);
            iKeyguardDrawnCallback.asBinder().transact(255, obtain, obtain2, 1);
            obtain2.readException();
        } catch (RemoteException e) {
            Log.e("MiuiKeyguardUtils", "something wrong when delayed turn on screen");
            e.printStackTrace();
        } catch (Throwable th) {
            obtain.recycle();
            obtain2.recycle();
            throw th;
        }
        obtain.recycle();
        obtain2.recycle();
    }

    public final void sendShowUnlockScreenBroadcast() {
        if (isKeyguardOccluded()) {
            this.mContext.sendBroadcastAsUser(new Intent("xiaomi.intent.action.SECURE_KEYGUARD_SHOWN"), UserHandle.CURRENT);
        }
    }

    public final void handlePreBiometricAuthenticated(int i) {
        forEachCallback(new KeyguardUpdateMonitorInjector$handlePreBiometricAuthenticated$1(i));
    }

    public final boolean isSimLocked() {
        return this.mSimLocked;
    }

    private final void handleSimLockedStateChange(boolean z) {
        if (this.mSimLocked != z) {
            this.mSimLocked = z;
            if (z) {
                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).cancelFaceAuth();
                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).updateFingerprintListeningState();
            }
            forEachCallback(new KeyguardUpdateMonitorInjector$handleSimLockedStateChange$1(z));
        }
    }

    private final boolean isSupportShowSimLockedTips() {
        return this.mContext.getResources().getBoolean(C0010R$bool.config_switch_sim_locked_tips);
    }

    public final void handleSimLocked(int i, int i2, int i3) {
        if (isSupportShowSimLockedTips()) {
            Log.d("KeyguardUpdateMonitorInjector", "handleSimStateChange(show sim locked tips)");
            if (i3 == 1 || i3 == 5) {
                handleSimLockedStateChange(false);
            } else if (i3 == 7) {
                handleSimLockedStateChange(true);
            }
        }
    }

    public final void handleStartedWakingUpWithReason(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "reason");
        this.mWakeupReason = str;
        forEachCallback(new KeyguardUpdateMonitorInjector$handleStartedWakingUpWithReason$1(str));
    }

    public final void handleFingerprintLockoutReset() {
        forEachCallback(KeyguardUpdateMonitorInjector$handleFingerprintLockoutReset$1.INSTANCE);
    }

    public final void sendScreenOnBroadcast2SuperWallpaper() {
        if (((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).isSuperWallpaper()) {
            Log.d("KeyguardViewMediator", "is_super_wallpaper==true wakeupReason:" + this.mWakeupReason + " UnlockWay:" + this.mUnlockWay);
            Intent intent = new Intent("com.android.systemui.SCREEN_ON");
            intent.putExtra("wakeupReason", this.mWakeupReason);
            intent.putExtra("wakeupWay", this.mUnlockWay);
            this.mContext.sendBroadcast(intent);
        }
    }

    public final void setKeyguardUnlockWay(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "way");
        if (z) {
            Slog.w("miui_keyguard", "unlock keyguard by " + str);
        }
        this.mUnlockWay = str;
    }

    public final boolean getDisableFingerprintListenState() {
        return this.mDisableFingerprintListenState;
    }
}
