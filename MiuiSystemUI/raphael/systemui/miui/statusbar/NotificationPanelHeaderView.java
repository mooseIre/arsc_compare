package com.android.systemui.miui.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.HeaderView;

public class NotificationPanelHeaderView extends HeaderView {
    public void onTuningChanged(String str, String str2) {
    }

    public void regionChanged() {
    }

    public void themeChanged() {
    }

    public NotificationPanelHeaderView(Context context) {
        super(context);
    }

    public NotificationPanelHeaderView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public NotificationPanelHeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mShortcut.setImageResource(R.drawable.notification_panel_manage_icon);
        this.mShortcutDestination = -1;
        this.mDateView.setClockMode(3);
        this.mClock.setClockMode(0);
        this.mClock.setShowAmPm(false);
    }
}
