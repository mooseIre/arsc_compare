package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.DemoModeController;
import com.android.systemui.statusbar.policy.MobileSignalController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SlaveWifiSignalController;
import java.util.Iterator;

public class MiuiStatusBarSignalPolicy extends StatusBarSignalPolicy implements DemoMode {
    protected boolean mInDemoMode;
    protected boolean mLastShowNoSim;
    protected boolean mLastSimDetected;
    private String mSlotNoSim;

    public MiuiStatusBarSignalPolicy(Context context, StatusBarIconController statusBarIconController) {
        super(context, statusBarIconController);
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy
    public void initMiuiSlot() {
        this.mIconController.setIcon("demo_mobile", C0013R$drawable.stat_sys_signal_5, this.mContext.getString(C0021R$string.accessibility_data_signal_full));
        this.mIconController.setIcon("demo_wifi", C0013R$drawable.stat_sys_wifi_signal_4, this.mContext.getString(C0021R$string.accessibility_wifi_signal_full));
        this.mIconController.setIconVisibility("demo_mobile", false);
        this.mIconController.setIconVisibility("demo_wifi", false);
        String string = this.mContext.getString(C0021R$string.status_bar_no_sim);
        this.mSlotNoSim = string;
        this.mIconController.setIcon(string, C0013R$drawable.stat_sys_no_sim, this.mContext.getResources().getString(C0021R$string.accessibility_no_sim));
        this.mIconController.setIconVisibility(this.mSlotNoSim, false);
        ((SlaveWifiSignalController) Dependency.get(SlaveWifiSignalController.class)).start();
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
        super.setNoSims(z, z2);
        this.mLastShowNoSim = z;
        this.mLastSimDetected = z2;
        this.mIconController.setIconVisibility(this.mSlotNoSim, z && !this.mInDemoMode && !this.mIsAirplaneMode);
    }

    @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy, com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        super.setIsAirplaneMode(iconState);
        setNoSims(this.mLastShowNoSim, this.mLastSimDetected);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, int i, String str, boolean z4, String str2, boolean z5) {
        boolean z6 = iconState.visible && !this.mBlockWifi;
        boolean z7 = z2 && this.mActivityEnabled && z6;
        boolean z8 = z3 && this.mActivityEnabled && z6;
        StatusBarSignalPolicy.WifiIconState copy = this.mWifiIconState.copy();
        copy.visible = z6;
        copy.resId = iconState.icon;
        copy.activityIn = z7;
        copy.activityOut = z8;
        copy.wifiStandard = i;
        copy.showWifiStandard = i == 6 && !z5;
        copy.slot = this.mSlotWifi;
        copy.airplaneSpacerVisible = this.mIsAirplaneMode;
        copy.contentDescription = iconState.contentDescription;
        copy.wifiNoNetwork = z5;
        if (z7 && z8) {
            copy.activityVisible = true;
            copy.activityResId = C0013R$drawable.stat_sys_wifi_inout;
        } else if (z7) {
            copy.activityVisible = true;
            copy.activityResId = C0013R$drawable.stat_sys_wifi_in;
        } else if (z8) {
            copy.activityVisible = true;
            copy.activityResId = C0013R$drawable.stat_sys_wifi_out;
        } else {
            copy.activityVisible = false;
            copy.activityResId = 0;
        }
        if (copy.wifiNoNetwork) {
            copy.activityVisible = false;
            copy.activityResId = 0;
        }
        updateWifiIconWithState(copy);
        this.mWifiIconState = copy;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy
    public void updateWifiIconWithState(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        this.mIconController.setSignalIcon(this.mSlotWifi, wifiIconState);
        if (!wifiIconState.visible || wifiIconState.resId <= 0) {
            this.mIconController.setIconVisibility(this.mSlotWifi, false);
        } else {
            this.mIconController.setIconVisibility(this.mSlotWifi, true);
        }
        updateMobileIconStateOnWifiIconStateChange(wifiIconState);
    }

    /* access modifiers changed from: protected */
    public void updateMobileIconStateOnWifiIconStateChange(StatusBarSignalPolicy.WifiIconState wifiIconState) {
        boolean z = false;
        boolean z2 = wifiIconState.visible && !wifiIconState.wifiNoNetwork;
        Iterator<StatusBarSignalPolicy.MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            StatusBarSignalPolicy.MobileIconState next = it.next();
            if (next.wifiAvailable != z2) {
                next.wifiAvailable = z2;
                z = true;
            }
        }
        if (z) {
            this.mIconController.setMobileIcons(this.mSlotMobile, StatusBarSignalPolicy.MobileIconState.copyStates(this.mMobileStates));
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4, MobileSignalController.MiuiMobileState miuiMobileState) {
        StatusBarSignalPolicy.MobileIconState state = getState(i4);
        if (state != null) {
            boolean z5 = true;
            state.visible = iconState.visible && !this.mBlockMobile;
            state.strengthId = iconState.icon;
            state.typeId = i;
            state.contentDescription = iconState.contentDescription;
            state.typeContentDescription = charSequence;
            state.roaming = z4;
            state.activityIn = z && this.mActivityEnabled;
            if (!z2 || !this.mActivityEnabled) {
                z5 = false;
            }
            state.activityOut = z5;
            state.airplane = miuiMobileState.airplane;
            state.dataConnected = miuiMobileState.dataConnected;
            state.networkName = miuiMobileState.showName;
            state.volte = miuiMobileState.volte;
            state.volteId = miuiMobileState.volteResId;
            state.hideVolte = miuiMobileState.hideVolte;
            state.vowifi = miuiMobileState.vowifi;
            state.vowifiId = miuiMobileState.vowifiResId;
            state.hideVowifi = miuiMobileState.hideVowifi;
            state.speechHd = miuiMobileState.speedHd;
            state.volteNoSerivce = miuiMobileState.volteNoService;
            state.fiveGDrawableId = miuiMobileState.qcom5GDrawableId;
            state.showDataTypeWhenWifiOn = miuiMobileState.showDataTypeWhenWifiOn;
            state.showDataTypeDataDisconnected = miuiMobileState.showDataTypeDataDisconnected;
            state.showMobileDataTypeInMMS = miuiMobileState.showMobileDataTypeInMMS;
            this.mIconController.setMobileIcons(this.mSlotMobile, StatusBarSignalPolicy.MobileIconState.copyStates(this.mMobileStates));
        }
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mInDemoMode && str.equals("enter")) {
            this.mInDemoMode = true;
            updateDemoIconVisibility();
        } else if (this.mInDemoMode && str.equals("exit")) {
            this.mInDemoMode = false;
            updateDemoIconVisibility();
        }
    }

    /* access modifiers changed from: protected */
    public void updateDemoIconVisibility() {
        this.mIconController.setIconVisibility("demo_mobile", this.mInDemoMode);
        this.mIconController.setIconVisibility("demo_wifi", this.mInDemoMode);
        setNoSims(this.mLastShowNoSim, this.mLastSimDetected);
    }
}
