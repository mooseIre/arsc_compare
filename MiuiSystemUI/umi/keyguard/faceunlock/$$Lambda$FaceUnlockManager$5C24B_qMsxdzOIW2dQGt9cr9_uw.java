package com.android.keyguard.faceunlock;

/* renamed from: com.android.keyguard.faceunlock.-$$Lambda$FaceUnlockManager$5C24B_qMsxdzOIW2dQGt9cr9_uw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FaceUnlockManager$5C24B_qMsxdzOIW2dQGt9cr9_uw implements Runnable {
    public static final /* synthetic */ $$Lambda$FaceUnlockManager$5C24B_qMsxdzOIW2dQGt9cr9_uw INSTANCE = new $$Lambda$FaceUnlockManager$5C24B_qMsxdzOIW2dQGt9cr9_uw();

    private /* synthetic */ $$Lambda$FaceUnlockManager$5C24B_qMsxdzOIW2dQGt9cr9_uw() {
    }

    public final void run() {
        FaceUnlockManager.sFaceUnlockManagerImpl.stopFaceUnlock();
    }
}
