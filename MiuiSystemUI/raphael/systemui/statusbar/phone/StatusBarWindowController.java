package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.os.Binder;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;

public class StatusBarWindowController {
    private int mBarHeight = -1;
    private final Context mContext;
    private final State mCurrentState = new State();
    private WindowManager.LayoutParams mLp;
    private final WindowManager.LayoutParams mLpChanged;
    private final Resources mResources;
    private ViewGroup mStatusBarView;
    private final SuperStatusBarViewFactory mSuperStatusBarViewFactory;
    private final WindowManager mWindowManager;

    public StatusBarWindowController(Context context, WindowManager windowManager, SuperStatusBarViewFactory superStatusBarViewFactory, Resources resources) {
        this.mContext = context;
        this.mWindowManager = windowManager;
        this.mSuperStatusBarViewFactory = superStatusBarViewFactory;
        this.mStatusBarView = superStatusBarViewFactory.getStatusBarWindowView();
        this.mLpChanged = new WindowManager.LayoutParams();
        this.mResources = resources;
        if (this.mBarHeight < 0) {
            this.mBarHeight = resources.getDimensionPixelSize(17105489);
        }
    }

    public void refreshStatusBarHeight() {
        int dimensionPixelSize = this.mResources.getDimensionPixelSize(17105489);
        if (this.mBarHeight != dimensionPixelSize) {
            this.mBarHeight = dimensionPixelSize;
            apply(this.mCurrentState);
        }
    }

    public void attach() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, this.mBarHeight, 2000, -2139095032, -3);
        this.mLp = layoutParams;
        layoutParams.privateFlags |= 16777216;
        layoutParams.token = new Binder();
        WindowManager.LayoutParams layoutParams2 = this.mLp;
        layoutParams2.gravity = 48;
        layoutParams2.setFitInsetsTypes(0);
        this.mLp.setTitle("StatusBar");
        this.mLp.packageName = this.mContext.getPackageName();
        WindowManager.LayoutParams layoutParams3 = this.mLp;
        layoutParams3.layoutInDisplayCutoutMode = 3;
        this.mWindowManager.addView(this.mStatusBarView, layoutParams3);
        this.mLpChanged.copyFrom(this.mLp);
    }

    public void setForceStatusBarVisible(boolean z) {
        State state = this.mCurrentState;
        state.mForceStatusBarVisible = z;
        apply(state);
    }

    private void applyHeight() {
        this.mLpChanged.height = this.mBarHeight;
    }

    private void apply(State state) {
        applyForceStatusBarVisibleFlag(state);
        applyHeight();
        WindowManager.LayoutParams layoutParams = this.mLp;
        if (layoutParams != null && layoutParams.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this.mStatusBarView, this.mLp);
        }
    }

    /* access modifiers changed from: private */
    public static class State {
        boolean mForceStatusBarVisible;

        private State() {
        }
    }

    private void applyForceStatusBarVisibleFlag(State state) {
        if (state.mForceStatusBarVisible) {
            this.mLpChanged.privateFlags |= 4096;
            return;
        }
        this.mLpChanged.privateFlags &= -4097;
    }
}
