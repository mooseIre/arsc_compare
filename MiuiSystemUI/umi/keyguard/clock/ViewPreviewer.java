package com.android.keyguard.clock;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

final class ViewPreviewer {
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    ViewPreviewer() {
    }

    /* access modifiers changed from: package-private */
    public Bitmap createPreview(final View view, final int i, final int i2) {
        if (view == null) {
            return null;
        }
        FutureTask futureTask = new FutureTask(new Callable<Bitmap>() {
            public Bitmap call() {
                Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(createBitmap);
                canvas.drawColor(-16777216);
                ViewPreviewer.this.dispatchVisibilityAggregated(view, true);
                view.measure(View.MeasureSpec.makeMeasureSpec(i, 1073741824), View.MeasureSpec.makeMeasureSpec(i2, 1073741824));
                view.layout(0, 0, i, i2);
                view.draw(canvas);
                return createBitmap;
            }
        });
        if (Looper.myLooper() == Looper.getMainLooper()) {
            futureTask.run();
        } else {
            this.mMainHandler.post(futureTask);
        }
        try {
            return (Bitmap) futureTask.get();
        } catch (Exception e) {
            Log.e("ViewPreviewer", "Error completing task", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void dispatchVisibilityAggregated(View view, boolean z) {
        boolean z2 = true;
        boolean z3 = view.getVisibility() == 0;
        if (z3 || !z) {
            view.onVisibilityAggregated(z);
        }
        if (view instanceof ViewGroup) {
            if (!z3 || !z) {
                z2 = false;
            }
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                dispatchVisibilityAggregated(viewGroup.getChildAt(i), z2);
            }
        }
    }
}
