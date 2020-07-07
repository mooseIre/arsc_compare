package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.doze.DozeHost$PulseCallback;
import com.android.systemui.doze.DozeLog;

public class DozeScrimController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("DozeScrimController", 3);
    private Animator mBehindAnimator;
    private float mBehindTarget;
    private final Context mContext;
    /* access modifiers changed from: private */
    public final DozeParameters mDozeParameters;
    /* access modifiers changed from: private */
    public boolean mDozing;
    private boolean mDozingAborted;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private Animator mInFrontAnimator;
    private float mInFrontTarget;
    private DozeHost$PulseCallback mPulseCallback;
    private final Runnable mPulseIn = new Runnable() {
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse in, mDozing=" + DozeScrimController.this.mDozing + " mPulseReason=" + DozeLog.pulseReasonToString(DozeScrimController.this.mPulseReason));
            }
            if (DozeScrimController.this.mDozing) {
                DozeLog.tracePulseStart(DozeScrimController.this.mPulseReason);
                DozeScrimController.this.pulseStarted();
                if (DozeScrimController.this.mDozeParameters.getAlwaysOn()) {
                    DozeScrimController.this.mHandler.post(new Runnable() {
                        public void run() {
                            DozeScrimController.this.onScreenTurnedOn();
                        }
                    });
                }
            }
        }
    };
    private final Runnable mPulseInFinished = new Runnable() {
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse in finished, mDozing=" + DozeScrimController.this.mDozing);
            }
            if (DozeScrimController.this.mDozing) {
                DozeScrimController.this.mHandler.postDelayed(DozeScrimController.this.mPulseOut, (long) DozeScrimController.this.mDozeParameters.getPulseVisibleDuration());
                DozeScrimController.this.mHandler.postDelayed(DozeScrimController.this.mPulseOutExtended, (long) DozeScrimController.this.mDozeParameters.getPulseVisibleDurationExtended());
            }
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mPulseOut = new Runnable() {
        public void run() {
            DozeScrimController.this.mHandler.removeCallbacks(DozeScrimController.this.mPulseOutExtended);
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse out, mDozing=" + DozeScrimController.this.mDozing);
            }
            if (DozeScrimController.this.mDozing) {
                DozeScrimController dozeScrimController = DozeScrimController.this;
                dozeScrimController.startScrimAnimation(true, dozeScrimController.mDozeParameters.getAlwaysOn() ? 0.0f : 1.0f, (long) DozeScrimController.this.mDozeParameters.getPulseOutDuration(), Interpolators.ALPHA_IN, DozeScrimController.this.mPulseOutFinished);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mPulseOutExtended = new Runnable() {
        public void run() {
            DozeScrimController.this.mHandler.removeCallbacks(DozeScrimController.this.mPulseOut);
            DozeScrimController.this.mPulseOut.run();
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mPulseOutFinished = new Runnable() {
        public void run() {
            if (DozeScrimController.DEBUG) {
                Log.d("DozeScrimController", "Pulse out finished");
            }
            DozeLog.tracePulseFinish();
            DozeScrimController.this.pulseFinished();
        }
    };
    /* access modifiers changed from: private */
    public int mPulseReason;
    private final ScrimController mScrimController;
    private boolean mWakeAndUnlocking;

    public DozeScrimController(ScrimController scrimController, Context context) {
        this.mContext = context;
        this.mScrimController = scrimController;
        this.mDozeParameters = new DozeParameters(context);
    }

    public void setDozing(boolean z, boolean z2) {
        if (this.mDozing != z) {
            this.mDozing = z;
            this.mWakeAndUnlocking = false;
            if (this.mDozing) {
                this.mDozingAborted = false;
                abortAnimations();
                float f = 1.0f;
                this.mScrimController.setDozeBehindAlpha(1.0f);
                ScrimController scrimController = this.mScrimController;
                if (this.mDozeParameters.getAlwaysOn()) {
                    f = 0.0f;
                }
                scrimController.setDozeInFrontAlpha(f);
                return;
            }
            cancelPulsing();
            if (z2) {
                startScrimAnimation(false, 0.0f, 700, Interpolators.LINEAR_OUT_SLOW_IN);
                startScrimAnimation(true, 0.0f, 700, Interpolators.LINEAR_OUT_SLOW_IN);
                return;
            }
            abortAnimations();
            this.mScrimController.setDozeBehindAlpha(0.0f);
            this.mScrimController.setDozeInFrontAlpha(0.0f);
        }
    }

    public void setWakeAndUnlocking() {
        if (!this.mWakeAndUnlocking) {
            this.mWakeAndUnlocking = true;
            this.mScrimController.setDozeBehindAlpha(0.0f);
            this.mScrimController.setDozeInFrontAlpha(0.0f);
        }
    }

    public void abortPulsing() {
        cancelPulsing();
        if (this.mDozing && !this.mWakeAndUnlocking) {
            float f = 1.0f;
            this.mScrimController.setDozeBehindAlpha(1.0f);
            ScrimController scrimController = this.mScrimController;
            if (this.mDozeParameters.getAlwaysOn() && !this.mDozingAborted) {
                f = 0.0f;
            }
            scrimController.setDozeInFrontAlpha(f);
        }
    }

    public void abortDoze() {
        this.mDozingAborted = true;
        abortPulsing();
    }

    public void onScreenTurnedOn() {
        if (isPulsing()) {
            int i = this.mPulseReason;
            boolean z = i == 3 || i == 4;
            startScrimAnimation(true, 0.0f, (long) this.mDozeParameters.getPulseInDuration(z), z ? Interpolators.LINEAR_OUT_SLOW_IN : Interpolators.ALPHA_OUT, this.mPulseInFinished);
        }
    }

    public boolean isPulsing() {
        return this.mPulseCallback != null;
    }

    public void extendPulse() {
        this.mHandler.removeCallbacks(this.mPulseOut);
    }

    private void cancelPulsing() {
        if (DEBUG) {
            Log.d("DozeScrimController", "Cancel pulsing");
        }
        if (this.mPulseCallback != null) {
            this.mHandler.removeCallbacks(this.mPulseIn);
            this.mHandler.removeCallbacks(this.mPulseOut);
            this.mHandler.removeCallbacks(this.mPulseOutExtended);
            pulseFinished();
        }
    }

    /* access modifiers changed from: private */
    public void pulseStarted() {
        DozeHost$PulseCallback dozeHost$PulseCallback = this.mPulseCallback;
        if (dozeHost$PulseCallback != null) {
            dozeHost$PulseCallback.onPulseStarted();
        }
    }

    /* access modifiers changed from: private */
    public void pulseFinished() {
        DozeHost$PulseCallback dozeHost$PulseCallback = this.mPulseCallback;
        if (dozeHost$PulseCallback != null) {
            dozeHost$PulseCallback.onPulseFinished();
            this.mPulseCallback = null;
        }
    }

    private void abortAnimations() {
        Animator animator = this.mInFrontAnimator;
        if (animator != null) {
            animator.cancel();
        }
        Animator animator2 = this.mBehindAnimator;
        if (animator2 != null) {
            animator2.cancel();
        }
    }

    private void startScrimAnimation(boolean z, float f, long j, Interpolator interpolator) {
        startScrimAnimation(z, f, j, interpolator, (Runnable) null);
    }

    /* access modifiers changed from: private */
    public void startScrimAnimation(final boolean z, float f, long j, Interpolator interpolator, final Runnable runnable) {
        Animator currentAnimator = getCurrentAnimator(z);
        if (currentAnimator != null) {
            if (getCurrentTarget(z) != f) {
                currentAnimator.cancel();
            } else {
                return;
            }
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{getDozeAlpha(z), f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                DozeScrimController.this.setDozeAlpha(z, ((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(j);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                DozeScrimController.this.setCurrentAnimator(z, (Animator) null);
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        ofFloat.start();
        setCurrentAnimator(z, ofFloat);
        setCurrentTarget(z, f);
    }

    private float getCurrentTarget(boolean z) {
        return z ? this.mInFrontTarget : this.mBehindTarget;
    }

    private void setCurrentTarget(boolean z, float f) {
        if (z) {
            this.mInFrontTarget = f;
        } else {
            this.mBehindTarget = f;
        }
    }

    private Animator getCurrentAnimator(boolean z) {
        return z ? this.mInFrontAnimator : this.mBehindAnimator;
    }

    /* access modifiers changed from: private */
    public void setCurrentAnimator(boolean z, Animator animator) {
        if (z) {
            this.mInFrontAnimator = animator;
        } else {
            this.mBehindAnimator = animator;
        }
    }

    /* access modifiers changed from: private */
    public void setDozeAlpha(boolean z, float f) {
        if (!this.mWakeAndUnlocking) {
            if (z) {
                this.mScrimController.setDozeInFrontAlpha(f);
            } else {
                this.mScrimController.setDozeBehindAlpha(f);
            }
        }
    }

    private float getDozeAlpha(boolean z) {
        if (z) {
            return this.mScrimController.getDozeInFrontAlpha();
        }
        return this.mScrimController.getDozeBehindAlpha();
    }
}
