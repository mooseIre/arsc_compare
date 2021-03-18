package com.android.keyguard;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityContainer;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import java.io.File;

public class KeyguardHostView extends FrameLayout implements KeyguardSecurityContainer.SecurityCallback {
    private AudioManager mAudioManager;
    private Runnable mCancelAction;
    private ActivityStarter.OnDismissAction mDismissAction;
    protected LockPatternUtils mLockPatternUtils;
    protected KeyguardSecurityContainer mSecurityContainer;
    private final KeyguardUpdateMonitorCallback mUpdateCallback;
    protected ViewMediatorCallback mViewMediatorCallback;

    public KeyguardHostView(Context context) {
        this(context, null);
    }

    public KeyguardHostView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.KeyguardHostView.AnonymousClass1 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                KeyguardHostView.this.getSecurityContainer().showPrimarySecurityScreen(false);
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onTrustGrantedWithFlags(int i, int i2) {
                if (i2 == KeyguardUpdateMonitor.getCurrentUser() && KeyguardHostView.this.isAttachedToWindow()) {
                    boolean isVisibleToUser = KeyguardHostView.this.isVisibleToUser();
                    boolean z = (i & 1) != 0;
                    boolean z2 = (i & 2) != 0;
                    if (!z && !z2) {
                        return;
                    }
                    if (!KeyguardHostView.this.mViewMediatorCallback.isScreenOn() || (!isVisibleToUser && !z2)) {
                        KeyguardHostView.this.mViewMediatorCallback.playTrustedSound();
                        return;
                    }
                    if (!isVisibleToUser) {
                        Log.i("KeyguardViewBase", "TrustAgent dismissed Keyguard.");
                    }
                    ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).setKeyguardUnlockWay("smart_lock", true);
                    KeyguardHostView.this.dismiss(false, i2, false);
                }
            }
        };
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateCallback);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.keyguardDoneDrawing();
        }
    }

    public void setOnDismissAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable) {
        Runnable runnable2 = this.mCancelAction;
        if (runnable2 != null) {
            runnable2.run();
            this.mCancelAction = null;
        }
        this.mDismissAction = onDismissAction;
        this.mCancelAction = runnable;
    }

    public boolean hasDismissActions() {
        return (this.mDismissAction == null && this.mCancelAction == null) ? false : true;
    }

    public void cancelDismissAction() {
        setOnDismissAction(null, null);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mSecurityContainer = (KeyguardSecurityContainer) findViewById(C0015R$id.keyguard_security_container);
        LockPatternUtils lockPatternUtils = new LockPatternUtils(((FrameLayout) this).mContext);
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
        this.mSecurityContainer.setSecurityCallback(this);
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPrimarySecurityScreen() {
        Log.d("KeyguardViewBase", "show()");
        this.mSecurityContainer.showPrimarySecurityScreen(false);
    }

    public void showPromptReason(int i) {
        this.mSecurityContainer.showPromptReason(i);
    }

    public void showMessage(String str, int i) {
        this.mSecurityContainer.showMessage("", str, i);
    }

    public void showMessage(String str, String str2, int i) {
        this.mSecurityContainer.showMessage(str, str2, i);
    }

    public void applyHintAnimation(long j) {
        this.mSecurityContainer.applyHintAnimation(j);
    }

    public boolean dismiss(int i) {
        return dismiss(false, i, false);
    }

    public boolean handleBackKey() {
        LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(((FrameLayout) this).mContext, "Wallpaper_Uncovered");
        return this.mSecurityContainer.onBackPressed();
    }

    /* access modifiers changed from: protected */
    public KeyguardSecurityContainer getSecurityContainer() {
        return this.mSecurityContainer;
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public boolean dismiss(boolean z, int i, boolean z2) {
        return this.mSecurityContainer.showNextSecurityScreenOrFinish(z, i, z2);
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void finish(boolean z, int i) {
        boolean z2;
        ActivityStarter.OnDismissAction onDismissAction = this.mDismissAction;
        if (onDismissAction != null) {
            z2 = onDismissAction.onDismiss();
            this.mDismissAction = null;
            this.mCancelAction = null;
        } else {
            z2 = false;
        }
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback == null) {
            return;
        }
        if (z2) {
            viewMediatorCallback.keyguardDonePending(z, i);
        } else {
            viewMediatorCallback.keyguardDone(z, i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void reset() {
        this.mViewMediatorCallback.resetKeyguard();
    }

    public void resetSecurityContainer() {
        this.mSecurityContainer.reset();
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z) {
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.setNeedsInput(z);
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().setSecurityMode(securityMode);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityContainer.SecurityCallback
    public void userActivity() {
        ViewMediatorCallback viewMediatorCallback = this.mViewMediatorCallback;
        if (viewMediatorCallback != null) {
            viewMediatorCallback.userActivity();
        }
    }

    public void onPause() {
        Log.d("KeyguardViewBase", String.format("screen off, instance %s at %s", Integer.toHexString(hashCode()), Long.valueOf(SystemClock.uptimeMillis())));
        this.mSecurityContainer.showPrimarySecurityScreen(true);
        this.mSecurityContainer.onPause();
        clearFocus();
    }

    public void onResume() {
        Log.d("KeyguardViewBase", "screen on, instance " + Integer.toHexString(hashCode()));
        this.mSecurityContainer.onResume(1);
        requestFocus();
    }

    public void startAppearAnimation() {
        this.mSecurityContainer.startAppearAnimation();
    }

    public void startDisappearAnimation(Runnable runnable) {
        if (!this.mSecurityContainer.startDisappearAnimation(runnable) && runnable != null) {
            runnable.run();
        }
    }

    public void cleanUp() {
        getSecurityContainer().onPause();
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (interceptMediaKey(keyEvent)) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyEvent.getAction() == 0) {
            if (!(keyCode == 79 || keyCode == 130 || keyCode == 222)) {
                if (!(keyCode == 126 || keyCode == 127)) {
                    switch (keyCode) {
                        case 85:
                            break;
                        case 86:
                        case 87:
                        case 88:
                        case 89:
                        case R$styleable.Constraint_layout_constraintVertical_chainStyle:
                        case 91:
                            break;
                        default:
                            return false;
                    }
                }
                if (PhoneUtils.isInCall(((FrameLayout) this).mContext)) {
                    return true;
                }
            }
            handleMediaKeyEvent(keyEvent);
            return true;
        } else if (keyEvent.getAction() != 1) {
            return false;
        } else {
            if (!(keyCode == 79 || keyCode == 130 || keyCode == 222 || keyCode == 126 || keyCode == 127)) {
                switch (keyCode) {
                    case 85:
                    case 86:
                    case 87:
                    case 88:
                    case 89:
                    case R$styleable.Constraint_layout_constraintVertical_chainStyle:
                    case 91:
                        break;
                    default:
                        return false;
                }
            }
            handleMediaKeyEvent(keyEvent);
            return true;
        }
    }

    private void handleMediaKeyEvent(KeyEvent keyEvent) {
        synchronized (this) {
            if (this.mAudioManager == null) {
                this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
            }
        }
        this.mAudioManager.dispatchMediaKeyEvent(keyEvent);
    }

    public boolean shouldEnableMenuKey() {
        return !getResources().getBoolean(C0010R$bool.config_disableMenuKeyInLockScreen) || ActivityManager.isRunningInTestHarness() || new File("/data/local/enable_menu_key").exists();
    }

    public void setViewMediatorCallback(ViewMediatorCallback viewMediatorCallback) {
        this.mViewMediatorCallback = viewMediatorCallback;
        viewMediatorCallback.setNeedsInput(this.mSecurityContainer.needsInput());
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityContainer.setLockPatternUtils(lockPatternUtils);
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityContainer.getSecurityMode();
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mSecurityContainer.getCurrentSecurityMode();
    }
}
