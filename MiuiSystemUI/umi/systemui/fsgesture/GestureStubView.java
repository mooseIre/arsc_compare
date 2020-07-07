package com.android.systemui.fsgesture;

import android.animation.Animator;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Application;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.fsgesture.GestureBackArrowView;
import com.android.systemui.fsgesture.GesturesBackController;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.DividerExistChangeEvent;
import com.android.systemui.recents.events.activity.DividerMinimizedChangeEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.stackdivider.Divider;
import java.util.ArrayList;
import miui.util.ObjectReference;
import miui.util.ReflectionUtils;

public class GestureStubView extends FrameLayout {
    private static boolean isUserSetUp;
    /* access modifiers changed from: private */
    public boolean mAnimating;
    /* access modifiers changed from: private */
    public Animator.AnimatorListener mAnimatorListener;
    private float mAssistX1;
    private float mAssistX2;
    private int mBesideNotchArrowXStart;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mCurrAction;
    /* access modifiers changed from: private */
    public float mCurrX;
    /* access modifiers changed from: private */
    public float mCurrY;
    private float mDensity;
    /* access modifiers changed from: private */
    public boolean mDisableQuickSwitch;
    private Display mDisplay;
    private StubViewDisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    /* access modifiers changed from: private */
    public MotionEvent mDownEvent;
    /* access modifiers changed from: private */
    public float mDownX;
    private float mDownY;
    private int mEarWidth;
    /* access modifiers changed from: private */
    public GestureBackArrowView mGestureBackArrowView;
    private int mGestureStubDefaultSize;
    private WindowManager.LayoutParams mGestureStubParams;
    /* access modifiers changed from: private */
    public int mGestureStubPos;
    private int mGestureStubSize;
    private GesturesBackController.GesturesBackCallback mGesturesBackCallback;
    /* access modifiers changed from: private */
    public GesturesBackController mGesturesBackController;
    /* access modifiers changed from: private */
    public H mHandler;
    /* access modifiers changed from: private */
    public boolean mHideNotch;
    private boolean mIsGestureAnimationEnabled;
    /* access modifiers changed from: private */
    public boolean mIsGestureStarted;
    private boolean mIsInMinimizedMultiWindowMode;
    private boolean mIsInMultiWindowMode;
    /* access modifiers changed from: private */
    public boolean mKeepHidden;
    /* access modifiers changed from: private */
    public KeyguardManager mKeyguardManager;
    private Configuration mLastConfiguration;
    private int[] mLocation;
    /* access modifiers changed from: private */
    public boolean mNeedAdaptRotation;
    /* access modifiers changed from: private */
    public boolean mNeedAdjustArrowPosition;
    private boolean mNeedRender;
    private int mNotchHeight;
    private int mNotchWidth;
    /* access modifiers changed from: private */
    public boolean mPendingResetStatus;
    /* access modifiers changed from: private */
    public int mRotation;
    private int mScreenHeight;
    /* access modifiers changed from: private */
    public int mScreenWidth;
    private MiuiSettingsObserver mSettingsObserver;
    private boolean mSwipeInRightDirection;
    /* access modifiers changed from: private */
    public Vibrator mVibrator;
    private WindowManager mWindowManager;

    private enum EventPosition {
        UPON_NOTCH,
        BELOW_NOTCH,
        ALIGN_NOTCH
    }

