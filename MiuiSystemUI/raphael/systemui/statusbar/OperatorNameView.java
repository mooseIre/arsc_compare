package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.WirelessUtils;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.tuner.TunerService;
import java.util.List;

public class OperatorNameView extends TextView implements DemoMode, DarkIconDispatcher.DarkReceiver, NetworkController.SignalCallback, TunerService.Tunable {
    private final KeyguardUpdateMonitorCallback mCallback;
    private boolean mDemoMode;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;

    public OperatorNameView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OperatorNameView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCallback = new KeyguardUpdateMonitorCallback() {
            /* class com.android.systemui.statusbar.OperatorNameView.AnonymousClass1 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onRefreshCarrierInfo() {
                OperatorNameView.this.updateText();
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mCallback);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this);
        ((NetworkController) Dependency.get(NetworkController.class)).addCallback((NetworkController.SignalCallback) this);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "show_operator_name");
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyguardUpdateMonitor.removeCallback(this.mCallback);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this);
        ((NetworkController) Dependency.get(NetworkController.class)).removeCallback((NetworkController.SignalCallback) this);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        if (z) {
            setTextColor(DarkIconDispatcher.getTint(rect, this, i));
            return;
        }
        if (DarkIconDispatcher.getDarkIntensity(rect, this, f) > 0.0f) {
            i2 = i3;
        }
        setTextColor(i2);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        update();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        update();
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            update();
        } else if (this.mDemoMode && str.equals("operator")) {
            setText(bundle.getString("name"));
        }
    }

    private void update() {
        boolean z = true;
        if (((TunerService) Dependency.get(TunerService.class)).getValue("show_operator_name", 1) == 0) {
            z = false;
        }
        setVisibility(z ? 0 : 8);
        boolean isNetworkSupported = ConnectivityManager.from(((TextView) this).mContext).isNetworkSupported(0);
        boolean isAirplaneModeOn = WirelessUtils.isAirplaneModeOn(((TextView) this).mContext);
        if (!isNetworkSupported || isAirplaneModeOn) {
            setText((CharSequence) null);
            setVisibility(8);
        } else if (!this.mDemoMode) {
            updateText();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateText() {
        CharSequence charSequence;
        ServiceState serviceState;
        int i = 0;
        List<SubscriptionInfo> filteredSubscriptionInfo = this.mKeyguardUpdateMonitor.getFilteredSubscriptionInfo(false);
        int size = filteredSubscriptionInfo.size();
        while (true) {
            if (i >= size) {
                charSequence = null;
                break;
            }
            int subscriptionId = filteredSubscriptionInfo.get(i).getSubscriptionId();
            int simState = this.mKeyguardUpdateMonitor.getSimState(subscriptionId);
            charSequence = filteredSubscriptionInfo.get(i).getCarrierName();
            if (!TextUtils.isEmpty(charSequence) && simState == 5 && (serviceState = this.mKeyguardUpdateMonitor.getServiceState(subscriptionId)) != null && serviceState.getState() == 0) {
                break;
            }
            i++;
        }
        setText(charSequence);
    }
}
