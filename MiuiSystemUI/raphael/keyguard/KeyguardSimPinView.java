package com.android.keyguard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.telephony.PinResult;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;

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
        this.mSubId = -1;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int i, int i2, int i3) {
                Log.v("KeyguardSimPinView", "onSimStateChanged(subId=" + i + ",state=" + i3 + ")");
                KeyguardSimPinView.this.resetState();
                if (PhoneUtils.getPhoneCount() == 2) {
                    KeyguardSimPinView.this.mSimImageView.setVisibility(0);
                    int slotIndex = SubscriptionManager.getSlotIndex(KeyguardSimPinView.this.mSubId);
                    if (slotIndex == 0) {
                        KeyguardSimPinView.this.mSimImageView.setImageResource(C0013R$drawable.miui_keyguard_unlock_sim_1);
                    } else if (slotIndex == 1) {
                        KeyguardSimPinView.this.mSimImageView.setImageResource(C0013R$drawable.miui_keyguard_unlock_sim_2);
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

    /* access modifiers changed from: private */
    public void setLockedSimMessage() {
        String str;
        SubscriptionManager.getSlotIndex(this.mSubId);
        boolean isEsimLocked = KeyguardEsimArea.isEsimLocked(this.mContext, this.mSubId);
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        int activeModemCount = telephonyManager != null ? telephonyManager.getActiveModemCount() : 1;
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{C0009R$attr.wallpaperTextColor});
        obtainStyledAttributes.getColor(0, -1);
        obtainStyledAttributes.recycle();
        if (activeModemCount < 2) {
            str = resources.getString(C0021R$string.kg_sim_pin_instructions);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
            String string = resources.getString(C0021R$string.kg_sim_pin_instructions_multi, new Object[]{subscriptionInfoForSubId != null ? subscriptionInfoForSubId.getDisplayName() : ""});
            if (subscriptionInfoForSubId != null) {
                subscriptionInfoForSubId.getIconTint();
            }
            str = string;
        }
        if (isEsimLocked) {
            str = resources.getString(C0021R$string.kg_sim_lock_esim_instructions, new Object[]{str});
        }
        if (this.mSecurityMessageDisplay != null && getVisibility() == 0) {
            this.mSecurityMessageDisplay.setMessage((CharSequence) str);
        }
    }

    private void showDefaultMessage() {
        setLockedSimMessage();
        if (this.mRemainingAttempts < 0) {
            new CheckSimPin("", this.mSubId) {
                /* access modifiers changed from: package-private */
                public void onSimCheckResponse(PinResult pinResult) {
                    Log.d("KeyguardSimPinView", "onSimCheckResponse  dummy One result " + pinResult.toString());
                    if (pinResult.getAttemptsRemaining() >= 0) {
                        int unused = KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                        KeyguardSimPinView.this.setLockedSimMessage();
                    }
                }
            }.start();
        }
    }

    private void handleSubInfoChangeIfNeeded() {
        int unlockedSubIdForState = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getUnlockedSubIdForState(2);
        if (unlockedSubIdForState != this.mSubId) {
            this.mSubId = unlockedSubIdForState;
            if (SubscriptionManager.isValidSubscriptionId(unlockedSubIdForState)) {
                this.mShowDefaultMessage = true;
                this.mRemainingAttempts = -1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        resetState();
    }

    /* access modifiers changed from: private */
    public String getPinPasswordErrorMessage(int i, boolean z) {
        String str;
        int i2;
        if (this.mSubId == -1) {
            return null;
        }
        if (i == 0) {
            str = getContext().getString(C0021R$string.kg_password_wrong_pin_code_pukked);
        } else if (i > 0) {
            if (z) {
                i2 = C0019R$plurals.kg_password_default_pin_message;
            } else {
                i2 = C0019R$plurals.kg_password_wrong_pin_code;
            }
            str = getContext().getResources().getQuantityString(i2, i, new Object[]{Integer.valueOf(i)});
        } else {
            str = getContext().getString(z ? C0021R$string.kg_sim_pin_instructions : C0021R$string.kg_password_pin_failed);
        }
        if (KeyguardEsimArea.isEsimLocked(this.mContext, this.mSubId)) {
            str = getResources().getString(C0021R$string.kg_sim_lock_esim_instructions, new Object[]{str});
        }
        Log.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    /* access modifiers changed from: protected */
    public int getPasswordTextViewId() {
        return C0015R$id.simPinEntry;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSimImageView = (ImageView) findViewById(C0015R$id.keyguard_sim);
        this.mContainer = (ViewGroup) findViewById(C0015R$id.container);
        this.mDeleteButton.setVisibility(0);
        this.mBackButton.setVisibility(8);
    }

    public void onResume(int i) {
        super.onResume(i);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        resetState();
    }

    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    private abstract class CheckSimPin extends Thread {
        private final String mPin;
        private int mSubId;

        /* access modifiers changed from: package-private */
        public abstract void onSimCheckResponse(PinResult pinResult);

        protected CheckSimPin(String str, int i) {
            this.mPin = str;
            this.mSubId = i;
        }

        public void run() {
            Log.v("KeyguardSimPinView", "call supplyPinReportResultForSubscriber(subid=" + this.mSubId + ")");
            final PinResult supplyPinReportPinResult = ((TelephonyManager) KeyguardSimPinView.this.mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPinReportPinResult(this.mPin);
            if (supplyPinReportPinResult == null) {
                Log.e("KeyguardSimPinView", "Error result for supplyPinReportResult.");
                KeyguardSimPinView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(PinResult.getDefaultFailedResult());
                    }
                });
                return;
            }
            Log.v("KeyguardSimPinView", "supplyPinReportResult returned: " + supplyPinReportPinResult.toString());
            KeyguardSimPinView.this.post(new Runnable() {
                public void run() {
                    CheckSimPin.this.onSimCheckResponse(supplyPinReportPinResult);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        if (this.mPasswordEntry.getText().length() < 4) {
            this.mSecurityMessageDisplay.setMessage(C0021R$string.kg_invalid_sim_pin_hint);
            resetPasswordText(true, true);
            this.mCallback.userActivity();
        } else if (this.mCheckSimPinThread == null) {
            AnonymousClass3 r0 = new CheckSimPin(this.mPasswordEntry.getText(), this.mSubId) {
                /* access modifiers changed from: package-private */
                public void onSimCheckResponse(final PinResult pinResult) {
                    KeyguardSimPinView.this.post(new Runnable() {
                        public void run() {
                            int unused = KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                            if (KeyguardSimPinView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPinView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPinView.this.resetPasswordText(true, pinResult.getType() != 0);
                            if (pinResult.getType() == 0) {
                                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPinView.this.mSubId);
                                int unused2 = KeyguardSimPinView.this.mRemainingAttempts = -1;
                                boolean unused3 = KeyguardSimPinView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPinView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                boolean unused4 = KeyguardSimPinView.this.mShowDefaultMessage = false;
                                if (pinResult.getType() == 1) {
                                    KeyguardSimPinView keyguardSimPinView = KeyguardSimPinView.this;
                                    keyguardSimPinView.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPinView.getPinPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                } else {
                                    KeyguardSimPinView keyguardSimPinView2 = KeyguardSimPinView.this;
                                    keyguardSimPinView2.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPinView2.getContext().getString(C0021R$string.kg_password_pin_failed));
                                }
                                Log.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + pinResult + " attemptsRemaining=" + pinResult.getAttemptsRemaining());
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

    /* access modifiers changed from: protected */
    public void handleConfigurationFontScaleChanged() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(C0012R$dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mDeleteButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationOrientationChanged() {
        Resources resources = getResources();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        layoutParams.width = resources.getDimensionPixelOffset(C0012R$dimen.miui_keyguard_sim_pin_view_layout_width);
        layoutParams.height = resources.getDimensionPixelOffset(C0012R$dimen.miui_keyguard_sim_pin_view_layout_height);
        this.mContainer.setLayoutParams(layoutParams);
    }
}
