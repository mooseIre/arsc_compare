package com.android.systemui.miui.volume;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import com.android.systemui.Constants;
import com.android.systemui.Interpolators;
import com.android.systemui.Logger;
import com.android.systemui.miui.ViewStateGroup;
import com.android.systemui.statistic.ScenarioConstants;
import com.android.systemui.statistic.ScenarioTrackUtil;
import com.android.systemui.volume.VolumeDialogMotion$Callback;
import java.util.ArrayList;
import java.util.List;

public class MiuiVolumeDialogMotion {
    /* access modifiers changed from: private */
    public static final String TAG = "MiuiVolumeDialogMotion";
    /* access modifiers changed from: private */
    public Callback mCallback;
    /* access modifiers changed from: private */
    public Animator mCollapseAnimator;
    private ViewStateGroup mCollapsedStates;
    private Context mContext;
    private List<View> mCornerBgViews = new ArrayList();
    private float mCornerRadiusCollapsed;
    private float mCornerRadiusExpanded;
    private View mDialogContentView;
    /* access modifiers changed from: private */
    public View mDialogView;
    private ObjectAnimator mDismissAnimator;
    private Display mDisplay;
    /* access modifiers changed from: private */
    public float mElevationCollapsed;
    /* access modifiers changed from: private */
    public Animator mExpandAnimator;
    private View mExpandButton;
    private boolean mExpanded;
    private ViewStateGroup mExpandedStates;
    private View mRingerModeLayout;
    private ObjectAnimator mShowAnimator;
    private FrameLayout mTempColumnContainer;

    public interface Callback extends VolumeDialogMotion$Callback {
        void onDismiss();

        void onShow();
    }

