package com.android.systemui.screenshot;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.UserHandle;
import miui.util.Log;

public class TakeScreenshotService extends Service {
    /* access modifiers changed from: private */
    public volatile Messenger mFallbackMessenger;
    /* access modifiers changed from: private */
    public volatile ServiceConnection mFallbackServiceConnection;
    /* access modifiers changed from: private */
    public final Object mFallbackServiceLock = new Object();
    private Handler mProxyHandler;
    private final HandlerThread mProxyThread = new HandlerThread("screen_proxy_thread", -2);
    /* access modifiers changed from: private */
    public volatile Messenger mRealMessenger;
    /* access modifiers changed from: private */
    public volatile ServiceConnection mRealServiceConnection;
    /* access modifiers changed from: private */
    public final Object mServiceLock = new Object();

    public IBinder onBind(Intent intent) {
        return new Messenger(this.mProxyHandler).getBinder();
    }

    public void onCreate() {
        super.onCreate();
        this.mProxyThread.start();
        this.mProxyHandler = new Handler(this.mProxyThread.getLooper()) {
            public void handleMessage(Message message) {
                if (TakeScreenshotService.this.mRealServiceConnection == null) {
                    synchronized (TakeScreenshotService.this.mServiceLock) {
                        TakeScreenshotService.this.bindRealService();
                        try {
                            TakeScreenshotService.this.mServiceLock.wait(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                boolean z = false;
                if (TakeScreenshotService.this.mRealMessenger != null) {
                    try {
                        TakeScreenshotService.this.mRealMessenger.send(Message.obtain(message));
                        z = true;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (!z) {
                    if (TakeScreenshotService.this.mFallbackServiceConnection == null) {
                        synchronized (TakeScreenshotService.this.mFallbackServiceLock) {
                            TakeScreenshotService.this.bindFallbackService();
                            try {
                                TakeScreenshotService.this.mFallbackServiceLock.wait(500);
                            } catch (InterruptedException e3) {
                                e3.printStackTrace();
                            }
                        }
                    }
                    if (TakeScreenshotService.this.mFallbackMessenger != null) {
                        try {
                            TakeScreenshotService.this.mFallbackMessenger.send(Message.obtain(message));
                        } catch (Exception e4) {
                            e4.printStackTrace();
                        }
                    }
                }
            }
        };
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRealServiceConnection != null) {
            unbindService(this.mRealServiceConnection);
            this.mRealServiceConnection = null;
            this.mRealMessenger = null;
        }
        if (this.mFallbackServiceConnection != null) {
            unbindService(this.mFallbackServiceConnection);
            this.mFallbackServiceConnection = null;
            this.mFallbackMessenger = null;
        }
        this.mProxyThread.quitSafely();
        Log.d("TakeScreenshotService", "Screenshot Service onDestroy");
    }

    /* access modifiers changed from: private */
    public void bindRealService() {
        if (this.mRealServiceConnection == null) {
            Log.i("TakeScreenshotService", "bindRealService: ");
            ComponentName componentName = new ComponentName("com.miui.screenshot", "com.miui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            AnonymousClass2 r0 = new ServiceConnection() {
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    synchronized (TakeScreenshotService.this.mServiceLock) {
                        Messenger unused = TakeScreenshotService.this.mRealMessenger = new Messenger(iBinder);
                        TakeScreenshotService.this.mServiceLock.notifyAll();
                    }
                }

                public void onServiceDisconnected(ComponentName componentName) {
                    synchronized (TakeScreenshotService.this.mServiceLock) {
                        Messenger unused = TakeScreenshotService.this.mRealMessenger = null;
                        TakeScreenshotService.this.mServiceLock.notifyAll();
                        if (TakeScreenshotService.this.mRealServiceConnection != null) {
                            TakeScreenshotService.this.unbindService(TakeScreenshotService.this.mRealServiceConnection);
                            ServiceConnection unused2 = TakeScreenshotService.this.mRealServiceConnection = null;
                        }
                    }
                }

                public void onBindingDied(ComponentName componentName) {
                    synchronized (TakeScreenshotService.this.mServiceLock) {
                        Messenger unused = TakeScreenshotService.this.mRealMessenger = null;
                        TakeScreenshotService.this.mServiceLock.notifyAll();
                        if (TakeScreenshotService.this.mRealServiceConnection != null) {
                            TakeScreenshotService.this.unbindService(TakeScreenshotService.this.mRealServiceConnection);
                            ServiceConnection unused2 = TakeScreenshotService.this.mRealServiceConnection = null;
                        }
                    }
                }
            };
            if (bindServiceAsUser(intent, r0, 33554433, UserHandle.CURRENT)) {
                this.mRealServiceConnection = r0;
            }
        }
    }

    /* access modifiers changed from: private */
    public void bindFallbackService() {
        if (this.mFallbackServiceConnection == null) {
            Log.i("TakeScreenshotService", "bindFallbackService: ");
            ComponentName componentName = new ComponentName("com.android.systemui", "com.android.systemui.screenshot.FallbackTakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            AnonymousClass3 r0 = new ServiceConnection() {
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    synchronized (TakeScreenshotService.this.mFallbackServiceLock) {
                        Messenger unused = TakeScreenshotService.this.mFallbackMessenger = new Messenger(iBinder);
                        TakeScreenshotService.this.mFallbackServiceLock.notifyAll();
                    }
                }

                public void onServiceDisconnected(ComponentName componentName) {
                    synchronized (TakeScreenshotService.this.mFallbackServiceLock) {
                        Messenger unused = TakeScreenshotService.this.mFallbackMessenger = null;
                        TakeScreenshotService.this.mFallbackServiceLock.notifyAll();
                        if (TakeScreenshotService.this.mFallbackServiceConnection != null) {
                            TakeScreenshotService.this.unbindService(TakeScreenshotService.this.mFallbackServiceConnection);
                            ServiceConnection unused2 = TakeScreenshotService.this.mFallbackServiceConnection = null;
                        }
                    }
                }

                public void onBindingDied(ComponentName componentName) {
                    synchronized (TakeScreenshotService.this.mFallbackServiceLock) {
                        Messenger unused = TakeScreenshotService.this.mFallbackMessenger = null;
                        TakeScreenshotService.this.mFallbackServiceLock.notifyAll();
                        if (TakeScreenshotService.this.mFallbackServiceConnection != null) {
                            TakeScreenshotService.this.unbindService(TakeScreenshotService.this.mFallbackServiceConnection);
                            ServiceConnection unused2 = TakeScreenshotService.this.mFallbackServiceConnection = null;
                        }
                    }
                }
            };
            if (bindServiceAsUser(intent, r0, 33554433, UserHandle.CURRENT)) {
                this.mFallbackServiceConnection = r0;
            }
        }
    }
}
