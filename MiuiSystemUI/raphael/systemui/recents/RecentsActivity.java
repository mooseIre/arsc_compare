package com.android.systemui.recents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowInsetsCompat;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.Application;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.miui.statusbar.CloudDataHelper;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.FsGestureMoveEvent;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.ActivitySetDummyTranslucentEvent;
import com.android.systemui.recents.events.activity.AnimFirstTaskViewAlphaEvent;
import com.android.systemui.recents.events.activity.CancelEnterRecentsWindowAnimationEvent;
import com.android.systemui.recents.events.activity.ConfigurationChangedEvent;
import com.android.systemui.recents.events.activity.DebugFlagsChangedEvent;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowLastAnimationFrameEvent;
import com.android.systemui.recents.events.activity.ExitRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsCompleteEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsZoomEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeMoveEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeResetEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeSlideInEvent;
import com.android.systemui.recents.events.activity.FsGestureRecentsModeSlideOutEvent;
import com.android.systemui.recents.events.activity.FsGestureSlideInEvent;
import com.android.systemui.recents.events.activity.FsGestureSlideOutEvent;
import com.android.systemui.recents.events.activity.HideMemoryAndDockEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchTaskFailedEvent;
import com.android.systemui.recents.events.activity.LaunchTaskSucceededEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.RotationChangedEvent;
import com.android.systemui.recents.events.activity.ScrollerFlingFinishEvent;
import com.android.systemui.recents.events.activity.ShowMemoryAndDockEvent;
import com.android.systemui.recents.events.activity.StackScrollChangedEvent;
import com.android.systemui.recents.events.activity.StartSmallWindowEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.recents.events.component.ChangeTaskLockStateEvent;
import com.android.systemui.recents.events.component.ExitMultiModeEvent;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.component.ScreenPinningRequestEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.CleanInRecentsEvents;
import com.android.systemui.recents.events.ui.DeleteTaskDataEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEndEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.HideIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.ShowIncompatibleAppOverlayEvent;
import com.android.systemui.recents.events.ui.StackViewScrolledEvent;
import com.android.systemui.recents.events.ui.UpdateFreeformTaskViewVisibilityEvent;
import com.android.systemui.recents.events.ui.UserInteractionEvent;
import com.android.systemui.recents.events.ui.focus.DismissFocusedTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusNextTaskViewEvent;
import com.android.systemui.recents.events.ui.focus.FocusPreviousTaskViewEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.ProcessManagerHelper;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsPackageMonitor;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.CircleAndTickAnimView;
import com.android.systemui.recents.views.RecentsRecommendView;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskView;
import com.miui.daemon.performance.PerfShielderManager;
import com.miui.enterprise.ApplicationHelper;
import com.xiaomi.stat.d;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import miui.os.Build;
import miui.process.ProcessConfig;
import miui.process.ProcessManager;
import miui.securityspace.XSpaceUserHandle;
import miui.util.HardwareInfo;
import miui.view.animation.SineEaseInOutInterpolator;
import miui.view.animation.SineEaseOutInterpolator;

