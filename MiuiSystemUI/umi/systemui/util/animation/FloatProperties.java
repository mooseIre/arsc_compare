package com.android.systemui.util.animation;

import android.graphics.Rect;
import androidx.dynamicanimation.animation.FloatPropertyCompat;

public class FloatProperties {
    public static FloatPropertyCompat<Rect> RECT_X = new FloatPropertyCompat<Rect>("RectX") {
        public void setValue(Rect rect, float f) {
            if (rect != null) {
                rect.offsetTo((int) f, rect.top);
            }
        }

        public float getValue(Rect rect) {
            if (rect != null) {
                return (float) rect.left;
            }
            return 0.0f;
        }
    };
    public static FloatPropertyCompat<Rect> RECT_Y = new FloatPropertyCompat<Rect>("RectY") {
        public void setValue(Rect rect, float f) {
            if (rect != null) {
                rect.offsetTo(rect.left, (int) f);
            }
        }

        public float getValue(Rect rect) {
            if (rect != null) {
                return (float) rect.top;
            }
            return 0.0f;
        }
    };
}
