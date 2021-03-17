package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.util.ArraySet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.CachingIconView;
import com.android.internal.widget.NotificationExpandButton;
import com.android.settingslib.Utils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.CustomInterpolatorTransformation;
import com.android.systemui.statusbar.notification.ImageTransformState;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.TransformState;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import java.util.Stack;

public class NotificationHeaderViewWrapper extends NotificationViewWrapper {
    private static final Interpolator LOW_PRIORITY_HEADER_CLOSE = new PathInterpolator(0.4f, 0.0f, 0.7f, 1.0f);
    private TextView mAppNameText;
    private View mAppOps;
    private View mAudiblyAlertedIcon;
    private View mCameraIcon;
    private NotificationExpandButton mExpandButton;
    private TextView mHeaderText;
    private CachingIconView mIcon;
    private FrameLayout mIconContainer;
    private boolean mIsLowPriority;
    private View mMicIcon;
    protected NotificationHeaderView mNotificationHeader;
    private View mOverlayIcon;
    private boolean mShowExpandButtonAtEnd;
    private boolean mTransformLowPriorityTitle;
    protected final ViewTransformationHelper mTransformationHelper;
    private ImageView mWorkProfileImage;

    protected NotificationHeaderViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mShowExpandButtonAtEnd = context.getResources().getBoolean(C0010R$bool.config_showNotificationExpandButtonAtEnd) || NotificationUtils.useNewInterruptionModel(context);
        ViewTransformationHelper viewTransformationHelper = new ViewTransformationHelper();
        this.mTransformationHelper = viewTransformationHelper;
        viewTransformationHelper.setCustomTransformation(new CustomInterpolatorTransformation(1) {
            /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
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
            @Override // com.android.systemui.statusbar.notification.CustomInterpolatorTransformation
            public boolean hasCustomTransformation() {
                return NotificationHeaderViewWrapper.this.mIsLowPriority && NotificationHeaderViewWrapper.this.mTransformLowPriorityTitle;
            }
        }, 1);
        resolveHeaderViews();
        addAppOpsOnClickListener(expandableNotificationRow);
    }

    /* access modifiers changed from: protected */
    public void resolveHeaderViews() {
        this.mIconContainer = (FrameLayout) this.mView.findViewById(16909040);
        this.mIcon = this.mView.findViewById(16908294);
        this.mHeaderText = (TextView) this.mView.findViewById(16909041);
        this.mAppNameText = (TextView) this.mView.findViewById(16908763);
        this.mExpandButton = this.mView.findViewById(16908957);
        this.mWorkProfileImage = (ImageView) this.mView.findViewById(16909339);
        this.mNotificationHeader = this.mView.findViewById(16909237);
        this.mCameraIcon = this.mView.findViewById(16908827);
        this.mMicIcon = this.mView.findViewById(16909180);
        this.mOverlayIcon = this.mView.findViewById(16909284);
        this.mAppOps = this.mView.findViewById(16908764);
        this.mAudiblyAlertedIcon = this.mView.findViewById(16908742);
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            notificationHeaderView.setShowExpandButtonAtEnd(this.mShowExpandButtonAtEnd);
            this.mNotificationHeader.getOriginalIconColor();
        }
        NotificationHeaderViewWrapperInjector.setAppIcon(this.mIcon, this.mRow.getEntry().getSbn());
        NotificationHeaderViewWrapperInjector.setAppNameText(this.mAppNameText, this.mRow.getEntry().getSbn());
    }

    private void addAppOpsOnClickListener(ExpandableNotificationRow expandableNotificationRow) {
        View.OnClickListener appOpsOnClickListener = expandableNotificationRow.getAppOpsOnClickListener();
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            notificationHeaderView.setAppOpsOnClickListener(appOpsOnClickListener);
        }
        View view = this.mAppOps;
        if (view != null) {
            view.setOnClickListener(appOpsOnClickListener);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void showAppOpsIcons(ArraySet<Integer> arraySet) {
        if (arraySet != null) {
            View view = this.mOverlayIcon;
            int i = 0;
            if (view != null) {
                view.setVisibility(arraySet.contains(24) ? 0 : 8);
            }
            View view2 = this.mCameraIcon;
            if (view2 != null) {
                view2.setVisibility(arraySet.contains(26) ? 0 : 8);
            }
            View view3 = this.mMicIcon;
            if (view3 != null) {
                if (!arraySet.contains(27)) {
                    i = 8;
                }
                view3.setVisibility(i);
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        this.mIsLowPriority = expandableNotificationRow.getEntry().isAmbient();
        this.mTransformLowPriorityTitle = !expandableNotificationRow.isChildInGroup() && !expandableNotificationRow.isSummaryWithChildren();
        ArraySet<View> allTransformingViews = this.mTransformationHelper.getAllTransformingViews();
        resolveHeaderViews();
        updateTransformedTypes();
        addRemainingTransformTypes();
        updateCropToPaddingForImageViews();
        this.mIcon.setTag(ImageTransformState.ICON_TAG, expandableNotificationRow.getEntry().getSbn().getNotification().getSmallIcon());
        ArraySet<View> allTransformingViews2 = this.mTransformationHelper.getAllTransformingViews();
        for (int i = 0; i < allTransformingViews.size(); i++) {
            View valueAt = allTransformingViews.valueAt(i);
            if (!allTransformingViews2.contains(valueAt)) {
                this.mTransformationHelper.resetTransformedView(valueAt);
            }
        }
    }

    public void applyConversationSkin() {
        TextView textView = this.mAppNameText;
        if (textView != null) {
            textView.setTextAppearance(16974721);
            ((ViewGroup.MarginLayoutParams) this.mAppNameText.getLayoutParams()).setMarginStart(0);
        }
        FrameLayout frameLayout = this.mIconContainer;
        if (frameLayout != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();
            marginLayoutParams.width = this.mIconContainer.getContext().getResources().getDimensionPixelSize(17105109);
            marginLayoutParams.setMarginStart(this.mIconContainer.getContext().getResources().getDimensionPixelSize(17105356) * -1);
        }
        CachingIconView cachingIconView = this.mIcon;
        if (cachingIconView != null) {
            ((ViewGroup.MarginLayoutParams) cachingIconView.getLayoutParams()).setMarginEnd(0);
        }
    }

    public void clearConversationSkin() {
        TextView textView = this.mAppNameText;
        if (textView != null) {
            this.mAppNameText.setTextAppearance(Utils.getThemeAttr(textView.getContext(), 17957006, 16974722));
            ((ViewGroup.MarginLayoutParams) this.mAppNameText.getLayoutParams()).setMarginStart(this.mAppNameText.getContext().getResources().getDimensionPixelSize(17105364));
        }
        FrameLayout frameLayout = this.mIconContainer;
        if (frameLayout != null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();
            marginLayoutParams.width = -2;
            marginLayoutParams.setMarginStart(0);
        }
        CachingIconView cachingIconView = this.mIcon;
        if (cachingIconView != null) {
            ((ViewGroup.MarginLayoutParams) cachingIconView.getLayoutParams()).setMarginEnd(this.mIcon.getContext().getResources().getDimensionPixelSize(17105368));
        }
    }

    private void addRemainingTransformTypes() {
        this.mTransformationHelper.addRemainingTransformTypes(this.mView);
    }

    private void updateCropToPaddingForImageViews() {
        Stack stack = new Stack();
        stack.push(this.mView);
        while (!stack.isEmpty()) {
            View view = (View) stack.pop();
            if ((view instanceof ImageView) && view.getId() != 16908895) {
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
        TextView textView;
        this.mTransformationHelper.reset();
        this.mTransformationHelper.addTransformedView(0, this.mIcon);
        this.mTransformationHelper.addViewTransformingToSimilar(this.mWorkProfileImage);
        if (this.mIsLowPriority && (textView = this.mHeaderText) != null) {
            this.mTransformationHelper.addTransformedView(1, textView);
        }
        View view = this.mCameraIcon;
        if (view != null) {
            this.mTransformationHelper.addViewTransformingToSimilar(view);
        }
        View view2 = this.mMicIcon;
        if (view2 != null) {
            this.mTransformationHelper.addViewTransformingToSimilar(view2);
        }
        View view3 = this.mOverlayIcon;
        if (view3 != null) {
            this.mTransformationHelper.addViewTransformingToSimilar(view3);
        }
        View view4 = this.mAudiblyAlertedIcon;
        if (view4 != null) {
            this.mTransformationHelper.addViewTransformingToSimilar(view4);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void updateExpandability(boolean z, View.OnClickListener onClickListener) {
        this.mExpandButton.setVisibility(z ? 0 : 8);
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        if (notificationHeaderView != null) {
            if (!z) {
                onClickListener = null;
            }
            notificationHeaderView.setOnClickListener(onClickListener);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setRecentlyAudiblyAlerted(boolean z) {
        View view = this.mAudiblyAlertedIcon;
        if (view != null) {
            view.setVisibility(z ? 0 : 8);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public NotificationHeaderView getNotificationHeader() {
        return this.mNotificationHeader;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public View getExpandButton() {
        return this.mExpandButton;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public int getOriginalIconColor() {
        return this.mIcon.getOriginalIconColor();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public View getShelfTransformationTarget() {
        return this.mIcon;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setShelfIconVisible(boolean z) {
        super.setShelfIconVisible(z);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public TransformState getCurrentState(int i) {
        return this.mTransformationHelper.getCurrentState(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, Runnable runnable) {
        this.mTransformationHelper.transformTo(transformableView, runnable);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformTo(transformableView, f);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView) {
        this.mTransformationHelper.transformFrom(transformableView);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformFrom(transformableView, f);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setIsChildInGroup(boolean z) {
        super.setIsChildInGroup(z);
        this.mTransformLowPriorityTitle = !z;
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        super.setVisible(z);
        this.mTransformationHelper.setVisible(z);
    }
}
