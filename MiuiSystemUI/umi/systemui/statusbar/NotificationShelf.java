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
    /* access modifiers changed from: private */
    public static final int TAG_CONTINUOUS_CLIPPING = C0015R$id.continuous_clipping_tag;
    private static final boolean USE_ANIMATIONS_WHEN_OPENING = SystemProperties.getBoolean("debug.icon_opening_animations", true);
    private AmbientState mAmbientState;
    /* access modifiers changed from: private */
    public boolean mAnimationsEnabled = true;
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
    /* access modifiers changed from: private */
    public NotificationIconContainer mShelfIcons;
    /* access modifiers changed from: private */
    public boolean mShowNotificationShelf;
    private int mStatusBarHeight;
    private int mStatusBarState;
    private int[] mTmp = new int[2];

    public boolean hasNoContentHeight() {
        return true;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean needsClippingToShelf() {
        return false;
    }

    public void setMaxShelfEnd(float f) {
    }

    public NotificationShelf(Context context, AttributeSet attributeSet, KeyguardBypassController keyguardBypassController) {
        super(context, attributeSet);
        this.mBypassController = keyguardBypassController;
    }

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
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        initDimens();
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

    public void updateState(AmbientState ambientState) {
        ExpandableView lastVisibleBackgroundChild = ambientState.getLastVisibleBackgroundChild();
        ShelfState shelfState = (ShelfState) getViewState();
        boolean z = true;
        if (!this.mShowNotificationShelf || lastVisibleBackgroundChild == null) {
            shelfState.hidden = true;
            shelfState.location = 64;
            boolean unused = shelfState.hasItemsInStableShelf = false;
            return;
        }
        float innerHeight = ((float) ambientState.getInnerHeight()) + ambientState.getTopPadding() + ambientState.getStackTranslation();
        ExpandableViewState viewState = lastVisibleBackgroundChild.getViewState();
        float f = viewState.yTranslation + ((float) viewState.height);
        shelfState.copyFrom(viewState);
        shelfState.height = getIntrinsicHeight();
        shelfState.yTranslation = Math.max(Math.min(f, innerHeight) - ((float) shelfState.height), getFullyClosedTranslation());
        shelfState.zTranslation = (float) ambientState.getBaseZHeight();
        float unused2 = shelfState.openedAmount = Math.min(1.0f, (shelfState.yTranslation - getFullyClosedTranslation()) / ((float) ((getIntrinsicHeight() * 2) + this.mCutoutHeight)));
        shelfState.clipTopAmount = 0;
        shelfState.alpha = 1.0f;
        shelfState.belowSpeedBump = this.mAmbientState.getSpeedBumpIndex() == 0;
        shelfState.hideSensitive = false;
        shelfState.xTranslation = getTranslationX();
        int i = this.mNotGoneIndex;
        if (i != -1) {
            shelfState.notGoneIndex = Math.min(shelfState.notGoneIndex, i);
        }
        boolean unused3 = shelfState.hasItemsInStableShelf = viewState.inShelf;
        if (this.mAmbientState.isShadeExpanded() && !this.mAmbientState.isQsCustomizerShowing()) {
            z = false;
        }
        shelfState.hidden = z;
        float unused4 = shelfState.maxShelfEnd = innerHeight;
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x0169  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x016b  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x016e A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01a5  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x01b4  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateAppearance() {
        /*
            r31 = this;
            r7 = r31
            boolean r0 = r7.mShowNotificationShelf
            if (r0 != 0) goto L_0x0007
            return
        L_0x0007:
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            r0.resetViewStates()
            float r8 = r31.getTranslationY()
            com.android.systemui.statusbar.notification.stack.AmbientState r0 = r7.mAmbientState
            com.android.systemui.statusbar.notification.row.ExpandableView r9 = r0.getLastVisibleBackgroundChild()
            r10 = -1
            r7.mNotGoneIndex = r10
            int r0 = r7.mMaxLayoutHeight
            int r1 = r31.getIntrinsicHeight()
            int r1 = r1 * 2
            int r0 = r0 - r1
            float r0 = (float) r0
            int r1 = (r8 > r0 ? 1 : (r8 == r0 ? 0 : -1))
            r11 = 1065353216(0x3f800000, float:1.0)
            r12 = 0
            if (r1 < 0) goto L_0x0038
            float r0 = r8 - r0
            int r1 = r31.getIntrinsicHeight()
            float r1 = (float) r1
            float r0 = r0 / r1
            float r0 = java.lang.Math.min(r11, r0)
            r13 = r0
            goto L_0x0039
        L_0x0038:
            r13 = r12
        L_0x0039:
            boolean r0 = r7.mHideBackground
            if (r0 == 0) goto L_0x004c
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r0 = r31.getViewState()
            com.android.systemui.statusbar.NotificationShelf$ShelfState r0 = (com.android.systemui.statusbar.NotificationShelf.ShelfState) r0
            boolean r0 = r0.hasItemsInStableShelf
            if (r0 != 0) goto L_0x004c
            r16 = 1
            goto L_0x004e
        L_0x004c:
            r16 = 0
        L_0x004e:
            com.android.systemui.statusbar.notification.stack.AmbientState r0 = r7.mAmbientState
            float r0 = r0.getCurrentScrollVelocity()
            int r1 = r7.mScrollFastThreshold
            float r1 = (float) r1
            int r1 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r1 > 0) goto L_0x0078
            com.android.systemui.statusbar.notification.stack.AmbientState r1 = r7.mAmbientState
            boolean r1 = r1.isExpansionChanging()
            if (r1 == 0) goto L_0x0075
            com.android.systemui.statusbar.notification.stack.AmbientState r1 = r7.mAmbientState
            float r1 = r1.getExpandingVelocity()
            float r1 = java.lang.Math.abs(r1)
            int r2 = r7.mScrollFastThreshold
            float r2 = (float) r2
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 <= 0) goto L_0x0075
            goto L_0x0078
        L_0x0075:
            r17 = 0
            goto L_0x007a
        L_0x0078:
            r17 = 1
        L_0x007a:
            int r0 = (r0 > r12 ? 1 : (r0 == r12 ? 0 : -1))
            if (r0 <= 0) goto L_0x0081
            r18 = 1
            goto L_0x0083
        L_0x0081:
            r18 = 0
        L_0x0083:
            com.android.systemui.statusbar.notification.stack.AmbientState r0 = r7.mAmbientState
            boolean r0 = r0.isExpansionChanging()
            if (r0 == 0) goto L_0x0096
            com.android.systemui.statusbar.notification.stack.AmbientState r0 = r7.mAmbientState
            boolean r0 = r0.isPanelTracking()
            if (r0 != 0) goto L_0x0096
            r19 = 1
            goto L_0x0098
        L_0x0096:
            r19 = 0
        L_0x0098:
            com.android.systemui.statusbar.notification.stack.AmbientState r0 = r7.mAmbientState
            int r6 = r0.getBaseZHeight()
            r0 = 0
            r5 = r0
            r2 = r12
            r20 = r2
            r0 = 0
            r1 = 0
            r3 = 0
            r4 = 0
            r11 = 0
            r14 = 0
            r21 = 0
        L_0x00ab:
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r15 = r7.mHostLayout
            int r15 = r15.getChildCount()
            r10 = 8
            if (r4 >= r15) goto L_0x0236
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r15 = r7.mHostLayout
            android.view.View r15 = r15.getChildAt(r4)
            com.android.systemui.statusbar.notification.row.ExpandableView r15 = (com.android.systemui.statusbar.notification.row.ExpandableView) r15
            boolean r22 = r15.needsClippingToShelf()
            if (r22 == 0) goto L_0x0215
            r22 = r2
            int r2 = r15.getVisibility()
            if (r2 != r10) goto L_0x00cd
            goto L_0x0217
        L_0x00cd:
            float r2 = com.android.systemui.statusbar.notification.stack.ViewState.getFinalTranslationZ(r15)
            float r10 = (float) r6
            int r2 = (r2 > r10 ? 1 : (r2 == r10 ? 0 : -1))
            if (r2 > 0) goto L_0x00df
            boolean r2 = r15.isPinned()
            if (r2 == 0) goto L_0x00dd
            goto L_0x00df
        L_0x00dd:
            r10 = 0
            goto L_0x00e0
        L_0x00df:
            r10 = 1
        L_0x00e0:
            if (r15 != r9) goto L_0x00e5
            r23 = 1
            goto L_0x00e7
        L_0x00e5:
            r23 = 0
        L_0x00e7:
            float r24 = r15.getTranslationY()
            if (r23 == 0) goto L_0x00f3
            boolean r2 = r15.isInShelf()
            if (r2 == 0) goto L_0x00fe
        L_0x00f3:
            if (r10 != 0) goto L_0x00fe
            if (r16 == 0) goto L_0x00f8
            goto L_0x00fe
        L_0x00f8:
            int r2 = r7.mPaddingBetweenElements
            float r2 = (float) r2
            float r2 = r8 - r2
            goto L_0x0104
        L_0x00fe:
            int r2 = r31.getIntrinsicHeight()
            float r2 = (float) r2
            float r2 = r2 + r8
        L_0x0104:
            int r2 = r7.updateNotificationClipHeight(r15, r2, r1)
            int r25 = java.lang.Math.max(r2, r0)
            r0 = r31
            r2 = r1
            r1 = r15
            r26 = r9
            r9 = r2
            r2 = r13
            r27 = r13
            r13 = r3
            r3 = r18
            r28 = r4
            r4 = r17
            r29 = r13
            r13 = r5
            r5 = r19
            r30 = r6
            r6 = r23
            float r0 = r0.updateShelfTransformation(r1, r2, r3, r4, r5, r6)
            boolean r1 = r15 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r1 == 0) goto L_0x01c1
            r1 = r15
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r1 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r1
            float r20 = r20 + r0
            int r2 = r1.getBackgroundColorWithoutTint()
            int r3 = (r24 > r8 ? 1 : (r24 == r8 ? 0 : -1))
            if (r3 < 0) goto L_0x0149
            int r3 = r7.mNotGoneIndex
            r4 = -1
            if (r3 != r4) goto L_0x014a
            r7.mNotGoneIndex = r9
            r7.setTintColor(r14)
            r7.setOverrideTintColor(r11, r12)
            goto L_0x0150
        L_0x0149:
            r4 = -1
        L_0x014a:
            int r3 = r7.mNotGoneIndex
            if (r3 != r4) goto L_0x0150
            r12 = r0
            goto L_0x0151
        L_0x0150:
            r14 = r11
        L_0x0151:
            com.android.systemui.statusbar.notification.stack.AmbientState r3 = r7.mAmbientState
            boolean r3 = r3.isShadeExpanded()
            if (r3 == 0) goto L_0x016b
            com.android.systemui.statusbar.notification.stack.AmbientState r3 = r7.mAmbientState
            boolean r3 = r3.isOnKeyguard()
            if (r3 == 0) goto L_0x0169
            com.android.systemui.statusbar.phone.KeyguardBypassController r3 = r7.mBypassController
            boolean r3 = r3.getBypassEnabled()
            if (r3 != 0) goto L_0x016b
        L_0x0169:
            r3 = 1
            goto L_0x016c
        L_0x016b:
            r3 = 0
        L_0x016c:
            if (r23 == 0) goto L_0x017c
            if (r3 == 0) goto L_0x017c
            if (r21 != 0) goto L_0x0174
            r3 = r2
            goto L_0x0176
        L_0x0174:
            r3 = r21
        L_0x0176:
            r1.setOverrideTintColor(r3, r0)
            r0 = r3
            r3 = 0
            goto L_0x0182
        L_0x017c:
            r0 = 0
            r3 = 0
            r1.setOverrideTintColor(r3, r0)
            r0 = r2
        L_0x0182:
            if (r9 != 0) goto L_0x0186
            if (r10 != 0) goto L_0x0189
        L_0x0186:
            r1.setAboveShelf(r3)
        L_0x0189:
            if (r9 != 0) goto L_0x01b4
            com.android.systemui.statusbar.notification.collection.NotificationEntry r3 = r1.getEntry()
            com.android.systemui.statusbar.notification.icon.IconPack r3 = r3.getIcons()
            com.android.systemui.statusbar.StatusBarIconView r3 = r3.getShelfIcon()
            com.android.systemui.statusbar.phone.NotificationIconContainer$IconState r3 = r7.getIconState(r3)
            if (r3 == 0) goto L_0x01b4
            float r3 = r3.clampedAppearAmount
            r4 = 1065353216(0x3f800000, float:1.0)
            int r3 = (r3 > r4 ? 1 : (r3 == r4 ? 0 : -1))
            if (r3 != 0) goto L_0x01b4
            float r3 = r15.getTranslationY()
            float r4 = r31.getTranslationY()
            float r3 = r3 - r4
            int r3 = (int) r3
            float r1 = r1.getCurrentTopRoundness()
            goto L_0x01b8
        L_0x01b4:
            r1 = r22
            r3 = r29
        L_0x01b8:
            int r4 = r9 + 1
            r21 = r0
            r11 = r14
            r14 = r2
            r2 = r1
            r1 = r4
            goto L_0x01c6
        L_0x01c1:
            r1 = r9
            r2 = r22
            r3 = r29
        L_0x01c6:
            boolean r0 = r15 instanceof com.android.systemui.statusbar.notification.row.ActivatableNotificationView
            if (r0 == 0) goto L_0x0210
            r0 = r15
            com.android.systemui.statusbar.notification.row.ActivatableNotificationView r0 = (com.android.systemui.statusbar.notification.row.ActivatableNotificationView) r0
            boolean r4 = r0.isFirstInSection()
            if (r4 == 0) goto L_0x020c
            if (r13 == 0) goto L_0x020c
            boolean r4 = r13.isLastInSection()
            if (r4 == 0) goto L_0x020c
            float r4 = r15.getTranslationY()
            float r5 = r31.getTranslationY()
            float r4 = r4 - r5
            float r5 = r31.getTranslationY()
            float r6 = r13.getTranslationY()
            int r9 = r13.getActualHeight()
            float r9 = (float) r9
            float r6 = r6 + r9
            float r5 = r5 - r6
            r6 = 0
            int r9 = (r5 > r6 ? 1 : (r5 == r6 ? 0 : -1))
            if (r9 <= 0) goto L_0x020a
            r2 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            int r9 = r7.mGapHeight
            float r9 = (float) r9
            float r5 = r5 / r9
            double r9 = (double) r5
            double r2 = java.lang.Math.min(r2, r9)
            float r2 = (float) r2
            r5 = 0
            r13.setBottomRoundness(r2, r5)
            int r3 = (int) r4
            goto L_0x020e
        L_0x020a:
            r5 = 0
            goto L_0x020e
        L_0x020c:
            r5 = 0
            r6 = 0
        L_0x020e:
            r13 = r0
            goto L_0x0212
        L_0x0210:
            r5 = 0
            r6 = 0
        L_0x0212:
            r0 = r25
            goto L_0x022a
        L_0x0215:
            r22 = r2
        L_0x0217:
            r29 = r3
            r28 = r4
            r30 = r6
            r26 = r9
            r27 = r13
            r6 = 0
            r9 = r1
            r13 = r5
            r5 = 0
            r1 = r9
            r2 = r22
            r3 = r29
        L_0x022a:
            int r4 = r28 + 1
            r5 = r13
            r9 = r26
            r13 = r27
            r6 = r30
            r10 = -1
            goto L_0x00ab
        L_0x0236:
            r9 = r1
            r22 = r2
            r29 = r3
            r5 = 0
            r31.clipTransientViews()
            r7.setClipTopAmount(r0)
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r31.getViewState()
            boolean r1 = r1.hidden
            if (r1 != 0) goto L_0x0253
            int r1 = r31.getIntrinsicHeight()
            if (r0 < r1) goto L_0x0251
            goto L_0x0253
        L_0x0251:
            r3 = r5
            goto L_0x0254
        L_0x0253:
            r3 = 1
        L_0x0254:
            boolean r0 = r7.mShowNotificationShelf
            if (r0 == 0) goto L_0x0260
            if (r3 == 0) goto L_0x025c
            r3 = 4
            goto L_0x025d
        L_0x025c:
            r3 = r5
        L_0x025d:
            r7.setVisibility(r3)
        L_0x0260:
            r15 = r29
            r7.setBackgroundTop(r15)
            r2 = r22
            r7.setFirstElementRoundness(r2)
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            com.android.systemui.statusbar.notification.stack.AmbientState r1 = r7.mAmbientState
            int r1 = r1.getSpeedBumpIndex()
            r0.setSpeedBumpIndex(r1)
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            r0.calculateIconTranslations()
            com.android.systemui.statusbar.phone.NotificationIconContainer r0 = r7.mShelfIcons
            r0.applyIconStates()
            r3 = r5
        L_0x0280:
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r0 = r7.mHostLayout
            int r0 = r0.getChildCount()
            if (r3 >= r0) goto L_0x02a4
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r0 = r7.mHostLayout
            android.view.View r0 = r0.getChildAt(r3)
            boolean r1 = r0 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r1 == 0) goto L_0x02a1
            int r1 = r0.getVisibility()
            if (r1 != r10) goto L_0x0299
            goto L_0x02a1
        L_0x0299:
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r0 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r0
            r7.updateIconClipAmount(r0)
            r7.updateContinuousClipping(r0)
        L_0x02a1:
            int r3 = r3 + 1
            goto L_0x0280
        L_0x02a4:
            r0 = 1065353216(0x3f800000, float:1.0)
            int r0 = (r20 > r0 ? 1 : (r20 == r0 ? 0 : -1))
            if (r0 >= 0) goto L_0x02ac
            r3 = 1
            goto L_0x02ad
        L_0x02ac:
            r3 = r5
        L_0x02ad:
            if (r3 != 0) goto L_0x02b4
            if (r16 == 0) goto L_0x02b2
            goto L_0x02b4
        L_0x02b2:
            r14 = r5
            goto L_0x02b5
        L_0x02b4:
            r14 = 1
        L_0x02b5:
            r7.setHideBackground(r14)
            int r0 = r7.mNotGoneIndex
            r1 = -1
            if (r0 != r1) goto L_0x02bf
            r7.mNotGoneIndex = r9
        L_0x02bf:
            return
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
    public void updateIconClipAmount(ExpandableNotificationRow expandableNotificationRow) {
        float translationY = expandableNotificationRow.getTranslationY();
        if (getClipTopAmount() != 0) {
            translationY = Math.max(translationY, getTranslationY() + ((float) getClipTopAmount()));
        }
        StatusBarIconView shelfIcon = expandableNotificationRow.getEntry().getIcons().getShelfIcon();
        float translationY2 = getTranslationY() + ((float) shelfIcon.getTop()) + shelfIcon.getTranslationY();
        if (translationY2 >= translationY || this.mAmbientState.isFullyHidden()) {
            shelfIcon.setClipBounds((Rect) null);
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
                public boolean onPreDraw() {
                    if (!ViewState.isAnimatingY(shelfIcon)) {
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.removeOnPreDrawListener(this);
                        }
                        shelfIcon.setTag(NotificationShelf.TAG_CONTINUOUS_CLIPPING, (Object) null);
                        return true;
                    }
                    NotificationShelf.this.updateIconClipAmount(expandableNotificationRow);
                    return true;
                }
            };
            viewTreeObserver.addOnPreDrawListener(r2);
            shelfIcon.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(this) {
                public void onViewAttachedToWindow(View view) {
                }

                public void onViewDetachedFromWindow(View view) {
                    if (view == shelfIcon) {
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.removeOnPreDrawListener(r2);
                        }
                        shelfIcon.setTag(NotificationShelf.TAG_CONTINUOUS_CLIPPING, (Object) null);
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
        ExpandableView expandableView2 = expandableView;
        float f8 = f;
        boolean z5 = z3;
        boolean z6 = z4;
        NotificationIconContainer.IconState iconState = getIconState(expandableView.getShelfIcon());
        float translationY = expandableView.getTranslationY();
        int actualHeight = expandableView.getActualHeight() + this.mPaddingBetweenElements;
        float calculateIconTransformationStart = calculateIconTransformationStart(expandableView);
        float f9 = (float) actualHeight;
        float min = Math.min((f9 + translationY) - calculateIconTransformationStart, Math.min(((float) getIntrinsicHeight()) * 1.5f * NotificationUtils.interpolate(1.0f, 1.5f, f8), f9));
        if (z6) {
            actualHeight = Math.min(actualHeight, expandableView.getMinHeight() - getIntrinsicHeight());
            min = Math.min(min, (float) (expandableView.getMinHeight() - getIntrinsicHeight()));
        }
        handleCustomTransformHeight(expandableView2, z5, iconState);
        float translationY2 = getTranslationY();
        boolean z7 = true;
        float f10 = 0.0f;
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
                float f11 = translationY2 - translationY;
                float min2 = Math.min(1.0f, f11 / ((float) actualHeight));
                f7 = 1.0f - NotificationUtils.interpolate(Interpolators.ACCELERATE_DECELERATE.getInterpolation(min2), min2, f8);
                f6 = 1.0f - MathUtils.constrain(z6 ? f11 / min : (translationY2 - calculateIconTransformationStart) / min, 0.0f, 1.0f);
                z7 = false;
            } else {
                f7 = 1.0f;
                f6 = 1.0f;
            }
            f2 = 1.0f - Math.min(1.0f, (translationY2 - translationY) / min);
            f4 = min;
            f3 = f7;
            f5 = f6;
        }
        if (iconState != null && z7 && !z5 && iconState.isLastExpandIcon) {
            iconState.isLastExpandIcon = false;
            iconState.customTransformHeight = Integer.MIN_VALUE;
        }
        if (!expandableView.isAboveShelf() && !expandableView.showingPulsing() && (z6 || iconState == null || iconState.translateContent)) {
            f10 = f2;
        }
        expandableView2.setContentTransformationAmount(f10, z6);
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
        ExpandableView expandableView2 = expandableView;
        float f5 = f;
        if (expandableView2 instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView2;
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
            float interpolate = NotificationUtils.interpolate((translationY + ((float) i2)) - ((getTranslationY() + ((float) shelfIcon.getTop())) + ((((float) shelfIcon.getHeight()) - iconScale) / 2.0f)), (!z || z3) ? 0.0f : NotificationUtils.interpolate(Math.min((((float) this.mIconAppearTopPadding) + translationY) - getTranslationY(), 0.0f), 0.0f, f5), f5);
            float interpolate2 = NotificationUtils.interpolate(((float) i) - (((float) shelfIcon.getLeft()) + (((1.0f - shelfIcon.getIconScale()) * ((float) shelfIcon.getWidth())) / 2.0f)), this.mShelfIcons.getActualPaddingStart(), f5);
            boolean z4 = !expandableNotificationRow.isShowingIcon();
            if (z4) {
                f3 = iconScale / 2.0f;
                interpolate2 = this.mShelfIcons.getActualPaddingStart();
                f4 = f5;
            } else {
                f4 = 1.0f;
            }
            float interpolate3 = NotificationUtils.interpolate(f3, iconScale, f5);
            if (iconState != null) {
                float f6 = interpolate3 / iconScale;
                iconState.scaleX = f6;
                iconState.scaleY = f6;
                iconState.hidden = f5 == 0.0f && !iconState.isAnimating(shelfIcon);
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
    public void setOpenedAmount(float f) {
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
    public void setHasItemsInStableShelf(boolean z) {
        if (this.mHasItemsInStableShelf != z) {
            this.mHasItemsInStableShelf = z;
            updateInteractiveness();
        }
    }

    public void setCollapsedIcons(NotificationIconContainer notificationIconContainer) {
        this.mCollapsedIcons = notificationIconContainer;
        notificationIconContainer.addOnLayoutChangeListener(this);
    }

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
            if (NotificationShelf.this.mShowNotificationShelf) {
                super.applyToView(view);
                NotificationShelf.this.setMaxShelfEnd(this.maxShelfEnd);
                NotificationShelf.this.setOpenedAmount(this.openedAmount);
                NotificationShelf.this.updateAppearance();
                NotificationShelf.this.setHasItemsInStableShelf(this.hasItemsInStableShelf);
                NotificationShelf.this.mShelfIcons.setAnimationsEnabled(NotificationShelf.this.mAnimationsEnabled);
            }
        }

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
