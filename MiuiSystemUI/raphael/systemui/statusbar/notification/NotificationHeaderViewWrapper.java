package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Icon;
import android.util.ArraySet;
import android.util.Log;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.Chronometer;
import android.widget.DateTimeView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.miui.AppIconsManager;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import java.util.Stack;
import miui.securityspace.XSpaceUserHandle;

public class NotificationHeaderViewWrapper extends NotificationViewWrapper {
    /* access modifiers changed from: private */
    public static final Interpolator LOW_PRIORITY_HEADER_CLOSE = new PathInterpolator(0.4f, 0.0f, 0.7f, 1.0f);
    protected TextView mAppNameText;
    protected View mChronometer;
    protected int mColor;
    protected ImageView mExpandButton;
    protected boolean mExpandable;
    protected int mGoogleContentMarginBottom;
    protected int mGoogleContentMarginEnd;
    protected int mGoogleContentMarginStart;
    protected int mGoogleContentMarginTop;
    private TextView mHeaderText;
    /* access modifiers changed from: private */
    public TextView mHeaderTextDivider;
    protected ImageView mIcon;
    /* access modifiers changed from: private */
    public boolean mIsLowPriority;
    /* access modifiers changed from: private */
    public int mMiniViewHeight;
    protected int mMiuiAppIconMargin;
    protected int mMiuiAppIconSize;
    protected int mMiuiContentMarginBottom;
    protected int mMiuiContentMarginEnd;
    protected int mMiuiContentMarginStart;
    protected int mMiuiContentMarginTop;
    protected NotificationHeaderView mNotificationHeader;
    protected boolean mShowMiniView;
    private StyleProcessor mStyleProcessor;
    protected DateTimeView mTime;
    /* access modifiers changed from: private */
    public int mTimeTextColor;
    /* access modifiers changed from: private */
    public int mTimeTextSize;
    /* access modifiers changed from: private */
    public boolean mTransformLowPriorityTitle;
    protected final ViewTransformationHelper mTransformationHelper = new ViewTransformationHelper();
    protected ImageView mWorkProfileImage;
    /* access modifiers changed from: private */
    public ImageView mXSpaceIcon;

