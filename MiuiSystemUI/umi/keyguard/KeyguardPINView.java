package com.android.keyguard;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.security.MiuiLockPatternUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import com.android.keyguard.PasswordTextView;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.animation.DisappearAnimationUtils;
import com.android.systemui.miui.anim.PhysicBasedInterpolator;
import com.android.systemui.plugins.R;
import miui.view.animation.SineEaseInOutInterpolator;

public class KeyguardPINView extends KeyguardPinBasedInputView implements PasswordTextView.TextChangeListener {
    /* access modifiers changed from: private */
    public boolean mAppearAnimating;
    private final AppearAnimationUtils mAppearAnimationUtils;
    private ViewGroup mContainer;
    /* access modifiers changed from: private */
    public boolean mDisappearAnimatePending;
    private final DisappearAnimationUtils mDisappearAnimationUtils;
    private final DisappearAnimationUtils mDisappearAnimationUtilsLocked;
    /* access modifiers changed from: private */
    public Runnable mDisappearFinishRunnable;
    private int mDisappearYTranslation;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private int mPasswordLength;
    private ViewGroup mRow0;
    private ViewGroup mRow1;
    private ViewGroup mRow2;
    private ViewGroup mRow3;
    private ViewGroup mRow4;
    private final int mScreenHeight;
    private View[][] mViews;

    /* access modifiers changed from: protected */
    public int getPasswordTextViewId() {
        return R.id.pinEntry;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardPINView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardPINView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAppearAnimationUtils = new AppearAnimationUtils(context);
        Context context2 = context;
        this.mDisappearAnimationUtils = new DisappearAnimationUtils(context2, 125, 0.6f, 0.45f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearAnimationUtilsLocked = new DisappearAnimationUtils(context2, 187, 0.6f, 0.45f, AnimationUtils.loadInterpolator(this.mContext, 17563663));
        this.mDisappearYTranslation = getResources().getDimensionPixelSize(R.dimen.miui_disappear_y_translation);
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mScreenHeight = context.getResources().getConfiguration().screenHeightDp;
        this.mPasswordLength = (int) new MiuiLockPatternUtils(context).getLockPasswordLength(KeyguardUpdateMonitor.getCurrentUser());
        if (this.mPasswordLength < 4) {
            this.mPasswordLength = 4;
            Log.e("KeyguardPINView", "get password length = " + this.mPasswordLength);
        }
    }

    /* access modifiers changed from: protected */
    public void resetState() {
        super.resetState();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPasswordEntry.removeTextChangedListener();
    }

    public void onPause() {
        super.onPause();
        dismissFodView();
    }

    public void onResume(int i) {
        super.onResume(i);
        showFodViewIfNeed();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPasswordEntry.addTextChangedListener(this);
        this.mContainer = (ViewGroup) findViewById(R.id.container);
        this.mRow0 = (ViewGroup) findViewById(R.id.row0);
        this.mRow1 = (ViewGroup) findViewById(R.id.row1);
        this.mRow2 = (ViewGroup) findViewById(R.id.row2);
        this.mRow3 = (ViewGroup) findViewById(R.id.row3);
        this.mRow4 = (ViewGroup) findViewById(R.id.row4);
        this.mViews = new View[][]{new View[]{this.mRow0, null, null}, new View[]{findViewById(R.id.key1), findViewById(R.id.key2), findViewById(R.id.key3)}, new View[]{findViewById(R.id.key4), findViewById(R.id.key5), findViewById(R.id.key6)}, new View[]{findViewById(R.id.key7), findViewById(R.id.key8), findViewById(R.id.key9)}, new View[]{null, findViewById(R.id.key0), null}, new View[]{findViewById(R.id.emergency_call_button), null, findViewById(R.id.delete_button), findViewById(R.id.back_button)}};
        setPositionForFod();
    }

    public void startAppearAnimation() {
        this.mKeyguardBouncerMessageView.setVisibility(0);
        setAlpha(1.0f);
        this.mAppearAnimating = true;
        this.mDisappearAnimatePending = false;
        setTranslationY((float) (this.mScreenHeight / 2));
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 500, 0.0f, new PhysicBasedInterpolator(0.99f, 0.3f));
        this.mAppearAnimationUtils.startAnimation2d(this.mViews, new Runnable() {
            public void run() {
                boolean unused = KeyguardPINView.this.mAppearAnimating = false;
                if (KeyguardPINView.this.mDisappearAnimatePending) {
                    boolean unused2 = KeyguardPINView.this.mDisappearAnimatePending = false;
                    KeyguardPINView keyguardPINView = KeyguardPINView.this;
                    keyguardPINView.startDisappearAnimation(keyguardPINView.mDisappearFinishRunnable);
                }
            }
        });
    }

