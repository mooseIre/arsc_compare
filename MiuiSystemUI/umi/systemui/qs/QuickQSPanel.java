package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.customize.MiuiQSCustomizer;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class QuickQSPanel extends QSPanel {
    private static int sDefaultMaxTiles = 6;
    private boolean mDisabledByPolicy;
    private int mMaxTiles;
    private final TunerService.Tunable mNumTiles = new TunerService.Tunable(this) {
        /* class com.android.systemui.qs.QuickQSPanel.AnonymousClass1 */

        @Override // com.android.systemui.tuner.TunerService.Tunable
        public void onTuningChanged(String str, String str2) {
        }
    };
    protected QSAnimator mQsAnimator;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public void addSecurityFooter() {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public boolean displayMediaMarginsOnMedia() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public String getDumpableTag() {
        return "QuickQSPanel";
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public int getTileCallbackType() {
        return 4;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public boolean needsDynamicRowsAndColumns() {
        return false;
    }

    public void setQSPanel(QSPanel qSPanel) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public void updatePadding() {
    }

    public QuickQSPanel(Context context, AttributeSet attributeSet, DumpManager dumpManager, BroadcastDispatcher broadcastDispatcher, QSLogger qSLogger, MediaHost mediaHost, UiEventLogger uiEventLogger) {
        super(context, attributeSet, dumpManager, broadcastDispatcher, qSLogger, mediaHost, uiEventLogger);
        sDefaultMaxTiles = getResources().getInteger(C0016R$integer.quick_settings_qqs_count);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public TileLayout createRegularTileLayout() {
        return new HeaderTileLayout(this.mContext, this.mUiEventLogger);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public QSPanel.QSTileLayout createHorizontalTileLayout() {
        return new DoubleLineTileLayout(this.mContext, this.mUiEventLogger);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public void initMediaHostState() {
        this.mMediaHost.setExpansion(0.0f);
        this.mMediaHost.setShowsOnlyActiveMedia(true);
        this.mMediaHost.init(1);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this.mNumTiles, "sysui_qqs_count");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this.mNumTiles);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public boolean shouldShowDetail() {
        return !this.mExpanded;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
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

    @Override // com.android.systemui.qs.QSPanel
    public void setHost(QSTileHost qSTileHost, MiuiQSCustomizer miuiQSCustomizer) {
        super.setHost(qSTileHost, miuiQSCustomizer);
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

    @Override // com.android.systemui.tuner.TunerService.Tunable, com.android.systemui.qs.QSPanel
    public void onTuningChanged(String str, String str2) {
        if ("qs_show_brightness".equals(str)) {
            super.onTuningChanged(str, "0");
        }
    }

    @Override // com.android.systemui.qs.QSPanel
    public void setTiles(Collection<QSTile> collection) {
        if (!((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter()) {
            ArrayList arrayList = new ArrayList();
            for (QSTile qSTile : collection) {
                arrayList.add(qSTile);
                if (arrayList.size() == this.mMaxTiles) {
                    break;
                }
            }
            super.setTiles(arrayList, true);
        }
    }

    public int getNumQuickTiles() {
        return this.mMaxTiles;
    }

    public static int getDefaultMaxTiles() {
        return sDefaultMaxTiles;
    }

    /* access modifiers changed from: package-private */
    public void setDisabledByPolicy(boolean z) {
        if (z != this.mDisabledByPolicy) {
            this.mDisabledByPolicy = z;
            setVisibility(z ? 8 : 0);
        }
    }

    public void setVisibility(int i) {
        if (this.mDisabledByPolicy) {
            if (getVisibility() != 8) {
                i = 8;
            } else {
                return;
            }
        }
        super.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public QSEvent openPanelEvent() {
        return QSEvent.QQS_PANEL_EXPANDED;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.QSPanel
    public QSEvent closePanelEvent() {
        return QSEvent.QQS_PANEL_COLLAPSED;
    }

    /* access modifiers changed from: private */
    public static class HeaderTileLayout extends TileLayout {
        private Rect mClippingBounds = new Rect();
        private final UiEventLogger mUiEventLogger;

        public HeaderTileLayout(Context context, UiEventLogger uiEventLogger) {
            super(context);
            this.mUiEventLogger = uiEventLogger;
            setClipChildren(false);
            setClipToPadding(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            layoutParams.gravity = 1;
            setLayoutParams(layoutParams);
        }

        /* access modifiers changed from: protected */
        public void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            updateResources();
        }

        public void onFinishInflate() {
            super.onFinishInflate();
            updateResources();
        }

        private ViewGroup.LayoutParams generateTileLayoutParams() {
            return new ViewGroup.LayoutParams(this.mCellWidth, this.mCellHeight);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.TileLayout
        public void addTileView(QSPanel.TileRecord tileRecord) {
            addView(tileRecord.tileView, getChildCount(), generateTileLayoutParams());
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.TileLayout
        public void onLayout(boolean z, int i, int i2, int i3, int i4) {
            this.mClippingBounds.set(0, 0, i3 - i, 10000);
            setClipBounds(this.mClippingBounds);
            calculateColumns();
            int i5 = 0;
            while (i5 < this.mRecords.size()) {
                this.mRecords.get(i5).tileView.setVisibility(i5 < this.mColumns ? 0 : 8);
                i5++;
            }
            setAccessibilityOrder();
            layoutTileRecords(this.mColumns);
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout, com.android.systemui.qs.TileLayout
        public boolean updateResources() {
            Resources resources = ((ViewGroup) this).mContext.getResources();
            int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.qs_tile_icon_bg_size);
            this.mCellWidth = dimensionPixelSize;
            this.mCellHeight = dimensionPixelSize;
            int dimensionPixelSize2 = resources.getDimensionPixelSize(C0012R$dimen.qs_quick_panel_content_padding_horizontal);
            int dimensionPixelSize3 = resources.getDimensionPixelSize(C0012R$dimen.qs_quick_panel_content_padding_top);
            int dimensionPixelSize4 = resources.getDimensionPixelSize(C0012R$dimen.qs_quick_panel_content_padding_top);
            setPaddingRelative(getPaddingStart(), dimensionPixelSize3, getPaddingEnd(), dimensionPixelSize4);
            setPadding(dimensionPixelSize2, dimensionPixelSize3, dimensionPixelSize2, dimensionPixelSize4);
            return false;
        }

        private boolean calculateColumns() {
            int i;
            int i2 = this.mColumns;
            int size = this.mRecords.size();
            if (size == 0) {
                this.mColumns = 0;
                return true;
            }
            int measuredWidth = (getMeasuredWidth() - getPaddingStart()) - getPaddingEnd();
            int max = (measuredWidth - (this.mCellWidth * size)) / Math.max(1, size - 1);
            if (max > 0) {
                this.mCellMarginHorizontal = max;
                this.mColumns = size;
            } else {
                int i3 = this.mCellWidth;
                if (i3 == 0) {
                    i = 1;
                } else {
                    i = Math.min(size, measuredWidth / i3);
                }
                this.mColumns = i;
                if (i == 1) {
                    this.mCellMarginHorizontal = (measuredWidth - this.mCellWidth) / 2;
                } else {
                    this.mCellMarginHorizontal = (measuredWidth - (this.mCellWidth * i)) / (i - 1);
                }
            }
            return this.mColumns != i2;
        }

        /* JADX DEBUG: Failed to insert an additional move for type inference into block B:11:0x0011 */
        /* JADX DEBUG: Failed to insert an additional move for type inference into block B:13:0x0011 */
        private void setAccessibilityOrder() {
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
                ArrayList<QSPanel.TileRecord> arrayList2 = this.mRecords;
                arrayList2.get(arrayList2.size() - 1).tileView.setAccessibilityTraversalBefore(C0015R$id.expand_indicator);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.TileLayout
        public void onMeasure(int i, int i2) {
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                if (next.tileView.getVisibility() != 8) {
                    next.tileView.measure(TileLayout.exactly(this.mCellWidth), TileLayout.exactly(this.mCellHeight));
                }
            }
            int paddingTop = this.mCellHeight + getPaddingTop() + getPaddingBottom();
            if (paddingTop < 0) {
                paddingTop = 0;
            }
            setMeasuredDimension(View.MeasureSpec.getSize(i), paddingTop);
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout, com.android.systemui.qs.TileLayout
        public int getNumVisibleTiles() {
            return this.mColumns;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.qs.TileLayout
        public int getColumnStart(int i) {
            if (this.mColumns == 1) {
                return getPaddingStart() + this.mCellMarginHorizontal;
            }
            return getPaddingStart() + (i * (this.mCellWidth + this.mCellMarginHorizontal));
        }

        @Override // com.android.systemui.qs.QSPanel.QSTileLayout, com.android.systemui.qs.TileLayout
        public void setListening(boolean z) {
            boolean z2 = !this.mListening && z;
            super.setListening(z);
            if (z2) {
                Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
                while (it.hasNext()) {
                    QSTile qSTile = it.next().tile;
                    this.mUiEventLogger.logWithInstanceId(QSEvent.QQS_TILE_VISIBLE, 0, qSTile.getMetricsSpec(), qSTile.getInstanceId());
                }
            }
        }
    }

    public void setQsAnimator(QSAnimator qSAnimator) {
        this.mQsAnimator = qSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setNumQuickTiles(this.mMaxTiles);
        }
    }

    @Override // com.android.systemui.qs.QSPanel
    public void updateResources() {
        setMaxTiles(getResources().getInteger(C0016R$integer.quick_settings_qqs_count));
    }
}
