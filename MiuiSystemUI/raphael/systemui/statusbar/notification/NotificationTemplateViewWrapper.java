package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.Constants;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper;
import com.android.systemui.util.ViewAnimUtils;

public class NotificationTemplateViewWrapper extends NotificationHeaderViewWrapper {
    /* access modifiers changed from: private */
    public LinearLayout mActions;
    /* access modifiers changed from: private */
    public int mActionsButtonColor;
    protected View mBigMainContainer;
    /* access modifiers changed from: private */
    public int mExpandButtonMarginBottom;
    /* access modifiers changed from: private */
    public int mExpandButtonSize;
    protected LinearLayout mLine1Container;
    protected View mMainColumnContainer;
    /* access modifiers changed from: private */
    public int mMinContractedHeight;
    /* access modifiers changed from: private */
    public View mMiniBar;
    private int mMiniBarHeight;
    /* access modifiers changed from: private */
    public TextView mMiuiAction;
    protected ImageView mPicture;
    /* access modifiers changed from: private */
    public ProgressBar mProgressBar;
    /* access modifiers changed from: private */
    public int mProgressBarMarginTop;
    protected LinearLayout mRightIconContainer;
    /* access modifiers changed from: private */
    public int mRightIconCornerRadius;
    /* access modifiers changed from: private */
    public int mRightIconMarginStart;
    /* access modifiers changed from: private */
    public int mRightIconMarginTop;
    /* access modifiers changed from: private */
    public int mRightIconSize;
    private boolean mShowMiniBar;
    private boolean mShowNightMode;
    /* access modifiers changed from: private */
    public boolean mShowingPublic;
    private StyleProcessor mStyleProcessor;
    protected TextView mText;
    protected TextView mTextLine1;
    protected TextView mTitle;

