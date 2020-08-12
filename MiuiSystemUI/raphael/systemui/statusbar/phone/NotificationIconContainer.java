package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Icon;
import android.graphics.drawable.IconCompat;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.AlphaOptimizedFrameLayout;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.stack.AnimationFilter;
import com.android.systemui.statusbar.stack.AnimationProperties;
import com.android.systemui.statusbar.stack.ViewState;
import java.util.ArrayList;
import java.util.HashMap;

public class NotificationIconContainer extends AlphaOptimizedFrameLayout {
    /* access modifiers changed from: private */
    public static final AnimationProperties ADD_ICON_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties DOT_ANIMATION_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties ICON_ANIMATION_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties UNDARK_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties mTempProperties = new AnimationProperties() {
        private AnimationFilter mAnimationFilter = new AnimationFilter();

        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    };
    private int mActualLayoutWidth = Integer.MIN_VALUE;
    private float mActualPaddingEnd = -2.14748365E9f;
    private float mActualPaddingStart = -2.14748365E9f;
    /* access modifiers changed from: private */
    public int mAddAnimationStartIndex = -1;
    /* access modifiers changed from: private */
    public boolean mAnimationsEnabled = true;
    /* access modifiers changed from: private */
    public int mCannedAnimationStartIndex = -1;
    private boolean mChangingViewPositions;
    private boolean mDark;
    /* access modifiers changed from: private */
    public boolean mDisallowNextAnimation;
    protected int mDotPadding;
    private int mIconSize;
    private final HashMap<View, IconState> mIconStates = new HashMap<>();
    protected int mMaxDotNum;
    private float mOpenedAmount = 0.0f;
    private Paint mPaint = new Paint();
    private ArrayMap<String, ArrayList<StatusBarIcon>> mReplacingIcons;
    private boolean mShowAllIcons = true;
    private int mSpeedBumpIndex = -1;
    protected int mStaticDotRadius;
    private float mVisualOverflowAdaption;

    static {
        AnonymousClass1 r0 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r0.setDuration(200);
        DOT_ANIMATION_PROPERTIES = r0;
        AnonymousClass2 r02 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                animationFilter.animateScale();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r02.setDuration(100);
        r02.setCustomInterpolator(View.TRANSLATION_Y, Interpolators.ICON_OVERSHOT);
        ICON_ANIMATION_PROPERTIES = r02;
        AnonymousClass4 r03 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateAlpha();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r03.setDuration(200);
        r03.setDelay(50);
        ADD_ICON_PROPERTIES = r03;
        AnonymousClass5 r04 = new AnimationProperties() {
            private AnimationFilter mAnimationFilter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateX();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r04.setDuration(200);
        UNDARK_PROPERTIES = r04;
    }

    public NotificationIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMaxDotNum = context.getResources().getInteger(R.integer.notification_icons_dots_num_max);
        initDimens();
        setWillNotDraw(true);
    }

    private void initDimens() {
        this.mDotPadding = getResources().getDimensionPixelSize(R.dimen.overflow_icon_dot_padding);
        this.mStaticDotRadius = getResources().getDimensionPixelSize(R.dimen.overflow_dot_radius);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = this.mPaint;
        paint.setColor(-65536);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(getActualPaddingStart(), 0.0f, getLayoutEnd(), (float) getHeight(), paint);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        LinearLayout.LayoutParams layoutParams;
        super.onFinishInflate();
        if (this.mContext.getResources().getBoolean(R.bool.notification_icon_container_layout_gravity_center) && (layoutParams = (LinearLayout.LayoutParams) getLayoutParams()) != null) {
            layoutParams.gravity = 17;
            setLayoutParams(layoutParams);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float height = ((float) getHeight()) / 2.0f;
        this.mIconSize = 0;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            int measuredWidth = childAt.getMeasuredWidth();
            int measuredHeight = childAt.getMeasuredHeight();
            int i6 = (int) (height - (((float) measuredHeight) / 2.0f));
            childAt.layout(0, i6, measuredWidth, measuredHeight + i6);
            if (i5 == 0) {
                this.mIconSize = childAt.getWidth();
            }
        }
        if (this.mShowAllIcons) {
            resetViewStates();
            calculateIconTranslations();
            applyIconStates();
        }
    }

