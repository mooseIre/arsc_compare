package com.android.systemui.shared.system;

import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import java.util.ArrayList;
import java.util.List;

public class PinnedStackListenerForwarder extends IPinnedStackListener.Stub {
    private List<PinnedStackListener> mListeners = new ArrayList();

    public static class PinnedStackListener {
        public void onActionsChanged(ParceledListSlice parceledListSlice) {
        }

        public void onImeVisibilityChanged(boolean z, int i) {
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
        }

        public void onMinimizedStateChanged(boolean z) {
        }

        public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) {
        }

        public void onShelfVisibilityChanged(boolean z, int i) {
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

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) {
        for (PinnedStackListener onMovementBoundsChanged : this.mListeners) {
            onMovementBoundsChanged.onMovementBoundsChanged(rect, rect2, rect3, z, z2, i);
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        for (PinnedStackListener onImeVisibilityChanged : this.mListeners) {
            onImeVisibilityChanged.onImeVisibilityChanged(z, i);
        }
    }

    public void onShelfVisibilityChanged(boolean z, int i) {
        for (PinnedStackListener onShelfVisibilityChanged : this.mListeners) {
            onShelfVisibilityChanged.onShelfVisibilityChanged(z, i);
        }
    }

    public void onMinimizedStateChanged(boolean z) {
        for (PinnedStackListener onMinimizedStateChanged : this.mListeners) {
            onMinimizedStateChanged.onMinimizedStateChanged(z);
        }
    }

    public void onActionsChanged(ParceledListSlice parceledListSlice) {
        for (PinnedStackListener onActionsChanged : this.mListeners) {
            onActionsChanged.onActionsChanged(parceledListSlice);
        }
    }
}
