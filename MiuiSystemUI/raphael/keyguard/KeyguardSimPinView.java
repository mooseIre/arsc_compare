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
    private CheckSimPin mCheckSimPinThread;
    private ViewGroup mContainer;
    private int mRemainingAttempts;
    private boolean mShowDefaultMessage;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void handleConfigurationSmallWidthChanged() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public boolean shouldLockout(long j) {
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public boolean startDisappearAnimation(Runnable runnable) {
        return false;
    }

    public KeyguardSimPinView(Context context) {
        this(context, null);
    }

    public KeyguardSimPinView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mSubId = -1;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.KeyguardSimPinView.AnonymousClass1 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
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

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardPinBasedInputView
    public void resetState() {
        super.resetState();
        Log.v("KeyguardSimPinView", "Resetting state");
        handleSubInfoChangeIfNeeded();
        if (this.mShowDefaultMessage) {
            showDefaultMessage();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLockedSimMessage() {
        String str;
        SubscriptionManager.getSlotIndex(this.mSubId);
        boolean isEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId);
        TelephonyManager telephonyManager = (TelephonyManager) ((LinearLayout) this).mContext.getSystemService("phone");
        int activeModemCount = telephonyManager != null ? telephonyManager.getActiveModemCount() : 1;
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{C0009R$attr.wallpaperTextColor});
        obtainStyledAttributes.getColor(0, -1);
        obtainStyledAttributes.recycle();
        if (activeModemCount < 2) {
            str = resources.getString(C0021R$string.kg_sim_pin_instructions);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
            String string = resources.getString(C0021R$string.kg_sim_pin_instructions_multi, subscriptionInfoForSubId != null ? subscriptionInfoForSubId.getDisplayName() : "");
            if (subscriptionInfoForSubId != null) {
                subscriptionInfoForSubId.getIconTint();
            }
            str = string;
        }
        if (isEsimLocked) {
            str = resources.getString(C0021R$string.kg_sim_lock_esim_instructions, str);
        }
        if (this.mSecurityMessageDisplay != null && getVisibility() == 0) {
            this.mSecurityMessageDisplay.setMessage(str);
        }
    }

    private void showDefaultMessage() {
        setLockedSimMessage();
        if (this.mRemainingAttempts < 0) {
            new CheckSimPin("", this.mSubId) {
                /* class com.android.keyguard.KeyguardSimPinView.AnonymousClass2 */

                /* access modifiers changed from: package-private */
                @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
                public void onSimCheckResponse(PinResult pinResult) {
                    Log.d("KeyguardSimPinView", "onSimCheckResponse  dummy One result " + pinResult.toString());
                    if (pinResult.getAttemptsRemaining() >= 0) {
                        KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
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
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        resetState();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getPinPasswordErrorMessage(int i, boolean z) {
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
            str = getContext().getResources().getQuantityString(i2, i, Integer.valueOf(i));
        } else {
            str = getContext().getString(z ? C0021R$string.kg_sim_pin_instructions : C0021R$string.kg_password_pin_failed);
        }
        if (KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId)) {
            str = getResources().getString(C0021R$string.kg_sim_lock_esim_instructions, str);
        }
        Log.d("KeyguardSimPinView", "getPinPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0015R$id.simPinEntry;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.MiuiKeyguardPasswordView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSimImageView = (ImageView) findViewById(C0015R$id.keyguard_sim);
        this.mContainer = (ViewGroup) findViewById(C0015R$id.container);
        this.mDeleteButton.setVisibility(0);
        this.mBackButton.setVisibility(8);
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardPinBasedInputView
    public void onResume(int i) {
        super.onResume(i);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        resetState();
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    /* access modifiers changed from: private */
    public abstract class CheckSimPin extends Thread {
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
            final PinResult supplyPinReportPinResult = ((TelephonyManager) ((LinearLayout) KeyguardSimPinView.this).mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPinReportPinResult(this.mPin);
            if (supplyPinReportPinResult == null) {
                Log.e("KeyguardSimPinView", "Error result for supplyPinReportResult.");
                KeyguardSimPinView.this.post(new Runnable() {
                    /* class com.android.keyguard.KeyguardSimPinView.CheckSimPin.AnonymousClass1 */

                    public void run() {
                        CheckSimPin.this.onSimCheckResponse(PinResult.getDefaultFailedResult());
                    }
                });
                return;
            }
            Log.v("KeyguardSimPinView", "supplyPinReportResult returned: " + supplyPinReportPinResult.toString());
            KeyguardSimPinView.this.post(new Runnable() {
                /* class com.android.keyguard.KeyguardSimPinView.CheckSimPin.AnonymousClass2 */

                public void run() {
                    CheckSimPin.this.onSimCheckResponse(supplyPinReportPinResult);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void verifyPasswordAndUnlock() {
        if (this.mPasswordEntry.getText().length() < 4) {
            this.mSecurityMessageDisplay.setMessage(C0021R$string.kg_invalid_sim_pin_hint);
            resetPasswordText(true, true);
            this.mCallback.userActivity();
        } else if (this.mCheckSimPinThread == null) {
            AnonymousClass3 r0 = new CheckSimPin(this.mPasswordEntry.getText(), this.mSubId) {
                /* class com.android.keyguard.KeyguardSimPinView.AnonymousClass3 */

                /* access modifiers changed from: package-private */
                @Override // com.android.keyguard.KeyguardSimPinView.CheckSimPin
                public void onSimCheckResponse(final PinResult pinResult) {
                    KeyguardSimPinView.this.post(new Runnable() {
                        /* class com.android.keyguard.KeyguardSimPinView.AnonymousClass3.AnonymousClass1 */

                        public void run() {
                            KeyguardSimPinView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                            if (KeyguardSimPinView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPinView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPinView.this.resetPasswordText(true, pinResult.getType() != 0);
                            if (pinResult.getType() == 0) {
                                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPinView.this.mSubId);
                                KeyguardSimPinView.this.mRemainingAttempts = -1;
                                KeyguardSimPinView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPinView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                KeyguardSimPinView.this.mShowDefaultMessage = false;
                                if (pinResult.getType() == 1) {
                                    KeyguardSimPinView keyguardSimPinView = KeyguardSimPinView.this;
                                    keyguardSimPinView.mSecurityMessageDisplay.setMessage(keyguardSimPinView.getPinPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                } else {
                                    KeyguardSimPinView keyguardSimPinView2 = KeyguardSimPinView.this;
                                    keyguardSimPinView2.mSecurityMessageDisplay.setMessage(keyguardSimPinView2.getContext().getString(C0021R$string.kg_password_pin_failed));
                                }
                                Log.d("KeyguardSimPinView", "verifyPasswordAndUnlock  CheckSimPin.onSimCheckResponse: " + pinResult + " attemptsRemaining=" + pinResult.getAttemptsRemaining());
                            }
                            KeyguardSimPinView.this.mCallback.userActivity();
                            KeyguardSimPinView.this.mCheckSimPinThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPinThread = r0;
            r0.start();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void handleConfigurationFontScaleChanged() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(C0012R$dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mDeleteButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void handleConfigurationOrientationChanged() {
        Resources resources = getResources();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        layoutParams.width = resources.getDimensionPixelOffset(C0012R$dimen.miui_keyguard_sim_pin_view_layout_width);
        layoutParams.height = resources.getDimensionPixelOffset(C0012R$dimen.miui_keyguard_sim_pin_view_layout_height);
        this.mContainer.setLayoutParams(layoutParams);
    }
}
