package com.android.systemui.miui.controlcenter.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.QSControlTileHost;
import com.android.systemui.miui.controlcenter.Utils;
import com.android.systemui.miui.statusbar.phone.ControlPanelContentView;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;
import miuix.recyclerview.widget.MiuiDefaultItemAnimator;
import miuix.recyclerview.widget.RecyclerView;

public class QSControlCustomizer extends FrameLayout implements TileQueryHelper.TileStateListener, QSTile.Callback {
    /* access modifiers changed from: private */
    public boolean isShown;
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
    public QSControlTileHost mHost;
    /* access modifiers changed from: private */
    public final KeyguardMonitor.Callback mKeyguardCallback = new KeyguardMonitor.Callback() {
        public void onKeyguardShowingChanged() {
            if (QSControlCustomizer.this.isAttachedToWindow() && ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).isShowing() && !QSControlCustomizer.this.mOpening) {
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
    private IStateStyle mPanelAnim;
    private AnimState mPanelHideAnim;
    private AnimState mPanelShowAnim;
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
                qSControlCustomizer.announceForAccessibility(qSControlCustomizer.mContext.getString(R.string.accessibility_desc_quick_settings_edit));
                ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).addCallback(QSControlCustomizer.this.mKeyguardCallback);
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
                qSControlCustomizer.announceForAccessibility(qSControlCustomizer.mContext.getString(R.string.accessibility_desc_quick_settings));
                ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).removeCallback(QSControlCustomizer.this.mKeyguardCallback);
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

    public QSControlCustomizer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        new QSDetailClipper(this);
        setClickable(true);
        this.mSpanCount = this.mContext.getResources().getInteger(R.dimen.qs_control_tiles_columns);
        this.mTileVerticalIntervel = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_margin_bottom);
        LayoutInflater.from(getContext()).inflate(R.layout.qs_control_customize_panel_content, this);
        this.mRecyclerView = (RecyclerView) findViewById(R.id.list_added);
        CCTileAdapter cCTileAdapter = new CCTileAdapter(getContext(), this.mSpanCount, this.mRecyclerView, true);
        this.mTileAdapter = cCTileAdapter;
        cCTileAdapter.setQsControlCustomizer(this);
        this.mTileQueryHelper = new CCTileQueryHelper(context, this);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        this.mOthersRecyclerView = (RecyclerView) findViewById(R.id.list_others);
        CCTileAdapter cCTileAdapter2 = new CCTileAdapter(getContext(), this.mSpanCount, this.mOthersRecyclerView, false);
        this.mOtherTilesAdapter = cCTileAdapter2;
        cCTileAdapter2.setQsControlCustomizer(this);
        this.mOthersRecyclerView.setAdapter(this.mOtherTilesAdapter);
        updateLayout();
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        this.mOthersRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        this.mRecyclerView.setItemAnimator(new MiuiDefaultItemAnimator());
        this.mOthersRecyclerView.setItemAnimator(new MiuiDefaultItemAnimator());
        UnAddedTilesLayout unAddedTilesLayout = (UnAddedTilesLayout) findViewById(R.id.unAdded_tiles);
        this.mUnAddedTilesLayout = unAddedTilesLayout;
        unAddedTilesLayout.setAddedLayout(this.mRecyclerView);
        TextView textView = (TextView) findViewById(R.id.save);
        this.mDoneButton = textView;
        Utils.createButtonFolmeTouchStyle(textView);
        this.mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = QSControlCustomizer.this.mDonedClicked = true;
                QSControlCustomizer.this.mQsPanelCallback.hide();
                QSControlCustomizer.this.mHost.setMiuiQSTilesEdited();
            }
        });
        this.mHeader = (LinearLayout) findViewById(R.id.header);
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mSubTitle = (TextView) findViewById(R.id.sub_title);
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
        AnimState animState3 = new AnimState("qs_control_customizer_show_panel");
        animState3.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        this.mPanelShowAnim = animState3;
        AnimState animState4 = new AnimState("qs_control_customizer_hide_panel");
        animState4.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        this.mPanelHideAnim = animState4;
        if (!this.mLayoutParamsInited) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mHeader.getLayoutParams();
            this.mUnAddedTilesLayout.setMarginTop(this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_title_height) + this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_sub_title_height) + layoutParams.topMargin + layoutParams.bottomMargin + this.mTileVerticalIntervel + this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_content_margin_top) + this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tile_height), this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_title_height) + this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_sub_title_height) + this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tiles_content_margin_top) + layoutParams.topMargin + layoutParams.bottomMargin + (this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_customizer_tile_height) * 4) + (this.mTileVerticalIntervel * 4));
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
        this.mTitle.setText(R.string.qs_control_customize_title);
        this.mSubTitle.setText(R.string.qs_control_customize_sub_title);
        this.mDoneButton.setText(R.string.quick_settings_done);
        Resources resources = this.mContext.getResources();
        int max = Math.max(1, resources.getInteger(R.integer.quick_settings_num_columns));
        if (this.mSpanCount != max) {
            if (resources.getConfiguration().orientation == 1) {
                this.mHeader.setVisibility(0);
            } else {
                this.mHeader.setVisibility(8);
            }
            this.mSpanCount = max;
            this.mTileAdapter.setSpanCount(max);
            this.mOtherTilesAdapter.setSpanCount(this.mSpanCount);
            updateLayout();
        }
    }

    public void updateResources() {
        this.mTitle.setTextAppearance(R.style.TextAppearance_QSControl_CustomizeTitle);
        this.mSubTitle.setTextAppearance(R.style.TextAppearance_QSControl_CustomizeSubTitle);
        this.mDoneButton.setTextAppearance(R.style.TextAppearance_QSControl_CustomizeSaveText);
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

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mHost = qSControlTileHost;
        this.mTileAdapter.setHost(qSControlTileHost);
        this.mOtherTilesAdapter.setHost(qSControlTileHost);
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
        this.mPanelAnim = Folme.useAt(controlPanelContentView.getControlCenterPanel()).state();
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
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mOtherTilesAdapter.setTileSpecs(arrayList);
        this.mOthersRecyclerView.setAdapter(this.mOtherTilesAdapter);
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
        if (z) {
            this.mPanelAnim.fromTo(this.mPanelShowAnim, this.mPanelHideAnim, new AnimConfig());
            IStateStyle iStateStyle = this.mAnim;
            AnimState animState = this.mHideAnim;
            AnimState animState2 = this.mShowAnim;
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(EaseManager.getStyle(-2, 0.8f, 0.5f));
            animConfig.setDelay(0);
            animConfig.addListeners(new TransitionListener() {
                public void onBegin(Object obj) {
                    super.onBegin(obj);
                    QSControlCustomizer.this.setVisibility(0);
                }
            });
            iStateStyle.fromTo(animState, animState2, animConfig);
            return;
        }
        IStateStyle iStateStyle2 = this.mPanelAnim;
        AnimState animState3 = this.mPanelHideAnim;
        AnimState animState4 = this.mPanelShowAnim;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setDelay(60);
        iStateStyle2.fromTo(animState3, animState4, animConfig2);
        IStateStyle iStateStyle3 = this.mAnim;
        AnimState animState5 = this.mShowAnim;
        AnimState animState6 = this.mHideAnim;
        AnimConfig animConfig3 = new AnimConfig();
        animConfig3.addListeners(new TransitionListener() {
            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlCustomizer.this.setVisibility(8);
            }
        });
        iStateStyle3.fromTo(animState5, animState6, animConfig3);
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
