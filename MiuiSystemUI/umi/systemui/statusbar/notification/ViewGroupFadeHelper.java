package com.android.systemui.statusbar.notification;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.TypeIntrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ViewGroupFadeHelper.kt */
public final class ViewGroupFadeHelper {
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    /* access modifiers changed from: private */
    public static final Function1<View, Boolean> visibilityIncluder = ViewGroupFadeHelper$Companion$visibilityIncluder$1.INSTANCE;

    public static final void fadeOutAllChildrenExcept(@NotNull ViewGroup viewGroup, @NotNull View view, long j, @Nullable Runnable runnable) {
        Companion.fadeOutAllChildrenExcept(viewGroup, view, j, runnable);
    }

    public static final void reset(@NotNull ViewGroup viewGroup) {
        Companion.reset(viewGroup);
    }

    /* compiled from: ViewGroupFadeHelper.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final void fadeOutAllChildrenExcept(@NotNull ViewGroup viewGroup, @NotNull View view, long j, @Nullable Runnable runnable) {
            Intrinsics.checkParameterIsNotNull(viewGroup, "root");
            Intrinsics.checkParameterIsNotNull(view, "excludedView");
            Set<View> gatherViews = gatherViews(viewGroup, view, ViewGroupFadeHelper.visibilityIncluder);
            for (View next : gatherViews) {
                if (next.getHasOverlappingRendering() && next.getLayerType() == 0) {
                    next.setLayerType(2, (Paint) null);
                    next.setTag(C0015R$id.view_group_fade_helper_hardware_layer, Boolean.TRUE);
                }
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
            Intrinsics.checkExpressionValueIsNotNull(ofFloat, "this");
            ofFloat.setDuration(j);
            ofFloat.setInterpolator(Interpolators.ALPHA_OUT);
            long j2 = j;
            ViewGroup viewGroup2 = viewGroup;
            Set<View> set = gatherViews;
            Runnable runnable2 = runnable;
            ofFloat.addUpdateListener(new ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$1(j2, viewGroup2, set, runnable2));
            ofFloat.addListener(new ViewGroupFadeHelper$Companion$fadeOutAllChildrenExcept$$inlined$apply$lambda$2(j2, viewGroup2, set, runnable2));
            ofFloat.start();
            viewGroup.setTag(C0015R$id.view_group_fade_helper_modified_views, gatherViews);
            viewGroup.setTag(C0015R$id.view_group_fade_helper_animator, ofFloat);
        }

        private final Set<View> gatherViews(ViewGroup viewGroup, View view, Function1<? super View, Boolean> function1) {
            LinkedHashSet linkedHashSet = new LinkedHashSet();
            ViewParent parent = view.getParent();
            ViewGroup viewGroup2 = view;
            while (true) {
                ViewGroup viewGroup3 = (ViewGroup) parent;
                View view2 = viewGroup2;
                ViewGroup viewGroup4 = viewGroup3;
                if (viewGroup4 == null) {
                    break;
                }
                int childCount = viewGroup4.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View childAt = viewGroup4.getChildAt(i);
                    Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
                    if (function1.invoke(childAt).booleanValue() && (!Intrinsics.areEqual((Object) view2, (Object) childAt))) {
                        linkedHashSet.add(childAt);
                    }
                }
                if (Intrinsics.areEqual((Object) viewGroup4, (Object) viewGroup)) {
                    break;
                }
                parent = viewGroup4.getParent();
                viewGroup2 = viewGroup4;
            }
            return linkedHashSet;
        }

        public final void reset(@NotNull ViewGroup viewGroup) {
            Intrinsics.checkParameterIsNotNull(viewGroup, "root");
            Set<View> asMutableSet = TypeIntrinsics.asMutableSet(viewGroup.getTag(C0015R$id.view_group_fade_helper_modified_views));
            Animator animator = (Animator) viewGroup.getTag(C0015R$id.view_group_fade_helper_animator);
            if (asMutableSet != null && animator != null) {
                animator.cancel();
                Float f = (Float) viewGroup.getTag(C0015R$id.view_group_fade_helper_previous_value_tag);
                for (View view : asMutableSet) {
                    Float f2 = (Float) view.getTag(C0015R$id.view_group_fade_helper_restore_tag);
                    if (f2 != null) {
                        if (Intrinsics.areEqual(f, view.getAlpha())) {
                            view.setAlpha(f2.floatValue());
                        }
                        if (Intrinsics.areEqual((Object) (Boolean) view.getTag(C0015R$id.view_group_fade_helper_hardware_layer), (Object) Boolean.TRUE)) {
                            view.setLayerType(0, (Paint) null);
                            view.setTag(C0015R$id.view_group_fade_helper_hardware_layer, (Object) null);
                        }
                        view.setTag(C0015R$id.view_group_fade_helper_restore_tag, (Object) null);
                    }
                }
                viewGroup.setTag(C0015R$id.view_group_fade_helper_modified_views, (Object) null);
                viewGroup.setTag(C0015R$id.view_group_fade_helper_previous_value_tag, (Object) null);
                viewGroup.setTag(C0015R$id.view_group_fade_helper_animator, (Object) null);
            }
        }
    }
}
