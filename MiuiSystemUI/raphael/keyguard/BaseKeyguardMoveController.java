package com.android.keyguard;

import android.view.ViewGroup;
import com.android.systemui.plugins.IntentButtonProvider;

public class BaseKeyguardMoveController {
    public CallBack mCallBack;

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

    public BaseKeyguardMoveController(CallBack callBack) {
        this.mCallBack = callBack;
    }
}
