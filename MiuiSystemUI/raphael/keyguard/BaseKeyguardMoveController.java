package com.android.keyguard;

import android.view.ViewGroup;
import com.android.systemui.plugins.IntentButtonProvider;

public class BaseKeyguardMoveController {
    public CallBack mCallBack;

    public interface CallBack {
        ViewGroup getMoveIconLayout(boolean z) {
            return null;
        }

        IntentButtonProvider.IntentButton.IconState getMoveIconState(boolean z) {
            return null;
        }

        boolean isMoveInCenterScreen() {
            return false;
        }

        boolean isRightMove() {
            return false;
        }

        void onAnimUpdate(float f) {
        }

        void onBackAnimationEnd(boolean z) {
        }

        void onCancelAnimationEnd(boolean z, boolean z2) {
        }

        void onCompletedAnimationEnd(boolean z) {
        }

        void updateCanShowGxzw(boolean z) {
        }

        void updateSwipingInProgress(boolean z) {
        }
    }

    public BaseKeyguardMoveController(CallBack callBack) {
        this.mCallBack = callBack;
    }
}
