package com.android.systemui;

import android.os.RemoteException;
import android.util.Log;
import android.view.IDockedStackListener;
import android.view.WindowManagerGlobal;
import java.util.function.Consumer;

public class DockedStackExistsListener extends IDockedStackListener.Stub {
    private final Consumer<Boolean> mCallback;

    public void onAdjustedForImeChanged(boolean z, long j) throws RemoteException {
    }

    public void onDividerVisibilityChanged(boolean z) throws RemoteException {
    }

    public void onDockSideChanged(int i) throws RemoteException {
    }

    public void onDockedStackMinimizedChanged(boolean z, long j, boolean z2) throws RemoteException {
    }

    private DockedStackExistsListener(Consumer<Boolean> consumer) {
        this.mCallback = consumer;
    }

    public void onDockedStackExistsChanged(boolean z) throws RemoteException {
        this.mCallback.accept(Boolean.valueOf(z));
    }

    public static void register(Consumer<Boolean> consumer) {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(new DockedStackExistsListener(consumer));
        } catch (RemoteException e) {
            Log.e("DockedStackExistsListener", "Failed registering docked stack exists listener", e);
        }
    }
}
