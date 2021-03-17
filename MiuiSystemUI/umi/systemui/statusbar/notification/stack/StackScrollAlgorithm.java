package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.MathUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.FooterView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StackScrollAlgorithm {
    private boolean mClipNotificationScrollToTop;
    private int mCollapsedSize;
    private int mGapHeight;
    protected float mHeadsUpInset;
    private final ViewGroup mHostView;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsExpanded;
    private int mPaddingBetweenElements;
    protected int mPinnedZTranslationExtra;
    private int mStatusBarHeight;
    private StackScrollAlgorithmState mTempAlgorithmState = new StackScrollAlgorithmState();

    public interface SectionProvider {
        boolean beginsSection(View view, View view2);
    }

    /* access modifiers changed from: protected */
    public abstract void updateZValuesForState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState);

    public StackScrollAlgorithm(Context context, ViewGroup viewGroup) {
        this.mHostView = viewGroup;
        initView(context);
    }

    public void initView(Context context) {
        initConstants(context);
    }

    private void initConstants(Context context) {
        Resources resources = context.getResources();
        this.mPaddingBetweenElements = resources.getDimensionPixelSize(C0012R$dimen.notification_divider_height);
        this.mIncreasedPaddingBetweenElements = resources.getDimensionPixelSize(C0012R$dimen.notification_divider_height_increased);
        this.mCollapsedSize = resources.getDimensionPixelSize(C0012R$dimen.notification_min_height);
        this.mStatusBarHeight = resources.getDimensionPixelSize(C0012R$dimen.status_bar_height);
        this.mClipNotificationScrollToTop = resources.getBoolean(C0010R$bool.config_clipNotificationScrollToTop);
        this.mHeadsUpInset = (float) (this.mStatusBarHeight + resources.getDimensionPixelSize(C0012R$dimen.heads_up_status_bar_padding));
        this.mPinnedZTranslationExtra = resources.getDimensionPixelSize(C0012R$dimen.heads_up_pinned_elevation);
        this.mGapHeight = resources.getDimensionPixelSize(C0012R$dimen.notification_section_divider_height);
    }

    public void resetViewStates(AmbientState ambientState) {
        StackScrollAlgorithmState stackScrollAlgorithmState = this.mTempAlgorithmState;
        resetChildViewStates();
        initAlgorithmState(this.mHostView, stackScrollAlgorithmState, ambientState);
        updatePositionsForState(stackScrollAlgorithmState, ambientState);
        updateZValuesForState(stackScrollAlgorithmState, ambientState);
        updateHeadsUpStates(stackScrollAlgorithmState, ambientState);
        updatePulsingStates(stackScrollAlgorithmState, ambientState);
        updateDimmedActivatedHideSensitive(ambientState, stackScrollAlgorithmState);
        updateClipping(stackScrollAlgorithmState, ambientState);
        updateSpeedBumpState(stackScrollAlgorithmState, ambientState);
        updateShelfState(ambientState);
        getNotificationChildrenStates(stackScrollAlgorithmState, ambientState);
    }

    private void resetChildViewStates() {
        int childCount = this.mHostView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((ExpandableView) this.mHostView.getChildAt(i)).resetViewState();
        }
    }

    private void getNotificationChildrenStates(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).updateChildrenStates(ambientState);
            }
        }
    }

    private void updateSpeedBumpState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int speedBumpIndex = ambientState.getSpeedBumpIndex();
        int i = 0;
        while (i < size) {
            stackScrollAlgorithmState.visibleChildren.get(i).getViewState().belowSpeedBump = i >= speedBumpIndex;
            i++;
        }
    }

    private void updateShelfState(AmbientState ambientState) {
        NotificationShelf shelf = ambientState.getShelf();
        if (shelf != null) {
            shelf.updateState(ambientState);
        }
    }

    /* access modifiers changed from: protected */
    public void updateClipping(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float f = 0.0f;
        float topPadding = !ambientState.isOnKeyguard() ? ambientState.getTopPadding() + ambientState.getStackTranslation() + ((float) ambientState.getExpandAnimationTopChange()) : 0.0f;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        boolean z = true;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewState = expandableView.getViewState();
            if (!expandableView.mustStayOnScreen() || viewState.headsUpIsVisible) {
                f = Math.max(topPadding, f);
            }
            float f2 = viewState.yTranslation;
            float f3 = ((float) viewState.height) + f2;
            boolean z2 = expandableView instanceof ExpandableNotificationRow;
            boolean z3 = z2 && ((ExpandableNotificationRow) expandableView).isPinned();
            if (!this.mClipNotificationScrollToTop || ((viewState.inShelf && (!z3 || z)) || f2 >= f)) {
                viewState.clipTopAmount = 0;
            } else {
                viewState.clipTopAmount = (int) (f - f2);
            }
            if (z3) {
                z = false;
            }
            if (z2 && !expandableView.isTransparent()) {
                if (!z3) {
                    f2 = f3;
                }
                f = Math.max(f, f2);
            }
        }
    }

    private void updateDimmedActivatedHideSensitive(AmbientState ambientState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        boolean isDimmed = ambientState.isDimmed();
        boolean isHideSensitive = ambientState.isHideSensitive();
        ActivatableNotificationView activatedChild = ambientState.getActivatedChild();
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewState = expandableView.getViewState();
            viewState.dimmed = isDimmed;
            boolean z = true;
            viewState.hideSensitive = (expandableView instanceof ExpandableNotificationRow ? ((ExpandableNotificationRow) expandableView).getEntry().hideSensitiveByAppLock : false) || isHideSensitive;
            if (activatedChild != expandableView) {
                z = false;
            }
            if (isDimmed && z) {
                viewState.zTranslation += ((float) ambientState.getZDistanceBetweenElements()) * 2.0f;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initAlgorithmState(ViewGroup viewGroup, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int i;
        stackScrollAlgorithmState.scrollY = (int) (((float) Math.max(0, ambientState.getScrollY())) + ambientState.getOverScrollAmount(false));
        int childCount = viewGroup.getChildCount();
        stackScrollAlgorithmState.visibleChildren.clear();
        stackScrollAlgorithmState.visibleChildren.ensureCapacity(childCount);
        stackScrollAlgorithmState.paddingMap.clear();
        int i2 = ambientState.isDozing() ? ambientState.hasPulsingNotifications() ? 1 : 0 : childCount;
        int i3 = 0;
        ExpandableView expandableView = null;
        for (int i4 = 0; i4 < childCount; i4++) {
            ExpandableView expandableView2 = (ExpandableView) viewGroup.getChildAt(i4);
            if (!((expandableView2.getViewState() != null && expandableView2.getViewState().hidden) || expandableView2.getVisibility() == 8 || expandableView2 == ambientState.getShelf())) {
                if (i4 >= i2) {
                    expandableView = null;
                }
                i3 = updateNotGoneIndex(stackScrollAlgorithmState, i3, expandableView2);
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                int i5 = (increasedPaddingAmount > 0.0f ? 1 : (increasedPaddingAmount == 0.0f ? 0 : -1));
                if (i5 != 0) {
                    stackScrollAlgorithmState.paddingMap.put(expandableView2, Float.valueOf(increasedPaddingAmount));
                    if (expandableView != null) {
                        Float f = stackScrollAlgorithmState.paddingMap.get(expandableView);
                        float paddingForValue = getPaddingForValue(Float.valueOf(increasedPaddingAmount));
                        if (f != null) {
                            float paddingForValue2 = getPaddingForValue(f);
                            if (i5 > 0) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue2, paddingForValue, increasedPaddingAmount);
                            } else if (f.floatValue() > 0.0f) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue, paddingForValue2, f.floatValue());
                            }
                        }
                        stackScrollAlgorithmState.paddingMap.put(expandableView, Float.valueOf(paddingForValue));
                    }
                } else if (expandableView != null) {
                    stackScrollAlgorithmState.paddingMap.put(expandableView, Float.valueOf(getPaddingForValue(stackScrollAlgorithmState.paddingMap.get(expandableView))));
                }
                if (expandableView2 instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
                    List<ExpandableNotificationRow> attachedChildren = expandableNotificationRow.getAttachedChildren();
                    if (expandableNotificationRow.isSummaryWithChildren() && attachedChildren != null) {
                        for (ExpandableNotificationRow expandableNotificationRow2 : attachedChildren) {
                            if (expandableNotificationRow2.getVisibility() != 8) {
                                expandableNotificationRow2.getViewState().notGoneIndex = i3;
                                i3++;
                            }
                        }
                    }
                }
                expandableView = expandableView2;
            }
        }
        ExpandableNotificationRow expandingNotification = ambientState.getExpandingNotification();
        if (expandingNotification != null) {
            i = expandingNotification.isChildInGroup() ? stackScrollAlgorithmState.visibleChildren.indexOf(expandingNotification.getNotificationParent()) : stackScrollAlgorithmState.visibleChildren.indexOf(expandingNotification);
        } else {
            i = -1;
        }
        stackScrollAlgorithmState.indexOfExpandingNotification = i;
    }

    private float getPaddingForValue(Float f) {
        if (f == null) {
            return (float) this.mPaddingBetweenElements;
        }
        if (f.floatValue() >= 0.0f) {
            return NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, f.floatValue());
        }
        return NotificationUtils.interpolate(0.0f, (float) this.mPaddingBetweenElements, f.floatValue() + 1.0f);
    }

    private int updateNotGoneIndex(StackScrollAlgorithmState stackScrollAlgorithmState, int i, ExpandableView expandableView) {
        expandableView.getViewState().notGoneIndex = i;
        stackScrollAlgorithmState.visibleChildren.add(expandableView);
        return i + 1;
    }

    /* access modifiers changed from: protected */
    public void updatePositionsForState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        float f = (float) (-stackScrollAlgorithmState.scrollY);
        for (int i = 0; i < size; i++) {
            f = updateChild(i, stackScrollAlgorithmState, ambientState, f, false);
        }
    }

    /* access modifiers changed from: protected */
    public float updateChild(int i, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState, float f, boolean z) {
        float f2;
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        boolean childNeedsGapHeight = childNeedsGapHeight(ambientState.getSectionProvider(), stackScrollAlgorithmState.anchorViewIndex, i, expandableView, i > 0 ? (ExpandableView) stackScrollAlgorithmState.visibleChildren.get(i - 1) : null);
        ExpandableViewState viewState = expandableView.getViewState();
        boolean z2 = false;
        viewState.location = 0;
        float f3 = (!childNeedsGapHeight || z) ? f : f + ((float) this.mGapHeight);
        int paddingAfterChild = getPaddingAfterChild(stackScrollAlgorithmState, ambientState, expandableView, i);
        int maxAllowedChildHeight = getMaxAllowedChildHeight(expandableView);
        if (z) {
            viewState.yTranslation = f3 - ((float) (maxAllowedChildHeight + paddingAfterChild));
            if (f3 <= 0.0f) {
                viewState.location = 2;
            }
        } else {
            viewState.yTranslation = f3;
        }
        boolean z3 = expandableView instanceof FooterView;
        boolean z4 = expandableView instanceof EmptyShadeView;
        viewState.location = 4;
        float topPadding = ambientState.getTopPadding() + ambientState.getStackTranslation();
        if (i <= stackScrollAlgorithmState.getIndexOfExpandingNotification()) {
            topPadding += (float) ambientState.getExpandAnimationTopChange();
        }
        if (expandableView.mustStayOnScreen()) {
            float f4 = viewState.yTranslation;
            if (f4 >= 0.0f) {
                if (f4 + ((float) viewState.height) + topPadding < ambientState.getMaxHeadsUpTranslation()) {
                    z2 = true;
                }
                viewState.headsUpIsVisible = z2;
            }
        }
        if (z3) {
            viewState.yTranslation = Math.min(viewState.yTranslation, (float) (ambientState.getInnerHeight() - maxAllowedChildHeight));
        } else if (z4) {
            viewState.yTranslation = ((float) (ambientState.getInnerHeight() - maxAllowedChildHeight)) + (ambientState.getStackTranslation() * 0.25f);
        } else if (expandableView != ambientState.getTrackedHeadsUpRow()) {
            clampPositionToShelf(expandableView, viewState, ambientState);
        }
        if (z) {
            f2 = viewState.yTranslation;
            if (childNeedsGapHeight) {
                f2 -= (float) this.mGapHeight;
            }
        } else {
            f2 = ((float) paddingAfterChild) + viewState.yTranslation + ((float) maxAllowedChildHeight);
            if (f2 <= 0.0f) {
                viewState.location = 2;
            }
        }
        if (viewState.location == 0) {
            Log.wtf("StackScrollAlgorithm", "Failed to assign location for child " + i);
        }
        viewState.yTranslation += topPadding;
        return f2;
    }

    public float getGapHeightForChild(SectionProvider sectionProvider, int i, int i2, View view, View view2) {
        if (childNeedsGapHeight(sectionProvider, i, i2, view, view2)) {
            return (float) this.mGapHeight;
        }
        return 0.0f;
    }

    private boolean childNeedsGapHeight(SectionProvider sectionProvider, int i, int i2, View view, View view2) {
        return sectionProvider.beginsSection(view, view2) && i2 > 0;
    }

    /* access modifiers changed from: protected */
    public int getPaddingAfterChild(StackScrollAlgorithmState stackScrollAlgorithmState, ExpandableView expandableView) {
        return stackScrollAlgorithmState.getPaddingAfterChild(expandableView);
    }

    /* access modifiers changed from: protected */
    public int getPaddingAfterChild(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState, ExpandableView expandableView, int i) {
        return getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
    }

    private void updatePulsingStates(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                if (expandableNotificationRow.showingPulsing() && (i != 0 || !ambientState.isPulseExpanding())) {
                    expandableNotificationRow.getViewState().hidden = false;
                }
            }
        }
    }

    private void updateHeadsUpStates(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        ExpandableViewState viewState;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        ExpandableNotificationRow trackedHeadsUpRow = ambientState.getTrackedHeadsUpRow();
        if (!(trackedHeadsUpRow == null || (viewState = trackedHeadsUpRow.getViewState()) == null)) {
            viewState.yTranslation = MathUtils.lerp(this.mHeadsUpInset, viewState.yTranslation - ambientState.getStackTranslation(), ambientState.getAppearFraction());
        }
        ExpandableNotificationRow expandableNotificationRow = null;
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i2);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) expandableView;
                if (expandableNotificationRow2.isHeadsUp()) {
                    ExpandableViewState viewState2 = expandableNotificationRow2.getViewState();
                    boolean z = true;
                    if (expandableNotificationRow == null && expandableNotificationRow2.mustStayOnScreen() && !viewState2.headsUpIsVisible) {
                        viewState2.location = 1;
                        expandableNotificationRow = expandableNotificationRow2;
                    }
                    if (expandableNotificationRow != expandableNotificationRow2) {
                        z = false;
                    }
                    float f = viewState2.yTranslation + ((float) viewState2.height);
                    if (this.mIsExpanded && expandableNotificationRow2.mustStayOnScreen() && !viewState2.headsUpIsVisible && !expandableNotificationRow2.showingPulsing()) {
                        clampHunToTop(ambientState, expandableNotificationRow2, viewState2);
                        if (z && expandableNotificationRow2.isAboveShelf()) {
                            clampHunToMaxTranslation(ambientState, expandableNotificationRow2, viewState2);
                            viewState2.hidden = false;
                        }
                    }
                    if (expandableNotificationRow2.isPinned()) {
                        viewState2.yTranslation = Math.max(viewState2.yTranslation, this.mHeadsUpInset);
                        viewState2.height = Math.max(expandableNotificationRow2.getIntrinsicHeight(), viewState2.height);
                        if (i < 2) {
                            viewState2.hidden = false;
                            i++;
                        }
                        ExpandableViewState viewState3 = expandableNotificationRow == null ? null : expandableNotificationRow.getViewState();
                        if (viewState3 != null && !z && (!this.mIsExpanded || f > viewState3.yTranslation + ((float) viewState3.height))) {
                            int intrinsicHeight = expandableNotificationRow2.getIntrinsicHeight();
                            viewState2.height = intrinsicHeight;
                            viewState2.yTranslation = Math.min((viewState3.yTranslation + ((float) viewState3.height)) - ((float) intrinsicHeight), viewState2.yTranslation);
                        }
                        if (!this.mIsExpanded && z && ambientState.getScrollY() > 0) {
                            viewState2.yTranslation -= (float) ambientState.getScrollY();
                        }
                    }
                    if (expandableNotificationRow2.isHeadsUpAnimatingAway()) {
                        viewState2.hidden = false;
                    }
                }
            }
        }
    }

    private void clampHunToTop(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, ExpandableViewState expandableViewState) {
        float max = Math.max(ambientState.getTopPadding() + ambientState.getStackTranslation(), expandableViewState.yTranslation);
        expandableViewState.height = (int) Math.max(((float) expandableViewState.height) - (max - expandableViewState.yTranslation), (float) expandableNotificationRow.getCollapsedHeight());
        expandableViewState.yTranslation = max;
    }

    private void clampHunToMaxTranslation(AmbientState ambientState, ExpandableNotificationRow expandableNotificationRow, ExpandableViewState expandableViewState) {
        float min = Math.min(ambientState.getMaxHeadsUpTranslation(), ((float) ambientState.getInnerHeight()) + ambientState.getTopPadding() + ambientState.getStackTranslation());
        float min2 = Math.min(expandableViewState.yTranslation, min - ((float) expandableNotificationRow.getCollapsedHeight()));
        expandableViewState.height = (int) Math.min((float) expandableViewState.height, min - min2);
        expandableViewState.yTranslation = min2;
    }

    private void clampPositionToShelf(ExpandableView expandableView, ExpandableViewState expandableViewState, AmbientState ambientState) {
        if (ambientState.getShelf() != null) {
            ExpandableNotificationRow trackedHeadsUpRow = ambientState.getTrackedHeadsUpRow();
            boolean z = trackedHeadsUpRow != null && this.mHostView.indexOfChild(expandableView) < this.mHostView.indexOfChild(trackedHeadsUpRow);
            int innerHeight = ambientState.getInnerHeight() - ambientState.getShelf().getIntrinsicHeight();
            if (!expandableView.isPinned() && !expandableView.isHeadsUpAnimatingAway()) {
                innerHeight = ambientState.isOnKeyguard() ? (innerHeight + ((int) ambientState.getTopPadding())) - ambientState.getStaticTopPadding() : (int) (((float) ambientState.getInnerHeight()) + ambientState.getTopPadding());
            }
            if (ambientState.isAppearing() && !expandableView.isAboveShelf() && !z && !ambientState.isNCSwitching()) {
                expandableViewState.yTranslation = Math.max(expandableViewState.yTranslation, (float) innerHeight);
            }
            float f = (float) innerHeight;
            float min = Math.min(expandableViewState.yTranslation, f);
            expandableViewState.yTranslation = min;
            if (min >= f) {
                expandableViewState.hidden = !expandableView.isExpandAnimationRunning() && !expandableView.hasExpandingChild();
                expandableViewState.inShelf = true;
                expandableViewState.headsUpIsVisible = false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getMaxAllowedChildHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view == null ? this.mCollapsedSize : view.getHeight();
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }

    public class StackScrollAlgorithmState {
        public int anchorViewIndex;
        private int indexOfExpandingNotification;
        public final HashMap<ExpandableView, Float> paddingMap = new HashMap<>();
        public int scrollY;
        public final ArrayList<ExpandableView> visibleChildren = new ArrayList<>();

        public StackScrollAlgorithmState() {
        }

        public int getPaddingAfterChild(ExpandableView expandableView) {
            Float f = this.paddingMap.get(expandableView);
            if (f == null) {
                return StackScrollAlgorithm.this.mPaddingBetweenElements;
            }
            return (int) f.floatValue();
        }

        public int getIndexOfExpandingNotification() {
            return this.indexOfExpandingNotification;
        }
    }
}
