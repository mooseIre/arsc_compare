package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.statusbar.policy.MiuiFiveGServiceClient;
import com.android.systemui.statusbar.policy.MobileSignalController;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FiveGControllerImpl {
    private final Context mContext;
    private MiuiFiveGServiceClient mFiveGServiceClient;
    private MiuiFiveGServiceClient.FiveGServiceState[] mFiveGServiceStates;
    private ArrayList<FiveGStateChangeCallback> mFiveGStateChangeCallbacks = new ArrayList<>();
    private FiveGStateListener[] mFiveGStateListeners;

    /* access modifiers changed from: package-private */
    public interface FiveGStateChangeCallback {
        int getSlot();

        void onSignalStrengthChanged(int i, MobileSignalController.MobileIconGroup mobileIconGroup);
    }

    public boolean isDataRegisteredOnLte(int i) {
        return i == 13 || i == 19;
    }

    public FiveGControllerImpl(Context context) {
        this.mContext = context;
        this.mFiveGServiceClient = new MiuiFiveGServiceClient(this.mContext);
        this.mFiveGServiceStates = new MiuiFiveGServiceClient.FiveGServiceState[2];
        this.mFiveGStateListeners = new FiveGStateListener[2];
        for (int i = 0; i < 2; i++) {
            this.mFiveGStateListeners[i] = new FiveGStateListener();
            this.mFiveGStateListeners[i].mSlot = i;
            this.mFiveGServiceStates[i] = new MiuiFiveGServiceClient.FiveGServiceState();
            registerFiveGStateListener(i);
        }
    }

    public void registerFiveGStateListener(int i) {
        this.mFiveGServiceClient.registerListener(i, this.mFiveGStateListeners[i]);
    }

    public void addCallback(FiveGStateChangeCallback fiveGStateChangeCallback) {
        synchronized (this.mFiveGStateChangeCallbacks) {
            this.mFiveGStateChangeCallbacks.add(fiveGStateChangeCallback);
        }
        fiveGStateChangeCallback.onSignalStrengthChanged(this.mFiveGServiceStates[fiveGStateChangeCallback.getSlot()].getSignalLevel(), this.mFiveGServiceStates[fiveGStateChangeCallback.getSlot()].getIconGroup());
        this.mFiveGServiceClient.addMobileSignalController((MobileSignalController) fiveGStateChangeCallback);
    }

    public void removeCallback(FiveGStateChangeCallback fiveGStateChangeCallback) {
        synchronized (this.mFiveGStateChangeCallbacks) {
            this.mFiveGStateChangeCallbacks.remove(fiveGStateChangeCallback);
        }
        this.mFiveGServiceClient.removeMobileSignalController((MobileSignalController) fiveGStateChangeCallback);
    }

    public boolean isFiveGConnect(int i, int i2) {
        return this.mFiveGServiceStates[i].isConnectedOnSaMode() || (this.mFiveGServiceStates[i].isConnectedOnNsaMode() && (isDataRegisteredOnLte(i2) || MobileSignalController.isCalling(MobileSignalController.getOtherSlotId(i))));
    }

    public boolean isConnectedOnSaMode(int i) {
        return this.mFiveGServiceStates[i].isConnectedOnSaMode();
    }

    public int getFiveGDrawable(int i) {
        if (this.mFiveGServiceStates[i].getIconGroup() == TelephonyIcons.FIVE_G_KR_ON) {
            return C0013R$drawable.signal_5g_on;
        }
        if (this.mFiveGServiceStates[i].getIconGroup() == TelephonyIcons.FIVE_G_KR_OFF) {
            return C0013R$drawable.signal_5g_off;
        }
        return 0;
    }

    public void dump(PrintWriter printWriter) {
        this.mFiveGServiceClient.dump(printWriter);
    }

    /* access modifiers changed from: package-private */
    public class FiveGStateListener implements MiuiFiveGServiceClient.IFiveGStateListener {
        int mSlot;

        FiveGStateListener() {
        }

        @Override // com.android.systemui.statusbar.policy.MiuiFiveGServiceClient.IFiveGStateListener
        public void onStateChanged(MiuiFiveGServiceClient.FiveGServiceState fiveGServiceState) {
            if (fiveGServiceState != null) {
                FiveGControllerImpl.this.mFiveGServiceStates[this.mSlot] = fiveGServiceState;
                int size = FiveGControllerImpl.this.mFiveGStateChangeCallbacks.size();
                for (int i = 0; i < size; i++) {
                    FiveGStateChangeCallback fiveGStateChangeCallback = (FiveGStateChangeCallback) FiveGControllerImpl.this.mFiveGStateChangeCallbacks.get(i);
                    if (fiveGStateChangeCallback != null && this.mSlot == fiveGStateChangeCallback.getSlot()) {
                        fiveGStateChangeCallback.onSignalStrengthChanged(fiveGServiceState.getSignalLevel(), fiveGServiceState.getIconGroup());
                    }
                }
            }
        }
    }
}
