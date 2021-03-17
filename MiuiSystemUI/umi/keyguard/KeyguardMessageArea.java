package com.android.keyguard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.lang.ref.WeakReference;

public class KeyguardMessageArea extends TextView implements SecurityMessageDisplay, ConfigurationController.ConfigurationListener {
    private static final Object ANNOUNCE_TOKEN = new Object();
    /* access modifiers changed from: private */
    public boolean mBouncerVisible;
    private final ConfigurationController mConfigurationController;
    private ColorStateList mDefaultColorState;
    private final Handler mHandler;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private CharSequence mMessage;
    private ColorStateList mNextMessageColorState;

    public KeyguardMessageArea(Context context) {
        super(context, (AttributeSet) null);
        this.mNextMessageColorState = ColorStateList.valueOf(-1);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onFinishedGoingToSleep(int i) {
                KeyguardMessageArea.this.setSelected(false);
            }

            public void onStartedWakingUp() {
                KeyguardMessageArea.this.setSelected(true);
            }

            public void onKeyguardBouncerChanged(boolean z) {
                boolean unused = KeyguardMessageArea.this.mBouncerVisible = z;
                KeyguardMessageArea.this.update();
            }
        };
        throw new IllegalStateException("This constructor should never be invoked");
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet, ConfigurationController configurationController) {
        this(context, attributeSet, (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class), configurationController);
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet, KeyguardUpdateMonitor keyguardUpdateMonitor, ConfigurationController configurationController) {
        super(context, attributeSet);
        this.mNextMessageColorState = ColorStateList.valueOf(-1);
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onFinishedGoingToSleep(int i) {
                KeyguardMessageArea.this.setSelected(false);
            }

            public void onStartedWakingUp() {
                KeyguardMessageArea.this.setSelected(true);
            }

            public void onKeyguardBouncerChanged(boolean z) {
                boolean unused = KeyguardMessageArea.this.mBouncerVisible = z;
                KeyguardMessageArea.this.update();
            }
        };
        setLayerType(2, (Paint) null);
        keyguardUpdateMonitor.registerCallback(this.mInfoCallback);
        this.mHandler = new Handler(Looper.myLooper());
        this.mConfigurationController = configurationController;
        onThemeChanged();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mConfigurationController.addCallback(this);
        onThemeChanged();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mConfigurationController.removeCallback(this);
    }

    public void onThemeChanged() {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{C0011R$color.white_disabled});
        ColorStateList valueOf = ColorStateList.valueOf(obtainStyledAttributes.getColor(0, -1));
        obtainStyledAttributes.recycle();
        this.mDefaultColorState = valueOf;
        update();
    }

    public void onDensityOrFontScaleChanged() {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(C0022R$style.Keyguard_TextView, new int[]{16842901});
        setTextSize(0, (float) obtainStyledAttributes.getDimensionPixelSize(0, 0));
        obtainStyledAttributes.recycle();
    }

    public void setMessage(CharSequence charSequence) {
        if (!TextUtils.isEmpty(charSequence)) {
            securityMessageChanged(charSequence);
        } else {
            clearMessage();
        }
    }

    public void setMessage(int i) {
        setMessage(i != 0 ? getContext().getResources().getText(i) : null);
    }

    public static KeyguardMessageArea findSecurityMessageDisplay(View view) {
        KeyguardMessageArea keyguardMessageArea = (KeyguardMessageArea) view.findViewById(C0015R$id.keyguard_message_area);
        return keyguardMessageArea == null ? (KeyguardMessageArea) view.getRootView().findViewById(C0015R$id.keyguard_message_area) : keyguardMessageArea;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        setSelected(((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive());
    }

    private void securityMessageChanged(CharSequence charSequence) {
        Object obj = ANNOUNCE_TOKEN;
        this.mMessage = charSequence;
        update();
        this.mHandler.removeCallbacksAndMessages(obj);
        this.mHandler.postAtTime(new AnnounceRunnable(this, getText()), obj, SystemClock.uptimeMillis() + 250);
    }

    private void clearMessage() {
        this.mMessage = null;
        update();
    }

    /* access modifiers changed from: private */
    public void update() {
        CharSequence charSequence = this.mMessage;
        setVisibility(TextUtils.isEmpty(charSequence) ? 4 : 0);
        setText(charSequence);
        ColorStateList colorStateList = this.mDefaultColorState;
        if (this.mNextMessageColorState.getDefaultColor() != -1) {
            colorStateList = this.mNextMessageColorState;
            this.mNextMessageColorState = ColorStateList.valueOf(-1);
        }
        setTextColor(colorStateList);
    }

    private static class AnnounceRunnable implements Runnable {
        private final WeakReference<View> mHost;
        private final CharSequence mTextToAnnounce;

        AnnounceRunnable(View view, CharSequence charSequence) {
            this.mHost = new WeakReference<>(view);
            this.mTextToAnnounce = charSequence;
        }

        public void run() {
            View view = (View) this.mHost.get();
            if (view != null) {
                view.announceForAccessibility(this.mTextToAnnounce);
            }
        }
    }
}
