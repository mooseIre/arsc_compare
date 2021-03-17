package com.android.keyguard;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.clock.ClockManager;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.util.wakelock.KeepAwakeAnimationListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.TimeZone;

public class KeyguardClockSwitch extends RelativeLayout {
    private ViewGroup mBigClockContainer;
    private final ClockVisibilityTransition mBoldClockTransition;
    private ClockManager.ClockChangedListener mClockChangedListener = new ClockManager.ClockChangedListener() {
        /* class com.android.keyguard.$$Lambda$KeyguardClockSwitch$H31kNGqlEfEtZQZgrBtirdKZKc */

        @Override // com.android.keyguard.clock.ClockManager.ClockChangedListener
        public final void onClockChanged(ClockPlugin clockPlugin) {
            KeyguardClockSwitch.m2lambda$H31kNGqlEfEtZQZgrBtirdKZKc(KeyguardClockSwitch.this, clockPlugin);
        }
    };
    private final ClockManager mClockManager;
    private ClockPlugin mClockPlugin;
    private final ClockVisibilityTransition mClockTransition;
    private TextClock mClockView;
    private TextClock mClockViewBold;
    private int[] mColorPalette;
    private final ColorExtractor.OnColorsChangedListener mColorsListener = new ColorExtractor.OnColorsChangedListener() {
        /* class com.android.keyguard.$$Lambda$KeyguardClockSwitch$1K4q2TFTethGttjK4WWfYwlPoo */

        public final void onColorsChanged(ColorExtractor colorExtractor, int i) {
            KeyguardClockSwitch.this.lambda$new$0$KeyguardClockSwitch(colorExtractor, i);
        }
    };
    private float mDarkAmount;
    private boolean mHasVisibleNotifications;
    private View mKeyguardStatusArea;
    private boolean mShowingHeader;
    private FrameLayout mSmallClockFrame;
    private final StatusBarStateController.StateListener mStateListener = new StatusBarStateController.StateListener() {
        /* class com.android.keyguard.KeyguardClockSwitch.AnonymousClass1 */

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            KeyguardClockSwitch.this.mStatusBarState = i;
            KeyguardClockSwitch.this.updateBigClockVisibility();
        }
    };
    private int mStatusBarState;
    private final StatusBarStateController mStatusBarStateController;
    private boolean mSupportsDarkText;
    private final SysuiColorExtractor mSysuiColorExtractor;
    private final Transition mTransition;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$KeyguardClockSwitch(ColorExtractor colorExtractor, int i) {
        if ((i & 2) != 0) {
            updateColors();
        }
    }

    public KeyguardClockSwitch(Context context, AttributeSet attributeSet, StatusBarStateController statusBarStateController, SysuiColorExtractor sysuiColorExtractor, ClockManager clockManager) {
        super(context, attributeSet);
        this.mStatusBarStateController = statusBarStateController;
        this.mStatusBarState = statusBarStateController.getState();
        this.mSysuiColorExtractor = sysuiColorExtractor;
        this.mClockManager = clockManager;
        ClockVisibilityTransition clockVisibilityTransition = new ClockVisibilityTransition();
        clockVisibilityTransition.setCutoff(0.3f);
        this.mClockTransition = clockVisibilityTransition;
        clockVisibilityTransition.addTarget(C0015R$id.default_clock_view);
        ClockVisibilityTransition clockVisibilityTransition2 = new ClockVisibilityTransition();
        clockVisibilityTransition2.setCutoff(0.7f);
        this.mBoldClockTransition = clockVisibilityTransition2;
        clockVisibilityTransition2.addTarget(C0015R$id.default_clock_view_bold);
        this.mTransition = new TransitionSet().setOrdering(0).addTransition(this.mClockTransition).addTransition(this.mBoldClockTransition).setDuration(275L).setInterpolator((TimeInterpolator) Interpolators.LINEAR_OUT_SLOW_IN);
    }

    public boolean hasCustomClock() {
        return this.mClockPlugin != null;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mClockView = (TextClock) findViewById(C0015R$id.default_clock_view);
        this.mClockViewBold = (TextClock) findViewById(C0015R$id.default_clock_view_bold);
        this.mSmallClockFrame = (FrameLayout) findViewById(C0015R$id.clock_view);
        this.mKeyguardStatusArea = findViewById(C0015R$id.keyguard_status_area);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mClockManager.addOnClockChangedListener(this.mClockChangedListener);
        this.mStatusBarStateController.addCallback(this.mStateListener);
        this.mSysuiColorExtractor.addOnColorsChangedListener(this.mColorsListener);
        updateColors();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mClockManager.removeOnClockChangedListener(this.mClockChangedListener);
        this.mStatusBarStateController.removeCallback(this.mStateListener);
        this.mSysuiColorExtractor.removeOnColorsChangedListener(this.mColorsListener);
        setClockPlugin(null);
    }

    /* access modifiers changed from: private */
    public void setClockPlugin(ClockPlugin clockPlugin) {
        ViewGroup viewGroup;
        FrameLayout frameLayout;
        ClockPlugin clockPlugin2 = this.mClockPlugin;
        if (clockPlugin2 != null) {
            View view = clockPlugin2.getView();
            if (view != null && view.getParent() == (frameLayout = this.mSmallClockFrame)) {
                frameLayout.removeView(view);
            }
            ViewGroup viewGroup2 = this.mBigClockContainer;
            if (viewGroup2 != null) {
                viewGroup2.removeAllViews();
                updateBigClockVisibility();
            }
            this.mClockPlugin.onDestroyView();
            this.mClockPlugin = null;
        }
        if (clockPlugin == null) {
            if (this.mShowingHeader) {
                this.mClockView.setVisibility(8);
                this.mClockViewBold.setVisibility(0);
            } else {
                this.mClockView.setVisibility(0);
                this.mClockViewBold.setVisibility(4);
            }
            this.mKeyguardStatusArea.setVisibility(0);
            return;
        }
        View view2 = clockPlugin.getView();
        if (view2 != null) {
            this.mSmallClockFrame.addView(view2, -1, new ViewGroup.LayoutParams(-1, -2));
            this.mClockView.setVisibility(8);
            this.mClockViewBold.setVisibility(8);
        }
        View bigClockView = clockPlugin.getBigClockView();
        if (!(bigClockView == null || (viewGroup = this.mBigClockContainer) == null)) {
            viewGroup.addView(bigClockView);
            updateBigClockVisibility();
        }
        if (!clockPlugin.shouldShowStatusArea()) {
            this.mKeyguardStatusArea.setVisibility(8);
        }
        this.mClockPlugin = clockPlugin;
        clockPlugin.setStyle(getPaint().getStyle());
        this.mClockPlugin.setTextColor(getCurrentTextColor());
        this.mClockPlugin.setDarkAmount(this.mDarkAmount);
        int[] iArr = this.mColorPalette;
        if (iArr != null) {
            this.mClockPlugin.setColorPalette(this.mSupportsDarkText, iArr);
        }
    }

    public void setBigClockContainer(ViewGroup viewGroup) {
        View bigClockView;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (!(clockPlugin == null || viewGroup == null || (bigClockView = clockPlugin.getBigClockView()) == null)) {
            viewGroup.addView(bigClockView);
        }
        this.mBigClockContainer = viewGroup;
        updateBigClockVisibility();
    }

    public void setTextColor(int i) {
        this.mClockView.setTextColor(i);
        this.mClockViewBold.setTextColor(i);
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setTextColor(i);
        }
    }

    public void setShowCurrentUserTime(boolean z) {
        this.mClockView.setShowCurrentUserTime(z);
        this.mClockViewBold.setShowCurrentUserTime(z);
    }

    public void setTextSize(int i, float f) {
        this.mClockView.setTextSize(i, f);
    }

    public void setFormat12Hour(CharSequence charSequence) {
        this.mClockView.setFormat12Hour(charSequence);
        this.mClockViewBold.setFormat12Hour(charSequence);
    }

    public void setFormat24Hour(CharSequence charSequence) {
        this.mClockView.setFormat24Hour(charSequence);
        this.mClockViewBold.setFormat24Hour(charSequence);
    }

    public void setDarkAmount(float f) {
        this.mDarkAmount = f;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setDarkAmount(f);
        }
        updateBigClockAlpha();
    }

    /* access modifiers changed from: package-private */
    public void setHasVisibleNotifications(boolean z) {
        ViewGroup viewGroup;
        if (z != this.mHasVisibleNotifications) {
            this.mHasVisibleNotifications = z;
            if (this.mDarkAmount == 0.0f && (viewGroup = this.mBigClockContainer) != null) {
                TransitionManager.beginDelayedTransition(viewGroup, new Fade().setDuration(275).addTarget(this.mBigClockContainer));
            }
            updateBigClockAlpha();
        }
    }

    public Paint getPaint() {
        return this.mClockView.getPaint();
    }

    public int getCurrentTextColor() {
        return this.mClockView.getCurrentTextColor();
    }

    public float getTextSize() {
        return this.mClockView.getTextSize();
    }

    /* access modifiers changed from: package-private */
    public int getPreferredY(int i) {
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            return clockPlugin.getPreferredY(i);
        }
        return i / 2;
    }

    public void refresh() {
        this.mClockView.refreshTime();
        this.mClockViewBold.refreshTime();
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.onTimeTick();
        }
        if (Build.IS_DEBUGGABLE) {
            Log.d("KeyguardClockSwitch", "Updating clock: " + ((Object) this.mClockView.getText()));
        }
    }

    public void onTimeZoneChanged(TimeZone timeZone) {
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.onTimeZoneChanged(timeZone);
        }
    }

    private void updateColors() {
        ColorExtractor.GradientColors colors = this.mSysuiColorExtractor.getColors(2);
        this.mSupportsDarkText = colors.supportsDarkText();
        int[] colorPalette = colors.getColorPalette();
        this.mColorPalette = colorPalette;
        ClockPlugin clockPlugin = this.mClockPlugin;
        if (clockPlugin != null) {
            clockPlugin.setColorPalette(this.mSupportsDarkText, colorPalette);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBigClockVisibility() {
        if (this.mBigClockContainer != null) {
            int i = this.mStatusBarState;
            int i2 = 0;
            boolean z = true;
            if (!(i == 1 || i == 2)) {
                z = false;
            }
            if (!z || this.mBigClockContainer.getChildCount() == 0) {
                i2 = 8;
            }
            if (this.mBigClockContainer.getVisibility() != i2) {
                this.mBigClockContainer.setVisibility(i2);
            }
        }
    }

    private void updateBigClockAlpha() {
        if (this.mBigClockContainer != null) {
            float f = this.mHasVisibleNotifications ? this.mDarkAmount : 1.0f;
            this.mBigClockContainer.setAlpha(f);
            if (f == 0.0f) {
                this.mBigClockContainer.setVisibility(4);
            } else if (this.mBigClockContainer.getVisibility() == 4) {
                this.mBigClockContainer.setVisibility(0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setKeyguardShowingHeader(boolean z) {
        if (this.mShowingHeader != z) {
            this.mShowingHeader = z;
            if (!hasCustomClock()) {
                float dimensionPixelSize = (float) ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.widget_small_font_size);
                float dimensionPixelSize2 = (float) ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.widget_big_font_size);
                this.mClockTransition.setScale(dimensionPixelSize / dimensionPixelSize2);
                this.mBoldClockTransition.setScale(dimensionPixelSize2 / dimensionPixelSize);
                TransitionManager.endTransitions((ViewGroup) this.mClockView.getParent());
                if (z) {
                    this.mTransition.addListener(new TransitionListenerAdapter() {
                        /* class com.android.keyguard.KeyguardClockSwitch.AnonymousClass2 */

                        public void onTransitionEnd(Transition transition) {
                            super.onTransitionEnd(transition);
                            if (KeyguardClockSwitch.this.mShowingHeader) {
                                KeyguardClockSwitch.this.mClockView.setVisibility(8);
                            }
                            transition.removeListener(this);
                        }
                    });
                }
                TransitionManager.beginDelayedTransition((ViewGroup) this.mClockView.getParent(), this.mTransition);
                int i = 4;
                this.mClockView.setVisibility(z ? 4 : 0);
                TextClock textClock = this.mClockViewBold;
                if (z) {
                    i = 0;
                }
                textClock.setVisibility(i);
                int dimensionPixelSize3 = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(z ? C0012R$dimen.widget_vertical_padding_clock : C0012R$dimen.title_clock_padding);
                TextClock textClock2 = this.mClockView;
                textClock2.setPadding(textClock2.getPaddingLeft(), this.mClockView.getPaddingTop(), this.mClockView.getPaddingRight(), dimensionPixelSize3);
                TextClock textClock3 = this.mClockViewBold;
                textClock3.setPadding(textClock3.getPaddingLeft(), this.mClockViewBold.getPaddingTop(), this.mClockViewBold.getPaddingRight(), dimensionPixelSize3);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ClockManager.ClockChangedListener getClockChangedListener() {
        return this.mClockChangedListener;
    }

    /* access modifiers changed from: package-private */
    public StatusBarStateController.StateListener getStateListener() {
        return this.mStateListener;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardClockSwitch:");
        printWriter.println("  mClockPlugin: " + this.mClockPlugin);
        printWriter.println("  mClockView: " + this.mClockView);
        printWriter.println("  mClockViewBold: " + this.mClockViewBold);
        printWriter.println("  mSmallClockFrame: " + this.mSmallClockFrame);
        printWriter.println("  mBigClockContainer: " + this.mBigClockContainer);
        printWriter.println("  mKeyguardStatusArea: " + this.mKeyguardStatusArea);
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mShowingHeader: " + this.mShowingHeader);
        printWriter.println("  mSupportsDarkText: " + this.mSupportsDarkText);
        printWriter.println("  mColorPalette: " + Arrays.toString(this.mColorPalette));
    }

    /* access modifiers changed from: private */
    public class ClockVisibilityTransition extends Visibility {
        private float mCutoff;
        private float mScale;

        ClockVisibilityTransition() {
            setCutoff(1.0f);
            setScale(1.0f);
        }

        public ClockVisibilityTransition setCutoff(float f) {
            this.mCutoff = f;
            return this;
        }

        public ClockVisibilityTransition setScale(float f) {
            this.mScale = f;
            return this;
        }

        public void captureStartValues(TransitionValues transitionValues) {
            super.captureStartValues(transitionValues);
            captureVisibility(transitionValues);
        }

        public void captureEndValues(TransitionValues transitionValues) {
            super.captureStartValues(transitionValues);
            captureVisibility(transitionValues);
        }

        private void captureVisibility(TransitionValues transitionValues) {
            transitionValues.values.put("systemui:keyguard:visibility", Integer.valueOf(transitionValues.view.getVisibility()));
        }

        public Animator onAppear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
            if (!viewGroup.isShown()) {
                return null;
            }
            return createAnimator(view, this.mCutoff, 4, ((Integer) transitionValues2.values.get("systemui:keyguard:visibility")).intValue(), this.mScale, 1.0f);
        }

        public Animator onDisappear(ViewGroup viewGroup, View view, TransitionValues transitionValues, TransitionValues transitionValues2) {
            if (!viewGroup.isShown()) {
                return null;
            }
            return createAnimator(view, 1.0f - this.mCutoff, 0, ((Integer) transitionValues2.values.get("systemui:keyguard:visibility")).intValue(), 1.0f, this.mScale);
        }

        private Animator createAnimator(final View view, float f, final int i, final int i2, float f2, float f3) {
            view.setPivotY((float) (view.getHeight() - view.getPaddingBottom()));
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(f, view, i2, f2, f3) {
                /* class com.android.keyguard.$$Lambda$KeyguardClockSwitch$ClockVisibilityTransition$0YYk1dKss121y1dzD6OuOcSJduA */
                public final /* synthetic */ float f$0;
                public final /* synthetic */ View f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ float f$3;
                public final /* synthetic */ float f$4;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    KeyguardClockSwitch.ClockVisibilityTransition.lambda$createAnimator$0(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, valueAnimator);
                }
            });
            ofFloat.addListener(new KeepAwakeAnimationListener(this, KeyguardClockSwitch.this.getContext()) {
                /* class com.android.keyguard.KeyguardClockSwitch.ClockVisibilityTransition.AnonymousClass1 */

                @Override // com.android.systemui.util.wakelock.KeepAwakeAnimationListener
                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    view.setVisibility(i);
                }

                @Override // com.android.systemui.util.wakelock.KeepAwakeAnimationListener
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    animator.removeListener(this);
                }
            });
            addListener(new TransitionListenerAdapter(this) {
                /* class com.android.keyguard.KeyguardClockSwitch.ClockVisibilityTransition.AnonymousClass2 */

                public void onTransitionEnd(Transition transition) {
                    view.setVisibility(i2);
                    view.setScaleX(1.0f);
                    view.setScaleY(1.0f);
                    transition.removeListener(this);
                }
            });
            return ofFloat;
        }

        static /* synthetic */ void lambda$createAnimator$0(float f, View view, int i, float f2, float f3, ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();
            if (animatedFraction > f) {
                view.setVisibility(i);
            }
            float lerp = MathUtils.lerp(f2, f3, animatedFraction);
            view.setScaleX(lerp);
            view.setScaleY(lerp);
        }
    }
}
