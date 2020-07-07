package com.android.keyguard.faceunlock;

import android.content.Context;

public class FaceUnlockManagerNoSupport extends BaseFaceUnlockManager {
    public void deleteFeature(String str, FaceRemoveCallback faceRemoveCallback) {
    }

    public void initAll() {
    }

    public boolean isFaceUnlockInited() {
        return false;
    }

    public void resetFailCount() {
    }

    public void runOnFaceUnlockWorkerThread(Runnable runnable) {
    }

    public void startFaceUnlock(FaceUnlockCallback faceUnlockCallback) {
    }

    public void stopFaceUnlock() {
    }

    public void updateFaceUnlockType(int i) {
    }

    public FaceUnlockManagerNoSupport(Context context, int i) {
    }
}
