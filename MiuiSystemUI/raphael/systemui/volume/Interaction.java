package com.android.systemui.volume;

import android.view.MotionEvent;
import android.view.View;

public class Interaction {

    public interface Callback {
        void onInteraction();
    }

    public static void register(View view, final Callback callback) {
        view.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.volume.Interaction.AnonymousClass1 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                Callback.this.onInteraction();
                return false;
            }
        });
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            /* class com.android.systemui.volume.Interaction.AnonymousClass2 */

            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                Callback.this.onInteraction();
                return false;
            }
        });
    }
}
