package com.android.systemui.controlcenter.phone;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.phone.ControlCenterPanelViewController;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.phone.detail.QSControlDetail;
import com.android.systemui.controlcenter.phone.widget.ControlCenterBigTileGroup;
import com.android.systemui.controlcenter.phone.widget.ControlCenterBrightnessView;
import com.android.systemui.controlcenter.phone.widget.ControlCenterContentContainer;
import com.android.systemui.controlcenter.phone.widget.ControlCenterFooterPanel;
import com.android.systemui.controlcenter.phone.widget.ControlCenterTilesContainer;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiRecord;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiTileRecord;
import com.android.systemui.controlcenter.phone.widget.QSControlCenterHeaderView;
import com.android.systemui.controlcenter.policy.NCSwitchController;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ConfigurationController;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterPanelView.kt */
public final class ControlCenterPanelView extends LinearLayout {
    @NotNull
    private ControlCenterBigTileGroup bigTileLayout;
    @NotNull
    private ControlCenterBrightnessView brightnessView;
    @NotNull
    private LinearLayout ccContainer;
    @NotNull
    private ControlCenterContentContainer contentContainer;
    @Nullable
    private ControlPanelWindowView controlPanelWindowView;
    private final ControlsPluginManager controlsPluginManager;
    private QSControlDetail.QSPanelCallback detailCallback;
    private MiuiQSPanel$MiuiRecord detailRecord;
    @NotNull
    private ControlCenterFooterPanel footer;
    private final H handler;
    @NotNull
    private QSControlCenterHeaderView header;
    private MotionEvent lastEvent;
    private boolean listening;
    private final ControlPanelController panelController;
    private final ControlCenterPanelViewController panelViewController;
    @NotNull
    private LinearLayout smartHomeContainer;
    @NotNull
    private ControlCenterTilesContainer tileContainer;
    @NotNull
    private QSControlCenterTileLayout tileLayout;
    private ControlCenterPanelViewController.TouchHandler touchHandler;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlCenterPanelView(@NotNull Context context, @Nullable AttributeSet attributeSet, @NotNull Looper looper, @NotNull ConfigurationController configurationController, @NotNull ControlsPluginManager controlsPluginManager2, @NotNull ControlPanelController controlPanelController, @NotNull NCSwitchController nCSwitchController, @NotNull StatusBarStateController statusBarStateController) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(looper, "uiLooper");
        Intrinsics.checkParameterIsNotNull(configurationController, "configurationController");
        Intrinsics.checkParameterIsNotNull(controlsPluginManager2, "controlsPluginManager");
        Intrinsics.checkParameterIsNotNull(controlPanelController, "panelController");
        Intrinsics.checkParameterIsNotNull(nCSwitchController, "ncSwitchController");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "statusBarStateController");
        this.controlsPluginManager = controlsPluginManager2;
        this.panelController = controlPanelController;
        this.handler = new H(this, looper);
        this.panelViewController = new ControlCenterPanelViewController(context, this, configurationController, this.panelController, nCSwitchController, statusBarStateController);
    }

    @NotNull
    public final ControlCenterFooterPanel getFooter() {
        ControlCenterFooterPanel controlCenterFooterPanel = this.footer;
        if (controlCenterFooterPanel != null) {
            return controlCenterFooterPanel;
        }
        Intrinsics.throwUninitializedPropertyAccessException("footer");
        throw null;
    }

    @NotNull
    public final ControlCenterContentContainer getContentContainer() {
        ControlCenterContentContainer controlCenterContentContainer = this.contentContainer;
        if (controlCenterContentContainer != null) {
            return controlCenterContentContainer;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contentContainer");
        throw null;
    }

    @NotNull
    public final ControlCenterTilesContainer getTileContainer() {
        ControlCenterTilesContainer controlCenterTilesContainer = this.tileContainer;
        if (controlCenterTilesContainer != null) {
            return controlCenterTilesContainer;
        }
        Intrinsics.throwUninitializedPropertyAccessException("tileContainer");
        throw null;
    }

    @NotNull
    public final ControlCenterBigTileGroup getBigTileLayout() {
        ControlCenterBigTileGroup controlCenterBigTileGroup = this.bigTileLayout;
        if (controlCenterBigTileGroup != null) {
            return controlCenterBigTileGroup;
        }
        Intrinsics.throwUninitializedPropertyAccessException("bigTileLayout");
        throw null;
    }

    @NotNull
    public final QSControlCenterTileLayout getTileLayout() {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
        if (qSControlCenterTileLayout != null) {
            return qSControlCenterTileLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
        throw null;
    }

    @NotNull
    public final QSControlCenterHeaderView getHeader() {
        QSControlCenterHeaderView qSControlCenterHeaderView = this.header;
        if (qSControlCenterHeaderView != null) {
            return qSControlCenterHeaderView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("header");
        throw null;
    }

    @NotNull
    public final LinearLayout getCcContainer() {
        LinearLayout linearLayout = this.ccContainer;
        if (linearLayout != null) {
            return linearLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("ccContainer");
        throw null;
    }

    @NotNull
    public final LinearLayout getSmartHomeContainer() {
        LinearLayout linearLayout = this.smartHomeContainer;
        if (linearLayout != null) {
            return linearLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("smartHomeContainer");
        throw null;
    }

    @NotNull
    public final ControlCenterBrightnessView getBrightnessView() {
        ControlCenterBrightnessView controlCenterBrightnessView = this.brightnessView;
        if (controlCenterBrightnessView != null) {
            return controlCenterBrightnessView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
        throw null;
    }

    public final void setListening(boolean z) {
        if (this.listening != z) {
            this.listening = z;
            ControlCenterBigTileGroup controlCenterBigTileGroup = this.bigTileLayout;
            if (controlCenterBigTileGroup != null) {
                controlCenterBigTileGroup.setListening(z);
                ControlCenterBrightnessView controlCenterBrightnessView = this.brightnessView;
                if (controlCenterBrightnessView != null) {
                    controlCenterBrightnessView.setListening(z);
                    ControlCenterFooterPanel controlCenterFooterPanel = this.footer;
                    if (controlCenterFooterPanel != null) {
                        controlCenterFooterPanel.setListening(z);
                        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
                        if (qSControlCenterTileLayout != null) {
                            qSControlCenterTileLayout.setListening(z);
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("footer");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("bigTileLayout");
                throw null;
            }
        }
    }

    public final void setHost(@Nullable QSTileHost qSTileHost) {
        ControlCenterFooterPanel controlCenterFooterPanel = this.footer;
        if (controlCenterFooterPanel != null) {
            controlCenterFooterPanel.getSettingsFooter().setHostEnvironment(qSTileHost);
            ControlCenterBigTileGroup controlCenterBigTileGroup = this.bigTileLayout;
            if (controlCenterBigTileGroup != null) {
                controlCenterBigTileGroup.setHost(qSTileHost);
                ControlCenterBrightnessView controlCenterBrightnessView = this.brightnessView;
                if (controlCenterBrightnessView != null) {
                    controlCenterBrightnessView.setHost(qSTileHost);
                    ControlCenterTilesContainer controlCenterTilesContainer = this.tileContainer;
                    if (controlCenterTilesContainer != null) {
                        controlCenterTilesContainer.setHost(qSTileHost);
                        this.panelViewController.notifyOrientationChanged();
                        return;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("tileContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("bigTileLayout");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("footer");
        throw null;
    }

    private final void setDetailRecord(MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord) {
        boolean z = true;
        if (!Intrinsics.areEqual(this.detailRecord, miuiQSPanel$MiuiRecord)) {
            this.detailRecord = miuiQSPanel$MiuiRecord;
            if (!(miuiQSPanel$MiuiRecord instanceof MiuiQSPanel$MiuiTileRecord) || !((MiuiQSPanel$MiuiTileRecord) miuiQSPanel$MiuiRecord).scanState) {
                z = false;
            }
            fireScanStateChanged(z);
        }
    }

    public final void setControlPanelContentView(@Nullable ControlPanelContentView controlPanelContentView) {
        if (controlPanelContentView != null) {
            ControlCenterBrightnessView controlCenterBrightnessView = this.brightnessView;
            if (controlCenterBrightnessView != null) {
                controlCenterBrightnessView.setControlPanelContentView(controlPanelContentView);
                controlPanelContentView.getDetailView().setQsPanel(this);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
            throw null;
        }
    }

    @Nullable
    public final ControlPanelWindowView getControlPanelWindowView() {
        return this.controlPanelWindowView;
    }

    public final void setControlPanelWindowView(@Nullable ControlPanelWindowView controlPanelWindowView2) {
        this.controlPanelWindowView = controlPanelWindowView2;
    }

    public final boolean isExpanded() {
        ControlPanelWindowView controlPanelWindowView2 = this.controlPanelWindowView;
        if (controlPanelWindowView2 != null) {
            return controlPanelWindowView2.isExpanded();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View requireViewById = requireViewById(C0015R$id.header);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(R.id.header)");
        this.header = (QSControlCenterHeaderView) requireViewById;
        View requireViewById2 = requireViewById(C0015R$id.foot_panel);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.foot_panel)");
        this.footer = (ControlCenterFooterPanel) requireViewById2;
        View requireViewById3 = requireViewById(C0015R$id.content_springer);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById(R.id.content_springer)");
        this.contentContainer = (ControlCenterContentContainer) requireViewById3;
        View requireViewById4 = requireViewById(C0015R$id.tiles_springer);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "requireViewById(R.id.tiles_springer)");
        this.tileContainer = (ControlCenterTilesContainer) requireViewById4;
        View requireViewById5 = requireViewById(C0015R$id.big_tiles);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById5, "requireViewById(R.id.big_tiles)");
        this.bigTileLayout = (ControlCenterBigTileGroup) requireViewById5;
        View requireViewById6 = requireViewById(C0015R$id.tile_layout);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById6, "requireViewById(R.id.tile_layout)");
        this.tileLayout = (QSControlCenterTileLayout) requireViewById6;
        View requireViewById7 = requireViewById(C0015R$id.cc_content);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById7, "requireViewById(R.id.cc_content)");
        this.ccContainer = (LinearLayout) requireViewById7;
        View requireViewById8 = requireViewById(C0015R$id.brightness_container);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById8, "requireViewById(R.id.brightness_container)");
        this.brightnessView = (ControlCenterBrightnessView) requireViewById8;
        View requireViewById9 = requireViewById(C0015R$id.smart_home_container);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById9, "requireViewById(R.id.smart_home_container)");
        this.smartHomeContainer = (LinearLayout) requireViewById9;
        this.panelViewController.onFinishInflate();
        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.setPanelView(this);
            ControlCenterBigTileGroup controlCenterBigTileGroup = this.bigTileLayout;
            if (controlCenterBigTileGroup != null) {
                controlCenterBigTileGroup.init(this);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("bigTileLayout");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
            throw null;
        }
    }

    public final void onUserSwitched(int i) {
        ControlCenterBigTileGroup controlCenterBigTileGroup = this.bigTileLayout;
        if (controlCenterBigTileGroup != null) {
            controlCenterBigTileGroup.onUserSwitched(i);
            ControlCenterBrightnessView controlCenterBrightnessView = this.brightnessView;
            if (controlCenterBrightnessView != null) {
                controlCenterBrightnessView.onUserSwitched(i);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("bigTileLayout");
            throw null;
        }
    }

    public final void addControlsPlugin() {
        ControlsPluginManager controlsPluginManager2 = this.controlsPluginManager;
        if (!this.panelController.isSuperPowerMode()) {
            LinearLayout linearLayout = this.smartHomeContainer;
            if (linearLayout != null) {
                linearLayout.suppressLayout(true);
                LinearLayout linearLayout2 = this.smartHomeContainer;
                if (linearLayout2 != null) {
                    linearLayout2.removeAllViews();
                    View controlsView = controlsPluginManager2.getControlsView();
                    if (controlsView != null) {
                        LinearLayout linearLayout3 = this.smartHomeContainer;
                        if (linearLayout3 != null) {
                            linearLayout3.addView(controlsView);
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("smartHomeContainer");
                            throw null;
                        }
                    }
                    LinearLayout linearLayout4 = this.smartHomeContainer;
                    if (linearLayout4 != null) {
                        linearLayout4.suppressLayout(false);
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("smartHomeContainer");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("smartHomeContainer");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("smartHomeContainer");
                throw null;
            }
        }
    }

    public final void removeControlsPlugin() {
        ControlsPluginManager controlsPluginManager2 = this.controlsPluginManager;
        LinearLayout linearLayout = this.smartHomeContainer;
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            controlsPluginManager2.hideControlView();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("smartHomeContainer");
        throw null;
    }

    public final void setTouchable(boolean z) {
        this.panelViewController.setTouchable(z);
    }

    public final void performCollapseByClick() {
        ControlPanelWindowView controlPanelWindowView2 = this.controlPanelWindowView;
        if (controlPanelWindowView2 != null) {
            controlPanelWindowView2.collapsePanel(true);
        }
    }

    public final boolean shouldCollapseByBottomTouch() {
        return this.panelViewController.getTransRatio() < 1.0f || !this.panelViewController.isPortrait();
    }

    public final void showPanel(boolean z, boolean z2) {
        MotionEvent motionEvent;
        this.panelViewController.cancelTransAnim();
        if (this.panelViewController.isPortrait()) {
            this.panelController.showDialog(z);
        }
        this.panelViewController.getPanelAnimator().animateShowPanel(z);
        if (!z && (motionEvent = this.lastEvent) != null) {
            super.dispatchTouchEvent(motionEvent);
        }
        setListening(z);
    }

    public final void finishCollapse() {
        removeControlsPlugin();
        this.panelViewController.resetTransRatio();
    }

    public final void notifyTileChanged() {
        this.panelViewController.getPanelAnimator().notifyTileChanged();
    }

    public final void updateTransHeight(float f) {
        if (this.panelViewController.couldOverFling()) {
            this.panelViewController.getPanelAnimator().updateOverExpandHeight(f);
        }
    }

    public final void updateResources() {
        QSControlCenterHeaderView qSControlCenterHeaderView = this.header;
        if (qSControlCenterHeaderView != null) {
            qSControlCenterHeaderView.updateResources();
            QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
            if (qSControlCenterTileLayout != null) {
                qSControlCenterTileLayout.updateResources();
                ControlCenterBigTileGroup controlCenterBigTileGroup = this.bigTileLayout;
                if (controlCenterBigTileGroup != null) {
                    controlCenterBigTileGroup.updateResources();
                    ControlCenterBrightnessView controlCenterBrightnessView = this.brightnessView;
                    if (controlCenterBrightnessView != null) {
                        controlCenterBrightnessView.updateResources();
                        ControlCenterFooterPanel controlCenterFooterPanel = this.footer;
                        if (controlCenterFooterPanel != null) {
                            controlCenterFooterPanel.updateResources();
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("footer");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("brightnessView");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("bigTileLayout");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("header");
            throw null;
        }
    }

    public final void setQSDetailCallback(@NotNull QSControlDetail.QSPanelCallback qSPanelCallback) {
        Intrinsics.checkParameterIsNotNull(qSPanelCallback, "callback");
        this.detailCallback = qSPanelCallback;
    }

    public final void showDetail(boolean z, @Nullable MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord) {
        this.handler.obtainMessage(1, z ? 1 : 0, 0, miuiQSPanel$MiuiRecord).sendToTarget();
    }

    public final void closeDetail(boolean z) {
        MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord = this.detailRecord;
        if (miuiQSPanel$MiuiRecord == null || !(miuiQSPanel$MiuiRecord instanceof MiuiQSPanel$MiuiTileRecord)) {
            MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord2 = this.detailRecord;
            if (miuiQSPanel$MiuiRecord2 != null) {
                showDetail(false, miuiQSPanel$MiuiRecord2);
            }
        } else if (miuiQSPanel$MiuiRecord != null) {
            QSTile qSTile = ((MiuiQSPanel$MiuiTileRecord) miuiQSPanel$MiuiRecord).tile;
            if (qSTile instanceof QSTileImpl) {
                ((QSTileImpl) qSTile).showDetail(false);
            }
        } else {
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controlcenter.phone.widget.MiuiQSPanel.MiuiTileRecord");
        }
    }

    public final void handleShowDetail(@NotNull MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord, boolean z) {
        Intrinsics.checkParameterIsNotNull(miuiQSPanel$MiuiRecord, "r");
        if (miuiQSPanel$MiuiRecord instanceof MiuiQSPanel$MiuiTileRecord) {
            handleShowDetailTile((MiuiQSPanel$MiuiTileRecord) miuiQSPanel$MiuiRecord, z);
            return;
        }
        View view = miuiQSPanel$MiuiRecord.wholeView;
        if (view != null && miuiQSPanel$MiuiRecord.translateView != null) {
            Intrinsics.checkExpressionValueIsNotNull(view, "r.wholeView");
            View view2 = miuiQSPanel$MiuiRecord.translateView;
            Intrinsics.checkExpressionValueIsNotNull(view2, "r.translateView");
            handleShowDetailImpl(miuiQSPanel$MiuiRecord, z, view, view2);
        }
    }

    private final void handleShowDetailTile(MiuiQSPanel$MiuiTileRecord miuiQSPanel$MiuiTileRecord, boolean z) {
        if ((this.detailRecord != null) != z || this.detailRecord != miuiQSPanel$MiuiTileRecord) {
            if (z) {
                QSTile qSTile = miuiQSPanel$MiuiTileRecord.tile;
                Intrinsics.checkExpressionValueIsNotNull(qSTile, "r.tile");
                DetailAdapter detailAdapter = qSTile.getDetailAdapter();
                miuiQSPanel$MiuiTileRecord.detailAdapter = detailAdapter;
                if (detailAdapter == null) {
                    return;
                }
            }
            miuiQSPanel$MiuiTileRecord.tile.setDetailListening(z);
            QSTileView qSTileView = miuiQSPanel$MiuiTileRecord.tileView;
            Intrinsics.checkExpressionValueIsNotNull(qSTileView, "r.tileView");
            View view = miuiQSPanel$MiuiTileRecord.expandIndicator;
            Intrinsics.checkExpressionValueIsNotNull(view, "r.expandIndicator");
            handleShowDetailImpl(miuiQSPanel$MiuiTileRecord, z, qSTileView, view);
        }
    }

    private final void handleShowDetailImpl(MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord, boolean z, View view, View view2) {
        DetailAdapter detailAdapter = null;
        setDetailRecord(z ? miuiQSPanel$MiuiRecord : null);
        if (z) {
            detailAdapter = miuiQSPanel$MiuiRecord.detailAdapter;
        }
        fireShowingDetail(detailAdapter, view, view2);
        this.panelViewController.getPanelAnimator().animateShowPanelWithoutScale(!z);
    }

    public final void fireScanStateChanged(boolean z) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.detailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onScanStateChanged(z);
        }
    }

    public final void fireToggleStateChanged(boolean z) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.detailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onToggleStateChanged(z);
        }
    }

    private final void fireShowingDetail(DetailAdapter detailAdapter, View view, View view2) {
        QSControlDetail.QSPanelCallback qSPanelCallback = this.detailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onShowingDetail(detailAdapter, view, view2);
        }
    }

    public final void clickTile(@Nullable ComponentName componentName) {
        ControlCenterTilesContainer controlCenterTilesContainer = this.tileContainer;
        if (controlCenterTilesContainer != null) {
            controlCenterTilesContainer.getTileLayout().clickTile(componentName);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("tileContainer");
            throw null;
        }
    }

    @NotNull
    public WindowInsets onApplyWindowInsets(@NotNull WindowInsets windowInsets) {
        Intrinsics.checkParameterIsNotNull(windowInsets, "insets");
        this.panelViewController.onApplyWindowInsets(windowInsets);
        WindowInsets onApplyWindowInsets = super.onApplyWindowInsets(windowInsets);
        Intrinsics.checkExpressionValueIsNotNull(onApplyWindowInsets, "super.onApplyWindowInsets(insets)");
        return onApplyWindowInsets;
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public final void setOnTouchListener(@Nullable ControlCenterPanelViewController.TouchHandler touchHandler2) {
        super.setOnTouchListener((View.OnTouchListener) touchHandler2);
        this.touchHandler = touchHandler2;
    }

    public boolean dispatchTouchEvent(@NotNull MotionEvent motionEvent) {
        Boolean dispatchTouchEvent;
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        ControlCenterPanelViewController.TouchHandler touchHandler2 = this.touchHandler;
        if (touchHandler2 == null || (dispatchTouchEvent = touchHandler2.dispatchTouchEvent(motionEvent)) == null) {
            if (motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1) {
                this.lastEvent = null;
            } else {
                MotionEvent obtain = MotionEvent.obtain(motionEvent);
                this.lastEvent = obtain;
                if (obtain != null) {
                    obtain.setAction(3);
                }
            }
            return super.dispatchTouchEvent(motionEvent);
        }
        dispatchTouchEvent.booleanValue();
        if (!dispatchTouchEvent.booleanValue()) {
            MotionEvent obtain2 = MotionEvent.obtain(motionEvent);
            Intrinsics.checkExpressionValueIsNotNull(obtain2, "copy");
            obtain2.setAction(3);
            super.dispatchTouchEvent(obtain2);
            obtain2.recycle();
        }
        return dispatchTouchEvent.booleanValue();
    }

    public boolean onInterceptTouchEvent(@NotNull MotionEvent motionEvent) {
        Boolean onInterceptTouchEvent;
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        ControlCenterPanelViewController.TouchHandler touchHandler2 = this.touchHandler;
        if (touchHandler2 == null || (onInterceptTouchEvent = touchHandler2.onInterceptTouchEvent(motionEvent)) == null) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        onInterceptTouchEvent.booleanValue();
        return onInterceptTouchEvent.booleanValue();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.performAttachedToWindow();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
            throw null;
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        QSControlCenterTileLayout qSControlCenterTileLayout = this.tileLayout;
        if (qSControlCenterTileLayout != null) {
            qSControlCenterTileLayout.performDetachedFromWindow();
            super.onDetachedFromWindow();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("tileLayout");
        throw null;
    }

    /* compiled from: ControlCenterPanelView.kt */
    public final class H extends Handler {
        final /* synthetic */ ControlCenterPanelView this$0;

        /* JADX INFO: super call moved to the top of the method (can break code semantics) */
        public H(@NotNull ControlCenterPanelView controlCenterPanelView, Looper looper) {
            super(looper);
            Intrinsics.checkParameterIsNotNull(looper, "looper");
            this.this$0 = controlCenterPanelView;
        }

        public void handleMessage(@NotNull Message message) {
            Intrinsics.checkParameterIsNotNull(message, "msg");
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                ControlCenterPanelView controlCenterPanelView = this.this$0;
                Object obj = message.obj;
                if (obj != null) {
                    MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord = (MiuiQSPanel$MiuiRecord) obj;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    controlCenterPanelView.handleShowDetail(miuiQSPanel$MiuiRecord, z);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controlcenter.phone.widget.MiuiQSPanel.MiuiRecord");
            } else if (i == 3) {
                ControlCenterPanelView controlCenterPanelView2 = this.this$0;
                Object obj2 = message.obj;
                if (obj2 != null) {
                    controlCenterPanelView2.announceForAccessibility((CharSequence) obj2);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
            }
        }
    }
}
