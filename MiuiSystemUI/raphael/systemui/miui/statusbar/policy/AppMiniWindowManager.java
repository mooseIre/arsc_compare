package com.android.systemui.miui.statusbar.policy;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.MiuiMultiWindowUtils;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.android.systemui.Dependency;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.Interpolators;
import com.android.systemui.Logger;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.HeadsUpAnimatedStubView;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.miui.statusbar.policy.AppMiniWindowManager;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.stack.ExpandableViewState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import miui.process.ForegroundInfo;
import miui.process.IForegroundWindowListener;
import miui.process.ProcessManager;
import miui.view.MiuiHapticFeedbackConstants;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;

public class AppMiniWindowManager implements Gefingerpoken, ConfigurationController.ConfigurationListener, OnHeadsUpChangedListener {
    private static final int SCREEN_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private boolean mAnimationStart;
    /* access modifiers changed from: private */
    public HeadsUpAnimatedStubView mContainer;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                AppMiniWindowManager.this.hideNotification();
            } else if (i == 2) {
                AppMiniWindowManager.this.updateMiniWindowBar(message.getData().getBoolean("show_mini_bar"));
            }
        }
    };
    private final boolean mHasFreeformFeature;
    /* access modifiers changed from: private */
    public ExpandableNotificationRow mHeadsUp;
    /* access modifiers changed from: private */
    public HeadsUpManager mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public PendingIntent mIntent;
    private boolean mInterceptTouch;
    private float mInterceptedY;
    private boolean mIsPortrait = true;
    /* access modifiers changed from: private */
    public ExpandableNotificationRow mPendingHeadsUp;
    private final Rect mPinnedNotificationBounds = new Rect();
    /* access modifiers changed from: private */
    public float mScaleX = 1.0f;
    /* access modifiers changed from: private */
    public float mScaleY = 1.0f;
    ShadeController mShadeController;
    /* access modifiers changed from: private */
    public boolean mStartingActivity;
    private final Rect mStubBounds = new Rect();
    private float mTouchSlop;
    private boolean mTouchingIndicator;
    private boolean mTracking;
    private final int mTriggerHeightHorizontal;
    private final int mTriggerHeightMin;
    private final int mTriggerHeightPortrait;
    private VelocityTracker mVelocityTracker;
    private IForegroundWindowListener.Stub mWindowListener = new IForegroundWindowListener.Stub() {
        public void onForegroundWindowChanged(ForegroundInfo foregroundInfo) {
            Log.d("AppMiniWindowManager", "onForegroundWindowChanged: " + foregroundInfo.mForegroundPackageName);
            if (!AppMiniWindowManager.this.mIntent.getCreatorPackage().equals(foregroundInfo.mForegroundPackageName)) {
                return;
            }
            if (AppMiniWindowManager.this.mHandler.hasMessages(1)) {
                AppMiniWindowManager.this.mHandler.removeMessages(1);
                AppMiniWindowManager.this.mHandler.sendEmptyMessage(1);
                return;
            }
            Message obtainMessage = AppMiniWindowManager.this.mHandler.obtainMessage();
            obtainMessage.what = 2;
            Bundle bundle = new Bundle();
            AppMiniWindowManager appMiniWindowManager = AppMiniWindowManager.this;
            bundle.putBoolean("show_mini_bar", appMiniWindowManager.canNotificationSlide(appMiniWindowManager.mContext, AppMiniWindowManager.this.mHeadsUp));
            obtainMessage.setData(bundle);
            AppMiniWindowManager.this.mHandler.sendMessage(obtainMessage);
        }
    };

    public void onHeadsUpPinnedModeChanged(boolean z) {
    }

    public AppMiniWindowManager(final Context context, ShadeController shadeController) {
        this.mContext = context;
        this.mShadeController = shadeController;
        this.mTouchSlop = (float) ViewConfiguration.get(context).getScaledTouchSlop();
        this.mTriggerHeightPortrait = context.getResources().getDimensionPixelSize(R.dimen.smallwindow_trigger_portrait);
        this.mTriggerHeightHorizontal = context.getResources().getDimensionPixelSize(R.dimen.smallwindow_trigger_horizontal);
        this.mTriggerHeightMin = context.getResources().getDimensionPixelSize(R.dimen.smallwindow_trigger_min);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        Object invoke = invoke(((WindowManager) context.getSystemService("window")).getDefaultDisplay(), "hasSmallFreeformFeature", new Object[0]);
        if (invoke == null || !((Boolean) invoke).booleanValue()) {
            this.mHasFreeformFeature = false;
        } else {
            this.mHasFreeformFeature = true;
        }
        if (this.mHasFreeformFeature && !isShownNotification(context)) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    AppMiniWindowManager.this.showSmallwindowGuideNotification(context);
                }
            }, 1800000);
        }
    }

    public void setHeadsUpStubView(HeadsUpAnimatedStubView headsUpAnimatedStubView) {
        this.mContainer = headsUpAnimatedStubView;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
        this.mHeadsUpManager.addListener(this);
    }

    public boolean isStartingActivity(String str) {
        ExpandableNotificationRow expandableNotificationRow;
        ExpandableNotificationRow expandableNotificationRow2 = this.mHeadsUp;
        if ((expandableNotificationRow2 == null || !expandableNotificationRow2.getEntry().key.equals(str)) && ((expandableNotificationRow = this.mPendingHeadsUp) == null || !expandableNotificationRow.getEntry().key.equals(str))) {
            return false;
        }
        return this.mStartingActivity;
    }

    private int getNotificationOffset(float f, float f2) {
        float min = Math.min(f / f2, 1.0f);
        float f3 = 13.0f * min;
        float f4 = f3 * min;
        return (int) (((((min * f4) / 75.0f) - (f4 / 25.0f)) + (f3 / 25.0f)) * f2);
    }

    private boolean isSlideAvailable() {
        ExpandableNotificationRow expandableNotificationRow = this.mHeadsUp;
        if (expandableNotificationRow == null) {
            return false;
        }
        ExpandableViewState viewState = expandableNotificationRow.getViewState();
        if ((viewState == null || !viewState.isAnimating(this.mHeadsUp)) && this.mHeadsUp.getTranslation() == 0.0f) {
            return true;
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (this.mHeadsUp == null) {
            return false;
        }
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        Log.d("AppMiniWindowManager", String.format("onInterceptTouchEvent action=%s {%.1f, %.1f}", new Object[]{MotionEvent.actionToString(motionEvent.getActionMasked()), Float.valueOf(x), Float.valueOf(y)}));
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInterceptTouch = false;
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            updatePinnedNotificationBounds();
            this.mTouchingIndicator = this.mPinnedNotificationBounds.contains((int) x, (int) y);
        } else if (actionMasked != 1 && actionMasked == 2) {
            float f = x - this.mInitialTouchX;
            float f2 = y - this.mInitialTouchY;
            boolean isSlideAvailable = isSlideAvailable();
            if (this.mTouchingIndicator && f2 > this.mTouchSlop && Math.abs(f2) > Math.abs(f) && isSlideAvailable && canNotificationSlide(this.mContext, this.mHeadsUp) && !this.mHeadsUp.isOptimizedGameHeadsUpBg()) {
                this.mInterceptTouch = true;
                this.mInterceptedY = y;
                this.mAnimationStart = false;
                setTracking(true);
                setHeightInternal(this.mPinnedNotificationBounds.height());
            }
        }
        return this.mInterceptTouch;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0046, code lost:
        if (r9 != 3) goto L_0x00e8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r9) {
        /*
            r8 = this;
            com.android.systemui.statusbar.ExpandableNotificationRow r0 = r8.mHeadsUp
            r1 = 0
            if (r0 != 0) goto L_0x0006
            return r1
        L_0x0006:
            android.view.VelocityTracker r0 = r8.mVelocityTracker
            r0.addMovement(r9)
            float r0 = r9.getX()
            float r2 = r9.getY()
            r3 = 3
            java.lang.Object[] r4 = new java.lang.Object[r3]
            int r5 = r9.getActionMasked()
            java.lang.String r5 = android.view.MotionEvent.actionToString(r5)
            r4[r1] = r5
            java.lang.Float r5 = java.lang.Float.valueOf(r0)
            r6 = 1
            r4[r6] = r5
            java.lang.Float r5 = java.lang.Float.valueOf(r2)
            r7 = 2
            r4[r7] = r5
            java.lang.String r5 = "onTouchEvent action=%s {%.1f, %.1f}"
            java.lang.String r4 = java.lang.String.format(r5, r4)
            java.lang.String r5 = "AppMiniWindowManager"
            android.util.Log.d(r5, r4)
            int r9 = r9.getActionMasked()
            if (r9 == 0) goto L_0x00e4
            r4 = 3000(0xbb8, double:1.482E-320)
            r0 = 0
            if (r9 == r6) goto L_0x0093
            if (r9 == r7) goto L_0x004a
            if (r9 == r3) goto L_0x0093
            goto L_0x00e8
        L_0x004a:
            float r9 = r8.mInterceptedY
            float r2 = r2 - r9
            float r9 = java.lang.Math.max(r0, r2)
            boolean r0 = r8.mTracking
            if (r0 == 0) goto L_0x00e8
            boolean r0 = r8.mAnimationStart
            if (r0 != 0) goto L_0x00e8
            android.graphics.Rect r0 = r8.mPinnedNotificationBounds
            int r0 = r0.height()
            int r2 = SCREEN_HEIGHT
            float r2 = (float) r2
            int r9 = r8.getNotificationOffset(r9, r2)
            if (r9 != 0) goto L_0x006b
            r8.setTracking(r1)
        L_0x006b:
            int r0 = r0 + r9
            r8.setHeightInternal(r0)
            boolean r0 = r8.mIsPortrait
            if (r0 == 0) goto L_0x0078
            int r0 = r8.mTriggerHeightPortrait
            if (r9 <= r0) goto L_0x00e8
            goto L_0x007c
        L_0x0078:
            int r0 = r8.mTriggerHeightHorizontal
            if (r9 <= r0) goto L_0x00e8
        L_0x007c:
            r8.startEnterAnimation()
            r8.startFreeFormActivity()
            java.lang.Class<com.android.systemui.miui.statusbar.analytics.SystemUIStat> r9 = com.android.systemui.miui.statusbar.analytics.SystemUIStat.class
            java.lang.Object r9 = com.android.systemui.Dependency.get(r9)
            com.android.systemui.miui.statusbar.analytics.SystemUIStat r9 = (com.android.systemui.miui.statusbar.analytics.SystemUIStat) r9
            r9.handleFreeformEventDistance()
            android.os.Handler r9 = r8.mHandler
            r9.sendEmptyMessageDelayed(r6, r4)
            goto L_0x00e8
        L_0x0093:
            boolean r9 = r8.mTracking
            if (r9 == 0) goto L_0x00d6
            boolean r9 = r8.mAnimationStart
            if (r9 != 0) goto L_0x00d6
            float r9 = r8.mInterceptedY
            float r2 = r2 - r9
            float r9 = java.lang.Math.max(r0, r2)
            int r0 = SCREEN_HEIGHT
            float r0 = (float) r0
            int r9 = r8.getNotificationOffset(r9, r0)
            android.view.VelocityTracker r0 = r8.mVelocityTracker
            r2 = 1000(0x3e8, float:1.401E-42)
            r0.computeCurrentVelocity(r2)
            android.view.VelocityTracker r0 = r8.mVelocityTracker
            float r0 = r0.getYVelocity(r1)
            r2 = 1148846080(0x447a0000, float:1000.0)
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 <= 0) goto L_0x00d6
            int r0 = r8.mTriggerHeightMin
            if (r9 <= r0) goto L_0x00d6
            r8.startEnterAnimation()
            r8.startFreeFormActivity()
            java.lang.Class<com.android.systemui.miui.statusbar.analytics.SystemUIStat> r9 = com.android.systemui.miui.statusbar.analytics.SystemUIStat.class
            java.lang.Object r9 = com.android.systemui.Dependency.get(r9)
            com.android.systemui.miui.statusbar.analytics.SystemUIStat r9 = (com.android.systemui.miui.statusbar.analytics.SystemUIStat) r9
            r9.handleFreeformEventSpeed()
            android.os.Handler r9 = r8.mHandler
            r9.sendEmptyMessageDelayed(r6, r4)
        L_0x00d6:
            boolean r9 = r8.mAnimationStart
            if (r9 != 0) goto L_0x00dd
            r8.startExitAnimation()
        L_0x00dd:
            boolean r9 = r8.mInterceptTouch
            if (r9 == 0) goto L_0x00e8
            r8.mInterceptTouch = r1
            return r6
        L_0x00e4:
            r8.mInitialTouchX = r0
            r8.mInitialTouchY = r2
        L_0x00e8:
            boolean r8 = r8.mInterceptTouch
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.statusbar.policy.AppMiniWindowManager.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void startFreeFormActivity() {
        try {
            ActivityOptions activityOptions = (ActivityOptions) callStaticObjectMethod(Class.forName("android.util.MiuiMultiWindowUtils"), ActivityOptions.class, "getActivityOptions", new Class[]{Context.class, String.class, Boolean.TYPE, Boolean.TYPE}, this.mContext, this.mIntent.getCreatorPackage(), true, false);
            if (activityOptions != null) {
                Method isMethodExist = isMethodExist(activityOptions, "getActivityOptionsInjector", (Object[]) null);
                if (isMethodExist != null) {
                    invoke(isMethodExist.invoke(activityOptions, new Object[0]), "setFreeformAnimation", false);
                }
                Intent intent = new Intent();
                if (!"com.tencent.tim".equals(this.mIntent.getCreatorPackage())) {
                    intent.addFlags(134217728);
                    intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                    intent.addFlags(8388608);
                }
                this.mIntent.send(this.mContext, 0, intent, (PendingIntent.OnFinished) null, (Handler) null, (String) null, activityOptions.toBundle());
                this.mStartingActivity = true;
            }
        } catch (Exception e) {
            Logger.fullW("AppMiniWindowManager", "Start freeform failed: " + e);
        }
    }

    private void setHeightInternal(int i) {
        Log.d("AppMiniWindowManager", "setHeightInternal h=" + i);
        Rect rect = this.mStubBounds;
        Rect rect2 = this.mPinnedNotificationBounds;
        int i2 = rect2.left;
        int i3 = rect2.top;
        rect.set(i2, i3, rect2.right, i3 + i);
        float max = Math.max(this.mScaleX, this.mScaleY);
        if (max < 1.0f) {
            float f = 1.0f - max;
            float width = ((float) this.mPinnedNotificationBounds.width()) * f;
            float height = ((float) this.mPinnedNotificationBounds.height()) * f;
            Rect rect3 = this.mStubBounds;
            float f2 = width / 2.0f;
            rect3.left = (int) (((float) rect3.left) + f2);
            float f3 = height / 2.0f;
            rect3.top = (int) (((float) rect3.top) + f3);
            rect3.right = (int) (((float) rect3.right) - f2);
            rect3.bottom = (int) (((float) rect3.bottom) - f3);
        }
        this.mContainer.applyStubBounds(this.mStubBounds);
        applyAlpha(i);
    }

    private void applyAlpha(int i) {
        int i2;
        int i3;
        if (this.mIsPortrait) {
            i3 = this.mPinnedNotificationBounds.height();
            i2 = this.mTriggerHeightPortrait;
        } else {
            i3 = this.mPinnedNotificationBounds.height();
            i2 = this.mTriggerHeightHorizontal;
        }
        float f = (float) (i3 + i2);
        float f2 = (float) i;
        float f3 = (f - f2) / f;
        if (f3 > 1.0f) {
            f3 = 1.0f;
        }
        float f4 = (f2 - f) / f;
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        this.mContainer.applyAlpha(f3, f4);
    }

    /* access modifiers changed from: private */
    public void setTracking(boolean z) {
        ExpandableNotificationRow expandableNotificationRow;
        Log.d("AppMiniWindowManager", "setTracking tracking=" + z);
        if (this.mTracking != z) {
            if (z) {
                this.mContainer.updateRenderStatus(true);
            }
            this.mTracking = z;
            this.mContainer.setAnimationRunning(z);
            this.mContainer.setBarVisibility(z);
            this.mHeadsUpManager.setSticky(z ? 2147483647L : 2000);
        }
        if (z && (expandableNotificationRow = this.mHeadsUp) != null) {
            this.mScaleX = expandableNotificationRow.getScaleX();
            this.mScaleY = this.mHeadsUp.getScaleY();
            if (this.mScaleX < 1.0f || this.mScaleY < 1.0f) {
                IStateStyle useValue = Folme.useValue(new Object[0]);
                useValue.setTo("scaleX", Float.valueOf(this.mScaleX), "scaleY", Float.valueOf(this.mScaleY));
                useValue.addListener(new MultiFloatTransitionListener() {
                    /* access modifiers changed from: package-private */
                    public void onUpdate(Map<String, Float> map) {
                        float unused = AppMiniWindowManager.this.mScaleX = map.get("scaleX").floatValue();
                        float unused2 = AppMiniWindowManager.this.mScaleY = map.get("scaleY").floatValue();
                    }
                });
                useValue.to("scaleX", 1, "scaleY", 1);
            }
        }
    }

    private void startEnterAnimation() {
        Rect rect = new Rect(this.mStubBounds);
        Rect freeformRect = MiuiMultiWindowUtils.getFreeformRect(this.mContext);
        freeformRect.right = freeformRect.left + ((int) (((float) freeformRect.width()) * MiuiMultiWindowUtils.sScale));
        freeformRect.bottom = freeformRect.top + ((int) (((float) freeformRect.height()) * MiuiMultiWindowUtils.sScale));
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.setDuration(300);
        ofFloat.setInterpolator(Interpolators.DECELERATE_CUBIC);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(rect, freeformRect) {
            private final /* synthetic */ Rect f$1;
            private final /* synthetic */ Rect f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                AppMiniWindowManager.this.lambda$startEnterAnimation$0$AppMiniWindowManager(this.f$1, this.f$2, valueAnimator);
            }
        });
        this.mAnimationStart = true;
        ofFloat.start();
    }

    public /* synthetic */ void lambda$startEnterAnimation$0$AppMiniWindowManager(Rect rect, Rect rect2, ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        Rect rect3 = this.mStubBounds;
        int i = rect.left;
        int i2 = (int) (((float) i) + (((float) (rect2.left - i)) * floatValue));
        int i3 = rect.top;
        int i4 = (int) (((float) i3) + (((float) (rect2.top - i3)) * floatValue));
        int i5 = rect.right;
        int i6 = rect.bottom;
        rect3.set(i2, i4, (int) (((float) i5) + (((float) (rect2.right - i5)) * floatValue)), (int) (((float) i6) + (((float) (rect2.bottom - i6)) * floatValue)));
        this.mContainer.applyStubBounds(this.mStubBounds);
        applyAlpha(this.mStubBounds.height());
    }

    private void startExitAnimation() {
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{this.mStubBounds.height(), this.mPinnedNotificationBounds.height()});
        ofInt.setDuration(300);
        ofInt.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                AppMiniWindowManager.this.setTracking(false);
                if (AppMiniWindowManager.this.mPendingHeadsUp != null && AppMiniWindowManager.this.mContainer != null) {
                    AppMiniWindowManager.this.mContainer.onHeadsUpPinned(AppMiniWindowManager.this.mPendingHeadsUp);
                    AppMiniWindowManager appMiniWindowManager = AppMiniWindowManager.this;
                    appMiniWindowManager.onHeadsUpPinned(appMiniWindowManager.mPendingHeadsUp);
                }
            }
        });
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                AppMiniWindowManager.this.lambda$startExitAnimation$1$AppMiniWindowManager(valueAnimator);
            }
        });
        ofInt.start();
    }

    public /* synthetic */ void lambda$startExitAnimation$1$AppMiniWindowManager(ValueAnimator valueAnimator) {
        setHeightInternal(((Integer) valueAnimator.getAnimatedValue()).intValue());
    }

    /* access modifiers changed from: private */
    public void hideNotification() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.setDuration(300);
        ofFloat.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                boolean unused = AppMiniWindowManager.this.mStartingActivity = false;
                if (AppMiniWindowManager.this.mHeadsUp != null) {
                    AppMiniWindowManager appMiniWindowManager = AppMiniWindowManager.this;
                    appMiniWindowManager.mShadeController.performRemoveNotification(appMiniWindowManager.mHeadsUp.getStatusBarNotification());
                }
                AppMiniWindowManager.this.mHeadsUpManager.releaseAllImmediately();
                AppMiniWindowManager.this.setTracking(false);
                AppMiniWindowManager.this.mContainer.setAlpha(1.0f);
            }
        });
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                AppMiniWindowManager.this.lambda$hideNotification$2$AppMiniWindowManager(valueAnimator);
            }
        });
        ofFloat.start();
    }

    public /* synthetic */ void lambda$hideNotification$2$AppMiniWindowManager(ValueAnimator valueAnimator) {
        this.mContainer.setAlpha(1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public void onConfigChanged(Configuration configuration) {
        boolean z = true;
        if (configuration.orientation != 1) {
            z = false;
        }
        if (this.mIsPortrait != z) {
            this.mIsPortrait = z;
            updatePinnedNotificationBounds();
        }
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        ExpandableNotificationRow expandableNotificationRow2 = this.mHeadsUp;
        if (expandableNotificationRow2 == null || !expandableNotificationRow2.isHiddenForAnimation()) {
            this.mHeadsUp = expandableNotificationRow;
            if (this.mHeadsUp == this.mPendingHeadsUp) {
                this.mPendingHeadsUp = null;
            } else {
                ProcessManager.registerForegroundWindowListener(this.mWindowListener);
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                }
            }
            this.mIntent = getIntent(this.mHeadsUp.getStatusBarNotification().getNotification());
            return;
        }
        this.mPendingHeadsUp = expandableNotificationRow;
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        if ((this.mHeadsUp != expandableNotificationRow || this.mPendingHeadsUp == null) && expandableNotificationRow == this.mHeadsUp) {
            this.mHeadsUp = null;
            this.mIntent = null;
            this.mContainer.reset();
            ProcessManager.unregisterForegroundWindowListener(this.mWindowListener);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                this.mVelocityTracker = null;
            }
        }
    }

    private void updatePinnedNotificationBounds() {
        this.mHeadsUpManager.getPinnedNotificationBounds(this.mPinnedNotificationBounds);
    }

    /* access modifiers changed from: private */
    public void updateMiniWindowBar(boolean z) {
        ExpandableNotificationRow expandableNotificationRow = this.mHeadsUp;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setMiniBarVisible(z);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("AppMiniWindowManager state:");
        printWriter.println("  mTracking=" + this.mTracking + " mHasFreeformFeature=" + this.mHasFreeformFeature);
    }

    public boolean canNotificationSlide(Context context, ExpandableNotificationRow expandableNotificationRow) {
        return canNotificationSlide(context, expandableNotificationRow.getStatusBarNotification());
    }

    public boolean canNotificationSlide(Context context, ExpandedNotification expandedNotification) {
        if (!this.mHasFreeformFeature || Recents.getSystemServices().hasDockedTask()) {
            return false;
        }
        PendingIntent intent = getIntent(expandedNotification.getNotification());
        ComponentName topActivityComponent = getTopActivityComponent(context);
        if (intent == null || topActivityComponent == null || !intent.isActivity()) {
            return false;
        }
        String creatorPackage = intent.getCreatorPackage();
        if (!((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).isInNotificationSlideWhiteList(creatorPackage)) {
            return false;
        }
        if ("com.tencent.mm".equals(creatorPackage)) {
            Intent intent2 = intent.getIntent();
            if (intent2 == null || intent2.getComponent() == null || isTopSameClass(intent2, topActivityComponent)) {
                return false;
            }
            if (!isTopSamePackage(intent2, topActivityComponent) || !hasSmallWindow(context)) {
                return true;
            }
            return false;
        } else if (topActivityComponent.getPackageName().equals(creatorPackage)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasSmallWindow(Context context) {
        try {
            if (Settings.Secure.getInt(context.getContentResolver(), "freeform_window_state", -1) != -1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isTopSameClass(Intent intent, ComponentName componentName) {
        if (intent == null || intent.getComponent() == null || componentName == null) {
            return false;
        }
        return intent.getComponent().getClassName().equals(componentName.getClassName());
    }

    private static boolean isTopSamePackage(Intent intent, ComponentName componentName) {
        if (intent == null || intent.getComponent() == null || componentName == null) {
            return false;
        }
        return intent.getComponent().getPackageName().equals(componentName.getPackageName());
    }

    private static ComponentName getTopActivityComponent(Context context) {
        List<ActivityManager.RunningTaskInfo> runningTasks = ((ActivityManager) context.getSystemService("activity")).getRunningTasks(1);
        if (runningTasks == null || runningTasks.isEmpty()) {
            return null;
        }
        return runningTasks.get(0).topActivity;
    }

    private void setShownNotification(Context context) {
        Settings.Global.putInt(context.getContentResolver(), "small_window_shown_notification", 1);
    }

    private boolean isShownNotification(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "small_window_shown_notification", -1) == 1;
    }

    /* access modifiers changed from: private */
    public void showSmallwindowGuideNotification(Context context) {
        Log.d("AppMiniWindowManager", "AppMiniWindowManager::showSmallwindowGuideNotification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
        intent.putExtra(":settings:show_fragment", "com.android.settings.freeform.FreeformGuideSettings");
        intent.putExtra(":settings:show_fragment_title", context.getResources().getString(R.string.small_window_guide_title));
        if (resolveActivity(context, intent) != null) {
            createNotificationChannelsForPackage(context, "com.miui.freeform");
            PendingIntent activity = PendingIntent.getActivity(context, 0, intent, 134217728);
            Notification.Builder builder = new Notification.Builder(context, "small_window_channel_id");
            builder.setSmallIcon(R.drawable.notification_smallicon);
            builder.setContentTitle(context.getResources().getString(R.string.small_window_notification_title));
            builder.setContentText(String.format(context.getResources().getString(R.string.small_window_notification_text), new Object[]{12}));
            builder.setContentIntent(activity);
            Notification build = builder.build();
            build.flags |= 16;
            try {
                invoke(notificationManager, "notifyAsPackage", "com.miui.freeform", "small_window", 1, build);
            } catch (Exception e) {
                Log.d("AppMiniWindowManager", "NotificationManager notifyAsPackage error", e);
            }
            setShownNotification(context);
        }
    }

    private ResolveInfo resolveActivity(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0);
    }

    private void createNotificationChannelsForPackage(Context context, String str) {
        try {
            INotificationManager.Stub.asInterface(ServiceManager.getService("notification")).createNotificationChannelsForPackage(str, getUidByPackageName(context, str), new ParceledListSlice(Arrays.asList(new NotificationChannel[]{new NotificationChannel("small_window_channel_id", "small_window_channel_name", 3)})));
        } catch (RemoteException e) {
            Log.d("AppMiniWindowManager", "NotificationManager create notification channel error", e);
        }
    }

    private int getUidByPackageName(Context context, String str) {
        try {
            return context.getPackageManager().getApplicationInfo(str, 0).uid;
        } catch (PackageManager.NameNotFoundException unused) {
            Log.i("AppMiniWindowManager", "not find packageName :" + str);
            return -1;
        }
    }

    private static PendingIntent getIntent(Notification notification) {
        if (notification == null) {
            return null;
        }
        PendingIntent pendingIntent = notification.contentIntent;
        return pendingIntent != null ? pendingIntent : notification.fullScreenIntent;
    }

    private static abstract class MultiFloatTransitionListener extends TransitionListener {
        private final Map<String, Float> mCurrentInfo;

        /* access modifiers changed from: package-private */
        public abstract void onUpdate(Map<String, Float> map);

        private MultiFloatTransitionListener() {
            this.mCurrentInfo = new HashMap();
        }

        public final void onUpdate(Object obj, Collection<UpdateInfo> collection) {
            super.onUpdate(obj, collection);
            collection.forEach(new Consumer() {
                public final void accept(Object obj) {
                    AppMiniWindowManager.MultiFloatTransitionListener.this.lambda$onUpdate$0$AppMiniWindowManager$MultiFloatTransitionListener((UpdateInfo) obj);
                }
            });
            onUpdate(this.mCurrentInfo);
        }

        public /* synthetic */ void lambda$onUpdate$0$AppMiniWindowManager$MultiFloatTransitionListener(UpdateInfo updateInfo) {
            this.mCurrentInfo.put(updateInfo.property.getName(), Float.valueOf(updateInfo.getFloatValue()));
        }
    }

    private static Object invoke(Object obj, String str, Object... objArr) {
        try {
            Class<?> cls = obj.getClass();
            if (objArr == null) {
                Method declaredMethod = cls.getDeclaredMethod(str, (Class[]) null);
                declaredMethod.setAccessible(true);
                return declaredMethod.invoke(obj, new Object[0]);
            }
            Class[] clsArr = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                if (objArr[i] instanceof Integer) {
                    clsArr[i] = Integer.TYPE;
                } else if (objArr[i] instanceof Boolean) {
                    clsArr[i] = Boolean.TYPE;
                } else if (objArr[i] instanceof Float) {
                    clsArr[i] = Float.TYPE;
                } else {
                    clsArr[i] = objArr[i].getClass();
                }
            }
            Method declaredMethod2 = cls.getDeclaredMethod(str, clsArr);
            declaredMethod2.setAccessible(true);
            return declaredMethod2.invoke(obj, objArr);
        } catch (Exception e) {
            Log.d("AppMiniWindowManager", "getDeclaredMethod:" + e.toString());
            return null;
        }
    }

    private static <T> T callStaticObjectMethod(Class<?> cls, Class<T> cls2, String str, Class<?>[] clsArr, Object... objArr) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method declaredMethod = cls.getDeclaredMethod(str, clsArr);
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke((Object) null, objArr);
    }

    private static Method isMethodExist(Object obj, String str, Object... objArr) {
        try {
            Class<?> cls = obj.getClass();
            if (objArr == null) {
                Method declaredMethod = cls.getDeclaredMethod(str, (Class[]) null);
                declaredMethod.setAccessible(true);
                return declaredMethod;
            }
            Class[] clsArr = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                if (objArr[i] instanceof Integer) {
                    clsArr[i] = Integer.TYPE;
                } else if (objArr[i] instanceof Boolean) {
                    clsArr[i] = Boolean.TYPE;
                } else {
                    clsArr[i] = objArr[i].getClass();
                }
            }
            Method declaredMethod2 = cls.getDeclaredMethod(str, clsArr);
            declaredMethod2.setAccessible(true);
            return declaredMethod2;
        } catch (Exception e) {
            Log.d("AppMiniWindowManager", "getDeclaredMethod:" + e.toString());
            return null;
        }
    }
}
