package com.android.keyguard;

import android.app.ActivityManagerCompat;
import android.content.Context;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.telephony.ServiceState;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.utils.PhoneUtils;
import miui.os.Build;

public class EmergencyButton extends Button {
    private int mDownX;
    private int mDownY;
    /* access modifiers changed from: private */
    public final EmergencyAffordanceManager mEmergencyAffordanceManager;
    private EmergencyButtonCallback mEmergencyButtonCallback;
    private final boolean mEnableEmergencyCallWhileSimLocked;
    KeyguardUpdateMonitorCallback mInfoCallback;
    private final boolean mIsVoiceCapable;
    /* access modifiers changed from: private */
    public boolean mLongPressWasDragged;
    /* access modifiers changed from: private */
    public boolean mSignalAvailable;

    public interface EmergencyButtonCallback {
        void onEmergencyButtonClickedWhenInCall();
    }

    public EmergencyButton(Context context) {
        this(context, (AttributeSet) null);
    }

    public EmergencyButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            public void onPhoneStateChanged(int i) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            public void onServiceStateChanged(int i, ServiceState serviceState) {
                EmergencyButton.this.updateEmergencyCallButton();
            }

            public void onPhoneSignalChanged(boolean z) {
                boolean unused = EmergencyButton.this.mSignalAvailable = z;
                EmergencyButton.this.updateEmergencyCallButton();
            }
        };
        this.mIsVoiceCapable = context.getResources().getBoolean(17891589);
        this.mEnableEmergencyCallWhileSimLocked = this.mContext.getResources().getBoolean(17891457);
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        new LockPatternUtils(this.mContext);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                EmergencyButton.this.takeEmergencyCallAction();
            }
        });
        setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (EmergencyButton.this.mLongPressWasDragged || PhoneUtils.isInCall(EmergencyButton.this.mContext)) {
                    return false;
                }
                if (!EmergencyButton.this.mEmergencyAffordanceManager.needsEmergencyAffordance() && !MiuiKeyguardUtils.isIndianRegion(EmergencyButton.this.mContext)) {
                    return false;
                }
                EmergencyButton.this.mEmergencyAffordanceManager.performEmergencyCall();
                return true;
            }
        });
        updateEmergencyCallButton();
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
        MiuiKeyguardUtils.userActivity(this.mContext);
        try {
            ActivityManagerCompat.stopSystemLockTaskMode();
        } catch (RemoteException unused) {
            Slog.w("EmergencyButton", "Failed to stop app pinning");
        }
        PhoneUtils.takeEmergencyCallAction(this.mContext, this.mEmergencyButtonCallback);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006c, code lost:
        if (r5.mSignalAvailable != false) goto L_0x0072;
     */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0074  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x008a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateEmergencyCallButton() {
        /*
            r5 = this;
            boolean r0 = r5.mIsVoiceCapable
            r1 = 1
            r2 = 0
            if (r0 == 0) goto L_0x0071
            boolean r0 = r5.isDeviceSupport()
            if (r0 == 0) goto L_0x0071
            android.content.Context r0 = r5.mContext
            boolean r0 = com.android.keyguard.utils.PhoneUtils.isInCall(r0)
            if (r0 == 0) goto L_0x0016
            goto L_0x0072
        L_0x0016:
            android.content.Context r0 = r5.mContext
            com.android.keyguard.KeyguardUpdateMonitor r0 = com.android.keyguard.KeyguardUpdateMonitor.getInstance(r0)
            boolean r0 = r0.isSimPinVoiceSecure()
            if (r0 == 0) goto L_0x0025
            boolean r0 = r5.mEnableEmergencyCallWhileSimLocked
            goto L_0x0044
        L_0x0025:
            android.content.Context r0 = r5.mContext
            com.android.systemui.statusbar.phone.UnlockMethodCache r0 = com.android.systemui.statusbar.phone.UnlockMethodCache.getInstance(r0)
            boolean r0 = r0.isMethodSecure()
            if (r0 != 0) goto L_0x0043
            android.content.Context r0 = r5.mContext
            android.content.res.Resources r0 = r0.getResources()
            r3 = 2131034158(0x7f05002e, float:1.7678826E38)
            boolean r0 = r0.getBoolean(r3)
            if (r0 == 0) goto L_0x0041
            goto L_0x0043
        L_0x0041:
            r0 = r2
            goto L_0x0044
        L_0x0043:
            r0 = r1
        L_0x0044:
            android.content.Context r3 = r5.mContext
            android.content.res.Resources r3 = r3.getResources()
            r4 = 2131034189(0x7f05004d, float:1.7678889E38)
            boolean r3 = r3.getBoolean(r4)
            if (r3 == 0) goto L_0x0064
            android.content.Context r3 = r5.mContext
            com.android.keyguard.KeyguardUpdateMonitor r3 = com.android.keyguard.KeyguardUpdateMonitor.getInstance(r3)
            if (r0 == 0) goto L_0x0063
            boolean r0 = r3.isOOS()
            if (r0 != 0) goto L_0x0063
            r0 = r1
            goto L_0x0064
        L_0x0063:
            r0 = r2
        L_0x0064:
            boolean r3 = com.android.keyguard.MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST
            if (r3 == 0) goto L_0x006f
            if (r0 == 0) goto L_0x0071
            boolean r0 = r5.mSignalAvailable
            if (r0 == 0) goto L_0x0071
            goto L_0x0072
        L_0x006f:
            r1 = r0
            goto L_0x0072
        L_0x0071:
            r1 = r2
        L_0x0072:
            if (r1 == 0) goto L_0x008a
            r5.setVisibility(r2)
            android.content.Context r0 = r5.mContext
            boolean r0 = com.android.keyguard.utils.PhoneUtils.isInCall(r0)
            if (r0 == 0) goto L_0x0083
            r0 = 17040538(0x104049a, float:2.4247872E-38)
            goto L_0x0086
        L_0x0083:
            r0 = 2131821274(0x7f1102da, float:1.9275287E38)
        L_0x0086:
            r5.setText(r0)
            goto L_0x008e
        L_0x008a:
            r0 = 4
            r5.setVisibility(r0)
        L_0x008e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.EmergencyButton.updateEmergencyCallButton():void");
    }

    public void setCallback(EmergencyButtonCallback emergencyButtonCallback) {
        this.mEmergencyButtonCallback = emergencyButtonCallback;
    }

    private boolean isDeviceSupport() {
        return !Build.IS_TABLET;
    }
}
