package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.phone.QSControlCenterTileLayout;
import com.android.systemui.qs.QSTileHost;
import kotlin.jvm.internal.Intrinsics;
import miuix.core.widget.NestedScrollView;
import miuix.springback.view.SpringBackLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterTilesContainer.kt */
public final class ControlCenterTilesContainer extends SpringBackLayout {
    @NotNull
    private LinearLayout container;
    @NotNull
    private NestedScrollView scroller;
    @NotNull
    private QSControlCenterTileLayout tileLayout;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlCenterTilesContainer(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @NotNull
    public final NestedScrollView getScroller() {
        NestedScrollView nestedScrollView = this.scroller;
        if (nestedScrollView != null) {
            return nestedScrollView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("scroller");
        throw null;
    }

    @NotNull
    public final LinearLayout getContainer() {
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            return linearLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("container");
        throw null;
    }

    @NotNull
    public final QSControlCenterTileLayout getTileLayout() {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
        if (qSControlCenterTileLayout != null) {
            return qSControlCenterTileLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
        throw null;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View requireViewById = requireViewById(C0015R$id.tiles_scroller);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(R.id.tiles_scroller)");
        this.scroller = (NestedScrollView) requireViewById;
        View requireViewById2 = requireViewById(C0015R$id.tiles_container);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.tiles_container)");
        this.container = (LinearLayout) requireViewById2;
        View requireViewById3 = requireViewById(C0015R$id.tile_layout);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById(R.id.tile_layout)");
        this.tileLayout = (QSControlCenterTileLayout) requireViewById3;
    }

    public final void setHost(@Nullable QSTileHost qSTileHost) {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.setHost(qSTileHost);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
            throw null;
        }
    }

    public void addView(@Nullable View view) {
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            linearLayout.addView(view);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("container");
            throw null;
        }
    }

    @Override // android.view.ViewGroup
    public void addView(@Nullable View view, int i) {
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            linearLayout.addView(view, i);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("container");
            throw null;
        }
    }

    public void removeView(@Nullable View view) {
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            linearLayout.removeView(view);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("container");
            throw null;
        }
    }

    public final void resetScrollY() {
        setScrollY(0);
        NestedScrollView nestedScrollView = this.scroller;
        if (nestedScrollView != null) {
            nestedScrollView.setScrollY(0);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("scroller");
            throw null;
        }
    }
}
