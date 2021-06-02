package com.android.systemui.statusbar.notification.stack;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationChildrenContainer.kt */
public final class MiuiNotificationChildrenContainer extends NotificationChildrenContainer {
    private boolean isGroupBackgroundAnimating;
    private ImageView mAppIcon;
    private ViewState mAppIconViewState;
    private ImageView mCollapsedButton;
    private View mGroupHeader;
    private ViewState mGroupHeaderViewState;
    private TextView mGroupInfo;
    private int mMiuiAppIconMargin;
    private int mMiuiAppIconSize;
    private int mOverflowNumberMarginEnd;

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void setShelfIconVisible(boolean z) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationChildrenContainer(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        initResources();
    }

    private final void initResources() {
        Resources resources = getResources();
        this.mMiuiAppIconSize = resources.getDimensionPixelSize(C0012R$dimen.notification_app_icon_size);
        this.mMiuiAppIconMargin = resources.getDimensionPixelSize(C0012R$dimen.notification_app_icon_margin);
        this.mOverflowNumberMarginEnd = resources.getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_end);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mGroupHeader != null) {
            int width = getWidth();
            View view = this.mGroupHeader;
            if (view != null) {
                ImageView imageView = this.mCollapsedButton;
                if (imageView != null) {
                    view.layout(0, 0, width, imageView.getMeasuredHeight());
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        if (this.mAppIcon != null) {
            int width2 = isRTL() ? (getWidth() - this.mMiuiAppIconMargin) - this.mMiuiAppIconSize : this.mMiuiAppIconMargin;
            int i5 = this.mMiuiAppIconMargin;
            ImageView imageView2 = this.mAppIcon;
            if (imageView2 != null) {
                int i6 = this.mMiuiAppIconSize;
                imageView2.layout(width2, i5, width2 + i6, i6 + i5);
            }
        }
    }

    private final boolean isRTL() {
        Resources resources = getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
        return (resources.getConfiguration().screenLayout & 192) == 128;
    }

    public final void setGroupBackgroundAnimating(boolean z) {
        this.isGroupBackgroundAnimating = z;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void startAnimationToState(@Nullable AnimationProperties animationProperties) {
        ViewState viewState;
        ViewState viewState2;
        super.startAnimationToState(animationProperties);
        View view = this.mGroupHeader;
        if (!(view == null || (viewState2 = this.mGroupHeaderViewState) == null)) {
            viewState2.animateTo(view, animationProperties);
        }
        ImageView imageView = this.mAppIcon;
        if (!(imageView == null || (viewState = this.mAppIconViewState) == null)) {
            viewState.animateTo(imageView, animationProperties);
        }
        startBackgroundAnimation();
    }

    private final NotificationBackgroundView getFirstChildBackground() {
        ExpandableNotificationRow expandableNotificationRow = this.mAttachedChildren.get(0);
        if (expandableNotificationRow != null) {
            return ((MiuiExpandableNotificationRow) expandableNotificationRow).getAnimatedBackground();
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow");
    }

    private final void startBackgroundAnimation() {
        if (!NotificationSettingsHelper.showGoogleStyle() && !this.isGroupBackgroundAnimating) {
            ExpandableNotificationRow expandableNotificationRow = this.mContainingNotification;
            Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow, "mContainingNotification");
            if (expandableNotificationRow.isGroupExpansionChanging() && !this.mAttachedChildren.isEmpty()) {
                this.isGroupBackgroundAnimating = true;
                ExpandableNotificationRow expandableNotificationRow2 = this.mContainingNotification;
                if (expandableNotificationRow2 != null) {
                    NotificationBackgroundView animatedBackground = ((MiuiExpandableNotificationRow) expandableNotificationRow2).getAnimatedBackground();
                    ExpandableNotificationRow expandableNotificationRow3 = this.mAttachedChildren.get(0);
                    Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow3, "child");
                    ExpandableViewState viewState = expandableNotificationRow3.getViewState();
                    NotificationBackgroundView firstChildBackground = getFirstChildBackground();
                    if (this.mChildrenExpanded) {
                        float actualHeight = (float) animatedBackground.getActualHeight();
                        if (viewState != null) {
                            float f = (float) viewState.height;
                            if (actualHeight == f) {
                                firstChildBackground.setVisibility(0);
                                ExpandableNotificationRow expandableNotificationRow4 = this.mContainingNotification;
                                Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow4, "mContainingNotification");
                                animatedBackground.setVisibility(expandableNotificationRow4.isDimmed() ? 4 : 0);
                                this.isGroupBackgroundAnimating = false;
                                return;
                            }
                            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
                            Intrinsics.checkExpressionValueIsNotNull(ofFloat, "anim");
                            ofFloat.setInterpolator(new DecelerateInterpolator());
                            ofFloat.addListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$1(this, animatedBackground, firstChildBackground));
                            ofFloat.addUpdateListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$2(this, actualHeight, f, animatedBackground, viewState));
                            ofFloat.setDuration((long) 300);
                            ofFloat.start();
                            return;
                        }
                        Intrinsics.throwNpe();
                        throw null;
                    }
                    float actualHeight2 = (float) animatedBackground.getActualHeight();
                    ExpandableNotificationRow expandableNotificationRow5 = this.mContainingNotification;
                    Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow5, "mContainingNotification");
                    ExpandableViewState viewState2 = expandableNotificationRow5.getViewState();
                    if (viewState2 != null) {
                        float f2 = (float) viewState2.height;
                        if (actualHeight2 == f2) {
                            animatedBackground.setVisibility(0);
                            animatedBackground.setAlpha(1.0f);
                            ExpandableNotificationRow expandableNotificationRow6 = this.mContainingNotification;
                            Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow6, "mContainingNotification");
                            animatedBackground.setActualHeight(expandableNotificationRow6.getActualHeight());
                            this.isGroupBackgroundAnimating = false;
                            return;
                        }
                        float translationY = animatedBackground.getTranslationY();
                        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
                        ofFloat2.addListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$3(this, animatedBackground, firstChildBackground));
                        ofFloat2.addUpdateListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$4(this, actualHeight2, f2, animatedBackground, translationY));
                        Intrinsics.checkExpressionValueIsNotNull(ofFloat2, "anim");
                        ofFloat2.setInterpolator(new DecelerateInterpolator());
                        ofFloat2.setDuration((long) 300);
                        ofFloat2.start();
                        return;
                    }
                    Intrinsics.throwNpe();
                    throw null;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int measureHeaderView(int i, int i2) {
        View view = this.mGroupHeader;
        if (view == null) {
            return 0;
        }
        if (view != null) {
            view.measure(i, i2);
            View view2 = this.mGroupHeader;
            if (view2 != null) {
                return view2.getMeasuredHeight();
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void updateChildrenHeaderAppearance() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            super.updateChildrenHeaderAppearance();
        }
        updateMiuiHeader(false);
    }

    private final void updateMiuiHeader(boolean z) {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            View view = this.mGroupHeader;
            if (view != null) {
                removeView(view);
                this.mGroupHeader = null;
                updateAppIcon(z);
                return;
            }
            return;
        }
        NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
        Intrinsics.checkExpressionValueIsNotNull(notificationHeaderView, "mNotificationHeader");
        notificationHeaderView.setVisibility(8);
        updateAppIcon(z);
        if (this.mGroupHeader == null || z) {
            View view2 = this.mGroupHeader;
            if (view2 != null) {
                removeView(view2);
                this.mGroupHeader = null;
            }
            View inflate = LayoutInflater.from(getContext()).inflate(C0017R$layout.notification_group_header, (ViewGroup) this, false);
            this.mGroupHeader = inflate;
            if (inflate != null) {
                this.mCollapsedButton = (ImageView) inflate.findViewById(C0015R$id.collapse_button);
                View view3 = this.mGroupHeader;
                if (view3 != null) {
                    this.mGroupInfo = (TextView) view3.findViewById(C0015R$id.group_info);
                    ImageView imageView = this.mCollapsedButton;
                    if (imageView != null) {
                        imageView.setOnClickListener(this.mHeaderClickListener);
                        addView(this.mGroupHeader, 0);
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        TextView textView = this.mGroupInfo;
        if (textView != null) {
            StringBuilder sb = new StringBuilder();
            ExpandableNotificationRow expandableNotificationRow = this.mContainingNotification;
            Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow, "mContainingNotification");
            NotificationEntry entry = expandableNotificationRow.getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry, "mContainingNotification.entry");
            ExpandedNotification sbn = entry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "mContainingNotification.entry.sbn");
            sb.append(sbn.getAppName());
            sb.append("(");
            sb.append(this.mAttachedChildren.size());
            sb.append(")");
            textView.setText(sb.toString());
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void updateState(@NotNull ExpandableViewState expandableViewState, @NotNull AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(expandableViewState, "parentState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        super.updateState(expandableViewState, ambientState);
        updateMiuiGroupHeaderState();
        updateAppIconState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getGroupHeaderHeight() {
        View view;
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getGroupHeaderHeight();
        }
        int i = this.mNotificatonTopPadding;
        if (!this.mChildrenExpanded || (view = this.mGroupHeader) == null) {
            return i;
        }
        if (view != null) {
            return i + view.getMeasuredHeight();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getIntrinsicHeight() {
        int intrinsicHeight = super.getIntrinsicHeight();
        if (!NotificationSettingsHelper.showMiuiStyle() || !this.mChildrenExpanded) {
            return intrinsicHeight;
        }
        return intrinsicHeight + (this.mGroupHeader == null ? 0 : getGroupHeaderHeight());
    }

    private final void updateMiuiGroupHeaderState() {
        float f;
        if (this.mGroupHeader != null) {
            if (this.mGroupHeaderViewState == null) {
                this.mGroupHeaderViewState = new ViewState();
            }
            ViewState viewState = this.mGroupHeaderViewState;
            if (viewState != null) {
                viewState.initFrom(this.mGroupHeader);
                ViewState viewState2 = this.mGroupHeaderViewState;
                if (viewState2 != null) {
                    viewState2.hidden = false;
                    if (viewState2 != null) {
                        viewState2.zTranslation = -1.0f;
                        if (viewState2 != null) {
                            if (this.mChildrenExpanded) {
                                f = (float) this.mNotificatonTopPadding;
                            } else {
                                float f2 = (float) this.mNotificatonTopPadding;
                                View view = this.mGroupHeader;
                                if (view != null) {
                                    f = f2 + ((float) view.getMeasuredHeight());
                                } else {
                                    Intrinsics.throwNpe();
                                    throw null;
                                }
                            }
                            viewState2.yTranslation = f;
                            ViewState viewState3 = this.mGroupHeaderViewState;
                            if (viewState3 != null) {
                                viewState3.alpha = this.mChildrenExpanded ? 1.0f : 0.0f;
                            } else {
                                Intrinsics.throwNpe();
                                throw null;
                            }
                        } else {
                            Intrinsics.throwNpe();
                            throw null;
                        }
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
    }

    private final void updateAppIconState() {
        Float f;
        ExpandableViewState viewState;
        if (this.mAppIcon != null) {
            if (this.mAppIconViewState == null) {
                this.mAppIconViewState = new ViewState();
            }
            ViewState viewState2 = this.mAppIconViewState;
            if (viewState2 != null) {
                viewState2.initFrom(this.mAppIcon);
            }
            ViewState viewState3 = this.mAppIconViewState;
            if (viewState3 != null) {
                viewState3.hidden = false;
            }
            ViewState viewState4 = this.mAppIconViewState;
            if (viewState4 != null) {
                viewState4.zTranslation = -1.0f;
            }
            ViewState viewState5 = this.mAppIconViewState;
            float f2 = 0.0f;
            if (viewState5 != null) {
                if (this.mChildrenExpanded) {
                    ExpandableNotificationRow expandableNotificationRow = this.mAttachedChildren.get(0);
                    f = (expandableNotificationRow == null || (viewState = expandableNotificationRow.getViewState()) == null) ? null : Float.valueOf(viewState.yTranslation);
                } else {
                    f = Float.valueOf(0.0f);
                }
                viewState5.yTranslation = f.floatValue();
            }
            ViewState viewState6 = this.mAppIconViewState;
            if (viewState6 != null) {
                if (!this.mChildrenExpanded) {
                    f2 = 1.0f;
                }
                viewState6.alpha = f2;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getIncreasedYPosition(@NotNull ExpandableNotificationRow expandableNotificationRow, int i) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "child");
        if (!NotificationSettingsHelper.showGoogleStyle()) {
            return this.mChildrenExpanded ? i : expandableNotificationRow.getMinHeight();
        }
        super.getIncreasedYPosition(expandableNotificationRow, i);
        return i;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getDividerHeight() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getDividerHeight();
        }
        return getResources().getDimensionPixelSize(C0012R$dimen.notification_divider_height);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getNotificationHeaderMargin() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getNotificationHeaderMargin();
        }
        return getResources().getDimensionPixelSize(C0012R$dimen.notification_children_container_margin_top_miui);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getNotificationTopPadding() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getNotificationTopPadding();
        }
        return getResources().getDimensionPixelSize(C0012R$dimen.notification_children_container_top_padding_miui);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void reInflateViews(@NotNull View.OnClickListener onClickListener, @NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "listener");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "notification");
        super.reInflateViews(onClickListener, statusBarNotification);
        initResources();
        updateMiuiHeader(true);
    }

    private final void updateAppIcon(boolean z) {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            ImageView imageView = this.mAppIcon;
            if (imageView != null) {
                removeView(imageView);
                this.mAppIcon = null;
            }
        } else if (this.mAppIcon == null || z) {
            ImageView imageView2 = this.mAppIcon;
            if (imageView2 != null) {
                removeView(imageView2);
                this.mAppIcon = null;
            }
            View inflate = LayoutInflater.from(getContext()).inflate(C0017R$layout.notification_template_part_app_icon, (ViewGroup) this, false);
            if (inflate != null) {
                this.mAppIcon = (ImageView) inflate;
                Context context = getContext();
                ExpandableNotificationRow expandableNotificationRow = this.mContainingNotification;
                Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow, "mContainingNotification");
                NotificationEntry entry = expandableNotificationRow.getEntry();
                Intrinsics.checkExpressionValueIsNotNull(entry, "mContainingNotification.entry");
                NotificationUtil.applyAppIconAllowCustom(context, entry.getSbn(), this.mAppIcon);
                addView(this.mAppIcon);
                ExpandableNotificationRow expandableNotificationRow2 = this.mContainingNotification;
                Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow2, "mContainingNotification");
                NotificationEntry entry2 = expandableNotificationRow2.getEntry();
                Intrinsics.checkExpressionValueIsNotNull(entry2, "mContainingNotification.entry");
                ExpandedNotification sbn = entry2.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "mContainingNotification.entry.sbn");
                setContentDescription(sbn.getAppName());
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.widget.ImageView");
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void applyState() {
        ViewState viewState;
        ViewState viewState2;
        super.applyState();
        ImageView imageView = this.mAppIcon;
        if (!(imageView == null || (viewState2 = this.mAppIconViewState) == null)) {
            viewState2.applyToView(imageView);
        }
        View view = this.mGroupHeader;
        if (!(view == null || (viewState = this.mGroupHeaderViewState) == null)) {
            viewState.applyToView(view);
        }
        if (!NotificationSettingsHelper.showGoogleStyle()) {
            ExpandableNotificationRow expandableNotificationRow = this.mContainingNotification;
            if (expandableNotificationRow != null) {
                NotificationBackgroundView animatedBackground = ((MiuiExpandableNotificationRow) expandableNotificationRow).getAnimatedBackground();
                NotificationBackgroundView firstChildBackground = getFirstChildBackground();
                int i = 0;
                if (this.mChildrenExpanded) {
                    animatedBackground.setVisibility(8);
                    firstChildBackground.setVisibility(0);
                    ExpandableNotificationRow expandableNotificationRow2 = this.mAttachedChildren.get(0);
                    Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow2, "child");
                    ExpandableViewState viewState3 = expandableNotificationRow2.getViewState();
                    if (viewState3 != null) {
                        animatedBackground.setActualHeight(viewState3.height);
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                } else {
                    ExpandableNotificationRow expandableNotificationRow3 = this.mContainingNotification;
                    Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow3, "mContainingNotification");
                    ExpandableViewState viewState4 = expandableNotificationRow3.getViewState();
                    if (viewState4 != null) {
                        animatedBackground.setActualHeight(viewState4.height);
                        ExpandableNotificationRow expandableNotificationRow4 = this.mContainingNotification;
                        Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow4, "mContainingNotification");
                        if (expandableNotificationRow4.isDimmed()) {
                            i = 4;
                        }
                        animatedBackground.setVisibility(i);
                        animatedBackground.setTranslationY(0.0f);
                        animatedBackground.setAlpha(1.0f);
                        firstChildBackground.setVisibility(8);
                        return;
                    }
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow");
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        ImageView imageView = this.mAppIcon;
        if (imageView == null) {
            return;
        }
        if (imageView != null) {
            imageView.measure(View.MeasureSpec.makeMeasureSpec(this.mMiuiAppIconSize, 1073741824), View.MeasureSpec.makeMeasureSpec(this.mMiuiAppIconSize, 1073741824));
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public int getOverflowNumberMarginEnd() {
        return this.mOverflowNumberMarginEnd;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationChildrenContainer
    public float getGroupExpandFraction() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getGroupExpandFraction();
        }
        return 0.0f;
    }
}
