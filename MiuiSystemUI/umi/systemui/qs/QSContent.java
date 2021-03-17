package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.android.systemui.plugins.qs.QS;

public class QSContent extends FrameLayout {
    private QS mQs;

    public QSContent(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        QS qs;
        return !(motionEvent.getActionMasked() == 0 && (qs = this.mQs) != null && qs.isShowingDetail()) && super.dispatchTouchEvent(motionEvent);
    }
}
