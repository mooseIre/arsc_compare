package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.ServiceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManagerCompat;
import android.telephony.TelephonyManager;
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

public class KeyguardSimPukView extends KeyguardPinBasedInputView {
    /* access modifiers changed from: private */
    public CheckSimPuk mCheckSimPukThread;
    private ViewGroup mContainer;
    /* access modifiers changed from: private */
    public String mPinText;
    /* access modifiers changed from: private */
    public String mPukText;
    /* access modifiers changed from: private */
    public int mRemainingAttempts;
    private AlertDialog mRemainingAttemptsDialog;
    /* access modifiers changed from: private */
    public boolean mShowDefaultMessage;
    /* access modifiers changed from: private */
    public ImageView mSimImageView;
    /* access modifiers changed from: private */
    public ProgressDialog mSimUnlockProgressDialog;
    /* access modifiers changed from: private */
    public StateMachine mStateMachine;
    /* access modifiers changed from: private */
    public int mSubId;
    KeyguardUpdateMonitorCallback mUpdateMonitorCallback;

    /* access modifiers changed from: protected */
    public int getPasswordTextViewId() {
        return R.id.pukEntry;
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

    public KeyguardSimPukView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardSimPukView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSimUnlockProgressDialog = null;
        this.mShowDefaultMessage = true;
        this.mRemainingAttempts = -1;
        this.mStateMachine = new StateMachine();
        this.mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onSimStateChanged(int i, int i2, IccCardConstants.State state) {
                Log.v("KeyguardSimPukView", "onSimStateChanged(subId=" + i + ",state=" + state + ")");
                KeyguardSimPukView.this.resetState();
                if (PhoneUtils.getPhoneCount() == 2) {
                    KeyguardSimPukView.this.mSimImageView.setVisibility(0);
                    int slotIndex = SubscriptionManagerCompat.getSlotIndex(KeyguardSimPukView.this.mSubId);
                    if (slotIndex == 0) {
                        KeyguardSimPukView.this.mSimImageView.setImageResource(R.drawable.miui_keyguard_unlock_sim_1);
                    } else if (slotIndex == 1) {
                        KeyguardSimPukView.this.mSimImageView.setImageResource(R.drawable.miui_keyguard_unlock_sim_2);
                    }
                } else {
                    KeyguardSimPukView.this.mSimImageView.setVisibility(8);
                }
            }
        };
    }

    private class StateMachine {
        final int CONFIRM_PIN;
        final int DONE;
        final int ENTER_PIN;
        final int ENTER_PUK;
        private int state;

        private StateMachine() {
            this.ENTER_PUK = 0;
            this.ENTER_PIN = 1;
            this.CONFIRM_PIN = 2;
            this.DONE = 3;
            this.state = 0;
        }

        public void next() {
            int i;
            int i2 = this.state;
            if (i2 == 0) {
                if (KeyguardSimPukView.this.checkPuk()) {
                    this.state = 1;
                    i = R.string.kg_puk_enter_pin_hint;
                } else {
                    i = R.string.kg_invalid_sim_puk_hint;
                }
            } else if (i2 == 1) {
                if (KeyguardSimPukView.this.checkPin()) {
                    this.state = 2;
                    i = R.string.kg_enter_confirm_pin_hint;
                } else {
                    i = R.string.kg_invalid_sim_pin_hint;
                }
            } else if (i2 != 2) {
                i = 0;
            } else if (KeyguardSimPukView.this.confirmPin()) {
                this.state = 3;
                i = R.string.keyguard_sim_unlock_progress_dialog_message;
                KeyguardSimPukView.this.updateSim();
            } else {
                this.state = 1;
                i = R.string.kg_invalid_confirm_pin_hint;
            }
            KeyguardSimPukView.this.resetPasswordText(true, true);
            if (i != 0) {
                KeyguardSimPukView.this.mSecurityMessageDisplay.setMessage(i);
            }
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            String unused = KeyguardSimPukView.this.mPinText = "";
            String unused2 = KeyguardSimPukView.this.mPukText = "";
            this.state = 0;
            KeyguardSimPukView.this.handleSubInfoChangeIfNeeded();
            if (KeyguardSimPukView.this.mShowDefaultMessage) {
                KeyguardSimPukView.this.showDefaultMessage();
            }
            KeyguardSimPukView.this.mPasswordEntry.requestFocus();
        }
    }

    /* access modifiers changed from: private */
    public void handleSubInfoChangeIfNeeded() {
        int nextSubIdForState = KeyguardUpdateMonitor.getInstance(this.mContext).getNextSubIdForState(IccCardConstants.State.PUK_REQUIRED);
        if (nextSubIdForState != this.mSubId && SubscriptionManager.isValidSubscriptionId(nextSubIdForState)) {
            this.mSubId = nextSubIdForState;
            this.mShowDefaultMessage = true;
            this.mRemainingAttempts = -1;
        }
    }

    /* access modifiers changed from: private */
    public String getPukPasswordErrorMessage(int i, boolean z) {
        String str;
        if (i == 0) {
            str = getContext().getString(R.string.kg_password_wrong_puk_code_dead);
        } else if (i > 0) {
            str = getContext().getResources().getQuantityString(z ? R.plurals.kg_password_default_puk_message : R.plurals.kg_password_wrong_puk_code, i, new Object[]{Integer.valueOf(i)});
        } else {
            str = getContext().getString(z ? R.string.kg_puk_enter_puk_hint : R.string.kg_password_puk_failed);
        }
        Log.d("KeyguardSimPukView", "getPukPasswordErrorMessage: attemptsRemaining=" + i + " displayMessage=" + str);
        return str;
    }

    public void resetState() {
        super.resetState();
        this.mStateMachine.reset();
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

    private abstract class CheckSimPuk extends Thread {
        private final String mPin;
        private final String mPuk;
        private final int mSubId;

        /* access modifiers changed from: package-private */
        public abstract void onSimLockChangedResponse(int i, int i2);

        protected CheckSimPuk(String str, String str2, int i) {
            this.mPuk = str;
            this.mPin = str2;
            this.mSubId = i;
        }

        public void run() {
            try {
                Log.v("KeyguardSimPukView", "call supplyPukReportResult()");
                final int[] supplyPukReportResultForSubscriber = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukReportResultForSubscriber(this.mSubId, this.mPuk, this.mPin);
                Log.v("KeyguardSimPukView", "supplyPukReportResult returned: " + supplyPukReportResultForSubscriber[0] + " " + supplyPukReportResultForSubscriber[1]);
                KeyguardSimPukView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPuk checkSimPuk = CheckSimPuk.this;
                        int[] iArr = supplyPukReportResultForSubscriber;
                        checkSimPuk.onSimLockChangedResponse(iArr[0], iArr[1]);
                    }
                });
            } catch (Exception e) {
                Log.e("KeyguardSimPukView", "Exception for supplyPukReportResult:", e);
                KeyguardSimPukView.this.post(new Runnable() {
                    public void run() {
                        CheckSimPuk.this.onSimLockChangedResponse(2, -1);
                    }
                });
            }
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (this.mSimUnlockProgressDialog == null) {
            this.mSimUnlockProgressDialog = new ProgressDialog(this.mContext);
            this.mSimUnlockProgressDialog.setMessage(this.mContext.getString(R.string.kg_sim_unlock_progress_dialog_message));
            this.mSimUnlockProgressDialog.setIndeterminate(true);
            this.mSimUnlockProgressDialog.setCancelable(false);
            if (!(this.mContext instanceof Activity)) {
                this.mSimUnlockProgressDialog.getWindow().setType(2009);
            }
        }
        return this.mSimUnlockProgressDialog;
    }

    /* access modifiers changed from: private */
    public Dialog getPukRemainingAttemptsDialog(int i) {
        String pukPasswordErrorMessage = getPukPasswordErrorMessage(i, false);
        AlertDialog alertDialog = this.mRemainingAttemptsDialog;
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setMessage(pukPasswordErrorMessage);
            builder.setCancelable(false);
            builder.setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null);
            this.mRemainingAttemptsDialog = builder.create();
            this.mRemainingAttemptsDialog.getWindow().setType(2009);
        } else {
            alertDialog.setMessage(pukPasswordErrorMessage);
        }
        return this.mRemainingAttemptsDialog;
    }

    /* access modifiers changed from: private */
    public boolean checkPuk() {
        if (this.mPasswordEntry.getText().length() != 8) {
            return false;
        }
        this.mPukText = this.mPasswordEntry.getText();
        return true;
    }

    /* access modifiers changed from: private */
    public boolean checkPin() {
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
    public void updateSim() {
        getSimUnlockProgressDialog().show();
        if (this.mCheckSimPukThread == null) {
            this.mCheckSimPukThread = new CheckSimPuk(this.mPukText, this.mPinText, this.mSubId) {
                /* access modifiers changed from: package-private */
                public void onSimLockChangedResponse(final int i, final int i2) {
                    KeyguardSimPukView.this.post(new Runnable() {
                        public void run() {
                            if (KeyguardSimPukView.this.mSimUnlockProgressDialog != null) {
                                KeyguardSimPukView.this.mSimUnlockProgressDialog.hide();
                            }
                            KeyguardSimPukView.this.resetPasswordText(true, i != 0);
                            if (i == 0) {
                                KeyguardUpdateMonitor.getInstance(KeyguardSimPukView.this.getContext()).reportSimUnlocked(KeyguardSimPukView.this.mSubId);
                                int unused = KeyguardSimPukView.this.mRemainingAttempts = -1;
                                boolean unused2 = KeyguardSimPukView.this.mShowDefaultMessage = true;
                                KeyguardSecurityCallback keyguardSecurityCallback = KeyguardSimPukView.this.mCallback;
                                if (keyguardSecurityCallback != null) {
                                    keyguardSecurityCallback.dismiss(true, KeyguardUpdateMonitor.getCurrentUser());
                                }
                            } else {
                                boolean unused3 = KeyguardSimPukView.this.mShowDefaultMessage = false;
                                if (i == 1) {
                                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                                    keyguardSimPukView.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPukView.getPukPasswordErrorMessage(i2, false));
                                    int i = i2;
                                    if (i <= 2) {
                                        KeyguardSimPukView.this.getPukRemainingAttemptsDialog(i).show();
                                    } else {
                                        KeyguardSimPukView keyguardSimPukView2 = KeyguardSimPukView.this;
                                        keyguardSimPukView2.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPukView2.getPukPasswordErrorMessage(i, false));
                                    }
                                } else {
                                    KeyguardSimPukView keyguardSimPukView3 = KeyguardSimPukView.this;
                                    keyguardSimPukView3.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPukView3.getContext().getString(R.string.kg_password_puk_failed));
                                }
                                Log.d("KeyguardSimPukView", "verifyPasswordAndUnlock  UpdateSim.onSimCheckResponse:  attemptsRemaining=" + i2);
                                KeyguardSimPukView.this.mStateMachine.reset();
                            }
                            CheckSimPuk unused4 = KeyguardSimPukView.this.mCheckSimPukThread = null;
                        }
                    });
                }
            };
            this.mCheckSimPukThread.start();
        }
    }

    /* access modifiers changed from: protected */
    public void verifyPasswordAndUnlock() {
        this.mStateMachine.next();
    }

    /* access modifiers changed from: private */
    public void showDefaultMessage() {
        String str;
        CharSequence charSequence;
        int i = this.mRemainingAttempts;
        if (i >= 0) {
            this.mSecurityMessageDisplay.setMessage((CharSequence) getPukPasswordErrorMessage(i, true));
            return;
        }
        int simCount = TelephonyManager.getDefault().getSimCount();
        Resources resources = getResources();
        int i2 = -1;
        if (simCount < 2) {
            str = resources.getString(R.string.kg_puk_enter_puk_hint);
        } else {
            SubscriptionInfo subscriptionInfoForSubId = KeyguardUpdateMonitor.getInstance(this.mContext).getSubscriptionInfoForSubId(this.mSubId);
            if (subscriptionInfoForSubId != null) {
                charSequence = subscriptionInfoForSubId.getDisplayName();
            } else {
                charSequence = "";
            }
            str = resources.getString(R.string.kg_puk_enter_puk_hint_multi, new Object[]{charSequence});
            if (subscriptionInfoForSubId != null) {
                i2 = subscriptionInfoForSubId.getIconTint();
            }
        }
        this.mSecurityMessageDisplay.setMessage((CharSequence) str);
        this.mSimImageView.setImageTintList(ColorStateList.valueOf(i2));
        new CheckSimPuk("", "", this.mSubId) {
            /* access modifiers changed from: package-private */
            public void onSimLockChangedResponse(int i, int i2) {
                Log.d("KeyguardSimPukView", "onSimCheckResponse  dummy One result" + i + " attemptsRemaining=" + i2);
                if (i2 >= 0) {
                    int unused = KeyguardSimPukView.this.mRemainingAttempts = i2;
                    KeyguardSimPukView keyguardSimPukView = KeyguardSimPukView.this;
                    keyguardSimPukView.mSecurityMessageDisplay.setMessage((CharSequence) keyguardSimPukView.getPukPasswordErrorMessage(i2, true));
                }
            }
        }.start();
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
}
