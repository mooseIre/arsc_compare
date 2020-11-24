package com.android.systemui.controls.dagger;

import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import dagger.Lazy;
import java.util.Optional;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsComponent.kt */
public final class ControlsComponent {
    private final boolean featureEnabled;
    private final Lazy<ControlsController> lazyControlsController;
    private final Lazy<ControlsListingController> lazyControlsListingController;
    private final Lazy<ControlsUiController> lazyControlsUiController;

    public ControlsComponent(boolean z, @NotNull Lazy<ControlsController> lazy, @NotNull Lazy<ControlsUiController> lazy2, @NotNull Lazy<ControlsListingController> lazy3) {
        Intrinsics.checkParameterIsNotNull(lazy, "lazyControlsController");
        Intrinsics.checkParameterIsNotNull(lazy2, "lazyControlsUiController");
        Intrinsics.checkParameterIsNotNull(lazy3, "lazyControlsListingController");
        this.featureEnabled = z;
        this.lazyControlsController = lazy;
        this.lazyControlsUiController = lazy2;
        this.lazyControlsListingController = lazy3;
    }

    @NotNull
    public final Optional<ControlsController> getControlsController() {
        Optional<ControlsController> optional;
        String str;
        if (this.featureEnabled) {
            optional = Optional.of(this.lazyControlsController.get());
            str = "Optional.of(lazyControlsController.get())";
        } else {
            optional = Optional.empty();
            str = "Optional.empty()";
        }
        Intrinsics.checkExpressionValueIsNotNull(optional, str);
        return optional;
    }

    @NotNull
    public final Optional<ControlsUiController> getControlsUiController() {
        Optional<ControlsUiController> optional;
        String str;
        if (this.featureEnabled) {
            optional = Optional.of(this.lazyControlsUiController.get());
            str = "Optional.of(lazyControlsUiController.get())";
        } else {
            optional = Optional.empty();
            str = "Optional.empty()";
        }
        Intrinsics.checkExpressionValueIsNotNull(optional, str);
        return optional;
    }

    @NotNull
    public final Optional<ControlsListingController> getControlsListingController() {
        if (this.featureEnabled) {
            Optional<ControlsListingController> of = Optional.of(this.lazyControlsListingController.get());
            Intrinsics.checkExpressionValueIsNotNull(of, "Optional.of(lazyControlsListingController.get())");
            return of;
        }
        Optional<ControlsListingController> empty = Optional.empty();
        Intrinsics.checkExpressionValueIsNotNull(empty, "Optional.empty()");
        return empty;
    }
}
