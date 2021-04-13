package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.view.ViewGroup;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.IntentButtonProvider;
import com.miui.systemui.util.HapticFeedBackImpl;

public class BaseKeyguardMoveController {
    public CallBack mCallBack;
    protected Context mContext;
    protected boolean mEnableErrorTips;
    protected float mInitialTouchX;
    protected float mInitialTouchY;
    protected boolean mIsOnIconTouchDown;
    protected boolean mMakeMistakes;
    protected float mMovingLength;
    protected final Point mScreenPoint = new Point();
    protected boolean mTouchDownInitial;

    public interface CallBack {
        default ViewGroup getMoveIconLayout(boolean z) {
            return null;
        }

        default IntentButtonProvider.IntentButton.IconState getMoveIconState(boolean z) {
            return null;
        }

        default boolean isMoveInCenterScreen() {
            return false;
        }

        default boolean isRightMove() {
            return false;
        }

        default void onAnimUpdate(float f) {
        }

        default void onBackAnimationEnd(boolean z) {
        }

        default void onCancelAnimationEnd(boolean z, boolean z2) {
        }

        default void onCompletedAnimationEnd(boolean z) {
        }

        default void updateCanShowGxzw(boolean z) {
        }

        default void updateSwipingInProgress(boolean z) {
        }
    }

    public BaseKeyguardMoveController(CallBack callBack, Context context) {
        this.mCallBack = callBack;
        this.mContext = context;
        context.getDisplay().getRealSize(this.mScreenPoint);
    }

    public boolean onTouchMove(float f, float f2) {
        if (!this.mTouchDownInitial || this.mIsOnIconTouchDown) {
            return false;
        }
        detectMistakes(f, f2);
        return false;
    }

    public void onTouchUp(float f, float f2) {
        if (this.mTouchDownInitial) {
            this.mTouchDownInitial = false;
            if (!this.mIsOnIconTouchDown) {
                startBackAnimationOfMistakeTouch();
            }
        }
        this.mMakeMistakes = false;
    }

    /* access modifiers changed from: protected */
    public void detectMistakes(float f, float f2) {
        if (this.mEnableErrorTips && !this.mMakeMistakes) {
            float f3 = this.mInitialTouchX;
            if (f > f3) {
                f = f3;
            }
            float f4 = this.mInitialTouchY;
            if (f2 > f4) {
                f2 = f4;
            }
            float sqrt = (float) Math.sqrt(Math.pow((double) (this.mInitialTouchX - f), 2.0d) + Math.pow((double) (this.mInitialTouchY - f2), 2.0d));
            this.mMovingLength = sqrt;
            if (sqrt / (((float) this.mScreenPoint.x) / 3.0f) > 0.3f) {
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extLongHapticFeedback(165, true, 60);
                startBackAnimationOfMistakeTouch();
                this.mMakeMistakes = true;
                return;
            }
            this.mCallBack.onAnimUpdate(sqrt);
        }
    }

    /* access modifiers changed from: protected */
    public void startBackAnimationOfMistakeTouch() {
        if (this.mEnableErrorTips && !this.mMakeMistakes) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mMovingLength, 0.0f);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.keyguard.$$Lambda$BaseKeyguardMoveController$B8xChlzPAx5RMQidPAAbvkVWIY */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    BaseKeyguardMoveController.this.lambda$startBackAnimationOfMistakeTouch$0$BaseKeyguardMoveController(valueAnimator);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter() {
                /* class com.android.keyguard.BaseKeyguardMoveController.AnonymousClass1 */

                public void onAnimationEnd(Animator animator) {
                    BaseKeyguardMoveController.this.mCallBack.onCancelAnimationEnd(true, true);
                }
            });
            ofFloat.start();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startBackAnimationOfMistakeTouch$0 */
    public /* synthetic */ void lambda$startBackAnimationOfMistakeTouch$0$BaseKeyguardMoveController(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mMovingLength = floatValue;
        this.mCallBack.onAnimUpdate(floatValue);
    }
}
