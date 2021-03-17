package com.android.systemui.controlcenter.phone;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.MiuiNotificationShadePolicy;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.miui.systemui.util.BlurUtil;
import java.util.HashSet;
import java.util.Iterator;

public class ControlPanelWindowManager implements OnHeadsUpChangedListener {
    private boolean added = false;
    private Context mContext;
    private ControlPanelWindowView mControlPanel;
    private ControlPanelController mControlPanelController;
    private float mDownX;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mIsExpand = false;
    private boolean mIsHeadsUp = false;
    private boolean mIsRowPinned = false;
    private WindowManager.LayoutParams mLp;
    private WindowManager.LayoutParams mLpChanged;
    private MiuiNotificationShadePolicy mMiuiNotificationShadePolicy;
    private HashSet<OnExpandChangeListener> mOnExpandChangeListeners;
    private StatusBar mStatusBar;
    private StatusBarStateController mStatusBarStateController;
    private boolean mTransToControlPanel = false;
    private WindowManager mWindowManager;

    public interface OnExpandChangeListener {
        void onExpandChange(boolean z);
    }

    public ControlPanelWindowManager(Context context, StatusBar statusBar, ControlPanelController controlPanelController, HeadsUpManagerPhone headsUpManagerPhone, StatusBarStateController statusBarStateController, MiuiNotificationShadePolicy miuiNotificationShadePolicy) {
        this.mContext = context;
        this.mStatusBar = statusBar;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mControlPanelController = controlPanelController;
        this.mStatusBarStateController = statusBarStateController;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mOnExpandChangeListeners = new HashSet<>();
        this.mMiuiNotificationShadePolicy = miuiNotificationShadePolicy;
    }

    public void addControlPanel(ControlPanelWindowView controlPanelWindowView) {
        if (!hasAdded()) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, 0, 0, 0, 2017, -2121989848, -3);
            this.mLp = layoutParams;
            layoutParams.privateFlags |= 64;
            layoutParams.setTitle("control_center");
            WindowManager.LayoutParams layoutParams2 = this.mLp;
            layoutParams2.systemUiVisibility = 1792;
            layoutParams2.extraFlags |= 32768;
            layoutParams2.setFitInsetsTypes(0);
            WindowManager.LayoutParams layoutParams3 = this.mLp;
            layoutParams3.layoutInDisplayCutoutMode = 3;
            try {
                this.mWindowManager.addView(controlPanelWindowView, layoutParams3);
            } catch (Exception unused) {
            }
            WindowManager.LayoutParams layoutParams4 = new WindowManager.LayoutParams();
            this.mLpChanged = layoutParams4;
            layoutParams4.copyFrom(this.mLp);
            this.mControlPanel = controlPanelWindowView;
            controlPanelWindowView.setControlPanelWindowManager(this);
            this.mControlPanel.setControlPanelController(this.mControlPanelController);
            this.added = true;
            this.mHeadsUpManager.addListener(this);
        }
    }

    public void removeControlPanel() {
        if (hasAdded()) {
            this.mWindowManager.removeView(this.mControlPanel);
            this.mControlPanel = null;
            this.added = false;
            this.mHeadsUpManager.removeListener(this);
        }
    }

    public void setBlurRatio(float f) {
        applyBlurRatio(f);
    }

    public void collapsePanel(boolean z) {
        if (hasAdded()) {
            this.mControlPanel.collapsePanel(z);
        }
    }

    public void collapsePanel(boolean z, boolean z2) {
        if (hasAdded()) {
            this.mControlPanel.collapsePanel(z, z2);
        }
    }

    public void onExpandChange(boolean z) {
        Log.d("ControlPanelWindowManager", "onExpandChange: " + z + "," + this.mIsExpand);
        if (z && !this.mIsExpand) {
            this.mIsExpand = true;
            this.mControlPanel.setVisibility(0);
            WindowManager.LayoutParams layoutParams = this.mLpChanged;
            layoutParams.height = -1;
            int i = layoutParams.flags & -9;
            layoutParams.flags = i;
            layoutParams.flags = i | 131072;
        } else if (!z && this.mIsExpand) {
            this.mIsExpand = false;
            this.mControlPanel.setVisibility(8);
            WindowManager.LayoutParams layoutParams2 = this.mLpChanged;
            layoutParams2.height = 0;
            int i2 = layoutParams2.flags | 8;
            layoutParams2.flags = i2;
            layoutParams2.flags = i2 & -131073;
        }
        updateFsgState(z);
        apply();
        notifyListeners(z);
    }

    public void updateFsgState(boolean z) {
        boolean z2 = true;
        boolean z3 = this.mStatusBarStateController.getState() == 0;
        boolean z4 = this.mStatusBar.getPanelController() == null || (!this.mStatusBar.getPanelController().isExpanding() && !this.mStatusBar.getPanelController().isFullyExpanded());
        if (!z && (!z3 || z4)) {
            z2 = false;
        }
        if (ControlCenter.DEBUG) {
            Log.d("ControlPanelWindowManager", "Fsg state disable:" + z2);
        }
        ControlCenterUtils.updateFsgState(this.mContext, "typefrom_status_bar_expansion", z2);
        if (!z2) {
            this.mMiuiNotificationShadePolicy.notifyFsgChanged(false);
        }
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
            BlurUtil.setBlur(this.mControlPanel.getViewRootImpl(), f, 0);
        }
    }

    public boolean dispatchToControlPanel(MotionEvent motionEvent, float f) {
        if (this.mControlPanelController.isUseControlCenter()) {
            if (motionEvent.getActionMasked() == 0) {
                this.mDownX = motionEvent.getRawX();
            }
            if (this.mDownX > f / 2.0f) {
                return this.mControlPanel.handleMotionEvent(motionEvent, true);
            }
            return false;
        }
        Log.d("ControlPanelWindowManager", " mIsHeadsUp:" + this.mIsHeadsUp + " mIsRowPinned:" + this.mIsRowPinned);
        return false;
    }

    public void dispatchToControlPanel(MotionEvent motionEvent) {
        this.mControlPanel.dispatchTouchEvent(motionEvent);
    }

    public void setTransToControlPanel(boolean z) {
        this.mTransToControlPanel = z;
    }

    public boolean getTransToControlPanel() {
        return this.mTransToControlPanel;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        this.mIsHeadsUp = z;
        this.mIsRowPinned = notificationEntry.isRowPinned();
    }

    public void trimMemory() {
        this.mStatusBar.trimMemory();
    }

    public boolean isPanelExpanded() {
        ControlPanelWindowView controlPanelWindowView = this.mControlPanel;
        if (controlPanelWindowView != null) {
            return controlPanelWindowView.isExpanded();
        }
        return false;
    }

    public void updateNavigationBarSlippery() {
        if (this.mStatusBar.getNavigationBarView() != null) {
            this.mStatusBar.getNavigationBarView().updateSlippery();
        }
    }
}
