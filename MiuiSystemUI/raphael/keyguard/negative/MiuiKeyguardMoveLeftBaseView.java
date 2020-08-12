package com.android.keyguard.negative;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBar;

public abstract class MiuiKeyguardMoveLeftBaseView extends RelativeLayout {
    protected StatusBar mStatusBar;

    public abstract boolean hasBackgroundImageDrawable();

    public abstract void initLeftView();

    public abstract boolean isSupportRightMove();

    public abstract void setCustomBackground();

    public abstract void setPanel(NotificationPanelView notificationPanelView);

    public abstract void uploadData();

    public MiuiKeyguardMoveLeftBaseView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardMoveLeftBaseView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }
}
