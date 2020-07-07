package com.android.systemui.qs;

import android.util.Log;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.TouchAnimator;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class QSAnimator implements QSHost.Callback, PagedTileLayout.PageListener, TouchAnimator.Listener, View.OnLayoutChangeListener, View.OnAttachStateChangeListener, TunerService.Tunable {
    private final ArrayList<View> mAllViews = new ArrayList<>();
    private boolean mAllowFancy;
    private TouchAnimator mFirstPageAnimator;
    private TouchAnimator mFirstPageDelayedAnimator;
    private QSTileHost mHost;
    /* access modifiers changed from: private */
    public float mLastPosition;
    private final TouchAnimator.Listener mNonFirstPageListener = new TouchAnimator.ListenerAdapter() {
        public void onAnimationAtEnd() {
            QSAnimator.this.mQuickQsPanel.setVisibility(4);
        }

        public void onAnimationStarted() {
            QSAnimator.this.mQuickQsPanel.setVisibility(0);
        }
    };
    private TouchAnimator mNonfirstPageAnimator;
    private int mNumQuickTiles;
    private boolean mOnFirstPage = true;
    private boolean mOnKeyguard;
    private PagedTileLayout mPagedLayout;
    private final QS mQs;
    private final QSPanel mQsPanel;
    /* access modifiers changed from: private */
    public final QuickQSPanel mQuickQsPanel;
    private final ArrayList<View> mTopFiveQs = new ArrayList<>();
    private TouchAnimator mTranslationXAnimator;
    private TouchAnimator mTranslationYAnimator;
    private Runnable mUpdateAnimators = new Runnable() {
        public void run() {
            QSAnimator.this.updateAnimators();
            QSAnimator qSAnimator = QSAnimator.this;
            qSAnimator.setPosition(qSAnimator.mLastPosition);
        }
    };

    public QSAnimator(QS qs, QuickQSPanel quickQSPanel, QSPanel qSPanel) {
        this.mQs = qs;
        this.mQuickQsPanel = quickQSPanel;
        this.mQsPanel = qSPanel;
        qSPanel.addOnAttachStateChangeListener(this);
        qs.getView().addOnLayoutChangeListener(this);
        if (this.mQsPanel.isAttachedToWindow()) {
            onViewAttachedToWindow((View) null);
        }
        QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
        if (tileLayout instanceof PagedTileLayout) {
            PagedTileLayout pagedTileLayout = (PagedTileLayout) tileLayout;
            this.mPagedLayout = pagedTileLayout;
            pagedTileLayout.setPageListener(this);
            return;
        }
        Log.w("QSAnimator", "QS Not using page layout");
    }

    public void onRtlChanged() {
        updateAnimators();
    }

    public void setOnKeyguard(boolean z) {
        this.mOnKeyguard = z;
        this.mQuickQsPanel.setVisibility(z ? 4 : 0);
        if (this.mOnKeyguard) {
            clearAnimationState();
        }
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        updateAnimators();
    }

    public void onViewAttachedToWindow(View view) {
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_qs_fancy_anim", "sysui_qs_move_whole_rows");
        this.mQuickQsPanel.setQsAnimator(this);
    }

    public void onViewDetachedFromWindow(View view) {
        this.mQuickQsPanel.setQsAnimator((QSAnimator) null);
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    public void onTuningChanged(String str, String str2) {
        if ("sysui_qs_fancy_anim".equals(str)) {
            boolean z = str2 == null || Integer.parseInt(str2) != 0;
            this.mAllowFancy = z;
            if (!z) {
                clearAnimationState();
            }
        } else if ("sysui_qs_move_whole_rows".equals(str) && str2 != null) {
            int parseInt = Integer.parseInt(str2);
        }
        updateAnimators();
    }

    public void setNumQuickTiles(int i) {
        if (this.mNumQuickTiles != i) {
            this.mNumQuickTiles = i;
            clearAnimationState();
            updateAnimators();
        }
    }

    public void onPageChanged(boolean z) {
        if (this.mOnFirstPage != z) {
            if (!z) {
                clearAnimationState();
            }
            this.mOnFirstPage = z;
        }
    }

    /* access modifiers changed from: private */
    public void updateAnimators() {
        float f;
        float f2;
        Iterator<QSTile> it;
        int[] iArr;
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        TouchAnimator.Builder builder2 = new TouchAnimator.Builder();
        TouchAnimator.Builder builder3 = new TouchAnimator.Builder();
        if (this.mQsPanel.getHost() != null) {
            Collection<QSTile> tiles = this.mQsPanel.getHost().getTiles();
            int[] iArr2 = new int[2];
            int[] iArr3 = new int[2];
            clearAnimationState();
            this.mAllViews.clear();
            this.mTopFiveQs.clear();
            QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
            this.mAllViews.add((View) tileLayout);
            float measuredHeight = (float) (((this.mQs.getView() != null ? this.mQs.getView().getMeasuredHeight() : 0) - this.mQs.getHeader().getBottom()) + this.mQs.getHeader().getPaddingBottom());
            builder.addFloat(tileLayout, "translationY", measuredHeight, 0.0f);
            Iterator<QSTile> it2 = tiles.iterator();
            int i = 0;
            int i2 = 0;
            while (it2.hasNext()) {
                QSTile next = it2.next();
                QSTileView tileView = this.mQsPanel.getTileView(next);
                if (tileView == null) {
                    Log.e("QSAnimator", "tileView is null " + next.getTileSpec());
                    f2 = measuredHeight;
                    it = it2;
                } else {
                    View iconView = tileView.getIcon().getIconView();
                    it = it2;
                    View view = this.mQs.getView();
                    f2 = measuredHeight;
                    if (i >= this.mNumQuickTiles || !this.mAllowFancy) {
                        iArr = iArr2;
                        i2 = i2;
                    } else {
                        QSTileView tileView2 = this.mQuickQsPanel.getTileView(next);
                        if (tileView2 != null) {
                            int i3 = iArr2[0];
                            getRelativePosition(iArr2, tileView2.getIcon().getIconView(), view);
                            getRelativePosition(iArr3, iconView, view);
                            int i4 = iArr3[0] - iArr2[0];
                            int i5 = iArr3[1] - iArr2[1];
                            iArr = iArr2;
                            if (i < this.mPagedLayout.getColumnCount()) {
                                builder2.addFloat(tileView2, "translationX", 0.0f, (float) i4);
                                builder3.addFloat(tileView2, "translationY", 0.0f, (float) i5);
                                builder2.addFloat(tileView, "translationX", (float) (-i4), 0.0f);
                                this.mTopFiveQs.add(tileView.getIcon());
                                i2 = i5;
                            } else {
                                builder2.addFloat(tileView2, "translationX", 0.0f, ((float) this.mQsPanel.getWidth()) - tileView2.getX());
                                builder3.addFloat(tileView2, "translationY", 0.0f, (float) i2);
                                builder.addFloat(tileView2, "alpha", 1.0f, 0.0f, 0.0f);
                            }
                            this.mAllViews.add(tileView.getIcon());
                            this.mAllViews.add(tileView2);
                        }
                    }
                    this.mAllViews.add(tileView);
                    i++;
                    it2 = it;
                    measuredHeight = f2;
                    iArr2 = iArr;
                }
                it2 = it;
                measuredHeight = f2;
            }
            float f3 = measuredHeight;
            if (this.mAllowFancy) {
                builder.setListener(this);
                this.mFirstPageAnimator = builder.build();
                TouchAnimator.Builder builder4 = new TouchAnimator.Builder();
                builder4.setStartDelay(0.5f);
                builder4.addFloat(tileLayout, "alpha", 0.0f, 1.0f);
                builder4.addFloat(this.mQsPanel.getPageIndicator(), "alpha", 0.0f, 1.0f);
                builder4.addFloat(this.mQsPanel.getFooter().getView(), "alpha", 0.0f, 1.0f);
                this.mFirstPageDelayedAnimator = builder4.build();
                this.mAllViews.add(this.mQsPanel.getPageIndicator());
                this.mAllViews.add(this.mQsPanel.getFooter().getView());
                if (tiles.size() <= 3) {
                    f = 1.0f;
                } else {
                    f = tiles.size() <= 6 ? 0.4f : 0.0f;
                }
                PathInterpolatorBuilder pathInterpolatorBuilder = new PathInterpolatorBuilder(0.0f, 0.0f, f, 1.0f);
                builder2.setInterpolator(pathInterpolatorBuilder.getXInterpolator());
                builder3.setInterpolator(pathInterpolatorBuilder.getYInterpolator());
                this.mTranslationXAnimator = builder2.build();
                this.mTranslationYAnimator = builder3.build();
            }
            TouchAnimator.Builder builder5 = new TouchAnimator.Builder();
            builder5.addFloat(this.mQuickQsPanel, "alpha", 1.0f, 0.0f, 0.0f);
            builder5.addFloat(this.mQsPanel.getPageIndicator(), "alpha", 0.0f, 0.0f, 1.0f);
            builder5.addFloat(tileLayout, "translationY", f3, 0.0f);
            builder5.addFloat(tileLayout, "alpha", 0.0f, 0.0f, 1.0f);
            builder5.setListener(this.mNonFirstPageListener);
            this.mNonfirstPageAnimator = builder5.build();
        }
    }

    private void getRelativePosition(int[] iArr, View view, View view2) {
        iArr[0] = (view.getWidth() / 2) + 0;
        iArr[1] = 0;
        getRelativePositionInt(iArr, view, view2);
    }

    private void getRelativePositionInt(int[] iArr, View view, View view2) {
        if (view != view2 && view != null) {
            if (!(view instanceof PagedTileLayout.TilePage)) {
                iArr[0] = iArr[0] + view.getLeft();
                iArr[1] = iArr[1] + view.getTop();
            }
            getRelativePositionInt(iArr, (View) view.getParent(), view2);
        }
    }

    public void setPosition(float f) {
        if (this.mFirstPageAnimator != null && !this.mOnKeyguard) {
            this.mLastPosition = f;
            if (!this.mOnFirstPage || !this.mAllowFancy) {
                this.mNonfirstPageAnimator.setPosition(f);
                return;
            }
            this.mQuickQsPanel.setAlpha(1.0f);
            this.mFirstPageAnimator.setPosition(f);
            this.mFirstPageDelayedAnimator.setPosition(f);
            this.mTranslationXAnimator.setPosition(f);
            this.mTranslationYAnimator.setPosition(f);
        }
    }

    public void onAnimationAtStart() {
        this.mQuickQsPanel.setVisibility(0);
    }

    public void onAnimationAtEnd() {
        this.mQuickQsPanel.setVisibility(4);
        int size = this.mTopFiveQs.size();
        for (int i = 0; i < size; i++) {
            this.mTopFiveQs.get(i).setVisibility(0);
        }
    }

    public void onAnimationStarted() {
        this.mQuickQsPanel.setVisibility(this.mOnKeyguard ? 4 : 0);
        if (this.mOnFirstPage) {
            int size = this.mTopFiveQs.size();
            for (int i = 0; i < size; i++) {
                this.mTopFiveQs.get(i).setVisibility(4);
            }
        }
    }

    private void clearAnimationState() {
        int size = this.mAllViews.size();
        this.mQuickQsPanel.setAlpha(0.0f);
        for (int i = 0; i < size; i++) {
            View view = this.mAllViews.get(i);
            view.setAlpha(1.0f);
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        }
        int size2 = this.mTopFiveQs.size();
        for (int i2 = 0; i2 < size2; i2++) {
            this.mTopFiveQs.get(i2).setVisibility(0);
        }
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mQsPanel.post(this.mUpdateAnimators);
    }

    public void onTilesChanged() {
        this.mQsPanel.post(this.mUpdateAnimators);
    }
}
