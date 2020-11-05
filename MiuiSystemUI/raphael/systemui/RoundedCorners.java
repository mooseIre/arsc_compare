package com.android.systemui;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import android.view.WindowManagerGlobal;
import android.widget.ImageView;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.OverlayManagerWrapper;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ConfigurationController;
import miui.os.Build;
import miui.util.CustomizeUtil;

public class RoundedCorners extends SystemUI implements CommandQueue.Callbacks, ConfigurationController.ConfigurationListener {
    RoundCornerData[] mBottomCorner = {new RoundCornerData(80, -1, -2, R.drawable.screen_round_corner_bottom), new RoundCornerData(5, -2, -1, R.drawable.screen_round_corner_bottom_rot90), new RoundCornerData(48, -1, -2, R.drawable.screen_round_corner_bottom_rot180), new RoundCornerData(3, -2, -1, R.drawable.screen_round_corner_bottom_rot270)};
    private Point mCurrentSize;
    protected int mCurrentUserId = 0;
    /* access modifiers changed from: private */
    public Display mDisplay;
    /* access modifiers changed from: private */
    public boolean mDriveMode;
    /* access modifiers changed from: private */
    public ContentObserver mDriveModeObserver;
    private boolean mEnableNotchConfig;
    /* access modifiers changed from: private */
    public boolean mForceBlack = false;
    private ContentObserver mForceBlackObserver;
    RoundCornerData[] mForceBlackTopCorner = {new RoundCornerData(48, -1, -2, R.drawable.force_black_top_corner), new RoundCornerData(3, -2, -1, R.drawable.force_black_top_corner_rot90), new RoundCornerData(80, -1, -2, R.drawable.force_black_top_corner_rot180), new RoundCornerData(5, -2, -1, R.drawable.force_black_top_corner_rot270)};
    /* access modifiers changed from: private */
    public boolean mForceBlackV2 = false;
    /* access modifiers changed from: private */
    public ContentObserver mForceBlackV2Observer;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHandyMode;
    private ImageView mHideNotchRoundCornerView;
    private Point mInitialSize;
    private boolean mIsRoundCorner;
    RoundCornerData[] mNotchCorner = {new RoundCornerData(48, -1, -2, R.drawable.screen_round_corner_notch), new RoundCornerData(3, -2, -1, R.drawable.screen_round_corner_notch_rot90), new RoundCornerData(80, -1, -2, R.drawable.screen_round_corner_notch_rot180), new RoundCornerData(5, -2, -1, R.drawable.screen_round_corner_notch_rot270)};
    /* access modifiers changed from: private */
    public ImageView mNotchRoundCornerView;
    private Runnable mOnDensityOrFontScaleChanged = new Runnable() {
        public void run() {
            if (RoundedCorners.this.mNotchRoundCornerView != null) {
                RoundedCorners.this.mNotchRoundCornerView.setBackgroundResource(0);
            }
            RoundedCorners.this.updateViews();
        }
    };
    /* access modifiers changed from: private */
    public boolean mOverlayDrip;
    private ContentObserver mOverlayDripObserver;
    /* access modifiers changed from: private */
    public OverlayManagerWrapper mOverlayManager;
    /* access modifiers changed from: private */
    public boolean mPendingRotationChange;
    /* access modifiers changed from: private */
    public int mRoundCornerRotation;
    /* access modifiers changed from: private */
    public ImageView mRoundCornerViewBottom;
    /* access modifiers changed from: private */
    public ImageView mRoundCornerViewTop;
    /* access modifiers changed from: private */
    public SecureSetting mSettings;
    RoundCornerData[] mTopCorner = {new RoundCornerData(48, -1, -2, R.drawable.screen_round_corner_top), new RoundCornerData(3, -2, -1, R.drawable.screen_round_corner_top_rot90), new RoundCornerData(80, -1, -2, R.drawable.screen_round_corner_top_rot180), new RoundCornerData(5, -2, -1, R.drawable.screen_round_corner_top_rot270)};
    RoundCornerData[] mTopOverlayDropCorner = {new RoundCornerData(48, -1, -2, R.drawable.overlay_screen_round_corner_top), new RoundCornerData(3, -2, -1, R.drawable.overlay_screen_round_corner_top_rot90), new RoundCornerData(80, -1, -2, R.drawable.overlay_screen_round_corner_top_rot180), new RoundCornerData(5, -2, -1, R.drawable.overlay_screen_round_corner_top_rot270)};
    private Runnable mUpdateNotchRoundCornerVisibility = new Runnable() {
        public void run() {
            RoundedCorners.this.updateNotchRoundCornerVisibility();
        }
    };
    Runnable mUpdateRoundCornerRunnable = new Runnable() {
        public void run() {
            int rotation;
            if (!RoundedCorners.this.mPendingRotationChange && (rotation = RoundedCorners.this.mDisplay.getRotation()) != RoundedCorners.this.mRoundCornerRotation) {
                int unused = RoundedCorners.this.mRoundCornerRotation = rotation;
                RoundedCorners.this.updateViews();
            }
        }
    };

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onConfigChanged(Configuration configuration) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void preloadRecentApps() {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    /* access modifiers changed from: private */
    public boolean isOverlay(int i) {
        OverlayManagerWrapper.OverlayInfo overlayInfo;
        try {
            overlayInfo = this.mOverlayManager.getOverlayInfo("com.android.systemui.notch.overlay", i);
        } catch (Exception e) {
            Log.w("RoundedCorners", "Can't get overlay info for user " + i, e);
            overlayInfo = null;
        }
        return overlayInfo != null && overlayInfo.isEnabled();
    }

    public void start() {
        boolean z = true;
        if (!SystemProperties.getBoolean("sys.miui.show_round_corner", true) || !this.mContext.getResources().getBoolean(R.bool.support_round_corner)) {
            z = false;
        }
        this.mIsRoundCorner = z;
        this.mHandler = startHandlerThread();
        this.mHandler.post(new Runnable() {
            public void run() {
                RoundedCorners.this.startOnScreenDecorationsThread();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public Handler startHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("RoundedCorners");
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    /* access modifiers changed from: private */
    public void startOnScreenDecorationsThread() {
        this.mCurrentUserId = KeyguardUpdateMonitor.getCurrentUser();
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        this.mInitialSize = new Point();
        try {
            WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(0, this.mInitialSize);
        } catch (RemoteException e) {
            Log.w("RoundedCorners", "Unable to get the display size:" + e);
        }
        this.mCurrentSize = new Point();
        this.mDisplay.getRealSize(this.mCurrentSize);
        initRoundCornerWindows();
        if (CustomizeUtil.HAS_NOTCH) {
            ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
            this.mForceBlackObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean z) {
                    RoundedCorners roundedCorners = RoundedCorners.this;
                    boolean unused = roundedCorners.mForceBlack = MiuiSettings.Global.getBoolean(roundedCorners.mContext.getContentResolver(), "force_black");
                    RoundedCorners.this.resolveConflict();
                    RoundedCorners.this.updateNotchRoundCornerVisibility();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_black"), false, this.mForceBlackObserver, -1);
            this.mForceBlackObserver.onChange(false);
            if (supportForceBlackV2()) {
                this.mOverlayManager = new OverlayManagerWrapper();
                this.mForceBlackV2Observer = new ContentObserver(this.mHandler) {
                    public void onChange(boolean z) {
                        RoundedCorners roundedCorners = RoundedCorners.this;
                        boolean unused = roundedCorners.mForceBlackV2 = MiuiSettings.Global.getBoolean(roundedCorners.mContext.getContentResolver(), "force_black_v2");
                        RoundedCorners.this.resolveConflict();
                        RoundedCorners.this.updateViews();
                        if (CustomizeUtil.HAS_NOTCH && RoundedCorners.this.mOverlayManager != null) {
                            RoundedCorners roundedCorners2 = RoundedCorners.this;
                            boolean access$700 = roundedCorners2.isOverlay(roundedCorners2.mCurrentUserId);
                            boolean access$400 = RoundedCorners.this.mForceBlackV2;
                            if (access$400 != access$700) {
                                try {
                                    RoundedCorners.this.mOverlayManager.setEnabled("com.android.systemui.notch.overlay", access$400, RoundedCorners.this.mCurrentUserId);
                                } catch (Exception e) {
                                    Log.w("RoundedCorners", "Can't apply overlay for user " + RoundedCorners.this.mCurrentUserId, e);
                                }
                            }
                            RoundedCorners roundedCorners3 = RoundedCorners.this;
                            if (roundedCorners3.mCurrentUserId != 0 && access$400 != roundedCorners3.isOverlay(0)) {
                                try {
                                    RoundedCorners.this.mOverlayManager.setEnabled("com.android.systemui.notch.overlay", access$400, 0);
                                } catch (Exception e2) {
                                    Log.w("RoundedCorners", "Can't apply overlay for user owner", e2);
                                }
                            }
                        }
                    }
                };
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_black_v2"), false, this.mForceBlackV2Observer, -1);
                this.mForceBlackV2Observer.onChange(false);
            }
            this.mDriveModeObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean z) {
                    RoundedCorners roundedCorners = RoundedCorners.this;
                    boolean z2 = true;
                    if (Settings.System.getIntForUser(roundedCorners.mContext.getContentResolver(), "drive_mode_drive_mode", 0, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
                        z2 = false;
                    }
                    boolean unused = roundedCorners.mDriveMode = z2;
                    RoundedCorners.this.updateNotchRoundCornerVisibility();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("drive_mode_drive_mode"), false, this.mDriveModeObserver, -1);
            this.mDriveModeObserver.onChange(false);
            if (supportOverlayRoundedCorner()) {
                this.mOverlayDripObserver = new ContentObserver(this.mHandler) {
                    public void onChange(boolean z) {
                        RoundedCorners roundedCorners = RoundedCorners.this;
                        boolean z2 = true;
                        if (Settings.Global.getInt(roundedCorners.mContext.getContentResolver(), "overlay_drip", 1) != 1) {
                            z2 = false;
                        }
                        boolean unused = roundedCorners.mOverlayDrip = z2;
                        RoundedCorners.this.updateViews();
                    }
                };
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("overlay_drip"), false, this.mOverlayDripObserver, -1);
                this.mOverlayDripObserver.onChange(false);
            }
            if (CustomizeUtil.HAS_NOTCH) {
                ImageView imageView = this.mRoundCornerViewTop;
                if (imageView != null) {
                    imageView.getViewTreeObserver().addOnPreDrawListener(new ValidatingPreDrawListener(this.mRoundCornerViewTop));
                }
                ImageView imageView2 = this.mRoundCornerViewBottom;
                if (imageView2 != null) {
                    imageView2.getViewTreeObserver().addOnPreDrawListener(new ValidatingPreDrawListener(this.mRoundCornerViewBottom));
                }
            }
        }
        if (CustomizeUtil.HAS_NOTCH || Build.DEVICE.equals("perseus")) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        }
    }

    /* access modifiers changed from: private */
    public void resolveConflict() {
        if (this.mForceBlack && this.mForceBlackV2 && supportForceBlackV2()) {
            MiuiSettings.Global.putBoolean(this.mContext.getContentResolver(), "force_black", false);
            Log.w("RoundedCorners", "resolve conflict");
        }
    }

    private boolean supportOverlayRoundedCorner() {
        return "cepheus".equals(Build.DEVICE) || "grus".equals(Build.DEVICE) || "crux".equals(Build.DEVICE);
    }

    private boolean supportForceBlackV2() {
        return Build.VERSION.SDK_INT >= 28;
    }

    static class RoundCornerData {
        int backgroundRes;
        int gravity;
        int height;
        int width;

        public RoundCornerData(int i, int i2, int i3, int i4) {
            this.gravity = i;
            this.width = i2;
            this.height = i3;
            this.backgroundRes = i4;
        }
    }

    private void initRoundCornerWindows() {
        if (this.mIsRoundCorner || CustomizeUtil.HAS_NOTCH) {
            if (this.mIsRoundCorner) {
                this.mRoundCornerViewTop = showRoundCornerViewAt(51, R.drawable.screen_round_corner_top);
                this.mRoundCornerViewBottom = showRoundCornerViewAt(83, R.drawable.screen_round_corner_bottom);
            }
            if (CustomizeUtil.HAS_NOTCH) {
                this.mNotchRoundCornerView = showRoundCornerViewAt(51, R.drawable.screen_round_corner_notch, true);
                updateNotchRoundCornerVisibility();
                if (Build.VERSION.SDK_INT >= 28 && this.mRoundCornerViewTop == null) {
                    this.mHideNotchRoundCornerView = showRoundCornerViewAt(51, R.drawable.screen_round_corner_bottom_rot180);
                    updateNotchRoundCornerVisibility();
                }
                this.mRoundCornerRotation = this.mDisplay.getRotation();
                ((DisplayManager) this.mContext.getSystemService("display")).registerDisplayListener(new DisplayManager.DisplayListener() {
                    public void onDisplayAdded(int i) {
                    }

                    public void onDisplayRemoved(int i) {
                    }

                    public void onDisplayChanged(int i) {
                        int rotation = RoundedCorners.this.mDisplay.getRotation();
                        if (RoundedCorners.this.mRoundCornerRotation != rotation) {
                            boolean unused = RoundedCorners.this.mPendingRotationChange = true;
                            if (RoundedCorners.this.mRoundCornerViewTop != null) {
                                ViewTreeObserver viewTreeObserver = RoundedCorners.this.mRoundCornerViewTop.getViewTreeObserver();
                                RoundedCorners roundedCorners = RoundedCorners.this;
                                viewTreeObserver.addOnPreDrawListener(new RestartingPreDrawListener(roundedCorners.mRoundCornerViewTop, rotation));
                            }
                            if (RoundedCorners.this.mRoundCornerViewBottom != null) {
                                ViewTreeObserver viewTreeObserver2 = RoundedCorners.this.mRoundCornerViewBottom.getViewTreeObserver();
                                RoundedCorners roundedCorners2 = RoundedCorners.this;
                                viewTreeObserver2.addOnPreDrawListener(new RestartingPreDrawListener(roundedCorners2.mRoundCornerViewBottom, rotation));
                            }
                        }
                        RoundedCorners.this.updateOrientation();
                    }
                }, this.mHandler);
            }
            this.mSettings = new SecureSetting(this.mContext, this.mHandler, "accessibility_display_inversion_enabled") {
                /* access modifiers changed from: protected */
                public void handleValueChanged(int i, boolean z) {
                    RoundedCorners.this.handleStateChange(i);
                }
            };
            this.mSettings.setListening(true);
            this.mSettings.onChange(false);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("miui.action.handymode_change");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if ("android.intent.action.USER_SWITCHED".equals(action)) {
                        RoundedCorners.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                        RoundedCorners.this.mSettings.setUserId(ActivityManager.getCurrentUser());
                        RoundedCorners roundedCorners = RoundedCorners.this;
                        roundedCorners.handleStateChange(roundedCorners.mSettings.getValue());
                        if (RoundedCorners.this.mDriveModeObserver != null) {
                            RoundedCorners.this.mDriveModeObserver.onChange(false);
                        }
                        if (RoundedCorners.this.mForceBlackV2Observer != null) {
                            RoundedCorners.this.mForceBlackV2Observer.onChange(false);
                        }
                    } else if ("miui.action.handymode_change".equals(action)) {
                        boolean unused = RoundedCorners.this.mHandyMode = intent.getIntExtra("handymode", 0) != 0;
                        int i = 8;
                        if (RoundedCorners.this.mRoundCornerViewTop != null) {
                            RoundedCorners.this.mRoundCornerViewTop.setVisibility(RoundedCorners.this.mHandyMode ? 8 : 0);
                        }
                        if (RoundedCorners.this.mRoundCornerViewBottom != null) {
                            ImageView access$1500 = RoundedCorners.this.mRoundCornerViewBottom;
                            if (!RoundedCorners.this.mHandyMode) {
                                i = 0;
                            }
                            access$1500.setVisibility(i);
                        }
                    }
                }
            }, intentFilter, (String) null, this.mHandler);
        }
    }

