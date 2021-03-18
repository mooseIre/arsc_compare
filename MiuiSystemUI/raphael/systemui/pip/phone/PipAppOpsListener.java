package com.android.systemui.pip.phone;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Pair;
import com.android.systemui.pip.phone.PipAppOpsListener;

public class PipAppOpsListener {
    private IActivityManager mActivityManager;
    private AppOpsManager.OnOpChangedListener mAppOpsChangedListener = new AppOpsManager.OnOpChangedListener() {
        /* class com.android.systemui.pip.phone.PipAppOpsListener.AnonymousClass1 */

        public void onOpChanged(String str, String str2) {
            try {
                Pair<ComponentName, Integer> topPipActivity = PipUtils.getTopPipActivity(PipAppOpsListener.this.mContext, PipAppOpsListener.this.mActivityManager);
                if (topPipActivity.first != null) {
                    ApplicationInfo applicationInfoAsUser = PipAppOpsListener.this.mContext.getPackageManager().getApplicationInfoAsUser(str2, 0, ((Integer) topPipActivity.second).intValue());
                    if (applicationInfoAsUser.packageName.equals(((ComponentName) topPipActivity.first).getPackageName()) && PipAppOpsListener.this.mAppOpsManager.checkOpNoThrow(67, applicationInfoAsUser.uid, str2) != 0) {
                        PipAppOpsListener.this.mHandler.post(new Runnable() {
                            /* class com.android.systemui.pip.phone.$$Lambda$PipAppOpsListener$1$UK38MrwiG74h0N6r_NQ6zq34Mqo */

                            public final void run() {
                                PipAppOpsListener.AnonymousClass1.this.lambda$onOpChanged$0$PipAppOpsListener$1();
                            }
                        });
                    }
                }
            } catch (PackageManager.NameNotFoundException unused) {
                PipAppOpsListener.this.unregisterAppOpsListener();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onOpChanged$0 */
        public /* synthetic */ void lambda$onOpChanged$0$PipAppOpsListener$1() {
            PipAppOpsListener.this.mCallback.dismissPip();
        }
    };
    private AppOpsManager mAppOpsManager;
    private Callback mCallback;
    private Context mContext;
    private Handler mHandler;

    public interface Callback {
        void dismissPip();
    }

    public PipAppOpsListener(Context context, IActivityManager iActivityManager, Callback callback) {
        this.mContext = context;
        this.mHandler = new Handler(this.mContext.getMainLooper());
        this.mActivityManager = iActivityManager;
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mCallback = callback;
    }

    public void onActivityPinned(String str) {
        registerAppOpsListener(str);
    }

    public void onActivityUnpinned() {
        unregisterAppOpsListener();
    }

    private void registerAppOpsListener(String str) {
        this.mAppOpsManager.startWatchingMode(67, str, this.mAppOpsChangedListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unregisterAppOpsListener() {
        this.mAppOpsManager.stopWatchingMode(this.mAppOpsChangedListener);
    }
}