    /* access modifiers changed from: private */
    public boolean isInSpeedLimit(int i) {
        if (this.mDownEvent == null) {
            return false;
        }
        float f = (float) i;
        if (Math.abs(this.mCurrX - this.mAssistX1) >= f || Math.abs(this.mCurrX - this.mAssistX2) >= f) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void updateAssistXPosition() {
        float f = this.mAssistX1;
        float f2 = this.mCurrX;
        this.mAssistX1 = f + ((f2 - f) / 4.0f);
        float f3 = this.mAssistX2;
        this.mAssistX2 = f3 + ((f2 - f3) / 2.0f);
    }

    /* access modifiers changed from: private */
    public boolean isSwipeRightInDirection() {
        if ((this.mGestureStubPos != 0 || this.mCurrX >= this.mAssistX1) && (this.mGestureStubPos != 1 || this.mCurrX <= this.mAssistX1)) {
            this.mSwipeInRightDirection = true;
        } else {
            this.mSwipeInRightDirection = false;
        }
        return this.mSwipeInRightDirection;
    }

    public static Task getNextTask(Context context, boolean z, int i) {
        ActivityManager.RunningTaskInfo runningTask;
        Task task;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(context);
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        TaskStack taskStack = createLoadPlan.getTaskStack();
        ActivityOptions activityOptions = null;
        if (taskStack == null || taskStack.getTaskCount() == 0 || (runningTask = systemServices.getRunningTask()) == null) {
            return null;
        }
        ArrayList<Task> stackTasks = taskStack.getStackTasks();
        int size = stackTasks.size();
        int i2 = 0;
        while (true) {
            if (i2 >= size - 1) {
                task = null;
                break;
            } else if (stackTasks.get(i2).key.id == runningTask.id) {
                task = stackTasks.get(i2 + 1);
                break;
            } else {
                i2++;
            }
        }
        if (task == null && size >= 1 && "com.miui.home".equals(runningTask.baseActivity.getPackageName())) {
            task = stackTasks.get(0);
        }
        if (task != null && task.icon == null) {
            task.icon = taskLoader.getAndUpdateActivityIcon(task.key, task.taskDescription, context.getResources(), true);
        }
        if (z && task != null) {
            if (i == 0) {
                activityOptions = ActivityOptions.makeCustomAnimation(context, R.anim.recents_quick_switch_left_enter, R.anim.recents_quick_switch_left_exit);
            } else if (i == 1) {
                activityOptions = ActivityOptions.makeCustomAnimation(context, R.anim.recents_quick_switch_right_enter, R.anim.recents_quick_switch_right_exit);
            }
            systemServices.startActivityFromRecents(context, task.key, task.title, activityOptions);
        }
        return task;
    }

    static boolean supportNextTask(KeyguardManager keyguardManager, ContentResolver contentResolver) {
        return !keyguardManager.isKeyguardLocked() && isUserSetUp(contentResolver);
    }

    private static boolean isUserSetUp(ContentResolver contentResolver) {
        if (!isUserSetUp) {
            boolean z = false;
            if (!(Settings.Global.getInt(contentResolver, "device_provisioned", 0) == 0 || Settings.Secure.getIntForUser(contentResolver, "user_setup_complete", 0, KeyguardUpdateMonitor.getCurrentUser()) == 0)) {
                z = true;
            }
            isUserSetUp = z;
        }
        return isUserSetUp;
    }

    public GestureStubView(Context context) {
        this(context, -1);
    }

    public GestureStubView(Context context, int i) {
        super(context);
        this.mLocation = new int[2];
        this.mCurrAction = -1;
        this.mScreenWidth = -1;
        this.mScreenHeight = -1;
        this.mGestureStubPos = -1;
        this.mGestureStubSize = -1;
        this.mGestureStubDefaultSize = -1;
        this.mNotchHeight = -1;
        this.mNotchWidth = -1;
        this.mEarWidth = -1;
        this.mRotation = 0;
        this.mDensity = -1.0f;
        this.mNeedAdjustArrowPosition = false;
        this.mDisableQuickSwitch = false;
        this.mAnimatorListener = new Animator.AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                boolean unused = GestureStubView.this.mAnimating = true;
            }

            public void onAnimationEnd(Animator animator) {
                boolean unused = GestureStubView.this.mAnimating = false;
                GestureStubView.this.mHandler.removeMessages(258);
                GestureStubView.this.mGestureBackArrowView.setVisibility(8);
                GestureStubView.this.resetRenderProperty("onAnimationEnd");
            }

            public void onAnimationCancel(Animator animator) {
                boolean unused = GestureStubView.this.mAnimating = false;
            }
        };
        this.mGesturesBackCallback = new GesturesBackController.GesturesBackCallback() {
            public void onSwipeStart(boolean z, float f) {
                boolean unused = GestureStubView.this.mIsGestureStarted = true;
                GestureStubView.this.clearMessages();
                Log.d("GestureStubView", "onSwipeStart: needAnimation: " + z);
                if (z) {
                    GestureStubView.this.mGestureBackArrowView.setVisibility(0);
                    GestureStubView.this.renderView();
                    if (GestureStubView.this.mNeedAdjustArrowPosition) {
                        int[] access$800 = GestureStubView.this.getParams(f);
                        GestureStubView.this.mGestureBackArrowView.onActionDown((float) access$800[0], (float) access$800[1], (float) access$800[2]);
                        return;
                    }
                    GestureStubView.this.mGestureBackArrowView.onActionDown(f, 0.0f, -1.0f);
                }
            }

            public void onSwipeProcess(boolean z, float f) {
                if (z) {
                    GestureStubView.this.mHandler.removeMessages(261);
                    GestureStubView.this.mHandler.sendEmptyMessage(261);
                } else {
                    GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_NONE);
                }
                GestureStubView.this.mGestureBackArrowView.onActionMove(f);
            }

