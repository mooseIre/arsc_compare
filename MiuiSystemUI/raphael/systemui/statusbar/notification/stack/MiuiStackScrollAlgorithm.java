package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentInflaterInjector;
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.ConvenienceExtensionsKt;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiStackScrollAlgorithm.kt */
public final class MiuiStackScrollAlgorithm extends StackScrollAlgorithm {
    private final Context mContext;
    private int mGroupMinusBottom;
    private int mGroupMinusTop;
    private int mHeadsUpMarginTop;
    private int mLatestVisibleChildrenCount;
    private int mStatusBarHeight;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiStackScrollAlgorithm(@NotNull Context context, @NotNull ViewGroup viewGroup) {
        super(context, viewGroup);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(viewGroup, "hostView");
        this.mContext = context;
        updateResources();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(new ConfigurationController.ConfigurationListener(this) {
            /* class com.android.systemui.statusbar.notification.stack.MiuiStackScrollAlgorithm.AnonymousClass1 */
            final /* synthetic */ MiuiStackScrollAlgorithm this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onDensityOrFontScaleChanged() {
                MiuiStackScrollAlgorithm miuiStackScrollAlgorithm = this.this$0;
                miuiStackScrollAlgorithm.initView(miuiStackScrollAlgorithm.mContext);
                this.this$0.updateResources();
            }
        });
    }

