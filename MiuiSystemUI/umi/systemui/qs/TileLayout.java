package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Iterator;

public class TileLayout extends ViewGroup implements QSPanel.QSTileLayout {
    protected int mCellHeight;
    protected int mCellWidth;
    protected int mColumns;
    protected int mContentHeight;
    protected int mContentMarginBottom;
    protected int mContentMarginHorizontal;
    protected int mContentMarginTop;
    private boolean mListening;
    protected final ArrayList<QSPanel.TileRecord> mRecords;
    protected int mRows;

    public TileLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public TileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRecords = new ArrayList<>();
        setFocusableInTouchMode(true);
        updateResources();
    }

    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        return getTop();
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
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this.mListening);
        addView(tileRecord.tileView);
    }

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

    public boolean updateResources() {
        Resources resources = this.mContext.getResources();
        int max = Math.max(1, resources.getInteger(R.integer.quick_settings_num_columns));
        int max2 = Math.max(1, resources.getInteger(R.integer.quick_settings_num_rows));
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.qs_tile_content_height);
        this.mContentMarginTop = resources.getDimensionPixelSize(R.dimen.qs_tile_content_margin_top);
        this.mContentMarginHorizontal = resources.getDimensionPixelSize(R.dimen.qs_tile_content_margin_horizontal);
        this.mContentMarginBottom = resources.getDimensionPixelSize(R.dimen.qs_tile_content_margin_bottom);
        if (this.mColumns == max && this.mRows == max2 && this.mContentHeight == dimensionPixelSize) {
            return false;
        }
        this.mColumns = max;
        this.mRows = max2;
        this.mContentHeight = dimensionPixelSize;
        requestLayout();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        this.mCellWidth = (size - (this.mContentMarginHorizontal * 2)) / this.mColumns;
        this.mCellHeight = ((this.mContentHeight - this.mContentMarginBottom) - this.mContentMarginTop) / this.mRows;
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        View view = this;
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            if (next.tileView.getVisibility() != 8) {
                next.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                view = next.tileView.updateAccessibilityOrder(view);
            }
        }
        setMeasuredDimension(size, this.mContentHeight);
    }

    private static int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int width = getWidth();
        int i6 = 0;
        boolean z2 = true;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        int i7 = 0;
        int i8 = 0;
        while (i6 < this.mRecords.size()) {
            int i9 = this.mColumns;
            if (i7 == i9) {
                i8++;
                i7 -= i9;
            }
            QSPanel.TileRecord tileRecord = this.mRecords.get(i6);
            int columnStart = getColumnStart(i7);
            int rowTop = getRowTop(i8);
            if (z2) {
                int i10 = width - columnStart;
                i5 = i10;
                columnStart = i10 - this.mCellWidth;
            } else {
                i5 = this.mCellWidth + columnStart;
            }
            QSTileView qSTileView = tileRecord.tileView;
            qSTileView.layout(columnStart, rowTop, i5, qSTileView.getMeasuredHeight() + rowTop);
            i6++;
            i7++;
        }
    }

    private int getRowTop(int i) {
        return (i * this.mCellHeight) + this.mContentMarginTop;
    }

    private int getColumnStart(int i) {
        return (i * this.mCellWidth) + this.mContentMarginHorizontal;
    }
}
