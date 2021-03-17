package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.controls.IControlsActionCallback;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsProviderLifecycleManager.kt */
public final class ControlsProviderLifecycleManager implements IBinder.DeathRecipient {
    private static final int BIND_FLAGS = 67108865;
    private final String TAG = ControlsProviderLifecycleManager.class.getSimpleName();
    private final IControlsActionCallback.Stub actionCallbackService;
    private int bindTryCount;
    @NotNull
    private final ComponentName componentName;
    private final Context context;
    private final DelayableExecutor executor;
    private final Intent intent;
    private Runnable onLoadCanceller;
    @GuardedBy({"queuedServiceMethods"})
    private final Set<ServiceMethod> queuedServiceMethods = new ArraySet();
    private boolean requiresBound;
    private final ControlsProviderLifecycleManager$serviceConnection$1 serviceConnection;
    @NotNull
    private final IBinder token = new Binder();
    @NotNull
    private final UserHandle user;
    private ServiceWrapper wrapper;

    public ControlsProviderLifecycleManager(@NotNull Context context2, @NotNull DelayableExecutor delayableExecutor, @NotNull IControlsActionCallback.Stub stub, @NotNull UserHandle userHandle, @NotNull ComponentName componentName2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "executor");
        Intrinsics.checkParameterIsNotNull(stub, "actionCallbackService");
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        Intrinsics.checkParameterIsNotNull(componentName2, "componentName");
        this.context = context2;
        this.executor = delayableExecutor;
        this.actionCallbackService = stub;
        this.user = userHandle;
        this.componentName = componentName2;
        Intent intent2 = new Intent();
        intent2.setComponent(this.componentName);
        Bundle bundle = new Bundle();
        bundle.putBinder("CALLBACK_TOKEN", this.token);
        intent2.putExtra("CALLBACK_BUNDLE", bundle);
        this.intent = intent2;
        this.serviceConnection = new ControlsProviderLifecycleManager$serviceConnection$1(this);
    }

    @NotNull
    public final UserHandle getUser() {
        return this.user;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final IBinder getToken() {
        return this.token;
    }

    /* access modifiers changed from: private */
    public final void bindService(boolean z) {
        this.executor.execute(new ControlsProviderLifecycleManager$bindService$1(this, z));
    }

    /* access modifiers changed from: private */
    public final void handlePendingServiceMethods() {
        ArraySet<ServiceMethod> arraySet;
        synchronized (this.queuedServiceMethods) {
            arraySet = new ArraySet(this.queuedServiceMethods);
            this.queuedServiceMethods.clear();
        }
        for (ServiceMethod serviceMethod : arraySet) {
            serviceMethod.run();
        }
    }

    public void binderDied() {
        if (this.wrapper != null) {
            this.wrapper = null;
            if (this.requiresBound) {
                Log.d(this.TAG, "binderDied");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void queueServiceMethod(ServiceMethod serviceMethod) {
        synchronized (this.queuedServiceMethods) {
            this.queuedServiceMethods.add(serviceMethod);
        }
    }

    private final void invokeOrQueue(ServiceMethod serviceMethod) {
        if (this.wrapper != null) {
            serviceMethod.run();
            return;
        }
        queueServiceMethod(serviceMethod);
        bindService(true);
    }

    public final void maybeBindAndLoad(@NotNull IControlsSubscriber.Stub stub) {
        Intrinsics.checkParameterIsNotNull(stub, "subscriber");
        this.onLoadCanceller = this.executor.executeDelayed(new ControlsProviderLifecycleManager$maybeBindAndLoad$1(this, stub), 20, TimeUnit.SECONDS);
        invokeOrQueue(new Load(this, stub));
    }

    public final void maybeBindAndLoadSuggested(@NotNull IControlsSubscriber.Stub stub) {
        Intrinsics.checkParameterIsNotNull(stub, "subscriber");
        this.onLoadCanceller = this.executor.executeDelayed(new ControlsProviderLifecycleManager$maybeBindAndLoadSuggested$1(this, stub), 20, TimeUnit.SECONDS);
        invokeOrQueue(new Suggest(this, stub));
    }

    public final void cancelLoadTimeout() {
        Runnable runnable = this.onLoadCanceller;
        if (runnable != null) {
            runnable.run();
        }
        this.onLoadCanceller = null;
    }

    public final void maybeBindAndSubscribe(@NotNull List<String> list, @NotNull IControlsSubscriber iControlsSubscriber) {
        Intrinsics.checkParameterIsNotNull(list, "controlIds");
        Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
        invokeOrQueue(new Subscribe(this, list, iControlsSubscriber));
    }

    public final void maybeBindAndSendAction(@NotNull String str, @NotNull ControlAction controlAction) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Intrinsics.checkParameterIsNotNull(controlAction, "action");
        invokeOrQueue(new Action(this, str, controlAction));
    }

    public final void startSubscription(@NotNull IControlsSubscription iControlsSubscription, long j) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
        String str = this.TAG;
        Log.d(str, "startSubscription: " + iControlsSubscription);
        ServiceWrapper serviceWrapper = this.wrapper;
        if (serviceWrapper != null) {
            serviceWrapper.request(iControlsSubscription, j);
        }
    }

    public final void cancelSubscription(@NotNull IControlsSubscription iControlsSubscription) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
        String str = this.TAG;
        Log.d(str, "cancelSubscription: " + iControlsSubscription);
        ServiceWrapper serviceWrapper = this.wrapper;
        if (serviceWrapper != null) {
            serviceWrapper.cancel(iControlsSubscription);
        }
    }

    public final void unbindService() {
        Runnable runnable = this.onLoadCanceller;
        if (runnable != null) {
            runnable.run();
        }
        this.onLoadCanceller = null;
        bindService(false);
    }

    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder("ControlsProviderLifecycleManager(");
        sb.append("component=" + this.componentName);
        sb.append(", user=" + this.user);
        sb.append(")");
        String sb2 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb2, "StringBuilder(\"ControlsP…\")\")\n        }.toString()");
        return sb2;
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public abstract class ServiceMethod {
        public abstract boolean callWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core();

        /* JADX WARN: Incorrect args count in method signature: ()V */
        public ServiceMethod() {
        }

        public final void run() {
            if (!callWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core()) {
                ControlsProviderLifecycleManager.this.queueServiceMethod(this);
                ControlsProviderLifecycleManager.this.binderDied();
            }
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Load extends ServiceMethod {
        @NotNull
        private final IControlsSubscriber.Stub subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Load(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub stub) {
            super();
            Intrinsics.checkParameterIsNotNull(stub, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.subscriber = stub;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "load " + this.this$0.getComponentName());
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.load(this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Suggest extends ServiceMethod {
        @NotNull
        private final IControlsSubscriber.Stub subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Suggest(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, IControlsSubscriber.Stub stub) {
            super();
            Intrinsics.checkParameterIsNotNull(stub, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.subscriber = stub;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "suggest " + this.this$0.getComponentName());
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.loadSuggested(this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Subscribe extends ServiceMethod {
        @NotNull
        private final List<String> list;
        @NotNull
        private final IControlsSubscriber subscriber;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Subscribe(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull List<String> list2, IControlsSubscriber iControlsSubscriber) {
            super();
            Intrinsics.checkParameterIsNotNull(list2, "list");
            Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
            this.this$0 = controlsProviderLifecycleManager;
            this.list = list2;
            this.subscriber = iControlsSubscriber;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "subscribe " + this.this$0.getComponentName() + " - " + this.list);
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.subscribe(this.list, this.subscriber);
            }
            return false;
        }
    }

    /* compiled from: ControlsProviderLifecycleManager.kt */
    public final class Action extends ServiceMethod {
        @NotNull
        private final ControlAction action;
        @NotNull
        private final String id;
        final /* synthetic */ ControlsProviderLifecycleManager this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public Action(@NotNull ControlsProviderLifecycleManager controlsProviderLifecycleManager, @NotNull String str, ControlAction controlAction) {
            super();
            Intrinsics.checkParameterIsNotNull(str, "id");
            Intrinsics.checkParameterIsNotNull(controlAction, "action");
            this.this$0 = controlsProviderLifecycleManager;
            this.id = str;
            this.action = controlAction;
        }

        @Override // com.android.systemui.controls.controller.ControlsProviderLifecycleManager.ServiceMethod
        public boolean callWrapper$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
            String str = this.this$0.TAG;
            Log.d(str, "onAction " + this.this$0.getComponentName() + " - " + this.id);
            ServiceWrapper serviceWrapper = this.this$0.wrapper;
            if (serviceWrapper != null) {
                return serviceWrapper.action(this.id, this.action, this.this$0.actionCallbackService);
            }
            return false;
        }
    }
}
