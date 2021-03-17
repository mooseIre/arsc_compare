package com.android.systemui.media;

import android.graphics.Rect;
import android.util.ArraySet;
import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.UniqueObjectHostView;
import java.util.Iterator;
import java.util.Objects;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHost.kt */
public final class MediaHost implements MediaHostState {
    @NotNull
    private final Rect currentBounds = new Rect();
    @NotNull
    public UniqueObjectHostView hostView;
    private final MediaHost$listener$1 listener = new MediaHost$listener$1(this);
    private int location = -1;
    private final MediaDataFilter mediaDataFilter;
    private final MediaHierarchyManager mediaHierarchyManager;
    private final MediaHostStatesManager mediaHostStatesManager;
    private final MediaHostStateHolder state;
    private final int[] tmpLocationOnScreen = {0, 0};
    private ArraySet<Function1<Boolean, Unit>> visibleChangedListeners = new ArraySet<>();

    @Override // com.android.systemui.media.MediaHostState
    @NotNull
    public MediaHostState copy() {
        return this.state.copy();
    }

    @Override // com.android.systemui.media.MediaHostState
    @NotNull
    public DisappearParameters getDisappearParameters() {
        return this.state.getDisappearParameters();
    }

    @Override // com.android.systemui.media.MediaHostState
    public float getExpansion() {
        return this.state.getExpansion();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getFalsingProtectionNeeded() {
        return this.state.getFalsingProtectionNeeded();
    }

    @Override // com.android.systemui.media.MediaHostState
    @Nullable
    public MeasurementInput getMeasurementInput() {
        return this.state.getMeasurementInput();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getShowsOnlyActiveMedia() {
        return this.state.getShowsOnlyActiveMedia();
    }

    @Override // com.android.systemui.media.MediaHostState
    public boolean getVisible() {
        return this.state.getVisible();
    }

    public void setDisappearParameters(@NotNull DisappearParameters disappearParameters) {
        Intrinsics.checkParameterIsNotNull(disappearParameters, "<set-?>");
        this.state.setDisappearParameters(disappearParameters);
    }

    @Override // com.android.systemui.media.MediaHostState
    public void setExpansion(float f) {
        this.state.setExpansion(f);
    }

    public void setFalsingProtectionNeeded(boolean z) {
        this.state.setFalsingProtectionNeeded(z);
    }

    public void setShowsOnlyActiveMedia(boolean z) {
        this.state.setShowsOnlyActiveMedia(z);
    }

    public void setVisible(boolean z) {
        this.state.setVisible(z);
    }

    public MediaHost(@NotNull MediaHostStateHolder mediaHostStateHolder, @NotNull MediaHierarchyManager mediaHierarchyManager2, @NotNull MediaDataFilter mediaDataFilter2, @NotNull MediaHostStatesManager mediaHostStatesManager2) {
        Intrinsics.checkParameterIsNotNull(mediaHostStateHolder, "state");
        Intrinsics.checkParameterIsNotNull(mediaHierarchyManager2, "mediaHierarchyManager");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter2, "mediaDataFilter");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager2, "mediaHostStatesManager");
        this.state = mediaHostStateHolder;
        this.mediaHierarchyManager = mediaHierarchyManager2;
        this.mediaDataFilter = mediaDataFilter2;
        this.mediaHostStatesManager = mediaHostStatesManager2;
    }

    @NotNull
    public final UniqueObjectHostView getHostView() {
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView != null) {
            return uniqueObjectHostView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("hostView");
        throw null;
    }

    public final void setHostView(@NotNull UniqueObjectHostView uniqueObjectHostView) {
        Intrinsics.checkParameterIsNotNull(uniqueObjectHostView, "<set-?>");
        this.hostView = uniqueObjectHostView;
    }

    public final int getLocation() {
        return this.location;
    }

