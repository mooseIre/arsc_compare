package com.android.systemui.partialscreenshot.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.partialscreenshot.PartialScreenshotView;
import com.android.systemui.partialscreenshot.shape.DrawShapeUtil;
import com.android.systemui.partialscreenshot.shape.EllipseScreenshot;

public class EllipseFactory extends ShapeFactory {
    private EllipseScreenshot ellipse;
    private float mLastX;
    private float mLastY;
    private int mState = 1;
    private float mX;
    private float mY;

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
            this.ellipse = new EllipseScreenshot(view);
            this.ellipse.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            partialScreenshotView.setProduct(this.ellipse);
        } else if (this.ellipse.getSelectionRect() != null) {
            this.ellipse.onActionDown(motionEvent);
        } else {
            this.mState = 1;
        }
        this.mLastX = motionEvent.getX();
        this.mLastY = motionEvent.getY();
        return true;
    }

    private boolean onActionMove(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        this.mX = motionEvent.getX();
        this.mY = motionEvent.getY();
        if (this.ellipse != null && DrawShapeUtil.distance(this.mX, this.mLastX, this.mY, this.mLastY) > 2.0d) {
            if (this.mState == 1) {
                this.ellipse.updateSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            } else {
                this.ellipse.onActionMove(motionEvent);
            }
            partialScreenshotView.setProduct(this.ellipse);
            this.mLastX = this.mX;
            this.mLastY = this.mY;
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
        this.ellipse = new EllipseScreenshot(partialScreenshotView);
        this.ellipse.startSelection(rect.left, rect.top);
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
