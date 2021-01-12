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
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import com.android.systemui.util.ConvenienceExtensionsKt;
import com.miui.systemui.SettingsManager;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiStackScrollAlgorithm.kt */
public final class MiuiStackScrollAlgorithm extends StackScrollAlgorithm {
    private final Context mContext;
    private final int mGroupMinusBottom;
    private final int mGroupMinusTop;
    private final int mHeadsUpMarginTop;
    private int mLatestVisibleChildrenCount;
    private final int mStatusBarHeight;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiStackScrollAlgorithm(@NotNull Context context, @NotNull ViewGroup viewGroup) {
        super(context, viewGroup);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(viewGroup, "hostView");
        this.mContext = context;
        this.mGroupMinusTop = context.getResources().getDimensionPixelSize(C0012R$dimen.notification_section_group_divider_top_minus);
        this.mGroupMinusBottom = context.getResources().getDimensionPixelSize(C0012R$dimen.notification_section_group_divider_bottom_minus);
        this.mStatusBarHeight = context.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_height);
        this.mHeadsUpMarginTop = context.getResources().getDimensionPixelSize(C0012R$dimen.heads_up_status_bar_padding);
    }

    private final boolean getMGameModeEnabled() {
        return ((SettingsManager) Dependency.get(SettingsManager.class)).getGameModeEnabled();
    }

    /* access modifiers changed from: protected */
    public void initAlgorithmState(@Nullable ViewGroup viewGroup, @Nullable StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @Nullable AmbientState ambientState) {
        updateSectionHeadersVisibility(viewGroup);
        super.initAlgorithmState(viewGroup, stackScrollAlgorithmState, ambientState);
    }

    /* access modifiers changed from: protected */
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
        for (T next : arrayList) {
            int i2 = i + 1;
            if (i >= 0) {
                ExpandableView expandableView = (ExpandableView) next;
                Intrinsics.checkExpressionValueIsNotNull(expandableView, "child");
                ExpandableViewState viewState = expandableView.getViewState();
                if (panelStretching) {
                    float coerceAtMost = ((float) 1) - ((((float) i) * 1.0f) / ((float) RangesKt___RangesKt.coerceAtMost(size, 10)));
                    f += 0.15f * coerceAtMost * coerceAtMost * ambientState.getSpringLength();
                    if (viewState != null) {
                        viewState.setSpringYOffset((int) (ambientState.getSpringLength() + f));
                    }
                }
                expandableView.setTag(C0015R$id.miui_child_index_hint, Integer.valueOf(i));
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
            r7 = this;
            boolean r7 = r9.getPanelAppeared()
            java.util.ArrayList<com.android.systemui.statusbar.notification.row.ExpandableView> r8 = r8.visibleChildren
            java.lang.String r0 = "algorithmState.visibleChildren"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r0)
            java.util.Iterator r8 = r8.iterator()
            r0 = 0
            r1 = r0
        L_0x0011:
            boolean r2 = r8.hasNext()
            if (r2 == 0) goto L_0x007f
            java.lang.Object r2 = r8.next()
            int r3 = r1 + 1
            if (r1 < 0) goto L_0x007a
            com.android.systemui.statusbar.notification.row.ExpandableView r2 = (com.android.systemui.statusbar.notification.row.ExpandableView) r2
            boolean r1 = r9.getPanelStretchingFromHeadsUp()
            boolean r4 = r2 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            r5 = 1
            if (r4 == 0) goto L_0x0047
            r4 = r2
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r4
            boolean r6 = r4.isPinned()
            if (r6 != 0) goto L_0x0045
            boolean r4 = r4.isHeadsUpAnimatingAway()
            if (r4 != 0) goto L_0x0045
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r4 = r9.getTrackedHeadsUpRow()
            boolean r4 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r2, (java.lang.Object) r4)
            if (r4 == 0) goto L_0x0047
            if (r1 != 0) goto L_0x0047
        L_0x0045:
            r1 = r5
            goto L_0x0048
        L_0x0047:
            r1 = r0
        L_0x0048:
            if (r7 != 0) goto L_0x004e
            if (r1 == 0) goto L_0x004d
            goto L_0x004e
        L_0x004d:
            r5 = r0
        L_0x004e:
            java.lang.String r1 = "child"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r2, r1)
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r2.getViewState()
            if (r1 == 0) goto L_0x0078
            r2 = 1065353216(0x3f800000, float:1.0)
            if (r5 == 0) goto L_0x005f
            r4 = r2
            goto L_0x0060
        L_0x005f:
            r4 = 0
        L_0x0060:
            r1.alpha = r4
            boolean r4 = r1.getAnimatingAddRemove()
            if (r4 != 0) goto L_0x0078
            r4 = 1061997773(0x3f4ccccd, float:0.8)
            if (r5 == 0) goto L_0x006f
            r6 = r2
            goto L_0x0070
        L_0x006f:
            r6 = r4
        L_0x0070:
            r1.scaleX = r6
            if (r5 == 0) goto L_0x0075
            goto L_0x0076
        L_0x0075:
            r2 = r4
        L_0x0076:
            r1.scaleY = r2
        L_0x0078:
            r1 = r3
            goto L_0x0011
        L_0x007a:
            kotlin.collections.CollectionsKt.throwIndexOverflow()
            r7 = 0
            throw r7
        L_0x007f:
            return
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
            ExpandableView expandableView = (ExpandableView) next;
            if ((expandableView instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) expandableView).isHeadsUpAnimatingAway()) {
                i = 1;
            }
            if (i != 0) {
                arrayList2.add(next);
            }
        }
        for (Object next2 : arrayList2) {
            int i2 = i + 1;
            if (i >= 0) {
                ExpandableView expandableView2 = (ExpandableView) next2;
                if (i == 0) {
                    Intrinsics.checkExpressionValueIsNotNull(expandableView2, "view");
                    ExpandableViewState viewState = expandableView2.getViewState();
                    if (viewState != null) {
                        viewState.yTranslation = -((float) expandableView2.getActualHeight());
                    }
                    ExpandableViewState viewState2 = expandableView2.getViewState();
                    if (viewState2 != null) {
                        viewState2.alpha = 1.0f;
                    }
                } else {
                    Intrinsics.checkExpressionValueIsNotNull(expandableView2, "view");
                    ExpandableViewState viewState3 = expandableView2.getViewState();
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
    public void updateZValuesForState(@NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @NotNull AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        float f = 0.0f;
        for (int size = stackScrollAlgorithmState.visibleChildren.size() - 1; size >= 0; size--) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(size);
            if (getMGameModeEnabled() || !(expandableView instanceof ExpandableNotificationRow) || !((ExpandableNotificationRow) expandableView).isPinned()) {
                Intrinsics.checkExpressionValueIsNotNull(expandableView, "child");
                ExpandableViewState viewState = expandableView.getViewState();
                if (viewState != null) {
                    viewState.zTranslation = 0.0f;
                }
            } else {
                f = updateChildZValue(size, f, stackScrollAlgorithmState, ambientState);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateClipping(@NotNull StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState, @Nullable AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(stackScrollAlgorithmState, "algorithmState");
        super.updateClipping(stackScrollAlgorithmState, ambientState);
        updateClippingForSpringOffset(stackScrollAlgorithmState);
    }

    private final void updateClippingForSpringOffset(StackScrollAlgorithm.StackScrollAlgorithmState stackScrollAlgorithmState) {
        ExpandableViewState viewState;
        ArrayList<ExpandableView> arrayList = stackScrollAlgorithmState.visibleChildren;
        Intrinsics.checkExpressionValueIsNotNull(arrayList, "algorithmState.visibleChildren");
        ExpandableView expandableView = (ExpandableView) CollectionsKt___CollectionsKt.firstOrNull(arrayList);
        if (expandableView != null && (viewState = expandableView.getViewState()) != null && viewState.getSpringYOffset() < 0) {
            viewState.clipTopAmount = 0;
        }
    }

    /* access modifiers changed from: protected */
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
        if (viewGroup != null && viewGroup.getChildCount() != 0) {
            boolean z = false;
            for (View next : ConvenienceExtensionsKt.getChildren(viewGroup)) {
                if ((next instanceof SectionHeaderView) || (next instanceof PeopleHubView)) {
                    if (z) {
                        next.setVisibility(0);
                    } else {
                        next.setVisibility(8);
                    }
                } else if (!z) {
                    Intrinsics.checkExpressionValueIsNotNull(next, "child");
                    if (next.getVisibility() != 0) {
                    }
                }
                z = true;
            }
        }
    }

    /* access modifiers changed from: protected */
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
