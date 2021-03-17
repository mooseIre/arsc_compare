package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
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

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r3.mQs;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean dispatchTouchEvent(android.view.MotionEvent r4) {
        /*
            r3 = this;
            int r0 = r4.getActionMasked()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x0014
            com.android.systemui.plugins.qs.QS r0 = r3.mQs
            if (r0 == 0) goto L_0x0014
            boolean r0 = r0.isShowingDetail()
            if (r0 == 0) goto L_0x0014
            r0 = r1
            goto L_0x0015
        L_0x0014:
            r0 = r2
        L_0x0015:
            if (r0 != 0) goto L_0x001e
            boolean r3 = super.dispatchTouchEvent(r4)
            if (r3 == 0) goto L_0x001e
            goto L_0x001f
        L_0x001e:
            r1 = r2
        L_0x001f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.QSContent.dispatchTouchEvent(android.view.MotionEvent):boolean");
    }
}
