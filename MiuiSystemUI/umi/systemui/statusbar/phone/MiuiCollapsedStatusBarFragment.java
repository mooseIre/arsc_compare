package com.android.systemui.statusbar.phone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.RegionController;
import com.android.systemui.statusbar.views.DarkCarrierText;
import com.android.systemui.statusbar.views.NetworkSpeedSplitter;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import java.util.ArrayList;
import java.util.Arrays;

public class MiuiCollapsedStatusBarFragment extends CollapsedStatusBarFragment implements RegionController.Callback {
    private StatusBarIconController.DarkIconManager mDripLeftDarkIconManager;
    private NetworkSpeedSplitter mDripNetworkSpeedSplitter;
    private NetworkSpeedView mDripNetworkSpeedView;
    private StatusBarIconController.DarkIconManager mDripRightDarkIconManager;
    private LinearLayout mDripSystemIconArea;
    private String mRegion;
    private DarkCarrierText mStatusBarCarrier;
    private View mStatusBarPromptContainer;

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0017R$layout.miui_status_bar, viewGroup, false);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        PhoneStatusBarView phoneStatusBarView = this.mStatusBar;
        if (phoneStatusBarView instanceof MiuiPhoneStatusBarView) {
            ((MiuiPhoneStatusBarView) phoneStatusBarView).setNotificationIconAreaInnner(notificationInnerAreaView);
        }
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(C0015R$id.centered_icon_area);
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
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0015R$id.drip_left_statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDripLeftDarkIconManager = darkIconManager;
        darkIconManager.setShouldLog(true);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).addIconGroup(this.mDripLeftDarkIconManager);
        ArrayList arrayList = new ArrayList(Arrays.asList(getContext().getResources().getStringArray(C0008R$array.config_drip_right_block_statusBarIcons)));
        StatusBarIconController.DarkIconManager darkIconManager2 = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0015R$id.drip_right_statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDripRightDarkIconManager = darkIconManager2;
        darkIconManager2.setShouldLog(true);
        this.mDripRightDarkIconManager.setDrip(true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDripRightDarkIconManager, arrayList);
        this.mDripSystemIconArea = (LinearLayout) this.mStatusBar.findViewById(C0015R$id.drip_left_statusIcons);
        this.mStatusBarPromptContainer = this.mStatusBar.findViewById(C0015R$id.prompt_container);
        this.mDripNetworkSpeedSplitter = (NetworkSpeedSplitter) this.mStatusBar.findViewById(C0015R$id.drip_network_speed_splitter);
        this.mDripNetworkSpeedView = (NetworkSpeedView) this.mStatusBar.findViewById(C0015R$id.drip_network_speed_view);
        ((RegionController) Dependency.get(RegionController.class)).addCallback(this);
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mStatusBarCarrier = (DarkCarrierText) view.findViewById(C0015R$id.status_bar_carrier_text);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver(this.mStatusBarCarrier);
        updateStatusBarCarrierVisibility();
    }

    @Override // com.android.systemui.statusbar.phone.CollapsedStatusBarFragment
    public void onDestroyView() {
        super.onDestroyView();
        ((RegionController) Dependency.get(RegionController.class)).removeCallback(this);
        if (this.mDripLeftDarkIconManager != null) {
            ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).removeIconGroup(this.mDripLeftDarkIconManager);
        }
        if (this.mDripRightDarkIconManager != null) {
            ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDripRightDarkIconManager);
        }
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver(this.mStatusBarCarrier);
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
                this.mDripNetworkSpeedSplitter.setVisibilityByDisableInfo(i);
                return;
            }
            this.mDripNetworkSpeedSplitter.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(i) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiCollapsedStatusBarFragment$XgjLBVG_k9i0Qm6JCAVkLt9_8E */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiCollapsedStatusBarFragment.this.lambda$hideNetworkSpeedSplitter$0$MiuiCollapsedStatusBarFragment(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideNetworkSpeedSplitter$0 */
    public /* synthetic */ void lambda$hideNetworkSpeedSplitter$0$MiuiCollapsedStatusBarFragment(int i) {
        this.mDripNetworkSpeedSplitter.setVisibilityByDisableInfo(i);
    }

    public void showNetworkSpeedSplitter(boolean z) {
        this.mDripNetworkSpeedSplitter.animate().cancel();
        this.mDripNetworkSpeedSplitter.setVisibilityByDisableInfo(0);
        if (!z) {
            this.mDripNetworkSpeedSplitter.setAlpha(1.0f);
            return;
        }
        this.mDripNetworkSpeedSplitter.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction(null);
        if (this.mKeyguardStateController.isKeyguardFadingAway()) {
            this.mDripNetworkSpeedSplitter.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
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
                /* class com.android.systemui.statusbar.phone.$$Lambda$MiuiCollapsedStatusBarFragment$21ZiEwIBLGqXEQcYfaOr5QmZJI */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiCollapsedStatusBarFragment.this.lambda$animateHideDripNetworkSpeedView$1$MiuiCollapsedStatusBarFragment(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateHideDripNetworkSpeedView$1 */
    public /* synthetic */ void lambda$animateHideDripNetworkSpeedView$1$MiuiCollapsedStatusBarFragment(int i) {
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
        updateStatusBarCarrierVisibility();
    }

    /* access modifiers changed from: protected */
    public void updateStatusBarCarrierVisibility() {
        DarkCarrierText darkCarrierText = this.mStatusBarCarrier;
        if (darkCarrierText != null) {
            darkCarrierText.setVisibility((darkCarrierText.getContext().getResources().getBoolean(C0010R$bool.config_showOperatorNameInStatusBar) || "SA".equals(this.mRegion)) ? 0 : 8);
        }
    }
}