    /* access modifiers changed from: private */
    public void handleStateChange(int i) {
        ImageView imageView = this.mRoundCornerViewTop;
        if (imageView != null && this.mRoundCornerViewBottom != null) {
            PorterDuffColorFilter porterDuffColorFilter = null;
            imageView.getBackground().setColorFilter(i != 0 ? new PorterDuffColorFilter(-1, PorterDuff.Mode.SRC_ATOP) : null);
            Drawable background = this.mRoundCornerViewBottom.getBackground();
            if (i != 0) {
                porterDuffColorFilter = new PorterDuffColorFilter(-1, PorterDuff.Mode.SRC_ATOP);
            }
            background.setColorFilter(porterDuffColorFilter);
        }
    }

    /* access modifiers changed from: private */
    public void updateViews() {
        this.mDisplay.getRealSize(this.mCurrentSize);
        RoundCornerData[] roundCornerDataArr = this.mTopCorner;
        int i = this.mRoundCornerRotation;
        RoundCornerData roundCornerData = roundCornerDataArr[i];
        if (this.mForceBlackV2) {
            roundCornerData = this.mForceBlackTopCorner[i];
        } else if (this.mOverlayDrip) {
            roundCornerData = this.mTopOverlayDropCorner[i];
        }
        updateRoundCornerViewAt(this.mRoundCornerViewTop, roundCornerData, false);
        updateRoundCornerViewAt(this.mRoundCornerViewBottom, this.mBottomCorner[this.mRoundCornerRotation], false);
        updateRoundCornerViewAt(this.mHideNotchRoundCornerView, this.mForceBlackTopCorner[this.mRoundCornerRotation], false);
        updateRoundCornerViewAt(this.mNotchRoundCornerView, this.mNotchCorner[this.mRoundCornerRotation], true);
        updateNotchRoundCornerVisibility();
        SecureSetting secureSetting = this.mSettings;
        if (secureSetting != null) {
            handleStateChange(secureSetting.getValue());
        }
    }

