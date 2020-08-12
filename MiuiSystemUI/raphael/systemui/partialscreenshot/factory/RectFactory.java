package com.android.systemui.partialscreenshot.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.partialscreenshot.PartialScreenshotView;
import com.android.systemui.partialscreenshot.shape.DrawShapeUtil;
import com.android.systemui.partialscreenshot.shape.PartialScreenshotShape;
import com.android.systemui.partialscreenshot.shape.RectScreenshot;

public class RectFactory extends ShapeFactory {
    private static RectFactory mRectFactory = new RectFactory();
    private float mLastX;
    private float mLastY;
    private int mState = 1;
    private float mX;
    private float mY;
    private RectScreenshot rect;

    public static synchronized RectFactory getInstance() {
        RectFactory rectFactory;
        synchronized (RectFactory.class) {
            rectFactory = mRectFactory;
        }
        return rectFactory;
    }

    private RectFactory() {
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
            this.rect = new RectScreenshot(view);
            this.rect.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            partialScreenshotView.setProduct(this.rect);
        } else if (this.rect.getSelectionRect() != null) {
            this.rect.onActionDown(motionEvent);
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
        if (this.rect != null && DrawShapeUtil.distance(this.mX, this.mLastX, this.mY, this.mLastY) > 2.0d) {
            if (this.mState == 1) {
                this.rect.updateSelection((int) motionEvent.getX(), (int) motionEvent.getY());
            } else {
                this.rect.onActionMove(motionEvent);
            }
            partialScreenshotView.setProduct(this.rect);
            this.mLastX = this.mX;
            this.mLastY = this.mY;
        }
        return true;
    }

    private boolean onActionUp(PartialScreenshotView partialScreenshotView, MotionEvent motionEvent) {
        RectScreenshot rectScreenshot = this.rect;
        if (rectScreenshot != null) {
            if (rectScreenshot.getSelectionRect() == null) {
                this.mState = 1;
                partialScreenshotView.clear();
            } else {
                this.mState = 2;
            }
        }
        return true;
    }

    public int getState() {
        return this.mState;
    }

    public Rect getTrimmingFrame() {
        return this.rect.getSelectionRect();
    }

    public void notifyShapeChanged(Rect rect2, PartialScreenshotView partialScreenshotView) {
        this.rect = new RectScreenshot(partialScreenshotView);
        this.rect.startSelection(rect2.left, rect2.top);
        this.rect.updateSelection(rect2.right, rect2.bottom);
        partialScreenshotView.setProduct(this.rect);
        this.mState = 2;
    }

    public Bitmap getPartialBitmap(Bitmap bitmap) {
        return this.rect.getPartialBitmap(bitmap);
    }

    public void clear(PartialScreenshotView partialScreenshotView) {
        this.mState = 1;
        this.rect = null;
        partialScreenshotView.setProduct((PartialScreenshotShape) null);
    }
}
