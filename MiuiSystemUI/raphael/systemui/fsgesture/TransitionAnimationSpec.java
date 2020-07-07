package com.android.systemui.fsgesture;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class TransitionAnimationSpec implements Parcelable {
    public static final Parcelable.Creator<TransitionAnimationSpec> CREATOR = new Parcelable.Creator<TransitionAnimationSpec>() {
        public TransitionAnimationSpec createFromParcel(Parcel parcel) {
            return new TransitionAnimationSpec(parcel);
        }

        public TransitionAnimationSpec[] newArray(int i) {
            return new TransitionAnimationSpec[i];
        }
    };
    public final Bitmap mBitmap;
    public final Rect mRect;

    public int describeContents() {
        return 0;
    }

    public TransitionAnimationSpec(Parcel parcel) {
        this.mBitmap = (Bitmap) parcel.readParcelable((ClassLoader) null);
        this.mRect = (Rect) parcel.readParcelable((ClassLoader) null);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mBitmap, i);
        parcel.writeParcelable(this.mRect, i);
    }
}
