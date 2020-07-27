package com.android.systemui.statusbar.policy;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;

public class BrightnessMirrorController {
    public long TRANSITION_DURATION_IN = 200;
    public long TRANSITION_DURATION_OUT = 150;
    /* access modifiers changed from: private */
    public View mBrightnessMirror;
    private final int[] mInt2Cache = new int[2];
    private FrameLayout mMirrorContent;
    private final View mNotificationPanel;
    private final View mNotificationsQuickSettingsContainer;
    /* access modifiers changed from: private */
    public View mQSBrightness;
    private final ScrimView mScrimBehind;
    /* access modifiers changed from: private */
    public final NotificationStackScrollLayout mStackScroller;
    private final StatusBarWindowView mStatusBarWindow;

    public BrightnessMirrorController(StatusBarWindowView statusBarWindowView) {
        this.mStatusBarWindow = statusBarWindowView;
        this.mScrimBehind = (ScrimView) statusBarWindowView.findViewById(R.id.scrim_behind);
        this.mBrightnessMirror = statusBarWindowView.findViewById(R.id.brightness_mirror);
        this.mMirrorContent = (FrameLayout) this.mBrightnessMirror.findViewById(R.id.mirror_content);
        this.mNotificationPanel = statusBarWindowView.findViewById(R.id.notification_panel);
        this.mNotificationsQuickSettingsContainer = this.mNotificationPanel.findViewById(R.id.notification_container_parent);
        this.mStackScroller = (NotificationStackScrollLayout) statusBarWindowView.findViewById(R.id.notification_stack_scroller);
    }

    public void showMirror() {
        if (this.mQSBrightness == null) {
            this.mQSBrightness = this.mNotificationPanel.findViewById(R.id.qs_brightness);
        }
        this.mQSBrightness.setVisibility(4);
        this.mBrightnessMirror.setVisibility(0);
        this.mStackScroller.setFadingOut(true);
        this.mScrimBehind.animateViewAlpha(0.0f, this.TRANSITION_DURATION_OUT, Interpolators.ALPHA_OUT);
        outAnimation(this.mNotificationsQuickSettingsContainer.animate()).withLayer().withEndAction(new Runnable() {
            public void run() {
                ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).toggleBlurBackgroundByBrightnessMirror(false);
            }
        });
    }

    public void hideMirror() {
        this.mScrimBehind.animateViewAlpha(1.0f, this.TRANSITION_DURATION_IN, Interpolators.ALPHA_IN);
        inAnimation(this.mNotificationsQuickSettingsContainer.animate()).withLayer().withEndAction(new Runnable() {
            public void run() {
                BrightnessMirrorController.this.mQSBrightness.setVisibility(0);
                BrightnessMirrorController.this.mBrightnessMirror.setVisibility(4);
                BrightnessMirrorController.this.mStackScroller.setFadingOut(false);
            }
        });
        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).toggleBlurBackgroundByBrightnessMirror(true);
    }

    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(0.0f).setDuration(this.TRANSITION_DURATION_OUT).setInterpolator(Interpolators.MIUI_ALPHA_OUT).withEndAction((Runnable) null);
    }

    private ViewPropertyAnimator inAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(1.0f).setDuration(this.TRANSITION_DURATION_IN).setInterpolator(Interpolators.MIUI_ALPHA_IN);
    }

    public void setLocation(View view) {
        view.getLocationInWindow(this.mInt2Cache);
        int width = this.mInt2Cache[0] + (view.getWidth() / 2);
        int height = this.mInt2Cache[1] + (view.getHeight() / 2);
        this.mBrightnessMirror.setTranslationX(0.0f);
        this.mBrightnessMirror.setTranslationY(0.0f);
        this.mMirrorContent.getLocationInWindow(this.mInt2Cache);
        int width2 = this.mInt2Cache[0] + (this.mMirrorContent.getWidth() / 2);
        int height2 = this.mInt2Cache[1] + (this.mMirrorContent.getHeight() / 2);
        this.mBrightnessMirror.setTranslationX((float) (width - width2));
        this.mBrightnessMirror.setTranslationY((float) (height - height2));
    }

    public View getMirror() {
        return this.mBrightnessMirror;
    }

    public void updateResources() {
        Resources resources = this.mBrightnessMirror.getResources();
        int integer = resources.getInteger(R.integer.notification_panel_layout_gravity);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mBrightnessMirror.getLayoutParams();
        if (layoutParams.gravity != integer) {
            layoutParams.gravity = integer;
            this.mBrightnessMirror.setLayoutParams(layoutParams);
        }
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mMirrorContent.getLayoutParams();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.notification_panel_width);
        if (layoutParams2.width != dimensionPixelSize) {
            layoutParams2.width = dimensionPixelSize;
            this.mMirrorContent.setLayoutParams(layoutParams2);
        }
    }

    public void onDensityOrFontScaleChanged() {
        int indexOfChild = this.mStatusBarWindow.indexOfChild(this.mBrightnessMirror);
        this.mStatusBarWindow.removeView(this.mBrightnessMirror);
        this.mBrightnessMirror = LayoutInflater.from(this.mBrightnessMirror.getContext()).inflate(R.layout.brightness_mirror, this.mStatusBarWindow, false);
        this.mStatusBarWindow.addView(this.mBrightnessMirror, indexOfChild);
        this.mMirrorContent = (FrameLayout) this.mBrightnessMirror.findViewById(R.id.mirror_content);
    }
}
