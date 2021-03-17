package com.android.keyguard.negative;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.keyguard.negative.IKeyguardOverlay;
import com.android.keyguard.negative.IKeyguardOverlayCallback;

public class LockScreenMagazineClient {
    private Context mContext;
    private boolean mDestroyed = false;
    private KeyguardClientCallback mKeyguardClientCallback;
    private KeyguardOverlayCallback mKeyguardOverlayCallback;
    private IKeyguardOverlay mOverlay;
    private boolean mResumed = false;
    private OverlayServiceConnection mServiceConnection;
    private final Intent mServiceIntent;
    private ServiceState mServiceState;
    private int mServiceStatus = -1;

    /* access modifiers changed from: private */
    public enum ServiceState {
        BINDING,
        CONNECTED,
        DISCONNECTED
    }

    /* access modifiers changed from: private */
    public class OverlayServiceConnection implements ServiceConnection {
        private OverlayServiceConnection() {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LockScreenMagazineClient.this.mServiceState = ServiceState.CONNECTED;
            LockScreenMagazineClient.this.mDestroyed = false;
            LockScreenMagazineClient.this.mOverlay = IKeyguardOverlay.Stub.asInterface(iBinder);
            Log.d("LockScreenMagazineClient", "onServiceConnected" + LockScreenMagazineClient.this.mOverlay);
            LockScreenMagazineClient.this.applyWindowToken();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Log.d("LockScreenMagazineClient", "onServiceDisconnected=" + componentName);
            LockScreenMagazineClient.this.mServiceState = ServiceState.DISCONNECTED;
            LockScreenMagazineClient.this.mOverlay = null;
            LockScreenMagazineClient.this.notifyStatusChanged(0);
        }
    }

    public LockScreenMagazineClient(Context context, KeyguardClientCallback keyguardClientCallback) {
        this.mContext = context;
        this.mKeyguardClientCallback = keyguardClientCallback;
        this.mServiceIntent = getServiceIntent(context, "com.mfashiongallery.emag");
        this.mServiceState = ServiceState.DISCONNECTED;
        this.mServiceConnection = new OverlayServiceConnection();
    }

    private Intent getServiceIntent(Context context, String str) {
        return new Intent("com.mfashiongallery.emag.WINDOW_OVERLAY").setPackage(str).setData(Uri.parse("app://" + context.getPackageName() + ":" + Process.myUid()).buildUpon().appendQueryParameter("v", Integer.toString(0)).build());
    }

    public void bind() {
        Log.d("LockScreenMagazineClient", "bind");
        connect();
        this.mResumed = true;
        if (isConnected()) {
            try {
                this.mOverlay.onResume();
            } catch (RemoteException e) {
                Log.e("LockScreenMagazineClient", "bind " + e.getMessage());
            }
        }
    }

    public void unBind() {
        Log.d("LockScreenMagazineClient", "unBind");
        removeClient();
    }

    private void removeClient() {
        Log.d("LockScreenMagazineClient", "removeClient mDestroyed=" + this.mDestroyed);
        if (!this.mDestroyed && isConnected()) {
            this.mDestroyed = true;
            this.mContext.unbindService(this.mServiceConnection);
            KeyguardOverlayCallback keyguardOverlayCallback = this.mKeyguardOverlayCallback;
            if (keyguardOverlayCallback != null) {
                keyguardOverlayCallback.clear();
                this.mKeyguardOverlayCallback = null;
            }
            this.mServiceState = ServiceState.DISCONNECTED;
            this.mOverlay = null;
            notifyStatusChanged(0);
        }
    }

    private void connect() {
        if (this.mServiceState != ServiceState.DISCONNECTED) {
            Log.e("LockScreenMagazineClient", "connect failed mServiceState=" + this.mServiceState);
            return;
        }
        if (!connectSafely(this.mContext, this.mServiceConnection, 160)) {
            this.mServiceState = ServiceState.DISCONNECTED;
        } else {
            this.mServiceState = ServiceState.BINDING;
        }
        Log.d("LockScreenMagazineClient", "connect mServiceState=" + this.mServiceState);
    }

    private boolean connectSafely(Context context, ServiceConnection serviceConnection, int i) {
        try {
            return context.bindServiceAsUser(this.mServiceIntent, serviceConnection, i | 1, UserHandle.CURRENT);
        } catch (SecurityException unused) {
            Log.e("LockScreenMagazineClient", "Unable to connect to overlay service");
            return false;
        }
    }

