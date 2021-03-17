package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import com.android.systemui.C0012R$dimen;

/* access modifiers changed from: package-private */
public class MiuiGxzwQuickLoadingView extends View {
    private float mCurrentLoadingRadius = this.mLoadingOriginalRadius;
    private boolean mLoading = false;
    private float mLoadingMaxRadius;
    private float mLoadingOriginalRadius;
    private Paint mPaint;

    public MiuiGxzwQuickLoadingView(Context context, float f) {
        super(context);
        this.mLoadingOriginalRadius = f;
        initView();
    }

    public void setCurrentLoadingRadius(float f) {
        this.mCurrentLoadingRadius = f;
        invalidate();
    }

    public void setLoading(boolean z) {
        this.mLoading = z;
        this.mCurrentLoadingRadius = this.mLoadingOriginalRadius;
        invalidate();
    }

    public float getLoadingOriginalRadius() {
        return this.mLoadingOriginalRadius;
    }

    public float getLoadingMaxRadius() {
        return this.mLoadingMaxRadius;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() / 2;
        int height = getHeight() / 2;
        if (this.mLoading) {
            this.mPaint.setStyle(Paint.Style.FILL);
            this.mPaint.setColor(1306978022);
            canvas.drawCircle((float) width, (float) height, this.mCurrentLoadingRadius, this.mPaint);
            return;
        }
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(452984831);
        float f = (float) width;
        float f2 = (float) height;
        canvas.drawCircle(f, f2, this.mCurrentLoadingRadius, this.mPaint);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setColor(1308622847);
        canvas.drawCircle(f, f2, this.mCurrentLoadingRadius, this.mPaint);
    }

    private void initView() {
        updatePixelSize();
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setStrokeWidth(1.0f);
    }

    private void updatePixelSize() {
        this.mLoadingMaxRadius = getContext().getResources().getDimension(C0012R$dimen.gxzw_quick_open_loading_max_radius);
    }
}
