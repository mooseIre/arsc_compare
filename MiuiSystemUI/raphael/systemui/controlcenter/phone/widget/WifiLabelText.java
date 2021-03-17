package com.android.systemui.controlcenter.phone.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;

public class WifiLabelText extends TextView {
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                WifiLabelText.this.initCarrier();
            }
        }
    };
    /* access modifiers changed from: private */
    public String mCustomCarrier;
    private ContentObserver mCustomCarrierObserver;
    /* access modifiers changed from: private */
    public boolean mForceHide;
    /* access modifiers changed from: private */
    public String mRealCarrier = "";
    /* access modifiers changed from: private */
    public boolean mShowCarrier;
    private ContentObserver mShowCarrierObserver = new ContentObserver(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER))) {
        public void onChange(boolean z) {
            super.onChange(z);
            WifiLabelText wifiLabelText = WifiLabelText.this;
            boolean z2 = true;
            if (Settings.System.getIntForUser(wifiLabelText.mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
                z2 = false;
            }
            boolean unused = wifiLabelText.mShowCarrier = z2;
            WifiLabelText.this.updateCarrier();
        }
    };
    private boolean mSupportNetwork;

    public boolean isFocused() {
        return true;
    }

    public WifiLabelText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (ConnectivityManager.from(this.mContext).isNetworkSupported(0)) {
            this.mSupportNetwork = true;
            setText("");
            setVisibility(8);
            return;
        }
        this.mSupportNetwork = false;
        initCarrier();
        registerObservers();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private void registerObservers() {
        this.mCustomCarrierObserver = new ContentObserver(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER))) {
            public void onChange(boolean z) {
                super.onChange(z);
                WifiLabelText wifiLabelText = WifiLabelText.this;
                String unused = wifiLabelText.mCustomCarrier = MiuiSettings.System.getStringForUser(wifiLabelText.mContext.getContentResolver(), "status_bar_custom_carrier0", KeyguardUpdateMonitor.getCurrentUser());
                WifiLabelText.this.updateCarrier();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_custom_carrier0"), false, this.mCustomCarrierObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier_under_keyguard"), false, this.mShowCarrierObserver, -1);
        updateCarrier();
    }

    /* access modifiers changed from: private */
    public void initCarrier() {
        boolean z = true;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
            z = false;
        }
        this.mShowCarrier = z;
        this.mCustomCarrier = MiuiSettings.System.getStringForUser(this.mContext.getContentResolver(), "status_bar_custom_carrier0", KeyguardUpdateMonitor.getCurrentUser());
        updateCarrier();
    }

    public void unregisterObservers() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mCustomCarrierObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mShowCarrierObserver);
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.mSupportNetwork) {
            unregisterObservers();
        }
    }

    public void updateCarrier() {
        if (!this.mSupportNetwork) {
            post(new Runnable() {
                public void run() {
                    String str;
                    if (!TextUtils.isEmpty(WifiLabelText.this.mCustomCarrier)) {
                        str = WifiLabelText.this.mCustomCarrier;
                    } else {
                        str = WifiLabelText.this.mRealCarrier;
                    }
                    if (!TextUtils.isEmpty(str) && !str.equals(WifiLabelText.this.getText())) {
                        WifiLabelText.this.setText(str);
                    }
                    WifiLabelText wifiLabelText = WifiLabelText.this;
                    wifiLabelText.setVisibility((wifiLabelText.mForceHide || !WifiLabelText.this.mShowCarrier) ? 8 : 0);
                }
            });
        }
    }
}
