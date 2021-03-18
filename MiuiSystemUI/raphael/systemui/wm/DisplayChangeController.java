package com.android.systemui.wm;

import android.os.Handler;
import android.os.RemoteException;
import android.view.IDisplayWindowRotationCallback;
import android.view.IDisplayWindowRotationController;
import android.view.IWindowManager;
import android.window.WindowContainerTransaction;
import com.android.systemui.wm.DisplayChangeController;
import java.util.ArrayList;
import java.util.Iterator;

public class DisplayChangeController {
    private final IDisplayWindowRotationController mDisplayRotationController;
    private final Handler mHandler;
    private final ArrayList<OnDisplayChangingListener> mRotationListener = new ArrayList<>();
    private final ArrayList<OnDisplayChangingListener> mTmpListeners = new ArrayList<>();
    private final IWindowManager mWmService;

    public interface OnDisplayChangingListener {
        void onRotateDisplay(int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction);
    }

    public DisplayChangeController(Handler handler, IWindowManager iWindowManager) {
        AnonymousClass1 r0 = new IDisplayWindowRotationController.Stub() {
            /* class com.android.systemui.wm.DisplayChangeController.AnonymousClass1 */

            public void onRotateDisplay(int i, int i2, int i3, IDisplayWindowRotationCallback iDisplayWindowRotationCallback) {
                DisplayChangeController.this.mHandler.post(new Runnable(i, i2, i3, iDisplayWindowRotationCallback) {
                    /* class com.android.systemui.wm.$$Lambda$DisplayChangeController$1$cr2NyoFjnt2r0DMHwy9cOe5oGO4 */
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ int f$3;
                    public final /* synthetic */ IDisplayWindowRotationCallback f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    public final void run() {
                        DisplayChangeController.AnonymousClass1.this.lambda$onRotateDisplay$0$DisplayChangeController$1(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onRotateDisplay$0 */
            public /* synthetic */ void lambda$onRotateDisplay$0$DisplayChangeController$1(int i, int i2, int i3, IDisplayWindowRotationCallback iDisplayWindowRotationCallback) {
                WindowContainerTransaction windowContainerTransaction = new WindowContainerTransaction();
                synchronized (DisplayChangeController.this.mRotationListener) {
                    DisplayChangeController.this.mTmpListeners.clear();
                    DisplayChangeController.this.mTmpListeners.addAll(DisplayChangeController.this.mRotationListener);
                }
                Iterator it = DisplayChangeController.this.mTmpListeners.iterator();
                while (it.hasNext()) {
                    ((OnDisplayChangingListener) it.next()).onRotateDisplay(i, i2, i3, windowContainerTransaction);
                }
                try {
                    iDisplayWindowRotationCallback.continueRotateDisplay(i3, windowContainerTransaction);
                } catch (RemoteException unused) {
                }
            }
        };
        this.mDisplayRotationController = r0;
        this.mHandler = handler;
        this.mWmService = iWindowManager;
        try {
            iWindowManager.setDisplayWindowRotationController(r0);
        } catch (RemoteException unused) {
            throw new RuntimeException("Unable to register rotation controller");
        }
    }

    public void addRotationListener(OnDisplayChangingListener onDisplayChangingListener) {
        synchronized (this.mRotationListener) {
            this.mRotationListener.add(onDisplayChangingListener);
        }
    }
}
