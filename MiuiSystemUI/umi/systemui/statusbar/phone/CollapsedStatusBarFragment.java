package com.android.systemui.statusbar.phone;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.SystemUI;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.widget.ClipEdgeLinearLayout;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NetworkSpeedView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import com.android.systemui.statusbar.policy.Clock;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import java.io.PrintWriter;
import java.util.List;
import miui.telephony.SubscriptionInfo;

public class CollapsedStatusBarFragment extends Fragment implements LocationController.LocationChangeCallback, CommandQueue.Callbacks {
    private DarkIconDispatcher.DarkReceiver mBattery;
    public ViewGroup mClockContainer;
    private boolean mClockVisible = true;
    /* access modifiers changed from: private */
    public CollapsedStatusBarFragmentController mController;
    private StatusBarTypeController.CutoutType mCutoutType;
    private int mDisabled1;
    private int mDisabled2;
    private ImageView mGpsDriveMode;
    private boolean mIsRegisted = false;
    private KeyguardMonitor mKeyguardMonitor;
    private LinearLayout mLeftSideLayout;
    private LocationController mLocationController = ((LocationController) Dependency.get(LocationController.class));
    private MiuiStatusBarPromptController mMiuiStatusBarPrompt;
    private FrameLayout mMiuiStatusBarPromptLayout;
    private NetworkController mNetworkController;
    private NetworkSpeedView mNetworkSpeedView;
    public ClipEdgeLinearLayout mNotchLeftEarIcons;
    ArraySet<String> mNotchleftearIconsList = new ArraySet<>();
    private NotificationIconAreaController mNotificationIconAreaController;
    private View mNotificationIconAreaInner;
    private Bundle mSavedInstanceState;
    /* access modifiers changed from: private */
    public boolean mShowBluetooth;
    /* access modifiers changed from: private */
    public boolean mShowLocation;
    private NetworkController.SignalCallback mSignalCallback = new NetworkController.SignalCallback() {
        public void setEthernetIndicators(NetworkController.IconState iconState) {
        }

        public void setIsDefaultDataSim(int i, boolean z) {
        }

        public void setIsImsRegisted(int i, boolean z) {
        }

        public void setMobileDataEnabled(boolean z) {
        }

        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
        }

        public void setNetworkNameVoice(int i, String str) {
        }

        public void setNoSims(boolean z) {
        }

