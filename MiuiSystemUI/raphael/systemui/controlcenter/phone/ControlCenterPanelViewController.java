package com.android.systemui.controlcenter.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.provider.MiuiSettings;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.controlcenter.phone.animator.AdvancedAnimatorImpl;
import com.android.systemui.controlcenter.phone.animator.ControlCenterPanelAnimator;
import com.android.systemui.controlcenter.phone.animator.PrimaryAnimatorImpl;
import com.android.systemui.controlcenter.phone.widget.ControlCenterBigTileGroup;
import com.android.systemui.controlcenter.phone.widget.ControlCenterContentContainer;
import com.android.systemui.controlcenter.phone.widget.ControlCenterFooterPanel;
import com.android.systemui.controlcenter.phone.widget.ControlCenterTilesContainer;
import com.android.systemui.controlcenter.phone.widget.QCToggleSliderView;
import com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView;
import com.android.systemui.controlcenter.policy.NCSwitchController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.util.CommonUtil;
import com.miui.systemui.util.MiuiAnimationUtils;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.VelocityMonitor;

public final class ControlCenterPanelViewController implements ConfigurationController.ConfigurationListener {
    private boolean animatingToCollapse;
    private final ControlPanelController ccController;
    private final ConfigurationController configurationController;
    private final Context context;
    private int expandThreshold;
    private IStateStyle expandTransAnim;
    private boolean isFsgEnabled;
    private boolean isFsgLineEnabled;
    private int maxVelocity = 1000;
    private int minVelocity = 500;
    private final NCSwitchController ncSwitchController;
    private int orientation = 1;
    private int paddingHorizontal;
    private final ControlCenterPanelAnimator panelAnimator = createPanelAnimator();
    private final ControlCenterPanelView panelView;
    private int screenHeight;
    private int screenWidth;
    private int stableInsetBottom;
    private final StatusBarStateController statusBarStateController;
    private float tileLayoutLastHeight;
    private float tileLayoutLastScrollY;
    private int tileLayoutMinHeight;
    private int touchSlop;
    private boolean touchable = true;
    private float transRatio;
    private final VelocityMonitor velocityMonitor = new VelocityMonitor();
    private final VelocityTracker velocityTracker;

