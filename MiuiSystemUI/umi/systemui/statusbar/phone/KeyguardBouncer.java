package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManagerCompat;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.faceunlock.FaceUnlockCallback;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.plugins.ActivityStarter;
import miui.system.R;
import miui.util.CustomizeUtil;
import miui.util.ScreenshotUtils;

public class KeyguardBouncer {
    /* access modifiers changed from: private */
    public ImageView mBgImageView;
    /* access modifiers changed from: private */
    public int mBouncerPromptReason;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (KeyguardBouncer.this.isAllowUnlockForBle()) {
                KeyguardBouncer.this.unlockByBle();
            }
        }
    };
    protected final ViewMediatorCallback mCallback;
    protected final ViewGroup mContainer;
    protected final Context mContext;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    /* access modifiers changed from: private */
    public boolean mFaceAuthTimeOut;
    /* access modifiers changed from: private */
    public final Runnable mFaceShakeRunnable = new Runnable(this) {
        public void run() {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performExtHapticFeedback(82);
        }
    };
    private FaceUnlockCallback mFaceUnlockCallBack = new FaceUnlockCallback() {
        public void onFaceAuthTimeOut(boolean z) {
            boolean unused = KeyguardBouncer.this.mFaceAuthTimeOut = true;
        }
    };
    private final FalsingManager mFalsingManager;
    /* access modifiers changed from: private */
    public boolean mForceBlack = false;
    private ContentObserver mForceBlackObserver;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHasUnlockByBle = false;
    /* access modifiers changed from: private */
    public boolean mIsLegacyKeyguardWallpaper;
    protected KeyguardHostView mKeyguardView;
    protected final LockPatternUtils mLockPatternUtils;
    /* access modifiers changed from: private */
    public View mNotchCorner;
    private final Runnable mRemoveViewRunnable = new Runnable() {
        public void run() {
            KeyguardBouncer.this.removeView();
        }
    };
    protected ViewGroup mRoot;
    private final Runnable mShowRunnable = new Runnable() {
        public void run() {
            KeyguardBouncer.this.mRoot.setVisibility(0);
            KeyguardBouncer.this.mKeyguardView.onResume();
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            keyguardBouncer.showPromptReason(keyguardBouncer.mBouncerPromptReason);
            if (!KeyguardBouncer.this.isFullscreenBouncer() && KeyguardBouncer.this.mUpdateMonitor.isScreenOn()) {
                if (KeyguardBouncer.this.mFaceAuthTimeOut) {
                    boolean unused = KeyguardBouncer.this.mFaceAuthTimeOut = false;
                    KeyguardBouncer.this.mHandler.postDelayed(KeyguardBouncer.this.mFaceShakeRunnable, 500);
                }
                KeyguardBouncer.this.mKeyguardView.applyHintAnimation(500);
            }
            if (KeyguardBouncer.this.mKeyguardView.getHeight() != 0) {
                KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
            } else {
                KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().removeOnPreDrawListener(this);
                        KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
                        return true;
                    }
                });
                KeyguardBouncer.this.mKeyguardView.requestLayout();
            }
            boolean unused2 = KeyguardBouncer.this.mShowingSoon = false;
            KeyguardBouncer.this.mKeyguardView.sendAccessibilityEvent(32);
            if (KeyguardBouncer.this.isAllowUnlockForBle()) {
                KeyguardBouncer.this.unlockByBle();
                boolean unused3 = KeyguardBouncer.this.mHasUnlockByBle = true;
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mShowingSoon;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStrongAuthStateChanged(int i) {
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            int unused = keyguardBouncer.mBouncerPromptReason = keyguardBouncer.mCallback.getBouncerPromptReason();
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (!KeyguardBouncer.this.mUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot() && z) {
                KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
                int unused = keyguardBouncer.mBouncerPromptReason = keyguardBouncer.mCallback.getBouncerPromptReason();
            }
        }

        public void onKeyguardOccludedChanged(boolean z) {
            if (!KeyguardBouncer.this.mIsLegacyKeyguardWallpaper && KeyguardBouncer.this.mBgImageView != null) {
                if (z) {
                    KeyguardBouncer.this.mBgImageView.setVisibility(0);
                    KeyguardBouncer.this.mBgImageView.setImageDrawable((Drawable) null);
                    KeyguardBouncer.this.mBgImageView.setBackgroundColor(KeyguardBouncer.this.mContext.getResources().getColor(R.color.blur_background_mask));
                    return;
                }
                KeyguardBouncer.this.mBgImageView.setVisibility(8);
            }
        }

        public void onStartedWakingUp() {
            KeyguardHostView keyguardHostView = KeyguardBouncer.this.mKeyguardView;
            if (keyguardHostView != null && keyguardHostView.getAlpha() != 1.0f) {
                KeyguardBouncer.this.mKeyguardView.animate().cancel();
                KeyguardBouncer.this.mKeyguardView.setAlpha(1.0f);
            }
        }

        public void onStartedGoingToSleep(int i) {
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            if (keyguardBouncer.mKeyguardView != null && keyguardBouncer.isShowing() && Dependency.getHost() != null && ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).isWallpaperSupportsAmbientMode()) {
                KeyguardBouncer.this.mKeyguardView.animate().cancel();
                KeyguardBouncer.this.mKeyguardView.animate().alpha(0.0f).setDuration(300).start();
            }
        }
    };
    private final KeyguardUpdateMonitor.WallpaperChangeCallback mWallpaperChangeCallback = new KeyguardUpdateMonitor.WallpaperChangeCallback() {
        public void onWallpaperChange(boolean z) {
            if (z) {
                KeyguardBouncer.this.updateWallpaper();
            }
        }
    };

    public KeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, ViewGroup viewGroup, DismissCallbackRegistry dismissCallbackRegistry) {
        this.mContext = context;
        this.mCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mContainer = viewGroup;
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(context);
        this.mUpdateMonitor = instance;
        instance.registerCallback(this.mUpdateMonitorCallback);
        this.mUpdateMonitor.registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
        FaceUnlockManager.getInstance().registerFaceUnlockCallback(this.mFaceUnlockCallBack);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mHandler = new Handler();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui_keyguard_ble_unlock_succeed");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.CURRENT, intentFilter, (String) null, (Handler) null);
        if (CustomizeUtil.HAS_NOTCH) {
            this.mForceBlackObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean z) {
                    KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
                    boolean unused = keyguardBouncer.mForceBlack = MiuiSettings.Global.getBoolean(keyguardBouncer.mContext.getContentResolver(), "force_black");
                    if (KeyguardBouncer.this.mNotchCorner != null) {
                        KeyguardBouncer.this.mNotchCorner.setVisibility(KeyguardBouncer.this.mForceBlack ? 0 : 8);
                    }
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_black"), false, this.mForceBlackObserver, -1);
            this.mForceBlackObserver.onChange(false);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r2.mRoot;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isAllowUnlockForBle() {
        /*
            r2 = this;
            com.android.keyguard.KeyguardUpdateMonitor r0 = r2.mUpdateMonitor
            boolean r0 = r0.isBleUnlockSuccess()
            if (r0 == 0) goto L_0x0040
            android.view.ViewGroup r0 = r2.mRoot
            if (r0 == 0) goto L_0x0040
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x0040
            boolean r0 = r2.mHasUnlockByBle
            if (r0 != 0) goto L_0x0040
            com.android.keyguard.KeyguardUpdateMonitor r0 = r2.mUpdateMonitor
            int r1 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()
            boolean r0 = r0.isUnlockingWithFingerprintAllowed(r1)
            if (r0 == 0) goto L_0x0040
            com.android.keyguard.KeyguardUpdateMonitor r0 = r2.mUpdateMonitor
            com.android.internal.telephony.IccCardConstants$State r1 = com.android.internal.telephony.IccCardConstants.State.PIN_REQUIRED
            int r0 = r0.getNextSubIdForState(r1)
            boolean r0 = android.telephony.SubscriptionManager.isValidSubscriptionId(r0)
            if (r0 != 0) goto L_0x0040
            com.android.keyguard.KeyguardUpdateMonitor r2 = r2.mUpdateMonitor
            com.android.internal.telephony.IccCardConstants$State r0 = com.android.internal.telephony.IccCardConstants.State.PUK_REQUIRED
            int r2 = r2.getNextSubIdForState(r0)
            boolean r2 = android.telephony.SubscriptionManager.isValidSubscriptionId(r2)
            if (r2 != 0) goto L_0x0040
            r2 = 1
            goto L_0x0041
        L_0x0040:
            r2 = 0
        L_0x0041:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.KeyguardBouncer.isAllowUnlockForBle():boolean");
    }

    /* access modifiers changed from: private */
    public void unlockByBle() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.finish(false, KeyguardUpdateMonitor.getCurrentUser());
            handleBleUnlockSucceed();
        }
    }

    private void handleBleUnlockSucceed() {
        Toast.makeText(this.mContext, com.android.systemui.plugins.R.string.miui_keyguard_ble_unlock_succeed_msg, 0).show();
        AnalyticsHelper.getInstance(this.mContext).recordUnlockWay("band", true);
    }

    /* access modifiers changed from: private */
    public void updateWallpaper() {
        if (this.mBgImageView != null && this.mIsLegacyKeyguardWallpaper) {
            new AsyncTask<Void, Void, Drawable>() {
                /* access modifiers changed from: protected */
                public Drawable doInBackground(Void... voidArr) {
                    return KeyguardWallpaperUtils.getLockWallpaperPreview(KeyguardBouncer.this.mContext);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Drawable drawable) {
                    Bitmap bitmap;
                    int i;
                    int i2;
                    if (KeyguardBouncer.this.mBgImageView != null) {
                        if (drawable == null) {
                            bitmap = null;
                        } else {
                            bitmap = ((BitmapDrawable) drawable).getBitmap();
                        }
                        if (bitmap == null) {
                            i = 0;
                        } else {
                            i = (int) (((float) bitmap.getWidth()) * 0.33333334f);
                        }
                        if (bitmap == null) {
                            i2 = 0;
                        } else {
                            i2 = (int) (((float) bitmap.getHeight()) * 0.33333334f);
                        }
                        if (i <= 0 || i2 <= 0) {
                            KeyguardBouncer.this.mBgImageView.setImageDrawable((Drawable) null);
                            KeyguardBouncer.this.mBgImageView.setBackgroundColor(KeyguardBouncer.this.mContext.getResources().getColor(R.color.blur_background_mask));
                            return;
                        }
                        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, i, i2, true);
                        KeyguardBouncer.this.mBgImageView.setBackgroundColor(0);
                        KeyguardBouncer.this.mBgImageView.setImageDrawable(new BitmapDrawable(KeyguardBouncer.this.mContext.getResources(), ScreenshotUtils.getBlurBackground(createScaledBitmap, (Bitmap) null)));
                        createScaledBitmap.recycle();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public void show(boolean z) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (currentUser != 0 || !UserManagerCompat.isSplitSystemUser()) {
            this.mFalsingManager.onBouncerShown();
            ensureView();
            if (z) {
                this.mKeyguardView.showPrimarySecurityScreen();
            }
            if (this.mRoot.getVisibility() != 0 && !this.mShowingSoon) {
                int currentUser2 = ActivityManager.getCurrentUser();
                boolean z2 = false;
                boolean z3 = !(UserManagerCompat.isSplitSystemUser() && currentUser2 == 0) && currentUser2 == currentUser;
                if (this.mUpdateMonitor.getUserBleAuthenticated(currentUser) && !this.mUpdateMonitor.getUserFingerprintAuthenticated(currentUser) && !this.mUpdateMonitor.getUserFaceAuthenticated(currentUser)) {
                    z2 = true;
                }
                if (!z3 || !this.mKeyguardView.dismiss(currentUser2)) {
                    if (!z3) {
                        Slog.w("KeyguardBouncer", "User can't dismiss keyguard: " + currentUser2 + " != " + currentUser);
                    }
                    this.mShowingSoon = true;
                    DejankUtils.postAfterTraversal(this.mShowRunnable);
                    this.mCallback.onBouncerVisiblityChanged(true);
                    LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Wallpaper_Covered");
                    AnalyticsHelper.getInstance(this.mContext).trackPageEnd("keyguard_view_main_lock_screen", "show_bouncer");
                    if (this.mUpdateMonitor.isKeyguardOccluded()) {
                        this.mContext.sendBroadcastAsUser(new Intent("xiaomi.intent.action.SECURE_KEYGUARD_SHOWN"), UserHandle.CURRENT);
                    }
                } else if (z2) {
                    handleBleUnlockSucceed();
                }
            }
        }
    }

    public void showPromptReason(int i) {
        KeyguardHostView keyguardHostView;
        if (!isFullscreenBouncer() && (keyguardHostView = this.mKeyguardView) != null) {
            keyguardHostView.showPromptReason(i);
        }
    }

    public void showMessage(String str, int i) {
        KeyguardHostView keyguardHostView;
        if (!isFullscreenBouncer() && (keyguardHostView = this.mKeyguardView) != null) {
            keyguardHostView.showMessage(str, i);
        }
    }

    public void showMessage(String str, String str2, int i) {
        KeyguardHostView keyguardHostView;
        if (!isFullscreenBouncer() && (keyguardHostView = this.mKeyguardView) != null) {
            keyguardHostView.showMessage(str, str2, i);
        }
    }

    public void applyHintAnimation(long j) {
        KeyguardHostView keyguardHostView;
        if (!isFullscreenBouncer() && (keyguardHostView = this.mKeyguardView) != null) {
            keyguardHostView.applyHintAnimation(j);
        }
    }

    private void cancelShowRunnable() {
        DejankUtils.removeCallbacks(this.mShowRunnable);
        this.mShowingSoon = false;
        this.mHandler.removeCallbacks(this.mFaceShakeRunnable);
        this.mFaceAuthTimeOut = false;
    }

    public void showWithDismissAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable) {
        ensureView();
        this.mKeyguardView.setOnDismissAction(onDismissAction, runnable);
        show(false);
    }

    public void hide(boolean z) {
        if (isShowing()) {
            this.mDismissCallbackRegistry.notifyDismissCancelled();
        }
        this.mFalsingManager.onBouncerHidden();
        this.mCallback.onBouncerVisiblityChanged(false);
        cancelShowRunnable();
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.cancelDismissAction();
            this.mKeyguardView.cleanUp();
        }
        ViewGroup viewGroup = this.mRoot;
        if (viewGroup != null) {
            viewGroup.setVisibility(4);
            if (z) {
                this.mHandler.postDelayed(this.mRemoveViewRunnable, 50);
            }
        }
    }

    public void startPreHideAnimation(Runnable runnable) {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.startDisappearAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void onFinishedGoingToSleep() {
        ViewGroup viewGroup;
        if (this.mKeyguardView != null && (viewGroup = this.mRoot) != null && viewGroup.getVisibility() == 0) {
            this.mKeyguardView.onPause();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r1 = r1.mRoot;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isShowing() {
        /*
            r1 = this;
            boolean r0 = r1.mShowingSoon
            if (r0 != 0) goto L_0x0011
            android.view.ViewGroup r1 = r1.mRoot
            if (r1 == 0) goto L_0x000f
            int r1 = r1.getVisibility()
            if (r1 != 0) goto L_0x000f
            goto L_0x0011
        L_0x000f:
            r1 = 0
            goto L_0x0012
        L_0x0011:
            r1 = 1
        L_0x0012:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.KeyguardBouncer.isShowing():boolean");
    }

    public void prepare() {
        boolean z = this.mRoot != null;
        ensureView();
        if (z) {
            this.mKeyguardView.showPrimarySecurityScreen();
        }
        this.mBouncerPromptReason = this.mCallback.getBouncerPromptReason();
    }

    /* access modifiers changed from: protected */
    public void ensureView() {
        this.mHandler.removeCallbacks(this.mRemoveViewRunnable);
        if (this.mRoot == null) {
            inflateView();
        }
    }

    /* access modifiers changed from: protected */
    public void inflateView() {
        removeView();
        this.mHandler.removeCallbacks(this.mRemoveViewRunnable);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this.mContext).inflate(com.android.systemui.plugins.R.layout.keyguard_bouncer, (ViewGroup) null);
        this.mRoot = viewGroup;
        this.mBgImageView = (ImageView) viewGroup.findViewById(com.android.systemui.plugins.R.id.keyguard_bouncer_bg);
        View findViewById = this.mRoot.findViewById(com.android.systemui.plugins.R.id.notch_corner_security);
        this.mNotchCorner = findViewById;
        int i = 8;
        findViewById.setVisibility(this.mForceBlack ? 0 : 8);
        KeyguardHostView keyguardHostView = (KeyguardHostView) this.mRoot.findViewById(com.android.systemui.plugins.R.id.keyguard_host_view);
        this.mKeyguardView = keyguardHostView;
        keyguardHostView.setLockPatternUtils(this.mLockPatternUtils);
        this.mKeyguardView.setViewMediatorCallback(this.mCallback);
        ViewGroup viewGroup2 = this.mContainer;
        viewGroup2.addView(this.mRoot, viewGroup2.getChildCount());
        this.mRoot.setVisibility(4);
        this.mHasUnlockByBle = false;
        boolean isLegacyKeyguardWallpaper = ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).isLegacyKeyguardWallpaper();
        this.mIsLegacyKeyguardWallpaper = isLegacyKeyguardWallpaper;
        ImageView imageView = this.mBgImageView;
        if (isLegacyKeyguardWallpaper) {
            i = 0;
        }
        imageView.setVisibility(i);
        updateWallpaper();
    }

    /* access modifiers changed from: protected */
    public void removeView() {
        ViewGroup viewGroup;
        ViewGroup viewGroup2 = this.mRoot;
        if (viewGroup2 != null && viewGroup2.getParent() == (viewGroup = this.mContainer)) {
            viewGroup.removeView(this.mRoot);
            this.mRoot = null;
        }
    }

    public boolean onBackPressed() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        return keyguardHostView != null && keyguardHostView.handleBackKey();
    }

    public boolean needsFullscreenBouncer() {
        ensureView();
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView == null) {
            return false;
        }
        KeyguardSecurityModel.SecurityMode securityMode = keyguardHostView.getSecurityMode();
        if (securityMode == KeyguardSecurityModel.SecurityMode.SimPin || securityMode == KeyguardSecurityModel.SecurityMode.SimPuk) {
            return true;
        }
        return false;
    }

    public boolean isFullscreenBouncer() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView == null) {
            return false;
        }
        KeyguardSecurityModel.SecurityMode currentSecurityMode = keyguardHostView.getCurrentSecurityMode();
        if (currentSecurityMode == KeyguardSecurityModel.SecurityMode.SimPin || currentSecurityMode == KeyguardSecurityModel.SecurityMode.SimPuk) {
            return true;
        }
        return false;
    }

    public boolean isSecure() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        return keyguardHostView == null || keyguardHostView.getSecurityMode() != KeyguardSecurityModel.SecurityMode.None;
    }

    public boolean shouldDismissOnMenuPressed() {
        return this.mKeyguardView.shouldEnableMenuKey();
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        ensureView();
        return this.mKeyguardView.interceptMediaKey(keyEvent);
    }

    public void notifyKeyguardAuthenticated(boolean z) {
        ensureView();
        this.mKeyguardView.finish(z, KeyguardUpdateMonitor.getCurrentUser());
    }
}
