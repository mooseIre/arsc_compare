package com.android.systemui.shared.system;

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
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onListenerRegistered(iPinnedStackController);
        }
    }

    public void onMovementBoundsChanged(boolean z) {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onMovementBoundsChanged(z);
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onImeVisibilityChanged(z, i);
        }
    }

    public void onActionsChanged(ParceledListSlice parceledListSlice) {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onActionsChanged(parceledListSlice);
        }
    }

    public void onActivityHidden(ComponentName componentName) {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onActivityHidden(componentName);
        }
    }

    public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onDisplayInfoChanged(displayInfo);
        }
    }

    public void onConfigurationChanged() {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onConfigurationChanged();
        }
    }

    public void onAspectRatioChanged(float f) {
        for (PinnedStackListener pinnedStackListener : this.mListeners) {
            pinnedStackListener.onAspectRatioChanged(f);
        }
    }
}
