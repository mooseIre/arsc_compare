package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricSourceType;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ImageView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.graphics.BitmapUtils;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.io.PrintWriter;
import miui.system.R;

public class KeyguardBouncer {
    private boolean isDefaultTheme;
    private ImageView mBgImageView;
    private int mBouncerPromptReason;
    protected final ViewMediatorCallback mCallback;
    protected final ViewGroup mContainer;
    protected final Context mContext;
    private final DismissCallbackRegistry mDismissCallbackRegistry;
    private float mExpansion = 1.0f;
    private final BouncerExpansionCallback mExpansionCallback;
    private boolean mFaceAuthTimeOut;
    private final Runnable mFaceShakeRunnable = new Runnable(this) {
        /* class com.android.systemui.statusbar.phone.KeyguardBouncer.AnonymousClass3 */

        public void run() {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(82, false, 0);
        }
    };
    private KeyguardUpdateMonitorCallback mFaceUnlockCallBack = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.statusbar.phone.KeyguardBouncer.AnonymousClass2 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
            super.onBiometricError(i, str, biometricSourceType);
            if (biometricSourceType == BiometricSourceType.FACE && i == 3) {
                KeyguardBouncer.this.mFaceAuthTimeOut = true;
            }
        }
    };
    private final FalsingManager mFalsingManager;
    private Drawable mForegroundDrawable;
    private final Handler mHandler;
    private boolean mIsAnimatingAway;
    private boolean mIsScrimmed;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    protected KeyguardHostView mKeyguardView;
    protected final LockPatternUtils mLockPatternUtils;
    private final Runnable mRemoveViewRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$iQsniWdIxLGqyYwRi09kQAh02M */

        public final void run() {
            KeyguardBouncer.this.removeView();
        }
    };
    protected ViewGroup mRoot;
    private final Runnable mShowRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.phone.KeyguardBouncer.AnonymousClass5 */

        public void run() {
            KeyguardBouncer.this.mRoot.setVisibility(0);
            KeyguardBouncer.this.mKeyguardView.onResume();
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            keyguardBouncer.showPromptReason(keyguardBouncer.mBouncerPromptReason);
            if (!KeyguardBouncer.this.isFullscreenBouncer() && KeyguardBouncer.this.mKeyguardUpdateMonitor.isScreenOn()) {
                if (KeyguardBouncer.this.mFaceAuthTimeOut) {
                    KeyguardBouncer.this.mFaceAuthTimeOut = false;
                    KeyguardBouncer.this.mHandler.postDelayed(KeyguardBouncer.this.mFaceShakeRunnable, 500);
                }
                KeyguardBouncer.this.mKeyguardView.applyHintAnimation(500);
            }
            if (KeyguardBouncer.this.mKeyguardView.getHeight() == 0 || KeyguardBouncer.this.mKeyguardView.getHeight() == KeyguardBouncer.this.mStatusBarHeight) {
                KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    /* class com.android.systemui.statusbar.phone.KeyguardBouncer.AnonymousClass5.AnonymousClass1 */

                    public boolean onPreDraw() {
                        KeyguardBouncer.this.mKeyguardView.getViewTreeObserver().removeOnPreDrawListener(this);
                        KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
                        return true;
                    }
                });
                KeyguardBouncer.this.mKeyguardView.requestLayout();
            } else {
                KeyguardBouncer.this.mKeyguardView.startAppearAnimation();
            }
            KeyguardBouncer.this.mShowingSoon = false;
            if (KeyguardBouncer.this.mExpansion == 0.0f) {
                KeyguardBouncer.this.mKeyguardView.onResume();
                KeyguardBouncer.this.mKeyguardView.resetSecurityContainer();
                KeyguardBouncer keyguardBouncer2 = KeyguardBouncer.this;
                keyguardBouncer2.showPromptReason(keyguardBouncer2.mBouncerPromptReason);
            }
            SysUiStatsLog.write(63, 2);
        }
    };
    private boolean mShowingSoon;
    private int mStatusBarHeight;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.statusbar.phone.KeyguardBouncer.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
            keyguardBouncer.mBouncerPromptReason = keyguardBouncer.mCallback.getBouncerPromptReason();
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            if (!KeyguardBouncer.this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasOwnerUserAuthenticatedSinceBoot() && z) {
                KeyguardBouncer keyguardBouncer = KeyguardBouncer.this;
                keyguardBouncer.mBouncerPromptReason = keyguardBouncer.mCallback.getBouncerPromptReason();
            }
        }
    };
    private final IMiuiKeyguardWallpaperController.IWallpaperChangeCallback mWallpaperChangeCallback = new IMiuiKeyguardWallpaperController.IWallpaperChangeCallback() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$KeyguardBouncer$dLXes0S_KzG5ebErOJJpnU2vSM8 */

        @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.IWallpaperChangeCallback
        public final void onWallpaperChange(boolean z) {
            KeyguardBouncer.this.lambda$new$1$KeyguardBouncer(z);
        }
    };

    public interface BouncerExpansionCallback {
        void onStartingToShow();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$KeyguardBouncer(boolean z) {
        updateWallpaper();
    }

    public KeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, ViewGroup viewGroup, DismissCallbackRegistry dismissCallbackRegistry, FalsingManager falsingManager, BouncerExpansionCallback bouncerExpansionCallback, KeyguardStateController keyguardStateController, KeyguardUpdateMonitor keyguardUpdateMonitor, KeyguardBypassController keyguardBypassController, Handler handler) {
        this.mContext = context;
        this.mCallback = viewMediatorCallback;
        this.mLockPatternUtils = lockPatternUtils;
        this.mContainer = viewGroup;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mFalsingManager = falsingManager;
        this.mDismissCallbackRegistry = dismissCallbackRegistry;
        this.mExpansionCallback = bouncerExpansionCallback;
        this.mHandler = handler;
        keyguardUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
        this.mForegroundDrawable = new ColorDrawable(this.mContext.getResources().getColor(R.color.blur_background_mask));
        this.mKeyguardUpdateMonitor.registerCallback(this.mFaceUnlockCallBack);
    }

    private void updateWallpaper() {
        if (this.mBgImageView == null) {
            return;
        }
        if (!this.isDefaultTheme || !KeyguardWallpaperUtils.isWallpaperShouldBlur() || DeviceConfig.isLowGpuDevice()) {
            new AsyncTask<Void, Void, Drawable>() {
                /* class com.android.systemui.statusbar.phone.KeyguardBouncer.AnonymousClass4 */

                /* access modifiers changed from: protected */
                public Drawable doInBackground(Void... voidArr) {
                    return new BitmapDrawable(KeyguardBouncer.this.mContext.getResources(), ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).getLockWallpaperPreview());
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Drawable drawable) {
                    Bitmap bitmap;
                    int i;
                    int i2;
                    if (KeyguardBouncer.this.mBgImageView == null) {
                        return;
                    }
                    if (!KeyguardWallpaperUtils.isWallpaperShouldBlur()) {
                        KeyguardBouncer.this.mBgImageView.setForeground(KeyguardBouncer.this.mForegroundDrawable);
                        KeyguardBouncer.this.mBgImageView.setImageDrawable(drawable);
                        return;
                    }
                    KeyguardBouncer.this.mBgImageView.setForeground(null);
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
                        KeyguardBouncer.this.mBgImageView.setImageDrawable(null);
                        KeyguardBouncer.this.mBgImageView.setBackgroundColor(KeyguardBouncer.this.mContext.getResources().getColor(R.color.blur_background_mask));
                        return;
                    }
                    Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, i, i2, true);
                    KeyguardBouncer.this.mBgImageView.setBackgroundColor(0);
                    KeyguardBouncer.this.mBgImageView.setImageDrawable(new BitmapDrawable(KeyguardBouncer.this.mContext.getResources(), BitmapUtils.getBlurBackground(createScaledBitmap, null)));
                    createScaledBitmap.recycle();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public void show(boolean z) {
        show(z, true);
    }

    public void show(boolean z, boolean z2) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (currentUser != 0 || !UserManager.isSplitSystemUser()) {
            ensureView();
            this.mIsScrimmed = z2;
            this.mFalsingManager.onBouncerShown();
            if (z) {
                showPrimarySecurityScreen();
            }
            if (this.mRoot.getVisibility() != 0 && !this.mShowingSoon) {
                int currentUser2 = KeyguardUpdateMonitor.getCurrentUser();
                boolean z3 = false;
                boolean z4 = !(UserManager.isSplitSystemUser() && currentUser2 == 0) && currentUser2 == currentUser;
                if (this.mKeyguardUpdateMonitor.getUserBleAuthenticated(currentUser) && !this.mKeyguardUpdateMonitor.getUserUnlockedWithBiometric(currentUser)) {
                    z3 = true;
                }
                if (!z4 || !this.mKeyguardView.dismiss(currentUser2)) {
                    if (!z4) {
                        Log.w("KeyguardBouncer", "User can't dismiss keyguard: " + currentUser2 + " != " + currentUser);
                    }
                    this.mShowingSoon = true;
                    DejankUtils.postAfterTraversal(this.mShowRunnable);
                    this.mCallback.onBouncerVisiblityChanged(true);
                    this.mExpansionCallback.onStartingToShow();
                } else if (z3) {
                    MiuiKeyguardUtils.handleBleUnlockSucceed(this.mContext);
                }
            }
        }
    }

    public boolean isScrimmed() {
        return this.mIsScrimmed;
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
        this.mHandler.removeCallbacks(this.mShowRunnable);
        this.mShowingSoon = false;
    }

    public void showWithDismissAction(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable) {
        ensureView();
        this.mKeyguardView.setOnDismissAction(onDismissAction, runnable);
        show(false);
    }

    public void hide(boolean z) {
        if (isShowing()) {
            SysUiStatsLog.write(63, 1);
            this.mDismissCallbackRegistry.notifyDismissCancelled();
        }
        this.mIsScrimmed = false;
        this.mFalsingManager.onBouncerHidden();
        this.mCallback.onBouncerVisiblityChanged(false);
        cancelShowRunnable();
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.cancelDismissAction();
            this.mKeyguardView.cleanUp();
        }
        this.mIsAnimatingAway = false;
        ViewGroup viewGroup = this.mRoot;
        if (viewGroup != null) {
            viewGroup.setVisibility(4);
            if (z) {
                this.mHandler.postDelayed(this.mRemoveViewRunnable, 50);
            }
        }
    }

    public void startPreHideAnimation(Runnable runnable) {
        this.mIsAnimatingAway = true;
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        if (keyguardHostView != null) {
            keyguardHostView.startDisappearAnimation(runnable);
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void onScreenTurnedOff() {
        ViewGroup viewGroup;
        if (this.mKeyguardView != null && (viewGroup = this.mRoot) != null && viewGroup.getVisibility() == 0) {
            this.mKeyguardView.onPause();
        }
    }

    public boolean isShowing() {
        ViewGroup viewGroup;
        return (this.mShowingSoon || ((viewGroup = this.mRoot) != null && viewGroup.getVisibility() == 0)) && !isAnimatingAway();
    }

    public boolean inTransit() {
        if (!this.mShowingSoon) {
            float f = this.mExpansion;
            if (f == 1.0f || f == 0.0f) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnimatingAway() {
        return this.mIsAnimatingAway;
    }

    public void prepare() {
        boolean z = this.mRoot != null;
        ensureView();
        if (z) {
            showPrimarySecurityScreen();
        }
        this.mBouncerPromptReason = this.mCallback.getBouncerPromptReason();
    }

    private void showPrimarySecurityScreen() {
        this.mKeyguardView.showPrimarySecurityScreen();
    }

    public boolean willDismissWithAction() {
        KeyguardHostView keyguardHostView = this.mKeyguardView;
        return keyguardHostView != null && keyguardHostView.hasDismissActions();
    }

    /* access modifiers changed from: protected */
    public void ensureView() {
        boolean hasCallbacks = this.mHandler.hasCallbacks(this.mRemoveViewRunnable);
        if (this.mRoot == null || hasCallbacks) {
            inflateView();
        }
    }

    /* access modifiers changed from: protected */
    public void inflateView() {
        removeView();
        this.mHandler.removeCallbacks(this.mRemoveViewRunnable);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(this.mContext).inflate(C0017R$layout.keyguard_bouncer, (ViewGroup) null);
        this.mRoot = viewGroup;
        KeyguardHostView keyguardHostView = (KeyguardHostView) viewGroup.findViewById(C0015R$id.keyguard_host_view);
        this.mKeyguardView = keyguardHostView;
        keyguardHostView.setLockPatternUtils(this.mLockPatternUtils);
        this.mKeyguardView.setViewMediatorCallback(this.mCallback);
        ViewGroup viewGroup2 = this.mContainer;
        viewGroup2.addView(this.mRoot, viewGroup2.getChildCount());
        this.mStatusBarHeight = this.mRoot.getResources().getDimensionPixelOffset(C0012R$dimen.status_bar_height);
        this.mRoot.setVisibility(4);
        WindowInsets rootWindowInsets = this.mRoot.getRootWindowInsets();
        if (rootWindowInsets != null) {
            this.mRoot.dispatchApplyWindowInsets(rootWindowInsets);
        }
        this.mBgImageView = (ImageView) this.mRoot.findViewById(C0015R$id.keyguard_bouncer_bg);
        boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        this.isDefaultTheme = isDefaultLockScreenTheme;
        this.mBgImageView.setVisibility((!isDefaultLockScreenTheme || !KeyguardWallpaperUtils.isWallpaperShouldBlur()) ? 0 : 8);
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
        KeyguardSecurityModel.SecurityMode securityMode = ((KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class)).getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
        return securityMode == KeyguardSecurityModel.SecurityMode.SimPin || securityMode == KeyguardSecurityModel.SecurityMode.SimPuk;
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

    public void dump(PrintWriter printWriter) {
        printWriter.println("KeyguardBouncer");
        printWriter.println("  isShowing(): " + isShowing());
        printWriter.println("  mStatusBarHeight: " + this.mStatusBarHeight);
        printWriter.println("  mExpansion: " + this.mExpansion);
        printWriter.println("  mKeyguardView; " + this.mKeyguardView);
        printWriter.println("  mShowingSoon: " + this.mKeyguardView);
        printWriter.println("  mBouncerPromptReason: " + this.mBouncerPromptReason);
        printWriter.println("  mIsAnimatingAway: " + this.mIsAnimatingAway);
    }
}
