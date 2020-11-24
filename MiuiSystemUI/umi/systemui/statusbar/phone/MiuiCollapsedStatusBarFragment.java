package com.android.systemui.statusbar.phone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0005R$array;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.views.NetworkSpeedSplitter;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import java.util.ArrayList;
import java.util.Arrays;

public class MiuiCollapsedStatusBarFragment extends CollapsedStatusBarFragment {
    private StatusBarIconController.DarkIconManager mDripLeftDarkIconManager;
    private NetworkSpeedSplitter mDripNetworkSpeedSplitter;
    private NetworkSpeedView mDripNetworkSpeedView;
    private StatusBarIconController.DarkIconManager mDripRightDarkIconManager;
    private LinearLayout mDripSystemIconArea;
    private View mStatusBarPromptContainer;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0014R$layout.miui_status_bar, viewGroup, false);
    }

    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        this.mStatusBar.setNotificationIconAreaInnner(notificationInnerAreaView);
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(C0012R$id.centered_icon_area);
        View centeredNotificationAreaView = notificationIconAreaController.getCenteredNotificationAreaView();
        this.mCenteredIconArea = centeredNotificationAreaView;
        if (centeredNotificationAreaView.getParent() != null) {
            ((ViewGroup) this.mCenteredIconArea.getParent()).removeView(this.mCenteredIconArea);
        }
        viewGroup.addView(this.mCenteredIconArea);
        showNotificationIconArea(false);
    }

    /* access modifiers changed from: protected */
    public void initMiuiViewsOnViewCreated(View view) {
        super.initMiuiViewsOnViewCreated(view);
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0012R$id.drip_left_statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDripLeftDarkIconManager = darkIconManager;
        darkIconManager.setShouldLog(true);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).addIconGroup(this.mDripLeftDarkIconManager);
        ArrayList arrayList = new ArrayList(Arrays.asList(getContext().getResources().getStringArray(C0005R$array.config_drip_right_block_statusBarIcons)));
        StatusBarIconController.DarkIconManager darkIconManager2 = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0012R$id.drip_right_statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDripRightDarkIconManager = darkIconManager2;
        darkIconManager2.setShouldLog(true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDripRightDarkIconManager, arrayList);
        this.mDripSystemIconArea = (LinearLayout) this.mStatusBar.findViewById(C0012R$id.drip_left_statusIcons);
        this.mStatusBarPromptContainer = this.mStatusBar.findViewById(C0012R$id.prompt_container);
        this.mDripNetworkSpeedSplitter = (NetworkSpeedSplitter) this.mStatusBar.findViewById(C0012R$id.drip_network_speed_splitter);
        this.mDripNetworkSpeedView = (NetworkSpeedView) this.mStatusBar.findViewById(C0012R$id.drip_network_speed_view);
    }

    public void hideSystemIconArea(boolean z) {
        super.hideSystemIconArea(z);
        animateHide(this.mDripSystemIconArea, z);
    }

    public void showSystemIconArea(boolean z) {
        super.showSystemIconArea(z);
        animateShow(this.mDripSystemIconArea, z);
    }

    /* access modifiers changed from: protected */
    public void hideMiuiStatusBarPrompt(boolean z) {
        super.hideMiuiStatusBarPrompt(z);
        animateHide(this.mStatusBarPromptContainer, z);
    }

    /* access modifiers changed from: protected */
    public void showMiuiStatusBarPrompt(boolean z) {
        super.showMiuiStatusBarPrompt(z);
        animateShow(this.mStatusBarPromptContainer, z);
    }

    public void hideClock(boolean z) {
        super.hideClock(z);
        hideNetworkSpeedSplitter(clockHiddenMode(), z);
    }

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
        this.mDripNetworkSpeedSplitter.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction((Runnable) null);
        if (this.mKeyguardStateController.isKeyguardFadingAway()) {
            this.mDripNetworkSpeedSplitter.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
        }
    }

    /* access modifiers changed from: protected */
    public void hideMiuiDripNetworkSpeedView(boolean z) {
        super.hideMiuiDripNetworkSpeedView(z);
        animateHideDripNetworkSpeedView(4, z);
    }

    /* access modifiers changed from: protected */
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
            this.mDripNetworkSpeedView.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction((Runnable) null);
            if (this.mKeyguardStateController.isKeyguardFadingAway()) {
                this.mDripNetworkSpeedView.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
            }
        }
    }
}
