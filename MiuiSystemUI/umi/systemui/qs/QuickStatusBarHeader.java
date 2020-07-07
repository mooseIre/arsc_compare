package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.systemui.CustomizedUtils;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.widget.ClipEdgeLinearLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.HeaderView;
import com.android.systemui.statusbar.SignalClusterView;

public class QuickStatusBarHeader extends RelativeLayout implements ControlPanelController.UseControlPanelChangeListener {
    private boolean mExpanded;
    private HeaderView mHeaderView;
    private int mOrientation;
    private boolean mUseControlPanel;

    public QuickStatusBarHeader(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mUseControlPanel = ((ControlPanelController) Dependency.get(ControlPanelController.class)).useControlPanel();
        this.mOrientation = getResources().getConfiguration().orientation;
        updateHeaderView();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.orientation;
        if (i != this.mOrientation) {
            this.mOrientation = i;
            updateHeaderView();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).addCallback((ControlPanelController.UseControlPanelChangeListener) this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).removeCallback((ControlPanelController.UseControlPanelChangeListener) this);
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            updateEverything();
        }
    }

    public void updateEverything() {
        post(new Runnable() {
            public void run() {
                QuickStatusBarHeader.this.setClickable(false);
            }
        });
    }

    private void updateHeaderView() {
        removeAllViews();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!this.mUseControlPanel || this.mOrientation != 1) {
            HeaderView headerView = (HeaderView) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_panel_header_view, this, false);
            this.mHeaderView = headerView;
            ClipEdgeLinearLayout clipEdgeLinearLayout = (ClipEdgeLinearLayout) headerView.findViewById(R.id.system_icons);
            clipEdgeLinearLayout.setClipEdge(true);
            ((SignalClusterView) clipEdgeLinearLayout.findViewById(R.id.signal_cluster)).setForceNormalType();
            layoutParams.height = CustomizedUtils.getNotchExpandedHeaderViewHeight(getContext(), getResources().getDimensionPixelSize(R.dimen.notch_expanded_header_height));
        } else {
            this.mHeaderView = (HeaderView) LayoutInflater.from(this.mContext).inflate(R.layout.notification_panel_header_view, this, false);
            layoutParams.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_panel_header_height);
        }
        setLayoutParams(layoutParams);
        addView(this.mHeaderView);
    }

    public void themeChanged() {
        HeaderView headerView = this.mHeaderView;
        if (headerView != null) {
            headerView.themeChanged();
        }
    }

    public void regionChanged() {
        HeaderView headerView = this.mHeaderView;
        if (headerView != null) {
            headerView.regionChanged();
        }
    }

    public void onUseControlPanelChange(boolean z) {
        if (this.mUseControlPanel != z) {
            this.mUseControlPanel = z;
            updateHeaderView();
        }
    }

    public void onSuperSaveModeChange(boolean z) {
        this.mHeaderView.updateShortCutVisible(!z);
    }
}
