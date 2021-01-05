package com.android.systemui.util.animation;

import android.graphics.PointF;
import android.view.View;
import java.util.LinkedHashMap;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TransitionLayout.kt */
public final class TransitionViewState {
    private float alpha = 1.0f;
    @NotNull
    private final PointF contentTranslation = new PointF();
    private int height;
    @NotNull
    private final PointF translation = new PointF();
    @NotNull
    private Map<Integer, WidgetState> widgetStates = new LinkedHashMap();
    private int width;

    @NotNull
    public final Map<Integer, WidgetState> getWidgetStates() {
        return this.widgetStates;
    }

    public final int getWidth() {
        return this.width;
    }

    public final void setWidth(int i) {
        this.width = i;
    }

    public final int getHeight() {
        return this.height;
    }

    public final void setHeight(int i) {
        this.height = i;
    }

    public final float getAlpha() {
        return this.alpha;
    }

    public final void setAlpha(float f) {
        this.alpha = f;
    }

    @NotNull
    public final PointF getTranslation() {
        return this.translation;
    }

    @NotNull
    public final PointF getContentTranslation() {
        return this.contentTranslation;
    }

    public static /* synthetic */ TransitionViewState copy$default(TransitionViewState transitionViewState, TransitionViewState transitionViewState2, int i, Object obj) {
        if ((i & 1) != 0) {
            transitionViewState2 = null;
        }
        return transitionViewState.copy(transitionViewState2);
    }

    @NotNull
    public final TransitionViewState copy(@Nullable TransitionViewState transitionViewState) {
        TransitionViewState transitionViewState2 = transitionViewState != null ? transitionViewState : new TransitionViewState();
        transitionViewState2.width = this.width;
        transitionViewState2.height = this.height;
        transitionViewState2.alpha = this.alpha;
        PointF pointF = transitionViewState2.translation;
        PointF pointF2 = this.translation;
        pointF.set(pointF2.x, pointF2.y);
        PointF pointF3 = transitionViewState2.contentTranslation;
        PointF pointF4 = this.contentTranslation;
        pointF3.set(pointF4.x, pointF4.y);
        for (Map.Entry next : this.widgetStates.entrySet()) {
            transitionViewState2.widgetStates.put(next.getKey(), WidgetState.copy$default((WidgetState) next.getValue(), 0.0f, 0.0f, 0, 0, 0, 0, 0.0f, 0.0f, false, 511, (Object) null));
        }
        return transitionViewState2;
    }

    public final void initFromLayout(@NotNull TransitionLayout transitionLayout) {
        TransitionLayout transitionLayout2 = transitionLayout;
        Intrinsics.checkParameterIsNotNull(transitionLayout2, "transitionLayout");
        int childCount = transitionLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = transitionLayout2.getChildAt(i);
            Map<Integer, WidgetState> map = this.widgetStates;
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            Integer valueOf = Integer.valueOf(childAt.getId());
            WidgetState widgetState = map.get(valueOf);
            if (widgetState == null) {
                widgetState = new WidgetState(0.0f, 0.0f, 0, 0, 0, 0, 0.0f, 0.0f, false, 384, (DefaultConstructorMarker) null);
                map.put(valueOf, widgetState);
            }
            widgetState.initFromLayout(childAt);
        }
        this.width = transitionLayout.getMeasuredWidth();
        this.height = transitionLayout.getMeasuredHeight();
        this.translation.set(0.0f, 0.0f);
        this.contentTranslation.set(0.0f, 0.0f);
        this.alpha = 1.0f;
    }
}
