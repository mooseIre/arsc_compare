package com.android.systemui.util.magnetictarget;

import com.android.systemui.util.magnetictarget.MagnetizedObject;

/* access modifiers changed from: package-private */
/* compiled from: MagnetizedObject.kt */
public final class MagnetizedObject$MagneticTarget$updateLocationOnScreen$1 implements Runnable {
    final /* synthetic */ MagnetizedObject.MagneticTarget this$0;

    MagnetizedObject$MagneticTarget$updateLocationOnScreen$1(MagnetizedObject.MagneticTarget magneticTarget) {
        this.this$0 = magneticTarget;
    }

    public final void run() {
        this.this$0.getTargetView().getLocationOnScreen(this.this$0.tempLoc);
        this.this$0.getCenterOnScreen().set((((float) this.this$0.tempLoc[0]) + (((float) this.this$0.getTargetView().getWidth()) / 2.0f)) - this.this$0.getTargetView().getTranslationX(), (((float) this.this$0.tempLoc[1]) + (((float) this.this$0.getTargetView().getHeight()) / 2.0f)) - this.this$0.getTargetView().getTranslationY());
    }
}
