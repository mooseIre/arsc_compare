package com.android.keyguard.magazine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.keyguard.Ease$Cubic;
import com.android.keyguard.Ease$Quint;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.clock.KeyguardClockContainer;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.Application;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.google.gson.Gson;
import com.xiaomi.stat.MiStat;
import miui.os.Build;
import miui.view.MiuiHapticFeedbackConstants;
import org.json.JSONObject;

public class LockScreenMagazineController {
    private static volatile LockScreenMagazineController sInstance;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("LockScreenMagazineController", "received broadcast " + action);
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REPLACED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                String dataString = intent.getDataString();
                if (!TextUtils.isEmpty(dataString)) {
                    dataString = dataString.split(":")[1];
                }
                if (!TextUtils.isEmpty(dataString) && LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME.equals(dataString)) {
                    Log.d("LockScreenMagazineController", "lock screen magazine package changed");
                    LockScreenMagazineController.this.mHandler.removeMessages(2);
                    LockScreenMagazineController.this.mHandler.sendEmptyMessageDelayed(2, 1000);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private long mCurrentTouchDownTime;
    private ValueAnimator mFullScreenAnimator;
    private RemoteViews mFullScreenRemoteView;
    private int mGXZWIconCenterX;
    private int mGXZWIconCenterY;
    private ContentObserver mGestureWakeupModeContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            LockScreenMagazineController lockScreenMagazineController = LockScreenMagazineController.this;
            boolean unused = lockScreenMagazineController.mOpenDoubleTapGoToSleep = MiuiSettings.System.getBoolean(lockScreenMagazineController.mContext.getContentResolver(), "gesture_wakeup", false);
        }
    };
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                LockScreenMagazineController.this.closeLockScreenMagazineStatus();
            } else if (i == 2) {
                LockScreenMagazineController.this.initLockScreenMagazinePreRes();
            } else if (i == 3 && LockScreenMagazineController.this.mStartedWakingUp) {
                LockScreenMagazineController.this.handleSingleClickEvent();
            }
        }
    };
    /* access modifiers changed from: private */
    public long mInitPreResElapsedRealtime = SystemClock.elapsedRealtime();
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsDecoupleHome;
    /* access modifiers changed from: private */
    public boolean mIsLockScreenMagazineOpenedWallpaper;
    /* access modifiers changed from: private */
    public boolean mIsLockScreenMagazinePkgExist;
    /* access modifiers changed from: private */
    public boolean mIsSupportLeftOverlay;
    /* access modifiers changed from: private */
    public boolean mIsSwitchAnimating;
    /* access modifiers changed from: private */
    public KeyguardBottomAreaView mKeyguardBottomAreaView;
    private KeyguardClockContainer mKeyguardClockView;
    private KeyguardSecurityModel mKeyguardSecurityModel;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserUnlocked() {
            LockScreenMagazineController.this.updateLockScreenMagazineAvailable();
            LockScreenMagazineController.this.queryLockScreenMagazineWallpaperInfo();
            LockScreenMagazineController.this.initLockScreenMagazinePreRes();
        }

        public void onLockScreenMagazineStatusChanged() {
            super.onLockScreenMagazineStatusChanged();
            Log.d("LockScreenMagazineController", "onLockScreenMagazineStatusChanged");
            LockScreenMagazineController.this.updateLockScreenMagazineAvailable();
            if (LockScreenMagazineController.this.mNotificationPanelView != null) {
                LockScreenMagazineController.this.mNotificationPanelView.inflateLeftView();
                LockScreenMagazineController.this.mLockScreenMagazinePre.updateViews();
                LockScreenMagazineController.this.mLockScreenMagazinePre.initSettingButton();
                LockScreenMagazineController.this.mKeyguardBottomAreaView.updateLeftAffordance();
                LockScreenMagazineController.this.mKeyguardBottomAreaView.initTipsView(true);
            }
            LockScreenMagazineController.this.reset();
        }

        public void onRegionChanged() {
            LockScreenMagazineController.this.initLockScreenMagazinePreRes();
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            if (z != LockScreenMagazineController.this.mKeyguardShowing) {
                boolean unused = LockScreenMagazineController.this.mKeyguardShowing = z;
                if (LockScreenMagazineController.this.mKeyguardShowing) {
                    LockScreenMagazineController lockScreenMagazineController = LockScreenMagazineController.this;
                    boolean unused2 = lockScreenMagazineController.mUnlockWithFingerprintPossible = lockScreenMagazineController.mUpdateMonitor.isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser());
                    long elapsedRealtime = SystemClock.elapsedRealtime();
                    if (elapsedRealtime - LockScreenMagazineController.this.mInitPreResElapsedRealtime > 172800000) {
                        LockScreenMagazineController.this.initLockScreenMagazinePreRes();
                        long unused3 = LockScreenMagazineController.this.mInitPreResElapsedRealtime = elapsedRealtime;
                    }
                }
                LockScreenMagazineController.this.reset();
            }
        }

        public void onEmergencyCallAction() {
            LockScreenMagazineController.this.reset();
        }

        public void onUserSwitchComplete(int i) {
            LockScreenMagazineController.this.updateLockScreenMagazineWallpaperInfo();
        }

        public void onDeviceProvisioned() {
            LockScreenMagazineController.this.initLockScreenMagazinePreRes();
            LockScreenMagazineController.this.mLockScreenMagazinePre.initSettingButton();
        }

        public void onStartedWakingUp() {
            boolean unused = LockScreenMagazineController.this.mStartedWakingUp = true;
        }

        public void onStartedGoingToSleep(int i) {
            boolean unused = LockScreenMagazineController.this.mStartedWakingUp = false;
        }

        public void onLockWallpaperProviderChanged() {
            LockScreenMagazineController.this.updateLockScreenMagazineWallpaperInfo();
            boolean isLockScreenMagazineOpenedWallpaper = WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(LockScreenMagazineController.this.mContext);
            if (LockScreenMagazineController.this.mIsLockScreenMagazineOpenedWallpaper != isLockScreenMagazineOpenedWallpaper) {
                boolean unused = LockScreenMagazineController.this.mIsLockScreenMagazineOpenedWallpaper = isLockScreenMagazineOpenedWallpaper;
                AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).recordLockScreenWallperProviderChanged();
            }
            LockScreenMagazineController.this.mHandler.removeMessages(1);
            LockScreenMagazineController.this.mHandler.sendEmptyMessageDelayed(1, 1000);
        }
    };
    /* access modifiers changed from: private */
    public long mLastClickTime = 0;
    private boolean mLockScreenMagazineAvailable;
    /* access modifiers changed from: private */
    public LockScreenMagazinePreView mLockScreenMagazinePre;
    private ContentObserver mLockScreenMagazineStatusObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            if (LockScreenMagazineUtils.getLockScreenMagazineStatus(LockScreenMagazineController.this.mContext) && !WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(LockScreenMagazineController.this.mContext)) {
                WallpaperAuthorityUtils.setWallpaperAuthoritySystemSetting(LockScreenMagazineController.this.mContext, WallpaperAuthorityUtils.APPLY_MAGAZINE_DEFAULT_AUTHORITY);
            }
        }
    };
    private RemoteViews mMainRemoteView;
    private ValueAnimator mNonFullScreenAnimator;
    /* access modifiers changed from: private */
    public NotificationPanelView mNotificationPanelView;
    private NotificationStackScrollLayout mNotificationStackScrollLayout;
    /* access modifiers changed from: private */
    public boolean mOpenDoubleTapGoToSleep;
    /* access modifiers changed from: private */
    public String mPreLeftScreenActivityName;
    /* access modifiers changed from: private */
    public String mPreLeftScreenDrawableResName;
    private Drawable mPreMainEntryDarkIcon;
    private Drawable mPreMainEntryLightIcon;
    /* access modifiers changed from: private */
    public String mPreMainEntryResDarkIconName;
    /* access modifiers changed from: private */
    public String mPreMainEntryResLightIconName;
    /* access modifiers changed from: private */
    public String mPreTransToLeftScreenDrawableResName;
    private LockScreenMagazinePreView.OnPreViewClickListener mPreViewClickListener = new LockScreenMagazinePreView.OnPreViewClickListener() {
        public void onPreButtonClick(View view) {
            AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).recordLockScreenMagazinePreviewAction(MiStat.Event.CLICK);
            if (LockScreenMagazineController.this.mIsLockScreenMagazinePkgExist) {
                if (SystemClock.elapsedRealtime() - LockScreenMagazineController.this.mLastClickTime > 500) {
                    Log.d("miui_keyguard", "preview button goto lock screen wall paper");
                    if (Build.IS_INTERNATIONAL_BUILD) {
                        Intent preLeftScreenIntent = LockScreenMagazineController.this.getPreLeftScreenIntent();
                        if (preLeftScreenIntent != null) {
                            preLeftScreenIntent.putExtra("entry_source", "cta");
                            LockScreenMagazineController.this.mContext.startActivityAsUser(preLeftScreenIntent, UserHandle.CURRENT);
                        }
                    } else {
                        LockScreenMagazineUtils.gotoLockScreenMagazine(LockScreenMagazineController.this.mContext, "buttonLockScreen");
                    }
                }
            } else if (SystemClock.elapsedRealtime() - LockScreenMagazineController.this.mLastClickTime < 300) {
                AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).record("keyguard_download_lockscreen_magazine");
                LockScreenMagazineController.this.startAppStoreToDownload();
            }
            long unused = LockScreenMagazineController.this.mLastClickTime = SystemClock.elapsedRealtime();
        }

        public void onSettingButtonClick(View view, Intent intent) {
            LockScreenMagazineController.this.startActivity(intent);
            AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).recordLockScreenMagazinePreviewAction("click_settings");
        }

        public void onLinkButtonClick(View view) {
            if (LockScreenMagazineController.this.openLockScreenMagazineAd()) {
                AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).recordLockScreenMagazinePreviewAction("click_link");
            }
        }
    };
    private boolean mPreViewShowing;
    Runnable mResetClockRunnable = new Runnable() {
        public void run() {
            LockScreenMagazineController.this.startSwitchAnimator(false);
        }
    };
    private int mScaledTouchSlop;
    private float mScreenHeight;
    private float mScreenWidth;
    /* access modifiers changed from: private */
    public boolean mStartedWakingUp;
    private boolean mSupportGestureWakeup;
    private AnimatorSet mSwitchAnimator;
    /* access modifiers changed from: private */
    public TextView mSwitchSystemUser;
    private int mUninvalidBottomAreaHeight;
    private int mUninvalidGXZWAreaRadius;
    private int mUninvalidStartEndAreaWidth;
    private int mUninvalidTopAreaHeight;
    /* access modifiers changed from: private */
    public boolean mUnlockWithFingerprintPossible;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    private final KeyguardUpdateMonitor.WallpaperChangeCallback mWallpaperChangeCallback = new KeyguardUpdateMonitor.WallpaperChangeCallback() {
        public void onWallpaperChange(boolean z) {
            if (z) {
                if (LockScreenMagazineController.this.mSwitchSystemUser != null) {
                    KeyguardUpdateMonitor unused = LockScreenMagazineController.this.mUpdateMonitor;
                    boolean isWallpaperColorLight = KeyguardUpdateMonitor.isWallpaperColorLight(LockScreenMagazineController.this.mContext);
                    LockScreenMagazineController.this.mSwitchSystemUser.setTextColor(isWallpaperColorLight ? -1308622848 : -1);
                    LockScreenMagazineController.this.mSwitchSystemUser.setCompoundDrawablesWithIntrinsicBounds(LockScreenMagazineController.this.mContext.getResources().getDrawable(isWallpaperColorLight ? R.drawable.logout_light : R.drawable.logout_dark), (Drawable) null, (Drawable) null, (Drawable) null);
                }
                LockScreenMagazineController.this.queryLockScreenMagazineWallpaperInfo();
            }
        }
    };

    public static LockScreenMagazineController getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LockScreenMagazineController.class) {
                if (sInstance == null) {
                    sInstance = new LockScreenMagazineController(context);
                }
            }
        }
        return sInstance;
    }

    private LockScreenMagazineController(Context context) {
        this.mContext = context;
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor = instance;
        instance.registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
        this.mKeyguardSecurityModel = new KeyguardSecurityModel(context);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(LockScreenMagazineUtils.SYSTEM_SETTINGS_KEY_LOCKSCREEN_MAGAZINE_STATUS), false, this.mLockScreenMagazineStatusObserver, -1);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        updateLockScreenMagazineAvailable();
        updateLockScreenMagazineWallpaperInfo();
        this.mIsLockScreenMagazineOpenedWallpaper = WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        context.registerReceiver(this.mBroadcastReceiver, intentFilter);
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("gesture_wakeup"), false, this.mGestureWakeupModeContentObserver, -1);
        this.mGestureWakeupModeContentObserver.onChange(false);
        this.mSupportGestureWakeup = MiuiKeyguardUtils.isSupportGestureWakeup();
        initLockScreenMagazinePreRes();
        initAntiMisoperation();
    }

    public void setNotificationPanelView(NotificationPanelView notificationPanelView) {
        this.mNotificationPanelView = notificationPanelView;
        LockScreenMagazinePreView lockScreenMagazinePreView = (LockScreenMagazinePreView) notificationPanelView.findViewById(R.id.wallpaper_des);
        this.mLockScreenMagazinePre = lockScreenMagazinePreView;
        lockScreenMagazinePreView.setButtonClickListener(this.mPreViewClickListener);
        this.mKeyguardClockView = (KeyguardClockContainer) this.mNotificationPanelView.findViewById(R.id.keyguard_clock_view);
        this.mKeyguardBottomAreaView = (KeyguardBottomAreaView) this.mNotificationPanelView.findViewById(R.id.keyguard_bottom_area);
        this.mSwitchSystemUser = (TextView) this.mNotificationPanelView.findViewById(R.id.switch_to_system_user);
        this.mNotificationStackScrollLayout = (NotificationStackScrollLayout) this.mNotificationPanelView.findViewById(R.id.notification_stack_scroller);
    }

    private void initAntiMisoperation() {
        this.mScaledTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        Display display = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenWidth = (float) Math.min(point.x, point.y);
        this.mScreenHeight = (float) Math.max(point.x, point.y);
        this.mUninvalidTopAreaHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.lock_screen_magazine_click_uninvalid_top_area_height);
        this.mUninvalidBottomAreaHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.lock_screen_magazine_click_uninvalid_bottom_area_height);
        this.mUninvalidStartEndAreaWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.lock_screen_magazine_click_uninvalid_start_end_area_width);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            Rect fodPosition = MiuiGxzwManager.getFodPosition(this.mContext);
            int width = fodPosition.width() / 2;
            this.mGXZWIconCenterX = fodPosition.left + width;
            this.mGXZWIconCenterY = fodPosition.top + width;
            this.mUninvalidGXZWAreaRadius = width + this.mContext.getResources().getDimensionPixelSize(R.dimen.lock_screen_magazine_click_uninvalid_gxzw_icon_area_margin);
        }
    }

    /* access modifiers changed from: private */
    public void updateLockScreenMagazineWallpaperInfo() {
        LockScreenMagazinePreView lockScreenMagazinePreView = this.mLockScreenMagazinePre;
        if (lockScreenMagazinePreView != null) {
            lockScreenMagazinePreView.refreshWallpaperInfo(this.mMainRemoteView, this.mFullScreenRemoteView);
        }
        KeyguardClockContainer keyguardClockContainer = this.mKeyguardClockView;
        if (keyguardClockContainer != null) {
            keyguardClockContainer.updateLockScreenMagazineInfo();
        }
    }

    public void updateRemoteView(RemoteViews remoteViews, RemoteViews remoteViews2) {
        this.mMainRemoteView = remoteViews;
        this.mFullScreenRemoteView = remoteViews2;
    }

    /* access modifiers changed from: private */
    public boolean openLockScreenMagazineAd() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mUpdateMonitor.getLockScreenMagazineWallpaperInfo();
        return WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(this.mContext) && lockScreenMagazineWallpaperInfo != null && lockScreenMagazineWallpaperInfo.opendAd(this.mContext);
    }

    public void reset() {
        cancelSwitchAnimate();
        removeResetClockCallbacks();
        resetViews();
    }

    private void resetViews() {
        if (this.mPreViewShowing) {
            this.mPreViewShowing = false;
            this.mUpdateMonitor.handleLockScreenMagazinePreViewVisibilityChanged(false);
        }
        if (!this.mKeyguardShowing || !MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
            this.mLockScreenMagazinePre.setVisibility(4);
            this.mLockScreenMagazinePre.setAlpha(0.0f);
            this.mKeyguardClockView.setAlpha(0.0f);
            this.mKeyguardBottomAreaView.setAlpha(0.0f);
        } else {
            this.mLockScreenMagazinePre.setVisibility(0);
            this.mLockScreenMagazinePre.setAlpha(1.0f);
            if (Build.IS_INTERNATIONAL_BUILD && this.mUpdateMonitor.isSupportLockScreenMagazineLeft() && !MiuiKeyguardUtils.isGxzwSensor()) {
                this.mLockScreenMagazinePre.setMainLayoutVisible(0);
                this.mLockScreenMagazinePre.setMainLayoutAlpha(1.0f);
            }
            this.mLockScreenMagazinePre.setFullScreenLayoutVisible(4);
            this.mLockScreenMagazinePre.setFullScreenLayoutAlpha(0.0f);
            this.mKeyguardClockView.setAlpha(1.0f);
            this.mKeyguardBottomAreaView.setAlpha(1.0f);
        }
        NotificationPanelView notificationPanelView = this.mNotificationPanelView;
        if (notificationPanelView != null) {
            notificationPanelView.refreshNotificationStackScrollerVisible();
        }
        setViewsAlpha(1.0f);
    }

    public void setWallPaperViewsAlpha(float f) {
        if (this.mLockScreenMagazinePre.getVisibility() == 0 && !this.mIsSwitchAnimating) {
            this.mLockScreenMagazinePre.setAlpha(f);
        }
    }

    /* access modifiers changed from: private */
    public void updateLockScreenMagazineAvailable() {
        this.mLockScreenMagazineAvailable = LockScreenMagazineUtils.isLockScreenMagazineAvailable(this.mContext);
    }

    /* access modifiers changed from: private */
    public void startSwitchAnimator(final boolean z) {
        this.mPreViewShowing = z;
        this.mUpdateMonitor.handleLockScreenMagazinePreViewVisibilityChanged(z);
        cancelSwitchAnimate();
        this.mSwitchAnimator = new AnimatorSet();
        float[] fArr = new float[2];
        float f = 0.0f;
        fArr[0] = z ? 0.0f : 1.0f;
        fArr[1] = z ? 1.0f : 0.0f;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(fArr);
        this.mFullScreenAnimator = ofFloat;
        ofFloat.setInterpolator(z ? Ease$Cubic.easeInOut : Ease$Quint.easeOut);
        this.mFullScreenAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockScreenMagazineController.this.mLockScreenMagazinePre.setFullScreenLayoutAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        float[] fArr2 = new float[2];
        fArr2[0] = z ? 1.0f : 0.0f;
        if (!z) {
            f = 1.0f;
        }
        fArr2[1] = f;
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(fArr2);
        this.mNonFullScreenAnimator = ofFloat2;
        ofFloat2.setInterpolator(z ? Ease$Quint.easeOut : Ease$Cubic.easeInOut);
        this.mNonFullScreenAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockScreenMagazineController.this.setViewsAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mSwitchAnimator.setDuration(500);
        this.mSwitchAnimator.play(this.mFullScreenAnimator).with(this.mNonFullScreenAnimator);
        this.mSwitchAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                int i = 0;
                boolean unused = LockScreenMagazineController.this.mIsSwitchAnimating = false;
                if (LockScreenMagazineController.this.needGlobalSwitchAnimate()) {
                    LockScreenMagazineController.this.mLockScreenMagazinePre.setMainLayoutVisible(z ? 4 : 0);
                }
                LockScreenMagazinePreView access$1000 = LockScreenMagazineController.this.mLockScreenMagazinePre;
                if (!z) {
                    i = 4;
                }
                access$1000.setFullScreenLayoutVisible(i);
            }

            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                boolean unused = LockScreenMagazineController.this.mIsSwitchAnimating = true;
                if (LockScreenMagazineController.this.needGlobalSwitchAnimate()) {
                    LockScreenMagazineController.this.mLockScreenMagazinePre.setMainLayoutVisible(0);
                }
                LockScreenMagazineController.this.mLockScreenMagazinePre.setFullScreenLayoutVisible(0);
            }
        });
        this.mSwitchAnimator.start();
        removeResetClockCallbacks();
        if (z) {
            postDelayedResetClock();
        }
    }

    /* access modifiers changed from: private */
    public boolean needGlobalSwitchAnimate() {
        return Build.IS_INTERNATIONAL_BUILD && this.mUpdateMonitor.isSupportLockScreenMagazineLeft() && !MiuiKeyguardUtils.isGxzwSensor();
    }

    /* access modifiers changed from: private */
    public void setViewsAlpha(float f) {
        this.mLockScreenMagazinePre.setMainLayoutAlpha(f);
        this.mKeyguardClockView.setClockAlpha(f);
        this.mKeyguardBottomAreaView.setViewsAlpha(f);
        this.mSwitchSystemUser.setAlpha(f);
        this.mNotificationStackScrollLayout.setAlpha(f);
    }

    private void cancelSwitchAnimate() {
        AnimatorSet animatorSet = this.mSwitchAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mSwitchAnimator.removeAllListeners();
            this.mSwitchAnimator = null;
        }
        ValueAnimator valueAnimator = this.mFullScreenAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mFullScreenAnimator.removeAllUpdateListeners();
            this.mFullScreenAnimator = null;
        }
        ValueAnimator valueAnimator2 = this.mNonFullScreenAnimator;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
            this.mNonFullScreenAnimator.removeAllUpdateListeners();
            this.mNonFullScreenAnimator = null;
        }
    }

    public boolean isSwitchAnimating() {
        return this.mIsSwitchAnimating;
    }

    private void postDelayedResetClock() {
        this.mHandler.postDelayed(this.mResetClockRunnable, 5000);
    }

    private void removeResetClockCallbacks() {
        this.mHandler.removeCallbacks(this.mResetClockRunnable);
    }

    public void onTouchEvent(MotionEvent motionEvent, int i) {
        NotificationPanelView notificationPanelView;
        if (this.mLockScreenMagazineAvailable && i == 1 && (notificationPanelView = this.mNotificationPanelView) != null && notificationPanelView.isQSFullyCollapsed() && !isMisoperation(motionEvent)) {
            if (!this.mSupportGestureWakeup || !this.mOpenDoubleTapGoToSleep) {
                handleSingleClickEvent();
                return;
            }
            this.mHandler.removeMessages(3);
            this.mHandler.sendEmptyMessageDelayed(3, 200);
        }
    }

    private boolean isMisoperation(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mCurrentTouchDownTime = System.currentTimeMillis();
            this.mInitialTouchX = motionEvent.getRawX();
            this.mInitialTouchY = motionEvent.getRawY();
            return true;
        } else if (motionEvent.getAction() != 1 || System.currentTimeMillis() - this.mCurrentTouchDownTime > 500) {
            return true;
        } else {
            float rawX = motionEvent.getRawX();
            float rawY = motionEvent.getRawY();
            if (Math.abs(this.mInitialTouchX - rawX) <= ((float) this.mScaledTouchSlop) && Math.abs(this.mInitialTouchY - rawY) <= ((float) this.mScaledTouchSlop)) {
                int i = this.mUninvalidStartEndAreaWidth;
                if (rawX >= ((float) i) && rawX <= this.mScreenWidth - ((float) i) && rawY >= ((float) this.mUninvalidTopAreaHeight) && rawY <= this.mScreenHeight - ((float) this.mUninvalidBottomAreaHeight)) {
                    if (!MiuiKeyguardUtils.isGxzwSensor() || !this.mUnlockWithFingerprintPossible || this.mPreViewShowing || getTowPointDistance((int) rawX, (int) rawY, this.mGXZWIconCenterX, this.mGXZWIconCenterY) >= this.mUninvalidGXZWAreaRadius) {
                        return false;
                    }
                    return true;
                }
            }
            return true;
        }
    }

    private int getTowPointDistance(int i, int i2, int i3, int i4) {
        return (int) Math.sqrt(Math.pow((double) (i - i3), 2.0d) + Math.pow((double) (i2 - i4), 2.0d));
    }

    /* access modifiers changed from: private */
    public void handleSingleClickEvent() {
        if (this.mLockScreenMagazineAvailable) {
            AnalyticsHelper.getInstance(this.mContext).record("action_main_screen_click");
            if (shouldShowPreView()) {
                handleSwitchAnimator();
            } else if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && this.mUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                this.mKeyguardBottomAreaView.startButtonLayoutAnimate(true);
            }
        }
    }

    private boolean shouldShowPreView() {
        if (Build.IS_INTERNATIONAL_BUILD) {
            return WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext);
        }
        return WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(this.mContext);
    }

    private void handleSwitchAnimator() {
        AnimatorSet animatorSet = this.mSwitchAnimator;
        if (animatorSet != null && (animatorSet.isRunning() || this.mSwitchAnimator.isStarted() || this.mIsSwitchAnimating)) {
            return;
        }
        if (this.mPreViewShowing) {
            startSwitchAnimator(false);
            return;
        }
        startSwitchAnimator(true);
        AnalyticsHelper.getInstance(this.mContext).recordLockScreenMagazinePreviewAction("show");
        LockScreenMagazineUtils.notifyFullScreenClickRecordEvent(this.mContext);
    }

    /* access modifiers changed from: private */
    public void startAppStoreToDownload() {
        try {
            startActivity(PackageUtils.getMarketDownloadIntent(LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME));
        } catch (Exception e) {
            Log.e("miui_keyguard", "start to download lockscreen wallpaper", e);
        }
    }

    public void updateResources(boolean z) {
        if (z) {
            updateLockScreenMagazineAvailable();
            setLockScreenMagazineAuthority();
            updateLockScreenMagazineWallpaperInfo();
        }
    }

    /* access modifiers changed from: private */
    public void initLockScreenMagazinePreRes() {
        if (this.mUpdateMonitor.isUserUnlocked() && MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(this.mContext)) {
            Log.d("LockScreenMagazineController", "initLockScreenMagazinePreRes");
            new AsyncTask<Void, Void, Void>() {
                /* access modifiers changed from: protected */
                public Void doInBackground(Void... voidArr) {
                    Bundle lockScreenMagazinePreContent = LockScreenMagazineUtils.getLockScreenMagazinePreContent(LockScreenMagazineController.this.mContext);
                    String string = lockScreenMagazinePreContent != null ? lockScreenMagazinePreContent.getString("result_json") : null;
                    if (!TextUtils.isEmpty(string)) {
                        try {
                            Log.d("LockScreenMagazineController", "initLockScreenMagazinePreRes resultJson = " + string);
                            JSONObject jSONObject = new JSONObject(string);
                            String unused = LockScreenMagazineController.this.mPreLeftScreenActivityName = jSONObject.optString("leftscreen_activity");
                            boolean unused2 = LockScreenMagazineController.this.mIsSupportLeftOverlay = jSONObject.optBoolean("is_support_overlay");
                            String unused3 = LockScreenMagazineController.this.mPreMainEntryResDarkIconName = jSONObject.optString("main_entry_res_icon_dark_svg");
                            String unused4 = LockScreenMagazineController.this.mPreMainEntryResLightIconName = jSONObject.optString("main_entry_res_icon_light_svg");
                            String unused5 = LockScreenMagazineController.this.mPreTransToLeftScreenDrawableResName = jSONObject.optString("trans_to_leftscreen_res_drawable");
                            String unused6 = LockScreenMagazineController.this.mPreLeftScreenDrawableResName = jSONObject.optString("leftscreen_res_drawable_preview");
                        } catch (Exception e) {
                            Log.e("LockScreenMagazineController", "initLockScreenMagazinePreRes", e);
                        }
                        LockScreenMagazineController.this.initPreMainEntryIcon();
                    } else {
                        String unused7 = LockScreenMagazineController.this.mPreLeftScreenActivityName = null;
                        String unused8 = LockScreenMagazineController.this.mPreMainEntryResDarkIconName = null;
                        String unused9 = LockScreenMagazineController.this.mPreMainEntryResLightIconName = null;
                        String unused10 = LockScreenMagazineController.this.mPreTransToLeftScreenDrawableResName = null;
                        String unused11 = LockScreenMagazineController.this.mPreLeftScreenDrawableResName = null;
                        boolean unused12 = LockScreenMagazineController.this.mIsSupportLeftOverlay = false;
                    }
                    LockScreenMagazineController.this.handlePreLeftScreenActivityName();
                    LockScreenMagazineController.this.initLockScreenMagazinePkgExist();
                    LockScreenMagazineController.this.checkIsDecoupleHome();
                    return null;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Void voidR) {
                    KeyguardUpdateMonitor.getInstance(LockScreenMagazineController.this.mContext).setSupportLockScreenMagazineOverlay(LockScreenMagazineController.this.mIsSupportLeftOverlay);
                    if (!TextUtils.isEmpty(LockScreenMagazineController.this.mPreLeftScreenActivityName)) {
                        KeyguardUpdateMonitor.getInstance(LockScreenMagazineController.this.mContext).setSupportLockScreenMagazineLeft(true);
                        return;
                    }
                    LockScreenMagazineUtils.setLockScreenMagazineStatus(LockScreenMagazineController.this.mContext, false);
                    LockScreenMagazineUtils.notifySubscriptionChange(LockScreenMagazineController.this.mContext);
                    KeyguardUpdateMonitor.getInstance(LockScreenMagazineController.this.mContext).setSupportLockScreenMagazineLeft(false);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    public void handlePreLeftScreenActivityName() {
        if (!TextUtils.isEmpty(this.mPreLeftScreenActivityName)) {
            try {
                String[] split = this.mPreLeftScreenActivityName.split("/");
                if (split != null && split.length > 1) {
                    this.mPreLeftScreenActivityName = split[1];
                }
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, this.mPreLeftScreenActivityName));
                if (PackageUtils.resolveIntent(this.mContext, intent) == null) {
                    this.mPreLeftScreenActivityName = null;
                }
            } catch (Exception e) {
                Log.e("LockScreenMagazineController", "handlePreLeftScreenActivityName failed", e);
                this.mPreLeftScreenActivityName = null;
            }
        }
    }

    public void initPreMainEntryIcon() {
        this.mPreMainEntryDarkIcon = PackageUtils.getDrawableFromPackage(this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, this.mPreMainEntryResDarkIconName);
        this.mPreMainEntryLightIcon = PackageUtils.getDrawableFromPackage(this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, this.mPreMainEntryResLightIconName);
    }

    /* access modifiers changed from: private */
    public void initLockScreenMagazinePkgExist() {
        boolean isAppInstalledForUser = PackageUtils.isAppInstalledForUser(this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardUpdateMonitor.getCurrentUser());
        this.mIsLockScreenMagazinePkgExist = isAppInstalledForUser;
        this.mUpdateMonitor.setLockScreenMagazinePkgExist(isAppInstalledForUser);
    }

    /* access modifiers changed from: private */
    public void checkIsDecoupleHome() {
        this.mIsDecoupleHome = LockScreenMagazineUtils.checkLockScreenMagazineDecoupleHome(this.mContext);
    }

    public boolean isDecoupleHome() {
        return this.mIsDecoupleHome;
    }

    private String getPreLeftScreenActivityName() {
        return this.mPreLeftScreenActivityName;
    }

    public String getPreTransToLeftScreenDrawableResName() {
        return this.mPreTransToLeftScreenDrawableResName;
    }

    public String getPreLeftScreenDrawableResName() {
        return this.mPreLeftScreenDrawableResName;
    }

    public Drawable getPreMainEntryResDarkIcon() {
        return this.mPreMainEntryDarkIcon;
    }

    public Drawable getPreMainEntryResLightIcon() {
        return this.mPreMainEntryLightIcon;
    }

    /* access modifiers changed from: private */
    public void closeLockScreenMagazineStatus() {
        if (WallpaperAuthorityUtils.isCustomWallpaper(this.mContext) && LockScreenMagazineUtils.getLockScreenMagazineStatus(this.mContext)) {
            Log.d("LockScreenMagazineController", "closeLockScreenMagazineStatus");
            LockScreenMagazineUtils.setLockScreenMagazineStatus(this.mContext, false);
            LockScreenMagazineUtils.notifySubscriptionChange(this.mContext);
        }
    }

    private void setLockScreenMagazineAuthority() {
        if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && LockScreenMagazineUtils.getLockScreenMagazineStatus(this.mContext)) {
            if (!WallpaperAuthorityUtils.isThemeLockWallpaper(this.mContext) || MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
                Log.d("LockScreenMagazineController", "setLockScreenMagazineAuthority");
                WallpaperAuthorityUtils.setWallpaperAuthoritySystemSetting(this.mContext, WallpaperAuthorityUtils.APPLY_MAGAZINE_DEFAULT_AUTHORITY);
            }
        }
    }

    /* access modifiers changed from: private */
    public void queryLockScreenMagazineWallpaperInfo() {
        if (this.mLockScreenMagazineAvailable && WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(this.mContext)) {
            new AsyncTask<Void, Void, Void>() {
                /* access modifiers changed from: protected */
                public Void doInBackground(Void... voidArr) {
                    LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = LockScreenMagazineUtils.getLockScreenMagazineWallpaperInfo(LockScreenMagazineController.this.mContext);
                    lockScreenMagazineWallpaperInfo.initExtra();
                    Log.i("LockScreenMagazineController", "queryLockScreenMagazineWallpaperInfo wallpaperUri = " + lockScreenMagazineWallpaperInfo.wallpaperUri);
                    LockScreenMagazineController.this.mUpdateMonitor.setLockScreenMagazineWallpaperInfo(lockScreenMagazineWallpaperInfo);
                    return null;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Void voidR) {
                    LockScreenMagazineController.this.updateLockScreenMagazineWallpaperInfo();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public Intent getPreLeftScreenIntent() {
        if (!this.mUpdateMonitor.isSupportLockScreenMagazineLeft()) {
            return null;
        }
        try {
            String preLeftScreenActivityName = getPreLeftScreenActivityName();
            if (TextUtils.isEmpty(preLeftScreenActivityName)) {
                return null;
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, preLeftScreenActivityName));
            intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
            if (Build.IS_INTERNATIONAL_BUILD) {
                intent.putExtra("wc_enable_source", "systemui");
                intent.putExtra("wallpaper_uri", this.mUpdateMonitor.getLockScreenMagazineWallpaperInfo().wallpaperUri);
                intent.putExtra("wallpaper_details", new Gson().toJson(this.mUpdateMonitor.getLockScreenMagazineWallpaperInfo()));
            } else {
                intent.putExtra("from", "keyguard");
            }
            return intent;
        } catch (Exception unused) {
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void startActivity(Intent intent) {
        StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        if (statusBar != null) {
            statusBar.startActivity(intent, true);
        }
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mKeyguardSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }
}
