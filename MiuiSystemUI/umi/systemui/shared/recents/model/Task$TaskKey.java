package com.android.systemui.shared.recents.model;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ViewDebug;
import java.util.Objects;

public class Task$TaskKey implements Parcelable {
    public static final Parcelable.Creator<Task$TaskKey> CREATOR = new Parcelable.Creator<Task$TaskKey>() {
        /* class com.android.systemui.shared.recents.model.Task$TaskKey.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Task$TaskKey createFromParcel(Parcel parcel) {
            return Task$TaskKey.readFromParcel(parcel);
        }

        @Override // android.os.Parcelable.Creator
        public Task$TaskKey[] newArray(int i) {
            return new Task$TaskKey[i];
        }
    };
    @ViewDebug.ExportedProperty(category = "recents")
    public final Intent baseIntent;
    @ViewDebug.ExportedProperty(category = "recents")
    public final int displayId;
    @ViewDebug.ExportedProperty(category = "recents")
    public final int id;
    @ViewDebug.ExportedProperty(category = "recents")
    public long lastActiveTime;
    private int mHashCode;
    public final ComponentName sourceComponent;
    @ViewDebug.ExportedProperty(category = "recents")
    public final int userId;
    @ViewDebug.ExportedProperty(category = "recents")
    public int windowingMode;

    public int describeContents() {
        return 0;
    }

    public Task$TaskKey(int i, int i2, Intent intent, ComponentName componentName, int i3, long j, int i4) {
        this.id = i;
        this.windowingMode = i2;
        this.baseIntent = intent;
        this.sourceComponent = componentName;
        this.userId = i3;
        this.lastActiveTime = j;
        this.displayId = i4;
        updateHashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Task$TaskKey)) {
            return false;
        }
        Task$TaskKey task$TaskKey = (Task$TaskKey) obj;
        if (this.id == task$TaskKey.id && this.windowingMode == task$TaskKey.windowingMode && this.userId == task$TaskKey.userId) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mHashCode;
    }

    public String toString() {
        return "id=" + this.id + " windowingMode=" + this.windowingMode + " user=" + this.userId + " lastActiveTime=" + this.lastActiveTime;
    }

    private void updateHashCode() {
        this.mHashCode = Objects.hash(Integer.valueOf(this.id), Integer.valueOf(this.windowingMode), Integer.valueOf(this.userId));
    }

    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeInt(this.windowingMode);
        parcel.writeTypedObject(this.baseIntent, i);
        parcel.writeInt(this.userId);
        parcel.writeLong(this.lastActiveTime);
        parcel.writeInt(this.displayId);
        parcel.writeTypedObject(this.sourceComponent, i);
    }

    /* access modifiers changed from: private */
    public static Task$TaskKey readFromParcel(Parcel parcel) {
        return new Task$TaskKey(parcel.readInt(), parcel.readInt(), (Intent) parcel.readTypedObject(Intent.CREATOR), (ComponentName) parcel.readTypedObject(ComponentName.CREATOR), parcel.readInt(), parcel.readLong(), parcel.readInt());
    }
}
