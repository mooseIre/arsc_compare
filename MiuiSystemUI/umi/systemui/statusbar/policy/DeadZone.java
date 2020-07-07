package com.android.systemui.statusbar.policy;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.R$styleable;
import com.android.systemui.plugins.R;

public class DeadZone extends View {
    private final Runnable mDebugFlash;
    private int mDecay;
    private int mDisplayRotation;
    private float mFlashFrac;
    private int mHold;
    private long mLastPokeTime;
    private boolean mShouldFlash;
    private int mSizeMax;
    private int mSizeMin;
    private boolean mVertical;

    static float lerp(float f, float f2, float f3) {
        return ((f2 - f) * f3) + f;
    }

    public DeadZone(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DeadZone(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet);
        this.mFlashFrac = 0.0f;
        this.mDebugFlash = new Runnable() {
            public void run() {
                ObjectAnimator.ofFloat(DeadZone.this, "flash", new float[]{1.0f, 0.0f}).setDuration(150).start();
            }
        };
        boolean z = false;
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.DeadZone, i, 0);
        this.mHold = obtainStyledAttributes.getInteger(1, 0);
        this.mDecay = obtainStyledAttributes.getInteger(0, 0);
        this.mSizeMin = obtainStyledAttributes.getDimensionPixelSize(3, 0);
        this.mSizeMax = obtainStyledAttributes.getDimensionPixelSize(2, 0);
        this.mVertical = obtainStyledAttributes.getInt(4, -1) == 1 ? true : z;
        setFlashOnTouchCapture(context.getResources().getBoolean(R.bool.config_dead_zone_flash));
    }

    private float getSize(long j) {
        int lerp;
        int i = this.mSizeMax;
        if (i == 0) {
            return 0.0f;
        }
        long j2 = j - this.mLastPokeTime;
        int i2 = this.mHold;
        int i3 = this.mDecay;
        if (j2 > ((long) (i2 + i3))) {
            lerp = this.mSizeMin;
        } else if (j2 < ((long) i2)) {
            return (float) i;
        } else {
            lerp = (int) lerp((float) i, (float) this.mSizeMin, ((float) (j2 - ((long) i2))) / ((float) i3));
        }
        return (float) lerp;
    }

    public void setFlashOnTouchCapture(boolean z) {
        this.mShouldFlash = z;
        this.mFlashFrac = 0.0f;
        postInvalidate();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getToolType(0) == 3) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 4) {
            poke(motionEvent);
            return true;
        }
        if (action == 0) {
            int size = (int) getSize(motionEvent.getEventTime());
            if (!this.mVertical ? motionEvent.getY() < ((float) size) : !(this.mDisplayRotation != 3 ? motionEvent.getX() >= ((float) size) : motionEvent.getX() <= ((float) (getWidth() - size)))) {
                Slog.v("DeadZone", "consuming errant click: (" + motionEvent.getX() + "," + motionEvent.getY() + ")");
                if (this.mShouldFlash) {
                    post(this.mDebugFlash);
                    postInvalidate();
                }
                return true;
            }
        }
        return false;
    }

    public void poke(MotionEvent motionEvent) {
        this.mLastPokeTime = motionEvent.getEventTime();
        if (this.mShouldFlash) {
            postInvalidate();
        }
    }

    public void onDraw(Canvas canvas) {
        if (this.mShouldFlash && this.mFlashFrac > 0.0f) {
            int size = (int) getSize(SystemClock.uptimeMillis());
            if (!this.mVertical) {
                canvas.clipRect(0, 0, canvas.getWidth(), size);
            } else if (this.mDisplayRotation == 3) {
                canvas.clipRect(canvas.getWidth() - size, 0, canvas.getWidth(), canvas.getHeight());
            } else {
                canvas.clipRect(0, 0, size, canvas.getHeight());
            }
            canvas.drawARGB((int) (this.mFlashFrac * 255.0f), 221, 238, 170);
        }
    }
}
