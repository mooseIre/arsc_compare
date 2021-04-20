package com.android.systemui.statusbar.notification;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.miui.systemui.NotificationSettings;
import com.miui.systemui.SettingsManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import miui.os.Build;

public class NotificationCenter extends SystemUI {
    private static int DEFAULT_DELAY = 5000;
    private static int DEFAULT_INTERVAL = 60000;
    private static String TAG = "NcSystem";
    public static boolean sSupportAggregate;
    private int mBindTimes = 0;
    private Handler mHandler;
    private volatile boolean mHasBind;
    private Messenger mNcClient;
    private ServiceConnection mNcConn;
    private volatile Messenger mNcService;

    public NotificationCenter(Context context) {
        super(context);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new WorkHandler(handlerThread.getLooper());
        this.mNcClient = new Messenger(this.mHandler);
        this.mNcConn = new NcServiceConn();
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifFoldListener(new NotificationSettings.FoldListener() {
            /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationCenter$Tx_8jRwmeKBb6aO3AIGLUdGcF4A */

            @Override // com.miui.systemui.NotificationSettings.FoldListener
            public final void onChanged(boolean z) {
                NotificationCenter.this.lambda$start$0$NotificationCenter(z);
            }
        });
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifAggregateListener(new NotificationSettings.AggregateListener() {
            /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationCenter$dnHkWzwUMp3huSfJEFNmzQqQO5M */

            @Override // com.miui.systemui.NotificationSettings.AggregateListener
            public final void onChanged(boolean z) {
                NotificationCenter.this.lambda$start$1$NotificationCenter(z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$start$0 */
    public /* synthetic */ void lambda$start$0$NotificationCenter(boolean z) {
        this.mHandler.removeMessages(10001);
        this.mHandler.sendEmptyMessageDelayed(10001, (long) DEFAULT_DELAY);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$start$1 */
    public /* synthetic */ void lambda$start$1$NotificationCenter(boolean z) {
        this.mHandler.removeMessages(10001);
        this.mHandler.sendEmptyMessageDelayed(10001, (long) DEFAULT_DELAY);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        this.mHandler.sendEmptyMessage(10001);
    }

    private class NcServiceConn implements ServiceConnection {
        private NcServiceConn() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(NotificationCenter.TAG, "NcService connected");
            NotificationCenter.this.mNcService = new Messenger(iBinder);
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.replyTo = NotificationCenter.this.mNcClient;
            try {
                NotificationCenter.this.mNcService.send(obtain);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            try {
                ((SystemUI) NotificationCenter.this).mContext.unbindService(NotificationCenter.this.mNcConn);
            } catch (Exception unused) {
            }
            Log.e(NotificationCenter.TAG, "NcService disconnected, unbind");
            NotificationCenter.this.mNcService = null;
            NotificationCenter.this.mHasBind = false;
            NotificationCenter.this.mHandler.removeMessages(10001);
            NotificationCenter.this.mHandler.sendEmptyMessageDelayed(10001, (long) NotificationCenter.DEFAULT_DELAY);
        }

        public void onBindingDied(ComponentName componentName) {
            try {
                ((SystemUI) NotificationCenter.this).mContext.unbindService(NotificationCenter.this.mNcConn);
            } catch (Exception unused) {
            }
            Log.e(NotificationCenter.TAG, "NcService died, unbind");
            NotificationCenter.this.mNcService = null;
            NotificationCenter.this.mHasBind = false;
            NotificationCenter.this.mHandler.removeMessages(10001);
            NotificationCenter.this.mHandler.sendEmptyMessageDelayed(10001, (long) NotificationCenter.DEFAULT_DELAY);
        }
    }

    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 10001) {
                NotificationCenter.this.initSupportAggregate();
            } else if (i == 10002) {
                NotificationCenter notificationCenter = NotificationCenter.this;
                if (!notificationCenter.isLite(((SystemUI) notificationCenter).mContext)) {
                    NotificationCenter.this.bindNcService();
                } else {
                    Log.w(NotificationCenter.TAG, "NcService lite");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initSupportAggregate() {
        Intent intent = new Intent();
        intent.setClassName("com.miui.notification", "miui.notification.aggregation.NotificationListActivity");
        boolean z = this.mContext.getPackageManager().resolveActivity(intent, 0) != null;
        sSupportAggregate = z;
        this.mBindTimes = 0;
        if (!z) {
            return;
        }
        if (!this.mHasBind || this.mNcService == null) {
            this.mHandler.removeMessages(10002);
            this.mHandler.sendEmptyMessage(10002);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bindNcService() {
        Intent intent = new Intent("com.miui.notification.NOTIFICATION_CENTER");
        intent.setPackage("com.miui.notification");
        this.mHasBind = this.mContext.bindServiceAsUser(intent, this.mNcConn, 1, UserHandle.CURRENT);
        String str = TAG;
        Log.i(str, "NcService bind: " + this.mHasBind);
        this.mBindTimes = this.mBindTimes + 1;
        if (!this.mHasBind && this.mBindTimes <= 3) {
            this.mHandler.removeMessages(10002);
            this.mHandler.sendEmptyMessageDelayed(10002, (long) DEFAULT_INTERVAL);
        }
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
        printWriter.println("sSupportAggregate:" + sSupportAggregate);
        printWriter.println("mBindTime:" + this.mBindTimes);
        printWriter.println("mHasBind:" + this.mHasBind);
        printWriter.println("mNcService:" + this.mNcService);
        printWriter.println("mIsLite:" + isLite(this.mContext));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isLite(Context context) {
        return Build.IS_MIUI_LITE_VERSION && !(NotificationSettings.Companion.isNotifAggregateEnabled(context) || NotificationSettings.Companion.isNotifFoldEnabled(context));
    }
}
