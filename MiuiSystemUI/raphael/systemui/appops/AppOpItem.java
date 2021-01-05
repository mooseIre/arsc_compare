package com.android.systemui.appops;

public class AppOpItem {
    private int mCode;
    private String mPackageName;
    private String mState;
    private int mUid;

    public AppOpItem(int i, int i2, String str, long j) {
        this.mCode = i;
        this.mUid = i2;
        this.mPackageName = str;
        this.mState = "AppOpItem(" + "Op code=" + i + ", " + "UID=" + i2 + ", " + "Package name=" + str + ")";
    }

    public int getCode() {
        return this.mCode;
    }

    public int getUid() {
        return this.mUid;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String toString() {
        return this.mState;
    }
}
