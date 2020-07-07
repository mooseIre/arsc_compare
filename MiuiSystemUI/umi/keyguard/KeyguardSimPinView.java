package com.android.keyguard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManagerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.plugins.R;
import miui.telephony.SubscriptionManager;

public class KeyguardSimPinView extends KeyguardPinBasedInputView {
    /* access modifiers changed from: private */
    public CheckSimPin mCheckSimPinThread;
    private ViewGroup mContainer;
    /* access modifiers changed from: private */
    public int mRemainingAttempts;
    /* access modifiers changed from: private */
    public boolean mShowDefaultMessage;
    /* access modifiers changed from: private */
    public ImageView mSimImageView;
    /* access modifiers changed from: private */
    public ProgressDialog mSimUnlockProgressDialog;
    /* access modifiers changed from: private */
    public int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    /* access modifiers changed from: protected */
    public int getPasswordTextViewId() {
        return R.id.simPinEntry;
    }

    /* access modifiers changed from: protected */
    public boolean shouldLockout(long j) {
        return false;
    }

    public void startAppearAnimation() {
    }

    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    public KeyguardSimPinView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardSimPinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
                Log.v("KeyguardSimPinView", "onSimStateChanged(subId=" + i + ",state=" + state + ")");
                KeyguardSimPinView.this.resetState();
                if (PhoneUtils.getPhoneCount() == 2) {
                    KeyguardSimPinView.this.mSimImageView.setVisibility(0);
                    int slotIndex = SubscriptionManagerCompat.getSlotIndex(KeyguardSimPinView.this.mSubId);
                    if (slotIndex == 0) {
                        KeyguardSimPinView.this.mSimImageView.setImageResource(R.drawable.miui_keyguard_unlock_sim_1);
                    } else if (slotIndex == 1) {
                        KeyguardSimPinView.this.mSimImageView.setImageResource(R.drawable.miui_keyguard_unlock_sim_2);
                    }
                } else {
                    KeyguardSimPinView.this.mSimImageView.setVisibility(8);
                }
            }
        };
    }

    public void resetState() {
        super.resetState();
        Log.v("KeyguardSimPinView", "Resetting state");
        handleSubInfoChangeIfNeeded();
        if (this.mShowDefaultMessage) {
            showDefaultMessage();
        }
    }

    private void handleSubInfoChangeIfNeeded() {
        int nextSubIdForState = KeyguardUpdateMonitor.getInstance(this.mContext).getNextSubIdForState(IccCardConstants.State.PIN_REQUIRED);
        if (nextSubIdForState != this.mSubId) {
            this.mSubId = nextSubIdForState;
            if (SubscriptionManager.isValidSubscriptionId(nextSubIdForState)) {
                this.mShowDefaultMessage = true;
                this.mRemainingAttempts = -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationFontScaleChanged() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(R.dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mDeleteButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationOrientationChanged() {
        Resources resources = getResources();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        layoutParams.width = resources.getDimensionPixelOffset(R.dimen.miui_keyguard_sim_pin_view_layout_width);
        layoutParams.height = resources.getDimensionPixelOffset(R.dimen.miui_keyguard_sim_pin_view_layout_height);
        this.mContainer.setLayoutParams(layoutParams);
    }

    /* access modifiers changed from: private */
    public String getPinPasswordErrorMessage(int i, boolean z) {
        String str;
        if (this.mSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return null;
        }
        if (i == 0) {
            str = getContext().getString(R.string.kg_password_wrong_pin_code_pukked);
        } else if (i > 0) {
            str = getContext().getResources().getQuantityString(z ? R.plurals.kg_password_default_pin_message : R.plurals.kg_password_wrong_pin_code, i, new Object[]{Integer.valueOf(i)});
        } else {
            str = getContext().getString(z ? R.string.kg_sim_pin_instructions : R.string.kg_password_pin_failed);
        }
        Log.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSimImageView = (ImageView) findViewById(R.id.keyguard_sim);
        this.mContainer = (ViewGroup) findViewById(R.id.container);
        this.mDeleteButton.setVisibility(0);
        this.mBackButton.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mShowDefaultMessage) {
            showDefaultMessage();
        }
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUpdateMonitorCallback);
    }

    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    private abstract class CheckSimPin extends Thread {
        private final String mPin;
        private int mSubId;

        /* access modifiers changed from: package-private */
        public abstract void onSimCheckResponse(int i, int i2);

        protected CheckSimPin(String str, int i) {
            this.mPin = str;
            this.mSubId = i;
        }

        public void run() {
            try {
                Log.v("KeyguardSimPinView", "call supplyPinReportResultForSubscriber(subid=" + this.mSubId + ")");
                final int[] supplyPinReportResultForSubscriber = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPinReportResultForSubscriber(this.mSubId, this.mPin);
                Log.v("KeyguardSimPinView", "supplyPinReportResult returned: " + supplyPinReportResultForSubscriber[0] + " " + supplyPinReportResultForSubscriber[1]);
                KeyguardSimPinView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPin checkSimPin = CheckSimPin.this;
                        int[] iArr = supplyPinReportResultForSubscriber;
                        checkSimPin.onSimCheckResponse(iArr[0], iArr[1]);
                    }
                });
            } catch (Exception e) {
                Log.e("KeyguardSimPinView", "Exception for supplyPinReportResult:", e);
                KeyguardSimPinView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(2, -1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        if (this.mPasswordEntry.getText().length() < 4) {
            this.mSecurityMessageDisplay.setMessage((int) R.string.kg_invalid_sim_pin_hint);
            resetPasswordText(true, true);
            this.mCallback.userActivity();
        } else if (this.mCheckSimPinThread == null) {
            AnonymousClass2 r0 = new CheckSimPin(this.mPasswordEntry.getText(), this.mSubId) {
                /* access modifiers changed from: package-private */
                public void onSimCheckResponse(final int i, final int i2) {
                    KeyguardSimPinView.this.post(new Runnable() {
                        public void run() {
                            int unused = KeyguardSimPinView.this.mRemainingAttempts = i2;
                            if (KeyguardSimPinView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPinView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPinView.this.resetPasswordText(true, i != 0);
                            if (i == 0) {
                                KeyguardUpdateMonitor.getInstance(KeyguardSimPinView.this.getContext()).reportSimUnlocked(KeyguardSimPinView.this.mSubId);
                                int unused2 = KeyguardSimPinView.this.mRemainingAttempts = -1;
                                boolean unused3 = KeyguardSimPinView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPinView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                boolean unused4 = KeyguardSimPinView.this.mShowDefaultMessage = false;
                                if (i == 1) {
                                    KeyguardSimPinView keyguardSimPinView = KeyguardSimPinView.this;
                                    keyguardSimPinView.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPinView.getPinPasswordErrorMessage(i2, false));
                                } else {
                                    KeyguardSimPinView keyguardSimPinView2 = KeyguardSimPinView.this;
                                    keyguardSimPinView2.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPinView2.getContext().getString(R.string.kg_password_pin_failed));
                                }
                                Log.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + i + " attemptsRemaining=" + i2);
                            }
                            KeyguardSimPinView.this.mCallback.userActivity();
                            CheckSimPin unused5 = KeyguardSimPinView.this.mCheckSimPinThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPinThread = r0;
            r0.start();
        }
    }

    private void showDefaultMessage() {
        String str;
        CharSequence charSequence;
        int i = this.mRemainingAttempts;
        if (i >= 0) {
            this.mSecurityMessageDisplay.setMessage((CharSequence) getPinPasswordErrorMessage(i, true));
            return;
        }
        int phoneCount = PhoneUtils.getPhoneCount();
        Resources resources = getResources();
        if (phoneCount < 2) {
            str = resources.getString(R.string.kg_sim_pin_instructions);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = KeyguardUpdateMonitor.getInstance(this.mContext).getSubscriptionInfoForSubId(this.mSubId);
            if (subscriptionInfoForSubId != null) {
                charSequence = subscriptionInfoForSubId.getDisplayName();
            } else {
                charSequence = "";
            }
            String string = resources.getString(R.string.kg_sim_pin_instructions_multi, new Object[]{charSequence});
            if (subscriptionInfoForSubId != null) {
                subscriptionInfoForSubId.getIconTint();
            }
            str = string;
        }
        this.mSecurityMessageDisplay.setMessage((CharSequence) str);
        new CheckSimPin("", this.mSubId) {
            /* access modifiers changed from: package-private */
            public void onSimCheckResponse(int i, int i2) {
                Log.d("KeyguardSimPinView", "onSimCheckResponse  dummy One result" + i + " attemptsRemaining=" + i2);
                if (i2 >= 0) {
                    int unused = KeyguardSimPinView.this.mRemainingAttempts = i2;
                    KeyguardSimPinView keyguardSimPinView = KeyguardSimPinView.this;
                    keyguardSimPinView.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPinView.getPinPasswordErrorMessage(i2, true));
                }
            }
        }.start();
    }
}
