package com.android.systemui.controls.management;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.applications.ServiceListing;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsListingControllerImpl.kt */
public final class ControlsListingControllerImpl implements ControlsListingController {
    /* access modifiers changed from: private */
    public Set<ComponentName> availableComponents;
    /* access modifiers changed from: private */
    public List<? extends ServiceInfo> availableServices;
    /* access modifiers changed from: private */
    public final Executor backgroundExecutor;
    /* access modifiers changed from: private */
    public final Set<ControlsListingController.ControlsListingCallback> callbacks;
    /* access modifiers changed from: private */
    public final Context context;
    /* access modifiers changed from: private */
    public int currentUserId;
    /* access modifiers changed from: private */
    public ServiceListing serviceListing;
    /* access modifiers changed from: private */
    public final Function1<Context, ServiceListing> serviceListingBuilder;
    /* access modifiers changed from: private */
    public final ServiceListing.Callback serviceListingCallback;
    /* access modifiers changed from: private */
    public AtomicInteger userChangeInProgress;

    @VisibleForTesting
    public ControlsListingControllerImpl(@NotNull Context context2, @NotNull Executor executor, @NotNull Function1<? super Context, ? extends ServiceListing> function1) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(executor, "backgroundExecutor");
        Intrinsics.checkParameterIsNotNull(function1, "serviceListingBuilder");
        this.context = context2;
        this.backgroundExecutor = executor;
        this.serviceListingBuilder = function1;
        this.serviceListing = (ServiceListing) function1.invoke(context2);
        this.callbacks = new LinkedHashSet();
        this.availableComponents = SetsKt__SetsKt.emptySet();
        this.availableServices = CollectionsKt__CollectionsKt.emptyList();
        this.userChangeInProgress = new AtomicInteger(0);
        this.currentUserId = ActivityManager.getCurrentUser();
        this.serviceListingCallback = new ControlsListingControllerImpl$serviceListingCallback$1(this);
        Log.d("ControlsListingControllerImpl", "Initializing");
        this.serviceListing.addCallback(this.serviceListingCallback);
        this.serviceListing.setListening(true);
        this.serviceListing.reload();
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public ControlsListingControllerImpl(@NotNull Context context2, @NotNull Executor executor) {
        this(context2, executor, AnonymousClass1.INSTANCE);
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
    }

    public int getCurrentUserId() {
        return this.currentUserId;
    }

    public void changeUser(@NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(userHandle, "newUser");
        this.userChangeInProgress.incrementAndGet();
        this.serviceListing.setListening(false);
        this.backgroundExecutor.execute(new ControlsListingControllerImpl$changeUser$1(this, userHandle));
    }

    public void addCallback(@NotNull ControlsListingController.ControlsListingCallback controlsListingCallback) {
        Intrinsics.checkParameterIsNotNull(controlsListingCallback, "listener");
        this.backgroundExecutor.execute(new ControlsListingControllerImpl$addCallback$1(this, controlsListingCallback));
    }

    public void removeCallback(@NotNull ControlsListingController.ControlsListingCallback controlsListingCallback) {
        Intrinsics.checkParameterIsNotNull(controlsListingCallback, "listener");
        this.backgroundExecutor.execute(new ControlsListingControllerImpl$removeCallback$1(this, controlsListingCallback));
    }

    @NotNull
    public List<ControlsServiceInfo> getCurrentServices() {
        List<? extends ServiceInfo> list = this.availableServices;
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (ServiceInfo controlsServiceInfo : list) {
            arrayList.add(new ControlsServiceInfo(this.context, controlsServiceInfo));
        }
        return arrayList;
    }

    @Nullable
    public CharSequence getAppLabel(@NotNull ComponentName componentName) {
        T t;
        Intrinsics.checkParameterIsNotNull(componentName, "name");
        Iterator<T> it = getCurrentServices().iterator();
        while (true) {
            if (!it.hasNext()) {
                t = null;
                break;
            }
            t = it.next();
            if (Intrinsics.areEqual((Object) ((ControlsServiceInfo) t).componentName, (Object) componentName)) {
                break;
            }
        }
        ControlsServiceInfo controlsServiceInfo = (ControlsServiceInfo) t;
        if (controlsServiceInfo != null) {
            return controlsServiceInfo.loadLabel();
        }
        return null;
    }
}
