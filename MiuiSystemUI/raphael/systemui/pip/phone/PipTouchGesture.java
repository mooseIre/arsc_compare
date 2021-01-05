package com.android.systemui.pip.phone;

public abstract class PipTouchGesture {
    public abstract void onDown(PipTouchState pipTouchState);

    public abstract boolean onMove(PipTouchState pipTouchState);

    public abstract boolean onUp(PipTouchState pipTouchState);
}
