package com.android.systemui;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.os.BinderInternal;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpHandler;
import com.android.systemui.dump.LogBufferFreezer;
import com.android.systemui.dump.SystemUIAuxiliaryDumpService;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SystemUIService extends Service {
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final DumpHandler mDumpHandler;
    private final LogBufferFreezer mLogBufferFreezer;
    private final Handler mMainHandler;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public SystemUIService(Handler handler, DumpHandler dumpHandler, BroadcastDispatcher broadcastDispatcher, LogBufferFreezer logBufferFreezer) {
        this.mMainHandler = handler;
        this.mDumpHandler = dumpHandler;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mLogBufferFreezer = logBufferFreezer;
    }

    public void onCreate() {
        super.onCreate();
        ((SystemUIApplication) getApplication()).startServicesIfNeeded();
        this.mLogBufferFreezer.attach(this.mBroadcastDispatcher);
        if (!Build.IS_DEBUGGABLE || !SystemProperties.getBoolean("debug.crash_sysui", false)) {
            if (Build.IS_DEBUGGABLE) {
                BinderInternal.nSetBinderProxyCountEnabled(true);
                BinderInternal.nSetBinderProxyCountWatermarks(1000, 900);
                BinderInternal.setBinderProxyCountCallback(new BinderInternal.BinderProxyLimitListener(this) {
                    /* class com.android.systemui.SystemUIService.AnonymousClass1 */

                    public void onLimitReached(int i) {
                        Slog.w("SystemUIService", "uid " + i + " sent too many Binder proxies to uid " + Process.myUid());
                    }
                }, this.mMainHandler);
            }
            startServiceAsUser(new Intent(getApplicationContext(), SystemUIAuxiliaryDumpService.class), UserHandle.SYSTEM);
            return;
        }
        throw new RuntimeException();
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (strArr.length == 0) {
            strArr = new String[]{"--dump-priority", "CRITICAL"};
        }
        this.mDumpHandler.dump(fileDescriptor, printWriter, strArr);
    }
}
