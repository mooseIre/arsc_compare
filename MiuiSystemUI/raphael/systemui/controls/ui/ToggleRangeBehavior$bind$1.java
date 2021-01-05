package com.android.systemui.controls.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ToggleRangeBehavior.kt */
public final class ToggleRangeBehavior$bind$1 extends View.AccessibilityDelegate {
    final /* synthetic */ ToggleRangeBehavior this$0;

    public boolean onRequestSendAccessibilityEvent(@NotNull ViewGroup viewGroup, @NotNull View view, @NotNull AccessibilityEvent accessibilityEvent) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "host");
        Intrinsics.checkParameterIsNotNull(view, "child");
        Intrinsics.checkParameterIsNotNull(accessibilityEvent, "event");
        return true;
    }

    ToggleRangeBehavior$bind$1(ToggleRangeBehavior toggleRangeBehavior) {
        this.this$0 = toggleRangeBehavior;
    }

    public void onInitializeAccessibilityNodeInfo(@NotNull View view, @NotNull AccessibilityNodeInfo accessibilityNodeInfo) {
        Intrinsics.checkParameterIsNotNull(view, "host");
        Intrinsics.checkParameterIsNotNull(accessibilityNodeInfo, "info");
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        int i = 0;
        float access$levelToRangeValue = this.this$0.levelToRangeValue(0);
        ToggleRangeBehavior toggleRangeBehavior = this.this$0;
        float access$levelToRangeValue2 = toggleRangeBehavior.levelToRangeValue(toggleRangeBehavior.getClipLayer().getLevel());
        float access$levelToRangeValue3 = this.this$0.levelToRangeValue(10000);
        double stepValue = (double) this.this$0.getRangeTemplate().getStepValue();
        if (stepValue != Math.floor(stepValue)) {
            i = 1;
        }
        if (this.this$0.isChecked()) {
            accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(i, access$levelToRangeValue, access$levelToRangeValue3, access$levelToRangeValue2));
        }
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:21:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean performAccessibilityAction(@org.jetbrains.annotations.NotNull android.view.View r7, int r8, @org.jetbrains.annotations.Nullable android.os.Bundle r9) {
        /*
            r6 = this;
            java.lang.String r0 = "host"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r7, r0)
            r0 = 0
            r1 = 1
            r2 = 16
            if (r8 != r2) goto L_0x0035
            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
            boolean r2 = r2.isToggleable()
            if (r2 != 0) goto L_0x0015
        L_0x0013:
            r2 = r0
            goto L_0x007a
        L_0x0015:
            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
            com.android.systemui.controls.ui.ControlViewHolder r2 = r2.getCvh()
            com.android.systemui.controls.ui.ControlActionCoordinator r2 = r2.getControlActionCoordinator()
            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
            com.android.systemui.controls.ui.ControlViewHolder r3 = r3.getCvh()
            com.android.systemui.controls.ui.ToggleRangeBehavior r4 = r6.this$0
            java.lang.String r4 = r4.getTemplateId()
            com.android.systemui.controls.ui.ToggleRangeBehavior r5 = r6.this$0
            boolean r5 = r5.isChecked()
            r2.toggle(r3, r4, r5)
            goto L_0x004c
        L_0x0035:
            r2 = 32
            if (r8 != r2) goto L_0x004e
            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
            com.android.systemui.controls.ui.ControlViewHolder r2 = r2.getCvh()
            com.android.systemui.controls.ui.ControlActionCoordinator r2 = r2.getControlActionCoordinator()
            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
            com.android.systemui.controls.ui.ControlViewHolder r3 = r3.getCvh()
            r2.longPress(r3)
        L_0x004c:
            r2 = r1
            goto L_0x007a
        L_0x004e:
            android.view.accessibility.AccessibilityNodeInfo$AccessibilityAction r2 = android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS
            int r2 = r2.getId()
            if (r8 != r2) goto L_0x0013
            if (r9 == 0) goto L_0x0013
            java.lang.String r2 = "android.view.accessibility.action.ARGUMENT_PROGRESS_VALUE"
            boolean r3 = r9.containsKey(r2)
            if (r3 != 0) goto L_0x0061
            goto L_0x0013
        L_0x0061:
            float r2 = r9.getFloat(r2)
            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
            int r2 = r3.rangeToLevelValue(r2)
            com.android.systemui.controls.ui.ToggleRangeBehavior r3 = r6.this$0
            boolean r4 = r3.isChecked()
            r3.updateRange(r2, r4, r1)
            com.android.systemui.controls.ui.ToggleRangeBehavior r2 = r6.this$0
            r2.endUpdateRange()
            goto L_0x004c
        L_0x007a:
            if (r2 != 0) goto L_0x0082
            boolean r6 = super.performAccessibilityAction(r7, r8, r9)
            if (r6 == 0) goto L_0x0083
        L_0x0082:
            r0 = r1
        L_0x0083:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ToggleRangeBehavior$bind$1.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean");
    }
}
