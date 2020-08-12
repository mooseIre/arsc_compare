package com.android.systemui.recents;

import android.content.Context;
import com.android.systemui.recents.misc.SystemServicesProxy;

public class RecentsDebugFlags {
    public boolean isPagingEnabled() {
        return false;
    }

    public RecentsDebugFlags(Context context) {
    }

    public boolean isFastToggleRecentsEnabled() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (systemServices.hasFreeformWorkspaceSupport() || systemServices.isTouchExplorationEnabled()) {
        }
        return false;
    }
}
