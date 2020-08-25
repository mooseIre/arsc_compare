package com.android.systemui.pip.phone;

import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import android.view.DisplayInfo;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import java.util.ArrayList;
import java.util.List;

public class PinnedStackListenerForwarder extends IPinnedStackListener.Stub {
    private List<PinnedStackListener> mListeners = new ArrayList();

    public static class PinnedStackListener {
        public void onActionsChanged(ParceledListSlice parceledListSlice) {
        }

        public void onActivityHidden(ComponentName componentName) {
        }

        public void onAspectRatioChanged(float f) {
        }

        public void onConfigurationChanged() {
        }

        public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        }

        public void onImeVisibilityChanged(boolean z, int i) {
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
        }

        public void onMovementBoundsChanged(boolean z) {
        }
    }

    public void addListener(PinnedStackListener pinnedStackListener) {
        this.mListeners.add(pinnedStackListener);
    }

    public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
        for (PinnedStackListener onListenerRegistered : this.mListeners) {
            onListenerRegistered.onListenerRegistered(iPinnedStackController);
        }
    }

    public void onMovementBoundsChanged(boolean z) {
        for (PinnedStackListener onMovementBoundsChanged : this.mListeners) {
            onMovementBoundsChanged.onMovementBoundsChanged(z);
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        for (PinnedStackListener onImeVisibilityChanged : this.mListeners) {
            onImeVisibilityChanged.onImeVisibilityChanged(z, i);
        }
    }

    public void onActionsChanged(ParceledListSlice parceledListSlice) {
        for (PinnedStackListener onActionsChanged : this.mListeners) {
            onActionsChanged.onActionsChanged(parceledListSlice);
        }
    }

    public void onActivityHidden(ComponentName componentName) {
        for (PinnedStackListener onActivityHidden : this.mListeners) {
            onActivityHidden.onActivityHidden(componentName);
        }
    }

    public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        for (PinnedStackListener onDisplayInfoChanged : this.mListeners) {
            onDisplayInfoChanged.onDisplayInfoChanged(displayInfo);
        }
    }

    public void onConfigurationChanged() {
        for (PinnedStackListener onConfigurationChanged : this.mListeners) {
            onConfigurationChanged.onConfigurationChanged();
        }
    }

    public void onAspectRatioChanged(float f) {
        for (PinnedStackListener onAspectRatioChanged : this.mListeners) {
            onAspectRatioChanged.onAspectRatioChanged(f);
        }
    }
}
