package com.android.systemui.globalactions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0012R$dimen;

public class GlobalActionsGridLayout extends GlobalActionsLayout {
    public GlobalActionsGridLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setupListView() {
        ListGridLayout listView = getListView();
        listView.setExpectedCount(this.mAdapter.countListItems());
        listView.setReverseSublists(shouldReverseSublists());
        listView.setReverseItems(shouldReverseListItems());
        listView.setSwapRowsAndColumns(shouldSwapRowsAndColumns());
    }

    @Override // com.android.systemui.globalactions.GlobalActionsLayout, com.android.systemui.MultiListLayout
    public void onUpdateList() {
        setupListView();
        super.onUpdateList();
        updateSeparatedItemSize();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateSeparatedItemSize() {
        ViewGroup separatedView = getSeparatedView();
        if (separatedView.getChildCount() != 0) {
            ViewGroup.LayoutParams layoutParams = separatedView.getChildAt(0).getLayoutParams();
            if (separatedView.getChildCount() == 1) {
                layoutParams.width = -1;
                layoutParams.height = -1;
                return;
            }
            layoutParams.width = -2;
            layoutParams.height = -2;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.globalactions.GlobalActionsLayout, com.android.systemui.MultiListLayout
    public ListGridLayout getListView() {
        return (ListGridLayout) super.getListView();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.MultiListLayout
    public void removeAllListViews() {
        ListGridLayout listView = getListView();
        if (listView != null) {
            listView.removeAllItems();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.globalactions.GlobalActionsLayout
    public void addToListView(View view, boolean z) {
        ListGridLayout listView = getListView();
        if (listView != null) {
            listView.addItem(view);
        }
    }

    @Override // com.android.systemui.MultiListLayout
    public void removeAllItems() {
        ViewGroup separatedView = getSeparatedView();
        ListGridLayout listView = getListView();
        if (separatedView != null) {
            separatedView.removeAllViews();
        }
        if (listView != null) {
            listView.removeAllItems();
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldReverseSublists() {
        return getCurrentRotation() == 2;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldSwapRowsAndColumns() {
        return getCurrentRotation() != 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.globalactions.GlobalActionsLayout
    public boolean shouldReverseListItems() {
        int currentRotation = getCurrentRotation();
        boolean z = currentRotation == 0 || currentRotation == 2;
        return getCurrentLayoutDirection() == 1 ? !z : z;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public float getAnimationDistance() {
        return (((float) getListView().getRowCount()) * getContext().getResources().getDimension(C0012R$dimen.global_actions_grid_item_height)) / 2.0f;
    }

    @Override // com.android.systemui.MultiListLayout
    public float getAnimationOffsetX() {
        int currentRotation = getCurrentRotation();
        if (currentRotation == 1) {
            return getAnimationDistance();
        }
        if (currentRotation != 2) {
            return 0.0f;
        }
        return -getAnimationDistance();
    }
}
