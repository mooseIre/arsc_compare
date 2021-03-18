package com.android.systemui.controlcenter.qs.tileview;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlPanelContentView;
import com.miui.systemui.util.MiuiInterpolators;

public class QCBrightnessMirrorController {
    public long TRANSITION_DURATION_IN = 200;
    public long TRANSITION_DURATION_OUT = 150;
    private View mBrightnessMirror;
    private final View mContent;
    private final ControlPanelContentView mControlPanelContentView;
    private final int[] mInt2Cache = new int[2];
    private FrameLayout mMirrorContent;
    private View mQSBrightness;

    public QCBrightnessMirrorController(ControlPanelContentView controlPanelContentView) {
        this.mControlPanelContentView = controlPanelContentView;
        View findViewById = controlPanelContentView.findViewById(C0015R$id.brightness_mirror);
        this.mBrightnessMirror = findViewById;
        this.mMirrorContent = (FrameLayout) findViewById.findViewById(C0015R$id.mirror_content);
        this.mContent = this.mControlPanelContentView.findViewById(C0015R$id.control_center_panel);
    }

    public void showMirror() {
        if (this.mQSBrightness == null) {
            this.mQSBrightness = this.mContent.findViewById(C0015R$id.qs_brightness);
        }
        this.mQSBrightness.setVisibility(4);
        this.mBrightnessMirror.setVisibility(0);
        outAnimation(this.mContent.animate()).withLayer().setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.controlcenter.qs.tileview.QCBrightnessMirrorController.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                QCBrightnessMirrorController.this.mControlPanelContentView.setControlPanelWindowBlurRatio(1.0f - valueAnimator.getAnimatedFraction());
            }
        });
        ((ControlCenterPanelView) this.mControlPanelContentView.findViewById(C0015R$id.control_center_panel)).setTouchable(false);
    }

    public void hideMirror() {
        inAnimation(this.mContent.animate()).withLayer().withStartAction(new Runnable() {
            /* class com.android.systemui.controlcenter.qs.tileview.$$Lambda$QCBrightnessMirrorController$6x4k8CXt4z15SmXjJtzjW9FffXs */

            public final void run() {
                QCBrightnessMirrorController.this.lambda$hideMirror$0$QCBrightnessMirrorController();
            }
        }).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.controlcenter.qs.tileview.QCBrightnessMirrorController.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                QCBrightnessMirrorController.this.mControlPanelContentView.setControlPanelWindowBlurRatio(valueAnimator.getAnimatedFraction());
                QCBrightnessMirrorController.this.mBrightnessMirror.setAlpha(1.0f - valueAnimator.getAnimatedFraction());
                QCBrightnessMirrorController.this.mQSBrightness.setAlpha(valueAnimator.getAnimatedFraction());
            }
        }).withEndAction(new Runnable() {
            /* class com.android.systemui.controlcenter.qs.tileview.QCBrightnessMirrorController.AnonymousClass2 */

            public void run() {
                QCBrightnessMirrorController.this.mBrightnessMirror.setAlpha(1.0f);
                QCBrightnessMirrorController.this.mBrightnessMirror.setVisibility(4);
            }
        });
        ((ControlCenterPanelView) this.mControlPanelContentView.findViewById(C0015R$id.control_center_panel)).setTouchable(true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideMirror$0 */
    public /* synthetic */ void lambda$hideMirror$0$QCBrightnessMirrorController() {
        this.mQSBrightness.setVisibility(0);
    }

    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator viewPropertyAnimator) {
        return viewPropertyAnimator.alpha(0.0f).setDuration(this.TRANSITION_DURATION_OUT).setInterpolator(MiuiInterpolators.MIUI_ALPHA_OUT).withEndAction(null);
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
}
