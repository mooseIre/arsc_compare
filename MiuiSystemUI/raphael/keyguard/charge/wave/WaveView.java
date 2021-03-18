package com.android.keyguard.charge.wave;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.android.keyguard.charge.ChargeUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import miui.view.animation.ExponentialEaseOutInterpolator;

public class WaveView extends View {
    private int mBubbleMaxHeight = 1380;
    private int mBubbleMaxSize = 15;
    private Paint mBubblePaint;
    private ArrayList<Bubble> mBubbles = new ArrayList<>();
    private float mDamp = 0.95f;
    private int mExtraHeightBetweenWaves = 10;
    private Handler mHandler = new Handler() {
        /* class com.android.keyguard.charge.wave.WaveView.AnonymousClass1 */

        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 10001) {
                WaveView.this.tryCreateBubble();
            }
        }
    };
    private int mMinMargin;
    private int mProgress = 45;
    private Random mRandom = new Random();
    private int mWaterSpeed = -5;
    private int mWave1Dx;
    private int mWave2Dx;
    private AnimatorSet mWaveAnimatorSet = new AnimatorSet();
    private int[] mWaveEndColor = {Color.parseColor("#f1691e"), Color.parseColor("#1aabff"), Color.parseColor("#1ef1b8")};
    private int mWaveHeight1 = 28;
    private int mWaveHeight2 = 28;
    private int mWaveLength1 = 1080;
    private int mWaveLength2 = 1400;
    private Paint mWavePaint;
    private Path mWavePath1;
    private Path mWavePath2;
    private int[] mWaveStartColor = {Color.parseColor("#c33021"), Color.parseColor("#4c11e1"), Color.parseColor("#0e8f20")};
    private int mWaveViewHeight = 2250;
    private int mWaveViewWidth = 2048;
    private int mWaveXOffset = 0;
    private int mWaveY = 0;
    private float mWaveYPercent = -0.1f;

    public WaveView(Context context) {
        super(context);
        initWave();
        initBubble();
        this.mMinMargin = (Math.max(this.mWaveHeight1, this.mWaveHeight2) / 2) + this.mExtraHeightBetweenWaves;
        updateWaveHeight();
        setWaveColor(this.mProgress);
    }

    public void setWaveViewWidth(int i) {
        this.mWaveViewWidth = i;
    }

    public void setWaveViewHeight(int i) {
        this.mWaveViewHeight = i;
        updateWaveHeight();
        setWaveColor(this.mProgress);
    }

    private void initWave() {
        this.mWavePaint = new Paint();
        this.mWavePath1 = new Path();
        this.mWavePath2 = new Path();
        this.mWavePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mWavePaint.setAlpha(100);
    }

    private void updateWaveHeight() {
        double d = 1.0d - (((double) this.mProgress) / 100.0d);
        int i = this.mWaveViewHeight;
        int i2 = this.mMinMargin;
        int i3 = ((int) (d * ((double) (i - (i2 * 2))))) + i2;
        this.mWaveY = i3;
        this.mBubbleMaxHeight = Math.max(1380, i3);
        if (ChargeUtils.supportWaveChargeAnimation()) {
            this.mWaveXOffset = 299;
        }
    }

    private void initBubble() {
        Paint paint = new Paint();
        this.mBubblePaint = paint;
        paint.setColor(-1);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i;
        int i2;
        super.onDraw(canvas);
        this.mWavePath1.reset();
        int i3 = this.mProgress;
        if (i3 < 100) {
            int i4 = this.mWaveViewHeight;
            int i5 = this.mMinMargin;
            i = ((int) ((1.0d - (((double) (((float) i3) * this.mWaveYPercent)) / 100.0d)) * ((double) (i4 - (i5 * 2))))) + i5;
        } else {
            i = ((int) ((1.0d - (((double) (((float) i3) * this.mWaveYPercent)) / 100.0d)) * ((double) this.mWaveViewHeight))) - this.mMinMargin;
        }
        int i6 = this.mWaveLength1;
        int i7 = i6 / 2;
        this.mWavePath1.moveTo((float) ((-i6) + this.mWave1Dx), (float) i);
        int i8 = -this.mWaveLength1;
        while (true) {
            i2 = this.mWaveViewWidth;
            if (i8 > this.mWaveLength1 + i2) {
                break;
            }
            float f = (float) (i7 / 2);
            float f2 = (float) i7;
            this.mWavePath1.rQuadTo(f, (float) (-this.mWaveHeight1), f2, 0.0f);
            this.mWavePath1.rQuadTo(f, (float) this.mWaveHeight1, f2, 0.0f);
            i8 += this.mWaveLength1;
        }
        this.mWavePath1.lineTo((float) i2, (float) this.mWaveViewHeight);
        this.mWavePath1.lineTo(0.0f, (float) this.mWaveViewHeight);
        this.mWavePath1.close();
        canvas.drawPath(this.mWavePath1, this.mWavePaint);
        this.mWavePath2.reset();
        int i9 = this.mWaveLength2 / 2;
        this.mWavePath2.moveTo((float) (-this.mWave2Dx), (float) (i + this.mExtraHeightBetweenWaves));
        int i10 = -this.mWaveLength2;
        while (true) {
            int i11 = this.mWaveViewWidth;
            if (i10 <= this.mWaveLength2 + i11) {
                float f3 = (float) (i9 / 2);
                float f4 = (float) i9;
                this.mWavePath2.rQuadTo(f3, (float) (-this.mWaveHeight2), f4, 0.0f);
                this.mWavePath2.rQuadTo(f3, (float) this.mWaveHeight2, f4, 0.0f);
                i10 += this.mWaveLength2;
            } else {
                this.mWavePath2.lineTo((float) i11, (float) this.mWaveViewHeight);
                this.mWavePath2.lineTo(0.0f, (float) this.mWaveViewHeight);
                this.mWavePath2.close();
                canvas.drawPath(this.mWavePath2, this.mWavePaint);
                drawBubble(canvas);
                return;
            }
        }
    }

    public void setProgress(int i) {
        this.mProgress = i;
        setWaveColor(i);
        updateWaveHeight();
        invalidate();
    }

    public void setWaveColor(int i) {
        char c = (i < 20 || i >= 60) ? (i < 60 || i > 100) ? (char) 0 : 2 : 1;
        this.mWavePaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mWaveViewHeight, this.mWaveStartColor[c], this.mWaveEndColor[c], Shader.TileMode.CLAMP));
    }

    public void startAnim() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(-0.1f, 1.0f);
        ofFloat.setDuration(2000L);
        ofFloat.setInterpolator(new ExponentialEaseOutInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.charge.wave.WaveView.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                WaveView.this.mWaveYPercent = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                WaveView.this.postInvalidate();
            }
        });
        ValueAnimator ofInt = ValueAnimator.ofInt(0, this.mWaveLength1);
        ofInt.setDuration(1600L);
        ofInt.setRepeatCount(-1);
        ofInt.setInterpolator(new LinearInterpolator());
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.charge.wave.WaveView.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (Math.abs(((Integer) valueAnimator.getAnimatedValue()).intValue() - WaveView.this.mWave1Dx) > 1) {
                    WaveView.this.mWave1Dx = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                    WaveView.this.postInvalidate();
                }
            }
        });
        ValueAnimator ofInt2 = ValueAnimator.ofInt(0, this.mWaveLength2);
        ofInt2.setDuration(2400L);
        ofInt2.setRepeatCount(-1);
        ofInt2.setInterpolator(new LinearInterpolator());
        ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.charge.wave.WaveView.AnonymousClass4 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                WaveView.this.mWave2Dx = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        this.mWaveAnimatorSet.setStartDelay(400);
        this.mWaveAnimatorSet.play(ofInt).with(ofInt2).with(ofFloat);
        this.mWaveAnimatorSet.start();
        this.mHandler.sendEmptyMessageDelayed(10001, 1000);
    }

    private void resetState() {
        this.mWaveYPercent = -0.1f;
        this.mWave1Dx = 0;
        this.mWave2Dx = 0;
        this.mBubbles.clear();
    }

    public void stopAnim() {
        this.mHandler.removeMessages(10001);
        AnimatorSet animatorSet = this.mWaveAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        resetState();
    }

    /* access modifiers changed from: private */
    public class Bubble {
        float Vx;
        float Vy;
        float alpha;
        float angle;
        float initAlpha;
        int radius;
        float scale;
        float sinRandom;
        int x;
        int y;

        private Bubble(WaveView waveView) {
        }
    }

    private void drawBubble(Canvas canvas) {
        refreshBubbles();
        Iterator<Bubble> it = this.mBubbles.iterator();
        while (it.hasNext()) {
            Bubble next = it.next();
            if (next != null) {
                this.mBubblePaint.setAlpha((int) (next.alpha * 255.0f));
                canvas.drawCircle((float) next.x, (float) next.y, (float) ((int) (((float) next.radius) * next.scale)), this.mBubblePaint);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void tryCreateBubble() {
        this.mHandler.sendEmptyMessageDelayed(10001, (long) (1000.0f / ((this.mRandom.nextFloat() * 4.0f) + 2.0f)));
        if (this.mBubbles.size() < this.mBubbleMaxSize) {
            Bubble bubble = new Bubble();
            bubble.radius = (int) ((this.mRandom.nextFloat() * 7.0f) + 7.0f);
            bubble.Vx = (this.mRandom.nextFloat() - 0.5f) * 12.0f;
            bubble.Vy = (-this.mRandom.nextFloat()) * 6.0f;
            bubble.x = (this.mWaveViewWidth / 2) + this.mWaveXOffset;
            bubble.y = this.mWaveViewHeight;
            bubble.scale = (this.mRandom.nextFloat() * 0.4f) + 0.4f;
            float nextFloat = (this.mRandom.nextFloat() * 0.7f) + 0.3f;
            bubble.initAlpha = nextFloat;
            bubble.alpha = nextFloat;
            bubble.angle = ((float) Math.random()) * 360.0f;
            bubble.sinRandom = (float) ((Math.random() * 2.0d) + 3.0d);
            this.mBubbles.add(bubble);
        }
    }

    private void refreshBubbles() {
        Iterator<Bubble> it = this.mBubbles.iterator();
        while (it.hasNext()) {
            Bubble next = it.next();
            if (((float) next.y) + next.Vy <= ((float) (this.mBubbleMaxHeight + 30))) {
                it.remove();
            } else {
                int indexOf = this.mBubbles.indexOf(next);
                double sin = Math.sin((((double) (next.angle + ((((float) next.y) * next.sinRandom) / 2.5f))) * 3.141592653589793d) / 180.0d);
                int i = next.y;
                int i2 = this.mWaveViewHeight;
                float f = next.scale;
                next.x = (int) (((float) next.x) + next.Vx + ((float) ((int) (((sin * ((double) ((i - 300) - i2))) / 150.0d) * ((double) f)))));
                int i3 = (int) (((float) i) + next.Vy + (((float) this.mWaterSpeed) * (f + 1.0f)));
                next.y = i3;
                float min = Math.min(f + (((float) (i2 - i3)) / 1000.0f), 1.0f);
                next.scale = min;
                float f2 = next.Vx;
                float f3 = this.mDamp;
                next.Vx = f2 * f3;
                next.Vy *= f3;
                next.alpha = Math.min((((float) (next.y - this.mBubbleMaxHeight)) - 20.0f) / 200.0f, min) * next.initAlpha;
                this.mBubbles.set(indexOf, next);
            }
        }
    }
}