    @NotNull
    public final Rect getCurrentBounds() {
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView != null) {
            uniqueObjectHostView.getLocationOnScreen(this.tmpLocationOnScreen);
            int i = 0;
            int i2 = this.tmpLocationOnScreen[0];
            UniqueObjectHostView uniqueObjectHostView2 = this.hostView;
            if (uniqueObjectHostView2 != null) {
                int paddingLeft = i2 + uniqueObjectHostView2.getPaddingLeft();
                int i3 = this.tmpLocationOnScreen[1];
                UniqueObjectHostView uniqueObjectHostView3 = this.hostView;
                if (uniqueObjectHostView3 != null) {
                    int paddingTop = i3 + uniqueObjectHostView3.getPaddingTop();
                    int i4 = this.tmpLocationOnScreen[0];
                    UniqueObjectHostView uniqueObjectHostView4 = this.hostView;
                    if (uniqueObjectHostView4 != null) {
                        int width = i4 + uniqueObjectHostView4.getWidth();
                        UniqueObjectHostView uniqueObjectHostView5 = this.hostView;
                        if (uniqueObjectHostView5 != null) {
                            int paddingRight = width - uniqueObjectHostView5.getPaddingRight();
                            int i5 = this.tmpLocationOnScreen[1];
                            UniqueObjectHostView uniqueObjectHostView6 = this.hostView;
                            if (uniqueObjectHostView6 != null) {
                                int height = i5 + uniqueObjectHostView6.getHeight();
                                UniqueObjectHostView uniqueObjectHostView7 = this.hostView;
                                if (uniqueObjectHostView7 != null) {
                                    int paddingBottom = height - uniqueObjectHostView7.getPaddingBottom();
                                    if (paddingRight < paddingLeft) {
                                        paddingLeft = 0;
                                        paddingRight = 0;
                                    }
                                    if (paddingBottom < paddingTop) {
                                        paddingBottom = 0;
                                    } else {
                                        i = paddingTop;
                                    }
                                    this.currentBounds.set(paddingLeft, i, paddingRight, paddingBottom);
                                    return this.currentBounds;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("hostView");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("hostView");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("hostView");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("hostView");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("hostView");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("hostView");
        throw null;
    }

    public final void addVisibilityChangeListener(@NotNull Function1<? super Boolean, Unit> function1) {
        Intrinsics.checkParameterIsNotNull(function1, "listener");
        this.visibleChangedListeners.add(function1);
    }

    public final void init(int i) {
        this.location = i;
        UniqueObjectHostView register = this.mediaHierarchyManager.register(this);
        this.hostView = register;
        if (register != null) {
            register.addOnAttachStateChangeListener(new MediaHost$init$1(this));
            UniqueObjectHostView uniqueObjectHostView = this.hostView;
            if (uniqueObjectHostView != null) {
                uniqueObjectHostView.setMeasurementManager(new MediaHost$init$2(this, i));
                this.state.setChangedListener(new MediaHost$init$3(this, i));
                updateViewVisibility();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("hostView");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void updateViewVisibility() {
        boolean z;
        if (getShowsOnlyActiveMedia()) {
            z = this.mediaDataFilter.hasActiveMedia();
        } else {
            z = this.mediaDataFilter.hasAnyMedia();
        }
        setVisible(z);
        int i = getVisible() ? 0 : 8;
        UniqueObjectHostView uniqueObjectHostView = this.hostView;
        if (uniqueObjectHostView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        } else if (i != uniqueObjectHostView.getVisibility()) {
            UniqueObjectHostView uniqueObjectHostView2 = this.hostView;
            if (uniqueObjectHostView2 != null) {
                uniqueObjectHostView2.setVisibility(i);
                Iterator<T> it = this.visibleChangedListeners.iterator();
                while (it.hasNext()) {
                    it.next().invoke(Boolean.valueOf(getVisible()));
                }
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("hostView");
            throw null;
        }
    }

    /* compiled from: MediaHost.kt */
    public static final class MediaHostStateHolder implements MediaHostState {
        @Nullable
        private Function0<Unit> changedListener;
        @NotNull
        private DisappearParameters disappearParameters = new DisappearParameters();
        private float expansion;
        private boolean falsingProtectionNeeded;
        private int lastDisappearHash = getDisappearParameters().hashCode();
        @Nullable
        private MeasurementInput measurementInput;
        private boolean showsOnlyActiveMedia;
        private boolean visible = true;

        @Override // com.android.systemui.media.MediaHostState
        @Nullable
        public MeasurementInput getMeasurementInput() {
            return this.measurementInput;
        }

        public void setMeasurementInput(@Nullable MeasurementInput measurementInput2) {
            if (measurementInput2 == null || !measurementInput2.equals(this.measurementInput)) {
                this.measurementInput = measurementInput2;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public float getExpansion() {
            return this.expansion;
        }

        @Override // com.android.systemui.media.MediaHostState
        public void setExpansion(float f) {
            if (!Float.valueOf(f).equals(Float.valueOf(this.expansion))) {
                this.expansion = f;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getShowsOnlyActiveMedia() {
            return this.showsOnlyActiveMedia;
        }

        public void setShowsOnlyActiveMedia(boolean z) {
            if (!Boolean.valueOf(z).equals(Boolean.valueOf(this.showsOnlyActiveMedia))) {
                this.showsOnlyActiveMedia = z;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getVisible() {
            return this.visible;
        }

        public void setVisible(boolean z) {
            if (this.visible != z) {
                this.visible = z;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        public boolean getFalsingProtectionNeeded() {
            return this.falsingProtectionNeeded;
        }

        public void setFalsingProtectionNeeded(boolean z) {
            if (this.falsingProtectionNeeded != z) {
                this.falsingProtectionNeeded = z;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        @Override // com.android.systemui.media.MediaHostState
        @NotNull
        public DisappearParameters getDisappearParameters() {
            return this.disappearParameters;
        }

        public void setDisappearParameters(@NotNull DisappearParameters disappearParameters2) {
            Intrinsics.checkParameterIsNotNull(disappearParameters2, "value");
            int hashCode = disappearParameters2.hashCode();
            if (!Integer.valueOf(this.lastDisappearHash).equals(Integer.valueOf(hashCode))) {
                this.disappearParameters = disappearParameters2;
                this.lastDisappearHash = hashCode;
                Function0<Unit> function0 = this.changedListener;
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        public final void setChangedListener(@Nullable Function0<Unit> function0) {
            this.changedListener = function0;
        }

        @Override // com.android.systemui.media.MediaHostState
        @NotNull
        public MediaHostState copy() {
            MediaHostStateHolder mediaHostStateHolder = new MediaHostStateHolder();
            mediaHostStateHolder.setExpansion(getExpansion());
            mediaHostStateHolder.setShowsOnlyActiveMedia(getShowsOnlyActiveMedia());
            MeasurementInput measurementInput2 = getMeasurementInput();
            MeasurementInput measurementInput3 = null;
            if (measurementInput2 != null) {
                measurementInput3 = MeasurementInput.copy$default(measurementInput2, 0, 0, 3, null);
            }
            mediaHostStateHolder.setMeasurementInput(measurementInput3);
            mediaHostStateHolder.setVisible(getVisible());
            mediaHostStateHolder.setDisappearParameters(getDisappearParameters().deepCopy());
            mediaHostStateHolder.setFalsingProtectionNeeded(getFalsingProtectionNeeded());
            return mediaHostStateHolder;
        }

        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof MediaHostState)) {
                return false;
            }
            MediaHostState mediaHostState = (MediaHostState) obj;
            if (Objects.equals(getMeasurementInput(), mediaHostState.getMeasurementInput()) && getExpansion() == mediaHostState.getExpansion() && getShowsOnlyActiveMedia() == mediaHostState.getShowsOnlyActiveMedia() && getVisible() == mediaHostState.getVisible() && getFalsingProtectionNeeded() == mediaHostState.getFalsingProtectionNeeded() && getDisappearParameters().equals(mediaHostState.getDisappearParameters())) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            MeasurementInput measurementInput2 = getMeasurementInput();
            return ((((((((((measurementInput2 != null ? measurementInput2.hashCode() : 0) * 31) + Float.hashCode(getExpansion())) * 31) + Boolean.hashCode(getFalsingProtectionNeeded())) * 31) + Boolean.hashCode(getShowsOnlyActiveMedia())) * 31) + (getVisible() ? 1 : 2)) * 31) + getDisappearParameters().hashCode();
        }
    }
}
