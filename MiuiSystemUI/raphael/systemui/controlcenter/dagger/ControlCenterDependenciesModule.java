package com.android.systemui.controlcenter.dagger;

import android.content.Context;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.phone.ExpandInfoControllerImpl;

public interface ControlCenterDependenciesModule {
    static ExpandInfoController provideExpandInfoController(Context context) {
        return new ExpandInfoControllerImpl(context);
    }
}
