package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import com.android.systemui.Constants;
import java.util.HashSet;
import java.util.Set;

public abstract class PanelBar extends FrameLayout {
    public static final boolean DEBUG = Constants.DEBUG;
    public static final String TAG = PanelBar.class.getSimpleName();
    private PanelBarStateController mController = PanelBarStateController.getInstance();
    PanelView mPanel;
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

    public static final void LOG(Class cls, String str) {
        Log.v(TAG + " " + cls.getSimpleName(), str);
    }

    public void go(int i) {
        if (DEBUG) {
            LOG("go state: %d -> %d", Integer.valueOf(this.mState), Integer.valueOf(i));
        }
        this.mController.update(i);
    }

    public void updateState(int i) {
        this.mState = i;
    }

    public int getState() {
        return this.mState;
    }

    public PanelBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mController.addPanelBar(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mController.removePanelBar(this);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setPanel(PanelView panelView) {
        this.mPanel = panelView;
        panelView.setBar(this);
    }

    public void setBouncerShowing(boolean z) {
        int i = z ? 4 : 0;
        setImportantForAccessibility(i);
        PanelView panelView = this.mPanel;
        if (panelView != null) {
            panelView.setImportantForAccessibility(i);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!panelEnabled()) {
            if (motionEvent.getAction() == 0) {
                Log.v(TAG, String.format("onTouch: all panels disabled, ignoring touch at (%d,%d)", new Object[]{Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())}));
            }
            return false;
        }
        if (motionEvent.getAction() == 0) {
            PanelView panelView = this.mPanel;
            if (panelView == null) {
                Log.w(TAG, String.format("onTouch: no panel for touch at (%d,%d)", new Object[]{Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())}));
                return true;
            }
            boolean isEnabled = panelView.isEnabled();
            if (DEBUG) {
                Object[] objArr = new Object[3];
                objArr[0] = Integer.valueOf(this.mState);
                objArr[1] = panelView;
                objArr[2] = isEnabled ? "" : " (disabled)";
                LOG("PanelBar.onTouch: state=%d ACTION_DOWN: panel %s %s", objArr);
            }
            if (!isEnabled) {
                Log.w(TAG, String.format("onTouch: panel (%s) is disabled, ignoring touch at (%d,%d)", new Object[]{panelView, Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY())}));
                return true;
            }
        }
        PanelView panelView2 = this.mPanel;
        if (panelView2 == null || panelView2.onTouchEvent(motionEvent)) {
            return true;
        }
        return false;
    }

    public void panelExpansionChanged(float f, boolean z) {
        boolean z2;
        PanelView panelView = this.mPanel;
        panelView.setVisibility(z ? 0 : 4);
        boolean z3 = true;
        if (z) {
            if (this.mState == 0) {
                go(1);
                onPanelPeeked();
            }
            if (panelView.getExpandedFraction() < 1.0f) {
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
    }

    public void collapsePanel(boolean z, boolean z2, float f) {
        boolean z3;
        PanelView panelView = this.mPanel;
        if (!z || panelView.isFullyCollapsed()) {
            panelView.resetViews();
            panelView.setExpandedFraction(0.0f);
            panelView.cancelPeek();
            z3 = false;
        } else {
            panelView.collapse(z2, f);
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

    public static class PanelBarStateController {
        private static PanelBarStateController sController = new PanelBarStateController();
        private Set<PanelBar> panelBarSet = new HashSet();

        private PanelBarStateController() {
        }

        public static PanelBarStateController getInstance() {
            return sController;
        }

        public void addPanelBar(PanelBar panelBar) {
            if (!this.panelBarSet.contains(panelBar)) {
                this.panelBarSet.add(panelBar);
            }
        }

        public void update(int i) {
            for (PanelBar updateState : this.panelBarSet) {
                updateState.updateState(i);
            }
        }

        public void removePanelBar(PanelBar panelBar) {
            this.panelBarSet.remove(panelBar);
        }
    }
}
