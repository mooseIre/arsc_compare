package com.android.systemui.globalactions;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0015R$id;
import com.android.systemui.HardwareBgDrawable;
import com.android.systemui.MultiListLayout;
import com.android.systemui.util.leak.RotationUtils;
import java.util.Locale;

public abstract class GlobalActionsLayout extends MultiListLayout {
    boolean mBackgroundsSet;

    /* access modifiers changed from: protected */
    public abstract boolean shouldReverseListItems();

    public GlobalActionsLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private void setBackgrounds() {
        HardwareBgDrawable backgroundDrawable;
        ViewGroup listView = getListView();
        HardwareBgDrawable backgroundDrawable2 = getBackgroundDrawable(getResources().getColor(C0011R$color.global_actions_grid_background, null));
        if (backgroundDrawable2 != null) {
            listView.setBackground(backgroundDrawable2);
        }
        if (getSeparatedView() != null && (backgroundDrawable = getBackgroundDrawable(getResources().getColor(C0011R$color.global_actions_separated_background, null))) != null) {
            getSeparatedView().setBackground(backgroundDrawable);
        }
    }

    /* access modifiers changed from: protected */
    public HardwareBgDrawable getBackgroundDrawable(int i) {
        HardwareBgDrawable hardwareBgDrawable = new HardwareBgDrawable(true, true, getContext());
        hardwareBgDrawable.setTint(i);
        return hardwareBgDrawable;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (getListView() != null && !this.mBackgroundsSet) {
            setBackgrounds();
            this.mBackgroundsSet = true;
        }
    }

    /* access modifiers changed from: protected */
    public void addToListView(View view, boolean z) {
        if (z) {
            getListView().addView(view, 0);
        } else {
            getListView().addView(view);
        }
    }

    /* access modifiers changed from: protected */
    public void addToSeparatedView(View view, boolean z) {
        ViewGroup separatedView = getSeparatedView();
        if (separatedView == null) {
            addToListView(view, z);
        } else if (z) {
            separatedView.addView(view, 0);
        } else {
            separatedView.addView(view);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getCurrentLayoutDirection() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getCurrentRotation() {
        return RotationUtils.getRotation(((LinearLayout) this).mContext);
    }

    @Override // com.android.systemui.MultiListLayout
    public void onUpdateList() {
        View view;
        super.onUpdateList();
        ViewGroup separatedView = getSeparatedView();
        ViewGroup listView = getListView();
        for (int i = 0; i < this.mAdapter.getCount(); i++) {
            boolean shouldBeSeparated = this.mAdapter.shouldBeSeparated(i);
            if (shouldBeSeparated) {
                view = this.mAdapter.getView(i, null, separatedView);
            } else {
                view = this.mAdapter.getView(i, null, listView);
            }
            if (shouldBeSeparated) {
                addToSeparatedView(view, false);
            } else {
                addToListView(view, shouldReverseListItems());
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.MultiListLayout
    public ViewGroup getSeparatedView() {
        return (ViewGroup) findViewById(C0015R$id.separated_button);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.MultiListLayout
    public ViewGroup getListView() {
        return (ViewGroup) findViewById(16908298);
    }

    /* access modifiers changed from: protected */
    public View getWrapper() {
        return getChildAt(0);
    }
}
