package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.net.INetworkPolicyListener;
import android.net.NetworkPolicyManager;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import com.android.systemui.statusbar.policy.DataSaverController;
import java.util.ArrayList;

public class DataSaverControllerImpl implements DataSaverController {
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<DataSaverController.Listener> mListeners = new ArrayList<>();
    private final INetworkPolicyListener mPolicyListener = new NetworkPolicyManager.Listener() {
        /* class com.android.systemui.statusbar.policy.DataSaverControllerImpl.AnonymousClass1 */

        public void onRestrictBackgroundChanged(final boolean z) {
            DataSaverControllerImpl.this.mHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.policy.DataSaverControllerImpl.AnonymousClass1.AnonymousClass1 */

                public void run() {
                    DataSaverControllerImpl.this.handleRestrictBackgroundChanged(z);
                }
            });
        }
    };
    private final NetworkPolicyManager mPolicyManager;

    public DataSaverControllerImpl(Context context) {
        this.mPolicyManager = NetworkPolicyManager.from(context);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRestrictBackgroundChanged(boolean z) {
        synchronized (this.mListeners) {
            for (int i = 0; i < this.mListeners.size(); i++) {
                this.mListeners.get(i).onDataSaverChanged(z);
            }
        }
    }

    public void addCallback(DataSaverController.Listener listener) {
        synchronized (this.mListeners) {
            this.mListeners.add(listener);
            if (this.mListeners.size() == 1) {
                this.mPolicyManager.registerListener(this.mPolicyListener);
            }
        }
        listener.onDataSaverChanged(isDataSaverEnabled());
    }

    public void removeCallback(DataSaverController.Listener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
            if (this.mListeners.size() == 0) {
                this.mPolicyManager.unregisterListener(this.mPolicyListener);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController
    public boolean isDataSaverEnabled() {
        return this.mPolicyManager.getRestrictBackground();
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController
    public void setDataSaverEnabled(boolean z) {
        this.mPolicyManager.setRestrictBackground(z);
        try {
            this.mPolicyListener.onRestrictBackgroundChanged(z);
        } catch (RemoteException unused) {
        }
    }
}
