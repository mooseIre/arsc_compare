package com.android.systemui.controls.controller;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.controls.Control;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.util.Log;
import com.android.systemui.controls.controller.ControlsBindingController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;

public class ControlsBindingControllerImpl implements ControlsBindingController {
    private final ControlsBindingControllerImpl$actionCallbackService$1 actionCallbackService = new ControlsBindingControllerImpl$actionCallbackService$1(this);
    private final DelayableExecutor backgroundExecutor;
    private final Context context;
    private ControlsProviderLifecycleManager currentProvider;
    private UserHandle currentUser = UserHandle.of(ActivityManager.getCurrentUser());
    private final Lazy<ControlsController> lazyController;
    private LoadSubscriber loadSubscriber;
    private StatefulControlSubscriber statefulControlSubscriber;

    public ControlsBindingControllerImpl(Context context2, DelayableExecutor delayableExecutor, Lazy<ControlsController> lazy) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(lazy, "lazyController");
        this.context = context2;
        this.backgroundExecutor = delayableExecutor;
        this.lazyController = lazy;
    }

    @Override // com.android.systemui.util.UserAwareController
    public int getCurrentUserId() {
        UserHandle userHandle = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "currentUser");
        return userHandle.getIdentifier();
    }

    public ControlsProviderLifecycleManager createProviderManager$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        Context context2 = this.context;
        DelayableExecutor delayableExecutor = this.backgroundExecutor;
        ControlsBindingControllerImpl$actionCallbackService$1 controlsBindingControllerImpl$actionCallbackService$1 = this.actionCallbackService;
        UserHandle userHandle = this.currentUser;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "currentUser");
        return new ControlsProviderLifecycleManager(context2, delayableExecutor, controlsBindingControllerImpl$actionCallbackService$1, userHandle, componentName);
    }

    private final ControlsProviderLifecycleManager retrieveLifecycleManager(ComponentName componentName) {
        ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.currentProvider;
        if (controlsProviderLifecycleManager != null) {
            if (!Intrinsics.areEqual(controlsProviderLifecycleManager != null ? controlsProviderLifecycleManager.getComponentName() : null, componentName)) {
                unbind();
            }
        }
        ControlsProviderLifecycleManager controlsProviderLifecycleManager2 = this.currentProvider;
        if (controlsProviderLifecycleManager2 == null) {
            controlsProviderLifecycleManager2 = createProviderManager$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(componentName);
        }
        this.currentProvider = controlsProviderLifecycleManager2;
        return controlsProviderLifecycleManager2;
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public Runnable bindAndLoad(ComponentName componentName, ControlsBindingController.LoadCallback loadCallback) {
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        Intrinsics.checkParameterIsNotNull(loadCallback, "callback");
        LoadSubscriber loadSubscriber2 = this.loadSubscriber;
        if (loadSubscriber2 != null) {
            loadSubscriber2.loadCancel();
        }
        LoadSubscriber loadSubscriber3 = new LoadSubscriber(this, loadCallback, 100000);
        this.loadSubscriber = loadSubscriber3;
        retrieveLifecycleManager(componentName).maybeBindAndLoad(loadSubscriber3);
        return loadSubscriber3.loadCancel();
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void bindAndLoadSuggested(ComponentName componentName, ControlsBindingController.LoadCallback loadCallback) {
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        Intrinsics.checkParameterIsNotNull(loadCallback, "callback");
        LoadSubscriber loadSubscriber2 = this.loadSubscriber;
        if (loadSubscriber2 != null) {
            loadSubscriber2.loadCancel();
        }
        LoadSubscriber loadSubscriber3 = new LoadSubscriber(this, loadCallback, 36);
        this.loadSubscriber = loadSubscriber3;
        retrieveLifecycleManager(componentName).maybeBindAndLoadSuggested(loadSubscriber3);
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void subscribe(StructureInfo structureInfo) {
        Intrinsics.checkParameterIsNotNull(structureInfo, "structureInfo");
        unsubscribe();
        ControlsProviderLifecycleManager retrieveLifecycleManager = retrieveLifecycleManager(structureInfo.getComponentName());
        ControlsController controlsController = this.lazyController.get();
        Intrinsics.checkExpressionValueIsNotNull(controlsController, "lazyController.get()");
        StatefulControlSubscriber statefulControlSubscriber2 = new StatefulControlSubscriber(controlsController, retrieveLifecycleManager, this.backgroundExecutor, 100000);
        this.statefulControlSubscriber = statefulControlSubscriber2;
        List<ControlInfo> controls = structureInfo.getControls();
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
        Iterator<T> it = controls.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getControlId());
        }
        retrieveLifecycleManager.maybeBindAndSubscribe(arrayList, statefulControlSubscriber2);
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void unsubscribe() {
        StatefulControlSubscriber statefulControlSubscriber2 = this.statefulControlSubscriber;
        if (statefulControlSubscriber2 != null) {
            statefulControlSubscriber2.cancel();
        }
        this.statefulControlSubscriber = null;
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void action(ComponentName componentName, ControlInfo controlInfo, ControlAction controlAction) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "controlInfo");
        Intrinsics.checkParameterIsNotNull(controlAction, "action");
        if (this.statefulControlSubscriber == null) {
            Log.w("ControlsBindingControllerImpl", "No actions can occur outside of an active subscription. Ignoring.");
        } else {
            retrieveLifecycleManager(componentName).maybeBindAndSendAction(controlInfo.getControlId(), controlAction);
        }
    }

    @Override // com.android.systemui.util.UserAwareController
    public void changeUser(UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(userHandle, "newUser");
        if (!Intrinsics.areEqual(userHandle, this.currentUser)) {
            unbind();
            this.currentUser = userHandle;
        }
    }

    /* access modifiers changed from: public */
    private final void unbind() {
        unsubscribe();
        LoadSubscriber loadSubscriber2 = this.loadSubscriber;
        if (loadSubscriber2 != null) {
            loadSubscriber2.loadCancel();
        }
        this.loadSubscriber = null;
        ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.currentProvider;
        if (controlsProviderLifecycleManager != null) {
            controlsProviderLifecycleManager.unbindService();
        }
        this.currentProvider = null;
    }

    @Override // com.android.systemui.controls.controller.ControlsBindingController
    public void onComponentRemoved(ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        this.backgroundExecutor.execute(new ControlsBindingControllerImpl$onComponentRemoved$1(this, componentName));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("  ControlsBindingController:\n");
        sb.append("    currentUser=" + this.currentUser + '\n');
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    StatefulControlSubscriber=");
        sb2.append(this.statefulControlSubscriber);
        sb.append(sb2.toString());
        sb.append("    Providers=" + this.currentProvider + '\n');
        String sb3 = sb.toString();
        Intrinsics.checkExpressionValueIsNotNull(sb3, "StringBuilder(\"  Control…\\n\")\n        }.toString()");
        return sb3;
    }

    /* access modifiers changed from: private */
    public abstract class CallbackRunnable implements Runnable {
        private final ControlsProviderLifecycleManager provider;
        final /* synthetic */ ControlsBindingControllerImpl this$0;
        private final IBinder token;

        public abstract void doRun();

        public CallbackRunnable(ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder iBinder) {
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            this.this$0 = controlsBindingControllerImpl;
            this.token = iBinder;
            this.provider = controlsBindingControllerImpl.currentProvider;
        }

        public final ControlsProviderLifecycleManager getProvider() {
            return this.provider;
        }

        public void run() {
            ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.provider;
            if (controlsProviderLifecycleManager == null) {
                Log.e("ControlsBindingControllerImpl", "No current provider set");
            } else if (!Intrinsics.areEqual(controlsProviderLifecycleManager.getUser(), this.this$0.currentUser)) {
                Log.e("ControlsBindingControllerImpl", "User " + this.provider.getUser() + " is not current user");
            } else if (!Intrinsics.areEqual(this.token, this.provider.getToken())) {
                Log.e("ControlsBindingControllerImpl", "Provider for token:" + this.token + " does not exist anymore");
            } else {
                doRun();
            }
        }
    }

    private final class OnLoadRunnable extends CallbackRunnable {
        private final ControlsBindingController.LoadCallback callback;
        private final List<Control> list;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OnLoadRunnable(ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder iBinder, List<Control> list2, ControlsBindingController.LoadCallback loadCallback) {
            super(controlsBindingControllerImpl, iBinder);
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(list2, "list");
            Intrinsics.checkParameterIsNotNull(loadCallback, "callback");
            this.list = list2;
            this.callback = loadCallback;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            Log.d("ControlsBindingControllerImpl", "LoadSubscription: Complete and loading controls");
            this.callback.accept(this.list);
        }
    }

    public final class OnCancelAndLoadRunnable extends CallbackRunnable {
        private final ControlsBindingController.LoadCallback callback;
        private final List<Control> list;
        private final IControlsSubscription subscription;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OnCancelAndLoadRunnable(ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder iBinder, List<Control> list2, IControlsSubscription iControlsSubscription, ControlsBindingController.LoadCallback loadCallback) {
            super(controlsBindingControllerImpl, iBinder);
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(list2, "list");
            Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
            Intrinsics.checkParameterIsNotNull(loadCallback, "callback");
            this.list = list2;
            this.subscription = iControlsSubscription;
            this.callback = loadCallback;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            Log.d("ControlsBindingControllerImpl", "LoadSubscription: Canceling and loading controls");
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                provider.cancelSubscription(this.subscription);
            }
            this.callback.accept(this.list);
        }
    }

    private final class OnSubscribeRunnable extends CallbackRunnable {
        private final long requestLimit;
        private final IControlsSubscription subscription;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OnSubscribeRunnable(ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder iBinder, IControlsSubscription iControlsSubscription, long j) {
            super(controlsBindingControllerImpl, iBinder);
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
            this.subscription = iControlsSubscription;
            this.requestLimit = j;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            Log.d("ControlsBindingControllerImpl", "LoadSubscription: Starting subscription");
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                provider.startSubscription(this.subscription, this.requestLimit);
            }
        }
    }

    public final class OnActionResponseRunnable extends CallbackRunnable {
        private final String controlId;
        private final int response;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OnActionResponseRunnable(ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder iBinder, String str, int i) {
            super(controlsBindingControllerImpl, iBinder);
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(str, "controlId");
            this.this$0 = controlsBindingControllerImpl;
            this.controlId = str;
            this.response = i;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                ((ControlsController) this.this$0.lazyController.get()).onActionResponse(provider.getComponentName(), this.controlId, this.response);
            }
        }
    }

    private final class OnLoadErrorRunnable extends CallbackRunnable {
        private final ControlsBindingController.LoadCallback callback;
        private final String error;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public OnLoadErrorRunnable(ControlsBindingControllerImpl controlsBindingControllerImpl, IBinder iBinder, String str, ControlsBindingController.LoadCallback loadCallback) {
            super(controlsBindingControllerImpl, iBinder);
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(str, "error");
            Intrinsics.checkParameterIsNotNull(loadCallback, "callback");
            this.error = str;
            this.callback = loadCallback;
        }

        @Override // com.android.systemui.controls.controller.ControlsBindingControllerImpl.CallbackRunnable
        public void doRun() {
            this.callback.error(this.error);
            ControlsProviderLifecycleManager provider = getProvider();
            if (provider != null) {
                Log.e("ControlsBindingControllerImpl", "onError receive from '" + provider.getComponentName() + "': " + this.error);
            }
        }
    }

    public final class LoadSubscriber extends IControlsSubscriber.Stub {
        private Function0<Unit> _loadCancelInternal;
        private final ControlsBindingController.LoadCallback callback;
        private AtomicBoolean isTerminated = new AtomicBoolean(false);
        private final ArrayList<Control> loadedControls = new ArrayList<>();
        private final long requestLimit;
        private IControlsSubscription subscription;
        final /* synthetic */ ControlsBindingControllerImpl this$0;

        public LoadSubscriber(ControlsBindingControllerImpl controlsBindingControllerImpl, ControlsBindingController.LoadCallback loadCallback, long j) {
            Intrinsics.checkParameterIsNotNull(loadCallback, "callback");
            this.this$0 = controlsBindingControllerImpl;
            this.callback = loadCallback;
            this.requestLimit = j;
        }

        public static final /* synthetic */ IControlsSubscription access$getSubscription$p(LoadSubscriber loadSubscriber) {
            IControlsSubscription iControlsSubscription = loadSubscriber.subscription;
            if (iControlsSubscription != null) {
                return iControlsSubscription;
            }
            Intrinsics.throwUninitializedPropertyAccessException("subscription");
            throw null;
        }

        public final ControlsBindingController.LoadCallback getCallback() {
            return this.callback;
        }

        public final long getRequestLimit() {
            return this.requestLimit;
        }

        public final ArrayList<Control> getLoadedControls() {
            return this.loadedControls;
        }

        public final Runnable loadCancel() {
            return new ControlsBindingControllerImpl$LoadSubscriber$loadCancel$1(this);
        }

        public void onSubscribe(IBinder iBinder, IControlsSubscription iControlsSubscription) {
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subs");
            this.subscription = iControlsSubscription;
            this._loadCancelInternal = new ControlsBindingControllerImpl$LoadSubscriber$onSubscribe$1(this);
            this.this$0.backgroundExecutor.execute(new OnSubscribeRunnable(this.this$0, iBinder, iControlsSubscription, this.requestLimit));
        }

        public void onNext(IBinder iBinder, Control control) {
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(control, "c");
            this.this$0.backgroundExecutor.execute(new ControlsBindingControllerImpl$LoadSubscriber$onNext$1(this, control, iBinder));
        }

        public void onError(IBinder iBinder, String str) {
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            Intrinsics.checkParameterIsNotNull(str, "s");
            maybeTerminateAndRun(new OnLoadErrorRunnable(this.this$0, iBinder, str, this.callback));
        }

        public void onComplete(IBinder iBinder) {
            Intrinsics.checkParameterIsNotNull(iBinder, "token");
            maybeTerminateAndRun(new OnLoadRunnable(this.this$0, iBinder, this.loadedControls, this.callback));
        }

        /* access modifiers changed from: public */
        private final void maybeTerminateAndRun(Runnable runnable) {
            if (!this.isTerminated.get()) {
                this._loadCancelInternal = ControlsBindingControllerImpl$LoadSubscriber$maybeTerminateAndRun$1.INSTANCE;
                ControlsProviderLifecycleManager controlsProviderLifecycleManager = this.this$0.currentProvider;
                if (controlsProviderLifecycleManager != null) {
                    controlsProviderLifecycleManager.cancelLoadTimeout();
                }
                this.this$0.backgroundExecutor.execute(new ControlsBindingControllerImpl$LoadSubscriber$maybeTerminateAndRun$2(this, runnable));
            }
        }
    }
}
