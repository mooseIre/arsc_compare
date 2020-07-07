package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.view.RotationPolicy;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.RotationLockController;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import miui.util.ObjectReference;
import miui.util.ReflectionUtils;

public final class RotationLockControllerImpl implements RotationLockController {
    private final CopyOnWriteArrayList<RotationLockController.RotationLockControllerCallback> mCallbacks = new CopyOnWriteArrayList<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener = new RotationPolicy.RotationPolicyListener() {
        public void onChange() {
            RotationLockControllerImpl.this.notifyChanged();
        }
    };

    public int getRotationLockOrientation() {
        return 0;
    }

    public RotationLockControllerImpl(Context context) {
        this.mContext = context;
        setListening(true);
    }

    public void addCallback(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        this.mCallbacks.add(rotationLockControllerCallback);
        notifyChanged(rotationLockControllerCallback);
    }

    public void removeCallback(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        this.mCallbacks.remove(rotationLockControllerCallback);
    }

    public boolean isRotationLocked() {
        return RotationPolicy.isRotationLocked(this.mContext);
    }

    public void setRotationLocked(boolean z) {
        setRotationLock(this.mContext, z);
    }

    public void setListening(boolean z) {
        if (z) {
            RotationPolicy.registerRotationPolicyListener(this.mContext, this.mRotationPolicyListener, -1);
        } else {
            RotationPolicy.unregisterRotationPolicyListener(this.mContext, this.mRotationPolicyListener);
        }
    }

    /* access modifiers changed from: private */
    public void notifyChanged() {
        Iterator<RotationLockController.RotationLockControllerCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            notifyChanged(it.next());
        }
    }

    private void notifyChanged(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        rotationLockControllerCallback.onRotationLockStateChanged(RotationPolicy.isRotationLocked(this.mContext), RotationPolicy.isRotationLockToggleVisible(this.mContext));
    }

    public void setRotationLock(Context context, boolean z) {
        Settings.System.putIntForUser(context.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", 0, KeyguardUpdateMonitor.getCurrentUser());
        setRotationLock(z, -1);
    }

    private void setRotationLock(final boolean z, final int i) {
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
                    if (z) {
                        windowManagerService.freezeRotation(i);
                        int access$100 = RotationLockControllerImpl.this.getRotation(windowManagerService);
                        if (access$100 != 0 && 2 != access$100) {
                            RotationLockControllerImpl.this.mHandler.post(new Runnable() {
                                public void run() {
                                    Util.showSystemOverlayToast(RotationLockControllerImpl.this.mContext, (int) R.string.miui_screen_rotation_freeze_message, 1);
                                }
                            });
                            return;
                        }
                        return;
                    }
                    windowManagerService.thawRotation();
                } catch (RemoteException unused) {
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public int getRotation(IWindowManager iWindowManager) {
        ObjectReference objectReference;
        if (Build.VERSION.SDK_INT < 26) {
            objectReference = ReflectionUtils.tryCallMethod(iWindowManager, "getRotation", Integer.class, new Object[0]);
        } else {
            objectReference = ReflectionUtils.tryCallMethod(iWindowManager, "getDefaultDisplayRotation", Integer.class, new Object[0]);
        }
        return ((Integer) objectReference.get()).intValue();
    }
}
