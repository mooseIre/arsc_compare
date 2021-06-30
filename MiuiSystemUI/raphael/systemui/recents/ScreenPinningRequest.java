package com.android.systemui.recents;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Binder;
import android.os.RemoteException;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.leak.RotationUtils;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Optional;

public class ScreenPinningRequest implements View.OnClickListener, NavigationModeController.ModeChangedListener {
    private final AccessibilityManager mAccessibilityService;
    private final Context mContext;
    private int mNavBarMode;
    private RequestWindowView mRequestWindow;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    private final WindowManager mWindowManager = ((WindowManager) this.mContext.getSystemService("window"));
    private int taskId;

    public ScreenPinningRequest(Context context, Optional<Lazy<StatusBar>> optional) {
        this.mContext = context;
        this.mStatusBarOptionalLazy = optional;
        this.mAccessibilityService = (AccessibilityManager) context.getSystemService("accessibility");
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
    }

    public void clearPrompt() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            this.mWindowManager.removeView(requestWindowView);
            this.mRequestWindow = null;
        }
    }

    public void showPrompt(int i, boolean z) {
        try {
            clearPrompt();
        } catch (IllegalArgumentException unused) {
        }
        this.taskId = i;
        RequestWindowView requestWindowView = new RequestWindowView(this.mContext, z);
        this.mRequestWindow = requestWindowView;
        requestWindowView.setSystemUiVisibility(256);
        this.mWindowManager.addView(this.mRequestWindow, getWindowLayoutParams());
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public void onConfigurationChanged() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            requestWindowView.onConfigurationChanged();
        }
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2024, 264, -3);
        layoutParams.token = new Binder();
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("ScreenPinningConfirmation");
        layoutParams.gravity = R$styleable.AppCompatTheme_windowActionModeOverlay;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    public void onClick(View view) {
        if (view.getId() == C0015R$id.screen_pinning_ok_button || this.mRequestWindow == view) {
            try {
                ActivityTaskManager.getService().startSystemLockTaskMode(this.taskId);
            } catch (RemoteException unused) {
            }
        }
        clearPrompt();
    }

    public FrameLayout.LayoutParams getRequestLayoutParams(int i) {
        return new FrameLayout.LayoutParams(-2, -2, i == 2 ? 19 : i == 1 ? 21 : 81);
    }

    /* access modifiers changed from: private */
    public class RequestWindowView extends FrameLayout {
        private final BroadcastDispatcher mBroadcastDispatcher = ((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));
        private final ColorDrawable mColor = new ColorDrawable(0);
        private ValueAnimator mColorAnim;
        private ViewGroup mLayout;
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
                    RequestWindowView requestWindowView = RequestWindowView.this;
                    requestWindowView.post(requestWindowView.mUpdateLayoutRunnable);
                } else if (intent.getAction().equals("android.intent.action.USER_SWITCHED") || intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    ScreenPinningRequest.this.clearPrompt();
                }
            }
        };
        private boolean mShowCancel;
        private final Runnable mUpdateLayoutRunnable = new Runnable() {
            /* class com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.AnonymousClass2 */

            public void run() {
                if (RequestWindowView.this.mLayout != null && RequestWindowView.this.mLayout.getParent() != null) {
                    ViewGroup viewGroup = RequestWindowView.this.mLayout;
                    RequestWindowView requestWindowView = RequestWindowView.this;
                    viewGroup.setLayoutParams(ScreenPinningRequest.this.getRequestLayoutParams(RotationUtils.getRotation(((FrameLayout) requestWindowView).mContext)));
                }
            }
        };

        public RequestWindowView(Context context, boolean z) {
            super(context);
            setClickable(true);
            setOnClickListener(ScreenPinningRequest.this);
            setBackground(this.mColor);
            this.mShowCancel = z;
        }

        public void onAttachedToWindow() {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ScreenPinningRequest.this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            float f = displayMetrics.density;
            int rotation = RotationUtils.getRotation(((FrameLayout) this).mContext);
            inflateView(rotation);
            int color = ((FrameLayout) this).mContext.getColor(C0011R$color.screen_pinning_request_window_bg);
            if (ActivityManager.isHighEndGfx()) {
                this.mLayout.setAlpha(0.0f);
                if (rotation == 2) {
                    this.mLayout.setTranslationX(f * -96.0f);
                } else if (rotation == 1) {
                    this.mLayout.setTranslationX(f * 96.0f);
                } else {
                    this.mLayout.setTranslationY(f * 96.0f);
                }
                this.mLayout.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
                ValueAnimator ofObject = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.valueOf(color));
                this.mColorAnim = ofObject;
                ofObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.AnonymousClass1 */

                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        RequestWindowView.this.mColor.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                this.mColorAnim.setDuration(1000L);
                this.mColorAnim.start();
            } else {
                this.mColor.setColor(color);
            }
            IntentFilter intentFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter);
        }

        private void inflateView(int i) {
            int i2;
            int i3;
            Context context = getContext();
            boolean z = true;
            if (i == 2) {
                i2 = C0017R$layout.screen_pinning_request_sea_phone;
            } else if (i == 1) {
                i2 = C0017R$layout.screen_pinning_request_land_phone;
            } else {
                i2 = C0017R$layout.screen_pinning_request;
            }
            ViewGroup viewGroup = (ViewGroup) View.inflate(context, i2, null);
            this.mLayout = viewGroup;
            viewGroup.setClickable(true);
            int i4 = 0;
            this.mLayout.setLayoutDirection(0);
            this.mLayout.findViewById(C0015R$id.screen_pinning_text_area).setLayoutDirection(3);
            View findViewById = this.mLayout.findViewById(C0015R$id.screen_pinning_buttons);
            WindowManagerWrapper instance = WindowManagerWrapper.getInstance();
            if (QuickStepContract.isGesturalMode(ScreenPinningRequest.this.mNavBarMode) || !instance.hasSoftNavigationBar(((FrameLayout) this).mContext.getDisplayId())) {
                findViewById.setVisibility(8);
            } else {
                findViewById.setLayoutDirection(3);
                swapChildrenIfRtlAndVertical(findViewById);
            }
            ((Button) this.mLayout.findViewById(C0015R$id.screen_pinning_ok_button)).setOnClickListener(ScreenPinningRequest.this);
            if (this.mShowCancel) {
                ((Button) this.mLayout.findViewById(C0015R$id.screen_pinning_cancel_button)).setOnClickListener(ScreenPinningRequest.this);
            } else {
                ((Button) this.mLayout.findViewById(C0015R$id.screen_pinning_cancel_button)).setVisibility(4);
            }
            NavigationBarView navigationBarView = (NavigationBarView) ScreenPinningRequest.this.mStatusBarOptionalLazy.map($$Lambda$ScreenPinningRequest$RequestWindowView$iq7_kF2IL9FTwkRZM6zjXuxpxgs.INSTANCE).orElse(null);
            if (navigationBarView == null || !navigationBarView.isRecentsButtonVisible()) {
                z = false;
            }
            boolean isTouchExplorationEnabled = ScreenPinningRequest.this.mAccessibilityService.isTouchExplorationEnabled();
            if (QuickStepContract.isGesturalMode(ScreenPinningRequest.this.mNavBarMode)) {
                i3 = C0021R$string.screen_pinning_description_gestural;
            } else if (z) {
                this.mLayout.findViewById(C0015R$id.screen_pinning_recents_group).setVisibility(0);
                this.mLayout.findViewById(C0015R$id.screen_pinning_home_bg_light).setVisibility(4);
                this.mLayout.findViewById(C0015R$id.screen_pinning_home_bg).setVisibility(4);
                if (isTouchExplorationEnabled) {
                    i3 = C0021R$string.screen_pinning_description_accessible;
                } else {
                    i3 = C0021R$string.screen_pinning_description;
                }
            } else {
                this.mLayout.findViewById(C0015R$id.screen_pinning_recents_group).setVisibility(4);
                this.mLayout.findViewById(C0015R$id.screen_pinning_home_bg_light).setVisibility(0);
                this.mLayout.findViewById(C0015R$id.screen_pinning_home_bg).setVisibility(0);
                if (isTouchExplorationEnabled) {
                    i3 = C0021R$string.screen_pinning_description_recents_invisible_accessible;
                } else {
                    i3 = C0021R$string.screen_pinning_description_recents_invisible;
                }
            }
            if (navigationBarView != null) {
                ((ImageView) this.mLayout.findViewById(C0015R$id.screen_pinning_back_icon)).setImageDrawable(navigationBarView.getBackDrawable());
                ((ImageView) this.mLayout.findViewById(C0015R$id.screen_pinning_home_icon)).setImageDrawable(navigationBarView.getHomeDrawable());
            }
            int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.screen_pinning_description_bullet_gap_width);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(getContext().getText(i3), new BulletSpan(dimensionPixelSize), 0);
            spannableStringBuilder.append((CharSequence) System.lineSeparator());
            spannableStringBuilder.append(getContext().getText(C0021R$string.screen_pinning_exposes_personal_data), new BulletSpan(dimensionPixelSize), 0);
            spannableStringBuilder.append((CharSequence) System.lineSeparator());
            spannableStringBuilder.append(getContext().getText(C0021R$string.screen_pinning_can_open_other_apps), new BulletSpan(dimensionPixelSize), 0);
            ((TextView) this.mLayout.findViewById(C0015R$id.screen_pinning_description)).setText(spannableStringBuilder);
            if (isTouchExplorationEnabled) {
                i4 = 4;
            }
            this.mLayout.findViewById(C0015R$id.screen_pinning_back_bg).setVisibility(i4);
            this.mLayout.findViewById(C0015R$id.screen_pinning_back_bg_light).setVisibility(i4);
            addView(this.mLayout, ScreenPinningRequest.this.getRequestLayoutParams(i));
        }

        private void swapChildrenIfRtlAndVertical(View view) {
            if (((FrameLayout) this).mContext.getResources().getConfiguration().getLayoutDirection() == 1) {
                LinearLayout linearLayout = (LinearLayout) view;
                if (linearLayout.getOrientation() == 1) {
                    int childCount = linearLayout.getChildCount();
                    ArrayList arrayList = new ArrayList(childCount);
                    for (int i = 0; i < childCount; i++) {
                        arrayList.add(linearLayout.getChildAt(i));
                    }
                    linearLayout.removeAllViews();
                    for (int i2 = childCount - 1; i2 >= 0; i2--) {
                        linearLayout.addView((View) arrayList.get(i2));
                    }
                }
            }
        }

        public void onDetachedFromWindow() {
            this.mBroadcastDispatcher.unregisterReceiver(this.mReceiver);
        }

        /* access modifiers changed from: protected */
        public void onConfigurationChanged() {
            removeAllViews();
            inflateView(RotationUtils.getRotation(((FrameLayout) this).mContext));
        }
    }
}
