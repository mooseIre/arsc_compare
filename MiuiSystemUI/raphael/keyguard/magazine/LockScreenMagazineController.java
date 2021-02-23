package com.android.keyguard.magazine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.clock.KeyguardClockContainer;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardBottomAreaInjector;
import com.android.keyguard.injector.KeyguardClockInjector;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.magazine.entity.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.google.gson.Gson;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.CommonExtensionsKt;
import com.miui.systemui.util.MiuiTextUtils;
import miui.os.Build;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class LockScreenMagazineController implements SettingsObserver.Callback {
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
                    LockScreenMagazineController.this.mHandler.removeMessages(1);
                    LockScreenMagazineController.this.mHandler.sendEmptyMessageDelayed(1, 1000);
                }
            }
        }
    };
    private final KeyguardClockContainer mClockContainerView;
    /* access modifiers changed from: private */
    public Context mContext;
    private long mCurrentTouchDownTime;
    private ValueAnimator mFullScreenAnimator;
    private RemoteViews mFullScreenRemoteView;
    private int mGXZWIconCenterX;
    private int mGXZWIconCenterY;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                LockScreenMagazineController.this.initLockScreenMagazinePreRes();
            } else if (i == 2 && LockScreenMagazineController.this.mStartedWakingUp) {
                boolean unused = LockScreenMagazineController.this.handleSingleClickEvent();
            }
        }
    };
    /* access modifiers changed from: private */
    public long mInitPreResElapsedRealtime = SystemClock.elapsedRealtime();
    private float mInitialTouchX;
    private float mInitialTouchY;
    /* access modifiers changed from: private */
    public boolean mIsJumpingIntent = false;
    /* access modifiers changed from: private */
    public boolean mIsLockScreenMagazineOpenedWallpaper;
    /* access modifiers changed from: private */
    public boolean mIsLockScreenMagazinePkgExist = true;
    private boolean mIsSupportLockScreenMagazineLeft;
    private boolean mIsSupportLockScreenMagazineLeftOverlay;
    /* access modifiers changed from: private */
    public boolean mIsSwitchAnimating;
    /* access modifiers changed from: private */
    public KeyguardBottomAreaView mKeyguardBottomArea;
    private KeyguardSecurityModel mKeyguardSecurityModel;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onUserUnlocked() {
            LockScreenMagazineController.this.updateLockScreenMagazineAvailable();
            LockScreenMagazineController.this.queryLockScreenMagazineWallpaperInfo();
            LockScreenMagazineController.this.initLockScreenMagazinePreRes();
        }

        public void onMagazineResourceInited() {
            Log.d("LockScreenMagazineController", "completeMagazineResInitialization");
            LockScreenMagazineController.this.updateLockScreenMagazineAvailable();
            LockScreenMagazineController.this.mLockScreenMagazinePre.updateViews();
            LockScreenMagazineController.this.mLockScreenMagazinePre.initSettingButton();
            LockScreenMagazineController.this.mKeyguardBottomArea.updateLeftAffordance();
            ((KeyguardNegative1PageInjector) Dependency.get(KeyguardNegative1PageInjector.class)).getLeftView().inflateLeftView();
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

        public void onUserSwitchComplete(int i) {
            LockScreenMagazineController.this.updateLockScreenMagazineWallpaperInfo();
        }

        public void onDeviceProvisioned() {
            LockScreenMagazineController.this.initLockScreenMagazinePreRes();
            LockScreenMagazineController.this.mLockScreenMagazinePre.initSettingButton();
        }

        public void onLockWallpaperProviderChanged() {
            LockScreenMagazineController.this.updateLockScreenMagazineWallpaperInfo();
            boolean isLockScreenMagazineOpenedWallpaper = WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper();
            if (LockScreenMagazineController.this.mIsLockScreenMagazineOpenedWallpaper != isLockScreenMagazineOpenedWallpaper) {
                boolean unused = LockScreenMagazineController.this.mIsLockScreenMagazineOpenedWallpaper = isLockScreenMagazineOpenedWallpaper;
                AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).recordLockScreenWallperProviderChanged();
            }
        }

        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            LockScreenMagazineController.this.handleLockScreenMagazinePreViewVisibilityChanged(z);
        }
    };
    private boolean mLockScreenLeftOverlayAvailable;
    private boolean mLockScreenMagazineAvailable;
    /* access modifiers changed from: private */
    public LockScreenMagazinePreView mLockScreenMagazinePre;
    private boolean mLockScreenMagazinePreViewVisible = false;
    private LockScreenMagazineWallpaperInfo mLockScreenMagazineWallpaperInfo = new LockScreenMagazineWallpaperInfo();
    private String mMagazineWallpaperAuthority;
    private RemoteViews mMainRemoteView;
    private ValueAnimator mNonFullScreenAnimator;
    private NotificationStackScrollLayout mNotificationStackScrollLayout;
    /* access modifiers changed from: private */
    public String mPreLeftScreenActivityName;
    /* access modifiers changed from: private */
    public String mPreLeftScreenDrawableResName;
    /* access modifiers changed from: private */
    public Drawable mPreMainEntryDarkIcon;
    /* access modifiers changed from: private */
    public Drawable mPreMainEntryLightIcon;
    /* access modifiers changed from: private */
    public String mPreMainEntryResDarkIconName;
    /* access modifiers changed from: private */
    public String mPreMainEntryResLightIconName;
    /* access modifiers changed from: private */
    public String mPreTransToLeftScreenDrawableResName;
    private LockScreenMagazinePreView.OnPreViewClickListener mPreViewClickListener = new LockScreenMagazinePreView.OnPreViewClickListener() {
        public void onPreButtonClick(View view) {
            AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).recordLockScreenMagazinePreviewAction("click");
            if (LockScreenMagazineController.this.mIsLockScreenMagazinePkgExist) {
                if (CommonExtensionsKt.checkFastDoubleClick(view, 500)) {
                    Log.d("miui_keyguard", "preview button goto lock screen wall paper");
                    if (Build.IS_INTERNATIONAL_BUILD) {
                        Intent preLeftScreenIntent = LockScreenMagazineController.this.getPreLeftScreenIntent();
                        if (preLeftScreenIntent != null) {
                            preLeftScreenIntent.putExtra("entry_source", "cta");
                            LockScreenMagazineController.this.mContext.startActivityAsUser(preLeftScreenIntent, UserHandle.CURRENT);
                            return;
                        }
                        return;
                    }
                    LockScreenMagazineUtils.gotoMagazine(LockScreenMagazineController.this.mContext, "buttonLockScreen");
                }
            } else if (!CommonExtensionsKt.checkFastDoubleClick(view, 300)) {
                AnalyticsHelper.getInstance(LockScreenMagazineController.this.mContext).record("keyguard_download_lockscreen_magazine");
                LockScreenMagazineController.this.startAppStoreToDownload();
            }
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
    /* access modifiers changed from: private */
    public String mPreviewComponent;
    Runnable mResetClockRunnable = new Runnable() {
        public void run() {
            LockScreenMagazineController.this.startSwitchAnimator(false);
        }
    };
    private int mScaledTouchSlop;
    private float mScreenHeight;
    private float mScreenWidth;
    private final SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public boolean mStartedWakingUp;
    private boolean mSupportGestureWakeup;
    private AnimatorSet mSwitchAnimator;
    private int mUninvalidBottomAreaHeight;
    private int mUninvalidGXZWAreaRadius;
    private int mUninvalidStartEndAreaWidth;
    private int mUninvalidTopAreaHeight;
    /* access modifiers changed from: private */
    public boolean mUnlockWithFingerprintPossible;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public final KeyguardUpdateMonitorInjector mUpdateMonitorInjector;
    private final IMiuiKeyguardWallpaperController.IWallpaperChangeCallback mWallpaperChangeCallback = new IMiuiKeyguardWallpaperController.IWallpaperChangeCallback() {
        public final void onWallpaperChange(boolean z) {
            LockScreenMagazineController.this.lambda$new$0$LockScreenMagazineController(z);
        }
    };

    public LockScreenMagazineController(Context context) {
        this.mContext = context;
        this.mMagazineWallpaperAuthority = WallpaperAuthorityUtils.getWallpaperAuthority();
        this.mKeyguardSecurityModel = (KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class);
        this.mClockContainerView = ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).getView();
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class);
        this.mSettingsObserver = (SettingsObserver) Dependency.get(SettingsObserver.class);
        this.mSupportGestureWakeup = MiuiKeyguardUtils.isSupportGestureWakeup();
        initLockScreenMagazinePreRes();
        initAntiMistakeOperation();
    }

    public void setView(LockScreenMagazinePreView lockScreenMagazinePreView) {
        this.mLockScreenMagazinePre = lockScreenMagazinePreView;
        if (lockScreenMagazinePreView != null) {
            lockScreenMagazinePreView.setButtonClickListener(this.mPreViewClickListener);
        }
    }

    public void setBottomAreaView(KeyguardBottomAreaView keyguardBottomAreaView) {
        this.mKeyguardBottomArea = keyguardBottomAreaView;
        LockScreenMagazinePreView lockScreenMagazinePreView = this.mLockScreenMagazinePre;
        if (lockScreenMagazinePreView != null && keyguardBottomAreaView != null) {
            lockScreenMagazinePreView.setElevation(keyguardBottomAreaView.getElevation() + 1.0f);
        }
    }

    public LockScreenMagazinePreView getView() {
        return this.mLockScreenMagazinePre;
    }

    public void onAttachedToWindow() {
        registerBroadcastReceivers();
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mSettingsObserver.addCallback(this, "gesture_wakeup");
        this.mSettingsObserver.addCallback(this, 1, new String[0]);
        this.mSettingsObserver.addCallback(this, "lock_wallpaper_provider_authority");
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
    }

    public void onDetachedFromWindow() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        this.mSettingsObserver.removeCallback(this);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this.mWallpaperChangeCallback);
    }

    private void registerBroadcastReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        Log.d("LockScreenMagazineController", "ContentObserver2Magazine onChange:$selfChange");
        boolean parseBoolean = MiuiTextUtils.parseBoolean(str2, false);
        if ("gesture_wakeup".equals(str)) {
            MiuiKeyguardUtils.setContentObserverForGestureWakeup(parseBoolean);
        } else if ("lock_wallpaper_provider_authority".equals(str)) {
            if (TextUtils.isEmpty(str2)) {
                str2 = "com.miui.home.none_provider";
            }
            if (!TextUtils.equals(this.mMagazineWallpaperAuthority, str2)) {
                this.mMagazineWallpaperAuthority = str2;
                if (!Build.IS_INTERNATIONAL_BUILD || !WallpaperAuthorityUtils.isHomeDefaultWallpaper()) {
                    this.mUpdateMonitorInjector.handleLockWallpaperProviderChanged();
                } else {
                    onRemoteViewChange((RemoteViews) null, (RemoteViews) null);
                }
            }
        }
    }

    public void onRemoteViewChange(RemoteViews remoteViews, RemoteViews remoteViews2) {
        if (this.mMainRemoteView != remoteViews) {
            this.mMainRemoteView = remoteViews;
        }
        if (this.mFullScreenRemoteView != remoteViews2) {
            this.mFullScreenRemoteView = remoteViews2;
        }
        this.mUpdateMonitorInjector.handleLockWallpaperProviderChanged();
    }

    public LockScreenMagazineController initAndUpdateParams(NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mNotificationStackScrollLayout = notificationStackScrollLayout;
        updateLockScreenMagazineAvailable();
        updateLockScreenMagazineWallpaperInfo();
        return this;
    }

    private void initAntiMistakeOperation() {
        this.mScaledTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        Display display = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenWidth = (float) Math.min(point.x, point.y);
        this.mScreenHeight = (float) Math.max(point.x, point.y);
        this.mUninvalidTopAreaHeight = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_click_uninvalid_top_area_height);
        this.mUninvalidBottomAreaHeight = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_click_uninvalid_bottom_area_height);
        this.mUninvalidStartEndAreaWidth = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_click_uninvalid_start_end_area_width);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            Rect fodPosition = MiuiGxzwManager.getFodPosition(this.mContext);
            int width = fodPosition.width() / 2;
            this.mGXZWIconCenterX = fodPosition.left + width;
            this.mGXZWIconCenterY = fodPosition.top + width;
            this.mUninvalidGXZWAreaRadius = width + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_click_uninvalid_gxzw_icon_area_margin);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$LockScreenMagazineController(boolean z) {
        TextView switchSystemUserEntrance = ((KeyguardPanelViewInjector) Dependency.get(KeyguardPanelViewInjector.class)).getSwitchSystemUserEntrance();
        switchSystemUserEntrance.setTextColor(z ? -1308622848 : -1);
        switchSystemUserEntrance.setCompoundDrawablesWithIntrinsicBounds(this.mContext.getResources().getDrawable(z ? C0013R$drawable.logout_light : C0013R$drawable.logout_dark), (Drawable) null, (Drawable) null, (Drawable) null);
        queryLockScreenMagazineWallpaperInfo();
    }

    /* access modifiers changed from: private */
    public void updateLockScreenMagazineWallpaperInfo() {
        LockScreenMagazinePreView lockScreenMagazinePreView = this.mLockScreenMagazinePre;
        if (lockScreenMagazinePreView != null) {
            lockScreenMagazinePreView.refreshWallpaperInfo(this.mMainRemoteView, this.mFullScreenRemoteView);
        }
        this.mClockContainerView.updateClockMagazineInfo();
    }

    /* access modifiers changed from: private */
    public boolean openLockScreenMagazineAd() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = getLockScreenMagazineWallpaperInfo();
        return WallpaperAuthorityUtils.isLockScreenMagazineWallpaper() && lockScreenMagazineWallpaperInfo != null && lockScreenMagazineWallpaperInfo.opendAd(this.mContext);
    }

    public void reset() {
        cancelSwitchAnimate();
        removeResetClockCallbacks();
        resetViews();
    }

    private void resetViews() {
        if (this.mPreViewShowing) {
            this.mPreViewShowing = false;
            this.mUpdateMonitorInjector.handleLockScreenMagazinePreViewVisibilityChanged(false);
        }
        if (!this.mKeyguardShowing || !MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
            this.mLockScreenMagazinePre.setVisibility(4);
            this.mLockScreenMagazinePre.setAlpha(0.0f);
            this.mClockContainerView.setAlpha(0.0f);
            setBottomAreaAlpha(0.0f);
        } else {
            this.mLockScreenMagazinePre.setVisibility(0);
            this.mLockScreenMagazinePre.setAlpha(1.0f);
            if (Build.IS_INTERNATIONAL_BUILD && isSupportLockScreenMagazineLeft() && !MiuiKeyguardUtils.isGxzwSensor() && !this.mUpdateMonitor.isBouncerShowing()) {
                this.mLockScreenMagazinePre.setMainLayoutVisible(0);
                this.mLockScreenMagazinePre.setMainLayoutAlpha(1.0f);
            }
            this.mLockScreenMagazinePre.setFullScreenLayoutVisible(4);
            this.mLockScreenMagazinePre.setFullScreenLayoutAlpha(0.0f);
            this.mClockContainerView.setAlpha(1.0f);
            setBottomAreaAlpha(1.0f);
        }
        ((KeyguardPanelViewInjector) Dependency.get(KeyguardPanelViewInjector.class)).updateNotificationStackScrollerVisibility();
        setViewsAlpha(1.0f);
    }

    private void setBottomAreaAlpha(float f) {
        ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).setAlpha(f);
    }

    /* access modifiers changed from: private */
    public void updateLockScreenMagazineAvailable() {
        this.mLockScreenMagazineAvailable = LockScreenMagazineUtils.isLockScreenMagazineAvailable();
    }

    /* access modifiers changed from: private */
    public void startSwitchAnimator(final boolean z) {
        this.mPreViewShowing = z;
        this.mUpdateMonitorInjector.handleLockScreenMagazinePreViewVisibilityChanged(z);
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
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockScreenMagazineController.this.lambda$startSwitchAnimator$1$LockScreenMagazineController(valueAnimator);
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
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockScreenMagazineController.this.lambda$startSwitchAnimator$2$LockScreenMagazineController(valueAnimator);
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
                LockScreenMagazinePreView access$600 = LockScreenMagazineController.this.mLockScreenMagazinePre;
                if (!z) {
                    i = 4;
                }
                access$600.setFullScreenLayoutVisible(i);
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
    /* renamed from: lambda$startSwitchAnimator$1 */
    public /* synthetic */ void lambda$startSwitchAnimator$1$LockScreenMagazineController(ValueAnimator valueAnimator) {
        this.mLockScreenMagazinePre.setFullScreenLayoutAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startSwitchAnimator$2 */
    public /* synthetic */ void lambda$startSwitchAnimator$2$LockScreenMagazineController(ValueAnimator valueAnimator) {
        setViewsAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* access modifiers changed from: private */
    public boolean needGlobalSwitchAnimate() {
        return Build.IS_INTERNATIONAL_BUILD && isSupportLockScreenMagazineLeft() && !MiuiKeyguardUtils.isGxzwSensor();
    }

    private void setViewsAlpha(float f) {
        this.mNotificationStackScrollLayout.setAlpha(f);
        this.mLockScreenMagazinePre.setMainLayoutAlpha(f);
        ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).setAlpha(f);
        ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).setAlpha(f);
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

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mLockScreenMagazineAvailable) {
            return false;
        }
        if (isMisOperation(motionEvent)) {
            return isPreViewVisible();
        }
        if (!this.mSupportGestureWakeup || !MiuiKeyguardUtils.supportDoubleTapSleep()) {
            return handleSingleClickEvent();
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 200);
        return true;
    }

    private boolean isMisOperation(MotionEvent motionEvent) {
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
    public boolean handleSingleClickEvent() {
        if (!this.mLockScreenMagazineAvailable) {
            return false;
        }
        if (shouldShowPreView()) {
            return handleSwitchAnimator();
        }
        if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() && isSupportLockScreenMagazineLeft()) {
            this.mKeyguardBottomArea.startButtonLayoutAnimate(true);
        }
        AnalyticsHelper.getInstance(this.mContext).record("action_main_screen_click");
        return false;
    }

    private boolean shouldShowPreView() {
        if (Build.IS_INTERNATIONAL_BUILD) {
            return WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper();
        }
        return WallpaperAuthorityUtils.isLockScreenMagazineWallpaper();
    }

    private boolean handleSwitchAnimator() {
        AnimatorSet animatorSet = this.mSwitchAnimator;
        if (animatorSet != null && (animatorSet.isRunning() || this.mSwitchAnimator.isStarted() || this.mIsSwitchAnimating)) {
            return false;
        }
        if (this.mPreViewShowing) {
            startSwitchAnimator(false);
        } else {
            startSwitchAnimator(true);
            AnalyticsHelper.getInstance(this.mContext).recordLockScreenMagazinePreviewAction("show");
            LockScreenMagazineUtils.notifyFullScreenClickRecordEvent(this.mContext);
        }
        return true;
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
            updateLockScreenMagazineWallpaperInfo();
        }
    }

    class MagazineResourceEntity {
        public boolean mIsLockScreenMagazinePkgExist = true;
        public boolean mIsSupportLeftOverlay;
        public String mPreLeftScreenActivityName;
        public String mPreLeftScreenDrawableResName;
        public Drawable mPreMainEntryDarkIcon;
        public Drawable mPreMainEntryLightIcon;
        public String mPreMainEntryResDarkIconName;
        public String mPreMainEntryResLightIconName;
        public String mPreTransToLeftScreenDrawableResName;

        MagazineResourceEntity(LockScreenMagazineController lockScreenMagazineController) {
        }
    }

    /* access modifiers changed from: private */
    public void initLockScreenMagazinePreRes() {
        if (this.mUpdateMonitor.isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()) && MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(this.mContext)) {
            Log.d("LockScreenMagazineController", "initLockScreenMagazinePreRes");
            new AsyncTask<Void, Void, MagazineResourceEntity>() {
                /* access modifiers changed from: protected */
                public MagazineResourceEntity doInBackground(Void... voidArr) {
                    Bundle lockScreenMagazinePreContent = LockScreenMagazineUtils.getLockScreenMagazinePreContent(LockScreenMagazineController.this.mContext);
                    String string = lockScreenMagazinePreContent != null ? lockScreenMagazinePreContent.getString("result_json") : null;
                    Log.d("LockScreenMagazineController", "initLockScreenMagazinePreRes resultJson = " + string);
                    MagazineResourceEntity magazineResourceEntity = new MagazineResourceEntity(LockScreenMagazineController.this);
                    magazineResourceEntity.mIsLockScreenMagazinePkgExist = PackageUtils.isAppInstalledForUser(LockScreenMagazineController.this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, KeyguardUpdateMonitor.getCurrentUser());
                    if (!TextUtils.isEmpty(string)) {
                        try {
                            JSONObject jSONObject = new JSONObject(string);
                            magazineResourceEntity.mPreLeftScreenActivityName = LockScreenMagazineController.this.checkLeftScreenActivityExist(jSONObject.optString("leftscreen_activity"));
                            magazineResourceEntity.mIsSupportLeftOverlay = jSONObject.optBoolean("is_support_overlay");
                            magazineResourceEntity.mPreMainEntryResDarkIconName = jSONObject.optString("main_entry_res_icon_dark_svg");
                            magazineResourceEntity.mPreMainEntryResLightIconName = jSONObject.optString("main_entry_res_icon_light_svg");
                            magazineResourceEntity.mPreTransToLeftScreenDrawableResName = jSONObject.optString("trans_to_leftscreen_res_drawable");
                            magazineResourceEntity.mPreLeftScreenDrawableResName = jSONObject.optString("leftscreen_res_drawable_preview");
                        } catch (Exception e) {
                            Log.e("LockScreenMagazineController", "initLockScreenMagazinePreRes", e);
                        }
                        magazineResourceEntity.mPreMainEntryDarkIcon = PackageUtils.getDrawableFromPackage(LockScreenMagazineController.this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, magazineResourceEntity.mPreMainEntryResDarkIconName);
                        magazineResourceEntity.mPreMainEntryLightIcon = PackageUtils.getDrawableFromPackage(LockScreenMagazineController.this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, magazineResourceEntity.mPreMainEntryResLightIconName);
                    }
                    return magazineResourceEntity;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(MagazineResourceEntity magazineResourceEntity) {
                    String unused = LockScreenMagazineController.this.mPreMainEntryResDarkIconName = magazineResourceEntity.mPreMainEntryResDarkIconName;
                    String unused2 = LockScreenMagazineController.this.mPreMainEntryResLightIconName = magazineResourceEntity.mPreMainEntryResLightIconName;
                    String unused3 = LockScreenMagazineController.this.mPreTransToLeftScreenDrawableResName = magazineResourceEntity.mPreTransToLeftScreenDrawableResName;
                    String unused4 = LockScreenMagazineController.this.mPreLeftScreenDrawableResName = magazineResourceEntity.mPreLeftScreenDrawableResName;
                    boolean unused5 = LockScreenMagazineController.this.mIsLockScreenMagazinePkgExist = magazineResourceEntity.mIsLockScreenMagazinePkgExist;
                    Drawable unused6 = LockScreenMagazineController.this.mPreMainEntryDarkIcon = magazineResourceEntity.mPreMainEntryDarkIcon;
                    Drawable unused7 = LockScreenMagazineController.this.mPreMainEntryLightIcon = magazineResourceEntity.mPreMainEntryLightIcon;
                    String unused8 = LockScreenMagazineController.this.mPreLeftScreenActivityName = magazineResourceEntity.mPreLeftScreenActivityName;
                    if (!TextUtils.isEmpty(magazineResourceEntity.mPreLeftScreenActivityName)) {
                        LockScreenMagazineController.this.setSupportLockScreenMagazineLeft(true);
                    } else {
                        LockScreenMagazineController.this.setSupportLockScreenMagazineLeft(false);
                    }
                    LockScreenMagazineController.this.setSupportLockScreenMagazineOverlay(magazineResourceEntity.mIsSupportLeftOverlay);
                    LockScreenMagazineController.this.mUpdateMonitorInjector.onMagazineResourceInited();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    public String checkLeftScreenActivityExist(String str) {
        String str2 = "";
        if (!TextUtils.isEmpty(str)) {
            try {
                String[] split = str.split("/");
                if (split != null && split.length > 1) {
                    str2 = split[1];
                }
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, str2));
                if (PackageUtils.resolveIntent(this.mContext, intent) == null) {
                    return null;
                }
            } catch (Exception e) {
                Log.e("LockScreenMagazineController", "handlePreLeftScreenActivityName failed", e);
                return null;
            }
        }
        return str2;
    }

    public void initPreMainEntryIcon() {
        this.mPreMainEntryDarkIcon = PackageUtils.getDrawableFromPackage(this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, this.mPreMainEntryResDarkIconName);
        this.mPreMainEntryLightIcon = PackageUtils.getDrawableFromPackage(this.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, this.mPreMainEntryResLightIconName);
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
    public void queryLockScreenMagazineWallpaperInfo() {
        if (this.mLockScreenMagazineAvailable && WallpaperAuthorityUtils.isLockScreenMagazineWallpaper()) {
            try {
                this.mLockScreenMagazineWallpaperInfo = (LockScreenMagazineWallpaperInfo) new Gson().fromJson(((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).getCurrentWallpaperString(), LockScreenMagazineWallpaperInfo.class);
            } catch (Exception e) {
                Log.e("LockScreenMagazineController", "getLockScreenMagazineWallpaperInfo fromJson error:" + e.getMessage());
            }
            LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
            if (lockScreenMagazineWallpaperInfo != null) {
                lockScreenMagazineWallpaperInfo.initExtra();
            } else {
                this.mLockScreenMagazineWallpaperInfo = new LockScreenMagazineWallpaperInfo();
            }
            updateLockScreenMagazineWallpaperInfo();
        }
    }

    public Intent getPreLeftScreenIntent() {
        if (!isSupportLockScreenMagazineLeft()) {
            return null;
        }
        try {
            String preLeftScreenActivityName = getPreLeftScreenActivityName();
            if (TextUtils.isEmpty(preLeftScreenActivityName)) {
                return null;
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, preLeftScreenActivityName));
            intent.addFlags(268435456);
            if (Build.IS_INTERNATIONAL_BUILD) {
                intent.putExtra("wc_enable_source", "systemui");
                intent.putExtra("wallpaper_uri", getLockScreenMagazineWallpaperInfo().wallpaperUri);
                intent.putExtra("wallpaper_details", new Gson().toJson(getLockScreenMagazineWallpaperInfo()));
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
        StatusBar statusBar = (StatusBar) Dependency.get(StatusBar.class);
        if (statusBar != null) {
            statusBar.startActivity(intent, true);
        }
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mKeyguardSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    public boolean isLockScreenMagazinePkgExist() {
        return this.mIsLockScreenMagazinePkgExist;
    }

    public boolean isLockScreenLeftOverlayAvailable() {
        return this.mLockScreenLeftOverlayAvailable;
    }

    public void setLockScreenLeftOverlayAvailable(boolean z) {
        this.mLockScreenLeftOverlayAvailable = z;
    }

    public void setSupportLockScreenMagazineLeft(boolean z) {
        this.mIsSupportLockScreenMagazineLeft = z;
    }

    public boolean isSupportLockScreenMagazineLeft() {
        return this.mIsSupportLockScreenMagazineLeft;
    }

    public boolean isSupportLockScreenMagazineLeftOverlay() {
        return this.mIsSupportLockScreenMagazineLeftOverlay;
    }

    public void setSupportLockScreenMagazineOverlay(boolean z) {
        this.mIsSupportLockScreenMagazineLeftOverlay = z;
    }

    public LockScreenMagazineWallpaperInfo getLockScreenMagazineWallpaperInfo() {
        return this.mLockScreenMagazineWallpaperInfo;
    }

    public void handleLockScreenMagazinePreViewVisibilityChanged(boolean z) {
        this.mLockScreenMagazinePreViewVisible = z;
    }

    public boolean isPreViewVisible() {
        return this.mLockScreenMagazinePreViewVisible;
    }

    public void startMagazineActivity(final long j) {
        if (!Build.IS_TABLET && !this.mIsJumpingIntent) {
            new AsyncTask<Void, Void, Bundle>() {
                /* access modifiers changed from: protected */
                public Bundle doInBackground(Void... voidArr) {
                    boolean unused = LockScreenMagazineController.this.mIsJumpingIntent = true;
                    return LockScreenMagazineController.this.getMagazineActivityExtras(j);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Bundle bundle) {
                    ComponentName unflattenFromString = LockScreenMagazineController.this.mPreviewComponent != null ? ComponentName.unflattenFromString(LockScreenMagazineController.this.mPreviewComponent) : null;
                    if (bundle == null || unflattenFromString == null) {
                        Log.e("LockScreenMagazineController", "start activity failed result:" + bundle + "component:" + unflattenFromString);
                    } else {
                        Intent intent = new Intent();
                        intent.setComponent(unflattenFromString);
                        intent.addFlags(268435456);
                        intent.putExtras(bundle);
                        try {
                            LockScreenMagazineController.this.mContext.startActivityAsUser(intent, ActivityOptions.makeCustomAnimation(LockScreenMagazineController.this.mContext, 0, 0).toBundle(), UserHandle.CURRENT);
                        } catch (Exception e) {
                            Log.e("LockScreenMagazineController", "start activity failed.", e);
                        }
                    }
                    boolean unused = LockScreenMagazineController.this.mIsJumpingIntent = false;
                }
            }.execute(new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00c0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.os.Bundle getMagazineActivityExtras(long r10) {
        /*
            r9 = this;
            r0 = 0
            r9.mPreviewComponent = r0
            android.content.Context r1 = r9.mContext
            boolean r1 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.isValidWallpaperAuthority(r1)
            if (r1 == 0) goto L_0x0010
            java.lang.String r1 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.getWallpaperAuthority()
            goto L_0x0012
        L_0x0010:
            java.lang.String r1 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.APPLY_MAGAZINE_DEFAULT_AUTHORITY
        L_0x0012:
            android.content.Context r2 = r9.mContext
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "content://"
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            android.net.Uri r3 = android.net.Uri.parse(r3)
            boolean r2 = com.android.keyguard.utils.ContentProviderUtils.isProviderExists(r2, r3)
            if (r2 != 0) goto L_0x0030
            return r0
        L_0x0030:
            java.lang.Class<com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController> r2 = com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.class
            java.lang.Object r2 = com.android.systemui.Dependency.get(r2)
            com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController r2 = (com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController) r2
            java.lang.String r2 = r2.getCurrentWallpaperString()
            com.google.gson.Gson r3 = new com.google.gson.Gson     // Catch:{ Exception -> 0x0087 }
            r3.<init>()     // Catch:{ Exception -> 0x0087 }
            java.lang.Class<com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo> r5 = com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo.class
            java.lang.Object r5 = r3.fromJson(r2, r5)     // Catch:{ Exception -> 0x0087 }
            com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo r5 = (com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo) r5     // Catch:{ Exception -> 0x0087 }
            com.android.keyguard.wallpaper.entity.RequestInfo r6 = new com.android.keyguard.wallpaper.entity.RequestInfo     // Catch:{ Exception -> 0x0087 }
            r7 = 2
            r6.<init>(r7, r5)     // Catch:{ Exception -> 0x0087 }
            java.lang.String r5 = r3.toJson(r6)     // Catch:{ Exception -> 0x0087 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0087 }
            r6.<init>()     // Catch:{ Exception -> 0x0087 }
            r6.append(r4)     // Catch:{ Exception -> 0x0087 }
            r6.append(r1)     // Catch:{ Exception -> 0x0087 }
            java.lang.String r1 = r6.toString()     // Catch:{ Exception -> 0x0087 }
            java.lang.String r1 = r9.getLockWallpaperListFromProvider(r1, r5)     // Catch:{ Exception -> 0x0087 }
            java.lang.Class<com.android.keyguard.wallpaper.entity.ResultInfo> r4 = com.android.keyguard.wallpaper.entity.ResultInfo.class
            java.lang.Object r1 = r3.fromJson(r1, r4)     // Catch:{ Exception -> 0x0087 }
            com.android.keyguard.wallpaper.entity.ResultInfo r1 = (com.android.keyguard.wallpaper.entity.ResultInfo) r1     // Catch:{ Exception -> 0x0087 }
            if (r1 == 0) goto L_0x0082
            java.lang.String r4 = r1.previewComponent     // Catch:{ Exception -> 0x0087 }
            r9.mPreviewComponent = r4     // Catch:{ Exception -> 0x0087 }
            java.lang.String r9 = r1.dialogComponent     // Catch:{ Exception -> 0x0087 }
            java.util.List<com.android.keyguard.wallpaper.entity.MiuiWallpaperInfo> r1 = r1.wallpaperInfos     // Catch:{ Exception -> 0x0080 }
            java.lang.String r0 = r3.toJson(r1)     // Catch:{ Exception -> 0x0080 }
            r8 = r0
            r0 = r9
            r9 = r8
            goto L_0x0083
        L_0x0080:
            r1 = move-exception
            goto L_0x0089
        L_0x0082:
            r9 = r0
        L_0x0083:
            r8 = r0
            r0 = r9
            r9 = r8
            goto L_0x00a3
        L_0x0087:
            r1 = move-exception
            r9 = r0
        L_0x0089:
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "getPreviewActivityExtras"
            r3.append(r4)
            java.lang.String r1 = r1.getMessage()
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            java.lang.String r3 = "LockScreenMagazineController"
            android.util.Log.e(r3, r1)
        L_0x00a3:
            android.os.Bundle r1 = new android.os.Bundle
            r1.<init>()
            java.lang.String r3 = "showtime"
            r1.putLong(r3, r10)
            java.lang.String r10 = "currentWallpaperInfo"
            r1.putString(r10, r2)
            java.lang.String r10 = "wallpaperInfos"
            r1.putString(r10, r0)
            java.lang.String r10 = "dialogComponent"
            r1.putString(r10, r9)
            boolean r9 = miui.os.Build.IS_INTERNATIONAL_BUILD
            if (r9 == 0) goto L_0x00c7
            java.lang.String r9 = "entry_source"
            java.lang.String r10 = "cta"
            r1.putString(r9, r10)
        L_0x00c7:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.magazine.LockScreenMagazineController.getMagazineActivityExtras(long):android.os.Bundle");
    }

    private String getLockWallpaperListFromProvider(String str, String str2) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("request_json", str2);
            Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(this.mContext, str, "getNextLockWallpaperUri", (String) null, bundle);
            if (resultFromProvider == null) {
                return null;
            }
            return resultFromProvider.getString("result_json");
        } catch (Exception e) {
            Log.e("LockScreenMagazineController", "getLockWallpaperListFromProvider failed." + e.getMessage());
            return null;
        }
    }

    public void onStartedWakingUp() {
        this.mStartedWakingUp = true;
        LockScreenMagazineUtils.sendLockScreenMagazineScreenOnBroadcast(this.mContext);
    }

    public void onStartedGoingToSleep() {
        this.mStartedWakingUp = false;
        LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Screen_OFF");
    }

    public void onKeyguardShowingChanged(boolean z) {
        if (!z) {
            LockScreenMagazineUtils.sendLockScreenMagazineUnlockBroadcast(this.mContext);
        }
    }

    public void onFinishedGoingToSleep() {
        reset();
    }
}
