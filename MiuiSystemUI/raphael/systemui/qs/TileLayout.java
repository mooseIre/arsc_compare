package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0016R$integer;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Iterator;

public class TileLayout extends ViewGroup implements QSPanel.QSTileLayout {
    protected int mCellHeight;
    protected int mCellMarginHorizontal;
    private int mCellMarginTop;
    protected int mCellMarginVertical;
    protected int mCellWidth;
    protected int mColumns;
    private final boolean mLessRows;
    protected boolean mListening;
    protected int mMaxAllowedRows;
    private int mMaxColumns;
    private int mMinRows;
    protected final ArrayList<QSPanel.TileRecord> mRecords;
    private int mResourceColumns;
    protected int mRows;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public TileLayout(Context context) {
        this(context, null);
    }

    public TileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRows = 1;
        this.mRecords = new ArrayList<>();
        this.mMaxAllowedRows = 3;
        this.mMinRows = 1;
        this.mMaxColumns = 100;
        setFocusableInTouchMode(true);
        this.mLessRows = false;
        updateResources();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        return getTop();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                it.next().tile.setListening(this, this.mListening);
            }
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean setMinRows(int i) {
        if (this.mMinRows == i) {
            return false;
        }
        this.mMinRows = i;
        updateResources();
        return true;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean setMaxColumns(int i) {
        this.mMaxColumns = i;
        return updateColumns();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this.mListening);
        addTileView(tileRecord);
    }

    /* access modifiers changed from: protected */
    public void addTileView(QSPanel.TileRecord tileRecord) {
        addView(tileRecord.tileView);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.remove(tileRecord);
        tileRecord.tile.setListening(this, false);
        removeView(tileRecord.tileView);
    }

    public void removeAllViews() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.setListening(this, false);
        }
        this.mRecords.clear();
        super.removeAllViews();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean updateResources() {
        Resources resources = ((ViewGroup) this).mContext.getResources();
        this.mResourceColumns = Math.max(1, resources.getInteger(C0016R$integer.quick_settings_num_columns));
        this.mCellHeight = ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_tile_height);
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.qs_tile_content_margin_top);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(C0012R$dimen.qs_tile_content_margin_horizontal);
        setPadding(dimensionPixelSize2, dimensionPixelSize, dimensionPixelSize2, getPaddingBottom());
        setClipToPadding(false);
        int max = Math.max(1, getResources().getInteger(C0016R$integer.quick_settings_num_rows));
        this.mMaxAllowedRows = max;
        if (this.mLessRows) {
            this.mMaxAllowedRows = Math.max(this.mMinRows, max - 1);
        }
        if (!updateColumns()) {
            return false;
        }
        requestLayout();
        return true;
    }

    private boolean updateColumns() {
        int i = this.mColumns;
        int min = Math.min(this.mResourceColumns, this.mMaxColumns);
        this.mColumns = min;
        return i != min;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:18:0x0033 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:20:0x0033 */
    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int size = this.mRecords.size();
        int size2 = View.MeasureSpec.getSize(i);
        int paddingStart = (size2 - getPaddingStart()) - getPaddingEnd();
        if (View.MeasureSpec.getMode(i2) == 0) {
            int i3 = this.mColumns;
            this.mRows = ((size + i3) - 1) / i3;
        }
        int i4 = this.mCellMarginHorizontal;
        int i5 = this.mColumns;
        this.mCellWidth = (paddingStart - (i4 * i5)) / i5;
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        View view = this;
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            if (next.tileView.getVisibility() != 8) {
                next.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                view = next.tileView.updateAccessibilityOrder(view);
            }
        }
        int i6 = this.mCellHeight;
        int i7 = this.mCellMarginVertical;
        int i8 = this.mRows;
        int i9 = 0;
        int paddingTop = ((i6 + i7) * i8) + (i8 != 0 ? this.mCellMarginTop - i7 : 0) + getPaddingTop();
        if (paddingTop >= 0) {
            i9 = paddingTop;
        }
        setMeasuredDimension(size2, i9);
    }

    public boolean updateMaxRows(int i, int i2) {
        int i3 = i - this.mCellMarginTop;
        int i4 = this.mCellMarginVertical;
        int i5 = this.mRows;
        int i6 = (i3 + i4) / (this.mCellHeight + i4);
        this.mRows = i6;
        int i7 = this.mMinRows;
        if (i6 < i7) {
            this.mRows = i7;
        } else {
            int i8 = this.mMaxAllowedRows;
            if (i6 >= i8) {
                this.mRows = i8;
            }
        }
        int i9 = this.mRows;
        int i10 = this.mColumns;
        if (i9 > ((i2 + i10) - 1) / i10) {
            this.mRows = ((i2 + i10) - 1) / i10;
        }
        if (i5 != this.mRows) {
            return true;
        }
        return false;
    }

    protected static int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    /* access modifiers changed from: protected */
    public void layoutTileRecords(int i) {
        boolean z = getLayoutDirection() == 1;
        int min = Math.min(i, this.mRows * this.mColumns);
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        while (i2 < min) {
            if (i3 == this.mColumns) {
                i4++;
                i3 = 0;
            }
            QSPanel.TileRecord tileRecord = this.mRecords.get(i2);
            int rowTop = getRowTop(i4);
            int columnStart = getColumnStart(z ? (this.mColumns - i3) - 1 : i3);
            QSTileView qSTileView = tileRecord.tileView;
            qSTileView.layout(columnStart, rowTop, this.mCellWidth + columnStart, qSTileView.getMeasuredHeight() + rowTop);
            i2++;
            i3++;
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        layoutTileRecords(this.mRecords.size());
    }

    private int getRowTop(int i) {
        return (i * (this.mCellHeight + this.mCellMarginVertical)) + this.mCellMarginTop + getPaddingTop();
    }

    /* access modifiers changed from: protected */
    public int getColumnStart(int i) {
        int paddingStart = getPaddingStart();
        int i2 = this.mCellMarginHorizontal;
        return paddingStart + (i2 / 2) + (i * (this.mCellWidth + i2));
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getNumVisibleTiles() {
        return this.mRecords.size();
    }
}
