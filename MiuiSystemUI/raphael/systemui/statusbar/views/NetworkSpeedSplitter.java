package com.android.systemui.statusbar.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;

public class NetworkSpeedSplitter extends TextView implements MiuiClock.ClockVisibilityListener, NetworkSpeedView.NetworkSpeedVisibilityListener, DarkIconDispatcher.DarkReceiver {
    private int mClockVisibility;
    private int mNetworkSpeedVisibility;

    public NetworkSpeedSplitter(Context context) {
        this(context, null);
    }

    public NetworkSpeedSplitter(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NetworkSpeedSplitter(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        setText(" | ");
    }

    @Override // com.android.systemui.statusbar.policy.MiuiClock.ClockVisibilityListener
    public void onClockVisibilityChanged(int i) {
        this.mClockVisibility = i;
        updateVisibility();
    }

    @Override // com.android.systemui.statusbar.views.NetworkSpeedView.NetworkSpeedVisibilityListener
    public void onNetworkSpeedVisibilityChanged(int i) {
        this.mNetworkSpeedVisibility = i;
        updateVisibility();
    }

    private void updateVisibility() {
        int i = 4;
        if (!(this.mNetworkSpeedVisibility == 4 || this.mClockVisibility == 4)) {
            i = 0;
        }
        if (this.mNetworkSpeedVisibility == 8 || this.mClockVisibility == 8) {
            i = 8;
        }
        setVisibility(i);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        if (z) {
            setTextColor(DarkIconDispatcher.getTint(rect, this, i));
            return;
        }
        if (DarkIconDispatcher.getDarkIntensity(rect, this, f) > 0.0f) {
            i2 = i3;
        }
        setTextColor(i2);
    }
}
