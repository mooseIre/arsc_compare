package com.android.keyguard.charge;

public class BatteryStatus {
    protected int chargeDeviceType;
    protected int chargeSpeed;
    protected int level;
    protected int plugged;
    protected int status;
    protected int wireState;

    public static boolean isChargingOrFull(int i) {
        return i == 2 || i == 5;
    }

    public static boolean isPluggedIn(int i) {
        return i == 1 || i == 2 || i == 4;
    }

    public BatteryStatus(int i, int i2, int i3, int i4, int i5, int i6) {
        this.status = i;
        this.plugged = i2;
        this.level = i3;
        this.wireState = i4;
        this.chargeSpeed = i5;
        this.chargeDeviceType = i6;
    }

    public int getPluggedState() {
        return this.plugged;
    }

    public int getLevel() {
        return this.level;
    }

    public int getWireState() {
        return this.wireState;
    }

    public int getChargeSpeed() {
        return this.chargeSpeed;
    }

    public boolean isQuickCharge() {
        return this.chargeSpeed == 1;
    }

    public boolean isSuperQuickCharge() {
        return this.chargeSpeed == 2;
    }

    public boolean isPluggedIn() {
        return isPluggedIn(this.plugged);
    }

    public boolean isUsbPluggedIn() {
        return this.plugged == 2;
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

    public String getChargingState() {
        int i = this.wireState;
        if (i == 11) {
            return "wired_charging";
        }
        return i == 10 ? "wireless_charging" : "not_charging";
    }
}
