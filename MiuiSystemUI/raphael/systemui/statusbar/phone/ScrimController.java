package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Trace;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.util.function.TriConsumer;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.C0015R$id;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dumpable;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.BlurUtils;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.AlarmTimeout;
import com.android.systemui.util.wakelock.DelayedWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;

public class ScrimController implements ViewTreeObserver.OnPreDrawListener, ColorExtractor.OnColorsChangedListener, Dumpable {
    private static final boolean DEBUG = Log.isLoggable("ScrimController", 3);
    private static final int TAG_END_ALPHA = C0015R$id.scrim_alpha_end;
    static final int TAG_KEY_ANIM = C0015R$id.scrim;
    private static final int TAG_START_ALPHA = C0015R$id.scrim_alpha_start;
    private boolean mAnimateChange;
    private long mAnimationDelay;
    private long mAnimationDuration = -1;
    private Animator.AnimatorListener mAnimatorListener;
    private float mBehindAlpha = -1.0f;
    private int mBehindTint;
    private boolean mBlankScreen;
    private Runnable mBlankingTransitionRunnable;
    private float mBubbleAlpha = -1.0f;
    private int mBubbleTint;
    private Callback mCallback;
    private final SysuiColorExtractor mColorExtractor;
    private ColorExtractor.GradientColors mColors;
    private boolean mDarkenWhileDragging;
    private final float mDefaultScrimAlpha;
    private final DockManager mDockManager;
    private final DozeParameters mDozeParameters;
    private boolean mExpansionAffectsAlpha = true;
    private float mExpansionFraction = 1.0f;
    private final Handler mHandler;
    private float mInFrontAlpha = -1.0f;
    private int mInFrontTint;
    private final Interpolator mInterpolator = new DecelerateInterpolator();
    private boolean mKeyguardOccluded;
    private final KeyguardStateController mKeyguardStateController;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final KeyguardVisibilityCallback mKeyguardVisibilityCallback;
    private boolean mNeedsDrawableColorUpdate;
    private Runnable mPendingFrameCallback;
    private boolean mScreenBlankingCallbackCalled;
    private boolean mScreenOn;
    private ScrimView mScrimBehind;
    private float mScrimBehindAlphaKeyguard = 0.2f;
    private ScrimView mScrimForBubble;
    private ScrimView mScrimInFront;
    private final TriConsumer<ScrimState, Float, ColorExtractor.GradientColors> mScrimStateListener;
    private Consumer<Integer> mScrimVisibleListener;
    private int mScrimsVisibility;
    private ScrimState mState = ScrimState.UNINITIALIZED;
    private final AlarmTimeout mTimeTicker;
    private boolean mTracking;
    private boolean mUpdatePending;
    private final WakeLock mWakeLock;
    private boolean mWakeLockHeld;
    private boolean mWallpaperSupportsAmbientMode;
    private boolean mWallpaperVisibilityTimedOut;

    public interface Callback {
        default void onCancelled() {
        }

        default void onDisplayBlanked() {
        }

        default void onFinished() {
        }

        default void onStart() {
        }
    }

    public void setCurrentUser(int i) {
    }

