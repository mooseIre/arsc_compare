package com.android.systemui.statusbar.policy;

import android.util.SparseArray;

public class CallStateControllerImpl {
    private SparseArray<Integer> mCallStateArray = new SparseArray<>();

    public int getCallState(int i) {
        return this.mCallStateArray.get(i, 0).intValue();
    }
}
