package com.android.systemui.classifier.brightline;

import android.view.MotionEvent;
import java.util.Locale;

class PointerCountClassifier extends FalsingClassifier {
    private int mMaxPointerCount;

    PointerCountClassifier(FalsingDataProvider falsingDataProvider) {
        super(falsingDataProvider);
    }

    public void onTouchEvent(MotionEvent motionEvent) {
        int i = this.mMaxPointerCount;
        if (motionEvent.getActionMasked() == 0) {
            this.mMaxPointerCount = motionEvent.getPointerCount();
        } else {
            this.mMaxPointerCount = Math.max(this.mMaxPointerCount, motionEvent.getPointerCount());
        }
        if (i != this.mMaxPointerCount) {
            FalsingClassifier.logDebug("Pointers observed:" + this.mMaxPointerCount);
        }
    }

    public boolean isFalseTouch() {
        int interactionType = getInteractionType();
        if (interactionType == 0 || interactionType == 2) {
            if (this.mMaxPointerCount > 2) {
                return true;
            }
            return false;
        } else if (this.mMaxPointerCount > 1) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public String getReason() {
        return String.format((Locale) null, "{pointersObserved=%d, threshold=%d}", new Object[]{Integer.valueOf(this.mMaxPointerCount), 1});
    }
}
