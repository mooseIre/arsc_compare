package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;
import miuix.core.widget.NestedScrollView;
import miuix.springback.view.SpringBackLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterContentContainer.kt */
public final class ControlCenterContentContainer extends SpringBackLayout {
    @NotNull
    private LinearLayout container;
    @NotNull
    private Space navigationBarSpace;
    @NotNull
    private NestedScrollView scroller;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlCenterContentContainer(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
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
    public final Space getNavigationBarSpace() {
        Space space = this.navigationBarSpace;
        if (space != null) {
            return space;
        }
        Intrinsics.throwUninitializedPropertyAccessException("navigationBarSpace");
        throw null;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0015R$id.content_scroller);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.content_scroller)");
        NestedScrollView nestedScrollView = (NestedScrollView) findViewById;
        this.scroller = nestedScrollView;
        if (nestedScrollView != null) {
            nestedScrollView.setFillViewport(true);
            View findViewById2 = findViewById(C0015R$id.content_container);
            Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.content_container)");
            this.container = (LinearLayout) findViewById2;
            View requireViewById = requireViewById(C0015R$id.navigation_bar_space);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(R.id.navigation_bar_space)");
            this.navigationBarSpace = (Space) requireViewById;
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("scroller");
        throw null;
    }

    public void removeView(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            linearLayout.removeView(view);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("container");
            throw null;
        }
    }

    public void addView(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "child");
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            linearLayout.addView(view);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("container");
            throw null;
        }
    }

    @Override // android.view.ViewGroup
    public void addView(@NotNull View view, int i) {
        Intrinsics.checkParameterIsNotNull(view, "child");
        LinearLayout linearLayout = this.container;
        if (linearLayout != null) {
            linearLayout.addView(view, i);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("container");
            throw null;
        }
    }

    public final boolean isScrolledToBottom() {
        NestedScrollView nestedScrollView = this.scroller;
        if (nestedScrollView != null) {
            int scrollY = nestedScrollView.getScrollY();
            NestedScrollView nestedScrollView2 = this.scroller;
            if (nestedScrollView2 != null) {
                int height = scrollY + nestedScrollView2.getHeight();
                LinearLayout linearLayout = this.container;
                if (linearLayout != null) {
                    return height >= linearLayout.getHeight();
                }
                Intrinsics.throwUninitializedPropertyAccessException("container");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("scroller");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("scroller");
        throw null;
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
