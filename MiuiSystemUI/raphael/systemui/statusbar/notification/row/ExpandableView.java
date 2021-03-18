package com.android.systemui.statusbar.notification.row;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ExpandableView extends FrameLayout implements Dumpable {
    private static Rect mClipRect = new Rect();
    private int mActualHeight;
    private boolean mChangingPosition = false;
    protected int mClipBottomAmount;
    private boolean mClipToActualHeight = true;
    protected int mClipTopAmount;
    protected int mContentShift;
    protected float mContentTransformationAmount;
    private float mContentTranslation;
    protected float mExtraWidthForClipping = 0.0f;
    protected boolean mFirstInSection;
    private boolean mInShelf;
    protected boolean mIsLastChild;
    protected boolean mLastInSection;
    private ArrayList<View> mMatchParentViews = new ArrayList<>();
    protected int mMinimumHeightForClipping = 0;
    protected OnHeightChangedListener mOnHeightChangedListener;
    private boolean mTransformingInShelf;
    private ViewGroup mTransientContainer;
    private final ExpandableViewState mViewState = createExpandableViewState();
    private boolean mWillBeGone;

    public interface OnHeightChangedListener {
        void onHeightChanged(ExpandableView expandableView, boolean z);

        void onReset(ExpandableView expandableView);
    }

    /* access modifiers changed from: protected */
    public void applyContentTransformation(float f, float f2) {
    }

    public boolean areChildrenExpanded() {
        return false;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public int getExtraBottomPadding() {
        return 0;
    }

    public float getIncreasedPaddingAmount() {
        return 0.0f;
    }

    public float getOutlineAlpha() {
        return 0.0f;
    }

    public int getOutlineTranslation() {
        return 0;
    }

    public StatusBarIconView getShelfIcon() {
        return null;
    }

    public View getShelfTransformationTarget() {
        return null;
    }

    public boolean hasExpandingChild() {
        return false;
    }

    public boolean hasNoContentHeight() {
        return false;
    }

    public boolean isAboveShelf() {
        return false;
    }

    public boolean isChildInGroup() {
        return false;
    }

    public boolean isContentExpandable() {
        return false;
    }

    public boolean isExpandAnimationRunning() {
        return false;
    }

    public boolean isGroupExpanded() {
        return false;
    }

    public boolean isGroupExpansionChanging() {
        return false;
    }

    public boolean isHeadsUpAnimatingAway() {
        return false;
    }

    public boolean isPinned() {
        return false;
    }

    public boolean isRemoved() {
        return false;
    }

    public boolean isSummaryWithChildren() {
        return false;
    }

    public boolean isTransparent() {
        return false;
    }

    public boolean mustStayOnScreen() {
        return false;
    }

    public boolean needsClippingToShelf() {
        return true;
    }

    public abstract void performAddAnimation(long j, long j2, boolean z);

    public abstract long performRemoveAnimation(long j, long j2, float f, boolean z, float f2, Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter);

    public void setActualHeightAnimating(boolean z) {
    }

    public void setBelowSpeedBump(boolean z) {
    }

    public boolean setBottomRoundness(float f, boolean z) {
        return false;
    }

    public void setDimmed(boolean z, boolean z2) {
    }

    public void setDistanceToTopRoundness(float f) {
    }

    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
    }

    public void setHeadsUpIsVisible() {
    }

    public void setHideSensitive(boolean z, boolean z2, long j, long j2) {
    }

    public void setHideSensitiveForIntrinsicHeight(boolean z) {
    }

    public void setMinClipTopAmount(int i) {
    }

    public boolean setTopRoundness(float f, boolean z) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean shouldClipToActualHeight() {
        return true;
    }

    public boolean showingPulsing() {
        return false;
    }

    public ExpandableView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDimens();
    }

    private void initDimens() {
        this.mContentShift = getResources().getDimensionPixelSize(C0012R$dimen.shelf_transform_content_shift);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i2);
        int paddingStart = getPaddingStart() + getPaddingEnd();
        int mode = View.MeasureSpec.getMode(i2);
        int i3 = Integer.MAX_VALUE;
        if (!(mode == 0 || size == 0)) {
            i3 = Math.min(size, Integer.MAX_VALUE);
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE);
        int childCount = getChildCount();
        int i4 = 0;
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            if (childAt.getVisibility() != 8) {
                ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                int i6 = layoutParams.height;
                if (i6 != -1) {
                    childAt.measure(FrameLayout.getChildMeasureSpec(i, paddingStart, layoutParams.width), i6 >= 0 ? View.MeasureSpec.makeMeasureSpec(Math.min(i6, i3), 1073741824) : makeMeasureSpec);
                    i4 = Math.max(i4, childAt.getMeasuredHeight());
                } else {
                    this.mMatchParentViews.add(childAt);
                }
            }
        }
        if (mode != 1073741824) {
            size = Math.min(i3, i4);
        }
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(size, 1073741824);
        Iterator<View> it = this.mMatchParentViews.iterator();
        while (it.hasNext()) {
            View next = it.next();
            next.measure(FrameLayout.getChildMeasureSpec(i, paddingStart, next.getLayoutParams().width), makeMeasureSpec2);
        }
        this.mMatchParentViews.clear();
        setMeasuredDimension(View.MeasureSpec.getSize(i), size);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateClipping();
    }

    public boolean pointInView(float f, float f2, float f3) {
        return f >= (-f3) && f2 >= ((float) this.mClipTopAmount) - f3 && f < ((float) (((FrameLayout) this).mRight - ((FrameLayout) this).mLeft)) + f3 && f2 < ((float) this.mActualHeight) + f3;
    }

    public void setActualHeight(int i, boolean z) {
        this.mActualHeight = i;
        updateClipping();
        if (z) {
            notifyHeightChanged(false);
        }
    }

    public void setActualHeight(int i) {
        setActualHeight(i, true);
    }

    public int getActualHeight() {
        return this.mActualHeight;
    }

    public int getMaxContentHeight() {
        return getHeight();
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean z) {
        return getHeight();
    }

    public int getCollapsedHeight() {
        return getHeight();
    }

    public int getIntrinsicHeight() {
        return getHeight();
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateClipping();
    }

    public int getClipTopAmount() {
        return this.mClipTopAmount;
    }

    public int getClipBottomAmount() {
        return this.mClipBottomAmount;
    }

    public void setOnHeightChangedListener(OnHeightChangedListener onHeightChangedListener) {
        this.mOnHeightChangedListener = onHeightChangedListener;
    }

    public void notifyHeightChanged(boolean z) {
        OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onHeightChanged(this, z);
        }
    }

    public int getPinnedHeadsUpHeight() {
        return getIntrinsicHeight();
    }

    public void setTranslation(float f) {
        setTranslationX(f);
    }

    public float getTranslation() {
        return getTranslationX();
    }

    public void onHeightReset() {
        OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onReset(this);
        }
    }

    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        rect.left = (int) (((float) rect.left) + getTranslationX());
        rect.right = (int) (((float) rect.right) + getTranslationX());
        rect.bottom = (int) (((float) rect.top) + getTranslationY() + ((float) getActualHeight()));
        rect.top = (int) (((float) rect.top) + getTranslationY() + ((float) getClipTopAmount()));
    }

    public void getBoundsOnScreen(Rect rect, boolean z) {
        super.getBoundsOnScreen(rect, z);
        if (((float) getTop()) + getTranslationY() < 0.0f) {
            rect.top = (int) (((float) rect.top) + ((float) getTop()) + getTranslationY());
        }
        rect.bottom = rect.top + getActualHeight();
        rect.top += getClipTopAmount();
    }

    /* access modifiers changed from: protected */
    public void updateClipping() {
        if (!this.mClipToActualHeight || !shouldClipToActualHeight()) {
            setClipBounds(null);
            return;
        }
        int clipTopAmount = getClipTopAmount();
        int max = Math.max(Math.max((getActualHeight() + getExtraBottomPadding()) - this.mClipBottomAmount, clipTopAmount), this.mMinimumHeightForClipping);
        int i = (int) (this.mExtraWidthForClipping / 2.0f);
        mClipRect.set(-i, clipTopAmount, getWidth() + i, max);
        setClipBounds(mClipRect);
    }

    public void setMinimumHeightForClipping(int i) {
        this.mMinimumHeightForClipping = i;
        updateClipping();
    }

    public void setExtraWidthForClipping(float f) {
        this.mExtraWidthForClipping = f;
        updateClipping();
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        updateClipping();
    }

    public boolean willBeGone() {
        return this.mWillBeGone;
    }

    public void setWillBeGone(boolean z) {
        this.mWillBeGone = z;
    }

    public void setLayerType(int i, Paint paint) {
        if (hasOverlappingRendering()) {
            super.setLayerType(i, paint);
        }
    }

    public boolean hasOverlappingRendering() {
        return super.hasOverlappingRendering() && getActualHeight() <= getHeight();
    }

    public void setChangingPosition(boolean z) {
        this.mChangingPosition = z;
    }

    public boolean isChangingPosition() {
        return this.mChangingPosition;
    }

    public void setTransientContainer(ViewGroup viewGroup) {
        this.mTransientContainer = viewGroup;
    }

    public ViewGroup getTransientContainer() {
        return this.mTransientContainer;
    }

    /* access modifiers changed from: protected */
    public ExpandableViewState createExpandableViewState() {
        return new ExpandableViewState();
    }

    public ExpandableViewState resetViewState() {
        this.mViewState.height = getIntrinsicHeight();
        this.mViewState.gone = getVisibility() == 8;
        ExpandableViewState expandableViewState = this.mViewState;
        expandableViewState.alpha = 1.0f;
        expandableViewState.notGoneIndex = -1;
        expandableViewState.xTranslation = getTranslationX();
        ExpandableViewState expandableViewState2 = this.mViewState;
        expandableViewState2.hidden = false;
        expandableViewState2.scaleX = getScaleX();
        this.mViewState.scaleY = getScaleY();
        ExpandableViewState expandableViewState3 = this.mViewState;
        expandableViewState3.inShelf = false;
        expandableViewState3.headsUpIsVisible = false;
        if (this instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) this;
            List<ExpandableNotificationRow> attachedChildren = expandableNotificationRow.getAttachedChildren();
            if (expandableNotificationRow.isSummaryWithChildren() && attachedChildren != null) {
                for (ExpandableNotificationRow expandableNotificationRow2 : attachedChildren) {
                    expandableNotificationRow2.resetViewState();
                }
            }
        }
        return this.mViewState;
    }

    public ExpandableViewState getViewState() {
        return this.mViewState;
    }

    public void applyViewState() {
        ExpandableViewState expandableViewState = this.mViewState;
        if (!expandableViewState.gone) {
            expandableViewState.applyToView(this);
        }
    }

    public void setInShelf(boolean z) {
        this.mInShelf = z;
    }

    public boolean isInShelf() {
        return this.mInShelf;
    }

    public int getRelativeTopPadding(View view) {
        int i = 0;
        while (view.getParent() instanceof ViewGroup) {
            i += view.getTop();
            view = (View) view.getParent();
            if (view == this) {
                break;
            }
        }
        return i;
    }

    public int getRelativeStartPadding(View view) {
        boolean isLayoutRtl = isLayoutRtl();
        int i = 0;
        while (view.getParent() instanceof ViewGroup) {
            View view2 = (View) view.getParent();
            i += isLayoutRtl ? view2.getWidth() - view.getRight() : view.getLeft();
            if (view2 == this) {
                return i;
            }
            view = view2;
        }
        return i;
    }

    public void setContentTransformationAmount(float f, boolean z) {
        boolean z2 = true;
        boolean z3 = z != this.mIsLastChild;
        if (this.mContentTransformationAmount == f) {
            z2 = false;
        }
        boolean z4 = z3 | z2;
        this.mIsLastChild = z;
        this.mContentTransformationAmount = f;
        if (z4) {
            updateContentTransformation();
        }
    }

    /* access modifiers changed from: protected */
    public void updateContentTransformation() {
        float contentTransformationShift = (-this.mContentTransformationAmount) * getContentTransformationShift();
        float interpolation = Interpolators.ALPHA_OUT.getInterpolation(Math.min((1.0f - this.mContentTransformationAmount) / 0.5f, 1.0f));
        if (this.mIsLastChild) {
            contentTransformationShift *= 0.4f;
        }
        this.mContentTranslation = contentTransformationShift;
        applyContentTransformation(interpolation, contentTransformationShift);
    }

    /* access modifiers changed from: protected */
    public float getContentTransformationShift() {
        return (float) this.mContentShift;
    }

    public void setTransformingInShelf(boolean z) {
        this.mTransformingInShelf = z;
    }

    public boolean isTransformingIntoShelf() {
        return this.mTransformingInShelf;
    }

    public float getContentTranslation() {
        return this.mContentTranslation;
    }

    public void setFirstInSection(boolean z) {
        this.mFirstInSection = z;
    }

    public void setLastInSection(boolean z) {
        this.mLastInSection = z;
    }

    public boolean isLastInSection() {
        return this.mLastInSection;
    }

    public boolean isFirstInSection() {
        return this.mFirstInSection;
    }

    public int getHeadsUpHeightWithoutHeader() {
        return getHeight();
    }
}
