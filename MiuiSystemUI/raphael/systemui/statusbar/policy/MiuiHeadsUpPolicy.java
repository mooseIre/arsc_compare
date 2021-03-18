package com.android.systemui.statusbar.policy;

import android.content.IntentFilter;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiHeadsUpPolicy.kt */
public final class MiuiHeadsUpPolicy implements OnHeadsUpChangedListener {
    private final BroadcastDispatcher broadcastDispatcher;
    private final HeadsUpManagerPhone headsUpManagerPhone;
    private final MiuiHeadsUpPolicy$mCloseSystemDialogReceiver$1 mCloseSystemDialogReceiver = new MiuiHeadsUpPolicy$mCloseSystemDialogReceiver$1(this);
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiHeadsUpPolicy$mKeyguardUpdateMonitorCallback$1(this);
    private final MiuiHeadsUpPolicy$mTaskChangedListener$1 mTaskChangedListener = new MiuiHeadsUpPolicy$mTaskChangedListener$1(this);

    public MiuiHeadsUpPolicy(@NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull HeadsUpManagerPhone headsUpManagerPhone2) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone2, "headsUpManagerPhone");
        this.broadcastDispatcher = broadcastDispatcher2;
        this.headsUpManagerPhone = headsUpManagerPhone2;
    }

    public final void start() {
        this.headsUpManagerPhone.addListener(this);
        BroadcastDispatcher.registerReceiver$default(this.broadcastDispatcher, this.mCloseSystemDialogReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), null, null, 12, null);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mKeyguardUpdateMonitorCallback);
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskChangedListener);
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        HeadsUpManagerInjector.sendExitFloatingIntent(notificationEntry);
    }

    /* access modifiers changed from: private */
    public final void releaseHeadsUps() {
        this.headsUpManagerPhone.releaseAllImmediately();
    }
}
