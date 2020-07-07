package com.android.keyguard.wallpaper.service;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.service.BaseKeyguardWallpaperService;
import com.android.systemui.Dependency;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseKeyguardWallpaperService extends WallpaperService {
    /* access modifiers changed from: private */
    public static final Field ENGINE_M_CURRENT_WINDOW_PRIVATE_FLAGS;
    /* access modifiers changed from: private */
    public static final Field ENGINE_M_LAYOUT;
    /* access modifiers changed from: private */
    public static final Field ENGINE_M_WINDOW_PRIVATE_FLAGS;
    public static final Method ENGINE_SET_BLURCURRENT;
    public static final Method ENGINE_UPDATE_SURFACE;

    static {
        Field field;
        Field field2;
        Field field3;
        Method method;
        Method method2 = null;
        try {
            method = WallpaperService.Engine.class.getDeclaredMethod("updateSurface", new Class[]{Boolean.TYPE, Boolean.TYPE, Boolean.TYPE});
            try {
                method.setAccessible(true);
                field3 = WallpaperService.Engine.class.getDeclaredField("mLayout");
                try {
                    field3.setAccessible(true);
                    field2 = WallpaperService.Engine.class.getDeclaredField("mWindowPrivateFlags");
                    try {
                        field2.setAccessible(true);
                        field = WallpaperService.Engine.class.getDeclaredField("mCurWindowPrivateFlags");
                        try {
                            field.setAccessible(true);
                        } catch (Exception unused) {
                        }
                    } catch (Exception unused2) {
                        field = null;
                    }
                } catch (Exception unused3) {
                    field2 = null;
                    field = field2;
                    method2 = WallpaperService.Engine.class.getDeclaredMethod("setBlurCurrent", new Class[]{Float.TYPE});
                    method2.setAccessible(true);
                    ENGINE_SET_BLURCURRENT = method2;
                    ENGINE_UPDATE_SURFACE = method;
                    ENGINE_M_LAYOUT = field3;
                    ENGINE_M_WINDOW_PRIVATE_FLAGS = field2;
                    ENGINE_M_CURRENT_WINDOW_PRIVATE_FLAGS = field;
                }
            } catch (Exception unused4) {
                field3 = null;
                field2 = field3;
                field = field2;
                method2 = WallpaperService.Engine.class.getDeclaredMethod("setBlurCurrent", new Class[]{Float.TYPE});
                method2.setAccessible(true);
                ENGINE_SET_BLURCURRENT = method2;
                ENGINE_UPDATE_SURFACE = method;
                ENGINE_M_LAYOUT = field3;
                ENGINE_M_WINDOW_PRIVATE_FLAGS = field2;
                ENGINE_M_CURRENT_WINDOW_PRIVATE_FLAGS = field;
            }
        } catch (Exception unused5) {
            method = null;
            field3 = null;
            field2 = field3;
            field = field2;
            method2 = WallpaperService.Engine.class.getDeclaredMethod("setBlurCurrent", new Class[]{Float.TYPE});
            method2.setAccessible(true);
            ENGINE_SET_BLURCURRENT = method2;
            ENGINE_UPDATE_SURFACE = method;
            ENGINE_M_LAYOUT = field3;
            ENGINE_M_WINDOW_PRIVATE_FLAGS = field2;
            ENGINE_M_CURRENT_WINDOW_PRIVATE_FLAGS = field;
        }
        try {
            method2 = WallpaperService.Engine.class.getDeclaredMethod("setBlurCurrent", new Class[]{Float.TYPE});
            method2.setAccessible(true);
        } catch (Exception unused6) {
            Log.d("MiuiKeyguardWallpaper", "not found setBlurCurrent");
        }
        ENGINE_SET_BLURCURRENT = method2;
        ENGINE_UPDATE_SURFACE = method;
        ENGINE_M_LAYOUT = field3;
        ENGINE_M_WINDOW_PRIVATE_FLAGS = field2;
        ENGINE_M_CURRENT_WINDOW_PRIVATE_FLAGS = field;
    }

    class BaseEngine extends WallpaperService.Engine implements MiuiKeyguardWallpaperController.KeyguardWallpaperCallback {
        float mBlurRatio = 0.0f;
        protected final Context mContext = BaseKeyguardWallpaperService.this;
        boolean mEngineVisible;
        protected final Handler mHandler = new Handler(Looper.getMainLooper());
        private final KeyguardUpdateMonitorCallback mKeyguardCallback;
        boolean mKeyguardShowing;
        boolean mKeyguardWallpaperVisible;
        public WindowManager.LayoutParams mLayoutParams;
        boolean mSurfaceCreated;
        private final Runnable mUpdateSurface = new Runnable() {
            public final void run() {
                BaseKeyguardWallpaperService.BaseEngine.this.updateSurfaceAttrs();
            }
        };
        boolean mWakingUp;
        float mWallpaperAnimValue = -1.0f;
        final MiuiKeyguardWallpaperController.KeyguardWallpaperType mWallpaperType;

        /* access modifiers changed from: protected */
        public void onKeyguardGoingAway() {
        }

        public void onKeyguardWallpaperUpdated(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType, boolean z, File file, Drawable drawable) {
        }

        /* access modifiers changed from: protected */
        public void onScreenTurnedOff() {
        }

        /* access modifiers changed from: protected */
        public void onStartedGoingToSleep(int i) {
        }

        /* access modifiers changed from: protected */
        public void onStartedWakingUp() {
        }

        public void onWallpaperAnimationUpdated(boolean z) {
        }

        BaseEngine(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType) {
            super(BaseKeyguardWallpaperService.this);
            this.mWallpaperType = keyguardWallpaperType;
            this.mKeyguardCallback = new KeyguardUpdateMonitorCallback(BaseKeyguardWallpaperService.this) {
                public void onStartedWakingUp() {
                    super.onStartedWakingUp();
                    BaseEngine baseEngine = BaseEngine.this;
                    baseEngine.mWakingUp = true;
                    baseEngine.onStartedWakingUp();
                }

                public void onStartedGoingToSleep(int i) {
                    super.onStartedGoingToSleep(i);
                    BaseEngine baseEngine = BaseEngine.this;
                    baseEngine.mWakingUp = false;
                    baseEngine.onStartedGoingToSleep(i);
                }

                public void onScreenTurnedOff() {
                    super.onScreenTurnedOff();
                    BaseEngine.this.onScreenTurnedOff();
                }

                public void onKeyguardShowingChanged(boolean z) {
                    super.onKeyguardShowingChanged(z);
                    BaseEngine baseEngine = BaseEngine.this;
                    baseEngine.mKeyguardShowing = z;
                    baseEngine.onKeyguardShowingChanged(z);
                }

                public void onKeyguardGoingAway() {
                    BaseEngine.this.onKeyguardGoingAway();
                }
            };
            if (BaseKeyguardWallpaperService.ENGINE_M_LAYOUT != null) {
                try {
                    this.mLayoutParams = (WindowManager.LayoutParams) BaseKeyguardWallpaperService.ENGINE_M_LAYOUT.get(this);
                    int intValue = ((Integer) BaseKeyguardWallpaperService.ENGINE_M_WINDOW_PRIVATE_FLAGS.get(this)).intValue() | 16;
                    BaseKeyguardWallpaperService.ENGINE_M_WINDOW_PRIVATE_FLAGS.set(this, Integer.valueOf(intValue));
                    BaseKeyguardWallpaperService.ENGINE_M_CURRENT_WINDOW_PRIVATE_FLAGS.set(this, Integer.valueOf(intValue));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            getSurfaceHolder().setFormat(-3);
            setOffsetNotificationsEnabled(false);
            setTouchEventsEnabled(false);
            this.mKeyguardShowing = KeyguardUpdateMonitor.getInstance(this.mContext).isKeyguardShowing();
            ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).addCallback(this);
            KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mKeyguardCallback);
        }

        public void onDestroy() {
            super.onDestroy();
            ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).removeCallback(this);
            KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mKeyguardCallback);
        }

        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
            this.mSurfaceCreated = true;
        }

        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            super.onSurfaceDestroyed(surfaceHolder);
            this.mSurfaceCreated = false;
        }

        public void onVisibilityChanged(boolean z) {
            super.onVisibilityChanged(z);
            this.mEngineVisible = z;
        }

        public void onKeyguardAnimationUpdated(float f) {
            this.mWallpaperAnimValue = f;
            scheduleUpdateSurface();
        }

        public void onWallpaperBlurUpdated(float f) {
            if (this.mBlurRatio != f) {
                this.mBlurRatio = f;
                updateBlurCurrent(this.mBlurRatio);
                updateSurface();
            }
        }

        /* access modifiers changed from: protected */
        public void onKeyguardShowingChanged(boolean z) {
            if (!z) {
                updateBlurCurrent(0.0f);
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                layoutParams.alpha = 0.0f;
                layoutParams.windowAnimations = 0;
                updateSurface();
            }
        }

        /* access modifiers changed from: protected */
        public void scheduleUpdateSurface() {
            this.mHandler.removeCallbacks(this.mUpdateSurface);
            this.mHandler.post(this.mUpdateSurface);
        }

        /* access modifiers changed from: private */
        public void updateSurfaceAttrs() {
            float f;
            float f2;
            if (getSurfaceHolder().getSurface().isValid()) {
                float f3 = this.mWallpaperAnimValue;
                if (f3 > 0.0f) {
                    f2 = (float) (1.0d - Math.pow((double) f3, 2.0d));
                    f = (float) Math.pow((double) (1.0f - Math.abs(Math.max(0.0f, Math.abs(this.mWallpaperAnimValue) - 0.6f) / 0.4f)), 2.0d);
                } else {
                    f = (float) (1.0d - Math.pow((double) (-f3), 3.0d));
                    f2 = 1.0f;
                }
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                if (layoutParams != null && BaseKeyguardWallpaperService.ENGINE_UPDATE_SURFACE != null) {
                    boolean z = (layoutParams.alpha == f2 && layoutParams.blurRatio == f && layoutParams.windowAnimations == 0) ? false : true;
                    updateBlurCurrent(f);
                    WindowManager.LayoutParams layoutParams2 = this.mLayoutParams;
                    layoutParams2.alpha = f2;
                    layoutParams2.windowAnimations = 0;
                    if (z) {
                        updateSurface();
                    }
                }
            }
        }

        private void updateSurface() {
            try {
                BaseKeyguardWallpaperService.ENGINE_UPDATE_SURFACE.invoke(this, new Object[]{true, false, true});
            } catch (Exception e) {
                Log.e("MiuiKeyguardWallpaper", "error in updateSurfaceAttrs", e);
            }
        }

        public void updateBlurCurrent(float f) {
            try {
                if (BaseKeyguardWallpaperService.ENGINE_SET_BLURCURRENT != null) {
                    BaseKeyguardWallpaperService.ENGINE_SET_BLURCURRENT.invoke(this, new Object[]{Float.valueOf(f)});
                }
            } catch (Exception e) {
                Log.e("MiuiKeyguardWallpaper", "error in updateBlurCurrent", e);
            }
        }
    }
}
