package com.android.systemui.partialscreenshot.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.partialscreenshot.PartialScreenshotView;
import com.android.systemui.partialscreenshot.shape.DrawShapeUtil;
import com.android.systemui.partialscreenshot.shape.IrregularScreenshot;

public class IrregularFactory extends ShapeFactory {
    private IrregularScreenshot Irregular;
    private float mLastX;
    private float mLastY;
    private int mState = 1;
    private float mX;
    private float mY;

    public void notifyShapeChanged(Rect rect, PartialScreenshotView partialScreenshotView) {
    }

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
            this.Irregular = new IrregularScreenshot(view);
            this.Irregular.clear();
            this.Irregular.addPath((float) ((int) motionEvent.getX()), (float) ((int) motionEvent.getY()));
            partialScreenshotView.setProduct(this.Irregular);
        } else if (this.Irregular.getmSelectionRect() != null) {
            this.Irregular.onActionDown(motionEvent);
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
        if (this.Irregular != null && DrawShapeUtil.distance(this.mX, this.mLastX, this.mY, this.mLastY) > 2.0d) {
            if (this.mState == 1) {
                this.Irregular.addPath((float) ((int) motionEvent.getX()), (float) ((int) motionEvent.getY()));
            } else {
                this.Irregular.onActionMove(motionEvent);
            }
            partialScreenshotView.setProduct(this.Irregular);
            this.mLastX = this.mX;
            this.mLastY = this.mY;
        }
        return true;
    }

    private boolean onActionUp(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        IrregularScreenshot irregularScreenshot;
        if (this.mState == 1 && (irregularScreenshot = this.Irregular) != null) {
            irregularScreenshot.addPath((float) ((int) motionEvent.getX()), (float) ((int) motionEvent.getY()));
            this.Irregular.setUp(true);
            partialScreenshotView.setProduct(this.Irregular);
            if (this.Irregular.checkIsValid()) {
                this.mState = 2;
            } else {
                partialScreenshotView.clear();
            }
        }
        return true;
    }

    public Rect getTrimmingFrame() {
        return this.Irregular.getmSelectionRect();
    }

    public int getState() {
        return this.mState;
    }

    public Bitmap getPartialBitmap(Bitmap bitmap) {
        return this.Irregular.getPartialBitmap(bitmap);
    }
}
