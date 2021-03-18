package com.android.systemui.pip.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.constraintlayout.widget.R$styleable;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipMenuActivity extends Activity {
    private AccessibilityManager mAccessibilityManager;
    private final List<RemoteAction> mActions = new ArrayList();
    private LinearLayout mActionsGroup;
    private boolean mAllowMenuTimeout = true;
    private boolean mAllowTouches = true;
    private Drawable mBackgroundDrawable;
    private int mBetweenActionPaddingLand;
    private View mDismissButton;
    private final Runnable mFinishRunnable = new Runnable() {
        /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$on3_yLRCya18yWlE7CavD_fHImk */

        public final void run() {
            PipMenuActivity.this.hideMenu();
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.systemui.pip.phone.PipMenuActivity.AnonymousClass2 */

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = (Bundle) message.obj;
                    PipMenuActivity.this.showMenu(bundle.getInt("menu_state"), (Rect) bundle.getParcelable("stack_bounds"), bundle.getBoolean("allow_timeout"), bundle.getBoolean("resize_menu_on_show"), bundle.getBoolean("show_menu_with_delay"), bundle.getBoolean("show_resize_handle"));
                    return;
                case 2:
                    PipMenuActivity.this.cancelDelayedFinish();
                    return;
                case 3:
                    PipMenuActivity.this.hideMenu((Runnable) message.obj);
                    return;
                case 4:
                    Bundle bundle2 = (Bundle) message.obj;
                    ParceledListSlice parcelable = bundle2.getParcelable("actions");
                    PipMenuActivity.this.setActions((Rect) bundle2.getParcelable("stack_bounds"), parcelable != null ? parcelable.getList() : Collections.EMPTY_LIST);
                    return;
                case 5:
                    PipMenuActivity.this.updateDismissFraction(((Bundle) message.obj).getFloat("dismiss_fraction"));
                    return;
                case 6:
                    PipMenuActivity.this.mAllowTouches = true;
                    return;
                case 7:
                    PipMenuActivity.this.dispatchPointerEvent((MotionEvent) message.obj);
                    return;
                case 8:
                    PipMenuActivity.this.mMenuContainerAnimator.setStartDelay(30);
                    PipMenuActivity.this.mMenuContainerAnimator.start();
                    return;
                case 9:
                    PipMenuActivity.this.fadeOutMenu();
                    return;
                default:
                    return;
            }
        }
    };
    private ValueAnimator.AnimatorUpdateListener mMenuBgUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        /* class com.android.systemui.pip.phone.PipMenuActivity.AnonymousClass1 */

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            PipMenuActivity.this.mBackgroundDrawable.setAlpha((int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * 0.3f * 255.0f));
        }
    };
    private View mMenuContainer;
    private AnimatorSet mMenuContainerAnimator;
    private int mMenuState;
    private Messenger mMessenger = new Messenger(this.mHandler);
    private boolean mResize = true;
    private View mResizeHandle;
    private View mSettingsButton;
    private Messenger mToControllerMessenger;
    private View mViewRoot;

    static /* synthetic */ boolean lambda$updateActionViews$3(View view, MotionEvent motionEvent) {
        return true;
    }

    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        getWindow().addFlags(262144);
        super.onCreate(bundle);
        setContentView(C0017R$layout.pip_menu_activity);
        this.mAccessibilityManager = (AccessibilityManager) getSystemService(AccessibilityManager.class);
        ColorDrawable colorDrawable = new ColorDrawable(-16777216);
        this.mBackgroundDrawable = colorDrawable;
        colorDrawable.setAlpha(0);
        View findViewById = findViewById(C0015R$id.background);
        this.mViewRoot = findViewById;
        findViewById.setBackground(this.mBackgroundDrawable);
        View findViewById2 = findViewById(C0015R$id.menu_container);
        this.mMenuContainer = findViewById2;
        findViewById2.setAlpha(0.0f);
        View findViewById3 = findViewById(C0015R$id.settings);
        this.mSettingsButton = findViewById3;
        findViewById3.setAlpha(0.0f);
        this.mSettingsButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$4MVIZwVdJN3lkWpqrFrI53Q9bPQ */

            public final void onClick(View view) {
                PipMenuActivity.this.lambda$onCreate$0$PipMenuActivity(view);
            }
        });
        View findViewById4 = findViewById(C0015R$id.dismiss);
        this.mDismissButton = findViewById4;
        findViewById4.setAlpha(0.0f);
        this.mDismissButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$lkNLpysIkUfrlXCWX9bvozrYe1U */

            public final void onClick(View view) {
                PipMenuActivity.this.lambda$onCreate$1$PipMenuActivity(view);
            }
        });
        findViewById(C0015R$id.expand_button).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$70yHDyzrwE1GNEVEQrmSEL7H6fY */

            public final void onClick(View view) {
                PipMenuActivity.this.lambda$onCreate$2$PipMenuActivity(view);
            }
        });
        View findViewById5 = findViewById(C0015R$id.resize_handle);
        this.mResizeHandle = findViewById5;
        findViewById5.setAlpha(0.0f);
        this.mActionsGroup = (LinearLayout) findViewById(C0015R$id.actions_group);
        this.mBetweenActionPaddingLand = getResources().getDimensionPixelSize(C0012R$dimen.pip_between_action_padding_land);
        updateFromIntent(getIntent());
        setTitle(C0021R$string.pip_menu_title);
        setDisablePreviewScreenshots(true);
        getWindow().setExitTransition(null);
        initAccessibility();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$0 */
    public /* synthetic */ void lambda$onCreate$0$PipMenuActivity(View view) {
        if (view.getAlpha() != 0.0f) {
            showSettings();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$1 */
    public /* synthetic */ void lambda$onCreate$1$PipMenuActivity(View view) {
        dismissPip();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$2 */
    public /* synthetic */ void lambda$onCreate$2$PipMenuActivity(View view) {
        if (this.mMenuContainer.getAlpha() != 0.0f) {
            expandPip();
        }
    }

    private void initAccessibility() {
        getWindow().getDecorView().setAccessibilityDelegate(new View.AccessibilityDelegate() {
            /* class com.android.systemui.pip.phone.PipMenuActivity.AnonymousClass3 */

            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, PipMenuActivity.this.getResources().getString(C0021R$string.pip_menu_title)));
            }

            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                if (i == 16 && PipMenuActivity.this.mMenuState == 1) {
                    Message obtain = Message.obtain();
                    obtain.what = R$styleable.Constraint_progress;
                    PipMenuActivity.this.sendMessage(obtain, "Could not notify controller to show PIP menu");
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        });
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i != 111) {
            return super.onKeyUp(i, keyEvent);
        }
        hideMenu();
        return true;
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateFromIntent(intent);
    }

    public void onUserInteraction() {
        if (this.mAllowMenuTimeout) {
            repostDelayedFinish(2000);
        }
    }

    /* access modifiers changed from: protected */
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        hideMenu();
    }

    public void onTopResumedActivityChanged(boolean z) {
        super.onTopResumedActivityChanged(z);
        if (!z && this.mMenuState != 0) {
            hideMenu();
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        hideMenu();
        cancelDelayedFinish();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        notifyActivityCallback(null);
    }

    public void onPictureInPictureModeChanged(boolean z) {
        if (!z) {
            finish();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchPointerEvent(MotionEvent motionEvent) {
        if (motionEvent.isTouchEvent()) {
            dispatchTouchEvent(motionEvent);
        } else {
            dispatchGenericMotionEvent(motionEvent);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!this.mAllowTouches) {
            return false;
        }
        if (motionEvent.getAction() != 4) {
            return super.dispatchTouchEvent(motionEvent);
        }
        hideMenu();
        return true;
    }

    public void finish() {
        notifyActivityCallback(null);
        super.finish();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showMenu(int i, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) {
        this.mAllowMenuTimeout = z;
        int i2 = this.mMenuState;
        if (i2 != i) {
            this.mAllowTouches = !(z2 && (i2 == 2 || i == 2));
            cancelDelayedFinish();
            updateActionViews(rect);
            AnimatorSet animatorSet = this.mMenuContainerAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            this.mMenuContainerAnimator = new AnimatorSet();
            View view = this.mMenuContainer;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), 1.0f);
            ofFloat.addUpdateListener(this.mMenuBgUpdateListener);
            View view2 = this.mSettingsButton;
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, View.ALPHA, view2.getAlpha(), 1.0f);
            View view3 = this.mDismissButton;
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(view3, View.ALPHA, view3.getAlpha(), 1.0f);
            View view4 = this.mResizeHandle;
            ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(view4, View.ALPHA, view4.getAlpha(), 0.0f);
            if (i == 2) {
                this.mMenuContainerAnimator.playTogether(ofFloat, ofFloat2, ofFloat3, ofFloat4);
            } else {
                this.mMenuContainerAnimator.playTogether(ofFloat3, ofFloat4);
            }
            this.mMenuContainerAnimator.setInterpolator(Interpolators.ALPHA_IN);
            this.mMenuContainerAnimator.setDuration(i == 1 ? 125 : 175);
            if (z) {
                this.mMenuContainerAnimator.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.pip.phone.PipMenuActivity.AnonymousClass4 */

                    public void onAnimationEnd(Animator animator) {
                        PipMenuActivity.this.repostDelayedFinish(3500);
                    }
                });
            }
            if (z3) {
                notifyMenuStateChange(i, z2, 8);
                return;
            }
            notifyMenuStateChange(i, z2, -1);
            this.mMenuContainerAnimator.start();
        } else if (z) {
            repostDelayedFinish(2000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fadeOutMenu() {
        this.mMenuContainer.setAlpha(0.0f);
        this.mSettingsButton.setAlpha(0.0f);
        this.mDismissButton.setAlpha(0.0f);
        this.mResizeHandle.setAlpha(0.0f);
    }

    /* access modifiers changed from: private */
    public void hideMenu() {
        hideMenu(null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hideMenu(Runnable runnable) {
        hideMenu(runnable, true, false, true);
    }

    private void hideMenu(final Runnable runnable, boolean z, final boolean z2, boolean z3) {
        if (this.mMenuState != 0) {
            cancelDelayedFinish();
            if (z) {
                notifyMenuStateChange(0, this.mResize, -1);
            }
            this.mMenuContainerAnimator = new AnimatorSet();
            View view = this.mMenuContainer;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, view.getAlpha(), 0.0f);
            ofFloat.addUpdateListener(this.mMenuBgUpdateListener);
            View view2 = this.mSettingsButton;
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, View.ALPHA, view2.getAlpha(), 0.0f);
            View view3 = this.mDismissButton;
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(view3, View.ALPHA, view3.getAlpha(), 0.0f);
            View view4 = this.mResizeHandle;
            this.mMenuContainerAnimator.playTogether(ofFloat, ofFloat2, ofFloat3, ObjectAnimator.ofFloat(view4, View.ALPHA, view4.getAlpha(), 0.0f));
            this.mMenuContainerAnimator.setInterpolator(Interpolators.ALPHA_OUT);
            this.mMenuContainerAnimator.setDuration(z3 ? 125 : 0);
            this.mMenuContainerAnimator.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.pip.phone.PipMenuActivity.AnonymousClass5 */

                public void onAnimationEnd(Animator animator) {
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    if (!z2) {
                        PipMenuActivity.this.finish();
                    }
                }
            });
            this.mMenuContainerAnimator.start();
            return;
        }
        finish();
    }

    private void updateFromIntent(Intent intent) {
        Messenger messenger = (Messenger) intent.getParcelableExtra("messenger");
        this.mToControllerMessenger = messenger;
        if (messenger == null) {
            Log.w("PipMenuActivity", "Controller messenger is null. Stopping.");
            finish();
            return;
        }
        notifyActivityCallback(this.mMessenger);
        ParceledListSlice parcelableExtra = intent.getParcelableExtra("actions");
        if (parcelableExtra != null) {
            this.mActions.clear();
            this.mActions.addAll(parcelableExtra.getList());
        }
        int intExtra = intent.getIntExtra("menu_state", 0);
        if (intExtra != 0) {
            showMenu(intExtra, (Rect) intent.getParcelableExtra("stack_bounds"), intent.getBooleanExtra("allow_timeout", true), intent.getBooleanExtra("resize_menu_on_show", false), intent.getBooleanExtra("show_menu_with_delay", false), intent.getBooleanExtra("show_resize_handle", false));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setActions(Rect rect, List<RemoteAction> list) {
        this.mActions.clear();
        this.mActions.addAll(list);
        updateActionViews(rect);
    }

    private void updateActionViews(Rect rect) {
        ViewGroup viewGroup = (ViewGroup) findViewById(C0015R$id.expand_container);
        ViewGroup viewGroup2 = (ViewGroup) findViewById(C0015R$id.actions_container);
        viewGroup2.setOnTouchListener($$Lambda$PipMenuActivity$BXxmOnLUs8BTsc_oWau4TVb1pE.INSTANCE);
        if (!this.mActions.isEmpty()) {
            boolean z = true;
            if (this.mMenuState != 1) {
                viewGroup2.setVisibility(0);
                if (this.mActionsGroup != null) {
                    LayoutInflater from = LayoutInflater.from(this);
                    while (this.mActionsGroup.getChildCount() < this.mActions.size()) {
                        this.mActionsGroup.addView((ImageButton) from.inflate(C0017R$layout.pip_menu_action, (ViewGroup) this.mActionsGroup, false));
                    }
                    int i = 0;
                    while (i < this.mActionsGroup.getChildCount()) {
                        this.mActionsGroup.getChildAt(i).setVisibility(i < this.mActions.size() ? 0 : 8);
                        i++;
                    }
                    if (rect == null || rect.width() <= rect.height()) {
                        z = false;
                    }
                    int i2 = 0;
                    while (i2 < this.mActions.size()) {
                        RemoteAction remoteAction = this.mActions.get(i2);
                        ImageButton imageButton = (ImageButton) this.mActionsGroup.getChildAt(i2);
                        remoteAction.getIcon().loadDrawableAsync(this, new Icon.OnDrawableLoadedListener(imageButton) {
                            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$FgVNA6rqcnXmAeLQlbztL7Zw7mU */
                            public final /* synthetic */ ImageButton f$0;

                            {
                                this.f$0 = r1;
                            }

                            public final void onDrawableLoaded(Drawable drawable) {
                                PipMenuActivity.lambda$updateActionViews$4(this.f$0, drawable);
                            }
                        }, this.mHandler);
                        imageButton.setContentDescription(remoteAction.getContentDescription());
                        if (remoteAction.isEnabled()) {
                            imageButton.setOnClickListener(new View.OnClickListener(remoteAction) {
                                /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$Ts5um0YR6IQ0YRdLS2dyHj4GSpg */
                                public final /* synthetic */ RemoteAction f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void onClick(View view) {
                                    PipMenuActivity.this.lambda$updateActionViews$6$PipMenuActivity(this.f$1, view);
                                }
                            });
                        }
                        imageButton.setEnabled(remoteAction.isEnabled());
                        imageButton.setAlpha(remoteAction.isEnabled() ? 1.0f : 0.54f);
                        ((LinearLayout.LayoutParams) imageButton.getLayoutParams()).leftMargin = (!z || i2 <= 0) ? 0 : this.mBetweenActionPaddingLand;
                        i2++;
                    }
                }
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
                layoutParams.topMargin = getResources().getDimensionPixelSize(C0012R$dimen.pip_action_padding);
                layoutParams.bottomMargin = getResources().getDimensionPixelSize(C0012R$dimen.pip_expand_container_edge_margin);
                viewGroup.requestLayout();
                return;
            }
        }
        viewGroup2.setVisibility(4);
    }

    static /* synthetic */ void lambda$updateActionViews$4(ImageButton imageButton, Drawable drawable) {
        drawable.setTint(-1);
        imageButton.setImageDrawable(drawable);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateActionViews$6 */
    public /* synthetic */ void lambda$updateActionViews$6$PipMenuActivity(RemoteAction remoteAction, View view) {
        this.mHandler.post(new Runnable(remoteAction) {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$KxDwr2Rt3pvREKtFVSgFixejo */
            public final /* synthetic */ RemoteAction f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                PipMenuActivity.lambda$updateActionViews$5(this.f$0);
            }
        });
    }

    static /* synthetic */ void lambda$updateActionViews$5(RemoteAction remoteAction) {
        try {
            remoteAction.getActionIntent().send();
        } catch (PendingIntent.CanceledException e) {
            Log.w("PipMenuActivity", "Failed to send action", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDismissFraction(float f) {
        int i;
        float f2 = 1.0f - f;
        int i2 = this.mMenuState;
        if (i2 == 2) {
            this.mMenuContainer.setAlpha(f2);
            this.mSettingsButton.setAlpha(f2);
            this.mDismissButton.setAlpha(f2);
            i = (int) (((f2 * 0.3f) + (f * 0.6f)) * 255.0f);
        } else {
            if (i2 == 1) {
                this.mDismissButton.setAlpha(f2);
            }
            i = (int) (f * 0.6f * 255.0f);
        }
        this.mBackgroundDrawable.setAlpha(i);
    }

    private void notifyMenuStateChange(int i, boolean z, int i2) {
        this.mMenuState = i;
        this.mResize = z;
        Message obtain = Message.obtain();
        obtain.what = 100;
        obtain.arg1 = i;
        obtain.arg2 = z ? 1 : 0;
        if (i2 != -1) {
            obtain.replyTo = this.mMessenger;
            Bundle bundle = new Bundle(1);
            bundle.putInt("message_callback_what", i2);
            obtain.obj = bundle;
        }
        sendMessage(obtain, "Could not notify controller of PIP menu visibility");
    }

    private void expandPip() {
        hideMenu(new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$gxeJOYpgn30UbyKen9nD4GpRdFQ */

            public final void run() {
                PipMenuActivity.this.lambda$expandPip$7$PipMenuActivity();
            }
        }, false, false, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$expandPip$7 */
    public /* synthetic */ void lambda$expandPip$7$PipMenuActivity() {
        sendEmptyMessage(R$styleable.Constraint_layout_goneMarginRight, "Could not notify controller to expand PIP");
    }

    private void dismissPip() {
        hideMenu(new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivity$guHLrBiStjvmB9r01MbFqRGaK3c */

            public final void run() {
                PipMenuActivity.this.lambda$dismissPip$8$PipMenuActivity();
            }
        }, false, true, this.mMenuState != 1);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismissPip$8 */
    public /* synthetic */ void lambda$dismissPip$8$PipMenuActivity() {
        sendEmptyMessage(R$styleable.Constraint_layout_goneMarginTop, "Could not notify controller to dismiss PIP");
    }

    private void showSettings() {
        Pair<ComponentName, Integer> topPipActivity = PipUtils.getTopPipActivity(this, ActivityManager.getService());
        if (topPipActivity.first != null) {
            UserHandle of = UserHandle.of(((Integer) topPipActivity.second).intValue());
            Intent intent = new Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.fromParts("package", ((ComponentName) topPipActivity.first).getPackageName(), null));
            intent.putExtra("android.intent.extra.user_handle", of);
            intent.setFlags(268468224);
            startActivity(intent);
        }
    }

    private void notifyActivityCallback(Messenger messenger) {
        Message obtain = Message.obtain();
        obtain.what = R$styleable.Constraint_motionStagger;
        obtain.replyTo = messenger;
        obtain.arg1 = this.mResize ? 1 : 0;
        sendMessage(obtain, "Could not notify controller of activity finished");
    }

    private void sendEmptyMessage(int i, String str) {
        Message obtain = Message.obtain();
        obtain.what = i;
        sendMessage(obtain, str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessage(Message message, String str) {
        Messenger messenger = this.mToControllerMessenger;
        if (messenger != null) {
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                Log.e("PipMenuActivity", str, e);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelDelayedFinish() {
        this.mHandler.removeCallbacks(this.mFinishRunnable);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void repostDelayedFinish(int i) {
        int recommendedTimeoutMillis = this.mAccessibilityManager.getRecommendedTimeoutMillis(i, 5);
        this.mHandler.removeCallbacks(this.mFinishRunnable);
        this.mHandler.postDelayed(this.mFinishRunnable, (long) recommendedTimeoutMillis);
    }
}
