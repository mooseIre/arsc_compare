package com.android.systemui.statusbar.phone;

import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import miui.util.CustomizeUtil;

public class BarTransitions {
    private static final boolean DEBUG = Constants.DEBUG;
    public static final boolean HIGH_END = ActivityManager.isHighEndGfx();
    private final BarBackgroundDrawable mBarBackground;
    private int mMode;
    private final String mTag;
    private final View mView;

    /* access modifiers changed from: protected */
    public boolean isLightsOut(int i) {
        return false;
    }

    public BarTransitions(View view, int i, int i2) {
        this.mTag = "BarTransitions." + view.getClass().getSimpleName();
        this.mView = view;
        BarBackgroundDrawable barBackgroundDrawable = new BarBackgroundDrawable(this.mView.getContext(), i, i2);
        this.mBarBackground = barBackgroundDrawable;
        if (HIGH_END) {
            this.mView.setBackground(barBackgroundDrawable);
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public void transitionTo(int i, boolean z) {
        if (!HIGH_END && (i == 1 || i == 2 || i == 4)) {
            i = 0;
        }
        if (!HIGH_END && i == 6) {
            i = 3;
        }
        int i2 = this.mMode;
        if (i2 != i) {
            this.mMode = i;
            if (DEBUG) {
                Log.d(this.mTag, String.format("%s -> %s animate=%s", new Object[]{modeToString(i2), modeToString(i), Boolean.valueOf(z)}));
            }
            onTransition(i2, this.mMode, z);
        }
    }

    /* access modifiers changed from: protected */
    public void onTransition(int i, int i2, boolean z) {
        if (HIGH_END) {
            applyModeBackground(i, i2, z);
        }
    }

    /* access modifiers changed from: protected */
    public void applyModeBackground(int i, int i2, boolean z) {
        if (DEBUG) {
            Log.d(this.mTag, String.format("applyModeBackground oldMode=%s newMode=%s animate=%s", new Object[]{modeToString(i), modeToString(i2), Boolean.valueOf(z)}));
        }
        this.mBarBackground.applyModeBackground(i, i2, z);
    }

    public void setForceBgColor(int i) {
        this.mBarBackground.setForceBgColor(i);
    }

    public void disableChangeBg(boolean z) {
        this.mBarBackground.disableChangeBg(z);
    }

    public void darkModeChanged() {
        this.mBarBackground.darkModeChanged();
    }

    public static String modeToString(int i) {
        if (i == 0) {
            return "MODE_OPAQUE";
        }
        if (i == 1) {
            return "MODE_SEMI_TRANSPARENT";
        }
        if (i == 2) {
            return "MODE_TRANSLUCENT";
        }
        if (i == 3) {
            return "MODE_LIGHTS_OUT";
        }
        if (i == 4) {
            return "MODE_TRANSPARENT";
        }
        if (i == 5) {
            return "MODE_WARNING";
        }
        if (i == 6) {
            return "MODE_LIGHTS_OUT_TRANSPARENT";
        }
        return "Unknown mode " + i;
    }

    public void finishAnimations() {
        this.mBarBackground.finishAnimation();
    }

    /* access modifiers changed from: package-private */
    public void setSemiTransparentColor(int i) {
        BarBackgroundDrawable barBackgroundDrawable = this.mBarBackground;
        if (barBackgroundDrawable != null && i != barBackgroundDrawable.getSemiTransparentColor()) {
            this.mBarBackground.setSemiTransparentColor(i);
            this.mView.invalidate();
        }
    }

    private static class BarBackgroundDrawable extends Drawable {
        private boolean mAnimating;
        private Context mAppContext;
        private int mColor;
        private int mColorStart;
        private boolean mDisableChangeBg;
        private long mEndTime;
        private int mForceBgColor;
        private final Drawable mGradient;
        private int mGradientAlpha;
        private int mGradientAlphaStart;
        private final TimeInterpolator mInterpolator;
        private int mMode = -1;
        private int mOpaqueColor;
        private final int mOpaqueColorId;
        private int mSemiTransparent;
        private long mStartTime;
        private final int mTransparent;
        private final int mWarning;

        public int getOpacity() {
            return -3;
        }

        public void setAlpha(int i) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public BarBackgroundDrawable(Context context, int i, int i2) {
            this.mAppContext = context.getApplicationContext();
            context.getResources();
            this.mOpaqueColorId = i2;
            this.mOpaqueColor = context.getColor(i2);
            this.mSemiTransparent = context.getColor(R.color.system_bar_background_semi_transparent);
            this.mTransparent = context.getColor(R.color.system_bar_background_transparent);
            this.mWarning = -65536;
            this.mGradient = context.getDrawable(i);
            this.mInterpolator = new LinearInterpolator();
        }

        /* access modifiers changed from: private */
        public int getSemiTransparentColor() {
            return this.mSemiTransparent;
        }

        /* access modifiers changed from: package-private */
        public void setSemiTransparentColor(int i) {
            this.mSemiTransparent = i;
        }

        public void setForceBgColor(int i) {
            if (this.mForceBgColor != i) {
                this.mForceBgColor = i;
                invalidateSelf();
            }
        }

        public void disableChangeBg(boolean z) {
            if (this.mDisableChangeBg != z) {
                this.mDisableChangeBg = z;
                invalidateSelf();
            }
        }

        /* access modifiers changed from: protected */
        public void onBoundsChange(Rect rect) {
            super.onBoundsChange(rect);
            this.mGradient.setBounds(rect);
        }

        public void applyModeBackground(int i, int i2, boolean z) {
            if (this.mMode != i2) {
                this.mMode = i2;
                this.mAnimating = z;
                if (z) {
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    this.mStartTime = elapsedRealtime;
                    this.mEndTime = elapsedRealtime + 200;
                    this.mGradientAlphaStart = this.mGradientAlpha;
                    this.mColorStart = this.mColor;
                }
                invalidateSelf();
            }
        }

        public void finishAnimation() {
            if (this.mAnimating) {
                this.mAnimating = false;
                invalidateSelf();
            }
        }

        public void darkModeChanged() {
            this.mOpaqueColor = this.mAppContext.getColor(this.mOpaqueColorId);
        }

        public void draw(Canvas canvas) {
            int i;
            int i2 = this.mMode;
            if (i2 == 5) {
                i = this.mWarning;
            } else if (i2 == 1) {
                i = this.mSemiTransparent;
            } else if (i2 == 4 || i2 == 2 || i2 == 6) {
                i = this.mTransparent;
            } else {
                i = this.mOpaqueColor;
            }
            if (CustomizeUtil.needChangeSize() && !this.mDisableChangeBg) {
                i = this.mForceBgColor;
            }
            if (!this.mAnimating) {
                this.mColor = i;
                this.mGradientAlpha = 0;
            } else {
                long elapsedRealtime = SystemClock.elapsedRealtime();
                long j = this.mEndTime;
                if (elapsedRealtime >= j) {
                    this.mAnimating = false;
                    this.mColor = i;
                    this.mGradientAlpha = 0;
                } else {
                    long j2 = this.mStartTime;
                    float max = Math.max(0.0f, Math.min(this.mInterpolator.getInterpolation(((float) (elapsedRealtime - j2)) / ((float) (j - j2))), 1.0f));
                    float f = 1.0f - max;
                    this.mGradientAlpha = (int) ((((float) 0) * max) + (((float) this.mGradientAlphaStart) * f));
                    this.mColor = Color.argb((int) ((((float) Color.alpha(i)) * max) + (((float) Color.alpha(this.mColorStart)) * f)), (int) ((((float) Color.red(i)) * max) + (((float) Color.red(this.mColorStart)) * f)), (int) ((((float) Color.green(i)) * max) + (((float) Color.green(this.mColorStart)) * f)), (int) ((max * ((float) Color.blue(i))) + (((float) Color.blue(this.mColorStart)) * f)));
                }
            }
            int i3 = this.mGradientAlpha;
            if (i3 > 0) {
                this.mGradient.setAlpha(i3);
                this.mGradient.draw(canvas);
            }
            if (Color.alpha(this.mColor) > 0) {
                canvas.drawColor(this.mColor);
            }
            if (this.mAnimating) {
                invalidateSelf();
            }
        }
    }
}
