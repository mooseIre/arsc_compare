package com.android.systemui.statusbar.notification;

import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: VisualStabilityManagerInjector.kt */
public final class VisualStabilityManagerInjector {
    private final ArrayList<VisualStabilityManagerInjector$Companion$Callback> mPanelVisibilityChangedCallbacks = new ArrayList<>();
    private boolean mPanelVisible;

    public final void addPanelVisibilityChangedCallback(@NotNull VisualStabilityManagerInjector$Companion$Callback visualStabilityManagerInjector$Companion$Callback) {
        Intrinsics.checkParameterIsNotNull(visualStabilityManagerInjector$Companion$Callback, "callback");
        if (!this.mPanelVisibilityChangedCallbacks.contains(visualStabilityManagerInjector$Companion$Callback)) {
            this.mPanelVisibilityChangedCallbacks.add(visualStabilityManagerInjector$Companion$Callback);
        }
    }

    public final void updateAllowedStates(boolean z, boolean z2, boolean z3) {
        boolean z4 = z && z2 && !z3;
        boolean z5 = this.mPanelVisible ^ z4;
        this.mPanelVisible = z4;
        if (z5) {
            Iterator<VisualStabilityManagerInjector$Companion$Callback> it = this.mPanelVisibilityChangedCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onVisibilityChanged(z4);
            }
        }
    }
}
