package com.android.systemui.statusbar.notification.zen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
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
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.RowAnimationUtils;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import com.android.systemui.statusbar.notification.stack.SwipeableView;
import com.miui.systemui.DebugConfig;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.property.ViewProperty;

public class ZenModeView extends ActivatableNotificationView implements SwipeableView {
    private static final ViewProperty TRANSITION_ALPHA = new ViewProperty("TransitionAlpha") {
        /* class com.android.systemui.statusbar.notification.zen.ZenModeView.AnonymousClass3 */

        public void setValue(View view, float f) {
            view.setTransitionAlpha(f);
        }

        public float getValue(View view) {
            return view.getTransitionAlpha();
        }
    };
    private final String CONTENT_ALL_TIME = getResources().getString(C0021R$string.zen_mode_warnings_all_time_content);
    private final String CONTENT_KEYGUARD = getResources().getString(C0021R$string.zen_mode_warnings_keyguard_content);
    private NotificationBackgroundView mBackgroundDimmed;
    private NotificationBackgroundView mBackgroundNormal;
    private ViewGroup mContent;
    public ZenModeViewController mController;
    private IStateStyle mFolme;
    private AnimConfig mHiddenConfig;
    private AnimState mHiddenState;
    private AnimConfig mShownConfig;
    private AnimState mShownState;
    public volatile int mVisibility = getVisibility();

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    public boolean getCanSwipe() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public boolean hasFinishedInitialization() {
        return true;
    }

    public ZenModeView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public long performRemoveAnimation(long j, long j2, float f, boolean z, float f2, Runnable runnable, AnimatorListenerAdapter animatorListenerAdapter) {
        IStateStyle iStateStyle = this.mFolme;
        if (iStateStyle == null) {
            doAfterAnim(this.mVisibility);
            return 0;
        }
        iStateStyle.cancel();
        this.mFolme.to(this.mHiddenState, this.mHiddenConfig);
        return 0;
    }

