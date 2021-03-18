package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.AmbientState;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationIconContainer;

public class NotificationShelf extends ActivatableNotificationView implements View.OnLayoutChangeListener, StatusBarStateController.StateListener {
    private static final boolean ICON_ANMATIONS_WHILE_SCROLLING = SystemProperties.getBoolean("debug.icon_scroll_animations", true);
    private static final int TAG_CONTINUOUS_CLIPPING = C0015R$id.continuous_clipping_tag;
    private static final boolean USE_ANIMATIONS_WHEN_OPENING = SystemProperties.getBoolean("debug.icon_opening_animations", true);
    private AmbientState mAmbientState;
    private boolean mAnimationsEnabled = true;
    private final KeyguardBypassController mBypassController;
    private Rect mClipRect = new Rect();
    private NotificationIconContainer mCollapsedIcons;
    private int mCutoutHeight;
    private float mFirstElementRoundness;
    private int mGapHeight;
    private boolean mHasItemsInStableShelf;
    private float mHiddenShelfIconSize;
    private boolean mHideBackground;
    private NotificationStackScrollLayout mHostLayout;
    private int mIconAppearTopPadding;
    private int mIconSize;
    private boolean mInteractive;
    private int mMaxLayoutHeight;
    private boolean mNoAnimationsInThisFrame;
    private int mNotGoneIndex;
    private float mOpenedAmount;
    private int mPaddingBetweenElements;
    private int mRelativeOffset;
    private int mScrollFastThreshold;
    private NotificationIconContainer mShelfIcons;
    private boolean mShowNotificationShelf;
    private int mStatusBarHeight;
    private int mStatusBarState;
    private int[] mTmp = new int[2];

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean hasNoContentHeight() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean needsClippingToShelf() {
        return false;
    }

    public void setMaxShelfEnd(float f) {
    }

    public NotificationShelf(Context context, AttributeSet attributeSet, KeyguardBypassController keyguardBypassController) {
        super(context, attributeSet);
        this.mBypassController = keyguardBypassController;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    @VisibleForTesting
    public void onFinishInflate() {
        super.onFinishInflate();
        NotificationIconContainer notificationIconContainer = (NotificationIconContainer) findViewById(C0015R$id.content);
        this.mShelfIcons = notificationIconContainer;
        notificationIconContainer.setClipChildren(false);
        this.mShelfIcons.setClipToPadding(false);
        setClipToActualHeight(false);
        setClipChildren(false);
        setClipToPadding(false);
        this.mShelfIcons.setIsStaticLayout(false);
        setBottomRoundness(1.0f, false);
        setFirstInSection(true);
        initDimens();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this, 3);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback(this);
    }

    public void bind(AmbientState ambientState, NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mAmbientState = ambientState;
        this.mHostLayout = notificationStackScrollLayout;
    }

