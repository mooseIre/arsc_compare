package com.android.systemui.recents.views;

import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewOutlineProvider;

public class AnimateableViewBounds extends ViewOutlineProvider {
    @ViewDebug.ExportedProperty(category = "recents")
    float mAlpha = 1.0f;
    @ViewDebug.ExportedProperty(category = "recents")
    Rect mClipBounds = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    Rect mClipRect = new Rect();
    @ViewDebug.ExportedProperty(category = "recents")
    int mCornerRadius;
    @ViewDebug.ExportedProperty(category = "recents")
    Rect mLastClipBounds = new Rect();
    View mSourceView;

    public AnimateableViewBounds(View view, int i) {
        this.mSourceView = view;
        this.mCornerRadius = i;
    }

    public void reset() {
        this.mClipRect.set(-1, -1, -1, -1);
        updateClipBounds();
    }

    public void getOutline(View view, Outline outline) {
        outline.setAlpha(0.0f);
        if (this.mCornerRadius > 0) {
            Rect rect = this.mClipRect;
            outline.setRoundRect(rect.left, rect.top, this.mSourceView.getWidth() - this.mClipRect.right, this.mSourceView.getHeight() - this.mClipRect.bottom, (float) this.mCornerRadius);
            return;
        }
        Rect rect2 = this.mClipRect;
        outline.setRect(rect2.left, rect2.top, this.mSourceView.getWidth() - this.mClipRect.right, this.mSourceView.getHeight() - this.mClipRect.bottom);
    }

    /* access modifiers changed from: package-private */
    public void setAlpha(float f) {
        if (Float.compare(f, this.mAlpha) != 0) {
            this.mAlpha = f;
            this.mSourceView.invalidateOutline();
        }
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public void setClipRight(int i) {
        this.mClipRect.right = i;
        updateClipBounds();
    }

    private void updateClipBounds() {
        this.mClipBounds.set(Math.max(0, this.mClipRect.left), Math.max(0, this.mClipRect.top), this.mSourceView.getWidth() - Math.max(0, this.mClipRect.right), this.mSourceView.getHeight() - Math.max(0, this.mClipRect.bottom));
        if (!this.mLastClipBounds.equals(this.mClipBounds)) {
            this.mSourceView.invalidateOutline();
            this.mLastClipBounds.set(this.mClipBounds);
        }
    }
}
