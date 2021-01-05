package com.android.systemui;

import android.app.Notification;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public abstract class SystemUI implements Dumpable {
    /* access modifiers changed from: protected */
    public final Context mContext;

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
    }

    public abstract void start();

    public SystemUI(Context context) {
        this.mContext = context;
    }

    public static void overrideNotificationAppName(Context context, Notification.Builder builder, boolean z) {
        String str;
        Bundle bundle = new Bundle();
        if (z) {
            str = context.getString(17040744);
        } else {
            str = context.getString(17040743);
        }
        bundle.putString("android.substName", str);
        builder.addExtras(bundle);
    }
}
