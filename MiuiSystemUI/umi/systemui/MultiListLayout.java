package com.android.systemui;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import com.android.systemui.util.leak.RotationUtils;

public abstract class MultiListLayout extends LinearLayout {
    protected MultiListAdapter mAdapter;
    protected int mRotation;
    protected RotationListener mRotationListener;

    public interface RotationListener {
        void onRotate(int i, int i2);
    }

    public abstract float getAnimationOffsetX();

    /* access modifiers changed from: protected */
    public abstract ViewGroup getListView();

    /* access modifiers changed from: protected */
    public abstract ViewGroup getSeparatedView();

    public MultiListLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRotation = RotationUtils.getRotation(context);
    }

    public void setListViewAccessibilityDelegate(View.AccessibilityDelegate accessibilityDelegate) {
        getListView().setAccessibilityDelegate(accessibilityDelegate);
    }

    /* access modifiers changed from: protected */
    public void setSeparatedViewVisibility(boolean z) {
        ViewGroup separatedView = getSeparatedView();
        if (separatedView != null) {
            separatedView.setVisibility(z ? 0 : 8);
        }
    }

    public void setAdapter(MultiListAdapter multiListAdapter) {
        this.mAdapter = multiListAdapter;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int rotation = RotationUtils.getRotation(this.mContext);
        int i = this.mRotation;
        if (rotation != i) {
            rotate(i, rotation);
            this.mRotation = rotation;
        }
    }

    /* access modifiers changed from: protected */
    public void rotate(int i, int i2) {
        RotationListener rotationListener = this.mRotationListener;
        if (rotationListener != null) {
            rotationListener.onRotate(i, i2);
        }
    }

    public void updateList() {
        if (this.mAdapter != null) {
            onUpdateList();
            return;
        }
        throw new IllegalStateException("mAdapter must be set before calling updateList");
    }

    /* access modifiers changed from: protected */
    public void removeAllSeparatedViews() {
        ViewGroup separatedView = getSeparatedView();
        if (separatedView != null) {
            separatedView.removeAllViews();
        }
    }

    /* access modifiers changed from: protected */
    public void removeAllListViews() {
        ViewGroup listView = getListView();
        if (listView != null) {
            listView.removeAllViews();
        }
    }

    /* access modifiers changed from: protected */
    public void removeAllItems() {
        removeAllListViews();
        removeAllSeparatedViews();
    }

    /* access modifiers changed from: protected */
    public void onUpdateList() {
        removeAllItems();
        setSeparatedViewVisibility(this.mAdapter.hasSeparatedItems());
    }

    public void setRotationListener(RotationListener rotationListener) {
        this.mRotationListener = rotationListener;
    }

    public static abstract class MultiListAdapter extends BaseAdapter {
        public abstract int countListItems();

        public abstract int countSeparatedItems();

        public abstract boolean shouldBeSeparated(int i);

        public boolean hasSeparatedItems() {
            return countSeparatedItems() > 0;
        }
    }
}
