package com.android.systemui.statusbar.notification.row;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.FakeShadowView;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.RowAnimationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowManager;
import com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationOneLineViewWrapper;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.util.CommonExtensionsKt;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiExpandableNotificationRow.kt */
public final class MiuiExpandableNotificationRow extends MiuiAnimatedNotificationRowBase {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final String TAG = "MiuiExpandableNotificationRow";
    private final Lazy mAppMiniWindowManager$delegate = LazyKt__LazyJVMKt.lazy(MiuiExpandableNotificationRow$mAppMiniWindowManager$2.INSTANCE);
    private final Lazy mBackgroundDimmed$delegate = LazyKt__LazyJVMKt.lazy(new MiuiExpandableNotificationRow$mBackgroundDimmed$2(this));
    private boolean mCanSlide;
    private final Lazy mFakeShadowView$delegate = LazyKt__LazyJVMKt.lazy(new MiuiExpandableNotificationRow$mFakeShadowView$2(this));
    private boolean mIsInModal;
    private boolean mLayoutInflated;
    private final Lazy mMiniBar$delegate = LazyKt__LazyJVMKt.lazy(new MiuiExpandableNotificationRow$mMiniBar$2(this));
    private final Lazy mMiniBarMarginBottom$delegate = LazyKt__LazyJVMKt.lazy(new MiuiExpandableNotificationRow$mMiniBarMarginBottom$2(this));
    private final Lazy mMiniWindowIcon$delegate = LazyKt__LazyJVMKt.lazy(new MiuiExpandableNotificationRow$mMiniWindowIcon$2(this));
    private final int[] mTmpPosition = {0, 0};

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiExpandableNotificationRow.class), "mAppMiniWindowManager", "getMAppMiniWindowManager()Lcom/android/systemui/statusbar/notification/policy/AppMiniWindowManager;");
        Reflection.property1(propertyReference1Impl);
        PropertyReference1Impl propertyReference1Impl2 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiExpandableNotificationRow.class), "mMiniBar", "getMMiniBar()Landroid/view/View;");
        Reflection.property1(propertyReference1Impl2);
        PropertyReference1Impl propertyReference1Impl3 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiExpandableNotificationRow.class), "mMiniWindowIcon", "getMMiniWindowIcon()Landroid/widget/ImageView;");
        Reflection.property1(propertyReference1Impl3);
        PropertyReference1Impl propertyReference1Impl4 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiExpandableNotificationRow.class), "mFakeShadowView", "getMFakeShadowView()Lcom/android/systemui/statusbar/notification/FakeShadowView;");
        Reflection.property1(propertyReference1Impl4);
        PropertyReference1Impl propertyReference1Impl5 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiExpandableNotificationRow.class), "mBackgroundDimmed", "getMBackgroundDimmed()Lcom/android/systemui/statusbar/notification/row/NotificationBackgroundView;");
        Reflection.property1(propertyReference1Impl5);
        PropertyReference1Impl propertyReference1Impl6 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiExpandableNotificationRow.class), "mMiniBarMarginBottom", "getMMiniBarMarginBottom()F");
        Reflection.property1(propertyReference1Impl6);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl, propertyReference1Impl2, propertyReference1Impl3, propertyReference1Impl4, propertyReference1Impl5, propertyReference1Impl6};
    }

    private final AppMiniWindowManager getMAppMiniWindowManager() {
        Lazy lazy = this.mAppMiniWindowManager$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (AppMiniWindowManager) lazy.getValue();
    }

    private final NotificationBackgroundView getMBackgroundDimmed() {
        Lazy lazy = this.mBackgroundDimmed$delegate;
        KProperty kProperty = $$delegatedProperties[4];
        return (NotificationBackgroundView) lazy.getValue();
    }

    private final FakeShadowView getMFakeShadowView() {
        Lazy lazy = this.mFakeShadowView$delegate;
        KProperty kProperty = $$delegatedProperties[3];
        return (FakeShadowView) lazy.getValue();
    }

    private final View getMMiniBar() {
        Lazy lazy = this.mMiniBar$delegate;
        KProperty kProperty = $$delegatedProperties[1];
        return (View) lazy.getValue();
    }

    private final float getMMiniBarMarginBottom() {
        Lazy lazy = this.mMiniBarMarginBottom$delegate;
        KProperty kProperty = $$delegatedProperties[5];
        return ((Number) lazy.getValue()).floatValue();
    }

    private final ImageView getMMiniWindowIcon() {
        Lazy lazy = this.mMiniWindowIcon$delegate;
        KProperty kProperty = $$delegatedProperties[2];
        return (ImageView) lazy.getValue();
    }

    public MiuiExpandableNotificationRow(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLayoutInflated = true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void initBackground() {
        super.initBackground();
        updateBackgroundBg();
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void updateBackground() {
        super.updateBackground();
        updateBackgroundBg();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public void setHeadsUp(boolean z) {
        super.setHeadsUp(z);
        updateBackgroundBg();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public void setHeadsUpAnimatingAway(boolean z) {
        super.setHeadsUpAnimatingAway(z);
        updateBackgroundBg();
    }

    private final void updateBackgroundBg() {
        if (isHeadsUpState()) {
            boolean isTransparentAble = NotificationContentInflaterInjector.isTransparentAble();
            NotificationContentView showingLayout = getShowingLayout();
            boolean z = isTransparentAble && ((showingLayout != null ? showingLayout.getVisibleWrapper(2) : null) instanceof MiuiNotificationOneLineViewWrapper);
            if (z) {
                this.mBackgroundNormal.setCustomBackground(C0013R$drawable.optimized_transparent_heads_up_notification_bg);
            } else {
                this.mBackgroundNormal.setCustomBackground(C0013R$drawable.notification_heads_up_bg);
            }
            View mMiniBar = getMMiniBar();
            Intrinsics.checkExpressionValueIsNotNull(mMiniBar, "mMiniBar");
            if (mMiniBar.getBackground() instanceof GradientDrawable) {
                int i = z ? C0011R$color.mini_window_bar_color_gamemode : C0011R$color.mini_window_bar_color;
                View mMiniBar2 = getMMiniBar();
                Intrinsics.checkExpressionValueIsNotNull(mMiniBar2, "mMiniBar");
                Drawable background = mMiniBar2.getBackground();
                if (background != null) {
                    ((GradientDrawable) background).setColor(getResources().getColor(i, null));
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
                }
            }
        } else {
            this.mBackgroundNormal.setCustomBackground(C0013R$drawable.notification_item_bg);
        }
        this.mBackgroundNormal.setBlurDisable(!NotificationContentInflaterInjector.isBlurAble(this.mIsInModal, isHeadsUpState()));
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public void setIsChildInGroup(boolean z, @Nullable ExpandableNotificationRow expandableNotificationRow) {
        if (z) {
            ExpandableViewState viewState = getViewState();
            if (viewState != null) {
                viewState.scaleX = 1.0f;
            }
            ExpandableViewState viewState2 = getViewState();
            if (viewState2 != null) {
                viewState2.scaleY = 1.0f;
            }
            setScaleX(1.0f);
            setScaleY(1.0f);
        }
        super.setIsChildInGroup(z, expandableNotificationRow);
    }

    public final void applyMiniWindowExpandParams(@Nullable MiniWindowExpandParameters miniWindowExpandParameters) {
        super.applyExpandAnimationParams(miniWindowExpandParameters);
        getContentView().animate().cancel();
        if (miniWindowExpandParameters != null) {
            int height = miniWindowExpandParameters.getHeight() - miniWindowExpandParameters.getStartHeight();
            float evaluateRowTranslationForMiniWindow = evaluateRowTranslationForMiniWindow(miniWindowExpandParameters.getLeft(), miniWindowExpandParameters.getRight());
            applyStateForMiniWindow(miniWindowExpandParameters.getAlpha(), miniWindowExpandParameters.getBackgroundAlpha(), (float) height);
            applyRowTranslationForMiniWindow(evaluateRowTranslationForMiniWindow);
            return;
        }
        applyStateForMiniWindow(1.0f, 1.0f, 0.0f);
        applyRowTranslationForMiniWindow(0.0f);
    }

    private final void applyStateForMiniWindow(float f, float f2, float f3) {
        View contentView = getContentView();
        Intrinsics.checkExpressionValueIsNotNull(contentView, "contentView");
        float f4 = f3 / 2.0f;
        contentView.setTranslationY(f4);
        View contentView2 = getContentView();
        Intrinsics.checkExpressionValueIsNotNull(contentView2, "contentView");
        contentView2.setAlpha(f);
        ExpandableViewState viewState = getViewState();
        if (viewState != null) {
            setTranslationZ(viewState.zTranslation * f);
        }
        ImageView mMiniWindowIcon = getMMiniWindowIcon();
        Intrinsics.checkExpressionValueIsNotNull(mMiniWindowIcon, "mMiniWindowIcon");
        mMiniWindowIcon.setTranslationY(f4);
        ImageView mMiniWindowIcon2 = getMMiniWindowIcon();
        Intrinsics.checkExpressionValueIsNotNull(mMiniWindowIcon2, "mMiniWindowIcon");
        mMiniWindowIcon2.setTransitionAlpha(f2);
        ImageView mMiniWindowIcon3 = getMMiniWindowIcon();
        Intrinsics.checkExpressionValueIsNotNull(mMiniWindowIcon3, "mMiniWindowIcon");
        int i = 0;
        if (f > ((float) 0)) {
            i = 8;
        }
        mMiniWindowIcon3.setVisibility(i);
        View mMiniBar = getMMiniBar();
        Intrinsics.checkExpressionValueIsNotNull(mMiniBar, "mMiniBar");
        mMiniBar.setTransitionAlpha(f2);
        NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
        Intrinsics.checkExpressionValueIsNotNull(notificationBackgroundView, "mBackgroundNormal");
        notificationBackgroundView.setTransitionAlpha(f2);
        FakeShadowView mFakeShadowView = getMFakeShadowView();
        Intrinsics.checkExpressionValueIsNotNull(mFakeShadowView, "mFakeShadowView");
        mFakeShadowView.setTransitionAlpha(f2);
    }

    private final float evaluateRowTranslationForMiniWindow(int i, int i2) {
        if (getViewState() != null) {
            ExpandableViewState viewState = getViewState();
            if (viewState == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (viewState.getTouchAnimating()) {
                return 0.0f;
            }
        }
        getLocationInWindow(this.mTmpPosition);
        return ((float) (i + ((i2 - i) / 2))) - ((((float) this.mTmpPosition[0]) - getTranslationX()) + (((float) getWidth()) / 2.0f));
    }

    private final void applyRowTranslationForMiniWindow(float f) {
        setTranslationX(f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView, com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setActualHeight(int i, boolean z) {
        super.setActualHeight(i, z);
        updateMiniBarTranslation();
    }

    private final void updateMiniBarTranslation() {
        View mMiniBar = getMMiniBar();
        Intrinsics.checkExpressionValueIsNotNull(mMiniBar, "mMiniBar");
        View mMiniBar2 = getMMiniBar();
        Intrinsics.checkExpressionValueIsNotNull(mMiniBar2, "mMiniBar");
        mMiniBar.setTranslationY((((float) getActualHeight()) - getMMiniBarMarginBottom()) - ((float) mMiniBar2.getMeasuredHeight()));
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public void setPinned(boolean z) {
        super.setPinned(z);
        updateMiniWindowBar();
        this.mBackgroundNormal.setHighSamplingFrequency(z);
    }

    public final void updateMiniWindowBar() {
        this.mCanSlide = getMAppMiniWindowManager().canNotificationSlide(getMiniWindowTargetPkg(), getPendingIntent());
        boolean z = false;
        boolean z2 = isPinned() || this.mIsInModal;
        if (this.mCanSlide && z2) {
            z = true;
        }
        setMiniBarVisible(z);
    }

    private final void setMiniBarVisible(boolean z) {
        View mMiniBar = getMMiniBar();
        Intrinsics.checkExpressionValueIsNotNull(mMiniBar, "mMiniBar");
        mMiniBar.setVisibility(z ? 0 : 8);
        ImageView mMiniWindowIcon = getMMiniWindowIcon();
        Drawable drawable = null;
        if (z) {
            NotificationEntry entry = getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry, "entry");
            ExpandedNotification sbn = entry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            Drawable appIcon = sbn.getAppIcon();
            if (appIcon != null) {
                drawable = CommonExtensionsKt.newMutatedDrawable(appIcon);
            }
        }
        mMiniWindowIcon.setImageDrawable(drawable);
        if (z) {
            updateMiniBarTranslation();
        }
    }

    public final void setIsInModal(boolean z) {
        this.mIsInModal = z;
        updateMiniWindowBar();
    }

    public final boolean canSlideToMiniWindow() {
        return this.mCanSlide;
    }

    public final int getMiniBarHeight() {
        View mMiniBar = getMMiniBar();
        Intrinsics.checkExpressionValueIsNotNull(mMiniBar, "mMiniBar");
        if (mMiniBar.getVisibility() != 0) {
            return 0;
        }
        View mMiniBar2 = getMMiniBar();
        Intrinsics.checkExpressionValueIsNotNull(mMiniBar2, "mMiniBar");
        return mMiniBar2.getMeasuredHeight();
    }

    @Nullable
    public final PendingIntent getPendingIntent() {
        NotificationEntry entry = getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "entry");
        ExpandedNotification sbn = entry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        return MiuiExpandableNotificationRowKt.access$getPendingIntent(sbn.getNotification());
    }

    @Nullable
    public final String getMiniWindowTargetPkg() {
        NotificationEntry entry = getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "entry");
        ExpandedNotification sbn = entry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
        if (sbn.isSubstituteNotification()) {
            return sbn.getPackageName();
        }
        PendingIntent pendingIntent = getPendingIntent();
        if (pendingIntent != null) {
            return pendingIntent.getCreatorPackage();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public boolean showSummaryBackground() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.showSummaryBackground();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public boolean showChildBackground() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.showChildBackground();
        }
        return isGroupExpanded() || isGroupExpansionChanging();
    }

    @NotNull
    public final View.OnClickListener getExpandClickListener() {
        View.OnClickListener onClickListener = this.mExpandClickListener;
        Intrinsics.checkExpressionValueIsNotNull(onClickListener, "mExpandClickListener");
        return onClickListener;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public boolean isBackgroundAnimating() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return super.isBackgroundAnimating();
        }
        return isSummaryWithChildren() && (isGroupExpanded() || isGroupExpansionChanging());
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    @Nullable
    public ExpandableViewState resetViewState() {
        ExpandableViewState resetViewState = super.resetViewState();
        if (isChildInGroup() || isSummaryWithChildren()) {
            if (resetViewState != null) {
                resetViewState.scaleX = 1.0f;
            }
            if (resetViewState != null) {
                resetViewState.scaleY = 1.0f;
            }
        }
        return resetViewState;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableOutlineView
    public boolean needsOutline() {
        if (isSummaryWithChildren()) {
            return false;
        }
        return super.needsOutline();
    }

    @NotNull
    public final NotificationBackgroundView getAnimatedBackground() {
        NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
        Intrinsics.checkExpressionValueIsNotNull(notificationBackgroundView, "mBackgroundNormal");
        return notificationBackgroundView;
    }

    /* access modifiers changed from: protected */
    public void damageInParent() {
        super.damageInParent();
        if (this.mLayoutInflated) {
            NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
            Intrinsics.checkExpressionValueIsNotNull(notificationBackgroundView, "mBackgroundNormal");
            if (notificationBackgroundView.getVisibility() == 0) {
                this.mBackgroundNormal.invalidate();
            }
            NotificationBackgroundView mBackgroundDimmed = getMBackgroundDimmed();
            Intrinsics.checkExpressionValueIsNotNull(mBackgroundDimmed, "mBackgroundDimmed");
            if (mBackgroundDimmed.getVisibility() == 0) {
                getMBackgroundDimmed().invalidate();
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
    public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
        startTouchAnimateIfNeed(motionEvent);
        if (needInterceptTouch()) {
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    private final boolean needInterceptTouch() {
        return isDimmed() && !isActive();
    }

    private final void startTouchAnimateIfNeed(MotionEvent motionEvent) {
        if (motionEvent != null) {
            if (!isChildInGroup() || isGroupExpanded()) {
                if (!isClickable()) {
                    setClickable(true);
                }
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    ExpandableViewState viewState = getViewState();
                    if (viewState == null) {
                        Intrinsics.throwNpe();
                        throw null;
                    } else if (0.95f != viewState.scaleX) {
                        ExpandableViewState viewState2 = getViewState();
                        if (viewState2 == null) {
                            Intrinsics.throwNpe();
                            throw null;
                        } else if (!viewState2.getTouchAnimating()) {
                            startTouchScaleAnimateIfNeed(0.95f);
                        }
                    }
                } else if (actionMasked == 1 || actionMasked == 3) {
                    postDelayed(new MiuiExpandableNotificationRow$startTouchAnimateIfNeed$1(this), isPinned() ? 120 : 0);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final void startTouchScaleAnimateIfNeed(float f) {
        if (DebugConfig.DEBUG) {
            String str = this.TAG;
            Log.d(str, "animateTouchScale scale=" + f + ", changing=" + isGroupExpansionChanging());
        }
        RowAnimationUtils.INSTANCE.startTouchAnimationIfNeed(this, f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void startActivateAnimation(boolean z) {
        Interpolator interpolator;
        if (isAttachedToWindow() && isDimmable()) {
            float f = z ? 1.0f : 1.05f;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "scaleX", f);
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this, "scaleY", f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(ofFloat).with(ofFloat2);
            if (!z) {
                interpolator = Interpolators.ALPHA_IN;
                Intrinsics.checkExpressionValueIsNotNull(interpolator, "Interpolators.ALPHA_IN");
            } else {
                interpolator = Interpolators.ALPHA_OUT;
                Intrinsics.checkExpressionValueIsNotNull(interpolator, "Interpolators.ALPHA_OUT");
            }
            animatorSet.setInterpolator(interpolator);
            animatorSet.setDuration((long) 220);
            if (z) {
                animatorSet.addListener(new MiuiExpandableNotificationRow$startActivateAnimation$1(this));
                animatorSet.start();
                return;
            }
            animatorSet.start();
            setTouchAnimatingState(true);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void makeInactive(boolean z) {
        if (isActive() && !z) {
            resetActivateAnimationIfNeed();
        }
        super.makeInactive(z);
    }

    private final void resetActivateAnimationIfNeed() {
        if (getScaleX() != 1.0f || getScaleY() != 1.0f) {
            if (isDimmed()) {
                setScaleX(1.0f);
                setScaleY(1.0f);
                setTouchAnimatingState(false);
                return;
            }
            startActivateAnimation(true);
        }
    }

    /* access modifiers changed from: private */
    public final void setTouchAnimatingState(boolean z) {
        ExpandableViewState viewState = getViewState();
        if (viewState != null) {
            viewState.setTouchAnimating(z);
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
