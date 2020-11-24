package com.android.keyguard.faceunlock;

public class FaceUnlockCallback {
    public abstract void onFaceAuthFailed();

    public abstract void onFaceAuthHelp(int i);

    public abstract void onFaceAuthLocked();

    public abstract void onFaceAuthTimeOut(boolean z);

    public abstract void onFaceAuthenticated();

    public void onFaceEnableChange(boolean z, boolean z2) {
    }

    public void unblockScreenOn() {
    }
}