    private void updateRoundCornerViewAt(View view, RoundCornerData roundCornerData, boolean z) {
        if (view != null) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) view.getLayoutParams();
            layoutParams.gravity = roundCornerData.gravity;
            layoutParams.width = roundCornerData.width;
            layoutParams.height = roundCornerData.height;
            setBackgroundResource(view, roundCornerData.backgroundRes, z);
            ((WindowManager) this.mContext.getSystemService("window")).updateViewLayout(view, layoutParams);
        }
    }

    private ImageView showRoundCornerViewAt(int i, int i2) {
        return showRoundCornerViewAt(i, i2, false);
    }

    private ImageView showRoundCornerViewAt(int i, int i2, boolean z) {
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        ImageView imageView = new ImageView(this.mContext);
        setBackgroundResource(imageView, i2, z);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -2, z ? WindowManagerCompat.getNotchType() : 2015, 1304, -3);
        layoutParams.privateFlags = 16;
        layoutParams.privateFlags |= 64;
        if (!z || Build.VERSION.SDK_INT < 28) {
            layoutParams.privateFlags |= 1048576;
        }
        layoutParams.gravity = i;
        layoutParams.setTitle("RoundCorner");
        WindowManagerCompat.setLayoutInDisplayCutoutMode(layoutParams, 1);
        windowManager.addView(imageView, layoutParams);
        return imageView;
    }

    private void setBackgroundResource(View view, int i, boolean z) {
        if (z) {
            view.setBackgroundResource(i);
            return;
        }
        Point point = this.mInitialSize;
        int min = Math.min(point.x, point.y);
        Point point2 = this.mCurrentSize;
        int min2 = Math.min(point2.x, point2.y);
        TypedValue typedValue = new TypedValue();
        this.mContext.getResources().getValue(i, typedValue, true);
        view.setBackground(this.mContext.getResources().getDrawableForDensity(i, (typedValue.density * min2) / min));
    }

    /* access modifiers changed from: private */
    public void updateNotchRoundCornerVisibility() {
        ImageView imageView = this.mNotchRoundCornerView;
        int i = 0;
        if (imageView != null) {
            imageView.setVisibility(((this.mRoundCornerRotation == 0 || !this.mEnableNotchConfig) && !this.mDriveMode && this.mForceBlack) ? 0 : 8);
        }
        ImageView imageView2 = this.mHideNotchRoundCornerView;
        if (imageView2 != null) {
            if (!this.mForceBlackV2) {
                i = 8;
            }
            imageView2.setVisibility(i);
        }
    }

    public void setStatus(int i, String str, Bundle bundle) {
        boolean z;
        if (CustomizeUtil.HAS_NOTCH && "upate_specail_mode".equals(str) && this.mEnableNotchConfig != (z = bundle.getBoolean("enable_config"))) {
            this.mEnableNotchConfig = z;
            this.mHandler.removeCallbacks(this.mUpdateNotchRoundCornerVisibility);
            this.mHandler.post(this.mUpdateNotchRoundCornerVisibility);
        }
    }

    public void onDensityOrFontScaleChanged() {
        this.mHandler.removeCallbacks(this.mOnDensityOrFontScaleChanged);
        this.mHandler.post(this.mOnDensityOrFontScaleChanged);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        if (CustomizeUtil.HAS_NOTCH) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    boolean unused = RoundedCorners.this.mPendingRotationChange = false;
                    RoundedCorners.this.updateOrientation();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void updateOrientation() {
        this.mUpdateRoundCornerRunnable.run();
    }

    private class RestartingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final int mTargetRotation;
        private final View mView;

        private RestartingPreDrawListener(View view, int i) {
            this.mView = view;
            this.mTargetRotation = i;
        }

        public boolean onPreDraw() {
            this.mView.getViewTreeObserver().removeOnPreDrawListener(this);
            if (this.mTargetRotation == RoundedCorners.this.mRoundCornerRotation) {
                return true;
            }
            boolean unused = RoundedCorners.this.mPendingRotationChange = false;
            RoundedCorners.this.updateOrientation();
            this.mView.invalidate();
            return false;
        }
    }

    private class ValidatingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final View mView;

        public ValidatingPreDrawListener(View view) {
            this.mView = view;
        }

        public boolean onPreDraw() {
            if (RoundedCorners.this.mDisplay.getRotation() == RoundedCorners.this.mRoundCornerRotation || RoundedCorners.this.mPendingRotationChange) {
                return true;
            }
            this.mView.invalidate();
            return false;
        }
    }
}
