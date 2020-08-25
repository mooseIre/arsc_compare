package com.android.keyguard;

import android.os.SystemClock;
import android.telephony.ServiceState;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.charge.BatteryStatus;

public class KeyguardUpdateMonitorCallback {
    private boolean mShowing;
    private long mVisibilityChangedCalled;

    public void onAirplaneModeChanged() {
    }

    public void onBootCompleted() {
    }

    public void onBottomAreaButtonClicked(boolean z) {
    }

    public void onClockVisibilityChanged() {
    }

    public void onDevicePolicyManagerStateChanged() {
    }

    public void onDeviceProvisioned() {
    }

    public void onDreamingStateChanged(boolean z) {
    }

    public void onEmergencyCallAction() {
    }

    public void onFaceUnlockStateChanged(boolean z, int i) {
    }

    public void onFingerprintAcquired(int i) {
    }

    public void onFingerprintAuthFailed() {
    }

    public void onFingerprintAuthenticated(int i) {
    }

    public void onFingerprintError(int i, String str) {
    }

    public void onFingerprintHelp(int i, String str) {
    }

    public void onFingerprintLockoutReset() {
    }

    public void onFingerprintRunningStateChanged(boolean z) {
    }

    public void onFinishedGoingToSleep(int i) {
    }

    public void onKeyguardBouncerChanged(boolean z) {
    }

    public void onKeyguardGoingAway() {
    }

    public void onKeyguardOccludedChanged(boolean z) {
    }

    public void onKeyguardShowingChanged(boolean z) {
    }

    public void onKeyguardVisibilityChanged(boolean z) {
    }

    public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
    }

    public void onLockScreenMagazineStatusChanged() {
    }

    public void onLockWallpaperProviderChanged() {
    }

    public void onPhoneSignalChanged(boolean z) {
    }

    public void onPhoneStateChanged(int i) {
    }

    public void onPreFingerprintAuthenticated(int i) {
    }

    public void onRefreshBatteryInfo(BatteryStatus batteryStatus) {
    }

    public void onRefreshCarrierInfo() {
    }

    public void onRegionChanged() {
    }

    public void onRingerModeChanged(int i) {
    }

    public void onScreenTurnedOff() {
    }

    public void onScreenTurnedOn() {
    }

    public void onServiceStateChanged(int i, ServiceState serviceState) {
    }

    public void onSimLockedStateChanged(boolean z) {
    }

    public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
    }

    public void onStartedGoingToSleep(int i) {
    }

    public void onStartedWakingUp() {
    }

    public void onStartedWakingUpWithReason(String str) {
    }

    public void onStrongAuthStateChanged(int i) {
    }

    public void onSuperSavePowerChanged(boolean z) {
    }

    public void onTimeChanged() {
    }

    public void onUserInfoChanged(int i) {
    }

    public void onUserSwitchComplete(int i) {
    }

    public void onUserSwitching(int i) {
    }

    public void onUserUnlocked() {
    }

    public void updateShowingStatus(boolean z) {
    }

    public void onKeyguardVisibilityChangedRaw(boolean z) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (z != this.mShowing || elapsedRealtime - this.mVisibilityChangedCalled >= 1000) {
            onKeyguardVisibilityChanged(z);
            this.mVisibilityChangedCalled = elapsedRealtime;
            this.mShowing = z;
        }
    }
}
