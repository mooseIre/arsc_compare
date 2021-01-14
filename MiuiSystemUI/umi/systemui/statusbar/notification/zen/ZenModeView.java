package com.android.systemui.statusbar.notification.zen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import com.android.systemui.statusbar.notification.stack.SwipeableView;

public class ZenModeView extends ActivatableNotificationView implements SwipeableView {
    private final String CONTENT_ALL_TIME = getResources().getString(C0021R$string.zen_mode_warnings_all_time_content);
    private final String CONTENT_KEYGUARD = getResources().getString(C0021R$string.zen_mode_warnings_keyguard_content);
    private NotificationBackgroundView mBackgroundDimmed;
    private NotificationBackgroundView mBackgroundNormal;
    private ViewGroup mContent;
    public ZenModeViewController mController;
    /* access modifiers changed from: private */
    public Animator mTranslateAnim;

    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    public boolean getCanSwipe() {
        return true;
    }

    public boolean hasFinishedInitialization() {
        return true;
    }

    public ZenModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup viewGroup = (ViewGroup) findViewById(C0015R$id.content);
        this.mContent = viewGroup;
        viewGroup.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                ZenModeView.this.lambda$onFinishInflate$0$ZenModeView(view);
            }
        });
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(C0015R$id.backgroundNormal);
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(C0015R$id.backgroundDimmed);
        updateBackgroundBg();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$ZenModeView(View view) {
        ZenModeViewController zenModeViewController = this.mController;
        if (zenModeViewController != null) {
            zenModeViewController.jump2Settings();
        }
    }

    private void updateBackgroundBg() {
        this.mBackgroundNormal.setCustomBackground(C0013R$drawable.notification_item_bg);
    }

    /* access modifiers changed from: protected */
    public View getContentView() {
        return this.mContent;
    }

    public void setController(ZenModeViewController zenModeViewController) {
        this.mController = zenModeViewController;
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

    /* access modifiers changed from: protected */
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        ViewGroup viewGroup = this.mContent;
        if (viewGroup != null) {
            return !isDownEventInQuitView(motionEvent, (TextView) viewGroup.findViewById(C0015R$id.zen_quit));
        }
        return true;
    }

    private boolean isDownEventInQuitView(MotionEvent motionEvent, View view) {
        if (view == null) {
            return false;
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        if (motionEvent.getRawX() < ((float) iArr[0]) || motionEvent.getRawX() > ((float) (iArr[0] + view.getWidth())) || motionEvent.getRawY() < ((float) iArr[1]) || motionEvent.getRawY() > ((float) (iArr[1] + view.getHeight()))) {
            return false;
        }
        return true;
    }

    public void loadOrReleaseContent(int i) {
        ViewGroup viewGroup = this.mContent;
        if (viewGroup != null) {
            if (i == 0) {
                if (viewGroup.getChildCount() == 0) {
                    loadContentViews();
                    this.mBackgroundDimmed.setVisibility(0);
                }
                resetContentText();
            } else if (viewGroup.getChildCount() != 0) {
                this.mContent.removeAllViews();
                this.mBackgroundDimmed.setVisibility(8);
            }
        }
    }

    private void loadContentViews() {
        ((TextView) LayoutInflater.from(getContext()).inflate(C0017R$layout.item_zen_mode, this.mContent, true).findViewById(C0015R$id.zen_quit)).setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                ZenModeView.this.lambda$loadContentViews$1$ZenModeView(view);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$loadContentViews$1 */
    public /* synthetic */ void lambda$loadContentViews$1$ZenModeView(View view) {
        ZenModeViewController zenModeViewController = this.mController;
        if (zenModeViewController != null) {
            zenModeViewController.setZenOff();
        }
    }

    public void resetContentText() {
        int i = Settings.System.getInt(getContext().getContentResolver(), "zen_mode_intercepted_when_unlocked", -1);
        if (i == -1) {
            Log.e("ZenModeView", "resetContentText: unable to get KEY_ZEN_MODE_INTERCEPT_SCENE");
            return;
        }
        TextView textView = (TextView) findViewById(C0015R$id.zen_content);
        if (textView != null) {
            textView.setText(i == 1 ? this.CONTENT_ALL_TIME : this.CONTENT_KEYGUARD);
        }
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
