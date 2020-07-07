package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.systemui.statusbar.policy.DemoModeController;
import java.util.ArrayList;
import java.util.List;

public class DemoModeControllerImpl implements DemoModeController {
    /* access modifiers changed from: private */
    public final List<DemoModeController.DemoModeCallback> mCallbacks = new ArrayList();
    private final Context mContext;
    private final BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle extras;
            if ("com.android.systemui.demo".equals(intent.getAction()) && (extras = intent.getExtras()) != null) {
                String lowerCase = extras.getString("command", "").trim().toLowerCase();
                if (lowerCase.length() > 0) {
                    Bundle unused = DemoModeControllerImpl.this.mLastArgs = extras;
                    String unused2 = DemoModeControllerImpl.this.mLastCommand = lowerCase;
                    try {
                        for (DemoModeController.DemoModeCallback onDemoModeChanged : DemoModeControllerImpl.this.mCallbacks) {
                            onDemoModeChanged.onDemoModeChanged(lowerCase, extras);
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Bundle mLastArgs;
    /* access modifiers changed from: private */
    public String mLastCommand;

    public DemoModeControllerImpl(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.demo");
        this.mContext.registerReceiverAsUser(this.mDemoReceiver, UserHandle.ALL, intentFilter, "android.permission.DUMP", (Handler) null);
    }

    public void addCallback(DemoModeController.DemoModeCallback demoModeCallback) {
        if (!this.mCallbacks.contains(demoModeCallback)) {
            this.mCallbacks.add(demoModeCallback);
            if (!TextUtils.isEmpty(this.mLastCommand)) {
                demoModeCallback.onDemoModeChanged(this.mLastCommand, this.mLastArgs);
            }
        }
    }

    public void removeCallback(DemoModeController.DemoModeCallback demoModeCallback) {
        if (this.mCallbacks.contains(demoModeCallback)) {
            this.mCallbacks.remove(demoModeCallback);
        }
    }
}