    protected NotificationTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mTransformationHelper.setCustomTransformation(new ViewTransformationHelper.CustomTransformation() {
            public boolean transformTo(TransformState transformState, TransformableView transformableView, float f) {
                if (!(transformableView instanceof HybridNotificationView)) {
                    return false;
                }
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeOut(transformState.getTransformedView(), f);
                if (currentState != null) {
                    transformState.transformViewVerticalTo(currentState, this, f);
                    currentState.recycle();
                }
                return true;
            }

            public boolean customTransformTarget(TransformState transformState, TransformState transformState2) {
                transformState.setTransformationEndY(getTransformationY(transformState, transformState2));
                return true;
            }

            public boolean transformFrom(TransformState transformState, TransformableView transformableView, float f) {
                if (!(transformableView instanceof HybridNotificationView)) {
                    return false;
                }
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeIn(transformState.getTransformedView(), f);
                if (currentState != null) {
                    transformState.transformViewVerticalFrom(currentState, this, f);
                    currentState.recycle();
                }
                return true;
            }

            public boolean initTransformation(TransformState transformState, TransformState transformState2) {
                transformState.setTransformationStartY(getTransformationY(transformState, transformState2));
                return true;
            }

            private float getTransformationY(TransformState transformState, TransformState transformState2) {
                return ((float) ((transformState2.getLaidOutLocationOnScreen()[1] + transformState2.getTransformedView().getHeight()) - transformState.getLaidOutLocationOnScreen()[1])) * 0.33f;
            }
        }, 2);
        resolveTemplateViews(expandableNotificationRow);
        initResources();
        handleTemplateViews();
        clearColorSpans();
    }

    private void initResources() {
        this.mExpandButtonSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_expand_button_size);
        this.mRightIconSize = this.mContext.getResources().getDimensionPixelSize(17105354);
        this.mProgressBarMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_progress_bar_margin_top);
        this.mRightIconMarginStart = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_right_icon_margin_start);
        this.mRightIconMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_right_icon_margin_top);
        this.mRightIconCornerRadius = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_right_icon_corner_radius);
        this.mExpandButtonMarginBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_expand_button_margin_bottom);
        this.mMinContractedHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.min_notification_layout_height);
        this.mMiniBarHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.mini_window_bar_height);
        this.mActionsButtonColor = this.mContext.getColor(R.color.notification_actions_button_color);
        this.mShowNightMode = (this.mContext.getResources().getConfiguration().uiMode & 48) == 32;
        this.mStyleProcessor = showMiuiStyle() ? new MiuiStyleProcessor() : new GoogleStyleProcessor();
    }

    /* JADX WARNING: type inference failed for: r3v17, types: [android.view.View] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void resolveTemplateViews(com.android.systemui.statusbar.ExpandableNotificationRow r3) {
        /*
            r2 = this;
            android.view.View r0 = r2.mView
            r1 = 16909171(0x1020373, float:2.3879704E-38)
            android.view.View r0 = r0.findViewById(r1)
            r2.mBigMainContainer = r0
            android.view.View r0 = r2.mView
            r1 = 16909175(0x1020377, float:2.3879715E-38)
            android.view.View r0 = r0.findViewById(r1)
            r2.mMainColumnContainer = r0
            android.view.View r0 = r2.mView
            r1 = 16909072(0x1020310, float:2.3879426E-38)
            android.view.View r0 = r0.findViewById(r1)
            android.widget.LinearLayout r0 = (android.widget.LinearLayout) r0
            r2.mLine1Container = r0
            android.view.View r0 = r2.mView
            r1 = 16909310(0x10203fe, float:2.3880093E-38)
            android.view.View r0 = r0.findViewById(r1)
            android.widget.LinearLayout r0 = (android.widget.LinearLayout) r0
            r2.mRightIconContainer = r0
            android.view.View r0 = r2.mView
            r1 = 16909309(0x10203fd, float:2.388009E-38)
            android.view.View r0 = r0.findViewById(r1)
            android.widget.ImageView r0 = (android.widget.ImageView) r0
            r2.mPicture = r0
            android.widget.ImageView r0 = r2.mPicture
            if (r0 == 0) goto L_0x0053
            r1 = 2131362260(0x7f0a01d4, float:1.8344296E38)
            com.android.systemui.miui.statusbar.ExpandedNotification r3 = r3.getStatusBarNotification()
            android.app.Notification r3 = r3.getNotification()
            android.graphics.drawable.Icon r3 = r3.getLargeIcon()
            r0.setTag(r1, r3)
        L_0x0053:
            android.view.View r3 = r2.mView
            r0 = 16908310(0x1020016, float:2.387729E-38)
            android.view.View r3 = r3.findViewById(r0)
            android.widget.TextView r3 = (android.widget.TextView) r3
            r2.mTitle = r3
            android.view.View r3 = r2.mView
            r0 = 16909472(0x10204a0, float:2.3880547E-38)
            android.view.View r3 = r3.findViewById(r0)
            android.widget.TextView r3 = (android.widget.TextView) r3
            r2.mTextLine1 = r3
            android.view.View r3 = r2.mView
            r0 = 16909444(0x1020484, float:2.388047E-38)
            android.view.View r3 = r3.findViewById(r0)
            android.widget.TextView r3 = (android.widget.TextView) r3
            r2.mText = r3
            android.view.View r3 = r2.mView
            r0 = 16908301(0x102000d, float:2.3877265E-38)
            android.view.View r3 = r3.findViewById(r0)
            boolean r0 = r3 instanceof android.widget.ProgressBar
            r1 = 0
            if (r0 == 0) goto L_0x008d
            android.widget.ProgressBar r3 = (android.widget.ProgressBar) r3
            r2.mProgressBar = r3
            goto L_0x008f
        L_0x008d:
            r2.mProgressBar = r1
        L_0x008f:
            android.view.View r3 = r2.mView
            r0 = 16908696(0x1020198, float:2.3878372E-38)
            android.view.View r3 = r3.findViewById(r0)
            android.widget.LinearLayout r3 = (android.widget.LinearLayout) r3
            r2.mActions = r3
            boolean r3 = r2.isNormalNotification()
            if (r3 == 0) goto L_0x00ae
            android.view.View r3 = r2.mView
            r0 = 16909124(0x1020344, float:2.3879572E-38)
            android.view.View r3 = r3.findViewById(r0)
            r1 = r3
            android.widget.TextView r1 = (android.widget.TextView) r1
        L_0x00ae:
            r2.mMiuiAction = r1
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper.resolveTemplateViews(com.android.systemui.statusbar.ExpandableNotificationRow):void");
    }

    /* access modifiers changed from: protected */
    public void handleTemplateViews() {
        if (NotificationViewWrapper.DEBUG) {
            Log.d("NViewWrapper", String.format("handleTemplateViews type=%s pb=%b act=%b icon=%b time=%b acts=%b ex=%b pb=%b", new Object[]{this.mShowingType, Boolean.valueOf(showProgressBar()), Boolean.valueOf(showMiuiAction()), Boolean.valueOf(showRightIcon()), Boolean.valueOf(showTimeChronometer()), Boolean.valueOf(showActions()), Boolean.valueOf(showExpandButton()), Boolean.valueOf(this.mShowingPublic)}));
        }
        this.mStyleProcessor.handleContainer();
        this.mStyleProcessor.handleAppIcon();
        handleTitle();
        handleText();
        this.mStyleProcessor.handleTimeChronometer();
        this.mStyleProcessor.handleMiuiAction();
        this.mStyleProcessor.handleRightIcon();
        this.mStyleProcessor.handleProgressBar();
        this.mStyleProcessor.handleExpandButton();
        this.mStyleProcessor.handleWorkProfileImage();
        this.mStyleProcessor.handleActions();
        this.mStyleProcessor.handleMiniBar();
    }

    private void clearColorSpans() {
        if (this.mShowNightMode) {
            clearColorSpans(this.mTitle);
            clearColorSpans(this.mText);
            clearColorSpans(this.mTextLine1);
        }
    }

    /* access modifiers changed from: protected */
    public void clearColorSpans(TextView textView) {
        if (textView != null && (textView.getText() instanceof Spanned)) {
            textView.setText(CompatibilityColorUtil.clearColorSpans(textView.getText()));
        }
    }

    /* access modifiers changed from: protected */
    public void handleTitle() {
        if (this.mTitle != null) {
            this.mStyleProcessor.handleTitle();
        }
    }

    /* access modifiers changed from: protected */
    public void handleText() {
        if (this.mText != null) {
            this.mStyleProcessor.handleText();
        }
    }

    /* access modifiers changed from: protected */
    public boolean showAppIcon() {
        return !this.mRow.isChildInGroup();
    }

    /* access modifiers changed from: protected */
    public boolean showProgressBar() {
        Notification notification = this.mRow.getEntry().notification.getNotification();
        int i = notification.extras.getInt("android.progressMax", 0);
        boolean z = notification.extras.getBoolean("android.progressIndeterminate");
        if (this.mProgressBar == null) {
            return false;
        }
        if (i != 0 || z) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showMiuiAction() {
        if (showMiuiStyle() && !this.mShowingPublic && this.mMiuiAction != null && this.mRow.getEntry().notification.isShowMiuiAction()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showRightIcon() {
        if (showMiuiAction()) {
            return false;
        }
        Notification notification = this.mRow.getEntry().notification.getNotification();
        if (notification.getLargeIcon() == null && notification.largeIcon == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        if (showProgressBar() || showMiuiAction()) {
            return false;
        }
        TextView textView = this.mText;
        int lineCount = textView != null ? textView.getLineCount() : 0;
        if (NotificationViewWrapper.DEBUG) {
            Log.d("NViewWrapper", "showTimeChronometer textLineCount=" + lineCount);
        }
        if (showRightIcon() && lineCount < 2) {
            return false;
        }
        Notification notification = this.mRow.getEntry().notification.getNotification();
        if (notification.showsTime() || notification.showsChronometer()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
        if (!this.mShowingPublic && !showProgressBar() && !showSingleLine() && !showMiuiAction() && !showRightIcon()) {
            return super.showExpandButton();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0010, code lost:
        r1 = r0.actions;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showActions() {
        /*
            r1 = this;
            com.android.systemui.statusbar.ExpandableNotificationRow r0 = r1.mRow
            com.android.systemui.statusbar.NotificationData$Entry r0 = r0.getEntry()
            com.android.systemui.miui.statusbar.ExpandedNotification r0 = r0.notification
            android.app.Notification r0 = r0.getNotification()
            android.widget.LinearLayout r1 = r1.mActions
            if (r1 == 0) goto L_0x0019
            android.app.Notification$Action[] r1 = r0.actions
            if (r1 == 0) goto L_0x0019
            int r1 = r1.length
            if (r1 <= 0) goto L_0x0019
            r1 = 1
            goto L_0x001a
        L_0x0019:
            r1 = 0
        L_0x001a:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper.showActions():boolean");
    }

    /* access modifiers changed from: protected */
    public boolean showMiniBar() {
        return this.mShowMiniBar;
    }

    /* access modifiers changed from: protected */
    public boolean showSingleLine() {
        if (!showProgressBar() && !showActions()) {
            return NotificationUtil.showSingleLine(this.mRow.getEntry().notification.getNotification());
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isNormalNotification() {
        return this.mView.getId() == 16909421 && "base".equals(this.mView.getTag());
    }

    /* access modifiers changed from: protected */
    public void addTemplateView(View view) {
        addTemplateView((ViewGroup) this.mView, view);
    }

    /* access modifiers changed from: protected */
    public void addTemplateView(ViewGroup viewGroup, View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        viewGroup.addView(view);
    }

    /* access modifiers changed from: protected */
    public boolean isTemplateViewAdded(View view) {
        return isTemplateViewAdded((ViewGroup) this.mView, view);
    }

    /* access modifiers changed from: protected */
    public boolean isTemplateViewAdded(ViewGroup viewGroup, View view) {
        return viewGroup.indexOfChild(view) > 0;
    }

    public void setMiniBarVisible(boolean z) {
        boolean z2 = this.mShowMiniBar != z;
        this.mShowMiniBar = z;
        if (z2) {
            this.mStyleProcessor.handleMiniBar();
        }
    }

    public int getMiniBarHeight() {
        if (showMiniBar()) {
            return this.mMiniBarHeight;
        }
        return 0;
    }

    public void setIsChildInGroup(boolean z) {
        super.setIsChildInGroup(z);
        this.mStyleProcessor.handleAppIcon();
        this.mStyleProcessor.handleRightIcon();
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        resolveTemplateViews(expandableNotificationRow);
        handleTemplateViews();
        clearColorSpans();
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        ProgressBar progressBar;
        ImageView imageView;
        super.updateTransformedTypes();
        TextView textView = this.mTitle;
        if (textView != null) {
            this.mTransformationHelper.addTransformedView(1, textView);
        }
        TextView textView2 = this.mText;
        if (textView2 != null) {
            this.mTransformationHelper.addTransformedView(2, textView2);
        }
        if (!showMiuiStyle() && (imageView = this.mPicture) != null) {
            this.mTransformationHelper.addTransformedView(3, imageView);
        }
        if (!showMiuiStyle() && (progressBar = this.mProgressBar) != null) {
            this.mTransformationHelper.addTransformedView(4, progressBar);
        }
    }

    public void setRemoteInputVisible(boolean z) {
        LinearLayout linearLayout = this.mActions;
        if (linearLayout != null) {
            linearLayout.setVisibility(z ? 4 : 0);
        }
    }

    public void showPublic() {
        this.mShowingPublic = true;
        handleTemplateViews();
    }

    public void forceSetText(CharSequence charSequence) {
        TextView textView = this.mText;
        if (textView != null) {
            textView.setText(charSequence);
        }
    }

    private abstract class StyleProcessor {
        /* access modifiers changed from: package-private */
        public void handleAppIcon() {
        }

        /* access modifiers changed from: package-private */
        public abstract void handleContainer();

        /* access modifiers changed from: package-private */
        public void handleExpandButton() {
        }

        /* access modifiers changed from: package-private */
        public void handleText() {
        }

        /* access modifiers changed from: package-private */
        public void handleTimeChronometer() {
        }

        /* access modifiers changed from: package-private */
        public abstract void handleTitle();

        /* access modifiers changed from: package-private */
        public void handleWorkProfileImage() {
        }

        private StyleProcessor() {
        }

        /* access modifiers changed from: package-private */
        public void handleMiuiAction() {
            if (NotificationTemplateViewWrapper.this.showMiuiAction()) {
                NotificationTemplateViewWrapper.this.mMiuiAction.setBackground(NotificationTemplateViewWrapper.this.mContext.getDrawable(R.drawable.notification_action_bg));
                NotificationTemplateViewWrapper.this.mMiuiAction.setText(NotificationTemplateViewWrapper.this.mRow.getEntry().notification.getMiuiActionTitle());
                NotificationTemplateViewWrapper.this.mMiuiAction.setVisibility(0);
                ViewAnimUtils.mouse(NotificationTemplateViewWrapper.this.mMiuiAction);
            } else if (NotificationTemplateViewWrapper.this.mMiuiAction != null) {
                NotificationTemplateViewWrapper.this.mMiuiAction.setVisibility(8);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleRightIcon() {
            View findViewById = NotificationTemplateViewWrapper.this.mView.findViewById(16909299);
            if (findViewById != null) {
                findViewById.setVisibility(8);
            }
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            Util.setViewRoundCorner(notificationTemplateViewWrapper.mPicture, (float) notificationTemplateViewWrapper.mRightIconCornerRadius);
        }

        /* access modifiers changed from: package-private */
        public void handleProgressBar() {
            if (NotificationTemplateViewWrapper.this.mProgressBar != null && !Constants.IS_INTERNATIONAL) {
                NotificationTemplateViewWrapper.this.mProgressBar.setIndeterminateTintList((ColorStateList) null);
                NotificationTemplateViewWrapper.this.mProgressBar.setProgressTintList((ColorStateList) null);
            }
            if (NotificationTemplateViewWrapper.this.showProgressBar()) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) NotificationTemplateViewWrapper.this.mProgressBar.getLayoutParams();
                marginLayoutParams.topMargin = NotificationTemplateViewWrapper.this.mProgressBarMarginTop;
                marginLayoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.showRightIcon() ? NotificationTemplateViewWrapper.this.mRightIconSize + NotificationTemplateViewWrapper.this.mRightIconMarginStart : 0);
                NotificationTemplateViewWrapper.this.mProgressBar.setLayoutParams(marginLayoutParams);
                NotificationTemplateViewWrapper.this.mProgressBar.setScreenReaderFocusable(true);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleActions() {
            if (NotificationTemplateViewWrapper.this.showActions()) {
                boolean z = false;
                NotificationTemplateViewWrapper.this.mActions.setPaddingRelative(0, 0, 0, 0);
                if (NotificationTemplateViewWrapper.this.mRow.getEntry().notification.getNotification().fullScreenIntent != null) {
                    z = true;
                }
                if (z) {
                    ViewGroup.LayoutParams layoutParams = NotificationTemplateViewWrapper.this.mActions.getLayoutParams();
                    layoutParams.height = -2;
                    NotificationTemplateViewWrapper.this.mActions.setLayoutParams(layoutParams);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void handleMiniBar() {
            if (NotificationTemplateViewWrapper.this.showMiniBar()) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
                if (!notificationTemplateViewWrapper.isTemplateViewAdded(notificationTemplateViewWrapper.mMiniBar)) {
                    NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
                    View unused = notificationTemplateViewWrapper2.mMiniBar = LayoutInflater.from(notificationTemplateViewWrapper2.mContext).inflate(R.layout.heads_up_mini_window_bar, (ViewGroup) null);
                    NotificationTemplateViewWrapper notificationTemplateViewWrapper3 = NotificationTemplateViewWrapper.this;
                    ((ViewGroup) notificationTemplateViewWrapper3.mView).addView(notificationTemplateViewWrapper3.mMiniBar);
                }
            }
            if (NotificationTemplateViewWrapper.this.mMiniBar != null) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mMiniBar.getLayoutParams();
                layoutParams.width = NotificationTemplateViewWrapper.this.mContext.getResources().getDimensionPixelSize(R.dimen.mini_window_bar_width);
                layoutParams.height = NotificationTemplateViewWrapper.this.mContext.getResources().getDimensionPixelSize(R.dimen.mini_window_bar_height);
                layoutParams.bottomMargin = NotificationTemplateViewWrapper.this.mContext.getResources().getDimensionPixelSize(R.dimen.mini_window_bar_marginBottom);
                layoutParams.gravity = 81;
                NotificationTemplateViewWrapper.this.mMiniBar.setLayoutParams(layoutParams);
                NotificationTemplateViewWrapper.this.mMiniBar.setAlpha(0.0f);
                NotificationTemplateViewWrapper.this.mMiniBar.setVisibility(NotificationTemplateViewWrapper.this.showMiniBar() ? 0 : 8);
            }
        }
    }

    private class MiuiStyleProcessor extends StyleProcessor {
        private MiuiStyleProcessor() {
            super();
        }

        /* access modifiers changed from: package-private */
        public void handleContainer() {
            if (!NotificationTemplateViewWrapper.this.isNormalNotification()) {
                setMiuiContentMargins(NotificationTemplateViewWrapper.this.mBigMainContainer);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleAppIcon() {
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            if (!notificationTemplateViewWrapper.isTemplateViewAdded(notificationTemplateViewWrapper.mIcon)) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper2.addTemplateView(notificationTemplateViewWrapper2.mIcon);
            }
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mIcon.getLayoutParams();
            NotificationTemplateViewWrapper notificationTemplateViewWrapper3 = NotificationTemplateViewWrapper.this;
            int i = notificationTemplateViewWrapper3.mMiuiAppIconSize;
            layoutParams.width = i;
            layoutParams.height = i;
            layoutParams.setMarginStart(notificationTemplateViewWrapper3.mMiuiAppIconMargin);
            layoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mMiuiAppIconMargin);
            int i2 = 0;
            layoutParams.topMargin = NotificationTemplateViewWrapper.this.showSingleLine() ? 0 : NotificationTemplateViewWrapper.this.mMiuiAppIconMargin;
            layoutParams.gravity = NotificationTemplateViewWrapper.this.showSingleLine() ? 8388627 : 8388659;
            NotificationTemplateViewWrapper.this.mIcon.setLayoutParams(layoutParams);
            ExpandedNotification expandedNotification = NotificationTemplateViewWrapper.this.mRow.getEntry().notification;
            NotificationTemplateViewWrapper notificationTemplateViewWrapper4 = NotificationTemplateViewWrapper.this;
            NotificationUtil.applyAppIconAllowCustom(notificationTemplateViewWrapper4.mContext, expandedNotification, notificationTemplateViewWrapper4.mIcon);
            NotificationTemplateViewWrapper notificationTemplateViewWrapper5 = NotificationTemplateViewWrapper.this;
            ImageView imageView = notificationTemplateViewWrapper5.mIcon;
            if (!notificationTemplateViewWrapper5.showAppIcon()) {
                i2 = 8;
            }
            imageView.setVisibility(i2);
            NotificationTemplateViewWrapper.this.mIcon.setContentDescription(expandedNotification.getAppName());
        }

        /* access modifiers changed from: package-private */
        public void handleRightIcon() {
            super.handleRightIcon();
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            if (notificationTemplateViewWrapper.mRightIconContainer != null) {
                if (notificationTemplateViewWrapper.showRightIcon()) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mRightIconContainer.getLayoutParams();
                    if (NotificationTemplateViewWrapper.this.showSingleLine() || (NotificationTemplateViewWrapper.this.isNormalNotification() && !NotificationTemplateViewWrapper.this.showTimeChronometer())) {
                        layoutParams.topMargin = 0;
                        layoutParams.gravity = 8388629;
                    } else {
                        layoutParams.topMargin = NotificationTemplateViewWrapper.this.showTimeChronometer() ? NotificationTemplateViewWrapper.this.mRightIconMarginTop : NotificationTemplateViewWrapper.this.mMiuiContentMarginTop;
                        layoutParams.gravity = 8388661;
                    }
                    NotificationTemplateViewWrapper.this.mRightIconContainer.setLayoutParams(layoutParams);
                    NotificationTemplateViewWrapper.this.mRightIconContainer.setVisibility(0);
                    return;
                }
                NotificationTemplateViewWrapper.this.mRightIconContainer.setVisibility(8);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleTitle() {
            if (NotificationTemplateViewWrapper.this.showSingleLine()) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
                if (!notificationTemplateViewWrapper.isTemplateViewAdded(notificationTemplateViewWrapper.mLine1Container)) {
                    NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
                    ((ViewGroup) notificationTemplateViewWrapper2.mMainColumnContainer).removeView(notificationTemplateViewWrapper2.mLine1Container);
                    NotificationTemplateViewWrapper notificationTemplateViewWrapper3 = NotificationTemplateViewWrapper.this;
                    ((ViewGroup) notificationTemplateViewWrapper3.mView).addView(notificationTemplateViewWrapper3.mLine1Container);
                    NotificationTemplateViewWrapper.this.mMainColumnContainer.setVisibility(8);
                }
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mLine1Container.getLayoutParams();
                layoutParams.setMarginStart(NotificationTemplateViewWrapper.this.mMiuiContentMarginStart);
                layoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.showRightIcon() ? NotificationTemplateViewWrapper.this.mRightIconSize + NotificationTemplateViewWrapper.this.mRightIconMarginStart : NotificationTemplateViewWrapper.this.mMiuiContentMarginEnd);
                layoutParams.gravity = 8388627;
                NotificationTemplateViewWrapper.this.mLine1Container.setLayoutParams(layoutParams);
                NotificationTemplateViewWrapper notificationTemplateViewWrapper4 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper4.mView.setMinimumHeight(notificationTemplateViewWrapper4.mMinContractedHeight);
                return;
            }
            NotificationTemplateViewWrapper notificationTemplateViewWrapper5 = NotificationTemplateViewWrapper.this;
            if (notificationTemplateViewWrapper5.isTemplateViewAdded(notificationTemplateViewWrapper5.mLine1Container)) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper6 = NotificationTemplateViewWrapper.this;
                ((ViewGroup) notificationTemplateViewWrapper6.mView).removeView(notificationTemplateViewWrapper6.mLine1Container);
                NotificationTemplateViewWrapper notificationTemplateViewWrapper7 = NotificationTemplateViewWrapper.this;
                ((ViewGroup) notificationTemplateViewWrapper7.mMainColumnContainer).addView(notificationTemplateViewWrapper7.mLine1Container, 0);
                NotificationTemplateViewWrapper.this.mMainColumnContainer.setVisibility(0);
            }
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) NotificationTemplateViewWrapper.this.mLine1Container.getLayoutParams();
            marginLayoutParams.setMarginEnd((!NotificationTemplateViewWrapper.this.showRightIcon() || NotificationTemplateViewWrapper.this.showTimeChronometer()) ? 0 : NotificationTemplateViewWrapper.this.mRightIconSize + NotificationTemplateViewWrapper.this.mRightIconMarginStart);
            NotificationTemplateViewWrapper.this.mLine1Container.setLayoutParams(marginLayoutParams);
            NotificationTemplateViewWrapper.this.mView.setMinimumHeight(0);
        }

        /* access modifiers changed from: package-private */
        public void handleText() {
            Notification notification;
            Bundle bundle;
            if (NotificationTemplateViewWrapper.this.mShowingPublic && (notification = NotificationTemplateViewWrapper.this.mRow.getEntry().notification.getNotification().publicVersion) != null && (bundle = notification.extras) != null && TextUtils.isEmpty(bundle.getString("android.text"))) {
                NotificationTemplateViewWrapper.this.mText.setText("");
                NotificationTemplateViewWrapper.this.mText.setVisibility(8);
            }
            Notification notification2 = NotificationTemplateViewWrapper.this.mRow.getStatusBarNotification().getNotification();
            if (NotificationUtil.isInboxStyle(notification2) || NotificationUtil.isMessagingStyle(notification2)) {
                NotificationTemplateViewWrapper.this.mText.setMaxLines(1);
                NotificationTemplateViewWrapper.this.mText.setSingleLine(true);
            }
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) NotificationTemplateViewWrapper.this.mText.getLayoutParams();
            if (NotificationTemplateViewWrapper.this.showRightIcon()) {
                marginLayoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mRightIconSize + NotificationTemplateViewWrapper.this.mRightIconMarginStart);
            } else if (NotificationTemplateViewWrapper.this.showExpandButton()) {
                marginLayoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mExpandButtonSize);
            } else {
                marginLayoutParams.setMarginEnd(0);
            }
            NotificationTemplateViewWrapper.this.mText.setLayoutParams(marginLayoutParams);
            int lineCount = NotificationTemplateViewWrapper.this.mText.getLineCount();
            if (lineCount == 1) {
                NotificationTemplateViewWrapper.this.mText.post(new Runnable(lineCount) {
                    private final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        NotificationTemplateViewWrapper.MiuiStyleProcessor.this.lambda$handleText$0$NotificationTemplateViewWrapper$MiuiStyleProcessor(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$handleText$0$NotificationTemplateViewWrapper$MiuiStyleProcessor(int i) {
            if (i != NotificationTemplateViewWrapper.this.mText.getLineCount()) {
                NotificationTemplateViewWrapper.this.handleTemplateViews();
            }
        }

        /* access modifiers changed from: package-private */
        public void handleTimeChronometer() {
            LinearLayout linearLayout;
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            LinearLayout linearLayout2 = notificationTemplateViewWrapper.mLine1Container;
            if (linearLayout2 != null && !notificationTemplateViewWrapper.isTemplateViewAdded(linearLayout2, notificationTemplateViewWrapper.mTime)) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper2.addTemplateView(notificationTemplateViewWrapper2.mLine1Container, notificationTemplateViewWrapper2.mTime);
                setTimeChronometerLp(NotificationTemplateViewWrapper.this.mTime);
            }
            NotificationTemplateViewWrapper notificationTemplateViewWrapper3 = NotificationTemplateViewWrapper.this;
            View view = notificationTemplateViewWrapper3.mChronometer;
            if ((view instanceof Chronometer) && (linearLayout = notificationTemplateViewWrapper3.mLine1Container) != null && !notificationTemplateViewWrapper3.isTemplateViewAdded(linearLayout, view)) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper4 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper4.addTemplateView(notificationTemplateViewWrapper4.mLine1Container, notificationTemplateViewWrapper4.mChronometer);
                setTimeChronometerLp(NotificationTemplateViewWrapper.this.mChronometer);
            }
            Notification notification = NotificationTemplateViewWrapper.this.mRow.getEntry().notification.getNotification();
            int i = 0;
            if (notification.showsTime()) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper5 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper5.mTime.setVisibility(notificationTemplateViewWrapper5.showTimeChronometer() ? 0 : 8);
            }
            if (notification.showsChronometer()) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper6 = NotificationTemplateViewWrapper.this;
                View view2 = notificationTemplateViewWrapper6.mChronometer;
                if (!notificationTemplateViewWrapper6.showTimeChronometer()) {
                    i = 8;
                }
                view2.setVisibility(i);
            }
        }

        private void setTimeChronometerLp(View view) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.gravity = 16;
            layoutParams.setMarginStart(NotificationTemplateViewWrapper.this.mContext.getResources().getDimensionPixelSize(R.dimen.notification_header_content_margin));
            view.setLayoutParams(layoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleExpandButton() {
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            if (!notificationTemplateViewWrapper.isTemplateViewAdded(notificationTemplateViewWrapper.mExpandButton)) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper2.addTemplateView(notificationTemplateViewWrapper2.mExpandButton);
            }
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mExpandButton.getLayoutParams();
            layoutParams.width = NotificationTemplateViewWrapper.this.mExpandButtonSize;
            layoutParams.height = NotificationTemplateViewWrapper.this.mExpandButtonSize;
            layoutParams.gravity = 8388693;
            layoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mMiuiContentMarginEnd);
            layoutParams.bottomMargin = NotificationTemplateViewWrapper.this.mExpandButtonMarginBottom;
            NotificationTemplateViewWrapper.this.mExpandButton.setLayoutParams(layoutParams);
            NotificationTemplateViewWrapper notificationTemplateViewWrapper3 = NotificationTemplateViewWrapper.this;
            notificationTemplateViewWrapper3.mExpandButton.setVisibility(notificationTemplateViewWrapper3.showExpandButton() ? 0 : 8);
        }

        /* access modifiers changed from: package-private */
        public void handleWorkProfileImage() {
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            if (!notificationTemplateViewWrapper.isTemplateViewAdded(notificationTemplateViewWrapper.mWorkProfileImage)) {
                NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
                notificationTemplateViewWrapper2.addTemplateView(notificationTemplateViewWrapper2.mWorkProfileImage);
            }
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mWorkProfileImage.getLayoutParams();
            layoutParams.gravity = 8388693;
            layoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mMiuiContentMarginEnd);
            NotificationTemplateViewWrapper notificationTemplateViewWrapper3 = NotificationTemplateViewWrapper.this;
            layoutParams.bottomMargin = notificationTemplateViewWrapper3.mMiuiContentMarginBottom;
            notificationTemplateViewWrapper3.mWorkProfileImage.setLayoutParams(layoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleActions() {
            super.handleActions();
            if (NotificationTemplateViewWrapper.this.showActions()) {
                int childCount = NotificationTemplateViewWrapper.this.mActions.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    Button button = (Button) NotificationTemplateViewWrapper.this.mActions.getChildAt(i);
                    button.setTextColor(NotificationTemplateViewWrapper.this.mActionsButtonColor);
                    button.setBackground(NotificationTemplateViewWrapper.this.mContext.getDrawable(R.drawable.notification_action_bg));
                    ViewAnimUtils.mouse(button);
                }
            }
        }

        private void setMiuiContentMargins(View view) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.setMarginStart(NotificationTemplateViewWrapper.this.mMiuiContentMarginStart);
            marginLayoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mMiuiContentMarginEnd);
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            marginLayoutParams.topMargin = notificationTemplateViewWrapper.mMiuiContentMarginTop;
            marginLayoutParams.bottomMargin = notificationTemplateViewWrapper.mMiuiContentMarginBottom;
            view.setLayoutParams(marginLayoutParams);
        }
    }

    private class GoogleStyleProcessor extends StyleProcessor {
        private GoogleStyleProcessor() {
            super();
        }

        /* access modifiers changed from: package-private */
        public void handleContainer() {
            if (NotificationTemplateViewWrapper.this.isNormalNotification()) {
                setGoogleContentMargins((View) NotificationTemplateViewWrapper.this.mMainColumnContainer.getParent());
                return;
            }
            setGoogleContentMargins(NotificationTemplateViewWrapper.this.mBigMainContainer);
            clearGoogleContentMargins(NotificationTemplateViewWrapper.this.mMainColumnContainer);
        }

        /* access modifiers changed from: package-private */
        public void handleRightIcon() {
            super.handleRightIcon();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationTemplateViewWrapper.this.mRightIconContainer.getLayoutParams();
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            layoutParams.topMargin = notificationTemplateViewWrapper.mGoogleContentMarginTop;
            layoutParams.setMarginEnd(notificationTemplateViewWrapper.mGoogleContentMarginEnd);
            NotificationTemplateViewWrapper.this.mRightIconContainer.setLayoutParams(layoutParams);
            NotificationTemplateViewWrapper notificationTemplateViewWrapper2 = NotificationTemplateViewWrapper.this;
            notificationTemplateViewWrapper2.mRightIconContainer.setVisibility(notificationTemplateViewWrapper2.showRightIcon() ? 0 : 8);
        }

        /* access modifiers changed from: package-private */
        public void handleTitle() {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) NotificationTemplateViewWrapper.this.mLine1Container.getLayoutParams();
            marginLayoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.showRightIcon() ? NotificationTemplateViewWrapper.this.mRightIconSize + NotificationTemplateViewWrapper.this.mRightIconMarginStart : 0);
            NotificationTemplateViewWrapper.this.mLine1Container.setLayoutParams(marginLayoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleActions() {
            super.handleActions();
            if (NotificationTemplateViewWrapper.this.showActions()) {
                clearActionsMargins();
                int childCount = NotificationTemplateViewWrapper.this.mActions.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    Button button = (Button) NotificationTemplateViewWrapper.this.mActions.getChildAt(i);
                    button.setPaddingRelative(0, 0, 0, 0);
                    button.setBackground((Drawable) null);
                    button.setBackgroundResource(0);
                    button.setTextColor(NotificationTemplateViewWrapper.this.mActionsButtonColor);
                }
            }
        }

        private void clearActionsMargins() {
            View view = (View) NotificationTemplateViewWrapper.this.mActions.getParent();
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.topMargin = 0;
            marginLayoutParams.bottomMargin = 0;
            view.setLayoutParams(marginLayoutParams);
        }

        private void setGoogleContentMargins(View view) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.setMarginStart(NotificationTemplateViewWrapper.this.mGoogleContentMarginStart);
            marginLayoutParams.setMarginEnd(NotificationTemplateViewWrapper.this.mGoogleContentMarginEnd);
            NotificationTemplateViewWrapper notificationTemplateViewWrapper = NotificationTemplateViewWrapper.this;
            marginLayoutParams.topMargin = notificationTemplateViewWrapper.mGoogleContentMarginTop;
            marginLayoutParams.bottomMargin = notificationTemplateViewWrapper.mGoogleContentMarginBottom;
            view.setLayoutParams(marginLayoutParams);
        }

        private void clearGoogleContentMargins(View view) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.setMarginStart(0);
            marginLayoutParams.setMarginEnd(0);
            marginLayoutParams.topMargin = 0;
            marginLayoutParams.bottomMargin = 0;
            view.setLayoutParams(marginLayoutParams);
        }
    }
}
