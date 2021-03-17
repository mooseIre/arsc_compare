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

    /* JADX WARN: Incorrect args count in method signature: ()V */
    ToggleRangeBehavior$bind$1(ToggleRangeBehavior toggleRangeBehavior) {
        this.this$0 = toggleRangeBehavior;
    }

    public void onInitializeAccessibilityNodeInfo(@NotNull View view, @NotNull AccessibilityNodeInfo accessibilityNodeInfo) {
        Intrinsics.checkParameterIsNotNull(view, "host");
        Intrinsics.checkParameterIsNotNull(accessibilityNodeInfo, "info");
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        int i = 0;
        float f = this.this$0.levelToRangeValue(0);
        ToggleRangeBehavior toggleRangeBehavior = this.this$0;
        float f2 = toggleRangeBehavior.levelToRangeValue(toggleRangeBehavior.getClipLayer().getLevel());
        float f3 = this.this$0.levelToRangeValue(10000);
        double stepValue = (double) this.this$0.getRangeTemplate().getStepValue();
        if (stepValue != Math.floor(stepValue)) {
            i = 1;
        }
        if (this.this$0.isChecked()) {
            accessibilityNodeInfo.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(i, f, f3, f2));
        }
        accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_PROGRESS);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x007c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean performAccessibilityAction(@org.jetbrains.annotations.NotNull android.view.View r7, int r8, @org.jetbrains.annotations.Nullable android.os.Bundle r9) {
        /*
        // Method dump skipped, instructions count: 132
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ToggleRangeBehavior$bind$1.performAccessibilityAction(android.view.View, int, android.os.Bundle):boolean");
    }
}
