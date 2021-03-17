package com.android.systemui.recents;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MiuiSettings;
import android.provider.Settings;
import com.android.systemui.model.SysUiState;
import com.android.systemui.statusbar.CommandQueue;

public class MiuiRecentProxy implements CommandQueue.Callbacks {
    private CommandQueue mCommandQueue;
    private Context mContext;
    private Handler mHandler;
    private boolean mIsFsgMode = false;
    private boolean mStatusBarHidden;

    public MiuiRecentProxy(Context context, CommandQueue commandQueue, Handler handler) {
        this.mContext = context;
        this.mCommandQueue = commandQueue;
        this.mHandler = handler;
    }

    public void start() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, new ContentObserver(this.mHandler) {
            /* class com.android.systemui.recents.MiuiRecentProxy.AnonymousClass1 */

            public void onChange(boolean z) {
                MiuiRecentProxy miuiRecentProxy = MiuiRecentProxy.this;
                miuiRecentProxy.mIsFsgMode = MiuiSettings.Global.getBoolean(miuiRecentProxy.mContext.getContentResolver(), "force_fsg_nav_bar");
            }
        });
        this.mIsFsgMode = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
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
}
