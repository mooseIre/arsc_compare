package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;

public class PagedTileLayout extends ViewPager implements QSPanel.QSTileLayout {
    private final PagerAdapter mAdapter = new PagerAdapter() {
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
            viewGroup.removeView((View) obj);
        }

        public Object instantiateItem(ViewGroup viewGroup, int i) {
            if (PagedTileLayout.this.isLayoutRtl()) {
                i = (PagedTileLayout.this.mPages.size() - 1) - i;
            }
            ViewGroup viewGroup2 = (ViewGroup) PagedTileLayout.this.mPages.get(i);
            viewGroup.addView(viewGroup2);
            return viewGroup2;
        }

        public int getCount() {
            return PagedTileLayout.this.mNumPages;
        }
    };
    private final Runnable mDistribute = new Runnable() {
        public void run() {
            PagedTileLayout.this.distributeTiles();
        }
    };
    private int mLayoutDirection;
    private boolean mListening;
    /* access modifiers changed from: private */
    public int mNumPages;
    private boolean mOffPage;
    private boolean mOldModeOn = false;
    /* access modifiers changed from: private */
    public PageIndicator mPageIndicator;
    /* access modifiers changed from: private */
    public PageListener mPageListener;
    private int mPageToRestore = -1;
    /* access modifiers changed from: private */
    public final ArrayList<TilePage> mPages = new ArrayList<>();
    private int mPosition;
    private final ArrayList<QSPanel.TileRecord> mTiles = new ArrayList<>();

    public interface PageListener {
        void onPageChanged(boolean z);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public PagedTileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setAdapter(this.mAdapter);
        setOverScrollMode(2);
        setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int i) {
            }

            public void onPageSelected(int i) {
                if (PagedTileLayout.this.mPageIndicator != null && PagedTileLayout.this.mPageListener != null) {
                    PageListener access$100 = PagedTileLayout.this.mPageListener;
                    boolean z = false;
                    if (!PagedTileLayout.this.isLayoutRtl() ? i == 0 : i == PagedTileLayout.this.mPages.size() - 1) {
                        z = true;
                    }
                    access$100.onPageChanged(z);
                }
            }

            public void onPageScrolled(int i, float f, int i2) {
                if (PagedTileLayout.this.mPageIndicator != null) {
                    boolean z = false;
                    PagedTileLayout.this.setCurrentPage(i, f != 0.0f);
                    PagedTileLayout.this.mPageIndicator.setLocation(((float) i) + f);
                    if (PagedTileLayout.this.mPageListener != null) {
                        PageListener access$100 = PagedTileLayout.this.mPageListener;
                        if (i2 == 0 && (!PagedTileLayout.this.isLayoutRtl() ? i == 0 : i == PagedTileLayout.this.mPages.size() - 1)) {
                            z = true;
                        }
                        access$100.onPageChanged(z);
                    }
                }
            }
        });
        setCurrentItem(0);
        this.mLayoutDirection = getLayoutDirection();
    }

    public void saveInstanceState(Bundle bundle) {
        bundle.putInt("current_page", getCurrentItem());
    }

    public void restoreInstanceState(Bundle bundle) {
        this.mPageToRestore = bundle.getInt("current_page", -1);
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        if (this.mLayoutDirection != i) {
            this.mLayoutDirection = i;
            setAdapter(this.mAdapter);
            setCurrentItem(0, false);
            this.mPageToRestore = 0;
        }
    }

    public void setCurrentItem(int i, boolean z) {
        if (isLayoutRtl()) {
            i = (this.mPages.size() - 1) - i;
        }
        super.setCurrentItem(i, z);
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            if (this.mListening) {
                setPageListening(this.mPosition, true);
                if (this.mOffPage) {
                    setPageListening(this.mPosition + 1, true);
                    return;
                }
                return;
            }
            for (int i = 0; i < this.mPages.size(); i++) {
                this.mPages.get(i).setListening(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setCurrentPage(int i, boolean z) {
        if (this.mPosition != i || this.mOffPage != z) {
            if (this.mListening) {
                int i2 = this.mPosition;
                if (i2 != i) {
                    setPageListening(i2, false);
                    if (this.mOffPage) {
                        setPageListening(this.mPosition + 1, false);
                    }
                    setPageListening(i, true);
                    if (z) {
                        setPageListening(i + 1, true);
                    }
                } else if (this.mOffPage != z) {
                    setPageListening(i2 + 1, z);
                }
            }
            this.mPosition = i;
            this.mOffPage = z;
        }
    }

    private void setPageListening(int i, boolean z) {
        if (i < this.mPages.size()) {
            if (isLayoutRtl()) {
                i = (this.mPages.size() - 1) - i;
            }
            this.mPages.get(i).setListening(z);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPages.add((TilePage) LayoutInflater.from(getContext()).inflate(R.layout.qs_paged_page, this, false));
    }

    public void setPageIndicator(PageIndicator pageIndicator) {
        this.mPageIndicator = pageIndicator;
    }

    public void setOldModeOn(boolean z) {
        if (this.mOldModeOn != z) {
            this.mOldModeOn = z;
            emptyPage();
            distributeTiles();
        }
    }

    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        ViewGroup viewGroup = (ViewGroup) tileRecord.tileView.getParent();
        if (viewGroup == null) {
            return 0;
        }
        return viewGroup.getTop() + getTop();
    }

    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mTiles.add(tileRecord);
        postDistributeTiles();
    }

    public void removeTile(QSPanel.TileRecord tileRecord) {
        if (this.mTiles.remove(tileRecord)) {
            postDistributeTiles();
        }
    }

    public void setPageListener(PageListener pageListener) {
        this.mPageListener = pageListener;
    }

    private void postDistributeTiles() {
        removeCallbacks(this.mDistribute);
        post(this.mDistribute);
    }

    private void emptyPage() {
        int size = this.mPages.size();
        for (int i = 0; i < size; i++) {
            this.mPages.get(i).removeAllViews();
        }
        while (this.mPages.size() > 0) {
            ArrayList<TilePage> arrayList = this.mPages;
            arrayList.remove(arrayList.size() - 1);
        }
        addTilePage();
        this.mNumPages = 1;
        this.mPageIndicator.setNumPages(this.mNumPages);
        this.mPageIndicator.setVisibility(8);
        setAdapter(this.mAdapter);
        this.mAdapter.notifyDataSetChanged();
    }

    /* access modifiers changed from: private */
    public void distributeTiles() {
        int i;
        int size = this.mPages.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mPages.get(i2).removeAllViews();
        }
        int size2 = this.mTiles.size();
        int i3 = 0;
        for (int i4 = 0; i4 < size2; i4++) {
            QSPanel.TileRecord tileRecord = this.mTiles.get(i4);
            if (this.mPages.get(i3).isFull() && (i3 = i3 + 1) == this.mPages.size()) {
                addTilePage();
            }
            this.mPages.get(i3).addTile(tileRecord);
        }
        int i5 = i3 + 1;
        if (this.mNumPages != i5) {
            this.mNumPages = i5;
            while (true) {
                int size3 = this.mPages.size();
                i = this.mNumPages;
                if (size3 <= i) {
                    break;
                }
                ArrayList<TilePage> arrayList = this.mPages;
                arrayList.remove(arrayList.size() - 1);
            }
            this.mPageIndicator.setNumPages(i);
            this.mPageIndicator.setVisibility(this.mNumPages > 1 ? 0 : 8);
            setAdapter(this.mAdapter);
            this.mAdapter.notifyDataSetChanged();
            int i6 = this.mPageToRestore;
            if (i6 == -1) {
                i6 = 0;
            }
            setCurrentItem(i6, false);
        } else {
            int i7 = this.mPageToRestore;
            if (i7 != -1) {
                setCurrentItem(i7, false);
                this.mPageToRestore = -1;
            }
        }
        this.mPageToRestore = -1;
    }

    private void addTilePage() {
        if (this.mOldModeOn) {
            this.mPages.add((TilePage) LayoutInflater.from(getContext()).inflate(R.layout.qs_old_mode_paged_page, this, false));
        } else {
            this.mPages.add((TilePage) LayoutInflater.from(getContext()).inflate(R.layout.qs_paged_page, this, false));
        }
    }

    public boolean updateResources() {
        boolean z = false;
        for (int i = 0; i < this.mPages.size(); i++) {
            z |= this.mPages.get(i).updateResources();
        }
        if (z) {
            distributeTiles();
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int childCount = getChildCount();
        int i3 = 0;
        for (int i4 = 0; i4 < childCount; i4++) {
            int measuredHeight = getChildAt(i4).getMeasuredHeight();
            if (measuredHeight > i3) {
                i3 = measuredHeight;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), i3 + getPaddingBottom());
    }

    public int getColumnCount() {
        if (this.mPages.size() == 0) {
            return 0;
        }
        return this.mPages.get(0).mColumns;
    }

    public static class TilePage extends TileLayout {
        public TilePage(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public boolean isFull() {
            return this.mRecords.size() >= this.mColumns * this.mRows;
        }
    }

    public static class OldModeTilePage extends TilePage {
        public OldModeTilePage(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public boolean updateResources() {
            int i;
            Resources resources = this.mContext.getResources();
            int max = Math.max(1, resources.getInteger(R.integer.quick_settings_num_columns));
            int max2 = Math.max(1, resources.getInteger(R.integer.quick_settings_old_mode_num_rows));
            if (resources.getConfiguration().orientation == 1) {
                i = resources.getDimensionPixelSize(R.dimen.qs_tile_old_mode_content_height);
            } else {
                i = resources.getDimensionPixelSize(R.dimen.qs_tile_content_height);
            }
            this.mContentMarginTop = resources.getDimensionPixelSize(R.dimen.qs_tile_content_margin_top);
            this.mContentMarginHorizontal = resources.getDimensionPixelSize(R.dimen.qs_tile_content_margin_horizontal);
            this.mContentMarginBottom = resources.getDimensionPixelSize(R.dimen.qs_tile_content_margin_bottom);
            if (this.mColumns == max && this.mRows == max2 && this.mContentHeight == i) {
                return false;
            }
            this.mColumns = max;
            this.mRows = max2;
            this.mContentHeight = i;
            requestLayout();
            return true;
        }
    }
}
