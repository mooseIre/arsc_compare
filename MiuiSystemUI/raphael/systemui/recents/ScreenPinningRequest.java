package com.android.systemui.recents;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.plugins.R;
import java.util.ArrayList;

public class ScreenPinningRequest implements View.OnClickListener {
    /* access modifiers changed from: private */
    public final AccessibilityManager mAccessibilityService = ((AccessibilityManager) this.mContext.getSystemService("accessibility"));
    private final Context mContext;
    private RequestWindowView mRequestWindow;
    /* access modifiers changed from: private */
    public final WindowManager mWindowManager = ((WindowManager) this.mContext.getSystemService("window"));
    private int taskId;

    public ScreenPinningRequest(Context context) {
        this.mContext = context;
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
        this.mRequestWindow = new RequestWindowView(this.mContext, z);
        this.mRequestWindow.setSystemUiVisibility(256);
        this.mWindowManager.addView(this.mRequestWindow, getWindowLayoutParams());
    }

    public void onConfigurationChanged() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            requestWindowView.onConfigurationChanged();
        }
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2024, 16777480, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("ScreenPinningConfirmation");
        layoutParams.gravity = 119;
        return layoutParams;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.screen_pinning_ok_button || this.mRequestWindow == view) {
            try {
                ActivityManagerCompat.startSystemLockTaskMode(this.taskId);
            } catch (RemoteException unused) {
            }
        }
        clearPrompt();
    }

    public FrameLayout.LayoutParams getRequestLayoutParams(boolean z) {
        return new FrameLayout.LayoutParams(-2, -2, z ? 21 : 81);
    }

    private class RequestWindowView extends FrameLayout {
        /* access modifiers changed from: private */
        public final ColorDrawable mColor = new ColorDrawable(0);
        private ValueAnimator mColorAnim;
        /* access modifiers changed from: private */
        public ViewGroup mLayout;
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
        /* access modifiers changed from: private */
        public final Runnable mUpdateLayoutRunnable = new Runnable() {
            public void run() {
                if (RequestWindowView.this.mLayout != null && RequestWindowView.this.mLayout.getParent() != null) {
                    ViewGroup access$300 = RequestWindowView.this.mLayout;
                    RequestWindowView requestWindowView = RequestWindowView.this;
                    access$300.setLayoutParams(ScreenPinningRequest.this.getRequestLayoutParams(requestWindowView.isLandscapePhone(requestWindowView.mContext)));
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
            boolean isLandscapePhone = isLandscapePhone(this.mContext);
            inflateView(isLandscapePhone);
            int color = this.mContext.getColor(R.color.screen_pinning_request_window_bg);
            if (ActivityManager.isHighEndGfx()) {
                this.mLayout.setAlpha(0.0f);
                if (isLandscapePhone) {
                    this.mLayout.setTranslationX(f * 96.0f);
                } else {
                    this.mLayout.setTranslationY(f * 96.0f);
                }
                this.mLayout.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
                this.mColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), new Object[]{0, Integer.valueOf(color)});
                this.mColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        RequestWindowView.this.mColor.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                this.mColorAnim.setDuration(1000);
                this.mColorAnim.start();
            } else {
                this.mColor.setColor(color);
            }
            IntentFilter intentFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
        }

        /* access modifiers changed from: private */
        public boolean isLandscapePhone(Context context) {
            Configuration configuration = this.mContext.getResources().getConfiguration();
            return configuration.orientation == 2 && configuration.smallestScreenWidthDp < 600;
        }

        private void inflateView(boolean z) {
            this.mLayout = (ViewGroup) View.inflate(getContext(), z ? R.layout.screen_pinning_request_land_phone : R.layout.screen_pinning_request, (ViewGroup) null);
            this.mLayout.setClickable(true);
            int i = 0;
            this.mLayout.setLayoutDirection(0);
            this.mLayout.findViewById(R.id.screen_pinning_text_area).setLayoutDirection(3);
            View findViewById = this.mLayout.findViewById(R.id.screen_pinning_buttons);
            if (Recents.getSystemServices().hasSoftNavigationBar(ContextCompat.getDisplayId(this.mContext))) {
                findViewById.setLayoutDirection(3);
                swapChildrenIfRtlAndVertical(findViewById);
            } else {
                findViewById.setVisibility(8);
            }
            ((Button) this.mLayout.findViewById(R.id.screen_pinning_ok_button)).setOnClickListener(ScreenPinningRequest.this);
            if (this.mShowCancel) {
                ((Button) this.mLayout.findViewById(R.id.screen_pinning_cancel_button)).setOnClickListener(ScreenPinningRequest.this);
            } else {
                ((Button) this.mLayout.findViewById(R.id.screen_pinning_cancel_button)).setVisibility(4);
            }
            ((TextView) this.mLayout.findViewById(R.id.screen_pinning_description)).setText(R.string.screen_pinning_description);
            if (ScreenPinningRequest.this.mAccessibilityService.isEnabled()) {
                i = 4;
            }
            this.mLayout.findViewById(R.id.screen_pinning_back_bg).setVisibility(i);
            this.mLayout.findViewById(R.id.screen_pinning_back_bg_light).setVisibility(i);
            addView(this.mLayout, ScreenPinningRequest.this.getRequestLayoutParams(z));
        }

        private void swapChildrenIfRtlAndVertical(View view) {
            if (this.mContext.getResources().getConfiguration().getLayoutDirection() == 1) {
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
            this.mContext.unregisterReceiver(this.mReceiver);
        }

        /* access modifiers changed from: protected */
        public void onConfigurationChanged() {
            removeAllViews();
            inflateView(isLandscapePhone(this.mContext));
        }
    }
}
