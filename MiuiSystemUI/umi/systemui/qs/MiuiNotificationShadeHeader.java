package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0014R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.statusbar.policy.RegionController;
import com.miui.systemui.util.CommonUtil;

public class MiuiNotificationShadeHeader extends RelativeLayout implements ControlPanelController.UseControlPanelChangeListener, RegionController.Callback, View.OnLayoutChangeListener {
    private ControlPanelWindowManager mControlPanelWindowManager;
    private boolean mExpanded;
    private MiuiHeaderView mHeaderView;
    private Configuration mLastConfiguration;
    private int mOrientation;
    private QSContainerImpl mQsContainerImpl;
    private int mShowControlHeight = -1;
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
        this.mControlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
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
            public void run() {
                MiuiNotificationShadeHeader.this.setClickable(false);
            }
        });
    }

    private void updateHeaderView() {
        removeAllViews();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        this.mShowControlHeight = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.qs_control_center_header_paddingTop);
        if (!this.mUseControlPanel || this.mOrientation != 1) {
            MiuiHeaderView miuiHeaderView = (MiuiHeaderView) LayoutInflater.from(this.mContext).inflate(C0014R$layout.miui_ns_qs_header_view, this, false);
            this.mHeaderView = miuiHeaderView;
            if (this.mOrientation != 1) {
                layoutParams.height = this.mContext.getResources().getDimensionPixelSize(17105489);
            } else if (((MiuiQSHeaderView) miuiHeaderView).showCarrier()) {
                layoutParams.height = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.notch_expanded_header_height_with_carrier);
            } else {
                layoutParams.height = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.notch_expanded_header_height);
            }
        } else {
            this.mHeaderView = (MiuiHeaderView) LayoutInflater.from(this.mContext).inflate(C0014R$layout.miui_ns_notification_header_view, this, false);
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

    public void onUseControlPanelChange(boolean z) {
        if (this.mUseControlPanel != z) {
            this.mUseControlPanel = z;
            updateHeaderView();
        }
    }

    public void onRegionChanged(String str) {
        regionChanged();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        QSContainerImpl qSContainerImpl = this.mQsContainerImpl;
        if (qSContainerImpl != null && i4 - i2 != i8 - i6) {
            qSContainerImpl.updateResources();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() != 0) {
            this.mControlPanelWindowManager.setTransToControlPanel(false);
            return super.dispatchTouchEvent(motionEvent);
        } else if (motionEvent.getRawY() >= ((float) this.mShowControlHeight) || !this.mControlPanelWindowManager.dispatchToControlPanel(motionEvent, (float) getWidth())) {
            return super.dispatchTouchEvent(motionEvent);
        } else {
            this.mControlPanelWindowManager.setTransToControlPanel(true);
            return true;
        }
    }
}
