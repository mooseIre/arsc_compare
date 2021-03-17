package com.android.systemui.statusbar.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.statusbar.views.NetworkSpeedView;

public class NetworkSpeedSplitter extends TextView implements MiuiClock.ClockVisibilityListener, NetworkSpeedView.NetworkSpeedVisibilityListener, DarkIconDispatcher.DarkReceiver {
    private boolean mClockVisible;
    private boolean mNetworkSpeedVisible;
    private int mVisibilityByDisableInfo;

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
    public void onClockVisibilityChanged(boolean z) {
        this.mClockVisible = z;
        updateVisibility();
    }

    @Override // com.android.systemui.statusbar.views.NetworkSpeedView.NetworkSpeedVisibilityListener
    public void onNetworkSpeedVisibilityChanged(boolean z) {
        this.mNetworkSpeedVisible = z;
        updateVisibility();
    }

    private void updateVisibility() {
        setVisibility((!this.mClockVisible || !this.mNetworkSpeedVisible) ? 8 : this.mVisibilityByDisableInfo);
    }

    public void setVisibilityByDisableInfo(int i) {
        this.mVisibilityByDisableInfo = i;
        updateVisibility();
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
