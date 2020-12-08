package com.android.systemui.statusbar.policy;

import android.content.IntentFilter;
import android.os.UserHandle;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiHeadsUpPolicy.kt */
public final class MiuiHeadsUpPolicy implements OnHeadsUpChangedListener {
    private final BroadcastDispatcher broadcastDispatcher;
    /* access modifiers changed from: private */
    public final HeadsUpManagerPhone headsUpManagerPhone;
    private final MiuiHeadsUpPolicy$mCloseSystemDialogReceiver$1 mCloseSystemDialogReceiver = new MiuiHeadsUpPolicy$mCloseSystemDialogReceiver$1(this);
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiHeadsUpPolicy$mKeyguardUpdateMonitorCallback$1(this);

    public MiuiHeadsUpPolicy(@NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull HeadsUpManagerPhone headsUpManagerPhone2) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone2, "headsUpManagerPhone");
        this.broadcastDispatcher = broadcastDispatcher2;
        this.headsUpManagerPhone = headsUpManagerPhone2;
    }

    public final void start() {
        this.headsUpManagerPhone.addListener(this);
        BroadcastDispatcher.registerReceiver$default(this.broadcastDispatcher, this.mCloseSystemDialogReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), (Executor) null, (UserHandle) null, 12, (Object) null);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    public void onHeadsUpUnPinned(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        HeadsUpManagerInjector.sendExitFloatingIntent(notificationEntry);
    }

    /* access modifiers changed from: private */
    public final void releaseHeadsUps() {
        this.headsUpManagerPhone.releaseAllImmediately();
    }
}