    public ControlCenterPanelViewController(Context context2, ControlCenterPanelView controlCenterPanelView, ConfigurationController configurationController2, ControlPanelController controlPanelController, NCSwitchController nCSwitchController, StatusBarStateController statusBarStateController2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(controlCenterPanelView, "panelView");
        Intrinsics.checkParameterIsNotNull(configurationController2, "configurationController");
        Intrinsics.checkParameterIsNotNull(controlPanelController, "ccController");
        Intrinsics.checkParameterIsNotNull(nCSwitchController, "ncSwitchController");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        this.context = context2;
        this.panelView = controlCenterPanelView;
        this.configurationController = configurationController2;
        this.ccController = controlPanelController;
        this.ncSwitchController = nCSwitchController;
        this.statusBarStateController = statusBarStateController2;
        VelocityTracker obtain = VelocityTracker.obtain();
        Intrinsics.checkExpressionValueIsNotNull(obtain, "VelocityTracker.obtain()");
        this.velocityTracker = obtain;
        this.panelView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this) {
            /* class com.android.systemui.controlcenter.phone.ControlCenterPanelViewController.AnonymousClass1 */
            final /* synthetic */ ControlCenterPanelViewController this$0;

            {
                this.this$0 = r1;
            }

            public void onViewAttachedToWindow(View view) {
                this.this$0.configurationController.addCallback(this.this$0);
            }

            public void onViewDetachedFromWindow(View view) {
                this.this$0.configurationController.removeCallback(this.this$0);
            }
        });
        this.paddingHorizontal = this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_panel_margin_horizontal);
    }

    public final boolean isPortrait() {
        return this.orientation == 1;
    }

    public final boolean isSuperPowerMode() {
        return this.ccController.isSuperPowerMode();
    }

    public final ControlCenterPanelAnimator getPanelAnimator() {
        return this.panelAnimator;
    }

    public final int getScreenHeight() {
        return this.screenHeight;
    }

    public final boolean getTouchable() {
        return this.touchable;
    }

    public final void setTouchable(boolean z) {
        this.touchable = z;
    }

    public final void onFinishInflate() {
        this.panelView.setOnTouchListener(new TouchHandler());
        IStateStyle state = Folme.useAt(this.panelView.getTileContainer()).state();
        Intrinsics.checkExpressionValueIsNotNull(state, "Folme.useAt(panelView.tileContainer).state()");
        this.expandTransAnim = state;
        this.panelAnimator.onFinishInflate();
        updateFsgState();
        onOrientationChanged(getOrientation(), true);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.context);
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "it");
        this.maxVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.touchSlop = viewConfiguration.getScaledTouchSlop();
    }

    private final ControlCenterPanelAnimator createPanelAnimator() {
        if (DeviceConfig.isLowGpuDevice()) {
            return new PrimaryAnimatorImpl(this.panelView, this);
        }
        return new AdvancedAnimatorImpl(this.panelView, this, this.ccController);
    }

    public final void resetTransRatio() {
        setTransRatio(0.0f);
        this.panelView.getContentContainer().resetScrollY();
        this.panelView.getTileContainer().resetScrollY();
    }

    public final void cancelTransAnim() {
        IStateStyle iStateStyle = this.expandTransAnim;
        if (iStateStyle != null) {
            iStateStyle.cancel();
            this.velocityMonitor.clear();
            this.velocityTracker.clear();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandTransAnim");
        throw null;
    }

    public final float getTransRatio() {
        return this.transRatio;
    }

    /* access modifiers changed from: public */
    private final void setTransRatio(float f) {
        if (this.transRatio != f && isExpandable()) {
            this.transRatio = f;
            this.panelView.getContentContainer().suppressLayout(true);
            if (isPortrait()) {
                ViewGroup.LayoutParams layoutParams = this.panelView.getBigTileLayout().getLayoutParams();
                if (layoutParams != null) {
                    this.panelView.getTileContainer().getLayoutParams().height = this.panelView.getBigTileLayout().calculateHeight() + ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin + this.panelView.getTileLayout().getMinHeight() + ((int) (this.transRatio * ((float) this.expandThreshold)));
                    this.panelView.getTileLayout().setExpandRatio(RangesKt___RangesKt.coerceIn(this.transRatio, 0.0f, 1.0f));
                    this.panelView.getTileContainer().setLayoutParams(this.panelView.getTileContainer().getLayoutParams());
                    this.panelView.getTileContainer().getScroller().setScrollY((int) (this.tileLayoutLastScrollY * RangesKt___RangesKt.coerceIn(this.transRatio, 0.0f, 1.0f)));
                    boolean z = this.transRatio > 0.0f;
                    this.panelView.getTileContainer().setNestedScrollingEnabled(z);
                    this.panelView.getContentContainer().getScroller().requestDisallowInterceptTouchEvent(z);
                    this.panelView.getContentContainer().getContainer().setClipChildren(z);
                    this.panelView.getTileContainer().setClipToPadding(z);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
                }
            }
            this.panelView.getFooter().getDivider().setAlpha(this.transRatio);
            this.panelView.getSmartHomeContainer().setAlpha(1.0f - this.transRatio);
            if (shouldHideIndicatorWhenExpand()) {
                float f2 = RangesKt___RangesKt.coerceIn(1.0f - this.transRatio, 0.0f, 1.0f);
                ImageView indicator = this.panelView.getFooter().getIndicator();
                if (this.transRatio >= 1.0f) {
                    Folme.useAt(indicator).state().cancel(ViewProperty.ALPHA);
                }
                indicator.setAlpha(f2 * f2);
            }
            boolean z2 = this.transRatio >= 1.0f;
            this.panelView.getSmartHomeContainer().setVisibility(z2 ? 8 : 0);
            this.panelView.getContentContainer().setSpringBackEnable(!z2);
            this.panelView.getContentContainer().suppressLayout(false);
        }
    }

    private final void updateLayout(boolean z) {
        this.panelView.getCcContainer().suppressLayout(true);
        if (z) {
            moveViews();
        }
        this.panelView.getContentContainer().resetScrollY();
        this.panelView.getTileContainer().resetScrollY();
        updatePadding();
        this.panelView.getFooter().getIndicator().setVisibility((!isPortrait() || isSuperPowerMode()) ? 8 : 0);
        this.panelView.getBigTileLayout().updateLayout();
        this.panelView.getCcContainer().suppressLayout(false);
    }

    private final void moveViews() {
        if (isPortrait()) {
            this.panelView.getCcContainer().setOrientation(1);
            ControlCenterBigTileGroup bigTileLayout = this.panelView.getBigTileLayout();
            this.panelView.getContentContainer().removeView(bigTileLayout);
            this.panelView.getTileContainer().addView(bigTileLayout, 0);
            ControlCenterTilesContainer tileContainer = this.panelView.getTileContainer();
            this.panelView.getCcContainer().removeView(tileContainer);
            this.panelView.getContentContainer().addView(tileContainer, 0);
            return;
        }
        this.panelView.getCcContainer().setOrientation(0);
        ControlCenterTilesContainer tileContainer2 = this.panelView.getTileContainer();
        this.panelView.getContentContainer().removeView(tileContainer2);
        this.panelView.getCcContainer().addView(tileContainer2, 0);
        ControlCenterBigTileGroup bigTileLayout2 = this.panelView.getBigTileLayout();
        this.panelView.getTileContainer().removeView(bigTileLayout2);
        this.panelView.getContentContainer().addView(bigTileLayout2, 0);
    }

    private final int getOrientation() {
        Resources resources = this.context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        return resources.getConfiguration().orientation;
    }

    private final void updatePadding() {
        ViewGroup.LayoutParams layoutParams = this.panelView.getContentContainer().getLayoutParams();
        ViewGroup.LayoutParams layoutParams2 = this.panelView.getTileContainer().getLayoutParams();
        if (layoutParams2 != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams2;
            ViewGroup.LayoutParams layoutParams3 = this.panelView.getBigTileLayout().getLayoutParams();
            if (layoutParams3 != null) {
                ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) layoutParams3;
                if (isPortrait()) {
                    QSControlCenterHeaderView header = this.panelView.getHeader();
                    header.setPadding(this.paddingHorizontal, this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_center_header_paddingTop), this.paddingHorizontal, header.getPaddingBottom());
                    ControlCenterPanelView controlCenterPanelView = this.panelView;
                    controlCenterPanelView.setPadding(0, 0, 0, controlCenterPanelView.getPaddingBottom());
                    LinearLayout container = this.panelView.getTileContainer().getContainer();
                    int i = this.paddingHorizontal;
                    container.setPadding(i, 0, i, container.getPaddingBottom());
                    LinearLayout container2 = this.panelView.getContentContainer().getContainer();
                    container2.setPadding(0, container2.getPaddingTop(), 0, container2.getPaddingBottom());
                    ControlCenterFooterPanel footer = this.panelView.getFooter();
                    footer.setPadding(this.paddingHorizontal, footer.getPaddingTop(), this.paddingHorizontal, footer.getPaddingBottom());
                    layoutParams.width = -1;
                    marginLayoutParams2.bottomMargin = this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tiles_margin_bottom);
                    marginLayoutParams.width = -1;
                    marginLayoutParams.setMarginEnd(0);
                    marginLayoutParams.height = this.panelView.getBigTileLayout().calculateHeight() + marginLayoutParams2.bottomMargin + this.panelView.getTileLayout().getMinHeight();
                    return;
                }
                int dimensionPixelSize = ((CommonUtil.getScreenSize(this.context).x - (this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land) * 2)) - this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_land_tiles_margin_middle)) / 2;
                QSControlCenterHeaderView header2 = this.panelView.getHeader();
                header2.setPadding(0, 0, 0, header2.getPaddingBottom());
                LinearLayout container3 = this.panelView.getContentContainer().getContainer();
                container3.setPadding(0, container3.getPaddingTop(), 0, container3.getPaddingBottom());
                LinearLayout container4 = this.panelView.getTileContainer().getContainer();
                container4.setPadding(0, this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tiles_padding_top), 0, container4.getPaddingBottom());
                ControlCenterFooterPanel footer2 = this.panelView.getFooter();
                footer2.setPadding(0, footer2.getPaddingTop(), 0, footer2.getPaddingBottom());
                ControlCenterPanelView controlCenterPanelView2 = this.panelView;
                controlCenterPanelView2.setPadding(dimensionPixelSize, 0, dimensionPixelSize, controlCenterPanelView2.getPaddingBottom());
                layoutParams.width = this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land);
                marginLayoutParams.width = this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land);
                marginLayoutParams.setMarginEnd(this.context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_land_tiles_margin_middle));
                marginLayoutParams2.bottomMargin = 0;
                marginLayoutParams.height = -1;
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
    }

    public final void notifyOrientationChanged() {
        int orientation2 = getOrientation();
        this.orientation = orientation2;
        onOrientationChanged(orientation2, true);
    }

    public final boolean couldOverFling() {
        return this.transRatio == 0.0f && this.panelView.getContentContainer().getScroller().getScrollY() == 0 && this.panelView.getTileContainer().getScroller().getScrollY() == 0;
    }

    private final void onOrientationChanged(int i, boolean z) {
        if (z || this.orientation != i) {
            updateScreenHeight();
            int i2 = 0;
            boolean z2 = this.orientation != i;
            this.orientation = i;
            updateLayout(z2);
            resetTransRatio();
            this.panelAnimator.notifyOrientationChanged();
            this.panelView.getTileLayout().setTranslationY(0.0f);
            QSControlCenterTileLayout tileLayout = this.panelView.getTileLayout();
            if (isPortrait()) {
                i2 = 4;
            }
            tileLayout.setBaseLineIdx(i2);
            this.panelView.getTileLayout().setExpanded(!isPortrait());
        }
    }

    private final void updateScreenHeight() {
        Object systemService = this.context.getSystemService("display");
        if (systemService != null) {
            Display display = ((DisplayManager) systemService).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            this.screenHeight = Math.max(point.y, point.x);
            this.screenWidth = Math.min(point.y, point.x);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.hardware.display.DisplayManager");
    }

    private final boolean shouldHideIndicatorWhenExpand() {
        return (!this.isFsgEnabled || !this.isFsgLineEnabled || this.statusBarStateController.getState() == 1 || this.statusBarStateController.getState() == 2 || this.stableInsetBottom == 0) ? false : true;
    }

    private final void updateFsgState() {
        this.isFsgEnabled = MiuiSettings.Global.getBoolean(this.context.getContentResolver(), "force_fsg_nav_bar");
        this.isFsgLineEnabled = !MiuiSettings.Global.getBoolean(this.context.getContentResolver(), "hide_gesture_line");
    }

    private final boolean isExpandable() {
        return !isSuperPowerMode();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        this.panelView.updateResources();
        onOrientationChanged(configuration.orientation, false);
        if (!isPortrait()) {
            this.ccController.showDialog(false);
        }
    }

    public final void onApplyWindowInsets(WindowInsets windowInsets) {
        Intrinsics.checkParameterIsNotNull(windowInsets, "insets");
        this.stableInsetBottom = windowInsets.getStableInsetBottom();
        this.panelView.getContentContainer().getNavigationBarSpace().getLayoutParams().height = this.stableInsetBottom;
        updateFsgState();
    }

    public final class TouchHandler implements View.OnTouchListener {
        private Boolean eventAborted;
        private int initialScrollY;
        private float initialTouchX = -1.0f;
        private float initialTouchY = -1.0f;
        private float initialTransRatio = -1.0f;
        private Boolean interceptedThisTouch;
        private boolean isMoveY;
        private boolean moved;
        private boolean startOnBrightness;
        private boolean startOnFooter;
        private boolean startOnTile;
        private boolean swipeCollapse;
        private float transY = -1.0f;

        public TouchHandler() {
            ControlCenterPanelViewController.this = r1;
        }

        public final Boolean dispatchTouchEvent(MotionEvent motionEvent) {
            ControlPanelWindowView controlPanelWindowView;
            Boolean bool = Boolean.TRUE;
            Boolean bool2 = Boolean.FALSE;
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            if (!ControlCenterPanelViewController.this.isPortrait()) {
                return null;
            }
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 0) {
                boolean z = true;
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        this.moved = true;
                        float f = rawY - this.initialTouchY;
                        this.transY = f;
                        if (Math.abs(f) <= Math.abs(rawX - this.initialTouchX)) {
                            z = false;
                        }
                        this.isMoveY = z;
                        Boolean bool3 = this.eventAborted;
                        if (bool3 != null) {
                            if (bool3 == null) {
                                Intrinsics.throwNpe();
                                throw null;
                            } else if (bool3.booleanValue()) {
                                ControlPanelWindowView controlPanelWindowView2 = ControlCenterPanelViewController.this.panelView.getControlPanelWindowView();
                                if (controlPanelWindowView2 != null) {
                                    controlPanelWindowView2.handleMotionEvent(motionEvent, false);
                                }
                                return bool2;
                            }
                        } else if (!this.swipeCollapse || this.transY >= ((float) 0) || !z) {
                            this.eventAborted = bool2;
                        } else {
                            this.eventAborted = bool;
                            return bool2;
                        }
                    } else if (actionMasked != 3) {
                        if (actionMasked == 5 && !ControlCenterPanelViewController.this.getTouchable()) {
                            return bool;
                        }
                    }
                }
                Boolean bool4 = this.eventAborted;
                if (bool4 != null && bool4.booleanValue()) {
                    ControlPanelWindowView controlPanelWindowView3 = ControlCenterPanelViewController.this.panelView.getControlPanelWindowView();
                    if (controlPanelWindowView3 != null) {
                        controlPanelWindowView3.handleMotionEvent(motionEvent, false);
                    }
                    return bool2;
                }
            } else {
                this.eventAborted = null;
                this.initialTouchX = rawX;
                this.initialTouchY = rawY;
                boolean shouldCollapseBySwipeUp = shouldCollapseBySwipeUp(rawX, rawY);
                this.swipeCollapse = shouldCollapseBySwipeUp;
                if (shouldCollapseBySwipeUp && (controlPanelWindowView = ControlCenterPanelViewController.this.panelView.getControlPanelWindowView()) != null) {
                    controlPanelWindowView.handleMotionEvent(motionEvent, false, false);
                }
            }
            return null;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
            if (r4 != 3) goto L_0x013a;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public final java.lang.Boolean onInterceptTouchEvent(android.view.MotionEvent r9) {
            /*
            // Method dump skipped, instructions count: 315
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.ControlCenterPanelViewController.TouchHandler.onInterceptTouchEvent(android.view.MotionEvent):java.lang.Boolean");
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(view, "v");
            Intrinsics.checkParameterIsNotNull(motionEvent, "event");
            boolean z = ControlCenterPanelViewController.this.panelView.isExpanded() && !ControlCenterPanelViewController.this.ccController.isNCSwitching();
            if (!this.startOnBrightness && ControlCenterPanelViewController.this.ncSwitchController.handleCNSwitchTouch(motionEvent, z)) {
                return true;
            }
            if (!ControlCenterPanelViewController.this.isPortrait()) {
                return false;
            }
            motionEvent.getRawX();
            motionEvent.getRawY();
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    ControlCenterPanelViewController.this.velocityTracker.addMovement(motionEvent);
                    ControlCenterPanelViewController.this.updateTransRatioByTouch(this.transY, this.initialTransRatio);
                    return true;
                } else if (actionMasked != 3) {
                    return false;
                }
            }
            return animateFinishingExpansion();
        }

        private final boolean animateFinishingExpansion() {
            if (!ControlCenterPanelViewController.this.isExpandable() || ControlCenterPanelViewController.this.getTransRatio() == 0.0f || ControlCenterPanelViewController.this.getTransRatio() == 1.0f) {
                return false;
            }
            float f = (ControlCenterPanelViewController.this.tileLayoutLastHeight + this.transY) - ((float) ControlCenterPanelViewController.this.tileLayoutMinHeight);
            ControlCenterPanelViewController.this.velocityTracker.computeCurrentVelocity(1000, (float) ControlCenterPanelViewController.this.maxVelocity);
            float yVelocity = ControlCenterPanelViewController.this.velocityTracker.getYVelocity();
            float f2 = (float) 0;
            if (yVelocity > f2 && Math.abs(yVelocity) > ((float) ControlCenterPanelViewController.this.minVelocity)) {
                ControlCenterPanelViewController.this.toExpandAnimation();
                return true;
            } else if (yVelocity >= f2 || Math.abs(yVelocity) <= ((float) ControlCenterPanelViewController.this.minVelocity)) {
                if (f > ((float) (ControlCenterPanelViewController.this.expandThreshold / 2))) {
                    ControlCenterPanelViewController.this.toExpandAnimation();
                } else {
                    ControlCenterPanelViewController.this.toCollapseAnimation();
                }
                return true;
            } else {
                ControlCenterPanelViewController.this.toCollapseAnimation();
                return true;
            }
        }

        private final boolean startOnBorder(float f, float f2) {
            if (f >= ((float) ControlCenterPanelViewController.this.paddingHorizontal) && f <= ((float) (ControlCenterPanelViewController.this.screenWidth - ControlCenterPanelViewController.this.paddingHorizontal))) {
                return false;
            }
            return true;
        }

        private final boolean startOnBottomSpace(float f, float f2) {
            int[] iArr = new int[2];
            Space navigationBarSpace = ControlCenterPanelViewController.this.panelView.getContentContainer().getNavigationBarSpace();
            navigationBarSpace.getLocationOnScreen(iArr);
            if (f < ((float) iArr[0]) || f > ((float) (iArr[0] + navigationBarSpace.getWidth())) || f2 < ((float) (iArr[1] + navigationBarSpace.getHeight()))) {
                return false;
            }
            ControlCenterContentContainer contentContainer = ControlCenterPanelViewController.this.panelView.getContentContainer();
            contentContainer.getLocationOnScreen(iArr);
            if (f2 > ((float) (iArr[1] + contentContainer.getHeight()))) {
                return false;
            }
            return true;
        }

        private final boolean startOnFooter(float f, float f2) {
            int[] iArr = new int[2];
            ControlCenterPanelViewController.this.panelView.getFooter().getLocationOnScreen(iArr);
            if (f2 < ((float) iArr[1]) || f < ((float) iArr[0]) || f > ((float) (iArr[0] + ControlCenterPanelViewController.this.panelView.getFooter().getWidth()))) {
                return false;
            }
            ControlCenterPanelViewController.this.panelView.getSmartHomeContainer().getLocationOnScreen(iArr);
            if (f2 > ((float) iArr[1])) {
                return false;
            }
            return true;
        }

        private final boolean startOnBrightnessView(float f, float f2) {
            QCToggleSliderView brightnessView = ControlCenterPanelViewController.this.panelView.getBrightnessView().getBrightnessView();
            int[] iArr = new int[2];
            brightnessView.getLocationOnScreen(iArr);
            if (f2 >= ((float) iArr[1]) && f >= ((float) iArr[0]) && f <= ((float) (iArr[0] + brightnessView.getWidth())) && f2 <= ((float) (iArr[1] + brightnessView.getHeight()))) {
                return true;
            }
            return false;
        }

        private final boolean startOnTile(float f, float f2) {
            int[] iArr = new int[2];
            ControlCenterPanelViewController.this.panelView.getTileContainer().getLocationOnScreen(iArr);
            if (f2 >= ((float) iArr[1]) && f >= ((float) iArr[0]) && f <= ((float) (iArr[0] + ControlCenterPanelViewController.this.panelView.getTileContainer().getWidth())) && f2 <= ((float) (iArr[1] + ControlCenterPanelViewController.this.panelView.getTileContainer().getHeight()))) {
                return true;
            }
            return false;
        }

        private final boolean shouldCollapseByClick(float f, float f2) {
            return startOnBorder(f, f2) || startOnBottomSpace(f, f2);
        }

        private final boolean shouldCollapseBySwipeUp(float f, float f2) {
            return (ControlCenterPanelViewController.this.getTransRatio() <= ((float) 0) || ControlCenterPanelViewController.this.animatingToCollapse) && ControlCenterPanelViewController.this.panelView.getContentContainer().isScrolledToBottom();
        }
    }

    private final void calculateTransitionValues() {
        int height = this.panelView.getBigTileLayout().getHeight();
        ViewGroup.LayoutParams layoutParams = this.panelView.getBigTileLayout().getLayoutParams();
        if (layoutParams != null) {
            int minHeight = height + ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin + this.panelView.getTileLayout().getMinHeight();
            this.tileLayoutMinHeight = minHeight;
            int height2 = ((this.screenHeight - minHeight) - this.panelView.getHeader().getHeight()) - this.panelView.getFooter().getDivider().getHeight();
            ViewGroup.LayoutParams layoutParams2 = this.panelView.getFooter().getDivider().getLayoutParams();
            if (layoutParams2 != null) {
                int height3 = (height2 - ((ViewGroup.MarginLayoutParams) layoutParams2).bottomMargin) - this.panelView.getBrightnessView().getHeight();
                ViewGroup.LayoutParams layoutParams3 = this.panelView.getFooter().getIndicator().getLayoutParams();
                if (layoutParams3 != null) {
                    this.expandThreshold = ((height3 - ((ViewGroup.MarginLayoutParams) layoutParams3).topMargin) - this.panelView.getFooter().getIndicator().getHeight()) - this.stableInsetBottom;
                    this.tileLayoutLastHeight = (float) this.panelView.getTileContainer().getHeight();
                    this.tileLayoutLastScrollY = (float) this.panelView.getTileContainer().getScroller().getScrollY();
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
            }
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
    }

    private final void toExpandAnimation() {
        IStateStyle iStateStyle = this.expandTransAnim;
        if (iStateStyle != null) {
            iStateStyle.cancel(ViewProperty.HEIGHT);
            AnimState animState = new AnimState("expand_trans");
            animState.add(ViewProperty.HEIGHT, this.panelView.getTileContainer().getHeight(), new long[0]);
            AnimState animState2 = new AnimState("expand_trans");
            animState2.add(ViewProperty.HEIGHT, this.tileLayoutMinHeight + this.expandThreshold, new long[0]);
            AnimConfig animConfig = new AnimConfig(ViewProperty.HEIGHT);
            animConfig.setFromSpeed(this.velocityMonitor.getVelocity(0));
            animConfig.setEase(-2, 0.85f, 0.35f);
            animConfig.addListeners(new ControlCenterPanelViewController$toExpandAnimation$animConfig$1(this));
            IStateStyle iStateStyle2 = this.expandTransAnim;
            if (iStateStyle2 != null) {
                iStateStyle2.fromTo(animState, animState2, animConfig);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("expandTransAnim");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandTransAnim");
        throw null;
    }

    private final void toCollapseAnimation() {
        IStateStyle iStateStyle = this.expandTransAnim;
        if (iStateStyle != null) {
            iStateStyle.cancel(ViewProperty.HEIGHT);
            AnimState animState = new AnimState("expand_trans");
            animState.add(ViewProperty.HEIGHT, this.panelView.getTileContainer().getHeight(), new long[0]);
            AnimState animState2 = new AnimState("expand_trans");
            animState2.add(ViewProperty.HEIGHT, this.tileLayoutMinHeight, new long[0]);
            AnimConfig animConfig = new AnimConfig(ViewProperty.HEIGHT);
            animConfig.setFromSpeed(this.velocityMonitor.getVelocity(0));
            animConfig.setEase(-2, 1.0f, 0.35f);
            animConfig.addListeners(new ControlCenterPanelViewController$toCollapseAnimation$animConfig$1(this));
            IStateStyle iStateStyle2 = this.expandTransAnim;
            if (iStateStyle2 != null) {
                iStateStyle2.fromTo(animState, animState2, animConfig);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("expandTransAnim");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("expandTransAnim");
        throw null;
    }

    private final void updateTransRatioByTouch(float f, float f2) {
        float f3 = ((this.tileLayoutLastHeight - ((float) this.tileLayoutMinHeight)) + f) / ((float) this.expandThreshold);
        if (f3 < 0.0f) {
            f3 = -MiuiAnimationUtils.INSTANCE.afterFriction(-f3, 0.3f);
        } else if (f3 > 1.0f) {
            f3 = MiuiAnimationUtils.INSTANCE.afterFriction(f3 - 1.0f, 0.3f) + 1.0f;
        }
        if (f2 == 1.0f) {
            setTransRatio(RangesKt___RangesKt.coerceAtMost(f3, 1.0f));
        } else if (f2 == 0.0f) {
            setTransRatio(RangesKt___RangesKt.coerceAtLeast(f3, 0.0f));
        }
        this.velocityMonitor.update(((this.transRatio * ((float) this.expandThreshold)) - this.tileLayoutLastHeight) + ((float) this.tileLayoutMinHeight));
    }
}
