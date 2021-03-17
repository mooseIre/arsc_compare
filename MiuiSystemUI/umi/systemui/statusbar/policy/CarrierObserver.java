package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.systemui.C0021R$string;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import miui.telephony.SubscriptionManager;

public class CarrierObserver {
    protected Handler mBgHandler;
    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.CarrierObserver.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            final int intExtra = intent.getIntExtra(SubscriptionManager.SLOT_KEY, SubscriptionManager.INVALID_SLOT_ID);
            if (intExtra != SubscriptionManager.INVALID_SLOT_ID) {
                final String str = null;
                if (VirtualSimUtils.isVirtualSim(context, intExtra)) {
                    str = VirtualSimUtils.getVirtualSimCarrierName(context);
                }
                if (TextUtils.isEmpty(str)) {
                    boolean booleanExtra = intent.getBooleanExtra("android.telephony.extra.SHOW_PLMN", false);
                    boolean booleanExtra2 = intent.getBooleanExtra("android.telephony.extra.SHOW_SPN", false);
                    String stringExtra = intent.getStringExtra("android.telephony.extra.PLMN");
                    String stringExtra2 = intent.getStringExtra("android.telephony.extra.SPN");
                    if (!booleanExtra || stringExtra == null) {
                        if (booleanExtra2 && stringExtra2 != null) {
                            str = stringExtra2;
                        }
                    } else if (!booleanExtra2 || stringExtra2 == null || Objects.equals(stringExtra2, stringExtra)) {
                        str = stringExtra;
                    } else {
                        str = stringExtra + CarrierObserver.this.mNetworkNameSeparator + stringExtra2;
                    }
                }
                if (intExtra < CarrierObserver.this.mPhoneCount) {
                    CarrierObserver.this.mMainHandler.post(new Runnable() {
                        /* class com.android.systemui.statusbar.policy.CarrierObserver.AnonymousClass1.AnonymousClass1 */

                        public void run() {
                            CarrierObserver.this.fireCarrierTextChanged(intExtra, str);
                        }
                    });
                }
            }
        }
    };
    public final ArrayList<WeakReference<Callback>> mCallbacks = new ArrayList<>();
    protected String[] mCarriers;
    protected Context mContext;
    protected Handler mMainHandler;
    protected String mNetworkNameSeparator;
    protected volatile int mPhoneCount;

    public interface Callback {
        void onCarrierChanged(String[] strArr);
    }

    public CarrierObserver(Context context, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mCarriers = new String[this.mPhoneCount];
        this.mNetworkNameSeparator = context.getString(C0021R$string.status_bar_network_name_separator);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.telephony.action.SERVICE_PROVIDERS_UPDATED");
        for (int i = 1; i < this.mPhoneCount; i++) {
            intentFilter.addAction("android.telephony.action.SERVICE_PROVIDERS_UPDATED" + i);
        }
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter, null, this.mBgHandler);
    }

    public void addCallback(Callback callback) {
        if (callback != null) {
            this.mCallbacks.add(new WeakReference<>(callback));
            callback.onCarrierChanged(this.mCarriers);
        }
    }

    /* access modifiers changed from: protected */
    public void fireCarrierTextChanged(int i, String str) {
        if (i >= 0) {
            String[] strArr = this.mCarriers;
            if (i < strArr.length) {
                strArr[i] = str;
                for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
                    Callback callback = this.mCallbacks.get(size).get();
                    if (callback != null) {
                        callback.onCarrierChanged(this.mCarriers);
                    }
                }
            }
        }
    }
}
