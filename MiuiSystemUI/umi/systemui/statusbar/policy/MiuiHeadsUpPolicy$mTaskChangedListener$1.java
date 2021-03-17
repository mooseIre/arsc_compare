package com.android.systemui.statusbar.policy;

import com.android.systemui.shared.system.TaskStackChangeListener;

/* compiled from: MiuiHeadsUpPolicy.kt */
public final class MiuiHeadsUpPolicy$mTaskChangedListener$1 extends TaskStackChangeListener {
    final /* synthetic */ MiuiHeadsUpPolicy this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiHeadsUpPolicy$mTaskChangedListener$1(MiuiHeadsUpPolicy miuiHeadsUpPolicy) {
        this.this$0 = miuiHeadsUpPolicy;
    }

    @Override // com.android.systemui.shared.system.TaskStackChangeListener
    public void onTaskMovedToFront(int i) {
        super.onTaskMovedToFront(i);
        this.this$0.releaseHeadsUps();
    }
}
