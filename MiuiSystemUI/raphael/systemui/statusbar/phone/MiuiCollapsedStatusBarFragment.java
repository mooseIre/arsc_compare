package com.android.systemui.statusbar.phone;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.android.systemui.C0009R$bool;
import com.android.systemui.C0014R$id;
import com.android.systemui.C0016R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.RegionController;
import com.android.systemui.statusbar.views.NetworkSpeedSplitter;
import com.android.systemui.statusbar.views.NetworkSpeedView;

public class MiuiCollapsedStatusBarFragment extends CollapsedStatusBarFragment implements RegionController.Callback, ControlPanelWindowManager.OnExpandChangeListener {
    private boolean mControlPanelExpand;
    private ControlPanelWindowManager mControlPanelWindowManager;
    private int mDisable1;
    private StatusBarIconController.DarkIconManager mDripLeftDarkIconManager;
    private NetworkSpeedSplitter mDripNetworkSpeedSplitter;
    private NetworkSpeedView mDripNetworkSpeedView;
    private LinearLayout mDripSystemIconArea;
    private String mRegion;
    private View mStatusBarPromptContainer;

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mControlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0016R$layout.miui_status_bar, viewGroup, false);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        PhoneStatusBarView phoneStatusBarView = this.mStatusBar;
        if (phoneStatusBarView instanceof MiuiPhoneStatusBarView) {
            ((MiuiPhoneStatusBarView) phoneStatusBarView).setNotificationIconAreaInnner(notificationInnerAreaView);
        }
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(C0014R$id.centered_icon_area);
        View centeredNotificationAreaView = notificationIconAreaController.getCenteredNotificationAreaView();
        this.mCenteredIconArea = centeredNotificationAreaView;
        if (centeredNotificationAreaView.getParent() != null) {
            ((ViewGroup) this.mCenteredIconArea.getParent()).removeView(this.mCenteredIconArea);
        }
        viewGroup.addView(this.mCenteredIconArea);
        showNotificationIconArea(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void initMiuiViewsOnViewCreated(View view) {
        super.initMiuiViewsOnViewCreated(view);
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0014R$id.drip_left_statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDripLeftDarkIconManager = darkIconManager;
        darkIconManager.setShouldLog(true);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).addIconGroup(this.mDripLeftDarkIconManager);
        this.mDripSystemIconArea = (LinearLayout) this.mStatusBar.findViewById(C0014R$id.drip_left_statusIcons);
        this.mStatusBarPromptContainer = this.mStatusBar.findViewById(C0014R$id.prompt_container);
        this.mDripNetworkSpeedSplitter = (NetworkSpeedSplitter) this.mStatusBar.findViewById(C0014R$id.drip_network_speed_splitter);
        this.mDripNetworkSpeedView = (NetworkSpeedView) this.mStatusBar.findViewById(C0014R$id.drip_network_speed_view);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
        this.mControlPanelExpand = this.mControlPanelWindowManager.isExpand();
        this.mControlPanelWindowManager.addExpandChangeListener(this);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void onDestroyView() {
        super.onDestroyView();
        this.mControlPanelWindowManager.removeExpandChangeListener(this);
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        if (this.mDripLeftDarkIconManager != null) {
            ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).removeIconGroup(this.mDripLeftDarkIconManager);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks, com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void disable(int i, int i2, int i3, boolean z) {
        this.mDisable1 = i2;
        super.disable(i, i2, i3, z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public int adjustDisableFlags(int i) {
        int adjustDisableFlags = super.adjustDisableFlags(i);
        return this.mControlPanelExpand ? 8388608 | 131072 | adjustDisableFlags | 1048576 : adjustDisableFlags;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public int clockHiddenMode() {
        return (this.mDisable1 & 8388608) != 0 ? 8 : 4;
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void hideSystemIconArea(boolean z) {
        super.hideSystemIconArea(z);
        animateHide(this.mDripSystemIconArea, z);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void showSystemIconArea(boolean z) {
        super.showSystemIconArea(z);
        animateShow(this.mDripSystemIconArea, z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void hideMiuiStatusBarPrompt(boolean z) {
        super.hideMiuiStatusBarPrompt(z);
        animateHide(this.mStatusBarPromptContainer, z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void showMiuiStatusBarPrompt(boolean z) {
        super.showMiuiStatusBarPrompt(z);
        animateShow(this.mStatusBarPromptContainer, z);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void hideClock(boolean z) {
        super.hideClock(z);
        hideNetworkSpeedSplitter(clockHiddenMode(), z);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void showClock(boolean z) {
        super.showClock(z);
        showNetworkSpeedSplitter(z);
    }

    public void hideNetworkSpeedSplitter(int i, boolean z) {
        NetworkSpeedSplitter networkSpeedSplitter = this.mDripNetworkSpeedSplitter;
        if (networkSpeedSplitter != null) {
            networkSpeedSplitter.animate().cancel();
            if (!z) {
                this.mDripNetworkSpeedSplitter.setAlpha(0.0f);
            } else {
                this.mDripNetworkSpeedSplitter.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT);
            }
        }
    }

    public void showNetworkSpeedSplitter(boolean z) {
        this.mDripNetworkSpeedSplitter.animate().cancel();
        if (!z) {
            this.mDripNetworkSpeedSplitter.setAlpha(1.0f);
        } else {
            this.mDripNetworkSpeedSplitter.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction(null);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void hideMiuiDripNetworkSpeedView(boolean z) {
        super.hideMiuiDripNetworkSpeedView(z);
        animateHideDripNetworkSpeedView(4, z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void showMiuiDripNetworkSpeedView(boolean z) {
        super.showMiuiDripNetworkSpeedView(z);
        animateShowDripNetworkSpeedView(z);
    }

    public void animateHideDripNetworkSpeedView(int i, boolean z) {
        NetworkSpeedView networkSpeedView = this.mDripNetworkSpeedView;
        if (networkSpeedView != null) {
            networkSpeedView.animate().cancel();
            if (!z) {
                this.mDripNetworkSpeedView.setAlpha(0.0f);
                this.mDripNetworkSpeedView.setVisibilityByDisableInfo(i);
                return;
            }
            this.mDripNetworkSpeedView.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(i) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiCollapsedStatusBarFragment$DnTL_J8vxJTRsAQew0NC20A9ieY */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiCollapsedStatusBarFragment.this.lambda$animateHideDripNetworkSpeedView$0$MiuiCollapsedStatusBarFragment(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateHideDripNetworkSpeedView$0 */
    public /* synthetic */ void lambda$animateHideDripNetworkSpeedView$0$MiuiCollapsedStatusBarFragment(int i) {
        this.mDripNetworkSpeedView.setVisibilityByDisableInfo(i);
    }

    public void animateShowDripNetworkSpeedView(boolean z) {
        NetworkSpeedView networkSpeedView = this.mDripNetworkSpeedView;
        if (networkSpeedView != null) {
            networkSpeedView.animate().cancel();
            this.mDripNetworkSpeedView.setVisibilityByDisableInfo(0);
            if (!z) {
                this.mDripNetworkSpeedView.setAlpha(1.0f);
                return;
            }
            this.mDripNetworkSpeedView.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction(null);
            if (this.mKeyguardStateController.isKeyguardFadingAway()) {
                this.mDripNetworkSpeedView.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.RegionController.Callback
    public void onRegionChanged(String str) {
        this.mRegion = str;
        initOperatorName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void initOperatorName() {
        PhoneStatusBarView phoneStatusBarView;
        if (this.mOperatorNameFrame == null && (phoneStatusBarView = this.mStatusBar) != null) {
            if (phoneStatusBarView.getContext().getResources().getBoolean(C0009R$bool.config_showOperatorNameInStatusBar) || "SA".equals(this.mRegion)) {
                Log.d("CollapsedStatusBarFragment", "initOperatorName: ");
                this.mOperatorNameFrame = ((ViewStub) this.mStatusBar.findViewById(C0014R$id.operator_name)).inflate();
            }
        }
    }

    @Override // com.android.systemui.controlcenter.phone.ControlPanelWindowManager.OnExpandChangeListener
    public void onExpandChange(boolean z) {
        if (this.mControlPanelExpand != z) {
            this.mControlPanelExpand = z;
            if (getContext() != null && getContext().getDisplay() != null) {
                this.mCommandQueue.recomputeDisableFlags(getContext().getDisplay().getDisplayId(), false);
            }
        }
    }
}
