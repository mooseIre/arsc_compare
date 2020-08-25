package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MiuiMultiWindowUtils;
import android.util.TypedValue;
import android.view.IGestureStubListener;
import android.view.KeyEvent;
import android.view.MiuiWindowManager;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.android.systemui.Application;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.fsgesture.IFsGestureCallback;
import com.android.systemui.fsgesture.TransitionAnimationSpec;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.FsGestureMoveEvent;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.AnimFirstTaskViewAlphaEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsCompleteEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsZoomEvent;
import com.android.systemui.recents.events.activity.FsGestureLaunchTargetTaskViewRectEvent;
import com.android.systemui.recents.events.activity.FsGesturePreloadRecentsEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeMoveEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeResetEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeSlideInEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeSlideOutEvent;
import com.android.systemui.recents.events.activity.FsGestureShowFirstCardEvent;
import com.android.systemui.recents.events.activity.FsGestureSlideInEvent;
import com.android.systemui.recents.events.activity.FsGestureSlideOutEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.MutableBoolean;
import com.xiaomi.stat.c.b;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import miui.util.ScreenshotUtils;

public class NavStubView extends FrameLayout {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    public static final int DEFAULT_ANIM_TIME;
    private static HashMap<String, Float> DEVICE_BOTTOM_EDGE_HEIGHTS;
    public static final boolean IS_E10;
    public static final String TAG = NavStubView.class.getSimpleName();
    public final int RADIUS_SIZE;
    /* access modifiers changed from: private */
    public AntiMistakeTouchView antiMistakeTouchView;
    ExecutorService jobExecutor;
    private ActivityManager mAm;
    private float mAssistDistantThreshold;
    private float mAssistLastProgress;
    /* access modifiers changed from: private */
    public AssistManager mAssistManager;
    private int mAssistantWidth;
    private boolean mBitmapShown;
    /* access modifiers changed from: private */
    public boolean mCancelActionToStartApp;
    private Intent mCloseScreenshotIntent;
    private Interpolator mCubicEaseOutInterpolator;
    /* access modifiers changed from: private */
    public float mCurAlpha;
    /* access modifiers changed from: private */
    public float mCurScale;
    /* access modifiers changed from: private */
    public ActivityManager.RecentTaskInfo mCurTask;
    /* access modifiers changed from: private */
    public int mCurrAction;
    /* access modifiers changed from: private */
    public float mCurrX;
    /* access modifiers changed from: private */
    public float mCurrY;
    private float mCurrentX;
    /* access modifiers changed from: private */
    public float mCurrentY;
    private float mDelta;
    private Rect mDest;
    /* access modifiers changed from: private */
    public RectF mDestRectF;
    /* access modifiers changed from: private */
    public float mDestRectHeightScale;
    /* access modifiers changed from: private */
    public float mDestTopOffset;
    private boolean mDisableTouch;
    private final int mDividerSize;
    /* access modifiers changed from: private */
    public MotionEvent mDownEvent;
    private int mDownNo;
    private long mDownTime;
    private float mDownX;
    private float mDownY;
    /* access modifiers changed from: private */
    public float mFollowTailX;
    /* access modifiers changed from: private */
    public float mFollowTailY;
    /* access modifiers changed from: private */
    public Handler mFrameHandler = new Handler();
    private BroadcastReceiver mFullScreenModeChangeReceiver;
    /* access modifiers changed from: private */
    public GestureStubListenerWrapper mGestureStubListenerWrapper;
    /* access modifiers changed from: private */
    public H mHandler;
    /* access modifiers changed from: private */
    public Runnable mHapticFeedbackRunnable;
    /* access modifiers changed from: private */
    public IFsGestureCallback mHomeCallback;
    /* access modifiers changed from: private */
    public ValueAnimator mHomeFadeInAnim;
    /* access modifiers changed from: private */
    public ValueAnimator mHomeFadeOutAnim;
    /* access modifiers changed from: private */
    public final Intent mHomeIntent;
    /* access modifiers changed from: private */
    public IActivityManager mIam;
    private float mInitX;
    private float mInitY;
    /* access modifiers changed from: private */
    public boolean mIsAlreadyCropStatusBar;
    private boolean mIsAppToHome;
    private boolean mIsAppToRecents;
    private boolean mIsAssistantAvailable;
    /* access modifiers changed from: private */
    public boolean mIsBgIconVisible;
    /* access modifiers changed from: private */
    public boolean mIsEnterRecents;
    private boolean mIsFsgVersionTwo;
    /* access modifiers changed from: private */
    public boolean mIsFullScreenMode;
    /* access modifiers changed from: private */
    public boolean mIsFullScreenModeCurTime;
    /* access modifiers changed from: private */
    public boolean mIsGestureStarted;
    private boolean mIsInFsMode;
    /* access modifiers changed from: private */
    public boolean mIsMultiWindow;
    /* access modifiers changed from: private */
    public boolean mIsSkipResetLauncherAlpha;
    private boolean mIsSuperPowerMode;
    private boolean mIsWriteSettingMove;
    private boolean mKeepHidden;
    private Configuration mLastConfiguration;
    private int mLastDownNo;
    private long mLastTouchTime;
    private boolean mLaunchedAssistant;
    private int[] mLocation;
    private Xfermode mModeSrcIn = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    /* access modifiers changed from: private */
    public int mMultiDelta;
    private boolean mNeedRender;
    boolean mOrientationChangedAfterDown;
    private Paint mPaint;
    /* access modifiers changed from: private */
    public boolean mPendingResetStatus;
    /* access modifiers changed from: private */
    public int mPivotLocX;
    /* access modifiers changed from: private */
    public int mPivotLocY;
    private QuartEaseOutInterpolator mQuartEaseOutInterpolator;
    private boolean mRecentVisible;
    /* access modifiers changed from: private */
    public ValueAnimator mRecentsModeHomeFadeInAnim;
    private final Intent mRecentsModeHomeIntent;
    /* access modifiers changed from: private */
    public ValueAnimator mRecentsModeRecentsSlideInAnim;
    private Bitmap mScreenBitmap;
    /* access modifiers changed from: private */
    public int mScreenBmpHeight;
    /* access modifiers changed from: private */
    public float mScreenBmpScale;
    /* access modifiers changed from: private */
    public int mScreenBmpWidth;
    /* access modifiers changed from: private */
    public int mScreenHeight;
    /* access modifiers changed from: private */
    public int mScreenWidth;
    private Rect mShowRect;
    private Rect mSrc;
    /* access modifiers changed from: private */
    public int mStateMode;
    private StatusBar mStatusBar;
    /* access modifiers changed from: private */
    public int mStatusBarDec;
    /* access modifiers changed from: private */
    public int mStatusBarHeight;
    private final StatusBarManager mStatusBarManager;
    private boolean mSupportAntiMistake;
    private Runnable mTailCatcherTask;
    private float mTouchDownY;
    private int mTouchSlop;
    private Runnable mUpdateViewLayoutRunnable;
    private WindowManager mWindowManager;
    /* access modifiers changed from: private */
    public int mWindowMode;
    /* access modifiers changed from: private */
    public float mXScale;
    /* access modifiers changed from: private */
    public float mYScale;
    public int targetBgAlpha = 136;

    private static class BerylliumConfig {
        protected static final Interpolator FAST_OUT_SLOW_IN = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    }

    private String getStateModeString(int i) {
        switch (i) {
            case 65537:
                return "StateMode.STATE_INIT";
            case 65538:
                return "StateMode.STATE_ON_DRAG";
            case 65539:
                return "StateMode.TASK_HOLD";
            case 65540:
                return "StateMode.HOME_HOLD";
            default:
                return "Unknown StateMode";
        }
    }

    static /* synthetic */ float access$016(NavStubView navStubView, float f) {
        float f2 = navStubView.mFollowTailX + f;
        navStubView.mFollowTailX = f2;
        return f2;
    }

    static /* synthetic */ float access$216(NavStubView navStubView, float f) {
        float f2 = navStubView.mFollowTailY + f;
        navStubView.mFollowTailY = f2;
        return f2;
    }

    static {
        boolean equals = "beryllium".equals(Build.PRODUCT);
        IS_E10 = equals;
        DEFAULT_ANIM_TIME = equals ? 144 : 300;
        HashMap<String, Float> hashMap = new HashMap<>();
        DEVICE_BOTTOM_EDGE_HEIGHTS = hashMap;
        hashMap.put("perseus", Float.valueOf(4.5f));
        DEVICE_BOTTOM_EDGE_HEIGHTS.put("cepheus", Float.valueOf(3.6f));
        DEVICE_BOTTOM_EDGE_HEIGHTS.put("dipper", Float.valueOf(6.4f));
        DEVICE_BOTTOM_EDGE_HEIGHTS.put("grus", Float.valueOf(3.6f));
    }

    /* access modifiers changed from: private */
    public void updateStateMode(int i) {
        this.mStateMode = i;
        String str = TAG;
        Log.d(str, "current state mode: " + getStateModeString(i));
    }

