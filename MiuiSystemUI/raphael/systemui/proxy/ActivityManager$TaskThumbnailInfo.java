package com.android.systemui.proxy;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

public class ActivityManager$TaskThumbnailInfo implements Parcelable {
    public static final Parcelable.Creator<ActivityManager$TaskThumbnailInfo> CREATOR = new Parcelable.Creator<ActivityManager$TaskThumbnailInfo>() {
        public ActivityManager$TaskThumbnailInfo createFromParcel(Parcel parcel) {
            return new ActivityManager$TaskThumbnailInfo(parcel);
        }

        public ActivityManager$TaskThumbnailInfo[] newArray(int i) {
            return new ActivityManager$TaskThumbnailInfo[i];
        }
    };
    public Rect insets;
    public float scale;
    public int screenOrientation;
    public int taskHeight;
    public int taskWidth;

    public int describeContents() {
        return 0;
    }

    public ActivityManager$TaskThumbnailInfo() {
        this.screenOrientation = 0;
        this.insets = new Rect(0, 0, 0, 0);
        this.scale = 1.0f;
    }

    private ActivityManager$TaskThumbnailInfo(Parcel parcel) {
        this.screenOrientation = 0;
        this.insets = new Rect(0, 0, 0, 0);
        this.scale = 1.0f;
        readFromParcel(parcel);
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.taskWidth);
        parcel.writeInt(this.taskHeight);
        parcel.writeInt(this.screenOrientation);
    }

    public void readFromParcel(Parcel parcel) {
        this.taskWidth = parcel.readInt();
        this.taskHeight = parcel.readInt();
        this.screenOrientation = parcel.readInt();
    }
}
