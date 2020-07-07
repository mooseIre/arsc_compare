package com.android.keyguard.faceunlock;

/* renamed from: com.android.keyguard.faceunlock.-$$Lambda$FaceUnlockManager$4K9BiyHSFxhXgOZApIQ6QOpJZao  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$FaceUnlockManager$4K9BiyHSFxhXgOZApIQ6QOpJZao implements Runnable {
    public static final /* synthetic */ $$Lambda$FaceUnlockManager$4K9BiyHSFxhXgOZApIQ6QOpJZao INSTANCE = new $$Lambda$FaceUnlockManager$4K9BiyHSFxhXgOZApIQ6QOpJZao();

    private /* synthetic */ $$Lambda$FaceUnlockManager$4K9BiyHSFxhXgOZApIQ6QOpJZao() {
    }

    public final void run() {
        FaceUnlockManager.sFaceUnlockManagerImpl.initAll();
    }
}
