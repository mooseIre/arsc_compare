package com.android.systemui.assist;

import android.content.Context;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import dagger.Lazy;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public final class AssistHandleLikeHomeBehavior implements AssistHandleBehaviorController.BehaviorController {
    private AssistHandleCallbacks mAssistHandleCallbacks;
    private boolean mIsAwake;
    private boolean mIsDozing;
    private boolean mIsHomeHandleHiding;
    private final Lazy<StatusBarStateController> mStatusBarStateController;
    private final StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() {
        /* class com.android.systemui.assist.AssistHandleLikeHomeBehavior.AnonymousClass1 */

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozingChanged(boolean z) {
            AssistHandleLikeHomeBehavior.this.handleDozingChanged(z);
        }
    };
    private final Lazy<SysUiState> mSysUiFlagContainer;
    private final SysUiState.SysUiStateCallback mSysUiStateCallback = new SysUiState.SysUiStateCallback() {
        /* class com.android.systemui.assist.$$Lambda$AssistHandleLikeHomeBehavior$vrkdH0qzooln_t3TWfQihWw8WM */

        @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
        public final void onSystemUiStateChanged(int i) {
            AssistHandleLikeHomeBehavior.m9lambda$vrkdH0qzooln_t3TWfQihWw8WM(AssistHandleLikeHomeBehavior.this, i);
        }
    };
    private final Lazy<WakefulnessLifecycle> mWakefulnessLifecycle;
    private final WakefulnessLifecycle.Observer mWakefulnessLifecycleObserver = new WakefulnessLifecycle.Observer() {
        /* class com.android.systemui.assist.AssistHandleLikeHomeBehavior.AnonymousClass2 */

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedWakingUp() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedWakingUp() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(true);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onStartedGoingToSleep() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }

        @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
        public void onFinishedGoingToSleep() {
            AssistHandleLikeHomeBehavior.this.handleWakefullnessChanged(false);
        }
    };

    private static boolean isHomeHandleHiding(int i) {
        return (i & 2) != 0;
    }

    AssistHandleLikeHomeBehavior(Lazy<StatusBarStateController> lazy, Lazy<WakefulnessLifecycle> lazy2, Lazy<SysUiState> lazy3) {
        this.mStatusBarStateController = lazy;
        this.mWakefulnessLifecycle = lazy2;
        this.mSysUiFlagContainer = lazy3;
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks) {
        this.mAssistHandleCallbacks = assistHandleCallbacks;
        this.mIsDozing = this.mStatusBarStateController.get().isDozing();
        this.mStatusBarStateController.get().addCallback(this.mStatusBarStateListener);
        this.mIsAwake = this.mWakefulnessLifecycle.get().getWakefulness() == 2;
        this.mWakefulnessLifecycle.get().addObserver(this.mWakefulnessLifecycleObserver);
        this.mSysUiFlagContainer.get().addCallback(this.mSysUiStateCallback);
        callbackForCurrentState();
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeDeactivated() {
        this.mAssistHandleCallbacks = null;
        this.mStatusBarStateController.get().removeCallback(this.mStatusBarStateListener);
        this.mWakefulnessLifecycle.get().removeObserver(this.mWakefulnessLifecycleObserver);
        this.mSysUiFlagContainer.get().removeCallback(this.mSysUiStateCallback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDozingChanged(boolean z) {
        if (this.mIsDozing != z) {
            this.mIsDozing = z;
            callbackForCurrentState();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWakefullnessChanged(boolean z) {
        if (this.mIsAwake != z) {
            this.mIsAwake = z;
            callbackForCurrentState();
        }
    }

    /* access modifiers changed from: private */
    public void handleSystemUiStateChange(int i) {
        boolean isHomeHandleHiding = isHomeHandleHiding(i);
        if (this.mIsHomeHandleHiding != isHomeHandleHiding) {
            this.mIsHomeHandleHiding = isHomeHandleHiding;
            callbackForCurrentState();
        }
    }

    private void callbackForCurrentState() {
        if (this.mAssistHandleCallbacks != null) {
            if (this.mIsHomeHandleHiding || !isFullyAwake()) {
                this.mAssistHandleCallbacks.hide();
            } else {
                this.mAssistHandleCallbacks.showAndStay();
            }
        }
    }

    private boolean isFullyAwake() {
        return this.mIsAwake && !this.mIsDozing;
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "Current AssistHandleLikeHomeBehavior State:");
        printWriter.println(str + "   mIsDozing=" + this.mIsDozing);
        printWriter.println(str + "   mIsAwake=" + this.mIsAwake);
        printWriter.println(str + "   mIsHomeHandleHiding=" + this.mIsHomeHandleHiding);
    }
}
