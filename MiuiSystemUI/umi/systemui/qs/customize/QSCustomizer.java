package com.android.systemui.qs.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.miui.anim.AnimatorListenerWrapper;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSAnimation;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class QSCustomizer extends LinearLayout implements TileQueryHelper.TileStateListener, QSTile.Callback {
    /* access modifiers changed from: private */
    public boolean isShown;
    private Animator.AnimatorListener mAnimInListener;
    /* access modifiers changed from: private */
    public Animator.AnimatorListener mAnimOutListener;
    /* access modifiers changed from: private */
    public final QSDetailClipper mClipper;
    private final Animator.AnimatorListener mCollapseAnimationListener;
    private boolean mCustomizerAnimating;
    private boolean mCustomizing;
    protected TextView mDoneButton;
    /* access modifiers changed from: private */
    public boolean mDonedClicked;
    private final Animator.AnimatorListener mExpandAnimationListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            if (QSCustomizer.this.isShown) {
                QSCustomizer.this.setCustomizing(true);
            }
            boolean unused = QSCustomizer.this.mOpening = false;
            QSCustomizer.this.setCustomizerAnimating(false);
        }

        public void onAnimationCancel(Animator animator) {
            boolean unused = QSCustomizer.this.mOpening = false;
            QSCustomizer.this.setCustomizerAnimating(false);
        }
    };
    protected RelativeLayout mHeader;
    /* access modifiers changed from: private */
    public QSTileHost mHost;
    /* access modifiers changed from: private */
    public final KeyguardMonitor.Callback mKeyguardCallback = new KeyguardMonitor.Callback() {
        public void onKeyguardShowingChanged() {
            if (QSCustomizer.this.isAttachedToWindow() && ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).isShowing() && !QSCustomizer.this.mOpening) {
                QSCustomizer.this.mQsPanelCallback.hide(0, 0, false);
            }
        }
    };
    private ViewGroup mNotifQsContainer;
    /* access modifiers changed from: private */
    public boolean mOpening;
    private QS mQs;
    protected QSPanelCallback mQsPanelCallback = new QSPanelCallback() {
        public void show(int i, int i2) {
            if (!QSCustomizer.this.isShown) {
                int unused = QSCustomizer.this.mX = i;
                int unused2 = QSCustomizer.this.mY = i2;
                MetricsLogger.visible(QSCustomizer.this.getContext(), 358);
                boolean unused3 = QSCustomizer.this.isShown = true;
                boolean unused4 = QSCustomizer.this.mShownRequested = true;
                boolean unused5 = QSCustomizer.this.mOpening = true;
                QSCustomizer.this.setTileSpecs();
                QSCustomizer.this.queryTiles();
                QSCustomizer.this.setCustomizerAnimating(true);
                QSCustomizer qSCustomizer = QSCustomizer.this;
                qSCustomizer.announceForAccessibility(qSCustomizer.mContext.getString(R.string.accessibility_desc_quick_settings_edit));
                ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).addCallback(QSCustomizer.this.mKeyguardCallback);
            }
        }

        public void hide(int i, int i2, boolean z) {
            boolean z2;
            if (QSCustomizer.this.isShown) {
                if (QSCustomizer.this.mTileAdapter != null) {
                    z2 = QSCustomizer.this.mTileAdapter.isTileMoved();
                    QSCustomizer.this.mTileAdapter.resetTileMoved();
                } else {
                    z2 = false;
                }
                ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleQSEditExitEvent(QSCustomizer.this.mResetClicked, z2, QSCustomizer.this.mDonedClicked ? "done_btn" : "back_pressed");
                QSCustomizer.this.resetDataTrackStates();
                MetricsLogger.hidden(QSCustomizer.this.getContext(), 358);
                boolean unused = QSCustomizer.this.isShown = false;
                boolean unused2 = QSCustomizer.this.mShownRequested = false;
                QSCustomizer.this.setCustomizing(false);
                if (z) {
                    QSCustomizer.this.mClipper.animateCircularClip(QSCustomizer.this.mX, QSCustomizer.this.mY, false, QSCustomizer.this.mAnimOutListener);
                } else {
                    QSCustomizer.this.setAlpha(0.0f);
                    QSCustomizer.this.mAnimOutListener.onAnimationEnd((Animator) null);
                }
                QSCustomizer.this.releaseTiles();
                QSCustomizer.this.setCustomizerAnimating(true);
                QSCustomizer qSCustomizer = QSCustomizer.this;
                qSCustomizer.announceForAccessibility(qSCustomizer.mContext.getString(R.string.accessibility_desc_quick_settings));
                ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).removeCallback(QSCustomizer.this.mKeyguardCallback);
            }
        }
    };
    /* access modifiers changed from: private */
    public RecyclerView mRecyclerView;
    protected TextView mResetButton;
    /* access modifiers changed from: private */
    public boolean mResetClicked;
    private Animator.AnimatorListener mRestoreInListener;
    /* access modifiers changed from: private */
    public boolean mShownRequested;
    private int mSpanCount;
    private List<String> mSpecs = new ArrayList();
    protected TextView mSubTitle;
    /* access modifiers changed from: private */
    public TileAdapter mTileAdapter;
    private final TileQueryHelper mTileQueryHelper;
    protected TextView mTitle;
    /* access modifiers changed from: private */
    public int mX;
    /* access modifiers changed from: private */
    public int mY;

    public interface QSPanelCallback {
        void hide(int i, int i2, boolean z);

        void show(int i, int i2);
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

    public QSCustomizer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AnonymousClass8 r5 = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (!QSCustomizer.this.isShown) {
                    QSCustomizer.this.setVisibility(8);
                }
                QSCustomizer.this.setCustomizerAnimating(false);
                QSCustomizer.this.mRecyclerView.setAdapter(QSCustomizer.this.mTileAdapter);
            }

            public void onAnimationCancel(Animator animator) {
                if (!QSCustomizer.this.isShown) {
                    QSCustomizer.this.setVisibility(8);
                }
                QSCustomizer.this.setCustomizerAnimating(false);
            }
        };
        this.mCollapseAnimationListener = r5;
        this.mAnimInListener = this.mExpandAnimationListener;
        this.mAnimOutListener = r5;
        this.mClipper = new QSDetailClipper(this);
        setClickable(true);
        this.mSpanCount = Math.max(1, this.mContext.getResources().getInteger(R.integer.quick_settings_num_columns));
        LayoutInflater.from(getContext()).inflate(R.layout.qs_customize_panel_content, this);
        this.mRecyclerView = (RecyclerView) findViewById(16908298);
        this.mTileAdapter = new TileAdapter(getContext(), this.mSpanCount, this.mRecyclerView);
        this.mTileQueryHelper = new TileQueryHelper(context, this);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        updateLayout();
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setMoveDuration(150);
        this.mRecyclerView.setItemAnimator(defaultItemAnimator);
        TextView textView = (TextView) findViewById(16908314);
        this.mResetButton = textView;
        textView.setText(R.string.reset);
        this.mResetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = QSCustomizer.this.mResetClicked = true;
                MetricsLogger.action(QSCustomizer.this.getContext(), 359);
                QSCustomizer.this.reset();
            }
        });
        TextView textView2 = (TextView) findViewById(16908313);
        this.mDoneButton = textView2;
        textView2.setText(R.string.quick_settings_done);
        this.mDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = QSCustomizer.this.mDonedClicked = true;
                QSCustomizer.this.mQsPanelCallback.hide(((int) view.getX()) + (view.getWidth() / 2), ((int) view.getY()) + (view.getHeight() / 2), true);
                QSCustomizer.this.mHost.setMiuiQSTilesEdited();
            }
        });
        this.mHeader = (RelativeLayout) findViewById(R.id.header);
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mSubTitle = (TextView) findViewById(R.id.sub_title);
        this.mSpanCount = -1;
        onConfigurationChanged(getResources().getConfiguration());
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTitle.setText(R.string.qs_customize_title);
        this.mSubTitle.setText(R.string.drag_to_add_tiles);
        this.mResetButton.setText(17041224);
        this.mDoneButton.setText(R.string.quick_settings_done);
        Resources resources = this.mContext.getResources();
        int max = Math.max(1, resources.getInteger(R.integer.quick_settings_num_columns));
        ViewGroup.LayoutParams layoutParams = this.mHeader.getLayoutParams();
        layoutParams.height = this.mContext.getResources().getDimensionPixelOffset(R.dimen.notch_expanded_header_height);
        this.mHeader.setLayoutParams(layoutParams);
        if (this.mSpanCount != max) {
            if (resources.getConfiguration().orientation == 1) {
                this.mHeader.setVisibility(0);
            } else {
                this.mHeader.setVisibility(8);
            }
            this.mSpanCount = max;
            this.mTileAdapter.setSpanCount(max);
            updateLayout();
        }
    }

    private void updateLayout() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), this.mSpanCount);
        gridLayoutManager.setSpanSizeLookup(this.mTileAdapter.getSizeLookup());
        this.mRecyclerView.setLayoutManager(gridLayoutManager);
        setPadding(getPaddingLeft(), getResources().getDimensionPixelSize(R.dimen.qs_customize_padding_top), getPaddingRight(), getResources().getDimensionPixelOffset(R.dimen.qs_customize_padding_bottom));
    }

    public void setQs(QS qs) {
        this.mQs = qs;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mTileAdapter.setHost(qSTileHost);
    }

    public void setContainer(ViewGroup viewGroup) {
        this.mNotifQsContainer = viewGroup;
    }

    /* access modifiers changed from: private */
    public void queryTiles() {
        this.mTileQueryHelper.queryTiles(this.mHost);
    }

    /* access modifiers changed from: private */
    public void releaseTiles() {
        this.mTileQueryHelper.releaseTiles();
    }

    public void saveInstanceState(Bundle bundle) {
        if (this.isShown) {
            ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).removeCallback(this.mKeyguardCallback);
        }
        bundle.putBoolean("qs_customizing", this.mCustomizing);
    }

    public void restoreInstanceState(Bundle bundle) {
        if (bundle.getBoolean("qs_customizing")) {
            setVisibility(0);
            addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    QSCustomizer.this.removeOnLayoutChangeListener(this);
                    QSCustomizer.this.showImmediately();
                }
            });
        }
    }

    public void onTilesChanged(List<TileQueryHelper.TileInfo> list, Map<String, QSTile> map) {
        for (QSTile next : map.values()) {
            next.removeCallback(this);
            next.addCallback(this);
        }
        this.mTileAdapter.onTilesChanged(list, map);
        post(new Runnable() {
            public void run() {
                QSCustomizer.this.handleShowAnimation();
            }
        });
    }

    public void onTileChanged(TileQueryHelper.TileInfo tileInfo) {
        this.mTileAdapter.onTileChanged(tileInfo);
    }

    /* access modifiers changed from: private */
    public void handleShowAnimation() {
        if (this.isShown && this.mShownRequested) {
            setAlpha(1.0f);
            setVisibility(0);
            this.mClipper.animateCircularClip(this.mX, this.mY, true, this.mAnimInListener);
            this.mShownRequested = false;
        }
    }

    /* access modifiers changed from: private */
    public void showImmediately() {
        if (!this.isShown) {
            setVisibility(0);
            this.isShown = true;
            setTileSpecs();
            setCustomizing(true);
            queryTiles();
            setCustomizerAnimating(false);
            announceForAccessibility(this.mContext.getString(R.string.accessibility_desc_quick_settings_edit));
            ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class)).addCallback(this.mKeyguardCallback);
            Animator.AnimatorListener animatorListener = this.mRestoreInListener;
            if (animatorListener != null) {
                animatorListener.onAnimationStart((Animator) null);
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetDataTrackStates() {
        this.mResetClicked = false;
        this.mDonedClicked = false;
    }

    public void setQsPanel(QSPanel qSPanel) {
        qSPanel.setQSCustomizerCallback(this.mQsPanelCallback);
    }

    public boolean isShown() {
        return this.isShown;
    }

    /* access modifiers changed from: private */
    public void setCustomizing(boolean z) {
        this.mCustomizing = z;
        this.mQs.notifyCustomizeChanged();
    }

    public boolean isCustomizing() {
        return this.mCustomizing;
    }

    /* access modifiers changed from: private */
    public void reset() {
        ArrayList arrayList = new ArrayList();
        for (String add : this.mHost.getQsDefaultTiles().split(",")) {
            arrayList.add(add);
        }
        this.mTileAdapter.resetTileSpecs(this.mHost, arrayList);
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
    }

    public void setAnimatedViews(List<View> list) {
        if (list != null && list.size() != 0) {
            this.mAnimInListener = AnimatorListenerWrapper.of(this.mExpandAnimationListener, new QSAnimation.QsHideBeforeAnimatorListener((View[]) list.toArray(new View[0])));
            this.mAnimOutListener = AnimatorListenerWrapper.of(this.mCollapseAnimationListener, new QSAnimation.QsShowBeforeAnimatorListener((View[]) list.toArray(new View[0])));
            QSAnimation.QsHideBeforeAnimatorListener qsHideBeforeAnimatorListener = new QSAnimation.QsHideBeforeAnimatorListener((View[]) list.toArray(new View[0]));
            qsHideBeforeAnimatorListener.setAlphaDuration(0);
            this.mRestoreInListener = qsHideBeforeAnimatorListener;
        }
    }

    /* access modifiers changed from: private */
    public void setCustomizerAnimating(boolean z) {
        if (this.mCustomizerAnimating != z) {
            this.mCustomizerAnimating = z;
            this.mNotifQsContainer.invalidate();
        }
    }

    public int getVisualBottom() {
        return getBottom() - getPaddingBottom();
    }

    public void onShowEdit(boolean z) {
        post(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                QSCustomizer.this.lambda$onShowEdit$0$QSCustomizer(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onShowEdit$0 */
    public /* synthetic */ void lambda$onShowEdit$0$QSCustomizer(boolean z) {
        if (z) {
            this.mQsPanelCallback.show(this.mX, this.mY);
        } else {
            this.mQsPanelCallback.hide(this.mX, this.mY, true);
        }
    }
}
