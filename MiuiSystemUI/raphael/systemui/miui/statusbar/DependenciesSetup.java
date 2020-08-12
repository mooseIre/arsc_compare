package com.android.systemui.miui.statusbar;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class DependenciesSetup {
    private Context mContext;

    public void setContext(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public Handler getMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public Handler getTimeTickHandler() {
        HandlerThread handlerThread = new HandlerThread("TimeTick");
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    public Handler getScreenOffHandler() {
        HandlerThread handlerThread = new HandlerThread("ScreenOff");
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    public Looper getSysUIBgLooper() {
        HandlerThread handlerThread = new HandlerThread("SysUiBg");
        handlerThread.start();
        return handlerThread.getLooper();
    }

    public Looper getNetBgLooper() {
        HandlerThread handlerThread = new HandlerThread("SysUiNetBg");
        handlerThread.start();
        return handlerThread.getLooper();
    }

    public Looper getBtBgLooper() {
        HandlerThread handlerThread = new HandlerThread("SysUiBtBg");
        handlerThread.start();
        return handlerThread.getLooper();
    }
}
