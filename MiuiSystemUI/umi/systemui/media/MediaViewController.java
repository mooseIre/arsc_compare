package com.android.systemui.media;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.systemui.C0023R$xml;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.MeasurementOutput;
import com.android.systemui.util.animation.TransitionLayout;
import com.android.systemui.util.animation.TransitionLayoutController;
import com.android.systemui.util.animation.TransitionViewState;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaViewController.kt */
public final class MediaViewController {
    private boolean animateNextStateChange;
    private long animationDelay;
    private long animationDuration;
    @NotNull
    private final ConstraintSet collapsedLayout = new ConstraintSet();
    private final ConfigurationController configurationController;
    private final MediaViewController$configurationListener$1 configurationListener = new MediaViewController$configurationListener$1(this);
    private int currentEndLocation = -1;
    private int currentHeight;
    private int currentStartLocation = -1;
    private float currentTransitionProgress = 1.0f;
    private int currentWidth;
    @NotNull
    private final ConstraintSet expandedLayout = new ConstraintSet();
    private boolean firstRefresh = true;
    private final TransitionLayoutController layoutController = new TransitionLayoutController();
    private final MeasurementOutput measurement = new MeasurementOutput(0, 0);
    private final MediaHostStatesManager mediaHostStatesManager;
    @NotNull
    public Function0<Unit> sizeChangedListener;
    @NotNull
    private final MediaHostStatesManager.Callback stateCallback = new MediaViewController$stateCallback$1(this);
    private final CacheKey tmpKey = new CacheKey(0, 0, 0.0f, 7, null);
    private final TransitionViewState tmpState = new TransitionViewState();
    private final TransitionViewState tmpState2 = new TransitionViewState();
    private final TransitionViewState tmpState3 = new TransitionViewState();
    private TransitionLayout transitionLayout;
    private final Map<CacheKey, TransitionViewState> viewStates = new LinkedHashMap();

