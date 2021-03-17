package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.hardware.biometrics.BiometricSourceType;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardBypassController.kt */
public class KeyguardBypassController implements Dumpable {
    private boolean bouncerShowing;
    private boolean bypassEnabled;
    private boolean hasFaceFeature;
    private boolean isPulseExpanding;
    private boolean launchingAffordance;
    private final KeyguardStateController mKeyguardStateController;
    private PendingUnlock pendingUnlock;
    private boolean qSExpanded;
    private final StatusBarStateController statusBarStateController;
    @NotNull
    public BiometricUnlockController unlockController;

    /* access modifiers changed from: private */
    /* compiled from: KeyguardBypassController.kt */
    public static final class PendingUnlock {
        private final boolean isStrongBiometric;
        @NotNull
        private final BiometricSourceType pendingUnlockType;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PendingUnlock)) {
                return false;
            }
            PendingUnlock pendingUnlock = (PendingUnlock) obj;
            return Intrinsics.areEqual(this.pendingUnlockType, pendingUnlock.pendingUnlockType) && this.isStrongBiometric == pendingUnlock.isStrongBiometric;
        }

        public int hashCode() {
            BiometricSourceType biometricSourceType = this.pendingUnlockType;
            int hashCode = (biometricSourceType != null ? biometricSourceType.hashCode() : 0) * 31;
            boolean z = this.isStrongBiometric;
            if (z) {
                z = true;
            }
            int i = z ? 1 : 0;
            int i2 = z ? 1 : 0;
            int i3 = z ? 1 : 0;
            return hashCode + i;
        }

        @NotNull
        public String toString() {
            return "PendingUnlock(pendingUnlockType=" + this.pendingUnlockType + ", isStrongBiometric=" + this.isStrongBiometric + ")";
        }

        public PendingUnlock(@NotNull BiometricSourceType biometricSourceType, boolean z) {
            Intrinsics.checkParameterIsNotNull(biometricSourceType, "pendingUnlockType");
            this.pendingUnlockType = biometricSourceType;
            this.isStrongBiometric = z;
        }

        @NotNull
        public final BiometricSourceType getPendingUnlockType() {
            return this.pendingUnlockType;
        }

        public final boolean isStrongBiometric() {
            return this.isStrongBiometric;
        }
    }

    public final void setUnlockController(@NotNull BiometricUnlockController biometricUnlockController) {
        Intrinsics.checkParameterIsNotNull(biometricUnlockController, "<set-?>");
        this.unlockController = biometricUnlockController;
    }

    public final void setPulseExpanding(boolean z) {
        this.isPulseExpanding = z;
    }

    public final boolean getBypassEnabled() {
        return this.bypassEnabled && this.mKeyguardStateController.isFaceAuthEnabled();
    }

    public final void setBouncerShowing(boolean z) {
        this.bouncerShowing = z;
    }

    public final void setLaunchingAffordance(boolean z) {
        this.launchingAffordance = z;
    }

    public final void setQSExpanded(boolean z) {
        boolean z2 = this.qSExpanded != z;
        this.qSExpanded = z;
        if (z2 && !z) {
            maybePerformPendingUnlock();
        }
    }

    public KeyguardBypassController(@NotNull Context context, @NotNull final TunerService tunerService, @NotNull StatusBarStateController statusBarStateController2, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull KeyguardStateController keyguardStateController, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(tunerService, "tunerService");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager, "lockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(keyguardStateController, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.mKeyguardStateController = keyguardStateController;
        this.statusBarStateController = statusBarStateController2;
        boolean hasSystemFeature = context.getPackageManager().hasSystemFeature("android.hardware.biometrics.face");
        this.hasFaceFeature = hasSystemFeature;
        if (hasSystemFeature) {
            dumpManager.registerDumpable("KeyguardBypassController", this);
            statusBarStateController2.addCallback(new StatusBarStateController.StateListener(this) {
                /* class com.android.systemui.statusbar.phone.KeyguardBypassController.AnonymousClass1 */
                final /* synthetic */ KeyguardBypassController this$0;

                /* JADX WARN: Incorrect args count in method signature: ()V */
                {
                    this.this$0 = r1;
                }

                @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
                public void onStateChanged(int i) {
                    if (i != 1) {
                        this.this$0.pendingUnlock = null;
                    }
                }
            });
            final int i = context.getResources().getBoolean(17891460) ? 1 : 0;
            tunerService.addTunable(new TunerService.Tunable(this) {
                /* class com.android.systemui.statusbar.phone.KeyguardBypassController.AnonymousClass2 */
                final /* synthetic */ KeyguardBypassController this$0;

                {
                    this.this$0 = r1;
                }

                @Override // com.android.systemui.tuner.TunerService.Tunable
                public void onTuningChanged(@Nullable String str, @Nullable String str2) {
                    this.this$0.bypassEnabled = tunerService.getValue(str, i) != 0;
                }
            }, "face_unlock_dismisses_keyguard");
            notificationLockscreenUserManager.addUserChangedListener(new NotificationLockscreenUserManager.UserChangedListener(this) {
                /* class com.android.systemui.statusbar.phone.KeyguardBypassController.AnonymousClass3 */
                final /* synthetic */ KeyguardBypassController this$0;

                /* JADX WARN: Incorrect args count in method signature: ()V */
                {
                    this.this$0 = r1;
                }

                @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
                public void onUserChanged(int i) {
                    this.this$0.pendingUnlock = null;
                }
            });
        }
    }

    public final boolean onBiometricAuthenticated(@NotNull BiometricSourceType biometricSourceType, boolean z) {
        Intrinsics.checkParameterIsNotNull(biometricSourceType, "biometricSourceType");
        if (!getBypassEnabled()) {
            return true;
        }
        boolean canBypass = canBypass();
        if (!canBypass && (this.isPulseExpanding || this.qSExpanded)) {
            this.pendingUnlock = new PendingUnlock(biometricSourceType, z);
        }
        return canBypass;
    }

    public final void maybePerformPendingUnlock() {
        PendingUnlock pendingUnlock2 = this.pendingUnlock;
        if (pendingUnlock2 == null) {
            return;
        }
        if (pendingUnlock2 != null) {
            BiometricSourceType pendingUnlockType = pendingUnlock2.getPendingUnlockType();
            PendingUnlock pendingUnlock3 = this.pendingUnlock;
            if (pendingUnlock3 == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (onBiometricAuthenticated(pendingUnlockType, pendingUnlock3.isStrongBiometric())) {
                BiometricUnlockController biometricUnlockController = this.unlockController;
                if (biometricUnlockController != null) {
                    PendingUnlock pendingUnlock4 = this.pendingUnlock;
                    if (pendingUnlock4 != null) {
                        BiometricSourceType pendingUnlockType2 = pendingUnlock4.getPendingUnlockType();
                        PendingUnlock pendingUnlock5 = this.pendingUnlock;
                        if (pendingUnlock5 != null) {
                            biometricUnlockController.startWakeAndUnlock(pendingUnlockType2, pendingUnlock5.isStrongBiometric());
                            this.pendingUnlock = null;
                            return;
                        }
                        Intrinsics.throwNpe();
                        throw null;
                    }
                    Intrinsics.throwNpe();
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("unlockController");
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final boolean canBypass() {
        if (!getBypassEnabled()) {
            return false;
        }
        if (!this.bouncerShowing && (this.statusBarStateController.getState() != 1 || this.launchingAffordance || this.isPulseExpanding || this.qSExpanded)) {
            return false;
        }
        return true;
    }

    public final void onStartedGoingToSleep() {
        this.pendingUnlock = null;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("KeyguardBypassController:");
        if (this.pendingUnlock != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("  mPendingUnlock.pendingUnlockType: ");
            PendingUnlock pendingUnlock2 = this.pendingUnlock;
            if (pendingUnlock2 != null) {
                sb.append(pendingUnlock2.getPendingUnlockType());
                printWriter.println(sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  mPendingUnlock.isStrongBiometric: ");
                PendingUnlock pendingUnlock3 = this.pendingUnlock;
                if (pendingUnlock3 != null) {
                    sb2.append(pendingUnlock3.isStrongBiometric());
                    printWriter.println(sb2.toString());
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            printWriter.println("  mPendingUnlock: " + this.pendingUnlock);
        }
        printWriter.println("  bypassEnabled: " + getBypassEnabled());
        printWriter.println("  canBypass: " + canBypass());
        printWriter.println("  bouncerShowing: " + this.bouncerShowing);
        printWriter.println("  isPulseExpanding: " + this.isPulseExpanding);
        printWriter.println("  launchingAffordance: " + this.launchingAffordance);
        printWriter.println("  qSExpanded: " + this.qSExpanded);
        printWriter.println("  hasFaceFeature: " + this.hasFaceFeature);
    }
}