    public ScrimController(LightBarController lightBarController, DozeParameters dozeParameters, AlarmManager alarmManager, final KeyguardStateController keyguardStateController, DelayedWakeLock.Builder builder, Handler handler, KeyguardUpdateMonitor keyguardUpdateMonitor, SysuiColorExtractor sysuiColorExtractor, DockManager dockManager, BlurUtils blurUtils) {
        Objects.requireNonNull(lightBarController);
        this.mScrimStateListener = new TriConsumer() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$v3pYAGeeZEy0j9LKp92o1adNfrk */

            public final void accept(Object obj, Object obj2, Object obj3) {
                LightBarController.this.setScrimState((ScrimState) obj, ((Float) obj2).floatValue(), (ColorExtractor.GradientColors) obj3);
            }
        };
        this.mDefaultScrimAlpha = blurUtils.supportsBlursOnWindows() ? 0.54f : 0.85f;
        this.mKeyguardStateController = keyguardStateController;
        this.mDarkenWhileDragging = !keyguardStateController.canDismissLockScreen();
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mKeyguardVisibilityCallback = new KeyguardVisibilityCallback();
        this.mHandler = handler;
        this.mTimeTicker = new AlarmTimeout(alarmManager, new AlarmManager.OnAlarmListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$ZxOK9HbkOUnaEI0FKoidLb2saOY */

            public final void onAlarm() {
                ScrimController.this.onHideWallpaperTimeout();
            }
        }, "hide_aod_wallpaper", this.mHandler);
        builder.setHandler(this.mHandler);
        builder.setTag("Scrims");
        this.mWakeLock = builder.build();
        this.mDozeParameters = dozeParameters;
        this.mDockManager = dockManager;
        keyguardStateController.addCallback(new KeyguardStateController.Callback() {
            /* class com.android.systemui.statusbar.phone.ScrimController.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardFadingAwayChanged() {
                ScrimController.this.setKeyguardFadingAway(keyguardStateController.isKeyguardFadingAway(), keyguardStateController.getKeyguardFadingAwayDuration());
            }
        });
        this.mColorExtractor = sysuiColorExtractor;
        sysuiColorExtractor.addOnColorsChangedListener(this);
        this.mColors = this.mColorExtractor.getNeutralColors();
        this.mNeedsDrawableColorUpdate = true;
    }

    public void attachViews(ScrimView scrimView, ScrimView scrimView2, ScrimView scrimView3) {
        this.mScrimBehind = scrimView;
        this.mScrimInFront = scrimView2;
        this.mScrimForBubble = scrimView3;
        ScrimState[] values = ScrimState.values();
        for (int i = 0; i < values.length; i++) {
            values[i].init(this.mScrimInFront, this.mScrimBehind, this.mScrimForBubble, this.mDozeParameters, this.mDockManager);
            values[i].setScrimBehindAlphaKeyguard(this.mScrimBehindAlphaKeyguard);
            values[i].setDefaultScrimAlpha(this.mDefaultScrimAlpha);
        }
        this.mScrimBehind.setDefaultFocusHighlightEnabled(false);
        this.mScrimInFront.setDefaultFocusHighlightEnabled(false);
        this.mScrimForBubble.setDefaultFocusHighlightEnabled(false);
        updateScrims();
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardVisibilityCallback);
    }

    /* access modifiers changed from: package-private */
    public void setScrimVisibleListener(Consumer<Integer> consumer) {
        this.mScrimVisibleListener = consumer;
    }

    public void transitionTo(ScrimState scrimState) {
        transitionTo(scrimState, null);
    }

    public void transitionTo(ScrimState scrimState, Callback callback) {
        if (scrimState != this.mState) {
            if (DEBUG) {
                Log.d("ScrimController", "State changed to: " + scrimState);
            }
            if (scrimState != ScrimState.UNINITIALIZED) {
                ScrimState scrimState2 = this.mState;
                this.mState = scrimState;
                Trace.traceCounter(4096, "scrim_state", scrimState.ordinal());
                Callback callback2 = this.mCallback;
                if (callback2 != null) {
                    callback2.onCancelled();
                }
                this.mCallback = callback;
                scrimState.prepare(scrimState2);
                this.mScreenBlankingCallbackCalled = false;
                this.mAnimationDelay = 0;
                this.mBlankScreen = scrimState.getBlanksScreen();
                this.mAnimateChange = scrimState.getAnimateChange();
                this.mAnimationDuration = scrimState.getAnimationDuration();
                this.mInFrontTint = scrimState.getFrontTint();
                this.mBehindTint = scrimState.getBehindTint();
                this.mBubbleTint = scrimState.getBubbleTint();
                this.mInFrontAlpha = scrimState.getFrontAlpha();
                this.mBehindAlpha = scrimState.getBehindAlpha();
                this.mBubbleAlpha = scrimState.getBubbleAlpha();
                if (Float.isNaN(this.mBehindAlpha) || Float.isNaN(this.mInFrontAlpha)) {
                    throw new IllegalStateException("Scrim opacity is NaN for state: " + scrimState + ", front: " + this.mInFrontAlpha + ", back: " + this.mBehindAlpha);
                }
                applyExpansionToAlpha();
                boolean z = true;
                this.mScrimInFront.setFocusable(!scrimState.isLowPowerState());
                this.mScrimBehind.setFocusable(!scrimState.isLowPowerState());
                Runnable runnable = this.mPendingFrameCallback;
                if (runnable != null) {
                    this.mScrimBehind.removeCallbacks(runnable);
                    this.mPendingFrameCallback = null;
                }
                if (this.mHandler.hasCallbacks(this.mBlankingTransitionRunnable)) {
                    this.mHandler.removeCallbacks(this.mBlankingTransitionRunnable);
                    this.mBlankingTransitionRunnable = null;
                }
                if (scrimState == ScrimState.BRIGHTNESS_MIRROR) {
                    z = false;
                }
                this.mNeedsDrawableColorUpdate = z;
                if (this.mState.isLowPowerState()) {
                    holdWakeLock();
                }
                this.mWallpaperVisibilityTimedOut = false;
                if (shouldFadeAwayWallpaper()) {
                    DejankUtils.postAfterTraversal(new Runnable() {
                        /* class com.android.systemui.statusbar.phone.$$Lambda$ScrimController$YQJRwwTLFgaOweq9aHvS8f9csz8 */

                        public final void run() {
                            ScrimController.this.lambda$transitionTo$0$ScrimController();
                        }
                    });
                } else {
                    AlarmTimeout alarmTimeout = this.mTimeTicker;
                    Objects.requireNonNull(alarmTimeout);
                    DejankUtils.postAfterTraversal(new Runnable() {
                        /* class com.android.systemui.statusbar.phone.$$Lambda$0ZxUFLvlsGlm9ET2o7nSDW8wc5w */

                        public final void run() {
                            AlarmTimeout.this.cancel();
                        }
                    });
                }
                if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition() && this.mState == ScrimState.UNLOCKED) {
                    this.mScrimInFront.postOnAnimationDelayed(new Runnable() {
                        /* class com.android.systemui.statusbar.phone.$$Lambda$5DY8P9cXHTvbVZZOVB_VSCJUZk0 */

                        public final void run() {
                            ScrimController.this.scheduleUpdate();
                        }
                    }, 16);
                    this.mAnimationDelay = 100;
                } else if ((this.mDozeParameters.getAlwaysOn() || scrimState2 != ScrimState.AOD) && (this.mState != ScrimState.AOD || this.mDozeParameters.getDisplayNeedsBlanking())) {
                    scheduleUpdate();
                } else {
                    onPreDraw();
                }
                dispatchScrimState(this.mScrimBehind.getViewAlpha());
                return;
            }
            throw new IllegalArgumentException("Cannot change to UNINITIALIZED.");
        } else if (callback != null && this.mCallback != callback) {
            callback.onFinished();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$transitionTo$0 */
    public /* synthetic */ void lambda$transitionTo$0$ScrimController() {
        this.mTimeTicker.schedule(this.mDozeParameters.getWallpaperAodDuration(), 1);
    }

    private boolean shouldFadeAwayWallpaper() {
        if (this.mWallpaperSupportsAmbientMode && this.mState == ScrimState.AOD && (this.mDozeParameters.getAlwaysOn() || this.mDockManager.isDocked())) {
            return true;
        }
        return false;
    }

    public ScrimState getState() {
        return this.mState;
    }

    public void onTrackingStarted() {
        this.mTracking = true;
        this.mDarkenWhileDragging = true ^ this.mKeyguardStateController.canDismissLockScreen();
    }

    public void onExpandingFinished() {
        this.mTracking = false;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void onHideWallpaperTimeout() {
        ScrimState scrimState = this.mState;
        if (scrimState == ScrimState.AOD || scrimState == ScrimState.PULSING) {
            holdWakeLock();
            this.mWallpaperVisibilityTimedOut = true;
            this.mAnimateChange = true;
            this.mAnimationDuration = this.mDozeParameters.getWallpaperFadeOutDuration();
            scheduleUpdate();
        }
    }

    private void holdWakeLock() {
        if (!this.mWakeLockHeld) {
            WakeLock wakeLock = this.mWakeLock;
            if (wakeLock != null) {
                this.mWakeLockHeld = true;
                wakeLock.acquire("ScrimController");
                return;
            }
            Log.w("ScrimController", "Cannot hold wake lock, it has not been set yet");
        }
    }

    public void setPanelExpansion(float f) {
        if (Float.isNaN(f)) {
            throw new IllegalArgumentException("Fraction should not be NaN");
        } else if (this.mExpansionFraction != f) {
            this.mExpansionFraction = f;
            ScrimState scrimState = this.mState;
            if ((scrimState == ScrimState.UNLOCKED || scrimState == ScrimState.KEYGUARD || scrimState == ScrimState.PULSING || scrimState == ScrimState.BUBBLE_EXPANDED) && this.mExpansionAffectsAlpha) {
                applyAndDispatchExpansion();
            }
        }
    }

    private void setOrAdaptCurrentAnimation(View view) {
        float currentScrimAlpha = getCurrentScrimAlpha(view);
        if (isAnimating(view)) {
            ValueAnimator valueAnimator = (ValueAnimator) view.getTag(TAG_KEY_ANIM);
            view.setTag(TAG_START_ALPHA, Float.valueOf(((Float) view.getTag(TAG_START_ALPHA)).floatValue() + (currentScrimAlpha - ((Float) view.getTag(TAG_END_ALPHA)).floatValue())));
            view.setTag(TAG_END_ALPHA, Float.valueOf(currentScrimAlpha));
            valueAnimator.setCurrentPlayTime(valueAnimator.getCurrentPlayTime());
            return;
        }
        updateScrimColor(view, currentScrimAlpha, getCurrentScrimTint(view));
    }

    private void applyExpansionToAlpha() {
        if (this.mExpansionAffectsAlpha) {
            ScrimState scrimState = this.mState;
            if (scrimState == ScrimState.UNLOCKED || scrimState == ScrimState.BUBBLE_EXPANDED) {
                this.mBehindAlpha = ((float) Math.pow((double) getInterpolatedFraction(), 0.800000011920929d)) * this.mDefaultScrimAlpha;
                this.mInFrontAlpha = 0.0f;
            } else if (scrimState == ScrimState.KEYGUARD || scrimState == ScrimState.PULSING) {
                float interpolatedFraction = getInterpolatedFraction();
                float behindAlpha = this.mState.getBehindAlpha();
                if (this.mDarkenWhileDragging) {
                    this.mBehindAlpha = MathUtils.lerp(this.mDefaultScrimAlpha, behindAlpha, interpolatedFraction);
                    this.mInFrontAlpha = this.mState.getFrontAlpha();
                } else {
                    this.mBehindAlpha = MathUtils.lerp(0.0f, behindAlpha, interpolatedFraction);
                    this.mInFrontAlpha = this.mState.getFrontAlpha();
                }
                this.mBehindTint = ColorUtils.blendARGB(ScrimState.BOUNCER.getBehindTint(), this.mState.getBehindTint(), interpolatedFraction);
            }
            if (Float.isNaN(this.mBehindAlpha) || Float.isNaN(this.mInFrontAlpha)) {
                throw new IllegalStateException("Scrim opacity is NaN for state: " + this.mState + ", front: " + this.mInFrontAlpha + ", back: " + this.mBehindAlpha);
            }
        }
    }

    private void applyAndDispatchExpansion() {
        applyExpansionToAlpha();
        if (!this.mUpdatePending) {
            setOrAdaptCurrentAnimation(this.mScrimBehind);
            setOrAdaptCurrentAnimation(this.mScrimInFront);
            setOrAdaptCurrentAnimation(this.mScrimForBubble);
            dispatchScrimState(this.mScrimBehind.getViewAlpha());
            if (this.mWallpaperVisibilityTimedOut) {
                this.mWallpaperVisibilityTimedOut = false;
                DejankUtils.postAfterTraversal(new Runnable() {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$ScrimController$qcAzw93VG0gxAU1AfapPWpIf3aU */

                    public final void run() {
                        ScrimController.this.lambda$applyAndDispatchExpansion$1$ScrimController();
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyAndDispatchExpansion$1 */
    public /* synthetic */ void lambda$applyAndDispatchExpansion$1$ScrimController() {
        this.mTimeTicker.schedule(this.mDozeParameters.getWallpaperAodDuration(), 1);
    }

    public void setAodFrontScrimAlpha(float f) {
        if (this.mInFrontAlpha != f && shouldUpdateFrontScrimAlpha()) {
            this.mInFrontAlpha = f;
            updateScrims();
        }
        ScrimState.AOD.setAodFrontScrimAlpha(f);
        ScrimState.PULSING.setAodFrontScrimAlpha(f);
    }

    private boolean shouldUpdateFrontScrimAlpha() {
        if ((this.mState != ScrimState.AOD || (!this.mDozeParameters.getAlwaysOn() && !this.mDockManager.isDocked())) && this.mState != ScrimState.PULSING) {
            return false;
        }
        return true;
    }

    public void setWakeLockScreenSensorActive(boolean z) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setWakeLockScreenSensorActive(z);
        }
        ScrimState scrimState2 = this.mState;
        if (scrimState2 == ScrimState.PULSING) {
            float behindAlpha = scrimState2.getBehindAlpha();
            if (this.mBehindAlpha != behindAlpha) {
                this.mBehindAlpha = behindAlpha;
                if (!Float.isNaN(behindAlpha)) {
                    updateScrims();
                    return;
                }
                throw new IllegalStateException("Scrim opacity is NaN for state: " + this.mState + ", back: " + this.mBehindAlpha);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void scheduleUpdate() {
        ScrimView scrimView;
        if (!this.mUpdatePending && (scrimView = this.mScrimBehind) != null) {
            scrimView.invalidate();
            this.mScrimBehind.getViewTreeObserver().addOnPreDrawListener(this);
            this.mUpdatePending = true;
        }
    }

    /* access modifiers changed from: protected */
    public void updateScrims() {
        boolean z = true;
        if (this.mNeedsDrawableColorUpdate) {
            this.mNeedsDrawableColorUpdate = false;
            boolean z2 = this.mScrimInFront.getViewAlpha() != 0.0f && !this.mBlankScreen;
            boolean z3 = this.mScrimBehind.getViewAlpha() != 0.0f && !this.mBlankScreen;
            boolean z4 = this.mScrimForBubble.getViewAlpha() != 0.0f && !this.mBlankScreen;
            this.mScrimInFront.setColors(this.mColors, z2);
            ScrimState scrimState = this.mState;
            if (scrimState == ScrimState.BOUNCER || scrimState == ScrimState.BOUNCER_SCRIMMED || scrimState == ScrimState.KEYGUARD) {
                this.mScrimBehind.setColors(this.mColorExtractor.getDarkColors(), z3);
            } else {
                this.mScrimBehind.setColors(this.mColors, z3);
            }
            this.mScrimForBubble.setColors(this.mColors, z4);
            ColorUtils.calculateMinimumBackgroundAlpha(this.mColors.supportsDarkText() ? -16777216 : -1, this.mColors.getMainColor(), 4.5f);
            dispatchScrimState(this.mScrimBehind.getViewAlpha());
        }
        ScrimState scrimState2 = this.mState;
        boolean z5 = (scrimState2 == ScrimState.AOD || scrimState2 == ScrimState.PULSING) && this.mWallpaperVisibilityTimedOut;
        ScrimState scrimState3 = this.mState;
        if (!(scrimState3 == ScrimState.PULSING || scrimState3 == ScrimState.AOD) || !this.mKeyguardOccluded) {
            z = false;
        }
        if (z5 || z) {
            this.mBehindAlpha = 1.0f;
        }
        setScrimAlpha(this.mScrimInFront, this.mInFrontAlpha);
        setScrimAlpha(this.mScrimBehind, this.mBehindAlpha);
        setScrimAlpha(this.mScrimForBubble, this.mBubbleAlpha);
        onFinished();
        dispatchScrimsVisible();
    }

    private void dispatchScrimState(float f) {
        this.mScrimStateListener.accept(this.mState, Float.valueOf(f), this.mScrimInFront.getColors());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchScrimsVisible() {
        int i = (this.mScrimInFront.getViewAlpha() == 1.0f || this.mScrimBehind.getViewAlpha() == 1.0f) ? 2 : (this.mScrimInFront.getViewAlpha() == 0.0f && this.mScrimBehind.getViewAlpha() == 0.0f) ? 0 : 1;
        if (this.mScrimsVisibility != i) {
            this.mScrimsVisibility = i;
            this.mScrimVisibleListener.accept(Integer.valueOf(i));
        }
    }

    private float getInterpolatedFraction() {
        float f = (this.mExpansionFraction * 1.2f) - 0.2f;
        if (f <= 0.0f) {
            return 0.0f;
        }
        return (float) (1.0d - ((1.0d - Math.cos(Math.pow((double) (1.0f - f), 2.0d) * 3.141590118408203d)) * 0.5d));
    }

    private void setScrimAlpha(ScrimView scrimView, float f) {
        boolean z = false;
        if (f == 0.0f) {
            scrimView.setClickable(false);
        } else {
            if (this.mState != ScrimState.AOD) {
                z = true;
            }
            scrimView.setClickable(z);
        }
        updateScrim(scrimView, f);
    }

    private String getScrimName(ScrimView scrimView) {
        if (scrimView == this.mScrimInFront) {
            return "front_scrim";
        }
        if (scrimView == this.mScrimBehind) {
            return "back_scrim";
        }
        return scrimView == this.mScrimForBubble ? "bubble_scrim" : "unknown_scrim";
    }

    private void updateScrimColor(View view, float f, int i) {
        float max = Math.max(0.0f, Math.min(1.0f, f));
        if (view instanceof ScrimView) {
            ScrimView scrimView = (ScrimView) view;
            Trace.traceCounter(4096, getScrimName(scrimView) + "_alpha", (int) (255.0f * max));
            Trace.traceCounter(4096, getScrimName(scrimView) + "_tint", Color.alpha(i));
            scrimView.setTint(i);
            scrimView.setViewAlpha(max);
        } else {
            view.setAlpha(max);
        }
        dispatchScrimsVisible();
    }

    private void startScrimAnimation(final View view, float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        Animator.AnimatorListener animatorListener = this.mAnimatorListener;
        if (animatorListener != null) {
            ofFloat.addListener(animatorListener);
        }
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(view, view instanceof ScrimView ? ((ScrimView) view).getTint() : 0) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$ScrimController$pQ1ZzyQHHAbZJylpLDQQk40ggTo */
            public final /* synthetic */ View f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ScrimController.this.lambda$startScrimAnimation$2$ScrimController(this.f$1, this.f$2, valueAnimator);
            }
        });
        ofFloat.setInterpolator(this.mInterpolator);
        ofFloat.setStartDelay(this.mAnimationDelay);
        ofFloat.setDuration(this.mAnimationDuration);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.phone.ScrimController.AnonymousClass2 */
            private Callback lastCallback = ScrimController.this.mCallback;

            public void onAnimationEnd(Animator animator) {
                view.setTag(ScrimController.TAG_KEY_ANIM, null);
                ScrimController.this.onFinished(this.lastCallback);
                ScrimController.this.dispatchScrimsVisible();
            }
        });
        view.setTag(TAG_START_ALPHA, Float.valueOf(f));
        view.setTag(TAG_END_ALPHA, Float.valueOf(getCurrentScrimAlpha(view)));
        view.setTag(TAG_KEY_ANIM, ofFloat);
        ofFloat.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startScrimAnimation$2 */
    public /* synthetic */ void lambda$startScrimAnimation$2$ScrimController(View view, int i, ValueAnimator valueAnimator) {
        float floatValue = ((Float) view.getTag(TAG_START_ALPHA)).floatValue();
        float floatValue2 = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        updateScrimColor(view, MathUtils.constrain(MathUtils.lerp(floatValue, getCurrentScrimAlpha(view), floatValue2), 0.0f, 1.0f), ColorUtils.blendARGB(i, getCurrentScrimTint(view), floatValue2));
        dispatchScrimsVisible();
    }

    private float getCurrentScrimAlpha(View view) {
        if (this.mWallpaperSupportsAmbientMode) {
            return 0.0f;
        }
        if (view == this.mScrimInFront) {
            return this.mInFrontAlpha;
        }
        if (view == this.mScrimBehind) {
            return this.mBehindAlpha;
        }
        if (view == this.mScrimForBubble) {
            return this.mBubbleAlpha;
        }
        throw new IllegalArgumentException("Unknown scrim view");
    }

    private int getCurrentScrimTint(View view) {
        if (view == this.mScrimInFront) {
            return this.mInFrontTint;
        }
        if (view == this.mScrimBehind) {
            return this.mBehindTint;
        }
        if (view == this.mScrimForBubble) {
            return this.mBubbleTint;
        }
        throw new IllegalArgumentException("Unknown scrim view");
    }

    public boolean onPreDraw() {
        this.mScrimBehind.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mUpdatePending = false;
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onStart();
        }
        updateScrims();
        return true;
    }

    private void onFinished() {
        onFinished(this.mCallback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onFinished(Callback callback) {
        if (this.mPendingFrameCallback == null) {
            if (!isAnimating(this.mScrimBehind) && !isAnimating(this.mScrimInFront) && !isAnimating(this.mScrimForBubble)) {
                if (this.mWakeLockHeld) {
                    this.mWakeLock.release("ScrimController");
                    this.mWakeLockHeld = false;
                }
                if (callback != null) {
                    callback.onFinished();
                    if (callback == this.mCallback) {
                        this.mCallback = null;
                    }
                }
                if (this.mState == ScrimState.UNLOCKED) {
                    this.mInFrontTint = 0;
                    this.mBehindTint = 0;
                    this.mBubbleTint = 0;
                    updateScrimColor(this.mScrimInFront, this.mInFrontAlpha, 0);
                    updateScrimColor(this.mScrimBehind, this.mBehindAlpha, this.mBehindTint);
                    updateScrimColor(this.mScrimForBubble, this.mBubbleAlpha, this.mBubbleTint);
                }
            } else if (callback != null && callback != this.mCallback) {
                callback.onFinished();
            }
        }
    }

    private boolean isAnimating(View view) {
        return view.getTag(TAG_KEY_ANIM) != null;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.mAnimatorListener = animatorListener;
    }

    private void updateScrim(ScrimView scrimView, float f) {
        Callback callback;
        float viewAlpha = scrimView.getViewAlpha();
        ValueAnimator valueAnimator = (ValueAnimator) ViewState.getChildTag(scrimView, TAG_KEY_ANIM);
        if (valueAnimator != null) {
            cancelAnimator(valueAnimator);
        }
        if (this.mPendingFrameCallback == null) {
            if (this.mBlankScreen) {
                blankDisplay();
                return;
            }
            boolean z = true;
            if (!this.mScreenBlankingCallbackCalled && (callback = this.mCallback) != null) {
                callback.onDisplayBlanked();
                this.mScreenBlankingCallbackCalled = true;
            }
            if (scrimView == this.mScrimBehind) {
                dispatchScrimState(f);
            }
            boolean z2 = f != viewAlpha;
            if (scrimView.getTint() == getCurrentScrimTint(scrimView)) {
                z = false;
            }
            if (!z2 && !z) {
                return;
            }
            if (this.mAnimateChange) {
                startScrimAnimation(scrimView, viewAlpha);
            } else {
                updateScrimColor(scrimView, f, getCurrentScrimTint(scrimView));
            }
        }
    }

    private void cancelAnimator(ValueAnimator valueAnimator) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    private void blankDisplay() {
        updateScrimColor(this.mScrimInFront, 1.0f, -16777216);
        $$Lambda$ScrimController$ag08GXJhpSWypcA8hrLE9y1Zo r0 = new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$ScrimController$ag08GXJhpSWypcA8hrLE9y1Zo */

            public final void run() {
                ScrimController.this.lambda$blankDisplay$4$ScrimController();
            }
        };
        this.mPendingFrameCallback = r0;
        doOnTheNextFrame(r0);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$blankDisplay$4 */
    public /* synthetic */ void lambda$blankDisplay$4$ScrimController() {
        Callback callback = this.mCallback;
        if (callback != null) {
            callback.onDisplayBlanked();
            this.mScreenBlankingCallbackCalled = true;
        }
        this.mBlankingTransitionRunnable = new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$ScrimController$naUAB1OlntOHTCtGQlLQ0dSkAuw */

            public final void run() {
                ScrimController.this.lambda$blankDisplay$3$ScrimController();
            }
        };
        int i = this.mScreenOn ? 32 : 500;
        if (DEBUG) {
            Log.d("ScrimController", "Fading out scrims with delay: " + i);
        }
        this.mHandler.postDelayed(this.mBlankingTransitionRunnable, (long) i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$blankDisplay$3 */
    public /* synthetic */ void lambda$blankDisplay$3$ScrimController() {
        this.mBlankingTransitionRunnable = null;
        this.mPendingFrameCallback = null;
        this.mBlankScreen = false;
        updateScrims();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void doOnTheNextFrame(Runnable runnable) {
        this.mScrimBehind.postOnAnimationDelayed(runnable, 32);
    }

    public void setScrimBehindChangeRunnable(Runnable runnable) {
        this.mScrimBehind.setChangeRunnable(runnable);
    }

    public void onColorsChanged(ColorExtractor colorExtractor, int i) {
        this.mColors = this.mColorExtractor.getNeutralColors();
        this.mNeedsDrawableColorUpdate = true;
        scheduleUpdate();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(" ScrimController: ");
        printWriter.print("  state: ");
        printWriter.println(this.mState);
        printWriter.print("  frontScrim:");
        printWriter.print(" viewAlpha=");
        printWriter.print(this.mScrimInFront.getViewAlpha());
        printWriter.print(" alpha=");
        printWriter.print(this.mInFrontAlpha);
        printWriter.print(" tint=0x");
        printWriter.println(Integer.toHexString(this.mScrimInFront.getTint()));
        printWriter.print("  backScrim:");
        printWriter.print(" viewAlpha=");
        printWriter.print(this.mScrimBehind.getViewAlpha());
        printWriter.print(" alpha=");
        printWriter.print(this.mBehindAlpha);
        printWriter.print(" tint=0x");
        printWriter.println(Integer.toHexString(this.mScrimBehind.getTint()));
        printWriter.print("  bubbleScrim:");
        printWriter.print(" viewAlpha=");
        printWriter.print(this.mScrimForBubble.getViewAlpha());
        printWriter.print(" alpha=");
        printWriter.print(this.mBubbleAlpha);
        printWriter.print(" tint=0x");
        printWriter.println(Integer.toHexString(this.mScrimForBubble.getTint()));
        printWriter.print("  mTracking=");
        printWriter.println(this.mTracking);
        printWriter.print("  mDefaultScrimAlpha=");
        printWriter.println(this.mDefaultScrimAlpha);
        printWriter.print("  mExpansionFraction=");
        printWriter.println(this.mExpansionFraction);
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        ScrimState[] values;
        this.mWallpaperSupportsAmbientMode = z;
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setWallpaperSupportsAmbientMode(z);
        }
    }

    public void onScreenTurnedOn() {
        this.mScreenOn = true;
        if (this.mHandler.hasCallbacks(this.mBlankingTransitionRunnable)) {
            if (DEBUG) {
                Log.d("ScrimController", "Shorter blanking because screen turned on. All good.");
            }
            this.mHandler.removeCallbacks(this.mBlankingTransitionRunnable);
            this.mBlankingTransitionRunnable.run();
        }
    }

    public void onScreenTurnedOff() {
        this.mScreenOn = false;
    }

    public void setExpansionAffectsAlpha(boolean z) {
        this.mExpansionAffectsAlpha = z;
        if (z) {
            applyAndDispatchExpansion();
        }
    }

    public void setKeyguardOccluded(boolean z) {
        this.mKeyguardOccluded = z;
        updateScrims();
    }

    public void setHasBackdrop(boolean z) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setHasBackdrop(z);
        }
        ScrimState scrimState2 = this.mState;
        if (scrimState2 == ScrimState.AOD || scrimState2 == ScrimState.PULSING) {
            float behindAlpha = this.mState.getBehindAlpha();
            if (Float.isNaN(behindAlpha)) {
                throw new IllegalStateException("Scrim opacity is NaN for state: " + this.mState + ", back: " + this.mBehindAlpha);
            } else if (this.mBehindAlpha != behindAlpha) {
                this.mBehindAlpha = behindAlpha;
                updateScrims();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setKeyguardFadingAway(boolean z, long j) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setKeyguardFadingAway(z, j);
        }
    }

    public void setLaunchingAffordanceWithPreview(boolean z) {
        for (ScrimState scrimState : ScrimState.values()) {
            scrimState.setLaunchingAffordanceWithPreview(z);
        }
    }

    /* access modifiers changed from: private */
    public class KeyguardVisibilityCallback extends KeyguardUpdateMonitorCallback {
        private KeyguardVisibilityCallback() {
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            ScrimController.this.mNeedsDrawableColorUpdate = true;
            ScrimController.this.scheduleUpdate();
        }
    }
}
