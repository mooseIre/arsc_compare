package com.android.systemui.partialscreenshot.factory;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.partialscreenshot.PartialScreenshotView;

public abstract class ShapeFactory implements View.OnTouchListener {
    public abstract Bitmap getPartialBitmap(Bitmap bitmap);

    public abstract int getState();

    public abstract Rect getTrimmingFrame();

    public abstract void notifyShapeChanged(Rect rect, PartialScreenshotView partialScreenshotView);

    public abstract boolean onTouch(View view, MotionEvent motionEvent);
}
