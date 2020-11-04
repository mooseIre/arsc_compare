package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.FiveGController;
import com.android.systemui.statusbar.policy.FiveGServiceClient;
import com.miui.systemui.annotation.Inject;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FiveGControllerImpl implements FiveGController {
    private final Context mContext;
    private FiveGServiceClient mFiveGServiceClient;
    /* access modifiers changed from: private */
    public FiveGServiceClient.FiveGServiceState[] mFiveGServiceStates;
    /* access modifiers changed from: private */
    public ArrayList<FiveGController.FiveGStateChangeCallback> mFiveGStateChangeCallbacks = new ArrayList<>();
    private FiveGStateListener[] mFiveGStateListeners;

    public boolean isDataRegisteredOnLte(int i) {
        return i == 13 || i == 19;
    }

    public FiveGControllerImpl(@Inject Context context) {
        this.mContext = context;
        this.mFiveGServiceClient = new FiveGServiceClient(this.mContext);
        this.mFiveGServiceStates = new FiveGServiceClient.FiveGServiceState[2];
        this.mFiveGStateListeners = new FiveGStateListener[2];
        for (int i = 0; i < 2; i++) {
            this.mFiveGStateListeners[i] = new FiveGStateListener();
            this.mFiveGStateListeners[i].mSlot = i;
            this.mFiveGServiceStates[i] = new FiveGServiceClient.FiveGServiceState();
            registerFiveGStateListener(i);
        }
    }

    public void registerFiveGStateListener(int i) {
        this.mFiveGServiceClient.registerListener(i, this.mFiveGStateListeners[i]);
    }

    public void addCallback(FiveGController.FiveGStateChangeCallback fiveGStateChangeCallback) {
        synchronized (this.mFiveGStateChangeCallbacks) {
            this.mFiveGStateChangeCallbacks.add(fiveGStateChangeCallback);
        }
        fiveGStateChangeCallback.onSignalStrengthChanged(this.mFiveGServiceStates[fiveGStateChangeCallback.getSlot()].getSignalLevel(), this.mFiveGServiceStates[fiveGStateChangeCallback.getSlot()].getIconGroup());
    }

    public void removeCallback(FiveGController.FiveGStateChangeCallback fiveGStateChangeCallback) {
        synchronized (this.mFiveGStateChangeCallbacks) {
            this.mFiveGStateChangeCallbacks.remove(fiveGStateChangeCallback);
        }
    }

    public boolean isFiveGConnect(int i, int i2) {
        return this.mFiveGServiceStates[i].isConnectedOnSaMode() || (this.mFiveGServiceStates[i].isConnectedOnNsaMode() && (isDataRegisteredOnLte(i2) || MobileSignalController.isCalling(MobileSignalController.getOtherSlotId(i))));
    }

    public boolean isConnectedOnSaMode(int i) {
        return this.mFiveGServiceStates[i].isConnectedOnSaMode();
    }

    public boolean isFiveGBearerAllocated(int i) {
        return this.mFiveGServiceStates[i].getAllocated() > 0;
    }

    public int getFiveGDrawable(int i) {
        if (this.mFiveGServiceStates[i].getIconGroup() == TelephonyIcons.FIVE_G_KR_ON) {
            return R.drawable.signal_5g_on;
        }
        if (this.mFiveGServiceStates[i].getIconGroup() == TelephonyIcons.FIVE_G_KR_OFF) {
            return R.drawable.signal_5g_off;
        }
        return 0;
    }

    public void dump(PrintWriter printWriter) {
        this.mFiveGServiceClient.dump(printWriter);
    }

    class FiveGStateListener implements FiveGServiceClient.IFiveGStateListener {
        int mSlot;

        FiveGStateListener() {
        }

        public void onStateChanged(FiveGServiceClient.FiveGServiceState fiveGServiceState) {
            if (fiveGServiceState != null) {
                FiveGControllerImpl.this.mFiveGServiceStates[this.mSlot] = fiveGServiceState;
                int size = FiveGControllerImpl.this.mFiveGStateChangeCallbacks.size();
                for (int i = 0; i < size; i++) {
                    FiveGController.FiveGStateChangeCallback fiveGStateChangeCallback = (FiveGController.FiveGStateChangeCallback) FiveGControllerImpl.this.mFiveGStateChangeCallbacks.get(i);
                    if (fiveGStateChangeCallback != null && this.mSlot == fiveGStateChangeCallback.getSlot()) {
                        fiveGStateChangeCallback.onSignalStrengthChanged(fiveGServiceState.getSignalLevel(), fiveGServiceState.getIconGroup());
                    }
                }
            }
        }
    }
}
