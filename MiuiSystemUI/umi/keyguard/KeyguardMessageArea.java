package com.android.keyguard;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.plugins.R;
import java.lang.ref.WeakReference;

class KeyguardMessageArea extends TextView implements SecurityMessageDisplay {
    private static final Object ANNOUNCE_TOKEN = new Object();
    private final int mDefaultColor;
    private final Handler mHandler;
    private KeyguardUpdateMonitorCallback mInfoCallback;
    private CharSequence mMessage;
    private int mNextMessageColor;

    public KeyguardMessageArea(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, KeyguardUpdateMonitor.getInstance(context));
    }

    public KeyguardMessageArea(Context context, AttributeSet attributeSet, KeyguardUpdateMonitor keyguardUpdateMonitor) {
        super(context, attributeSet);
        this.mNextMessageColor = -1;
        this.mInfoCallback = new KeyguardUpdateMonitorCallback() {
            public void onFinishedGoingToSleep(int i) {
                KeyguardMessageArea.this.setSelected(false);
            }

            public void onStartedWakingUp() {
                KeyguardMessageArea.this.setSelected(true);
            }
        };
        setLayerType(2, (Paint) null);
        keyguardUpdateMonitor.registerCallback(this.mInfoCallback);
        this.mHandler = new Handler(Looper.myLooper());
        this.mDefaultColor = getCurrentTextColor();
        update();
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

    public static SecurityMessageDisplay findSecurityMessageDisplay(View view) {
        return (KeyguardMessageArea) view.findViewById(R.id.keyguard_message_area);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        setSelected(KeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive());
    }

    private void securityMessageChanged(CharSequence charSequence) {
        this.mMessage = charSequence;
        update();
        this.mHandler.removeCallbacksAndMessages(ANNOUNCE_TOKEN);
        this.mHandler.postAtTime(new AnnounceRunnable(this, getText()), ANNOUNCE_TOKEN, SystemClock.uptimeMillis() + 250);
    }

    private void clearMessage() {
        this.mMessage = null;
        update();
    }

    private void update() {
        CharSequence charSequence = this.mMessage;
        setVisibility(TextUtils.isEmpty(charSequence) ? 4 : 0);
        setText(charSequence);
        int i = this.mDefaultColor;
        int i2 = this.mNextMessageColor;
        if (i2 != -1) {
            this.mNextMessageColor = -1;
            i = i2;
        }
        setTextColor(i);
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
