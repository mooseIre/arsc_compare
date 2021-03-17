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
    private final List<DemoMode> mCallbacks = new ArrayList();
    private final BroadcastReceiver mDemoReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.DemoModeController.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            Bundle extras;
            if ("com.android.systemui.demo".equals(intent.getAction()) && (extras = intent.getExtras()) != null) {
                String lowerCase = extras.getString("command", "").trim().toLowerCase();
                if (lowerCase.length() > 0) {
                    DemoModeController.this.mLastArgs = extras;
                    DemoModeController.this.mLastCommand = lowerCase;
                    try {
                        for (DemoMode demoMode : DemoModeController.this.mCallbacks) {
                            demoMode.dispatchDemoCommand(lowerCase, extras);
                        }
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }
            }
        }
    };
    private Bundle mLastArgs;
    private String mLastCommand;

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
