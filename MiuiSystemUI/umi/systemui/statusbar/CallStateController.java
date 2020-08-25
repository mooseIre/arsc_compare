package com.android.systemui.statusbar;

public interface CallStateController {
    int getCallState();

    boolean isMsim();

    void setCallState(int i, int i2);

    void setCallState(String str);

    void setSimCount(int i);
}
