package com.android.systemui.controlcenter.policy;

import android.graphics.HardwareRenderer;
import android.os.MiuiProcess;
import android.os.Process;
import android.util.Log;
import android.util.Slog;
import android.view.ThreadedRenderer;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BoostHelper {
    private static BoostHelper sInjector = new BoostHelper();
    private int mOldRenderPriority;
    private int mOldUIPriority;
    private int mRenderThreadTid = 0;

    private BoostHelper() {
    }

    public static BoostHelper getInstance() {
        return sInjector;
    }

    private int getRenderThreadId(View view) {
        if (this.mRenderThreadTid == 0) {
            int i = 0;
            try {
                ThreadedRenderer threadedRenderer = view.getThreadedRenderer();
                Method declaredMethod = HardwareRenderer.class.getDeclaredMethod("nGetRenderThreadTid", Long.TYPE);
                declaredMethod.setAccessible(true);
                Field declaredField = HardwareRenderer.class.getDeclaredField("mNativeProxy");
                declaredField.setAccessible(true);
                i = ((Integer) declaredMethod.invoke(threadedRenderer, Long.valueOf(declaredField.getLong(threadedRenderer)))).intValue();
                Log.d("systemui_boost", "getRenderThreadId   tid=" + i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.mRenderThreadTid = i;
        }
        return this.mRenderThreadTid;
    }

    public void boostSystemUI(View view, boolean z) {
        int myPid = Process.myPid();
        int renderThreadId = getRenderThreadId(view);
        Log.d("systemui_boost", "control center panel visible = " + z + ";ui thread tid=" + myPid + ";render thread tid=" + renderThreadId);
        if (renderThreadId == 0) {
            Slog.e("systemui_boost", "render-thread tid = 0, do not boost");
        } else if (z) {
            try {
                this.mOldUIPriority = Process.getThreadPriority(myPid);
                Log.d("systemui_boost", "ui thread old priority=" + this.mOldUIPriority);
                try {
                    this.mOldRenderPriority = Process.getThreadPriority(renderThreadId);
                    Log.d("systemui_boost", "render thread old priority=" + this.mOldRenderPriority);
                    MiuiProcess.setThreadPriority(myPid, -20, "systemui_boost");
                    MiuiProcess.setThreadPriority(renderThreadId, -20, "systemui_boost");
                    Log.d("systemui_boost", "ui thread and render thread are boosted");
                } catch (IllegalArgumentException unused) {
                    Log.e("systemui_boost", "render thread tid=" + renderThreadId + ", does not exist");
                }
            } catch (IllegalArgumentException unused2) {
                Log.e("systemui_boost", "ui thread tid=" + myPid + ", does not exist");
            }
        } else {
            MiuiProcess.setThreadPriority(myPid, this.mOldUIPriority, "systemui_boost");
            MiuiProcess.setThreadPriority(renderThreadId, this.mOldRenderPriority, "systemui_boost");
            Log.d("systemui_boost", "ui thread and render thread are reset");
        }
    }
}