    public final void updateResources() {
        this.mGroupMinusTop = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.notification_section_group_divider_top_minus);
        this.mGroupMinusBottom = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.notification_section_group_divider_bottom_minus);
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(17105489);
        this.mHeadsUpMarginTop = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.heads_up_status_bar_padding);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm
    public void initAlgorithmState(@Nullable ViewGroup viewGroup, @Nullable StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @Nullable AmbientState ambientState) {
        updateSectionHeadersVisibility(viewGroup);
        super.initAlgorithmState(viewGroup, stackScrollAlgorithmState, ambientState);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm
    public void updatePositionsForState(@NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @NotNull AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        this.mLatestVisibleChildrenCount = stackScrollAlgorithmState.visibleChildren.size();
        evaluateHeadsUpInsets();
        updateChildrenSpringYOffset(stackScrollAlgorithmState, ambientState);
        updateChildrenAppearDisappearState(stackScrollAlgorithmState, ambientState);
        updateHeadsUpAnimatingAwayState(stackScrollAlgorithmState);
        super.updatePositionsForState(stackScrollAlgorithmState, ambientState);
    }

    private final void evaluateHeadsUpInsets() {
        Resources resources = this.mContext.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "mContext.resources");
        int i = 0;
        boolean z = resources.getConfiguration().orientation == 2;
        int i2 = this.mHeadsUpMarginTop;
        if (!z) {
            i = this.mStatusBarHeight;
        }
        this.mHeadsUpInset = (float) (i2 + i);
    }

    private final void updateChildrenSpringYOffset(StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        boolean panelStretching = ambientState.getPanelStretching();
        ArrayList<ExpandableView> arrayList = stackScrollAlgorithmState.visibleChildren;
        Intrinsics.checkExpressionValueIsNotNull(arrayList, "algorithmState.visibleChildren");
        float f = 0.0f;
        int i = 0;
        for (T t : arrayList) {
            int i2 = i + 1;
            if (i >= 0) {
                T t2 = t;
                Intrinsics.checkExpressionValueIsNotNull(t2, "child");
                ExpandableViewState viewState = t2.getViewState();
                if (panelStretching) {
                    float f2 = ((float) 1) - ((((float) i) * 1.0f) / ((float) RangesKt___RangesKt.coerceAtMost(size, 10)));
                    f += 0.15f * f2 * f2 * ambientState.getSpringLength();
                    if (viewState != null) {
                        viewState.setSpringYOffset((int) (ambientState.getSpringLength() + f));
                    }
                }
                t2.setTag(C0015R$id.miui_child_index_hint, Integer.valueOf(i));
                i = i2;
            } else {
                CollectionsKt.throwIndexOverflow();
                throw null;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0078 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateChildrenAppearDisappearState(com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm.StackScrollAlgorithmState r8, com.android.systemui.statusbar.notification.stack.AmbientState r9) {
        /*
        // Method dump skipped, instructions count: 128
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.MiuiStackScrollAlgorithm.updateChildrenAppearDisappearState(com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm$StackScrollAlgorithmState, com.android.systemui.statusbar.notification.stack.AmbientState):void");
    }

    private final void updateHeadsUpAnimatingAwayState(StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState) {
        int i;
        ArrayList<ExpandableView> arrayList = stackScrollAlgorithmState.visibleChildren;
        Intrinsics.checkExpressionValueIsNotNull(arrayList, "algorithmState.visibleChildren");
        ArrayList arrayList2 = new ArrayList();
        Iterator<T> it = arrayList.iterator();
        while (true) {
            i = 0;
            if (!it.hasNext()) {
                break;
            }
            T next = it.next();
            T t = next;
            if ((t instanceof ExpandableNotificationRow) && t.isHeadsUpAnimatingAway()) {
                i = 1;
            }
            if (i != 0) {
                arrayList2.add(next);
            }
        }
        for (Object obj : arrayList2) {
            int i2 = i + 1;
            if (i >= 0) {
                ExpandableView expandableView = (ExpandableView) obj;
                if (i == 0) {
                    Intrinsics.checkExpressionValueIsNotNull(expandableView, "view");
                    ExpandableViewState viewState = expandableView.getViewState();
                    if (viewState != null) {
                        viewState.yTranslation = -((float) expandableView.getActualHeight());
                    }
                    ExpandableViewState viewState2 = expandableView.getViewState();
                    if (viewState2 != null) {
                        viewState2.alpha = 1.0f;
                    }
                } else {
                    Intrinsics.checkExpressionValueIsNotNull(expandableView, "view");
                    ExpandableViewState viewState3 = expandableView.getViewState();
                    if (viewState3 != null) {
                        viewState3.alpha = 0.0f;
                    }
                }
                i = i2;
            } else {
                CollectionsKt.throwIndexOverflow();
                throw null;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm
    public void updateZValuesForState(@NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @NotNull AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        float f = (float) 2;
        boolean isTransparentAble = NotificationContentInflaterInjector.isTransparentAble();
        ArrayList<ExpandableView> arrayList = stackScrollAlgorithmState.visibleChildren;
        Intrinsics.checkExpressionValueIsNotNull(arrayList, "algorithmState.visibleChildren");
        for (T t : arrayList) {
            if (!isTransparentAble && (t instanceof ExpandableNotificationRow)) {
                T t2 = t;
                if (t2.isPinned()) {
                    f = updateChildZValue(t2, f, ambientState);
                }
            }
            Intrinsics.checkExpressionValueIsNotNull(t, "it");
            ExpandableViewState viewState = t.getViewState();
            if (viewState != null) {
                viewState.zTranslation = 0.0f;
            }
        }
    }

    private final float updateChildZValue(ExpandableNotificationRow expandableNotificationRow, float f, AmbientState ambientState) {
        ExpandableViewState viewState = expandableNotificationRow.getViewState();
        int zDistanceBetweenElements = ambientState.getZDistanceBetweenElements();
        float baseZHeight = (float) ambientState.getBaseZHeight();
        float f2 = 0.0f;
        if (f > 0.0f) {
            f2 = f * ((float) zDistanceBetweenElements);
        }
        if (viewState != null) {
            viewState.zTranslation = baseZHeight + f2 + ((1.0f - expandableNotificationRow.getHeaderVisibleAmount()) * ((float) this.mPinnedZTranslationExtra));
        }
        return f - ((float) 1);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm
    public void updateClipping(@NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @Nullable AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        super.updateClipping(stackScrollAlgorithmState, ambientState);
        updateClippingForSpringOffset(stackScrollAlgorithmState);
    }

    private final void updateClippingForSpringOffset(StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState) {
        ExpandableViewState viewState;
        ArrayList<ExpandableView> arrayList = stackScrollAlgorithmState.visibleChildren;
        Intrinsics.checkExpressionValueIsNotNull(arrayList, "algorithmState.visibleChildren");
        ExpandableView expandableView = (ExpandableView) CollectionsKt.firstOrNull(arrayList);
        if (expandableView != null && (viewState = expandableView.getViewState()) != null && viewState.getSpringYOffset() < 0) {
            viewState.setSpringYOffset(0);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm
    public float updateChild(int i, @NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @NotNull AmbientState ambientState, float f, boolean z) {
        ExpandableViewState viewState;
        ExpandableViewState viewState2;
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        float updateChild = super.updateChild(i, stackScrollAlgorithmState, ambientState, f, z);
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        if ((expandableView instanceof EmptyShadeView) && (viewState2 = ((EmptyShadeView) expandableView).getViewState()) != null) {
            viewState2.yTranslation = Math.max(viewState2.yTranslation, (((float) ambientState.getStackScrollLayoutHeight()) / 2.0f) - ((float) viewState2.height));
        }
        if (!(expandableView == null || (viewState = expandableView.getViewState()) == null)) {
            viewState.yTranslation += (float) viewState.getSpringYOffset();
        }
        return updateChild;
    }

    private final void updateSectionHeadersVisibility(ViewGroup viewGroup) {
        if (!(viewGroup == null || viewGroup.getChildCount() == 0)) {
            boolean z = false;
            for (View view : ConvenienceExtensionsKt.getChildren(viewGroup)) {
                if ((view instanceof SectionHeaderView) || (view instanceof PeopleHubView)) {
                    ExpandableViewState viewState = ((ExpandableView) view).getViewState();
                    if (viewState != null) {
                        viewState.hidden = !z;
                    }
                    if (z) {
                    }
                } else if (!z) {
                    Intrinsics.checkExpressionValueIsNotNull(view, "child");
                    if (view.getVisibility() != 0) {
                    }
                }
                z = true;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm
    public int getPaddingAfterChild(@NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @NotNull AmbientState ambientState, @NotNull ExpandableView expandableView, int i) {
        int i2;
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        Intrinsics.checkParameterIsNotNull(expandableView, "child");
        if ((expandableView instanceof MiuiExpandableNotificationRow) && ((MiuiExpandableNotificationRow) expandableView).isGroupExpanded()) {
            int i3 = i + 1;
            if (stackScrollAlgorithmState.visibleChildren.size() > i3) {
                ExpandableView expandableView2 = stackScrollAlgorithmState.visibleChildren.get(i3);
                if ((expandableView2 instanceof ZenModeView) || (expandableView2 instanceof SectionHeaderView) || (expandableView2 instanceof PeopleHubView)) {
                    return this.mGroupMinusBottom + super.getPaddingAfterChild(stackScrollAlgorithmState, ambientState, expandableView, i);
                }
            }
        } else if (((expandableView instanceof SectionHeaderView) || (expandableView instanceof PeopleHubView)) && stackScrollAlgorithmState.visibleChildren.size() > (i2 = i + 1)) {
            ExpandableView expandableView3 = stackScrollAlgorithmState.visibleChildren.get(i2);
            if ((expandableView3 instanceof MiuiExpandableNotificationRow) && ((MiuiExpandableNotificationRow) expandableView3).isGroupExpanded()) {
                return this.mGroupMinusTop;
            }
        }
        return super.getPaddingAfterChild(stackScrollAlgorithmState, ambientState, expandableView, i);
    }

    public final int getLatestVisibleChildCount() {
        return this.mLatestVisibleChildrenCount;
    }
}
