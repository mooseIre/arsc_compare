package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import androidx.collection.ArrayMap;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.AlphaOptimizedFrameLayout;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.phone.NotificationIconContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

public class NotificationIconContainer extends AlphaOptimizedFrameLayout {
    /* access modifiers changed from: private */
    public static final AnimationProperties ADD_ICON_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties DOT_ANIMATION_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties ICON_ANIMATION_PROPERTIES;
    /* access modifiers changed from: private */
    public static final AnimationProperties UNISOLATION_PROPERTY;
    /* access modifiers changed from: private */
    public static final AnimationProperties UNISOLATION_PROPERTY_OTHERS;
    /* access modifiers changed from: private */
    public static final AnimationProperties sTempProperties = new AnimationProperties() {
        private AnimationFilter mAnimationFilter = new AnimationFilter();

        public AnimationFilter getAnimationFilter() {
            return this.mAnimationFilter;
        }
    };
    public int MAX_DOTS = 3;
    public int MAX_STATIC_ICONS = 3;
    public int MAX_VISIBLE_ICONS_ON_LOCK = 3;
    private int[] mAbsolutePosition = new int[2];
    private int mActualLayoutWidth = Integer.MIN_VALUE;
    private float mActualPaddingEnd = -2.14748365E9f;
    private float mActualPaddingStart = -2.14748365E9f;
    /* access modifiers changed from: private */
    public int mAddAnimationStartIndex = -1;
    private boolean mAnimationsEnabled = true;
    /* access modifiers changed from: private */
    public int mCannedAnimationStartIndex = -1;
    private boolean mChangingViewPositions;
    /* access modifiers changed from: private */
    public boolean mDisallowNextAnimation;
    private int mDotPadding;
    private boolean mDozing;
    private IconState mFirstVisibleIconState;
    private int mIconSize;
    private final HashMap<View, IconState> mIconStates = new HashMap<>();
    private boolean mIsStaticLayout = true;
    /* access modifiers changed from: private */
    public StatusBarIconView mIsolatedIcon;
    /* access modifiers changed from: private */
    public View mIsolatedIconForAnimation;
    private IconState mLastVisibleIconState;
    private int mNumDots;
    private boolean mOnLockScreen;
    private float mOpenedAmount = 0.0f;
    private int mOverflowWidth;
    private ArrayMap<String, ArrayList<StatusBarIcon>> mReplacingIcons;
    private int mSpeedBumpIndex = -1;
    private int mStaticDotDiameter;
    private int mStaticDotRadius;
    private float mVisualOverflowStart;

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
                animationFilter.animateX();
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
                animationFilter.animateAlpha();
                this.mAnimationFilter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.mAnimationFilter;
            }
        };
        r04.setDuration(110);
        UNISOLATION_PROPERTY_OTHERS = r04;
        AnonymousClass6 r05 = new AnimationProperties() {
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
        r05.setDuration(110);
        UNISOLATION_PROPERTY = r05;
    }

    public NotificationIconContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDimens();
        setWillNotDraw(true);
    }

    private void initDimens() {
        this.mDotPadding = getResources().getDimensionPixelSize(C0012R$dimen.overflow_icon_dot_padding);
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.overflow_dot_radius);
        this.mStaticDotRadius = dimensionPixelSize;
        this.mStaticDotDiameter = dimensionPixelSize * 2;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(-65536);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(getActualPaddingStart(), 0.0f, getLayoutEnd(), (float) getHeight(), paint);
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
                setIconSize(childAt.getWidth());
            }
        }
        getLocationOnScreen(this.mAbsolutePosition);
        if (this.mIsStaticLayout) {
            updateState();
        }
    }

    private void setIconSize(int i) {
        this.mIconSize = i;
        this.mOverflowWidth = i + ((this.MAX_DOTS - 1) * (this.mStaticDotDiameter + this.mDotPadding));
    }

    public void updateState() {
        resetViewStates();
        calculateIconTranslations();
        applyIconStates();
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
        this.mIsolatedIconForAnimation = null;
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        boolean isReplacingIcon = isReplacingIcon(view);
        if (!this.mChangingViewPositions) {
            IconState iconState = new IconState(view);
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
        if (view instanceof StatusBarIconView) {
            ((StatusBarIconView) view).setDozing(this.mDozing, false, 0);
        }
    }

    private boolean isReplacingIcon(View view) {
        if (this.mReplacingIcons == null || !(view instanceof StatusBarIconView)) {
            return false;
        }
        StatusBarIconView statusBarIconView = (StatusBarIconView) view;
        Icon sourceIcon = statusBarIconView.getSourceIcon();
        ArrayList arrayList = this.mReplacingIcons.get(statusBarIconView.getNotification().getGroupKey());
        if (arrayList == null || !sourceIcon.sameAs(((StatusBarIcon) arrayList.get(0)).icon)) {
            return false;
        }
        return true;
    }

    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (view instanceof StatusBarIconView) {
            boolean isReplacingIcon = isReplacingIcon(view);
            StatusBarIconView statusBarIconView = (StatusBarIconView) view;
            if (areAnimationsEnabled(statusBarIconView) && statusBarIconView.getVisibleState() != 2 && view.getVisibility() == 0 && isReplacingIcon) {
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
                if (areAnimationsEnabled(statusBarIconView) && !isReplacingIcon) {
                    boolean z = false;
                    addTransientView(statusBarIconView, 0);
                    if (view == this.mIsolatedIcon) {
                        z = true;
                    }
                    statusBarIconView.setVisibleState(2, true, new Runnable(statusBarIconView) {
                        public final /* synthetic */ StatusBarIconView f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            NotificationIconContainer.this.lambda$onViewRemoved$0$NotificationIconContainer(this.f$1);
                        }
                    }, z ? 110 : 0);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewRemoved$0 */
    public /* synthetic */ void lambda$onViewRemoved$0$NotificationIconContainer(StatusBarIconView statusBarIconView) {
        removeTransientView(statusBarIconView);
    }

    /* access modifiers changed from: private */
    public boolean areAnimationsEnabled(StatusBarIconView statusBarIconView) {
        return this.mAnimationsEnabled || statusBarIconView == this.mIsolatedIcon;
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
        float actualPaddingStart = getActualPaddingStart();
        int childCount = getChildCount();
        if (this.mOnLockScreen) {
            i = this.MAX_VISIBLE_ICONS_ON_LOCK;
        } else {
            i = this.mIsStaticLayout ? this.MAX_STATIC_ICONS : childCount;
        }
        float layoutEnd = getLayoutEnd();
        getMaxOverflowStart();
        float f = 0.0f;
        this.mVisualOverflowStart = 0.0f;
        this.mFirstVisibleIconState = null;
        int i2 = this.mSpeedBumpIndex;
        if (i2 == -1 || i2 >= getChildCount()) {
        }
        int i3 = -1;
        int i4 = 0;
        while (i4 < childCount) {
            View childAt = getChildAt(i4);
            IconState iconState = this.mIconStates.get(childAt);
            if (iconState.iconAppearAmount == 1.0f) {
                iconState.xTranslation = actualPaddingStart;
            }
            if (this.mFirstVisibleIconState == null) {
                this.mFirstVisibleIconState = iconState;
            }
            int i5 = this.mSpeedBumpIndex;
            boolean z = (i5 != -1 && i4 >= i5 && iconState.iconAppearAmount > 0.0f) || i4 >= i;
            if (i4 == childCount - 1) {
            }
            float iconScaleIncreased = (!this.mOnLockScreen || !(childAt instanceof StatusBarIconView)) ? 1.0f : ((StatusBarIconView) childAt).getIconScaleIncreased();
            int i6 = (this.mOpenedAmount > 0.0f ? 1 : (this.mOpenedAmount == 0.0f ? 0 : -1));
            iconState.visibleState = 0;
            boolean z2 = actualPaddingStart > layoutEnd - ((float) this.mIconSize);
            if (i3 == -1 && (z || z2)) {
                this.mVisualOverflowStart = actualPaddingStart;
                i3 = i4;
            }
            actualPaddingStart += iconState.iconAppearAmount * ((float) childAt.getWidth()) * iconScaleIncreased;
            i4++;
        }
        this.mNumDots = 0;
        if (i3 != -1) {
            actualPaddingStart = this.mVisualOverflowStart;
            for (int i7 = i3; i7 < childCount; i7++) {
                IconState iconState2 = this.mIconStates.get(getChildAt(i7));
                int i8 = this.mStaticDotDiameter + this.mDotPadding;
                iconState2.xTranslation = actualPaddingStart;
                int i9 = this.mNumDots;
                int i10 = this.MAX_DOTS;
                if (i9 < i10) {
                    if (((float) (((this.mIconSize + i8) + 1) / 2)) + actualPaddingStart <= layoutEnd) {
                        iconState2.visibleState = 1;
                        int i11 = i9 + 1;
                        this.mNumDots = i11;
                        if (i11 == i10) {
                            i8 *= i10;
                        }
                        actualPaddingStart += ((float) i8) * iconState2.iconAppearAmount;
                        this.mLastVisibleIconState = iconState2;
                    }
                }
                iconState2.visibleState = 2;
            }
        } else if (childCount > 0) {
            this.mLastVisibleIconState = this.mIconStates.get(getChildAt(childCount - 1));
            this.mFirstVisibleIconState = this.mIconStates.get(getChildAt(0));
        }
        if (this.mOnLockScreen && actualPaddingStart < getLayoutEnd()) {
            IconState iconState3 = this.mFirstVisibleIconState;
            float f2 = iconState3 == null ? 0.0f : iconState3.xTranslation;
            IconState iconState4 = this.mLastVisibleIconState;
            if (iconState4 != null) {
                f = Math.min((float) getWidth(), iconState4.xTranslation + ((float) this.mIconSize)) - f2;
            }
            float layoutEnd2 = ((getLayoutEnd() - getActualPaddingStart()) - f) / 2.0f;
            if (i3 != -1) {
                layoutEnd2 = (((getLayoutEnd() - this.mVisualOverflowStart) / 2.0f) + layoutEnd2) / 2.0f;
            }
            for (int i12 = 0; i12 < childCount; i12++) {
                this.mIconStates.get(getChildAt(i12)).xTranslation += layoutEnd2;
            }
        }
        if (isLayoutRtl()) {
            for (int i13 = 0; i13 < childCount; i13++) {
                View childAt2 = getChildAt(i13);
                IconState iconState5 = this.mIconStates.get(childAt2);
                iconState5.xTranslation = (((float) getWidth()) - iconState5.xTranslation) - ((float) childAt2.getWidth());
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

    public float getActualPaddingStart() {
        float f = this.mActualPaddingStart;
        return f == -2.14748365E9f ? (float) getPaddingStart() : f;
    }

    public void setIsStaticLayout(boolean z) {
        this.mIsStaticLayout = z;
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

    public int getFinalTranslationX() {
        float f;
        if (this.mLastVisibleIconState == null) {
            return 0;
        }
        if (isLayoutRtl()) {
            f = ((float) getWidth()) - this.mLastVisibleIconState.xTranslation;
        } else {
            f = this.mLastVisibleIconState.xTranslation + ((float) this.mIconSize);
        }
        return Math.min(getWidth(), (int) f);
    }

    private float getMaxOverflowStart() {
        return getLayoutEnd() - ((float) this.mOverflowWidth);
    }

    public void setChangingViewPositions(boolean z) {
        this.mChangingViewPositions = z;
    }

    public void setDozing(boolean z, boolean z2, long j) {
        this.mDozing = z;
        this.mDisallowNextAnimation |= !z2;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof StatusBarIconView) {
                ((StatusBarIconView) childAt).setDozing(z, z2, j);
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

    public boolean hasOverflow() {
        return this.mNumDots > 0;
    }

    public boolean hasPartialOverflow() {
        int i = this.mNumDots;
        return i > 0 && i < this.MAX_DOTS;
    }

    public int getPartialOverflowExtraPadding() {
        if (!hasPartialOverflow()) {
            return 0;
        }
        int i = (this.MAX_DOTS - this.mNumDots) * (this.mStaticDotDiameter + this.mDotPadding);
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
    }

    public int getNoOverflowExtraPadding() {
        if (this.mNumDots != 0) {
            return 0;
        }
        int i = this.mOverflowWidth;
        return getFinalTranslationX() + i > getWidth() ? getWidth() - getFinalTranslationX() : i;
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

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        if (z) {
            this.mIsolatedIconForAnimation = statusBarIconView != null ? statusBarIconView : this.mIsolatedIcon;
        }
        this.mIsolatedIcon = statusBarIconView;
        updateState();
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        if (z) {
            updateState();
        }
    }

    public void setOnLockScreen(boolean z) {
        this.mOnLockScreen = z;
    }

    public class IconState extends ViewState {
        public float clampedAppearAmount = 1.0f;
        public int customTransformHeight = Integer.MIN_VALUE;
        public float iconAppearAmount = 1.0f;
        public int iconColor = 0;
        public boolean isLastExpandIcon;
        public boolean justAdded = true;
        /* access modifiers changed from: private */
        public boolean justReplaced;
        private final Consumer<Property> mCannedAnimationEndListener;
        private final View mView;
        public boolean needsCannedAnimation;
        public boolean noAnimations;
        public boolean translateContent;
        public boolean useFullTransitionAmount;
        public boolean useLinearTransitionAmount;
        public int visibleState;

        public IconState(View view) {
            this.mView = view;
            this.mCannedAnimationEndListener = new Consumer() {
                public final void accept(Object obj) {
                    NotificationIconContainer.IconState.this.lambda$new$0$NotificationIconContainer$IconState((Property) obj);
                }
            };
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$new$0 */
        public /* synthetic */ void lambda$new$0$NotificationIconContainer$IconState(Property property) {
            if (property == View.TRANSLATION_Y && this.iconAppearAmount == 0.0f && this.mView.getVisibility() == 0) {
                this.mView.setVisibility(4);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:37:0x0082  */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x012f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyToView(android.view.View r13) {
            /*
                r12 = this;
                boolean r0 = r13 instanceof com.android.systemui.statusbar.StatusBarIconView
                r1 = 0
                if (r0 == 0) goto L_0x0188
                r0 = r13
                com.android.systemui.statusbar.StatusBarIconView r0 = (com.android.systemui.statusbar.StatusBarIconView) r0
                r2 = 0
                com.android.systemui.statusbar.phone.NotificationIconContainer r3 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                boolean r3 = r3.areAnimationsEnabled(r0)
                r4 = 1
                if (r3 == 0) goto L_0x0020
                com.android.systemui.statusbar.phone.NotificationIconContainer r3 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                boolean r3 = r3.mDisallowNextAnimation
                if (r3 != 0) goto L_0x0020
                boolean r3 = r12.noAnimations
                if (r3 != 0) goto L_0x0020
                r3 = r4
                goto L_0x0021
            L_0x0020:
                r3 = r1
            L_0x0021:
                if (r3 == 0) goto L_0x015e
                boolean r5 = r12.justAdded
                r6 = 2
                if (r5 != 0) goto L_0x003a
                boolean r5 = r12.justReplaced
                if (r5 == 0) goto L_0x002d
                goto L_0x003a
            L_0x002d:
                int r5 = r12.visibleState
                int r7 = r0.getVisibleState()
                if (r5 == r7) goto L_0x0054
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                goto L_0x0052
            L_0x003a:
                super.applyToView(r0)
                boolean r5 = r12.justAdded
                if (r5 == 0) goto L_0x0054
                float r5 = r12.iconAppearAmount
                r7 = 0
                int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
                if (r5 == 0) goto L_0x0054
                r0.setAlpha(r7)
                r0.setVisibleState(r6, r1)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.ADD_ICON_PROPERTIES
            L_0x0052:
                r5 = r4
                goto L_0x0055
            L_0x0054:
                r5 = r1
            L_0x0055:
                if (r5 != 0) goto L_0x007c
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mAddAnimationStartIndex
                if (r7 < 0) goto L_0x007c
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r13)
                com.android.systemui.statusbar.phone.NotificationIconContainer r8 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r8 = r8.mAddAnimationStartIndex
                if (r7 < r8) goto L_0x007c
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x0077
                int r7 = r12.visibleState
                if (r7 == r6) goto L_0x007c
            L_0x0077:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                r5 = r4
            L_0x007c:
                boolean r7 = r12.needsCannedAnimation
                r8 = 100
                if (r7 == 0) goto L_0x00e8
                com.android.systemui.statusbar.notification.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                com.android.systemui.statusbar.notification.stack.AnimationFilter r5 = r5.getAnimationFilter()
                r5.reset()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                com.android.systemui.statusbar.notification.stack.AnimationFilter r7 = r7.getAnimationFilter()
                r5.combineFilter(r7)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r7.resetCustomInterpolators()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                com.android.systemui.statusbar.notification.stack.AnimationProperties r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                r7.combineCustomInterpolators(r10)
                boolean r7 = r0.showsConversation()
                if (r7 == 0) goto L_0x00b3
                android.view.animation.Interpolator r7 = com.android.systemui.Interpolators.ICON_OVERSHOT_LESS
                goto L_0x00b5
            L_0x00b3:
                android.view.animation.Interpolator r7 = com.android.systemui.Interpolators.ICON_OVERSHOT
            L_0x00b5:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                android.util.Property r11 = android.view.View.TRANSLATION_Y
                r10.setCustomInterpolator(r11, r7)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                java.util.function.Consumer<android.util.Property> r10 = r12.mCannedAnimationEndListener
                r7.setAnimationEndAction(r10)
                if (r2 == 0) goto L_0x00d7
                com.android.systemui.statusbar.notification.stack.AnimationFilter r7 = r2.getAnimationFilter()
                r5.combineFilter(r7)
                com.android.systemui.statusbar.notification.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r5.combineCustomInterpolators(r2)
            L_0x00d7:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r2.setDuration(r8)
                com.android.systemui.statusbar.phone.NotificationIconContainer r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r5.indexOfChild(r13)
                int unused = r5.mCannedAnimationStartIndex = r7
                r5 = r4
            L_0x00e8:
                if (r5 != 0) goto L_0x0127
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mCannedAnimationStartIndex
                if (r7 < 0) goto L_0x0127
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r13)
                com.android.systemui.statusbar.phone.NotificationIconContainer r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r10 = r10.mCannedAnimationStartIndex
                if (r7 <= r10) goto L_0x0127
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x010a
                int r7 = r12.visibleState
                if (r7 == r6) goto L_0x0127
            L_0x010a:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                com.android.systemui.statusbar.notification.stack.AnimationFilter r2 = r2.getAnimationFilter()
                r2.reset()
                r2.animateX()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r2.resetCustomInterpolators()
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.sTempProperties
                r2.setDuration(r8)
                r5 = r4
            L_0x0127:
                com.android.systemui.statusbar.phone.NotificationIconContainer r6 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                android.view.View r6 = r6.mIsolatedIconForAnimation
                if (r6 == 0) goto L_0x015f
                com.android.systemui.statusbar.phone.NotificationIconContainer r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                android.view.View r2 = r2.mIsolatedIconForAnimation
                r5 = 0
                if (r13 != r2) goto L_0x014b
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.UNISOLATION_PROPERTY
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                com.android.systemui.statusbar.StatusBarIconView r7 = r7.mIsolatedIcon
                if (r7 == 0) goto L_0x0146
                goto L_0x0147
            L_0x0146:
                r8 = r5
            L_0x0147:
                r2.setDelay(r8)
                goto L_0x015c
            L_0x014b:
                com.android.systemui.statusbar.notification.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.UNISOLATION_PROPERTY_OTHERS
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                com.android.systemui.statusbar.StatusBarIconView r7 = r7.mIsolatedIcon
                if (r7 != 0) goto L_0x0158
                goto L_0x0159
            L_0x0158:
                r8 = r5
            L_0x0159:
                r2.setDelay(r8)
            L_0x015c:
                r5 = r4
                goto L_0x015f
            L_0x015e:
                r5 = r1
            L_0x015f:
                int r6 = r12.visibleState
                r0.setVisibleState(r6, r3)
                int r6 = r12.iconColor
                boolean r7 = r12.needsCannedAnimation
                if (r7 == 0) goto L_0x016e
                if (r3 == 0) goto L_0x016e
                r3 = r4
                goto L_0x016f
            L_0x016e:
                r3 = r1
            L_0x016f:
                r0.setIconColor(r6, r3)
                if (r5 == 0) goto L_0x0178
                r12.animateTo(r0, r2)
                goto L_0x017b
            L_0x0178:
                super.applyToView(r13)
            L_0x017b:
                float r13 = r12.iconAppearAmount
                r2 = 1065353216(0x3f800000, float:1.0)
                int r13 = (r13 > r2 ? 1 : (r13 == r2 ? 0 : -1))
                if (r13 != 0) goto L_0x0184
                goto L_0x0185
            L_0x0184:
                r4 = r1
            L_0x0185:
                r0.setIsInShelf(r4)
            L_0x0188:
                r12.justAdded = r1
                r12.justReplaced = r1
                r12.needsCannedAnimation = r1
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationIconContainer.IconState.applyToView(android.view.View):void");
        }

        public boolean hasCustomTransformHeight() {
            return this.isLastExpandIcon && this.customTransformHeight != Integer.MIN_VALUE;
        }

        public void initFrom(View view) {
            super.initFrom(view);
            if (view instanceof StatusBarIconView) {
                this.iconColor = ((StatusBarIconView) view).getStaticDrawableColor();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setMaxStaticIcons(int i) {
        this.MAX_STATIC_ICONS = i;
    }

    /* access modifiers changed from: protected */
    public void setMaxVisibleIconsOnLock(int i) {
        this.MAX_STATIC_ICONS = i;
    }

    /* access modifiers changed from: protected */
    public void setMaxDots(int i) {
        this.MAX_DOTS = i;
    }
}
