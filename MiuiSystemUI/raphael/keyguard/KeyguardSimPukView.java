package com.android.keyguard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.SystemProperties;
import android.telephony.PinResult;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.PasswordTextView;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import java.util.HashMap;
import java.util.Map;

public class KeyguardSimPukView extends KeyguardPinBasedInputView implements PasswordTextView.TextChangeListener {
    private CheckSimPuk mCheckSimPukThread;
    private ViewGroup mContainer;
    private String mKrCustomized;
    private String mPinText;
    private String mPukText;
    private int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    private boolean mShowDefaultMessage;
    private ImageView mSimImageView;
    private ProgressDialog mSimUnlockProgressDialog;
    private StateMachine mStateMachine;
    private int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private Map<String, String> mWrongPukCodeMessageMap;

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

    public KeyguardSimPukView(Context context) {
        this(context, null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mStateMachine = new StateMachine();
        this.mSubId = -1;
        this.mWrongPukCodeMessageMap = new HashMap(4);
        this.mKrCustomized = SystemProperties.get("ro.miui.customized.region");
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.KeyguardSimPukView.AnonymousClass1 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onSimStateChanged(int i, int i2, int i3) {
                Log.v("KeyguardSimPukView", "onSimStateChanged(subId=" + i + ",state=" + i3 + ")");
                KeyguardSimPukView.this.resetState();
                if (PhoneUtils.getPhoneCount() == 2) {
                    KeyguardSimPukView.this.mSimImageView.setVisibility(0);
                    int slotIndex = SubscriptionManager.getSlotIndex(KeyguardSimPukView.this.mSubId);
                    if (slotIndex == 0) {
                        KeyguardSimPukView.this.mSimImageView.setImageResource(C0013R$drawable.miui_keyguard_unlock_sim_1);
                    } else if (slotIndex == 1) {
                        KeyguardSimPukView.this.mSimImageView.setImageResource(C0013R$drawable.miui_keyguard_unlock_sim_2);
                    }
                } else {
                    KeyguardSimPukView.this.mSimImageView.setVisibility(8);
                }
            }
        };
        updateWrongPukMessageMap(context);
    }

    /* access modifiers changed from: package-private */
    public void updateWrongPukMessageMap(Context context) {
        String[] stringArray = context.getResources().getStringArray(C0008R$array.kg_wrong_puk_code_message_list);
        if (stringArray.length == 0) {
            Log.d("KeyguardSimPukView", "There is no customization PUK prompt");
            return;
        }
        for (String str : stringArray) {
            String[] split = str.trim().split(":");
            if (split.length != 2) {
                Log.e("KeyguardSimPukView", "invalid key value config " + str);
            } else {
                this.mWrongPukCodeMessageMap.put(split[0], split[1]);
            }
        }
    }

    private String getMessageTextForWrongPukCode(int i) {
        SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
        if (subscriptionInfoForSubId == null) {
            return null;
        }
        return this.mWrongPukCodeMessageMap.get(subscriptionInfoForSubId.getMccString() + subscriptionInfoForSubId.getMncString());
    }

    /* access modifiers changed from: private */
    public class StateMachine {
        private int state;

        private StateMachine() {
            this.state = 0;
        }