    private void initDimens() {
        Resources resources = getResources();
        this.mIconAppearTopPadding = resources.getDimensionPixelSize(C0012R$dimen.notification_icon_appear_padding);
        this.mStatusBarHeight = resources.getDimensionPixelOffset(C0012R$dimen.status_bar_height);
        resources.getDimensionPixelOffset(C0012R$dimen.status_bar_padding_start);
        this.mPaddingBetweenElements = resources.getDimensionPixelSize(C0012R$dimen.notification_divider_height);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = resources.getDimensionPixelOffset(C0012R$dimen.notification_shelf_height);
        setLayoutParams(layoutParams);
        int dimensionPixelOffset = resources.getDimensionPixelOffset(C0012R$dimen.shelf_icon_container_padding);
        this.mShelfIcons.setPadding(dimensionPixelOffset, 0, dimensionPixelOffset, 0);
        this.mScrollFastThreshold = resources.getDimensionPixelOffset(C0012R$dimen.scroll_fast_threshold);
        this.mShowNotificationShelf = resources.getBoolean(C0010R$bool.config_showNotificationShelf);
        this.mIconSize = resources.getDimensionPixelSize(17105492);
        this.mHiddenShelfIconSize = (float) resources.getDimensionPixelOffset(C0012R$dimen.hidden_shelf_icon_size);
        this.mGapHeight = resources.getDimensionPixelSize(C0012R$dimen.qs_notification_padding);
        if (!this.mShowNotificationShelf) {
            setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public View getContentView() {
        return this.mShelfIcons;
    }

    public NotificationIconContainer getShelfIcons() {
        return this.mShelfIcons;
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public ExpandableViewState createExpandableViewState() {
        return new ShelfState();
    }

    public void updateState(AmbientState ambientState) {
        ExpandableView lastVisibleBackgroundChild = ambientState.getLastVisibleBackgroundChild();
        ShelfState shelfState = (ShelfState) getViewState();
        boolean z = true;
        if (!this.mShowNotificationShelf || lastVisibleBackgroundChild == null) {
            shelfState.hidden = true;
            shelfState.location = 64;
            shelfState.hasItemsInStableShelf = false;
            return;
        }
        float innerHeight = ((float) ambientState.getInnerHeight()) + ambientState.getTopPadding() + ambientState.getStackTranslation();
        ExpandableViewState viewState = lastVisibleBackgroundChild.getViewState();
        float f = viewState.yTranslation + ((float) viewState.height);
        shelfState.copyFrom(viewState);
        shelfState.height = getIntrinsicHeight();
        shelfState.yTranslation = Math.max(Math.min(f, innerHeight) - ((float) shelfState.height), getFullyClosedTranslation());
        shelfState.zTranslation = (float) ambientState.getBaseZHeight();
        shelfState.openedAmount = Math.min(1.0f, (shelfState.yTranslation - getFullyClosedTranslation()) / ((float) ((getIntrinsicHeight() * 2) + this.mCutoutHeight)));
        shelfState.clipTopAmount = 0;
        shelfState.alpha = 1.0f;
        shelfState.belowSpeedBump = this.mAmbientState.getSpeedBumpIndex() == 0;
        shelfState.hideSensitive = false;
        shelfState.xTranslation = getTranslationX();
        int i = this.mNotGoneIndex;
        if (i != -1) {
            shelfState.notGoneIndex = Math.min(shelfState.notGoneIndex, i);
        }
        shelfState.hasItemsInStableShelf = viewState.inShelf;
        if (this.mAmbientState.isShadeExpanded() && !this.mAmbientState.isQsCustomizerShowing()) {
            z = false;
        }
        shelfState.hidden = z;
        shelfState.maxShelfEnd = innerHeight;
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x016e A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x018b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateAppearance() {
        /*
        // Method dump skipped, instructions count: 704
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.NotificationShelf.updateAppearance():void");
    }

    private void clipTransientViews() {
        for (int i = 0; i < this.mHostLayout.getTransientViewCount(); i++) {
            View transientView = this.mHostLayout.getTransientView(i);
            if (transientView instanceof ExpandableView) {
                updateNotificationClipHeight((ExpandableView) transientView, getTranslationY(), -1);
            }
        }
    }

    private void setFirstElementRoundness(float f) {
        if (this.mFirstElementRoundness != f) {
            this.mFirstElementRoundness = f;
            setTopRoundness(f, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateIconClipAmount(ExpandableNotificationRow expandableNotificationRow) {
        float translationY = expandableNotificationRow.getTranslationY();
        if (getClipTopAmount() != 0) {
            translationY = Math.max(translationY, getTranslationY() + ((float) getClipTopAmount()));
        }
        StatusBarIconView shelfIcon = expandableNotificationRow.getEntry().getIcons().getShelfIcon();
        float translationY2 = getTranslationY() + ((float) shelfIcon.getTop()) + shelfIcon.getTranslationY();
        if (translationY2 >= translationY || this.mAmbientState.isFullyHidden()) {
            shelfIcon.setClipBounds(null);
            return;
        }
        int i = (int) (translationY - translationY2);
        shelfIcon.setClipBounds(new Rect(0, i, shelfIcon.getWidth(), Math.max(i, shelfIcon.getHeight())));
    }

    private void updateContinuousClipping(final ExpandableNotificationRow expandableNotificationRow) {
        final StatusBarIconView shelfIcon = expandableNotificationRow.getEntry().getIcons().getShelfIcon();
        boolean z = true;
        boolean z2 = ViewState.isAnimatingY(shelfIcon) && !this.mAmbientState.isDozing();
        if (shelfIcon.getTag(TAG_CONTINUOUS_CLIPPING) == null) {
            z = false;
        }
        if (z2 && !z) {
            final ViewTreeObserver viewTreeObserver = shelfIcon.getViewTreeObserver();
            final AnonymousClass1 r2 = new ViewTreeObserver.OnPreDrawListener() {
                /* class com.android.systemui.statusbar.NotificationShelf.AnonymousClass1 */

                public boolean onPreDraw() {
                    if (!ViewState.isAnimatingY(shelfIcon)) {
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.removeOnPreDrawListener(this);
                        }
                        shelfIcon.setTag(NotificationShelf.TAG_CONTINUOUS_CLIPPING, null);
                        return true;
                    }
                    NotificationShelf.this.updateIconClipAmount(expandableNotificationRow);
                    return true;
                }
            };
            viewTreeObserver.addOnPreDrawListener(r2);
            shelfIcon.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this) {
                /* class com.android.systemui.statusbar.NotificationShelf.AnonymousClass2 */

                public void onViewAttachedToWindow(View view) {
                }

                public void onViewDetachedFromWindow(View view) {
                    if (view == shelfIcon) {
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.removeOnPreDrawListener(r2);
                        }
                        shelfIcon.setTag(NotificationShelf.TAG_CONTINUOUS_CLIPPING, null);
                    }
                }
            });
            shelfIcon.setTag(TAG_CONTINUOUS_CLIPPING, r2);
        }
    }

    private int updateNotificationClipHeight(ExpandableView expandableView, float f, int i) {
        float translationY = expandableView.getTranslationY() + ((float) expandableView.getActualHeight());
        boolean z = true;
        boolean z2 = (expandableView.isPinned() || expandableView.isHeadsUpAnimatingAway()) && !this.mAmbientState.isDozingAndNotPulsing(expandableView);
        if (!this.mAmbientState.isPulseExpanding()) {
            z = expandableView.showingPulsing();
        } else if (i != 0) {
            z = false;
        }
        if (translationY <= f || z || (!this.mAmbientState.isShadeExpanded() && z2)) {
            expandableView.setClipBottomAmount(0);
        } else {
            int i2 = (int) (translationY - f);
            if (z2) {
                i2 = Math.min(expandableView.getIntrinsicHeight() - expandableView.getCollapsedHeight(), i2);
            }
            expandableView.setClipBottomAmount(i2);
        }
        if (z) {
            return (int) (translationY - getTranslationY());
        }
        return 0;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setFakeShadowIntensity(float f, float f2, int i, int i2) {
        if (!this.mHasItemsInStableShelf) {
            f = 0.0f;
        }
        super.setFakeShadowIntensity(f, f2, i, i2);
    }

    private float updateShelfTransformation(ExpandableView expandableView, float f, boolean z, boolean z2, boolean z3, boolean z4) {
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float f7;
        NotificationIconContainer.IconState iconState = getIconState(expandableView.getShelfIcon());
        float translationY = expandableView.getTranslationY();
        int actualHeight = expandableView.getActualHeight() + this.mPaddingBetweenElements;
        float calculateIconTransformationStart = calculateIconTransformationStart(expandableView);
        float f8 = (float) actualHeight;
        float min = Math.min((f8 + translationY) - calculateIconTransformationStart, Math.min(((float) getIntrinsicHeight()) * 1.5f * NotificationUtils.interpolate(1.0f, 1.5f, f), f8));
        if (z4) {
            actualHeight = Math.min(actualHeight, expandableView.getMinHeight() - getIntrinsicHeight());
            min = Math.min(min, (float) (expandableView.getMinHeight() - getIntrinsicHeight()));
        }
        handleCustomTransformHeight(expandableView, z3, iconState);
        float translationY2 = getTranslationY();
        boolean z5 = true;
        float f9 = 0.0f;
        if (((float) actualHeight) + translationY < translationY2 || ((this.mAmbientState.isUnlockHintRunning() && !expandableView.isInShelf()) || (!this.mAmbientState.isShadeExpanded() && (expandableView.isPinned() || expandableView.isHeadsUpAnimatingAway())))) {
            f4 = min;
            f5 = 0.0f;
            f3 = 0.0f;
            f2 = 0.0f;
        } else {
            if (translationY < translationY2) {
                if (iconState != null && iconState.hasCustomTransformHeight()) {
                    actualHeight = iconState.customTransformHeight;
                    min = (float) actualHeight;
                }
                float f10 = translationY2 - translationY;
                float min2 = Math.min(1.0f, f10 / ((float) actualHeight));
                f7 = 1.0f - NotificationUtils.interpolate(Interpolators.ACCELERATE_DECELERATE.getInterpolation(min2), min2, f);
                f6 = 1.0f - MathUtils.constrain(z4 ? f10 / min : (translationY2 - calculateIconTransformationStart) / min, 0.0f, 1.0f);
                z5 = false;
            } else {
                f7 = 1.0f;
                f6 = 1.0f;
            }
            f2 = 1.0f - Math.min(1.0f, (translationY2 - translationY) / min);
            f4 = min;
            f3 = f7;
            f5 = f6;
        }
        if (iconState != null && z5 && !z3 && iconState.isLastExpandIcon) {
            iconState.isLastExpandIcon = false;
            iconState.customTransformHeight = Integer.MIN_VALUE;
        }
        if (!expandableView.isAboveShelf() && !expandableView.showingPulsing() && (z4 || iconState == null || iconState.translateContent)) {
            f9 = f2;
        }
        expandableView.setContentTransformationAmount(f9, z4);
        updateIconPositioning(expandableView, f5, f3, f4, z, z2, z3, z4);
        return f3;
    }

    private float calculateIconTransformationStart(ExpandableView expandableView) {
        View shelfTransformationTarget = expandableView.getShelfTransformationTarget();
        if (shelfTransformationTarget == null) {
            return expandableView.getTranslationY();
        }
        return (expandableView.getTranslationY() + ((float) expandableView.getRelativeTopPadding(shelfTransformationTarget))) - ((float) expandableView.getShelfIcon().getTop());
    }

    private void handleCustomTransformHeight(ExpandableView expandableView, boolean z, NotificationIconContainer.IconState iconState) {
        if (iconState != null && z && this.mAmbientState.getScrollY() == 0 && !this.mAmbientState.isOnKeyguard() && !iconState.isLastExpandIcon) {
            float intrinsicPadding = (float) (this.mAmbientState.getIntrinsicPadding() + this.mHostLayout.getPositionInLinearLayout(expandableView));
            float intrinsicHeight = (float) (this.mMaxLayoutHeight - getIntrinsicHeight());
            if (intrinsicPadding < intrinsicHeight && ((float) expandableView.getIntrinsicHeight()) + intrinsicPadding >= intrinsicHeight && expandableView.getTranslationY() < intrinsicPadding) {
                boolean z2 = true;
                iconState.isLastExpandIcon = true;
                iconState.customTransformHeight = Integer.MIN_VALUE;
                if (((float) (this.mMaxLayoutHeight - getIntrinsicHeight())) - intrinsicPadding >= ((float) getIntrinsicHeight())) {
                    z2 = false;
                }
                if (!z2) {
                    iconState.customTransformHeight = (int) (((float) (this.mMaxLayoutHeight - getIntrinsicHeight())) - intrinsicPadding);
                }
            }
        }
    }

    private void updateIconPositioning(ExpandableView expandableView, float f, float f2, float f3, boolean z, boolean z2, boolean z3, boolean z4) {
        StatusBarIconView shelfIcon = expandableView.getShelfIcon();
        NotificationIconContainer.IconState iconState = getIconState(shelfIcon);
        if (iconState != null) {
            boolean z5 = false;
            boolean z6 = iconState.isLastExpandIcon && !iconState.hasCustomTransformHeight();
            float f4 = 1.0f;
            float f5 = (f > 0.5f ? 1 : (f == 0.5f ? 0 : -1)) > 0 || isTargetClipped(expandableView) ? 1.0f : 0.0f;
            if (f == f5) {
                boolean z7 = (z2 || z3) && !z6;
                iconState.noAnimations = z7;
                iconState.useFullTransitionAmount = z7 || (!ICON_ANMATIONS_WHILE_SCROLLING && f == 0.0f && z);
                iconState.useLinearTransitionAmount = !ICON_ANMATIONS_WHILE_SCROLLING && f == 0.0f && !this.mAmbientState.isExpansionChanging();
                iconState.translateContent = (((float) this.mMaxLayoutHeight) - getTranslationY()) - ((float) getIntrinsicHeight()) > 0.0f;
            }
            if (!z6 && (z2 || (z3 && iconState.useFullTransitionAmount && !ViewState.isAnimatingY(shelfIcon)))) {
                iconState.cancelAnimations(shelfIcon);
                iconState.useFullTransitionAmount = true;
                iconState.noAnimations = true;
            }
            if (iconState.hasCustomTransformHeight()) {
                iconState.useFullTransitionAmount = true;
            }
            if (iconState.isLastExpandIcon) {
                iconState.translateContent = false;
            }
            if (!this.mAmbientState.isHiddenAtAll() || expandableView.isInShelf()) {
                if (z4 || !USE_ANIMATIONS_WHEN_OPENING || iconState.useFullTransitionAmount || iconState.useLinearTransitionAmount) {
                    f4 = f;
                } else {
                    iconState.needsCannedAnimation = iconState.clampedAppearAmount != f5 && !this.mNoAnimationsInThisFrame;
                    f4 = f5;
                }
            } else if (!this.mAmbientState.isFullyHidden()) {
                f4 = 0.0f;
            }
            iconState.iconAppearAmount = (!USE_ANIMATIONS_WHEN_OPENING || iconState.useFullTransitionAmount) ? f2 : f4;
            iconState.clampedAppearAmount = f5;
            if (f5 != f4) {
                z5 = true;
            }
            setIconTransformationAmount(expandableView, f4, f3, z5, z4);
        }
    }

    private boolean isTargetClipped(ExpandableView expandableView) {
        View shelfTransformationTarget = expandableView.getShelfTransformationTarget();
        if (shelfTransformationTarget != null && expandableView.getTranslationY() + expandableView.getContentTranslation() + ((float) expandableView.getRelativeTopPadding(shelfTransformationTarget)) + ((float) shelfTransformationTarget.getHeight()) >= getTranslationY() - ((float) this.mPaddingBetweenElements)) {
            return true;
        }
        return false;
    }

    private void setIconTransformationAmount(ExpandableView expandableView, float f, float f2, boolean z, boolean z2) {
        int i;
        int i2;
        float f3;
        float f4;
        if (expandableView instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            StatusBarIconView shelfIcon = expandableNotificationRow.getShelfIcon();
            NotificationIconContainer.IconState iconState = getIconState(shelfIcon);
            View shelfTransformationTarget = expandableNotificationRow.getShelfTransformationTarget();
            if (shelfTransformationTarget != null) {
                i2 = expandableNotificationRow.getRelativeTopPadding(shelfTransformationTarget);
                i = expandableNotificationRow.getRelativeStartPadding(shelfTransformationTarget);
                f3 = (float) shelfTransformationTarget.getHeight();
            } else {
                i2 = this.mIconAppearTopPadding;
                f3 = 0.0f;
                i = 0;
            }
            float iconScale = (this.mAmbientState.isFullyHidden() ? this.mHiddenShelfIconSize : (float) this.mIconSize) * shelfIcon.getIconScale();
            float translationY = expandableNotificationRow.getTranslationY() + expandableNotificationRow.getContentTranslation();
            boolean z3 = expandableNotificationRow.isInShelf() && !expandableNotificationRow.isTransformingIntoShelf();
            float interpolate = NotificationUtils.interpolate((translationY + ((float) i2)) - ((getTranslationY() + ((float) shelfIcon.getTop())) + ((((float) shelfIcon.getHeight()) - iconScale) / 2.0f)), (!z || z3) ? 0.0f : NotificationUtils.interpolate(Math.min((((float) this.mIconAppearTopPadding) + translationY) - getTranslationY(), 0.0f), 0.0f, f), f);
            float interpolate2 = NotificationUtils.interpolate(((float) i) - (((float) shelfIcon.getLeft()) + (((1.0f - shelfIcon.getIconScale()) * ((float) shelfIcon.getWidth())) / 2.0f)), this.mShelfIcons.getActualPaddingStart(), f);
            boolean z4 = !expandableNotificationRow.isShowingIcon();
            if (z4) {
                f3 = iconScale / 2.0f;
                interpolate2 = this.mShelfIcons.getActualPaddingStart();
                f4 = f;
            } else {
                f4 = 1.0f;
            }
            float interpolate3 = NotificationUtils.interpolate(f3, iconScale, f);
            if (iconState != null) {
                float f5 = interpolate3 / iconScale;
                iconState.scaleX = f5;
                iconState.scaleY = f5;
                iconState.hidden = f == 0.0f && !iconState.isAnimating(shelfIcon);
                if (expandableNotificationRow.isDrawingAppearAnimation() && !expandableNotificationRow.isInShelf()) {
                    iconState.hidden = true;
                    iconState.iconAppearAmount = 0.0f;
                }
                iconState.alpha = f4;
                iconState.yTranslation = interpolate;
                iconState.xTranslation = interpolate2;
                if (z3) {
                    iconState.iconAppearAmount = 1.0f;
                    iconState.alpha = 1.0f;
                    iconState.scaleX = 1.0f;
                    iconState.scaleY = 1.0f;
                    iconState.hidden = false;
                }
                if (expandableNotificationRow.isAboveShelf() || expandableNotificationRow.showingPulsing() || (!expandableNotificationRow.isInShelf() && ((z2 && expandableNotificationRow.areGutsExposed()) || expandableNotificationRow.getTranslationZ() > ((float) this.mAmbientState.getBaseZHeight())))) {
                    iconState.hidden = true;
                }
                int contrastedStaticDrawableColor = shelfIcon.getContrastedStaticDrawableColor(getBackgroundColorWithoutTint());
                if (!z4 && contrastedStaticDrawableColor != 0) {
                    contrastedStaticDrawableColor = NotificationUtils.interpolateColors(expandableNotificationRow.getOriginalIconColor(), contrastedStaticDrawableColor, iconState.iconAppearAmount);
                }
                iconState.iconColor = contrastedStaticDrawableColor;
            }
        }
    }

    private NotificationIconContainer.IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mShelfIcons.getIconState(statusBarIconView);
    }

    private float getFullyClosedTranslation() {
        return (float) ((-(getIntrinsicHeight() - this.mStatusBarHeight)) / 2);
    }

    private void setHideBackground(boolean z) {
        if (this.mHideBackground != z) {
            this.mHideBackground = z;
            updateBackground();
            updateOutline();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView
    public boolean needsOutline() {
        return !this.mHideBackground && super.needsOutline();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public boolean shouldHideBackground() {
        return super.shouldHideBackground() || this.mHideBackground;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateRelativeOffset();
        int i5 = getResources().getDisplayMetrics().heightPixels;
        this.mClipRect.set(0, -i5, getWidth(), i5);
        this.mShelfIcons.setClipBounds(this.mClipRect);
    }

    private void updateRelativeOffset() {
        this.mCollapsedIcons.getLocationOnScreen(this.mTmp);
        int[] iArr = this.mTmp;
        this.mRelativeOffset = iArr[0];
        getLocationOnScreen(iArr);
        this.mRelativeOffset -= this.mTmp[0];
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        WindowInsets onApplyWindowInsets = super.onApplyWindowInsets(windowInsets);
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        this.mCutoutHeight = (displayCutout == null || displayCutout.getSafeInsetTop() < 0) ? 0 : displayCutout.getSafeInsetTop();
        return onApplyWindowInsets;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setOpenedAmount(float f) {
        int i;
        this.mNoAnimationsInThisFrame = f == 1.0f && this.mOpenedAmount == 0.0f;
        this.mOpenedAmount = f;
        if (!this.mAmbientState.isPanelFullWidth() || this.mAmbientState.isDozing()) {
            f = 1.0f;
        }
        int i2 = this.mRelativeOffset;
        if (isLayoutRtl()) {
            i2 = (getWidth() - i2) - this.mCollapsedIcons.getWidth();
        }
        this.mShelfIcons.setActualLayoutWidth((int) NotificationUtils.interpolate((float) (this.mCollapsedIcons.getFinalTranslationX() + i2), (float) this.mShelfIcons.getWidth(), Interpolators.FAST_OUT_SLOW_IN_REVERSE.getInterpolation(f)));
        boolean hasOverflow = this.mCollapsedIcons.hasOverflow();
        int paddingEnd = this.mCollapsedIcons.getPaddingEnd();
        if (!hasOverflow) {
            i = this.mCollapsedIcons.getNoOverflowExtraPadding();
        } else {
            i = this.mCollapsedIcons.getPartialOverflowExtraPadding();
        }
        this.mShelfIcons.setActualPaddingEnd(NotificationUtils.interpolate((float) (paddingEnd - i), (float) this.mShelfIcons.getPaddingEnd(), f));
        this.mShelfIcons.setActualPaddingStart(NotificationUtils.interpolate((float) i2, (float) this.mShelfIcons.getPaddingStart(), f));
        this.mShelfIcons.setOpenedAmount(f);
    }

    public void setMaxLayoutHeight(int i) {
        this.mMaxLayoutHeight = i;
    }

    public int getNotGoneIndex() {
        return this.mNotGoneIndex;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setHasItemsInStableShelf(boolean z) {
        if (this.mHasItemsInStableShelf != z) {
            this.mHasItemsInStableShelf = z;
            updateInteractiveness();
        }
    }

    public void setCollapsedIcons(NotificationIconContainer notificationIconContainer) {
        this.mCollapsedIcons = notificationIconContainer;
        notificationIconContainer.addOnLayoutChangeListener(this);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.mStatusBarState = i;
        updateInteractiveness();
    }

    private void updateInteractiveness() {
        int i = 1;
        boolean z = this.mStatusBarState == 1 && this.mHasItemsInStableShelf;
        this.mInteractive = z;
        setClickable(z);
        setFocusable(this.mInteractive);
        if (!this.mInteractive) {
            i = 4;
        }
        setImportantForAccessibility(i);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public boolean isInteractive() {
        return this.mInteractive;
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        if (!z) {
            this.mShelfIcons.setAnimationsEnabled(false);
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mInteractive) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, getContext().getString(C0021R$string.accessibility_overflow_action)));
        }
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateRelativeOffset();
    }

    public void onUiModeChanged() {
        updateBackgroundColors();
    }

    /* access modifiers changed from: private */
    public class ShelfState extends ExpandableViewState {
        private boolean hasItemsInStableShelf;
        private float maxShelfEnd;
        private float openedAmount;

        private ShelfState() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState, com.android.systemui.statusbar.notification.stack.ExpandableViewState
        public void applyToView(View view) {
            if (NotificationShelf.this.mShowNotificationShelf) {
                super.applyToView(view);
                NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
                NotificationShelf.this.setOpenedAmount(this.openedAmount);
                NotificationShelf.this.updateAppearance();
                NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
                NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
            }
        }

        @Override // com.android.systemui.statusbar.notification.stack.ViewState, com.android.systemui.statusbar.notification.stack.MiuiViewStateBase, com.android.systemui.statusbar.notification.stack.ExpandableViewState
        public void animateTo(View view, AnimationProperties animationProperties) {
            if (NotificationShelf.this.mShowNotificationShelf) {
                super.animateTo(view, animationProperties);
                NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
                NotificationShelf.this.setOpenedAmount(this.openedAmount);
                NotificationShelf.this.updateAppearance();
                NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
                NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
            }
        }
    }
}
