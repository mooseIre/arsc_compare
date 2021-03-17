package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.statusbar.policy.RegionController;
import com.miui.systemui.util.CommonUtil;

public class MiuiNotificationShadeHeader extends RelativeLayout implements ControlPanelController.UseControlPanelChangeListener, RegionController.Callback, View.OnLayoutChangeListener {
    private boolean mExpanded;
    private MiuiHeaderView mHeaderView;
    private Configuration mLastConfiguration;
    private int mOrientation;
    private QSContainerImpl mQsContainerImpl;
    private boolean mUseControlPanel;

    public MiuiNotificationShadeHeader(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLastConfiguration = new Configuration(context.getResources().getConfiguration());
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mUseControlPanel = ((ControlPanelController) Dependency.get(ControlPanelController.class)).useControlPanel();
        this.mOrientation = getResources().getConfiguration().orientation;
        addOnLayoutChangeListener(this);
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
        if (CommonUtil.isThemeResourcesChanged(this.mLastConfiguration.updateFrom(configuration), configuration.extraConfig.themeChangedFlags)) {
            themeChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).addCallback((ControlPanelController.UseControlPanelChangeListener) this);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).removeCallback((ControlPanelController.UseControlPanelChangeListener) this);
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            updateEverything();
        }
    }

    public void setQSContainer(QSContainerImpl qSContainerImpl) {
        this.mQsContainerImpl = qSContainerImpl;
    }

    public void updateEverything() {
        post(new Runnable() {
            /* class com.android.systemui.qs.MiuiNotificationShadeHeader.AnonymousClass1 */

            public void run() {
                MiuiNotificationShadeHeader.this.setClickable(false);
            }
        });
    }

    private void updateHeaderView() {
        removeAllViews();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!this.mUseControlPanel || this.mOrientation != 1) {
            MiuiHeaderView miuiHeaderView = (MiuiHeaderView) LayoutInflater.from(((RelativeLayout) this).mContext).inflate(C0017R$layout.miui_ns_qs_header_view, (ViewGroup) this, false);
            this.mHeaderView = miuiHeaderView;
            if (this.mOrientation != 1) {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(17105489);
            } else if (((MiuiQSHeaderView) miuiHeaderView).showCarrier()) {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.notch_expanded_header_height_with_carrier);
            } else {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.notch_expanded_header_height);
            }
        } else {
            this.mHeaderView = (MiuiHeaderView) LayoutInflater.from(((RelativeLayout) this).mContext).inflate(C0017R$layout.miui_ns_notification_header_view, (ViewGroup) this, false);
            layoutParams.height = -2;
        }
        setLayoutParams(layoutParams);
        addView(this.mHeaderView);
        themeChanged();
        regionChanged();
    }

    public void themeChanged() {
        MiuiHeaderView miuiHeaderView = this.mHeaderView;
        if (miuiHeaderView != null) {
            miuiHeaderView.themeChanged();
        }
    }

    public void regionChanged() {
        MiuiHeaderView miuiHeaderView = this.mHeaderView;
        if (miuiHeaderView != null) {
            miuiHeaderView.regionChanged();
        }
    }

    @Override // com.android.systemui.controlcenter.phone.ControlPanelController.UseControlPanelChangeListener
    public void onUseControlPanelChange(boolean z) {
        if (this.mUseControlPanel != z) {
            this.mUseControlPanel = z;
            updateHeaderView();
        }
    }

    @Override // com.android.systemui.statusbar.policy.RegionController.Callback
    public void onRegionChanged(String str) {
        regionChanged();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        QSContainerImpl qSContainerImpl = this.mQsContainerImpl;
        if (qSContainerImpl != null && i4 - i2 != i8 - i6) {
            qSContainerImpl.updateResources();
        }
    }
}
