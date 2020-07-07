package com.android.systemui.statusbar;

import com.android.internal.statusbar.StatusBarIcon;

public class ExpandedIcon extends StatusBarIcon {
    public ExpandedIcon(StatusBarIcon statusBarIcon) {
        super(statusBarIcon.user, statusBarIcon.pkg, statusBarIcon.icon, statusBarIcon.iconLevel, statusBarIcon.number, statusBarIcon.contentDescription);
        this.visible = statusBarIcon.visible;
    }

    public ExpandedIcon clone() {
        return new ExpandedIcon(ExpandedIcon.super.clone());
    }
}
