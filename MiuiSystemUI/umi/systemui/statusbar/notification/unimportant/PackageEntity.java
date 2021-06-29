package com.android.systemui.statusbar.notification.unimportant;

public class PackageEntity {
    private int mDailyClick;
    private int mDailyShow;
    private boolean mDataChanged;
    private int mHistoryClick;
    private int mHistoryShow;
    private Object mLock = new Object();
    private String mPackageName;

    public PackageEntity(String str) {
        this.mPackageName = str;
    }

    public int getDailyClick() {
        return this.mDailyClick;
    }

    public int getDailyShow() {
        return this.mDailyShow;
    }

    public int getTotalClick() {
        return this.mDailyClick + this.mHistoryClick;
    }

    public int getTotalShow() {
        return this.mDailyShow + this.mHistoryShow;
    }

    public void addClickCount() {
        synchronized (this.mLock) {
            this.mDailyClick++;
            this.mDataChanged = true;
        }
    }

    public void addShowCount() {
        synchronized (this.mLock) {
            this.mDailyShow++;
            this.mDataChanged = true;
        }
    }

    public void setDailyData(int i, int i2) {
        synchronized (this.mLock) {
            this.mDailyClick += i;
            this.mDailyShow += i2;
        }
    }

    public void setHistoryData(int i, int i2) {
        synchronized (this.mLock) {
            this.mHistoryClick = i;
            this.mHistoryShow = i2;
        }
    }

    public void onDateChanged(int i, int i2) {
        synchronized (this.mLock) {
            this.mDailyClick = 0;
            this.mDailyShow = 0;
            this.mHistoryClick = i;
            this.mHistoryShow = i2;
            this.mDataChanged = false;
        }
    }

    public void setDataChanged(boolean z) {
        synchronized (this.mLock) {
            this.mDataChanged = z;
        }
    }

    public boolean isDataChanged() {
        return this.mDataChanged;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String toString() {
        return "PackageEntity{mPackageName='" + this.mPackageName + '\'' + ", mDailyClick=" + this.mDailyClick + ", mDailyShow=" + this.mDailyShow + ", mHistoryClick=" + this.mHistoryClick + ", mHistoryShow=" + this.mHistoryShow + ", mDataChanged=" + this.mDataChanged + '}';
    }
}
