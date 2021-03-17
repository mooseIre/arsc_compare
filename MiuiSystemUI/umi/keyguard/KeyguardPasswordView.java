package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.UserHandle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.internal.widget.LockscreenCredential;
import com.android.internal.widget.TextViewInputDisabler;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.widget.MiuiKeyBoardView;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.miui.systemui.anim.PhysicBasedInterpolator;
import miui.os.Build;
import miui.view.animation.SineEaseInOutInterpolator;

public class KeyguardPasswordView extends KeyguardAbsKeyInputView implements KeyguardSecurityView {
    private boolean mAppearAnimating;
    private boolean mDisappearAnimatePending;
    private Runnable mDisappearFinishRunnable;
    private final int mDisappearYTranslation;
    private Space mEmptySpace;
    private MiuiKeyBoardView mKeyboardView;
    private ViewGroup mKeyboardViewLayout;
    private Interpolator mLinearOutSlowInInterpolator;
    private EditText mPasswordEntry;
    private TextViewInputDisabler mPasswordEntryDisabler;
    private final int mScreenHeight;

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public boolean needsInput() {
        return true;
    }

    public KeyguardPasswordView(Context context) {
        this(context, null);
    }

    public KeyguardPasswordView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        new AppearAnimationUtils(context);
        new DisappearAnimationUtils(context, 125, 0.6f, 0.45f, AnimationUtils.loadInterpolator(((LinearLayout) this).mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(C0012R$dimen.miui_disappear_y_translation);
        this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        AnimationUtils.loadInterpolator(context, 17563663);
        this.mScreenHeight = context.getResources().getConfiguration().screenHeightDp;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetState() {
        this.mPasswordEntry.setTextOperationUser(UserHandle.of(KeyguardUpdateMonitor.getCurrentUser()));
        setPasswordEntryEnabled(true);
        setPasswordEntryInputEnabled(true);
        if (!this.mResumed || !this.mPasswordEntry.isVisibleToUser()) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public int getPasswordTextViewId() {
        return C0015R$id.passwordEntry;
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public void onResume(int i) {
        super.onResume(i);
        post(new Runnable() {
            /* class com.android.keyguard.KeyguardPasswordView.AnonymousClass1 */

            public void run() {
                if (KeyguardPasswordView.this.isShown() && KeyguardPasswordView.this.mPasswordEntry.isEnabled()) {
                    KeyguardPasswordView.this.mPasswordEntry.requestFocus();
                }
            }
        });
        this.mPasswordEntry.setHint(C0021R$string.input_password_hint_text);
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public void onPause() {
        super.onPause();
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public void reset() {
        super.reset();
        this.mPasswordEntry.requestFocus();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView, com.android.keyguard.MiuiKeyguardPasswordView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPasswordEntry = (EditText) findViewById(getPasswordTextViewId());
        this.mPasswordEntryDisabler = new TextViewInputDisabler(this.mPasswordEntry);
        this.mPasswordEntry.setKeyListener(TextKeyListener.getInstance());
        this.mPasswordEntry.setInputType(0);
        this.mPasswordEntry.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.KeyguardPasswordView.AnonymousClass2 */

            public void onClick(View view) {
                KeyguardPasswordView.this.mCallback.userActivity();
            }
        });
        this.mPasswordEntry.setSelected(true);
        this.mPasswordEntry.requestFocus();
        MiuiKeyBoardView miuiKeyBoardView = (MiuiKeyBoardView) findViewById(C0015R$id.mixed_password_keyboard_view);
        this.mKeyboardView = miuiKeyBoardView;
        miuiKeyBoardView.addKeyboardListener(new MiuiKeyBoardView.OnKeyboardActionListener() {
            /* class com.android.keyguard.KeyguardPasswordView.AnonymousClass3 */

            @Override // com.android.keyguard.widget.MiuiKeyBoardView.OnKeyboardActionListener
            public void onText(CharSequence charSequence) {
                if (TextUtils.isEmpty(KeyguardPasswordView.this.mPasswordEntry.getText().toString())) {
                    KeyguardPasswordView.this.mPasswordEntry.setHint(C0021R$string.input_password_hint_text);
                }
                KeyguardPasswordView.this.mCallback.userActivity();
                KeyguardPasswordView.this.mPasswordEntry.append(charSequence);
            }

            @Override // com.android.keyguard.widget.MiuiKeyBoardView.OnKeyboardActionListener
            public void onKeyBoardDelete() {
                KeyguardPasswordView.this.mCallback.userActivity();
                Editable text = KeyguardPasswordView.this.mPasswordEntry.getText();
                if (!TextUtils.isEmpty(text.toString())) {
                    text.delete(text.length() - 1, text.length());
                }
            }

            @Override // com.android.keyguard.widget.MiuiKeyBoardView.OnKeyboardActionListener
            public void onKeyBoardOK() {
                KeyguardPasswordView.this.mCallback.userActivity();
                KeyguardPasswordView.this.verifyPasswordAndUnlock();
            }
        });
        Space space = (Space) findViewById(C0015R$id.empty_space);
        this.mEmptySpace = space;
        space.setVisibility(8);
        this.mKeyboardViewLayout = (ViewGroup) findViewById(C0015R$id.mixed_password_keyboard_view_layout);
        setPositionForFod();
    }

    /* access modifiers changed from: protected */
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        return this.mPasswordEntry.requestFocus(i, rect);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void resetPasswordText(boolean z, boolean z2) {
        this.mPasswordEntry.setText("");
        if (z2) {
            this.mPasswordEntry.setHint(C0021R$string.wrong_password);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public LockscreenCredential getEnteredCredential() {
        return LockscreenCredential.createPasswordOrNone(this.mPasswordEntry.getText());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryEnabled(boolean z) {
        this.mPasswordEntry.setEnabled(z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.KeyguardAbsKeyInputView
    public void setPasswordEntryInputEnabled(boolean z) {
        this.mPasswordEntryDisabler.setInputEnabled(z);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        RenderNodeAnimator renderNodeAnimator;
        RenderNodeAnimator renderNodeAnimator2;
        this.mKeyguardBouncerMessageView.setVisibility(0);
        setAlpha(1.0f);
        this.mAppearAnimating = true;
        this.mDisappearAnimatePending = false;
        setTranslationY((float) (this.mScreenHeight / 2));
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, new PhysicBasedInterpolator(0.99f, 0.3f));
        if (this.mKeyboardViewLayout.isHardwareAccelerated()) {
            ViewGroup viewGroup = this.mKeyboardViewLayout;
            viewGroup.setTranslationY((float) viewGroup.getHeight());
            renderNodeAnimator2 = new RenderNodeAnimator(1, 0.0f);
            renderNodeAnimator2.setTarget(this.mKeyboardViewLayout);
            this.mKeyboardViewLayout.setScaleY(2.0f);
            renderNodeAnimator = new RenderNodeAnimator(4, 1.0f);
            renderNodeAnimator.setTarget(this.mKeyboardViewLayout);
        } else {
            ViewGroup viewGroup2 = this.mKeyboardViewLayout;
            renderNodeAnimator2 = ObjectAnimator.ofFloat(viewGroup2, View.TRANSLATION_Y, (float) viewGroup2.getHeight(), 0.0f);
            renderNodeAnimator = ObjectAnimator.ofFloat(this.mKeyboardViewLayout, View.SCALE_Y, 2.0f, 1.0f);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(renderNodeAnimator2, renderNodeAnimator);
        animatorSet.setDuration(500L);
        animatorSet.setInterpolator(this.mLinearOutSlowInInterpolator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.keyguard.KeyguardPasswordView.AnonymousClass4 */

            public void onAnimationEnd(Animator animator) {
                KeyguardPasswordView.this.mAppearAnimating = false;
                if (KeyguardPasswordView.this.mDisappearAnimatePending) {
                    KeyguardPasswordView.this.mDisappearAnimatePending = false;
                    KeyguardPasswordView keyguardPasswordView = KeyguardPasswordView.this;
                    keyguardPasswordView.startDisappearAnimation(keyguardPasswordView.mDisappearFinishRunnable);
                }
            }
        });
        animatorSet.start();
    }

    @Override // com.android.keyguard.KeyguardSecurityView, com.android.keyguard.KeyguardAbsKeyInputView
    public boolean startDisappearAnimation(final Runnable runnable) {
        RenderNodeAnimator renderNodeAnimator;
        RenderNodeAnimator renderNodeAnimator2;
        if (this.mAppearAnimating) {
            this.mDisappearAnimatePending = true;
            this.mDisappearFinishRunnable = runnable;
            return true;
        }
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 350, (float) this.mDisappearYTranslation, new SineEaseInOutInterpolator());
        if (this.mKeyboardViewLayout.isHardwareAccelerated()) {
            renderNodeAnimator2 = new RenderNodeAnimator(1, (float) (this.mDisappearYTranslation / 6));
            renderNodeAnimator2.setTarget(this.mKeyboardViewLayout);
            renderNodeAnimator = new RenderNodeAnimator(11, 0.0f);
            renderNodeAnimator.setTarget(this);
        } else {
            ViewGroup viewGroup = this.mKeyboardViewLayout;
            renderNodeAnimator2 = ObjectAnimator.ofFloat(viewGroup, View.TRANSLATION_Y, viewGroup.getTranslationY(), (float) (this.mDisappearYTranslation / 6));
            renderNodeAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, 1.0f, 0.0f);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(renderNodeAnimator2, renderNodeAnimator);
        animatorSet.setDuration(350L);
        animatorSet.setInterpolator(new SineEaseInOutInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter(this) {
            /* class com.android.keyguard.KeyguardPasswordView.AnonymousClass5 */

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                Log.d("KeyguardPasswordView", "startDisappearAnimation finish");
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        animatorSet.start();
        this.mEmergencyButton.setEnabled(false);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void handleConfigurationFontScaleChanged() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(C0012R$dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mBackButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void handleConfigurationOrientationChanged() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mPasswordEntry.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_password_margin_bottom);
        this.mPasswordEntry.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mKeyguardBouncerMessageView.getLayoutParams();
        layoutParams2.topMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_bouncer_message_view_margin_top);
        this.mKeyguardBouncerMessageView.setLayoutParams(layoutParams2);
        LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mKeyboardViewLayout.getLayoutParams();
        layoutParams3.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_layout_height);
        this.mKeyboardViewLayout.setLayoutParams(layoutParams3);
        LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mKeyboardView.getLayoutParams();
        layoutParams4.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_key_board_view_height);
        this.mKeyboardView.setLayoutParams(layoutParams4);
        setPositionForFod();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.MiuiKeyguardPasswordView
    public void handleConfigurationSmallWidthChanged() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mPasswordEntry.getLayoutParams();
        layoutParams.bottomMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_password_margin_bottom);
        layoutParams.width = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_password_entry_width);
        layoutParams.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_password_entry_height);
        this.mPasswordEntry.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mKeyguardBouncerMessageView.getLayoutParams();
        layoutParams2.topMargin = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_bouncer_message_view_margin_top);
        this.mKeyguardBouncerMessageView.setLayoutParams(layoutParams2);
        LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mKeyboardViewLayout.getLayoutParams();
        layoutParams3.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_layout_height);
        this.mKeyboardViewLayout.setLayoutParams(layoutParams3);
        LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mKeyboardView.getLayoutParams();
        layoutParams4.height = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_key_board_view_height);
        this.mKeyboardView.setLayoutParams(layoutParams4);
        setPositionForFod();
    }

