package com.android.systemui.statusbar.policy;

public interface RotationLockController extends Listenable, CallbackController<RotationLockControllerCallback> {

    public interface RotationLockControllerCallback {
        void onRotationLockStateChanged(boolean z, boolean z2);
    }

    int getRotationLockOrientation();

    boolean isRotationLocked();

    void setRotationLocked(boolean z);
}
