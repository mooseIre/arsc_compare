package com.android.systemui.volume;

import android.view.MotionEvent;
import android.view.View;

public class Interaction {

    public interface Callback {
        void onInteraction();
    }

    public static void register(View view, final Callback callback) {
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Callback.this.onInteraction();
                return false;
            }
        });
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            public boolean onGenericMotion(View view, MotionEvent motionEvent) {
                Callback.this.onInteraction();
                return false;
            }
        });
    }
}
