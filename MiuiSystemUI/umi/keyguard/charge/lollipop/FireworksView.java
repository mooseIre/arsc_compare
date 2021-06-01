package com.android.keyguard.charge.lollipop;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.view.WindowManager;
import com.android.systemui.C0013R$drawable;
import java.util.ArrayList;
import java.util.List;

public class FireworksView extends View {
    private static final int OUTER_TRACK_END_COLOR = Color.parseColor("#ff210672");
    private static final int OUTER_TRACK_MIDDLE_COLOR = Color.parseColor("#B42F3A81");
    private static final int OUTER_TRACK_START_COLOR = Color.parseColor("#002F3A81");
    private Drawable mFireDrawable;
    private int mFireHeight;
    private List<PointF> mFireList;
    private Runnable mFireRunnable;
    private int mFireWidth;
    private FireworksManager mFireworksManager;
    private Choreographer.FrameCallback mFrameCallback;
    private boolean mIsAnimationRuning;
    private long mLastTime;
    private Point mScreenSize;
    private float mSpeedMove;
    private Paint mTrackPaint;
    private int mTrackStokeWidth;
    private int mViewHeight;
    private int mViewWidth;
    private WindowManager mWindowManager;

    private int evaluateAlpha(int i, int i2) {
        float f = ((float) i2) * 0.3f;
        if (((float) i) > f) {
            return 255;
        }
        return (int) (((float) (i * 255)) / f);
    }

    public FireworksView(Context context) {
        this(context, null);
    }

    public FireworksView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FireworksView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFrameCallback = new Choreographer.FrameCallback() {
            /* class com.android.keyguard.charge.lollipop.FireworksView.AnonymousClass1 */

            public void doFrame(long j) {
                if (FireworksView.this.mIsAnimationRuning) {
                    long j2 = (j - FireworksView.this.mLastTime) / 1000000;
                    FireworksView.this.mLastTime = j;
                    if (FireworksView.this.mFireworksManager != null) {
                        FireworksView.this.mFireworksManager.freshPositions(FireworksView.this.mFireList, j2);
                        FireworksView.this.invalidate();
                    }
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
        this.mFireRunnable = new Runnable() {
            /* class com.android.keyguard.charge.lollipop.FireworksView.AnonymousClass2 */

            public void run() {
                if (FireworksView.this.mFireworksManager != null) {
                    FireworksView.this.mFireworksManager.fire();
                }
                FireworksView.this.postDelayed(this, 120);
            }
        };
        init(context);
    }

    private void init(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        Paint paint = new Paint(1);
        this.mTrackPaint = paint;
        paint.setStyle(Paint.Style.STROKE);
        this.mTrackPaint.setStrokeWidth((float) this.mTrackStokeWidth);
        this.mTrackPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mViewHeight, new int[]{OUTER_TRACK_START_COLOR, OUTER_TRACK_MIDDLE_COLOR, OUTER_TRACK_END_COLOR}, new float[]{0.0f, 0.12f, 1.0f}, Shader.TileMode.CLAMP));
        this.mFireworksManager = new FireworksManager(this.mViewHeight, this.mSpeedMove);
        this.mFireList = new ArrayList();
        this.mFireDrawable = context.getDrawable(C0013R$drawable.charge_animation_fire_light_icon);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTrack(canvas);
        drawFireworks(canvas);
    }

    private void drawTrack(Canvas canvas) {
        float f = ((float) this.mViewWidth) / 6.0f;
        for (int i = 1; i <= 5; i++) {
            float f2 = f * ((float) i);
            canvas.drawLine(f2, 0.0f, f2, (float) this.mViewHeight, this.mTrackPaint);
        }
    }

    private void drawFireworks(Canvas canvas) {
        List<PointF> list = this.mFireList;
        if (list != null) {
            float f = ((float) this.mViewWidth) / 6.0f;
            for (PointF pointF : list) {
                float f2 = pointF.y;
                int i = this.mFireWidth;
                int i2 = (int) (((pointF.x + 1.0f) * f) - ((float) (i / 2)));
                int i3 = (int) f2;
                this.mFireDrawable.setAlpha(evaluateAlpha(i3, this.mViewHeight));
                this.mFireDrawable.setBounds(i2, i3, i + i2, this.mFireHeight + i3);
                this.mFireDrawable.draw(canvas);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        setMeasuredDimension(this.mViewWidth, this.mViewHeight);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            this.mTrackPaint.setStrokeWidth((float) this.mTrackStokeWidth);
            this.mTrackPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mViewHeight, new int[]{OUTER_TRACK_START_COLOR, OUTER_TRACK_MIDDLE_COLOR, OUTER_TRACK_END_COLOR}, new float[]{0.0f, 0.12f, 1.0f}, Shader.TileMode.CLAMP));
            this.mFireworksManager.updateDistanceAndSpeed(this.mViewHeight, this.mSpeedMove);
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        int min = Math.min(point.x, point.y);
        int i = this.mScreenSize.y;
        float f = (((float) min) * 1.0f) / 1080.0f;
        float f2 = (((float) i) * 1.0f) / 2340.0f;
        this.mViewWidth = (int) (122.0f * f);
        this.mViewHeight = (i / 2) - ((int) (292.0f * f));
        this.mFireWidth = (int) (15.0f * f);
        this.mFireHeight = (int) (345.0f * f2);
        this.mTrackStokeWidth = (int) (f * 4.0f);
        this.mSpeedMove = f2 * 1.4633334f;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    public void start() {
        this.mIsAnimationRuning = true;
        Choreographer.getInstance().postFrameCallback(this.mFrameCallback);
        post(this.mFireRunnable);
    }

    public void stop() {
        this.mIsAnimationRuning = false;
        removeCallbacks(this.mFireRunnable);
        Choreographer.getInstance().removeFrameCallback(this.mFrameCallback);
    }
}
