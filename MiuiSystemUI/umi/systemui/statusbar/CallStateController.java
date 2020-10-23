package com.android.systemui.statusbar;

public interface CallStateController {
    int getCallState();

    int getCallState(int i);

    boolean isMsim();

    void setCallState(int i, int i2);

    void setCallState(String str);

    void setSimCount(int i);
}
