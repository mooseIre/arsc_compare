package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DoubleLineTileLayout.kt */
public final class DoubleLineTileLayout extends ViewGroup implements QSPanel.QSTileLayout {
    private boolean _listening;
    private int cellMarginHorizontal;
    private int cellMarginVertical;
    @NotNull
    private final ArrayList<QSPanel.TileRecord> mRecords = new ArrayList<>();
    private int smallTileSize;
    private int tilesToShow;
    private final UiEventLogger uiEventLogger;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public DoubleLineTileLayout(@NotNull Context context, @NotNull UiEventLogger uiEventLogger2) {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(uiEventLogger2, "uiEventLogger");
        this.uiEventLogger = uiEventLogger2;
        setFocusableInTouchMode(true);
        setClipChildren(false);
        setClipToPadding(false);
        updateResources();
    }

    private final int getTwoLineHeight() {
        return (this.smallTileSize * 2) + (this.cellMarginVertical * 1);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(@NotNull QSPanel.TileRecord tileRecord) {
        Intrinsics.checkParameterIsNotNull(tileRecord, "tile");
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this._listening);
        addTileView(tileRecord);
    }

    /* access modifiers changed from: protected */
    public final void addTileView(@NotNull QSPanel.TileRecord tileRecord) {
        Intrinsics.checkParameterIsNotNull(tileRecord, "tile");
        addView(tileRecord.tileView);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(@NotNull QSPanel.TileRecord tileRecord) {
        Intrinsics.checkParameterIsNotNull(tileRecord, "tile");
        this.mRecords.remove(tileRecord);
        tileRecord.tile.setListening(this, false);
        removeView(tileRecord.tileView);
    }

    public void removeAllViews() {
        Iterator<T> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.setListening(this, false);
        }
        this.mRecords.clear();
        super.removeAllViews();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(@Nullable QSPanel.TileRecord tileRecord) {
        return getTop();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean updateResources() {
        Context context = ((ViewGroup) this).mContext;
        Intrinsics.checkExpressionValueIsNotNull(context, "mContext");
        Resources resources = context.getResources();
        this.smallTileSize = resources.getDimensionPixelSize(C0012R$dimen.qs_quick_tile_size);
        this.cellMarginHorizontal = resources.getDimensionPixelSize(C0012R$dimen.qs_tile_margin_horizontal_two_line);
        this.cellMarginVertical = resources.getDimensionPixelSize(C0012R$dimen.new_qs_vertical_margin);
        requestLayout();
        return false;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setListening(boolean z) {
        if (this._listening != z) {
            this._listening = z;
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                it.next().tile.setListening(this, z);
            }
            if (z) {
                int numVisibleTiles = getNumVisibleTiles();
                for (int i = 0; i < numVisibleTiles; i++) {
                    QSTile qSTile = this.mRecords.get(i).tile;
                    UiEventLogger uiEventLogger2 = this.uiEventLogger;
                    QSEvent qSEvent = QSEvent.QQS_TILE_VISIBLE;
                    Intrinsics.checkExpressionValueIsNotNull(qSTile, "tile");
                    uiEventLogger2.logWithInstanceId(qSEvent, 0, qSTile.getMetricsSpec(), qSTile.getInstanceId());
                }
            }
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getNumVisibleTiles() {
        return this.tilesToShow;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        updateResources();
        postInvalidate();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        updateResources();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        Iterator<T> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tileView.measure(TileLayout.exactly(this.smallTileSize), TileLayout.exactly(this.smallTileSize));
        }
        setMeasuredDimension(View.MeasureSpec.getSize(i), getTwoLineHeight() + getPaddingBottom() + getPaddingTop());
    }

    private final int calculateMaxColumns(int i) {
        int i2 = this.smallTileSize;
        int i3 = this.cellMarginHorizontal;
        if (i2 + i3 == 0) {
            return 0;
        }
        return ((i - i2) / (i2 + i3)) + 1;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingLeft = ((i3 - i) - getPaddingLeft()) - getPaddingRight();
        int min = Math.min(calculateMaxColumns(paddingLeft), this.mRecords.size() / 2);
        if (min != 0) {
            this.tilesToShow = min * 2;
            int i5 = paddingLeft / min;
            int size = this.mRecords.size();
            for (int i6 = 0; i6 < size; i6++) {
                QSTileView qSTileView = this.mRecords.get(i6).tileView;
                if (i6 >= this.tilesToShow) {
                    Intrinsics.checkExpressionValueIsNotNull(qSTileView, "tileView");
                    qSTileView.setVisibility(8);
                } else {
                    Intrinsics.checkExpressionValueIsNotNull(qSTileView, "tileView");
                    qSTileView.setVisibility(0);
                    if (i6 > 0) {
                        qSTileView.updateAccessibilityOrder(this.mRecords.get(i6 - 1).tileView);
                    }
                    int leftForColumn = getLeftForColumn(i6 % min, i5);
                    int topBottomRow = i6 < min ? 0 : getTopBottomRow();
                    int i7 = this.smallTileSize;
                    qSTileView.layout(leftForColumn, topBottomRow, leftForColumn + i7, i7 + topBottomRow);
                }
            }
        }
    }

    private final int getLeftForColumn(int i, int i2) {
        return (int) ((((float) (i * i2)) + (((float) i2) / 2.0f)) - (((float) this.smallTileSize) / 2.0f));
    }

    private final int getTopBottomRow() {
        return this.smallTileSize + this.cellMarginVertical;
    }
}