    public boolean startDisappearAnimation(final Runnable runnable) {
        DisappearAnimationUtils disappearAnimationUtils;
        if (this.mAppearAnimating) {
            this.mDisappearAnimatePending = true;
            this.mDisappearFinishRunnable = runnable;
            return true;
        }
        setTranslationY(0.0f);
        AppearAnimationUtils.startTranslationYAnimation(this, 0, 350, (float) this.mDisappearYTranslation, new SineEaseInOutInterpolator());
        if (this.mKeyguardUpdateMonitor.needsSlowUnlockTransition()) {
            disappearAnimationUtils = this.mDisappearAnimationUtilsLocked;
        } else {
            disappearAnimationUtils = this.mDisappearAnimationUtils;
        }
        disappearAnimationUtils.startAnimation2d(this.mViews, new Runnable() {
            public void run() {
                Log.d("KeyguardPINView", "startDisappearAnimation finish");
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        return true;
    }

    public void onTextChanged(int i) {
        if (i == this.mPasswordLength) {
            verifyPasswordAndUnlock();
        }
        if (i == 0) {
            this.mBackButton.setVisibility(0);
            this.mDeleteButton.setVisibility(8);
            return;
        }
        this.mBackButton.setVisibility(8);
        this.mDeleteButton.setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationFontScaleChanged() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(R.dimen.miui_keyguard_view_eca_text_size);
        this.mEmergencyButton.setTextSize(0, dimensionPixelSize);
        this.mBackButton.setTextSize(0, dimensionPixelSize);
        this.mDeleteButton.setTextSize(0, dimensionPixelSize);
    }

    /* access modifiers changed from: protected */
    public void handleConfigurationOrientationChanged() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_rows_layout_height);
        this.mContainer.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mKeyguardBouncerMessageView.getLayoutParams();
        layoutParams2.topMargin = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_bouncer_message_view_margin_top);
        this.mKeyguardBouncerMessageView.setLayoutParams(layoutParams2);
        setPositionForFod();
    }

    /* access modifiers changed from: protected */
    public void handleWrongPassword() {
        TranslateAnimation translateAnimation = new TranslateAnimation(1, -0.1f, 1, 0.1f, 1, 0.0f, 1, 0.0f);
        translateAnimation.setDuration(30);
        translateAnimation.setRepeatCount(3);
        translateAnimation.setRepeatMode(2);
        this.mPasswordEntry.startAnimation(translateAnimation);
    }

    private void setPositionForFod() {
        if (MiuiKeyguardUtils.isGxzwSensor() && MiuiGxzwManager.getInstance().isShowFodWithPassword()) {
            Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            int max = Math.max(point.x, point.y);
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_rows_layout_height);
            int dimensionPixelOffset2 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_row0_margin_bottom);
            int dimensionPixelOffset3 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_row1_row2_row3_margin_bottom);
            int dimensionPixelOffset4 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_row4_margin_bottom);
            int dimensionPixelOffset5 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_row4_margin_bottom_fod);
            int dimensionPixelOffset6 = getResources().getDimensionPixelOffset(R.dimen.miui_keyguard_pin_view_row5_margin_bottom);
            Rect fodPosition = MiuiGxzwManager.getFodPosition(getContext());
            int height = fodPosition.top + (fodPosition.height() / 2);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContainer.getLayoutParams();
            layoutParams.bottomMargin = ((max - ((((((dimensionPixelOffset - dimensionPixelOffset2) - (dimensionPixelOffset3 * 3)) - dimensionPixelOffset4) - dimensionPixelOffset6) / 6) / 2)) - dimensionPixelOffset6) - height;
            layoutParams.height = dimensionPixelOffset + dimensionPixelOffset5;
            this.mContainer.setLayoutParams(layoutParams);
            LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mRow4.getLayoutParams();
            layoutParams2.bottomMargin = dimensionPixelOffset4 + dimensionPixelOffset5;
            this.mRow4.setLayoutParams(layoutParams2);
        }
    }

    /* access modifiers changed from: protected */
    public void showFodViewIfNeed() {
        super.showFodViewIfNeed();
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword()) {
            if (MiuiGxzwManager.getInstance().isShowFodInBouncer() && this.mRow0.getVisibility() == 0) {
                onShowFodView();
                this.mRow0.setVisibility(4);
                this.mRow1.setVisibility(4);
                this.mRow2.setVisibility(4);
                this.mRow3.setVisibility(4);
                this.mRow4.setVisibility(4);
            } else if (!MiuiGxzwManager.getInstance().isShowFodInBouncer()) {
                MiuiGxzwManager.getInstance().setDimissFodInBouncer(true);
                dismissFodView();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dismissFodView() {
        super.dismissFodView();
        if (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwManager.getInstance().isShowFodWithPassword() && this.mRow0.getVisibility() != 0) {
            onDismissFodView();
            this.mRow0.setVisibility(0);
            this.mRow1.setVisibility(0);
            this.mRow2.setVisibility(0);
            this.mRow3.setVisibility(0);
            this.mRow4.setVisibility(0);
        }
    }

    /* access modifiers changed from: protected */
    public void usePassword() {
        super.usePassword();
        startAppearAnimation();
    }
}