    public MiuiVolumeDialogMotion(View view, ViewGroup viewGroup, FrameLayout frameLayout, View view2, View view3) {
        Context context = view.getContext();
        this.mContext = context;
        this.mDialogView = view;
        this.mDialogContentView = viewGroup;
        this.mTempColumnContainer = frameLayout;
        this.mExpandButton = view2;
        this.mRingerModeLayout = view3;
        this.mCornerRadiusExpanded = (float) context.getResources().getDimensionPixelSize(R$dimen.miui_volume_bg_radius_expanded);
        this.mCornerRadiusCollapsed = (float) this.mContext.getResources().getDimensionPixelSize(R$dimen.miui_volume_bg_radius);
        this.mElevationCollapsed = this.mContext.getResources().getDimension(R$dimen.miui_volume_elevation_collapsed);
        setupAnimationInfo();
        setupStates();
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setDisplay(Display display) {
        this.mDisplay = display;
        if (display != null) {
            updateStates();
        }
    }

    public void updateStates() {
        setupStates();
        if (this.mExpanded) {
            this.mExpandedStates.apply((ViewGroup) this.mDialogView);
        } else {
            this.mCollapsedStates.apply((ViewGroup) this.mDialogView);
        }
    }

    private void setupAnimationInfo() {
        this.mCornerBgViews.add(this.mDialogContentView);
        this.mCornerBgViews.add(this.mRingerModeLayout);
        this.mCornerBgViews.add(this.mTempColumnContainer);
        if (this.mExpandAnimator == null) {
            this.mExpandAnimator = AnimatorInflater.loadAnimator(this.mContext, R$animator.miui_volume_bg_expand);
        }
        if (this.mCollapseAnimator == null) {
            this.mCollapseAnimator = AnimatorInflater.loadAnimator(this.mContext, R$animator.miui_volume_bg_collapse);
        }
        if (this.mShowAnimator == null) {
            ObjectAnimator createAnimator = createAnimator(true);
            this.mShowAnimator = createAnimator;
            createAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    MiuiVolumeDialogMotion.this.mCallback.onAnimatingChanged(true);
                }

                public void onAnimationEnd(Animator animator) {
                    Logger.i(MiuiVolumeDialogMotion.TAG, "startShowAnimation end!");
                    MiuiVolumeDialogMotion.this.mCallback.onAnimatingChanged(false);
                    MiuiVolumeDialogMotion miuiVolumeDialogMotion = MiuiVolumeDialogMotion.this;
                    miuiVolumeDialogMotion.setViewsElevation(miuiVolumeDialogMotion.mElevationCollapsed);
                    ScenarioTrackUtil.finishScenario(ScenarioConstants.SCENARIO_VOLUME_DIALOG_SHOW);
                }
            });
        }
        if (this.mDismissAnimator == null) {
            this.mDismissAnimator = createAnimator(false);
        }
    }

    private ObjectAnimator createAnimator(boolean z) {
        View view = this.mDialogView;
        float[] fArr = new float[1];
        fArr[0] = z ? 0.0f : (float) view.getWidth();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "translationX", fArr);
        ofFloat.setDuration(300).setInterpolator(z ? Interpolators.DECELERATE_QUART : Interpolators.ACCELERATE_DECELERATE);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiVolumeDialogMotion.this.lambda$createAnimator$0$MiuiVolumeDialogMotion(this.f$1, valueAnimator);
            }
        });
        return ofFloat;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createAnimator$0 */
    public /* synthetic */ void lambda$createAnimator$0$MiuiVolumeDialogMotion(boolean z, ValueAnimator valueAnimator) {
        setViewsAlpha(z ? valueAnimator.getAnimatedFraction() : 1.0f - valueAnimator.getAnimatedFraction());
    }

    private void setupStates() {
        int i;
        int i2;
        Display display;
        Resources resources = this.mContext.getResources();
        boolean z = resources.getConfiguration().orientation == 2;
        int dimensionPixelSize = resources.getDimensionPixelSize(R$dimen.miui_volume_offset_end);
        if (z && Constants.IS_NOTCH && (display = this.mDisplay) != null) {
            if (display.getRotation() == 3) {
                dimensionPixelSize += resources.getDimensionPixelSize(R$dimen.miui_volume_status_bar_height);
            }
        }
        ViewStateGroup.Builder builder = new ViewStateGroup.Builder(this.mContext);
        builder.addStateWithIntRes(this.mDialogView.getId(), 1, R$integer.miui_volume_dialog_gravity_collapsed);
        builder.addState(this.mDialogView.getId(), 11, 1);
        builder.addStateWithIntDimen(this.mDialogView.getId(), 6, R$dimen.miui_volume_offset_top_collapsed);
        builder.addState(this.mDialogView.getId(), 5, 0);
        builder.addState(this.mDialogView.getId(), 7, dimensionPixelSize);
        builder.addState(this.mDialogView.getId(), 12, 8388613);
        builder.addState(this.mDialogContentView.getId(), 2, -2);
        builder.addState(this.mDialogContentView.getId(), 3, -2);
        builder.addState(this.mRingerModeLayout.getId(), 2, -2);
        builder.addState(this.mRingerModeLayout.getId(), 3, -2);
        builder.addState(this.mRingerModeLayout.getId(), 11, 1);
        builder.addStateWithIntDimen(this.mRingerModeLayout.getId(), 6, R$dimen.miui_volume_footer_margin_top);
        builder.addStateWithIntDimen(this.mRingerModeLayout.getId(), 5, R$dimen.miui_volume_footer_margin_left);
        builder.addState(this.mExpandButton.getId(), 10, 0);
        builder.addStateWithIntDimen(R$id.miui_ringer_state_layout, 2, R$dimen.miui_volume_silence_button_height);
        builder.addStateWithIntDimen(R$id.miui_ringer_state_layout, 3, R$dimen.miui_volume_silence_button_height);
        builder.addState(R$id.miui_volume_ringer_divider, 10, 8);
        builder.addStateWithIntDimen(this.mRingerModeLayout.getId(), 9, R$dimen.miui_volume_bg_padding);
        this.mCollapsedStates = builder.build();
        if (this.mContext.getResources().getBoolean(R$bool.miui_volume_expand_freeland)) {
            i2 = (int) resources.getDimension(R$dimen.miui_volume_offset_top_collapsed);
        } else if (z) {
            i = ((int) (((((float) resources.getDisplayMetrics().widthPixels) - resources.getDimension(R$dimen.miui_volume_content_width_expanded)) - resources.getDimension(R$dimen.miui_volume_ringer_btn_layout_width)) - resources.getDimension(R$dimen.miui_volume_footer_margin_left_expanded))) / 2;
            i2 = 0;
            ViewStateGroup.Builder builder2 = new ViewStateGroup.Builder(this.mContext);
            builder2.addStateWithIntRes(this.mDialogView.getId(), 1, R$integer.miui_volume_dialog_gravity_expanded);
            builder2.addStateWithIntRes(this.mDialogView.getId(), 11, R$integer.miui_volume_layout_orientation_expanded);
            builder2.addState(this.mDialogView.getId(), 6, i2);
            builder2.addState(this.mDialogView.getId(), 5, i);
            builder2.addStateWithIntDimen(this.mDialogView.getId(), 7, R$dimen.miui_volume_offset_end_expanded);
            builder2.addState(this.mDialogView.getId(), 12, 1);
            builder2.addStateWithIntDimen(this.mDialogContentView.getId(), 2, R$dimen.miui_volume_content_width_expanded);
            builder2.addStateWithIntDimen(this.mDialogContentView.getId(), 3, R$dimen.miui_volume_content_height_expanded);
            builder2.addStateWithIntDimen(this.mRingerModeLayout.getId(), 2, R$dimen.miui_volume_ringer_layout_width_expanded);
            builder2.addStateWithIntDimen(this.mRingerModeLayout.getId(), 3, R$dimen.miui_volume_ringer_layout_height_expanded);
            builder2.addStateWithIntRes(this.mRingerModeLayout.getId(), 11, R$integer.miui_volume_layout_orientation_expanded);
            builder2.addStateWithIntDimen(this.mRingerModeLayout.getId(), 6, R$dimen.miui_volume_footer_margin_top_expanded);
            builder2.addStateWithIntDimen(this.mRingerModeLayout.getId(), 5, R$dimen.miui_volume_footer_margin_left_expanded);
            builder2.addState(this.mExpandButton.getId(), 10, 8);
            builder2.addState(this.mTempColumnContainer.getId(), 10, 8);
            builder2.addStateWithIntDimen(R$id.miui_ringer_state_layout, 2, R$dimen.miui_volume_ringer_btn_layout_width);
            builder2.addStateWithIntDimen(R$id.miui_ringer_state_layout, 3, R$dimen.miui_volume_ringer_btn_layout_height);
            builder2.addState(R$id.miui_volume_ringer_divider, 10, 0);
            builder2.addState(this.mRingerModeLayout.getId(), 9, 0);
            this.mExpandedStates = builder2.build();
        } else {
            i2 = ((int) (((((float) resources.getDisplayMetrics().heightPixels) - resources.getDimension(R$dimen.miui_volume_content_height_expanded)) - resources.getDimension(R$dimen.miui_volume_ringer_btn_layout_height)) - resources.getDimension(R$dimen.miui_volume_footer_margin_top_expanded))) / 2;
        }
        i = 0;
        ViewStateGroup.Builder builder22 = new ViewStateGroup.Builder(this.mContext);
        builder22.addStateWithIntRes(this.mDialogView.getId(), 1, R$integer.miui_volume_dialog_gravity_expanded);
        builder22.addStateWithIntRes(this.mDialogView.getId(), 11, R$integer.miui_volume_layout_orientation_expanded);
        builder22.addState(this.mDialogView.getId(), 6, i2);
        builder22.addState(this.mDialogView.getId(), 5, i);
        builder22.addStateWithIntDimen(this.mDialogView.getId(), 7, R$dimen.miui_volume_offset_end_expanded);
        builder22.addState(this.mDialogView.getId(), 12, 1);
        builder22.addStateWithIntDimen(this.mDialogContentView.getId(), 2, R$dimen.miui_volume_content_width_expanded);
        builder22.addStateWithIntDimen(this.mDialogContentView.getId(), 3, R$dimen.miui_volume_content_height_expanded);
        builder22.addStateWithIntDimen(this.mRingerModeLayout.getId(), 2, R$dimen.miui_volume_ringer_layout_width_expanded);
        builder22.addStateWithIntDimen(this.mRingerModeLayout.getId(), 3, R$dimen.miui_volume_ringer_layout_height_expanded);
        builder22.addStateWithIntRes(this.mRingerModeLayout.getId(), 11, R$integer.miui_volume_layout_orientation_expanded);
        builder22.addStateWithIntDimen(this.mRingerModeLayout.getId(), 6, R$dimen.miui_volume_footer_margin_top_expanded);
        builder22.addStateWithIntDimen(this.mRingerModeLayout.getId(), 5, R$dimen.miui_volume_footer_margin_left_expanded);
        builder22.addState(this.mExpandButton.getId(), 10, 8);
        builder22.addState(this.mTempColumnContainer.getId(), 10, 8);
        builder22.addStateWithIntDimen(R$id.miui_ringer_state_layout, 2, R$dimen.miui_volume_ringer_btn_layout_width);
        builder22.addStateWithIntDimen(R$id.miui_ringer_state_layout, 3, R$dimen.miui_volume_ringer_btn_layout_height);
        builder22.addState(R$id.miui_volume_ringer_divider, 10, 0);
        builder22.addState(this.mRingerModeLayout.getId(), 9, 0);
        this.mExpandedStates = builder22.build();
    }

    public void startExpandH(boolean z) {
        if (this.mExpandAnimator.isRunning()) {
            this.mExpandAnimator.cancel();
        }
        if (this.mCollapseAnimator.isRunning()) {
            this.mCollapseAnimator.cancel();
        }
        if (z) {
            this.mExpandAnimator.setTarget(this);
            this.mExpandAnimator.start();
            this.mExpandAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    MiuiVolumeDialogMotion.this.mExpandAnimator.setTarget((Object) null);
                }
            });
            this.mExpandedStates.apply((ViewGroup) this.mDialogView);
            ScenarioTrackUtil.finishScenario(ScenarioConstants.SCENARIO_EXPAND_VOLUME_DIALOG);
        } else {
            this.mCollapseAnimator.setTarget(this);
            this.mCollapseAnimator.start();
            this.mCollapseAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    MiuiVolumeDialogMotion.this.mCollapseAnimator.setTarget((Object) null);
                }
            });
            this.mCollapsedStates.apply((ViewGroup) this.mDialogView);
        }
        this.mExpanded = z;
    }

    public void setExpandFraction(float f) {
        float f2 = this.mCornerRadiusCollapsed;
        float f3 = f2 + ((this.mCornerRadiusExpanded - f2) * f);
        for (View applyCornerRadius : this.mCornerBgViews) {
            applyCornerRadius(applyCornerRadius, f3);
        }
        setViewsElevation(this.mElevationCollapsed * (1.0f - f));
    }

    /* access modifiers changed from: private */
    public void setViewsElevation(float f) {
        this.mDialogContentView.setElevation(f);
        this.mRingerModeLayout.setElevation(f);
        this.mTempColumnContainer.setElevation(f);
    }

    private void setViewsAlpha(float f) {
        this.mDialogContentView.setAlpha(f);
        this.mRingerModeLayout.setAlpha(f);
        this.mTempColumnContainer.setAlpha(f);
    }

    private void applyCornerRadius(View view, float f) {
        Drawable background = view.getBackground();
        if (background != null && (background instanceof GradientDrawable)) {
            ((GradientDrawable) background).setCornerRadius(f);
        }
    }

    public boolean isAnimating() {
        return this.mShowAnimator.isRunning() || this.mDismissAnimator.isRunning();
    }

    public void startShow() {
        String str = TAG;
        Logger.i(str, "startShow mShowing:" + this.mShowAnimator.isRunning() + " mDismissing:" + this.mDismissAnimator.isRunning());
        if (!this.mShowAnimator.isRunning()) {
            if (this.mDismissAnimator.isRunning()) {
                this.mDismissAnimator.cancel();
                startShowAnimation();
                return;
            }
            this.mCallback.onShow();
            this.mDialogView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    MiuiVolumeDialogMotion.this.mDialogView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    MiuiVolumeDialogMotion.this.mDialogView.setTranslationX((float) MiuiVolumeDialogMotion.this.mDialogView.getMeasuredWidth());
                    MiuiVolumeDialogMotion.this.mDialogView.requestLayout();
                    MiuiVolumeDialogMotion.this.startShowAnimation();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void startShowAnimation() {
        String str = TAG;
        Logger.i(str, "startShowAnimation mShowAnimator:" + this.mShowAnimator);
        this.mShowAnimator.start();
    }

    private void startDismissAnimation(final Runnable runnable) {
        String str = TAG;
        Logger.i(str, "startDismissAnimation mDismissAnimator:" + this.mDismissAnimator);
        this.mDismissAnimator.setFloatValues(new float[]{(float) this.mDialogView.getWidth()});
        this.mDismissAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                MiuiVolumeDialogMotion.this.mCallback.onAnimatingChanged(true);
            }

            public void onAnimationEnd(Animator animator) {
                Logger.i(MiuiVolumeDialogMotion.TAG, "startDismissAnimation end!");
                MiuiVolumeDialogMotion.this.mCallback.onAnimatingChanged(false);
                MiuiVolumeDialogMotion.this.mCallback.onDismiss();
                runnable.run();
                ScenarioTrackUtil.finishScenario(ScenarioConstants.SCENARIO_VOLUME_DIALOG_HIDE);
                animator.removeListener(this);
            }
        });
        this.mDismissAnimator.start();
    }

    public void startDismiss(Runnable runnable) {
        String str = TAG;
        Logger.i(str, "startDismiss mDismissing:" + this.mDismissAnimator.isRunning() + " mShowing:" + this.mShowAnimator.isRunning() + " isShown:" + this.mDialogView.isShown());
        if (!this.mDismissAnimator.isRunning()) {
            if (this.mShowAnimator.isRunning()) {
                this.mShowAnimator.cancel();
            }
            if (this.mDialogView.isShown()) {
                startDismissAnimation(runnable);
            }
        }
    }
}