public class RecentsActivity extends Activity implements ViewTreeObserver.OnPreDrawListener {
    public static long mFreeBeforeClean = 0;
    /* access modifiers changed from: private */
    public static boolean sForceBlack = false;
    private ContentObserver mAccessControlLockModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            SystemServicesProxy systemServices = Recents.getSystemServices();
            if (systemServices != null) {
                systemServices.setAccessControlLockMode(Settings.Secure.getIntForUser(RecentsActivity.this.getContentResolver(), "access_control_lock_mode", 1, -2));
            }
        }
    };
    private View mBackGround;
    private ValueAnimator mBlurAnim;
    private CircleAndTickAnimView mClearAnimView;
    public final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
    /* access modifiers changed from: private */
    public ReferenceCountedTrigger mDismissAllTaskViewEventTrigger;
    private DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int i) {
        }

        public void onDisplayRemoved(int i) {
        }

        public void onDisplayChanged(int i) {
            int rotation = ((WindowManager) RecentsActivity.this.getSystemService("window")).getDefaultDisplay().getRotation();
            if (RecentsActivity.this.mRotation != rotation) {
                int unused = RecentsActivity.this.mRotation = rotation;
                RecentsActivity.this.setIncompatibleOverlayPadding();
                RecentsActivity.this.setNotchPadding();
                RecentsEventBus.getDefault().send(new RotationChangedEvent(RecentsActivity.this.mRotation));
            }
        }
    };
    private TextView mDockBtn;
    private Button mExitMultiModeBtn;
    private boolean mFinishedOnStartup;
    private int mFocusTimerDuration;
    private ContentObserver mForceBlackObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            boolean unused = RecentsActivity.sForceBlack = MiuiSettings.Global.getBoolean(RecentsActivity.this.getContentResolver(), "force_black");
        }
    };
    /* access modifiers changed from: private */
    public long mFreeAtFirst;
    private boolean mFsSlideIn = false;
    private boolean mFsZoom = false;
    /* access modifiers changed from: private */
    public Handler mHandler = new RecentsHandler();
    private Intent mHomeIntent;
    private boolean mIgnoreAltTabRelease;
    private View mIncompatibleAppOverlay;
    private boolean mIsAddExitMultiModeBtn;
    private boolean mIsInMultiWindowMode;
    private boolean mIsRecommendVisible;
    /* access modifiers changed from: private */
    public boolean mIsShowRecommend;
    /* access modifiers changed from: private */
    public boolean mIsVisible;
    private DozeTrigger mIterateTrigger;
    private int mLastDeviceOrientation = 0;
    private int mLastDisplayDensity;
    private long mLastTabKeyEventTime;
    private ViewGroup mMemoryAndClearContainer;
    private boolean mNeedMoveRecentsToFrontOfFsGesture = true;
    private boolean mNeedReloadStackView = true;
    private RecentsPackageMonitor mPackageMonitor;
    private boolean mReceivedNewIntent;
    private FrameLayout mRecentsContainer;
    private final ViewTreeObserver.OnPreDrawListener mRecentsDrawnEventListener = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            RecentsActivity.this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
            RecentsEventBus.getDefault().post(new RecentsDrawnEvent());
            RecentsActivity.this.checkFsGestureOnEnterRecents();
            return true;
        }
    };
    private RecentsRecommendView mRecentsRecommendView;
    /* access modifiers changed from: private */
    public RecentsView mRecentsView;
    /* access modifiers changed from: private */
    public int mRotation = -1;
    private final Runnable mSendEnterWindowAnimationCompleteRunnable = new Runnable() {
        public void run() {
            RecentsEventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
        }
    };
    private View mSeparatorForMemoryInfo;
    private Method mSetDummyTranslucentMethod;
    private ContentObserver mShowRecommendObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            BackgroundThread.getHandler().removeCallbacks(RecentsActivity.this.mShowRecommendRunnable);
            BackgroundThread.getHandler().post(RecentsActivity.this.mShowRecommendRunnable);
        }
    };
    /* access modifiers changed from: private */
    public Runnable mShowRecommendRunnable = new Runnable() {
        public void run() {
            final boolean booleanForUser = MiuiSettings.System.getBooleanForUser(RecentsActivity.this.getContentResolver(), "miui_recents_show_recommend", MiuiSettings.SettingsCloudData.getCloudDataBoolean(RecentsActivity.this.getContentResolver(), "showRecentsRecommend", "isShow", true), -2);
            RecentsActivity.this.mHandler.post(new Runnable() {
                public void run() {
                    boolean unused = RecentsActivity.this.mIsShowRecommend = booleanForUser;
                    RecentsActivity.this.updateRecentsRecommendViewVisible();
                }
            });
        }
    };
    private ContentObserver mSlideCoverObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            if (Settings.System.getIntForUser(RecentsActivity.this.getContentResolver(), "sc_status", -1, -2) == 0 && RecentsActivity.this.mIsVisible) {
                RecentsActivity.this.moveTaskToBack(true);
            }
        }
    };
    private ContentObserver mSuperSavePowerObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            if (MiuiSettings.System.isSuperSaveModeOpen(RecentsActivity.this, UserHandle.myUserId()) && RecentsActivity.this.mIsVisible) {
                RecentsActivity.this.moveTaskToBack(true);
            }
        }
    };
    final BroadcastReceiver mSystemBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                RecentsActivity.this.mHandler.post(new Runnable() {
                    public void run() {
                        RecentsActivity.this.dismissRecentsToHomeIfVisible(false);
                    }
                });
            }
        }
    };
    private TextView mTipView;
    /* access modifiers changed from: private */
    public long mTotalMemory;
    private ViewGroup mTxtMemoryContainer;
    private TextView mTxtMemoryInfo1;
    private TextView mTxtMemoryInfo2;
    private final UserInteractionEvent mUserInteractionEvent = new UserInteractionEvent();

    public final void onBusEvent(FsGestureRecentsModeMoveEvent fsGestureRecentsModeMoveEvent) {
    }

    public boolean onMenuOpened(int i, Menu menu) {
        return false;
    }

    class LaunchHomeRunnable implements Runnable {
        Intent mLaunchIntent;
        ActivityOptions mOpts;

        public LaunchHomeRunnable(Intent intent, ActivityOptions activityOptions) {
            this.mLaunchIntent = intent;
            this.mOpts = activityOptions;
        }

        public void run() {
            RecentsActivity.this.mHandler.post(new Runnable() {
                public void run() {
                    try {
                        ActivityOptions activityOptions = LaunchHomeRunnable.this.mOpts;
                        if (activityOptions == null) {
                            activityOptions = ActivityOptions.makeCustomAnimation(RecentsActivity.this, R.anim.recents_to_launcher_enter, R.anim.recents_to_launcher_exit);
                        }
                        RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(false));
                        RecentsActivity.this.startActivityAsUser(LaunchHomeRunnable.this.mLaunchIntent, activityOptions.toBundle(), UserHandle.CURRENT);
                        Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, 1.0f);
                    } catch (Exception e) {
                        Log.e("RecentsActivity", RecentsActivity.this.getString(R.string.recents_launch_error_message, new Object[]{"Home"}), e);
                    }
                }
            });
        }
    }

    class RecentsHandler extends Handler {
        RecentsHandler() {
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            if (message.what == 1000) {
                RecentsActivity.this.doClearAnim();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean dismissRecentsToTargetTask(int i) {
        return Recents.getSystemServices().isRecentsActivityVisible() && this.mRecentsView.launchTargetTask(i);
    }

    /* access modifiers changed from: package-private */
    public boolean dismissRecentsToLaunchTargetTaskOrHome() {
        if (!Recents.getSystemServices().isRecentsActivityVisible()) {
            return false;
        }
        if (this.mRecentsView.launchPreviousTask()) {
            return true;
        }
        dismissRecentsToHome(true);
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean dismissRecentsToTargetTaskOrHome() {
        Recents.getSystemServices();
        boolean z = false;
        if (this.mIsVisible) {
            z = true;
            if (this.mRecentsView.launchTargetTask(0)) {
                return true;
            }
            dismissRecentsToHome(true);
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void dismissRecentsToHome(boolean z) {
        dismissRecentsToHome(z, (ActivityOptions) null);
    }

    /* access modifiers changed from: package-private */
    public void dismissRecentsToHome(boolean z, ActivityOptions activityOptions) {
        this.mRecentsView.getMenuView().removeMenu(false);
        DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted = new DismissRecentsToHomeAnimationStarted(z);
        this.mHandler.post(new LaunchHomeRunnable(this.mHomeIntent, activityOptions));
        if (Recents.getConfiguration().getLaunchState().launchedViaFsGesture) {
            this.mNeedMoveRecentsToFrontOfFsGesture = false;
        }
        Recents.getSystemServices().sendCloseSystemWindows("homekey");
        RecentsEventBus.getDefault().send(dismissRecentsToHomeAnimationStarted);
    }

    /* access modifiers changed from: package-private */
    public boolean dismissRecentsToHomeIfVisible(boolean z) {
        if (!Recents.getSystemServices().isRecentsActivityVisible()) {
            return false;
        }
        dismissRecentsToHome(z);
        return true;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().requestFeature(1);
        boolean z = false;
        this.mFinishedOnStartup = false;
        if (Recents.getSystemServices() == null) {
            this.mFinishedOnStartup = true;
            finish();
            return;
        }
        RecentsEventBus.getDefault().register(this, 2);
        this.mPackageMonitor = new RecentsPackageMonitor();
        this.mPackageMonitor.register(this);
        setContentView(R.layout.recents);
        takeKeyEvents(true);
        this.mRecentsContainer = (FrameLayout) findViewById(R.id.recents_container);
        this.mRecentsView = (RecentsView) findViewById(R.id.recents_view);
        this.mTotalMemory = HardwareInfo.getTotalPhysicalMemory() / 1024;
        this.mMemoryAndClearContainer = (ViewGroup) findViewById(R.id.memoryAndClearContainer);
        this.mTxtMemoryContainer = (ViewGroup) findViewById(R.id.txtMemoryContainer);
        this.mTxtMemoryInfo1 = (TextView) findViewById(R.id.txtMemoryInfo1);
        this.mTxtMemoryInfo2 = (TextView) findViewById(R.id.txtMemoryInfo2);
        this.mSeparatorForMemoryInfo = findViewById(R.id.separatorForMemoryInfo);
        this.mClearAnimView = (CircleAndTickAnimView) findViewById(R.id.clearAnimView);
        this.mDockBtn = (TextView) findViewById(R.id.btnDock);
        this.mTipView = (TextView) findViewById(R.id.tip);
        this.mBackGround = findViewById(R.id.background);
        addRecentsRecommendViewIfNeeded();
        getWindow().getAttributes().privateFlags |= 16384;
        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        this.mLastDeviceOrientation = appConfiguration.orientation;
        this.mLastDisplayDensity = appConfiguration.densityDpi;
        this.mFocusTimerDuration = getResources().getInteger(R.integer.recents_auto_advance_duration);
        this.mIterateTrigger = new DozeTrigger(this.mFocusTimerDuration, new Runnable() {
            public void run() {
                RecentsActivity.this.dismissRecentsToTargetTask(288);
            }
        });
        this.mBackGround.setBackgroundDrawable(this.mRecentsView.getBackgroundScrim());
        this.mHomeIntent = new Intent("android.intent.action.MAIN", (Uri) null);
        this.mHomeIntent.addCategory("android.intent.category.HOME");
        this.mHomeIntent.addFlags(270532608);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(this.mSystemBroadcastReceiver, intentFilter, (String) null, (Handler) Dependency.get(Dependency.SCREEN_OFF_HANDLER));
        getWindow().addPrivateFlags(64);
        this.mClearAnimView.setDrawables(R.drawable.notifications_clear_all, R.drawable.btn_clear_all);
        this.mClearAnimView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                RecentsActivity.this.cleanInRecents();
            }
        });
        this.mClearAnimView.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.applications.ManageApplicationsActivity");
                intent.putExtra("com.android.settings.APPLICATION_LIST_TYPE", 2);
                intent.setFlags(268435456);
                TaskStackBuilder.create(RecentsActivity.this.getApplicationContext()).addNextIntentWithParentStack(intent).startActivities((Bundle) null, UserHandle.CURRENT);
                return true;
            }
        });
        this.mDockBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!BaseRecentsImpl.toastForbidDockedWhenScreening(RecentsActivity.this.getApplicationContext())) {
                    RecentsActivity.this.updateDockRegions(true);
                }
            }
        });
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.recents_dock_btn_margin_start);
        fitsSystemWindowInsets(this.mDockBtn, new Rect(dimensionPixelSize, getResources().getDimensionPixelSize(R.dimen.recents_dock_btn_margin_top), dimensionPixelSize, 0));
        fitsSystemWindowInsets(this.mMemoryAndClearContainer, (Rect) null);
        fitsSystemWindowInsets(this.mTxtMemoryContainer, (Rect) null);
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        try {
            this.mSetDummyTranslucentMethod = getClass().getMethod("setDummyTranslucent", new Class[]{Boolean.TYPE});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (Recents.getConfiguration().getLaunchState().launchedViaFsGesture && Recents.getConfiguration().getLaunchState().launchedFromHome) {
            z = true;
        }
        RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(z));
        registerContentObservers();
    }

    public static boolean isForceBlack() {
        return sForceBlack;
    }

    private void registerContentObservers() {
        if (Constants.IS_NOTCH) {
            getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_black"), false, this.mForceBlackObserver, -1);
            this.mForceBlackObserver.onChange(false);
        }
        if (Utilities.isSlideCoverDevice()) {
            getContentResolver().registerContentObserver(Settings.System.getUriFor("sc_status"), false, this.mSlideCoverObserver, -1);
        }
        getContentResolver().registerContentObserver(Settings.Secure.getUriFor("access_control_lock_mode"), false, this.mAccessControlLockModeObserver, -1);
        this.mAccessControlLockModeObserver.onChange(false);
        getContentResolver().registerContentObserver(Settings.System.getUriFor("miui_recents_show_recommend"), false, this.mShowRecommendObserver, -1);
        getContentResolver().registerContentObserver(CloudDataHelper.URI_CLOUD_ALL_DATA_NOTIFY, false, this.mShowRecommendObserver, -1);
        this.mShowRecommendObserver.onChange(false);
        if (UserHandle.myUserId() == 0) {
            getContentResolver().registerContentObserver(Settings.System.getUriFor("power_supersave_mode_open"), false, this.mSuperSavePowerObserver, UserHandle.myUserId());
            this.mSuperSavePowerObserver.onChange(false);
        }
    }

    private void unRegisterContentObservers() {
        if (Constants.IS_NOTCH) {
            getContentResolver().unregisterContentObserver(this.mForceBlackObserver);
        }
        if (Utilities.isSlideCoverDevice()) {
            getContentResolver().unregisterContentObserver(this.mSlideCoverObserver);
        }
        getContentResolver().unregisterContentObserver(this.mAccessControlLockModeObserver);
        getContentResolver().unregisterContentObserver(this.mShowRecommendObserver);
        if (UserHandle.myUserId() == 0) {
            getContentResolver().unregisterContentObserver(this.mSuperSavePowerObserver);
        }
    }

    /* access modifiers changed from: private */
    public void cleanInRecents() {
        long freeMemory = getFreeMemory();
        this.mFreeAtFirst = freeMemory;
        mFreeBeforeClean = freeMemory;
        deepClean();
        DismissAllTaskViewsEvent dismissAllTaskViewsEvent = new DismissAllTaskViewsEvent();
        dismissAllTaskViewsEvent.getAnimationTrigger().increment();
        RecentsEventBus.getDefault().send(dismissAllTaskViewsEvent);
        this.mDismissAllTaskViewEventTrigger = dismissAllTaskViewsEvent.getAnimationTrigger();
        this.mHandler.removeMessages(1000);
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1000), 300);
    }

    public void updateDockRegions(boolean z) {
        RecentsConfiguration.sCanMultiWindow = z;
        float f = 1.0f;
        if (this.mIsVisible) {
            this.mDockBtn.animate().alpha((z || this.mDockBtn.getTranslationY() != 0.0f) ? 0.0f : 1.0f).setDuration(50).start();
            this.mTxtMemoryContainer.animate().alpha((z || this.mTxtMemoryContainer.getTranslationY() != 0.0f) ? 0.0f : 1.0f).setDuration(50).start();
            RecentsRecommendView recentsRecommendView = this.mRecentsRecommendView;
            if (recentsRecommendView != null) {
                ViewPropertyAnimator animate = recentsRecommendView.animate();
                if (z) {
                    f = 0.0f;
                }
                animate.alpha(f).setDuration(50).start();
                this.mRecentsRecommendView.setAllItemClickable(!z);
            }
        } else {
            RecentsRecommendView recentsRecommendView2 = this.mRecentsRecommendView;
            if (recentsRecommendView2 != null) {
                if (z) {
                    f = 0.0f;
                }
                recentsRecommendView2.setAlpha(f);
                this.mRecentsRecommendView.setAllItemClickable(!z);
            }
        }
        if (z) {
            this.mRecentsView.announceForAccessibility(getString(R.string.accessibility_drag_hint_message));
            this.mRecentsView.showDockRegionsAnim();
            return;
        }
        this.mRecentsView.hideDockRegionsAnim();
    }

    private void fitsSystemWindowInsets(View view, final Rect rect) {
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                Rect systemWindowInsetsAsRect = WindowInsetsCompat.getSystemWindowInsetsAsRect(windowInsets);
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                Rect rect = rect;
                if (rect == null) {
                    marginLayoutParams.setMargins(systemWindowInsetsAsRect.left, 0, systemWindowInsetsAsRect.right, systemWindowInsetsAsRect.bottom);
                } else {
                    marginLayoutParams.setMargins(systemWindowInsetsAsRect.left + rect.left, rect.top + 0, systemWindowInsetsAsRect.right + rect.right, systemWindowInsetsAsRect.bottom + rect.bottom);
                }
                return windowInsets;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Log.d("RecentsActivity", "onStart");
        reloadStackView();
        RecentsEventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, true));
        MetricsLogger.visible(this, 224);
        if (!Recents.getConfiguration().getLaunchState().launchedViaFsGesture) {
            Recents.getSystemServices().changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 0.0f, 1.0f);
        }
        registerDisplayListener();
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        reloadStackView();
        this.mReceivedNewIntent = true;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.mNeedReloadStackView = true;
        this.mClearAnimView.stopAnimator();
        refreshMemoryInfo();
        this.mTxtMemoryContainer.setVisibility(isMemInfoShow() ? 0 : 4);
        setupVisible();
        if (this.mRecentsRecommendView != null) {
            RecentsPushEventHelper.sendShowRecommendCardEvent(this.mIsRecommendVisible);
        }
        updateBlurRatioIfNeed();
        if (!Recents.getSystemServices().hasDockedTask()) {
            setSystemUiVisibility();
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateBlurRatioIfNeed();
    }

    private void updateBlurRatioIfNeed() {
        if (!Recents.getConfiguration().getLaunchState().launchedViaFsGesture) {
            this.mRecentsView.updateBlurRatio(1.0f);
        }
    }

    private void startBlurAnim(float f, long j) {
        float blurRatio = this.mRecentsView.getBlurRatio();
        ValueAnimator valueAnimator = this.mBlurAnim;
        if (valueAnimator == null) {
            this.mBlurAnim = new ValueAnimator();
            this.mBlurAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    RecentsActivity.this.mRecentsView.updateBlurRatio(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
        } else {
            valueAnimator.cancel();
        }
        this.mBlurAnim.setFloatValues(new float[]{blurRatio, f});
        this.mBlurAnim.setDuration((long) ((((float) j) * Math.abs(f - blurRatio)) / 1.0f));
        this.mBlurAnim.start();
    }

    private void reloadStackView() {
        if (this.mNeedReloadStackView) {
            int i = 0;
            this.mNeedReloadStackView = false;
            RecentsTaskLoader taskLoader = Recents.getTaskLoader();
            RecentsTaskLoadPlan consumeInstanceLoadPlan = BaseRecentsImpl.consumeInstanceLoadPlan();
            if (consumeInstanceLoadPlan == null) {
                consumeInstanceLoadPlan = taskLoader.createLoadPlan(this);
            }
            RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
            if (!consumeInstanceLoadPlan.hasTasks()) {
                taskLoader.preloadTasks(consumeInstanceLoadPlan, launchState.launchedToTaskId, !launchState.launchedFromHome);
            }
            RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
            options.runningTaskId = launchState.launchedToTaskId;
            options.numVisibleTasks = launchState.launchedNumVisibleTasks;
            options.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
            taskLoader.loadTasks(this, consumeInstanceLoadPlan, options);
            TaskStack taskStack = consumeInstanceLoadPlan.getTaskStack();
            this.mRecentsView.onReload(this.mIsVisible, taskStack.getTaskCount() == 0);
            this.mRecentsView.updateStack(taskStack, true);
            if (!launchState.launchedFromHome && !launchState.launchedFromApp) {
                RecentsEventBus.getDefault().send(new EnterRecentsWindowAnimationCompletedEvent());
            }
            if (launchState.launchedWithAltTab) {
                MetricsLogger.count(this, "overview_trigger_alttab", 1);
            } else {
                MetricsLogger.count(this, "overview_trigger_nav_btn", 1);
            }
            if (launchState.launchedFromApp) {
                Task launchTarget = taskStack.getLaunchTarget();
                if (launchTarget != null) {
                    i = taskStack.indexOfStackTask(launchTarget);
                }
                MetricsLogger.count(this, "overview_source_app", 1);
                MetricsLogger.histogram(this, "overview_source_app_index", i);
            } else {
                MetricsLogger.count(this, "overview_source_home", 1);
            }
            MetricsLogger.histogram(this, "overview_task_count", this.mRecentsView.getStack().getTaskCount());
            this.mIsVisible = true;
            RecentsPushEventHelper.sendEnterRecentsEvent(taskStack, launchState.launchedViaFsGesture ? "fullScreen" : "clickButton", getResources().getConfiguration().orientation == 2 ? "landscape" : "portrait");
        }
    }

    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        this.mHandler.removeCallbacks(this.mSendEnterWindowAnimationCompleteRunnable);
        if (!this.mReceivedNewIntent) {
            this.mHandler.post(this.mSendEnterWindowAnimationCompleteRunnable);
        } else {
            this.mSendEnterWindowAnimationCompleteRunnable.run();
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.mIgnoreAltTabRelease = false;
        this.mIterateTrigger.stopDozing();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mRecentsView == null) {
            Log.e("RecentsActivity", "onConfigurationChanged error, mRecentsView==null.");
            return;
        }
        Configuration appConfiguration = Utilities.getAppConfiguration(this);
        int stackTaskCount = this.mRecentsView.getStack().getStackTaskCount();
        boolean z = true;
        boolean z2 = this.mLastDeviceOrientation != appConfiguration.orientation;
        RecentsEventBus recentsEventBus = RecentsEventBus.getDefault();
        boolean z3 = this.mLastDisplayDensity != appConfiguration.densityDpi;
        if (stackTaskCount <= 0) {
            z = false;
        }
        recentsEventBus.send(new ConfigurationChangedEvent(false, z2, z3, z));
        if (z2) {
            ViewGroup viewGroup = this.mMemoryAndClearContainer;
            viewGroup.setPadding(viewGroup.getPaddingLeft(), this.mMemoryAndClearContainer.getPaddingTop(), this.mMemoryAndClearContainer.getPaddingRight(), getResources().getDimensionPixelSize(R.dimen.recent_task_padding_bottom));
            this.mLastDeviceOrientation = appConfiguration.orientation;
            updateRecentsRecommendViewVisible();
        }
        this.mLastDisplayDensity = appConfiguration.densityDpi;
    }

    public void onMultiWindowModeChanged(boolean z) {
        if (this.mIsVisible) {
            RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
            RecentsTaskLoader taskLoader = Recents.getTaskLoader();
            RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this);
            taskLoader.preloadTasks(createLoadPlan, -1, false);
            RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
            options.numVisibleTasks = launchState.launchedNumVisibleTasks;
            options.numVisibleTaskThumbnails = launchState.launchedNumVisibleThumbnails;
            taskLoader.loadTasks(this, createLoadPlan, options);
            TaskStack taskStack = createLoadPlan.getTaskStack();
            int stackTaskCount = taskStack.getStackTaskCount();
            boolean z2 = stackTaskCount > 0;
            RecentsEventBus.getDefault().send(new ConfigurationChangedEvent(true, false, false, stackTaskCount > 0));
            RecentsEventBus.getDefault().send(new MultiWindowStateChangedEvent(z, z2, taskStack));
            setupVisible();
        }
        updateDockRegions(false);
        if (!z) {
            setSystemUiVisibility();
        }
        this.mIsInMultiWindowMode = z;
        setNotchPadding();
    }

    private void updateExitMultiModeBtnVisible(boolean z) {
        if (Utilities.supportsMultiWindow()) {
            if (z) {
                if (this.mExitMultiModeBtn == null) {
                    this.mExitMultiModeBtn = (Button) LayoutInflater.from(getApplicationContext()).inflate(R.layout.exit_multi_window_btn, (ViewGroup) null);
                    this.mExitMultiModeBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            RecentsEventBus.getDefault().send(new UndockingTaskEvent());
                            Log.i("RecentsActivity", "exit splitScreen mode ---- click exit button.");
                        }
                    });
                    Recents.getSystemServices().mWm.addView(this.mExitMultiModeBtn, getExitMultiModeBtnParams());
                    this.mIsAddExitMultiModeBtn = true;
                }
                this.mExitMultiModeBtn.setVisibility(0);
                return;
            }
            Button button = this.mExitMultiModeBtn;
            if (button != null && this.mIsAddExitMultiModeBtn) {
                button.setVisibility(8);
            }
        }
    }

    private WindowManager.LayoutParams getExitMultiModeBtnParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2027, 40, -3);
        layoutParams.gravity = 49;
        layoutParams.windowAnimations = R.style.Animation_StatusBarBlur;
        layoutParams.setTitle("ExitMultiModeBtn");
        return layoutParams;
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("RecentsActivity", "onStop");
        this.mIsVisible = false;
        this.mReceivedNewIntent = false;
        RecentsEventBus.getDefault().send(new RecentsVisibilityChangedEvent(this, false));
        MetricsLogger.hidden(this, 224);
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        if (!launchState.launchedViaFsGesture) {
            resetHomeAlphaScale();
        }
        launchState.reset();
        updateExitMultiModeBtnVisible(false);
        resetToNormalState();
        TaskStackView.setIsChangingConfigurations(isChangingConfigurations());
        Recents.getSystemServices().endProlongedAnimations();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (!this.mFinishedOnStartup) {
            unregisterReceiver(this.mSystemBroadcastReceiver);
            this.mPackageMonitor.unregister();
            RecentsEventBus.getDefault().unregister(this);
            if (this.mExitMultiModeBtn != null && this.mIsAddExitMultiModeBtn) {
                Recents.getSystemServices().mWm.removeView(this.mExitMultiModeBtn);
                this.mIsAddExitMultiModeBtn = false;
            }
            unRegisterDisplayListener();
            unRegisterContentObservers();
            RecentsView recentsView = this.mRecentsView;
            if (recentsView != null) {
                recentsView.release();
            }
        }
    }

    public void onTrimMemory(int i) {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        if (taskLoader != null) {
            taskLoader.onTrimMemory(i);
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (i == 19) {
            RecentsEventBus.getDefault().send(new FocusNextTaskViewEvent(0));
            return true;
        } else if (i == 20) {
            RecentsEventBus.getDefault().send(new FocusPreviousTaskViewEvent());
            return true;
        } else if (i == 61) {
            boolean z = SystemClock.elapsedRealtime() - this.mLastTabKeyEventTime > ((long) getResources().getInteger(R.integer.recents_alt_tab_key_delay));
            if (keyEvent.getRepeatCount() <= 0 || z) {
                if (keyEvent.isShiftPressed()) {
                    RecentsEventBus.getDefault().send(new FocusPreviousTaskViewEvent());
                } else {
                    RecentsEventBus.getDefault().send(new FocusNextTaskViewEvent(0));
                }
                this.mLastTabKeyEventTime = SystemClock.elapsedRealtime();
                if (keyEvent.isAltPressed()) {
                    this.mIgnoreAltTabRelease = false;
                }
            }
            return true;
        } else if ((i != 67 && i != 112) || keyEvent.getRepeatCount() > 0) {
            return super.onKeyDown(i, keyEvent);
        } else {
            RecentsEventBus.getDefault().send(new DismissFocusedTaskViewEvent());
            MetricsLogger.histogram(this, "overview_task_dismissed_source", 0);
            return true;
        }
    }

    public void onUserInteraction() {
        RecentsEventBus.getDefault().send(this.mUserInteractionEvent);
    }

    public void onBackPressed() {
        if (RecentsConfiguration.sCanMultiWindow) {
            updateDockRegions(false);
        } else if (this.mRecentsView.getMenuView().isShowing()) {
            this.mRecentsView.getMenuView().removeMenu(true);
        } else {
            RecentsEventBus.getDefault().send(new ToggleRecentsEvent());
            RecentsPushEventHelper.sendHideRecentsEvent(Recents.getConfiguration().getLaunchState().launchedViaFsGesture ? "backGesture" : "clickBackKey");
        }
    }

    public final void onBusEvent(ToggleRecentsEvent toggleRecentsEvent) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            dismissRecentsToHome(true);
        } else {
            dismissRecentsToLaunchTargetTaskOrHome();
        }
    }

    public final void onBusEvent(IterateRecentsEvent iterateRecentsEvent) {
        int i;
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled()) {
            i = getResources().getInteger(R.integer.recents_subsequent_auto_advance_duration);
            this.mIterateTrigger.setDozeDuration(i);
            if (!this.mIterateTrigger.isDozing()) {
                this.mIterateTrigger.startDozing();
            } else {
                this.mIterateTrigger.poke();
            }
        } else {
            i = 0;
        }
        RecentsEventBus.getDefault().send(new FocusNextTaskViewEvent(i));
        MetricsLogger.action(this, 276);
    }

    public final void onBusEvent(UserInteractionEvent userInteractionEvent) {
        this.mIterateTrigger.stopDozing();
    }

    public final void onBusEvent(HideRecentsEvent hideRecentsEvent) {
        if (RecentsConfiguration.sCanMultiWindow) {
            updateDockRegions(false);
            if (!hideRecentsEvent.triggeredFromScroll) {
                return;
            }
        }
        if (hideRecentsEvent.triggeredFromAltTab) {
            if (!this.mIgnoreAltTabRelease) {
                dismissRecentsToTargetTaskOrHome();
            }
        } else if (hideRecentsEvent.triggeredFromHomeKey || hideRecentsEvent.triggeredFromScroll) {
            dismissRecentsToHome(true);
            RecentsEventBus.getDefault().send(this.mUserInteractionEvent);
        } else if (hideRecentsEvent.triggeredFromFsGesture) {
            this.mRecentsView.launchPreviousTask();
        }
    }

    public final void onBusEvent(EnterRecentsWindowLastAnimationFrameEvent enterRecentsWindowLastAnimationFrameEvent) {
        RecentsEventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(true));
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitRecentsWindowFirstAnimationFrameEvent exitRecentsWindowFirstAnimationFrameEvent) {
        if (this.mRecentsView.isLastTaskLaunchedFreeform()) {
            RecentsEventBus.getDefault().send(new UpdateFreeformTaskViewVisibilityEvent(false));
        }
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
        SpringAnimationUtils.getInstance().startLaunchTaskSucceededAnim(this.mRecentsView, exitRecentsWindowFirstAnimationFrameEvent.launchTask);
    }

    public final void onBusEvent(EnterRecentsWindowFirstAnimationFrameEvent enterRecentsWindowFirstAnimationFrameEvent) {
        SpringAnimationUtils.getInstance().startAppToRecentsAnim(this.mRecentsView);
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent dockedFirstAnimationFrameEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(CancelEnterRecentsWindowAnimationEvent cancelEnterRecentsWindowAnimationEvent) {
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        int i = launchState.launchedToTaskId;
        if (i != -1) {
            Task task = cancelEnterRecentsWindowAnimationEvent.launchTask;
            if (task == null || i != task.key.id) {
                SystemServicesProxy systemServices = Recents.getSystemServices();
                systemServices.cancelWindowTransition(launchState.launchedToTaskId);
                systemServices.cancelThumbnailTransition(getTaskId());
            }
        }
    }

    public final void onBusEvent(ShowApplicationInfoEvent showApplicationInfoEvent) {
        Intent intent = new Intent();
        String packageName = showApplicationInfoEvent.task.key.getComponent().getPackageName();
        if (Build.IS_TABLET) {
            if (!showHybridApplicationInfo(intent, packageName, showApplicationInfoEvent.task.key.baseIntent)) {
                intent.setAction((String) null);
                intent.setClassName("com.android.settings", "com.android.settings.applications.InstalledAppDetailsTop");
            }
            intent.putExtra("package", packageName);
            intent.setFlags(335544320);
            if (XSpaceUserHandle.isXSpaceUserId(showApplicationInfoEvent.task.key.userId)) {
                intent.putExtra("is_xspace_app", true);
            } else {
                intent.putExtra("is_xspace_app", false);
            }
        } else {
            if (!showHybridApplicationInfo(intent, packageName, showApplicationInfoEvent.task.key.baseIntent)) {
                intent.setAction("miui.intent.action.APP_MANAGER_APPLICATION_DETAIL");
            }
            intent.putExtra(d.am, packageName);
            intent.putExtra("miui.intent.extra.USER_ID", showApplicationInfoEvent.task.key.userId);
            intent.setFlags(276824064);
        }
        try {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities((Bundle) null, UserHandle.CURRENT);
        } catch (Exception e) {
            Log.e("RecentsActivity", "ShowApplicationInfo", e);
        }
        MetricsLogger.count(this, "overview_app_info", 1);
    }

    private boolean showHybridApplicationInfo(Intent intent, String str, Intent intent2) {
        if (!str.equals("com.miui.hybrid") || intent.setAction("com.miui.hybrid.action.APP_DETAIL_MANAGER").resolveActivity(getPackageManager()) == null) {
            return false;
        }
        intent.putExtra("base_intent", intent2);
        return true;
    }

    public final void onBusEvent(ShowIncompatibleAppOverlayEvent showIncompatibleAppOverlayEvent) {
        if (this.mIncompatibleAppOverlay == null) {
            this.mIncompatibleAppOverlay = Utilities.findViewStubById((Activity) this, (int) R.id.incompatible_app_overlay_stub).inflate();
            setIncompatibleOverlayPadding();
            this.mIncompatibleAppOverlay.setWillNotDraw(false);
            this.mIncompatibleAppOverlay.setVisibility(0);
        }
        this.mIncompatibleAppOverlay.animate().alpha(1.0f).setDuration(150).setInterpolator(Interpolators.ALPHA_IN).start();
    }

    public final void onBusEvent(HideIncompatibleAppOverlayEvent hideIncompatibleAppOverlayEvent) {
        View view = this.mIncompatibleAppOverlay;
        if (view != null) {
            view.animate().alpha(0.0f).setDuration(150).setInterpolator(Interpolators.ALPHA_OUT).start();
        }
    }

    public final void onBusEvent(DeleteTaskDataEvent deleteTaskDataEvent) {
        if (!ApplicationHelper.shouldKeeAlive(this, deleteTaskDataEvent.task.key.getComponent().getPackageName(), deleteTaskDataEvent.task.key.userId)) {
            Recents.getTaskLoader().deleteTaskData(deleteTaskDataEvent.task, false);
            Slog.d("RecentsActivity", "removeTask: " + deleteTaskDataEvent.task.toString());
            SystemServicesProxy systemServices = Recents.getSystemServices();
            if (!deleteTaskDataEvent.remainProcess) {
                systemServices.killProcess(deleteTaskDataEvent.task);
            }
        }
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent allTaskViewsDismissedEvent) {
        if (!Recents.getSystemServices().hasDockedTask() || !allTaskViewsDismissedEvent.mEmpty) {
            if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
                dismissRecentsToHome(false);
            } else {
                dismissRecentsToTargetTaskOrHome();
            }
        } else if (!allTaskViewsDismissedEvent.mFromDockGesture) {
            this.mRecentsView.showEmptyView(allTaskViewsDismissedEvent.msgResId);
            this.mTipView.setVisibility(4);
        }
        MetricsLogger.count(this, "overview_task_all_dismissed", 1);
    }

    public final void onBusEvent(LaunchTaskSucceededEvent launchTaskSucceededEvent) {
        MetricsLogger.histogram(this, "overview_task_launch_index", launchTaskSucceededEvent.taskIndexFromStackFront);
        RecentsPushEventHelper.sendHideRecentsEvent("switchApp");
        if (Recents.getConfiguration().getLaunchState().launchedViaFsGesture) {
            this.mNeedMoveRecentsToFrontOfFsGesture = false;
            RecentsEventBus.getDefault().send(new AnimFirstTaskViewAlphaEvent(1.0f, false));
        }
        resetToNormalState();
        startRecentsContainerFadeOutAnim(0, 120);
    }

    public final void onBusEvent(LaunchTaskFailedEvent launchTaskFailedEvent) {
        dismissRecentsToHome(true);
        MetricsLogger.count(this, "overview_task_launch_failed", 1);
    }

    public final void onBusEvent(ScreenPinningRequestEvent screenPinningRequestEvent) {
        MetricsLogger.count(this, "overview_screen_pinned", 1);
    }

    public final void onBusEvent(DebugFlagsChangedEvent debugFlagsChangedEvent) {
        finish();
    }

    public final void onBusEvent(StackViewScrolledEvent stackViewScrolledEvent) {
        this.mIgnoreAltTabRelease = true;
    }

    public final void onBusEvent(DockedTopTaskEvent dockedTopTaskEvent) {
        this.mRecentsView.getViewTreeObserver().addOnPreDrawListener(this.mRecentsDrawnEventListener);
        this.mRecentsView.invalidate();
    }

    public final void onBusEvent(ExitMultiModeEvent exitMultiModeEvent) {
        Button button = this.mExitMultiModeBtn;
        if (button != null && this.mIsAddExitMultiModeBtn) {
            button.setVisibility(8);
        }
    }

    public final void onBusEvent(ChangeTaskLockStateEvent changeTaskLockStateEvent) {
        Task task = changeTaskLockStateEvent.task;
        String packageName = task.key.getComponent().getPackageName();
        if (ApplicationHelper.shouldKeeAlive(getApplicationContext(), packageName, task.key.userId)) {
            Slog.d("Enterprise", "Package " + packageName + " is protected");
            return;
        }
        task.isLocked = changeTaskLockStateEvent.isLocked;
        ProcessManagerHelper.updateApplicationLockedState(packageName, task.key.userId, task.isLocked);
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent enterRecentsWindowAnimationCompletedEvent) {
        if (!Recents.getConfiguration().getLaunchState().launchedViaFsGesture) {
            startRecentsContainerFadeInAnim(0, 150);
        }
    }

    public final void onBusEvent(DismissRecentsToHomeAnimationStarted dismissRecentsToHomeAnimationStarted) {
        startRecentsContainerFadeOutAnim(0, 50);
    }

    public final void onBusEvent(CleanInRecentsEvents cleanInRecentsEvents) {
        cleanInRecents();
    }

    public final void onBusEvent(FsGestureEnterRecentsZoomEvent fsGestureEnterRecentsZoomEvent) {
        this.mFsZoom = true;
        SpringAnimationUtils.getInstance().startFsZoomAnim(this.mRecentsView, fsGestureEnterRecentsZoomEvent.mAnimEndRunnable);
        startRecentsContainerFadeInAnim(0, 200);
    }

    public final void onBusEvent(FsGestureEnterRecentsEvent fsGestureEnterRecentsEvent) {
        this.mFsSlideIn = false;
        this.mFsZoom = false;
        this.mNeedMoveRecentsToFrontOfFsGesture = true;
        this.mRecentsView.updateBlurRatio(0.0f);
    }

    public final void onBusEvent(FsGestureEnterRecentsCompleteEvent fsGestureEnterRecentsCompleteEvent) {
        if (fsGestureEnterRecentsCompleteEvent.mMoveRecentsToFront && this.mNeedMoveRecentsToFrontOfFsGesture) {
            ((ActivityManager) getSystemService("activity")).moveTaskToFront(getTaskId(), 0);
            this.mRecentsView.updateBlurRatio(1.0f);
        }
        RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(false));
        if (!this.mFsZoom) {
            SpringAnimationUtils.getInstance().cancelSlideInSpringAnim(this.mRecentsView);
            completeFsGestureEnter();
        }
    }

    public final void onBusEvent(FsGestureSlideInEvent fsGestureSlideInEvent) {
        if (!this.mFsZoom) {
            this.mFsSlideIn = true;
            SpringAnimationUtils.getInstance().startSlideInSpringAnim(this.mRecentsView, fsGestureSlideInEvent.mPositionX, fsGestureSlideInEvent.mPositionY);
            this.mBackGround.animate().alpha(1.0f).setDuration(200).start();
            startBlurAnim(1.0f, 200);
        }
    }

    public final void onBusEvent(FsGestureMoveEvent fsGestureMoveEvent) {
        SpringAnimationUtils.getInstance().startFsMoveAnim(this.mRecentsView, fsGestureMoveEvent.mTouchX, fsGestureMoveEvent.mTouchY);
    }

    public final void onBusEvent(FsGestureSlideOutEvent fsGestureSlideOutEvent) {
        if (Recents.getConfiguration().getLaunchState().launchedFromHome) {
            this.mBackGround.animate().alpha(0.0f).setDuration(200).start();
        }
        startBlurAnim(0.0f, 150);
        SpringAnimationUtils.getInstance().startSlideOutSpringAnim(this.mRecentsView);
    }

    public final void onBusEvent(FsGestureRecentsModeSlideInEvent fsGestureRecentsModeSlideInEvent) {
        startBlurAnim(1.0f, 200);
        this.mBackGround.animate().alpha(1.0f).setDuration(200).start();
        startRecentsContainerFadeInAnim(0, 200);
        SpringAnimationUtils.getInstance().startFsGestureRecentsModeSlideInAnim(this.mRecentsView);
    }

    public final void onBusEvent(FsGestureRecentsModeSlideOutEvent fsGestureRecentsModeSlideOutEvent) {
        startBlurAnim(0.0f, 200);
        resetToNormalState();
        this.mBackGround.animate().alpha(0.0f).setDuration(200).start();
        startRecentsContainerFadeOutAnim(0, 200);
        RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(true));
        SpringAnimationUtils.getInstance().startFsGestureRecentsModeSlideOutAnim(this.mRecentsView);
    }

    public final void onBusEvent(FsGestureRecentsModeResetEvent fsGestureRecentsModeResetEvent) {
        startBlurAnim(1.0f, 200);
        this.mBackGround.animate().alpha(1.0f).setDuration(200).start();
        startRecentsContainerFadeInAnim(0, 200);
        SpringAnimationUtils.getInstance().startFsGestureRecentsModeResetAnim(this.mRecentsView, new Runnable() {
            public void run() {
                RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(false));
            }
        });
    }

    public final void onBusEvent(ActivitySetDummyTranslucentEvent activitySetDummyTranslucentEvent) {
        Method method = this.mSetDummyTranslucentMethod;
        if (method != null) {
            try {
                method.invoke(this, new Object[]{Boolean.valueOf(activitySetDummyTranslucentEvent.mIsTranslucent)});
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
        }
    }

    public final void onBusEvent(StartSmallWindowEvent startSmallWindowEvent) {
        dismissRecentsToHome(true);
    }

    public final void onBusEvent(HideMemoryAndDockEvent hideMemoryAndDockEvent) {
        startRecentsContainerFadeOutAnim(0, 180);
        this.mDockBtn.setEnabled(false);
        this.mClearAnimView.setEnabled(false);
    }

    public final void onBusEvent(ShowMemoryAndDockEvent showMemoryAndDockEvent) {
        startRecentsContainerFadeInAnim(0, 180);
        this.mDockBtn.setEnabled(true);
        this.mClearAnimView.setEnabled(true);
    }

    public final void onBusEvent(StackScrollChangedEvent stackScrollChangedEvent) {
        int i;
        RecentsRecommendView recentsRecommendView = this.mRecentsRecommendView;
        if (recentsRecommendView == null || recentsRecommendView.getVisibility() != 0) {
            i = Math.min(stackScrollChangedEvent.mTranslationY + Math.max(this.mRecentsView.getTaskViewPaddingView() - this.mDockBtn.getBottom(), 0), 0);
        } else {
            i = Math.min(stackScrollChangedEvent.mTranslationY, 0);
            this.mRecentsRecommendView.setTranslationY((float) i);
        }
        float f = (float) i;
        this.mDockBtn.setTranslationY(f);
        this.mTxtMemoryContainer.setTranslationY(f);
        float f2 = 0.0f;
        if (this.mMemoryAndClearContainer.getAlpha() != 0.0f) {
            if (!RecentsConfiguration.sCanMultiWindow) {
                f2 = Math.max((f / 100.0f) + 1.0f, 0.0f);
            }
            this.mDockBtn.setAlpha(f2);
            this.mTxtMemoryContainer.setAlpha(f2);
        }
        this.mTipView.setTranslationY((float) Math.min(stackScrollChangedEvent.mTranslationY, 0));
    }

    public final void onBusEvent(ScrollerFlingFinishEvent scrollerFlingFinishEvent) {
        if (this.mDockBtn.getAlpha() < 1.0f || this.mTxtMemoryContainer.getAlpha() < 1.0f) {
            this.mDockBtn.setAlpha(0.0f);
            this.mTxtMemoryContainer.setAlpha(0.0f);
        }
    }

    public void prepareFsGestureEnter() {
        this.mRecentsView.setAlpha(0.0f);
        this.mRecentsView.setTranslationY(0.0f);
        this.mBackGround.setAlpha(0.0f);
        this.mRecentsContainer.setAlpha(0.0f);
        for (TaskView headerView : this.mRecentsView.getTaskViews()) {
            headerView.getHeaderView().setAlpha(0.0f);
        }
    }

    public void completeFsGestureEnter() {
        this.mRecentsView.resetProperty();
        this.mBackGround.setAlpha(1.0f);
        this.mRecentsContainer.setAlpha(1.0f);
        for (TaskView next : this.mRecentsView.getTaskViews()) {
            next.getHeaderView().setAlpha(1.0f);
            next.setTranslationX(0.0f);
            next.setTranslationY(0.0f);
        }
    }

    public final void checkFsGestureOnEnterRecents() {
        Recents recents = (Recents) ((Application) getApplication()).getSystemUIApplication().getComponent(Recents.class);
        boolean isHideRecentsViewByFsGesture = (recents == null || recents.getRecentsImpl() == null) ? false : recents.getRecentsImpl().getIsHideRecentsViewByFsGesture();
        if (Recents.getConfiguration().getLaunchState().launchedViaFsGesture) {
            if (!this.mFsSlideIn && !this.mFsZoom) {
                prepareFsGestureEnter();
            }
            if (isHideRecentsViewByFsGesture && !Recents.getConfiguration().getLaunchState().launchedFromHome) {
                RecentsEventBus.getDefault().send(new AnimFirstTaskViewAlphaEvent(0.0f, false, true));
                return;
            }
            return;
        }
        this.mRecentsView.resetProperty();
    }

    public boolean onPreDraw() {
        this.mRecentsView.getViewTreeObserver().removeOnPreDrawListener(this);
        this.mRecentsView.post(new Runnable() {
            public void run() {
                Recents.getSystemServices().endProlongedAnimations();
            }
        });
        return true;
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        RecentsEventBus.getDefault().dump(str, printWriter);
        Recents.getTaskLoader().dump(str, printWriter);
        String hexString = Integer.toHexString(System.identityHashCode(this));
        printWriter.print(str);
        printWriter.print("RecentsActivity");
        printWriter.print(" visible=");
        printWriter.print(this.mIsVisible ? "Y" : "N");
        printWriter.print(" [0x");
        printWriter.print(hexString);
        printWriter.print("]");
        printWriter.println();
        RecentsView recentsView = this.mRecentsView;
        if (recentsView != null) {
            recentsView.dump(str, printWriter);
        }
        if (strArr != null && strArr.length > 0) {
            if ("enableDebugRecents".equals(strArr[0])) {
                SystemServicesProxy.DEBUG = true;
                RecentsEventBus.DEBUG_TRACE_ALL = true;
            } else if ("disableDebugRecents".equals(strArr[0])) {
                SystemServicesProxy.DEBUG = false;
                RecentsEventBus.DEBUG_TRACE_ALL = false;
            }
        }
    }

    public long getFreeMemory() {
        long j;
        try {
            j = PerfShielderManager.getFreeMemory().longValue();
        } catch (Exception e) {
            Log.e("RecentsActivity", "getFreeMemory", e);
            j = Process.getFreeMemory();
        }
        Log.d("RecentsActivity", "getFreeMemory:" + j);
        return j / 1024;
    }

    public void refreshMemoryInfo() {
        String formatedMemory = getFormatedMemory(Math.max(mFreeBeforeClean, getFreeMemory()), false);
        String formatedMemory2 = getFormatedMemory(this.mTotalMemory, true);
        this.mTxtMemoryInfo1.setText(getString(R.string.status_bar_recent_memory_info1, new Object[]{formatedMemory, formatedMemory2}));
        this.mTxtMemoryInfo2.setText(getString(R.string.status_bar_recent_memory_info2, new Object[]{formatedMemory, formatedMemory2}));
        this.mSeparatorForMemoryInfo.setVisibility(TextUtils.isEmpty(this.mTxtMemoryInfo1.getText()) || TextUtils.isEmpty(this.mTxtMemoryInfo2.getText()) ? 8 : 0);
        this.mClearAnimView.setContentDescription(getString(R.string.accessibility_recent_task_memory_info, new Object[]{formatedMemory, formatedMemory2}));
    }

    /* access modifiers changed from: private */
    public void endForClear() {
        RecentsEventBus.getDefault().post(new DismissAllTaskViewsEndEvent());
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                long freeMemory = RecentsActivity.this.getFreeMemory();
                RecentsPushEventHelper.sendOneKeyCleanEvent(RecentsActivity.this.mFreeAtFirst, freeMemory, RecentsActivity.this.mTotalMemory);
                Toast makeText = Toast.makeText(RecentsActivity.this.getApplicationContext(), RecentsActivity.getToastMsg(RecentsActivity.this.getApplicationContext(), RecentsActivity.this.mFreeAtFirst, freeMemory), 0);
                if (makeText.getWindowParams() != null) {
                    makeText.getWindowParams().privateFlags |= 16;
                }
                makeText.show();
            }
        }, 300);
    }

    private void updateDockBtnVisible() {
        this.mDockBtn.setVisibility((RecentsConfiguration.sCanMultiWindow || this.mRecentsView.getStack().getStackTaskCount() <= 0 || isInMultiWindowMode() || !Utilities.supportsMultiWindow() || Utilities.isInSmallWindowMode(this) || this.mRecentsView.getMenuView().isShowing() || Utilities.IS_MIUI_LITE_VERSION) ? 4 : 0);
    }

    private boolean isMemInfoShow() {
        return MiuiSettings.System.getBooleanForUser(getApplicationContext().getContentResolver(), "miui_recents_show_mem_info", false, -2);
    }

    /* access modifiers changed from: private */
    public void doClearAnim() {
        refreshMemoryInfo();
        this.mClearAnimView.animatorStart(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                if (RecentsActivity.this.mDismissAllTaskViewEventTrigger != null) {
                    RecentsActivity.this.mDismissAllTaskViewEventTrigger.decrement();
                    final ReferenceCountedTrigger access$1100 = RecentsActivity.this.mDismissAllTaskViewEventTrigger;
                    RecentsActivity.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            access$1100.flushLastDecrementRunnables();
                        }
                    }, 300);
                    ReferenceCountedTrigger unused = RecentsActivity.this.mDismissAllTaskViewEventTrigger = null;
                }
                RecentsActivity.this.endForClear();
            }
        });
    }

    private void setupVisible() {
        boolean z = true;
        int i = 0;
        updateExitMultiModeBtnVisible(this.mIsVisible && isInMultiWindowMode());
        updateDockBtnVisible();
        updateRecentsRecommendViewVisible();
        if (isInMultiWindowMode()) {
            z = false;
        }
        int stackTaskCount = this.mRecentsView.getStack().getStackTaskCount();
        this.mMemoryAndClearContainer.setVisibility((!z || stackTaskCount <= 0) ? 4 : 0);
        this.mTxtMemoryContainer.setVisibility((!z || !isMemInfoShow()) ? 4 : 0);
        TextView textView = this.mTipView;
        if (!isInMultiWindowMode() || stackTaskCount <= 0 || this.mRecentsView.getMenuView().isShowing()) {
            i = 4;
        }
        textView.setVisibility(i);
    }

    /* access modifiers changed from: private */
    public void updateRecentsRecommendViewVisible() {
        if (this.mRecentsRecommendView != null) {
            boolean z = true;
            int i = 0;
            if (isInMultiWindowMode() || this.mRecentsView.getMenuView().isShowing() || !this.mIsShowRecommend || this.mLastDeviceOrientation != 1) {
                z = false;
            }
            this.mIsRecommendVisible = z;
            RecentsRecommendView recentsRecommendView = this.mRecentsRecommendView;
            if (!this.mIsRecommendVisible) {
                i = 4;
            }
            recentsRecommendView.setVisibility(i);
        }
    }

    private void deepClean() {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                ArrayList<Task> stackTasks = RecentsActivity.this.mRecentsView.getStack().getStackTasks();
                ArrayList arrayList = new ArrayList();
                Iterator<Task> it = stackTasks.iterator();
                while (it.hasNext()) {
                    arrayList.add(Integer.valueOf(it.next().key.id));
                }
                ProcessConfig processConfig = new ProcessConfig(1);
                processConfig.setRemoveTaskNeeded(true);
                processConfig.setRemovingTaskIdList(arrayList);
                ProcessManager.kill(processConfig);
                if (RecentsActivity.this.mHandler.hasMessages(1000)) {
                    RecentsActivity.this.mHandler.removeMessages(1000);
                    RecentsActivity.this.mHandler.sendMessage(RecentsActivity.this.mHandler.obtainMessage(1000));
                }
            }
        });
    }

    public void resetHomeAlphaScale() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (!systemServices.isFsGestureAnimating()) {
            systemServices.changeAlphaScaleForFsGesture(Constants.HOME_LAUCNHER_PACKAGE_NAME, 1.0f, 1.0f);
        }
    }

    public static String getToastMsg(Context context, long j, long j2) {
        long max = Math.max(j2 - j, 0);
        if (max <= 10240) {
            return context.getResources().getString(286130389);
        }
        long j3 = max / 1024;
        if (j3 < 1024) {
            return context.getResources().getString(R.string.memory_clear_result_mega, new Object[]{String.format(Locale.getDefault(), "%d", new Object[]{Long.valueOf(j3)})});
        }
        return context.getResources().getString(R.string.memory_clear_result_giga, new Object[]{String.format(Locale.getDefault(), "%.1f", new Object[]{Float.valueOf(((float) j3) / 1024.0f)})});
    }

    public String getFormatedMemory(long j, boolean z) {
        long j2;
        long j3 = j / 1024;
        if (j3 < 1024) {
            return j3 + " M";
        }
        if (z) {
            j2 = (long) (Math.ceil(((double) j3) / 1024.0d) * 10.0d);
        } else {
            j2 = Math.round((((double) j3) * 10.0d) / 1024.0d);
        }
        long j4 = j2 / 10;
        long j5 = j2 % 10;
        if (j5 != 0) {
            return j4 + "." + j5 + " G";
        }
        return j4 + " G";
    }

    public void setIncompatibleOverlayPadding() {
        if (this.mIncompatibleAppOverlay != null) {
            boolean z = MiuiSettings.Global.getBoolean(getContentResolver(), "force_black_v2");
            if (!Constants.IS_NOTCH || z || this.mRotation != 0) {
                this.mIncompatibleAppOverlay.setPadding(0, 0, 0, 0);
                return;
            }
            this.mIncompatibleAppOverlay.setPadding(0, getResources().getDimensionPixelSize(R.dimen.status_bar_height), 0, 0);
        }
    }

    private void registerDisplayListener() {
        if (Constants.IS_NOTCH) {
            ((DisplayManager) getSystemService("display")).registerDisplayListener(this.mDisplayListener, (Handler) null);
            this.mDisplayListener.onDisplayChanged(0);
        }
    }

    private void unRegisterDisplayListener() {
        if (Constants.IS_NOTCH) {
            ((DisplayManager) getSystemService("display")).unregisterDisplayListener(this.mDisplayListener);
        }
    }

    public void setNotchPadding() {
        if (this.mRecentsContainer != null && this.mRecentsView != null) {
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            int i = this.mRotation == 1 ? dimensionPixelSize : 0;
            int i2 = this.mRotation == 3 ? dimensionPixelSize : 0;
            if (this.mRotation != 0) {
                dimensionPixelSize = 0;
            }
            if (this.mIsInMultiWindowMode) {
                dimensionPixelSize = 0;
                i = 0;
            }
            if (Utilities.isAndroidPorNewer()) {
                i = 0;
                i2 = 0;
            }
            this.mRecentsContainer.setPadding(i, dimensionPixelSize, i2, 0);
            this.mRecentsView.requstLayoutTaskStackView();
        }
    }

    private void setSystemUiVisibility() {
        this.mRecentsView.setSystemUiVisibility(772);
    }

    private void addRecentsRecommendViewIfNeeded() {
        if (Utilities.isShowRecentsRecommend()) {
            this.mRecentsRecommendView = (RecentsRecommendView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.recents_recommend_view, (ViewGroup) null);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -2);
            Point point = new Point();
            ((WindowManager) getSystemService("window")).getDefaultDisplay().getRealSize(point);
            int min = Math.min(point.x, point.y);
            int i = (min - (((int) ((((float) min) * getResources().getFloat(R.dimen.recents_task_rect_scale)) - ((float) getResources().getDimensionPixelSize(R.dimen.recents_task_view_padding)))) * 2)) / 3;
            layoutParams.setMargins(i, (int) Utilities.dpToPx(getResources(), 60.0f), i, 0);
            this.mRecentsContainer.addView(this.mRecentsRecommendView, layoutParams);
        }
    }

    public void startRecentsContainerFadeInAnim(long j, long j2) {
        this.mRecentsContainer.animate().alpha(1.0f).setStartDelay(j).setDuration(j2).setInterpolator(new SineEaseInOutInterpolator()).start();
        this.mClearAnimView.animate().scaleX(1.0f).scaleY(1.0f).setStartDelay(j).setDuration(j2).setInterpolator(new SineEaseInOutInterpolator()).start();
    }

    public void startRecentsContainerFadeOutAnim(long j, long j2) {
        this.mRecentsContainer.animate().alpha(0.0f).setStartDelay(j).setDuration(j2).setInterpolator(new SineEaseOutInterpolator()).start();
        this.mClearAnimView.animate().scaleX(0.8f).scaleY(0.8f).setStartDelay(j).setDuration(j2).setInterpolator(new SineEaseOutInterpolator()).start();
    }

    private void resetToNormalState() {
        this.mRecentsView.getMenuView().removeMenu(true);
        updateDockRegions(false);
    }
}
