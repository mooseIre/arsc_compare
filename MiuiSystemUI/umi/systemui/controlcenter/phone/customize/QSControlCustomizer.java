package com.android.systemui.controlcenter.phone.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlPanelContentView;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.TileQueryHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;
import miuix.recyclerview.widget.MiuiDefaultItemAnimator;

public class QSControlCustomizer extends FrameLayout implements TileQueryHelper.TileStateListener, QSTile.Callback {
    /* access modifiers changed from: private */
    public boolean isShown = false;
    private IStateStyle mAnim;
    private final Animator.AnimatorListener mCollapseAnimationListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            if (!QSControlCustomizer.this.isShown) {
                QSControlCustomizer.this.setVisibility(8);
            }
            QSControlCustomizer.this.setCustomizerAnimating(false);
            QSControlCustomizer.this.mRecyclerView.setAdapter(QSControlCustomizer.this.mTileAdapter);
            QSControlCustomizer.this.mOthersRecyclerView.setAdapter(QSControlCustomizer.this.mOtherTilesAdapter);
        }

        public void onAnimationCancel(Animator animator) {
            if (!QSControlCustomizer.this.isShown) {
                QSControlCustomizer.this.setVisibility(8);
            }
            QSControlCustomizer.this.setCustomizerAnimating(false);
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mCustomizerAnimating;
    protected TextView mDoneButton;
    /* access modifiers changed from: private */
    public boolean mDonedClicked;
    private final Animator.AnimatorListener mExpandAnimationListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            if (QSControlCustomizer.this.isShown) {
                QSControlCustomizer.this.setCustomizing(true);
            }
            boolean unused = QSControlCustomizer.this.mOpening = false;
            QSControlCustomizer.this.setCustomizerAnimating(false);
        }

        public void onAnimationCancel(Animator animator) {
            boolean unused = QSControlCustomizer.this.mOpening = false;
            QSControlCustomizer.this.setCustomizerAnimating(false);
        }
    };
    protected LinearLayout mHeader;
    private AnimState mHideAnim;
    /* access modifiers changed from: private */
    public QSTileHost mHost;
    /* access modifiers changed from: private */
    public final MiuiKeyguardUpdateMonitorCallback mKeyguardCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onKeyguardShowingChanged(boolean z) {
            if (QSControlCustomizer.this.isAttachedToWindow() && z && !QSControlCustomizer.this.mOpening) {
                QSControlCustomizer.this.mQsPanelCallback.hide();
            }
        }
    };
    private boolean mLayoutParamsInited;
    /* access modifiers changed from: private */
    public boolean mOpening;
    /* access modifiers changed from: private */
    public CCTileAdapter mOtherTilesAdapter;
    /* access modifiers changed from: private */
    public RecyclerView mOthersRecyclerView;
    protected ControlCenterPanelView mQSCenterPanel;
    protected QSControlPanelCallback mQsPanelCallback = new QSControlPanelCallback() {
        public void show() {
            if (!QSControlCustomizer.this.isShown) {
                MetricsLogger.visible(QSControlCustomizer.this.getContext(), 358);
                boolean unused = QSControlCustomizer.this.isShown = true;
                boolean unused2 = QSControlCustomizer.this.mShownRequested = true;
                boolean unused3 = QSControlCustomizer.this.mOpening = true;
                QSControlCustomizer.this.setTileSpecs();
                QSControlCustomizer.this.queryTiles();
                QSControlCustomizer.this.setCustomizerAnimating(true);
                QSControlCustomizer qSControlCustomizer = QSControlCustomizer.this;
                qSControlCustomizer.announceForAccessibility(qSControlCustomizer.mContext.getString(C0021R$string.accessibility_desc_quick_settings_edit));
                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(QSControlCustomizer.this.mKeyguardCallback);
            }
        }

        public void hide() {
            if (QSControlCustomizer.this.isShown) {
                if (QSControlCustomizer.this.mTileAdapter != null) {
                    QSControlCustomizer.this.mTileAdapter.isTileMoved();
                    QSControlCustomizer.this.mTileAdapter.resetTileMoved();
                }
                boolean access$000 = QSControlCustomizer.this.mDonedClicked;
                QSControlCustomizer.this.resetDataTrackStates();
                MetricsLogger.hidden(QSControlCustomizer.this.getContext(), 358);
                boolean unused = QSControlCustomizer.this.isShown = false;
                boolean unused2 = QSControlCustomizer.this.mShownRequested = false;
                QSControlCustomizer.this.setCustomizing(false);
                QSControlCustomizer.this.mUnAddedTilesLayout.resetMargin();
                QSControlCustomizer.this.startAnimation(false);
                QSControlCustomizer.this.releaseTiles();
                QSControlCustomizer.this.setCustomizerAnimating(true);
                QSControlCustomizer qSControlCustomizer = QSControlCustomizer.this;
                qSControlCustomizer.announceForAccessibility(qSControlCustomizer.mContext.getString(C0021R$string.accessibility_desc_quick_settings));
                ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(QSControlCustomizer.this.mKeyguardCallback);
            }
        }
    };
    /* access modifiers changed from: private */
    public RecyclerView mRecyclerView;
    private AnimState mShowAnim;
    /* access modifiers changed from: private */
    public boolean mShownRequested;
    private int mSpanCount;
    private List<String> mSpecs = new ArrayList();
    protected TextView mSubTitle;
    /* access modifiers changed from: private */
    public CCTileAdapter mTileAdapter;
    private final CCTileQueryHelper mTileQueryHelper;
    private int mTileVerticalIntervel;
    protected TextView mTitle;
    /* access modifiers changed from: private */
    public UnAddedTilesLayout mUnAddedTilesLayout;

    public interface QSControlPanelCallback {
        void hide();

        void show();
    }

    /* access modifiers changed from: private */
    public void setCustomizing(boolean z) {
    }

    public void onAnnouncementRequested(CharSequence charSequence) {
    }

    public void onScanStateChanged(boolean z) {
    }

    public void onShowDetail(boolean z) {
    }

    public void onStateChanged(QSTile.State state) {
    }

    public void onToggleStateChanged(boolean z) {
    }

    public QSControlCustomizer(Context context, AttributeSet attributeSet, CCTileQueryHelper cCTileQueryHelper, KeyguardUpdateMonitorInjector keyguardUpdateMonitorInjector) {
        super(context, attributeSet);
        this.mContext = context;
        setClickable(true);
        this.mSpanCount = this.mContext.getResources().getInteger(C0012R$dimen.qs_control_tiles_columns);
        this.mTileVerticalIntervel = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_margin_bottom);
        LayoutInflater.from(getContext()).inflate(C0017R$layout.qs_control_customize_panel_content, this);
        this.mRecyclerView = (RecyclerView) findViewById(C0015R$id.list_added);
        CCTileAdapter cCTileAdapter = new CCTileAdapter(getContext(), this.mSpanCount, this.mRecyclerView, true);
        this.mTileAdapter = cCTileAdapter;
        cCTileAdapter.setQsControlCustomizer(this);
        this.mTileQueryHelper = cCTileQueryHelper;
        cCTileQueryHelper.setListener(this);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        this.mOthersRecyclerView = (RecyclerView) findViewById(C0015R$id.list_others);
        CCTileAdapter cCTileAdapter2 = new CCTileAdapter(getContext(), this.mSpanCount, this.mOthersRecyclerView, false);
        this.mOtherTilesAdapter = cCTileAdapter2;
        cCTileAdapter2.setQsControlCustomizer(this);
        this.mOthersRecyclerView.setAdapter(this.mOtherTilesAdapter);
        updateLayout();
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        this.mOthersRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        this.mRecyclerView.setItemAnimator(new MiuiDefaultItemAnimator());
        this.mOthersRecyclerView.setItemAnimator(new MiuiDefaultItemAnimator());
        UnAddedTilesLayout unAddedTilesLayout = (UnAddedTilesLayout) findViewById(C0015R$id.unAdded_tiles);
        this.mUnAddedTilesLayout = unAddedTilesLayout;
        unAddedTilesLayout.setAddedLayout(this.mRecyclerView);
        TextView textView = (TextView) findViewById(C0015R$id.save);
        this.mDoneButton = textView;
        ControlCenterUtils.createButtonFolmeTouchStyle(textView);
        this.mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = QSControlCustomizer.this.mDonedClicked = true;
                QSControlCustomizer.this.mQsPanelCallback.hide();
                QSControlCustomizer.this.mHost.getHostInjector().setMiuiQSTilesEdited();
            }
        });
        this.mHeader = (LinearLayout) findViewById(C0015R$id.header);
        this.mTitle = (TextView) findViewById(C0015R$id.title);
        this.mSubTitle = (TextView) findViewById(C0015R$id.sub_title);
        this.mSpanCount = -1;
        onConfigurationChanged(getResources().getConfiguration());
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mAnim = Folme.useAt(this).state();
        AnimState animState = new AnimState("qs_control_customizer_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState.add(ViewProperty.TRANSLATION_Y, 0, new long[0]);
        this.mShowAnim = animState;
        AnimState animState2 = new AnimState("qs_control_customizer_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState2.add(ViewProperty.TRANSLATION_Y, 100, new long[0]);
        this.mHideAnim = animState2;
        if (!this.mLayoutParamsInited) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mHeader.getLayoutParams();
            this.mUnAddedTilesLayout.setMarginTop(this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_title_height) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_sub_title_height) + layoutParams.topMargin + layoutParams.bottomMargin + this.mTileVerticalIntervel + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_content_margin_top) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tile_height), this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_title_height) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_sub_title_height) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tiles_content_margin_top) + layoutParams.topMargin + layoutParams.bottomMargin + (this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_customizer_tile_height) * 4) + (this.mTileVerticalIntervel * 4));
            RecyclerView recyclerView = this.mRecyclerView;
            recyclerView.setPadding(recyclerView.getPaddingLeft(), this.mRecyclerView.getPaddingTop(), this.mRecyclerView.getPaddingRight(), this.mUnAddedTilesLayout.getHeight());
            this.mRecyclerView.requestLayout();
            this.mLayoutParamsInited = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
    }

    public void requestLayout() {
        if (!isInLayout() && getParent() != null && getParent().isLayoutRequested()) {
            for (ViewParent parent = getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof View) {
                    ((View) parent).mPrivateFlags &= -4097;
                }
            }
        }
        super.requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTitle.setText(C0021R$string.qs_control_customize_title);
        this.mSubTitle.setText(C0021R$string.qs_control_customize_sub_title);
        this.mDoneButton.setText(C0021R$string.qs_control_customize_save_text);
        int max = Math.max(1, this.mContext.getResources().getInteger(C0012R$dimen.qs_control_tiles_columns));
        if (this.mSpanCount != max) {
            this.mSpanCount = max;
            this.mTileAdapter.setSpanCount(max);
            this.mOtherTilesAdapter.setSpanCount(this.mSpanCount);
            updateLayout();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTileAdapter.removeAccessibilityListener();
        this.mOtherTilesAdapter.removeAccessibilityListener();
    }

    public void updateResources() {
        this.mTitle.setTextAppearance(C0022R$style.TextAppearance_QSControl_CustomizeTitle);
        this.mSubTitle.setTextAppearance(C0022R$style.TextAppearance_QSControl_CustomizeSubTitle);
        this.mDoneButton.setTextAppearance(C0022R$style.TextAppearance_QSControl_CustomizeSaveText);
        this.mUnAddedTilesLayout.updateResources();
    }

    private void updateLayout() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), this.mSpanCount);
        gridLayoutManager.setSpanSizeLookup(this.mTileAdapter.getSizeLookup());
        this.mRecyclerView.setLayoutManager(gridLayoutManager);
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(getContext(), this.mSpanCount);
        gridLayoutManager2.setSpanSizeLookup(this.mOtherTilesAdapter.getSizeLookup());
        this.mOthersRecyclerView.setLayoutManager(gridLayoutManager2);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mTileAdapter.setHost(qSTileHost);
        this.mOtherTilesAdapter.setHost(qSTileHost);
    }

    /* access modifiers changed from: private */
    public void queryTiles() {
        this.mTileQueryHelper.queryTiles(this.mHost);
    }

    public void addInTileAdapter(TileQueryHelper.TileInfo tileInfo, boolean z) {
        if (z) {
            this.mTileAdapter.addTileItem(tileInfo);
            refreshLayoutByAddTile();
            return;
        }
        this.mOtherTilesAdapter.addTileItem(tileInfo);
        this.mOthersRecyclerView.smoothScrollToPosition(this.mOtherTilesAdapter.getItemCount() - 1);
    }

    private void refreshLayoutByAddTile() {
        this.mRecyclerView.smoothScrollToPosition(this.mTileAdapter.getItemCount() - 1);
    }

    /* access modifiers changed from: private */
    public void releaseTiles() {
        this.mTileQueryHelper.releaseTiles();
    }

    public void onTilesChanged(List<TileQueryHelper.TileInfo> list, Map<String, QSTile> map) {
        for (QSTile next : map.values()) {
            next.removeCallback(this);
            next.addCallback(this);
        }
        this.mTileAdapter.onTilesChanged(list, map);
        this.mOtherTilesAdapter.onTilesChanged(list, map);
        post(new Runnable() {
            public void run() {
                QSControlCustomizer.this.handleShowAnimation();
            }
        });
    }

    public void onTileChanged(TileQueryHelper.TileInfo tileInfo) {
        this.mTileAdapter.onTileChanged(tileInfo);
        this.mOtherTilesAdapter.onTileChanged(tileInfo);
    }

    /* access modifiers changed from: private */
    public void handleShowAnimation() {
        if (this.isShown && this.mShownRequested) {
            startAnimation(true);
            this.mShownRequested = false;
        }
    }

    /* access modifiers changed from: private */
    public void resetDataTrackStates() {
        this.mDonedClicked = false;
    }

    public void setQSControlCenterPanel(ControlPanelContentView controlPanelContentView) {
        controlPanelContentView.setQSCustomizerCallback(this.mQsPanelCallback);
        this.mQSCenterPanel = (ControlCenterPanelView) controlPanelContentView.getControlCenterPanel();
    }

    public boolean isShown() {
        return this.isShown;
    }

    /* access modifiers changed from: private */
    public void setTileSpecs() {
        ArrayList arrayList = new ArrayList();
        Collection<QSTile> tiles = this.mHost.getTiles();
        if (tiles != null) {
            tiles.size();
        }
        for (QSTile next : tiles) {
            if (!"edit".equals(next.getTileSpec())) {
                arrayList.add(next.getTileSpec());
            }
        }
        this.mSpecs.addAll(arrayList);
        this.mTileAdapter.setTileSpecs(arrayList);
        this.mOtherTilesAdapter.setTileSpecs(arrayList);
    }

    /* access modifiers changed from: private */
    public void setCustomizerAnimating(boolean z) {
        if (this.mCustomizerAnimating != z) {
            this.mCustomizerAnimating = z;
        }
    }

    /* access modifiers changed from: private */
    public void startAnimation(boolean z) {
        this.mUnAddedTilesLayout.init();
        this.mAnim.cancel();
        if (z) {
            IStateStyle iStateStyle = this.mAnim;
            AnimState animState = this.mHideAnim;
            AnimState animState2 = this.mShowAnim;
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(EaseManager.getStyle(-2, 0.8f, 0.5f));
            animConfig.setDelay(60);
            animConfig.addListeners(new TransitionListener() {
                public void onBegin(Object obj) {
                    super.onBegin(obj);
                    QSControlCustomizer.this.setLayerType(2, (Paint) null);
                    QSControlCustomizer.this.mRecyclerView.suppressLayout(true);
                    QSControlCustomizer.this.mOthersRecyclerView.suppressLayout(true);
                    QSControlCustomizer.this.mQSCenterPanel.setLayerType(2, (Paint) null);
                    QSControlCustomizer.this.setVisibility(0);
                }

                public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                    super.onUpdate(obj, floatProperty, f, f2, z);
                    if (floatProperty == ViewProperty.ALPHA) {
                        QSControlCustomizer.this.mQSCenterPanel.setAlpha(1.0f - f);
                    }
                }

                public void onComplete(Object obj) {
                    super.onComplete(obj);
                    QSControlCustomizer.this.setLayerType(0, (Paint) null);
                    QSControlCustomizer.this.mQSCenterPanel.setLayerType(0, (Paint) null);
                    QSControlCustomizer.this.mRecyclerView.suppressLayout(false);
                    QSControlCustomizer.this.mOthersRecyclerView.suppressLayout(false);
                    QSControlCustomizer.this.mQSCenterPanel.setVisibility(8);
                }
            });
            iStateStyle.fromTo(animState, animState2, animConfig);
            return;
        }
        IStateStyle iStateStyle2 = this.mAnim;
        AnimState animState3 = this.mShowAnim;
        AnimState animState4 = this.mHideAnim;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setDelay(60);
        animConfig2.addListeners(new TransitionListener() {
            public void onBegin(Object obj) {
                super.onBegin(obj);
                QSControlCustomizer.this.mQSCenterPanel.setVisibility(0);
                QSControlCustomizer.this.setLayerType(2, (Paint) null);
                QSControlCustomizer.this.mQSCenterPanel.setLayerType(2, (Paint) null);
            }

            public void onUpdate(Object obj, FloatProperty floatProperty, float f, float f2, boolean z) {
                super.onUpdate(obj, floatProperty, f, f2, z);
                if (floatProperty == ViewProperty.ALPHA) {
                    QSControlCustomizer.this.mQSCenterPanel.setAlpha(1.0f - f);
                }
            }

            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCustomizer.this.setVisibility(8);
                QSControlCustomizer.this.setLayerType(0, (Paint) null);
                QSControlCustomizer.this.mQSCenterPanel.setLayerType(0, (Paint) null);
            }
        });
        iStateStyle2.fromTo(animState3, animState4, animConfig2);
    }

    public void onShowEdit(boolean z) {
        post(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                QSControlCustomizer.this.lambda$onShowEdit$0$QSControlCustomizer(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onShowEdit$0 */
    public /* synthetic */ void lambda$onShowEdit$0$QSControlCustomizer(boolean z) {
        if (z) {
            this.mQsPanelCallback.show();
        } else {
            this.mQsPanelCallback.hide();
        }
    }
}
