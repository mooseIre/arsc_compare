package com.android.systemui.statusbar.stack;

import android.app.Notification;
import android.app.NotificationCompat;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationHeaderUtil;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import java.util.ArrayList;
import java.util.List;

public class NotificationChildrenContainer extends ViewGroup {
    private static final AnimationProperties ALPHA_FADE_IN;
    private int mActualHeight;
    private ImageView mAppIcon;
    private int mChildTopMargin;
    private final List<ExpandableNotificationRow> mChildren;
    private boolean mChildrenExpanded;
    private int mClipBottomAmount;
    private ViewState mCollapseButtonViewState;
    private float mCollapsedBottomMargin;
    private TextView mCollapsedButton;
    private int mCollapsedButtonPadding;
    private ExpandableNotificationRow mContainingNotification;
    private int mContentMarginEnd;
    private ViewGroup mCurrentHeader;
    private int mDividerHeight;
    private int mExpandedBottomMargin;
    private ViewState mGroupOverFlowState;
    private View.OnClickListener mHeaderClickListener;
    private int mHeaderHeight;
    private NotificationHeaderUtil mHeaderUtil;
    private ViewState mHeaderViewState;
    private final HybridGroupManager mHybridGroupManager;
    private boolean mIsLowPriority;
    private int mMiuiAppIconMargin;
    private int mMiuiAppIconSize;
    private boolean mNeverAppliedGroupState;
    private NotificationHeaderView mNotificationHeader;
    private ViewGroup mNotificationHeaderAmbient;
    private NotificationHeaderView mNotificationHeaderLowPriority;
    private int mNotificationHeaderMargin;
    private NotificationViewWrapper mNotificationHeaderWrapper;
    private NotificationViewWrapper mNotificationHeaderWrapperAmbient;
    private NotificationViewWrapper mNotificationHeaderWrapperLowPriority;
    private int mNotificationTopPadding;
    private TextView mOverflowNumber;
    private int mOverflowNumberBottomPadding;
    private int mOverflowNumberTopMargin;
    private int mOverflowNumberTopPadding;
    private int mRealHeight;
    private boolean mUserLocked;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void prepareExpansionChanged(StackScrollState stackScrollState) {
    }