    public NavStubView(Context context) {
        super(context);
        new PorterDuffXfermode(PorterDuff.Mode.OVERLAY);
        this.mLastConfiguration = new Configuration();
        this.mOrientationChangedAfterDown = false;
        this.mCurrAction = -1;
        this.mLocation = new int[2];
        this.mIsEnterRecents = false;
        this.mIsSkipResetLauncherAlpha = false;
        this.mHapticFeedbackRunnable = new Runnable() {
            public void run() {
                if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("hold", false);
                } else {
                    NavStubView.this.performHapticFeedback(1);
                }
            }
        };
        this.mTailCatcherTask = new Runnable() {
            public void run() {
                NavStubView navStubView = NavStubView.this;
                NavStubView.access$016(navStubView, (((float) navStubView.mPivotLocX) - NavStubView.this.mFollowTailX) / 4.0f);
                NavStubView navStubView2 = NavStubView.this;
                NavStubView.access$216(navStubView2, (((float) navStubView2.mPivotLocY) - NavStubView.this.mFollowTailY) / 4.0f);
                float abs = Math.abs(((float) NavStubView.this.mPivotLocX) - NavStubView.this.mFollowTailX);
                float abs2 = Math.abs(((float) NavStubView.this.mPivotLocY) - NavStubView.this.mFollowTailY);
                double sqrt = Math.sqrt((double) ((abs * abs) + (abs2 * abs2)));
                if (NavStubView.this.mWindowMode == 4) {
                    NavStubView.this.mFrameHandler.postDelayed(this, 16);
                    return;
                }
                switch (NavStubView.this.mStateMode) {
                    case 65538:
                        if (NavStubView.this.mCurrentY < ((float) (NavStubView.this.mScreenHeight - 160)) && sqrt < 80.0d && !NavStubView.this.mIsEnterRecents && (NavStubView.this.mWindowMode == 2 || NavStubView.this.mWindowMode == 1)) {
                            boolean unused = NavStubView.this.mIsEnterRecents = true;
                            RecentsEventBus.getDefault().post(new FsGesturePreloadRecentsEvent());
                            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsEvent());
                        }
                        if (NavStubView.this.mCurrentY < ((float) (NavStubView.this.mScreenHeight - 320)) && sqrt < 40.0d) {
                            NavStubView navStubView3 = NavStubView.this;
                            navStubView3.removeCallbacks(navStubView3.mHapticFeedbackRunnable);
                            NavStubView navStubView4 = NavStubView.this;
                            navStubView4.postDelayed(navStubView4.mHapticFeedbackRunnable, 100);
                            int access$400 = NavStubView.this.mWindowMode;
                            if (access$400 != 1) {
                                if (access$400 != 2) {
                                    if (access$400 == 3) {
                                        NavStubView.this.updateStateMode(65540);
                                        Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 0.0f, 0.8f);
                                        boolean unused2 = NavStubView.this.mIsSkipResetLauncherAlpha = true;
                                        NavStubView.this.mRecentsModeHomeFadeInAnim.setFloatValues(new float[]{0.8f, 0.95f});
                                        NavStubView.this.mRecentsModeHomeFadeInAnim.start();
                                        RecentsEventBus.getDefault().send(new FsGestureRecentsModeSlideOutEvent());
                                        break;
                                    }
                                } else {
                                    NavStubView.this.updateStateMode(65539);
                                    RecentsEventBus.getDefault().send(new FsGestureSlideInEvent(NavStubView.this.mCurrX, NavStubView.this.mCurrY));
                                    break;
                                }
                            } else {
                                NavStubView.this.updateStateMode(65539);
                                RecentsEventBus.getDefault().send(new FsGestureSlideInEvent(NavStubView.this.mCurrX, NavStubView.this.mCurrY));
                                NavStubView.this.mHomeFadeOutAnim.setFloatValues(new float[]{NavStubView.this.mCurScale, 0.8f});
                                NavStubView.this.mHomeFadeOutAnim.start();
                                break;
                            }
                        }
                        break;
                    case 65539:
                        if (NavStubView.this.mCurrentY > ((float) (NavStubView.this.mScreenHeight - 240))) {
                            NavStubView.this.updateStateMode(65538);
                            RecentsEventBus.getDefault().send(new FsGestureSlideOutEvent());
                            if (NavStubView.this.mWindowMode == 1) {
                                NavStubView.this.mHomeFadeInAnim.start();
                                break;
                            }
                        }
                        break;
                    case 65540:
                        if (NavStubView.this.mCurrentY > ((float) (NavStubView.this.mScreenHeight - 240))) {
                            NavStubView.this.updateStateMode(65538);
                            RecentsEventBus.getDefault().post(new FsGesturePreloadRecentsEvent());
                            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsEvent());
                            NavStubView.this.mRecentsModeRecentsSlideInAnim.start();
                            RecentsEventBus.getDefault().send(new FsGestureRecentsModeSlideInEvent());
                            break;
                        }
                        break;
                }
                NavStubView.this.mFrameHandler.postDelayed(this, 16);
            }
        };
        this.mDestRectHeightScale = 1.0f;
        this.mCurAlpha = 1.0f;
        this.mShowRect = new Rect();
        this.mSrc = new Rect();
        this.mDest = new Rect();
        this.mLastDownNo = 0;
        this.mDownNo = 0;
        boolean z = true;
        this.jobExecutor = Executors.newFixedThreadPool(1);
        this.mFullScreenModeChangeReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.android.systemui.fullscreen.statechange".equals(intent.getAction())) {
                    int i = 0;
                    boolean unused = NavStubView.this.mIsFullScreenMode = intent.getBooleanExtra("isEnter", false);
                    if (NavStubView.this.antiMistakeTouchView != null) {
                        AntiMistakeTouchView access$3000 = NavStubView.this.antiMistakeTouchView;
                        if (!NavStubView.this.isMistakeTouch()) {
                            i = 8;
                        }
                        access$3000.updateVisibilityState(i);
                    }
                }
            }
        };
        this.mCubicEaseOutInterpolator = IS_E10 ? BerylliumConfig.FAST_OUT_SLOW_IN : new CubicEaseOutInterpolator();
        this.mQuartEaseOutInterpolator = new QuartEaseOutInterpolator();
        this.mUpdateViewLayoutRunnable = new Runnable() {
            public void run() {
                NavStubView navStubView = NavStubView.this;
                navStubView.updateViewLayout(navStubView.getHotSpaceHeight());
            }
        };
        this.RADIUS_SIZE = context.getResources().getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius) * 2;
        this.mAssistantWidth = getResources().getDimensionPixelSize(R.dimen.gestures_assistant_width);
        this.mAssistDistantThreshold = (float) getResources().getDimensionPixelSize(R.dimen.gestures_assistant_drag_threshold);
        updateStateMode(65537);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mStatusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mScreenWidth = displayMetrics.widthPixels;
        this.mScreenHeight = displayMetrics.heightPixels;
        Paint paint = new Paint();
        this.mPaint = paint;
        paint.setAntiAlias(true);
        Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mHomeIntent = intent;
        intent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        Intent intent2 = new Intent(this.mHomeIntent);
        this.mRecentsModeHomeIntent = intent2;
        intent2.putExtra("skip_reset_gesture_view_state", true);
        this.mIam = ActivityManagerNative.getDefault();
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mDividerSize = getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_thickness) - (getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_insets) * 2);
        int identifier = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (identifier > 0) {
            this.mStatusBarHeight = getResources().getDimensionPixelSize(identifier);
        }
        Intent intent3 = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mCloseScreenshotIntent = intent3;
        intent3.putExtra("reason", "fs_gesture");
        this.mGestureStubListenerWrapper = new GestureStubListenerWrapper();
        this.mHandler = new H();
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initValueAnimator();
        GestureStubListenerWrapper gestureStubListenerWrapper = this.mGestureStubListenerWrapper;
        if (!(gestureStubListenerWrapper == null || gestureStubListenerWrapper.mListener == null)) {
            z = false;
        }
        this.mIsFsgVersionTwo = z;
        this.mSupportAntiMistake = z;
        if (z) {
            AntiMistakeTouchView antiMistakeTouchView2 = new AntiMistakeTouchView(context);
            this.antiMistakeTouchView = antiMistakeTouchView2;
            addView(antiMistakeTouchView2, antiMistakeTouchView2.getFrameLayoutParams());
        }
        this.mRecentVisible = false;
        this.mAssistManager = (AssistManager) Dependency.get(AssistManager.class);
        this.mIsSuperPowerMode = MiuiSettings.System.isSuperSaveModeOpen(this.mContext, UserHandle.myUserId());
    }

    private void initValueAnimator() {
        ValueAnimator valueAnimator = new ValueAnimator();
        this.mHomeFadeOutAnim = valueAnimator;
        valueAnimator.setInterpolator(Interpolators.CUBIC_EASE_OUT);
        this.mHomeFadeOutAnim.setDuration(200);
        this.mHomeFadeOutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f - valueAnimator.getAnimatedFraction(), floatValue);
            }
        });
        ValueAnimator valueAnimator2 = new ValueAnimator();
        this.mHomeFadeInAnim = valueAnimator2;
        valueAnimator2.setFloatValues(new float[]{0.0f, 1.0f});
        this.mHomeFadeInAnim.setInterpolator(Interpolators.CUBIC_EASE_OUT);
        this.mHomeFadeInAnim.setDuration(150);
        this.mHomeFadeInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, animatedFraction, ((NavStubView.this.mCurScale - 0.8f) * animatedFraction) + 0.8f);
            }
        });
        ValueAnimator valueAnimator3 = new ValueAnimator();
        this.mRecentsModeHomeFadeInAnim = valueAnimator3;
        valueAnimator3.setInterpolator(Interpolators.CUBIC_EASE_OUT);
        this.mRecentsModeHomeFadeInAnim.setDuration(200);
        this.mRecentsModeHomeFadeInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, Math.min((floatValue - 0.8f) * 6.6666665f, 1.0f), floatValue);
            }
        });
        ValueAnimator valueAnimator4 = new ValueAnimator();
        this.mRecentsModeRecentsSlideInAnim = valueAnimator4;
        valueAnimator4.setFloatValues(new float[]{0.0f, 1.0f});
        this.mRecentsModeRecentsSlideInAnim.setInterpolator(Interpolators.CUBIC_EASE_OUT);
        this.mRecentsModeRecentsSlideInAnim.setDuration(200);
        this.mRecentsModeRecentsSlideInAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = 1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue();
                Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, floatValue, (0.15f * floatValue) + 0.8f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        AntiMistakeTouchView antiMistakeTouchView2;
        int updateFrom = this.mLastConfiguration.updateFrom(configuration);
        boolean z = true;
        int i = 0;
        boolean z2 = (updateFrom & 128) != 0;
        this.mOrientationChangedAfterDown = this.mOrientationChangedAfterDown || z2;
        boolean z3 = (updateFrom & 1024) != 0;
        boolean z4 = (updateFrom & 2048) != 0;
        if ((updateFrom & 4096) == 0) {
            z = false;
        }
        if ((z && z4 && z3) || z2) {
            updateViewLayout(getHotSpaceHeight());
        }
        if (this.mSupportAntiMistake && (antiMistakeTouchView2 = this.antiMistakeTouchView) != null) {
            if (!isMistakeTouch()) {
                i = 8;
            }
            antiMistakeTouchView2.updateVisibilityState(i);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        RecentsEventBus.getDefault().register(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.fullscreen.statechange");
        this.mContext.registerReceiverAsUser(this.mFullScreenModeChangeReceiver, UserHandle.ALL, intentFilter, "miui.permission.USE_INTERNAL_GENERAL_API", (Handler) null);
    }

    public final void onBusEvent(FsGestureLaunchTargetTaskViewRectEvent fsGestureLaunchTargetTaskViewRectEvent) {
        RectF rectF = fsGestureLaunchTargetTaskViewRectEvent.mRectF;
        this.mDestRectF = rectF;
        if (rectF != null) {
            this.mDestRectHeightScale = (((float) this.mScreenBmpWidth) * rectF.height()) / (this.mDestRectF.width() * ((float) this.mScreenBmpHeight));
        }
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        this.mIsMultiWindow = multiWindowStateChangedEvent.inMultiWindow;
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        boolean z = recentsVisibilityChangedEvent.visible;
        this.mRecentVisible = z;
        if (z && this.mCancelActionToStartApp) {
            RecentsEventBus.getDefault().post(new HideRecentsEvent(false, false, true));
        }
        if (this.mSupportAntiMistake && this.antiMistakeTouchView != null && this.mIsFullScreenMode && getResources().getConfiguration().orientation == 2) {
            int i = 8;
            if (this.mRecentVisible) {
                this.antiMistakeTouchView.updateVisibilityState(8);
            } else {
                AntiMistakeTouchView antiMistakeTouchView2 = this.antiMistakeTouchView;
                if (isMistakeTouch()) {
                    i = 0;
                }
                antiMistakeTouchView2.updateVisibilityState(i);
            }
        }
        if (this.mRecentVisible) {
            return;
        }
        if (this.mIsSkipResetLauncherAlpha) {
            this.mIsSkipResetLauncherAlpha = false;
        } else {
            Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, 1.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecentsEventBus.getDefault().unregister(this);
        this.mContext.unregisterReceiver(this.mFullScreenModeChangeReceiver);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i;
        int i2;
        int i3;
        super.onDraw(canvas);
        if (this.mScreenBitmap != null) {
            if (this.mIsAppToHome) {
                i3 = this.mPivotLocX - (this.mScreenBmpWidth / 2);
                i2 = this.mPivotLocY;
                i = this.mScreenBmpHeight / 2;
            } else {
                i3 = this.mPivotLocX - (this.mScreenBmpWidth / 2);
                i2 = this.mPivotLocY;
                i = this.mScreenBmpHeight;
            }
            int i4 = i2 - i;
            Rect rect = this.mShowRect;
            rect.left = i3;
            rect.top = i4;
            rect.right = i3 + this.mScreenBmpWidth;
            rect.bottom = i4 + this.mScreenBmpHeight;
            if (!this.mIsAppToHome) {
                canvas.save();
                canvas.translate((float) this.mPivotLocX, (float) this.mPivotLocY);
                float f = this.mCurScale;
                canvas.scale(f, f);
                canvas.translate((float) (-this.mPivotLocX), (float) (-this.mPivotLocY));
                if (this.mDestRectF == null || !this.mIsAppToRecents) {
                    this.mPaint.setAlpha(255);
                    this.mPaint.setXfermode((Xfermode) null);
                    this.mPaint.setStyle(Paint.Style.FILL);
                    Rect rect2 = this.mShowRect;
                    float f2 = (float) rect2.right;
                    float f3 = (float) ((int) (((float) this.mShowRect.top) + (((float) this.mScreenBmpHeight) * this.mCurScale)));
                    float f4 = f2;
                    float f5 = f3;
                    int saveLayer = canvas.saveLayer((float) rect2.left, (float) rect2.top, f4, f5, (Paint) null);
                    Rect rect3 = this.mShowRect;
                    float f6 = (float) rect3.right;
                    int i5 = this.RADIUS_SIZE;
                    float f7 = (float) i5;
                    float f8 = (float) i5;
                    canvas.drawRoundRect((float) rect3.left, (float) rect3.top, f6, f5, f7, f8, this.mPaint);
                    this.mPaint.setAlpha((int) (this.mCurAlpha * 255.0f));
                    this.mPaint.setXfermode(this.mModeSrcIn);
                    canvas.drawBitmap(this.mScreenBitmap, (Rect) null, this.mShowRect, this.mPaint);
                    this.mPaint.setXfermode((Xfermode) null);
                    canvas.restoreToCount(saveLayer);
                } else {
                    Rect rect4 = this.mSrc;
                    rect4.left = 0;
                    int i6 = this.mStatusBarDec;
                    rect4.top = i6;
                    rect4.right = this.mScreenBmpWidth;
                    rect4.bottom = (int) ((this.mScreenBmpScale * ((float) this.mScreenBmpHeight)) + ((float) i6));
                    this.mDest.set(rect4);
                    this.mDest.offset(this.mShowRect.left, (int) ((((float) this.mPivotLocY) + ((this.mDestTopOffset - 1.0f) * ((float) this.mScreenBmpHeight))) - ((float) this.mStatusBarDec)));
                    this.mPaint.setAlpha(255);
                    this.mPaint.setXfermode((Xfermode) null);
                    this.mPaint.setStyle(Paint.Style.FILL);
                    Rect rect5 = this.mShowRect;
                    float f9 = (float) rect5.bottom;
                    int saveLayer2 = canvas.saveLayer((float) rect5.left, (float) rect5.top, (float) rect5.right, f9, (Paint) null);
                    Rect rect6 = this.mDest;
                    float f10 = (float) rect6.bottom;
                    int i7 = this.RADIUS_SIZE;
                    canvas.drawRoundRect((float) rect6.left, (float) rect6.top, (float) rect6.right, f10, (float) i7, (float) i7, this.mPaint);
                    this.mPaint.setXfermode(this.mModeSrcIn);
                    canvas.drawBitmap(this.mScreenBitmap, this.mSrc, this.mDest, this.mPaint);
                    this.mPaint.setXfermode((Xfermode) null);
                    canvas.restoreToCount(saveLayer2);
                }
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate((float) this.mPivotLocX, (float) this.mPivotLocY);
                canvas.scale(this.mXScale, this.mYScale);
                canvas.translate((float) (-this.mPivotLocX), (float) (-this.mPivotLocY));
                Rect rect7 = this.mShowRect;
                rect7.bottom = (int) (((float) rect7.top) + (((float) this.mScreenBmpHeight) * this.mCurScale));
                this.mPaint.setAlpha((int) (this.mCurAlpha * 255.0f));
                canvas.drawBitmap(this.mScreenBitmap, (Rect) null, this.mShowRect, this.mPaint);
                canvas.restore();
            }
            if (!this.mBitmapShown) {
                this.mBitmapShown = true;
                this.mGestureStubListenerWrapper.onGestureStart();
            }
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            MotionEvent access$2100 = NavStubView.this.mDownEvent;
            switch (message.what) {
                case 255:
                    if (!NavStubView.this.mIsGestureStarted) {
                        if (NavStubView.DEBUG) {
                            Log.d(NavStubView.TAG, "handleMessage MSG_SET_GESTURE_STUB_UNTOUCHABLE");
                        }
                        NavStubView.this.disableTouch(true);
                        sendMessageDelayed(obtainMessage(260), 20);
                        boolean unused = NavStubView.this.mPendingResetStatus = true;
                        sendMessageDelayed(obtainMessage(257), 500);
                        return;
                    }
                    return;
                case 256:
                    if (access$2100 != null && !NavStubView.this.mIsGestureStarted) {
                        float access$1200 = NavStubView.this.mCurrX - access$2100.getRawX();
                        float access$1300 = NavStubView.this.mCurrY - access$2100.getRawY();
                        if (NavStubView.DEBUG) {
                            String str = NavStubView.TAG;
                            Log.d(str, "handleMessage MSG_CHECK_GESTURE_STUB_TOUCHABLE diffX: " + access$1200 + " diffY: " + access$1300 + " mDownX: " + access$2100.getRawX() + " mDownY: " + access$2100.getRawY());
                        }
                        if (Math.abs(access$1200) <= 30.0f && Math.abs(access$1300) <= 30.0f) {
                            NavStubView.this.mHandler.removeMessages(255);
                            NavStubView.this.mHandler.sendMessage(NavStubView.this.mHandler.obtainMessage(255));
                            return;
                        }
                        return;
                    }
                    return;
                case 257:
                    boolean unused2 = NavStubView.this.mPendingResetStatus = false;
                    NavStubView.this.disableTouch(false);
                    if (NavStubView.DEBUG) {
                        Log.d(NavStubView.TAG, "handleMessage MSG_RESET_GESTURE_STUB_TOUCHABLE");
                        return;
                    }
                    return;
                case 258:
                    MotionEvent motionEvent = (MotionEvent) message.obj;
                    NavStubView.this.onPointerEvent(motionEvent);
                    motionEvent.recycle();
                    return;
                case 260:
                    if (NavStubView.this.mCurrAction == 2 || NavStubView.this.mCurrAction == 0) {
                        NavStubView.this.injectMotionEvent(0);
                    } else {
                        NavStubView.this.injectMotionEvent(0);
                        NavStubView.this.injectMotionEvent(1);
                    }
                    if (NavStubView.this.mDownEvent != null) {
                        NavStubView.this.mDownEvent.recycle();
                        MotionEvent unused3 = NavStubView.this.mDownEvent = null;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void disableTouch(boolean z) {
        Log.d(TAG, "distouch : " + z);
        this.mDisableTouch = z;
        if (isAttachedToWindow()) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
            if (z) {
                layoutParams.flags |= 16;
            } else {
                layoutParams.flags &= -17;
            }
            this.mWindowManager.updateViewLayout(this, layoutParams);
        }
    }

    public void setVisibility(int i) {
        boolean z = i != 0;
        this.mKeepHidden = z;
        if (z) {
            super.setVisibility(8);
            MotionEvent motionEvent = this.mDownEvent;
            if (motionEvent != null) {
                motionEvent.recycle();
                this.mDownEvent = null;
                return;
            }
            return;
        }
        super.setVisibility(0);
    }

    /* access modifiers changed from: private */
    public void injectMotionEvent(int i) {
        MotionEvent motionEvent = this.mDownEvent;
        if (motionEvent != null) {
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "injectMotionEvent action :" + i + " downX: " + motionEvent.getRawX() + " downY: " + motionEvent.getRawY() + " flags:" + motionEvent.getFlags());
            } else {
                int i2 = i;
            }
            if ((motionEvent.getFlags() & 65536) == 0) {
                MotionEvent.PointerProperties[] createArray = MotionEvent.PointerProperties.createArray(1);
                motionEvent.getPointerProperties(0, createArray[0]);
                MotionEvent.PointerCoords[] createArray2 = MotionEvent.PointerCoords.createArray(1);
                motionEvent.getPointerCoords(0, createArray2[0]);
                createArray2[0].x = motionEvent.getRawX();
                createArray2[0].y = motionEvent.getRawY();
                InputManager.getInstance().injectInputEvent(MotionEvent.obtain(motionEvent.getDownTime(), motionEvent.getEventTime(), i, 1, createArray, createArray2, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags()), 0);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004b, code lost:
        if (r1 != 3) goto L_0x01b2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r14) {
        /*
            r13 = this;
            java.lang.String r0 = TAG
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x0030
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onTouchEvent:"
            r1.append(r2)
            float r2 = r14.getRawX()
            r1.append(r2)
            java.lang.String r2 = " "
            r1.append(r2)
            float r3 = r14.getRawY()
            r1.append(r3)
            r1.append(r2)
            r1.append(r14)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
        L_0x0030:
            boolean r1 = r13.mDisableTouch
            r2 = 0
            if (r1 == 0) goto L_0x0036
            return r2
        L_0x0036:
            int r1 = r14.getAction()
            r13.mCurrAction = r1
            r3 = 258(0x102, float:3.62E-43)
            r4 = 300(0x12c, double:1.48E-321)
            r6 = 3
            r7 = 1
            if (r1 == 0) goto L_0x0171
            r8 = 255(0xff, float:3.57E-43)
            if (r1 == r7) goto L_0x00e2
            r9 = 2
            if (r1 == r9) goto L_0x004f
            if (r1 == r6) goto L_0x00e2
            goto L_0x01b2
        L_0x004f:
            float r1 = r14.getRawX()
            r13.mCurrX = r1
            float r1 = r14.getRawY()
            r13.mCurrY = r1
            r4 = 1073741824(0x40000000, float:2.0)
            float r5 = r13.mInitY
            float r1 = r1 - r5
            float r1 = java.lang.Math.abs(r1)
            float r1 = r1 * r4
            float r4 = r13.mCurrX
            float r5 = r13.mInitX
            float r4 = r4 - r5
            float r4 = java.lang.Math.abs(r4)
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 >= 0) goto L_0x009e
            float r1 = r13.mCurrX
            float r4 = r13.mInitX
            float r1 = r1 - r4
            float r1 = java.lang.Math.abs(r1)
            int r4 = r13.mTouchSlop
            float r4 = (float) r4
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 <= 0) goto L_0x009e
            boolean r1 = r13.mIsGestureStarted
            if (r1 != 0) goto L_0x009e
            boolean r1 = r13.mPendingResetStatus
            if (r1 != 0) goto L_0x009e
            com.android.systemui.statusbar.phone.NavStubView$H r1 = r13.mHandler
            android.os.Message r4 = r1.obtainMessage(r8)
            r1.sendMessage(r4)
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x01b2
            java.lang.String r1 = "h-slide detected, sendMessage MSG_SET_GESTURE_STUB_UNTOUCHABLE"
            android.util.Log.d(r0, r1)
            goto L_0x01b2
        L_0x009e:
            boolean r0 = r13.mPendingResetStatus
            if (r0 != 0) goto L_0x01b2
            float r0 = r13.mInitY
            float r1 = r13.mCurrY
            float r0 = r0 - r1
            int r1 = r13.mTouchSlop
            float r1 = (float) r1
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 <= 0) goto L_0x01b2
            boolean r0 = r13.mIsGestureStarted
            if (r0 != 0) goto L_0x01b2
            r13.mIsGestureStarted = r7
            r13.exitFreeFormWindowIfNeeded()
            r13.clearMessages()
            android.view.MotionEvent r0 = r13.mDownEvent
            if (r0 == 0) goto L_0x00dd
            android.content.res.Resources r0 = r13.getResources()
            android.content.res.Configuration r0 = r0.getConfiguration()
            int r0 = r0.orientation
            if (r0 != r7) goto L_0x00dd
            android.view.MotionEvent r0 = r13.mDownEvent
            android.view.MotionEvent r0 = r0.copy()
            com.android.systemui.statusbar.phone.NavStubView$H r1 = r13.mHandler
            android.os.Message r0 = r1.obtainMessage(r3, r0)
            com.android.systemui.statusbar.phone.NavStubView$H r1 = r13.mHandler
            r1.sendMessage(r0)
            goto L_0x01b2
        L_0x00dd:
            r14.setAction(r2)
            goto L_0x01b2
        L_0x00e2:
            android.view.MotionEvent r1 = r13.mDownEvent
            if (r1 != 0) goto L_0x00e7
            return r7
        L_0x00e7:
            float r9 = r14.getRawX()
            r13.mCurrX = r9
            float r9 = r14.getRawY()
            r13.mCurrY = r9
            long r9 = r14.getEventTime()
            long r11 = r1.getEventTime()
            long r9 = r9 - r11
            int r4 = (r9 > r4 ? 1 : (r9 == r4 ? 0 : -1))
            if (r4 >= 0) goto L_0x0154
            boolean r4 = r13.mIsGestureStarted
            if (r4 != 0) goto L_0x0154
            r13.clearMessages()
            float r4 = r13.mCurrX
            float r5 = r1.getRawX()
            float r4 = r4 - r5
            float r5 = r13.mCurrY
            float r1 = r1.getRawY()
            float r5 = r5 - r1
            boolean r1 = r13.mIsGestureStarted
            if (r1 != 0) goto L_0x0154
            float r1 = java.lang.Math.abs(r4)
            r9 = 1106247680(0x41f00000, float:30.0)
            int r1 = (r1 > r9 ? 1 : (r1 == r9 ? 0 : -1))
            if (r1 > 0) goto L_0x0154
            float r1 = java.lang.Math.abs(r5)
            int r1 = (r1 > r9 ? 1 : (r1 == r9 ? 0 : -1))
            if (r1 > 0) goto L_0x0154
            com.android.systemui.statusbar.phone.NavStubView$H r1 = r13.mHandler
            android.os.Message r8 = r1.obtainMessage(r8)
            r1.sendMessage(r8)
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x0154
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r8 = "currTime - mDownTime < MSG_CHECK_GESTURE_STUB_TOUCHABLE_TIMEOUT updateViewLayout UnTouchable, diffX:"
            r1.append(r8)
            r1.append(r4)
            java.lang.String r4 = " diffY:"
            r1.append(r4)
            r1.append(r5)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
        L_0x0154:
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x016e
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "ACTION_UP: mIsGestureStarted: "
            r1.append(r4)
            boolean r4 = r13.mIsGestureStarted
            r1.append(r4)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
        L_0x016e:
            r13.mIsGestureStarted = r2
            goto L_0x01b2
        L_0x0171:
            float r1 = r14.getRawX()
            r13.mInitX = r1
            float r1 = r14.getRawY()
            r13.mInitY = r1
            float r1 = r14.getRawX()
            r13.mCurrX = r1
            float r1 = r14.getRawY()
            r13.mCurrY = r1
            android.view.MotionEvent r1 = r13.mDownEvent
            if (r1 == 0) goto L_0x0193
            r1.recycle()
            r1 = 0
            r13.mDownEvent = r1
        L_0x0193:
            android.view.MotionEvent r1 = r14.copy()
            r13.mDownEvent = r1
            com.android.systemui.statusbar.phone.NavStubView$H r1 = r13.mHandler
            r8 = 256(0x100, float:3.59E-43)
            r1.removeMessages(r8)
            com.android.systemui.statusbar.phone.NavStubView$H r1 = r13.mHandler
            android.os.Message r8 = r1.obtainMessage(r8)
            r1.sendMessageDelayed(r8, r4)
            boolean r1 = DEBUG
            if (r1 == 0) goto L_0x01b2
            java.lang.String r1 = "onTouch ACTION_DOWN sendMessageDelayed MSG_CHECK_GESTURE_STUB_TOUCHABLE"
            android.util.Log.d(r0, r1)
        L_0x01b2:
            boolean r0 = r13.mPendingResetStatus
            if (r0 != 0) goto L_0x01d0
            boolean r0 = r13.mIsGestureStarted
            if (r0 != 0) goto L_0x01c0
            int r0 = r13.mCurrAction
            if (r0 == r7) goto L_0x01c0
            if (r0 != r6) goto L_0x01d0
        L_0x01c0:
            android.view.MotionEvent r14 = r14.copy()
            com.android.systemui.statusbar.phone.NavStubView$H r0 = r13.mHandler
            android.os.Message r14 = r0.obtainMessage(r3, r14)
            com.android.systemui.statusbar.phone.NavStubView$H r13 = r13.mHandler
            r13.sendMessage(r14)
            return r7
        L_0x01d0:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavStubView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void clearMessages() {
        this.mHandler.removeMessages(256);
        this.mHandler.removeMessages(255);
        this.mHandler.removeMessages(260);
    }

    public boolean onPointerEvent(MotionEvent motionEvent) {
        AntiMistakeTouchView antiMistakeTouchView2;
        AntiMistakeTouchView antiMistakeTouchView3;
        String str = TAG;
        if (DEBUG) {
            Log.d(str, "onPointEvent:" + motionEvent.getRawX() + " " + motionEvent.getRawY() + " " + motionEvent);
        }
        if (this.mIsInFsMode) {
            return false;
        }
        if (motionEvent.getAction() == 0 && isMistakeTouch()) {
            if (!this.mSupportAntiMistake || ((antiMistakeTouchView2 = this.antiMistakeTouchView) != null && antiMistakeTouchView2.containsLocation(motionEvent.getRawX()))) {
                if (SystemClock.uptimeMillis() - this.mLastTouchTime > 2000) {
                    Toast makeText = Toast.makeText(this.mContext, getResources().getString(R.string.please_slide_agian), 0);
                    if (makeText.getWindowParams() != null) {
                        makeText.getWindowParams().privateFlags |= 16;
                    }
                    makeText.show();
                    AntiMistakeTouchView antiMistakeTouchView4 = this.antiMistakeTouchView;
                    if (antiMistakeTouchView4 != null) {
                        antiMistakeTouchView4.slideUp();
                    }
                    this.mLastTouchTime = SystemClock.uptimeMillis();
                    return false;
                }
            } else if (this.mSupportAntiMistake && (antiMistakeTouchView3 = this.antiMistakeTouchView) != null) {
                antiMistakeTouchView3.slideUp();
            }
        }
        if (motionEvent.getAction() == 0) {
            this.mDownNo++;
        }
        if (this.mDownNo == this.mLastDownNo) {
            return false;
        }
        if (1 == motionEvent.getAction()) {
            this.mLastDownNo = this.mDownNo;
        }
        if (motionEvent.getAction() == 0) {
            int max = Math.max(this.mScreenHeight, this.mScreenWidth);
            int min = Math.min(this.mScreenHeight, this.mScreenWidth);
            if (getResources().getConfiguration().orientation == 2) {
                this.mScreenHeight = min;
                this.mScreenWidth = max;
            } else {
                this.mScreenHeight = max;
                this.mScreenWidth = min;
            }
        }
        if (motionEvent.getAction() == 0) {
            this.mIsFullScreenModeCurTime = this.mIsFullScreenMode;
            this.mDownTime = SystemClock.uptimeMillis();
            this.mOrientationChangedAfterDown = false;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            boolean isRecentsActivityVisible = Recents.getSystemServices().isRecentsActivityVisible(mutableBoolean);
            if (this.mStatusBar == null) {
                this.mStatusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
            }
            StatusBar statusBar = this.mStatusBar;
            if (statusBar != null && statusBar.isKeyguardShowing()) {
                this.mWindowMode = 4;
            } else if (canTriggerAssistantAction(motionEvent)) {
                this.mWindowMode = 6;
            } else if (isRecentsActivityVisible) {
                this.mWindowMode = 3;
            } else if (mutableBoolean.value) {
                this.mWindowMode = 1;
            } else {
                this.mWindowMode = 2;
            }
            StatusBar statusBar2 = this.mStatusBar;
            if (statusBar2 == null) {
                this.mStatusBarManager.collapsePanels();
            } else if (statusBar2.mExpandedVisible) {
                statusBar2.animateCollapsePanels(0);
            }
        }
        Log.d(str, "current window mode:" + this.mWindowMode + " (1:home, 2:app, 3:recent-task, 4:keyguard)");
        if (1 == motionEvent.getAction()) {
            this.mIsInFsMode = true;
        }
        int i = this.mWindowMode;
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    recentsTouchResolution(motionEvent);
                } else if (i == 4) {
                    keyguardTouchResolution(motionEvent);
                } else if (i != 6) {
                    this.mIsInFsMode = false;
                } else {
                    assistantTouchResolution(motionEvent);
                }
            } else if (!this.mIsFsgVersionTwo) {
                appTouchResolution(motionEvent);
            } else {
                appTouchResolutionForVersionTwo(motionEvent);
            }
        } else if (!this.mIsSuperPowerMode) {
            homeTouchResolution(motionEvent);
        } else if (1 == motionEvent.getAction() || 3 == motionEvent.getAction()) {
            finalization(false, true, true, "actionUpResolution-5");
        }
        return false;
    }

    private void appTouchResolutionForVersionTwo(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            updateStateMode(65537);
        } else if (motionEvent.getAction() == 2) {
            if (this.mStateMode == 65537) {
                updateStateMode(65538);
            }
            RecentsEventBus.getDefault().send(new FsGestureMoveEvent(this.mCurrX, this.mCurrY));
        } else if (motionEvent.getAction() == 1) {
            finalization(false, false, false, "appTouchResolution2");
        }
        this.mIsInFsMode = false;
    }

    public float getCurrentX() {
        return this.mCurrX;
    }

    public float getCurrentY() {
        return this.mCurrY;
    }

    /* access modifiers changed from: private */
    public boolean isMistakeTouch() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "show_mistake_touch_toast", 1) != 0 && !this.mRecentVisible && getResources().getConfiguration().orientation == 2 && this.mIsFullScreenMode;
    }

    private void appTouchResolution(MotionEvent motionEvent) {
        int i;
        int i2;
        Bitmap bitmap;
        String str = TAG;
        this.mCurrentY = motionEvent.getRawY();
        int action = motionEvent.getAction();
        if (action == 0) {
            Log.d(str, "======>>>>down: " + SystemClock.uptimeMillis());
            initValue(motionEvent);
            try {
                this.mCurTask = ActivityManagerCompat.getRecentTasksForUser(this.mAm, 1, 127, -2).get(0);
            } catch (Exception e) {
                Log.e(str, "Failed to get recent tasks", e);
            }
            this.jobExecutor.execute(new Runnable() {
                public void run() {
                    NavStubView.this.mGestureStubListenerWrapper.onGestureReady();
                }
            });
            try {
                Bitmap screenshot = ScreenshotUtils.getScreenshot(this.mContext.getApplicationContext(), 1.0f, 0, MiuiWindowManager.getLayer(this.mContext, b.m) - 1, true);
                this.mScreenBitmap = screenshot;
                if (screenshot != null) {
                    this.mScreenBitmap = screenshot.copy(Bitmap.Config.ARGB_8888, false);
                    screenshot.recycle();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (this.mScreenBitmap == null) {
                Bitmap createBitmap = Bitmap.createBitmap(this.mScreenWidth, this.mScreenHeight, Bitmap.Config.ARGB_8888);
                this.mScreenBitmap = createBitmap;
                createBitmap.eraseColor(Color.parseColor("#00000000"));
            }
            if (this.mIsMultiWindow) {
                try {
                    ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(3, 3, 0);
                    if (this.mScreenHeight > this.mScreenWidth) {
                        bitmap = Bitmap.createBitmap(this.mScreenBitmap, 0, stackInfo.bounds.bottom + this.mDividerSize, this.mScreenBitmap.getWidth(), (this.mScreenHeight - stackInfo.bounds.bottom) - this.mDividerSize);
                    } else {
                        bitmap = Bitmap.createBitmap(this.mScreenBitmap, stackInfo.bounds.right + this.mDividerSize, 0, (this.mScreenWidth - stackInfo.bounds.right) - this.mDividerSize, this.mScreenHeight);
                    }
                    if (this.mScreenHeight < this.mScreenWidth) {
                        int i3 = (stackInfo.bounds.right + this.mDividerSize) / 2;
                        this.mMultiDelta = i3;
                        this.mDelta += (float) i3;
                    }
                    this.mScreenBitmap.recycle();
                    this.mScreenBitmap = bitmap;
                    this.targetBgAlpha = 0;
                } catch (Exception e3) {
                    e3.printStackTrace();
                }
            }
            boolean z = MiuiSettings.Global.getBoolean(getContext().getContentResolver(), "force_black");
            if (!this.mIsMultiWindow && Constants.IS_NOTCH && z && (i = this.mScreenHeight) > (i2 = this.mScreenWidth)) {
                try {
                    Bitmap createBitmap2 = Bitmap.createBitmap(this.mScreenBitmap, 0, this.mStatusBarHeight, i2, i - this.mStatusBarHeight);
                    this.mScreenBitmap.recycle();
                    this.mScreenBitmap = createBitmap2;
                    this.mIsAlreadyCropStatusBar = true;
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
            }
            Bitmap createRoundCornerBmp = createRoundCornerBmp(this.mScreenBitmap);
            this.mScreenBitmap = createRoundCornerBmp;
            this.mScreenBmpWidth = createRoundCornerBmp.getWidth();
            this.mScreenBmpHeight = this.mScreenBitmap.getHeight();
            this.mScreenBitmap.setHasAlpha(false);
            this.mScreenBitmap.prepareToDraw();
        } else if (action == 1) {
            actionUpResolution();
        } else if (action == 2) {
            actionMoveResolution(motionEvent);
            this.mCurScale = 1.0f - (linearToCubic(this.mCurrentY, (float) this.mScreenHeight, 0.0f, 3.0f) * 0.385f);
            invalidate();
            Log.d(str, "=======>>>>>move: " + SystemClock.uptimeMillis());
        } else if (action == 3) {
            this.mGestureStubListenerWrapper.onGestureFinish(false);
            if (this.mOrientationChangedAfterDown) {
                startAppAnimation(3);
            } else {
                finalization(false, true, true, "appTouchResolution");
            }
        }
    }

    private void assistantTouchResolution(MotionEvent motionEvent) {
        this.mCurrentX = motionEvent.getRawX();
        this.mCurrentY = motionEvent.getRawY();
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    writeSettingMove(motionEvent);
                    updateAssistantProgress();
                    return;
                } else if (action != 3) {
                    return;
                }
            }
            if (!this.mLaunchedAssistant) {
                ValueAnimator duration = ValueAnimator.ofFloat(new float[]{this.mAssistLastProgress, 0.0f}).setDuration((long) (this.mAssistLastProgress * 300.0f));
                duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        NavStubView.this.mAssistManager.onInvocationProgress(1, ((Float) valueAnimator.getAnimatedValue()).floatValue());
                    }
                });
                duration.setInterpolator(Interpolators.DECELERATE);
                duration.start();
            }
            finalization(false, false, false, "assistantTouchResolution");
            return;
        }
        initValue(motionEvent);
        this.mLaunchedAssistant = false;
    }

    private void updateAssistantProgress() {
        if (!this.mLaunchedAssistant) {
            float hypot = (((float) Math.hypot((double) (this.mDownY - this.mCurrentY), (double) (this.mDownX - this.mCurrentX))) * 1.0f) / this.mAssistDistantThreshold;
            float min = Math.min(Math.max(0.0f, hypot), 1.0f);
            this.mAssistLastProgress = min;
            if (min == 1.0f) {
                this.mAssistManager.onAssistantGestureCompletion();
                Bundle bundle = new Bundle();
                bundle.putInt("triggered_by", 83);
                bundle.putInt("invocation_type", 1);
                this.mAssistManager.startAssist(bundle);
                this.mLaunchedAssistant = true;
                return;
            }
            this.mAssistManager.onInvocationProgress(1, hypot);
        }
    }

    public void setIsAssistantAvailable(boolean z) {
        this.mIsAssistantAvailable = z;
    }

    private boolean canTriggerAssistantAction(MotionEvent motionEvent) {
        return this.mIsAssistantAvailable && Utilities.isAndroidQorNewer() && (motionEvent.getRawX() < ((float) this.mAssistantWidth) || motionEvent.getRawX() > ((float) (this.mScreenWidth - this.mAssistantWidth)));
    }

    private void homeTouchResolution(MotionEvent motionEvent) {
        this.mCurrentY = motionEvent.getRawY();
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    actionMoveResolution(motionEvent);
                    writeSettingMove(motionEvent);
                    this.mCurScale = 1.0f - (linearToCubic(this.mCurrentY, (float) this.mScreenHeight, 0.0f, 3.0f) * 0.15f);
                    if (this.mStateMode != 65539 && !this.mHomeFadeInAnim.isRunning()) {
                        Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, this.mCurScale);
                        return;
                    }
                    return;
                } else if (action != 3) {
                    return;
                }
            }
            actionUpResolution();
            return;
        }
        initValue(motionEvent);
        Recents.getSystemServices().notifyHomeModeFsGestureStart(Constants.HOME_LAUCNHER_PACKAGE_NAME);
    }

    private void writeSettingMove(MotionEvent motionEvent) {
        int rawY = (int) (motionEvent.getRawY() - this.mTouchDownY);
        if (!this.mIsWriteSettingMove && Math.abs(rawY) > ViewConfiguration.get(this.mContext).getScaledTouchSlop()) {
            Settings.System.putInt(this.mContext.getContentResolver(), "full_screen_gesture", 1);
            this.mIsWriteSettingMove = true;
        }
    }

    private void keyguardTouchResolution(MotionEvent motionEvent) {
        this.mCurrentY = motionEvent.getRawY();
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    actionCommonMoveResolution(motionEvent);
                    return;
                } else if (action != 3) {
                    return;
                }
            }
            if (this.mStateMode == 65538 && isFastPullUp()) {
                this.mContext.sendBroadcast(this.mCloseScreenshotIntent);
                if (this.mWindowMode == 4) {
                    sendEvent(0, 0, 3);
                    sendEvent(1, 0, 3);
                }
            }
            finalization(false, false, false, "keyguardTouchResolution");
            return;
        }
        initValue(motionEvent);
    }

    private void recentsTouchResolution(MotionEvent motionEvent) {
        this.mCurrentY = motionEvent.getRawY();
        int action = motionEvent.getAction();
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    actionCommonMoveResolution(motionEvent);
                    writeSettingMove(motionEvent);
                    this.mCurScale = 1.0f - (linearToCubic(this.mCurrentY, (float) this.mScreenHeight, 0.0f, 3.0f) * 0.15f);
                    if (this.mStateMode == 65538 && !this.mRecentsModeRecentsSlideInAnim.isRunning()) {
                        RecentsEventBus.getDefault().send(new FsGestureRecentsModeMoveEvent(this.mCurScale));
                        return;
                    }
                    return;
                } else if (action != 3) {
                    return;
                }
            }
            if (this.mStateMode == 65538) {
                if (isFastPullUp()) {
                    writeSettingUp(4);
                    this.mContext.sendBroadcast(this.mCloseScreenshotIntent);
                    RecentsPushEventHelper.sendHideRecentsEvent("homeGesture");
                    RecentsEventBus.getDefault().send(new HideRecentsEvent(false, true, false));
                } else {
                    writeSettingUp(2);
                    startRecentsResetAnim();
                }
            }
            if (this.mStateMode == 65540) {
                if (isFastPullDown()) {
                    writeSettingUp(2);
                    RecentsEventBus.getDefault().post(new FsGesturePreloadRecentsEvent());
                    RecentsEventBus.getDefault().send(new FsGestureEnterRecentsEvent());
                    startRecentsResetAnim();
                } else {
                    writeSettingUp(4);
                    Context context = this.mContext;
                    context.startActivityAsUser(this.mRecentsModeHomeIntent, ActivityOptions.makeCustomAnimation(context, 0, 0).toBundle(), UserHandle.CURRENT);
                    startHomeFadeInAnim();
                }
            }
            finalization(false, false, false, "recentsTouchResolution");
            return;
        }
        initValue(motionEvent);
    }

    private void actionCommonMoveResolution(MotionEvent motionEvent) {
        this.mPivotLocX = (int) (((motionEvent.getRawX() + this.mDownX) / 2.0f) + this.mDelta);
        int i = this.mScreenHeight;
        this.mPivotLocY = (int) (((float) i) - (linearToCubic(this.mCurrentY, (float) i, 0.0f, 3.0f) * 444.0f));
        if (this.mStateMode == 65537) {
            updateStateMode(65538);
            Log.d(TAG, "current state mode: StateMode.STATE_ON_DRAG");
            this.mFrameHandler.post(this.mTailCatcherTask);
            if (!this.mIsFsgVersionTwo) {
                updateViewLayout(-1);
            }
        }
    }

    private void startRecentsResetAnim() {
        if (this.mRecentsModeRecentsSlideInAnim.isRunning()) {
            this.mRecentsModeRecentsSlideInAnim.cancel();
        }
        RecentsEventBus.getDefault().send(new FsGestureRecentsModeResetEvent());
    }

    private void startHomeFadeInAnim() {
        if (this.mRecentsModeHomeFadeInAnim.isRunning()) {
            float floatValue = ((Float) this.mRecentsModeHomeFadeInAnim.getAnimatedValue()).floatValue();
            this.mRecentsModeHomeFadeInAnim.cancel();
            this.mRecentsModeHomeFadeInAnim.setFloatValues(new float[]{floatValue, 1.0f});
        } else {
            this.mRecentsModeHomeFadeInAnim.setFloatValues(new float[]{0.95f, 1.0f});
        }
        this.mRecentsModeHomeFadeInAnim.start();
    }

    public void sendEvent(int i, int i2, int i3) {
        sendEvent(i, i2, i3, SystemClock.uptimeMillis());
    }

    /* access modifiers changed from: package-private */
    public void sendEvent(int i, int i2, int i3, long j) {
        int i4 = i2;
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, j, i, i3, (i4 & 128) != 0 ? 1 : 0, 0, -1, 0, i4 | 8 | 64, 257), 0);
    }

    private void initValue(MotionEvent motionEvent) {
        this.mDownX = motionEvent.getRawX();
        this.mDownY = motionEvent.getRawY();
        this.mIsAppToHome = false;
        int i = this.mScreenWidth;
        this.mDelta = ((float) (i / 2)) - this.mDownX;
        int i2 = i / 2;
        this.mPivotLocX = i2;
        this.mFollowTailX = (float) i2;
        int i3 = this.mScreenHeight;
        this.mPivotLocY = i3;
        this.mFollowTailY = (float) i3;
        this.mTouchDownY = motionEvent.getRawY();
        this.mCurTask = null;
        updateStateMode(65537);
        Log.d(TAG, "current state mode: StateMode.STATE_INIT");
    }

    private Bitmap createRoundCornerBmp(Bitmap bitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        RectF rectF = new RectF(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight());
        int i = this.RADIUS_SIZE;
        canvas.drawRoundRect(rectF, (float) i, (float) i, this.mPaint);
        this.mPaint.setXfermode(this.mModeSrcIn);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, this.mPaint);
        this.mPaint.setXfermode((Xfermode) null);
        return createBitmap;
    }

    private void actionMoveResolution(MotionEvent motionEvent) {
        if (this.mStateMode == 65537) {
            if (this.mWindowMode == 2) {
                setBackgroundColor(Color.argb(this.targetBgAlpha, 0, 0, 0));
            }
            if (this.mWindowMode == 2) {
                this.mGestureStubListenerWrapper.skipAppTransition();
            }
        }
        if (this.mStateMode == 65539) {
            RecentsEventBus.getDefault().send(new FsGestureMoveEvent(this.mCurrX, this.mCurrY));
        }
        actionCommonMoveResolution(motionEvent);
    }

    private class GestureStubListenerWrapper {
        private Method getGestureStubListenerMethod;
        IGestureStubListener mListener = getGestureStubListener();

        public GestureStubListenerWrapper() {
        }

        public void onGestureReady() {
            try {
                if (this.mListener != null) {
                    this.mListener.onGestureReady();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onGestureStart() {
            try {
                if (this.mListener != null) {
                    this.mListener.onGestureStart();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onGestureFinish(boolean z) {
            try {
                if (this.mListener != null) {
                    this.mListener.onGestureFinish(z);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void skipAppTransition() {
            try {
                if (this.mListener != null) {
                    this.mListener.skipAppTransition();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private IGestureStubListener getGestureStubListener() {
            try {
                if (this.getGestureStubListenerMethod == null) {
                    this.getGestureStubListenerMethod = WindowManagerGlobal.getWindowManagerService().getClass().getMethod("getGestureStubListener", new Class[0]);
                }
                if (this.getGestureStubListenerMethod != null) {
                    return (IGestureStubListener) this.getGestureStubListenerMethod.invoke(WindowManagerGlobal.getWindowManagerService(), new Object[0]);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private void actionUpResolution() {
        this.mFrameHandler.removeCallbacksAndMessages((Object) null);
        boolean z = this.mStateMode == 65538;
        boolean z2 = this.mStateMode == 65539;
        if (!z && !z2) {
            finalization(false, true, true, "actionUpResolution-5");
        } else if (isFastPullDown()) {
            if (z2) {
                RecentsEventBus.getDefault().send(new FsGestureSlideOutEvent());
            }
            int i = this.mWindowMode;
            if (i == 2) {
                startAppAnimation(1);
            } else if (i == 1) {
                startHomeAnimation(1);
            } else {
                finalization(false, true, true, "actionUpResolution-1");
            }
        } else if (isFastPullUp()) {
            if (z2) {
                RecentsEventBus.getDefault().send(new FsGestureSlideOutEvent());
            }
            this.mContext.sendBroadcast(this.mCloseScreenshotIntent);
            int i2 = this.mWindowMode;
            if (i2 == 2) {
                startAppAnimation(2);
            } else if (i2 == 1) {
                startHomeAnimation(2);
            } else {
                finalization(false, true, true, "actionUpResolution-2");
            }
        } else if (z) {
            int i3 = this.mWindowMode;
            if (i3 == 2) {
                startAppAnimation(1);
            } else if (i3 == 1) {
                startHomeAnimation(1);
            } else {
                finalization(false, true, true, "actionUpResolution-3");
            }
        } else {
            this.mContext.sendBroadcast(this.mCloseScreenshotIntent);
            int i4 = this.mWindowMode;
            if (i4 == 2) {
                startAppAnimation(3);
            } else if (i4 == 1) {
                startHomeAnimation(3);
            } else {
                finalization(false, true, true, "actionUpResolution-4");
            }
        }
    }

    private void writeSettingUp(int i) {
        if (this.mIsWriteSettingMove) {
            Settings.System.putInt(this.mContext.getContentResolver(), "full_screen_gesture", i);
            this.mIsWriteSettingMove = false;
        }
    }

    private boolean isFastPullUp() {
        return ((float) this.mPivotLocY) - this.mFollowTailY < -40.0f;
    }

    private boolean isFastPullDown() {
        return ((float) this.mPivotLocY) - this.mFollowTailY > 40.0f;
    }

    private void startHomeAnimation(final int i) {
        if (i == 1 || i == 2) {
            writeSettingUp(2);
        }
        if (this.mStateMode == 65539 && (i == 1 || i == 2)) {
            this.mHomeFadeInAnim.start();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mCurScale, 1.0f});
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = NavStubView.this.mCurScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float access$1400 = NavStubView.this.mCurScale;
                int i = i;
                if ((i == 1 || i == 2) && !NavStubView.this.mHomeFadeInAnim.isRunning()) {
                    Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, access$1400);
                }
            }
        });
        long j = (long) DEFAULT_ANIM_TIME;
        if (i == 2) {
            j = 200;
        }
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                if (i == 2) {
                    NavStubView.this.mHomeIntent.putExtra("ignore_bring_to_front", true);
                    NavStubView.this.mHomeIntent.putExtra("filter_flag", true);
                    NavStubView.this.mContext.startActivityAsUser(NavStubView.this.mHomeIntent, ActivityOptions.makeCustomAnimation(NavStubView.this.mContext, 0, 0).toBundle(), UserHandle.CURRENT);
                    if (NavStubView.this.mIsEnterRecents) {
                        boolean unused = NavStubView.this.mIsSkipResetLauncherAlpha = true;
                    }
                }
            }

            public void onAnimationEnd(Animator animator) {
                long j;
                int i = i;
                if (i == 1) {
                    NavStubView.this.mContext.startActivityAsUser(NavStubView.this.mHomeIntent, ActivityOptions.makeCustomAnimation(NavStubView.this.mContext, 0, 0).toBundle(), UserHandle.CURRENT);
                    if (NavStubView.this.mIsEnterRecents) {
                        boolean unused = NavStubView.this.mIsSkipResetLauncherAlpha = true;
                    }
                } else if (i == 3) {
                    j = 50;
                    NavStubView.this.mFrameHandler.postDelayed(new Runnable() {
                        public void run() {
                            NavStubView.this.finalization(false, false, true, "startHomeAnimation");
                        }
                    }, j);
                }
                j = 100;
                NavStubView.this.mFrameHandler.postDelayed(new Runnable() {
                    public void run() {
                        NavStubView.this.finalization(false, false, true, "startHomeAnimation");
                    }
                }, j);
            }
        });
        ofFloat.setDuration(j).start();
        if (i == 3) {
            writeSettingUp(3);
            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsZoomEvent(0, new Runnable() {
                public void run() {
                    RecentsEventBus.getDefault().send(new FsGestureEnterRecentsCompleteEvent());
                }
            }));
        }
    }

    private void exitFreeFormWindowIfNeeded() {
        MiuiMultiWindowUtils.exitFreeFormWindowIfNeeded();
    }

    private void startAppAnimation(int i) {
        ValueAnimator valueAnimator;
        final int i2 = i;
        String str = "home";
        if (i2 == 2) {
            if (Constants.IS_TABLET || getResources().getConfiguration().orientation == 1) {
                this.mIsAppToHome = false;
                IFsGestureCallback iFsGestureCallback = Recents.getSystemServices().mIFsGestureCallbackMap.get(Constants.HOME_LAUCNHER_PACKAGE_NAME);
                this.mHomeCallback = iFsGestureCallback;
                TransitionAnimationSpec transitionAnimationSpec = null;
                if (iFsGestureCallback != null) {
                    try {
                        transitionAnimationSpec = iFsGestureCallback.getSpec(this.mCurTask.baseIntent.getComponent().getPackageName() + "/", this.mCurTask.userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                this.mIsAppToHome = (transitionAnimationSpec == null || transitionAnimationSpec.mRect == null || transitionAnimationSpec.mRect.top == 0 || transitionAnimationSpec.mRect.right == 0 || transitionAnimationSpec.mBitmap == null) ? false : true;
                if (this.mIsAppToHome) {
                    this.mGestureStubListenerWrapper.skipAppTransition();
                    Rect rect = transitionAnimationSpec.mRect;
                    final int i3 = (rect.bottom + rect.top) / 2;
                    final int i4 = (rect.right + rect.left) / 2;
                    Bitmap bitmap = transitionAnimationSpec.mBitmap;
                    float f = this.mCurScale;
                    int i5 = this.mPivotLocX;
                    int i6 = (int) (((float) this.mPivotLocY) - ((((float) this.mScreenBmpHeight) * f) / 2.0f));
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationStart(Animator animator) {
                            Recents.getSystemServices().setIsFsGestureAnimating(true);
                            NavStubView.this.mContext.startActivityAsUser(NavStubView.this.mHomeIntent, ActivityOptions.makeCustomAnimation(NavStubView.this.mContext, 0, 0).toBundle(), UserHandle.CURRENT);
                            try {
                                NavStubView.this.mHomeCallback.notifyMiuiAnimationStart();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        public void onAnimationEnd(Animator animator) {
                            NavStubView.this.finalization(false, false, true, "startAppAnimation-1");
                            Recents.getSystemServices().setIsFsGestureAnimating(false);
                            NavStubJankyFrameReporter.recordJankyFrames("home");
                        }
                    });
                    PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat("homeScale", new float[]{0.8f, 1.0f});
                    PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat("homeAlpha", new float[]{0.0f, 1.0f});
                    PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat("xScale", new float[]{this.mCurScale, 0.03f});
                    ValueAnimator ofPropertyValuesHolder = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("yScale", new float[]{this.mCurScale, 0.03f}), PropertyValuesHolder.ofInt("yPivot", new int[]{i6, i3}), ofFloat3, PropertyValuesHolder.ofInt("xPivot", new int[]{i5, i4}), ofFloat2, ofFloat});
                    ofPropertyValuesHolder.setInterpolator(this.mQuartEaseOutInterpolator);
                    ofPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            NavStubJankyFrameReporter.caculateAnimationFrameInterval("home");
                            float unused = NavStubView.this.mYScale = ((Float) valueAnimator.getAnimatedValue("yScale")).floatValue();
                            float unused2 = NavStubView.this.mXScale = ((Float) valueAnimator.getAnimatedValue("xScale")).floatValue();
                            int unused3 = NavStubView.this.mPivotLocY = ((Integer) valueAnimator.getAnimatedValue("yPivot")).intValue();
                            int unused4 = NavStubView.this.mPivotLocX = ((Integer) valueAnimator.getAnimatedValue("xPivot")).intValue();
                            float floatValue = NavStubView.IS_E10 ? 1.0f : ((Float) valueAnimator.getAnimatedValue("homeScale")).floatValue();
                            if (valueAnimator.getAnimatedFraction() > 0.75f) {
                                boolean unused5 = NavStubView.this.mIsBgIconVisible = true;
                            }
                            Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, ((Float) valueAnimator.getAnimatedValue("homeAlpha")).floatValue(), floatValue, i4, i3, NavStubView.this.mPivotLocX, NavStubView.this.mPivotLocY, NavStubView.this.mIsBgIconVisible);
                            NavStubView.this.invalidate();
                        }
                    });
                    ofPropertyValuesHolder.setDuration((long) DEFAULT_ANIM_TIME);
                    ValueAnimator ofFloat4 = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
                    ofFloat4.setInterpolator(this.mCubicEaseOutInterpolator);
                    ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            float unused = NavStubView.this.mCurAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        }
                    });
                    ofFloat4.setDuration((long) (DEFAULT_ANIM_TIME / 4));
                    ofFloat4.setStartDelay((long) (DEFAULT_ANIM_TIME / 3));
                    ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{this.targetBgAlpha, 0});
                    ofInt.setInterpolator(this.mCubicEaseOutInterpolator);
                    ofInt.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            try {
                                NavStubView.this.mHomeCallback.notifyMiuiAnimationEnd();
                                NavStubView.this.mGestureStubListenerWrapper.onGestureFinish(false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            NavStubView.this.setBackgroundColor(Color.argb(((Integer) valueAnimator.getAnimatedValue()).intValue(), 0, 0, 0));
                        }
                    });
                    ofInt.setDuration((long) DEFAULT_ANIM_TIME);
                    NavStubJankyFrameReporter.resetAnimationFrameIntervalParams(str);
                    animatorSet.playTogether(new Animator[]{ofPropertyValuesHolder, ofInt, ofFloat4});
                    animatorSet.start();
                    return;
                }
            } else {
                this.mGestureStubListenerWrapper.skipAppTransition();
                Context context = this.mContext;
                context.startActivityAsUser(this.mHomeIntent, ActivityOptions.makeCustomAnimation(context, 0, 0).toBundle(), UserHandle.CURRENT);
                finalization(false, true, true, "startAppAnimation-2");
                return;
            }
        }
        if (i2 == 1) {
            valueAnimator = ValueAnimator.ofFloat(new float[]{this.mCurScale, 1.0f});
            str = "cancel";
        } else if (i2 == 2) {
            valueAnimator = ValueAnimator.ofFloat(new float[]{this.mCurScale, 0.5f});
        } else if (i2 == 3) {
            this.mIsAppToRecents = true;
            float f2 = 0.0f;
            RectF rectF = this.mDestRectF;
            if (rectF != null) {
                f2 = rectF.width() / ((float) this.mScreenBmpWidth);
            }
            valueAnimator = ValueAnimator.ofFloat(new float[]{this.mCurScale, f2});
            str = "recents";
        } else {
            return;
        }
        final String str2 = str;
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        final int i7 = this.mPivotLocX;
        final int i8 = this.mPivotLocY;
        final float f3 = this.mCurScale;
        final int i9 = i;
        AnonymousClass18 r14 = r1;
        final String str3 = str2;
        AnonymousClass18 r1 = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float f;
                float unused = NavStubView.this.mCurScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float animatedFraction = valueAnimator.getAnimatedFraction();
                int i = i9;
                if (i != 1) {
                    int i2 = 0;
                    if (i == 2) {
                        NavStubView navStubView = NavStubView.this;
                        int unused2 = navStubView.mPivotLocX = (int) (((float) i7) + (((float) ((navStubView.mScreenBmpWidth / 2) - i7)) * animatedFraction));
                        NavStubView navStubView2 = NavStubView.this;
                        int unused3 = navStubView2.mPivotLocY = (int) (((float) i8) + (((float) (((navStubView2.mScreenBmpHeight * 3) / 4) - i8)) * animatedFraction));
                        NavStubView navStubView3 = NavStubView.this;
                        float f2 = 1.0f - animatedFraction;
                        navStubView3.setBackgroundColor(Color.argb((int) (((float) navStubView3.targetBgAlpha) * f2), 0, 0, 0));
                        float unused4 = NavStubView.this.mCurAlpha = f2;
                    } else if (i == 3) {
                        NavStubView navStubView4 = NavStubView.this;
                        navStubView4.setBackgroundColor(Color.argb((int) (((float) navStubView4.targetBgAlpha) * (1.0f - animatedFraction)), 0, 0, 0));
                        float f3 = 0.0f;
                        if (NavStubView.this.mDestRectF != null) {
                            f3 = (NavStubView.this.mDestRectF.left + NavStubView.this.mDestRectF.right) / 2.0f;
                            f = NavStubView.this.mDestRectF.bottom;
                        } else {
                            f = 0.0f;
                        }
                        NavStubView navStubView5 = NavStubView.this;
                        int i3 = i7;
                        int unused5 = navStubView5.mPivotLocX = (int) (((float) i3) + ((f3 - ((float) i3)) * animatedFraction));
                        NavStubView navStubView6 = NavStubView.this;
                        int i4 = i8;
                        int unused6 = navStubView6.mPivotLocY = (int) (((float) i4) + ((f - ((float) i4)) * animatedFraction));
                        NavStubView navStubView7 = NavStubView.this;
                        float unused7 = navStubView7.mScreenBmpScale = f3 + ((navStubView7.mDestRectHeightScale - f3) * animatedFraction);
                        NavStubView navStubView8 = NavStubView.this;
                        float unused8 = navStubView8.mDestTopOffset = (1.0f - navStubView8.mDestRectHeightScale) * animatedFraction;
                        NavStubView navStubView9 = NavStubView.this;
                        if (!navStubView9.mIsAlreadyCropStatusBar && !NavStubView.this.mIsFullScreenModeCurTime) {
                            i2 = (int) (((float) NavStubView.this.mStatusBarHeight) * animatedFraction);
                        }
                        int unused9 = navStubView9.mStatusBarDec = i2;
                    }
                } else {
                    int access$4900 = NavStubView.this.mScreenWidth / 2;
                    if (NavStubView.this.mIsMultiWindow && NavStubView.this.mScreenHeight < NavStubView.this.mScreenWidth) {
                        access$4900 += NavStubView.this.mMultiDelta;
                    }
                    NavStubView navStubView10 = NavStubView.this;
                    int i5 = i7;
                    int unused10 = navStubView10.mPivotLocX = (int) (((float) i5) + (((float) (access$4900 - i5)) * animatedFraction));
                    NavStubView navStubView11 = NavStubView.this;
                    int unused11 = navStubView11.mPivotLocY = (int) (((float) i8) + (((float) (navStubView11.mScreenHeight - i8)) * animatedFraction));
                }
                NavStubJankyFrameReporter.caculateAnimationFrameInterval(str3);
                NavStubView.this.invalidate();
            }
        };
        valueAnimator.addUpdateListener(r14);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                if (i2 == 2) {
                    NavStubView.this.mGestureStubListenerWrapper.skipAppTransition();
                    NavStubView.this.mContext.startActivityAsUser(NavStubView.this.mHomeIntent, ActivityOptions.makeCustomAnimation(NavStubView.this.mContext, 0, 0).toBundle(), UserHandle.CURRENT);
                }
            }

            public void onAnimationEnd(Animator animator) {
                int i = i2;
                final boolean z = true;
                long j = 0;
                if (i == 1) {
                    if (NavStubView.this.mCurTask != null) {
                        try {
                            NavStubView.this.mIam.startActivityFromRecents(NavStubView.this.mCurTask.persistentId, ActivityOptions.makeCustomAnimation(NavStubView.this.mContext, 0, 0).toBundle());
                        } catch (Exception e) {
                            Log.e(NavStubView.TAG, "Fail to start activity", e);
                            NavStubView.this.mContext.startActivityAsUser(NavStubView.this.mHomeIntent, ActivityOptions.makeCustomAnimation(NavStubView.this.mContext, 0, 0).toBundle(), UserHandle.CURRENT);
                        }
                    }
                    boolean unused = NavStubView.this.mCancelActionToStartApp = true;
                    j = NavStubView.this.getResources().getConfiguration().orientation == 2 ? 400 : 300;
                    NavStubView.this.mGestureStubListenerWrapper.onGestureFinish(true);
                } else if (i == 2) {
                    NavStubView.this.mGestureStubListenerWrapper.onGestureFinish(false);
                    z = false;
                } else if (i == 3) {
                    NavStubView.this.mGestureStubListenerWrapper.onGestureFinish(false);
                }
                NavStubView.this.mFrameHandler.postDelayed(new Runnable() {
                    public void run() {
                        NavStubView navStubView = NavStubView.this;
                        boolean z = z;
                        navStubView.finalization(false, z, true, "startAppAnimation-3-" + i2);
                    }
                }, j);
                NavStubJankyFrameReporter.recordJankyFrames(str2);
            }
        });
        NavStubJankyFrameReporter.resetAnimationFrameIntervalParams(str2);
        if (i2 == 2) {
            AnimatorSet animatorSet2 = new AnimatorSet();
            ValueAnimator ofPropertyValuesHolder2 = ValueAnimator.ofPropertyValuesHolder(new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat("homeAlpha", new float[]{0.0f, 1.0f}), PropertyValuesHolder.ofFloat("homeScale", new float[]{2.0f, 1.0f})});
            ofPropertyValuesHolder2.setInterpolator(this.mCubicEaseOutInterpolator);
            ofPropertyValuesHolder2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, ((Float) valueAnimator.getAnimatedValue("homeAlpha")).floatValue(), NavStubView.IS_E10 ? 1.0f : ((Float) valueAnimator.getAnimatedValue("homeScale")).floatValue());
                }
            });
            animatorSet2.playTogether(new Animator[]{valueAnimator, ofPropertyValuesHolder2});
            animatorSet2.setDuration(200).start();
        } else {
            valueAnimator.setDuration((long) DEFAULT_ANIM_TIME).start();
        }
        if (i2 == 3) {
            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsZoomEvent(0, new Runnable() {
                public void run() {
                    RecentsEventBus.getDefault().send(new FsGestureShowFirstCardEvent());
                    RecentsEventBus.getDefault().send(new AnimFirstTaskViewAlphaEvent(1.0f, false));
                    RecentsEventBus.getDefault().send(new FsGestureEnterRecentsCompleteEvent());
                }
            }));
        }
    }

    /* access modifiers changed from: private */
    public void finalization(boolean z, boolean z2, boolean z3, String str) {
        String str2 = TAG;
        Log.d(str2, "===>>>finalization executed from: " + str);
        this.mIsFullScreenModeCurTime = false;
        this.mIsAlreadyCropStatusBar = false;
        this.mIsBgIconVisible = false;
        this.mIsEnterRecents = false;
        this.mIsAppToRecents = false;
        this.mIsAppToHome = false;
        this.mCancelActionToStartApp = false;
        this.mDestRectF = null;
        this.mCurAlpha = 1.0f;
        this.targetBgAlpha = 136;
        this.mPaint.setAlpha(255);
        this.mHomeIntent.removeExtra("ignore_bring_to_front");
        this.mHomeIntent.removeExtra("filter_flag");
        updateStateMode(65537);
        Bitmap bitmap = this.mScreenBitmap;
        if (bitmap != null) {
            bitmap.recycle();
            this.mScreenBitmap = null;
            this.mBitmapShown = false;
        }
        setBackgroundColor(0);
        removeCallbacks(this.mUpdateViewLayoutRunnable);
        post(this.mUpdateViewLayoutRunnable);
        Handler handler = this.mFrameHandler;
        if (handler != null) {
            handler.removeCallbacksAndMessages((Object) null);
        }
        if (z2) {
            if (z) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.95f, 1.0f});
                ofFloat.setInterpolator(new DecelerateInterpolator());
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, ((Float) valueAnimator.getAnimatedValue()).floatValue());
                    }
                });
                ofFloat.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        Recents.getSystemServices().setIsFsGestureAnimating(false);
                    }
                });
                ofFloat.setDuration(200).start();
            } else {
                Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, 1.0f);
            }
        }
        this.mIsInFsMode = false;
    }

    private static class CubicEaseOutInterpolator implements Interpolator {
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return (f2 * f2 * f2) + 1.0f;
        }

        private CubicEaseOutInterpolator() {
        }
    }

    private static class QuartEaseOutInterpolator implements Interpolator {
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return -((((f2 * f2) * f2) * f2) - 1.0f);
        }

        private QuartEaseOutInterpolator() {
        }
    }

    private float linearToCubic(float f, float f2, float f3, float f4) {
        String str = TAG;
        if (f3 == f2) {
            Log.e(str, "linearToCubic error:end=" + f3 + "   orignal=" + f2);
            return 1.0f;
        }
        float pow = f4 != 0.0f ? (float) (1.0d - Math.pow((double) (1.0f - ((f - f2) / (f3 - f2))), (double) f4)) : 0.0f;
        if (pow > 1.0f || pow < 0.0f) {
            Log.e(str, "linearToCubic error:now=" + f + "   orignal=" + f2 + "   end=" + f3 + "   pow=" + f4);
        }
        return Math.max(0.0f, Math.min(pow, 1.0f));
    }

    public boolean gatherTransparentRegion(Region region) {
        if (!this.mNeedRender && region != null && !isMistakeTouch()) {
            int width = getWidth();
            int height = getHeight();
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "gatherTransparentRegion: need render w:" + width + "  h:" + height);
            }
            if (width > 0 && height > 0) {
                getLocationInWindow(this.mLocation);
                int[] iArr = this.mLocation;
                int i = iArr[0];
                int i2 = iArr[1];
                region.op(i, i2, i + width, i2 + height, Region.Op.UNION);
                return false;
            }
        }
        return super.gatherTransparentRegion(region);
    }

    /* access modifiers changed from: private */
    public void updateViewLayout(int i) {
        if (isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this, getWindowParam(i));
        }
        if (getParent() != null) {
            this.mNeedRender = i == -1;
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "need render:" + this.mNeedRender);
            }
            getParent().requestTransparentRegion(this);
        }
    }

    public WindowManager.LayoutParams getWindowParam(int i) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, i, 2027, 296, -3);
        layoutParams.privateFlags |= 64;
        layoutParams.gravity = 80;
        layoutParams.setTitle("GestureStub");
        return layoutParams;
    }

    public int getHotSpaceHeight() {
        Float f;
        if ("lithium".equals(Build.DEVICE)) {
            return (int) (getResources().getDisplayMetrics().density * 14.0f);
        }
        float f2 = getResources().getDisplayMetrics().density * 13.0f;
        if (getResources().getConfiguration().orientation == 1 && (f = DEVICE_BOTTOM_EDGE_HEIGHTS.get(Build.DEVICE)) != null && f.floatValue() < 4.5f) {
            f2 += TypedValue.applyDimension(5, 4.5f - f.floatValue(), getResources().getDisplayMetrics());
        }
        return (int) f2;
    }

    public void setIsSuperPowerMode(boolean z) {
        this.mIsSuperPowerMode = z;
    }
}
