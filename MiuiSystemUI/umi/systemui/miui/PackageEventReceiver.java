package com.android.systemui.miui;

public interface PackageEventReceiver {
    void onPackageAdded(int i, String str, boolean z);

    void onPackageChanged(int i, String str);

    void onPackageRemoved(int i, String str, boolean z, boolean z2);
}
