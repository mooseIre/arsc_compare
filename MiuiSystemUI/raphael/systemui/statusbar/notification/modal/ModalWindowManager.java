package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.miui.systemui.util.BlurUtil;

public class ModalWindowManager {
    private boolean added = false;
    private WindowManager.LayoutParams mLp;
    private WindowManager.LayoutParams mLpChanged;
    private WindowManager mWindowManager;
    private View mWindowView;

    public ModalWindowManager(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public boolean hasAdded() {
        return this.added;
    }

    public void addNotificationModalWindow(View view) {
        if (!hasAdded()) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, 0, 0, 0, 2017, -2121989848, -3);
            this.mLp = layoutParams;
            layoutParams.privateFlags |= 64;
            layoutParams.setTitle("NotificationModalWindowManager");
            WindowManager.LayoutParams layoutParams2 = this.mLp;
            layoutParams2.systemUiVisibility = 1792;
            layoutParams2.extraFlags |= 32768;
            layoutParams2.layoutInDisplayCutoutMode = 3;
            layoutParams2.setFitInsetsTypes(0);
            this.mWindowManager.addView(view, this.mLp);
            WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
            this.mLpChanged = layoutParams3;
            layoutParams3.copyFrom(this.mLp);
            this.mWindowView = view;
            this.added = true;
        }
    }

    public void show() {
        this.mWindowView.setVisibility(0);
        WindowManager.LayoutParams layoutParams = this.mLpChanged;
        layoutParams.height = -1;
        int i = layoutParams.flags & -9;
        layoutParams.flags = i;
        layoutParams.flags = i & -33;
        apply();
    }

    public void clearFocus() {
        this.mLpChanged.flags |= 8;
        apply();
    }

    public void hide() {
        this.mWindowView.setVisibility(8);
        WindowManager.LayoutParams layoutParams = this.mLpChanged;
        layoutParams.height = 0;
        int i = 8 | layoutParams.flags;
        layoutParams.flags = i;
        layoutParams.flags = i & -131073;
        apply();
    }

    public void setBlurRatio(float f) {
        applyBlurRatio(f);
    }

    private void applyBlurRatio(float f) {
        if (hasAdded()) {
            Log.d("ModalWindowManager", "setBlurRatio: " + f);
            BlurUtil.setBlur(this.mWindowView.getViewRootImpl(), f, 0);
            apply();
        }
    }

    private void apply() {
        if (this.mLp.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this.mWindowView, this.mLp);
        }
    }
}
