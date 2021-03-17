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
        boolean z2 = true;
        if (z) {
            z = true;
        }
        int i = (hashCode + (z ? 1 : 0)) * 31;
        boolean z3 = this.isBouncer;
        if (z3) {
            z3 = true;
        }
        int i2 = (i + (z3 ? 1 : 0)) * 31;
        boolean z4 = this.isAuthInterruptActive;
        if (z4) {
            z4 = true;
        }
        int i3 = (i2 + (z4 ? 1 : 0)) * 31;
        boolean z5 = this.isKeyguardAwake;
        if (z5) {
            z5 = true;
        }
        int i4 = (i3 + (z5 ? 1 : 0)) * 31;
        boolean z6 = this.isListeningForFaceAssistant;
        if (z6) {
            z6 = true;
        }
        int i5 = (i4 + (z6 ? 1 : 0)) * 31;
        boolean z7 = this.isSwitchingUser;
        if (z7) {
            z7 = true;
        }
        int i6 = (i5 + (z7 ? 1 : 0)) * 31;
        boolean z8 = this.isFaceDisabled;
        if (z8) {
            z8 = true;
        }
        int i7 = (i6 + (z8 ? 1 : 0)) * 31;
        boolean z9 = this.isBecauseCannotSkipBouncer;
        if (z9) {
            z9 = true;
        }
        int i8 = (i7 + (z9 ? 1 : 0)) * 31;
        boolean z10 = this.isKeyguardGoingAway;
        if (z10) {
            z10 = true;
        }
        int i9 = (i8 + (z10 ? 1 : 0)) * 31;
        boolean z11 = this.isFaceSettingEnabledForUser;
        if (z11) {
            z11 = true;
        }
        int i10 = (i9 + (z11 ? 1 : 0)) * 31;
        boolean z12 = this.isLockIconPressed;
        if (z12) {
            z12 = true;
        }
        int i11 = (i10 + (z12 ? 1 : 0)) * 31;
        boolean z13 = this.isScanningAllowedByStrongAuth;
        if (z13) {
            z13 = true;
        }
        int i12 = (i11 + (z13 ? 1 : 0)) * 31;
        boolean z14 = this.isPrimaryUser;
        if (z14) {
            z14 = true;
        }
        int i13 = (i12 + (z14 ? 1 : 0)) * 31;
        boolean z15 = this.isSecureCameraLaunched;
        if (!z15) {
            z2 = z15;
        }
        return i13 + (z2 ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "KeyguardFaceListenModel(timeMillis=" + this.timeMillis + ", userId=" + this.userId + ", isListeningForFace=" + this.isListeningForFace + ", isBouncer=" + this.isBouncer + ", isAuthInterruptActive=" + this.isAuthInterruptActive + ", isKeyguardAwake=" + this.isKeyguardAwake + ", isListeningForFaceAssistant=" + this.isListeningForFaceAssistant + ", isSwitchingUser=" + this.isSwitchingUser + ", isFaceDisabled=" + this.isFaceDisabled + ", isBecauseCannotSkipBouncer=" + this.isBecauseCannotSkipBouncer + ", isKeyguardGoingAway=" + this.isKeyguardGoingAway + ", isFaceSettingEnabledForUser=" + this.isFaceSettingEnabledForUser + ", isLockIconPressed=" + this.isLockIconPressed + ", isScanningAllowedByStrongAuth=" + this.isScanningAllowedByStrongAuth + ", isPrimaryUser=" + this.isPrimaryUser + ", isSecureCameraLaunched=" + this.isSecureCameraLaunched + ")";
    }

    public final long getTimeMillis() {
        return this.timeMillis;
    }
}
