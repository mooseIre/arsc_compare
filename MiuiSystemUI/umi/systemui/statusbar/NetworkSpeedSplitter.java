package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NetworkSpeedView;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;

public class NetworkSpeedSplitter extends TextView implements Clock.ClockVisibilityListener, NetworkSpeedView.NetworkSpeedVisibilityListener, DarkIconDispatcher.DarkReceiver {
    private boolean mClockVisible;
    private DarkIconDispatcher mDarkIconDispatcher;
    private boolean mNetworkSpeedVisible;

    public NetworkSpeedSplitter(Context context) {
        super(context);
        init();
    }

    public NetworkSpeedSplitter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public NetworkSpeedSplitter(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        setText(" | ");
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
    }

    public void onClockVisibilityChanged(boolean z) {
        this.mClockVisible = z;
        setVisibility(isVisible() ? 0 : 8);
    }

    public void onNetworkSpeedVisibilityChanged(boolean z) {
        this.mNetworkSpeedVisible = z;
        setVisibility(isVisible() ? 0 : 8);
    }

    private boolean isVisible() {
        return this.mClockVisible && this.mNetworkSpeedVisible;
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        boolean showCtsSpecifiedColor = Util.showCtsSpecifiedColor();
        int i2 = R.color.status_bar_textColor;
        if (showCtsSpecifiedColor) {
            Resources resources = getResources();
            if (DarkIconDispatcherHelper.inDarkMode(rect, this, f)) {
                i2 = R.color.status_bar_icon_text_color_dark_mode_cts;
            }
            setTextColor(resources.getColor(i2));
        } else if (this.mDarkIconDispatcher.useTint()) {
            setTextColor(DarkIconDispatcherHelper.getTint(rect, this, i));
        } else {
            Resources resources2 = getResources();
            if (DarkIconDispatcherHelper.inDarkMode(rect, this, f)) {
                i2 = R.color.status_bar_textColor_darkmode;
            }
            setTextColor(resources2.getColor(i2));
        }
    }
}
