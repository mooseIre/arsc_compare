package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class QuickQSPanel extends QSPanel {
    protected QSPanel mFullPanel;
    private int mMaxTiles;
    protected QSAnimator mQsAnimator;

    /* access modifiers changed from: protected */
    public void setupPageIndicator() {
    }

    public QuickQSPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        QSSecurityFooter qSSecurityFooter = this.mFooter;
        if (qSSecurityFooter != null) {
            removeView(qSSecurityFooter.getView());
        }
        if (this.mTileLayout != null) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                this.mTileLayout.removeTile(this.mRecords.get(i));
            }
            removeView((View) this.mTileLayout);
        }
        this.mTileLayout = new HeaderTileLayout(context);
        addView((View) this.mTileLayout, 0);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        HeaderTileLayout headerTileLayout = (HeaderTileLayout) this.mTileLayout;
        headerTileLayout.measure(i, i2);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        super.onMeasure(i, View.MeasureSpec.makeMeasureSpec(layoutParams.topMargin + layoutParams.bottomMargin + headerTileLayout.getMeasuredHeight(), 1073741824));
    }

    public void setQSPanelAndHeader(QSPanel qSPanel, View view) {
        this.mFullPanel = qSPanel;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mTileLayout.setListening(this.mListening);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTileLayout.setListening(false);
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowDetail() {
        return !this.mExpanded;
    }

    /* access modifiers changed from: protected */
    public void drawTile(QSPanel.TileRecord tileRecord, QSTile.State state) {
        if (state instanceof QSTile.SignalState) {
            QSTile.SignalState signalState = new QSTile.SignalState();
            state.copyTo(signalState);
            signalState.activityIn = false;
            signalState.activityOut = false;
            state = signalState;
        }
        super.drawTile(tileRecord, state);
    }

    public void setQsAnimator(QSAnimator qSAnimator) {
        this.mQsAnimator = qSAnimator;
        QSAnimator qSAnimator2 = this.mQsAnimator;
        if (qSAnimator2 != null) {
            qSAnimator2.setNumQuickTiles(this.mMaxTiles);
        }
    }

    public void setHost(QSTileHost qSTileHost) {
        super.setHost(qSTileHost);
        setTiles(this.mHost.getTiles());
    }

    public void setMaxTiles(int i) {
        if (this.mMaxTiles != i) {
            this.mMaxTiles = i;
            QSAnimator qSAnimator = this.mQsAnimator;
            if (qSAnimator != null) {
                qSAnimator.setNumQuickTiles(i);
            }
            QSTileHost qSTileHost = this.mHost;
            if (qSTileHost != null) {
                setTiles(qSTileHost.getTiles());
            }
        }
    }

    public void setTiles(Collection<QSTile> collection) {
        ArrayList arrayList = new ArrayList();
        for (QSTile add : collection) {
            arrayList.add(add);
            if (arrayList.size() == this.mMaxTiles) {
                break;
            }
        }
        super.setTiles(arrayList, true);
    }

    public void updateResources(boolean z) {
        setMaxTiles(getResources().getInteger(R.integer.quick_settings_qqs_count));
    }

    private static class HeaderTileLayout extends LinearLayout implements QSPanel.QSTileLayout {
        private int mContentPaddingBottom;
        private int mContentPaddingHorizontal;
        private int mContentPaddingTop;
        private boolean mListening;
        protected final ArrayList<QSPanel.TileRecord> mRecords = new ArrayList<>();

        public int getOffsetTop(QSPanel.TileRecord tileRecord) {
            return 0;
        }

        public boolean hasOverlappingRendering() {
            return false;
        }

        public HeaderTileLayout(Context context) {
            super(context);
            setClipChildren(false);
            setClipToPadding(false);
            setGravity(16);
            updateResources();
        }

        public void setListening(boolean z) {
            if (this.mListening != z) {
                this.mListening = z;
                Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    it.next().tile.setListening(this, this.mListening);
                }
            }
        }

        public void addTile(QSPanel.TileRecord tileRecord) {
            if (getChildCount() != 0) {
                addView(new Space(this.mContext), getChildCount(), generateSpaceParams());
            }
            addView(tileRecord.tileView, getChildCount(), generateLayoutParams());
            this.mRecords.add(tileRecord);
            tileRecord.tile.setListening(this, this.mListening);
        }

        private LinearLayout.LayoutParams generateSpaceParams() {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_icon_bg_size));
            layoutParams.weight = 1.0f;
            layoutParams.gravity = 17;
            return layoutParams;
        }

        private LinearLayout.LayoutParams generateLayoutParams() {
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_icon_bg_size);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
            layoutParams.gravity = 17;
            return layoutParams;
        }

        public void removeTile(QSPanel.TileRecord tileRecord) {
            int childIndex = getChildIndex(tileRecord.tileView);
            removeViewAt(childIndex);
            if (getChildCount() != 0) {
                removeViewAt(childIndex);
            }
            this.mRecords.remove(tileRecord);
            tileRecord.tile.setListening(this, false);
        }

        private int getChildIndex(QSTileView qSTileView) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (getChildAt(i) == qSTileView) {
                    return i;
                }
            }
            return -1;
        }

        public boolean updateResources() {
            Resources resources = getResources();
            this.mContentPaddingHorizontal = resources.getDimensionPixelSize(R.dimen.qs_quick_panel_content_padding_horizontal);
            this.mContentPaddingTop = resources.getDimensionPixelSize(R.dimen.qs_quick_panel_content_padding_top);
            this.mContentPaddingBottom = resources.getDimensionPixelSize(R.dimen.qs_quick_panel_content_padding_bottom);
            int i = this.mContentPaddingHorizontal;
            setPadding(i, this.mContentPaddingTop, i, this.mContentPaddingBottom);
            return true;
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            super.onMeasure(i, i2);
            ArrayList<QSPanel.TileRecord> arrayList = this.mRecords;
            if (arrayList != null && arrayList.size() > 0) {
                Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
                View view = this;
                while (it.hasNext()) {
                    QSPanel.TileRecord next = it.next();
                    if (next.tileView.getVisibility() != 8) {
                        view = next.tileView.updateAccessibilityOrder(view);
                    }
                }
                this.mRecords.get(0).tileView.setAccessibilityTraversalAfter(R.id.alarm_status_collapsed);
                ArrayList<QSPanel.TileRecord> arrayList2 = this.mRecords;
                arrayList2.get(arrayList2.size() - 1).tileView.setAccessibilityTraversalBefore(R.id.expand_indicator);
            }
        }
    }
}
