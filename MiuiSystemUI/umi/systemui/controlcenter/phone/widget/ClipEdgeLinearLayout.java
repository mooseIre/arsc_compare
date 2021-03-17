package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.R$styleable;

public class ClipEdgeLinearLayout extends LinearLayout {
    private boolean mClipEdge = false;
    private boolean mClipEnd = false;
    private Rect mClipRect = new Rect();
    private boolean mNeedCipe;

    public ClipEdgeLinearLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(attributeSet);
    }

    private void init(AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R$styleable.ClipEdgeLinearLayout);
            this.mClipEdge = obtainStyledAttributes.getBoolean(R$styleable.ClipEdgeLinearLayout_clipEdge, false);
            this.mClipEnd = obtainStyledAttributes.getBoolean(R$styleable.ClipEdgeLinearLayout_clipEnd, false);
            obtainStyledAttributes.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public void measureChildWithMargins(View view, int i, int i2, int i3, int i4) {
        if (this.mClipEdge) {
            i2 = 0;
        }
        super.measureChildWithMargins(view, i, i2, i3, i4);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mClipEdge) {
            getClipRect();
        }
    }

    private void getClipRect() {
        Rect rect = this.mClipRect;
        int i = 0;
        rect.left = 0;
        rect.top = 0;
        rect.bottom = getHeight();
        this.mClipRect.right = 0;
        if (clipEnd()) {
            i = getWidth();
        }
        setClipRectRight(i, this);
    }

    private void setClipRectRight(int i, ViewGroup viewGroup) {
        this.mNeedCipe = false;
        for (int i2 = 0; i2 < viewGroup.getChildCount(); i2++) {
            View childAt = viewGroup.getChildAt(i2);
            if (childAt.getLeft() < i && childAt.getRight() > i && childAt.getVisibility() != 8) {
                if (childAt instanceof ViewGroup) {
                    setClipRectRight(i - childAt.getLeft(), (ViewGroup) childAt);
                } else {
                    if (clipEnd()) {
                        this.mClipRect.right = getWidth() - (i - childAt.getLeft());
                    } else {
                        this.mClipRect.left = childAt.getRight() - i;
                        this.mClipRect.right = getWidth();
                    }
                    this.mNeedCipe = true;
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean clipEnd() {
        if (isLayoutRtl()) {
            return !this.mClipEnd;
        }
        return this.mClipEnd;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0023  */
    /* JADX WARNING: Removed duplicated region for block: B:14:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0016  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void dispatchDraw(android.graphics.Canvas r3) {
        /*
            r2 = this;
            boolean r0 = r2.mClipEdge
            if (r0 == 0) goto L_0x0013
            boolean r0 = r2.mNeedCipe
            if (r0 == 0) goto L_0x0013
            android.graphics.Rect r0 = r2.mClipRect
            int r1 = r0.right
            int r0 = r0.left
            int r1 = r1 - r0
            if (r1 < 0) goto L_0x0013
            r0 = 1
            goto L_0x0014
        L_0x0013:
            r0 = 0
        L_0x0014:
            if (r0 == 0) goto L_0x001e
            r3.save()
            android.graphics.Rect r1 = r2.mClipRect
            r3.clipRect(r1)
        L_0x001e:
            super.dispatchDraw(r3)
            if (r0 == 0) goto L_0x0026
            r3.restore()
        L_0x0026:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.widget.ClipEdgeLinearLayout.dispatchDraw(android.graphics.Canvas):void");
    }
}
