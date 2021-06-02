package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0017R$layout;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class PagedTileLayout extends ViewPager implements QSPanel.QSTileLayout {
    private static final Interpolator SCROLL_CUBIC = $$Lambda$PagedTileLayout$fHkBmUM3caZV4_eDd9apVT7Ho.INSTANCE;
    private final PagerAdapter mAdapter = new PagerAdapter() {
        /* class com.android.systemui.qs.PagedTileLayout.AnonymousClass3 */

        @Override // androidx.viewpager.widget.PagerAdapter
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
            viewGroup.removeView((View) obj);
            PagedTileLayout.this.updateListening();
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public Object instantiateItem(ViewGroup viewGroup, int i) {
            if (PagedTileLayout.this.isLayoutRtl()) {
                i = (PagedTileLayout.this.mPages.size() - 1) - i;
            }
            ViewGroup viewGroup2 = (ViewGroup) PagedTileLayout.this.mPages.get(i);
            if (viewGroup2.getParent() != null) {
                viewGroup.removeView(viewGroup2);
            }
            viewGroup.addView(viewGroup2);
            PagedTileLayout.this.updateListening();
            return viewGroup2;
        }

        @Override // androidx.viewpager.widget.PagerAdapter
        public int getCount() {
            return PagedTileLayout.this.mPages.size();
        }
    };
    private AnimatorSet mBounceAnimatorSet;
    private boolean mDistributeTiles = false;
    private int mExcessHeight;
    private int mLastExcessHeight;
    private float mLastExpansion;
    private int mLastMaxHeight = -1;
    private int mLayoutDirection;
    private int mLayoutOrientation;
    private boolean mListening;
    private int mMaxColumns = 100;
    private int mMinRows = 1;
    private final ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        /* class com.android.systemui.qs.PagedTileLayout.AnonymousClass2 */

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageSelected(int i) {
            PagedTileLayout.this.updateSelected();
            if (PagedTileLayout.this.mPageIndicator != null && PagedTileLayout.this.mPageListener != null) {
                PageListener pageListener = PagedTileLayout.this.mPageListener;
                boolean z = false;
                if (!PagedTileLayout.this.isLayoutRtl() ? i == 0 : i == PagedTileLayout.this.mPages.size() - 1) {
                    z = true;
                }
                pageListener.onPageChanged(z);
            }
        }

        @Override // androidx.viewpager.widget.ViewPager.OnPageChangeListener
        public void onPageScrolled(int i, float f, int i2) {
            if (PagedTileLayout.this.mPageIndicator != null) {
                PagedTileLayout.this.mPageIndicatorPosition = ((float) i) + f;
                PagedTileLayout.this.mPageIndicator.setLocation(PagedTileLayout.this.mPageIndicatorPosition);
                if (PagedTileLayout.this.mPageListener != null) {
                    PageListener pageListener = PagedTileLayout.this.mPageListener;
                    boolean z = true;
                    if (i2 != 0 || (!PagedTileLayout.this.isLayoutRtl() ? i != 0 : i != PagedTileLayout.this.mPages.size() - 1)) {
                        z = false;
                    }
                    pageListener.onPageChanged(z);
                }
            }
        }
    };
    private MiuiPageIndicator mPageIndicator;
    private float mPageIndicatorPosition;
    private PageListener mPageListener;
    private int mPageToRestore = -1;
    private final ArrayList<TilePage> mPages = new ArrayList<>();
    private Scroller mScroller;
    private final ArrayList<QSPanel.TileRecord> mTiles = new ArrayList<>();
    private final UiEventLogger mUiEventLogger = QSEvents.INSTANCE.getQsUiEventsLogger();

    public interface PageListener {
        void onPageChanged(boolean z);
    }

    static /* synthetic */ float lambda$static$0(float f) {
        float f2 = f - 1.0f;
        return (f2 * f2 * f2) + 1.0f;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public PagedTileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mScroller = new Scroller(context, SCROLL_CUBIC);
        setAdapter(this.mAdapter);
        setOnPageChangeListener(this.mOnPageChangeListener);
        setCurrentItem(0, false);
        this.mLayoutOrientation = getResources().getConfiguration().orientation;
        this.mLayoutDirection = getLayoutDirection();
        new Rect();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void saveInstanceState(Bundle bundle) {
        bundle.putInt("current_page", getCurrentItem());
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void restoreInstanceState(Bundle bundle) {
        this.mPageToRestore = bundle.getInt("current_page", -1);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = this.mLayoutOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mLayoutOrientation = i2;
            setCurrentItem(0, false);
            this.mPageToRestore = 0;
        }
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

    @Override // androidx.viewpager.widget.ViewPager
    public void setCurrentItem(int i, boolean z) {
        if (isLayoutRtl()) {
            i = (this.mPages.size() - 1) - i;
        }
        super.setCurrentItem(i, z);
    }

    private int getCurrentPageNumber() {
        int currentItem = getCurrentItem();
        return this.mLayoutDirection == 1 ? (this.mPages.size() - 1) - currentItem : currentItem;
    }

    private void logVisibleTiles(TilePage tilePage) {
        for (int i = 0; i < tilePage.mRecords.size(); i++) {
            QSTile qSTile = tilePage.mRecords.get(i).tile;
            this.mUiEventLogger.logWithInstanceId(QSEvent.QS_TILE_VISIBLE, 0, qSTile.getMetricsSpec(), qSTile.getInstanceId());
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            updateListening();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateListening() {
        Iterator<TilePage> it = this.mPages.iterator();
        while (it.hasNext()) {
            TilePage next = it.next();
            next.setListening(next.getParent() == null ? false : this.mListening);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void fakeDragBy(float f) {
        try {
            super.fakeDragBy(f);
            postInvalidateOnAnimation();
        } catch (NullPointerException e) {
            Log.e("PagedTileLayout", "FakeDragBy called before begin", e);
            post(new Runnable(this.mPages.size() - 1) {
                /* class com.android.systemui.qs.$$Lambda$PagedTileLayout$EgpYNrlBIoNqA0Bh2xqmY_dv38 */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PagedTileLayout.this.lambda$fakeDragBy$1$PagedTileLayout(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$fakeDragBy$1 */
    public /* synthetic */ void lambda$fakeDragBy$1$PagedTileLayout(int i) {
        setCurrentItem(i, true);
        AnimatorSet animatorSet = this.mBounceAnimatorSet;
        if (animatorSet != null) {
            animatorSet.start();
        }
        setOffscreenPageLimit(1);
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void endFakeDrag() {
        try {
            super.endFakeDrag();
        } catch (NullPointerException e) {
            Log.e("PagedTileLayout", "endFakeDrag called before begin", e);
        }
    }

    @Override // androidx.viewpager.widget.ViewPager
    public void computeScroll() {
        if (!this.mScroller.isFinished() && this.mScroller.computeScrollOffset()) {
            if (!isFakeDragging()) {
                beginFakeDrag();
            }
            fakeDragBy((float) (getScrollX() - this.mScroller.getCurrX()));
        } else if (isFakeDragging()) {
            endFakeDrag();
            AnimatorSet animatorSet = this.mBounceAnimatorSet;
            if (animatorSet != null) {
                animatorSet.start();
            }
            setOffscreenPageLimit(1);
        }
        super.computeScroll();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPages.add(createTilePage());
        this.mAdapter.notifyDataSetChanged();
    }

    private TilePage createTilePage() {
        TilePage tilePage = (TilePage) LayoutInflater.from(getContext()).inflate(C0017R$layout.qs_paged_page, (ViewGroup) this, false);
        tilePage.setMinRows(this.mMinRows);
        tilePage.setMaxColumns(this.mMaxColumns);
        return tilePage;
    }

    public void setPageIndicator(MiuiPageIndicator miuiPageIndicator) {
        this.mPageIndicator = miuiPageIndicator;
        miuiPageIndicator.setNumPages(this.mPages.size());
        this.mPageIndicator.setLocation(this.mPageIndicatorPosition);
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        ViewGroup viewGroup = (ViewGroup) tileRecord.tileView.getParent();
        if (viewGroup == null) {
            return 0;
        }
        return viewGroup.getTop() + getTop();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mTiles.add(tileRecord);
        this.mDistributeTiles = true;
        requestLayout();
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void removeTile(QSPanel.TileRecord tileRecord) {
        if (this.mTiles.remove(tileRecord)) {
            this.mDistributeTiles = true;
            requestLayout();
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public void setExpansion(float f) {
        this.mLastExpansion = f;
        updateSelected();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSelected() {
        float f = this.mLastExpansion;
        if (f <= 0.0f || f >= 1.0f) {
            boolean z = this.mLastExpansion == 1.0f;
            setImportantForAccessibility(4);
            int currentPageNumber = getCurrentPageNumber();
            int i = 0;
            while (i < this.mPages.size()) {
                TilePage tilePage = this.mPages.get(i);
                tilePage.setSelected(i == currentPageNumber ? z : false);
                if (tilePage.isSelected()) {
                    logVisibleTiles(tilePage);
                }
                i++;
            }
            setImportantForAccessibility(0);
        }
    }

    public void setPageListener(PageListener pageListener) {
        this.mPageListener = pageListener;
    }

    private void distributeTiles() {
        emptyAndInflateOrRemovePages();
        int maxTiles = this.mPages.get(0).maxTiles();
        int size = this.mTiles.size();
        int i = 0;
        for (int i2 = 0; i2 < size; i2++) {
            QSPanel.TileRecord tileRecord = this.mTiles.get(i2);
            if (this.mPages.get(i).mRecords.size() == maxTiles) {
                i++;
            }
            this.mPages.get(i).addTile(tileRecord);
        }
    }

    private void emptyAndInflateOrRemovePages() {
        int numPages = getNumPages();
        int size = this.mPages.size();
        for (int i = 0; i < size; i++) {
            this.mPages.get(i).removeAllViews();
        }
        if (size != numPages) {
            while (this.mPages.size() < numPages) {
                this.mPages.add(createTilePage());
            }
            while (this.mPages.size() > numPages) {
                ArrayList<TilePage> arrayList = this.mPages;
                arrayList.remove(arrayList.size() - 1);
            }
            this.mPageIndicator.setNumPages(this.mPages.size());
            setAdapter(this.mAdapter);
            this.mAdapter.notifyDataSetChanged();
            int i2 = this.mPageToRestore;
            if (i2 != -1) {
                setCurrentItem(i2, false);
                this.mPageToRestore = -1;
            }
        }
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean updateResources() {
        getContext().getResources().getDimensionPixelSize(C0012R$dimen.notification_side_paddings);
        setPadding(0, 0, 0, getContext().getResources().getDimensionPixelSize(C0012R$dimen.qs_paged_tile_layout_padding_bottom));
        boolean z = false;
        for (int i = 0; i < this.mPages.size(); i++) {
            z |= this.mPages.get(i).updateResources();
        }
        if (z) {
            this.mDistributeTiles = true;
            requestLayout();
        }
        return z;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean setMinRows(int i) {
        this.mMinRows = i;
        boolean z = false;
        for (int i2 = 0; i2 < this.mPages.size(); i2++) {
            if (this.mPages.get(i2).setMinRows(i)) {
                this.mDistributeTiles = true;
                z = true;
            }
        }
        return z;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public boolean setMaxColumns(int i) {
        this.mMaxColumns = i;
        boolean z = false;
        for (int i2 = 0; i2 < this.mPages.size(); i2++) {
            if (this.mPages.get(i2).setMaxColumns(i)) {
                this.mDistributeTiles = true;
                z = true;
            }
        }
        return z;
    }

    public void setExcessHeight(int i) {
        this.mExcessHeight = i;
    }

    /* access modifiers changed from: protected */
    @Override // androidx.viewpager.widget.ViewPager
    public void onMeasure(int i, int i2) {
        int size = this.mTiles.size();
        if (!(!this.mDistributeTiles && this.mLastMaxHeight == View.MeasureSpec.getSize(i2) && this.mLastExcessHeight == this.mExcessHeight)) {
            int size2 = View.MeasureSpec.getSize(i2);
            this.mLastMaxHeight = size2;
            int i3 = this.mExcessHeight;
            this.mLastExcessHeight = i3;
            if (this.mPages.get(0).updateMaxRows(size2 - i3, size) || this.mDistributeTiles) {
                this.mDistributeTiles = false;
                distributeTiles();
            }
            int i4 = this.mPages.get(0).mRows;
            for (int i5 = 0; i5 < this.mPages.size(); i5++) {
                this.mPages.get(i5).mRows = i4;
            }
        }
        super.onMeasure(i, i2);
        int childCount = getChildCount();
        int i6 = 0;
        for (int i7 = 0; i7 < childCount; i7++) {
            int measuredHeight = getChildAt(i7).getMeasuredHeight();
            if (measuredHeight > i6) {
                i6 = measuredHeight;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), i6 + getPaddingBottom());
    }

    public int getColumnCount() {
        if (this.mPages.size() == 0) {
            return 0;
        }
        return this.mPages.get(0).mColumns;
    }

    public int getNumPages() {
        int size = this.mTiles.size();
        int max = Math.max(size / this.mPages.get(0).maxTiles(), 1);
        return size > this.mPages.get(0).maxTiles() * max ? max + 1 : max;
    }

    @Override // com.android.systemui.qs.QSPanel.QSTileLayout
    public int getNumVisibleTiles() {
        if (this.mPages.size() == 0) {
            return 0;
        }
        return this.mPages.get(getCurrentPageNumber()).mRecords.size();
    }

    public void startTileReveal(Set<String> set, final Runnable runnable) {
        if (!set.isEmpty() && this.mPages.size() >= 2 && getScrollX() == 0 && beginFakeDrag()) {
            int size = this.mPages.size() - 1;
            ArrayList arrayList = new ArrayList();
            Iterator<QSPanel.TileRecord> it = this.mPages.get(size).mRecords.iterator();
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                if (set.contains(next.tile.getTileSpec())) {
                    arrayList.add(setupBounceAnimator(next.tileView, arrayList.size()));
                }
            }
            if (arrayList.isEmpty()) {
                endFakeDrag();
                return;
            }
            AnimatorSet animatorSet = new AnimatorSet();
            this.mBounceAnimatorSet = animatorSet;
            animatorSet.playTogether(arrayList);
            this.mBounceAnimatorSet.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.qs.PagedTileLayout.AnonymousClass1 */

                public void onAnimationEnd(Animator animator) {
                    PagedTileLayout.this.mBounceAnimatorSet = null;
                    runnable.run();
                }
            });
            setOffscreenPageLimit(size);
            int width = getWidth() * size;
            Scroller scroller = this.mScroller;
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            if (isLayoutRtl()) {
                width = -width;
            }
            scroller.startScroll(scrollX, scrollY, width, 0, 750);
            postInvalidateOnAnimation();
        }
    }

    private static Animator setupBounceAnimator(View view, int i) {
        view.setAlpha(0.0f);
        view.setScaleX(0.0f);
        view.setScaleY(0.0f);
        ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f), PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f), PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f));
        ofPropertyValuesHolder.setDuration(450L);
        ofPropertyValuesHolder.setStartDelay((long) (i * 85));
        ofPropertyValuesHolder.setInterpolator(new OvershootInterpolator(1.3f));
        return ofPropertyValuesHolder;
    }

    public static class TilePage extends TileLayout {
        public TilePage(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public int maxTiles() {
            return Math.max(this.mColumns * this.mRows, 1);
        }
    }
}
