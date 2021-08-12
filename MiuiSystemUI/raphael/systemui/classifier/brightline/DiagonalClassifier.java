package com.android.systemui.classifier.brightline;

import com.android.systemui.util.DeviceConfigProxy;

/* access modifiers changed from: package-private */
public class DiagonalClassifier extends FalsingClassifier {
    private final float mHorizontalAngleRange;
    private final float mVerticalAngleRange;

    private float normalizeAngle(float f) {
        return f < 0.0f ? (f % 6.2831855f) + 6.2831855f : f > 6.2831855f ? f % 6.2831855f : f;
    }

    DiagonalClassifier(FalsingDataProvider falsingDataProvider, DeviceConfigProxy deviceConfigProxy) {
        super(falsingDataProvider);
        this.mHorizontalAngleRange = deviceConfigProxy.getFloat("systemui", "brightline_falsing_diagonal_horizontal_angle_range", 0.08726646f);
        this.mVerticalAngleRange = deviceConfigProxy.getFloat("systemui", "brightline_falsing_diagonal_horizontal_angle_range", 0.08726646f);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public boolean isFalseTouch() {
        float angle = getAngle();
        if (angle == Float.MAX_VALUE || getInteractionType() == 5 || getInteractionType() == 6) {
            return false;
        }
        float f = this.mHorizontalAngleRange;
        float f2 = 0.7853982f - f;
        float f3 = f + 0.7853982f;
        if (isVertical()) {
            float f4 = this.mVerticalAngleRange;
            f2 = 0.7853982f - f4;
            f3 = f4 + 0.7853982f;
        }
        if (angleBetween(angle, f2, f3) || angleBetween(angle, f2 + 1.5707964f, f3 + 1.5707964f) || angleBetween(angle, f2 - 1.5707964f, f3 - 1.5707964f) || angleBetween(angle, f2 + 3.1415927f, f3 + 3.1415927f)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public String getReason() {
        return String.format(null, "{angle=%f, vertical=%s}", Float.valueOf(getAngle()), Boolean.valueOf(isVertical()));
    }

    private boolean angleBetween(float f, float f2, float f3) {
        float normalizeAngle = normalizeAngle(f2);
        float normalizeAngle2 = normalizeAngle(f3);
        return normalizeAngle > normalizeAngle2 ? f >= normalizeAngle || f <= normalizeAngle2 : f >= normalizeAngle && f <= normalizeAngle2;
    }
}
