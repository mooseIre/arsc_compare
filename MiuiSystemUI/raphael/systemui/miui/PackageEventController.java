package com.android.systemui.miui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.android.systemui.Constants;

public class PackageEventController extends BroadcastReceiver {
    private Context mContext;
    /* access modifiers changed from: private */
    public PackageEventReceiver mPackageChangedReceiver;
    private Handler mScheduler;

    public PackageEventController(Context context, PackageEventReceiver packageEventReceiver, Handler handler) {
        this.mContext = context;
        this.mScheduler = handler;
        this.mPackageChangedReceiver = packageEventReceiver;
        if (this.mScheduler == null) {
            this.mScheduler = new Handler(Looper.getMainLooper());
        }
    }

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
    }

    public void onReceive(Context context, final Intent intent) {
        final String packageName = getPackageName(intent);
        final int intExtra = intent.getIntExtra("android.intent.extra.UID", -1);
        if (!TextUtils.isEmpty(packageName) && intExtra >= 0 && !TextUtils.isEmpty(intent.getAction())) {
            if ("android.intent.action.PACKAGE_CHANGED".equals(intent.getAction())) {
                this.mScheduler.post(new Runnable() {
                    public void run() {
                        PackageEventController.this.mPackageChangedReceiver.onPackageChanged(intExtra, packageName);
                    }
                });
            } else if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
                this.mScheduler.post(new Runnable() {
                    public void run() {
                        PackageEventController.this.mPackageChangedReceiver.onPackageAdded(intExtra, packageName, intent.getBooleanExtra("android.intent.extra.REPLACING", false));
                    }
                });
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(intent.getAction())) {
                this.mScheduler.post(new Runnable() {
                    public void run() {
                        PackageEventController.this.mPackageChangedReceiver.onPackageRemoved(intExtra, packageName, intent.getBooleanExtra("android.intent.extra.DATA_REMOVED", false), intent.getBooleanExtra("android.intent.extra.REPLACING", false));
                    }
                });
            }
            if (Constants.DEBUG) {
                Log.i("PackageEventController", "broadcast received: " + intent.getAction() + " " + packageName);
            }
        }
    }

    private static String getPackageName(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            return data.getSchemeSpecificPart();
        }
        return null;
    }
}
