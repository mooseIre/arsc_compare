package com.android.keyguard;

import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;

public class EmergencyButton extends Button {
    private int mDownX;
    private int mDownY;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    MiuiKeyguardUpdateMonitorCallback mInfoCallback;
    private final boolean mIsVoiceCapable;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLongPressWasDragged;
    private PowerManager mPowerManager;
    private boolean mSignalAvailable;

    public interface EmergencyButtonCallback {
        void onEmergencyButtonClickedWhenInCall();
    }

    public EmergencyButton(Context context) {
        this(context, null);
    }

    public EmergencyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInfoCallback = new MiuiKeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.EmergencyButton.AnonymousClass1 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onPhoneStateChanged(int i) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onServiceStateChanged(int i, ServiceState serviceState) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
            public void onPhoneSignalChanged(boolean z) {
                EmergencyButton.this.mSignalAvailable = z;
                EmergencyButton.this.updateEmergencyCallButton();
            }
        };
        this.mIsVoiceCapable = getTelephonyManager().isVoiceCapable();
        this.mEnableEmergencyCallWhileSimLocked = ((Button) this).mContext.getResources().getBoolean(17891457);
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) ((Button) this).mContext.getSystemService("phone");
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mInfoCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mInfoCallback);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(((Button) this).mContext);
        this.mPowerManager = (PowerManager) ((Button) this).mContext.getSystemService("power");
        setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.$$Lambda$EmergencyButton$KTHEYrkUJc7xBxT3_mk1UfqYZ8 */

            public final void onClick(View view) {
                EmergencyButton.this.lambda$onFinishInflate$0$EmergencyButton(view);
            }
        });
        setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.android.keyguard.$$Lambda$EmergencyButton$lDso_ObwUd3nlVNy8pLMJXmgJO0 */

            public final boolean onLongClick(View view) {
                return EmergencyButton.this.lambda$onFinishInflate$1$EmergencyButton(view);
            }
        });
        DejankUtils.whitelistIpcs(new Runnable() {
            /* class com.android.keyguard.$$Lambda$7IHJ89G67Qw9GERRIAzsEiEpU8 */

            public final void run() {
                EmergencyButton.this.updateEmergencyCallButton();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$EmergencyButton(View view) {
        takeEmergencyCallAction();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$1 */
    public /* synthetic */ boolean lambda$onFinishInflate$1$EmergencyButton(View view) {
        if (this.mLongPressWasDragged) {
            return false;
        }
        if (!this.mEmergencyAffordanceManager.needsEmergencyAffordance() && !MiuiKeyguardUtils.isIndianRegion()) {
            return false;
        }
        this.mEmergencyAffordanceManager.performEmergencyCall();
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (motionEvent.getActionMasked() == 0) {
            this.mDownX = x;
            this.mDownY = y;
            this.mLongPressWasDragged = false;
        } else {
            int abs = Math.abs(x - this.mDownX);
            int abs2 = Math.abs(y - this.mDownY);
            int scaledTouchSlop = ViewConfiguration.get(((Button) this).mContext).getScaledTouchSlop();
            if (Math.abs(abs2) > scaledTouchSlop || Math.abs(abs) > scaledTouchSlop) {
                this.mLongPressWasDragged = true;
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public boolean performLongClick() {
        return super.performLongClick();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateEmergencyCallButton();
    }

    public void takeEmergencyCallAction() {
        MetricsLogger.action(((Button) this).mContext, 200);
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null) {
            powerManager.userActivity(SystemClock.uptimeMillis(), true);
        }
        try {
            ActivityTaskManager.getService().stopSystemLockTaskMode();
        } catch (RemoteException unused) {
            Slog.w("EmergencyButton", "Failed to stop app pinning");
        }
        if (PhoneUtils.isInCall(((Button) this).mContext)) {
            PhoneUtils.resumeCall(((Button) this).mContext);
            EmergencyButtonCallback emergencyButtonCallback = this.mEmergencyButtonCallback;
            if (emergencyButtonCallback != null) {
                emergencyButtonCallback.onEmergencyButtonClickedWhenInCall();
                return;
            }
            return;
        }
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        if (keyguardUpdateMonitor != null) {
            keyguardUpdateMonitor.reportEmergencyCallAction(true);
        } else {
            Log.w("EmergencyButton", "KeyguardUpdateMonitor was null, launching intent anyway.");
        }
        TelecomManager telecommManager = getTelecommManager();
        if (telecommManager == null) {
            Log.wtf("EmergencyButton", "TelecomManager was null, cannot launch emergency dialer");
            return;
        }
        getContext().startActivityAsUser(telecommManager.createLaunchEmergencyDialerIntent(null).setFlags(343932928).putExtra("com.android.phone.EmergencyDialer.extra.ENTRY_TYPE", 1), ActivityOptions.makeCustomAnimation(getContext(), 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0068, code lost:
        if (r5.mSignalAvailable != false) goto L_0x006e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0070  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0085  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateEmergencyCallButton() {
        /*
        // Method dump skipped, instructions count: 139
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.EmergencyButton.updateEmergencyCallButton():void");
    }

    public void setCallback(EmergencyButtonCallback emergencyButtonCallback) {
        this.mEmergencyButtonCallback = emergencyButtonCallback;
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) ((Button) this).mContext.getSystemService("telecom");
    }
}