    static {
        AnonymousClass1 r0 = new AnimationProperties() {
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
        r0.setDuration(200);
        ALPHA_FADE_IN = r0;
    }

    public NotificationChildrenContainer(Context context) {
        this(context, (AttributeSet) null);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NotificationChildrenContainer(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mChildren = new ArrayList();
        this.mChildTopMargin = 0;
        this.mDividerHeight = 0;
        initDimens();
        this.mHybridGroupManager = new HybridGroupManager(getContext(), this);
    }

    private void initDimens() {
        int i;
        boolean showGoogleStyle = NotificationUtil.showGoogleStyle();
        Resources resources = getResources();
        this.mHeaderHeight = resources.getDimensionPixelSize(R.dimen.notification_header_height);
        int i2 = 0;
        this.mNotificationHeaderMargin = showGoogleStyle ? resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_top) : 0;
        if (showGoogleStyle) {
            i = 0;
        } else {
            i = resources.getDimensionPixelSize(17105362);
        }
        this.mNotificationTopPadding = i;
        if (!showGoogleStyle) {
            i2 = resources.getDimensionPixelSize(R.dimen.notification_group_expanded_bottom_margin);
        }
        this.mExpandedBottomMargin = i2;
        this.mCollapsedBottomMargin = (float) resources.getDimensionPixelSize(17105359);
        this.mCollapsedButtonPadding = resources.getDimensionPixelSize(R.dimen.notification_collapsed_button_padding);
        resources.getDimensionPixelSize(17105362);
        this.mContentMarginEnd = resources.getDimensionPixelSize(17105360);
        this.mOverflowNumberTopPadding = resources.getDimensionPixelSize(R.dimen.notification_group_overflow_padding_top);
        this.mOverflowNumberBottomPadding = resources.getDimensionPixelSize(R.dimen.notification_group_overflow_padding_bottom);
        this.mOverflowNumberTopMargin = resources.getDimensionPixelSize(R.dimen.notification_group_overflow_margin_top);
        this.mMiuiAppIconSize = resources.getDimensionPixelSize(R.dimen.notification_app_icon_size);
        this.mMiuiAppIconMargin = resources.getDimensionPixelSize(R.dimen.notification_app_icon_margin);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int min = Math.min(this.mChildren.size(), 8);
        for (int i5 = 0; i5 < min; i5++) {
            View view = this.mChildren.get(i5);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        if (this.mOverflowNumber != null) {
            int overflowNumberPadding = this.mHybridGroupManager.getOverflowNumberPadding();
            boolean z2 = true;
            if (getLayoutDirection() != 1) {
                z2 = false;
            }
            if (!z2) {
                overflowNumberPadding = (getWidth() - this.mOverflowNumber.getMeasuredWidth()) - overflowNumberPadding;
            }
            TextView textView = this.mOverflowNumber;
            textView.layout(overflowNumberPadding, 0, this.mOverflowNumber.getMeasuredWidth() + overflowNumberPadding, textView.getMeasuredHeight());
        }
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            notificationHeaderView.layout(0, 0, notificationHeaderView.getMeasuredWidth(), this.mNotificationHeader.getMeasuredHeight());
        }
        NotificationHeaderView notificationHeaderView2 = this.mNotificationHeaderLowPriority;
        if (notificationHeaderView2 != null) {
            notificationHeaderView2.layout(0, 0, notificationHeaderView2.getMeasuredWidth(), this.mNotificationHeaderLowPriority.getMeasuredHeight());
        }
        ViewGroup viewGroup = this.mNotificationHeaderAmbient;
        if (viewGroup != null) {
            viewGroup.layout(0, 0, viewGroup.getMeasuredWidth(), this.mNotificationHeaderAmbient.getMeasuredHeight());
        }
        if (this.mAppIcon != null) {
            int width = isRTL() ? (getWidth() - this.mMiuiAppIconMargin) - this.mMiuiAppIconSize : this.mMiuiAppIconMargin;
            int i6 = this.mMiuiAppIconMargin;
            ImageView imageView = this.mAppIcon;
            int i7 = this.mMiuiAppIconSize;
            imageView.layout(width, i6, width + i7, i7 + i6);
        }
        if (this.mCollapsedButton != null) {
            int width2 = (getWidth() - this.mContentMarginEnd) + this.mCollapsedButtonPadding;
            TextView textView2 = this.mCollapsedButton;
            textView2.layout(width2 - this.mCollapsedButton.getMeasuredWidth(), 0, width2, textView2.getMeasuredHeight());
        }
    }

    private boolean isRTL() {
        return (getResources().getConfiguration().screenLayout & 192) == 128;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        TextView textView;
        int i4 = i;
        int mode = View.MeasureSpec.getMode(i2);
        boolean z = true;
        boolean z2 = mode == 1073741824;
        boolean z3 = mode == Integer.MIN_VALUE;
        int size = View.MeasureSpec.getSize(i2);
        if (z2 || z3) {
            i3 = View.MeasureSpec.makeMeasureSpec(size, Integer.MIN_VALUE);
        } else {
            i3 = i2;
        }
        int size2 = View.MeasureSpec.getSize(i);
        TextView textView2 = this.mOverflowNumber;
        if (textView2 != null) {
            textView2.measure(View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE), i3);
        }
        int i5 = this.mNotificationHeaderMargin;
        int min = Math.min(this.mChildren.size(), 8);
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        int i6 = min > maxAllowedVisibleChildren ? maxAllowedVisibleChildren - 1 : -1;
        int i7 = 0;
        while (i7 < min) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i7);
            expandableNotificationRow.setSingleLineWidthIndention((!(i7 == i6 ? z : false) || (textView = this.mOverflowNumber) == null) ? 0 : textView.getMeasuredWidth() + this.mHybridGroupManager.getOverflowNumberPadding());
            expandableNotificationRow.measure(i4, i3);
            if (expandableNotificationRow.getVisibility() != 8) {
                i5 += expandableNotificationRow.getIntrinsicHeight();
            }
            i7++;
            z = true;
        }
        this.mRealHeight = i5;
        if (mode != 0) {
            i5 = Math.min(i5, size);
        }
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824);
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            notificationHeaderView.measure(i4, makeMeasureSpec);
        }
        if (this.mNotificationHeaderLowPriority != null) {
            this.mNotificationHeaderLowPriority.measure(i4, View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824));
        }
        if (this.mNotificationHeaderAmbient != null) {
            this.mNotificationHeaderAmbient.measure(i4, View.MeasureSpec.makeMeasureSpec(this.mHeaderHeight, 1073741824));
        }
        ImageView imageView = this.mAppIcon;
        if (imageView != null) {
            imageView.measure(View.MeasureSpec.makeMeasureSpec(this.mMiuiAppIconSize, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMiuiAppIconSize, 1073741824));
        }
        TextView textView3 = this.mCollapsedButton;
        if (textView3 != null) {
            textView3.measure(View.MeasureSpec.makeMeasureSpec(size2, Integer.MIN_VALUE), i3);
            i5 += this.mCollapsedButton.getMeasuredHeight();
            this.mRealHeight += this.mCollapsedButton.getMeasuredHeight();
        }
        setMeasuredDimension(size2, i5);
    }

    public boolean pointInView(float f, float f2, float f3) {
        float f4 = -f3;
        return f >= f4 && f2 >= f4 && f < ((float) (this.mRight - this.mLeft)) + f3 && f2 < ((float) this.mRealHeight) + f3;
    }

    public void addNotification(ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i < 0) {
            i = this.mChildren.size();
        }
        this.mChildren.add(i, expandableNotificationRow);
        addView(expandableNotificationRow);
        expandableNotificationRow.setUserLocked(this.mUserLocked);
        updateGroupOverflow();
        expandableNotificationRow.setContentTransformationAmount(0.0f, false);
    }

    public void removeNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mChildren.indexOf(expandableNotificationRow);
        this.mChildren.remove(expandableNotificationRow);
        removeView(expandableNotificationRow);
        expandableNotificationRow.setSystemChildExpanded(false);
        expandableNotificationRow.setUserLocked(false);
        updateGroupOverflow();
        if (this.mHeaderUtil != null && !expandableNotificationRow.isRemoved()) {
            this.mHeaderUtil.restoreNotificationHeader(expandableNotificationRow);
        }
    }

    public void rebuildCollapseButton() {
        if (this.mCollapsedButton == null) {
            TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.notification_group_collapsed_button, this, false);
            this.mCollapsedButton = textView;
            textView.setOnClickListener(this.mContainingNotification.getExpandClickListener());
            addView(this.mCollapsedButton);
        }
    }

    public int getNotificationChildCount() {
        return this.mChildren.size();
    }

    public void recreateNotificationHeader(View.OnClickListener onClickListener) {
        this.mHeaderClickListener = onClickListener;
        Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(getContext(), this.mContainingNotification.getStatusBarNotification().getNotification());
        RemoteViews makeNotificationHeader = NotificationCompat.makeNotificationHeader(recoverBuilder, false);
        if (this.mNotificationHeader == null) {
            NotificationHeaderView apply = makeNotificationHeader.apply(getContext(), this);
            this.mNotificationHeader = apply;
            View findViewById = apply.findViewById(16908294);
            this.mNotificationHeader.findViewById(16908957).setVisibility(0);
            this.mNotificationHeader.setOnClickListener(this.mHeaderClickListener);
            this.mNotificationHeaderWrapper = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeader, this.mContainingNotification);
            resetAppIcon(findViewById);
            addView(this.mNotificationHeader, 0);
            invalidate();
        } else {
            makeNotificationHeader.reapply(getContext(), this.mNotificationHeader);
        }
        this.mNotificationHeaderWrapper.onContentUpdated(this.mContainingNotification);
        if (NotificationUtil.showGoogleStyle()) {
            recreateLowPriorityHeader(recoverBuilder);
            if (Constants.SUPPORT_AOD) {
                recreateAmbientHeader(recoverBuilder);
            }
        }
        updateHeaderVisibility(false);
        updateChildrenHeaderAppearance();
    }

    private void resetAppIcon(View view) {
        if (NotificationUtil.showGoogleStyle()) {
            removeView(this.mAppIcon);
            this.mAppIcon = null;
        } else if (this.mAppIcon == null) {
            ImageView imageView = (ImageView) view;
            this.mAppIcon = imageView;
            addView(imageView);
            setContentDescription(this.mContainingNotification.getStatusBarNotification().getAppName());
        }
    }

    private void recreateAmbientHeader(Notification.Builder builder) {
        ExpandedNotification statusBarNotification = this.mContainingNotification.getStatusBarNotification();
        if (builder == null) {
            builder = NotificationCompat.recoverBuilder(getContext(), statusBarNotification.getNotification());
        }
        RemoteViews makeNotificationHeader = NotificationCompat.makeNotificationHeader(builder, true);
        if (this.mNotificationHeaderAmbient == null) {
            this.mNotificationHeaderAmbient = (ViewGroup) makeNotificationHeader.apply(getContext(), this);
            NotificationViewWrapper wrap = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeaderAmbient, this.mContainingNotification);
            this.mNotificationHeaderWrapperAmbient = wrap;
            wrap.onContentUpdated(this.mContainingNotification);
            addView(this.mNotificationHeaderAmbient, 0);
            invalidate();
        } else {
            makeNotificationHeader.reapply(getContext(), this.mNotificationHeaderAmbient);
        }
        resetHeaderVisibilityIfNeeded(this.mNotificationHeaderAmbient, calculateDesiredHeader());
        this.mNotificationHeaderWrapperAmbient.onContentUpdated(this.mContainingNotification);
    }

    private void recreateLowPriorityHeader(Notification.Builder builder) {
        ExpandedNotification statusBarNotification = this.mContainingNotification.getStatusBarNotification();
        if (this.mIsLowPriority) {
            if (builder == null) {
                builder = NotificationCompat.recoverBuilder(getContext(), statusBarNotification.getNotification());
            }
            RemoteViews makeLowPriorityContentView = NotificationCompat.makeLowPriorityContentView(builder, true);
            if (this.mNotificationHeaderLowPriority == null) {
                NotificationHeaderView apply = makeLowPriorityContentView.apply(getContext(), this);
                this.mNotificationHeaderLowPriority = apply;
                apply.findViewById(16908957).setVisibility(0);
                this.mNotificationHeaderLowPriority.setOnClickListener(this.mHeaderClickListener);
                this.mNotificationHeaderWrapperLowPriority = NotificationViewWrapper.wrap(getContext(), this.mNotificationHeaderLowPriority, this.mContainingNotification);
                addView(this.mNotificationHeaderLowPriority, 0);
                invalidate();
            } else {
                makeLowPriorityContentView.reapply(getContext(), this.mNotificationHeaderLowPriority);
            }
            this.mNotificationHeaderWrapperLowPriority.onContentUpdated(this.mContainingNotification);
            resetHeaderVisibilityIfNeeded(this.mNotificationHeaderLowPriority, calculateDesiredHeader());
            return;
        }
        removeView(this.mNotificationHeaderLowPriority);
        this.mNotificationHeaderLowPriority = null;
        this.mNotificationHeaderWrapperLowPriority = null;
    }

    public void updateChildrenHeaderAppearance() {
        initHeaderUtil();
        NotificationHeaderUtil notificationHeaderUtil = this.mHeaderUtil;
        if (notificationHeaderUtil != null) {
            notificationHeaderUtil.updateChildrenHeaderAppearance();
        }
    }

    public void updateGroupOverflow() {
        int size = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        if (size > maxAllowedVisibleChildren) {
            TextView bindOverflowNumber = this.mHybridGroupManager.bindOverflowNumber(this.mOverflowNumber, size - maxAllowedVisibleChildren);
            this.mOverflowNumber = bindOverflowNumber;
            bindOverflowNumber.setOnClickListener(this.mContainingNotification.getExpandClickListener());
            if (this.mGroupOverFlowState == null) {
                this.mGroupOverFlowState = new ViewState();
                this.mNeverAppliedGroupState = true;
                return;
            }
            return;
        }
        TextView textView = this.mOverflowNumber;
        if (textView != null) {
            removeView(textView);
            if (isShown()) {
                final TextView textView2 = this.mOverflowNumber;
                addTransientView(textView2, getTransientViewCount());
                CrossFadeHelper.fadeOut((View) textView2, (Runnable) new Runnable() {
                    public void run() {
                        NotificationChildrenContainer.this.removeTransientView(textView2);
                    }
                });
            }
            this.mOverflowNumber = null;
            this.mGroupOverFlowState = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateGroupOverflow();
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        return this.mChildren;
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> list, VisualStabilityManager visualStabilityManager, VisualStabilityManager.Callback callback) {
        int i = 0;
        if (list == null) {
            return false;
        }
        boolean z = false;
        while (i < this.mChildren.size() && i < list.size()) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            ExpandableNotificationRow expandableNotificationRow2 = list.get(i);
            if (expandableNotificationRow != expandableNotificationRow2) {
                if (visualStabilityManager.canReorderNotification(expandableNotificationRow2)) {
                    this.mChildren.remove(expandableNotificationRow2);
                    this.mChildren.add(i, expandableNotificationRow2);
                    z = true;
                } else {
                    visualStabilityManager.addReorderingAllowedCallback(callback);
                }
            }
            i++;
        }
        updateExpansionStates();
        return z;
    }

    private void updateExpansionStates() {
        if (!this.mChildrenExpanded && !this.mUserLocked) {
            int size = this.mChildren.size();
            for (int i = 0; i < size; i++) {
                ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
                boolean z = true;
                if (i != 0 || size != 1) {
                    z = false;
                }
                expandableNotificationRow.setSystemChildExpanded(z);
            }
        }
    }

    public int getIntrinsicHeight() {
        return getIntrinsicHeight((float) getMaxAllowedVisibleChildren());
    }

    /* JADX WARNING: Removed duplicated region for block: B:49:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00c2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getIntrinsicHeight(float r12) {
        /*
            r11 = this;
            boolean r0 = r11.showingAsLowPriority()
            if (r0 == 0) goto L_0x000d
            android.view.NotificationHeaderView r11 = r11.mNotificationHeaderLowPriority
            int r11 = r11.getHeight()
            return r11
        L_0x000d:
            int r0 = r11.mNotificationHeaderMargin
            java.util.List<com.android.systemui.statusbar.ExpandableNotificationRow> r1 = r11.mChildren
            int r1 = r1.size()
            boolean r2 = r11.mUserLocked
            r3 = 0
            if (r2 == 0) goto L_0x001f
            float r2 = r11.getGroupExpandFraction()
            goto L_0x0020
        L_0x001f:
            r2 = r3
        L_0x0020:
            boolean r4 = r11.mChildrenExpanded
            r5 = 1
            r6 = 0
            if (r4 != 0) goto L_0x0031
            com.android.systemui.statusbar.ExpandableNotificationRow r4 = r11.mContainingNotification
            boolean r4 = r4.isShowingAmbient()
            if (r4 == 0) goto L_0x002f
            goto L_0x0031
        L_0x002f:
            r4 = r6
            goto L_0x0032
        L_0x0031:
            r4 = r5
        L_0x0032:
            boolean r7 = r11.mUserLocked
            if (r7 == 0) goto L_0x0041
            float r0 = (float) r0
            int r7 = r11.mNotificationTopPadding
            float r7 = (float) r7
            float r7 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r7, r3, r2)
            float r0 = r0 + r7
            int r0 = (int) r0
            goto L_0x0046
        L_0x0041:
            if (r4 != 0) goto L_0x0046
            int r7 = r11.mNotificationTopPadding
            int r0 = r0 + r7
        L_0x0046:
            r7 = r6
            r8 = r7
        L_0x0048:
            if (r7 >= r1) goto L_0x0096
            float r9 = (float) r8
            int r9 = (r9 > r12 ? 1 : (r9 == r12 ? 0 : -1))
            if (r9 < 0) goto L_0x0050
            goto L_0x0096
        L_0x0050:
            if (r5 != 0) goto L_0x006d
            boolean r9 = r11.mUserLocked
            if (r9 == 0) goto L_0x0064
            float r0 = (float) r0
            int r9 = r11.mChildTopMargin
            float r9 = (float) r9
            int r10 = r11.mDividerHeight
            float r10 = (float) r10
            float r9 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r9, r10, r2)
            float r0 = r0 + r9
            int r0 = (int) r0
            goto L_0x0084
        L_0x0064:
            if (r4 == 0) goto L_0x0069
            int r9 = r11.mDividerHeight
            goto L_0x006b
        L_0x0069:
            int r9 = r11.mChildTopMargin
        L_0x006b:
            int r0 = r0 + r9
            goto L_0x0084
        L_0x006d:
            boolean r5 = r11.mUserLocked
            if (r5 == 0) goto L_0x007c
            float r0 = (float) r0
            int r5 = r11.mDividerHeight
            float r5 = (float) r5
            float r5 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r3, r5, r2)
            float r0 = r0 + r5
            int r0 = (int) r0
            goto L_0x0083
        L_0x007c:
            if (r4 == 0) goto L_0x0081
            int r5 = r11.mDividerHeight
            goto L_0x0082
        L_0x0081:
            r5 = r6
        L_0x0082:
            int r0 = r0 + r5
        L_0x0083:
            r5 = r6
        L_0x0084:
            java.util.List<com.android.systemui.statusbar.ExpandableNotificationRow> r9 = r11.mChildren
            java.lang.Object r9 = r9.get(r7)
            com.android.systemui.statusbar.ExpandableNotificationRow r9 = (com.android.systemui.statusbar.ExpandableNotificationRow) r9
            int r9 = r9.getIntrinsicHeight()
            int r0 = r0 + r9
            int r8 = r8 + 1
            int r7 = r7 + 1
            goto L_0x0048
        L_0x0096:
            boolean r12 = r11.mUserLocked
            if (r12 == 0) goto L_0x00a7
            float r12 = (float) r0
            float r0 = r11.mCollapsedBottomMargin
            int r1 = r11.mExpandedBottomMargin
            float r1 = (float) r1
            float r0 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r0, r1, r2)
        L_0x00a4:
            float r12 = r12 + r0
            int r12 = (int) r12
            goto L_0x00b1
        L_0x00a7:
            if (r4 == 0) goto L_0x00ad
            int r12 = r11.mExpandedBottomMargin
            int r12 = r12 + r0
            goto L_0x00b1
        L_0x00ad:
            float r12 = (float) r0
            float r0 = r11.mCollapsedBottomMargin
            goto L_0x00a4
        L_0x00b1:
            boolean r0 = r11.mUserLocked
            if (r0 == 0) goto L_0x00c2
            float r12 = (float) r12
            int r11 = r11.getCollapsedButtonHeight()
            float r11 = (float) r11
            float r11 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r3, r11, r2)
            float r12 = r12 + r11
            int r12 = (int) r12
            goto L_0x00c9
        L_0x00c2:
            if (r4 == 0) goto L_0x00c9
            int r11 = r11.getCollapsedButtonHeight()
            int r12 = r12 + r11
        L_0x00c9:
            return r12
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.stack.NotificationChildrenContainer.getIntrinsicHeight(float):int");
    }

    /* JADX WARNING: type inference failed for: r3v14, types: [android.widget.TextView, android.view.View] */
    /* JADX WARNING: type inference failed for: r3v17, types: [android.widget.TextView] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getState(com.android.systemui.statusbar.stack.StackScrollState r20, com.android.systemui.statusbar.stack.ExpandableViewState r21) {
        /*
            r19 = this;
            r0 = r19
            r1 = r20
            r2 = r21
            java.util.List<com.android.systemui.statusbar.ExpandableNotificationRow> r3 = r0.mChildren
            int r3 = r3.size()
            int r4 = r0.mNotificationHeaderMargin
            int r5 = r19.getMaxAllowedVisibleChildren()
            r6 = 1
            int r5 = r5 - r6
            int r7 = r5 + 1
            boolean r8 = r0.mUserLocked
            if (r8 == 0) goto L_0x0022
            boolean r8 = r19.showingAsLowPriority()
            if (r8 != 0) goto L_0x0022
            r8 = r6
            goto L_0x0023
        L_0x0022:
            r8 = 0
        L_0x0023:
            boolean r10 = r0.mUserLocked
            if (r10 == 0) goto L_0x0035
            float r7 = r19.getGroupExpandFraction()
            int r10 = r0.getMaxAllowedVisibleChildren(r6)
            r18 = r10
            r10 = r7
            r7 = r18
            goto L_0x0036
        L_0x0035:
            r10 = 0
        L_0x0036:
            boolean r12 = r0.mChildrenExpanded
            if (r12 == 0) goto L_0x0044
            com.android.systemui.statusbar.ExpandableNotificationRow r12 = r0.mContainingNotification
            boolean r12 = r12.isGroupExpansionChanging()
            if (r12 != 0) goto L_0x0044
            r12 = r6
            goto L_0x0045
        L_0x0044:
            r12 = 0
        L_0x0045:
            r13 = r4
            r15 = r6
            r14 = 0
        L_0x0048:
            if (r14 >= r3) goto L_0x010f
            java.util.List<com.android.systemui.statusbar.ExpandableNotificationRow> r6 = r0.mChildren
            java.lang.Object r6 = r6.get(r14)
            com.android.systemui.statusbar.ExpandableNotificationRow r6 = (com.android.systemui.statusbar.ExpandableNotificationRow) r6
            if (r15 != 0) goto L_0x006f
            if (r8 == 0) goto L_0x0064
            float r4 = (float) r4
            int r11 = r0.mChildTopMargin
            float r11 = (float) r11
            int r9 = r0.mDividerHeight
            float r9 = (float) r9
            float r9 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r11, r9, r10)
            float r4 = r4 + r9
            int r4 = (int) r4
            goto L_0x008a
        L_0x0064:
            boolean r9 = r0.mChildrenExpanded
            if (r9 == 0) goto L_0x006b
            int r9 = r0.mDividerHeight
            goto L_0x006d
        L_0x006b:
            int r9 = r0.mChildTopMargin
        L_0x006d:
            int r4 = r4 + r9
            goto L_0x008a
        L_0x006f:
            if (r8 == 0) goto L_0x007f
            float r4 = (float) r4
            int r9 = r0.mNotificationTopPadding
            float r9 = (float) r9
            int r11 = r0.mDividerHeight
            float r11 = (float) r11
            float r9 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r9, r11, r10)
            float r4 = r4 + r9
            int r4 = (int) r4
            goto L_0x0089
        L_0x007f:
            boolean r9 = r0.mChildrenExpanded
            if (r9 == 0) goto L_0x0086
            int r9 = r0.mDividerHeight
            goto L_0x0088
        L_0x0086:
            int r9 = r0.mNotificationTopPadding
        L_0x0088:
            int r4 = r4 + r9
        L_0x0089:
            r15 = 0
        L_0x008a:
            com.android.systemui.statusbar.stack.ExpandableViewState r9 = r1.getViewStateForView(r6)
            int r6 = r6.getIntrinsicHeight()
            r9.height = r6
            float r11 = (float) r4
            r9.yTranslation = r11
            r11 = 0
            r9.hidden = r11
            if (r12 == 0) goto L_0x00a3
            com.android.systemui.statusbar.ExpandableNotificationRow r11 = r0.mContainingNotification
            float r11 = r11.getTranslationZ()
            goto L_0x00a4
        L_0x00a3:
            r11 = 0
        L_0x00a4:
            r9.zTranslation = r11
            boolean r11 = r2.dimmed
            r9.dimmed = r11
            boolean r11 = r2.dark
            r9.dark = r11
            boolean r11 = r2.hideSensitive
            r9.hideSensitive = r11
            boolean r11 = r2.belowSpeedBump
            r9.belowSpeedBump = r11
            r11 = 0
            r9.clipTopAmount = r11
            r11 = 0
            r9.alpha = r11
            if (r14 >= r7) goto L_0x00cd
            boolean r11 = r19.showingAsLowPriority()
            if (r11 == 0) goto L_0x00c6
            r11 = r10
            goto L_0x00c8
        L_0x00c6:
            r11 = 1065353216(0x3f800000, float:1.0)
        L_0x00c8:
            r9.alpha = r11
            r17 = r5
            goto L_0x00fd
        L_0x00cd:
            r11 = 1065353216(0x3f800000, float:1.0)
            int r16 = (r10 > r11 ? 1 : (r10 == r11 ? 0 : -1))
            if (r16 != 0) goto L_0x00f1
            if (r14 > r5) goto L_0x00f1
            int r11 = r0.mActualHeight
            float r11 = (float) r11
            r17 = r5
            float r5 = r9.yTranslation
            float r11 = r11 - r5
            int r5 = r9.height
            float r5 = (float) r5
            float r11 = r11 / r5
            r9.alpha = r11
            r5 = 1065353216(0x3f800000, float:1.0)
            float r5 = java.lang.Math.min(r5, r11)
            r11 = 0
            float r5 = java.lang.Math.max(r11, r5)
            r9.alpha = r5
            goto L_0x00fd
        L_0x00f1:
            r17 = r5
            r5 = 1
            r9.hidden = r5
            int r5 = r19.getIntrinsicHeight()
            float r5 = (float) r5
            r9.yTranslation = r5
        L_0x00fd:
            int r5 = r2.location
            r9.location = r5
            boolean r5 = r2.inShelf
            r9.inShelf = r5
            int r4 = r4 + r6
            if (r14 >= r7) goto L_0x0109
            r13 = r4
        L_0x0109:
            int r14 = r14 + 1
            r5 = r17
            goto L_0x0048
        L_0x010f:
            r5 = 1065353216(0x3f800000, float:1.0)
            android.widget.TextView r2 = r0.mOverflowNumber
            if (r2 == 0) goto L_0x01b6
            java.util.List<com.android.systemui.statusbar.ExpandableNotificationRow> r2 = r0.mChildren
            r4 = 1
            int r6 = r0.getMaxAllowedVisibleChildren(r4)
            int r3 = java.lang.Math.min(r6, r3)
            int r3 = r3 - r4
            java.lang.Object r2 = r2.get(r3)
            com.android.systemui.statusbar.ExpandableNotificationRow r2 = (com.android.systemui.statusbar.ExpandableNotificationRow) r2
            com.android.systemui.statusbar.stack.ViewState r3 = r0.mGroupOverFlowState
            com.android.systemui.statusbar.stack.ExpandableViewState r1 = r1.getViewStateForView(r2)
            r3.copyFrom(r1)
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mGroupOverFlowState
            int r3 = r0.mOverflowNumberTopPadding
            r1.paddingTop = r3
            int r3 = r0.mOverflowNumberBottomPadding
            r1.paddingBottom = r3
            android.widget.TextView r1 = r0.mOverflowNumber
            int r1 = r1.getMeasuredHeight()
            com.android.systemui.statusbar.notification.HybridNotificationView r3 = r2.getSingleLineView()
            int r3 = r3.getMeasuredHeight()
            com.android.systemui.statusbar.stack.ViewState r4 = r0.mGroupOverFlowState
            float r6 = r4.yTranslation
            int r3 = r3 - r1
            int r3 = r3 / 2
            int r1 = r0.mOverflowNumberTopMargin
            int r3 = r3 + r1
            float r1 = (float) r3
            float r6 = r6 + r1
            r4.yTranslation = r6
            com.android.systemui.statusbar.ExpandableNotificationRow r1 = r0.mContainingNotification
            boolean r1 = r1.isShowingAmbient()
            if (r1 != 0) goto L_0x0171
            boolean r1 = r0.mChildrenExpanded
            if (r1 != 0) goto L_0x0163
            goto L_0x0171
        L_0x0163:
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mGroupOverFlowState
            float r2 = r1.yTranslation
            int r3 = r0.mNotificationHeaderMargin
            float r3 = (float) r3
            float r2 = r2 + r3
            r1.yTranslation = r2
            r11 = 0
            r1.alpha = r11
            goto L_0x01b7
        L_0x0171:
            r11 = 0
            r1 = 0
            com.android.systemui.statusbar.ExpandableNotificationRow r3 = r0.mContainingNotification
            boolean r3 = r3.isShowingAmbient()
            if (r3 == 0) goto L_0x0180
            com.android.systemui.statusbar.notification.HybridNotificationView r1 = r2.getAmbientSingleLineView()
            goto L_0x0188
        L_0x0180:
            boolean r3 = r0.mUserLocked
            if (r3 == 0) goto L_0x0188
            com.android.systemui.statusbar.notification.HybridNotificationView r1 = r2.getSingleLineView()
        L_0x0188:
            if (r1 == 0) goto L_0x01b7
            android.widget.TextView r3 = r1.getTextView()
            int r4 = r3.getVisibility()
            r6 = 8
            if (r4 != r6) goto L_0x019a
            android.widget.TextView r3 = r1.getTitleView()
        L_0x019a:
            int r4 = r3.getVisibility()
            if (r4 != r6) goto L_0x01a1
            goto L_0x01a2
        L_0x01a1:
            r1 = r3
        L_0x01a2:
            com.android.systemui.statusbar.stack.ViewState r3 = r0.mGroupOverFlowState
            float r4 = r3.yTranslation
            float r2 = com.android.systemui.statusbar.notification.NotificationUtils.getRelativeYOffset(r1, r2)
            float r4 = r4 + r2
            r3.yTranslation = r4
            com.android.systemui.statusbar.stack.ViewState r2 = r0.mGroupOverFlowState
            float r1 = r1.getAlpha()
            r2.alpha = r1
            goto L_0x01b7
        L_0x01b6:
            r11 = 0
        L_0x01b7:
            android.view.NotificationHeaderView r1 = r0.mNotificationHeader
            if (r1 == 0) goto L_0x01db
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mHeaderViewState
            if (r1 != 0) goto L_0x01c6
            com.android.systemui.statusbar.stack.ViewState r1 = new com.android.systemui.statusbar.stack.ViewState
            r1.<init>()
            r0.mHeaderViewState = r1
        L_0x01c6:
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mHeaderViewState
            android.view.NotificationHeaderView r2 = r0.mNotificationHeader
            r1.initFrom(r2)
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mHeaderViewState
            if (r12 == 0) goto L_0x01d8
            com.android.systemui.statusbar.ExpandableNotificationRow r2 = r0.mContainingNotification
            float r2 = r2.getTranslationZ()
            goto L_0x01d9
        L_0x01d8:
            r2 = r11
        L_0x01d9:
            r1.zTranslation = r2
        L_0x01db:
            android.widget.TextView r1 = r0.mCollapsedButton
            if (r1 == 0) goto L_0x0205
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mCollapseButtonViewState
            if (r1 != 0) goto L_0x01ea
            com.android.systemui.statusbar.stack.ViewState r1 = new com.android.systemui.statusbar.stack.ViewState
            r1.<init>()
            r0.mCollapseButtonViewState = r1
        L_0x01ea:
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mCollapseButtonViewState
            android.widget.TextView r2 = r0.mCollapsedButton
            r1.initFrom(r2)
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mCollapseButtonViewState
            boolean r2 = com.android.systemui.miui.statusbar.notification.NotificationUtil.showGoogleStyle()
            r1.hidden = r2
            com.android.systemui.statusbar.stack.ViewState r1 = r0.mCollapseButtonViewState
            float r2 = (float) r13
            r1.yTranslation = r2
            boolean r0 = r0.mChildrenExpanded
            if (r0 == 0) goto L_0x0203
            r11 = r5
        L_0x0203:
            r1.alpha = r11
        L_0x0205:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.stack.NotificationChildrenContainer.getState(com.android.systemui.statusbar.stack.StackScrollState, com.android.systemui.statusbar.stack.ExpandableViewState):void");
    }

    private int getMaxAllowedVisibleChildren() {
        return getMaxAllowedVisibleChildren(false);
    }

    private int getMaxAllowedVisibleChildren(boolean z) {
        if (this.mContainingNotification.isShowingAmbient()) {
            return 3;
        }
        if (!z && (this.mChildrenExpanded || this.mContainingNotification.isUserLocked())) {
            return 8;
        }
        if (this.mIsLowPriority) {
            return 5;
        }
        if (this.mContainingNotification.isOnKeyguard() || (!this.mContainingNotification.isExpanded() && !this.mContainingNotification.isHeadsUp())) {
            return 3;
        }
        return 5;
    }

    public void applyState() {
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            expandableNotificationRow.getViewState().applyToView(expandableNotificationRow);
            expandableNotificationRow.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
        }
        ViewState viewState = this.mGroupOverFlowState;
        if (viewState != null) {
            viewState.applyToView(this.mOverflowNumber);
            this.mNeverAppliedGroupState = false;
        }
        ViewState viewState2 = this.mHeaderViewState;
        if (viewState2 != null) {
            viewState2.applyToView(this.mNotificationHeader);
        }
        ViewState viewState3 = this.mCollapseButtonViewState;
        if (viewState3 != null) {
            viewState3.applyToView(this.mCollapsedButton);
        }
        ImageView imageView = this.mAppIcon;
        if (imageView != null) {
            imageView.setVisibility(0);
        }
        if (NotificationUtil.showGoogleStyle()) {
            this.mNotificationHeader.findViewById(16908957).setVisibility(0);
        }
        updateChildrenClipping();
    }

    private void updateChildrenClipping() {
        boolean z;
        int i;
        int size = this.mChildren.size();
        int actualHeight = this.mContainingNotification.getActualHeight() - this.mClipBottomAmount;
        for (int i2 = 0; i2 < size; i2++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
            if (expandableNotificationRow.getVisibility() != 8) {
                float translationY = expandableNotificationRow.getTranslationY();
                float actualHeight2 = ((float) expandableNotificationRow.getActualHeight()) + translationY;
                float f = (float) actualHeight;
                boolean z2 = true;
                if (translationY > f) {
                    i = 0;
                    z = false;
                } else {
                    i = actualHeight2 > f ? (int) (actualHeight2 - f) : 0;
                    z = true;
                }
                boolean z3 = expandableNotificationRow.getVisibility() == 0;
                if (i2 >= getMaxAllowedVisibleChildren()) {
                    z2 = false;
                }
                boolean z4 = z & z2;
                if (z4 != z3) {
                    expandableNotificationRow.setVisibility(z4 ? 0 : 4);
                }
                expandableNotificationRow.setClipBottomAmount(i);
            }
        }
    }

    public void startAnimationToState(AnimationProperties animationProperties) {
        int size = this.mChildren.size();
        while (true) {
            size--;
            if (size < 0) {
                break;
            }
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(size);
            expandableNotificationRow.getViewState().animateTo(expandableNotificationRow, animationProperties);
            expandableNotificationRow.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
        }
        TextView textView = this.mOverflowNumber;
        if (textView != null) {
            if (this.mNeverAppliedGroupState) {
                ViewState viewState = this.mGroupOverFlowState;
                float f = viewState.alpha;
                viewState.alpha = 0.0f;
                viewState.applyToView(textView);
                this.mGroupOverFlowState.alpha = f;
                this.mNeverAppliedGroupState = false;
            }
            this.mGroupOverFlowState.animateTo(this.mOverflowNumber, animationProperties);
        }
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            this.mHeaderViewState.applyToView(notificationHeaderView);
        }
        TextView textView2 = this.mCollapsedButton;
        if (textView2 != null) {
            this.mCollapseButtonViewState.animateTo(textView2, animationProperties);
        }
        updateChildrenClipping();
    }

    public ExpandableNotificationRow getViewAtPosition(float f) {
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            float translationY = expandableNotificationRow.getTranslationY();
            float clipTopAmount = ((float) expandableNotificationRow.getClipTopAmount()) + translationY;
            float actualHeight = translationY + ((float) expandableNotificationRow.getActualHeight());
            if (f >= clipTopAmount && f <= actualHeight) {
                return expandableNotificationRow;
            }
        }
        return null;
    }

    public void setChildrenExpanded(boolean z) {
        this.mChildrenExpanded = z;
        updateExpansionStates();
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            notificationHeaderView.setExpanded(z);
        }
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            this.mChildren.get(i).setChildrenExpanded(z, false);
        }
    }

    public void setContainingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mContainingNotification = expandableNotificationRow;
        initHeaderUtil();
    }

    private void initHeaderUtil() {
        if (NotificationUtil.showMiuiStyle()) {
            this.mHeaderUtil = null;
        } else if (this.mHeaderUtil == null) {
            this.mHeaderUtil = new NotificationHeaderUtil(this.mContainingNotification);
        }
    }

    public NotificationHeaderView getHeaderView() {
        return this.mNotificationHeader;
    }

    public NotificationHeaderView getLowPriorityHeaderView() {
        return this.mNotificationHeaderLowPriority;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return super.onInterceptTouchEvent(motionEvent);
    }

    @VisibleForTesting
    public ViewGroup getCurrentHeaderView() {
        return this.mCurrentHeader;
    }

    public void notifyShowAmbientChanged() {
        updateHeaderVisibility(false);
    }

    /* access modifiers changed from: private */
    public void updateHeaderVisibility(boolean z) {
        NotificationHeaderView notificationHeaderView;
        NotificationHeaderView calculateDesiredHeader;
        if (!NotificationUtil.showMiuiStyle() && (notificationHeaderView = this.mCurrentHeader) != (calculateDesiredHeader = calculateDesiredHeader())) {
            NotificationHeaderView notificationHeaderView2 = this.mNotificationHeaderAmbient;
            if (calculateDesiredHeader == notificationHeaderView2 || notificationHeaderView == notificationHeaderView2) {
                z = false;
            }
            if (z) {
                if (calculateDesiredHeader == null || notificationHeaderView == null) {
                    z = false;
                } else {
                    notificationHeaderView.setVisibility(0);
                    calculateDesiredHeader.setVisibility(0);
                    NotificationViewWrapper wrapperForView = getWrapperForView(calculateDesiredHeader);
                    NotificationViewWrapper wrapperForView2 = getWrapperForView(notificationHeaderView);
                    wrapperForView.transformFrom(wrapperForView2);
                    wrapperForView2.transformTo((TransformableView) wrapperForView, (Runnable) new Runnable() {
                        public void run() {
                            NotificationChildrenContainer.this.updateHeaderVisibility(false);
                        }
                    });
                    startChildAlphaAnimations(calculateDesiredHeader == this.mNotificationHeader);
                }
            }
            if (!z) {
                if (calculateDesiredHeader != null) {
                    getWrapperForView(calculateDesiredHeader).setVisible(true);
                    calculateDesiredHeader.setVisibility(0);
                }
                if (notificationHeaderView != null) {
                    NotificationViewWrapper wrapperForView3 = getWrapperForView(notificationHeaderView);
                    if (wrapperForView3 != null) {
                        wrapperForView3.setVisible(false);
                    }
                    notificationHeaderView.setVisibility(4);
                }
            }
            resetHeaderVisibilityIfNeeded(this.mNotificationHeader, calculateDesiredHeader);
            resetHeaderVisibilityIfNeeded(this.mNotificationHeaderAmbient, calculateDesiredHeader);
            resetHeaderVisibilityIfNeeded(this.mNotificationHeaderLowPriority, calculateDesiredHeader);
            this.mCurrentHeader = calculateDesiredHeader;
        }
    }

    private void resetHeaderVisibilityIfNeeded(View view, View view2) {
        if (!NotificationUtil.showMiuiStyle() && view != null) {
            if (!(view == this.mCurrentHeader || view == view2)) {
                getWrapperForView(view).setVisible(false);
                view.setVisibility(4);
            }
            if (view == view2 && view.getVisibility() != 0) {
                getWrapperForView(view).setVisible(true);
                view.setVisibility(0);
            }
        }
    }

    private ViewGroup calculateDesiredHeader() {
        if (this.mContainingNotification.isShowingAmbient()) {
            return this.mNotificationHeaderAmbient;
        }
        if (showingAsLowPriority()) {
            return this.mNotificationHeaderLowPriority;
        }
        return this.mNotificationHeader;
    }

    private void startChildAlphaAnimations(boolean z) {
        float f = z ? 1.0f : 0.0f;
        float f2 = 1.0f - f;
        int size = this.mChildren.size();
        int i = 0;
        while (i < size && i < 5) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i);
            expandableNotificationRow.setAlpha(f2);
            ViewState viewState = new ViewState();
            viewState.initFrom(expandableNotificationRow);
            viewState.alpha = f;
            ALPHA_FADE_IN.setDelay((long) (i * 50));
            viewState.animateTo(expandableNotificationRow, ALPHA_FADE_IN);
            i++;
        }
    }

    private void updateHeaderTransformation() {
        if (this.mUserLocked && showingAsLowPriority()) {
            float groupExpandFraction = getGroupExpandFraction();
            this.mNotificationHeaderWrapper.transformFrom(this.mNotificationHeaderWrapperLowPriority, groupExpandFraction);
            this.mNotificationHeader.setVisibility(0);
            this.mNotificationHeaderWrapperLowPriority.transformTo((TransformableView) this.mNotificationHeaderWrapper, groupExpandFraction);
        }
    }

    private NotificationViewWrapper getWrapperForView(View view) {
        if (view == this.mNotificationHeader) {
            return this.mNotificationHeaderWrapper;
        }
        if (view == this.mNotificationHeaderAmbient) {
            return this.mNotificationHeaderWrapperAmbient;
        }
        return this.mNotificationHeaderWrapperLowPriority;
    }

    public int getMaxContentHeight() {
        int i;
        if (showingAsLowPriority()) {
            return getMinHeight(5, true);
        }
        int i2 = this.mNotificationHeaderMargin + this.mNotificationTopPadding;
        int size = this.mChildren.size();
        int i3 = 0;
        for (int i4 = 0; i4 < size && i3 < 8; i4++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i4);
            if (expandableNotificationRow.isExpanded(true)) {
                i = expandableNotificationRow.getMaxExpandHeight();
            } else {
                i = expandableNotificationRow.getShowingLayout().getMinHeight(true);
            }
            i2 = (int) (((float) i2) + ((float) i));
            i3++;
        }
        return i3 > 0 ? i2 + (i3 * this.mDividerHeight) : i2;
    }

    public void setActualHeight(int i) {
        int minHeight;
        if (this.mUserLocked) {
            this.mActualHeight = i;
            float groupExpandFraction = getGroupExpandFraction();
            boolean showingAsLowPriority = showingAsLowPriority();
            updateHeaderTransformation();
            int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
            int size = this.mChildren.size();
            for (int i2 = 0; i2 < size; i2++) {
                ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
                if (showingAsLowPriority) {
                    minHeight = expandableNotificationRow.getShowingLayout().getMinHeight(false);
                } else if (expandableNotificationRow.isExpanded(true)) {
                    minHeight = expandableNotificationRow.getMaxExpandHeight();
                } else {
                    minHeight = expandableNotificationRow.getShowingLayout().getMinHeight(true);
                }
                float f = (float) minHeight;
                if (i2 < maxAllowedVisibleChildren) {
                    expandableNotificationRow.setActualHeight((int) NotificationUtils.interpolate((float) expandableNotificationRow.getShowingLayout().getMinHeight(false), f, groupExpandFraction), false);
                } else {
                    expandableNotificationRow.setActualHeight((int) f, false);
                }
            }
        }
    }

    public float getGroupExpandFraction() {
        int i;
        if (showingAsLowPriority()) {
            i = getMaxContentHeight();
        } else {
            i = getVisibleChildrenExpandHeight();
        }
        int collapsedHeight = getCollapsedHeight();
        return Math.max(0.0f, Math.min(1.0f, ((float) (this.mActualHeight - collapsedHeight)) / ((float) (i - collapsedHeight))));
    }

    private int getVisibleChildrenExpandHeight() {
        int i;
        int i2 = this.mNotificationHeaderMargin + this.mNotificationTopPadding + this.mDividerHeight;
        int size = this.mChildren.size();
        int maxAllowedVisibleChildren = getMaxAllowedVisibleChildren(true);
        int i3 = 0;
        for (int i4 = 0; i4 < size && i3 < maxAllowedVisibleChildren; i4++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i4);
            if (expandableNotificationRow.isExpanded(true)) {
                i = expandableNotificationRow.getMaxExpandHeight();
            } else {
                i = expandableNotificationRow.getShowingLayout().getMinHeight(true);
            }
            i2 = (int) (((float) i2) + ((float) i));
            i3++;
        }
        return i2;
    }

    public int getMinHeight() {
        boolean isShowingAmbient = this.mContainingNotification.isShowingAmbient();
        return getMinHeight(3, false);
    }

    public int getCollapsedHeight() {
        return getMinHeight(getMaxAllowedVisibleChildren(true), false);
    }

    private int getMinHeight(int i, boolean z) {
        if (!z && showingAsLowPriority()) {
            return this.mNotificationHeaderLowPriority.getHeight();
        }
        int i2 = this.mNotificationHeaderMargin;
        int size = this.mChildren.size();
        boolean z2 = true;
        int i3 = 0;
        for (int i4 = 0; i4 < size && i3 < i; i4++) {
            if (!z2) {
                i2 += this.mChildTopMargin;
            } else {
                z2 = false;
            }
            i2 += this.mChildren.get(i4).getSingleLineView().getHeight();
            i3++;
        }
        return (int) (((float) i2) + this.mCollapsedBottomMargin);
    }

    public boolean showingAsLowPriority() {
        return this.mIsLowPriority && !this.mContainingNotification.isExpanded();
    }

    public void setDark(boolean z, boolean z2, long j) {
        TextView textView = this.mOverflowNumber;
        if (textView != null) {
            this.mHybridGroupManager.setOverflowNumberDark(textView, z, z2, j);
        }
        this.mNotificationHeaderWrapper.setDark(z, z2, j);
    }

    public void reInflateViews(View.OnClickListener onClickListener, StatusBarNotification statusBarNotification) {
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            removeView(notificationHeaderView);
            this.mNotificationHeader = null;
        }
        NotificationHeaderView notificationHeaderView2 = this.mNotificationHeaderLowPriority;
        if (notificationHeaderView2 != null) {
            removeView(notificationHeaderView2);
            this.mNotificationHeaderLowPriority = null;
        }
        ViewGroup viewGroup = this.mNotificationHeaderAmbient;
        if (viewGroup != null) {
            removeView(viewGroup);
            this.mNotificationHeaderAmbient = null;
        }
        recreateNotificationHeader(onClickListener);
        initDimens();
        removeView(this.mOverflowNumber);
        this.mOverflowNumber = null;
        this.mGroupOverFlowState = null;
        updateGroupOverflow();
        TextView textView = this.mCollapsedButton;
        if (textView != null) {
            removeView(textView);
            this.mCollapsedButton = null;
            rebuildCollapseButton();
        }
    }

    public void setUserLocked(boolean z) {
        this.mUserLocked = z;
        if (!z) {
            updateHeaderVisibility(false);
        }
        int size = this.mChildren.size();
        for (int i = 0; i < size; i++) {
            this.mChildren.get(i).setUserLocked(z && !showingAsLowPriority());
        }
    }

    public void onNotificationUpdated() {
        this.mHybridGroupManager.setOverflowNumberColor(this.mOverflowNumber, getContext().getColor(R.color.notification_overflow_number_color), getContext().getColor(R.color.notification_overflow_number_color));
    }

    public int getPositionInLinearLayout(View view) {
        int i = this.mNotificationHeaderMargin + this.mNotificationTopPadding;
        for (int i2 = 0; i2 < this.mChildren.size(); i2++) {
            ExpandableNotificationRow expandableNotificationRow = this.mChildren.get(i2);
            boolean z = expandableNotificationRow.getVisibility() != 8;
            if (z) {
                i += this.mDividerHeight;
            }
            if (expandableNotificationRow == view) {
                return i;
            }
            if (z) {
                i += expandableNotificationRow.getIntrinsicHeight();
            }
        }
        return 0;
    }

    public void setIconsVisible(boolean z) {
        NotificationViewWrapper notificationViewWrapper = this.mNotificationHeaderWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.getNotificationHeader().getIcon().setForceHidden(!z);
        }
        NotificationViewWrapper notificationViewWrapper2 = this.mNotificationHeaderWrapperLowPriority;
        if (notificationViewWrapper2 != null) {
            notificationViewWrapper2.getNotificationHeader().getIcon().setForceHidden(!z);
        }
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateChildrenClipping();
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
        if (this.mContainingNotification != null) {
            recreateLowPriorityHeader((Notification.Builder) null);
            updateHeaderVisibility(false);
        }
        boolean z2 = this.mUserLocked;
        if (z2) {
            setUserLocked(z2);
        }
    }

    public NotificationHeaderView getVisibleHeader() {
        return showingAsLowPriority() ? this.mNotificationHeaderLowPriority : this.mNotificationHeader;
    }

    public void onExpansionChanged() {
        if (this.mIsLowPriority) {
            boolean z = this.mUserLocked;
            if (z) {
                setUserLocked(z);
            }
            updateHeaderVisibility(true);
        }
    }

    public int getCollapsedButtonHeight() {
        if (NotificationUtil.showGoogleStyle()) {
            return 0;
        }
        return this.mCollapsedButton.getMeasuredHeight();
    }

    @VisibleForTesting
    public boolean isUserLocked() {
        return this.mUserLocked;
    }
}