    private boolean isConnected() {
        return this.mOverlay != null;
    }

    public void startMove() {
        if (isConnected()) {
            try {
                this.mOverlay.startScroll();
            } catch (RemoteException e) {
                Log.e("LockScreenMagazineClient", "startMove " + e.getMessage());
            }
        }
    }

    public void updateMove(float f) {
        if (isConnected()) {
            try {
                this.mOverlay.onScroll(f);
            } catch (RemoteException e) {
                Log.e("LockScreenMagazineClient", "updateMove " + e.getMessage());
            }
        }
    }

    public void endMove() {
        if (isConnected()) {
            try {
                this.mOverlay.endScroll();
            } catch (RemoteException e) {
                Log.e("LockScreenMagazineClient", "endMove " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyStatusChanged(int i) {
        if (this.mServiceStatus != i) {
            this.mServiceStatus = i;
            KeyguardClientCallback keyguardClientCallback = this.mKeyguardClientCallback;
            boolean z = true;
            if ((i & 1) == 0) {
                z = false;
            }
            keyguardClientCallback.onServiceStateChanged(z);
        }
    }

    public void hideOverlay(boolean z) {
        Log.d("LockScreenMagazineClient", "hideOverlay animate=" + z + ";mOverlay=" + this.mOverlay);
        if (isConnected()) {
            try {
                this.mOverlay.closeOverlay(z ? 1 : 0);
            } catch (RemoteException e) {
                Log.e("LockScreenMagazineClient", "hideOverlay " + e.getMessage());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyWindowToken() {
        Log.d("LockScreenMagazineClient", "applyWindowToken");
        if (!isConnected()) {
            Log.e("LockScreenMagazineClient", "not connected");
            return;
        }
        try {
            if (this.mKeyguardOverlayCallback == null) {
                this.mKeyguardOverlayCallback = new KeyguardOverlayCallback();
            }
            this.mKeyguardOverlayCallback.setClient(this);
            this.mOverlay.windowAttached(null, this.mKeyguardOverlayCallback, 3);
            if (this.mResumed) {
                this.mOverlay.onResume();
            } else {
                this.mOverlay.onPause();
            }
        } catch (RemoteException e) {
            Log.e("LockScreenMagazineClient", "applyWindowToken " + e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public static class KeyguardOverlayCallback extends IKeyguardOverlayCallback.Stub implements Handler.Callback {
        private LockScreenMagazineClient mClient;
        private final Handler mUIHandler = new Handler(Looper.getMainLooper(), this);

        public void setClient(LockScreenMagazineClient lockScreenMagazineClient) {
            this.mClient = lockScreenMagazineClient;
        }

        public void clear() {
            this.mClient = null;
        }

        public boolean handleMessage(Message message) {
            LockScreenMagazineClient lockScreenMagazineClient = this.mClient;
            if (lockScreenMagazineClient == null) {
                Log.e("LockScreenMagazineClient", "mClient == null");
                return true;
            }
            int i = message.what;
            if (i == 0) {
                if ((lockScreenMagazineClient.mServiceStatus & 1) != 0) {
                    Log.d("LockScreenMagazineClient", "MSG_OVERLAY_SCROLL_CHANGED" + ((Float) message.obj).floatValue());
                    this.mClient.mKeyguardClientCallback.onOverlayScrollChanged(((Float) message.obj).floatValue());
                }
                return true;
            } else if (i != 1) {
                return false;
            } else {
                Log.d("LockScreenMagazineClient", "MSG_STATUS_CHANGED" + message.arg1);
                this.mClient.notifyStatusChanged(message.arg1);
                return true;
            }
        }

        @Override // com.android.keyguard.negative.IKeyguardOverlayCallback
        public void overlayScrollChanged(float f) throws RemoteException {
            this.mUIHandler.removeMessages(0);
            Message.obtain(this.mUIHandler, 0, Float.valueOf(f)).sendToTarget();
        }

        @Override // com.android.keyguard.negative.IKeyguardOverlayCallback
        public void overlayStatusChanged(int i) {
            Message.obtain(this.mUIHandler, 1, i, 0).sendToTarget();
        }
    }
}
