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
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;

public class EmergencyButton extends Button {
    private int mDownX;
    private int mDownY;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private final boolean mIsVoiceCapable;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLongPressWasDragged;
    private PowerManager mPowerManager;

    public interface EmergencyButtonCallback {
        void onEmergencyButtonClickedWhenInCall();
    }

    public EmergencyButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public EmergencyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int i, int i2, int i3) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            public void onPhoneStateChanged(int i) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            public void onServiceStateChanged(int i, ServiceState serviceState) {
                EmergencyButton.this.updateEmergencyCallButton();
            }
        };
        this.mIsVoiceCapable = getTelephonyManager().isVoiceCapable();
        this.mEnableEmergencyCallWhileSimLocked = this.mContext.getResources().getBoolean(17891457);
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService("phone");
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
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                EmergencyButton.this.lambda$onFinishInflate$0$EmergencyButton(view);
            }
        });
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            setOnLongClickListener(new View.OnLongClickListener() {
                public final boolean onLongClick(View view) {
                    return EmergencyButton.this.lambda$onFinishInflate$1$EmergencyButton(view);
                }
            });
        }
        DejankUtils.whitelistIpcs((Runnable) new Runnable() {
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
        if (this.mLongPressWasDragged || !this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
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
            int scaledTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
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
        MetricsLogger.action(this.mContext, 200);
        PowerManager powerManager = this.mPowerManager;
        if (powerManager != null) {
            powerManager.userActivity(SystemClock.uptimeMillis(), true);
        }
        try {
            ActivityTaskManager.getService().stopSystemLockTaskMode();
        } catch (RemoteException unused) {
            Slog.w("EmergencyButton", "Failed to stop app pinning");
        }
        if (PhoneUtils.isInCall(this.mContext)) {
            PhoneUtils.resumeCall(this.mContext);
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
        getContext().startActivityAsUser(telecommManager.createLaunchEmergencyDialerIntent((String) null).setFlags(343932928).putExtra("com.android.phone.EmergencyDialer.extra.ENTRY_TYPE", 1), ActivityOptions.makeCustomAnimation(getContext(), 0, 0).toBundle(), new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005a, code lost:
        if (r3.isOOS() == false) goto L_0x0060;
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0077  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateEmergencyCallButton() {
        /*
            r5 = this;
            boolean r0 = r5.mIsVoiceCapable
            r1 = 1
            r2 = 0
            if (r0 == 0) goto L_0x005f
            android.content.Context r0 = r5.mContext
            boolean r0 = com.android.keyguard.utils.PhoneUtils.isInCall(r0)
            if (r0 == 0) goto L_0x000f
            goto L_0x0060
        L_0x000f:
            java.lang.Class<com.android.keyguard.KeyguardUpdateMonitor> r0 = com.android.keyguard.KeyguardUpdateMonitor.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            com.android.keyguard.KeyguardUpdateMonitor r0 = (com.android.keyguard.KeyguardUpdateMonitor) r0
            boolean r0 = r0.isSimPinVoiceSecure()
            if (r0 == 0) goto L_0x0020
            boolean r0 = r5.mEnableEmergencyCallWhileSimLocked
            goto L_0x003e
        L_0x0020:
            com.android.internal.widget.LockPatternUtils r0 = r5.mLockPatternUtils
            int r3 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()
            boolean r0 = r0.isSecure(r3)
            if (r0 != 0) goto L_0x003d
            android.content.Context r0 = r5.mContext
            android.content.res.Resources r0 = r0.getResources()
            int r3 = com.android.systemui.C0007R$bool.config_showEmergencyButton
            boolean r0 = r0.getBoolean(r3)
            if (r0 == 0) goto L_0x003b
            goto L_0x003d
        L_0x003b:
            r0 = r2
            goto L_0x003e
        L_0x003d:
            r0 = r1
        L_0x003e:
            android.content.Context r3 = r5.mContext
            android.content.res.Resources r3 = r3.getResources()
            int r4 = com.android.systemui.C0007R$bool.kg_hide_emgcy_btn_when_oos
            boolean r3 = r3.getBoolean(r4)
            if (r3 == 0) goto L_0x005d
            java.lang.Class<com.android.keyguard.KeyguardUpdateMonitor> r3 = com.android.keyguard.KeyguardUpdateMonitor.class
            java.lang.Object r3 = com.android.systemui.Dependency.get(r3)
            com.android.keyguard.KeyguardUpdateMonitor r3 = (com.android.keyguard.KeyguardUpdateMonitor) r3
            if (r0 == 0) goto L_0x005f
            boolean r0 = r3.isOOS()
            if (r0 != 0) goto L_0x005f
            goto L_0x0060
        L_0x005d:
            r1 = r0
            goto L_0x0060
        L_0x005f:
            r1 = r2
        L_0x0060:
            if (r1 == 0) goto L_0x0077
            r5.setVisibility(r2)
            android.content.Context r0 = r5.mContext
            boolean r0 = com.android.keyguard.utils.PhoneUtils.isInCall(r0)
            if (r0 == 0) goto L_0x0071
            r0 = 17040538(0x104049a, float:2.4247872E-38)
            goto L_0x0073
        L_0x0071:
            int r0 = com.android.systemui.C0018R$string.emergency_call_string
        L_0x0073:
            r5.setText(r0)
            goto L_0x007c
        L_0x0077:
            r0 = 8
            r5.setVisibility(r0)
        L_0x007c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.EmergencyButton.updateEmergencyCallButton():void");
    }

    public void setCallback(EmergencyButtonCallback emergencyButtonCallback) {
        this.mEmergencyButtonCallback = emergencyButtonCallback;
    }

    private TelecomManager getTelecommManager() {
        return (TelecomManager) this.mContext.getSystemService("telecom");
    }
}
