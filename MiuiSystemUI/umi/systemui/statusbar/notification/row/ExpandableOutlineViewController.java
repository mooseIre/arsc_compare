package com.android.systemui.statusbar.notification.row;

public class ExpandableOutlineViewController {
    private final ExpandableViewController mExpandableViewController;

    public ExpandableOutlineViewController(ExpandableOutlineView expandableOutlineView, ExpandableViewController expandableViewController) {
        this.mExpandableViewController = expandableViewController;
    }

    public void init() {
        this.mExpandableViewController.init();
    }
}
