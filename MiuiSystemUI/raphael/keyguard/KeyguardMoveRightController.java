package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.keyguard.BaseKeyguardMoveController;
import com.android.keyguard.MiuiKeyguardCameraView;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.Dependency;
import com.miui.systemui.DebugConfig;
import java.io.IOException;

public class KeyguardMoveRightController extends BaseKeyguardMoveController {
    private boolean mCameraViewShowing;
    private final Context mContext;
    private boolean mIsOnIconTouchDown;
    private MiuiKeyguardCameraView mKeyguardCameraView;
    private MiuiKeyguardCameraView.CallBack mKeyguardCameraViewCallBack = new MiuiKeyguardCameraView.CallBack() {
        /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass1 */

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onAnimUpdate(float f) {
            KeyguardMoveRightController.this.mCallBack.onAnimUpdate(f);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onCompletedAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onCompletedAnimationEnd(true);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(false);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onCancelAnimationEnd(boolean z) {
            KeyguardMoveRightController.this.mCallBack.onCancelAnimationEnd(true, z);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onBackAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onBackAnimationEnd(true);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void onVisibilityChanged(boolean z) {
            KeyguardMoveRightController.this.mCameraViewShowing = z;
            KeyguardMoveRightController.this.mCallBack.getMoveIconLayout(true).setVisibility(KeyguardMoveRightController.this.mCameraViewShowing ? 8 : 0);
            if (KeyguardMoveRightController.this.mIsOnIconTouchDown) {
                KeyguardMoveRightController keyguardMoveRightController = KeyguardMoveRightController.this;
                keyguardMoveRightController.mCallBack.updateCanShowGxzw(!keyguardMoveRightController.mCameraViewShowing);
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardCameraView.CallBack
        public void updatePreViewBackground() {
            KeyguardMoveRightController.this.updatePreViewBackground();
        }
    };
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass2 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStartedGoingToSleep(int i) {
            if (KeyguardMoveRightController.this.mCameraViewShowing) {
                KeyguardMoveRightController.this.reset();
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.reset();
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.reset();
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onKeyguardShowingChanged(boolean z) {
            if (!z && KeyguardMoveRightController.this.mKeyguardCameraView != null) {
                KeyguardMoveRightController.this.mKeyguardCameraView.removeViewFromWindow();
                KeyguardMoveRightController.this.mKeyguardCameraView.releaseBitmapResource();
                KeyguardMoveRightController.this.mKeyguardCameraView = null;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onStrongAuthStateChanged(int i) {
            super.onStrongAuthStateChanged(i);
            KeyguardUpdateMonitor unused = KeyguardMoveRightController.this.mKeyguardUpdateMonitor;
            if (i == KeyguardUpdateMonitor.getCurrentUser() && !KeyguardMoveRightController.this.mUserAuthenticatedSinceBoot && KeyguardMoveRightController.this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
                KeyguardMoveRightController.this.mUserAuthenticatedSinceBoot = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass2.AnonymousClass1 */

                    public void run() {
                        KeyguardMoveRightController.this.updatePreViewBackground();
                    }
                }, 2000);
            }
        }
    };
    private boolean mTouchDownInitial;
    private boolean mUserAuthenticatedSinceBoot;

    public KeyguardMoveRightController(Context context, BaseKeyguardMoveController.CallBack callBack) {
        super(callBack);
        this.mContext = context;
        this.mUserAuthenticatedSinceBoot = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        if (MiuiKeyguardUtils.hasNavigationBar(this.mContext)) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, new ContentObserver(new Handler()) {
                /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass3 */

                public void onChange(boolean z) {
                    KeyguardMoveRightController.this.updatePreViewBackground();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePreViewBackground() {
        new AsyncTask<Void, Void, Drawable>() {
            /* class com.android.keyguard.KeyguardMoveRightController.AnonymousClass4 */

            /* access modifiers changed from: protected */
            public Drawable doInBackground(Void... voidArr) {
                if (!KeyguardMoveRightController.this.mUserAuthenticatedSinceBoot) {
                    return null;
                }
                if (!PackageUtils.IS_VELA_CAMERA) {
                    return getDrawableExceptVela();
                }
                KeyguardMoveRightController keyguardMoveRightController = KeyguardMoveRightController.this;
                return keyguardMoveRightController.getDrawableFromPackageBy565(keyguardMoveRightController.mContext, PackageUtils.PACKAGE_NAME_CAMERA, MiuiKeyguardUtils.getCameraImageName(KeyguardMoveRightController.this.mContext, MiuiKeyguardUtils.isFullScreenGestureOpened()));
            }

            private Drawable getDrawableExceptVela() {
                Context context = KeyguardMoveRightController.this.mContext;
                Drawable drawable = null;
                Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(context, "content://" + PackageUtils.PACKAGE_NAME_CAMERA + ".splashProvider", "getCameraSplash", (String) null, (Bundle) null);
                if (resultFromProvider != null) {
                    String valueOf = String.valueOf(resultFromProvider.get("getCameraSplash"));
                    if (!TextUtils.isEmpty(valueOf)) {
                        try {
                            drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(KeyguardMoveRightController.this.mContext.getContentResolver(), Uri.parse(valueOf), KeyguardMoveRightController.this.mContext.getResources()), $$Lambda$KeyguardMoveRightController$4$BLih8lMjXuQGgfkpxsSjkJl_48.INSTANCE);
                        } catch (IOException unused) {
                            Log.e("KeyguardMoveRightController", "updatePreViewBackground ContentProviderUtils.getResultFromProvider splashProvider失败");
                        }
                    }
                }
                if (drawable != null) {
                    return drawable;
                }
                KeyguardMoveRightController keyguardMoveRightController = KeyguardMoveRightController.this;
                return keyguardMoveRightController.getDrawableFromPackageBy565(keyguardMoveRightController.mContext, PackageUtils.PACKAGE_NAME_CAMERA, MiuiKeyguardUtils.getCameraImageName(KeyguardMoveRightController.this.mContext, MiuiKeyguardUtils.isFullScreenGestureOpened()));
            }

            static /* synthetic */ void lambda$getDrawableExceptVela$0(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
                imageDecoder.setAllocator(1);
                imageDecoder.setMemorySizePolicy(0);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Drawable drawable) {
                if (drawable == null) {
                    Log.e("KeyguardMoveRightController", "updatePreViewBackground  onPostExecute resultDrawable == null");
                } else if (KeyguardMoveRightController.this.mKeyguardCameraView != null) {
                    KeyguardMoveRightController.this.mKeyguardCameraView.setPreviewImageDrawable(drawable);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public Drawable getDrawableFromPackageBy565(Context context, String str, String str2) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(str);
            int identifier = resourcesForApplication.getIdentifier(str2, "drawable", str);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            return new BitmapDrawable(this.mContext.getResources(), BitmapFactory.decodeResource(resourcesForApplication, identifier, options));
        } catch (Exception unused) {
            Log.e("KeyguardMoveRightController", "something wrong when get image from" + str);
            return null;
        }
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (!this.mCallBack.isMoveInCenterScreen() || this.mCallBack.isRightMove()) {
            MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
            if (miuiKeyguardCameraView != null) {
                miuiKeyguardCameraView.reset();
                return;
            }
            return;
        }
        if (DebugConfig.DEBUG_KEYGUARD) {
            Log.d("KeyguardMoveRightController", "onTouchDown mTouchDownInitial = true");
        }
        this.mIsOnIconTouchDown = z;
        if (z) {
            if (this.mKeyguardCameraView == null) {
                this.mKeyguardCameraView = new MiuiKeyguardCameraView(this.mContext, this.mKeyguardCameraViewCallBack);
                updatePreViewBackground();
            }
            this.mKeyguardCameraView.onTouchDown(f, f2, this.mIsOnIconTouchDown);
            this.mCallBack.getMoveIconLayout(true).setVisibility(8);
            this.mCallBack.updateCanShowGxzw(false);
        }
        this.mTouchDownInitial = true;
    }

    public boolean onTouchMove(float f, float f2) {
        if (!this.mTouchDownInitial) {
            return false;
        }
        MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
        if (miuiKeyguardCameraView != null) {
            miuiKeyguardCameraView.onTouchMove(f, f2);
        }
        if (!this.mIsOnIconTouchDown) {
            return true;
        }
        this.mCallBack.updateCanShowGxzw(false);
        return true;
    }

    public void onTouchUp(float f, float f2) {
        if (this.mTouchDownInitial) {
            this.mTouchDownInitial = false;
            MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
            if (miuiKeyguardCameraView != null) {
                miuiKeyguardCameraView.onTouchUp(f, f2);
            }
            this.mCallBack.updateSwipingInProgress(false);
        }
    }

    public void reset() {
        MiuiKeyguardCameraView miuiKeyguardCameraView = this.mKeyguardCameraView;
        if (miuiKeyguardCameraView != null) {
            miuiKeyguardCameraView.reset();
        }
        if (this.mCallBack.isMoveInCenterScreen()) {
            this.mCallBack.updateCanShowGxzw(true);
        }
        this.mCallBack.getMoveIconLayout(true).setVisibility(0);
    }
}
