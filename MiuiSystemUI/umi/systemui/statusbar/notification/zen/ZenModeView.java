package com.android.systemui.statusbar.notification.zen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.stack.SwipeableView;

public class ZenModeView extends ActivatableNotificationView implements SwipeableView {
    private ViewGroup mContent;
    /* access modifiers changed from: private */
    public Animator mTranslateAnim;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public final void onClick(View view) {
            ZenModeView.this.lambda$new$0$ZenModeView(view);
        }
    };

    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        return true;
    }

    public boolean getCanSwipe() {
        return true;
    }

    public boolean hasFinishedInitialization() {
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$ZenModeView(View view) {
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(generateSilentModeIntent(), 0);
    }

    public ZenModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup viewGroup = (ViewGroup) findViewById(C0015R$id.content);
        this.mContent = viewGroup;
        viewGroup.setOnClickListener(this.onClickListener);
    }

    /* access modifiers changed from: protected */
    public View getContentView() {
        return this.mContent;
    }

    public boolean isVisiable() {
        return this.mContent.getVisibility() == 0;
    }

    public boolean handleSlideBack() {
        if (getTranslationX() == 0.0f) {
            return false;
        }
        animateTranslateNotification(0.0f);
        return true;
    }

    public void animateTranslateNotification(float f) {
        Animator animator = this.mTranslateAnim;
        if (animator != null) {
            animator.cancel();
        }
        Animator translateViewAnimator = getTranslateViewAnimator(f);
        this.mTranslateAnim = translateViewAnimator;
        if (translateViewAnimator != null) {
            translateViewAnimator.start();
        }
    }

    public Animator getTranslateViewAnimator(final float f) {
        Animator animator = this.mTranslateAnim;
        if (animator != null) {
            animator.cancel();
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "translationX", new float[]{getTranslationX(), f});
        ofFloat.addListener(new AnimatorListenerAdapter() {
            boolean cancelled = false;

            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (!this.cancelled && f == 0.0f) {
                    Animator unused = ZenModeView.this.mTranslateAnim = null;
                }
            }
        });
        this.mTranslateAnim = ofFloat;
        return ofFloat;
    }

    private Intent generateSilentModeIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$MiuiSilentModeAcivity"));
        return intent;
    }

    public void setTranslation(float f) {
        setTranslationX(f);
    }

    public float getTranslation() {
        return getTranslationX();
    }

    public void resetTranslation() {
        Animator animator = this.mTranslateAnim;
        if (animator != null) {
            animator.cancel();
        }
        setTranslation(0.0f);
        setTransitionAlpha(1.0f);
    }
}
