package com.android.systemui.controlcenter.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.phone.detail.QSControlDetail;
import com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView;
import com.android.systemui.controlcenter.phone.widget.AutoBrightnessView;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiRecord;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiTileRecord;
import com.android.systemui.controlcenter.phone.widget.QCToggleSliderView;
import com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView;
import com.android.systemui.controlcenter.phone.widget.QSControlFooter;
import com.android.systemui.controlcenter.qs.tileview.QCBrightnessMirrorController;
import com.android.systemui.controlcenter.qs.tileview.QSBigTileView;
import com.android.systemui.controlcenter.utils.Constants;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.controlcenter.utils.FolmeAnimState;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.MiuiBrightnessController;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.ExpandQuickTilesEvent;
import com.miui.systemui.util.CommonUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import miuix.animation.property.ViewProperty;

public class QSControlCenterPanel extends FrameLayout implements ConfigurationController.ConfigurationListener, FolmeAnimState {
    private boolean DEBUG = Constants.DEBUG;
    private boolean isMoveY = false;
    protected IStateStyle mAnim;
    private AutoBrightnessView mAutoBrightnessView;
    private float mBaseTransitionY;
    private QSBigTileView mBigTile0;
    private QSBigTileView mBigTile1;
    private QSBigTileView mBigTile2;
    private QSBigTileView mBigTile3;
    private ViewGroup mBigTiles;
    private ScrollView mBigTilesScrollView;
    private MiuiBrightnessController mBrightnessController;
    private QCBrightnessMirrorController mBrightnessMirrorController;
    private QCToggleSliderView mBrightnessView;
    public BroadcastDispatcher mBroadcastDispatcher;
    private Context mContext;
    private QSControlFooter mControlFooter;
    private ControlPanelContentView mControlPanelContentView;
    private ControlPanelWindowView mControlPanelWindowView;
    private ControlsPluginManager mControlsPluginManager;
    private QSControlDetail.QSPanelCallback mDetailCallback;
    private MiuiQSPanel$MiuiRecord mDetailRecord;
    private int mDistanceToBottom;
    private View mEditTiles;
    /* access modifiers changed from: private */
    public int mExpandHeightThres;
    private ImageView mExpandIndicator;
    private ImageView mExpandIndicatorBottom;
    private QSControlExpandTileView mExpandTileView;
    private boolean mExpanded;
    private LinearLayout mFootPanel;
    private int mFootPanelBaseIdx;
    protected IStateStyle mFootPanelTransAnim;
    private final H mHandler = new H();
    private QSControlCenterHeaderView mHeader;
    private float mInitialTouchX = -1.0f;
    private float mInitialTouchY = -1.0f;
    private AutoBrightnessView mLandAutoBrightnessView;
    private MiuiBrightnessController mLandBrightnessController;
    private QCToggleSliderView mLandBrightnessView;
    private QSControlFooter mLandCtrFooter;
    private LinearLayout mLandFootPanel;
    private QCBrightnessMirrorController mLandMirrorController;
    private LinearLayout mLandSmartControlsView;
    private LinearLayout mLandTiles;
    private boolean mListening;
    private int[] mLocation;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private boolean mMoved;
    private int mOrientation;
    private int mPaddingHorizontal;
    private ControlPanelController mPanelController;
    private LinearLayout mQSBrightnessLayout;
    private FrameLayout mQSContainer;
    private QSControlScrollView mQsControlScrollView;
    /* access modifiers changed from: private */
    public QSControlCenterTileLayout mQuickQsControlCenterTileLayout;
    private int mScreenHeight;
    private LinearLayout mSmartControlsView;
    private int mStableInsetBottom;
    private View mTileView0;
    private LinearLayout mTilesContainer;
    private final int mTouchSlop;
    private boolean mTouchable;
    private int mTransLineNum;
    private ArrayList<View> mTransViews;
    private VelocityTracker mVelocityTracker;
    private ArrayList<View> mViews;

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public QSControlCenterPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Display display = null;
        this.mDetailCallback = null;
        this.mViews = new ArrayList<>();
        this.mTransViews = new ArrayList<>();
        this.mLocation = new int[2];
        this.mTouchable = true;
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        this.mPaddingHorizontal = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_panel_margin_horizontal);
        WindowManager windowManager = (WindowManager) this.mContext.getApplicationContext().getSystemService("window");
        display = windowManager != null ? windowManager.getDefaultDisplay() : display;
        if (display != null) {
            display.getRealSize(new Point());
        }
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHeader = (QSControlCenterHeaderView) findViewById(C0015R$id.header);
        this.mEditTiles = findViewById(C0015R$id.tiles_edit);
        this.mQSContainer = (FrameLayout) findViewById(C0015R$id.qs_container);
        QSControlScrollView qSControlScrollView = (QSControlScrollView) findViewById(C0015R$id.scroll_container);
        this.mQsControlScrollView = qSControlScrollView;
        qSControlScrollView.setQSControlCenterPanel(this);
        this.mBigTilesScrollView = (ScrollView) findViewById(C0015R$id.scroll_container_big_tiles);
        QSControlCenterTileLayout qSControlCenterTileLayout = (QSControlCenterTileLayout) findViewById(C0015R$id.quick_tile_layout);
        this.mQuickQsControlCenterTileLayout = qSControlCenterTileLayout;
        qSControlCenterTileLayout.setQSControlCenterPanel(this);
        this.mFootPanel = (LinearLayout) findViewById(C0015R$id.foot_panel);
        this.mControlFooter = (QSControlFooter) findViewById(C0015R$id.settings_footer);
        this.mLandFootPanel = (LinearLayout) findViewById(C0015R$id.foot_panel_land);
        this.mLandCtrFooter = (QSControlFooter) findViewById(C0015R$id.land_footer);
        this.mQSBrightnessLayout = (LinearLayout) findViewById(C0015R$id.qs_brightness_container);
        this.mBrightnessView = (QCToggleSliderView) findViewById(C0015R$id.qs_brightness);
        this.mLandBrightnessView = (QCToggleSliderView) findViewById(C0015R$id.qs_brightness_land);
        this.mBrightnessController = new MiuiBrightnessController(getContext(), this.mBrightnessView, this.mBroadcastDispatcher);
        this.mLandBrightnessController = new MiuiBrightnessController(getContext(), this.mLandBrightnessView, this.mBroadcastDispatcher);
        ImageView imageView = (ImageView) findViewById(C0015R$id.qs_expand_indicator);
        this.mExpandIndicator = imageView;
        imageView.setVisibility(this.mPanelController.isSuperPowerMode() ? 8 : 0);
        this.mExpandIndicatorBottom = (ImageView) findViewById(C0015R$id.qs_expand_indicator_bottom);
        this.mAutoBrightnessView = (AutoBrightnessView) findViewById(C0015R$id.auto_brightness);
        this.mLandAutoBrightnessView = (AutoBrightnessView) findViewById(C0015R$id.auto_brightness_land);
        this.mExpandTileView = (QSControlExpandTileView) findViewById(C0015R$id.expand_tile);
        this.mSmartControlsView = (LinearLayout) findViewById(C0015R$id.ll_smart_controls);
        this.mLandSmartControlsView = (LinearLayout) findViewById(C0015R$id.ll_smart_controls_land);
        this.mTilesContainer = (LinearLayout) findViewById(C0015R$id.tiles_container);
        this.mBigTiles = (ViewGroup) findViewById(C0015R$id.big_tiles);
        this.mLandTiles = (LinearLayout) findViewById(C0015R$id.land_tiles);
        if (Constants.IS_INTERNATIONAL) {
            this.mExpandTileView.setVisibility(8);
            QSBigTileView qSBigTileView = (QSBigTileView) findViewById(C0015R$id.big_tile_0);
            this.mBigTile0 = qSBigTileView;
            qSBigTileView.setVisibility(0);
            QSBigTileView qSBigTileView2 = this.mBigTile0;
            this.mTileView0 = qSBigTileView2;
            qSBigTileView2.init(this, "cell", 0);
            QSBigTileView qSBigTileView3 = (QSBigTileView) findViewById(C0015R$id.big_tile_1);
            this.mBigTile1 = qSBigTileView3;
            qSBigTileView3.init(this, "wifi", 1);
            QSBigTileView qSBigTileView4 = (QSBigTileView) findViewById(C0015R$id.big_tile_2);
            this.mBigTile2 = qSBigTileView4;
            qSBigTileView4.init(this, "bt", 2);
            QSBigTileView qSBigTileView5 = (QSBigTileView) findViewById(C0015R$id.big_tile_3);
            this.mBigTile3 = qSBigTileView5;
            qSBigTileView5.init(this, "flashlight", 3);
        } else {
            this.mTileView0 = this.mExpandTileView;
            QSBigTileView qSBigTileView6 = (QSBigTileView) findViewById(C0015R$id.big_tile_1);
            this.mBigTile1 = qSBigTileView6;
            qSBigTileView6.init(this, "bt", 1);
            QSBigTileView qSBigTileView7 = (QSBigTileView) findViewById(C0015R$id.big_tile_2);
            this.mBigTile2 = qSBigTileView7;
            qSBigTileView7.init(this, "cell", 2);
            QSBigTileView qSBigTileView8 = (QSBigTileView) findViewById(C0015R$id.big_tile_3);
            this.mBigTile3 = qSBigTileView8;
            qSBigTileView8.init(this, "wifi", 3);
        }
        initAnimState();
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        this.mMinimumVelocity = 500;
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mControlsPluginManager = (ControlsPluginManager) Dependency.get(ControlsPluginManager.class);
    }

    public void setControlPanelContentView(ControlPanelContentView controlPanelContentView) {
        this.mControlPanelContentView = controlPanelContentView;
        this.mBrightnessMirrorController = new QCBrightnessMirrorController(controlPanelContentView);
        this.mLandMirrorController = new QCBrightnessMirrorController(controlPanelContentView);
        this.mControlPanelContentView.getDetailView().setQsPanel(this);
        this.mBrightnessView.setMirror((QCToggleSliderView) this.mBrightnessMirrorController.getMirror().findViewById(C0015R$id.brightness_slider));
        this.mBrightnessView.setMirrorController(this.mBrightnessMirrorController);
        this.mLandBrightnessView.setMirror((QCToggleSliderView) this.mLandMirrorController.getMirror().findViewById(C0015R$id.brightness_slider));
        this.mLandBrightnessView.setMirrorController(this.mLandMirrorController);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mExpandIndicatorBottom.getLayoutParams();
        layoutParams.bottomMargin = windowInsets.getStableInsetBottom();
        this.mExpandIndicatorBottom.setLayoutParams(layoutParams);
        int paddingTop = getPaddingTop();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int stableInsetBottom = windowInsets.getStableInsetBottom();
        this.mStableInsetBottom = windowInsets.getStableInsetBottom();
        setPadding(paddingLeft, paddingTop, paddingRight, stableInsetBottom);
        return super.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mControlFooter.setHostEnvironment(qSTileHost);
        this.mLandCtrFooter.setHostEnvironment(qSTileHost);
        this.mQuickQsControlCenterTileLayout.setHost(qSTileHost);
        QSBigTileView qSBigTileView = this.mBigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.setHost(qSTileHost);
        }
        this.mBigTile1.setHost(qSTileHost);
        this.mBigTile2.setHost(qSTileHost);
        this.mBigTile3.setHost(qSTileHost);
        this.mAutoBrightnessView.setHost(qSTileHost);
        this.mLandAutoBrightnessView.setHost(qSTileHost);
        int i = getResources().getConfiguration().orientation;
        this.mOrientation = i;
        onOrientationChanged(i, true);
    }

    public void clickTile(ComponentName componentName) {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.mQuickQsControlCenterTileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.clickTile(componentName);
        }
    }

    public void addControlsPlugin() {
        View controlsView;
        if (this.mControlsPluginManager != null && !this.mPanelController.isSuperPowerMode() && (controlsView = this.mControlsPluginManager.getControlsView()) != null) {
            if (this.mOrientation == 1) {
                this.mSmartControlsView.addView(controlsView);
            } else {
                this.mLandSmartControlsView.addView(controlsView);
            }
        }
    }

    public void removeControlsPlugin() {
        if (this.mControlsPluginManager != null) {
            this.mSmartControlsView.removeAllViews();
            this.mLandSmartControlsView.removeAllViews();
            this.mControlsPluginManager.hideControlView();
        }
    }

    private boolean isOrientationPortrait() {
        return this.mOrientation == 1;
    }

    public void onConfigChanged(Configuration configuration) {
        updateResources();
        onOrientationChanged(getResources().getConfiguration().orientation, false);
    }

    public void onOrientationChanged(int i, boolean z) {
        if (z || this.mOrientation != i) {
            if (this.mOrientation != i) {
                this.mOrientation = i;
                updateLayout();
            }
            updateScreenHeight();
            updateFootPanelLayout();
            this.mExpandIndicatorBottom.setAlpha(0.0f);
            this.mFootPanel.setAlpha(1.0f);
            int i2 = 0;
            this.mFootPanel.setVisibility(0);
            setBrightnessListening(this.mExpanded);
            setPadding(getPaddingLeft(), this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_center_header_paddingTop), getPaddingRight(), getPaddingBottom());
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mQSContainer.getLayoutParams();
            this.mQuickQsControlCenterTileLayout.setTranslationY(0.0f);
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_brightness_width);
            int i3 = 5;
            if (i == 1) {
                this.mBigTilesScrollView.setVisibility(8);
                this.mFootPanel.setVisibility(0);
                this.mControlFooter.setForceHide(false);
                this.mControlFooter.refreshState();
                this.mLandFootPanel.setVisibility(8);
                this.mLandCtrFooter.setForceHide(true);
                this.mLandCtrFooter.refreshState();
                this.mQSBrightnessLayout.setVisibility(0);
                this.mQuickQsControlCenterTileLayout.setBaseLineIdx(4);
                if (!this.mPanelController.isSuperPowerMode()) {
                    i3 = 6;
                }
                this.mFootPanelBaseIdx = i3;
                this.mTransLineNum = 4;
                this.mBrightnessMirrorController.updateResources();
                this.mQuickQsControlCenterTileLayout.setExpanded(false);
                this.mFootPanel.setTranslationY(0.0f);
                ImageView imageView = this.mExpandIndicator;
                if (this.mPanelController.isSuperPowerMode()) {
                    i2 = 8;
                }
                imageView.setVisibility(i2);
                this.mExpandIndicator.setAlpha(1.0f);
                layoutParams.height = -2;
                this.mQSContainer.setLayoutParams(layoutParams);
                this.mEditTiles.setScaleX(1.0f);
                this.mEditTiles.setScaleY(1.0f);
                this.mEditTiles.setAlpha(1.0f);
                this.mAutoBrightnessView.setScaleX(1.0f);
                this.mAutoBrightnessView.setScaleY(1.0f);
                this.mAutoBrightnessView.setAlpha(1.0f);
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mBrightnessView.getLayoutParams();
                if (layoutParams2.width != dimensionPixelSize) {
                    layoutParams2.width = dimensionPixelSize;
                    this.mBrightnessView.setLayoutParams(layoutParams2);
                }
                this.mBrightnessView.setScaleX(1.0f);
                this.mBrightnessView.setScaleY(1.0f);
                this.mBrightnessView.setAlpha(1.0f);
                updateSmartControls(true);
                this.mControlFooter.resetViews();
            } else {
                this.mBigTilesScrollView.setVisibility(0);
                this.mFootPanel.setVisibility(8);
                this.mControlFooter.setForceHide(true);
                this.mControlFooter.refreshState();
                this.mLandFootPanel.setVisibility(0);
                this.mLandCtrFooter.setForceHide(false);
                this.mLandCtrFooter.refreshState();
                this.mFootPanelBaseIdx = 4;
                this.mTransLineNum = 5;
                this.mQuickQsControlCenterTileLayout.setBaseLineIdx(0);
                this.mQuickQsControlCenterTileLayout.setExpanded(true);
                this.mLandMirrorController.updateResources();
                this.mQSBrightnessLayout.setVisibility(8);
                this.mExpandIndicator.setVisibility(8);
                this.mFootPanel.setTranslationY(0.0f);
                layoutParams.height = (int) (((float) this.mQuickQsControlCenterTileLayout.getMaxHeight()) + this.mQuickQsControlCenterTileLayout.getTranslationY());
                this.mQSContainer.setLayoutParams(layoutParams);
                LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mLandBrightnessView.getLayoutParams();
                if (layoutParams3.width != dimensionPixelSize) {
                    layoutParams3.width = dimensionPixelSize;
                    this.mLandBrightnessView.setLayoutParams(layoutParams3);
                }
                this.mLandAutoBrightnessView.setScaleX(1.0f);
                this.mLandAutoBrightnessView.setScaleY(1.0f);
                this.mLandAutoBrightnessView.setAlpha(1.0f);
                this.mLandBrightnessView.setScaleX(1.0f);
                this.mLandBrightnessView.setScaleY(1.0f);
                this.mLandBrightnessView.setAlpha(1.0f);
                updateSmartControls(false);
                this.mLandCtrFooter.resetViews();
            }
            updateViews();
        }
    }

    public void updateResources() {
        this.mHeader.updateResources();
        this.mExpandTileView.updateResources();
        QSBigTileView qSBigTileView = this.mBigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.updateResources();
        }
        this.mBigTile1.updateResources();
        this.mBigTile2.updateResources();
        this.mBigTile3.updateResources();
        this.mAutoBrightnessView.updateResources();
        this.mLandAutoBrightnessView.updateResources();
        this.mBrightnessView.updateResources();
        this.mLandBrightnessView.updateResources();
        this.mExpandIndicator.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.qs_control_tiles_indicator));
        this.mExpandIndicatorBottom.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.qs_control_tiles_indicator));
        this.mQuickQsControlCenterTileLayout.updateResources();
        this.mControlFooter.updateResources();
        this.mLandCtrFooter.updateResources();
    }

    private void updateSmartControls(boolean z) {
        if (z) {
            if (this.mLandSmartControlsView.getChildCount() > 0) {
                View childAt = this.mLandSmartControlsView.getChildAt(0);
                this.mLandSmartControlsView.removeAllViews();
                this.mSmartControlsView.addView(childAt);
            }
        } else if (this.mSmartControlsView.getChildCount() > 0) {
            View childAt2 = this.mSmartControlsView.getChildAt(0);
            this.mSmartControlsView.removeAllViews();
            this.mLandSmartControlsView.addView(childAt2);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "dispatchTouchEvent " + motionEvent.getAction());
        }
        if (this.mTouchable || motionEvent.getActionMasked() != 5) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "onInterceptTouchEvent " + motionEvent.getAction());
        }
        if (!isOrientationPortrait()) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInitialTouchX = motionEvent.getRawX();
            this.mInitialTouchY = motionEvent.getRawY();
            this.mQuickQsControlCenterTileLayout.startMove();
            this.mBaseTransitionY = this.mFootPanel.getTranslationY();
            this.mExpandIndicatorBottom.getBoundsOnScreen(new Rect());
        } else if (actionMasked == 2) {
            this.isMoveY = Math.abs(motionEvent.getRawY() - this.mInitialTouchY) > Math.abs(motionEvent.getRawX() - this.mInitialTouchX);
            if (!isBigTileTouched() && this.mQuickQsControlCenterTileLayout.isCollapsed() && this.isMoveY && Math.abs(this.mInitialTouchY - motionEvent.getRawY()) > ((float) this.mTouchSlop)) {
                if (this.mSmartControlsView.getChildCount() > 0) {
                    if (motionEvent.getRawY() < this.mInitialTouchY && !this.mQsControlScrollView.isScrolledToBottom() && isSmartControlOverScreen()) {
                        return false;
                    }
                    if (motionEvent.getRawY() <= this.mInitialTouchY || this.mQsControlScrollView.isScrolledToTop()) {
                        return true;
                    }
                    return false;
                }
                return true;
            } else if (motionEvent.getRawY() < this.mInitialTouchY && this.isMoveY && this.mQuickQsControlCenterTileLayout.isExpanded() && (this.mQsControlScrollView.isScrolledToBottom() || (this.mQsControlScrollView.isScrolledToTop() && !this.mQuickQsControlCenterTileLayout.canScroll()))) {
                return true;
            } else {
                if (this.mQuickQsControlCenterTileLayout.isExpanding() && this.isMoveY) {
                    return true;
                }
            }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private boolean isSmartControlOverScreen() {
        this.mSmartControlsView.getLocationOnScreen(this.mLocation);
        return this.mLocation[1] + this.mSmartControlsView.getHeight() > this.mScreenHeight - this.mStableInsetBottom;
    }

    private boolean isHeaderAreaTouchDown(float f) {
        this.mHeader.getLocationOnScreen(this.mLocation);
        if (f < 0.0f || f > ((float) (this.mLocation[1] + this.mHeader.getHeight()))) {
            return false;
        }
        return true;
    }

    private boolean isFootAreaTouchDown(float f) {
        this.mFootPanel.getLocationOnScreen(this.mLocation);
        boolean z = f >= ((float) this.mLocation[1]);
        this.mSmartControlsView.getLocationOnScreen(this.mLocation);
        if (!z || f > ((float) this.mLocation[1])) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0034, code lost:
        if (r0 != 3) goto L_0x013a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            boolean r0 = r5.DEBUG
            if (r0 == 0) goto L_0x001e
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "onTouchEvent "
            r0.append(r1)
            int r1 = r6.getAction()
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "QSControlCenterPanel"
            android.util.Log.d(r1, r0)
        L_0x001e:
            r5.initVelocityTrackerIfNotExists()
            android.view.VelocityTracker r0 = r5.mVelocityTracker
            r0.addMovement(r6)
            int r0 = r6.getActionMasked()
            r1 = 0
            r2 = 1
            if (r0 == 0) goto L_0x010e
            r3 = 2
            if (r0 == r2) goto L_0x0083
            if (r0 == r3) goto L_0x0038
            r1 = 3
            if (r0 == r1) goto L_0x0092
            goto L_0x013a
        L_0x0038:
            r5.mMoved = r2
            boolean r0 = r5.isOrientationPortrait()
            if (r0 != 0) goto L_0x0042
            goto L_0x013a
        L_0x0042:
            float r0 = r5.mInitialTouchY
            boolean r0 = r5.isHeaderAreaTouchDown(r0)
            if (r0 == 0) goto L_0x004c
            goto L_0x013a
        L_0x004c:
            float r0 = r6.getRawY()
            float r3 = r5.mInitialTouchY
            int r0 = (r0 > r3 ? 1 : (r0 == r3 ? 0 : -1))
            if (r0 >= 0) goto L_0x0065
            com.android.systemui.controlcenter.phone.QSControlCenterTileLayout r0 = r5.mQuickQsControlCenterTileLayout
            boolean r0 = r0.isCollapsed()
            if (r0 == 0) goto L_0x0065
            com.android.systemui.controlcenter.phone.ControlPanelWindowView r5 = r5.mControlPanelWindowView
            r5.onTouchEvent(r6)
            goto L_0x013a
        L_0x0065:
            float r6 = r6.getRawY()
            float r0 = r5.mInitialTouchY
            float r6 = r6 - r0
            miuix.animation.IStateStyle r0 = r5.mFootPanelTransAnim
            miuix.animation.property.FloatProperty[] r3 = new miuix.animation.property.FloatProperty[r2]
            miuix.animation.property.ViewProperty r4 = miuix.animation.property.ViewProperty.TRANSLATION_Y
            r3[r1] = r4
            r0.cancel(r3)
            float r0 = r5.mBaseTransitionY
            float r0 = r0 + r6
            int r6 = r5.mExpandHeightThres
            float r6 = (float) r6
            float r0 = r0 / r6
            r5.setTransRatio(r0)
            goto L_0x013a
        L_0x0083:
            float r0 = r6.getRawX()
            int r0 = (int) r0
            float r1 = r6.getRawY()
            int r1 = (int) r1
            boolean r4 = r5.mMoved
            r5.performCollapseByClick(r0, r1, r4)
        L_0x0092:
            boolean r0 = r5.isOrientationPortrait()
            if (r0 != 0) goto L_0x009a
            goto L_0x013a
        L_0x009a:
            float r0 = r6.getRawY()
            float r1 = r5.mInitialTouchY
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 >= 0) goto L_0x00b6
            com.android.systemui.controlcenter.phone.QSControlCenterTileLayout r0 = r5.mQuickQsControlCenterTileLayout
            boolean r0 = r0.isCollapsed()
            if (r0 == 0) goto L_0x00b6
            r6.setAction(r2)
            com.android.systemui.controlcenter.phone.ControlPanelWindowView r5 = r5.mControlPanelWindowView
            r5.onTouchEvent(r6)
            goto L_0x013a
        L_0x00b6:
            com.android.systemui.controlcenter.phone.QSControlCenterTileLayout r6 = r5.mQuickQsControlCenterTileLayout
            boolean r6 = r6.isExpanding()
            if (r6 == 0) goto L_0x010a
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            r0 = 1000(0x3e8, float:1.401E-42)
            int r1 = r5.mMaximumVelocity
            float r1 = (float) r1
            r6.computeCurrentVelocity(r0, r1)
            android.view.VelocityTracker r6 = r5.mVelocityTracker
            float r6 = r6.getYVelocity()
            float r0 = java.lang.Math.abs(r6)
            int r1 = r5.mMinimumVelocity
            float r1 = (float) r1
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            r1 = 0
            if (r0 <= 0) goto L_0x00e2
            int r0 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1))
            if (r0 <= 0) goto L_0x00e2
            r5.toBottomAnimation()
            goto L_0x010a
        L_0x00e2:
            float r0 = java.lang.Math.abs(r6)
            int r4 = r5.mMinimumVelocity
            float r4 = (float) r4
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 <= 0) goto L_0x00f5
            int r6 = (r6 > r1 ? 1 : (r6 == r1 ? 0 : -1))
            if (r6 >= 0) goto L_0x00f5
            r5.toTopAnimation()
            goto L_0x010a
        L_0x00f5:
            android.widget.LinearLayout r6 = r5.mFootPanel
            float r6 = r6.getTranslationY()
            int r0 = r5.mExpandHeightThres
            int r0 = r0 / r3
            float r0 = (float) r0
            int r6 = (r6 > r0 ? 1 : (r6 == r0 ? 0 : -1))
            if (r6 < 0) goto L_0x0107
            r5.toBottomAnimation()
            goto L_0x010a
        L_0x0107:
            r5.toTopAnimation()
        L_0x010a:
            r5.recycleVelocityTracker()
            goto L_0x013a
        L_0x010e:
            r5.mMoved = r1
            boolean r0 = r5.isOrientationPortrait()
            if (r0 != 0) goto L_0x0117
            goto L_0x013a
        L_0x0117:
            float r0 = r6.getRawX()
            r5.mInitialTouchX = r0
            float r6 = r6.getRawY()
            r5.mInitialTouchY = r6
            com.android.systemui.controlcenter.phone.QSControlCenterTileLayout r6 = r5.mQuickQsControlCenterTileLayout
            r6.startMove()
            android.widget.LinearLayout r6 = r5.mFootPanel
            float r6 = r6.getTranslationY()
            r5.mBaseTransitionY = r6
            android.graphics.Rect r6 = new android.graphics.Rect
            r6.<init>()
            android.widget.ImageView r5 = r5.mExpandIndicatorBottom
            r5.getBoundsOnScreen(r6)
        L_0x013a:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.QSControlCenterPanel.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void performCollapseByClick(int i, int i2, boolean z) {
        if (!z && shouldCollapseByClick(i, i2)) {
            this.mControlPanelWindowView.collapsePanel(true);
        }
    }

    private boolean shouldCollapseByClick(int i, int i2) {
        Rect rect = new Rect();
        Rect rect2 = new Rect();
        if (isOrientationPortrait()) {
            this.mTilesContainer.getBoundsOnScreen(rect);
            this.mFootPanel.getBoundsOnScreen(rect2);
            int i3 = rect2.left;
            int i4 = this.mPaddingHorizontal;
            rect2.left = i3 + i4;
            rect2.right -= i4;
            if (!rect.contains(i, i2) && !rect2.contains(i, i2)) {
                return true;
            }
            float f = (float) i2;
            if (isHeaderAreaTouchDown(f) || isFootAreaTouchDown(f)) {
                return true;
            }
            return false;
        }
        this.mLandTiles.getBoundsOnScreen(rect);
        this.mTilesContainer.getBoundsOnScreen(rect2);
        if (rect.contains(i, i2) || rect2.contains(i, i2)) {
            return false;
        }
        return true;
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void updateLayout() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mQsControlScrollView.getLayoutParams();
        if (this.mOrientation == 1) {
            this.mLandTiles.removeView(this.mBigTiles);
            if (this.mTilesContainer.findViewById(C0015R$id.big_tiles) == null) {
                this.mTilesContainer.addView(this.mBigTiles, 0);
            }
            layoutParams.width = -1;
            this.mBigTilesScrollView.setVisibility(8);
            QSControlCenterHeaderView qSControlCenterHeaderView = this.mHeader;
            qSControlCenterHeaderView.setPadding(this.mPaddingHorizontal, qSControlCenterHeaderView.getPaddingTop(), this.mPaddingHorizontal, this.mHeader.getPaddingBottom());
            QSControlScrollView qSControlScrollView = this.mQsControlScrollView;
            qSControlScrollView.setPadding(this.mPaddingHorizontal, qSControlScrollView.getPaddingTop(), this.mPaddingHorizontal, this.mQsControlScrollView.getPaddingBottom());
            setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        } else {
            this.mTilesContainer.removeView(this.mBigTiles);
            if (this.mLandTiles.findViewById(C0015R$id.big_tiles) == null) {
                this.mLandTiles.addView(this.mBigTiles, 0);
            }
            this.mBigTilesScrollView.setVisibility(0);
            layoutParams.width = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land);
            QSControlScrollView qSControlScrollView2 = this.mQsControlScrollView;
            qSControlScrollView2.setPadding(0, qSControlScrollView2.getPaddingTop(), 0, this.mQsControlScrollView.getPaddingBottom());
            int dimensionPixelSize = ((int) ((float) ((CommonUtil.getScreenSize(this.mContext).x - (this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land) * 2)) - this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_land_tiles_margin_middle)))) / 2;
            QSControlCenterHeaderView qSControlCenterHeaderView2 = this.mHeader;
            qSControlCenterHeaderView2.setPadding(0, qSControlCenterHeaderView2.getPaddingTop(), 0, this.mHeader.getPaddingBottom());
            setPadding(dimensionPixelSize, getPaddingTop(), dimensionPixelSize, getPaddingBottom());
        }
        this.mQsControlScrollView.setLayoutParams(layoutParams);
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_height);
        int dimensionPixelSize3 = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tiles_interval_vertical);
        QSBigTileView qSBigTileView = this.mBigTile0;
        if (qSBigTileView != null) {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) qSBigTileView.getLayoutParams();
            layoutParams2.height = dimensionPixelSize2;
            this.mBigTile0.setLayoutParams(layoutParams2);
        } else {
            RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mExpandTileView.getLayoutParams();
            layoutParams3.height = dimensionPixelSize2;
            this.mExpandTileView.setLayoutParams(layoutParams3);
        }
        RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mBigTile1.getLayoutParams();
        layoutParams4.height = dimensionPixelSize2;
        this.mBigTile1.setLayoutParams(layoutParams4);
        RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mBigTile2.getLayoutParams();
        layoutParams5.height = dimensionPixelSize2;
        layoutParams5.topMargin = dimensionPixelSize3;
        this.mBigTile2.setLayoutParams(layoutParams5);
        RelativeLayout.LayoutParams layoutParams6 = (RelativeLayout.LayoutParams) this.mBigTile3.getLayoutParams();
        layoutParams6.height = dimensionPixelSize2;
        layoutParams6.topMargin = dimensionPixelSize3;
        this.mBigTile3.setLayoutParams(layoutParams6);
    }

    private void toBottomAnimation() {
        this.mFootPanelTransAnim.cancel(ViewProperty.TRANSLATION_Y);
        AnimState animState = new AnimState("foot_panel_trans");
        animState.add(ViewProperty.TRANSLATION_Y, this.mExpandHeightThres, new long[0]);
        AnimConfig animConfig = new AnimConfig();
        animConfig.addListeners(new TransitionListener() {
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                QSControlCenterPanel qSControlCenterPanel = QSControlCenterPanel.this;
                qSControlCenterPanel.setTransRatio(f / ((float) qSControlCenterPanel.mExpandHeightThres));
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCenterPanel.this.setTransRatio(1.0f);
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new ExpandQuickTilesEvent());
                QSControlCenterPanel.this.mQuickQsControlCenterTileLayout.setExpanded(true);
            }
        });
        this.mFootPanelTransAnim.to(animState, animConfig);
    }

    private void toTopAnimation() {
        this.mFootPanelTransAnim.cancel(ViewProperty.TRANSLATION_Y);
        AnimState animState = new AnimState("foot_panel_trans");
        animState.add(ViewProperty.TRANSLATION_Y, 0, new long[0]);
        AnimConfig animConfig = new AnimConfig();
        animConfig.addListeners(new TransitionListener() {
            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                QSControlCenterPanel qSControlCenterPanel = QSControlCenterPanel.this;
                qSControlCenterPanel.setTransRatio(f / ((float) qSControlCenterPanel.mExpandHeightThres));
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCenterPanel.this.setTransRatio(0.0f);
                QSControlCenterPanel.this.mQuickQsControlCenterTileLayout.setExpanded(false);
            }
        });
        this.mFootPanelTransAnim.to(animState, animConfig);
    }

    /* access modifiers changed from: private */
    public void setTransRatio(float f) {
        if (!this.mPanelController.isSuperPowerMode()) {
            float min = Math.min(1.0f, Math.max(0.0f, f));
            if (min == 1.0f) {
                this.mSmartControlsView.setVisibility(8);
            } else {
                this.mSmartControlsView.setVisibility(0);
                this.mSmartControlsView.setAlpha(1.0f - min);
            }
            this.mFootPanel.setTranslationY(((float) this.mExpandHeightThres) * min);
            this.mQuickQsControlCenterTileLayout.setExpandRatio(min);
            this.mQsControlScrollView.srcollTotratio(min);
            if (this.mExpandHeightThres <= this.mQuickQsControlCenterTileLayout.caculateExpandHeight() || min == 0.0f) {
                updateFootPanelLayoutBtRatio(min);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r1.mBigTile0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isBigTileTouched() {
        /*
            r1 = this;
            com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView r0 = r1.mExpandTileView
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
            com.android.systemui.controlcenter.qs.tileview.QSBigTileView r0 = r1.mBigTile0
            if (r0 == 0) goto L_0x0012
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
        L_0x0012:
            com.android.systemui.controlcenter.qs.tileview.QSBigTileView r0 = r1.mBigTile1
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
            com.android.systemui.controlcenter.qs.tileview.QSBigTileView r0 = r1.mBigTile2
            boolean r0 = r0.isClicked()
            if (r0 != 0) goto L_0x002d
            com.android.systemui.controlcenter.qs.tileview.QSBigTileView r1 = r1.mBigTile3
            boolean r1 = r1.isClicked()
            if (r1 == 0) goto L_0x002b
            goto L_0x002d
        L_0x002b:
            r1 = 0
            goto L_0x002e
        L_0x002d:
            r1 = 1
        L_0x002e:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.QSControlCenterPanel.isBigTileTouched():boolean");
    }

    public void setControlPanelWindowView(ControlPanelWindowView controlPanelWindowView) {
        this.mControlPanelWindowView = controlPanelWindowView;
    }

    private void updateScreenHeight() {
        Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenHeight = Math.max(point.y, point.x);
    }

    public void setQSDetailCallback(QSControlDetail.QSPanelCallback qSPanelCallback) {
        this.mDetailCallback = qSPanelCallback;
    }

    private void updateFootPanelLayout() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mFootPanel.getLayoutParams();
        int footPanelMarginTop = getFootPanelMarginTop();
        layoutParams.topMargin = footPanelMarginTop;
        this.mFootPanel.setLayoutParams(layoutParams);
        updateExpandHeightThres();
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "updateFootPanelLayout screenHeight:" + this.mScreenHeight + " topMargin:" + footPanelMarginTop + "  thres:" + this.mExpandHeightThres + "mOrientation:" + this.mOrientation);
        }
    }

    public void updateFootPanelLayoutBtRatio(float f) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mFootPanel.getLayoutParams();
        int round = Math.round(((float) ((this.mQuickQsControlCenterTileLayout.getMaxHeight() - this.mExpandHeightThres) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_center_tile_margin_top))) * f);
        layoutParams.topMargin = Math.round(((float) getFootPanelMarginTop()) * (1.0f - f)) + round;
        if (f == 1.0f) {
            this.mFootPanel.setTranslationY((float) this.mExpandHeightThres);
            layoutParams.height = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_foot_panel_brightness_height) + this.mExpandHeightThres;
        } else if (f == 0.0f) {
            layoutParams.height = -2;
        }
        this.mFootPanel.setLayoutParams(layoutParams);
        if (this.DEBUG) {
            Log.d("QSControlCenterPanel", "updateFootPanelLayout topMargin:" + round + "  layoutParams.height:" + layoutParams.height);
        }
    }

    public void updateExpandHeightThres() {
        int footPanelMarginTop = (this.mScreenHeight - (getFootPanelMarginTop() + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_foot_panel_to_top))) - this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_center_header_paddingTop);
        this.mDistanceToBottom = footPanelMarginTop;
        int min = Math.min(footPanelMarginTop, this.mQuickQsControlCenterTileLayout.caculateExpandHeight());
        this.mExpandHeightThres = min;
        this.mQuickQsControlCenterTileLayout.setExpandHeightThres(min);
    }

    private int getFootPanelMarginTop() {
        return this.mQuickQsControlCenterTileLayout.getMinHeight() + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_center_tile_margin_top);
    }

    public void showDetail(boolean z, MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0, miuiQSPanel$MiuiRecord).sendToTarget();
    }

    public void closeDetail(boolean z) {
        MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord = this.mDetailRecord;
        if (miuiQSPanel$MiuiRecord == null || !(miuiQSPanel$MiuiRecord instanceof MiuiQSPanel$MiuiTileRecord)) {
            MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord2 = this.mDetailRecord;
            if (miuiQSPanel$MiuiRecord2 != null) {
                showDetail(false, miuiQSPanel$MiuiRecord2);
                return;
            }
            return;
        }
        QSTile qSTile = ((MiuiQSPanel$MiuiTileRecord) miuiQSPanel$MiuiRecord).tile;
        if (qSTile instanceof QSTileImpl) {
            ((QSTileImpl) qSTile).showDetail(false);
        }
    }

    /* access modifiers changed from: protected */
    public void handleShowDetail(MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord, boolean z) {
        View view;
        if (miuiQSPanel$MiuiRecord instanceof MiuiQSPanel$MiuiTileRecord) {
            handleShowDetailTile((MiuiQSPanel$MiuiTileRecord) miuiQSPanel$MiuiRecord, z);
            return;
        }
        View view2 = miuiQSPanel$MiuiRecord.wholeView;
        if (view2 != null && (view = miuiQSPanel$MiuiRecord.translateView) != null) {
            handleShowDetailImpl(miuiQSPanel$MiuiRecord, z, view2, view);
        }
    }

    private void handleShowDetailTile(MiuiQSPanel$MiuiTileRecord miuiQSPanel$MiuiTileRecord, boolean z) {
        if ((this.mDetailRecord != null) != z || this.mDetailRecord != miuiQSPanel$MiuiTileRecord) {
            if (z) {
                DetailAdapter detailAdapter = miuiQSPanel$MiuiTileRecord.tile.getDetailAdapter();
                miuiQSPanel$MiuiTileRecord.detailAdapter = detailAdapter;
                if (detailAdapter == null) {
                    return;
                }
            }
            miuiQSPanel$MiuiTileRecord.tile.setDetailListening(z);
            handleShowDetailImpl(miuiQSPanel$MiuiTileRecord, z, miuiQSPanel$MiuiTileRecord.tileView, miuiQSPanel$MiuiTileRecord.expandIndicator);
        }
    }

    private void handleShowDetailImpl(MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord, boolean z, View view, View view2) {
        DetailAdapter detailAdapter = null;
        setDetailRecord(z ? miuiQSPanel$MiuiRecord : null);
        if (z) {
            detailAdapter = miuiQSPanel$MiuiRecord.detailAdapter;
        }
        fireShowingDetail(detailAdapter, view, view2);
        if (z) {
            this.mAnim.cancel();
            this.mAnim.to(FolmeAnimState.mHideAnim, FolmeAnimState.mAnimConfig);
            return;
        }
        this.mAnim.cancel();
        this.mAnim.to(FolmeAnimState.mShowAnim, FolmeAnimState.mAnimConfig);
    }

    /* access modifiers changed from: protected */
    public void setDetailRecord(MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord) {
        if (miuiQSPanel$MiuiRecord != this.mDetailRecord) {
            this.mDetailRecord = miuiQSPanel$MiuiRecord;
            fireScanStateChanged((miuiQSPanel$MiuiRecord instanceof MiuiQSPanel$MiuiTileRecord) && ((MiuiQSPanel$MiuiTileRecord) miuiQSPanel$MiuiRecord).scanState);
        }
    }

    public void fireShowingDetail(DetailAdapter detailAdapter, View view, View view2) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onShowingDetail(detailAdapter, view, view2);
        }
    }

    public void fireToggleStateChanged(boolean z) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onToggleStateChanged(z);
        }
    }

    public void fireScanStateChanged(boolean z) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onScanStateChanged(z);
        }
    }

    public void setExpand(boolean z, boolean z2) {
        this.mExpanded = z;
        if (isOrientationPortrait()) {
            this.mFootPanel.setVisibility(0);
        }
        if (this.mQuickQsControlCenterTileLayout.isExpanded() && !z) {
            onOrientationChanged(this.mOrientation, true);
        }
        if (z) {
            if (isOrientationPortrait()) {
                setTransRatio(0.0f);
            }
            this.mExpandIndicatorBottom.setAlpha(0.0f);
            this.mQsControlScrollView.fullScroll(33);
            this.mBigTilesScrollView.fullScroll(33);
        }
        setListening(z);
        if (z2) {
            panelAnimateOn(z);
        }
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            QSBigTileView qSBigTileView = this.mBigTile0;
            if (qSBigTileView != null) {
                qSBigTileView.handleSetListening(z);
            }
            this.mBigTile1.handleSetListening(z);
            this.mBigTile2.handleSetListening(z);
            this.mBigTile3.handleSetListening(z);
            setBrightnessListening(z);
            boolean z2 = true;
            this.mLandCtrFooter.setListening(z && !isOrientationPortrait());
            QSControlFooter qSControlFooter = this.mControlFooter;
            if (!z || !isOrientationPortrait()) {
                z2 = false;
            }
            qSControlFooter.setListening(z2);
        }
    }

    public void setBrightnessListening(boolean z) {
        if (isOrientationPortrait()) {
            this.mLandAutoBrightnessView.handleSetListening(false);
            this.mLandBrightnessController.unregisterCallbacks();
            this.mAutoBrightnessView.handleSetListening(z);
            if (z) {
                this.mBrightnessController.registerCallbacks();
            } else {
                this.mBrightnessController.unregisterCallbacks();
            }
        } else {
            this.mAutoBrightnessView.handleSetListening(false);
            this.mBrightnessController.unregisterCallbacks();
            this.mLandAutoBrightnessView.handleSetListening(z);
            if (z) {
                this.mLandBrightnessController.registerCallbacks();
            } else {
                this.mLandBrightnessController.unregisterCallbacks();
            }
        }
    }

    public void updateTransHeight(float f) {
        if (isOrientationPortrait()) {
            if (f == 0.0f) {
                this.mQuickQsControlCenterTileLayout.updateTransHeight(this.mTransViews, f, this.mScreenHeight, this.mTransLineNum);
                return;
            }
            int i = this.mScreenHeight;
            this.mTransViews.forEach(new Consumer(Math.max(0.0f, Math.min(f, (float) i)), i) {
                public final /* synthetic */ float f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void accept(Object obj) {
                    QSControlCenterPanel.this.lambda$updateTransHeight$0$QSControlCenterPanel(this.f$1, this.f$2, (View) obj);
                }
            });
            this.mQuickQsControlCenterTileLayout.updateTransHeight((List<View>) null, f, this.mScreenHeight, this.mTransLineNum);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateTransHeight$0 */
    public /* synthetic */ void lambda$updateTransHeight$0$QSControlCenterPanel(float f, int i, View view) {
        view.setTranslationY(ControlCenterUtils.getTranslationY(((Integer) view.getTag(C0015R$id.tag_control_center_trans)).intValue(), this.mTransLineNum, f, (float) i));
    }

    /* access modifiers changed from: protected */
    public void initAnimState() {
        this.mAnim = Folme.useAt(this).state();
        this.mFootPanelTransAnim = Folme.useAt(this.mFootPanel).state();
        FolmeAnimState.mAnimConfig.addListeners(new TransitionListener() {
            public void onBegin(Object obj) {
                super.onBegin(obj);
                QSControlCenterPanel.this.setLayerType(2, (Paint) null);
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCenterPanel.this.setLayerType(0, (Paint) null);
            }
        });
        updateViews();
    }

    private void updateViews() {
        this.mViews.clear();
        this.mTransViews.clear();
        this.mTransLineNum = (isOrientationPortrait() ? 7 : 5) + this.mQuickQsControlCenterTileLayout.getShowLines();
        addAnimateView(findViewById(C0015R$id.carrier_text), 0);
        addAnimateView(findViewById(C0015R$id.system_icon_area), 0);
        if (!this.mPanelController.isSuperPowerMode()) {
            addAnimateView(findViewById(C0015R$id.control_center_shortcut), 1);
        }
        addAnimateView(findViewById(C0015R$id.date_time), 1);
        addAnimateView(findViewById(C0015R$id.big_time), 1);
        addAnimateView(this.mTileView0, 2);
        addAnimateView(this.mBigTile1, 2);
        addAnimateView(this.mBigTile2, 3);
        addAnimateView(this.mBigTile3, 3);
        if (isOrientationPortrait()) {
            addAnimateView(findViewById(C0015R$id.auto_brightness), this.mFootPanelBaseIdx);
            addAnimateView(findViewById(C0015R$id.qs_brightness), this.mFootPanelBaseIdx);
            addAnimateView(this.mControlFooter.findViewById(C0015R$id.footer_text), this.mFootPanelBaseIdx);
            addAnimateView(this.mControlFooter.findViewById(C0015R$id.footer_icon), this.mFootPanelBaseIdx);
            if (!this.mPanelController.isSuperPowerMode()) {
                addAnimateView(findViewById(C0015R$id.qs_expand_indicator), this.mFootPanelBaseIdx - 1);
            }
        } else {
            addAnimateView(findViewById(C0015R$id.auto_brightness_land), this.mFootPanelBaseIdx);
            addAnimateView(findViewById(C0015R$id.qs_brightness_land), this.mFootPanelBaseIdx);
            addAnimateView(this.mLandCtrFooter.findViewById(C0015R$id.footer_text), this.mFootPanelBaseIdx);
            addAnimateView(this.mLandCtrFooter.findViewById(C0015R$id.footer_icon), this.mFootPanelBaseIdx);
        }
        addTransAnimateView(this.mHeader.findViewById(C0015R$id.panel_header), 0);
        addTransAnimateView(this.mHeader.findViewById(C0015R$id.tiles_header), 1);
        addTransAnimateView(this.mTileView0, 2);
        addTransAnimateView(this.mBigTile1, 2);
        addTransAnimateView(this.mBigTile2, 3);
        addTransAnimateView(this.mBigTile3, 3);
        if (isOrientationPortrait()) {
            addTransAnimateView(findViewById(C0015R$id.auto_brightness), this.mTransLineNum - 3);
            addTransAnimateView(findViewById(C0015R$id.qs_brightness), this.mTransLineNum - 3);
            addTransAnimateView(this.mSmartControlsView, this.mTransLineNum - 2);
            addTransAnimateView(this.mControlFooter.findViewById(C0015R$id.footer_text), this.mFootPanelBaseIdx);
            addTransAnimateView(this.mControlFooter.findViewById(C0015R$id.footer_icon), this.mFootPanelBaseIdx);
            if (!this.mPanelController.isSuperPowerMode()) {
                addTransAnimateView(findViewById(C0015R$id.qs_expand_indicator), this.mTransLineNum - 1);
            }
        } else {
            addTransAnimateView(findViewById(C0015R$id.auto_brightness_land), this.mFootPanelBaseIdx);
            addTransAnimateView(findViewById(C0015R$id.qs_brightness_land), this.mFootPanelBaseIdx);
            addTransAnimateView(this.mLandCtrFooter.findViewById(C0015R$id.footer_text), this.mFootPanelBaseIdx);
            addTransAnimateView(this.mLandCtrFooter.findViewById(C0015R$id.footer_icon), this.mFootPanelBaseIdx);
        }
        Iterator<View> it = this.mTransViews.iterator();
        while (it.hasNext()) {
            Folme.useAt(it.next());
        }
    }

    private void addAnimateView(View view, int i) {
        if (view != null && !this.mViews.contains(view)) {
            view.setTag(C0015R$id.tag_control_center, Integer.valueOf(i));
            this.mViews.add(view);
        }
    }

    private void addTransAnimateView(View view, int i) {
        if (view != null && !this.mTransViews.contains(view)) {
            view.setTag(C0015R$id.tag_control_center_trans, Integer.valueOf(i));
            this.mTransViews.add(view);
        }
    }

    private void panelAnimateOn(boolean z) {
        this.mQuickQsControlCenterTileLayout.visAnimOn(z);
        IStateStyle state = Folme.useAt((View[]) this.mTransViews.toArray(new View[0])).state();
        if (z) {
            if (!isOrientationPortrait() || this.mPanelController.isSuperPowerMode()) {
                Folme.useAt(this.mEditTiles).state();
            } else {
                Folme.useAt(this.mEditTiles).state().end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
            }
            state.fromTo(FolmeAnimState.mPanelHideAnim, FolmeAnimState.mPanelShowAnim, FolmeAnimState.mPanelAnimConfig);
            if (isOrientationPortrait() && !this.mPanelController.isSuperPowerMode()) {
                Folme.useAt(this.mEditTiles).state().fromTo(FolmeAnimState.mPanelHideAnim, FolmeAnimState.mPanelShowAnim, FolmeAnimState.mPanelAnimConfig);
                return;
            }
            return;
        }
        if (!isOrientationPortrait() || this.mPanelController.isSuperPowerMode()) {
            Folme.useAt(this.mEditTiles).state();
        } else {
            Folme.useAt(this.mEditTiles).state().end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
        }
        state.fromTo(FolmeAnimState.mPanelShowAnim, FolmeAnimState.mPanelHideAnim, FolmeAnimState.mPanelAnimConfig);
        if (isOrientationPortrait() && !this.mPanelController.isSuperPowerMode()) {
            Folme.useAt(this.mEditTiles).state().fromTo(FolmeAnimState.mPanelShowAnim, FolmeAnimState.mPanelHideAnim, FolmeAnimState.mPanelAnimConfig);
        }
    }

    public void finishCollapse() {
        if (this.mControlPanelContentView.getVisibility() == 0) {
            this.mControlPanelContentView.setVisibility(4);
        }
        if (isOrientationPortrait()) {
            this.mQuickQsControlCenterTileLayout.setExpanded(false);
        }
    }

    public void setTouchable(boolean z) {
        this.mTouchable = z;
    }

    class H extends Handler {
        H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                QSControlCenterPanel qSControlCenterPanel = QSControlCenterPanel.this;
                MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord = (MiuiQSPanel$MiuiRecord) message.obj;
                if (message.arg1 == 0) {
                    z = false;
                }
                qSControlCenterPanel.handleShowDetail(miuiQSPanel$MiuiRecord, z);
            } else if (i != 4 && i == 3) {
                QSControlCenterPanel.this.announceForAccessibility((CharSequence) message.obj);
            }
        }
    }
}
