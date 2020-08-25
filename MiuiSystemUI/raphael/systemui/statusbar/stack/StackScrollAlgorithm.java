package com.android.systemui.statusbar.stack;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class StackScrollAlgorithm {
    private int mCollapsedSize;
    private int mHeadsUpMarginTop;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsExpanded;
    private boolean mIsExpandedBecauseOfHeadsUp;
    /* access modifiers changed from: private */
    public int mPaddingBetweenElements;
    private StackScrollAlgorithmState mTempAlgorithmState = new StackScrollAlgorithmState();

    public StackScrollAlgorithm(Context context) {
        initView(context);
    }

    public void initView(Context context) {
        initConstants(context);
    }

    private void initConstants(Context context) {
        this.mPaddingBetweenElements = context.getResources().getDimensionPixelSize(R.dimen.notification_divider_height);
        this.mIncreasedPaddingBetweenElements = context.getResources().getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mCollapsedSize = context.getResources().getDimensionPixelSize(R.dimen.notification_min_height);
        this.mHeadsUpMarginTop = HeadsUpManager.getHeadsUpTopMargin(context);
    }

    public void getStackScrollState(AmbientState ambientState, StackScrollState stackScrollState) {
        StackScrollAlgorithmState stackScrollAlgorithmState = this.mTempAlgorithmState;
        stackScrollState.resetViewStates();
        initAlgorithmState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updatePositionsForState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateZValuesForState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateHeadsUpStates(stackScrollState, stackScrollAlgorithmState);
        handleDraggedViews(ambientState, stackScrollState, stackScrollAlgorithmState);
        updateDimmedActivatedHideSensitive(ambientState, stackScrollState, stackScrollAlgorithmState);
        updateClipping(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateSpeedBumpState(stackScrollState, stackScrollAlgorithmState, ambientState);
        updateShelfState(stackScrollState, ambientState);
        handleRowAppearState(stackScrollAlgorithmState, ambientState);
        getNotificationChildrenStates(stackScrollState, stackScrollAlgorithmState, ambientState);
    }

    private void handleRowAppearState(StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        boolean z = ambientState.isPanelAppear() || ambientState.isOnKeyguard();
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            boolean z2 = expandableView instanceof ExpandableNotificationRow;
            boolean z3 = z2 && ((ExpandableNotificationRow) expandableView).isTouchingScale();
            if (!z || !z3) {
                boolean z4 = z2 && ((ExpandableNotificationRow) expandableView).isPinned();
                boolean z5 = z2 && ((ExpandableNotificationRow) expandableView).isHeadsUpAnimatingAway();
                boolean z6 = z2 && ((ExpandableNotificationRow) expandableView).getTranslation() > 0.0f;
                if (!z4 && !z5 && !z6) {
                    float f = 1.0f;
                    float f2 = z ? 1.0f : 0.8f;
                    if (!z) {
                        f = 0.0f;
                    }
                    ExpandableViewState viewState = expandableView.getViewState();
                    if (viewState != null) {
                        viewState.scaleY = f2;
                        viewState.scaleX = f2;
                        viewState.alpha = f;
                    }
                }
            }
        }
    }

    private void getNotificationChildrenStates(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).getChildrenStates(stackScrollState);
            }
        }
    }

    private void updateSpeedBumpState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        int speedBumpIndex = ambientState.getSpeedBumpIndex();
        int i = 0;
        while (i < size) {
            stackScrollState.getViewStateForView(stackScrollAlgorithmState.visibleChildren.get(i)).belowSpeedBump = i >= speedBumpIndex;
            i++;
        }
    }

    private void updateShelfState(StackScrollState stackScrollState, AmbientState ambientState) {
        ambientState.getShelf().updateState(stackScrollState, ambientState);
    }

    private void updateClipping(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float topPadding = !ambientState.isOnKeyguard() ? ambientState.getTopPadding() + ambientState.getStackTranslation() : 0.0f;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        float f = 0.0f;
        float f2 = 0.0f;
        for (int i = 0; i < size; i++) {
            ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
            if (!expandableView.mustStayOnScreen()) {
                f = Math.max(topPadding, f);
                f2 = Math.max(topPadding, f2);
            }
            float f3 = viewStateForView.yTranslation;
            float f4 = ((float) viewStateForView.height) + f3;
            boolean z = expandableView instanceof ExpandableNotificationRow;
            boolean z2 = true;
            boolean z3 = z && ((ExpandableNotificationRow) expandableView).isPinned();
            if (!z || !((ExpandableNotificationRow) expandableView).isHeadsUpAnimatingAway()) {
                z2 = false;
            }
            if (viewStateForView.inShelf || f3 >= f || ((z3 || z2) && !ambientState.isShadeExpanded())) {
                viewStateForView.clipTopAmount = 0;
            } else {
                viewStateForView.clipTopAmount = (int) (f - f3);
            }
            if (!expandableView.isTransparent()) {
                f2 = f3;
                f = f4;
            }
        }
    }

    public static boolean canChildBeDismissed(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        if (expandableNotificationRow.areGutsExposed()) {
            return false;
        }
        return expandableNotificationRow.canViewBeDismissed();
    }

    private void updateDimmedActivatedHideSensitive(AmbientState ambientState, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        boolean isDimmed = ambientState.isDimmed();
        boolean isDark = ambientState.isDark();
        boolean isHideSensitive = ambientState.isHideSensitive();
        ActivatableNotificationView activatedChild = ambientState.getActivatedChild();
        int size = stackScrollAlgorithmState.visibleChildren.size();
        boolean z = false;
        for (int i = 0; i < size; i++) {
            View view = stackScrollAlgorithmState.visibleChildren.get(i);
            if (view instanceof ExpandableNotificationRow) {
                z = ((ExpandableNotificationRow) view).getEntry().hideSensitiveByAppLock;
            }
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(view);
            viewStateForView.dimmed = isDimmed;
            viewStateForView.dark = isDark;
            boolean z2 = true;
            viewStateForView.hideSensitive = isHideSensitive || z;
            if (activatedChild != view) {
                z2 = false;
            }
            if (isDimmed && z2) {
                viewStateForView.zTranslation += ((float) ambientState.getZDistanceBetweenElements()) * 2.0f;
            }
        }
    }

    private void handleDraggedViews(AmbientState ambientState, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        ArrayList<View> draggedViews = ambientState.getDraggedViews();
        Iterator<View> it = draggedViews.iterator();
        while (it.hasNext()) {
            View next = it.next();
            int indexOf = stackScrollAlgorithmState.visibleChildren.indexOf(next);
            if (indexOf >= 0 && indexOf < stackScrollAlgorithmState.visibleChildren.size() - 1) {
                View view = stackScrollAlgorithmState.visibleChildren.get(indexOf + 1);
                if (!draggedViews.contains(view)) {
                    ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(view);
                    if (ambientState.isShadeExpanded()) {
                        viewStateForView.shadowAlpha = 1.0f;
                        viewStateForView.hidden = false;
                    }
                }
                stackScrollState.getViewStateForView(next).alpha = next.getAlpha();
            }
        }
    }

    private void initAlgorithmState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        StackScrollState stackScrollState2 = stackScrollState;
        StackScrollAlgorithmState stackScrollAlgorithmState2 = stackScrollAlgorithmState;
        stackScrollAlgorithmState2.scrollY = (int) (((float) Math.max(0, ambientState.getScrollY())) + ambientState.getOverScrollAmount(false));
        ViewGroup hostView = stackScrollState.getHostView();
        int childCount = hostView.getChildCount();
        stackScrollAlgorithmState2.visibleChildren.clear();
        stackScrollAlgorithmState2.visibleChildren.ensureCapacity(childCount);
        stackScrollAlgorithmState2.paddingMap.clear();
        ExpandableView expandableView = null;
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            ExpandableView expandableView2 = (ExpandableView) hostView.getChildAt(i2);
            if (!(expandableView2.getVisibility() == 8 || expandableView2 == ambientState.getShelf())) {
                i = updateNotGoneIndex(stackScrollState2, stackScrollAlgorithmState2, i, expandableView2);
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                int i3 = (increasedPaddingAmount > 0.0f ? 1 : (increasedPaddingAmount == 0.0f ? 0 : -1));
                if (i3 != 0) {
                    stackScrollAlgorithmState2.paddingMap.put(expandableView2, Float.valueOf(increasedPaddingAmount));
                    if (expandableView != null) {
                        Float f = stackScrollAlgorithmState2.paddingMap.get(expandableView);
                        float paddingForValue = getPaddingForValue(Float.valueOf(increasedPaddingAmount));
                        if (f != null) {
                            float paddingForValue2 = getPaddingForValue(f);
                            if (i3 > 0) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue2, paddingForValue, increasedPaddingAmount);
                            } else if (f.floatValue() > 0.0f) {
                                paddingForValue = NotificationUtils.interpolate(paddingForValue, paddingForValue2, f.floatValue());
                            }
                        }
                        stackScrollAlgorithmState2.paddingMap.put(expandableView, Float.valueOf(paddingForValue));
                    }
                } else if (expandableView != null) {
                    stackScrollAlgorithmState2.paddingMap.put(expandableView, Float.valueOf(getPaddingForValue(stackScrollAlgorithmState2.paddingMap.get(expandableView))));
                }
                if (expandableView2 instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
                    List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                    if (expandableNotificationRow.isSummaryWithChildren() && notificationChildren != null) {
                        for (ExpandableNotificationRow next : notificationChildren) {
                            if (next.getVisibility() != 8) {
                                stackScrollState2.getViewStateForView(next).notGoneIndex = i;
                                i++;
                            }
                        }
                    }
                }
                expandableView = expandableView2;
            }
        }
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

    private int updateNotGoneIndex(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, int i, ExpandableView expandableView) {
        stackScrollState.getViewStateForView(expandableView).notGoneIndex = i;
        stackScrollAlgorithmState.visibleChildren.add(expandableView);
        return i + 1;
    }

    private void updatePositionsForState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        int springLength = ambientState.getSpringLength();
        boolean z = springLength > 0;
        float f = 0.0f;
        int size = stackScrollAlgorithmState.visibleChildren.size();
        float f2 = (float) (-stackScrollAlgorithmState.scrollY);
        for (int i = 0; i < size; i++) {
            if (z) {
                float min = 1.0f - ((((float) i) * 1.0f) / ((float) Math.min(size, 10)));
                f += 0.15f * min * min * ((float) springLength);
                stackScrollState.getViewStateForView(stackScrollAlgorithmState.visibleChildren.get(i)).springYOffset = ((int) f) + springLength;
            }
            f2 = updateChild(i, stackScrollState, stackScrollAlgorithmState, ambientState, f2);
        }
    }

    /* access modifiers changed from: protected */
    public float updateChild(int i, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState, float f) {
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
        viewStateForView.location = 0;
        int paddingAfterChild = getPaddingAfterChild(stackScrollAlgorithmState, expandableView);
        int maxAllowedChildHeight = getMaxAllowedChildHeight(expandableView);
        viewStateForView.yTranslation = f;
        viewStateForView.location = 4;
        int viewType = expandableView.getViewType();
        if (viewType == 2) {
            viewStateForView.yTranslation = ((float) (ambientState.getInnerHeight() - maxAllowedChildHeight)) + (ambientState.getStackTranslation() * 0.25f);
        } else if (viewType == 0 || viewType == 1) {
            clampPositionToShelf(viewStateForView, ambientState);
        }
        float f2 = viewStateForView.yTranslation + ((float) maxAllowedChildHeight) + ((float) paddingAfterChild);
        if (f2 <= 0.0f) {
            viewStateForView.location = 2;
        }
        if (viewStateForView.location == 0) {
            Log.wtf("StackScrollAlgorithm", "Failed to assign location for child " + i);
        }
        viewStateForView.yTranslation += ambientState.getTopPadding() + ambientState.getStackTranslation();
        viewStateForView.yTranslation += (float) viewStateForView.springYOffset;
        return f2;
    }

    /* access modifiers changed from: protected */
    public int getPaddingAfterChild(StackScrollAlgorithmState stackScrollAlgorithmState, ExpandableView expandableView) {
        return stackScrollAlgorithmState.getPaddingAfterChild(expandableView);
    }

    private void updateHeadsUpStates(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState) {
        int size = stackScrollAlgorithmState.visibleChildren.size();
        ExpandableNotificationRow expandableNotificationRow = null;
        for (int i = 0; i < size; i++) {
            View view = stackScrollAlgorithmState.visibleChildren.get(i);
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) view;
                if (expandableNotificationRow2.isHeadsUp() || expandableNotificationRow2.isHeadsUpAnimatingAway()) {
                    ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableNotificationRow2);
                    if (expandableNotificationRow == null) {
                        viewStateForView.location = 1;
                        expandableNotificationRow = expandableNotificationRow2;
                    }
                    if (expandableNotificationRow2.isHeadsUpAnimatingAway()) {
                        applyCommonHeadsUpChildState(viewStateForView, expandableNotificationRow2);
                        if (expandableNotificationRow2 == expandableNotificationRow) {
                            viewStateForView.yTranslation = (float) (-viewStateForView.height);
                            viewStateForView.alpha = 1.0f;
                        } else {
                            viewStateForView.alpha = 0.0f;
                        }
                    } else if (expandableNotificationRow2.isPinned()) {
                        applyCommonHeadsUpChildState(viewStateForView, expandableNotificationRow2);
                        viewStateForView.yTranslation = (float) this.mHeadsUpMarginTop;
                        viewStateForView.alpha = 1.0f;
                    }
                    viewStateForView.scaleY = 1.0f;
                    viewStateForView.scaleX = 1.0f;
                    viewStateForView.hidden = expandableNotificationRow2.isHiddenForAnimation() | viewStateForView.hidden;
                }
            }
        }
    }

    private void applyCommonHeadsUpChildState(ExpandableViewState expandableViewState, ExpandableNotificationRow expandableNotificationRow) {
        expandableViewState.height = Math.max(expandableNotificationRow.getIntrinsicHeight(), expandableViewState.height);
        expandableViewState.hidden = false;
        expandableViewState.shadowAlpha = 0.8f;
    }

    private void clampPositionToShelf(ExpandableViewState expandableViewState, AmbientState ambientState) {
        float innerHeight = (float) ((int) (((float) ambientState.getInnerHeight()) + ambientState.getTopPadding()));
        expandableViewState.yTranslation = Math.min(expandableViewState.yTranslation, innerHeight);
        if (expandableViewState.yTranslation >= innerHeight) {
            expandableViewState.hidden = true;
            expandableViewState.inShelf = true;
        }
        if (!ambientState.isShadeExpanded()) {
            expandableViewState.height = 0;
        }
    }

    /* access modifiers changed from: protected */
    public int getMaxAllowedChildHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view == null ? this.mCollapsedSize : view.getHeight();
    }

    private void updateZValuesForState(StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        float f = 0.0f;
        for (int size = stackScrollAlgorithmState.visibleChildren.size() - 1; size >= 0; size--) {
            f = updateChildZValue(size, f, stackScrollState, stackScrollAlgorithmState, ambientState);
        }
    }

    /* access modifiers changed from: protected */
    public float updateChildZValue(int i, float f, StackScrollState stackScrollState, StackScrollAlgorithmState stackScrollAlgorithmState, AmbientState ambientState) {
        ExpandableView expandableView = stackScrollAlgorithmState.visibleChildren.get(i);
        ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(expandableView);
        int zDistanceBetweenElements = ambientState.getZDistanceBetweenElements();
        float baseZHeight = (float) ambientState.getBaseZHeight();
        float f2 = 1.0f;
        if (expandableView.mustStayOnScreen() && viewStateForView.yTranslation < ambientState.getTopPadding() + ambientState.getStackTranslation()) {
            if (f != 0.0f) {
                f += 1.0f;
            } else {
                f += Math.min(1.0f, ((ambientState.getTopPadding() + ambientState.getStackTranslation()) - viewStateForView.yTranslation) / ((float) viewStateForView.height));
            }
            viewStateForView.zTranslation = baseZHeight + (((float) zDistanceBetweenElements) * f);
        } else if (i != 0 || !expandableView.isAboveShelf()) {
            viewStateForView.zTranslation = baseZHeight;
        } else {
            int intrinsicHeight = ambientState.getShelf().getIntrinsicHeight();
            float innerHeight = ((float) (ambientState.getInnerHeight() - intrinsicHeight)) + ambientState.getTopPadding() + ambientState.getStackTranslation();
            float pinnedHeadsUpHeight = viewStateForView.yTranslation + ((float) expandableView.getPinnedHeadsUpHeight()) + ((float) this.mPaddingBetweenElements);
            if (innerHeight > pinnedHeadsUpHeight) {
                viewStateForView.zTranslation = baseZHeight;
            } else {
                if (intrinsicHeight != 0) {
                    f2 = Math.min((pinnedHeadsUpHeight - innerHeight) / ((float) intrinsicHeight), 1.0f);
                }
                viewStateForView.zTranslation = baseZHeight + (f2 * ((float) zDistanceBetweenElements));
            }
        }
        return f;
    }

    public void setIsExpanded(boolean z) {
        this.mIsExpanded = z;
    }

    public void setExpandedBecauseOfHeadsUp(boolean z) {
        this.mIsExpandedBecauseOfHeadsUp = z;
    }

    public class StackScrollAlgorithmState {
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
    }
}
