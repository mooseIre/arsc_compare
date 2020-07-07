package com.android.systemui.recents;

import android.os.Parcel;
import android.os.Parcelable;

public class RecentsActivityLaunchState implements Parcelable {
    public static final Parcelable.Creator<RecentsActivityLaunchState> CREATOR = new Parcelable.Creator<RecentsActivityLaunchState>() {
        public RecentsActivityLaunchState createFromParcel(Parcel parcel) {
            return new RecentsActivityLaunchState(parcel);
        }

        public RecentsActivityLaunchState[] newArray(int i) {
            return new RecentsActivityLaunchState[i];
        }
    };
    public boolean launchedFromApp;
    public boolean launchedFromHome;
    public int launchedNumVisibleTasks;
    public int launchedNumVisibleThumbnails;
    public int launchedToTaskId;
    public boolean launchedViaDockGesture;
    public boolean launchedViaDragGesture;
    public boolean launchedViaFsGesture;
    public boolean launchedWithAltTab;

    public int describeContents() {
        return 0;
    }

    public void reset() {
        this.launchedFromHome = false;
        this.launchedFromApp = false;
        this.launchedToTaskId = -1;
        this.launchedWithAltTab = false;
        this.launchedViaDragGesture = false;
        this.launchedViaDockGesture = false;
        this.launchedViaFsGesture = false;
    }

    public int getInitialFocusTaskIndex(int i) {
        RecentsDebugFlags debugFlags = Recents.getDebugFlags();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (this.launchedFromApp) {
            if (launchState.launchedWithAltTab || !debugFlags.isFastToggleRecentsEnabled()) {
                return Math.max(0, i - 2);
            }
            return i - 1;
        } else if (launchState.launchedWithAltTab || !debugFlags.isFastToggleRecentsEnabled()) {
            return i - 1;
        } else {
            return -1;
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte(this.launchedWithAltTab ? (byte) 1 : 0);
        parcel.writeByte(this.launchedFromApp ? (byte) 1 : 0);
        parcel.writeByte(this.launchedFromHome ? (byte) 1 : 0);
        parcel.writeByte(this.launchedViaDragGesture ? (byte) 1 : 0);
        parcel.writeByte(this.launchedViaDockGesture ? (byte) 1 : 0);
        parcel.writeByte(this.launchedViaFsGesture ? (byte) 1 : 0);
        parcel.writeInt(this.launchedToTaskId);
        parcel.writeInt(this.launchedNumVisibleTasks);
        parcel.writeInt(this.launchedNumVisibleThumbnails);
    }

    public RecentsActivityLaunchState() {
    }

    protected RecentsActivityLaunchState(Parcel parcel) {
        boolean z = true;
        this.launchedWithAltTab = parcel.readByte() != 0;
        this.launchedFromApp = parcel.readByte() != 0;
        this.launchedFromHome = parcel.readByte() != 0;
        this.launchedViaDragGesture = parcel.readByte() != 0;
        this.launchedViaDockGesture = parcel.readByte() != 0;
        this.launchedViaFsGesture = parcel.readByte() == 0 ? false : z;
        this.launchedToTaskId = parcel.readInt();
        this.launchedNumVisibleTasks = parcel.readInt();
        this.launchedNumVisibleThumbnails = parcel.readInt();
    }
}
