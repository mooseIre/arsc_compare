package com.android.systemui.statusbar.notification.stack;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.NotificationHeaderView;
import android.view.View;
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

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationChildrenContainer(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = getResources();
        this.mMiuiAppIconSize = resources.getDimensionPixelSize(C0012R$dimen.notification_app_icon_size);
        this.mMiuiAppIconMargin = resources.getDimensionPixelSize(C0012R$dimen.notification_app_icon_margin);
        this.mOverflowNumberMarginEnd = resources.getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_end);
    }

    /* access modifiers changed from: protected */
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
                            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                            Intrinsics.checkExpressionValueIsNotNull(ofFloat, "anim");
                            ofFloat.setInterpolator(new DecelerateInterpolator());
                            ofFloat.addListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$1(this, animatedBackground, firstChildBackground));
                            ofFloat.addUpdateListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$2(this, actualHeight, f, animatedBackground, viewState));
                            ofFloat.setDuration((long) 360);
                            ofFloat.start();
                            return;
                        }
                        Intrinsics.throwNpe();
                        throw null;
                    }
                    float actualHeight2 = (float) animatedBackground.getActualHeight();
                    ExpandableNotificationRow expandableNotificationRow4 = this.mContainingNotification;
                    Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow4, "mContainingNotification");
                    ExpandableViewState viewState2 = expandableNotificationRow4.getViewState();
                    if (viewState2 != null) {
                        float translationY = animatedBackground.getTranslationY();
                        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                        ofFloat2.addListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$3(this, animatedBackground, firstChildBackground));
                        ofFloat2.addUpdateListener(new MiuiNotificationChildrenContainer$startBackgroundAnimation$4(this, actualHeight2, (float) viewState2.height, animatedBackground, translationY));
                        Intrinsics.checkExpressionValueIsNotNull(ofFloat2, "anim");
                        ofFloat2.setInterpolator(new DecelerateInterpolator());
                        ofFloat2.setDuration((long) 360);
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

    public void updateChildrenHeaderAppearance() {
        super.updateChildrenHeaderAppearance();
        updateMiuiHeader();
    }

    private final void updateMiuiHeader() {
        if (!NotificationSettingsHelper.showGoogleStyle()) {
            NotificationHeaderView notificationHeaderView = this.mNotificationHeader;
            Intrinsics.checkExpressionValueIsNotNull(notificationHeaderView, "mNotificationHeader");
            notificationHeaderView.setVisibility(8);
            setAppIcon();
            if (this.mGroupHeader == null) {
                View inflate = LayoutInflater.from(getContext()).inflate(C0017R$layout.notification_group_header, this, false);
                this.mGroupHeader = inflate;
                if (inflate != null) {
                    this.mCollapsedButton = (ImageView) inflate.findViewById(C0015R$id.collapse_button);
                    View view = this.mGroupHeader;
                    if (view != null) {
                        this.mGroupInfo = (TextView) view.findViewById(C0015R$id.group_info);
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
    }

    public void updateState(@NotNull ExpandableViewState expandableViewState, @NotNull AmbientState ambientState) {
        Intrinsics.checkParameterIsNotNull(expandableViewState, "parentState");
        Intrinsics.checkParameterIsNotNull(ambientState, "ambientState");
        super.updateState(expandableViewState, ambientState);
        updateMiuiGroupHeaderState();
        updateAppIconState();
    }

    /* access modifiers changed from: protected */
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

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003a, code lost:
        r1 = r1.getViewState();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateAppIconState() {
        /*
            r4 = this;
            android.widget.ImageView r0 = r4.mAppIcon
            if (r0 == 0) goto L_0x0060
            com.android.systemui.statusbar.notification.stack.ViewState r0 = r4.mAppIconViewState
            if (r0 != 0) goto L_0x000f
            com.android.systemui.statusbar.notification.stack.ViewState r0 = new com.android.systemui.statusbar.notification.stack.ViewState
            r0.<init>()
            r4.mAppIconViewState = r0
        L_0x000f:
            com.android.systemui.statusbar.notification.stack.ViewState r0 = r4.mAppIconViewState
            if (r0 == 0) goto L_0x0018
            android.widget.ImageView r1 = r4.mAppIcon
            r0.initFrom(r1)
        L_0x0018:
            com.android.systemui.statusbar.notification.stack.ViewState r0 = r4.mAppIconViewState
            r1 = 0
            if (r0 == 0) goto L_0x001f
            r0.hidden = r1
        L_0x001f:
            com.android.systemui.statusbar.notification.stack.ViewState r0 = r4.mAppIconViewState
            if (r0 == 0) goto L_0x0027
            r2 = -1082130432(0xffffffffbf800000, float:-1.0)
            r0.zTranslation = r2
        L_0x0027:
            com.android.systemui.statusbar.notification.stack.ViewState r0 = r4.mAppIconViewState
            r2 = 0
            if (r0 == 0) goto L_0x0053
            boolean r3 = r4.mChildrenExpanded
            if (r3 == 0) goto L_0x0049
            java.util.List<com.android.systemui.statusbar.notification.row.ExpandableNotificationRow> r3 = r4.mAttachedChildren
            java.lang.Object r1 = r3.get(r1)
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r1 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r1
            if (r1 == 0) goto L_0x0047
            com.android.systemui.statusbar.notification.stack.ExpandableViewState r1 = r1.getViewState()
            if (r1 == 0) goto L_0x0047
            float r1 = r1.yTranslation
            java.lang.Float r1 = java.lang.Float.valueOf(r1)
            goto L_0x004d
        L_0x0047:
            r1 = 0
            goto L_0x004d
        L_0x0049:
            java.lang.Float r1 = java.lang.Float.valueOf(r2)
        L_0x004d:
            float r1 = r1.floatValue()
            r0.yTranslation = r1
        L_0x0053:
            com.android.systemui.statusbar.notification.stack.ViewState r0 = r4.mAppIconViewState
            if (r0 == 0) goto L_0x0060
            boolean r4 = r4.mChildrenExpanded
            if (r4 == 0) goto L_0x005c
            goto L_0x005e
        L_0x005c:
            r2 = 1065353216(0x3f800000, float:1.0)
        L_0x005e:
            r0.alpha = r2
        L_0x0060:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.MiuiNotificationChildrenContainer.updateAppIconState():void");
    }

    /* access modifiers changed from: protected */
    public int getIncreasedYPosition(@NotNull ExpandableNotificationRow expandableNotificationRow, int i) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "child");
        if (!NotificationSettingsHelper.showGoogleStyle()) {
            return this.mChildrenExpanded ? i : expandableNotificationRow.getMinHeight();
        }
        super.getIncreasedYPosition(expandableNotificationRow, i);
        return i;
    }

    /* access modifiers changed from: protected */
    public int getDividerHeight() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getDividerHeight();
        }
        return getResources().getDimensionPixelSize(C0012R$dimen.notification_divider_height);
    }

    /* access modifiers changed from: protected */
    public int getNotificationHeaderMargin() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getNotificationHeaderMargin();
        }
        return getResources().getDimensionPixelSize(C0012R$dimen.notification_children_container_margin_top_miui);
    }

    /* access modifiers changed from: protected */
    public int getNotificationTopPadding() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.getNotificationTopPadding();
        }
        return getResources().getDimensionPixelSize(C0012R$dimen.notification_children_container_top_padding_miui);
    }

    public void reInflateViews(@NotNull View.OnClickListener onClickListener, @NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "listener");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "notification");
        super.reInflateViews(onClickListener, statusBarNotification);
        setAppIcon();
    }

    private final void setAppIcon() {
        if (!NotificationSettingsHelper.showGoogleStyle() && this.mAppIcon == null) {
            View inflate = LayoutInflater.from(getContext()).inflate(C0017R$layout.notification_template_part_app_icon, this, false);
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
                        animatedBackground.setVisibility(0);
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
    public int getOverflowNumberMarginEnd() {
        return this.mOverflowNumberMarginEnd;
    }
}
