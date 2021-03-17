package com.android.systemui.fsgesture;

import android.view.WindowManagerPolicyConstants;

public class GesturesBackController implements WindowManagerPolicyConstants.PointerEventListener {
    static float convertOffset(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        return (float) (10.0d - (Math.sin((((double) ((Math.min(f, 360.0f) / 2.0f) + 90.0f)) * 3.141592653589793d) / 180.0d) * 10.0d));
    }

    static boolean isFinished(float f, int i) {
        return f >= 0.0f && ((Math.min(f, 360.0f) / 2.0f) + 90.0f > 180.0f || i > 2);
    }
}
