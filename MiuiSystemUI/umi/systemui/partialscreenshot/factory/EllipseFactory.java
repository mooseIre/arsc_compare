package com.android.systemui.partialscreenshot.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.partialscreenshot.PartialScreenshotView;
import com.android.systemui.partialscreenshot.shape.EllipseScreenshot;

public class EllipseFactory extends ShapeFactory {
    private EllipseScreenshot ellipse;
    private int mState = 1;

    public boolean onTouch(View view, MotionEvent motionEvent) {
        PartialScreenshotView partialScreenshotView = (PartialScreenshotView) view;
        int action = motionEvent.getAction();
        if (action == 0) {
            return onActionDown(partialScreenshotView, motionEvent, view);
        }
        if (action == 1) {
            return onActionUp(partialScreenshotView, motionEvent);
        }
        if (action != 2) {
            return false;
        }
        return onActionMove(partialScreenshotView, motionEvent);
    }

    private boolean onActionDown(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent, View view) {
        if (this.mState == 1) {
            EllipseScreenshot ellipseScreenshot = new EllipseScreenshot(view);
            this.ellipse = ellipseScreenshot;
            ellipseScreenshot.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            partialScreenshotView.setProduct(this.ellipse);
        } else if (this.ellipse.getSelectionRect() != null) {
            this.ellipse.onActionDown(motionEvent);
        } else {
            this.mState = 1;
        }
        return true;
    }

    private boolean onActionMove(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        EllipseScreenshot ellipseScreenshot = this.ellipse;
        if (ellipseScreenshot != null) {
            if (this.mState == 1) {
                ellipseScreenshot.updateSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            } else {
                ellipseScreenshot.onActionMove(motionEvent);
            }
            partialScreenshotView.setProduct(this.ellipse);
        }
        return true;
    }

    private boolean onActionUp(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        EllipseScreenshot ellipseScreenshot = this.ellipse;
        if (ellipseScreenshot != null) {
            if (ellipseScreenshot.getSelectionRect() == null) {
                this.mState = 1;
                partialScreenshotView.clear();
            } else {
                this.mState = 2;
            }
        }
        return true;
    }

    public Rect getTrimmingFrame() {
        return this.ellipse.getSelectionRect();
    }

    public void notifyShapeChanged(Rect rect, PartialScreenshotView partialScreenshotView) {
        EllipseScreenshot ellipseScreenshot = new EllipseScreenshot(partialScreenshotView);
        this.ellipse = ellipseScreenshot;
        ellipseScreenshot.startSelection(rect.left, rect.top);
        this.ellipse.updateSelection(rect.right, rect.bottom);
        partialScreenshotView.setProduct(this.ellipse);
        this.mState = 2;
    }

    public int getState() {
        return this.mState;
    }

    public Bitmap getPartialBitmap(Bitmap bitmap) {
        return this.ellipse.getPartialBitmap(bitmap);
    }
}
