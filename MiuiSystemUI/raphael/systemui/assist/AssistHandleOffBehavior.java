package com.android.systemui.assist;

import android.content.Context;
import com.android.systemui.assist.AssistHandleBehaviorController;

final class AssistHandleOffBehavior implements AssistHandleBehaviorController.BehaviorController {
    AssistHandleOffBehavior() {
    }

    @Override // com.android.systemui.assist.AssistHandleBehaviorController.BehaviorController
    public void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks) {
        assistHandleCallbacks.hide();
    }
}
