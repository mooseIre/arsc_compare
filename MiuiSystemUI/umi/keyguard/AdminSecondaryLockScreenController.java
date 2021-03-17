package com.android.keyguard;

import android.app.admin.IKeyguardCallback;
import android.app.admin.IKeyguardClient;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.SurfaceControlViewHost;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.AdminSecondaryLockScreenController;
import java.util.NoSuchElementException;

public class AdminSecondaryLockScreenController {
    private final IKeyguardCallback mCallback = new IKeyguardCallback.Stub() {
        /* class com.android.keyguard.AdminSecondaryLockScreenController.AnonymousClass2 */

        public void onDismiss() {
            AdminSecondaryLockScreenController.this.mHandler.post(new Runnable() {
                /* class com.android.keyguard.$$Lambda$AdminSecondaryLockScreenController$2$mj_fj3Skgetp50CQsMekknlxNLQ */

                public final void run() {
                    AdminSecondaryLockScreenController.AnonymousClass2.this.lambda$onDismiss$0$AdminSecondaryLockScreenController$2();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDismiss$0 */
        public /* synthetic */ void lambda$onDismiss$0$AdminSecondaryLockScreenController$2() {
            AdminSecondaryLockScreenController.this.dismiss(UserHandle.getCallingUserId());
        }

        public void onRemoteContentReady(SurfaceControlViewHost.SurfacePackage surfacePackage) {
            if (AdminSecondaryLockScreenController.this.mHandler != null) {
                AdminSecondaryLockScreenController.this.mHandler.removeCallbacksAndMessages(null);
            }
            if (surfacePackage != null) {
                AdminSecondaryLockScreenController.this.mView.setChildSurfacePackage(surfacePackage);
            } else {
                AdminSecondaryLockScreenController.this.mHandler.post(new Runnable() {
                    /* class com.android.keyguard.$$Lambda$AdminSecondaryLockScreenController$2$UrCunHVgAzRTASUYiAKWtT8yJkE */

                    public final void run() {
                        AdminSecondaryLockScreenController.AnonymousClass2.this.lambda$onRemoteContentReady$1$AdminSecondaryLockScreenController$2();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onRemoteContentReady$1 */
        public /* synthetic */ void lambda$onRemoteContentReady$1$AdminSecondaryLockScreenController$2() {
            AdminSecondaryLockScreenController.this.dismiss(KeyguardUpdateMonitor.getCurrentUser());
        }
    };
    private IKeyguardClient mClient;
    private final ServiceConnection mConnection = new ServiceConnection() {
        /* class com.android.keyguard.AdminSecondaryLockScreenController.AnonymousClass1 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AdminSecondaryLockScreenController.this.mClient = IKeyguardClient.Stub.asInterface(iBinder);
            if (AdminSecondaryLockScreenController.this.mView.isAttachedToWindow() && AdminSecondaryLockScreenController.this.mClient != null) {
                AdminSecondaryLockScreenController.this.onSurfaceReady();
                try {
                    iBinder.linkToDeath(AdminSecondaryLockScreenController.this.mKeyguardClientDeathRecipient, 0);
                } catch (RemoteException e) {
                    Log.e("AdminSecondaryLockScreenController", "Lost connection to secondary lockscreen service", e);
                    AdminSecondaryLockScreenController.this.dismiss(KeyguardUpdateMonitor.getCurrentUser());
                }
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            AdminSecondaryLockScreenController.this.mClient = null;
        }
    };
    private final Context mContext;
    private Handler mHandler;
    private KeyguardSecurityCallback mKeyguardCallback;
    private final IBinder.DeathRecipient mKeyguardClientDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.android.keyguard.$$Lambda$AdminSecondaryLockScreenController$hZxS1P2BiTbPLgxMgeMNzfQP6sE */

        public final void binderDied() {
            AdminSecondaryLockScreenController.this.lambda$new$0$AdminSecondaryLockScreenController();
        }
    };
    private final ViewGroup mParent;
    @VisibleForTesting
    protected SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        /* class com.android.keyguard.AdminSecondaryLockScreenController.AnonymousClass4 */

        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        }

        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            int currentUser = KeyguardUpdateMonitor.getCurrentUser();
            AdminSecondaryLockScreenController.this.mUpdateMonitor.registerCallback(AdminSecondaryLockScreenController.this.mUpdateCallback);
            if (AdminSecondaryLockScreenController.this.mClient != null) {
                AdminSecondaryLockScreenController.this.onSurfaceReady();
            }
            AdminSecondaryLockScreenController.this.mHandler.postDelayed(new Runnable(currentUser) {
                /* class com.android.keyguard.$$Lambda$AdminSecondaryLockScreenController$4$S47yCokzqIXXVhoyS6AoyEb9Xw */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    AdminSecondaryLockScreenController.AnonymousClass4.this.lambda$surfaceCreated$0$AdminSecondaryLockScreenController$4(this.f$1);
                }
            }, 500);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$surfaceCreated$0 */
        public /* synthetic */ void lambda$surfaceCreated$0$AdminSecondaryLockScreenController$4(int i) {
            AdminSecondaryLockScreenController.this.dismiss(i);
            Log.w("AdminSecondaryLockScreenController", "Timed out waiting for secondary lockscreen content.");
        }

        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            AdminSecondaryLockScreenController.this.mUpdateMonitor.removeCallback(AdminSecondaryLockScreenController.this.mUpdateCallback);
        }
    };
    private final KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.AdminSecondaryLockScreenController.AnonymousClass3 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onSecondaryLockscreenRequirementChanged(int i) {
            if (AdminSecondaryLockScreenController.this.mUpdateMonitor.getSecondaryLockscreenRequirement(i) == null) {
                AdminSecondaryLockScreenController.this.dismiss(i);
            }
        }
    };
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private AdminSecurityView mView;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$AdminSecondaryLockScreenController() {
        hide();
        Log.d("AdminSecondaryLockScreenController", "KeyguardClient service died");
    }

    public AdminSecondaryLockScreenController(Context context, ViewGroup viewGroup, KeyguardUpdateMonitor keyguardUpdateMonitor, KeyguardSecurityCallback keyguardSecurityCallback, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mParent = viewGroup;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mKeyguardCallback = keyguardSecurityCallback;
        this.mView = new AdminSecurityView(this, this.mContext, this.mSurfaceHolderCallback);
    }

    public void show(Intent intent) {
        if (this.mClient == null) {
            this.mContext.bindService(intent, this.mConnection, 1);
        }
        if (!this.mView.isAttachedToWindow()) {
            this.mParent.addView(this.mView);
        }
    }

    public void hide() {
        if (this.mView.isAttachedToWindow()) {
            this.mParent.removeView(this.mView);
        }
        IKeyguardClient iKeyguardClient = this.mClient;
        if (iKeyguardClient != null) {
            try {
                iKeyguardClient.asBinder().unlinkToDeath(this.mKeyguardClientDeathRecipient, 0);
            } catch (NoSuchElementException unused) {
                Log.w("AdminSecondaryLockScreenController", "IKeyguardClient death recipient already released");
            }
            this.mContext.unbindService(this.mConnection);
            this.mClient = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSurfaceReady() {
        try {
            IBinder hostToken = this.mView.getHostToken();
            if (hostToken != null) {
                this.mClient.onCreateKeyguardSurface(hostToken, this.mCallback);
            } else {
                hide();
            }
        } catch (RemoteException e) {
            Log.e("AdminSecondaryLockScreenController", "Error in onCreateKeyguardSurface", e);
            dismiss(KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismiss(int i) {
        this.mHandler.removeCallbacksAndMessages(null);
        if (this.mView.isAttachedToWindow() && i == KeyguardUpdateMonitor.getCurrentUser()) {
            hide();
            KeyguardSecurityCallback keyguardSecurityCallback = this.mKeyguardCallback;
            if (keyguardSecurityCallback != null) {
                keyguardSecurityCallback.dismiss(true, i, true);
            }
        }
    }

    /* access modifiers changed from: private */
    public class AdminSecurityView extends SurfaceView {
        private SurfaceHolder.Callback mSurfaceHolderCallback;

        AdminSecurityView(AdminSecondaryLockScreenController adminSecondaryLockScreenController, Context context, SurfaceHolder.Callback callback) {
            super(context);
            this.mSurfaceHolderCallback = callback;
            setZOrderOnTop(true);
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            getHolder().addCallback(this.mSurfaceHolderCallback);
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            getHolder().removeCallback(this.mSurfaceHolderCallback);
        }
    }
}
