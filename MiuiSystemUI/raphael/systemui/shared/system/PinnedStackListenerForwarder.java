package com.android.systemui.shared.system;

import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.RemoteException;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import java.util.ArrayList;
import java.util.List;

public class PinnedStackListenerForwarder extends IPinnedStackListener.Stub {
    private List<IPinnedStackListener> mListeners = new ArrayList();

    public void addListener(IPinnedStackListener iPinnedStackListener) {
        this.mListeners.add(iPinnedStackListener);
    }

    public void onListenerRegistered(IPinnedStackController iPinnedStackController) throws RemoteException {
        for (IPinnedStackListener onListenerRegistered : this.mListeners) {
            onListenerRegistered.onListenerRegistered(iPinnedStackController);
        }
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, boolean z, boolean z2, int i) throws RemoteException {
        for (IPinnedStackListener onMovementBoundsChanged : this.mListeners) {
            onMovementBoundsChanged.onMovementBoundsChanged(rect, rect2, rect3, z, z2, i);
        }
    }

    public void onImeVisibilityChanged(boolean z, int i) throws RemoteException {
        for (IPinnedStackListener onImeVisibilityChanged : this.mListeners) {
            onImeVisibilityChanged.onImeVisibilityChanged(z, i);
        }
    }

    public void onShelfVisibilityChanged(boolean z, int i) throws RemoteException {
        for (IPinnedStackListener onShelfVisibilityChanged : this.mListeners) {
            onShelfVisibilityChanged.onShelfVisibilityChanged(z, i);
        }
    }

    public void onMinimizedStateChanged(boolean z) throws RemoteException {
        for (IPinnedStackListener onMinimizedStateChanged : this.mListeners) {
            onMinimizedStateChanged.onMinimizedStateChanged(z);
        }
    }

    public void onActionsChanged(ParceledListSlice parceledListSlice) throws RemoteException {
        for (IPinnedStackListener onActionsChanged : this.mListeners) {
            onActionsChanged.onActionsChanged(parceledListSlice);
        }
    }
}
