package com.android.keyguard.charge;

public class MiuiBatteryStatus {
    public int carChargeMode;
    public int chargeDeviceType;
    public int chargeSpeed;
    public int health;
    public int level;
    public int maxChargingWattage;
    public int plugged;
    public int status;
    public int wireState;

    public static boolean isPluggedIn(int i) {
        return i == 1 || i == 2 || i == 4;
    }

    public MiuiBatteryStatus(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        this.status = i;
        this.plugged = i2;
        this.level = i3;
        this.wireState = i4;
        this.chargeSpeed = i5;
        this.chargeDeviceType = i6;
        this.health = i7;
        this.maxChargingWattage = i8;
        this.carChargeMode = i9;
    }

    public boolean isPluggedIn() {
        return isPluggedIn(this.plugged);
    }

    public boolean isCharged() {
        return this.status == 5 || this.level >= 100;
    }

    public boolean isBatteryLow() {
        return this.level < 20;
    }

    public boolean isChargingOrFull() {
        int i = this.status;
        return i == 2 || i == 5;
    }

    public boolean isCharging() {
        return isPluggedIn() && isChargingOrFull();
    }

    public boolean isPluggedInWired() {
        int i = this.plugged;
        return i == 1 || i == 2;
    }

    public boolean isUsbPluggedIn() {
        return this.plugged == 2;
    }

    public boolean isQuickCharge() {
        return this.chargeSpeed >= 1;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isCarCharge() {
        return this.carChargeMode == 1;
    }
}
