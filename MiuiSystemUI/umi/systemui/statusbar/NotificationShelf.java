package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import com.android.systemui.Interpolators;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.NotificationIconContainer;
import com.android.systemui.statusbar.stack.AmbientState;
import com.android.systemui.statusbar.stack.AnimationProperties;
import com.android.systemui.statusbar.stack.ExpandableViewState;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.StackScrollState;
import com.android.systemui.statusbar.stack.ViewState;

public class NotificationShelf extends ActivatableNotificationView implements View.OnLayoutChangeListener {
    private static final boolean ICON_ANMATIONS_WHILE_SCROLLING = SystemProperties.getBoolean("debug.icon_scroll_animations", true);
    private static final boolean USE_ANIMATIONS_WHEN_OPENING = SystemProperties.getBoolean("debug.icon_opening_animations", true);
    private AmbientState mAmbientState;
    /* access modifiers changed from: private */
    public boolean mAnimationsEnabled = true;
    private NotificationIconContainer mCollapsedIcons;
    private boolean mDark;
    private boolean mHasItemsInStableShelf;
    private boolean mHideBackground;
    private NotificationStackScrollLayout mHostLayout;
    private int mIconAppearTopPadding;
    private boolean mInteractive;
    private int mMaxLayoutHeight;
    private boolean mNoAnimationsInThisFrame;
    private int mNotGoneIndex;
    private float mOpenedAmount;
    private int mPaddingBetweenElements;
    private int mRelativeOffset;
    private int mScrollFastThreshold;
    /* access modifiers changed from: private */
    public NotificationIconContainer mShelfIcons;
    private ShelfState mShelfState;
    private int mStatusBarHeight;
    private int mStatusBarState;
    private int[] mTmp = new int[2];

    public boolean hasNoContentHeight() {
        return true;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setMaxShelfEnd(float f) {
    }

    public NotificationShelf(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        NotificationIconContainer notificationIconContainer = (NotificationIconContainer) findViewById(R.id.content);
        this.mShelfIcons = notificationIconContainer;
        notificationIconContainer.setClipChildren(false);
        this.mShelfIcons.setClipToPadding(false);
        setClipToActualHeight(false);
        setClipChildren(false);
        setClipToPadding(false);
        this.mShelfIcons.setShowAllIcons(false);
        new ViewInvertHelper((View) this.mShelfIcons, 700);
        initDimens();
        this.mShelfState = (ShelfState) getViewState();
    }

    public void bind(AmbientState ambientState, NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mAmbientState = ambientState;
        this.mHostLayout = notificationStackScrollLayout;
    }

    private void initDimens() {
        this.mIconAppearTopPadding = getResources().getDimensionPixelSize(R.dimen.notification_icon_appear_padding);
        this.mStatusBarHeight = getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
        getResources().getDimensionPixelOffset(R.dimen.status_bar_padding_start);
        this.mPaddingBetweenElements = getResources().getDimensionPixelSize(R.dimen.notification_divider_height);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelOffset(R.dimen.notification_shelf_height);
        setLayoutParams(layoutParams);
        int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.shelf_icon_container_padding);
        this.mShelfIcons.setPadding(dimensionPixelOffset, 0, dimensionPixelOffset, 0);
        this.mScrollFastThreshold = getResources().getDimensionPixelOffset(R.dimen.scroll_fast_threshold);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        if (this.mDark != z) {
            this.mDark = z;
            this.mShelfIcons.setDark(z, z2, j);
            updateInteractiveness();
        }
    }

    /* access modifiers changed from: protected */
    public View getContentView() {
        return this.mShelfIcons;
    }

    public NotificationIconContainer getShelfIcons() {
        return this.mShelfIcons;
    }

    public ExpandableViewState createExpandableViewState() {
        return new ShelfState();
    }

