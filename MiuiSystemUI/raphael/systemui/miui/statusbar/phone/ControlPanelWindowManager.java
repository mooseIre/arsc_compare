package com.android.systemui.miui.statusbar.phone;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceControlCompat;
import android.view.WindowManager;
import com.android.systemui.Application;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.Utils;
import java.util.HashSet;
import java.util.Iterator;

public class ControlPanelWindowManager {
    private boolean added = false;
    private Context mContext;
    private ControlPanelWindowView mControlPanel;
    private WindowManager.LayoutParams mLp;
    private WindowManager.LayoutParams mLpChanged;
    private HashSet<OnExpandChangeListener> mOnExpandChangeListeners;
    private WindowManager mWindowManager;

    public interface OnExpandChangeListener {
        void onExpandChange(boolean z);
    }

    public ControlPanelWindowManager(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mOnExpandChangeListeners = new HashSet<>();
    }

    public void addControlPanel(ControlPanelWindowView controlPanelWindowView) {
        if (!hasAdded()) {
            this.mLp = new WindowManager.LayoutParams(-1, 0, 0, 0, Build.VERSION.SDK_INT > 29 ? 2017 : 2014, -2121989848, -3);
            WindowManager.LayoutParams layoutParams = this.mLp;
            layoutParams.privateFlags |= 64;
            layoutParams.setTitle("control_center");
            WindowManager.LayoutParams layoutParams2 = this.mLp;
            layoutParams2.systemUiVisibility = 1792;
            layoutParams2.extraFlags |= 32768;
            int i = Build.VERSION.SDK_INT;
            try {
                this.mWindowManager.addView(controlPanelWindowView, this.mLp);
            } catch (Exception unused) {
            }
            this.mLpChanged = new WindowManager.LayoutParams();
            this.mLpChanged.copyFrom(this.mLp);
            this.mControlPanel = controlPanelWindowView;
            this.mControlPanel.setControlPanelWindowManager(this);
            this.added = true;
        }
    }

    public void removeControlPanel() {
        if (hasAdded()) {
            this.mWindowManager.removeView(this.mControlPanel);
            this.mControlPanel = null;
            this.added = false;
        }
    }

    public void setBlurRatio(float f) {
        applyBlurRatio(f);
    }

    public void collapsePanel(boolean z) {
        collapsePanel(z, 0);
    }

    public void collapsePanel(boolean z, int i) {
        if (hasAdded()) {
            this.mControlPanel.collapsePanel(z, i);
        }
    }

    public void onExpandChange(boolean z) {
        Log.d("ControlPanelWindowManager", "onExpandChange: " + z);
        if (z) {
            this.mControlPanel.setVisibility(0);
            WindowManager.LayoutParams layoutParams = this.mLpChanged;
            layoutParams.height = -1;
            layoutParams.flags &= -9;
            layoutParams.flags |= 131072;
            Utils.updateFsgState(this.mContext, "typefrom_status_bar_expansion", true);
            setEnableForceLightNavigationHandle(true);
        } else {
            this.mControlPanel.setVisibility(8);
            WindowManager.LayoutParams layoutParams2 = this.mLpChanged;
            layoutParams2.height = 0;
            layoutParams2.flags = 8 | layoutParams2.flags;
            layoutParams2.flags &= -131073;
            StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
            if (statusBar == null || statusBar.isQSFullyCollapsed()) {
                Utils.updateFsgState(this.mContext, "typefrom_status_bar_expansion", false);
            }
            setEnableForceLightNavigationHandle(false);
        }
        apply();
        notifyListeners(z);
    }

    private void setEnableForceLightNavigationHandle(boolean z) {
        StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        if (statusBar != null && statusBar.getNavigationBarView() != null && statusBar.getNavigationBarView().getNavigationHandle() != null) {
            statusBar.getNavigationBarView().getNavigationHandle().setEnableForceLight(z);
        }
    }

    public void setNotTouchable(boolean z) {
        if (z) {
            this.mLpChanged.flags |= 16;
        } else {
            this.mLpChanged.flags &= -17;
        }
        apply();
    }

    public boolean hasAdded() {
        return this.added;
    }

    public void addExpandChangeListener(OnExpandChangeListener onExpandChangeListener) {
        this.mOnExpandChangeListeners.add(onExpandChangeListener);
    }

    public void removeExpandChangeListener(OnExpandChangeListener onExpandChangeListener) {
        this.mOnExpandChangeListeners.remove(onExpandChangeListener);
    }

    private void notifyListeners(boolean z) {
        Iterator<OnExpandChangeListener> it = this.mOnExpandChangeListeners.iterator();
        while (it.hasNext()) {
            it.next().onExpandChange(z);
        }
    }

    private void apply() {
        if (this.mLp.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this.mControlPanel, this.mLp);
        }
    }

    private void applyBlurRatio(float f) {
        if (hasAdded()) {
            Log.d("ControlPanelWindowManager", "setBlurRatio: " + f);
            SurfaceControlCompat.setBlur(this.mLpChanged, this.mControlPanel.getViewRootImpl(), f, 0);
            apply();
        }
    }
}
