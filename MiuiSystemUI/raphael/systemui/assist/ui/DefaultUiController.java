package com.android.systemui.assist.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.metrics.LogMaker;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.assist.AssistantSessionEvent;
import com.android.systemui.statusbar.NavigationBarController;
import java.util.Locale;

public class DefaultUiController implements AssistManager.UiController {
    private static final boolean VERBOSE = (Build.TYPE.toLowerCase(Locale.ROOT).contains("debug") || Build.TYPE.toLowerCase(Locale.ROOT).equals("eng"));
    protected final AssistLogger mAssistLogger;
    private boolean mAttached = false;
    private ValueAnimator mInvocationAnimator = new ValueAnimator();
    private boolean mInvocationInProgress = false;
    protected InvocationLightsView mInvocationLightsView;
    private float mLastInvocationProgress = 0.0f;
    private final WindowManager.LayoutParams mLayoutParams;
    private final PathInterpolator mProgressInterpolator = new PathInterpolator(0.83f, 0.0f, 0.84f, 1.0f);
    protected final FrameLayout mRoot;
    private final WindowManager mWindowManager;

    public DefaultUiController(Context context, AssistLogger assistLogger) {
        this.mAssistLogger = assistLogger;
        this.mRoot = new FrameLayout(context);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -2, 0, 0, 2024, 808, -3);
        this.mLayoutParams = layoutParams;
        layoutParams.privateFlags = 64;
        layoutParams.gravity = 80;
        layoutParams.setFitInsetsTypes(0);
        this.mLayoutParams.setTitle("Assist");
        InvocationLightsView invocationLightsView = (InvocationLightsView) LayoutInflater.from(context).inflate(C0017R$layout.invocation_lights, (ViewGroup) this.mRoot, false);
        this.mInvocationLightsView = invocationLightsView;
        invocationLightsView.setColors(-16776961, -65536, -256, -16711936);
        this.mRoot.addView(this.mInvocationLightsView);
    }

    @Override // com.android.systemui.assist.AssistManager.UiController
    public void onInvocationProgress(int i, float f) {
        boolean z = this.mInvocationInProgress;
        if (f == 1.0f) {
            animateInvocationCompletion(i, 0.0f);
        } else if (f == 0.0f) {
            hide();
        } else {
            if (!z) {
                attach();
                this.mInvocationInProgress = true;
                updateAssistHandleVisibility();
            }
            setProgressInternal(i, f);
        }
        this.mLastInvocationProgress = f;
        logInvocationProgressMetrics(i, f, z);
    }

    @Override // com.android.systemui.assist.AssistManager.UiController
    public void onGestureCompletion(float f) {
        animateInvocationCompletion(1, f);
        logInvocationProgressMetrics(1, 1.0f, this.mInvocationInProgress);
    }

    public void hide() {
        detach();
        if (this.mInvocationAnimator.isRunning()) {
            this.mInvocationAnimator.cancel();
        }
        this.mInvocationLightsView.hide();
        this.mInvocationInProgress = false;
        updateAssistHandleVisibility();
    }

    /* access modifiers changed from: protected */
    public void logInvocationProgressMetrics(int i, float f, boolean z) {
        if (f == 1.0f && VERBOSE) {
            Log.v("DefaultUiController", "Invocation complete: type=" + i);
        }
        if (!z && f > 0.0f) {
            if (VERBOSE) {
                Log.v("DefaultUiController", "Invocation started: type=" + i);
            }
            this.mAssistLogger.reportAssistantInvocationEventFromLegacy(i, false, null, null);
            MetricsLogger.action(new LogMaker(1716).setType(4).setSubtype(((AssistManager) Dependency.get(AssistManager.class)).toLoggingSubType(i)));
        }
        ValueAnimator valueAnimator = this.mInvocationAnimator;
        if ((valueAnimator == null || !valueAnimator.isRunning()) && z && f == 0.0f) {
            if (VERBOSE) {
                Log.v("DefaultUiController", "Invocation cancelled: type=" + i);
            }
            this.mAssistLogger.reportAssistantSessionEvent(AssistantSessionEvent.ASSISTANT_SESSION_INVOCATION_CANCELLED);
            MetricsLogger.action(new LogMaker(1716).setType(5).setSubtype(1));
        }
    }

    private void updateAssistHandleVisibility() {
        AssistHandleViewController assistHandleViewController;
        NavigationBarController navigationBarController = (NavigationBarController) Dependency.get(NavigationBarController.class);
        if (navigationBarController == null) {
            assistHandleViewController = null;
        } else {
            assistHandleViewController = navigationBarController.getAssistHandlerViewController();
        }
        if (assistHandleViewController != null) {
            assistHandleViewController.lambda$setAssistHintBlocked$1(this.mInvocationInProgress);
        }
    }

    private void attach() {
        if (!this.mAttached) {
            this.mWindowManager.addView(this.mRoot, this.mLayoutParams);
            this.mAttached = true;
        }
    }

    private void detach() {
        if (this.mAttached) {
            this.mWindowManager.removeViewImmediate(this.mRoot);
            this.mAttached = false;
        }
    }

    private void setProgressInternal(int i, float f) {
        this.mInvocationLightsView.onInvocationProgress(this.mProgressInterpolator.getInterpolation(f));
    }

    private void animateInvocationCompletion(int i, float f) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mLastInvocationProgress, 1.0f);
        this.mInvocationAnimator = ofFloat;
        ofFloat.setStartDelay(1);
        this.mInvocationAnimator.setDuration(200L);
        this.mInvocationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(i) {
            /* class com.android.systemui.assist.ui.$$Lambda$DefaultUiController$DsyFMixn8vpgo7pkqARg9d_ZEVw */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DefaultUiController.this.lambda$animateInvocationCompletion$0$DefaultUiController(this.f$1, valueAnimator);
            }
        });
        this.mInvocationAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.assist.ui.DefaultUiController.AnonymousClass1 */

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                DefaultUiController.this.mInvocationInProgress = false;
                DefaultUiController.this.mLastInvocationProgress = 0.0f;
                DefaultUiController.this.hide();
            }
        });
        this.mInvocationAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateInvocationCompletion$0 */
    public /* synthetic */ void lambda$animateInvocationCompletion$0$DefaultUiController(int i, ValueAnimator valueAnimator) {
        setProgressInternal(i, ((Float) valueAnimator.getAnimatedValue()).floatValue());
    }
}