    public MediaViewController(@NotNull Context context, @NotNull ConfigurationController configurationController2, @NotNull MediaHostStatesManager mediaHostStatesManager2) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(configurationController2, "configurationController");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager2, "mediaHostStatesManager");
        this.configurationController = configurationController2;
        this.mediaHostStatesManager = mediaHostStatesManager2;
        this.collapsedLayout.load(context, C0023R$xml.media_collapsed);
        this.expandedLayout.load(context, C0023R$xml.media_expanded);
        this.mediaHostStatesManager.addController(this);
        this.layoutController.setSizeChangedListener(new Function2<Integer, Integer, Unit>(this) {
            /* class com.android.systemui.media.MediaViewController.AnonymousClass1 */
            final /* synthetic */ MediaViewController this$0;

            {
                this.this$0 = r1;
            }

            /* Return type fixed from 'java.lang.Object' to match base method */
            /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
            @Override // kotlin.jvm.functions.Function2
            public /* bridge */ /* synthetic */ Unit invoke(Integer num, Integer num2) {
                invoke(num.intValue(), num2.intValue());
                return Unit.INSTANCE;
            }

            public final void invoke(int i, int i2) {
                this.this$0.setCurrentWidth(i);
                this.this$0.setCurrentHeight(i2);
                this.this$0.getSizeChangedListener().invoke();
            }
        });
        this.configurationController.addCallback(this.configurationListener);
    }

    @NotNull
    public final Function0<Unit> getSizeChangedListener() {
        Function0<Unit> function0 = this.sizeChangedListener;
        if (function0 != null) {
            return function0;
        }
        Intrinsics.throwUninitializedPropertyAccessException("sizeChangedListener");
        throw null;
    }

    public final void setSizeChangedListener(@NotNull Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(function0, "<set-?>");
        this.sizeChangedListener = function0;
    }

    public final int getCurrentWidth() {
        return this.currentWidth;
    }

    public final void setCurrentWidth(int i) {
        this.currentWidth = i;
    }

    public final int getCurrentHeight() {
        return this.currentHeight;
    }

    public final void setCurrentHeight(int i) {
        this.currentHeight = i;
    }

    public final float getTranslationX() {
        TransitionLayout transitionLayout2 = this.transitionLayout;
        if (transitionLayout2 != null) {
            return transitionLayout2.getTranslationX();
        }
        return 0.0f;
    }

    public final float getTranslationY() {
        TransitionLayout transitionLayout2 = this.transitionLayout;
        if (transitionLayout2 != null) {
            return transitionLayout2.getTranslationY();
        }
        return 0.0f;
    }

    @NotNull
    public final MediaHostStatesManager.Callback getStateCallback() {
        return this.stateCallback;
    }

    @NotNull
    public final ConstraintSet getCollapsedLayout() {
        return this.collapsedLayout;
    }

    @NotNull
    public final ConstraintSet getExpandedLayout() {
        return this.expandedLayout;
    }

    public final void onDestroy() {
        this.mediaHostStatesManager.removeController(this);
        this.configurationController.removeCallback(this.configurationListener);
    }

    private final void ensureAllMeasurements() {
        for (Map.Entry<Integer, MediaHostState> entry : this.mediaHostStatesManager.getMediaHostStates().entrySet()) {
            obtainViewState(entry.getValue());
        }
    }

    private final ConstraintSet constraintSetForExpansion(float f) {
        return f > ((float) 0) ? this.expandedLayout : this.collapsedLayout;
    }

    private final TransitionViewState obtainViewState(MediaHostState mediaHostState) {
        if (mediaHostState == null || mediaHostState.getMeasurementInput() == null) {
            return null;
        }
        CacheKey cacheKey = this.tmpKey;
        getKey(mediaHostState, cacheKey);
        TransitionViewState transitionViewState = this.viewStates.get(cacheKey);
        if (transitionViewState != null) {
            return transitionViewState;
        }
        CacheKey copy$default = CacheKey.copy$default(cacheKey, 0, 0, 0.0f, 7, null);
        if (this.transitionLayout == null) {
            return null;
        }
        if (mediaHostState.getExpansion() == 0.0f || mediaHostState.getExpansion() == 1.0f) {
            TransitionLayout transitionLayout2 = this.transitionLayout;
            if (transitionLayout2 != null) {
                MeasurementInput measurementInput = mediaHostState.getMeasurementInput();
                if (measurementInput != null) {
                    TransitionViewState calculateViewState = transitionLayout2.calculateViewState(measurementInput, constraintSetForExpansion(mediaHostState.getExpansion()), new TransitionViewState());
                    this.viewStates.put(copy$default, calculateViewState);
                    return calculateViewState;
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        MediaHostState copy = mediaHostState.copy();
        copy.setExpansion(0.0f);
        TransitionViewState obtainViewState = obtainViewState(copy);
        if (obtainViewState != null) {
            MediaHostState copy2 = mediaHostState.copy();
            copy2.setExpansion(1.0f);
            TransitionViewState obtainViewState2 = obtainViewState(copy2);
            if (obtainViewState2 != null) {
                return TransitionLayoutController.getInterpolatedState$default(this.layoutController, obtainViewState, obtainViewState2, mediaHostState.getExpansion(), null, 8, null);
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.TransitionViewState");
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.TransitionViewState");
    }

    private final CacheKey getKey(MediaHostState mediaHostState, CacheKey cacheKey) {
        MeasurementInput measurementInput = mediaHostState.getMeasurementInput();
        int i = 0;
        cacheKey.setHeightMeasureSpec(measurementInput != null ? measurementInput.getHeightMeasureSpec() : 0);
        MeasurementInput measurementInput2 = mediaHostState.getMeasurementInput();
        if (measurementInput2 != null) {
            i = measurementInput2.getWidthMeasureSpec();
        }
        cacheKey.setWidthMeasureSpec(i);
        cacheKey.setExpansion(mediaHostState.getExpansion());
        return cacheKey;
    }

    public final void attach(@NotNull TransitionLayout transitionLayout2) {
        Intrinsics.checkParameterIsNotNull(transitionLayout2, "transitionLayout");
        this.transitionLayout = transitionLayout2;
        this.layoutController.attach(transitionLayout2);
        int i = this.currentEndLocation;
        if (i != -1) {
            setCurrentState(this.currentStartLocation, i, this.currentTransitionProgress, true);
        }
    }

    @Nullable
    public final MeasurementOutput getMeasurementsForState(@NotNull MediaHostState mediaHostState) {
        Intrinsics.checkParameterIsNotNull(mediaHostState, "hostState");
        TransitionViewState obtainViewState = obtainViewState(mediaHostState);
        if (obtainViewState == null) {
            return null;
        }
        this.measurement.setMeasuredWidth(obtainViewState.getWidth());
        this.measurement.setMeasuredHeight(obtainViewState.getHeight());
        return this.measurement;
    }

    public final void setCurrentState(int i, int i2, float f, boolean z) {
        TransitionViewState transitionViewState;
        this.currentEndLocation = i2;
        this.currentStartLocation = i;
        this.currentTransitionProgress = f;
        boolean z2 = this.animateNextStateChange && !z;
        MediaHostState mediaHostState = this.mediaHostStatesManager.getMediaHostStates().get(Integer.valueOf(i2));
        if (mediaHostState != null) {
            MediaHostState mediaHostState2 = this.mediaHostStatesManager.getMediaHostStates().get(Integer.valueOf(i));
            TransitionViewState obtainViewState = obtainViewState(mediaHostState);
            if (obtainViewState != null) {
                TransitionViewState updateViewStateToCarouselSize = updateViewStateToCarouselSize(obtainViewState, i2, this.tmpState2);
                if (updateViewStateToCarouselSize != null) {
                    this.layoutController.setMeasureState(updateViewStateToCarouselSize);
                    this.animateNextStateChange = false;
                    if (this.transitionLayout != null) {
                        TransitionViewState updateViewStateToCarouselSize2 = updateViewStateToCarouselSize(obtainViewState(mediaHostState2), i, this.tmpState3);
                        if (!mediaHostState.getVisible()) {
                            if (!(updateViewStateToCarouselSize2 == null || mediaHostState2 == null || !mediaHostState2.getVisible())) {
                                updateViewStateToCarouselSize2 = this.layoutController.getGoneState(updateViewStateToCarouselSize2, mediaHostState2.getDisappearParameters(), f, this.tmpState);
                            }
                            transitionViewState = updateViewStateToCarouselSize;
                            this.layoutController.setState(transitionViewState, z, z2, this.animationDuration, this.animationDelay);
                            return;
                        } else if (mediaHostState2 == null || mediaHostState2.getVisible()) {
                            if (!(f == 1.0f || updateViewStateToCarouselSize2 == null)) {
                                if (f != 0.0f) {
                                    updateViewStateToCarouselSize2 = this.layoutController.getInterpolatedState(updateViewStateToCarouselSize2, updateViewStateToCarouselSize, f, this.tmpState);
                                }
                            }
                            transitionViewState = updateViewStateToCarouselSize;
                            this.layoutController.setState(transitionViewState, z, z2, this.animationDuration, this.animationDelay);
                            return;
                        } else {
                            updateViewStateToCarouselSize2 = this.layoutController.getGoneState(updateViewStateToCarouselSize, mediaHostState.getDisappearParameters(), 1.0f - f, this.tmpState);
                        }
                        transitionViewState = updateViewStateToCarouselSize2;
                        this.layoutController.setState(transitionViewState, z, z2, this.animationDuration, this.animationDelay);
                        return;
                    }
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }
        }
    }

    private final TransitionViewState updateViewStateToCarouselSize(TransitionViewState transitionViewState, int i, TransitionViewState transitionViewState2) {
        TransitionViewState copy;
        if (transitionViewState == null || (copy = transitionViewState.copy(transitionViewState2)) == null) {
            return null;
        }
        MeasurementOutput measurementOutput = this.mediaHostStatesManager.getCarouselSizes().get(Integer.valueOf(i));
        if (measurementOutput != null) {
            copy.setHeight(Math.max(measurementOutput.getMeasuredHeight(), copy.getHeight()));
            copy.setWidth(Math.max(measurementOutput.getMeasuredWidth() - (MiuiMediaHeaderView.Companion.getMSidePaddings() * 2), copy.getWidth()));
        }
        return copy;
    }

    private final TransitionViewState obtainViewStateForLocation(int i) {
        MediaHostState mediaHostState = this.mediaHostStatesManager.getMediaHostStates().get(Integer.valueOf(i));
        if (mediaHostState != null) {
            return obtainViewState(mediaHostState);
        }
        return null;
    }

    public final void onLocationPreChange(int i) {
        TransitionViewState obtainViewStateForLocation = obtainViewStateForLocation(i);
        if (obtainViewStateForLocation != null) {
            this.layoutController.setMeasureState(obtainViewStateForLocation);
        }
    }

    public final void animatePendingStateChange(long j, long j2) {
        this.animateNextStateChange = true;
        this.animationDuration = j;
        this.animationDelay = j2;
    }

    public final void refreshState() {
        this.viewStates.clear();
        if (this.firstRefresh) {
            ensureAllMeasurements();
            this.firstRefresh = false;
        }
        setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, true);
    }
}
