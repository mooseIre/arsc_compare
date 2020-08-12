package com.android.systemui.miui;

import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ViewStateGroup {
    /* access modifiers changed from: private */
    public SparseArray<ViewState> mStates;

    private ViewStateGroup() {
        this.mStates = new SparseArray<>();
    }

    public void apply(ViewGroup viewGroup) {
        for (int i = 0; i < this.mStates.size(); i++) {
            SparseArray<ViewState> sparseArray = this.mStates;
            ViewState viewState = sparseArray.get(sparseArray.keyAt(i), (Object) null);
            if (viewState != null) {
                viewState.apply(viewGroup.findViewById(viewState.mViewId));
            }
        }
    }

    public static class Builder {
        private Context mContext;
        ViewStateGroup mResult = new ViewStateGroup();

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder addState(int i, int i2, int i3) {
            ViewState viewState = (ViewState) this.mResult.mStates.get(i);
            if (viewState == null) {
                viewState = new ViewState(i);
                this.mResult.mStates.put(i, viewState);
            }
            viewState.mIntStates.put(i2, i3);
            return this;
        }

        public Builder addStateWithIntDimen(int i, int i2, int i3) {
            addState(i, i2, this.mContext.getResources().getDimensionPixelSize(i3));
            return this;
        }

        public Builder addStateWithIntRes(int i, int i2, int i3) {
            addState(i, i2, this.mContext.getResources().getInteger(i3));
            return this;
        }

        public ViewStateGroup build() {
            return this.mResult;
        }
    }

    public static class ViewState {
        private SparseArray<Float> mFloatStates = new SparseArray<>();
        /* access modifiers changed from: private */
        public SparseIntArray mIntStates = new SparseIntArray();
        /* access modifiers changed from: private */
        public int mViewId;

        ViewState(int i) {
            this.mViewId = i;
        }

        /* access modifiers changed from: package-private */
        public void apply(View view) {
            if (view != null && this.mViewId == view.getId()) {
                for (int i = 0; i < this.mIntStates.size(); i++) {
                    int keyAt = this.mIntStates.keyAt(i);
                    applyIntProperty(view, keyAt, this.mIntStates.get(keyAt));
                }
                for (int i2 = 0; i2 < this.mFloatStates.size(); i2++) {
                    int keyAt2 = this.mFloatStates.keyAt(i2);
                    applyFloatProperty(view, keyAt2, this.mFloatStates.get(keyAt2).floatValue());
                }
            }
        }

        private static void applyIntProperty(View view, int i, int i2) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            switch (i) {
                case 1:
                    setLayoutGravity(layoutParams, i2);
                    return;
                case 2:
                    layoutParams.width = i2;
                    return;
                case 3:
                    layoutParams.height = i2;
                    return;
                case 5:
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).leftMargin = i2;
                        return;
                    }
                    return;
                case 6:
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).topMargin = i2;
                        return;
                    }
                    return;
                case 7:
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).rightMargin = i2;
                        return;
                    }
                    return;
                case 8:
                    if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin = i2;
                        return;
                    }
                    return;
                case 9:
                    view.setPadding(i2, i2, i2, i2);
                    return;
                case 10:
                    view.setVisibility(i2);
                    return;
                case 11:
                    if (view instanceof LinearLayout) {
                        ((LinearLayout) view).setOrientation(i2);
                        return;
                    }
                    return;
                case 12:
                    if (view instanceof LinearLayout) {
                        ((LinearLayout) view).setGravity(i2);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }

        private static void applyFloatProperty(View view, int i, float f) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (i == 4 && (layoutParams instanceof LinearLayout.LayoutParams)) {
                ((LinearLayout.LayoutParams) layoutParams).weight = f;
            }
        }

        private static void setLayoutGravity(ViewGroup.LayoutParams layoutParams, int i) {
            if (layoutParams instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) layoutParams).gravity = i;
            } else if (layoutParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) layoutParams).gravity = i;
            }
        }
    }
}