    private void setPositionForFod() {
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            int max = Math.max(point.x, point.y);
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_key_board_view_height);
            int dimensionPixelOffset2 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_eca_fod_margin_top);
            int dimensionPixelOffset3 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_eca_margin_bottom);
            int dimensionPixelOffset4 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_layout_padingTop);
            int dimensionPixelOffset5 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_password_margin_bottom);
            int dimensionPixelOffset6 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_password_entry_fod_margin);
            int dimensionPixelOffset7 = (((((getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_layout_height) + dimensionPixelOffset2) + dimensionPixelOffset3) - dimensionPixelOffset) - dimensionPixelOffset4) - dimensionPixelOffset2) - dimensionPixelOffset3;
            int dimensionPixelOffset8 = getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_em_btm_height);
            int i = (max - (dimensionPixelOffset7 / 2)) - dimensionPixelOffset3;
            Rect fodPosition = MiuiGxzwManager.getFodPosition(getContext());
            int height = fodPosition.top + (fodPosition.height() / 2);
            int dimensionPixelOffset9 = (((max - dimensionPixelOffset7) - dimensionPixelOffset3) - fodPosition.bottom) - getResources().getDimensionPixelOffset(C0012R$dimen.miui_keyguard_password_view_em_fod_top_margin);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mKeyboardViewLayout.getLayoutParams();
            if (MiuiKeyguardUtils.isGlobalAndFingerprintEnable()) {
                layoutParams.bottomMargin = dimensionPixelOffset9;
                layoutParams.height = layoutParams.height + dimensionPixelOffset2 + dimensionPixelOffset3 + ((i - height) - dimensionPixelOffset9);
            } else {
                layoutParams.bottomMargin = i - height;
                layoutParams.height = layoutParams.height + dimensionPixelOffset2 + dimensionPixelOffset3;
            }
            this.mKeyboardViewLayout.setLayoutParams(layoutParams);
            if (MiuiKeyguardUtils.isGlobalAndFingerprintEnable()) {
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mEmergencyButton.getLayoutParams();
                int i2 = dimensionPixelOffset6 * 2;
                layoutParams2.topMargin = (((i - height) - dimensionPixelOffset9) + dimensionPixelOffset2) - i2;
                layoutParams2.height = dimensionPixelOffset8;
                this.mEmergencyButton.setLayoutParams(layoutParams2);
                LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mBackButton.getLayoutParams();
                layoutParams3.topMargin = dimensionPixelOffset2 - i2;
                layoutParams3.height = dimensionPixelOffset7;
                this.mBackButton.setLayoutParams(layoutParams3);
                View findViewById = findViewById(C0015R$id.passwordEntry);
                LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) findViewById.getLayoutParams();
                layoutParams4.bottomMargin = ((dimensionPixelOffset5 + dimensionPixelOffset6) - dimensionPixelOffset2) - dimensionPixelOffset3;
                findViewById.setLayoutParams(layoutParams4);
                return;
            }
            View findViewById2 = findViewById(C0015R$id.keyguard_selector_fade_container);
            LinearLayout.LayoutParams layoutParams5 = (LinearLayout.LayoutParams) findViewById2.getLayoutParams();
            layoutParams5.topMargin = dimensionPixelOffset2;
            findViewById2.setLayoutParams(layoutParams5);
            LinearLayout.LayoutParams layoutParams6 = (LinearLayout.LayoutParams) this.mEmergencyButton.getLayoutParams();
            layoutParams6.height = dimensionPixelOffset8;
            this.mEmergencyButton.setLayoutParams(layoutParams6);
            View findViewById3 = findViewById(C0015R$id.passwordEntry);
            LinearLayout.LayoutParams layoutParams7 = (LinearLayout.LayoutParams) findViewById3.getLayoutParams();
            layoutParams7.bottomMargin = ((dimensionPixelOffset5 + dimensionPixelOffset6) - dimensionPixelOffset2) - dimensionPixelOffset3;
            findViewById3.setLayoutParams(layoutParams7);
            if (!Build.IS_INTERNATIONAL_BUILD) {
                this.mEmptySpace.setVisibility(0);
            }
        }
    }
}
