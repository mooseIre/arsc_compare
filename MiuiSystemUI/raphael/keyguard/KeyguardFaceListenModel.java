package com.android.keyguard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardFaceListenModel.kt */
public final class KeyguardFaceListenModel {
    private final boolean isAuthInterruptActive;
    private final boolean isBecauseCannotSkipBouncer;
    private final boolean isBouncer;
    private final boolean isFaceDisabled;
    private final boolean isFaceSettingEnabledForUser;
    private final boolean isKeyguardAwake;
    private final boolean isKeyguardGoingAway;
    private final boolean isListeningForFace;
    private final boolean isListeningForFaceAssistant;
    private final boolean isLockIconPressed;
    private final boolean isPrimaryUser;
    private final boolean isScanningAllowedByStrongAuth;
    private final boolean isSecureCameraLaunched;
    private final boolean isSwitchingUser;
    private final long timeMillis;
    private final int userId;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyguardFaceListenModel)) {
            return false;
        }
        KeyguardFaceListenModel keyguardFaceListenModel = (KeyguardFaceListenModel) obj;
        return this.timeMillis == keyguardFaceListenModel.timeMillis && this.userId == keyguardFaceListenModel.userId && this.isListeningForFace == keyguardFaceListenModel.isListeningForFace && this.isBouncer == keyguardFaceListenModel.isBouncer && this.isAuthInterruptActive == keyguardFaceListenModel.isAuthInterruptActive && this.isKeyguardAwake == keyguardFaceListenModel.isKeyguardAwake && this.isListeningForFaceAssistant == keyguardFaceListenModel.isListeningForFaceAssistant && this.isSwitchingUser == keyguardFaceListenModel.isSwitchingUser && this.isFaceDisabled == keyguardFaceListenModel.isFaceDisabled && this.isBecauseCannotSkipBouncer == keyguardFaceListenModel.isBecauseCannotSkipBouncer && this.isKeyguardGoingAway == keyguardFaceListenModel.isKeyguardGoingAway && this.isFaceSettingEnabledForUser == keyguardFaceListenModel.isFaceSettingEnabledForUser && this.isLockIconPressed == keyguardFaceListenModel.isLockIconPressed && this.isScanningAllowedByStrongAuth == keyguardFaceListenModel.isScanningAllowedByStrongAuth && this.isPrimaryUser == keyguardFaceListenModel.isPrimaryUser && this.isSecureCameraLaunched == keyguardFaceListenModel.isSecureCameraLaunched;
    }

    public int hashCode() {
        int hashCode = ((Long.hashCode(this.timeMillis) * 31) + Integer.hashCode(this.userId)) * 31;
        boolean z = this.isListeningForFace;
        int i = 1;
        if (z) {
            z = true;
        }
        int i2 = z ? 1 : 0;
        int i3 = z ? 1 : 0;
        int i4 = z ? 1 : 0;
        int i5 = (hashCode + i2) * 31;
        boolean z2 = this.isBouncer;
        if (z2) {
            z2 = true;
        }
        int i6 = z2 ? 1 : 0;
        int i7 = z2 ? 1 : 0;
        int i8 = z2 ? 1 : 0;
        int i9 = (i5 + i6) * 31;
        boolean z3 = this.isAuthInterruptActive;
        if (z3) {
            z3 = true;
        }
        int i10 = z3 ? 1 : 0;
        int i11 = z3 ? 1 : 0;
        int i12 = z3 ? 1 : 0;
        int i13 = (i9 + i10) * 31;
        boolean z4 = this.isKeyguardAwake;
        if (z4) {
            z4 = true;
        }
        int i14 = z4 ? 1 : 0;
        int i15 = z4 ? 1 : 0;
        int i16 = z4 ? 1 : 0;
        int i17 = (i13 + i14) * 31;
        boolean z5 = this.isListeningForFaceAssistant;
        if (z5) {
            z5 = true;
        }
        int i18 = z5 ? 1 : 0;
        int i19 = z5 ? 1 : 0;
        int i20 = z5 ? 1 : 0;
        int i21 = (i17 + i18) * 31;
        boolean z6 = this.isSwitchingUser;
        if (z6) {
            z6 = true;
        }
        int i22 = z6 ? 1 : 0;
        int i23 = z6 ? 1 : 0;
        int i24 = z6 ? 1 : 0;
        int i25 = (i21 + i22) * 31;
        boolean z7 = this.isFaceDisabled;
        if (z7) {
            z7 = true;
        }
        int i26 = z7 ? 1 : 0;
        int i27 = z7 ? 1 : 0;
        int i28 = z7 ? 1 : 0;
        int i29 = (i25 + i26) * 31;
        boolean z8 = this.isBecauseCannotSkipBouncer;
        if (z8) {
            z8 = true;
        }
        int i30 = z8 ? 1 : 0;
        int i31 = z8 ? 1 : 0;
        int i32 = z8 ? 1 : 0;
        int i33 = (i29 + i30) * 31;
        boolean z9 = this.isKeyguardGoingAway;
        if (z9) {
            z9 = true;
        }
        int i34 = z9 ? 1 : 0;
        int i35 = z9 ? 1 : 0;
        int i36 = z9 ? 1 : 0;
        int i37 = (i33 + i34) * 31;
        boolean z10 = this.isFaceSettingEnabledForUser;
        if (z10) {
            z10 = true;
        }
        int i38 = z10 ? 1 : 0;
        int i39 = z10 ? 1 : 0;
        int i40 = z10 ? 1 : 0;
        int i41 = (i37 + i38) * 31;
        boolean z11 = this.isLockIconPressed;
        if (z11) {
            z11 = true;
        }
        int i42 = z11 ? 1 : 0;
        int i43 = z11 ? 1 : 0;
        int i44 = z11 ? 1 : 0;
        int i45 = (i41 + i42) * 31;
        boolean z12 = this.isScanningAllowedByStrongAuth;
        if (z12) {
            z12 = true;
        }
        int i46 = z12 ? 1 : 0;
        int i47 = z12 ? 1 : 0;
        int i48 = z12 ? 1 : 0;
        int i49 = (i45 + i46) * 31;
        boolean z13 = this.isPrimaryUser;
        if (z13) {
            z13 = true;
        }
        int i50 = z13 ? 1 : 0;
        int i51 = z13 ? 1 : 0;
        int i52 = z13 ? 1 : 0;
        int i53 = (i49 + i50) * 31;
        boolean z14 = this.isSecureCameraLaunched;
        if (!z14) {
            i = z14 ? 1 : 0;
        }
        return i53 + i;
    }

    @NotNull
    public String toString() {
        return "KeyguardFaceListenModel(timeMillis=" + this.timeMillis + ", userId=" + this.userId + ", isListeningForFace=" + this.isListeningForFace + ", isBouncer=" + this.isBouncer + ", isAuthInterruptActive=" + this.isAuthInterruptActive + ", isKeyguardAwake=" + this.isKeyguardAwake + ", isListeningForFaceAssistant=" + this.isListeningForFaceAssistant + ", isSwitchingUser=" + this.isSwitchingUser + ", isFaceDisabled=" + this.isFaceDisabled + ", isBecauseCannotSkipBouncer=" + this.isBecauseCannotSkipBouncer + ", isKeyguardGoingAway=" + this.isKeyguardGoingAway + ", isFaceSettingEnabledForUser=" + this.isFaceSettingEnabledForUser + ", isLockIconPressed=" + this.isLockIconPressed + ", isScanningAllowedByStrongAuth=" + this.isScanningAllowedByStrongAuth + ", isPrimaryUser=" + this.isPrimaryUser + ", isSecureCameraLaunched=" + this.isSecureCameraLaunched + ")";
    }

    public final long getTimeMillis() {
        return this.timeMillis;
    }
}
