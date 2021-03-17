package com.android.systemui.accessibility;

import android.os.IBinder;

public abstract class MirrorWindowControl {

    public interface MirrorWindowDelegate {
    }

    public final void destroyControl() {
        throw null;
    }

    public abstract void setWindowDelegate(MirrorWindowDelegate mirrorWindowDelegate);

    public final void showControl(IBinder iBinder) {
        throw null;
    }
}
