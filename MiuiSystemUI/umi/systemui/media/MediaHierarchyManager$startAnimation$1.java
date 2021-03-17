package com.android.systemui.media;

/* compiled from: MediaHierarchyManager.kt */
final class MediaHierarchyManager$startAnimation$1 implements Runnable {
    final /* synthetic */ MediaHierarchyManager this$0;

    MediaHierarchyManager$startAnimation$1(MediaHierarchyManager mediaHierarchyManager) {
        this.this$0 = mediaHierarchyManager;
    }

    public final void run() {
        this.this$0.animator.start();
    }
}
