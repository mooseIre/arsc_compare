package com.android.systemui.statusbar.policy;

import android.util.SparseArray;

public class CallStateControllerImpl {
    private int mCallState;
    private SparseArray<Integer> mCallStateArray = new SparseArray<>();

    public int getCallState(int i) {
        return this.mCallStateArray.get(i, 0).intValue();
    }

    public int getCallState() {
        return this.mCallState;
    }

    public void setCallState(int i, int i2) {
        this.mCallStateArray.put(i, Integer.valueOf(i2));
    }
}
