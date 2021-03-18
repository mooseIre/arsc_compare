package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.C0015R$id;

public class EmergencyCarrierArea extends AlphaOptimizedLinearLayout {
    private CarrierText mCarrierText;
    private EmergencyButton mEmergencyButton;

    public EmergencyCarrierArea(Context context) {
        super(context);
    }

    public EmergencyCarrierArea(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCarrierText = (CarrierText) findViewById(C0015R$id.carrier_text);
        EmergencyButton emergencyButton = (EmergencyButton) findViewById(C0015R$id.emergency_call_button);
        this.mEmergencyButton = emergencyButton;
        emergencyButton.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.keyguard.EmergencyCarrierArea.AnonymousClass1 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (EmergencyCarrierArea.this.mCarrierText.getVisibility() != 0) {
                    return false;
                }
                int action = motionEvent.getAction();
                if (action == 0) {
                    EmergencyCarrierArea.this.mCarrierText.animate().alpha(0.0f);
                } else if (action == 1) {
                    EmergencyCarrierArea.this.mCarrierText.animate().alpha(1.0f);
                }
                return false;
            }
        });
    }
}
