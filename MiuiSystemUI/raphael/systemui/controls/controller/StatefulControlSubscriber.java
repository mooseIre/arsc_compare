package com.android.systemui.controls.controller;

import android.os.IBinder;
import android.service.controls.Control;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import com.android.systemui.util.concurrency.DelayableExecutor;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: StatefulControlSubscriber.kt */
public final class StatefulControlSubscriber extends IControlsSubscriber.Stub {
    private final DelayableExecutor bgExecutor;
    private final ControlsController controller;
    private final ControlsProviderLifecycleManager provider;
    private final long requestLimit;
    private IControlsSubscription subscription;
    private boolean subscriptionOpen;

    public StatefulControlSubscriber(@NotNull ControlsController controlsController, @NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull DelayableExecutor delayableExecutor, long j) {
        Intrinsics.checkParameterIsNotNull(controlsController, "controller");
        Intrinsics.checkParameterIsNotNull(controlsProviderLifecycleManager, "provider");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "bgExecutor");
        this.controller = controlsController;
        this.provider = controlsProviderLifecycleManager;
        this.bgExecutor = delayableExecutor;
        this.requestLimit = j;
    }

    private final void run(IBinder iBinder, Function0<Unit> function0) {
        if (Intrinsics.areEqual(this.provider.getToken(), iBinder)) {
            this.bgExecutor.execute(new StatefulControlSubscriber$run$1(function0));
        }
    }

    public void onSubscribe(@NotNull IBinder iBinder, @NotNull IControlsSubscription iControlsSubscription) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subs");
        run(iBinder, new StatefulControlSubscriber$onSubscribe$1(this, iControlsSubscription));
    }

    public void onNext(@NotNull IBinder iBinder, @NotNull Control control) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(control, "control");
        run(iBinder, new StatefulControlSubscriber$onNext$1(this, iBinder, control));
    }

    public void onError(@NotNull IBinder iBinder, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        Intrinsics.checkParameterIsNotNull(str, "error");
        run(iBinder, new StatefulControlSubscriber$onError$1(this, str));
    }

    public void onComplete(@NotNull IBinder iBinder) {
        Intrinsics.checkParameterIsNotNull(iBinder, "token");
        run(iBinder, new StatefulControlSubscriber$onComplete$1(this));
    }

    public final void cancel() {
        if (this.subscriptionOpen) {
            this.bgExecutor.execute(new StatefulControlSubscriber$cancel$1(this));
        }
    }
}