    public void updateState(StackScrollState stackScrollState, AmbientState ambientState) {
        ExpandableView lastVisibleBackgroundChild = ambientState.getLastVisibleBackgroundChild();
        if (lastVisibleBackgroundChild != null) {
            float innerHeight = ((float) ambientState.getInnerHeight()) + ambientState.getTopPadding() + ambientState.getStackTranslation();
            ExpandableViewState viewStateForView = stackScrollState.getViewStateForView(lastVisibleBackgroundChild);
            float f = viewStateForView.yTranslation + ((float) viewStateForView.height);
            this.mShelfState.copyFrom(viewStateForView);
            this.mShelfState.height = getIntrinsicHeight();
            this.mShelfState.yTranslation = Math.max(Math.min(f, innerHeight) - ((float) this.mShelfState.height), getFullyClosedTranslation());
            this.mShelfState.zTranslation = (float) ambientState.getBaseZHeight();
            float unused = this.mShelfState.openedAmount = Math.min(1.0f, (this.mShelfState.yTranslation - getFullyClosedTranslation()) / ((float) (getIntrinsicHeight() * 2)));
            ShelfState shelfState = this.mShelfState;
            shelfState.clipTopAmount = 0;
            shelfState.alpha = this.mAmbientState.hasPulsingNotifications() ? 0.0f : 1.0f;
            this.mShelfState.belowSpeedBump = this.mAmbientState.getSpeedBumpIndex() == 0;
            ShelfState shelfState2 = this.mShelfState;
            shelfState2.shadowAlpha = 1.0f;
            shelfState2.hideSensitive = false;
            shelfState2.xTranslation = getTranslationX();
            int i = this.mNotGoneIndex;
            if (i != -1) {
                ShelfState shelfState3 = this.mShelfState;
                shelfState3.notGoneIndex = Math.min(shelfState3.notGoneIndex, i);
            }
            boolean unused2 = this.mShelfState.hasItemsInStableShelf = viewStateForView.inShelf;
            this.mShelfState.hidden = !this.mAmbientState.isShadeExpanded();
            float unused3 = this.mShelfState.maxShelfEnd = innerHeight;
        } else {
            ShelfState shelfState4 = this.mShelfState;
            shelfState4.hidden = true;
            shelfState4.location = 64;
            boolean unused4 = shelfState4.hasItemsInStableShelf = false;
        }
        this.mShelfState.hidden = true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:65:0x015d  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x016b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateAppearance() {
        /*
            r28 = this;
            r7 = r28
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            r0.resetViewStates()
            float r8 = r28.getTranslationY()
            com.android.systemui.statusbar.stack.AmbientState r0 = r7.mAmbientState
            com.android.systemui.statusbar.ExpandableView r9 = r0.getLastVisibleBackgroundChild()
            r10 = -1
            r7.mNotGoneIndex = r10
            int r0 = r7.mMaxLayoutHeight
            int r1 = r28.getIntrinsicHeight()
            int r1 = r1 * 2
            int r0 = r0 - r1
            float r0 = (float) r0
            int r1 = (r8 > r0 ? 1 : (r8 == r0 ? 0 : -1))
            r11 = 1065353216(0x3f800000, float:1.0)
            r12 = 0
            if (r1 < 0) goto L_0x0033
            float r0 = r8 - r0
            int r1 = r28.getIntrinsicHeight()
            float r1 = (float) r1
            float r0 = r0 / r1
            float r0 = java.lang.Math.min(r11, r0)
            r13 = r0
            goto L_0x0034
        L_0x0033:
            r13 = r12
        L_0x0034:
            boolean r0 = r7.mHideBackground
            if (r0 == 0) goto L_0x0043
            com.android.systemui.statusbar.NotificationShelf$ShelfState r0 = r7.mShelfState
            boolean r0 = r0.hasItemsInStableShelf
            if (r0 != 0) goto L_0x0043
            r16 = 1
            goto L_0x0045
        L_0x0043:
            r16 = 0
        L_0x0045:
            com.android.systemui.statusbar.stack.AmbientState r0 = r7.mAmbientState
            float r0 = r0.getCurrentScrollVelocity()
            int r1 = r7.mScrollFastThreshold
            float r1 = (float) r1
            int r1 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r1 > 0) goto L_0x006f
            com.android.systemui.statusbar.stack.AmbientState r1 = r7.mAmbientState
            boolean r1 = r1.isExpansionChanging()
            if (r1 == 0) goto L_0x006c
            com.android.systemui.statusbar.stack.AmbientState r1 = r7.mAmbientState
            float r1 = r1.getExpandingVelocity()
            float r1 = java.lang.Math.abs(r1)
            int r2 = r7.mScrollFastThreshold
            float r2 = (float) r2
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 <= 0) goto L_0x006c
            goto L_0x006f
        L_0x006c:
            r17 = 0
            goto L_0x0071
        L_0x006f:
            r17 = 1
        L_0x0071:
            int r0 = (r0 > r12 ? 1 : (r0 == r12 ? 0 : -1))
            if (r0 <= 0) goto L_0x0078
            r18 = 1
            goto L_0x007a
        L_0x0078:
            r18 = 0
        L_0x007a:
            com.android.systemui.statusbar.stack.AmbientState r0 = r7.mAmbientState
            boolean r0 = r0.isExpansionChanging()
            if (r0 == 0) goto L_0x008d
            com.android.systemui.statusbar.stack.AmbientState r0 = r7.mAmbientState
            boolean r0 = r0.isPanelTracking()
            if (r0 != 0) goto L_0x008d
            r19 = 1
            goto L_0x008f
        L_0x008d:
            r19 = 0
        L_0x008f:
            com.android.systemui.statusbar.stack.AmbientState r0 = r7.mAmbientState
            int r6 = r0.getBaseZHeight()
            r2 = r12
            r20 = r2
            r0 = 0
            r3 = 0
            r4 = 0
            r5 = 0
            r21 = 0
        L_0x009e:
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r1 = r7.mHostLayout
            int r1 = r1.getChildCount()
            if (r0 >= r1) goto L_0x0184
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r1 = r7.mHostLayout
            int r22 = r0 + 1
            android.view.View r0 = r1.getChildAt(r0)
            com.android.systemui.statusbar.ExpandableView r0 = (com.android.systemui.statusbar.ExpandableView) r0
            int r1 = r0.getVisibility()
            r14 = 8
            if (r1 != r14) goto L_0x00b9
            goto L_0x00cf
        L_0x00b9:
            int r1 = r0.getViewType()
            r14 = 11
            if (r1 != r14) goto L_0x00cd
            int r1 = r28.getIntrinsicHeight()
            float r1 = (float) r1
            float r1 = r1 + r8
            r7.updateExpandableViewClipHeight(r0, r1)
            int r5 = r5 + 1
            goto L_0x00cf
        L_0x00cd:
            if (r1 == 0) goto L_0x00d2
        L_0x00cf:
            r0 = r22
            goto L_0x009e
        L_0x00d2:
            r14 = r0
            com.android.systemui.statusbar.ExpandableNotificationRow r14 = (com.android.systemui.statusbar.ExpandableNotificationRow) r14
            float r1 = com.android.systemui.statusbar.stack.ViewState.getFinalTranslationZ(r14)
            float r11 = (float) r6
            int r1 = (r1 > r11 ? 1 : (r1 == r11 ? 0 : -1))
            if (r1 <= 0) goto L_0x00e0
            r11 = 1
            goto L_0x00e1
        L_0x00e0:
            r11 = 0
        L_0x00e1:
            if (r0 != r9) goto L_0x00e6
            r23 = 1
            goto L_0x00e8
        L_0x00e6:
            r23 = 0
        L_0x00e8:
            float r24 = r14.getTranslationY()
            if (r23 != 0) goto L_0x0119
            if (r11 != 0) goto L_0x0119
            if (r16 == 0) goto L_0x00f3
            goto L_0x0119
        L_0x00f3:
            int r0 = r28.getIntrinsicHeight()
            float r0 = (float) r0
            float r0 = r0 + r8
            int r1 = r7.mPaddingBetweenElements
            float r1 = (float) r1
            float r0 = r0 - r1
            float r0 = r0 - r24
            boolean r1 = r14.isBelowSpeedBump()
            if (r1 != 0) goto L_0x011c
            int r1 = r28.getNotificationMergeSize()
            float r1 = (float) r1
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 > 0) goto L_0x011c
            int r0 = r28.getNotificationMergeSize()
            float r0 = (float) r0
            float r0 = r24 + r0
            java.lang.Math.min(r8, r0)
            goto L_0x011c
        L_0x0119:
            r28.getIntrinsicHeight()
        L_0x011c:
            r0 = r28
            r1 = r14
            r12 = r2
            r2 = r13
            r15 = r3
            r3 = r18
            r25 = r4
            r4 = r17
            r26 = r5
            r5 = r19
            r27 = r6
            r6 = r23
            float r0 = r0.updateIconAppearance(r1, r2, r3, r4, r5, r6)
            float r20 = r20 + r0
            int r3 = r14.getBackgroundColorWithoutTint()
            int r1 = (r24 > r8 ? 1 : (r24 == r8 ? 0 : -1))
            if (r1 < 0) goto L_0x014f
            int r1 = r7.mNotGoneIndex
            if (r1 != r10) goto L_0x014f
            r5 = r26
            r7.mNotGoneIndex = r5
            r7.setTintColor(r15)
            r4 = r25
            r7.setOverrideTintColor(r4, r12)
            goto L_0x015a
        L_0x014f:
            r4 = r25
            r5 = r26
            int r1 = r7.mNotGoneIndex
            if (r1 != r10) goto L_0x015a
            r2 = r0
            r4 = r15
            goto L_0x015b
        L_0x015a:
            r2 = r12
        L_0x015b:
            if (r23 == 0) goto L_0x016b
            if (r21 != 0) goto L_0x0161
            r1 = r3
            goto L_0x0163
        L_0x0161:
            r1 = r21
        L_0x0163:
            r14.setOverrideTintColor(r1, r0)
            r21 = r1
            r0 = 0
            r1 = 0
            goto L_0x0172
        L_0x016b:
            r0 = 0
            r1 = 0
            r14.setOverrideTintColor(r1, r0)
            r21 = r3
        L_0x0172:
            if (r5 != 0) goto L_0x0176
            if (r11 != 0) goto L_0x0179
        L_0x0176:
            r14.setAboveShelf(r1)
        L_0x0179:
            int r5 = r5 + 1
            r12 = r0
            r0 = r22
            r6 = r27
            r11 = 1065353216(0x3f800000, float:1.0)
            goto L_0x009e
        L_0x0184:
            r1 = 0
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            com.android.systemui.statusbar.stack.AmbientState r2 = r7.mAmbientState
            int r2 = r2.getSpeedBumpIndex()
            r0.setSpeedBumpIndex(r2)
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            r0.calculateIconTranslations()
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            r0.applyIconStates()
            r0 = 1065353216(0x3f800000, float:1.0)
            int r0 = (r20 > r0 ? 1 : (r20 == r0 ? 0 : -1))
            if (r0 >= 0) goto L_0x01a2
            r0 = 1
            goto L_0x01a3
        L_0x01a2:
            r0 = r1
        L_0x01a3:
            if (r0 != 0) goto L_0x01aa
            if (r16 == 0) goto L_0x01a8
            goto L_0x01aa
        L_0x01a8:
            r14 = r1
            goto L_0x01ab
        L_0x01aa:
            r14 = 1
        L_0x01ab:
            r7.setHideBackground(r14)
            int r0 = r7.mNotGoneIndex
            if (r0 != r10) goto L_0x01b4
            r7.mNotGoneIndex = r5
        L_0x01b4:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.NotificationShelf.updateAppearance():void");
    }

    private void updateExpandableViewClipHeight(ExpandableView expandableView, float f) {
        float translationY = expandableView.getTranslationY() + ((float) expandableView.getActualHeight());
        if (translationY <= f || !this.mAmbientState.isShadeExpanded()) {
            expandableView.setClipBottomAmount(0);
        } else {
            expandableView.setClipBottomAmount((int) (translationY - f));
        }
    }

    private float updateIconAppearance(ExpandableNotificationRow expandableNotificationRow, float f, boolean z, boolean z2, boolean z3, boolean z4) {
        float f2;
        float f3;
        float f4 = f;
        float translationY = expandableNotificationRow.getTranslationY();
        int actualHeight = expandableNotificationRow.getActualHeight() + this.mPaddingBetweenElements;
        float intrinsicHeight = ((float) getIntrinsicHeight()) * 1.5f * NotificationUtils.interpolate(1.0f, 1.5f, f);
        if (z4) {
            actualHeight = Math.min(actualHeight, expandableNotificationRow.getMinHeight() - getIntrinsicHeight());
            intrinsicHeight = Math.min(intrinsicHeight, (float) (expandableNotificationRow.getMinHeight() - getIntrinsicHeight()));
        }
        float f5 = (float) actualHeight;
        float translationY2 = getTranslationY();
        if (translationY + f5 < translationY2 || ((this.mAmbientState.isUnlockHintRunning() && !expandableNotificationRow.isInShelf()) || (!this.mAmbientState.isShadeExpanded() && (expandableNotificationRow.isPinned() || expandableNotificationRow.isHeadsUpAnimatingAway())))) {
            f3 = 0.0f;
        } else if (translationY < translationY2) {
            float f6 = translationY2 - translationY;
            float f7 = f6 / f5;
            f2 = 1.0f - NotificationUtils.interpolate(Interpolators.ACCELERATE_DECELERATE.getInterpolation(f7), f7, f);
            f3 = 1.0f - Math.min(1.0f, f6 / intrinsicHeight);
            updateIconPositioning(expandableNotificationRow, f3, f2, intrinsicHeight, z, z2, z3, z4);
            return f2;
        } else {
            f3 = 1.0f;
        }
        f2 = f3;
        updateIconPositioning(expandableNotificationRow, f3, f2, intrinsicHeight, z, z2, z3, z4);
        return f2;
    }

    private void updateIconPositioning(ExpandableNotificationRow expandableNotificationRow, float f, float f2, float f3, boolean z, boolean z2, boolean z3, boolean z4) {
        float f4;
        StatusBarIconView statusBarIconView = expandableNotificationRow.getEntry().expandedIcon;
        NotificationIconContainer.IconState iconState = getIconState(statusBarIconView);
        if (iconState != null) {
            float f5 = f > 0.5f ? 1.0f : 0.0f;
            if (f5 == f2) {
                boolean z5 = z2 || z3;
                iconState.noAnimations = z5;
                iconState.useFullTransitionAmount = z5 || (!ICON_ANMATIONS_WHILE_SCROLLING && f2 == 0.0f && z);
                iconState.useLinearTransitionAmount = !ICON_ANMATIONS_WHILE_SCROLLING && f2 == 0.0f && !this.mAmbientState.isExpansionChanging();
                iconState.translateContent = (((float) this.mMaxLayoutHeight) - getTranslationY()) - ((float) getIntrinsicHeight()) > 0.0f;
            }
            if (z2 || (z3 && iconState.useFullTransitionAmount && !ViewState.isAnimatingY(statusBarIconView))) {
                iconState.cancelAnimations(statusBarIconView);
                iconState.useFullTransitionAmount = true;
                iconState.noAnimations = true;
            }
            if (z4 || !USE_ANIMATIONS_WHEN_OPENING || iconState.useFullTransitionAmount || iconState.useLinearTransitionAmount) {
                f4 = f;
            } else {
                iconState.needsCannedAnimation = iconState.clampedAppearAmount != f5 && !this.mNoAnimationsInThisFrame;
                f4 = f5;
            }
            iconState.iconAppearAmount = (!USE_ANIMATIONS_WHEN_OPENING || iconState.useFullTransitionAmount) ? f2 : f4;
            iconState.clampedAppearAmount = f5;
            if (!expandableNotificationRow.isAboveShelf() && !z4) {
                boolean z6 = iconState.translateContent;
            }
            ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
            expandableNotificationRow.setContentTransformationAmount(0.0f, false);
            setIconTransformationAmount(expandableNotificationRow, f4, f3, f5 != f4, z4);
        }
    }

    private void setIconTransformationAmount(ExpandableNotificationRow expandableNotificationRow, float f, float f2, boolean z, boolean z2) {
        int i;
        StatusBarIconView statusBarIconView = expandableNotificationRow.getEntry().expandedIcon;
        NotificationIconContainer.IconState iconState = getIconState(statusBarIconView);
        View notificationIcon = expandableNotificationRow.getNotificationIcon();
        float translationY = expandableNotificationRow.getTranslationY() + expandableNotificationRow.getContentTranslation();
        boolean z3 = expandableNotificationRow.isInShelf() && !expandableNotificationRow.isTransformingIntoShelf();
        if (z && !z3) {
            translationY = getTranslationY() - f2;
        }
        if (notificationIcon != null) {
            i = expandableNotificationRow.getRelativeTopPadding(notificationIcon);
        } else {
            i = this.mIconAppearTopPadding;
        }
        float interpolate = NotificationUtils.interpolate((translationY + ((float) i)) - ((getTranslationY() + ((float) statusBarIconView.getTop())) + (((1.0f - statusBarIconView.getIconScale()) * ((float) statusBarIconView.getHeight())) / 2.0f)), 0.0f, f);
        if (!(!expandableNotificationRow.isShowingIcon())) {
            f = 1.0f;
        }
        if (iconState != null) {
            iconState.scaleX = 1.0f;
            iconState.scaleY = 1.0f;
            iconState.hidden = true;
            iconState.alpha = f;
            iconState.yTranslation = interpolate;
            if (z3) {
                iconState.iconAppearAmount = 1.0f;
                iconState.alpha = 1.0f;
                iconState.scaleX = 1.0f;
                iconState.scaleY = 1.0f;
                iconState.hidden = false;
            }
            if (expandableNotificationRow.isAboveShelf() || (!expandableNotificationRow.isInShelf() && ((z2 && expandableNotificationRow.areGutsExposed()) || expandableNotificationRow.getTranslationZ() > ((float) this.mAmbientState.getBaseZHeight())))) {
                iconState.hidden = true;
            }
            iconState.iconColor = 0;
        }
    }

    private NotificationIconContainer.IconState getIconState(StatusBarIconView statusBarIconView) {
        return this.mShelfIcons.getIconState(statusBarIconView);
    }

    private float getFullyClosedTranslation() {
        return (float) ((-(getIntrinsicHeight() - this.mStatusBarHeight)) / 2);
    }

    public int getNotificationMergeSize() {
        return getIntrinsicHeight();
    }

    private void setHideBackground(boolean z) {
        if (this.mHideBackground != z) {
            this.mHideBackground = z;
            updateBackground();
            updateOutline();
        }
    }

    /* access modifiers changed from: protected */
    public boolean needsOutline() {
        return !this.mHideBackground && super.needsOutline();
    }

    /* access modifiers changed from: protected */
    public boolean shouldHideBackground() {
        return super.shouldHideBackground() || this.mHideBackground;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateRelativeOffset();
    }

    private void updateRelativeOffset() {
        this.mCollapsedIcons.getLocationOnScreen(this.mTmp);
        int[] iArr = this.mTmp;
        this.mRelativeOffset = iArr[0];
        getLocationOnScreen(iArr);
        this.mRelativeOffset -= this.mTmp[0];
    }

    /* access modifiers changed from: private */
    public void setOpenedAmount(float f) {
        this.mNoAnimationsInThisFrame = f == 1.0f && this.mOpenedAmount == 0.0f;
        this.mOpenedAmount = f;
        if (!this.mAmbientState.isPanelFullWidth()) {
            f = 1.0f;
        }
        int i = this.mRelativeOffset;
        if (isLayoutRtl()) {
            i = (getWidth() - i) - this.mCollapsedIcons.getWidth();
        }
        this.mShelfIcons.setActualLayoutWidth((int) NotificationUtils.interpolate((float) (this.mCollapsedIcons.getWidth() + i), (float) this.mShelfIcons.getWidth(), f));
        boolean hasOverflow = this.mCollapsedIcons.hasOverflow();
        int paddingEnd = this.mCollapsedIcons.getPaddingEnd();
        if (!hasOverflow) {
            paddingEnd = (int) (((float) paddingEnd) - (((float) this.mCollapsedIcons.getIconSize()) * 1.0f));
        }
        this.mShelfIcons.setActualPaddingEnd(NotificationUtils.interpolate((float) paddingEnd, (float) this.mShelfIcons.getPaddingEnd(), f));
        this.mShelfIcons.setActualPaddingStart(NotificationUtils.interpolate((float) i, (float) this.mShelfIcons.getPaddingStart(), f));
        this.mShelfIcons.setOpenedAmount(f);
        this.mShelfIcons.setVisualOverflowAdaption(this.mCollapsedIcons.getVisualOverflowAdaption());
    }

    public void setMaxLayoutHeight(int i) {
        this.mMaxLayoutHeight = i;
    }

    public int getNotGoneIndex() {
        return this.mNotGoneIndex;
    }

    /* access modifiers changed from: private */
    public void setHasItemsInStableShelf(boolean z) {
        if (this.mHasItemsInStableShelf != z) {
            this.mHasItemsInStableShelf = z;
            updateInteractiveness();
        }
    }

    public boolean hasItemsInStableShelf() {
        return this.mHasItemsInStableShelf;
    }

    public void setCollapsedIcons(NotificationIconContainer notificationIconContainer) {
        this.mCollapsedIcons = notificationIconContainer;
        notificationIconContainer.addOnLayoutChangeListener(this);
    }

    public void setStatusBarState(int i) {
        if (this.mStatusBarState != i) {
            this.mStatusBarState = i;
            updateInteractiveness();
        }
    }

    private void updateInteractiveness() {
        int i = 1;
        boolean z = this.mStatusBarState == 1 && this.mHasItemsInStableShelf && !this.mDark;
        this.mInteractive = z;
        setClickable(z);
        setFocusable(this.mInteractive);
        if (!this.mInteractive) {
            i = 4;
        }
        setImportantForAccessibility(i);
    }

    /* access modifiers changed from: protected */
    public boolean isInteractive() {
        return this.mInteractive;
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        this.mCollapsedIcons.setAnimationsEnabled(z);
        if (!z) {
            this.mShelfIcons.setAnimationsEnabled(false);
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mInteractive) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, getContext().getString(R.string.accessibility_overflow_action)));
        }
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateRelativeOffset();
    }

    private class ShelfState extends ExpandableViewState {
        /* access modifiers changed from: private */
        public boolean hasItemsInStableShelf;
        /* access modifiers changed from: private */
        public float maxShelfEnd;
        /* access modifiers changed from: private */
        public float openedAmount;

        private ShelfState() {
        }

        public void applyToView(View view) {
            super.applyToView(view);
            NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
            NotificationShelf.this.setOpenedAmount(this.openedAmount);
            NotificationShelf.this.updateAppearance();
            NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
            NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
        }

        public void animateTo(View view, AnimationProperties animationProperties) {
            super.animateTo(view, animationProperties);
            NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
            NotificationShelf.this.setOpenedAmount(this.openedAmount);
            NotificationShelf.this.updateAppearance();
            NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
            NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
        }
    }
}
