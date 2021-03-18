package com.android.systemui.media;

import com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView;
import com.android.systemui.util.animation.MeasurementOutput;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaHostStatesManager.kt */
public final class MediaHostStatesManager {
    private final Set<Callback> callbacks = new LinkedHashSet();
    @NotNull
    private final Map<Integer, MeasurementOutput> carouselSizes = new LinkedHashMap();
    private final Set<MediaViewController> controllers = new LinkedHashSet();
    @NotNull
    private final Map<Integer, MediaHostState> mediaHostStates = new LinkedHashMap();

    /* compiled from: MediaHostStatesManager.kt */
    public interface Callback {
        void onHostStateChanged(int i, @NotNull MediaHostState mediaHostState);
    }

    @NotNull
    public final Map<Integer, MeasurementOutput> getCarouselSizes() {
        return this.carouselSizes;
    }

    @NotNull
    public final Map<Integer, MediaHostState> getMediaHostStates() {
        return this.mediaHostStates;
    }

    public final void updateHostState(int i, @NotNull MediaHostState mediaHostState) {
        Intrinsics.checkParameterIsNotNull(mediaHostState, "hostState");
        if (!mediaHostState.equals(this.mediaHostStates.get(Integer.valueOf(i)))) {
            MediaHostState copy = mediaHostState.copy();
            this.mediaHostStates.put(Integer.valueOf(i), copy);
            updateCarouselDimensions(i, mediaHostState);
            for (MediaViewController mediaViewController : this.controllers) {
                mediaViewController.getStateCallback().onHostStateChanged(i, copy);
            }
            for (Callback callback : this.callbacks) {
                callback.onHostStateChanged(i, copy);
            }
        }
    }

    @NotNull
    public final MeasurementOutput updateCarouselDimensions(int i, @NotNull MediaHostState mediaHostState) {
        Intrinsics.checkParameterIsNotNull(mediaHostState, "hostState");
        MeasurementOutput measurementOutput = new MeasurementOutput(0, 0);
        for (MediaViewController mediaViewController : this.controllers) {
            MeasurementOutput measurementsForState = mediaViewController.getMeasurementsForState(mediaHostState);
            if (measurementsForState != null) {
                if (measurementsForState.getMeasuredHeight() > measurementOutput.getMeasuredHeight()) {
                    measurementOutput.setMeasuredHeight(measurementsForState.getMeasuredHeight());
                }
                if (measurementsForState.getMeasuredWidth() + (MiuiMediaHeaderView.Companion.getMSidePaddings() * 2) > measurementOutput.getMeasuredWidth()) {
                    measurementOutput.setMeasuredWidth(measurementsForState.getMeasuredWidth() + (MiuiMediaHeaderView.Companion.getMSidePaddings() * 2));
                }
            }
        }
        this.carouselSizes.put(Integer.valueOf(i), measurementOutput);
        return measurementOutput;
    }

    public final void addCallback(@NotNull Callback callback) {
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        this.callbacks.add(callback);
    }

    public final void addController(@NotNull MediaViewController mediaViewController) {
        Intrinsics.checkParameterIsNotNull(mediaViewController, "controller");
        this.controllers.add(mediaViewController);
    }

    public final void removeController(@NotNull MediaViewController mediaViewController) {
        Intrinsics.checkParameterIsNotNull(mediaViewController, "controller");
        this.controllers.remove(mediaViewController);
    }
}
