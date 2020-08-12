package com.android.systemui.screenshot;

import android.app.Notification;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.android.systemui.partialscreenshot.PartialScreenshot;
import com.android.systemui.plugins.R;
import com.android.systemui.util.NotificationChannels;
import miui.util.Log;

public class TakeScreenshotService extends Service {
    /* access modifiers changed from: private */
    public static int sRunningCount;
    /* access modifiers changed from: private */
    public Handler mGalleryHandler;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            PartialScreenshot partialScreenshot;
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                StatHelper.recordCountEvent(TakeScreenshotService.this.getApplicationContext(), "all");
                GlobalScreenshot.beforeTakeScreenshot(TakeScreenshotService.this);
                Message obtain = Message.obtain(message);
                obtain.what = 2;
                sendMessageDelayed(obtain, 150);
            } else if (i == 2) {
                TakeScreenshotService.access$008();
                TakeScreenshotService takeScreenshotService = TakeScreenshotService.this;
                GlobalScreenshot globalScreenshot = new GlobalScreenshot(takeScreenshotService, takeScreenshotService.mGalleryHandler);
                final Messenger messenger = message.replyTo;
                AnonymousClass1 r3 = new Runnable() {
                    public void run() {
                        try {
                            messenger.send(Message.obtain((Handler) null, 1));
                        } catch (RemoteException unused) {
                        }
                    }
                };
                AnonymousClass2 r1 = new Runnable() {
                    public void run() {
                        TakeScreenshotService.access$010();
                        if (TakeScreenshotService.sRunningCount <= 0) {
                            TakeScreenshotService.this.stopSelf();
                        }
                    }
                };
                boolean z2 = (message.arg1 & 1) > 0;
                if (message.arg2 <= 0) {
                    z = false;
                }
                globalScreenshot.takeScreenshot(r3, r1, z2, z);
            } else if (i == 3) {
                final Messenger messenger2 = message.replyTo;
                AnonymousClass3 r12 = new Runnable() {
                    public void run() {
                        try {
                            messenger2.send(Message.obtain((Handler) null, 1));
                        } catch (RemoteException e) {
                            Log.d("TakeScreenshotService", e.getMessage());
                        }
                    }
                };
                TakeScreenshotService.access$008();
                AnonymousClass4 r0 = new Runnable() {
                    public void run() {
                        TakeScreenshotService.access$010();
                        if (TakeScreenshotService.sRunningCount <= 0) {
                            TakeScreenshotService.this.stopSelf();
                        }
                    }
                };
                Bundle data = message.getData();
                if (data == null || data.getFloatArray("partial.screenshot.points") == null) {
                    partialScreenshot = new PartialScreenshot(TakeScreenshotService.this);
                } else {
                    partialScreenshot = new PartialScreenshot(TakeScreenshotService.this, data.getFloatArray("partial.screenshot.points"));
                }
                r12.run();
                partialScreenshot.takePartialScreenshot(r0);
            }
        }
    };
    private HandlerThread mHandlerThread = new HandlerThread("screen_gallery_thread", 10);

    static /* synthetic */ int access$008() {
        int i = sRunningCount;
        sRunningCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$010() {
        int i = sRunningCount;
        sRunningCount = i - 1;
        return i;
    }

    public IBinder onBind(Intent intent) {
        startService(new Intent(this, TakeScreenshotService.class));
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        Notification.Builder smallIcon = new Notification.Builder(this).setSmallIcon(R.drawable.fold_tips);
        NotificationCompat.setChannelId(smallIcon, NotificationChannels.SCREENSHOTS);
        startForeground(1, smallIcon.build());
        return new Messenger(this.mHandler).getBinder();
    }

    public void onCreate() {
        super.onCreate();
        this.mHandlerThread.start();
        this.mGalleryHandler = new Handler(this.mHandlerThread.getLooper());
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandlerThread.quitSafely();
        Log.d("TakeScreenshotService", "Screenshot Service onDestroy");
    }
}
