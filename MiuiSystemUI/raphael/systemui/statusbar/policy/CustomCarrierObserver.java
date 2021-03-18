package com.android.systemui.statusbar.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class CustomCarrierObserver {
    protected Handler mBgHandler;
    public final ArrayList<WeakReference<Callback>> mCallbacks = new ArrayList<>();
    protected Context mContext;
    protected String[] mCustomCarrier;
    protected Handler mMainHandler;
    protected volatile int mPhoneCount;

    public interface Callback {
        void onCustomCarrierChanged(String[] strArr);
    }

    public CustomCarrierObserver(Context context, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        this.mCustomCarrier = new String[this.mPhoneCount];
        final AnonymousClass1 r8 = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.statusbar.policy.CustomCarrierObserver.AnonymousClass1 */

            public void onChange(boolean z) {
                final String[] strArr = new String[CustomCarrierObserver.this.mPhoneCount];
                int i = CustomCarrierObserver.this.mPhoneCount;
                for (int i2 = 0; i2 < i; i2++) {
                    ContentResolver contentResolver = CustomCarrierObserver.this.mContext.getContentResolver();
                    strArr[i2] = MiuiSettings.System.getStringForUser(contentResolver, "status_bar_custom_carrier" + i2, -2);
                }
                CustomCarrierObserver.this.mMainHandler.post(new Runnable() {
                    /* class com.android.systemui.statusbar.policy.CustomCarrierObserver.AnonymousClass1.AnonymousClass1 */

                    public void run() {
                        CustomCarrierObserver.this.fireCustomCarrierTextChanged(strArr);
                    }
                });
            }
        };
        for (int i = 0; i < this.mPhoneCount; i++) {
            this.mCustomCarrier[i] = MiuiSettings.System.getStringForUser(this.mContext.getContentResolver(), "status_bar_custom_carrier" + i, -2);
            context.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_custom_carrier" + i), false, r8, -1);
        }
        this.mBgHandler.post(new Runnable(this) {
            /* class com.android.systemui.statusbar.policy.CustomCarrierObserver.AnonymousClass2 */

            public void run() {
                r8.onChange(false);
            }
        });
    }

    public void addCallback(Callback callback) {
        if (callback != null) {
            this.mCallbacks.add(new WeakReference<>(callback));
            callback.onCustomCarrierChanged(this.mCustomCarrier);
        }
    }

    /* access modifiers changed from: protected */
    public void fireCustomCarrierTextChanged(String[] strArr) {
        this.mCustomCarrier = strArr;
        synchronized (this.mCallbacks) {
            for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
                Callback callback = this.mCallbacks.get(size).get();
                if (callback != null) {
                    callback.onCustomCarrierChanged(strArr);
                }
            }
        }
    }
}
