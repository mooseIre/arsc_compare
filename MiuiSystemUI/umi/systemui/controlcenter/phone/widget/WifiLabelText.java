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
        /* class com.android.systemui.controlcenter.phone.widget.WifiLabelText.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                WifiLabelText.this.initCarrier();
            }
        }
    };
    private String mCustomCarrier;
    private ContentObserver mCustomCarrierObserver;
    private boolean mForceHide;
    private String mRealCarrier = "";
    private boolean mShowCarrier;
    private ContentObserver mShowCarrierObserver = new ContentObserver(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER))) {
        /* class com.android.systemui.controlcenter.phone.widget.WifiLabelText.AnonymousClass2 */

        public void onChange(boolean z) {
            super.onChange(z);
            WifiLabelText wifiLabelText = WifiLabelText.this;
            boolean z2 = true;
            if (Settings.System.getIntForUser(((TextView) wifiLabelText).mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
                z2 = false;
            }
            wifiLabelText.mShowCarrier = z2;
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
        if (ConnectivityManager.from(((TextView) this).mContext).isNetworkSupported(0)) {
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
        ((TextView) this).mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    private void registerObservers() {
        this.mCustomCarrierObserver = new ContentObserver(new Handler((Looper) Dependency.get(Dependency.BG_LOOPER))) {
            /* class com.android.systemui.controlcenter.phone.widget.WifiLabelText.AnonymousClass3 */

            public void onChange(boolean z) {
                super.onChange(z);
                WifiLabelText wifiLabelText = WifiLabelText.this;
                wifiLabelText.mCustomCarrier = MiuiSettings.System.getStringForUser(((TextView) wifiLabelText).mContext.getContentResolver(), "status_bar_custom_carrier0", KeyguardUpdateMonitor.getCurrentUser());
                WifiLabelText.this.updateCarrier();
            }
        };
        ((TextView) this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_custom_carrier0"), false, this.mCustomCarrierObserver, -1);
        ((TextView) this).mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_carrier_under_keyguard"), false, this.mShowCarrierObserver, -1);
        updateCarrier();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initCarrier() {
        boolean z = true;
        if (Settings.System.getIntForUser(((TextView) this).mContext.getContentResolver(), "status_bar_show_carrier_under_keyguard", 1, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
            z = false;
        }
        this.mShowCarrier = z;
        this.mCustomCarrier = MiuiSettings.System.getStringForUser(((TextView) this).mContext.getContentResolver(), "status_bar_custom_carrier0", KeyguardUpdateMonitor.getCurrentUser());
        updateCarrier();
    }

    public void unregisterObservers() {
        ((TextView) this).mContext.getContentResolver().unregisterContentObserver(this.mCustomCarrierObserver);
        ((TextView) this).mContext.getContentResolver().unregisterContentObserver(this.mShowCarrierObserver);
        ((TextView) this).mContext.unregisterReceiver(this.mBroadcastReceiver);
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
                /* class com.android.systemui.controlcenter.phone.widget.WifiLabelText.AnonymousClass4 */

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
