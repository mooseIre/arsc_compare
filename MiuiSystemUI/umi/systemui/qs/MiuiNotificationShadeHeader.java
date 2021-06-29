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
import com.android.systemui.statusbar.notification.unimportant.FoldListener;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.policy.RegionController;
import com.miui.systemui.util.CommonUtil;

public class MiuiNotificationShadeHeader extends RelativeLayout implements ControlPanelController.UseControlPanelChangeListener, RegionController.Callback, View.OnLayoutChangeListener, FoldListener {
    private boolean mExpanded;
    private MiuiHeaderView mHeaderView;
    private Configuration mLastConfiguration;
    private int mOrientation;
    private MiuiQSContainer mQsContainerImpl;
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
        FoldManager.Companion.addListener(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).removeCallback((ControlPanelController.UseControlPanelChangeListener) this);
        FoldManager.Companion.removeListener(this);
    }

    public int getNormalHeight() {
        MiuiHeaderView miuiHeaderView = this.mHeaderView;
        return miuiHeaderView == null ? getHeight() : (int) miuiHeaderView.getNormalHeight();
    }

    public int getUnimportantHeight() {
        MiuiHeaderView miuiHeaderView = this.mHeaderView;
        return miuiHeaderView == null ? getHeight() : (int) miuiHeaderView.getUnimportantHeight();
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            updateEverything();
        }
    }

    public void setQSContainer(MiuiQSContainer miuiQSContainer) {
        this.mQsContainerImpl = miuiQSContainer;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateEverything$0 */
    public /* synthetic */ void lambda$updateEverything$0$MiuiNotificationShadeHeader() {
        setClickable(false);
    }

    public void updateEverything() {
        post(new Runnable() {
            /* class com.android.systemui.qs.$$Lambda$MiuiNotificationShadeHeader$rVXRgv1G66m6D_R1f3iaRyrAas */

            public final void run() {
                MiuiNotificationShadeHeader.this.lambda$updateEverything$0$MiuiNotificationShadeHeader();
            }
        });
    }

    private void updateHeaderView() {
        FoldManager.Companion.removeListener(this.mHeaderView);
        removeAllViews();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!this.mUseControlPanel || this.mOrientation != 1) {
            this.mHeaderView = (MiuiHeaderView) LayoutInflater.from(((RelativeLayout) this).mContext).inflate(C0017R$layout.miui_ns_qs_header_view, (ViewGroup) this, false);
        } else {
            this.mHeaderView = (MiuiHeaderView) LayoutInflater.from(((RelativeLayout) this).mContext).inflate(C0017R$layout.miui_ns_notification_header_view, (ViewGroup) this, false);
        }
        resetHeight();
        setLayoutParams(layoutParams);
        addView(this.mHeaderView);
        FoldManager.Companion.addListener(this.mHeaderView);
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
        MiuiQSContainer miuiQSContainer = this.mQsContainerImpl;
        if (miuiQSContainer != null && i4 - i2 != i8 - i6) {
            miuiQSContainer.updateResources();
        }
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void showUnimportantNotifications() {
        resetHeight();
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void resetAll(boolean z) {
        resetHeight();
    }

    private void resetHeight() {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!this.mUseControlPanel || this.mOrientation != 1) {
            if (this.mOrientation != 1) {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(17105490);
            } else if (((MiuiQSHeaderView) this.mHeaderView).showCarrier()) {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.notch_expanded_header_height_with_carrier);
            } else {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.notch_expanded_header_height);
            }
            if (FoldManager.Companion.isShowingUnimportant()) {
                layoutParams.height = ((RelativeLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.unimportant_miui_header_height);
            }
        } else {
            layoutParams.height = -2;
        }
        setLayoutParams(layoutParams);
    }
}
