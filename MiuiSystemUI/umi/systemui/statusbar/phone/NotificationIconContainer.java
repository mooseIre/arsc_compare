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

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0090, code lost:
        if (r1 >= (r8 ? r4 - ((float) r0.mIconSize) : r5)) goto L_0x0092;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void calculateIconTranslations() {
        /*
            r21 = this;
            r0 = r21
            float r1 = r21.getActualPaddingStart()
            int r2 = r21.getChildCount()
            r3 = 3
            int r3 = java.lang.Math.min(r2, r3)
            float r4 = r21.getLayoutEnd()
            int r5 = r0.mIconSize
            float r5 = (float) r5
            r6 = 1073741824(0x40000000, float:2.0)
            float r5 = r5 * r6
            float r5 = r4 - r5
            int r7 = r0.mSpeedBumpIndex
            r8 = -1
            if (r7 == r8) goto L_0x0028
            int r11 = r21.getChildCount()
            if (r7 >= r11) goto L_0x0028
            r7 = 1
            goto L_0x0029
        L_0x0028:
            r7 = 0
        L_0x0029:
            r11 = 0
            r13 = r8
            r14 = r11
            r12 = 0
        L_0x002d:
            if (r12 >= r2) goto L_0x00fa
            android.view.View r6 = r0.getChildAt(r12)
            java.util.HashMap<android.view.View, com.android.systemui.statusbar.phone.NotificationIconContainer$IconState> r10 = r0.mIconStates
            java.lang.Object r10 = r10.get(r6)
            com.android.systemui.statusbar.phone.NotificationIconContainer$IconState r10 = (com.android.systemui.statusbar.phone.NotificationIconContainer.IconState) r10
            r10.xTranslation = r1
            int r15 = r0.mSpeedBumpIndex
            if (r15 == r8) goto L_0x0049
            if (r12 < r15) goto L_0x0049
            float r15 = r10.iconAppearAmount
            int r15 = (r15 > r11 ? 1 : (r15 == r11 ? 0 : -1))
            if (r15 > 0) goto L_0x004b
        L_0x0049:
            if (r12 < r3) goto L_0x004d
        L_0x004b:
            r15 = 1
            goto L_0x004e
        L_0x004d:
            r15 = 0
        L_0x004e:
            int r8 = r2 + -1
            if (r12 != r8) goto L_0x0054
            r8 = 1
            goto L_0x0055
        L_0x0054:
            r8 = 0
        L_0x0055:
            boolean r9 = r0.mDark
            r17 = 1065353216(0x3f800000, float:1.0)
            if (r9 == 0) goto L_0x0069
            boolean r9 = r6 instanceof com.android.systemui.statusbar.StatusBarIconView
            if (r9 == 0) goto L_0x0069
            r9 = r6
            com.android.systemui.statusbar.StatusBarIconView r9 = (com.android.systemui.statusbar.StatusBarIconView) r9
            float r9 = r9.getIconScaleFullyDark()
            r18 = r3
            goto L_0x006d
        L_0x0069:
            r18 = r3
            r9 = r17
        L_0x006d:
            float r3 = r0.mOpenedAmount
            int r3 = (r3 > r11 ? 1 : (r3 == r11 ? 0 : -1))
            if (r3 == 0) goto L_0x007d
            if (r8 == 0) goto L_0x007b
            if (r7 != 0) goto L_0x007b
            if (r15 != 0) goto L_0x007b
            r3 = 1
            goto L_0x007c
        L_0x007b:
            r3 = 0
        L_0x007c:
            r8 = r3
        L_0x007d:
            r3 = 0
            r10.visibleState = r3
            r3 = -1
            if (r13 != r3) goto L_0x00e2
            if (r15 != 0) goto L_0x0092
            if (r8 == 0) goto L_0x008d
            int r3 = r0.mIconSize
            float r3 = (float) r3
            float r3 = r4 - r3
            goto L_0x008e
        L_0x008d:
            r3 = r5
        L_0x008e:
            int r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r3 < 0) goto L_0x00e2
        L_0x0092:
            if (r8 == 0) goto L_0x009a
            if (r15 != 0) goto L_0x009a
            int r3 = r12 + -1
            r13 = r3
            goto L_0x009b
        L_0x009a:
            r13 = r12
        L_0x009b:
            int r3 = r0.mStaticDotRadius
            int r8 = r3 * 6
            int r14 = r0.mDotPadding
            int r19 = r14 * 2
            int r8 = r8 + r19
            int r11 = r0.mIconSize
            r20 = r4
            float r4 = (float) r11
            float r4 = r4 * r17
            float r4 = r4 + r5
            r16 = 2
            int r8 = r8 / 2
            float r8 = (float) r8
            float r4 = r4 - r8
            float r8 = (float) r11
            r16 = 1056964608(0x3f000000, float:0.5)
            float r8 = r8 * r16
            float r4 = r4 - r8
            float r8 = (float) r3
            float r4 = r4 + r8
            if (r15 == 0) goto L_0x00c9
            int r3 = r3 * 2
            float r3 = (float) r3
            float r4 = r4 + r3
            float r3 = (float) r14
            float r4 = r4 + r3
            float r3 = java.lang.Math.min(r1, r4)
            r14 = r3
            goto L_0x00d4
        L_0x00c9:
            float r8 = r1 - r5
            float r11 = (float) r11
            float r8 = r8 / r11
            int r3 = r3 * 2
            int r3 = r3 + r14
            float r3 = (float) r3
            float r8 = r8 * r3
            float r4 = r4 + r8
            r14 = r4
        L_0x00d4:
            boolean r3 = r0.mShowAllIcons
            if (r3 != 0) goto L_0x00e4
            float r3 = r0.mVisualOverflowAdaption
            float r4 = r0.mOpenedAmount
            float r17 = r17 - r4
            float r3 = r3 * r17
            float r14 = r14 + r3
            goto L_0x00e4
        L_0x00e2:
            r20 = r4
        L_0x00e4:
            float r3 = r10.iconAppearAmount
            int r4 = r6.getWidth()
            float r4 = (float) r4
            float r3 = r3 * r4
            float r3 = r3 * r9
            float r1 = r1 + r3
            int r12 = r12 + 1
            r3 = r18
            r4 = r20
            r6 = 1073741824(0x40000000, float:2.0)
            r8 = -1
            r11 = 0
            goto L_0x002d
        L_0x00fa:
            java.util.HashMap<android.view.View, com.android.systemui.statusbar.phone.NotificationIconContainer$IconState> r3 = r0.mIconStates
            r4 = 0
            android.view.View r5 = r0.getChildAt(r4)
            java.lang.Object r3 = r3.get(r5)
            com.android.systemui.statusbar.phone.NotificationIconContainer$IconState r3 = (com.android.systemui.statusbar.phone.NotificationIconContainer.IconState) r3
            r4 = -1
            if (r13 == r4) goto L_0x0156
            r5 = r3
            r6 = r13
            r1 = r14
            r3 = 0
            r4 = 0
        L_0x010f:
            if (r6 >= r2) goto L_0x0154
            android.view.View r7 = r0.getChildAt(r6)
            java.util.HashMap<android.view.View, com.android.systemui.statusbar.phone.NotificationIconContainer$IconState> r8 = r0.mIconStates
            java.lang.Object r7 = r8.get(r7)
            com.android.systemui.statusbar.phone.NotificationIconContainer$IconState r7 = (com.android.systemui.statusbar.phone.NotificationIconContainer.IconState) r7
            int r8 = r0.mStaticDotRadius
            r9 = 2
            int r8 = r8 * r9
            int r9 = r0.mDotPadding
            int r8 = r8 + r9
            r7.xTranslation = r1
            int r9 = r0.mMaxDotNum
            if (r3 >= r9) goto L_0x014b
            r9 = 1
            if (r3 != r9) goto L_0x013b
            float r10 = r7.iconAppearAmount
            r11 = 1061997773(0x3f4ccccd, float:0.8)
            int r10 = (r10 > r11 ? 1 : (r10 == r11 ? 0 : -1))
            if (r10 >= 0) goto L_0x013b
            r10 = 0
            r7.visibleState = r10
            r5 = r7
            goto L_0x0140
        L_0x013b:
            r10 = 0
            r7.visibleState = r9
            int r3 = r3 + 1
        L_0x0140:
            int r9 = r0.mMaxDotNum
            if (r3 != r9) goto L_0x0145
            int r8 = r8 * r9
        L_0x0145:
            float r8 = (float) r8
            float r7 = r7.iconAppearAmount
            float r8 = r8 * r7
            float r1 = r1 + r8
            goto L_0x0151
        L_0x014b:
            r8 = 2
            r10 = 0
            r7.visibleState = r8
            int r4 = r4 + 1
        L_0x0151:
            int r6 = r6 + 1
            goto L_0x010f
        L_0x0154:
            r10 = 0
            goto L_0x015a
        L_0x0156:
            r10 = 0
            r5 = r3
            r3 = r10
            r4 = r3
        L_0x015a:
            boolean r6 = r0.mDark
            if (r6 == 0) goto L_0x0191
            float r6 = r21.getLayoutEnd()
            int r6 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
            if (r6 >= 0) goto L_0x0191
            float r6 = r21.getLayoutEnd()
            float r6 = r6 - r1
            r1 = 1073741824(0x40000000, float:2.0)
            float r6 = r6 / r1
            r7 = -1
            if (r13 == r7) goto L_0x017a
            float r7 = r21.getLayoutEnd()
            float r7 = r7 - r14
            float r7 = r7 / r1
            float r7 = r7 + r6
            float r6 = r7 / r1
        L_0x017a:
            r1 = r10
        L_0x017b:
            if (r1 >= r2) goto L_0x0191
            android.view.View r7 = r0.getChildAt(r1)
            java.util.HashMap<android.view.View, com.android.systemui.statusbar.phone.NotificationIconContainer$IconState> r8 = r0.mIconStates
            java.lang.Object r7 = r8.get(r7)
            com.android.systemui.statusbar.phone.NotificationIconContainer$IconState r7 = (com.android.systemui.statusbar.phone.NotificationIconContainer.IconState) r7
            float r8 = r7.xTranslation
            float r8 = r8 + r6
            r7.xTranslation = r8
            int r1 = r1 + 1
            goto L_0x017b
        L_0x0191:
            boolean r1 = r21.isLayoutRtl()
            if (r1 == 0) goto L_0x01b9
            r9 = r10
        L_0x0198:
            if (r9 >= r2) goto L_0x01b9
            android.view.View r1 = r0.getChildAt(r9)
            java.util.HashMap<android.view.View, com.android.systemui.statusbar.phone.NotificationIconContainer$IconState> r6 = r0.mIconStates
            java.lang.Object r6 = r6.get(r1)
            com.android.systemui.statusbar.phone.NotificationIconContainer$IconState r6 = (com.android.systemui.statusbar.phone.NotificationIconContainer.IconState) r6
            int r7 = r21.getWidth()
            float r7 = (float) r7
            float r8 = r6.xTranslation
            float r7 = r7 - r8
            int r1 = r1.getWidth()
            float r1 = (float) r1
            float r7 = r7 - r1
            r6.xTranslation = r7
            int r9 = r9 + 1
            goto L_0x0198
        L_0x01b9:
            java.lang.Class<com.android.systemui.statusbar.phone.StatusBarTypeController> r1 = com.android.systemui.statusbar.phone.StatusBarTypeController.class
            java.lang.Object r1 = com.android.systemui.Dependency.get(r1)
            com.android.systemui.statusbar.phone.StatusBarTypeController r1 = (com.android.systemui.statusbar.phone.StatusBarTypeController) r1
            boolean r1 = r1.hasCutout()
            if (r1 == 0) goto L_0x01f9
            if (r13 == 0) goto L_0x01ce
            int r1 = r2 - r4
            r4 = 1
            if (r1 != r4) goto L_0x01f9
        L_0x01ce:
            r1 = -1
            if (r13 != r1) goto L_0x01e0
            int r1 = r0.mIconSize
            int r2 = r2 * r1
            int r0 = r21.getWidth()
            if (r0 >= r2) goto L_0x01f9
            if (r5 == 0) goto L_0x01f9
            r1 = 2
            r5.visibleState = r1
            goto L_0x01f9
        L_0x01e0:
            r1 = 2
            int r2 = r0.mIconSize
            int r2 = r2 * r13
            int r4 = r0.mStaticDotRadius
            int r4 = r4 * r3
            int r4 = r4 * r1
            int r2 = r2 + r4
            int r1 = r3 + -1
            int r4 = r0.mDotPadding
            int r1 = r1 * r4
            int r2 = r2 + r1
            int r1 = r21.getWidth()
            if (r1 >= r2) goto L_0x01f9
            int r13 = r13 + r3
            r0.updateIconStates(r13)
        L_0x01f9:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationIconContainer.calculateIconTranslations():void");
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

        /* JADX WARNING: Removed duplicated region for block: B:42:0x008f  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void applyToView(android.view.View r12) {
            /*
                r11 = this;
                boolean r0 = r12 instanceof com.android.systemui.statusbar.StatusBarIconView
                r1 = 0
                if (r0 == 0) goto L_0x0134
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
                if (r3 == 0) goto L_0x0118
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
                if (r5 == r7) goto L_0x0061
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                goto L_0x005f
            L_0x0047:
                super.applyToView(r0)
                boolean r5 = r11.justAdded
                if (r5 == 0) goto L_0x0061
                float r5 = r11.iconAppearAmount
                r7 = 0
                int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
                if (r5 == 0) goto L_0x0061
                r0.setAlpha(r7)
                r0.setVisibleState(r6, r1)
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.ADD_ICON_PROPERTIES
            L_0x005f:
                r5 = r4
                goto L_0x0062
            L_0x0061:
                r5 = r1
            L_0x0062:
                if (r5 != 0) goto L_0x0089
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mAddAnimationStartIndex
                if (r7 < 0) goto L_0x0089
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r12)
                com.android.systemui.statusbar.phone.NotificationIconContainer r8 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r8 = r8.mAddAnimationStartIndex
                if (r7 < r8) goto L_0x0089
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x0084
                int r7 = r11.visibleState
                if (r7 == r6) goto L_0x0089
            L_0x0084:
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.DOT_ANIMATION_PROPERTIES
                r5 = r4
            L_0x0089:
                boolean r7 = r11.needsCannedAnimation
                r8 = 100
                if (r7 == 0) goto L_0x00d8
                com.android.systemui.statusbar.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                com.android.systemui.statusbar.stack.AnimationFilter r5 = r5.getAnimationFilter()
                r5.reset()
                com.android.systemui.statusbar.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                com.android.systemui.statusbar.stack.AnimationFilter r7 = r7.getAnimationFilter()
                r5.combineFilter(r7)
                com.android.systemui.statusbar.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r7.resetCustomInterpolators()
                com.android.systemui.statusbar.stack.AnimationProperties r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                com.android.systemui.statusbar.stack.AnimationProperties r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.ICON_ANIMATION_PROPERTIES
                r7.combineCustomInterpolators(r10)
                if (r2 == 0) goto L_0x00c7
                com.android.systemui.statusbar.stack.AnimationFilter r7 = r2.getAnimationFilter()
                r5.combineFilter(r7)
                com.android.systemui.statusbar.stack.AnimationProperties r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r5.combineCustomInterpolators(r2)
            L_0x00c7:
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.setDuration(r8)
                com.android.systemui.statusbar.phone.NotificationIconContainer r5 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r5.indexOfChild(r12)
                int unused = r5.mCannedAnimationStartIndex = r7
                r5 = r4
            L_0x00d8:
                if (r5 != 0) goto L_0x0119
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.mCannedAnimationStartIndex
                if (r7 < 0) goto L_0x0119
                com.android.systemui.statusbar.phone.NotificationIconContainer r7 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r7 = r7.indexOfChild(r12)
                com.android.systemui.statusbar.phone.NotificationIconContainer r10 = com.android.systemui.statusbar.phone.NotificationIconContainer.this
                int r10 = r10.mCannedAnimationStartIndex
                if (r7 <= r10) goto L_0x0119
                int r7 = r0.getVisibleState()
                if (r7 != r6) goto L_0x00fa
                int r7 = r11.visibleState
                if (r7 == r6) goto L_0x0119
            L_0x00fa:
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                com.android.systemui.statusbar.stack.AnimationFilter r2 = r2.getAnimationFilter()
                r2.reset()
                r2.animateX()
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.resetCustomInterpolators()
                com.android.systemui.statusbar.stack.AnimationProperties r2 = com.android.systemui.statusbar.phone.NotificationIconContainer.mTempProperties
                r2.setDuration(r8)
                r5 = r4
                goto L_0x0119
            L_0x0118:
                r5 = r1
            L_0x0119:
                int r6 = r11.visibleState
                r0.setVisibleState(r6, r3)
                int r6 = r11.iconColor
                boolean r7 = r11.needsCannedAnimation
                if (r7 == 0) goto L_0x0127
                if (r3 == 0) goto L_0x0127
                goto L_0x0128
            L_0x0127:
                r4 = r1
            L_0x0128:
                r0.setIconColor(r6, r4)
                if (r5 == 0) goto L_0x0131
                r11.animateTo(r0, r2)
                goto L_0x0134
            L_0x0131:
                super.applyToView(r12)
            L_0x0134:
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