        public void next() {
            int i;
            int i2 = this.state;
            if (i2 == 0) {
                if (KeyguardSimPukView.this.checkPuk()) {
                    this.state = 1;
                    i = C0021R$string.kg_puk_enter_pin_hint;
                } else {
                    i = C0021R$string.kg_invalid_sim_puk_hint;
                }
            } else if (i2 == 1) {
                if (KeyguardSimPukView.this.checkPin()) {
                    this.state = 2;
                    i = C0021R$string.kg_enter_confirm_pin_hint;
                } else {
                    i = C0021R$string.kg_invalid_sim_pin_hint;
                }
            } else if (i2 != 2) {
                i = 0;
            } else if (KeyguardSimPukView.this.confirmPin()) {
                this.state = 3;
                i = C0021R$string.keyguard_sim_unlock_progress_dialog_message;
                KeyguardSimPukView.this.updateSim();
            } else {
                this.state = 1;
                i = C0021R$string.kg_invalid_confirm_pin_hint;
            }
            KeyguardSimPukView.this.resetPasswordText(true, true);
            if (i != 0) {
                KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(i);
            }
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            KeyguardSimPukView.this.mPinText = "";
            KeyguardSimPukView.this.mPukText = "";
            this.state = 0;
            KeyguardSimPukView.this.handleSubInfoChangeIfNeeded();
            if (KeyguardSimPukView.this.mShowDefaultMessage) {
                KeyguardSimPukView.this.showDefaultMessage();
            }
            KeyguardEsimArea.isEsimLocked(((LinearLayout) KeyguardSimPukView.this).mContext, KeyguardSimPukView.this.mSubId);
            KeyguardSimPukView.this.mPasswordEntry.requestFocus();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showDefaultMessage() {
        String str;
        CharSequence charSequence;
        int i = this.mRemainingAttempts;
        if (i >= 0) {
            this.mSecurityMessageDisplay.setMessage(getPukPasswordErrorMessage(i, true));
            return;
        }
        boolean isEsimLocked = KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId);
        TelephonyManager telephonyManager = (TelephonyManager) ((LinearLayout) this).mContext.getSystemService("phone");
        int activeModemCount = telephonyManager != null ? telephonyManager.getActiveModemCount() : 1;
        Resources resources = getResources();
        TypedArray obtainStyledAttributes = ((LinearLayout) this).mContext.obtainStyledAttributes(new int[]{C0009R$attr.wallpaperTextColor});
        obtainStyledAttributes.getColor(0, -1);
        obtainStyledAttributes.recycle();
        if (activeModemCount < 2) {
            str = resources.getString(C0021R$string.kg_puk_enter_puk_hint);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getSubscriptionInfoForSubId(this.mSubId);
            if (subscriptionInfoForSubId != null) {
                charSequence = subscriptionInfoForSubId.getDisplayName();
            } else {
                charSequence = "";
            }
            String string = resources.getString(C0021R$string.kg_puk_enter_puk_hint_multi, charSequence);
            if (subscriptionInfoForSubId != null) {
                subscriptionInfoForSubId.getIconTint();
            }
            str = string;
        }
        if (isEsimLocked) {
            str = resources.getString(C0021R$string.kg_sim_lock_esim_instructions, str);
        }
        SecurityMessageDisplay securityMessageDisplay = this.mSecurityMessageDisplay;
        if (securityMessageDisplay != null) {
            securityMessageDisplay.setMessage(str);
        }
        new CheckSimPuk("", "", this.mSubId) {
            /* class com.android.keyguard.KeyguardSimPukView.AnonymousClass2 */

            /* access modifiers changed from: package-private */
            @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
            public void onSimLockChangedResponse(PinResult pinResult) {
                if (pinResult == null) {
                    Log.e("KeyguardSimPukView", "onSimCheckResponse, pin result is NULL");
                    return;
                }
                Log.d("KeyguardSimPukView", "onSimCheckResponse  dummy One result " + pinResult.toString());
                if (pinResult.getAttemptsRemaining() >= 0) {
                    KeyguardSimPukView.this.mRemainingAttempts = pinResult.getAttemptsRemaining();
                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                    keyguardSimPukView.mSecurityMessageDisplay.setMessage(keyguardSimPukView.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), true));
                }
            }
        }.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSubInfoChangeIfNeeded() {
        int unlockedSubIdForState = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getUnlockedSubIdForState(3);
        if (unlockedSubIdForState != this.mSubId && SubscriptionManager.isValidSubscriptionId(unlockedSubIdForState)) {
            this.mSubId = unlockedSubIdForState;
            this.mShowDefaultMessage = true;
            this.mRemainingAttempts = -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getPukPasswordErrorMessage(int i, boolean z) {
        String str;
        int i2;
        int i3;
        if (i == 0) {
            str = getMessageTextForWrongPukCode(this.mSubId);
            if (str == null) {
                str = getContext().getString(C0021R$string.kg_password_wrong_puk_code_dead);
            }
        } else if (i > 0) {
            if (z) {
                i3 = C0019R$plurals.kg_password_default_puk_message;
            } else {
                i3 = C0019R$plurals.kg_password_wrong_puk_code;
            }
            str = getContext().getResources().getQuantityString(i3, i, Integer.valueOf(i));
        } else {
            if (z) {
                i2 = C0021R$string.kg_puk_enter_puk_hint;
            } else {
                i2 = C0021R$string.kg_password_puk_failed;
            }
            str = getContext().getString(i2);
        }
        if (KeyguardEsimArea.isEsimLocked(((LinearLayout) this).mContext, this.mSubId)) {
            str = getResources().getString(C0021R$string.kg_sim_lock_esim_instructions, str);
        }
        Log.d("KeyguardSimPukView", "getPukPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardPinBasedInputView
    public void resetState() {
        super.resetState();
        this.mStateMachine.reset();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0015R$id.pukEntry;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.KeyguardPinBasedInputView, com.android.keyguard.MiuiKeyguardPasswordView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPasswordEntry.addTextChangedListener(this);
        this.mSimImageView = (ImageView) findViewById(C0015R$id.keyguard_sim);
        this.mContainer = (ViewGroup) findViewById(C0015R$id.container);
        this.mDeleteButton.setVisibility(0);
        this.mBackButton.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        resetState();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public void onPause() {
        ProgressDialog progressDialog = this.mSimUnlockProgressDialog;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mSimUnlockProgressDialog = null;
        }
    }

    /* access modifiers changed from: private */
    public abstract class CheckSimPuk extends Thread {
        private final String mPin;
        private final String mPuk;
        private final int mSubId;

        /* access modifiers changed from: package-private */
        public abstract void onSimLockChangedResponse(PinResult pinResult);

        protected CheckSimPuk(String str, String str2, int i) {
            this.mPuk = str;
            this.mPin = str2;
            this.mSubId = i;
        }

        public void run() {
            Log.v("KeyguardSimPukView", "call supplyPukReportResult()");
            final PinResult supplyPukReportPinResult = ((TelephonyManager) ((LinearLayout) KeyguardSimPukView.this).mContext.getSystemService("phone")).createForSubscriptionId(this.mSubId).supplyPukReportPinResult(this.mPuk, this.mPin);
            if (supplyPukReportPinResult == null) {
                Log.e("KeyguardSimPukView", "Error result for supplyPukReportResult.");
                KeyguardSimPukView.this.post(new Runnable() {
                    /* class com.android.keyguard.KeyguardSimPukView.CheckSimPuk.AnonymousClass1 */

                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(PinResult.getDefaultFailedResult());
                    }
                });
                return;
            }
            Log.v("KeyguardSimPukView", "supplyPukReportResult returned: " + supplyPukReportPinResult.toString());
            KeyguardSimPukView.this.post(new Runnable() {
                /* class com.android.keyguard.KeyguardSimPukView.CheckSimPuk.AnonymousClass2 */

                public void run() {
                    CheckSimPuk.this.onSimLockChangedResponse(supplyPukReportPinResult);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Dialog getPukRemainingAttemptsDialog(int i) {
        String pukPasswordErrorMessage = getPukPasswordErrorMessage(i, false);
        AlertDialog alertDialog = this.mRemainingAttemptsDialog;
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(((LinearLayout) this).mContext);
            builder.setMessage(pukPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(C0021R$string.ok, (DialogInterface.OnClickListener) null);
            AlertDialog create = builder.create();
            this.mRemainingAttemptsDialog = create;
            create.getWindow().setType(2009);
        } else {
            alertDialog.setMessage(pukPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() != 8) {
            return false;
        }
        this.mPukText = this.mPasswordEntry.getText();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean checkPin() {
        int length = this.mPasswordEntry.getText().length();
        if (length < 4 || length > 8) {
            return false;
        }
        this.mPinText = this.mPasswordEntry.getText();
        return true;
    }

    public boolean confirmPin() {
        return this.mPinText.equals(this.mPasswordEntry.getText());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSim() {
        if (this.mCheckSimPukThread == null) {
            AnonymousClass3 r0 = new CheckSimPuk(this.mPukText, this.mPinText, this.mSubId) {
                /* class com.android.keyguard.KeyguardSimPukView.AnonymousClass3 */

                /* access modifiers changed from: package-private */
                @Override // com.android.keyguard.KeyguardSimPukView.CheckSimPuk
                public void onSimLockChangedResponse(final PinResult pinResult) {
                    KeyguardSimPukView.this.post(new Runnable() {
                        /* class com.android.keyguard.KeyguardSimPukView.AnonymousClass3.AnonymousClass1 */

                        public void run() {
                            if (KeyguardSimPukView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPukView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPukView.this.resetPasswordText(true, pinResult.getType() != 0);
                            if (pinResult.getType() == 0) {
                                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).reportSimUnlocked(KeyguardSimPukView.this.mSubId);
                                KeyguardSimPukView.this.mRemainingAttempts = -1;
                                KeyguardSimPukView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPukView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                KeyguardSimPukView.this.mShowDefaultMessage = false;
                                if (pinResult.getType() == 1) {
                                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                                    keyguardSimPukView.mSecurityMessageDisplay.setMessage(keyguardSimPukView.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                    if (pinResult.getAttemptsRemaining() <= 2) {
                                        KeyguardSimPukView.this.getPukRemainingAttemptsDialog(pinResult.getAttemptsRemaining()).show();
                                    } else {
                                        KeyguardSimPukView keyguardSimPukView2 = KeyguardSimPukView.this;
                                        keyguardSimPukView2.mSecurityMessageDisplay.setMessage(keyguardSimPukView2.getPukPasswordErrorMessage(pinResult.getAttemptsRemaining(), false));
                                    }
                                } else {
                                    KeyguardSimPukView keyguardSimPukView3 = KeyguardSimPukView.this;
                                    keyguardSimPukView3.mSecurityMessageDisplay.setMessage(keyguardSimPukView3.getContext().getString(C0021R$string.kg_password_puk_failed));
                                }
                                Log.d("KeyguardSimPukView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + pinResult.getAttemptsRemaining());
                            }
                            KeyguardSimPukView.this.mStateMachine.reset();
                            KeyguardSimPukView.this.mCheckSimPukThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPukThread = r0;
            r0.start();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void verifyPasswordAndUnlock() {
        this.mStateMachine.next();
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

    @Override // com.android.keyguard.PasswordTextView.TextChangeListener
    public void onTextChanged(int i) {
        if (!"kr_kt".equals(this.mKrCustomized) && !"kr_skt".equals(this.mKrCustomized) && !"kr_lgu".equals(this.mKrCustomized)) {
            return;
        }
        if (i >= 8) {
            this.mPasswordEntry.setEnabled(false);
        } else {
            this.mPasswordEntry.setEnabled(true);
        }
    }
}
