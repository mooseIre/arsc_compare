package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.provider.Settings;
import com.android.internal.view.RotationPolicy;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.statusbar.policy.RotationLockController;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RotationLockControllerImpl implements RotationLockController {
    private final CopyOnWriteArrayList<RotationLockController.RotationLockControllerCallback> mCallbacks = new CopyOnWriteArrayList<>();
    private final Context mContext;
    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener = new RotationPolicy.RotationPolicyListener() {
        /* class com.android.systemui.statusbar.policy.RotationLockControllerImpl.AnonymousClass1 */

        public void onChange() {
            RotationLockControllerImpl.this.notifyChanged();
        }
    };

    @Override // com.android.systemui.statusbar.policy.RotationLockController
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

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public boolean isRotationLocked() {
        return RotationPolicy.isRotationLocked(this.mContext);
    }

    @Override // com.android.systemui.statusbar.policy.RotationLockController
    public void setRotationLockedAtAngle(boolean z, int i) {
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "hide_rotation_lock_toggle_for_accessibility", 0, KeyguardUpdateMonitor.getCurrentUser());
        RotationPolicy.setRotationLockAtAngle(this.mContext, z, i);
    }

    public void setListening(boolean z) {
        if (z) {
            RotationPolicy.registerRotationPolicyListener(this.mContext, this.mRotationPolicyListener, -1);
        } else {
            RotationPolicy.unregisterRotationPolicyListener(this.mContext, this.mRotationPolicyListener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyChanged() {
        Iterator<RotationLockController.RotationLockControllerCallback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            notifyChanged(it.next());
        }
    }

    private void notifyChanged(RotationLockController.RotationLockControllerCallback rotationLockControllerCallback) {
        rotationLockControllerCallback.onRotationLockStateChanged(RotationPolicy.isRotationLocked(this.mContext), RotationPolicy.isRotationLockToggleVisible(this.mContext));
    }
}
