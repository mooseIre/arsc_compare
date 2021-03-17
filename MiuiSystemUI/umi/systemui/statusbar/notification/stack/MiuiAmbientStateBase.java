package com.android.systemui.statusbar.notification.stack;

/* compiled from: NotificationStackScrollLayoutExt.kt */
public class MiuiAmbientStateBase {
    private boolean isNCSwitching;
    private boolean panelAppeared;
    private boolean panelStretching;
    private boolean panelStretchingFromHeadsUp;
    private float springLength;
    private int stackScrollLayoutHeight;
    private int staticTopPadding;

    public final void setQsExpansionEnabled(boolean z) {
    }

    public final boolean getPanelStretching() {
        return this.panelStretching;
    }

    public final void setPanelStretching(boolean z) {
        this.panelStretching = z;
    }

    public final boolean getPanelStretchingFromHeadsUp() {
        return this.panelStretchingFromHeadsUp;
    }

    public final void setPanelStretchingFromHeadsUp(boolean z) {
        this.panelStretchingFromHeadsUp = z;
    }

    public final boolean getPanelAppeared() {
        return this.panelAppeared;
    }

    public final void setPanelAppeared(boolean z) {
        this.panelAppeared = z;
    }

    public final float getSpringLength() {
        return this.springLength;
    }

    public final void setSpringLength(float f) {
        this.springLength = f;
    }

    public final int getStackScrollLayoutHeight() {
        return this.stackScrollLayoutHeight;
    }

    public final void setStackScrollLayoutHeight(int i) {
        this.stackScrollLayoutHeight = i;
    }

    public final boolean isNCSwitching() {
        return this.isNCSwitching;
    }

    public final void setNCSwitching(boolean z) {
        this.isNCSwitching = z;
    }

    public final int getStaticTopPadding() {
        return this.staticTopPadding;
    }

    public final void setStaticTopPadding(int i) {
        this.staticTopPadding = i;
    }
}
