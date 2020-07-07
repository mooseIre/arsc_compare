package com.android.systemui;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import com.android.systemui.miui.PackageEventReceiver;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public abstract class SystemUI implements SysUiServiceProvider, PackageEventReceiver {
    public Map<Class<?>, Object> mComponents;
    public Context mContext;

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
    }

    public void onPackageAdded(int i, String str, boolean z) {
    }

    public void onPackageChanged(int i, String str) {
    }

    public void onPackageRemoved(int i, String str, boolean z, boolean z2) {
    }

    public abstract void start();

    public SystemUI() {
        Dependency.inject(this);
    }

    public <T> T getComponent(Class<T> cls) {
        Map<Class<?>, Object> map = this.mComponents;
        if (map != null) {
            return map.get(cls);
        }
        return null;
    }

    public static <T> T getComponent(Context context, Class<T> cls) {
        return ((Application) context.getApplicationContext()).getSystemUIApplication().getComponent(cls);
    }

    public <T, C extends T> void putComponent(Class<T> cls, C c) {
        Map<Class<?>, Object> map = this.mComponents;
        if (map != null) {
            map.put(cls, c);
        }
    }

    public static void overrideNotificationAppName(Context context, Notification.Builder builder) {
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", context.getString(17039504));
        builder.addExtras(bundle);
    }
}
