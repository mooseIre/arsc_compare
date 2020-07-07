package com.android.keyguard.faceunlock;

public abstract class BaseFaceUnlockManager {
    public abstract void deleteFeature(String str, FaceRemoveCallback faceRemoveCallback);

    public abstract void initAll();

    public abstract boolean isFaceUnlockInited();

    public abstract void resetFailCount();

    public abstract void runOnFaceUnlockWorkerThread(Runnable runnable);

    public abstract void startFaceUnlock(FaceUnlockCallback faceUnlockCallback);

    public abstract void stopFaceUnlock();

    public abstract void updateFaceUnlockType(int i);
}
