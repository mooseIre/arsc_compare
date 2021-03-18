package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.DisplayCutout;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.MiuiBatteryMeterView;
import com.android.systemui.ScreenDecorations;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class KeyguardStatusBarView extends RelativeLayout implements BatteryController.BatteryStateChangeCallback, UserInfoController.OnUserInfoChangedListener, ConfigurationController.ConfigurationListener {
    private boolean mBatteryCharging;
    private BatteryController mBatteryController;
    private boolean mBatteryListening;
    protected MiuiBatteryMeterView mBatteryView;
    protected TextView mCarrierLabel;
    private ControlPanelWindowManager mControlPanelWindowManager;
    private int mCutoutSideNudge = 0;
    private View mCutoutSpace;
    private DisplayCutout mDisplayCutout;
    protected StatusBarIconController.MiuiLightDarkIconManager mDripLeftIconManager;
    private MiuiDripLeftStatusIconContainer mDripLeftStatusIconContainer;
    protected FrameLayout mDripLeftStatusIconFrameContainer;
    protected StatusBarIconController.MiuiLightDarkIconManager mDripRightIconManager;
    private MiuiStatusIconContainer mDripRightStatusIconContainer;
    protected final Rect mEmptyRect = new Rect(0, 0, 0, 0);
    protected StatusBarIconController.MiuiLightDarkIconManager mIconManager;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private boolean mKeyguardUserSwitcherShowing;
    private int mLayoutState = 0;
    private ImageView mMultiUserAvatar;
    private MultiUserSwitch mMultiUserSwitch;
    protected NetworkSpeedView mNetworkSpeedView;
    private Pair<Integer, Integer> mPadding = new Pair<>(0, 0);
    private int mRoundedCornerPadding = 0;
    protected FrameLayout mStatusBarPromptContainer;
    protected ViewGroup mStatusIconArea;
    private MiuiStatusIconContainer mStatusIconContainer;
    private int mSystemIconsBaseMargin;
    private View mSystemIconsContainer;
    private int mSystemIconsSwitcherHiddenExpandedMargin;
    private UserSwitcherController mUserSwitcherController;

    private int calculateMargin(int i, int i2) {
        if (i2 >= i) {
            return 0;
        }
        return i - i2;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
    }

    public void setDarkStyle(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void updateIconsAndTextColors() {
    }

    public KeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsContainer = findViewById(C0015R$id.system_icons_container);
        this.mMultiUserSwitch = (MultiUserSwitch) findViewById(C0015R$id.multi_user_switch);
        this.mMultiUserAvatar = (ImageView) findViewById(C0015R$id.multi_user_avatar);
        this.mCarrierLabel = (TextView) findViewById(C0015R$id.keyguard_carrier_text);
        this.mBatteryView = (MiuiBatteryMeterView) this.mSystemIconsContainer.findViewById(C0015R$id.battery);
        this.mCutoutSpace = findViewById(C0015R$id.cutout_space_view);
        this.mStatusIconArea = (ViewGroup) findViewById(C0015R$id.status_icon_area);
        this.mDripRightStatusIconContainer = (MiuiStatusIconContainer) findViewById(C0015R$id.drip_right_statusIcons);
        this.mDripLeftStatusIconFrameContainer = (FrameLayout) findViewById(C0015R$id.keyguard_drip_left_statusIcons_container);
        this.mDripLeftStatusIconContainer = (MiuiDripLeftStatusIconContainer) findViewById(C0015R$id.keyguard_drip_left_statusIcons);
        this.mStatusBarPromptContainer = (FrameLayout) findViewById(C0015R$id.prompt_container);
        this.mNetworkSpeedView = (NetworkSpeedView) findViewById(C0015R$id.fullscreen_network_speed_view);
        this.mStatusIconContainer = (MiuiStatusIconContainer) findViewById(C0015R$id.statusIcons);
        loadDimens();
        updateUserSwitcher();
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mControlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mMultiUserAvatar.getLayoutParams();
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.multi_user_avatar_keyguard_size);
        marginLayoutParams.height = dimensionPixelSize;
        marginLayoutParams.width = dimensionPixelSize;
        this.mMultiUserAvatar.setLayoutParams(marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.mMultiUserSwitch.getLayoutParams();
        marginLayoutParams2.width = getResources().getDimensionPixelSize(C0012R$dimen.multi_user_switch_width_keyguard);
        marginLayoutParams2.setMarginEnd(getResources().getDimensionPixelSize(C0012R$dimen.multi_user_switch_keyguard_margin));
        this.mMultiUserSwitch.setLayoutParams(marginLayoutParams2);
        ViewGroup.MarginLayoutParams marginLayoutParams3 = (ViewGroup.MarginLayoutParams) this.mSystemIconsContainer.getLayoutParams();
        marginLayoutParams3.setMarginStart(getResources().getDimensionPixelSize(C0012R$dimen.system_icons_super_container_margin_start));
        this.mSystemIconsContainer.setLayoutParams(marginLayoutParams3);
        View view = this.mSystemIconsContainer;
        view.setPaddingRelative(view.getPaddingStart(), this.mSystemIconsContainer.getPaddingTop(), getResources().getDimensionPixelSize(C0012R$dimen.system_icons_keyguard_padding_end), this.mSystemIconsContainer.getPaddingBottom());
        updateKeyguardStatusBarHeight();
    }

    private void updateKeyguardStatusBarHeight() {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        marginLayoutParams.height = getResources().getDimensionPixelSize(C0012R$dimen.status_bar_height) + i;
        setLayoutParams(marginLayoutParams);
    }

    private void loadDimens() {
        Resources resources = getResources();
        this.mSystemIconsSwitcherHiddenExpandedMargin = resources.getDimensionPixelSize(C0012R$dimen.system_icons_switcher_hidden_expanded_margin);
        this.mSystemIconsBaseMargin = resources.getDimensionPixelSize(C0012R$dimen.system_icons_super_container_avatarless_margin_end);
        this.mCutoutSideNudge = getResources().getDimensionPixelSize(C0012R$dimen.display_cutout_margin_consumption);
        getContext().getResources().getBoolean(17891375);
        this.mRoundedCornerPadding = resources.getDimensionPixelSize(C0012R$dimen.rounded_corner_content_padding);
    }

    private void updateVisibilities() {
        if (this.mMultiUserSwitch.getParent() == this.mStatusIconArea || this.mKeyguardUserSwitcherShowing) {
            ViewParent parent = this.mMultiUserSwitch.getParent();
            ViewGroup viewGroup = this.mStatusIconArea;
            if (parent == viewGroup && this.mKeyguardUserSwitcherShowing) {
                viewGroup.removeView(this.mMultiUserSwitch);
                return;
            }
            return;
        }
        if (this.mMultiUserSwitch.getParent() != null) {
            getOverlay().remove(this.mMultiUserSwitch);
        }
        this.mStatusIconArea.addView(this.mMultiUserSwitch, 0);
    }

    private void updateSystemIconsLayoutParams() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams();
        int i = this.mMultiUserSwitch.getVisibility() == 8 ? this.mSystemIconsBaseMargin : 0;
        if (this.mKeyguardUserSwitcherShowing) {
            i = this.mSystemIconsSwitcherHiddenExpandedMargin;
        }
        int calculateMargin = calculateMargin(i, ((Integer) this.mPadding.second).intValue());
        if (calculateMargin != layoutParams.getMarginEnd()) {
            layoutParams.setMarginEnd(calculateMargin);
            this.mSystemIconsContainer.setLayoutParams(layoutParams);
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mLayoutState = 0;
        if (updateLayoutConsideringCutout()) {
            requestLayout();
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private boolean updateLayoutConsideringCutout() {
        this.mDisplayCutout = getRootWindowInsets().getDisplayCutout();
        updateKeyguardStatusBarHeight();
        Pair<Integer, Integer> cornerCutoutMargins = StatusBarWindowView.cornerCutoutMargins(this.mDisplayCutout, getDisplay());
        updatePadding(cornerCutoutMargins);
        if (this.mDisplayCutout == null || cornerCutoutMargins != null) {
            return updateLayoutParamsNoCutout();
        }
        return updateLayoutParamsForCutout();
    }

    private void updatePadding(Pair<Integer, Integer> pair) {
        DisplayCutout displayCutout = this.mDisplayCutout;
        int i = displayCutout == null ? 0 : displayCutout.getWaterfallInsets().top;
        this.mPadding = StatusBarWindowView.paddingNeededForCutoutAndRoundedCorner(this.mDisplayCutout, pair, this.mRoundedCornerPadding);
        Pair<Integer, Integer> pair2 = new Pair<>(Integer.valueOf(((Integer) this.mPadding.first).intValue() + getResources().getDimensionPixelOffset(C0012R$dimen.status_bar_padding_start)), Integer.valueOf(((Integer) this.mPadding.second).intValue() + getResources().getDimensionPixelOffset(C0012R$dimen.status_bar_padding_end)));
        this.mPadding = pair2;
        setPadding(((Integer) pair2.first).intValue(), i, ((Integer) this.mPadding.second).intValue(), 0);
    }

    private boolean updateLayoutParamsNoCutout() {
        if (this.mLayoutState == 2) {
            return false;
        }
        this.mLayoutState = 2;
        View view = this.mCutoutSpace;
        if (view != null) {
            view.setVisibility(8);
        }
        this.mDripRightStatusIconContainer.setVisibility(8);
        this.mDripLeftStatusIconFrameContainer.setVisibility(8);
        this.mStatusIconContainer.setVisibility(0);
        this.mNetworkSpeedView.setVisibilityByStatusBar(true);
        ((RelativeLayout.LayoutParams) this.mStatusIconArea.getLayoutParams()).addRule(1, C0015R$id.keyguard_carrier_text);
        ((LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams()).setMarginStart(getResources().getDimensionPixelSize(C0012R$dimen.system_icons_super_container_margin_start));
        return true;
    }

    private boolean updateLayoutParamsForCutout() {
        if (this.mLayoutState == 1) {
            return false;
        }
        this.mLayoutState = 1;
        if (this.mCutoutSpace == null) {
            updateLayoutParamsNoCutout();
        }
        this.mDripRightStatusIconContainer.setVisibility(0);
        this.mDripLeftStatusIconFrameContainer.setVisibility(0);
        this.mStatusIconContainer.setVisibility(8);
        this.mNetworkSpeedView.setVisibilityByStatusBar(false);
        Rect rect = new Rect();
        ScreenDecorations.DisplayCutoutView.boundsFromDirection(this.mDisplayCutout, 48, rect);
        this.mCutoutSpace.setVisibility(0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mCutoutSpace.getLayoutParams();
        int i = rect.left;
        int i2 = this.mCutoutSideNudge;
        rect.left = i + i2;
        rect.right -= i2;
        layoutParams.width = rect.width();
        layoutParams.height = rect.height();
        layoutParams.addRule(13);
        ((RelativeLayout.LayoutParams) this.mStatusIconArea.getLayoutParams()).addRule(1, C0015R$id.cutout_space_view);
        ((LinearLayout.LayoutParams) this.mSystemIconsContainer.getLayoutParams()).setMarginStart(0);
        return true;
    }

    public void setListening(boolean z) {
        if (z != this.mBatteryListening) {
            this.mBatteryListening = z;
            if (z) {
                this.mBatteryController.addCallback(this);
            } else {
                this.mBatteryController.removeCallback(this);
            }
        }
    }

    private void updateUserSwitcher() {
        boolean z = this.mKeyguardUserSwitcher != null;
        this.mMultiUserSwitch.setClickable(z);
        this.mMultiUserSwitch.setFocusable(z);
        this.mMultiUserSwitch.setKeyguardMode(z);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        UserInfoController userInfoController = (UserInfoController) Dependency.get(UserInfoController.class);
        userInfoController.addCallback(this);
        UserSwitcherController userSwitcherController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
        this.mUserSwitcherController = userSwitcherController;
        this.mMultiUserSwitch.setUserSwitcherController(userSwitcherController);
        userInfoController.reloadUserInfo();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        int lightModeIconColorSingleTone = ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).getLightModeIconColorSingleTone();
        this.mIconManager = new StatusBarIconController.MiuiLightDarkIconManager((ViewGroup) findViewById(C0015R$id.statusIcons), (CommandQueue) Dependency.get(CommandQueue.class), true, lightModeIconColorSingleTone);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mIconManager);
        ArrayList arrayList = new ArrayList(Arrays.asList(getContext().getResources().getStringArray(C0008R$array.config_drip_right_block_statusBarIcons)));
        StatusBarIconController.MiuiLightDarkIconManager miuiLightDarkIconManager = new StatusBarIconController.MiuiLightDarkIconManager(this.mDripRightStatusIconContainer, (CommandQueue) Dependency.get(CommandQueue.class), true, lightModeIconColorSingleTone);
        this.mDripRightIconManager = miuiLightDarkIconManager;
        miuiLightDarkIconManager.setDrip(true);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).addIconGroup(this.mDripRightIconManager, arrayList);
        this.mDripLeftIconManager = new StatusBarIconController.MiuiLightDarkIconManager(this.mDripLeftStatusIconContainer, (CommandQueue) Dependency.get(CommandQueue.class), true, lightModeIconColorSingleTone);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).addIconGroup(this.mDripLeftIconManager);
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).addPromptContainer(this.mStatusBarPromptContainer, 0);
        onThemeChanged();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((UserInfoController) Dependency.get(UserInfoController.class)).removeCallback(this);
        ((MiuiDripLeftStatusBarIconControllerImpl) Dependency.get(MiuiDripLeftStatusBarIconControllerImpl.class)).removeIconGroup(this.mDripLeftIconManager);
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).removePromptContainer(this.mStatusBarPromptContainer);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mDripRightIconManager);
        ((StatusBarIconController) Dependency.get(StatusBarIconController.class)).removeIconGroup(this.mIconManager);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        this.mMultiUserAvatar.setImageDrawable(drawable);
    }

    public void setQSPanel(QSPanel qSPanel) {
        this.mMultiUserSwitch.setQsPanel(qSPanel);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (this.mBatteryCharging != z2) {
            this.mBatteryCharging = z2;
            updateVisibilities();
        }
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        this.mMultiUserSwitch.setKeyguardUserSwitcher(keyguardUserSwitcher);
        updateUserSwitcher();
    }

    public void setKeyguardUserSwitcherShowing(boolean z, boolean z2) {
        this.mKeyguardUserSwitcherShowing = z;
        if (z2) {
            animateNextLayoutChange();
        }
        updateVisibilities();
        updateLayoutConsideringCutout();
        updateSystemIconsLayoutParams();
    }

    private void animateNextLayoutChange() {
        final int left = this.mSystemIconsContainer.getLeft();
        final boolean z = this.mMultiUserSwitch.getParent() == this.mStatusIconArea;
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.systemui.statusbar.phone.KeyguardStatusBarView.AnonymousClass1 */

            public boolean onPreDraw() {
                KeyguardStatusBarView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean z = z && KeyguardStatusBarView.this.mMultiUserSwitch.getParent() != KeyguardStatusBarView.this.mStatusIconArea;
                KeyguardStatusBarView.this.mSystemIconsContainer.setX((float) left);
                KeyguardStatusBarView.this.mSystemIconsContainer.animate().translationX(0.0f).setDuration(400).setStartDelay(z ? 300 : 0).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).start();
                if (z) {
                    KeyguardStatusBarView.this.getOverlay().add(KeyguardStatusBarView.this.mMultiUserSwitch);
                    KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(0.0f).setDuration(300).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
                        /* class com.android.systemui.statusbar.phone.$$Lambda$KeyguardStatusBarView$1$DyabYtIeJMptnepd5jqXSnZ7UZ0 */

                        public final void run() {
                            KeyguardStatusBarView.AnonymousClass1.this.lambda$onPreDraw$0$KeyguardStatusBarView$1();
                        }
                    }).start();
                } else {
                    KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(0.0f);
                    KeyguardStatusBarView.this.mMultiUserSwitch.animate().alpha(1.0f).setDuration(300).setStartDelay(200).setInterpolator(Interpolators.ALPHA_IN);
                }
                return true;
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onPreDraw$0 */
            public /* synthetic */ void lambda$onPreDraw$0$KeyguardStatusBarView$1() {
                KeyguardStatusBarView.this.mMultiUserSwitch.setAlpha(1.0f);
                KeyguardStatusBarView.this.getOverlay().remove(KeyguardStatusBarView.this.mMultiUserSwitch);
            }
        });
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        if (i != 0) {
            this.mSystemIconsContainer.animate().cancel();
            this.mSystemIconsContainer.setTranslationX(0.0f);
            this.mMultiUserSwitch.animate().cancel();
            this.mMultiUserSwitch.setAlpha(1.0f);
            return;
        }
        updateVisibilities();
        updateSystemIconsLayoutParams();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        updateIconsAndTextColors();
        ((UserInfoControllerImpl) Dependency.get(UserInfoController.class)).onDensityOrFontScaleChanged();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        loadDimens();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        onThemeChanged();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyguardStatusBarView:");
        printWriter.println("  mBatteryCharging: " + this.mBatteryCharging);
        printWriter.println("  mKeyguardUserSwitcherShowing: " + this.mKeyguardUserSwitcherShowing);
        printWriter.println("  mBatteryListening: " + this.mBatteryListening);
        printWriter.println("  mLayoutState: " + this.mLayoutState);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() != 0) {
            return super.dispatchTouchEvent(motionEvent);
        }
        if (!this.mControlPanelWindowManager.dispatchToControlPanel(motionEvent, (float) getWidth())) {
            this.mControlPanelWindowManager.setTransToControlPanel(false);
            return super.dispatchTouchEvent(motionEvent);
        }
        this.mControlPanelWindowManager.setTransToControlPanel(true);
        return false;
    }
}