        public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
        }

        public void setSpeechHd(int i, boolean z) {
        }

        public void setSubs(List<SubscriptionInfo> list) {
        }

        public void setVolteNoService(int i, boolean z) {
        }

        public void setVowifi(int i, boolean z) {
        }

        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
        }

        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CollapsedStatusBarFragment.this.mStatusBarComponent.recomputeDisableFlags(true);
        }
    };
    private SignalClusterView mSignalClusterView;
    private PhoneStatusBarView mStatusBar;
    /* access modifiers changed from: private */
    public StatusBar mStatusBarComponent;
    /* access modifiers changed from: private */
    public Clock mStatusClock;
    public LinearLayout mStatusIcons;
    private LinearLayout mSystemIconArea;

    interface HideAnimateCallback {
        void callOnEnd();
    }

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void onLocationSettingsChanged(boolean z) {
    }

    public void onLocationStatusChanged(Intent intent) {
    }

    public void preloadRecentApps() {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    public static CollapsedStatusBarFragment newInstance(StatusBarTypeController.CutoutType cutoutType) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = new CollapsedStatusBarFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("CollapsedStatusBarFragment_cutout_type", cutoutType);
        collapsedStatusBarFragment.setArguments(bundle);
        return collapsedStatusBarFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d("StatusBarFragment", "onCreate");
        this.mKeyguardMonitor = (KeyguardMonitor) Dependency.get(KeyguardMonitor.class);
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mStatusBarComponent = (StatusBar) SystemUI.getComponent(getContext(), StatusBar.class);
        this.mMiuiStatusBarPrompt = (MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.status_bar, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mStatusBar = (PhoneStatusBarView) view;
        this.mSavedInstanceState = bundle;
        if (bundle != null) {
            if (bundle.containsKey("panel_state")) {
                this.mStatusBar.go(bundle.getInt("panel_state"));
            }
            if (bundle.containsKey("clock_visible")) {
                this.mClockVisible = bundle.getBoolean("clock_visible");
            }
        }
        initCutoutType(bundle);
        CollapsedStatusBarFragmentController collapsedStatusBarFragmentController = StatusBarFactory.getInstance().getCollapsedStatusBarFragmentController(this.mCutoutType);
        this.mController = collapsedStatusBarFragmentController;
        collapsedStatusBarFragmentController.init(this);
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(R.id.phone_status_bar_contents_container);
        LayoutInflater.from(viewGroup.getContext()).inflate(this.mController.getLayoutId(), viewGroup, true);
        this.mStatusBar.initBarTransitions();
        this.mBattery = (DarkIconDispatcher.DarkReceiver) this.mStatusBar.findViewById(R.id.battery);
        this.mClockContainer = (ViewGroup) this.mStatusBar.findViewById(R.id.clock_container);
        this.mLeftSideLayout = (LinearLayout) this.mStatusBar.findViewById(R.id.leftside);
        this.mStatusIcons = (LinearLayout) this.mStatusBar.findViewById(R.id.statusIcons);
        this.mSystemIconArea = (LinearLayout) this.mStatusBar.findViewById(R.id.system_icon_area);
        this.mSignalClusterView = (SignalClusterView) this.mStatusBar.findViewById(R.id.signal_cluster);
        this.mNetworkSpeedView = (NetworkSpeedView) this.mSystemIconArea.findViewById(R.id.network_speed_view);
        Clock clock = (Clock) this.mStatusBar.findViewById(R.id.clock);
        this.mStatusClock = clock;
        clock.setVisibility(this.mClockVisible ? 0 : 8);
        this.mGpsDriveMode = (ImageView) this.mStatusBar.findViewById(R.id.gps_drivemode);
        this.mMiuiStatusBarPromptLayout = (FrameLayout) this.mStatusBar.findViewById(R.id.notchLeftEar);
        updateViewsNotch();
        updateViewsCutoutType();
        if (this.mController.isNotch()) {
            updateNotchPromptViewLayout(this.mLeftSideLayout);
            this.mNotchLeftEarIcons = (ClipEdgeLinearLayout) this.mStatusBar.findViewById(R.id.notch_leftear_icons);
        }
        this.mController.initViews(this.mStatusBar);
        showSystemIconArea(false);
        initEmergencyCryptkeeperText();
        registerListeners();
    }

    private void initCutoutType(Bundle bundle) {
        if (bundle == null) {
            this.mCutoutType = (StatusBarTypeController.CutoutType) getArguments().get("CollapsedStatusBarFragment_cutout_type");
        } else if (bundle.containsKey("CollapsedStatusBarFragment_cutout_type")) {
            this.mCutoutType = (StatusBarTypeController.CutoutType) bundle.get("CollapsedStatusBarFragment_cutout_type");
        }
        if (this.mCutoutType == null) {
            this.mCutoutType = StatusBarTypeController.CutoutType.NONE;
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("panel_state", this.mStatusBar.getState());
        bundle.putBoolean("clock_visible", this.mClockVisible);
        bundle.putSerializable("CollapsedStatusBarFragment_cutout_type", this.mCutoutType);
    }

    public void onDestroyView() {
        super.onDestroyView();
        unregisterListeners();
    }

    public void onHiddenChanged(boolean z) {
        super.onHiddenChanged(z);
        String simpleName = CollapsedStatusBarFragment.class.getSimpleName();
        StringBuilder sb = new StringBuilder();
        sb.append(" currentFragment:");
        sb.append(this);
        sb.append(" type is: ");
        StatusBarTypeController.CutoutType cutoutType = this.mCutoutType;
        sb.append(cutoutType == null ? null : cutoutType.name());
        sb.append(" switch hidden to: ");
        sb.append(z);
        Log.d(simpleName, sb.toString());
        if (z) {
            unregisterListeners();
        } else {
            registerListeners();
        }
    }

    public StatusBarTypeController.CutoutType getCutoutType() {
        return this.mCutoutType;
    }

    private void addDarkReceivers() {
        Class cls = DarkIconDispatcher.class;
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mSignalClusterView);
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mNetworkSpeedView);
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mStatusClock);
        ((DarkIconDispatcher) Dependency.get(cls)).addDarkReceiver(this.mBattery);
    }

    private void removeDarkReceivers() {
        Class cls = DarkIconDispatcher.class;
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mSignalClusterView);
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mNetworkSpeedView);
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mStatusClock);
        ((DarkIconDispatcher) Dependency.get(cls)).removeDarkReceiver(this.mBattery);
    }

    private void registerListeners() {
        if (!this.mIsRegisted) {
            this.mIsRegisted = true;
            this.mController.start(this.mStatusBar);
            addDarkReceivers();
            if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
                this.mNetworkController.addCallback(this.mSignalCallback);
            }
            ((CommandQueue) SystemUI.getComponent(getContext(), CommandQueue.class)).addCallbacks(this);
        }
    }

    private void unregisterListeners() {
        if (this.mIsRegisted) {
            this.mIsRegisted = false;
            CollapsedStatusBarFragmentController collapsedStatusBarFragmentController = this.mController;
            if (collapsedStatusBarFragmentController != null) {
                collapsedStatusBarFragmentController.stop();
            }
            removeDarkReceivers();
            if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
                this.mNetworkController.removeCallback(this.mSignalCallback);
            }
            ((CommandQueue) SystemUI.getComponent(getContext(), CommandQueue.class)).removeCallbacks(this);
        }
    }

    public void initNotificationIconArea(NotificationIconAreaController notificationIconAreaController) {
        ViewGroup viewGroup = (ViewGroup) this.mStatusBar.findViewById(R.id.notification_icon_area);
        View notificationInnerAreaView = notificationIconAreaController.getNotificationInnerAreaView();
        this.mNotificationIconAreaInner = notificationInnerAreaView;
        this.mNotificationIconAreaController = notificationIconAreaController;
        if (notificationInnerAreaView.getParent() != null) {
            ((ViewGroup) this.mNotificationIconAreaInner.getParent()).removeView(this.mNotificationIconAreaInner);
        }
        viewGroup.addView(this.mNotificationIconAreaInner);
        showNotificationIconArea(false);
        if (!this.mController.isNotch()) {
            notificationIconAreaController.setMoreIcon((StatusBarIconView) this.mStatusBar.findViewById(R.id.moreIcon));
        }
        notificationIconAreaController.setupClockContainer(this.mClockContainer);
        updateSystemIconVisible(false);
        updateNotificationIconVisible(false);
    }

    private void updateSystemIconVisible(boolean z) {
        if ((this.mDisabled1 & 1048576) != 0) {
            hideSystemIconArea(z);
        } else {
            showSystemIconArea(z);
        }
    }

    private void updateNotificationIconVisible(boolean z) {
        if ((this.mDisabled1 & 131072) != 0) {
            hideNotificationIconArea(z);
        } else {
            showNotificationIconArea(z);
        }
    }

    public void disable(int i, int i2, boolean z) {
        int adjustDisableFlags = adjustDisableFlags(i);
        int i3 = this.mDisabled1 ^ adjustDisableFlags;
        this.mDisabled1 = adjustDisableFlags;
        this.mDisabled2 = i2;
        boolean z2 = true;
        int i4 = 0;
        boolean z3 = (1048576 & i3) != 0;
        boolean z4 = (i3 & 131072) != 0;
        if (z3) {
            updateSystemIconVisible(z);
        }
        if (z4) {
            updateNotificationIconVisible(z);
        }
        boolean isStateNormal = this.mMiuiStatusBarPrompt.isStateNormal();
        if (z3 || z4) {
            z2 = false;
        }
        refreshClockVisibility(z, isStateNormal, z2, false);
        FrameLayout frameLayout = this.mMiuiStatusBarPromptLayout;
        if (this.mKeyguardMonitor.isShowing()) {
            i4 = 8;
        }
        frameLayout.setVisibility(i4);
    }

    /* access modifiers changed from: protected */
    public int adjustDisableFlags(int i) {
        if (this.mStatusBarComponent.shouldHideNotificationIcons()) {
            i |= 131072;
        }
        if (!this.mStatusBarComponent.isLaunchTransitionFadingAway() && !this.mKeyguardMonitor.isKeyguardFadingAway() && shouldHideNotificationIcons()) {
            i = i | 131072 | 8388608;
            if (this.mStatusBarComponent.getBarState() == 1 || !((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() || getContext().getResources().getConfiguration().orientation == 2) {
                i |= 1048576;
            }
        }
        NetworkController networkController = this.mNetworkController;
        if (networkController == null || !EncryptionHelper.IS_DATA_ENCRYPTED) {
            return i;
        }
        if (networkController.hasEmergencyCryptKeeperText()) {
            i |= 131072;
        }
        return !this.mNetworkController.isRadioOn() ? i | 1048576 : i;
    }

    private boolean shouldHideNotificationIcons() {
        return !this.mStatusBar.isClosed() && this.mStatusBarComponent.hideStatusBarIconsWhenExpanded();
    }

    public void hideSystemIconArea(boolean z) {
        animateHide(this.mSystemIconArea, z, true);
        this.mController.hideSystemIconArea(z, true);
    }

    public void showSystemIconArea(boolean z) {
        animateShow(this.mSystemIconArea, z);
        this.mController.showSystemIconArea(z);
    }

    public void hideNotificationIconArea(boolean z) {
        animateHide(this.mNotificationIconAreaInner, z, true);
    }

    public void showNotificationIconArea(boolean z) {
        animateShow(this.mNotificationIconAreaInner, z);
    }

    /* access modifiers changed from: protected */
    public void animateHide(View view, boolean z, boolean z2) {
        animateHideWithCallback(view, z, z2, (HideAnimateCallback) null);
    }

    /* access modifiers changed from: package-private */
    public void animateHideWithCallback(final View view, boolean z, boolean z2, final HideAnimateCallback hideAnimateCallback) {
        if (view != null) {
            final int i = z2 ? 4 : 8;
            view.animate().cancel();
            if (!z) {
                view.setAlpha(0.0f);
                view.setVisibility(i);
                return;
            }
            view.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable(this) {
                public void run() {
                    view.setVisibility(i);
                    HideAnimateCallback hideAnimateCallback = hideAnimateCallback;
                    if (hideAnimateCallback != null) {
                        hideAnimateCallback.callOnEnd();
                    }
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void animateShow(View view, boolean z) {
        if (view != null) {
            view.animate().cancel();
            view.setVisibility(0);
            if (!z) {
                view.setAlpha(1.0f);
                return;
            }
            view.animate().alpha(1.0f).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(50).withEndAction((Runnable) null);
            if (this.mKeyguardMonitor.isKeyguardFadingAway()) {
                view.animate().setDuration(this.mKeyguardMonitor.getKeyguardFadingAwayDuration()).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(this.mKeyguardMonitor.getKeyguardFadingAwayDelay()).start();
            }
        }
    }

    private void initEmergencyCryptkeeperText() {
        View findViewById = this.mStatusBar.findViewById(R.id.emergency_cryptkeeper_text);
        if (this.mNetworkController.hasEmergencyCryptKeeperText()) {
            this.mStatusClock.setVisibility(8);
            if (findViewById != null) {
                ((ViewStub) findViewById).inflate();
            }
        } else if (findViewById != null) {
            ((ViewGroup) findViewById.getParent()).removeView(findViewById);
        }
    }

    public void onLocationActiveChanged(boolean z) {
        if (this.mStatusIcons.getVisibility() != 8) {
            return;
        }
        if (this.mLocationController.isLocationActive()) {
            this.mGpsDriveMode.setVisibility(0);
        } else {
            this.mGpsDriveMode.setVisibility(8);
        }
    }

    public void updateInDriveMode(boolean z) {
        if (!this.mController.isNotch()) {
            this.mNetworkSpeedView.setDriveMode(z);
        }
        int i = 0;
        if (!z || this.mController.isNotch()) {
            this.mLocationController.removeCallback(this);
            if (!this.mStatusBarComponent.mDemoMode) {
                boolean z2 = (this.mDisabled2 & 2) != 0;
                LinearLayout linearLayout = this.mStatusIcons;
                if ((this.mController.isNotch() && this.mController.isStatusIconsVisible()) || z2) {
                    i = 8;
                }
                linearLayout.setVisibility(i);
            }
            this.mGpsDriveMode.setVisibility(8);
        } else {
            this.mLocationController.addCallback(this);
            this.mStatusIcons.setVisibility(8);
            ImageView imageView = this.mGpsDriveMode;
            if (!this.mController.isGPSDriveModeVisible()) {
                i = 8;
            }
            imageView.setVisibility(i);
        }
        this.mNotificationIconAreaController.setForceHideMoreIcon(z);
    }

    public void refreshClockVisibility(boolean z, boolean z2, boolean z3, boolean z4) {
        boolean z5 = false;
        boolean z6 = (this.mDisabled1 & 8388608) == 0 && this.mController.isClockVisibleByPrompt(z2);
        Log.d("StatusBarClock", " isNormalMode= " + z2 + " clock visible=" + z6);
        this.mLeftSideLayout.setVisibility(0);
        if (this.mClockVisible != z6 || z4) {
            this.mClockVisible = z6;
            CollapsedStatusBarFragmentController collapsedStatusBarFragmentController = this.mController;
            if (z6 && !this.mNetworkController.hasEmergencyCryptKeeperText()) {
                z5 = true;
            }
            collapsedStatusBarFragmentController.updateLeftPartVisibility(z5, z3, true, z4);
            if (z6) {
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).applyDark(this.mStatusClock);
            }
        }
    }

    public void updatePromptLayout() {
        this.mMiuiStatusBarPrompt.setPromptLayout(this.mMiuiStatusBarPromptLayout);
    }

    public void onConfigurationChanged() {
        this.mMiuiStatusBarPrompt.updateTouchRegion();
    }

    public void dump(PrintWriter printWriter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" mMiuiStatusBarPrompt.isStateNormal()= ");
        MiuiStatusBarPromptController miuiStatusBarPromptController = this.mMiuiStatusBarPrompt;
        String str = null;
        sb.append(miuiStatusBarPromptController == null ? null : Boolean.valueOf(miuiStatusBarPromptController.isStateNormal()));
        printWriter.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(" mLeftSideLayout.getVisibility()= ");
        LinearLayout linearLayout = this.mLeftSideLayout;
        sb2.append(linearLayout == null ? null : Integer.valueOf(linearLayout.getVisibility()));
        printWriter.println(sb2.toString());
        StringBuilder sb3 = new StringBuilder();
        sb3.append(" mClockContainer.getVisibility()= ");
        ViewGroup viewGroup = this.mClockContainer;
        sb3.append(viewGroup == null ? null : Integer.valueOf(viewGroup.getVisibility()));
        printWriter.println(sb3.toString());
        printWriter.println(" savedInstanceState= " + this.mSavedInstanceState);
        printWriter.println(" mDisabled1= " + Integer.toHexString(this.mDisabled1));
        StringBuilder sb4 = new StringBuilder();
        sb4.append(" statusBar cutout type = ");
        StatusBarTypeController.CutoutType cutoutType = this.mCutoutType;
        if (cutoutType != null) {
            str = cutoutType.name();
        }
        sb4.append(str);
        printWriter.println(sb4.toString());
        printWriter.println(" statusBar ishidden = " + isHidden());
    }

    public void refreshClockAmPm(boolean z) {
        Clock clock = this.mStatusClock;
        int i = clock.mForceHideAmPm;
        if (z) {
            clock.mForceHideAmPm = i | 2;
            if (i == 0) {
                clock.update();
                return;
            }
            return;
        }
        int i2 = i & -3;
        clock.mForceHideAmPm = i2;
        if (i > 0 && i2 == 0) {
            clock.update();
        }
    }

    public void clockVisibleAnimate(boolean z, boolean z2) {
        Clock clock = this.mStatusClock;
        if (z) {
            animateShow(clock, z2);
        } else {
            animateHide(clock, z2, false);
        }
    }

    private void updateViewsNotch() {
        ((BatteryMeterView) this.mStatusBar.findViewById(R.id.battery)).setNotchEar(this.mController.isNotch());
        this.mNetworkSpeedView.setNotch(this.mController.isNotch());
        if (this.mController.isStatusIconsVisible()) {
            this.mStatusIcons.setVisibility(8);
        }
    }

    private void updateViewsCutoutType() {
        ((BatteryMeterView) this.mStatusBar.findViewById(R.id.battery)).setCutoutType(this.mCutoutType);
        this.mSignalClusterView.setCutoutType(this.mCutoutType);
    }

    private void updateNotchPromptViewLayout(View view) {
        if (view != null) {
            boolean isStatusIconsVisible = this.mController.isStatusIconsVisible();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            if ((layoutParams.gravity == 17) != isStatusIconsVisible) {
                if (isStatusIconsVisible) {
                    layoutParams.gravity = 17;
                } else {
                    layoutParams.gravity = 8388627;
                }
                view.setLayoutParams(layoutParams);
            }
        }
    }

    public class LeftEarIconManager extends StatusBarIconController.DarkIconManager {
        public LeftEarIconManager(LinearLayout linearLayout) {
            super(linearLayout);
        }

        /* access modifiers changed from: protected */
        public void onIconAdded(int i, String str, boolean z, StatusBarIcon statusBarIcon) {
            super.onIconAdded(i, str, z, statusBarIcon);
            updateIcons(i, statusBarIcon);
        }

        public void onSetIcon(int i, String str, StatusBarIcon statusBarIcon) {
            super.onSetIcon(i, str, statusBarIcon);
            updateIcons(i, statusBarIcon);
        }

        /* access modifiers changed from: protected */
        public void onRemoveIcon(int i, String str) {
            updateIcons(i, (StatusBarIcon) null);
            super.onRemoveIcon(i, str);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v9, resolved type: boolean} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v11, resolved type: int} */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v8 */
        /* JADX WARNING: type inference failed for: r0v10 */
        /* JADX WARNING: type inference failed for: r0v12 */
        /* JADX WARNING: Multi-variable type inference failed */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void updateIcons(int r6, com.android.internal.statusbar.StatusBarIcon r7) {
            /*
                r5 = this;
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r0 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.miui.widget.ClipEdgeLinearLayout r0 = r0.mNotchLeftEarIcons
                android.view.View r6 = r0.getChildAt(r6)
                com.android.systemui.statusbar.StatusBarIconView r6 = (com.android.systemui.statusbar.StatusBarIconView) r6
                if (r6 != 0) goto L_0x000d
                return
            L_0x000d:
                r0 = 0
                if (r7 != 0) goto L_0x0012
                r1 = r0
                goto L_0x0018
            L_0x0012:
                android.graphics.drawable.Icon r1 = r7.icon
                int r1 = r1.getResId()
            L_0x0018:
                java.lang.String r2 = r6.getSlot()
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r3 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                android.util.ArraySet<java.lang.String> r3 = r3.mNotchleftearIconsList
                boolean r3 = r3.contains(r2)
                if (r3 != 0) goto L_0x0027
                return
            L_0x0027:
                java.lang.String r3 = "bluetooth"
                boolean r3 = r2.equals(r3)
                r4 = 1
                if (r3 == 0) goto L_0x0072
                if (r7 == 0) goto L_0x006c
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragmentController r7 = r7.mController
                boolean r7 = r7.isNarrowNotch()
                if (r7 != 0) goto L_0x005f
                r7 = 2131233576(0x7f080b28, float:1.8083293E38)
                if (r1 == r7) goto L_0x0050
                r7 = 2131233580(0x7f080b2c, float:1.8083302E38)
                if (r1 == r7) goto L_0x0050
                r7 = 2131233578(0x7f080b2a, float:1.8083297E38)
                if (r1 != r7) goto L_0x004e
                goto L_0x0050
            L_0x004e:
                r7 = r0
                goto L_0x0051
            L_0x0050:
                r7 = r4
            L_0x0051:
                if (r7 == 0) goto L_0x0054
                goto L_0x0056
            L_0x0054:
                r0 = 8
            L_0x0056:
                r6.setVisibility(r0)
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r6 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                boolean unused = r6.mShowBluetooth = r7
                goto L_0x0089
            L_0x005f:
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                int r6 = r6.getVisibility()
                if (r6 != 0) goto L_0x0068
                r0 = r4
            L_0x0068:
                boolean unused = r7.mShowBluetooth = r0
                goto L_0x0089
            L_0x006c:
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r6 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                boolean unused = r6.mShowBluetooth = r0
                goto L_0x0089
            L_0x0072:
                java.lang.String r1 = "location"
                boolean r1 = r2.equals(r1)
                if (r1 == 0) goto L_0x0089
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r1 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                if (r7 != 0) goto L_0x007f
                goto L_0x0086
            L_0x007f:
                int r6 = r6.getVisibility()
                if (r6 != 0) goto L_0x0086
                r0 = r4
            L_0x0086:
                boolean unused = r1.mShowLocation = r0
            L_0x0089:
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r6 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.policy.Clock r6 = r6.mStatusClock
                int r6 = r6.mForceHideAmPm
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                boolean r7 = r7.mShowBluetooth
                if (r7 != 0) goto L_0x00a1
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                boolean r7 = r7.mShowLocation
                if (r7 == 0) goto L_0x00c4
            L_0x00a1:
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragmentController r7 = r7.mController
                boolean r7 = r7.isNarrowNotch()
                if (r7 != 0) goto L_0x00c4
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.policy.Clock r7 = r7.mStatusClock
                int r0 = r7.mForceHideAmPm
                r0 = r0 | r4
                r7.mForceHideAmPm = r0
                if (r6 != 0) goto L_0x00e5
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r5 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.policy.Clock r5 = r5.mStatusClock
                r5.update()
                goto L_0x00e5
            L_0x00c4:
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r7 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.policy.Clock r7 = r7.mStatusClock
                int r0 = r7.mForceHideAmPm
                r0 = r0 & -2
                r7.mForceHideAmPm = r0
                if (r6 <= 0) goto L_0x00e5
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r6 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.policy.Clock r6 = r6.mStatusClock
                int r6 = r6.mForceHideAmPm
                if (r6 != 0) goto L_0x00e5
                com.android.systemui.statusbar.phone.CollapsedStatusBarFragment r5 = com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.this
                com.android.systemui.statusbar.policy.Clock r5 = r5.mStatusClock
                r5.update()
            L_0x00e5:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.CollapsedStatusBarFragment.LeftEarIconManager.updateIcons(int, com.android.internal.statusbar.StatusBarIcon):void");
        }
    }
}
