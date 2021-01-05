package com.android.systemui.controls.controller;

import android.service.controls.IControlsActionCallback;
import android.service.controls.IControlsProvider;
import android.service.controls.IControlsSubscriber;
import android.service.controls.IControlsSubscription;
import android.service.controls.actions.ControlAction;
import android.service.controls.actions.ControlActionWrapper;
import android.util.Log;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ServiceWrapper.kt */
public final class ServiceWrapper {
    @NotNull
    private final IControlsProvider service;

    public ServiceWrapper(@NotNull IControlsProvider iControlsProvider) {
        Intrinsics.checkParameterIsNotNull(iControlsProvider, "service");
        this.service = iControlsProvider;
    }

    public final boolean load(@NotNull IControlsSubscriber iControlsSubscriber) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
        try {
            this.service.load(iControlsSubscriber);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean loadSuggested(@NotNull IControlsSubscriber iControlsSubscriber) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
        try {
            this.service.loadSuggested(iControlsSubscriber);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean subscribe(@NotNull List<String> list, @NotNull IControlsSubscriber iControlsSubscriber) {
        Intrinsics.checkParameterIsNotNull(list, "controlIds");
        Intrinsics.checkParameterIsNotNull(iControlsSubscriber, "subscriber");
        try {
            this.service.subscribe(list, iControlsSubscriber);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean request(@NotNull IControlsSubscription iControlsSubscription, long j) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
        try {
            iControlsSubscription.request(j);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean cancel(@NotNull IControlsSubscription iControlsSubscription) {
        Intrinsics.checkParameterIsNotNull(iControlsSubscription, "subscription");
        try {
            iControlsSubscription.cancel();
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }

    public final boolean action(@NotNull String str, @NotNull ControlAction controlAction, @NotNull IControlsActionCallback iControlsActionCallback) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Intrinsics.checkParameterIsNotNull(controlAction, "action");
        Intrinsics.checkParameterIsNotNull(iControlsActionCallback, "cb");
        try {
            this.service.action(str, new ControlActionWrapper(controlAction), iControlsActionCallback);
            return true;
        } catch (Exception e) {
            Log.e("ServiceWrapper", "Caught exception from ControlsProviderService", e);
            return false;
        }
    }
}