    public void doAfterAnim(int i) {
        setVisibility(i);
        loadOrReleaseContent(i);
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void performAddAnimation(long j, long j2, boolean z) {
        IStateStyle iStateStyle = this.mFolme;
        if (iStateStyle != null) {
            iStateStyle.cancel();
            this.mFolme.to(this.mShownState, this.mShownConfig);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mContent = (ViewGroup) findViewById(C0015R$id.content);
        setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.zen.$$Lambda$ZenModeView$_6oqLxuLrd6dpOtYMGRgCl0UadQ */

            public final void onClick(View view) {
                ZenModeView.this.lambda$onFinishInflate$0$ZenModeView(view);
            }
        });
        this.mBackgroundNormal = (NotificationBackgroundView) findViewById(C0015R$id.backgroundNormal);
        this.mBackgroundDimmed = (NotificationBackgroundView) findViewById(C0015R$id.backgroundDimmed);
        updateBackgroundBg();
        initFolme();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$ZenModeView(View view) {
        ZenModeViewController zenModeViewController = this.mController;
        if (zenModeViewController != null) {
            zenModeViewController.jump2Settings();
        }
    }

    private void initFolme() {
        this.mFolme = Folme.useAt(this).state();
        AnimState animState = new AnimState("zen_mode_shown");
        animState.add(TRANSITION_ALPHA, 1.0d);
        animState.add(ViewProperty.SCALE_Y, 1.0d);
        animState.add(ViewProperty.SCALE_X, 1.0d);
        this.mShownState = animState;
        AnimState animState2 = new AnimState("zen_mode_hidden");
        animState2.add(TRANSITION_ALPHA, 0.10000000149011612d);
        animState2.add(ViewProperty.SCALE_Y, 0.8999999761581421d);
        animState2.add(ViewProperty.SCALE_X, 0.8999999761581421d);
        this.mHiddenState = animState2;
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-1, 250.0f);
        this.mShownConfig = animConfig;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(-1, 250.0f);
        animConfig2.addListeners(new TransitionListener() {
            /* class com.android.systemui.statusbar.notification.zen.ZenModeView.AnonymousClass1 */

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                ZenModeView zenModeView = ZenModeView.this;
                zenModeView.doAfterAnim(zenModeView.mVisibility);
            }
        });
        this.mHiddenConfig = animConfig2;
    }

    private void updateBackgroundBg() {
        this.mBackgroundNormal.setCustomBackground(C0013R$drawable.notification_item_bg);
        this.mBackgroundDimmed.setCustomBackground(C0013R$drawable.notification_material_bg_dim);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public View getContentView() {
        return this.mContent;
    }

    public void setController(ZenModeViewController zenModeViewController) {
        this.mController = zenModeViewController;
    }

    public void loadOrReleaseContent(int i) {
        ViewGroup viewGroup = this.mContent;
        if (viewGroup != null) {
            if (i == 0) {
                if (viewGroup.getChildCount() == 0) {
                    loadContentViews();
                }
                resetContentText();
            } else if (viewGroup.getChildCount() != 0) {
                this.mContent.removeAllViews();
            }
        }
    }

    private void loadContentViews() {
        ((TextView) LayoutInflater.from(getContext()).inflate(C0017R$layout.item_zen_mode, this.mContent, true).findViewById(C0015R$id.zen_quit)).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.zen.$$Lambda$ZenModeView$IX1IT9wvL5wir1_ziDkIeeeOlqA */

            public final void onClick(View view) {
                ZenModeView.this.lambda$loadContentViews$1$ZenModeView(view);
            }
        });
        updateBackgroundBg();
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

    public void reInflate() {
        ViewGroup viewGroup = this.mContent;
        if (viewGroup != null && viewGroup.getChildCount() != 0) {
            this.mContent.removeAllViews();
            loadContentViews();
            resetContentText();
        }
    }

    public void setVisibility(int i) {
        this.mVisibility = i;
        super.setVisibility(i);
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public void resetTranslation() {
        setTranslation(0.0f);
    }

    public void resetScaleAndAlpha() {
        setScaleX(1.0f);
        setScaleY(1.0f);
        setTransitionAlpha(1.0f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
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

    public boolean onTouchEvent(MotionEvent motionEvent) {
        startTouchAnimateIfNeed(motionEvent);
        if (needInterceptTouch()) {
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    private boolean needInterceptTouch() {
        return isDimmed() && !isActive();
    }

    private void startTouchAnimateIfNeed(MotionEvent motionEvent) {
        if (motionEvent != null) {
            if (!isChildInGroup() || isGroupExpanded()) {
                if (!isClickable()) {
                    setClickable(true);
                }
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked != 0) {
                    if (actionMasked == 1 || actionMasked == 3) {
                        postDelayed(new Runnable() {
                            /* class com.android.systemui.statusbar.notification.zen.$$Lambda$ZenModeView$p9RHGsSRRqIXQRTvUeXq8hpCVtA */

                            public final void run() {
                                ZenModeView.this.lambda$startTouchAnimateIfNeed$2$ZenModeView();
                            }
                        }, isPinned() ? 120 : 0);
                    }
                } else if (0.95f != getViewState().scaleX && !getViewState().getTouchAnimating()) {
                    startTouchScaleAnimateIfNeed(0.95f);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startTouchAnimateIfNeed$2 */
    public /* synthetic */ void lambda$startTouchAnimateIfNeed$2$ZenModeView() {
        startTouchScaleAnimateIfNeed(1.0f);
    }

    private void startTouchScaleAnimateIfNeed(float f) {
        if (DebugConfig.DEBUG) {
            Log.d("ZenModeView", "animateTouchScale scale=$scale, changing=$isGroupExpansionChanging");
        }
        RowAnimationUtils.startTouchAnimationIfNeed(this, f);
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void startActivateAnimation(boolean z) {
        if (isAttachedToWindow() && isDimmable()) {
            float f = z ? 1.0f : 1.05f;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "scaleX", f);
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this, "scaleY", f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(ofFloat).with(ofFloat2);
            animatorSet.setInterpolator(!z ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT);
            animatorSet.setDuration(220L);
            if (z) {
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.statusbar.notification.zen.ZenModeView.AnonymousClass2 */

                    public void onAnimationEnd(Animator animator) {
                        ZenModeView.this.updateBackground();
                        ZenModeView.this.setTouchAnimatingState(false);
                    }
                });
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

    private void resetActivateAnimationIfNeed() {
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
    /* access modifiers changed from: public */
    private void setTouchAnimatingState(boolean z) {
        getViewState().setTouchAnimating(z);
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setTranslation(float f) {
        setTranslationX(f);
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView, com.android.systemui.statusbar.notification.row.ExpandableView
    public float getTranslation() {
        return getTranslationX();
    }
}
