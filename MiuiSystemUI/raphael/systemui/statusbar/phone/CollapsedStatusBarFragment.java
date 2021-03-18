package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NetworkController;

public class CollapsedStatusBarFragment extends Fragment implements CommandQueue.Callbacks, StatusBarStateController.StateListener {
    protected View mCenteredIconArea;
    private View mClockView;
    private CommandQueue mCommandQueue;
    private StatusBarIconController.DarkIconManager mDarkIconManager;
    private int mDisabled1;
    protected KeyguardStateController mKeyguardStateController;
    private NetworkController mNetworkController;
    protected View mNotificationIconAreaInner;
    private View mOperatorNameFrame;
    private NetworkController.SignalCallback mSignalCallback = new NetworkController.SignalCallback() {
        /* class com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CollapsedStatusBarFragment.this.mCommandQueue.recomputeDisableFlags(CollapsedStatusBarFragment.this.getContext().getDisplayId(), true);
        }
    };
    protected PhoneStatusBarView mStatusBar;
    private StatusBar mStatusBarComponent;
    private StatusBarStateController mStatusBarStateController;
    private LinearLayout mSystemIconArea;

    /* access modifiers changed from: protected */
    public void hideMiuiDripNetworkSpeedView(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void hideMiuiStatusBarPrompt(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void initMiuiViewsOnViewCreated(View view) {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
    }

    /* access modifiers changed from: protected */
    public void showMiuiDripNetworkSpeedView(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void showMiuiStatusBarPrompt(boolean z) {
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mStatusBarStateController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);
        this.mStatusBarComponent = (StatusBar) Dependency.get(StatusBar.class);
        this.mCommandQueue = (CommandQueue) Dependency.get(CommandQueue.class);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0017R$layout.status_bar, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mStatusBar = (PhoneStatusBarView) view;
        if (bundle != null && bundle.containsKey("panel_state")) {
            this.mStatusBar.restoreHierarchyState(bundle.getSparseParcelableArray("panel_state"));
        }
        StatusBarIconController.DarkIconManager darkIconManager = new StatusBarIconController.DarkIconManager((LinearLayout) view.findViewById(C0015R$id.statusIcons), (CommandQueue) Dependency.get(CommandQueue.class));
        this.mDarkIconManager = darkIconManager;
        darkIconManager.setShouldLog(true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDarkIconManager);
        this.mSystemIconArea = (LinearLayout) this.mStatusBar.findViewById(C0015R$id.system_icon_area);
        initMiuiViewsOnViewCreated(view);
        this.mClockView = this.mStatusBar.findViewById(C0015R$id.clock);
        showSystemIconArea(false);
        showClock(false);
        initEmergencyCryptkeeperText();
        initOperatorName();
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        SparseArray<? extends Parcelable> sparseArray = new SparseArray<>();
        this.mStatusBar.saveHierarchyState(sparseArray);
        bundle.putSparseParcelableArray("panel_state", sparseArray);
    }

    public void onResume() {
        super.onResume();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mStatusBarStateController.addCallback(this);
    }

    public void onPause() {
        super.onPause();
        this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
        this.mStatusBarStateController.removeCallback(this);
    }

    public void onDestroyView() {
        super.onDestroyView();
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDarkIconManager);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            this.mNetworkController.removeCallback(this.mSignalCallback);
        }
    }

    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(C0015R$id.notification_icon_area);
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        if (notificationInnerAreaView.getParent() != null) {
            ((ViewGroup) this.mNotificationIconAreaInner.getParent()).removeView(this.mNotificationIconAreaInner);
        }
        viewGroup.addView(this.mNotificationIconAreaInner);
        ViewGroup viewGroup2 = (ViewGroup) this.mStatusBar.findViewById(C0015R$id.centered_icon_area);
        View centeredNotificationAreaView = notificationIconAreaController.getCenteredNotificationAreaView();
        this.mCenteredIconArea = centeredNotificationAreaView;
        if (centeredNotificationAreaView.getParent() != null) {
            ((ViewGroup) this.mCenteredIconArea.getParent()).removeView(this.mCenteredIconArea);
        }
        viewGroup2.addView(this.mCenteredIconArea);
        showNotificationIconArea(false);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == getContext().getDisplayId()) {
            int adjustDisableFlags = adjustDisableFlags(i2);
            int i4 = this.mDisabled1 ^ adjustDisableFlags;
            this.mDisabled1 = adjustDisableFlags;
            if ((i4 & 1048576) != 0) {
                if ((1048576 & adjustDisableFlags) != 0) {
                    hideSystemIconArea(z);
                    hideOperatorName(z);
                    hideMiuiStatusBarPrompt(z);
                    hideMiuiDripNetworkSpeedView(z);
                } else {
                    showSystemIconArea(z);
                    showOperatorName(z);
                    showMiuiStatusBarPrompt(z);
                    showMiuiDripNetworkSpeedView(z);
                }
            }
            if ((i4 & 131072) != 0) {
                if ((131072 & adjustDisableFlags) != 0) {
                    hideNotificationIconArea(z);
                } else {
                    showNotificationIconArea(z);
                }
            }
            if ((i4 & 8388608) != 0 || this.mClockView.getVisibility() != clockHiddenMode()) {
                if ((adjustDisableFlags & 8388608) != 0) {
                    hideClock(z);
                } else {
                    showClock(z);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int adjustDisableFlags(int i) {
        boolean headsUpShouldBeVisible = this.mStatusBarComponent.headsUpShouldBeVisible();
        if (!this.mKeyguardStateController.isLaunchTransitionFadingAway() && !this.mKeyguardStateController.isKeyguardFadingAway() && shouldHideNotificationIcons() && (this.mStatusBarStateController.getState() != 1 || !headsUpShouldBeVisible)) {
            i = i | 131072 | 1048576 | 8388608;
        }
        NetworkController networkController = this.mNetworkController;
        if (networkController != null && EncryptionHelper.IS_DATA_ENCRYPTED) {
            if (networkController.hasEmergencyCryptKeeperText()) {
                i |= 131072;
            }
            if (!this.mNetworkController.isRadioOn()) {
                i |= 1048576;
            }
        }
        return (!this.mStatusBarStateController.isDozing() || !this.mStatusBarComponent.getPanelController().hasCustomClock()) ? i : i | 9437184;
    }

    private boolean shouldHideNotificationIcons() {
        if ((this.mStatusBar.isClosed() || !this.mStatusBarComponent.hideStatusBarIconsWhenExpanded()) && !this.mStatusBarComponent.hideStatusBarIconsForBouncer()) {
            return false;
        }
        return true;
    }

    public void hideSystemIconArea(boolean z) {
        animateHide(this.mSystemIconArea, z);
    }

    public void showSystemIconArea(boolean z) {
        animateShow(this.mSystemIconArea, z);
    }

    public void hideClock(boolean z) {
        animateHiddenState(this.mClockView, clockHiddenMode(), z);
    }

    public void showClock(boolean z) {
        animateShow(this.mClockView, z);
    }

    /* access modifiers changed from: protected */
    public int clockHiddenMode() {
        if ((this.mDisabled1 & 8388608) == 0 && !this.mStatusBar.isClosed() && !this.mKeyguardStateController.isShowing() && !this.mStatusBarStateController.isDozing()) {
            return 4;
        }
        return 8;
    }

    public void hideNotificationIconArea(boolean z) {
        animateHide(this.mNotificationIconAreaInner, z);
        animateHide(this.mCenteredIconArea, z);
    }

    public void showNotificationIconArea(boolean z) {
        animateShow(this.mNotificationIconAreaInner, z);
        animateShow(this.mCenteredIconArea, z);
    }

    public void hideOperatorName(boolean z) {
        View view = this.mOperatorNameFrame;
        if (view != null) {
            animateHide(view, z);
        }
    }

    public void showOperatorName(boolean z) {
        View view = this.mOperatorNameFrame;
        if (view != null) {
            animateShow(view, z);
        }
    }

    private void animateHiddenState(View view, int i, boolean z) {
        view.animate().cancel();
        if (!z) {
            view.setAlpha(0.0f);
            view.setVisibility(i);
            return;
        }
        view.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(view, i) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$CollapsedStatusBarFragment$27RMKG7VU7GD3kVXbGdyl_3FVd4 */
            public final /* synthetic */ View f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                CollapsedStatusBarFragment.lambda$animateHiddenState$0(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void animateHide(View view, boolean z) {
        animateHiddenState(view, 4, z);
    }

    /* access modifiers changed from: protected */
    public void animateShow(View view, boolean z) {
        view.animate().cancel();
        view.setVisibility(0);
        if (!z) {
            view.setAlpha(1.0f);
            return;
        }
        view.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction(null);
        if (this.mKeyguardStateController.isKeyguardFadingAway()) {
            view.animate().setDuration(this.mKeyguardStateController.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).start();
        }
    }

    private void initEmergencyCryptkeeperText() {
        View findViewById = this.mStatusBar.findViewById(C0015R$id.emergency_cryptkeeper_text);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            if (findViewById != null) {
                ((ViewStub) findViewById).inflate();
            }
            this.mNetworkController.addCallback(this.mSignalCallback);
        } else if (findViewById != null) {
            ((ViewGroup) findViewById.getParent()).removeView(findViewById);
        }
    }

    private void initOperatorName() {
        this.mOperatorNameFrame = ((ViewStub) this.mStatusBar.findViewById(C0015R$id.operator_name)).inflate();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        int displayId = getContext().getDisplayId();
        int i = this.mDisabled1;
        disable(displayId, i, i, false);
    }
}
