package com.android.systemui.fsgesture;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import com.android.systemui.fsgesture.IFsGestureService;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.FsGestureShowStateEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;

public class FsGestureService extends Service {
    private IFsGestureService.Stub mBinder = new IFsGestureService.Stub() {
        public void registerCallback(final String str, final IFsGestureCallback iFsGestureCallback) throws RemoteException {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    SystemServicesProxy.getInstance(FsGestureService.this.getApplicationContext()).registerFsGestureCall(str, iFsGestureCallback);
                }
            });
        }

        public void unregisterCallback(final String str, final IFsGestureCallback iFsGestureCallback) throws RemoteException {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    SystemServicesProxy.getInstance(FsGestureService.this.getApplicationContext()).unRegisterFsGestureCall(str, iFsGestureCallback);
                }
            });
        }

        public void notifyHomeStatus(boolean z) throws RemoteException {
            RecentsEventBus.getDefault().post(new FsGestureShowStateEvent(z, "typefrom_home"));
        }
    };

    public void onCreate() {
        super.onCreate();
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }
}
