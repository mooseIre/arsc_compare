package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.util.ArraySet;
import android.util.Log;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StatusBarSignalPolicy implements NetworkController.SignalCallback, SecurityController.SecurityControllerCallback, TunerService.Tunable {
    protected boolean mActivityEnabled;
    protected boolean mBlockAirplane;
    protected boolean mBlockEthernet;
    protected boolean mBlockMobile;
    protected boolean mBlockWifi;
    protected final Context mContext;
    protected boolean mForceBlockWifi;
    protected final Handler mHandler = Handler.getMain();
    protected final StatusBarIconController mIconController;
    protected boolean mIsAirplaneMode = false;
    protected ArrayList<MobileIconState> mMobileStates = new ArrayList<>();
    protected final NetworkController mNetworkController;
    protected final SecurityController mSecurityController;
    protected final String mSlotAirplane;
    protected final String mSlotEthernet;
    protected final String mSlotMobile;
    protected final String mSlotVpn;
    protected final String mSlotWifi;
    protected WifiIconState mWifiIconState = new WifiIconState();

    public abstract void initMiuiSlot();

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataEnabled(boolean z) {
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
    }

    /* access modifiers changed from: protected */
    public abstract void updateWifiIconWithState(WifiIconState wifiIconState);

    public StatusBarSignalPolicy(Context context, StatusBarIconController statusBarIconController) {
        this.mContext = context;
        this.mSlotAirplane = context.getString(17041382);
        this.mSlotMobile = this.mContext.getString(17041399);
        this.mSlotWifi = this.mContext.getString(17041414);
        this.mSlotEthernet = this.mContext.getString(17041392);
        this.mSlotVpn = this.mContext.getString(17041413);
        this.mActivityEnabled = this.mContext.getResources().getBoolean(C0010R$bool.config_showActivity);
        this.mIconController = statusBarIconController;
        initMiuiSlot();
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mSecurityController = (SecurityController) Dependency.get(SecurityController.class);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
        this.mNetworkController.addCallback((NetworkController.SignalCallback) this);
        this.mSecurityController.addCallback(this);
    }

    /* access modifiers changed from: private */
    public void updateVpn() {
        boolean z = this.mSecurityController.isVpnEnabled() && !this.mSecurityController.isSilentVpnPackage();
        this.mIconController.setIcon(this.mSlotVpn, currentVpnIconId(this.mSecurityController.isVpnBranded()), this.mContext.getResources().getString(C0021R$string.accessibility_vpn_on));
        this.mIconController.setIconVisibility(this.mSlotVpn, z);
    }

    private int currentVpnIconId(boolean z) {
        return C0013R$drawable.stat_sys_vpn;
    }

    @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
    public void onStateChanged() {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$StatusBarSignalPolicy$UsBELiDs0GJjQ8hYeagcWJmxhFc */

            public final void run() {
                StatusBarSignalPolicy.this.updateVpn();
            }
        });
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            ArraySet<String> iconBlacklist = StatusBarIconController.getIconBlacklist(this.mContext, str2);
            boolean contains = iconBlacklist.contains(this.mSlotAirplane);
            boolean contains2 = iconBlacklist.contains(this.mSlotMobile);
            boolean contains3 = iconBlacklist.contains(this.mSlotWifi);
            boolean contains4 = iconBlacklist.contains(this.mSlotEthernet);
            if (contains != this.mBlockAirplane || contains2 != this.mBlockMobile || contains4 != this.mBlockEthernet || contains3 != this.mBlockWifi) {
                this.mBlockAirplane = contains;
                this.mBlockMobile = contains2;
                this.mBlockEthernet = contains4;
                this.mBlockWifi = contains3 || this.mForceBlockWifi;
                this.mNetworkController.removeCallback((NetworkController.SignalCallback) this);
                this.mNetworkController.addCallback((NetworkController.SignalCallback) this);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
        boolean z5 = true;
        boolean z6 = iconState.visible && !this.mBlockWifi;
        boolean z7 = z2 && this.mActivityEnabled && z6;
        boolean z8 = z3 && this.mActivityEnabled && z6;
        WifiIconState copy = this.mWifiIconState.copy();
        copy.visible = z6;
        copy.resId = iconState.icon;
        copy.activityIn = z7;
        copy.activityOut = z8;
        copy.slot = this.mSlotWifi;
        copy.airplaneSpacerVisible = this.mIsAirplaneMode;
        copy.contentDescription = iconState.contentDescription;
        MobileIconState firstMobileState = getFirstMobileState();
        if (firstMobileState == null || firstMobileState.typeId == 0) {
            z5 = false;
        }
        copy.signalSpacerVisible = z5;
        updateWifiIconWithState(copy);
        this.mWifiIconState = copy;
    }

    private void updateShowWifiSignalSpacer(WifiIconState wifiIconState) {
        MobileIconState firstMobileState = getFirstMobileState();
        wifiIconState.signalSpacerVisible = (firstMobileState == null || firstMobileState.typeId == 0) ? false : true;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
        MobileIconState state = getState(i4);
        if (state != null) {
            int i5 = state.typeId;
            boolean z5 = true;
            boolean z6 = i != i5 && (i == 0 || i5 == 0);
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
            state.volteId = i3;
            this.mIconController.setMobileIcons(this.mSlotMobile, MobileIconState.copyStates(this.mMobileStates));
            if (z6) {
                WifiIconState copy = this.mWifiIconState.copy();
                updateShowWifiSignalSpacer(copy);
                if (!Objects.equals(copy, this.mWifiIconState)) {
                    updateWifiIconWithState(copy);
                    this.mWifiIconState = copy;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public MobileIconState getState(int i) {
        Iterator<MobileIconState> it = this.mMobileStates.iterator();
        while (it.hasNext()) {
            MobileIconState next = it.next();
            if (next.subId == i) {
                return next;
            }
        }
        Log.e("StatusBarSignalPolicy", "Unexpected subscription " + i);
        return null;
    }

    /* access modifiers changed from: protected */
    public MobileIconState getFirstMobileState() {
        if (this.mMobileStates.size() > 0) {
            return this.mMobileStates.get(0);
        }
        return null;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setSubs(List<SubscriptionInfo> list) {
        if (!hasCorrectSubs(list)) {
            this.mIconController.removeAllIconsForSlot(this.mSlotMobile);
            this.mMobileStates.clear();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                this.mMobileStates.add(new MobileIconState(list.get(i).getSubscriptionId()));
            }
        }
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> list) {
        int size = list.size();
        if (size != this.mMobileStates.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.mMobileStates.get(i).subId != list.get(i).getSubscriptionId()) {
                return false;
            }
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setEthernetIndicators(NetworkController.IconState iconState) {
        boolean z = iconState.visible && !this.mBlockEthernet;
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (!z || i <= 0) {
            this.mIconController.setIconVisibility(this.mSlotEthernet, false);
            return;
        }
        this.mIconController.setIcon(this.mSlotEthernet, i, str);
        this.mIconController.setIconVisibility(this.mSlotEthernet, true);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        boolean z = iconState.visible && !this.mBlockAirplane;
        this.mIsAirplaneMode = z;
        int i = iconState.icon;
        String str = iconState.contentDescription;
        if (!z || i <= 0) {
            this.mIconController.setIconVisibility(this.mSlotAirplane, false);
            return;
        }
        this.mIconController.setIcon(this.mSlotAirplane, i, str);
        this.mIconController.setIconVisibility(this.mSlotAirplane, true);
    }

    /* access modifiers changed from: private */
    public static abstract class SignalIconState {
        public boolean activityIn;
        public boolean activityOut;
        public String contentDescription;
        public String slot;
        public boolean visible;

        private SignalIconState() {
        }

        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SignalIconState signalIconState = (SignalIconState) obj;
            if (this.visible == signalIconState.visible && this.activityOut == signalIconState.activityOut && this.activityIn == signalIconState.activityIn && Objects.equals(this.contentDescription, signalIconState.contentDescription) && Objects.equals(this.slot, signalIconState.slot)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(Boolean.valueOf(this.visible), Boolean.valueOf(this.activityOut), this.slot);
        }

        /* access modifiers changed from: protected */
        public void copyTo(SignalIconState signalIconState) {
            signalIconState.visible = this.visible;
            signalIconState.activityIn = this.activityIn;
            signalIconState.activityOut = this.activityOut;
            signalIconState.slot = this.slot;
            signalIconState.contentDescription = this.contentDescription;
        }
    }

    public static class WifiIconState extends SignalIconState {
        public int activityResId;
        public boolean activityVisible;
        public boolean airplaneSpacerVisible;
        public int resId;
        public boolean showWifiStandard;
        public boolean signalSpacerVisible;
        public boolean wifiNoNetwork;
        public int wifiStandard;

        public WifiIconState() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public boolean equals(Object obj) {
            if (obj == null || WifiIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            WifiIconState wifiIconState = (WifiIconState) obj;
            if (this.resId == wifiIconState.resId && this.airplaneSpacerVisible == wifiIconState.airplaneSpacerVisible && this.signalSpacerVisible == wifiIconState.signalSpacerVisible && this.activityResId == wifiIconState.activityResId && this.activityVisible == wifiIconState.activityVisible && this.wifiStandard == wifiIconState.wifiStandard && this.showWifiStandard == wifiIconState.showWifiStandard && this.wifiNoNetwork == wifiIconState.wifiNoNetwork) {
                return true;
            }
            return false;
        }

        public void copyTo(WifiIconState wifiIconState) {
            super.copyTo((SignalIconState) wifiIconState);
            wifiIconState.resId = this.resId;
            wifiIconState.airplaneSpacerVisible = this.airplaneSpacerVisible;
            wifiIconState.signalSpacerVisible = this.signalSpacerVisible;
            wifiIconState.activityVisible = this.activityVisible;
            wifiIconState.activityResId = this.activityResId;
            wifiIconState.wifiStandard = this.wifiStandard;
            wifiIconState.showWifiStandard = this.showWifiStandard;
            wifiIconState.wifiNoNetwork = this.wifiNoNetwork;
        }

        public WifiIconState copy() {
            WifiIconState wifiIconState = new WifiIconState();
            copyTo(wifiIconState);
            return wifiIconState;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.resId), Boolean.valueOf(this.airplaneSpacerVisible), Boolean.valueOf(this.signalSpacerVisible), Boolean.valueOf(this.activityVisible), Integer.valueOf(this.activityResId), Integer.valueOf(this.wifiStandard), Boolean.valueOf(this.showWifiStandard), Boolean.valueOf(this.wifiNoNetwork));
        }

        public String toString() {
            return "WifiIconState(resId=" + this.resId + ", visible=" + this.visible + ", activityVisible = " + this.activityVisible + ", activityResId = " + this.activityResId + ", wifiStandard=" + this.wifiStandard + ", showWifiStandard=" + this.showWifiStandard + ", wifiNoNetwork=" + this.wifiNoNetwork + ")";
        }
    }

    public static class MobileIconState extends SignalIconState {
        public boolean airplane;
        public boolean dataConnected;
        public int fiveGDrawableId;
        public boolean hideVolte;
        public boolean hideVowifi;
        public String networkName;
        public boolean roaming;
        public boolean showDataTypeDataDisconnected;
        public boolean showDataTypeWhenWifiOn;
        public boolean showMobileDataTypeInMMS;
        public boolean speechHd;
        public int strengthId;
        public int subId;
        public CharSequence typeContentDescription;
        public int typeId;
        public boolean volte;
        public int volteId;
        public boolean volteNoSerivce;
        public boolean vowifi;
        public int vowifiId;
        public boolean wifiAvailable;

        private MobileIconState(int i) {
            super();
            this.subId = i;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public boolean equals(Object obj) {
            if (obj == null || MobileIconState.class != obj.getClass() || !super.equals(obj)) {
                return false;
            }
            MobileIconState mobileIconState = (MobileIconState) obj;
            if (this.subId == mobileIconState.subId && this.strengthId == mobileIconState.strengthId && this.typeId == mobileIconState.typeId && this.roaming == mobileIconState.roaming && Objects.equals(this.typeContentDescription, mobileIconState.typeContentDescription) && this.volteId == mobileIconState.volteId && this.airplane == mobileIconState.airplane && this.dataConnected == mobileIconState.dataConnected && this.wifiAvailable == mobileIconState.wifiAvailable && this.volte == mobileIconState.volte && this.hideVolte == mobileIconState.hideVolte && this.vowifiId == mobileIconState.vowifiId && this.vowifi == mobileIconState.vowifi && this.hideVowifi == mobileIconState.hideVowifi && this.speechHd == mobileIconState.speechHd && this.volteNoSerivce == mobileIconState.volteNoSerivce && this.fiveGDrawableId == mobileIconState.fiveGDrawableId && this.showDataTypeWhenWifiOn == mobileIconState.showDataTypeWhenWifiOn && this.showDataTypeDataDisconnected == mobileIconState.showDataTypeDataDisconnected && this.showMobileDataTypeInMMS == mobileIconState.showMobileDataTypeInMMS && Objects.equals(this.networkName, mobileIconState.networkName)) {
                return true;
            }
            return false;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarSignalPolicy.SignalIconState
        public int hashCode() {
            return Objects.hash(Integer.valueOf(super.hashCode()), Integer.valueOf(this.subId), Integer.valueOf(this.strengthId), Integer.valueOf(this.typeId), Boolean.valueOf(this.roaming), this.typeContentDescription, Integer.valueOf(this.volteId), Boolean.valueOf(this.airplane), Boolean.valueOf(this.dataConnected), Boolean.valueOf(this.wifiAvailable), this.networkName, Boolean.valueOf(this.volte), Boolean.valueOf(this.hideVolte), Integer.valueOf(this.vowifiId), Boolean.valueOf(this.vowifi), Boolean.valueOf(this.hideVowifi), Boolean.valueOf(this.speechHd), Boolean.valueOf(this.volteNoSerivce), Integer.valueOf(this.fiveGDrawableId), Boolean.valueOf(this.showDataTypeWhenWifiOn), Boolean.valueOf(this.showDataTypeDataDisconnected), Boolean.valueOf(this.showMobileDataTypeInMMS));
        }

        public MobileIconState copy() {
            MobileIconState mobileIconState = new MobileIconState(this.subId);
            copyTo(mobileIconState);
            return mobileIconState;
        }

        public void copyTo(MobileIconState mobileIconState) {
            super.copyTo((SignalIconState) mobileIconState);
            mobileIconState.subId = this.subId;
            mobileIconState.strengthId = this.strengthId;
            mobileIconState.typeId = this.typeId;
            mobileIconState.roaming = this.roaming;
            mobileIconState.typeContentDescription = this.typeContentDescription;
            mobileIconState.volteId = this.volteId;
            mobileIconState.airplane = this.airplane;
            mobileIconState.networkName = this.networkName;
            mobileIconState.dataConnected = this.dataConnected;
            mobileIconState.wifiAvailable = this.wifiAvailable;
            mobileIconState.volte = this.volte;
            mobileIconState.hideVolte = this.hideVolte;
            mobileIconState.volteNoSerivce = this.volteNoSerivce;
            mobileIconState.vowifiId = this.vowifiId;
            mobileIconState.vowifi = this.vowifi;
            mobileIconState.hideVowifi = this.hideVowifi;
            mobileIconState.speechHd = this.speechHd;
            mobileIconState.fiveGDrawableId = this.fiveGDrawableId;
            mobileIconState.showDataTypeWhenWifiOn = this.showDataTypeWhenWifiOn;
            mobileIconState.showDataTypeDataDisconnected = this.showDataTypeDataDisconnected;
            mobileIconState.showMobileDataTypeInMMS = this.showMobileDataTypeInMMS;
        }

        public static List<MobileIconState> copyStates(List<MobileIconState> list) {
            ArrayList arrayList = new ArrayList();
            for (MobileIconState mobileIconState : list) {
                MobileIconState mobileIconState2 = new MobileIconState(mobileIconState.subId);
                mobileIconState.copyTo(mobileIconState2);
                arrayList.add(mobileIconState2);
            }
            return arrayList;
        }

        public String toString() {
            return "MobileIconState(subId=" + this.subId + ", strengthId=" + this.strengthId + ", roaming=" + this.roaming + ", typeId=" + this.typeId + ", volteId=" + this.volteId + ", airplane = " + this.airplane + ", dataConnected = " + this.dataConnected + ", wifiAvailable = " + this.wifiAvailable + ", networkName = " + this.networkName + ", volte = " + this.volte + ", hideVolte = " + this.hideVolte + ", vowifiId = " + this.vowifiId + ", vowifi = " + this.vowifi + ", hideVowifi = " + this.hideVowifi + ", speechHd = " + this.speechHd + ", volteNoSerivce = " + this.volteNoSerivce + ", fiveGDrawableId = " + this.fiveGDrawableId + ", showDataTypeWhenWifiOn = " + this.showDataTypeWhenWifiOn + ", showDataTypeDataDisconnected = " + this.showDataTypeDataDisconnected + ", showMobileDataTypeInMMS = " + this.showMobileDataTypeInMMS + ", visible=" + this.visible + ")";
        }
    }
}
