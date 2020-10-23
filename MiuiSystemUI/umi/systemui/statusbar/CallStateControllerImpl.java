package com.android.systemui.statusbar;

import android.telephony.TelephonyManager;
import android.util.SparseArray;

public class CallStateControllerImpl implements CallStateController {
    private int mCallState;
    private SparseArray<Integer> mCallStateArray = new SparseArray<>();
    private int mSimCount;

    public int getCallState(int i) {
        return this.mCallStateArray.get(i, 0).intValue();
    }

    public int getCallState() {
        return this.mCallState;
    }

    public void setCallState(int i, int i2) {
        this.mCallStateArray.put(i, Integer.valueOf(i2));
    }

    public void setCallState(String str) {
        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(str)) {
            this.mCallState = 2;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(str)) {
            this.mCallState = 1;
        } else {
            this.mCallState = 0;
        }
    }

    public void setSimCount(int i) {
        this.mSimCount = i;
    }

    public boolean isMsim() {
        return this.mSimCount == 2;
    }
}
