package com.android.systemui.recents;

import android.content.Context;
import android.content.Intent;
import com.android.systemui.Dependency;
import com.android.systemui.model.SysUiState;
import com.android.systemui.statusbar.CommandQueue;
import com.miui.systemui.util.GestureObserver;

public class MiuiRecentProxy implements CommandQueue.Callbacks, GestureObserver.Callback {
    private CommandQueue mCommandQueue;
    private Context mContext;
    private boolean mIsFsgMode = false;
    private boolean mStatusBarHidden;

    public MiuiRecentProxy(Context context, CommandQueue commandQueue) {
        this.mContext = context;
        this.mCommandQueue = commandQueue;
    }

    public void start() {
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        GestureObserver gestureObserver = (GestureObserver) Dependency.get(GestureObserver.class);
        gestureObserver.addCallback(this);
        this.mIsFsgMode = gestureObserver.isFullscreenGesture();
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        boolean z2 = (i2 & 256) != 0;
        if (this.mStatusBarHidden != z2) {
            this.mStatusBarHidden = z2;
            if (this.mIsFsgMode) {
                Intent intent = new Intent();
                intent.setAction("com.android.systemui.fullscreen.statechange");
                intent.putExtra("isEnter", this.mStatusBarHidden);
                this.mContext.sendBroadcast(intent);
            }
        }
    }

    public static void setWindowStateInject(SysUiState sysUiState, int i, int i2, int i3) {
        boolean z = true;
        if (i2 == 1) {
            if (i3 == 0) {
                z = false;
            }
            sysUiState.setFlag(1048576, z);
            sysUiState.commitUpdate(i);
        }
    }

    @Override // com.miui.systemui.util.GestureObserver.Callback
    public void onGestureConfigChange(boolean z, boolean z2) {
        this.mIsFsgMode = z;
    }
}
