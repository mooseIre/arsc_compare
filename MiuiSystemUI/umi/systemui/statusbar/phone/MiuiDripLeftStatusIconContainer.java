package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import java.util.ArrayList;

public class MiuiDripLeftStatusIconContainer extends AlphaOptimizedLinearLayout {
    private static final AnimationProperties ADD_ICON_PROPERTIES;
    private static final AnimationProperties ANIMATE_ALL_PROPERTIES;
    private static final AnimationProperties X_ANIMATION_PROPERTIES;
    private int mIconSpacing;
    private ArrayList<String> mIgnoredSlots;
    private ArrayList<StatusIconState> mLayoutStates;
    private ArrayList<View> mMeasureViews;
    private boolean mNeedsUnderflow;
    private boolean mShouldRestrictIcons;
    private int mUnderflowWidth;

    public MiuiDripLeftStatusIconContainer(Context context) {
        this(context, null);
    }

    public MiuiDripLeftStatusIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNeedsUnderflow = true;
        this.mShouldRestrictIcons = false;
        this.mLayoutStates = new ArrayList<>();
        this.mMeasureViews = new ArrayList<>();
        this.mIgnoredSlots = new ArrayList<>();
        initDimens();
        setWillNotDraw(true);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    private void initDimens() {
        getResources().getDimensionPixelSize(17105492);
        getResources().getDimensionPixelSize(C0012R$dimen.overflow_icon_dot_padding);
        this.mIconSpacing = getResources().getDimensionPixelSize(C0012R$dimen.status_bar_system_icon_spacing);
        getResources().getDimensionPixelSize(C0012R$dimen.overflow_dot_radius);
        this.mUnderflowWidth = 0;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = ((float) getHeight()) / 2.0f;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (((float) measuredHeight) / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
        }
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        this.mMeasureViews.clear();
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) getChildAt(i3);
            if (statusIconDisplayable.isIconVisible() && !statusIconDisplayable.isIconBlocked() && !this.mIgnoredSlots.contains(statusIconDisplayable.getSlot())) {
                this.mMeasureViews.add((View) statusIconDisplayable);
            }
        }
        int size2 = this.mMeasureViews.size();
        int i4 = ((LinearLayout) this).mPaddingLeft + ((LinearLayout) this).mPaddingRight;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(size, 0);
        if (!(mode == 1073741824 || mode == Integer.MIN_VALUE)) {
            size = 1073741823;
        }
        boolean z = true;
        for (int i5 = 0; i5 < size2; i5++) {
            View view = this.mMeasureViews.get(i5);
            measureChild(view, makeMeasureSpec, i2);
            int i6 = i5 == size2 - 1 ? 0 : this.mIconSpacing;
            if (!z || getViewTotalMeasuredWidth(view) + i4 + i6 > size) {
                z = false;
            } else {
                i4 += getViewTotalMeasuredWidth(view) + i6;
            }
        }
        setMeasuredDimension(i4, View.MeasureSpec.getSize(i2));
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        StatusIconState statusIconState = new StatusIconState();
        statusIconState.justAdded = true;
        view.setTag(C0015R$id.status_bar_view_state_tag, statusIconState);
    }

    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        view.setTag(C0015R$id.status_bar_view_state_tag, null);
    }

    private void calculateIconTranslations() {
        this.mLayoutStates.clear();
        float width = (float) getWidth();
        float paddingStart = (float) getPaddingStart();
        float paddingEnd = width - ((float) getPaddingEnd());
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) childAt;
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (!statusIconDisplayable.isIconVisible() || statusIconDisplayable.isIconBlocked() || this.mIgnoredSlots.contains(statusIconDisplayable.getSlot())) {
                viewStateFromChild.visibleState = 2;
            } else {
                viewStateFromChild.xTranslation = paddingStart;
                viewStateFromChild.visibleState = 0;
                this.mLayoutStates.add(viewStateFromChild);
                paddingStart = paddingStart + ((float) getViewTotalWidth(childAt)) + ((float) this.mIconSpacing);
            }
        }
        int size = this.mLayoutStates.size();
        int i2 = 7;
        if (size > 7) {
            i2 = 6;
        }
        int i3 = 0;
        int i4 = 0;
        while (true) {
            if (i3 >= size) {
                i3 = -1;
                break;
            }
            StatusIconState statusIconState = this.mLayoutStates.get(i3);
            if ((this.mNeedsUnderflow && statusIconState.xTranslation >= ((float) this.mUnderflowWidth) + paddingEnd) || (this.mShouldRestrictIcons && i4 >= i2)) {
                break;
            }
            Math.min(paddingEnd, statusIconState.xTranslation + ((float) this.mUnderflowWidth) + ((float) this.mIconSpacing));
            i4++;
            i3++;
        }
        if (i3 != -1) {
            while (i3 < size) {
                this.mLayoutStates.get(i3).visibleState = 2;
                i3++;
            }
        }
        if (isLayoutRtl()) {
            for (int i5 = 0; i5 < childCount; i5++) {
                View childAt2 = getChildAt(i5);
                StatusIconState viewStateFromChild2 = getViewStateFromChild(childAt2);
                viewStateFromChild2.xTranslation = (width - viewStateFromChild2.xTranslation) - ((float) childAt2.getWidth());
            }
        }
    }

    private void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (viewStateFromChild != null) {
                viewStateFromChild.applyToView(childAt);
            }
        }
    }

    private void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            StatusIconState viewStateFromChild = getViewStateFromChild(childAt);
            if (viewStateFromChild != null) {
                viewStateFromChild.initFrom(childAt);
                viewStateFromChild.alpha = 1.0f;
                viewStateFromChild.hidden = false;
            }
        }
    }

    private static StatusIconState getViewStateFromChild(View view) {
        return (StatusIconState) view.getTag(C0015R$id.status_bar_view_state_tag);
    }

    private static int getViewTotalMeasuredWidth(View view) {
        return view.getMeasuredWidth() + view.getPaddingStart() + view.getPaddingEnd();
    }

    private static int getViewTotalWidth(View view) {
        return view.getWidth() + view.getPaddingStart() + view.getPaddingEnd();
    }

    public static class StatusIconState extends ViewState {
        float distanceToViewEnd = -1.0f;
        public boolean justAdded = true;
        public int visibleState = 0;

        @Override // com.android.systemui.statusbar.notification.stack.ViewState
        public void applyToView(View view) {
            float width = (view.getParent() instanceof View ? (float) ((View) view.getParent()).getWidth() : 0.0f) - this.xTranslation;
            if (view instanceof StatusIconDisplayable) {
                StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) view;
                AnimationProperties animationProperties = null;
                boolean z = true;
                if (this.justAdded || (statusIconDisplayable.getVisibleState() == 2 && this.visibleState == 0)) {
                    super.applyToView(view);
                    view.setAlpha(0.0f);
                    statusIconDisplayable.setVisibleState(2);
                    animationProperties = MiuiDripLeftStatusIconContainer.ADD_ICON_PROPERTIES;
                } else {
                    int visibleState2 = statusIconDisplayable.getVisibleState();
                    int i = this.visibleState;
                    if (visibleState2 != i) {
                        if (statusIconDisplayable.getVisibleState() == 0 && this.visibleState == 2) {
                            z = false;
                        } else {
                            animationProperties = MiuiDripLeftStatusIconContainer.ANIMATE_ALL_PROPERTIES;
                        }
                    } else if (!(i == 2 || this.distanceToViewEnd == width)) {
                        animationProperties = MiuiDripLeftStatusIconContainer.X_ANIMATION_PROPERTIES;
                    }
                }
                statusIconDisplayable.setVisibleState(this.visibleState, z);
                if (animationProperties != null) {
                    animateTo(view, animationProperties);
                } else {
                    super.applyToView(view);
                }
                this.justAdded = false;
                this.distanceToViewEnd = width;
            }
        }
    }

    static {
        AnonymousClass1 r0 = new AnimationProperties() {
            /* class com.android.systemui.statusbar.phone.MiuiDripLeftStatusIconContainer.AnonymousClass1 */
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateAlpha();
                this.mAnimationFilter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r0.setDuration(200);
        r0.setDelay(50);
        ADD_ICON_PROPERTIES = r0;
        AnonymousClass2 r02 = new AnimationProperties() {
            /* class com.android.systemui.statusbar.phone.MiuiDripLeftStatusIconContainer.AnonymousClass2 */
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                this.mAnimationFilter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r02.setDuration(200);
        X_ANIMATION_PROPERTIES = r02;
        AnonymousClass3 r03 = new AnimationProperties() {
            /* class com.android.systemui.statusbar.phone.MiuiDripLeftStatusIconContainer.AnonymousClass3 */
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                animationFilter.animateScale();
                this.mAnimationFilter = animationFilter;
            }

            @Override // com.android.systemui.statusbar.notification.stack.AnimationProperties
            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r03.setDuration(200);
        ANIMATE_ALL_PROPERTIES = r03;
    }
}