    public void applyIconStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            ViewState viewState = this.mIconStates.get(childAt);
            if (viewState != null) {
                viewState.applyToView(childAt);
            }
        }
        this.mAddAnimationStartIndex = -1;
        this.mCannedAnimationStartIndex = -1;
        this.mDisallowNextAnimation = false;
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        boolean isReplacingIcon = isReplacingIcon(view);
        if (!this.mChangingViewPositions) {
            IconState iconState = new IconState();
            if (isReplacingIcon) {
                iconState.justAdded = false;
                boolean unused = iconState.justReplaced = true;
            }
            this.mIconStates.put(view, iconState);
        }
        int indexOfChild = indexOfChild(view);
        if (indexOfChild < getChildCount() - 1 && !isReplacingIcon && this.mIconStates.get(getChildAt(indexOfChild + 1)).iconAppearAmount > 0.0f) {
            int i = this.mAddAnimationStartIndex;
            if (i < 0) {
                this.mAddAnimationStartIndex = indexOfChild;
            } else {
                this.mAddAnimationStartIndex = Math.min(i, indexOfChild);
            }
        }
        boolean z = this.mDark;
        if (z && (view instanceof StatusBarIconView)) {
            ((StatusBarIconView) view).setDark(z, false, 0);
        }
    }

    private boolean isReplacingIcon(View view) {
        if (this.mReplacingIcons == null || !(view instanceof StatusBarIconView)) {
            return false;
        }
        StatusBarIconView statusBarIconView = (StatusBarIconView) view;
        Icon sourceIcon = statusBarIconView.getSourceIcon();
        ArrayList arrayList = this.mReplacingIcons.get(statusBarIconView.getNotification().getGroupKey());
        if (arrayList == null || !IconCompat.sameAs(sourceIcon, ((StatusBarIcon) arrayList.get(0)).icon)) {
            return false;
        }
        return true;
    }

    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (view instanceof StatusBarIconView) {
            boolean isReplacingIcon = isReplacingIcon(view);
            final StatusBarIconView statusBarIconView = (StatusBarIconView) view;
            if (statusBarIconView.getVisibleState() != 2 && view.getVisibility() == 0 && isReplacingIcon) {
                int findFirstViewIndexAfter = findFirstViewIndexAfter(statusBarIconView.getTranslationX());
                int i = this.mAddAnimationStartIndex;
                if (i < 0) {
                    this.mAddAnimationStartIndex = findFirstViewIndexAfter;
                } else {
                    this.mAddAnimationStartIndex = Math.min(i, findFirstViewIndexAfter);
                }
            }
            if (!this.mChangingViewPositions) {
                this.mIconStates.remove(view);
                if (!isReplacingIcon && view.isAttachedToWindow()) {
                    addTransientView(statusBarIconView, 0);
                    statusBarIconView.setVisibleState(2, true, new Runnable() {
                        public void run() {
                            NotificationIconContainer.this.removeTransientView(statusBarIconView);
                        }
                    });
                }
            }
        }
    }

    private int findFirstViewIndexAfter(float f) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i).getTranslationX() > f) {
                return i;
            }
        }
        return getChildCount();
    }

    public void resetViewStates() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            ViewState viewState = this.mIconStates.get(childAt);
            viewState.initFrom(childAt);
            viewState.alpha = 1.0f;
            viewState.hidden = false;
        }
    }

    public void calculateIconTranslations() {
        int i;
        int i2;
        int i3;
        int i4;
        float f;
        float actualPaddingStart = getActualPaddingStart();
        int childCount = getChildCount();
        int min = Math.min(childCount, 3);
        float layoutEnd = getLayoutEnd();
        float f2 = layoutEnd - (((float) this.mIconSize) * 2.0f);
        int i5 = this.mSpeedBumpIndex;
        int i6 = -1;
        boolean z = i5 != -1 && i5 < getChildCount();
        float f3 = 0.0f;
        float f4 = actualPaddingStart;
        int i7 = -1;
        float f5 = 0.0f;
        int i8 = 0;
        while (i8 < childCount) {
            View childAt = getChildAt(i8);
            IconState iconState = this.mIconStates.get(childAt);
            iconState.xTranslation = f4;
            int i9 = this.mSpeedBumpIndex;
            boolean z2 = (i9 != i6 && i8 >= i9 && iconState.iconAppearAmount > f3) || i8 >= min;
            boolean z3 = i8 == childCount + -1;
            if (!this.mDark || !(childAt instanceof StatusBarIconView)) {
                i4 = min;
                f = 1.0f;
            } else {
                f = ((StatusBarIconView) childAt).getIconScaleFullyDark();
                i4 = min;
            }
            if (this.mOpenedAmount != f3) {
                z3 = z3 && !z && !z2;
            }
            iconState.visibleState = 0;
            if (i7 == -1) {
                if (!z2) {
                    if (f4 < (z3 ? layoutEnd - ((float) this.mIconSize) : f2)) {
                    }
                }
                int i10 = (!z3 || z2) ? i8 : i8 - 1;
                int i11 = this.mStaticDotRadius;
                int i12 = this.mDotPadding;
                int i13 = this.mIconSize;
                int i14 = i10;
                float f6 = ((((((float) i13) * 1.0f) + f2) - ((float) (((i11 * 6) + (i12 * 2)) / 2))) - (((float) i13) * 0.5f)) + ((float) i11);
                float min2 = z2 ? Math.min(f4, f6 + ((float) (i11 * 2)) + ((float) i12)) : f6 + (((f4 - f2) / ((float) i13)) * ((float) ((i11 * 2) + i12)));
                if (!this.mShowAllIcons) {
                    min2 += this.mVisualOverflowAdaption * (1.0f - this.mOpenedAmount);
                }
                f5 = min2;
                i7 = i14;
            }
            f4 += iconState.iconAppearAmount * ((float) childAt.getWidth()) * f;
            i8++;
            min = i4;
            i6 = -1;
            f3 = 0.0f;
        }
        IconState iconState2 = this.mIconStates.get(getChildAt(0));
        if (i7 != -1) {
            IconState iconState3 = iconState2;
            f4 = f5;
            i3 = 0;
            i = 0;
            for (int i15 = i7; i15 < childCount; i15++) {
                IconState iconState4 = this.mIconStates.get(getChildAt(i15));
                int i16 = (this.mStaticDotRadius * 2) + this.mDotPadding;
                iconState4.xTranslation = f4;
                if (i < this.mMaxDotNum) {
                    if (i != 1 || iconState4.iconAppearAmount >= 0.8f) {
                        iconState4.visibleState = 1;
                        i++;
                    } else {
                        iconState4.visibleState = 0;
                        iconState3 = iconState4;
                    }
                    int i17 = this.mMaxDotNum;
                    if (i == i17) {
                        i16 *= i17;
                    }
                    f4 += ((float) i16) * iconState4.iconAppearAmount;
                } else {
                    iconState4.visibleState = 2;
                    i3++;
                }
            }
            i2 = 0;
            iconState2 = iconState3;
        } else {
            i2 = 0;
            i3 = 0;
            i = 0;
        }
        if (this.mDark && f4 < getLayoutEnd()) {
            float layoutEnd2 = (getLayoutEnd() - f4) / 2.0f;
            if (i7 != -1) {
                layoutEnd2 = (((getLayoutEnd() - f5) / 2.0f) + layoutEnd2) / 2.0f;
            }
            for (int i18 = i2; i18 < childCount; i18++) {
                this.mIconStates.get(getChildAt(i18)).xTranslation += layoutEnd2;
            }
        }
        if (isLayoutRtl()) {
            while (i2 < childCount) {
                View childAt2 = getChildAt(i2);
                IconState iconState5 = this.mIconStates.get(childAt2);
                iconState5.xTranslation = (((float) getWidth()) - iconState5.xTranslation) - ((float) childAt2.getWidth());
                i2++;
            }
        }
        if (!((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout()) {
            return;
        }
        if (i7 != 0 && childCount - i3 != 1) {
            return;
        }
        if (i7 == -1) {
            if (getWidth() < childCount * this.mIconSize && iconState2 != null) {
                iconState2.visibleState = 2;
                return;
            }
            return;
        }
        if (getWidth() < (this.mIconSize * i7) + (this.mStaticDotRadius * i * 2) + ((i - 1) * this.mDotPadding)) {
            updateIconStates(i7 + i);
        }
    }

    private void updateIconStates(int i) {
        for (int i2 = 0; i2 < i; i2++) {
            IconState iconState = this.mIconStates.get(getChildAt(i2));
            if (iconState != null) {
                iconState.visibleState = 2;
            }
        }
    }

    private float getLayoutEnd() {
        return ((float) getActualWidth()) - getActualPaddingEnd();
    }

    private float getActualPaddingEnd() {
        float f = this.mActualPaddingEnd;
        return f == -2.14748365E9f ? (float) getPaddingEnd() : f;
    }

    private float getActualPaddingStart() {
        float f = this.mActualPaddingStart;
        return f == -2.14748365E9f ? (float) getPaddingStart() : f;
    }

    public void setShowAllIcons(boolean z) {
        this.mShowAllIcons = z;
    }

    public void setActualLayoutWidth(int i) {
        this.mActualLayoutWidth = i;
    }

    public void setActualPaddingEnd(float f) {
        this.mActualPaddingEnd = f;
    }

    public void setActualPaddingStart(float f) {
        this.mActualPaddingStart = f;
    }

    public int getActualWidth() {
        int i = this.mActualLayoutWidth;
        return i == Integer.MIN_VALUE ? getWidth() : i;
    }

    public void setChangingViewPositions(boolean z) {
        this.mChangingViewPositions = z;
    }

    public void setDark(boolean z, boolean z2, long j) {
        this.mDark = z;
        this.mDisallowNextAnimation |= !z2;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof StatusBarIconView) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) childAt;
                statusBarIconView.setDark(z, z2, j);
                if (!z && z2) {
                    getIconState(statusBarIconView).justUndarkened = true;
                }
            }
        }
    }

    public IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mIconStates.get(statusBarIconView);
    }

    public void setSpeedBumpIndex(int i) {
        this.mSpeedBumpIndex = i;
    }

    public void setOpenedAmount(float f) {
        this.mOpenedAmount = f;
    }

    public float getVisualOverflowAdaption() {
        return this.mVisualOverflowAdaption;
    }

    public void setVisualOverflowAdaption(float f) {
        this.mVisualOverflowAdaption = f;
    }

    public boolean hasOverflow() {
        return ((((float) getChildCount()) + 0.0f) * ((float) this.mIconSize)) - ((((float) getWidth()) - getActualPaddingStart()) - getActualPaddingEnd()) > 0.0f;
    }

    public int getIconSize() {
        return this.mIconSize;
    }

    public void setAnimationsEnabled(boolean z) {
        if (!z && this.mAnimationsEnabled) {
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                ViewState viewState = this.mIconStates.get(childAt);
                if (viewState != null) {
                    viewState.cancelAnimations(childAt);
                    viewState.applyToView(childAt);
                }
            }
        }
        this.mAnimationsEnabled = z;
    }

    public void setReplacingIcons(ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap) {
        this.mReplacingIcons = arrayMap;
    }

    public class IconState extends ViewState {
        public float clampedAppearAmount = 1.0f;
        public float iconAppearAmount = 1.0f;
        public int iconColor = 0;
        public boolean justAdded = true;
        /* access modifiers changed from: private */
        public boolean justReplaced;
        public boolean justUndarkened;
        public boolean needsCannedAnimation;
        public boolean noAnimations;
        public boolean translateContent;
        public boolean useFullTransitionAmount;
        public boolean useLinearTransitionAmount;
        public int visibleState;

        public IconState() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:42:0x0091  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyToView(android.view.View r12) {
            /*
                r11 = this;
                boolean r0 = r12 instanceof com.android.systemui.statusbar.StatusBarIconView
                r1 = 0
                if (r0 == 0) goto L_0x0139
                r0 = r12
                com.android.systemui.statusbar.StatusBarIconView r0 = (com.android.systemui.statusbar.StatusBarIconView) r0
                r2 = 0
                com.android.systemui.statusbar.phone.NotificationIconContainer r3 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                boolean r3 = r3.mAnimationsEnabled
                r4 = 1
                if (r3 != 0) goto L_0x0016
                boolean r3 = r11.justUndarkened
                if (r3 == 0) goto L_0x0024
            L_0x0016:
                com.android.systemui.statusbar.phone.NotificationIconContainer r3 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                boolean r3 = r3.mDisallowNextAnimation
                if (r3 != 0) goto L_0x0024
                boolean r3 = r11.noAnimations
                if (r3 != 0) goto L_0x0024
                r3 = r4
                goto L_0x0025
            L_0x0024:
                r3 = r1
            L_0x0025:
                if (r3 == 0) goto L_0x011c
                boolean r5 = r11.justAdded
                r6 = 2
                if (r5 != 0) goto L_0x0047
                boolean r5 = r11.justReplaced
                if (r5 == 0) goto L_0x0031
                goto L_0x0047
            L_0x0031:
                boolean r5 = r11.justUndarkened
                if (r5 == 0) goto L_0x003a
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.UNDARK_PROPERTIES
                goto L_0x005f
            L_0x003a:
                int r5 = r11.visibleState
                int r7 = r0.getVisibleState()
                if (r5 == r7) goto L_0x0062
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                goto L_0x005f
            L_0x0047:
                super.applyToView(r0)
                boolean r5 = r11.justAdded
                if (r5 == 0) goto L_0x0062
                float r5 = r11.iconAppearAmount
                r7 = 0
                int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
                if (r5 == 0) goto L_0x0062
                r0.setAlpha(r7)
                r0.setVisibleState(r6, r1)
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.ADD_ICON_PROPERTIES
            L_0x005f:
                r5 = r2
                r2 = r4
                goto L_0x0064
            L_0x0062:
                r5 = r2
                r2 = r1
            L_0x0064:
                if (r2 != 0) goto L_0x008b
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mAddAnimationStartIndex
                if (r7 < 0) goto L_0x008b
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r12)
                com.android.systemui.statusbar.phone.NotificationIconContainer r8 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r8 = r8.mAddAnimationStartIndex
                if (r7 < r8) goto L_0x008b
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x0086
                int r7 = r11.visibleState
                if (r7 == r6) goto L_0x008b
            L_0x0086:
                com.android.systemui.statusbar.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                r2 = r4
            L_0x008b:
                boolean r7 = r11.needsCannedAnimation
                r8 = 100
                if (r7 == 0) goto L_0x00db
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                com.android.systemui.statusbar.stack.AnimationFilter r2 = r2.getAnimationFilter()
                r2.reset()
                com.android.systemui.statusbar.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                com.android.systemui.statusbar.stack.AnimationFilter r7 = r7.getAnimationFilter()
                r2.combineFilter(r7)
                com.android.systemui.statusbar.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r7.resetCustomInterpolators()
                com.android.systemui.statusbar.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                com.android.systemui.statusbar.stack.AnimationProperties r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                r7.combineCustomInterpolators(r10)
                if (r5 == 0) goto L_0x00c9
                com.android.systemui.statusbar.stack.AnimationFilter r7 = r5.getAnimationFilter()
                r2.combineFilter(r7)
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.combineCustomInterpolators(r5)
            L_0x00c9:
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.setDuration(r8)
                com.android.systemui.statusbar.phone.NotificationIconContainer r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r5.indexOfChild(r12)
                int unused = r5.mCannedAnimationStartIndex = r7
                r5 = r2
                r2 = r4
            L_0x00db:
                if (r2 != 0) goto L_0x011e
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mCannedAnimationStartIndex
                if (r7 < 0) goto L_0x011e
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r12)
                com.android.systemui.statusbar.phone.NotificationIconContainer r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r10 = r10.mCannedAnimationStartIndex
                if (r7 <= r10) goto L_0x011e
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x00fd
                int r7 = r11.visibleState
                if (r7 == r6) goto L_0x011e
            L_0x00fd:
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                com.android.systemui.statusbar.stack.AnimationFilter r2 = r2.getAnimationFilter()
                r2.reset()
                r2.animateX()
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.resetCustomInterpolators()
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.setDuration(r8)
                r5 = r2
                r2 = r4
                goto L_0x011e
            L_0x011c:
                r5 = r2
                r2 = r1
            L_0x011e:
                int r6 = r11.visibleState
                r0.setVisibleState(r6, r3)
                int r6 = r11.iconColor
                boolean r7 = r11.needsCannedAnimation
                if (r7 == 0) goto L_0x012c
                if (r3 == 0) goto L_0x012c
                goto L_0x012d
            L_0x012c:
                r4 = r1
            L_0x012d:
                r0.setIconColor(r6, r4)
                if (r2 == 0) goto L_0x0136
                r11.animateTo(r0, r5)
                goto L_0x0139
            L_0x0136:
                super.applyToView(r12)
            L_0x0139:
                r11.justAdded = r1
                r11.justReplaced = r1
                r11.needsCannedAnimation = r1
                r11.justUndarkened = r1
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationIconContainer.IconState.applyToView(android.view.View):void");
        }

        public void initFrom(View view) {
            super.initFrom(view);
            if (view instanceof StatusBarIconView) {
                this.iconColor = ((StatusBarIconView) view).getStaticDrawableColor();
            }
        }
    }
}
