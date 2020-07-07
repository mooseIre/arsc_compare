package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.statusbar.policy.DemoModeController;
import java.util.Iterator;
import java.util.LinkedList;

public class NetworkSpeedView extends TextView implements DemoMode, DarkIconDispatcher.DarkReceiver {
    private DarkIconDispatcher mDarkIconDispatcher = ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class));
    private final DemoModeController.DemoModeCallback mDemoCallback = new DemoModeController.DemoModeCallback() {
        public void onDemoModeChanged(String str, Bundle bundle) {
            NetworkSpeedView.this.dispatchDemoCommand(str, bundle);
        }
    };
    private boolean mDemoMode;
    private boolean mForceHide;
    private boolean mIsDriveMode;
    private int mMaxLength;
    private NetworkSpeedController mNetworkSpeedController = ((NetworkSpeedController) Dependency.get(NetworkSpeedController.class));
    private boolean mNotch;
    private LinkedList<NetworkSpeedVisibilityListener> mVisibilityListeners = new LinkedList<>();

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
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this.mDemoCallback);
    }

    /* access modifiers changed from: protected */
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        int ceil = TextUtils.isEmpty(charSequence) ? 0 : (int) Math.ceil((double) getPaint().measureText(charSequence.toString()));
        if (this.mMaxLength != ceil) {
            this.mMaxLength = ceil;
            setWidth(this.mMaxLength);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((DemoModeController) Dependency.get(DemoModeController.class)).removeCallback(this.mDemoCallback);
        this.mNetworkSpeedController.removeFromViewList(this);
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

    public boolean isDriveMode() {
        return this.mIsDriveMode;
    }

    public boolean isNotch() {
        return this.mNotch;
    }

    public boolean isForceHide() {
        return this.mForceHide;
    }

    public boolean isDemoMode() {
        return this.mDemoMode;
    }

    public void setDriveMode(boolean z) {
        this.mIsDriveMode = z;
        if (this.mIsDriveMode) {
            setVisibility(8);
        } else {
            this.mNetworkSpeedController.postUpdateNetworkSpeed();
        }
    }

    public void setNotch(boolean z) {
        this.mNotch = z;
        if (z) {
            setVisibility(8);
        } else {
            this.mNetworkSpeedController.postUpdateNetworkSpeed();
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        Log.d("demo_mode", "NetworkSpeedView mDemoMode = " + this.mDemoMode + ", command = " + str);
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            setVisibility(8);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            this.mNetworkSpeedController.postUpdateNetworkSpeed();
        }
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        Iterator it = this.mVisibilityListeners.iterator();
        while (it.hasNext()) {
            ((NetworkSpeedVisibilityListener) it.next()).onNetworkSpeedVisibilityChanged(isShown());
        }
    }

    public void addVisibilityListener(NetworkSpeedVisibilityListener networkSpeedVisibilityListener) {
        this.mVisibilityListeners.add(networkSpeedVisibilityListener);
    }

    public void removeVisibilityListener(NetworkSpeedVisibilityListener networkSpeedVisibilityListener) {
        this.mVisibilityListeners.remove(networkSpeedVisibilityListener);
    }
}
