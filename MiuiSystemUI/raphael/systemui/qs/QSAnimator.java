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
    private TouchAnimator mAllPagesDelayedAnimator;
    private final ArrayList<View> mAllViews = new ArrayList<>();
    private boolean mAllowFancy;
    private TouchAnimator mBrightnessAnimator;
    private TouchAnimator mFirstPageAnimator;
    private TouchAnimator mFirstPageDelayedAnimator;
    private QSTileHost mHost;
    private float mLastPosition;
    private boolean mNeedsAnimatorUpdate = false;
    private final TouchAnimator.Listener mNonFirstPageListener = new TouchAnimator.ListenerAdapter() {
        /* class com.android.systemui.qs.QSAnimator.AnonymousClass1 */

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationAtEnd() {
            QSAnimator.this.mQuickQsPanel.setVisibility(4);
        }

        @Override // com.android.systemui.qs.TouchAnimator.Listener
        public void onAnimationStarted() {
            QSAnimator.this.mQuickQsPanel.setVisibility(0);
        }
    };
    private TouchAnimator mNonfirstPageAnimator;
    private TouchAnimator mNonfirstPageDelayedAnimator;
    private boolean mOnFirstPage = true;
    private boolean mOnKeyguard;
    private PagedTileLayout mPagedLayout;
    private final QS mQs;
    private final QSPanel mQsPanel;
    private final QuickQSPanel mQuickQsPanel;
    private final ArrayList<View> mQuickQsViews = new ArrayList<>();
    private boolean mShowCollapsedOnKeyguard;
    private TouchAnimator mTranslationXAnimator;
    private TouchAnimator mTranslationYAnimator;
    private Runnable mUpdateAnimators = new Runnable() {
        /* class com.android.systemui.qs.QSAnimator.AnonymousClass2 */

        public void run() {
            QSAnimator.this.miuiUpdateAnimators();
            QSAnimator.this.setCurrentPosition();
        }
    };

    public QSAnimator(QS qs, QuickQSPanel quickQSPanel, QSPanel qSPanel) {
        this.mQs = qs;
        this.mQuickQsPanel = quickQSPanel;
        this.mQsPanel = qSPanel;
        qSPanel.addOnAttachStateChangeListener(this);
        qs.getView().addOnLayoutChangeListener(this);
        if (this.mQsPanel.isAttachedToWindow()) {
            onViewAttachedToWindow(null);
        }
        QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
        if (tileLayout instanceof PagedTileLayout) {
            this.mPagedLayout = (PagedTileLayout) tileLayout;
        } else {
            Log.w("QSAnimator", "QS Not using page layout");
        }
        qSPanel.setPageListener(this);
    }

    public void onRtlChanged() {
        miuiUpdateAnimators();
    }

    public void onQsScrollingChanged() {
        this.mNeedsAnimatorUpdate = true;
    }

    public void setOnKeyguard(boolean z) {
        this.mOnKeyguard = z;
        updateQQSVisibility();
        if (this.mOnKeyguard) {
            clearAnimationState();
        }
    }

    /* access modifiers changed from: package-private */
    public void setShowCollapsedOnKeyguard(boolean z) {
        this.mShowCollapsedOnKeyguard = z;
        updateQQSVisibility();
        setCurrentPosition();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentPosition() {
        setPosition(this.mLastPosition);
    }

    private void updateQQSVisibility() {
        this.mQuickQsPanel.setVisibility((!this.mOnKeyguard || this.mShowCollapsedOnKeyguard) ? 0 : 4);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        miuiUpdateAnimators();
    }

    public void onViewAttachedToWindow(View view) {
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_qs_fancy_anim", "sysui_qs_move_whole_rows");
        this.mQuickQsPanel.setQsAnimator(this);
    }

    public void onViewDetachedFromWindow(View view) {
        this.mQuickQsPanel.setQsAnimator(null);
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        if ("sysui_qs_fancy_anim".equals(str)) {
            boolean parseIntegerSwitch = TunerService.parseIntegerSwitch(str2, true);
            this.mAllowFancy = parseIntegerSwitch;
            if (!parseIntegerSwitch) {
                clearAnimationState();
            }
        } else if ("sysui_qs_move_whole_rows".equals(str)) {
            TunerService.parseIntegerSwitch(str2, true);
        }
        miuiUpdateAnimators();
    }

    @Override // com.android.systemui.qs.PagedTileLayout.PageListener
    public void onPageChanged(boolean z) {
        if (this.mOnFirstPage != z) {
            if (!z) {
                clearAnimationState();
            }
            this.mOnFirstPage = z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void miuiUpdateAnimators() {
        int i;
        float f;
        Collection<QSTile> collection;
        Iterator<QSTile> it;
        int[] iArr;
        this.mNeedsAnimatorUpdate = false;
        TouchAnimator.Builder builder = new TouchAnimator.Builder();
        TouchAnimator.Builder builder2 = new TouchAnimator.Builder();
        TouchAnimator.Builder builder3 = new TouchAnimator.Builder();
        if (this.mQsPanel.getHost() != null) {
            Collection<QSTile> tiles = this.mQsPanel.getHost().getTiles();
            int[] iArr2 = new int[2];
            int[] iArr3 = new int[2];
            clearAnimationState();
            this.mAllViews.clear();
            this.mQuickQsViews.clear();
            QSPanel.QSTileLayout tileLayout = this.mQsPanel.getTileLayout();
            this.mAllViews.add((View) tileLayout);
            float measuredHeight = (float) (((this.mQs.getView() != null ? this.mQs.getView().getMeasuredHeight() : 0) - this.mQs.getHeader().getBottom()) + this.mQs.getHeader().getPaddingBottom());
            builder.addFloat(tileLayout, "translationY", measuredHeight, 0.0f);
            Iterator<QSTile> it2 = tiles.iterator();
            int i2 = 0;
            int i3 = 0;
            while (it2.hasNext()) {
                QSTile next = it2.next();
                QSTileView tileView = this.mQsPanel.getTileView(next);
                if (tileView == null) {
                    Log.e("QSAnimator", "tileView is null " + next.getTileSpec());
                    collection = tiles;
                    it = it2;
                } else {
                    View iconView = tileView.getIcon().getIconView();
                    it = it2;
                    View view = this.mQs.getView();
                    collection = tiles;
                    if (i2 >= this.mQuickQsPanel.getTileLayout().getNumVisibleTiles() || !this.mAllowFancy) {
                        iArr = iArr2;
                        i3 = i3;
                    } else {
                        QSTileView tileView2 = this.mQuickQsPanel.getTileView(next);
                        if (tileView2 != null) {
                            getRelativePosition(iArr2, tileView2.getIcon().getIconView(), view);
                            getRelativePosition(iArr3, iconView, view);
                            int i4 = iArr3[0] - iArr2[0];
                            int i5 = iArr3[1] - iArr2[1];
                            iArr = iArr2;
                            if (i2 < this.mPagedLayout.getColumnCount()) {
                                builder2.addFloat(tileView2, "translationX", 0.0f, (float) i4);
                                builder3.addFloat(tileView2, "translationY", 0.0f, (float) i5);
                                builder2.addFloat(tileView, "translationX", (float) (-i4), 0.0f);
                                this.mQuickQsViews.add(tileView.getIconWithBackground());
                                i3 = i5;
                            } else {
                                builder2.addFloat(tileView2, "translationX", 0.0f, ((float) this.mQsPanel.getWidth()) - tileView2.getX());
                                builder3.addFloat(tileView2, "translationY", 0.0f, (float) i3);
                                builder.addFloat(tileView2, "alpha", 1.0f, 0.0f, 0.0f);
                            }
                            this.mAllViews.add(tileView.getIcon());
                            this.mAllViews.add(tileView2);
                        }
                    }
                    this.mAllViews.add(tileView);
                    i2++;
                    it2 = it;
                    tiles = collection;
                    iArr2 = iArr;
                }
                it2 = it;
                tiles = collection;
            }
            if (this.mAllowFancy) {
                View brightnessView = this.mQsPanel.getBrightnessView();
                if (brightnessView != null) {
                    builder.addFloat(brightnessView, "translationY", measuredHeight, 0.0f);
                    TouchAnimator.Builder builder4 = new TouchAnimator.Builder();
                    builder4.addFloat(brightnessView, "alpha", 0.0f, 1.0f);
                    builder4.setStartDelay(0.5f);
                    this.mBrightnessAnimator = builder4.build();
                    this.mAllViews.add(brightnessView);
                } else {
                    this.mBrightnessAnimator = null;
                }
                builder.setListener(this);
                this.mFirstPageAnimator = builder.build();
                TouchAnimator.Builder builder5 = new TouchAnimator.Builder();
                builder5.setStartDelay(0.5f);
                builder5.addFloat(tileLayout, "alpha", 0.0f, 0.0f, 1.0f);
                builder5.addFloat(tileLayout, "translationY", measuredHeight, 0.0f);
                this.mFirstPageDelayedAnimator = builder5.build();
                TouchAnimator.Builder builder6 = new TouchAnimator.Builder();
                builder6.setStartDelay(0.5f);
                if (this.mQsPanel.getSecurityFooter() != null) {
                    i = 2;
                    builder6.addFloat(this.mQsPanel.getSecurityFooter().getView(), "alpha", 0.0f, 1.0f);
                } else {
                    i = 2;
                }
                if (this.mQsPanel.getDivider() != null) {
                    float[] fArr = new float[i];
                    // fill-array-data instruction
                    fArr[0] = 0.0f;
                    fArr[1] = 1.0f;
                    builder6.addFloat(this.mQsPanel.getDivider(), "alpha", fArr);
                }
                this.mAllPagesDelayedAnimator = builder6.build();
                if (this.mQsPanel.getSecurityFooter() != null) {
                    this.mAllViews.add(this.mQsPanel.getSecurityFooter().getView());
                }
                if (this.mQsPanel.getDivider() != null) {
                    this.mAllViews.add(this.mQsPanel.getDivider());
                }
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
            TouchAnimator.Builder builder7 = new TouchAnimator.Builder();
            builder7.addFloat(this.mQuickQsPanel, "alpha", 1.0f, 0.0f, 0.0f);
            builder7.setListener(this.mNonFirstPageListener);
            this.mNonfirstPageAnimator = builder7.build();
            TouchAnimator.Builder builder8 = new TouchAnimator.Builder();
            builder8.addFloat(tileLayout, "translationY", measuredHeight, 0.0f);
            builder8.addFloat(tileLayout, "alpha", 0.0f, 0.0f, 1.0f);
            builder8.setStartDelay(0.5f);
            this.mNonfirstPageDelayedAnimator = builder8.build();
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
            if (!(view instanceof PagedTileLayout)) {
                iArr[0] = iArr[0] - view.getScrollX();
                iArr[1] = iArr[1] - view.getScrollY();
            }
            getRelativePositionInt(iArr, (View) view.getParent(), view2);
        }
    }

    public void setPosition(float f) {
        if (this.mNeedsAnimatorUpdate) {
            miuiUpdateAnimators();
        }
        if (this.mFirstPageAnimator != null) {
            if (this.mOnKeyguard) {
                f = this.mShowCollapsedOnKeyguard ? 0.0f : 1.0f;
            }
            this.mLastPosition = f;
            if (!this.mOnFirstPage || !this.mAllowFancy) {
                this.mNonfirstPageAnimator.setPosition(f);
                this.mNonfirstPageDelayedAnimator.setPosition(f);
            } else {
                this.mQuickQsPanel.setAlpha(1.0f);
                this.mFirstPageAnimator.setPosition(f);
                this.mFirstPageDelayedAnimator.setPosition(f);
                this.mTranslationXAnimator.setPosition(f);
                this.mTranslationYAnimator.setPosition(f);
                TouchAnimator touchAnimator = this.mBrightnessAnimator;
                if (touchAnimator != null) {
                    touchAnimator.setPosition(f);
                }
            }
            if (this.mAllowFancy) {
                this.mAllPagesDelayedAnimator.setPosition(f);
            }
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtStart() {
        this.mQuickQsPanel.setVisibility(0);
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationAtEnd() {
        this.mQuickQsPanel.setVisibility(4);
        int size = this.mQuickQsViews.size();
        for (int i = 0; i < size; i++) {
            this.mQuickQsViews.get(i).setVisibility(0);
        }
    }

    @Override // com.android.systemui.qs.TouchAnimator.Listener
    public void onAnimationStarted() {
        updateQQSVisibility();
        if (this.mOnFirstPage) {
            int size = this.mQuickQsViews.size();
            for (int i = 0; i < size; i++) {
                this.mQuickQsViews.get(i).setVisibility(4);
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
        int size2 = this.mQuickQsViews.size();
        for (int i2 = 0; i2 < size2; i2++) {
            this.mQuickQsViews.get(i2).setVisibility(0);
        }
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mQsPanel.post(this.mUpdateAnimators);
    }

    @Override // com.android.systemui.qs.QSHost.Callback
    public void onTilesChanged() {
        this.mQsPanel.post(this.mUpdateAnimators);
    }

    public void setNumQuickTiles(int i) {
        clearAnimationState();
        miuiUpdateAnimators();
    }
}
