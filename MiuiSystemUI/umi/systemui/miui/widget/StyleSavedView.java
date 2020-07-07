package com.android.systemui.miui.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class StyleSavedView extends LinearLayout {
    public StyleSavedView(Context context) {
        super(context);
    }

    public StyleSavedView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public StyleSavedView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getBackground(), ((FrameLayout.LayoutParams) getLayoutParams()).gravity);
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setBackground(savedState.getDrawalbe());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.gravity = savedState.getLayoutGravity();
        setLayoutParams(layoutParams);
    }

    private static class SavedState extends View.BaseSavedState {
        private final Drawable mDrawable;
        private final int mLayoutGravity;

        private SavedState(Parcelable parcelable, Drawable drawable, int i) {
            super(parcelable);
            this.mDrawable = drawable;
            this.mLayoutGravity = i;
        }

        public Drawable getDrawalbe() {
            return this.mDrawable;
        }

        public int getLayoutGravity() {
            return this.mLayoutGravity;
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeParcelable(((BitmapDrawable) this.mDrawable).getBitmap(), i);
            parcel.writeInt(this.mLayoutGravity);
        }
    }
}
