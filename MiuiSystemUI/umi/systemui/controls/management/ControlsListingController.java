package com.android.systemui.controls.management;

import android.content.ComponentName;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.statusbar.policy.CallbackController;
import com.android.systemui.util.UserAwareController;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsListingController.kt */
public interface ControlsListingController extends CallbackController<ControlsListingCallback>, UserAwareController {

    @FunctionalInterface
    /* compiled from: ControlsListingController.kt */
    public interface ControlsListingCallback {
        void onServicesUpdated(@NotNull List<ControlsServiceInfo> list);
    }

    @Nullable
    CharSequence getAppLabel(@NotNull ComponentName componentName);
}
