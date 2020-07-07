package com.android.systemui.statusbar.notification;

import android.content.ComponentName;
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
import com.android.systemui.SystemUI;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NotificationCenter extends SystemUI {
    /* access modifiers changed from: private */
    public static int DEFAULT_DELAY = 5000;
    private static int DEFAULT_INTERVAL = 60000;
    /* access modifiers changed from: private */
    public static String TAG = "NcSystem";
    public static boolean sSupportAggregate;
    private int mBindTimes = 0;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public volatile boolean mHasBind;
    /* access modifiers changed from: private */
    public Messenger mNcClient;
    private ServiceConnection mNcConn;
    /* access modifiers changed from: private */
    public volatile Messenger mNcService;

    public void start() {
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new WorkHandler(handlerThread.getLooper());
        this.mNcClient = new Messenger(this.mHandler);
        this.mNcConn = new NcServiceConn();
    }

    public void onPackageAdded(int i, String str, boolean z) {
        onPkgAction(str);
    }

    public void onPackageChanged(int i, String str) {
        onPkgAction(str);
    }

    public void onPackageRemoved(int i, String str, boolean z, boolean z2) {
        onPkgAction(str);
    }

    private void onPkgAction(String str) {
        if ("com.miui.notification".equals(str)) {
            this.mHandler.removeMessages(10001);
            this.mHandler.sendEmptyMessageDelayed(10001, (long) DEFAULT_DELAY);
        }
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
        this.mHandler.sendEmptyMessage(10001);
    }

    private class NcServiceConn implements ServiceConnection {
        private NcServiceConn() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(NotificationCenter.TAG, "NcService connected");
            Messenger unused = NotificationCenter.this.mNcService = new Messenger(iBinder);
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
            Log.e(NotificationCenter.TAG, "NcService disconnected");
            Messenger unused = NotificationCenter.this.mNcService = null;
            boolean unused2 = NotificationCenter.this.mHasBind = false;
            NotificationCenter.this.mHandler.removeMessages(10001);
            NotificationCenter.this.mHandler.sendEmptyMessageDelayed(10001, (long) NotificationCenter.DEFAULT_DELAY);
        }

        public void onBindingDied(ComponentName componentName) {
            Log.e(NotificationCenter.TAG, "NcService died");
            Messenger unused = NotificationCenter.this.mNcService = null;
            boolean unused2 = NotificationCenter.this.mHasBind = false;
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
                NotificationCenter.this.bindNcService();
            }
        }
    }

    /* access modifiers changed from: private */
    public void initSupportAggregate() {
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
    public void bindNcService() {
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

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
        printWriter.println("sSupportAggregate:" + sSupportAggregate);
        printWriter.println("mBindTime:" + this.mBindTimes);
        printWriter.println("mHasBind:" + this.mHasBind);
        printWriter.println("mNcService:" + this.mNcService);
    }
}
