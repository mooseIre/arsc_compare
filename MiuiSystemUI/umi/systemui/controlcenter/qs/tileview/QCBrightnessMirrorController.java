package com.android.systemui.controlcenter.qs.tileview;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.phone.ControlPanelContentView;
import com.miui.systemui.util.MiuiInterpolators;

public class QCBrightnessMirrorController {
    public long TRANSITION_DURATION_IN = 200;
    public long TRANSITION_DURATION_OUT = 150;
    /* access modifiers changed from: private */
    public View mBrightnessMirror;
    private final View mContent;
    /* access modifiers changed from: private */
    public final ControlPanelContentView mControlPanelContentView;
    private final int[] mInt2Cache = new int[2];
    private FrameLayout mMirrorContent;
    /* access modifiers changed from: private */
    public View mQSBrightness;

    public QCBrightnessMirrorController(ControlPanelContentView controlPanelContentView) {
        this.mControlPanelContentView = controlPanelContentView;
        View findViewById = controlPanelContentView.findViewById(C0015R$id.brightness_mirror);
        this.mBrightnessMirror = findViewById;
        this.mMirrorContent = (FrameLayout) findViewById.findViewById(C0015R$id.mirror_content);
        this.mContent = this.mControlPanelContentView.findViewById(C0015R$id.qs_control_center_panel);
    }

    public void showMirror() {
        if (this.mQSBrightness == null) {
            this.mQSBrightness = this.mContent.findViewById(C0015R$id.qs_brightness);
        }
        this.mQSBrightness.setVisibility(4);
        this.mBrightnessMirror.setVisibility(0);
        outAnimation(this.mContent.animate()).withLayer().withEndAction(new Runnable() {
            public void run() {
                QCBrightnessMirrorController.this.mControlPanelContentView.setControlPanelWindowBlurRatio(0.0f);
            }
        });
    }

    public void hideMirror() {
        inAnimation(this.mContent.animate()).withLayer().withEndAction(new Runnable() {
            public void run() {
                QCBrightnessMirrorController.this.mQSBrightness.setVisibility(0);
                QCBrightnessMirrorController.this.mBrightnessMirror.setVisibility(4);
            }
        });
        this.mControlPanelContentView.setControlPanelWindowBlurRatio(1.0f);
    }

    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(0.0f).setDuration(this.TRANSITION_DURATION_OUT).setInterpolator(MiuiInterpolators.MIUI_ALPHA_OUT).withEndAction((Runnable) null);
    }

    private ViewPropertyAnimator inAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(1.0f).setDuration(this.TRANSITION_DURATION_IN).setInterpolator(MiuiInterpolators.MIUI_ALPHA_IN);
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
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mMirrorContent.getLayoutParams();
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.qs_control_brightness_width);
        if (layoutParams.width != dimensionPixelSize) {
            layoutParams.width = dimensionPixelSize;
            this.mMirrorContent.setLayoutParams(layoutParams);
        }
    }
}
