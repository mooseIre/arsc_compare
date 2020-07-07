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
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.component.HidePipMenuEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PipMenuActivity extends Activity {
    private final List<RemoteAction> mActions = new ArrayList();
    private LinearLayout mActionsGroup;
    private boolean mAllowMenuTimeout = true;
    /* access modifiers changed from: private */
    public boolean mAllowTouches = true;
    /* access modifiers changed from: private */
    public Drawable mBackgroundDrawable;
    private int mBetweenActionPaddingLand;
    private View mDismissButton;
    private PointF mDownDelta = new PointF();
    private PointF mDownPosition = new PointF();
    private ImageView mExpandButton;
    private final Runnable mFinishRunnable = new Runnable() {
        public void run() {
            PipMenuActivity.this.hideMenu();
        }
    };
    private Handler mHandler = new Handler();
    private ValueAnimator.AnimatorUpdateListener mMenuBgUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            PipMenuActivity.this.mBackgroundDrawable.setAlpha((int) (((Float) valueAnimator.getAnimatedValue()).floatValue() * 0.3f * 255.0f));
        }
    };
    private View mMenuContainer;
    private AnimatorSet mMenuContainerAnimator;
    private int mMenuState;
    private Messenger mMessenger = new Messenger(new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = (Bundle) message.obj;
                    PipMenuActivity.this.showMenu(bundle.getInt("menu_state"), (Rect) bundle.getParcelable("stack_bounds"), (Rect) bundle.getParcelable("movement_bounds"), bundle.getBoolean("allow_timeout"));
                    return;
                case 2:
                    PipMenuActivity.this.cancelDelayedFinish();
                    return;
                case 3:
                    PipMenuActivity.this.hideMenu();
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
                    boolean unused = PipMenuActivity.this.mAllowTouches = true;
                    return;
                default:
                    return;
            }
        }
    });
    private Messenger mToControllerMessenger;
    private ViewConfiguration mViewConfig;
    private View mViewRoot;

    static /* synthetic */ boolean lambda$updateActionViews$5(View view, MotionEvent motionEvent) {
        return true;
    }

    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        this.mViewConfig = ViewConfiguration.get(this);
        getWindow().addFlags(537133056);
        super.onCreate(bundle);
        setContentView(R.layout.pip_menu_activity);
        ColorDrawable colorDrawable = new ColorDrawable(-16777216);
        this.mBackgroundDrawable = colorDrawable;
        colorDrawable.setAlpha(0);
        View findViewById = findViewById(R.id.background);
        this.mViewRoot = findViewById;
        findViewById.setBackground(this.mBackgroundDrawable);
        View findViewById2 = findViewById(R.id.menu_container);
        this.mMenuContainer = findViewById2;
        findViewById2.setAlpha(0.0f);
        this.mMenuContainer.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                PipMenuActivity.this.lambda$onCreate$0$PipMenuActivity(view);
            }
        });
        View findViewById3 = findViewById(R.id.dismiss);
        this.mDismissButton = findViewById3;
        findViewById3.setAlpha(0.0f);
        this.mDismissButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                PipMenuActivity.this.lambda$onCreate$1$PipMenuActivity(view);
            }
        });
        this.mActionsGroup = (LinearLayout) findViewById(R.id.actions_group);
        this.mBetweenActionPaddingLand = getResources().getDimensionPixelSize(R.dimen.pip_between_action_padding_land);
        ImageView imageView = (ImageView) findViewById(R.id.expand_button);
        this.mExpandButton = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                PipMenuActivity.this.lambda$onCreate$2$PipMenuActivity(view);
            }
        });
        updateFromIntent(getIntent());
        setTitle(R.string.pip_menu_title);
        setDisablePreviewScreenshots(true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$0 */
    public /* synthetic */ void lambda$onCreate$0$PipMenuActivity(View view) {
        if (this.mMenuState == 1) {
            showPipMenu();
        } else {
            hideMenu();
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
        if (this.mMenuState == 1) {
            showPipMenu();
        } else {
            expandPip();
        }
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

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        cancelDelayedFinish();
        RecentsEventBus.getDefault().unregister(this);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        notifyActivityCallback((Messenger) null);
    }

    public void onPictureInPictureModeChanged(boolean z) {
        if (!z) {
            finish();
        }
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (!this.mAllowTouches) {
            return super.dispatchTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mDownPosition.set(motionEvent.getX(), motionEvent.getY());
            this.mDownDelta.set(0.0f, 0.0f);
        } else if (action == 2) {
            this.mDownDelta.set(motionEvent.getX() - this.mDownPosition.x, motionEvent.getY() - this.mDownPosition.y);
            if (this.mDownDelta.length() > ((float) this.mViewConfig.getScaledTouchSlop()) && this.mMenuState != 0) {
                notifyRegisterInputConsumer();
                cancelDelayedFinish();
            }
        } else if (action == 4) {
            hideMenu();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public void finish() {
        notifyActivityCallback((Messenger) null);
        super.finish();
        overridePendingTransition(0, 0);
    }

    public final void onBusEvent(HidePipMenuEvent hidePipMenuEvent) {
        if (this.mMenuState != 0) {
            hidePipMenuEvent.getAnimationTrigger().increment();
            hideMenu(new Runnable(hidePipMenuEvent) {
                public final /* synthetic */ HidePipMenuEvent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipMenuActivity.this.lambda$onBusEvent$4$PipMenuActivity(this.f$1);
                }
            }, true);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onBusEvent$4 */
    public /* synthetic */ void lambda$onBusEvent$4$PipMenuActivity(HidePipMenuEvent hidePipMenuEvent) {
        this.mHandler.post(new Runnable() {
            public final void run() {
                HidePipMenuEvent.this.getAnimationTrigger().decrement();
            }
        });
    }

    /* access modifiers changed from: private */
    public void showMenu(int i, Rect rect, Rect rect2, boolean z) {
        this.mAllowMenuTimeout = z;
        int i2 = this.mMenuState;
        if (i2 != i) {
            this.mAllowTouches = !(i2 == 2 || i == 2);
            cancelDelayedFinish();
            updateActionViews(rect);
            AnimatorSet animatorSet = this.mMenuContainerAnimator;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            notifyMenuStateChange(i);
            this.mMenuContainerAnimator = new AnimatorSet();
            View view = this.mMenuContainer;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{view.getAlpha(), 1.0f});
            ofFloat.addUpdateListener(this.mMenuBgUpdateListener);
            View view2 = this.mDismissButton;
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, View.ALPHA, new float[]{view2.getAlpha(), 1.0f});
            if (i == 2) {
                this.mMenuContainerAnimator.playTogether(new Animator[]{ofFloat, ofFloat2});
            } else {
                this.mMenuContainerAnimator.play(ofFloat2);
            }
            this.mMenuContainerAnimator.setInterpolator(Interpolators.ALPHA_IN);
            this.mMenuContainerAnimator.setDuration(125);
            if (z) {
                this.mMenuContainerAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        PipMenuActivity.this.repostDelayedFinish(3500);
                    }
                });
            }
            this.mMenuContainerAnimator.start();
            return;
        }
        if (z) {
            repostDelayedFinish(2000);
        }
        notifyUnregisterInputConsumer();
    }

    /* access modifiers changed from: private */
    public void hideMenu() {
        hideMenu((Runnable) null, true);
    }

    private void hideMenu(final Runnable runnable, boolean z) {
        if (this.mMenuState != 0) {
            cancelDelayedFinish();
            if (z) {
                notifyMenuStateChange(0);
            }
            this.mMenuContainerAnimator = new AnimatorSet();
            View view = this.mMenuContainer;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, View.ALPHA, new float[]{view.getAlpha(), 0.0f});
            ofFloat.addUpdateListener(this.mMenuBgUpdateListener);
            View view2 = this.mDismissButton;
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, View.ALPHA, new float[]{view2.getAlpha(), 0.0f});
            this.mMenuContainerAnimator.playTogether(new Animator[]{ofFloat, ofFloat2});
            this.mMenuContainerAnimator.setInterpolator(Interpolators.ALPHA_OUT);
            this.mMenuContainerAnimator.setDuration(125);
            this.mMenuContainerAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    PipMenuActivity.this.finish();
                }
            });
            this.mMenuContainerAnimator.start();
            return;
        }
        finish();
    }

    private void updateFromIntent(Intent intent) {
        this.mToControllerMessenger = (Messenger) intent.getParcelableExtra("messenger");
        notifyActivityCallback(this.mMessenger);
        RecentsEventBus.getDefault().register(this);
        ParceledListSlice parcelableExtra = intent.getParcelableExtra("actions");
        if (parcelableExtra != null) {
            this.mActions.clear();
            this.mActions.addAll(parcelableExtra.getList());
        }
        int intExtra = intent.getIntExtra("menu_state", 0);
        if (intExtra != 0) {
            showMenu(intExtra, (Rect) intent.getParcelableExtra("stack_bounds"), (Rect) intent.getParcelableExtra("movement_bounds"), intent.getBooleanExtra("allow_timeout", true));
        }
    }

    /* access modifiers changed from: private */
    public void setActions(Rect rect, List<RemoteAction> list) {
        this.mActions.clear();
        this.mActions.addAll(list);
        updateActionViews(rect);
    }

    private void updateActionViews(Rect rect) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.expand_container);
        ViewGroup viewGroup2 = (ViewGroup) findViewById(R.id.actions_container);
        viewGroup2.setOnTouchListener($$Lambda$PipMenuActivity$6uC3xpV7xV21Vuu2JnbvTzh8Rok.INSTANCE);
        if (!this.mActions.isEmpty()) {
            boolean z = true;
            if (this.mMenuState != 1) {
                viewGroup2.setVisibility(0);
                if (this.mActionsGroup != null) {
                    LayoutInflater from = LayoutInflater.from(this);
                    while (this.mActionsGroup.getChildCount() < this.mActions.size()) {
                        this.mActionsGroup.addView((ImageView) from.inflate(R.layout.pip_menu_action, this.mActionsGroup, false));
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
                        ImageView imageView = (ImageView) this.mActionsGroup.getChildAt(i2);
                        remoteAction.getIcon().loadDrawableAsync(this, new Icon.OnDrawableLoadedListener(imageView) {
                            public final /* synthetic */ ImageView f$0;

                            {
                                this.f$0 = r1;
                            }

                            public final void onDrawableLoaded(Drawable drawable) {
                                PipMenuActivity.lambda$updateActionViews$6(this.f$0, drawable);
                            }
                        }, this.mHandler);
                        imageView.setContentDescription(remoteAction.getContentDescription());
                        if (remoteAction.isEnabled()) {
                            imageView.setOnClickListener(new View.OnClickListener(remoteAction) {
                                public final /* synthetic */ RemoteAction f$0;

                                {
                                    this.f$0 = r1;
                                }

                                public final void onClick(View view) {
                                    PipMenuActivity.lambda$updateActionViews$7(this.f$0, view);
                                }
                            });
                        }
                        imageView.setEnabled(remoteAction.isEnabled());
                        imageView.setAlpha(remoteAction.isEnabled() ? 1.0f : 0.54f);
                        ((LinearLayout.LayoutParams) imageView.getLayoutParams()).leftMargin = (!z || i2 <= 0) ? 0 : this.mBetweenActionPaddingLand;
                        i2++;
                    }
                }
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.pip_action_padding);
                layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.pip_expand_container_edge_margin);
                viewGroup.requestLayout();
                return;
            }
        }
        viewGroup2.setVisibility(4);
    }

    static /* synthetic */ void lambda$updateActionViews$6(ImageView imageView, Drawable drawable) {
        drawable.setTint(-1);
        imageView.setImageDrawable(drawable);
    }

    static /* synthetic */ void lambda$updateActionViews$7(RemoteAction remoteAction, View view) {
        try {
            remoteAction.getActionIntent().send();
        } catch (PendingIntent.CanceledException e) {
            Log.w("PipMenuActivity", "Failed to send action", e);
        }
    }

    /* access modifiers changed from: private */
    public void updateDismissFraction(float f) {
        int i;
        float f2 = 1.0f - f;
        int i2 = this.mMenuState;
        if (i2 == 2) {
            this.mMenuContainer.setAlpha(f2);
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

    private void notifyRegisterInputConsumer() {
        Message obtain = Message.obtain();
        obtain.what = R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle;
        sendMessage(obtain, "Could not notify controller to register input consumer");
    }

    private void notifyUnregisterInputConsumer() {
        Message obtain = Message.obtain();
        obtain.what = R.styleable.AppCompatTheme_textAppearanceSearchResultTitle;
        sendMessage(obtain, "Could not notify controller to unregister input consumer");
    }

    private void notifyMenuStateChange(int i) {
        this.mMenuState = i;
        Message obtain = Message.obtain();
        obtain.what = 100;
        obtain.arg1 = i;
        sendMessage(obtain, "Could not notify controller of PIP menu visibility");
    }

    private void expandPip() {
        hideMenu(new Runnable() {
            public final void run() {
                PipMenuActivity.this.lambda$expandPip$8$PipMenuActivity();
            }
        }, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$expandPip$8 */
    public /* synthetic */ void lambda$expandPip$8$PipMenuActivity() {
        sendEmptyMessage(R.styleable.AppCompatTheme_textAppearanceListItem, "Could not notify controller to expand PIP");
    }

    private void dismissPip() {
        hideMenu(new Runnable() {
            public final void run() {
                PipMenuActivity.this.lambda$dismissPip$9$PipMenuActivity();
            }
        }, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismissPip$9 */
    public /* synthetic */ void lambda$dismissPip$9$PipMenuActivity() {
        sendEmptyMessage(R.styleable.AppCompatTheme_textAppearanceListItemSmall, "Could not notify controller to dismiss PIP");
    }

    private void showPipMenu() {
        Message obtain = Message.obtain();
        obtain.what = R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu;
        sendMessage(obtain, "Could not notify controller to show PIP menu");
    }

    private void notifyActivityCallback(Messenger messenger) {
        Message obtain = Message.obtain();
        obtain.what = R.styleable.AppCompatTheme_textAppearancePopupMenuHeader;
        obtain.replyTo = messenger;
        sendMessage(obtain, "Could not notify controller of activity finished");
    }

    private void sendEmptyMessage(int i, String str) {
        Message obtain = Message.obtain();
        obtain.what = i;
        sendMessage(obtain, str);
    }

    private void sendMessage(Message message, String str) {
        try {
            this.mToControllerMessenger.send(message);
        } catch (RemoteException e) {
            Log.e("PipMenuActivity", str, e);
        }
    }

    /* access modifiers changed from: private */
    public void cancelDelayedFinish() {
        this.mHandler.removeCallbacks(this.mFinishRunnable);
    }

    /* access modifiers changed from: private */
    public void repostDelayedFinish(long j) {
        this.mHandler.removeCallbacks(this.mFinishRunnable);
        this.mHandler.postDelayed(this.mFinishRunnable, j);
    }
}
