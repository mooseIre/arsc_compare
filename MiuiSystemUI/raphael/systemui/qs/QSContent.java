package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.systemui.plugins.qs.QS;

public class QSContent extends LinearLayout {
    private QS mQs;

    public QSContent(Context context) {
        super(context);
    }

    public QSContent(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0007, code lost:
        r0 = r2.mQs;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean dispatchTouchEvent(android.view.MotionEvent r3) {
        /*
            r2 = this;
            int r0 = r3.getActionMasked()
            r1 = 0
            if (r0 != 0) goto L_0x0013
            com.android.systemui.plugins.qs.QS r0 = r2.mQs
            if (r0 == 0) goto L_0x0013
            boolean r0 = r0.isShowingDetail()
            if (r0 == 0) goto L_0x0013
            r0 = 1
            goto L_0x0014
        L_0x0013:
            r0 = r1
        L_0x0014:
            if (r0 == 0) goto L_0x0017
            goto L_0x001b
        L_0x0017:
            boolean r1 = super.dispatchTouchEvent(r3)
        L_0x001b:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QSContent.dispatchTouchEvent(android.view.MotionEvent):boolean");
    }

    public void setQs(QS qs) {
        this.mQs = qs;
    }
}
