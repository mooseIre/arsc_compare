package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.miui.systemui.DebugConfig;

public abstract class PanelBar extends FrameLayout {
    public static final boolean DEBUG = DebugConfig.DEBUG_PANEL;
    public static final String TAG = PanelBar.class.getSimpleName();
    private boolean mBouncerShowing;
    private boolean mExpanded;
    PanelViewController mPanel;
    protected float mPanelFraction;
    private int mState = 0;
    private boolean mTracking;

    public void onClosingFinished() {
    }

    public boolean panelEnabled() {
        return true;
    }

    public abstract void panelScrimMinFractionChanged(float f);

    public static final void LOG(String str, Object... objArr) {
        if (DEBUG) {
            Log.v(TAG, String.format(str, objArr));
        }
    }

    public void go(int i) {
        if (DEBUG) {
            LOG("go state: %d -> %d", Integer.valueOf(this.mState), Integer.valueOf(i));
        }
        this.mState = i;
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("panel_bar_super_parcelable", super.onSaveInstanceState());
        bundle.putInt("state", this.mState);
        return bundle;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null || !(parcelable instanceof Bundle)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        Bundle bundle = (Bundle) parcelable;
        super.onRestoreInstanceState(bundle.getParcelable("panel_bar_super_parcelable"));
        if (bundle.containsKey("state")) {
            go(bundle.getInt("state", 0));
        }
    }

    public PanelBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setPanel(PanelViewController panelViewController) {
        this.mPanel = panelViewController;
        panelViewController.setBar(this);
    }

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
        int i = z ? 4 : 0;
        setImportantForAccessibility(i);
        updateVisibility();
        PanelViewController panelViewController = this.mPanel;
        if (panelViewController != null) {
            panelViewController.getView().setImportantForAccessibility(i);
        }
    }

    public float getExpansionFraction() {
        return this.mPanelFraction;
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    /* access modifiers changed from: protected */
    public void updateVisibility() {
        this.mPanel.getView().setVisibility(shouldPanelBeVisible() ? 0 : 4);
    }

    /* access modifiers changed from: protected */
    public boolean shouldPanelBeVisible() {
        return !this.mPanel.isDozing() && (this.mExpanded || this.mBouncerShowing);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!panelEnabled()) {
            if (motionEvent.getAction() == 0) {
                Log.v(TAG, String.format("onTouch: all panels disabled, ignoring touch at (%d,%d)", Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())));
            }
            return false;
        }
        if (motionEvent.getAction() == 0) {
            PanelViewController panelViewController = this.mPanel;
            if (panelViewController == null) {
                Log.v(TAG, String.format("onTouch: no panel for touch at (%d,%d)", Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())));
                return true;
            }
            boolean isEnabled = panelViewController.isEnabled();
            if (DEBUG) {
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(this.mState);
                objArr[1] = panelViewController;
                objArr[2] = isEnabled ? "" : " (disabled)";
                LOG("PanelBar.onTouch: state=%d ACTION_DOWN: panel %s %s", objArr);
            }
            if (!isEnabled) {
                Log.v(TAG, String.format("onTouch: panel (%s) is disabled, ignoring touch at (%d,%d)", panelViewController, Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())));
                return true;
            }
        }
        PanelViewController panelViewController2 = this.mPanel;
        return panelViewController2 == null || panelViewController2.getView().dispatchTouchEvent(motionEvent);
    }

    public void panelExpansionChanged(float f, boolean z) {
        boolean z2;
        if (!Float.isNaN(f)) {
            PanelViewController panelViewController = this.mPanel;
            this.mExpanded = z;
            this.mPanelFraction = f;
            updateVisibility();
            boolean z3 = true;
            if (z) {
                if (this.mState == 0) {
                    go(1);
                    onPanelPeeked();
                }
                if (panelViewController.getExpandedFraction() < 1.0f) {
                    z3 = false;
                }
                z2 = false;
            } else {
                z2 = true;
                z3 = false;
            }
            if (z3 && !this.mTracking) {
                go(2);
                onPanelFullyOpened();
            } else if (z2 && !this.mTracking && this.mState != 0) {
                go(0);
                onPanelCollapsed();
            }
        } else {
            throw new IllegalArgumentException("frac cannot be NaN");
        }
    }

    public void collapsePanel(boolean z, boolean z2, float f) {
        boolean z3;
        PanelViewController panelViewController = this.mPanel;
        if (!z || panelViewController.isFullyCollapsed()) {
            panelViewController.resetViews(false);
            panelViewController.setExpandedFraction(0.0f);
            panelViewController.cancelPeek();
            z3 = false;
        } else {
            panelViewController.collapse(z2, f);
            z3 = true;
        }
        if (DEBUG) {
            LOG("collapsePanel: animate=%s waiting=%s", Boolean.valueOf(z), Boolean.valueOf(z3));
        }
        if (!z3 && this.mState != 0) {
            go(0);
            onPanelCollapsed();
        }
    }

    public void onPanelPeeked() {
        if (DEBUG) {
            LOG("onPanelPeeked", new Object[0]);
        }
    }

    public boolean isClosed() {
        return this.mState == 0;
    }

    public void onPanelCollapsed() {
        if (DEBUG) {
            LOG("onPanelCollapsed", new Object[0]);
        }
    }

    public void onPanelFullyOpened() {
        if (DEBUG) {
            LOG("onPanelFullyOpened", new Object[0]);
        }
    }

    public void onTrackingStarted() {
        this.mTracking = true;
    }

    public void onTrackingStopped(boolean z) {
        this.mTracking = false;
    }

    public void onExpandingFinished() {
        if (DEBUG) {
            LOG("onExpandingFinished", new Object[0]);
        }
    }
}