    protected NotificationHeaderViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mTransformationHelper.setCustomTransformation(new CustomInterpolatorTransformation(1) {
            public Interpolator getCustomInterpolator(int i, boolean z) {
                boolean z2 = NotificationHeaderViewWrapper.this.mView instanceof NotificationHeaderView;
                if (i != 16) {
                    return null;
                }
                if ((!z2 || z) && (z2 || !z)) {
                    return NotificationHeaderViewWrapper.LOW_PRIORITY_HEADER_CLOSE;
                }
                return Interpolators.LINEAR_OUT_SLOW_IN;
            }

            /* access modifiers changed from: protected */
            public boolean hasCustomTransformation() {
                return NotificationHeaderViewWrapper.this.mIsLowPriority && NotificationHeaderViewWrapper.this.mTransformLowPriorityTitle;
            }
        }, 1);
        resolveHeaderViews();
        initResources();
        handleHeaderViews();
    }

    private void initResources() {
        Resources resources = this.mContext.getResources();
        this.mMiuiContentMarginStart = resources.getDimensionPixelSize(17105320);
        this.mMiuiContentMarginTop = resources.getDimensionPixelSize(17105321);
        this.mMiuiContentMarginEnd = resources.getDimensionPixelSize(17105319);
        this.mMiuiContentMarginBottom = resources.getDimensionPixelSize(17105318);
        this.mGoogleContentMarginStart = resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_start);
        this.mGoogleContentMarginEnd = resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_end);
        this.mGoogleContentMarginTop = resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_top);
        this.mGoogleContentMarginBottom = resources.getDimensionPixelSize(R.dimen.google_notification_content_margin_bottom);
        this.mMiuiAppIconSize = resources.getDimensionPixelSize(R.dimen.notification_app_icon_size);
        this.mMiuiAppIconMargin = resources.getDimensionPixelSize(R.dimen.notification_app_icon_margin);
        this.mMiniViewHeight = resources.getDimensionPixelSize(R.dimen.notification_low_priority_height);
        this.mTimeTextColor = this.mContext.getColor(R.color.notification_time_color);
        this.mTimeTextSize = resources.getDimensionPixelSize(R.dimen.notification_time_text_size);
        this.mColor = this.mNotificationHeader.getOriginalNotificationColor();
        boolean z = true;
        this.mShowMiniView = this.mRow.isLowPriority() && this.mView == this.mNotificationHeader;
        if (this.mRow.isMediaNotification() || !showMiuiStyle()) {
            z = false;
        }
        this.mStyleProcessor = z ? new MiuiStyleProcessor() : new GoogleStyleProcessor();
    }

    private void resolveHeaderViews() {
        this.mNotificationHeader = this.mView.findViewById(16909174);
        this.mIcon = (ImageView) this.mNotificationHeader.findViewById(16908294);
        this.mAppNameText = (TextView) this.mNotificationHeader.findViewById(16908733);
        this.mHeaderText = (TextView) this.mNotificationHeader.findViewById(16908981);
        this.mHeaderTextDivider = (TextView) this.mNotificationHeader.findViewById(16908982);
        this.mTime = this.mNotificationHeader.findViewById(16909475);
        this.mChronometer = this.mNotificationHeader.findViewById(16908812);
        this.mExpandButton = (ImageView) this.mNotificationHeader.findViewById(16908902);
        this.mXSpaceIcon = (ImageView) this.mNotificationHeader.findViewById(16909584);
        this.mWorkProfileImage = (ImageView) this.mNotificationHeader.findViewById(16909272);
    }

    private void handleHeaderViews() {
        if (NotificationViewWrapper.DEBUG) {
            Log.d("NViewWrapper", "handleHeaderViews");
        }
        this.mStyleProcessor.handleHeader();
        this.mStyleProcessor.handleIcon();
        this.mStyleProcessor.handleAppNameText();
        this.mStyleProcessor.handleHeaderText();
        this.mStyleProcessor.handleHeaderTextDivider();
        this.mStyleProcessor.handleTimeChronometer();
        this.mStyleProcessor.handleExpandButton();
        this.mStyleProcessor.handleXSpaceIcon();
        this.mStyleProcessor.handleWorkProfile();
        this.mStyleProcessor.handleDivider();
    }

    /* access modifiers changed from: protected */
    public boolean showMiuiStyle() {
        return NotificationUtil.showMiuiStyle();
    }

    /* access modifiers changed from: protected */
    public boolean isHeaderViewRemoved(View view) {
        return this.mNotificationHeader.indexOfChild(view) < 0;
    }

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
        return this.mExpandable;
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        this.mIsLowPriority = expandableNotificationRow.isLowPriority();
        this.mTransformLowPriorityTitle = !expandableNotificationRow.isChildInGroup() && !expandableNotificationRow.isSummaryWithChildren();
        ArraySet<View> allTransformingViews = this.mTransformationHelper.getAllTransformingViews();
        handleHeaderViews();
        updateTransformedTypes();
        addRemainingTransformTypes();
        updateCropToPaddingForImageViews();
        Icon smallIcon = NotificationUtil.getSmallIcon(this.mContext, expandableNotificationRow.getStatusBarNotification());
        if (!isHeaderViewRemoved(this.mIcon)) {
            this.mIcon.setTag(R.id.image_icon_tag, smallIcon);
        }
        ImageView imageView = this.mWorkProfileImage;
        if (imageView != null) {
            imageView.setTag(R.id.image_icon_tag, smallIcon);
        }
        ImageView imageView2 = this.mXSpaceIcon;
        if (imageView2 != null) {
            imageView2.setTag(R.id.image_icon_tag, smallIcon);
        }
        ArraySet<View> allTransformingViews2 = this.mTransformationHelper.getAllTransformingViews();
        for (int i = 0; i < allTransformingViews.size(); i++) {
            View valueAt = allTransformingViews.valueAt(i);
            if (!allTransformingViews2.contains(valueAt)) {
                this.mTransformationHelper.resetTransformedView(valueAt);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addRemainingTransformTypes() {
        this.mTransformationHelper.addRemainingTransformTypes(this.mView);
    }

    private void updateCropToPaddingForImageViews() {
        Stack stack = new Stack();
        stack.push(this.mView);
        while (!stack.isEmpty()) {
            View view = (View) stack.pop();
            if (view instanceof ImageView) {
                ((ImageView) view).setCropToPadding(true);
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    stack.push(viewGroup.getChildAt(i));
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        this.mTransformationHelper.reset();
        if (!isHeaderViewRemoved(this.mIcon)) {
            this.mTransformationHelper.addTransformedView(0, this.mIcon);
        }
        if (this.mIsLowPriority) {
            this.mTransformationHelper.addTransformedView(1, this.mHeaderText);
        }
    }

    public void updateExpandability(boolean z, View.OnClickListener onClickListener) {
        this.mExpandable = z;
        this.mStyleProcessor.handleExpandButton();
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (!z) {
            onClickListener = null;
        }
        notificationHeaderView.setOnClickListener(onClickListener);
    }

    public NotificationHeaderView getNotificationHeader() {
        return this.mNotificationHeader;
    }

    public TransformState getCurrentState(int i) {
        return this.mTransformationHelper.getCurrentState(i);
    }

    public void transformTo(TransformableView transformableView, Runnable runnable) {
        this.mTransformationHelper.transformTo(transformableView, runnable);
    }

    public void transformTo(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformTo(transformableView, f);
    }

    public void transformFrom(TransformableView transformableView) {
        this.mTransformationHelper.transformFrom(transformableView);
    }

    public void transformFrom(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformFrom(transformableView, f);
    }

    public void setIsChildInGroup(boolean z) {
        super.setIsChildInGroup(z);
        this.mTransformLowPriorityTitle = !z;
    }

    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mTransformationHelper.setVisible(z);
    }

    private abstract class StyleProcessor {
        /* access modifiers changed from: package-private */
        public void handleDivider() {
        }

        /* access modifiers changed from: package-private */
        public abstract void handleExpandButton();

        /* access modifiers changed from: package-private */
        public abstract void handleHeader();

        /* access modifiers changed from: package-private */
        public void handleHeaderText() {
        }

        /* access modifiers changed from: package-private */
        public void handleTimeChronometer() {
        }

        /* access modifiers changed from: package-private */
        public void handleWorkProfile() {
        }

        /* access modifiers changed from: package-private */
        public abstract void handleXSpaceIcon();

        private StyleProcessor() {
        }

        /* access modifiers changed from: package-private */
        public void handleIcon() {
            ExpandedNotification expandedNotification = NotificationHeaderViewWrapper.this.mRow.getEntry().notification;
            if (NotificationUtil.shouldSubstituteSmallIcon(expandedNotification)) {
                NotificationHeaderViewWrapper.this.mIcon.setImageDrawable(((AppIconsManager) Dependency.get(AppIconsManager.class)).getAppIcon(NotificationHeaderViewWrapper.this.mContext, expandedNotification.getPackageName(), expandedNotification.getUser().getIdentifier()));
            }
        }

        /* access modifiers changed from: package-private */
        public void handleAppNameText() {
            ExpandedNotification expandedNotification = NotificationHeaderViewWrapper.this.mRow.getEntry().notification;
            if (NotificationHeaderViewWrapper.this.mRow.getEntry().notification.isSubstituteNotification()) {
                NotificationHeaderViewWrapper.this.mAppNameText.setText(expandedNotification.getAppName());
            }
        }

        /* access modifiers changed from: package-private */
        public void handleHeaderTextDivider() {
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            if (notificationHeaderViewWrapper.mShowMiniView) {
                notificationHeaderViewWrapper.mHeaderTextDivider.setVisibility(8);
            }
        }
    }

    private class MiuiStyleProcessor extends StyleProcessor {
        private MiuiStyleProcessor() {
            super();
        }

        /* access modifiers changed from: package-private */
        public void handleHeader() {
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            if (notificationHeaderViewWrapper.mShowMiniView) {
                NotificationHeaderView notificationHeaderView = notificationHeaderViewWrapper.mNotificationHeader;
                int i = notificationHeaderViewWrapper.mMiuiAppIconMargin;
                notificationHeaderView.setPadding(i, i, notificationHeaderViewWrapper.mMiuiContentMarginEnd, i);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationHeaderViewWrapper.this.mNotificationHeader.getLayoutParams();
                layoutParams.width = -1;
                layoutParams.height = NotificationHeaderViewWrapper.this.mMiniViewHeight;
                NotificationHeaderViewWrapper.this.mNotificationHeader.setLayoutParams(layoutParams);
                NotificationHeaderViewWrapper.this.mNotificationHeader.setVisibility(0);
                return;
            }
            notificationHeaderViewWrapper.mNotificationHeader.setVisibility(8);
        }

        /* access modifiers changed from: package-private */
        public void handleIcon() {
            super.handleIcon();
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            if (notificationHeaderViewWrapper.mShowMiniView) {
                ViewGroup.LayoutParams layoutParams = notificationHeaderViewWrapper.mIcon.getLayoutParams();
                NotificationHeaderViewWrapper notificationHeaderViewWrapper2 = NotificationHeaderViewWrapper.this;
                int i = notificationHeaderViewWrapper2.mMiuiAppIconSize;
                layoutParams.width = i;
                layoutParams.height = i;
                notificationHeaderViewWrapper2.mIcon.setLayoutParams(layoutParams);
            } else if (!notificationHeaderViewWrapper.isHeaderViewRemoved(notificationHeaderViewWrapper.mIcon)) {
                NotificationHeaderViewWrapper notificationHeaderViewWrapper3 = NotificationHeaderViewWrapper.this;
                notificationHeaderViewWrapper3.mNotificationHeader.removeView(notificationHeaderViewWrapper3.mIcon);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleTimeChronometer() {
            processTimeText(NotificationHeaderViewWrapper.this.mTime);
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            if (notificationHeaderViewWrapper.mShowMiniView) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) notificationHeaderViewWrapper.mTime.getLayoutParams();
                marginLayoutParams.rightMargin = 0;
                NotificationHeaderViewWrapper.this.mTime.setLayoutParams(marginLayoutParams);
            } else if (!notificationHeaderViewWrapper.isHeaderViewRemoved(notificationHeaderViewWrapper.mTime)) {
                NotificationHeaderViewWrapper notificationHeaderViewWrapper2 = NotificationHeaderViewWrapper.this;
                notificationHeaderViewWrapper2.mNotificationHeader.removeView(notificationHeaderViewWrapper2.mTime);
            }
            View view = NotificationHeaderViewWrapper.this.mChronometer;
            if (view instanceof Chronometer) {
                processTimeText(view);
                NotificationHeaderViewWrapper notificationHeaderViewWrapper3 = NotificationHeaderViewWrapper.this;
                if (notificationHeaderViewWrapper3.mShowMiniView) {
                    ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) notificationHeaderViewWrapper3.mChronometer.getLayoutParams();
                    marginLayoutParams2.rightMargin = 0;
                    NotificationHeaderViewWrapper.this.mChronometer.setLayoutParams(marginLayoutParams2);
                } else if (!notificationHeaderViewWrapper3.isHeaderViewRemoved(notificationHeaderViewWrapper3.mChronometer)) {
                    NotificationHeaderViewWrapper notificationHeaderViewWrapper4 = NotificationHeaderViewWrapper.this;
                    notificationHeaderViewWrapper4.mNotificationHeader.removeView(notificationHeaderViewWrapper4.mChronometer);
                }
            }
        }

        private void processTimeText(View view) {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextColor(NotificationHeaderViewWrapper.this.mTimeTextColor);
                textView.setTextSize(0, (float) NotificationHeaderViewWrapper.this.mTimeTextSize);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleExpandButton() {
            int i = NotificationHeaderViewWrapper.this.mShowingType == NotificationViewWrapper.TYPE_SHOWING.TYPE_EXPANDED ? R.drawable.ic_collapse_notification : R.drawable.ic_expand_notification;
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            notificationHeaderViewWrapper.mExpandButton.setImageDrawable(notificationHeaderViewWrapper.mContext.getDrawable(i));
            NotificationHeaderViewWrapper notificationHeaderViewWrapper2 = NotificationHeaderViewWrapper.this;
            if (notificationHeaderViewWrapper2.mShowMiniView) {
                notificationHeaderViewWrapper2.mNotificationHeader.setShowExpandButtonAtEnd(true);
                NotificationHeaderViewWrapper.this.mExpandButton.setVisibility(0);
            } else if (!notificationHeaderViewWrapper2.isHeaderViewRemoved(notificationHeaderViewWrapper2.mExpandButton)) {
                NotificationHeaderViewWrapper notificationHeaderViewWrapper3 = NotificationHeaderViewWrapper.this;
                notificationHeaderViewWrapper3.mNotificationHeader.removeView(notificationHeaderViewWrapper3.mExpandButton);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleXSpaceIcon() {
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            if (!notificationHeaderViewWrapper.isHeaderViewRemoved(notificationHeaderViewWrapper.mExpandButton)) {
                NotificationHeaderViewWrapper notificationHeaderViewWrapper2 = NotificationHeaderViewWrapper.this;
                notificationHeaderViewWrapper2.mNotificationHeader.removeView(notificationHeaderViewWrapper2.mExpandButton);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleWorkProfile() {
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            if (!notificationHeaderViewWrapper.isHeaderViewRemoved(notificationHeaderViewWrapper.mWorkProfileImage)) {
                NotificationHeaderViewWrapper notificationHeaderViewWrapper2 = NotificationHeaderViewWrapper.this;
                notificationHeaderViewWrapper2.mNotificationHeader.removeView(notificationHeaderViewWrapper2.mWorkProfileImage);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleDivider() {
            NotificationHeaderViewWrapper.this.mNotificationHeader.findViewById(16908982).setVisibility(8);
            NotificationHeaderViewWrapper.this.mNotificationHeader.findViewById(16909479).setVisibility(8);
        }
    }

    private class GoogleStyleProcessor extends StyleProcessor {
        private GoogleStyleProcessor() {
            super();
        }

        /* access modifiers changed from: package-private */
        public void handleHeader() {
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            int i = notificationHeaderViewWrapper.mGoogleContentMarginStart;
            int paddingTop = notificationHeaderViewWrapper.mNotificationHeader.getPaddingTop();
            NotificationHeaderViewWrapper notificationHeaderViewWrapper2 = NotificationHeaderViewWrapper.this;
            NotificationHeaderViewWrapper.this.mNotificationHeader.setPadding(i, paddingTop, notificationHeaderViewWrapper2.mGoogleContentMarginEnd, notificationHeaderViewWrapper2.mNotificationHeader.getPaddingBottom());
            if (NotificationHeaderViewWrapper.this.mRow.isMediaNotification()) {
                NotificationHeaderViewWrapper.this.mNotificationHeader.setVisibility(8);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleExpandButton() {
            NotificationHeaderViewWrapper notificationHeaderViewWrapper = NotificationHeaderViewWrapper.this;
            notificationHeaderViewWrapper.mExpandButton.setVisibility(notificationHeaderViewWrapper.mExpandable ? 0 : 8);
        }

        /* access modifiers changed from: package-private */
        public void handleXSpaceIcon() {
            if (XSpaceUserHandle.isXSpaceUser(NotificationHeaderViewWrapper.this.mRow.getStatusBarNotification().getUser())) {
                NotificationHeaderViewWrapper.this.mXSpaceIcon.setColorFilter(NotificationHeaderViewWrapper.this.mColor);
                NotificationHeaderViewWrapper.this.mXSpaceIcon.setVisibility(0);
                return;
            }
            NotificationHeaderViewWrapper.this.mXSpaceIcon.setVisibility(8);
        }
    }
}
