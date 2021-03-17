package com.android.systemui.statusbar.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.NetworkSpeedController;
import java.util.Iterator;
import java.util.LinkedList;

public class NetworkSpeedView extends TextView implements DarkIconDispatcher.DarkReceiver {
    private NetworkSpeedController mNetworkSpeedController = ((NetworkSpeedController) Dependency.get(NetworkSpeedController.class));
    private int mVisibilityByDisableInfo;
    private LinkedList<NetworkSpeedVisibilityListener> mVisibilityListeners = new LinkedList<>();
    private boolean mVisibleByController;
    private boolean mVisibleByStatusBar;

    public interface NetworkSpeedVisibilityListener {
        void onNetworkSpeedVisibilityChanged(boolean z);
    }

    public NetworkSpeedView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mNetworkSpeedController.addToViewList(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mNetworkSpeedController.removeFromViewList(this);
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

    public void setVisibilityByController(boolean z) {
        this.mVisibleByController = z;
        updateVisibility();
    }

    public void setVisibilityByStatusBar(boolean z) {
        this.mVisibleByStatusBar = z;
        updateVisibility();
    }

    public void setVisibilityByDisableInfo(int i) {
        this.mVisibilityByDisableInfo = i;
        updateVisibility();
    }

    public void updateVisibility() {
        if (!this.mVisibleByController) {
            setVisibility(8);
        } else if (this.mVisibleByStatusBar) {
            setVisibility(this.mVisibilityByDisableInfo);
        } else {
            setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        Iterator<NetworkSpeedVisibilityListener> it = this.mVisibilityListeners.iterator();
        while (it.hasNext()) {
            it.next().onNetworkSpeedVisibilityChanged(isShown());
        }
    }

    public void addVisibilityListener(NetworkSpeedVisibilityListener networkSpeedVisibilityListener) {
        this.mVisibilityListeners.add(networkSpeedVisibilityListener);
    }
}
