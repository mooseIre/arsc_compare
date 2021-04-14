package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.IOException;

public class SecretCodeBroadcastReceiver extends BroadcastReceiver {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public void onReceive(Context context, Intent intent) {
        if ("android.telephony.action.SECRET_CODE".equals(intent.getAction())) {
            captureHeap(context);
        }
    }

    private void captureHeap(Context context) {
        new Thread(new Runnable(context) {
            /* class com.android.systemui.$$Lambda$SecretCodeBroadcastReceiver$R3p7NtiAGjVcxL0o0FMhGZi4AY */
            public final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                SecretCodeBroadcastReceiver.this.lambda$captureHeap$1$SecretCodeBroadcastReceiver(this.f$1);
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$captureHeap$1 */
    public /* synthetic */ void lambda$captureHeap$1$SecretCodeBroadcastReceiver(Context context) {
        File file = new File(Environment.getExternalStorageDirectory(), "/MIUI/SysUI/");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file, "sysui.hprof");
        if (file2.exists()) {
            file2.delete();
        }
        try {
            Debug.dumpHprofData(file2.getPath());
        } catch (IOException unused) {
        }
        this.mHandler.post(new Runnable(context) {
            /* class com.android.systemui.$$Lambda$SecretCodeBroadcastReceiver$YO39WSepHGapQ0Qx9vJXLrXJjz4 */
            public final /* synthetic */ Context f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                SecretCodeBroadcastReceiver.lambda$captureHeap$0(this.f$0);
            }
        });
    }
}
