package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import java.util.List;

public class EmergencyCryptkeeperText extends TextView {
    private final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() {
        public void onPhoneStateChanged(int i) {
            EmergencyCryptkeeperText.this.update();
        }

        public void onRefreshCarrierInfo() {
            EmergencyCryptkeeperText.this.update();
        }
    };
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.AIRPLANE_MODE".equals(intent.getAction())) {
                EmergencyCryptkeeperText.this.update();
            }
        }
    };

    public EmergencyCryptkeeperText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mKeyguardUpdateMonitor = instance;
        instance.registerCallback(this.mCallback);
        getContext().registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.AIRPLANE_MODE"));
        update();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateMonitor;
        if (keyguardUpdateMonitor != null) {
            keyguardUpdateMonitor.removeCallback(this.mCallback);
        }
        getContext().unregisterReceiver(this.mReceiver);
    }

    public void update() {
        int i = 0;
        boolean isNetworkSupported = ConnectivityManager.from(this.mContext).isNetworkSupported(0);
        boolean z = true;
        boolean z2 = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 1;
        if (!isNetworkSupported || z2) {
            setText((CharSequence) null);
            setVisibility(8);
            return;
        }
        List<SubscriptionInfo> subscriptionInfo = this.mKeyguardUpdateMonitor.getSubscriptionInfo(false);
        int size = subscriptionInfo.size();
        CharSequence charSequence = null;
        for (int i2 = 0; i2 < size; i2++) {
            IccCardConstants.State simState = this.mKeyguardUpdateMonitor.getSimState(subscriptionInfo.get(i2).getSimSlotIndex());
            CharSequence carrierName = subscriptionInfo.get(i2).getCarrierName();
            if (simState.iccCardExist() && !TextUtils.isEmpty(carrierName)) {
                z = false;
                charSequence = carrierName;
            }
        }
        if (z) {
            if (size != 0) {
                charSequence = subscriptionInfo.get(0).getCarrierName();
            } else {
                charSequence = getContext().getText(17040096);
                Intent registerReceiver = getContext().registerReceiver((BroadcastReceiver) null, new IntentFilter("android.provider.Telephony.SPN_STRINGS_UPDATED"));
                if (registerReceiver != null) {
                    charSequence = registerReceiver.getStringExtra("plmn");
                }
            }
        }
        setText(charSequence);
        if (TextUtils.isEmpty(charSequence)) {
            i = 8;
        }
        setVisibility(i);
    }
}
