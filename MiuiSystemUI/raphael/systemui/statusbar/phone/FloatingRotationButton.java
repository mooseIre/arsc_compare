package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.settingslib.Utils;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0017R$layout;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.statusbar.policy.KeyButtonView;

public class FloatingRotationButton implements RotationButton {
    private boolean mCanShow = true;
    private final Context mContext;
    private final int mDiameter;
    private boolean mIsShowing;
    private KeyButtonDrawable mKeyButtonDrawable;
    private final KeyButtonView mKeyButtonView;
    private final int mMargin;
    private RotationButtonController mRotationButtonController;
    private final WindowManager mWindowManager;

    FloatingRotationButton(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        KeyButtonView keyButtonView = (KeyButtonView) LayoutInflater.from(this.mContext).inflate(C0017R$layout.rotate_suggestion, (ViewGroup) null);
        this.mKeyButtonView = keyButtonView;
        keyButtonView.setVisibility(0);
        Resources resources = this.mContext.getResources();
        this.mDiameter = resources.getDimensionPixelSize(C0012R$dimen.floating_rotation_button_diameter);
        this.mMargin = Math.max(resources.getDimensionPixelSize(C0012R$dimen.floating_rotation_button_min_margin), resources.getDimensionPixelSize(C0012R$dimen.rounded_corner_content_padding));
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setRotationButtonController(RotationButtonController rotationButtonController) {
        this.mRotationButtonController = rotationButtonController;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public View getCurrentView() {
        return this.mKeyButtonView;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean show() {
        if (!this.mCanShow || this.mIsShowing) {
            return false;
        }
        this.mIsShowing = true;
        int i = this.mDiameter;
        int i2 = this.mMargin;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i, i, i2, i2, 2024, 8, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("FloatingRotationButton");
        layoutParams.setFitInsetsTypes(0);
        int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
        if (rotation == 0) {
            layoutParams.gravity = 83;
        } else if (rotation == 1) {
            layoutParams.gravity = 85;
        } else if (rotation == 2) {
            layoutParams.gravity = 53;
        } else if (rotation == 3) {
            layoutParams.gravity = 51;
        }
        updateIcon();
        this.mWindowManager.addView(this.mKeyButtonView, layoutParams);
        KeyButtonDrawable keyButtonDrawable = this.mKeyButtonDrawable;
        if (keyButtonDrawable != null && keyButtonDrawable.canAnimate()) {
            this.mKeyButtonDrawable.resetAnimation();
            this.mKeyButtonDrawable.startAnimation();
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean hide() {
        if (!this.mIsShowing) {
            return false;
        }
        this.mWindowManager.removeViewImmediate(this.mKeyButtonView);
        this.mIsShowing = false;
        return true;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public boolean isVisible() {
        return this.mIsShowing;
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void updateIcon() {
        if (this.mIsShowing) {
            KeyButtonDrawable imageDrawable = getImageDrawable();
            this.mKeyButtonDrawable = imageDrawable;
            this.mKeyButtonView.setImageDrawable(imageDrawable);
            this.mKeyButtonDrawable.setCallback(this.mKeyButtonView);
            KeyButtonDrawable keyButtonDrawable = this.mKeyButtonDrawable;
            if (keyButtonDrawable != null && keyButtonDrawable.canAnimate()) {
                this.mKeyButtonDrawable.resetAnimation();
                this.mKeyButtonDrawable.startAnimation();
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mKeyButtonView.setOnClickListener(onClickListener);
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setOnHoverListener(View.OnHoverListener onHoverListener) {
        this.mKeyButtonView.setOnHoverListener(onHoverListener);
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public KeyButtonDrawable getImageDrawable() {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.mContext.getApplicationContext(), this.mRotationButtonController.getStyleRes());
        int themeAttr = Utils.getThemeAttr(contextThemeWrapper, C0009R$attr.darkIconTheme);
        ContextThemeWrapper contextThemeWrapper2 = new ContextThemeWrapper(contextThemeWrapper, Utils.getThemeAttr(contextThemeWrapper, C0009R$attr.lightIconTheme));
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(new ContextThemeWrapper(contextThemeWrapper, themeAttr), C0009R$attr.singleToneColor);
        return KeyButtonDrawable.create(contextThemeWrapper2, Utils.getColorAttrDefaultColor(contextThemeWrapper2, C0009R$attr.singleToneColor), colorAttrDefaultColor, C0013R$drawable.ic_sysbar_rotate_button, false, Color.valueOf((float) Color.red(colorAttrDefaultColor), (float) Color.green(colorAttrDefaultColor), (float) Color.blue(colorAttrDefaultColor), 0.92f));
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setDarkIntensity(float f) {
        this.mKeyButtonView.setDarkIntensity(f);
    }

    @Override // com.android.systemui.statusbar.phone.RotationButton
    public void setCanShowRotationButton(boolean z) {
        this.mCanShow = z;
        if (!z) {
            hide();
        }
    }
}
