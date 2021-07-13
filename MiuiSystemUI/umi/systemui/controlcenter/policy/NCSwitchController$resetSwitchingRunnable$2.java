package com.android.systemui.controlcenter.policy;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.StatusBar;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NCSwitchController.kt */
public final class NCSwitchController$resetSwitchingRunnable$2 extends Lambda implements Function0<Runnable> {
    final /* synthetic */ NCSwitchController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NCSwitchController$resetSwitchingRunnable$2(NCSwitchController nCSwitchController) {
        super(0);
        this.this$0 = nCSwitchController;
    }

    @Override // kotlin.jvm.functions.Function0
    @NotNull
    public final Runnable invoke() {
        return new Runnable(this) {
            /* class com.android.systemui.controlcenter.policy.NCSwitchController$resetSwitchingRunnable$2.AnonymousClass1 */
            final /* synthetic */ NCSwitchController$resetSwitchingRunnable$2 this$0;

            {
                this.this$0 = r1;
            }

            public final void run() {
                Object obj = Dependency.get(StatusBar.class);
                Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(StatusBar::class.java)");
                NotificationPanelViewController panelController = ((StatusBar) obj).getPanelController();
                if (panelController != null) {
                    MiuiNotificationPanelViewController miuiNotificationPanelViewController = (MiuiNotificationPanelViewController) panelController;
                    boolean isNCSwitching = miuiNotificationPanelViewController.isNCSwitching();
                    boolean isNCSwitching2 = this.this$0.this$0.mControlPanelController.isNCSwitching();
                    if (isNCSwitching || isNCSwitching2) {
                        this.this$0.this$0.panelViewLogger.logNcSwitchError(isNCSwitching, isNCSwitching2);
                        if (isNCSwitching) {
                            miuiNotificationPanelViewController.requestNCSwitching(false);
                        }
                        if (isNCSwitching2) {
                            this.this$0.this$0.mControlPanelController.requestNCSwitching(false);
                            return;
                        }
                        return;
                    }
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController");
            }
        };
    }
}