            public void onSwipeStop(boolean z, float f) {
                Log.d("GestureStubView", "onSwipeStop");
                boolean unused = GestureStubView.this.mIsGestureStarted = false;
                GestureStubView.this.mHandler.sendMessageDelayed(GestureStubView.this.mHandler.obtainMessage(258), 500);
                if (!GestureStubView.this.isInSpeedLimit(20)) {
                    if (GestureStubView.this.isSwipeRightInDirection()) {
                        GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_BACK);
                    } else {
                        GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_NONE);
                    }
                }
                if (z) {
                    int i = AnonymousClass3.$SwitchMap$com$android$systemui$fsgesture$GestureBackArrowView$ReadyState[GestureStubView.this.mGestureBackArrowView.getCurrentState().ordinal()];
                    if (i == 1) {
                        GestureStubView.this.injectKeyEvent(4);
                    } else if (i == 2 && (!GestureStubView.supportNextTask(GestureStubView.this.mKeyguardManager, GestureStubView.this.mContentResolver) || GestureStubView.getNextTask(GestureStubView.this.mContext, true, GestureStubView.this.mGestureStubPos) == null)) {
                        GestureStubView.this.mVibrator.vibrate(100);
                    }
                }
                GestureStubView.this.mHandler.removeMessages(261);
                GestureStubView.this.mGestureBackArrowView.onActionUp(GesturesBackController.convertOffset(f), GestureStubView.this.mAnimatorListener);
            }

            public void onSwipeStopDirect() {
                Log.d("GestureStubView", "onSwipeStopDirect");
                boolean unused = GestureStubView.this.mIsGestureStarted = false;
                GestureStubView.this.injectKeyEvent(4);
            }
        };
        this.mLastConfiguration = new Configuration();
        this.mContext = context;
        this.mLastConfiguration.updateFrom(getResources().getConfiguration());
        this.mIsGestureStarted = false;
        this.mGestureStubPos = 2;
        this.mHandler = new H();
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        initGestureStubSize(i);
        this.mWindowManager.addView(this, getGestureStubWindowParam());
        this.mDisplayListener = new StubViewDisplayListener();
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        this.mKeyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        this.mSettingsObserver = new MiuiSettingsObserver(this.mHandler);
        this.mContentResolver = context.getContentResolver();
        isUserSetUp = isUserSetUp(this.mContentResolver);
        this.mVibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSettingsObserver.register();
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, (Handler) null);
        RecentsEventBus.getDefault().register(this);
        SystemUIApplication systemUIApplication = ((Application) this.mContext.getApplicationContext()).getSystemUIApplication();
        if (systemUIApplication != null) {
            Divider divider = (Divider) systemUIApplication.getComponent(Divider.class);
            boolean z = true;
            this.mIsInMultiWindowMode = divider != null && divider.isExists();
            if (divider == null || !divider.isMinimized()) {
                z = false;
            }
            this.mIsInMinimizedMultiWindowMode = z;
        }
    }

    /* access modifiers changed from: private */
    public int[] getParams(float f) {
        EventPosition eventPosition;
        int i = this.mEarWidth;
        if (f < ((float) i)) {
            eventPosition = EventPosition.UPON_NOTCH;
        } else if (f > ((float) (this.mScreenWidth - i))) {
            eventPosition = EventPosition.BELOW_NOTCH;
        } else {
            eventPosition = ((float) this.mNotchWidth) > this.mDensity * 164.0f ? EventPosition.ALIGN_NOTCH : EventPosition.UPON_NOTCH;
        }
        int adaptBesideNotchArrowXStart = adaptBesideNotchArrowXStart();
        int[] iArr = new int[3];
        int i2 = AnonymousClass3.$SwitchMap$com$android$systemui$fsgesture$GestureStubView$EventPosition[eventPosition.ordinal()];
        if (i2 == 1) {
            int i3 = this.mEarWidth;
            iArr[0] = (i3 / 3) * 2;
            iArr[1] = adaptBesideNotchArrowXStart;
            iArr[2] = (int) (((float) i3) + (this.mDensity * 36.0f) + 0.5f);
        } else if (i2 == 2) {
            iArr[0] = this.mScreenWidth / 2;
            iArr[1] = adaptAlignNotchArrowXStart();
            iArr[2] = (int) ((((float) this.mNotchWidth) - (this.mDensity * 54.0f)) + 0.5f);
        } else if (i2 == 3) {
            int i4 = this.mScreenWidth;
            int i5 = this.mEarWidth;
            iArr[0] = i4 - ((i5 / 3) * 2);
            iArr[1] = adaptBesideNotchArrowXStart;
            iArr[2] = (int) (((float) i5) + (this.mDensity * 36.0f) + 0.5f);
        }
        return iArr;
    }

    /* renamed from: com.android.systemui.fsgesture.GestureStubView$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$fsgesture$GestureBackArrowView$ReadyState = new int[GestureBackArrowView.ReadyState.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$fsgesture$GestureStubView$EventPosition = new int[EventPosition.values().length];

        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x003d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x001f */
        static {
            /*
                com.android.systemui.fsgesture.GestureStubView$EventPosition[] r0 = com.android.systemui.fsgesture.GestureStubView.EventPosition.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$systemui$fsgesture$GestureStubView$EventPosition = r0
                r0 = 1
                int[] r1 = $SwitchMap$com$android$systemui$fsgesture$GestureStubView$EventPosition     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.android.systemui.fsgesture.GestureStubView$EventPosition r2 = com.android.systemui.fsgesture.GestureStubView.EventPosition.UPON_NOTCH     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                r1 = 2
                int[] r2 = $SwitchMap$com$android$systemui$fsgesture$GestureStubView$EventPosition     // Catch:{ NoSuchFieldError -> 0x001f }
                com.android.systemui.fsgesture.GestureStubView$EventPosition r3 = com.android.systemui.fsgesture.GestureStubView.EventPosition.ALIGN_NOTCH     // Catch:{ NoSuchFieldError -> 0x001f }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r2 = $SwitchMap$com$android$systemui$fsgesture$GestureStubView$EventPosition     // Catch:{ NoSuchFieldError -> 0x002a }
                com.android.systemui.fsgesture.GestureStubView$EventPosition r3 = com.android.systemui.fsgesture.GestureStubView.EventPosition.BELOW_NOTCH     // Catch:{ NoSuchFieldError -> 0x002a }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r4 = 3
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                com.android.systemui.fsgesture.GestureBackArrowView$ReadyState[] r2 = com.android.systemui.fsgesture.GestureBackArrowView.ReadyState.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$com$android$systemui$fsgesture$GestureBackArrowView$ReadyState = r2
                int[] r2 = $SwitchMap$com$android$systemui$fsgesture$GestureBackArrowView$ReadyState     // Catch:{ NoSuchFieldError -> 0x003d }
                com.android.systemui.fsgesture.GestureBackArrowView$ReadyState r3 = com.android.systemui.fsgesture.GestureBackArrowView.ReadyState.READY_STATE_BACK     // Catch:{ NoSuchFieldError -> 0x003d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x003d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x003d }
            L_0x003d:
                int[] r0 = $SwitchMap$com$android$systemui$fsgesture$GestureBackArrowView$ReadyState     // Catch:{ NoSuchFieldError -> 0x0047 }
                com.android.systemui.fsgesture.GestureBackArrowView$ReadyState r2 = com.android.systemui.fsgesture.GestureBackArrowView.ReadyState.READY_STATE_RECENT     // Catch:{ NoSuchFieldError -> 0x0047 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0047 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0047 }
            L_0x0047:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.GestureStubView.AnonymousClass3.<clinit>():void");
        }
    }

    private int adaptBesideNotchArrowXStart() {
        int i = this.mBesideNotchArrowXStart;
        if (i <= 0) {
            return i;
        }
        ObjectReference tryCallMethod = ReflectionUtils.tryCallMethod(getViewRootImpl(), "isFocusWindowAdaptNotch", Boolean.class, new Object[0]);
        if ((tryCallMethod != null ? ((Boolean) tryCallMethod.get()).booleanValue() : false) || this.mHideNotch) {
            return 0;
        }
        return i;
    }

    private int adaptAlignNotchArrowXStart() {
        int i = this.mNotchHeight - 1;
        if (this.mHideNotch) {
            return 0;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSettingsObserver.unregister();
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        RecentsEventBus.getDefault().unregister(this);
    }

    public void showGestureStub() {
        if (!this.mAnimating) {
            this.mHandler.removeMessages(259);
            this.mKeepHidden = false;
            resetRenderProperty("showGestureStub");
            GestureBackArrowView gestureBackArrowView = this.mGestureBackArrowView;
            if (gestureBackArrowView != null) {
                gestureBackArrowView.reset();
            }
            setVisibility(0);
            Log.d("GestureStubView", "showGestureStub");
        }
    }

    public void hideGestureStubDelay() {
        this.mHandler.removeMessages(259);
        this.mHandler.sendEmptyMessageDelayed(259, 300);
    }

    /* access modifiers changed from: private */
    public void hideGestureStub() {
        this.mKeepHidden = true;
        MotionEvent motionEvent = this.mDownEvent;
        if (motionEvent != null) {
            motionEvent.recycle();
            this.mDownEvent = null;
        }
        setVisibility(8);
        Log.d("GestureStubView", "hideGestureStub");
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int updateFrom = this.mLastConfiguration.updateFrom(configuration);
        boolean z = (updateFrom & 1024) != 0;
        boolean z2 = (updateFrom & 2048) != 0;
        boolean z3 = (updateFrom & 4096) != 0;
        if (!this.mKeepHidden && z3 && z2 && z) {
            initScreenSizeAndDensity(-1);
            GestureBackArrowView gestureBackArrowView = this.mGestureBackArrowView;
            if (gestureBackArrowView != null) {
                removeView(gestureBackArrowView);
            }
            setGestureStubPosition(this.mGestureStubPos);
            if (this.mGesturesBackController != null) {
                int[] initGestureEdgeSize = initGestureEdgeSize();
                this.mGesturesBackController.setGestureEdgeWidth(initGestureEdgeSize[0], initGestureEdgeSize[1]);
            }
        }
        adaptRotation(false);
        adaptNotchHidden();
    }

    public void clearGestureStub() {
        hideGestureStub();
        this.mWindowManager.removeView(this);
        Log.d("GestureStubView", "clearGestureStub");
    }

    private void initGestureStubSize(int i) {
        initScreenSizeAndDensity(i);
        if (SystemProperties.getInt("ro.miui.notch", 0) == 1) {
            this.mNotchHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.notch_height);
            this.mNotchWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.notch_width);
            int i2 = this.mScreenHeight;
            int i3 = this.mScreenWidth;
            this.mEarWidth = (i2 < i3 ? i2 - this.mNotchWidth : i3 - this.mNotchWidth) / 2;
        }
        int[] initGestureEdgeSize = initGestureEdgeSize();
        this.mGesturesBackController = new GesturesBackController(this.mGesturesBackCallback, initGestureEdgeSize[0], initGestureEdgeSize[1]);
    }

    private int[] initGestureEdgeSize() {
        int[] iArr = new int[2];
        this.mRotation = this.mDisplay.getRotation();
        int i = this.mRotation;
        if (i == 1) {
            int i2 = this.mNotchHeight;
            int i3 = this.mGestureStubDefaultSize;
            iArr[0] = i2 + i3;
            iArr[1] = this.mScreenWidth - i3;
        } else if (i == 3) {
            int i4 = this.mGestureStubDefaultSize;
            iArr[0] = i4;
            iArr[1] = (this.mScreenWidth - i4) - this.mNotchHeight;
        } else {
            int i5 = this.mGestureStubDefaultSize;
            iArr[0] = i5;
            iArr[1] = this.mScreenWidth - i5;
        }
        return iArr;
    }

    private void initScreenSizeAndDensity(int i) {
        Point point = new Point();
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplay.getRealSize(point);
        int i2 = point.y;
        int i3 = point.x;
        if (i2 > i3) {
            this.mScreenWidth = i3;
            this.mScreenHeight = i2;
        } else {
            this.mScreenWidth = i2;
            this.mScreenHeight = i3;
        }
        if (i == -1) {
            int i4 = this.mScreenWidth;
            if (i4 == 720) {
                this.mGestureStubSize = 40;
                this.mGestureStubDefaultSize = 40;
            } else if (i4 != 1080) {
                this.mGestureStubSize = 54;
                this.mGestureStubDefaultSize = 54;
            } else {
                this.mGestureStubSize = 54;
                this.mGestureStubDefaultSize = 54;
            }
        } else {
            this.mGestureStubSize = i;
            this.mGestureStubDefaultSize = i;
        }
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
    }

    private void adaptNotch() {
        if (Constants.IS_NOTCH && this.mNotchHeight > 0) {
            int rotation = this.mDisplay.getRotation();
            if (rotation == 1) {
                int i = this.mGestureStubPos;
                if (i == 0) {
                    this.mGestureStubSize = this.mGestureStubDefaultSize + this.mNotchHeight;
                    this.mNeedAdjustArrowPosition = true;
                } else if (i == 1) {
                    this.mGestureStubSize = this.mGestureStubDefaultSize;
                    this.mNeedAdjustArrowPosition = false;
                }
                if (((float) this.mNotchWidth) < this.mDensity * 164.0f) {
                    this.mBesideNotchArrowXStart = this.mNotchHeight - 1;
                }
            } else if (rotation != 3) {
                this.mGestureStubSize = this.mGestureStubDefaultSize;
                this.mNeedAdjustArrowPosition = false;
                this.mBesideNotchArrowXStart = 0;
            } else {
                int i2 = this.mGestureStubPos;
                if (i2 == 1) {
                    this.mGestureStubSize = this.mGestureStubDefaultSize + this.mNotchHeight;
                    this.mNeedAdjustArrowPosition = true;
                } else if (i2 == 0) {
                    this.mGestureStubSize = this.mGestureStubDefaultSize;
                    this.mNeedAdjustArrowPosition = false;
                }
                if (((float) this.mNotchWidth) < this.mDensity * 164.0f) {
                    this.mBesideNotchArrowXStart = this.mNotchHeight - 1;
                }
            }
        }
    }

    public void setSize(int i) {
        this.mGestureStubDefaultSize = i;
        this.mGestureStubSize = i;
        adaptNotch();
        if (this.mGesturesBackController != null) {
            int[] initGestureEdgeSize = initGestureEdgeSize();
            this.mGesturesBackController.setGestureEdgeWidth(initGestureEdgeSize[0], initGestureEdgeSize[1]);
        }
        try {
            if (isAttachedToWindow()) {
                resetRenderProperty("setSize");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean gatherTransparentRegion(Region region) {
        if (!this.mNeedRender && region != null) {
            int width = getWidth();
            int height = getHeight();
            Log.d("GestureStubView", "gatherTransparentRegion: need render w:" + width + "  h:" + height);
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
    public void resetRenderProperty(String str) {
        if (!this.mKeepHidden) {
            this.mWindowManager.updateViewLayout(this, getGestureStubWindowParam());
            Log.d("GestureStubView", "resetRenderProperty: " + str);
            if (getParent() != null) {
                this.mNeedRender = false;
                getParent().requestTransparentRegion(this);
            }
        }
    }

    /* access modifiers changed from: private */
    public void renderView() {
        if (!this.mKeepHidden) {
            this.mWindowManager.updateViewLayout(this, getAnimatingLayoutParam());
            Log.d("GestureStubView", "renderView");
            if (getParent() != null) {
                this.mNeedRender = true;
                getParent().requestTransparentRegion(this);
            }
        }
    }

    private WindowManager.LayoutParams getGestureStubWindowParam() {
        int i;
        int i2;
        int i3;
        if (this.mGestureStubPos == 2) {
            i3 = this.mGestureStubSize;
            i = -1;
        } else {
            i = this.mGestureStubSize;
            int i4 = this.mRotation;
            float f = 0.6f;
            if (i4 == 0 || i4 == 2) {
                if (this.mIsInMultiWindowMode && !this.mIsInMinimizedMultiWindowMode) {
                    f = 1.0f;
                }
                i2 = this.mScreenHeight;
            } else {
                i2 = this.mScreenWidth;
            }
            i3 = (int) (((float) i2) * f);
        }
        int i5 = i3;
        int i6 = i;
        WindowManager.LayoutParams layoutParams = this.mGestureStubParams;
        boolean z = false;
        if (layoutParams == null) {
            this.mGestureStubParams = new WindowManager.LayoutParams(i6, i5, 2027, 296, 1);
            WindowManagerCompat.setLayoutInDisplayCutoutMode(this.mGestureStubParams, 1);
            setBackgroundColor(0);
            this.mGestureStubParams.alpha = 1.0f;
        } else {
            layoutParams.width = i6;
            layoutParams.height = i5;
        }
        int i7 = this.mGestureStubPos;
        int i8 = 80;
        if (i7 == 2) {
            WindowManager.LayoutParams layoutParams2 = this.mGestureStubParams;
            layoutParams2.gravity = 80;
            layoutParams2.setTitle("GestureStubBottom");
            return this.mGestureStubParams;
        }
        if (i7 == 0) {
            z = true;
        }
        int i9 = z ? 3 : 5;
        int i10 = this.mRotation;
        if (i10 == 0 || i10 == 2) {
            this.mGestureStubParams.verticalMargin = (getResources().getDisplayMetrics().density * 13.0f) / ((float) this.mScreenHeight);
        } else {
            i8 = 16;
            this.mGestureStubParams.verticalMargin = 0.0f;
        }
        WindowManager.LayoutParams layoutParams3 = this.mGestureStubParams;
        layoutParams3.gravity = i9 | i8;
        layoutParams3.setTitle(z ? "GestureStubLeft" : "GestureStubRight");
        return this.mGestureStubParams;
    }

    private WindowManager.LayoutParams getAnimatingLayoutParam() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2027, 296, 1);
        WindowManagerCompat.setLayoutInDisplayCutoutMode(layoutParams, 1);
        layoutParams.alpha = 1.0f;
        return layoutParams;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0018, code lost:
        if (r0 != 3) goto L_0x0132;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r13) {
        /*
            r12 = this;
            int r0 = r13.getAction()
            r12.mCurrAction = r0
            int r0 = r12.mCurrAction
            r1 = 150(0x96, double:7.4E-322)
            r3 = 0
            r4 = 2
            r5 = 1
            java.lang.String r6 = "GestureStubView"
            if (r0 == 0) goto L_0x00fc
            r7 = 255(0xff, float:3.57E-43)
            if (r0 == r5) goto L_0x0056
            if (r0 == r4) goto L_0x001c
            r8 = 3
            if (r0 == r8) goto L_0x0056
            goto L_0x0132
        L_0x001c:
            float r0 = r13.getRawX()
            r12.mCurrX = r0
            float r0 = r13.getRawY()
            r12.mCurrY = r0
            float r0 = r12.mCurrY
            float r1 = r12.mDownY
            float r0 = r0 - r1
            float r0 = java.lang.Math.abs(r0)
            r1 = 1073741824(0x40000000, float:2.0)
            float r2 = r12.mCurrX
            float r8 = r12.mDownX
            float r2 = r2 - r8
            float r2 = java.lang.Math.abs(r2)
            float r2 = r2 * r1
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 <= 0) goto L_0x0132
            boolean r0 = r12.mIsGestureStarted
            if (r0 != 0) goto L_0x0132
            com.android.systemui.fsgesture.GestureStubView$H r0 = r12.mHandler
            android.os.Message r1 = r0.obtainMessage(r7)
            r0.sendMessage(r1)
            java.lang.String r0 = "up-slide detected, sendMessage MSG_SET_GESTURE_STUB_UNTOUCHABLE"
            android.util.Log.d(r6, r0)
            goto L_0x0132
        L_0x0056:
            android.view.MotionEvent r0 = r12.mDownEvent
            if (r0 != 0) goto L_0x005b
            return r5
        L_0x005b:
            float r8 = r13.getRawX()
            r12.mCurrX = r8
            float r8 = r13.getRawY()
            r12.mCurrY = r8
            long r8 = r13.getEventTime()
            long r10 = r0.getEventTime()
            long r8 = r8 - r10
            int r1 = (r8 > r1 ? 1 : (r8 == r1 ? 0 : -1))
            if (r1 >= 0) goto L_0x00c4
            boolean r1 = r12.mIsGestureStarted
            if (r1 != 0) goto L_0x00c4
            r12.clearMessages()
            float r1 = r12.mCurrX
            float r2 = r0.getRawX()
            float r1 = r1 - r2
            float r2 = r12.mCurrY
            float r0 = r0.getRawY()
            float r2 = r2 - r0
            boolean r0 = r12.mIsGestureStarted
            if (r0 != 0) goto L_0x00c4
            float r0 = java.lang.Math.abs(r1)
            r8 = 1106247680(0x41f00000, float:30.0)
            int r0 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r0 > 0) goto L_0x00c4
            float r0 = java.lang.Math.abs(r2)
            int r0 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r0 > 0) goto L_0x00c4
            com.android.systemui.fsgesture.GestureStubView$H r0 = r12.mHandler
            android.os.Message r7 = r0.obtainMessage(r7)
            r0.sendMessage(r7)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r7 = "currTime - mDownTime < MSG_CHECK_GESTURE_STUB_TOUCHABLE_TIMEOUT updateViewLayout UnTouchable, diffX:"
            r0.append(r7)
            r0.append(r1)
            java.lang.String r1 = " diffY:"
            r0.append(r1)
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r6, r0)
        L_0x00c4:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "ACTION_UP: mIsGestureStarted: "
            r0.append(r1)
            boolean r1 = r12.mIsGestureStarted
            r0.append(r1)
            java.lang.String r1 = " mIsGestureAnimationEnabled: "
            r0.append(r1)
            boolean r1 = r12.mIsGestureAnimationEnabled
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r6, r0)
            boolean r0 = r12.mIsGestureStarted
            if (r0 == 0) goto L_0x00f9
            boolean r0 = r12.mIsGestureAnimationEnabled
            if (r0 == 0) goto L_0x00f9
            com.android.systemui.fsgesture.GestureStubView$H r0 = r12.mHandler
            r1 = 258(0x102, float:3.62E-43)
            android.os.Message r1 = r0.obtainMessage(r1)
            r6 = 500(0x1f4, double:2.47E-321)
            r0.sendMessageDelayed(r1, r6)
        L_0x00f9:
            r12.mIsGestureStarted = r3
            goto L_0x0132
        L_0x00fc:
            float r0 = r13.getRawX()
            r12.mAssistX2 = r0
            r12.mAssistX1 = r0
            r12.mDownX = r0
            r12.mCurrX = r0
            float r0 = r13.getRawY()
            r12.mDownY = r0
            r12.mCurrY = r0
            android.view.MotionEvent r0 = r12.mDownEvent
            if (r0 == 0) goto L_0x0117
            r0.recycle()
        L_0x0117:
            android.view.MotionEvent r0 = r13.copy()
            r12.mDownEvent = r0
            com.android.systemui.fsgesture.GestureStubView$H r0 = r12.mHandler
            r7 = 256(0x100, float:3.59E-43)
            r0.removeMessages(r7)
            com.android.systemui.fsgesture.GestureStubView$H r0 = r12.mHandler
            android.os.Message r7 = r0.obtainMessage(r7)
            r0.sendMessageDelayed(r7, r1)
            java.lang.String r0 = "onTouch ACTION_DOWN sendMessageDelayed MSG_CHECK_GESTURE_STUB_TOUCHABLE"
            android.util.Log.d(r6, r0)
        L_0x0132:
            com.android.systemui.fsgesture.GesturesBackController r0 = r12.mGesturesBackController
            if (r0 == 0) goto L_0x0142
            int r1 = r12.mGestureStubPos
            if (r1 == r4) goto L_0x0142
            boolean r12 = r12.mPendingResetStatus
            if (r12 != 0) goto L_0x0142
            r0.onPointerEvent(r13)
            return r5
        L_0x0142:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.GestureStubView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void enableGestureBackAnimation(boolean z) {
        this.mIsGestureAnimationEnabled = z;
        this.mGesturesBackController.enableGestureBackAnimation(z);
        Log.d("GestureStubView", "enableGestureBackAnimation enable:" + z);
    }

    public void disableQuickSwitch(boolean z) {
        this.mDisableQuickSwitch = z;
    }

    public void setGestureStubPosition(int i) {
        this.mGestureStubPos = i;
        this.mGestureBackArrowView = new GestureBackArrowView(this.mContext, this.mGestureStubPos);
        addView(this.mGestureBackArrowView);
        Point point = new Point();
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplay.getRealSize(point);
        this.mGestureBackArrowView.setDisplayWidth(point.x);
    }

    public void adaptAndRender() {
        adaptNotch();
        resetRenderProperty("adaptAndRender");
    }

    /* access modifiers changed from: private */
    public void clearMessages() {
        this.mHandler.removeMessages(256);
        this.mHandler.removeMessages(255);
    }

    /* access modifiers changed from: private */
    public void injectMotionEvent(int i) {
        MotionEvent motionEvent = this.mDownEvent;
        if (motionEvent != null) {
            Log.d("GestureStubView", "injectMotionEvent action :" + i + " downX: " + motionEvent.getRawX() + " downY: " + motionEvent.getRawY() + " flags:" + motionEvent.getFlags());
            if ((motionEvent.getFlags() & 65536) == 0) {
                MotionEvent.PointerProperties[] createArray = MotionEvent.PointerProperties.createArray(1);
                motionEvent.getPointerProperties(0, createArray[0]);
                MotionEvent.PointerCoords[] createArray2 = MotionEvent.PointerCoords.createArray(1);
                motionEvent.getPointerCoords(0, createArray2[0]);
                createArray2[0].x = motionEvent.getRawX();
                createArray2[0].y = motionEvent.getRawY();
                int i2 = i;
                InputManager.getInstance().injectInputEvent(MotionEvent.obtain(motionEvent.getDownTime(), motionEvent.getEventTime(), i2, 1, createArray, createArray2, motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags()), 0);
            }
        }
    }

    /* access modifiers changed from: private */
    public void injectKeyEvent(int i) {
        Log.d("GestureStubView", "injectKeyEvent keyCode:" + i);
        long uptimeMillis = SystemClock.uptimeMillis();
        long j = uptimeMillis;
        long j2 = uptimeMillis;
        int i2 = i;
        KeyEvent keyEvent = new KeyEvent(j, j2, 0, i2, 0, 0, -1, 0, 8, 257);
        KeyEvent keyEvent2 = r2;
        KeyEvent keyEvent3 = new KeyEvent(j, j2, 1, i2, 0, 0, -1, 0, 8, 257);
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
        InputManager.getInstance().injectInputEvent(keyEvent2, 0);
        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("mesh_heavy", false);
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            MotionEvent access$2000 = GestureStubView.this.mDownEvent;
            switch (message.what) {
                case 255:
                    if (!GestureStubView.this.mIsGestureStarted) {
                        Log.d("GestureStubView", "handleMessage MSG_SET_GESTURE_STUB_UNTOUCHABLE");
                        GestureStubView.this.setVisibility(8);
                        if (GestureStubView.this.mGesturesBackController != null) {
                            GestureStubView.this.mGesturesBackController.reset();
                        }
                        sendMessageDelayed(obtainMessage(260), 20);
                        boolean unused = GestureStubView.this.mPendingResetStatus = true;
                        sendMessageDelayed(obtainMessage(257), 500);
                        return;
                    }
                    return;
                case 256:
                    if (access$2000 != null && !GestureStubView.this.mIsGestureStarted) {
                        float access$2100 = GestureStubView.this.mCurrX - access$2000.getRawX();
                        float access$2200 = GestureStubView.this.mCurrY - access$2000.getRawY();
                        Log.d("GestureStubView", "handleMessage MSG_CHECK_GESTURE_STUB_TOUCHABLE diffX: " + access$2100 + " diffY: " + access$2200 + " mDownX: " + access$2000.getRawX() + " mDownY: " + access$2000.getRawY());
                        if (Math.abs(access$2100) <= 30.0f && Math.abs(access$2200) <= 30.0f) {
                            GestureStubView.this.mHandler.removeMessages(255);
                            GestureStubView.this.mHandler.sendMessage(GestureStubView.this.mHandler.obtainMessage(255));
                            return;
                        }
                        return;
                    }
                    return;
                case 257:
                    boolean unused2 = GestureStubView.this.mPendingResetStatus = false;
                    if (!GestureStubView.this.mKeepHidden) {
                        GestureStubView.this.setVisibility(0);
                    }
                    Log.d("GestureStubView", "handleMessage MSG_RESET_GESTURE_STUB_TOUCHABLE");
                    return;
                case 258:
                    boolean unused3 = GestureStubView.this.mAnimating = false;
                    GestureStubView.this.mGestureBackArrowView.setVisibility(8);
                    GestureStubView.this.resetRenderProperty("MSG_RESET_ANIMATING_STATUS");
                    Log.d("GestureStubView", "reset animating status");
                    return;
                case 259:
                    GestureStubView.this.hideGestureStub();
                    return;
                case 260:
                    if (GestureStubView.this.mCurrAction == 2 || GestureStubView.this.mCurrAction == 0) {
                        GestureStubView.this.injectMotionEvent(0);
                    } else {
                        GestureStubView.this.injectMotionEvent(0);
                        GestureStubView.this.injectMotionEvent(1);
                    }
                    if (GestureStubView.this.mDownEvent != null) {
                        GestureStubView.this.mDownEvent.recycle();
                        MotionEvent unused4 = GestureStubView.this.mDownEvent = null;
                        return;
                    }
                    return;
                case 261:
                    if (GestureStubView.this.mIsGestureStarted) {
                        if (GestureStubView.this.mDisableQuickSwitch) {
                            GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_BACK);
                            return;
                        }
                        GestureStubView.this.updateAssistXPosition();
                        if (!GestureStubView.this.isSwipeRightInDirection()) {
                            if (GestureStubView.this.isInSpeedLimit(20)) {
                                if (Math.abs(GestureStubView.this.mCurrX - GestureStubView.this.mDownX) < ((float) GestureStubView.this.mScreenWidth) * 0.33f) {
                                    GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_BACK);
                                } else {
                                    GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_RECENT);
                                }
                            }
                        } else if (!GestureStubView.this.isInSpeedLimit(20)) {
                            if (GestureStubView.this.mGestureBackArrowView.getCurrentState() != GestureBackArrowView.ReadyState.READY_STATE_RECENT) {
                                GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_BACK);
                            }
                        } else if (Math.abs(GestureStubView.this.mCurrX - GestureStubView.this.mDownX) > ((float) GestureStubView.this.mScreenWidth) * 0.33f) {
                            GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_RECENT);
                        } else {
                            GestureStubView.this.mGestureBackArrowView.setReadyFinish(GestureBackArrowView.ReadyState.READY_STATE_BACK);
                        }
                        GestureStubView.this.mHandler.sendEmptyMessageDelayed(261, 17);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void adaptRotation(boolean z) {
        int rotation = this.mDisplay.getRotation();
        if ((!z && rotation != this.mRotation) || (z && Math.abs(this.mRotation - rotation) == 2)) {
            this.mRotation = rotation;
            rotateGesture();
        }
    }

    private void adaptNotchHidden() {
        if (this.mNeedAdaptRotation) {
            rotateGesture();
        }
        this.mNeedAdaptRotation = false;
    }

    private void rotateGesture() {
        setSize(this.mGestureStubDefaultSize);
        if (this.mGestureBackArrowView != null) {
            Point point = new Point();
            this.mDisplay = this.mWindowManager.getDefaultDisplay();
            this.mDisplay.getRealSize(point);
            this.mGestureBackArrowView.setDisplayWidth(point.x);
        }
    }

    public final void onBusEvent(DividerExistChangeEvent dividerExistChangeEvent) {
        boolean z = dividerExistChangeEvent.isExist;
        if (z != this.mIsInMultiWindowMode) {
            this.mIsInMultiWindowMode = z;
            resetRenderProperty("multiWindowModeChange");
        }
    }

    public final void onBusEvent(DividerMinimizedChangeEvent dividerMinimizedChangeEvent) {
        boolean z = dividerMinimizedChangeEvent.isMinimized;
        if (z != this.mIsInMinimizedMultiWindowMode) {
            this.mIsInMinimizedMultiWindowMode = z;
            resetRenderProperty("multiWindowModeChange");
        }
    }

    private class StubViewDisplayListener implements DisplayManager.DisplayListener {
        public void onDisplayAdded(int i) {
        }

        public void onDisplayRemoved(int i) {
        }

        private StubViewDisplayListener() {
        }

        public void onDisplayChanged(int i) {
            GestureStubView.this.adaptRotation(true);
        }
    }

    class MiuiSettingsObserver extends ContentObserver {
        MiuiSettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void register() {
            GestureStubView gestureStubView = GestureStubView.this;
            ContentResolver unused = gestureStubView.mContentResolver = gestureStubView.mContext.getContentResolver();
            GestureStubView.this.mContentResolver.registerContentObserver(Settings.Global.getUriFor("force_black_v2"), false, this, -1);
        }

        /* access modifiers changed from: package-private */
        public void unregister() {
            GestureStubView gestureStubView = GestureStubView.this;
            ContentResolver unused = gestureStubView.mContentResolver = gestureStubView.mContext.getContentResolver();
            GestureStubView.this.mContentResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean z) {
            GestureStubView gestureStubView = GestureStubView.this;
            boolean unused = gestureStubView.mHideNotch = MiuiSettings.Global.getBoolean(gestureStubView.mContentResolver, "force_black_v2");
            GestureStubView gestureStubView2 = GestureStubView.this;
            boolean z2 = true;
            if (!(gestureStubView2.mRotation == 1 || GestureStubView.this.mRotation == 3)) {
                z2 = false;
            }
            boolean unused2 = gestureStubView2.mNeedAdaptRotation = z2;
        }
    }
}
