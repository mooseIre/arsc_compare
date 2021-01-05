package com.android.systemui.keyguard.dagger;

import android.app.trust.TrustManager;
import android.content.Context;
import android.os.PowerManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardViewController;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.util.DeviceConfigProxy;
import dagger.Lazy;
import java.util.concurrent.Executor;

public class KeyguardModule {
    public static KeyguardViewMediator newKeyguardViewMediator(Context context, FalsingManager falsingManager, LockPatternUtils lockPatternUtils, BroadcastDispatcher broadcastDispatcher, Lazy<KeyguardViewController> lazy, DismissCallbackRegistry dismissCallbackRegistry, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, PowerManager powerManager, TrustManager trustManager, Executor executor, DeviceConfigProxy deviceConfigProxy, NavigationModeController navigationModeController) {
        return new KeyguardViewMediator(context, falsingManager, lockPatternUtils, broadcastDispatcher, lazy, dismissCallbackRegistry, keyguardUpdateMonitor, dumpManager, executor, powerManager, trustManager, deviceConfigProxy, navigationModeController);
    }
}
