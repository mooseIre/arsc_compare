package com.android.systemui.pip.phone;

public abstract class PipTouchGesture {
    /* access modifiers changed from: package-private */
    public abstract void onDown(PipTouchState pipTouchState);

    /* access modifiers changed from: package-private */
    public abstract boolean onMove(PipTouchState pipTouchState);

    /* access modifiers changed from: package-private */
    public abstract boolean onUp(PipTouchState pipTouchState);
}
