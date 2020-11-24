package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.systemui.DemoMode;
import com.android.systemui.broadcast.BroadcastDispatcher;
import java.util.ArrayList;
import java.util.List;

public class DemoModeController {
    /* access modifiers changed from: private */
    public final List<DemoMode> mCallbacks = new ArrayList();
    private final BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle extras;
            if ("com.android.systemui.demo".equals(intent.getAction()) && (extras = intent.getExtras()) != null) {
                String lowerCase = extras.getString("command", "").trim().toLowerCase();
                if (lowerCase.length() > 0) {
                    Bundle unused = DemoModeController.this.mLastArgs = extras;
                    String unused2 = DemoModeController.this.mLastCommand = lowerCase;
                    try {
                        for (DemoMode dispatchDemoCommand : DemoModeController.this.mCallbacks) {
                            dispatchDemoCommand.dispatchDemoCommand(lowerCase, extras);
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

    public DemoModeController(BroadcastDispatcher broadcastDispatcher) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.demo");
        broadcastDispatcher.registerReceiver(this.mDemoReceiver, intentFilter);
    }

    public void addCallback(DemoMode demoMode) {
        if (!this.mCallbacks.contains(demoMode)) {
            this.mCallbacks.add(demoMode);
            if (!TextUtils.isEmpty(this.mLastCommand)) {
                demoMode.dispatchDemoCommand(this.mLastCommand, this.mLastArgs);
            }
        }
    }

    public void removeCallback(DemoMode demoMode) {
        this.mCallbacks.remove(demoMode);
    }
}
