package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.internal.util.Preconditions;
import com.android.systemui.CameraAvailabilityListener;
import com.android.systemui.RegionInterceptingFrameLayout;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.SecureSetting;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import com.miui.systemui.util.OverlayManagerWrapper;
import java.util.Objects;
import java.util.concurrent.Executor;
import miui.util.CustomizeUtil;

public class ScreenDecorations extends SystemUI implements TunerService.Tunable, CommandQueue.Callbacks, ConfigurationController.ConfigurationListener {
    private static final boolean DEBUG_COLOR;
    private static final boolean DEBUG_SCREENSHOT_ROUNDED_CORNERS;
    protected int mBottomCornerRadius;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private CameraAvailabilityListener mCameraListener;
    private CameraAvailabilityListener.CameraTransitionCallback mCameraTransitionCallback = new CameraAvailabilityListener.CameraTransitionCallback(this) {
        /* class com.android.systemui.ScreenDecorations.AnonymousClass1 */

        @Override // com.android.systemui.CameraAvailabilityListener.CameraTransitionCallback
        public void onApplyCameraProtection(Path path, Rect rect) {
        }

        @Override // com.android.systemui.CameraAvailabilityListener.CameraTransitionCallback
        public void onHideCameraProtection() {
        }
    };
    private SecureSetting mColorInversionSetting;
    private CommandQueue mCommandQueue;
    private int mCurrentUserId;
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    protected int[] mDripOverlayTopDrawables = {C0013R$drawable.overlay_screen_round_corner_top_rot90, C0013R$drawable.overlay_screen_round_corner_top, C0013R$drawable.overlay_screen_round_corner_top_rot270, C0013R$drawable.overlay_screen_round_corner_top_rot180};
    private volatile boolean mEnableForceBlack;
    private boolean mForceBlack;
    private ContentObserver mForceBlackObserver;
    protected int[] mForceBlackTopDrawables = {C0013R$drawable.force_black_screen_round_corner_top_rot90, C0013R$drawable.force_black_screen_round_corner_top, C0013R$drawable.force_black_screen_round_corner_top_rot270, C0013R$drawable.force_black_screen_round_corner_top_rot180};
    protected View mForceBlackTopOverlay;
    private boolean mForceBlackV2;
    private ContentObserver mForceBlackV2Observer;
    protected int[] mForceBlackV2TopDrawables = {C0013R$drawable.force_black_top_corner_rot90, C0013R$drawable.force_black_top_corner, C0013R$drawable.force_black_top_corner_rot270, C0013R$drawable.force_black_top_corner_rot180};
    private Handler mHandler;
    private boolean mHandyMode;
    protected boolean mIsRegistered;
    private final Handler mMainHandler;
    protected int[] mNormalBottomDrawables = {C0013R$drawable.screen_round_corner_bottom_rot270, C0013R$drawable.screen_round_corner_bottom_rot180, C0013R$drawable.screen_round_corner_bottom_rot90, C0013R$drawable.screen_round_corner_bottom};
    protected int[] mNormalTopDrawables = {C0013R$drawable.screen_round_corner_top_rot90, C0013R$drawable.screen_round_corner_top, C0013R$drawable.screen_round_corner_top_rot270, C0013R$drawable.screen_round_corner_top_rot180};
    private boolean mOverlayDrip;
    private ContentObserver mOverlayDripObserver;
    private OverlayManagerWrapper mOverlayManager;
    protected View[] mOverlays;
    private boolean mPendingRotationChange;
    private int mRotation;
    private boolean mSupportRoundCorner;
    protected int mTopCornerRadius;
    private final TunerService mTunerService;
    private Runnable mUpdateForceBlackTopOverlayVisibility = new Runnable() {
        /* class com.android.systemui.ScreenDecorations.AnonymousClass6 */

        public void run() {
            ScreenDecorations.this.updateForceBlackTopOverlayVisibility();
        }
    };
    private Runnable mUpdateScreenDecorations = new Runnable() {
        /* class com.android.systemui.ScreenDecorations.AnonymousClass10 */

        public void run() {
            ScreenDecorations.this.updateScreenDecorations();
        }
    };
    private final BroadcastReceiver mUserSwitchIntentReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.ScreenDecorations.AnonymousClass9 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = false;
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                ScreenDecorations.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (ScreenDecorations.this.mForceBlackObserver != null) {
                    ScreenDecorations.this.mForceBlackObserver.onChange(false);
                }
                if (ScreenDecorations.this.mForceBlackV2Observer != null) {
                    ScreenDecorations.this.mForceBlackV2Observer.onChange(false);
                }
                ScreenDecorations.this.mColorInversionSetting.setUserId(ScreenDecorations.this.mCurrentUserId);
                ScreenDecorations screenDecorations = ScreenDecorations.this;
                screenDecorations.updateColorInversion(screenDecorations.mColorInversionSetting.getValue());
            } else if ("miui.action.handymode_change".equals(action)) {
                ScreenDecorations screenDecorations2 = ScreenDecorations.this;
                if (intent.getIntExtra("handymode", 0) != 0) {
                    z = true;
                }
                screenDecorations2.mHandyMode = z;
                ScreenDecorations.this.postUpdateScreenDecorationsFront();
            }
        }
    };
    private WindowManager mWindowManager;

    private static int getBoundPositionFromRotation(int i, int i2) {
        int i3 = i - i2;
        return i3 < 0 ? i3 + 4 : i3;
    }

    public static String getWindowTitleByPos(int i) {
        return "RoundCorner";
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
    }

    static {
        boolean z = SystemProperties.getBoolean("debug.screenshot_rounded_corners", false);
        DEBUG_SCREENSHOT_ROUNDED_CORNERS = z;
        DEBUG_COLOR = z;
    }

    public ScreenDecorations(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher, TunerService tunerService, CommandQueue commandQueue) {
        super(context);
        this.mMainHandler = handler;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mTunerService = tunerService;
        this.mCommandQueue = commandQueue;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        if (CustomizeUtil.HAS_NOTCH) {
            this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        }
        Handler startHandlerThread = startHandlerThread();
        this.mHandler = startHandlerThread;
        startHandlerThread.post(new Runnable() {
            /* class com.android.systemui.$$Lambda$ScreenDecorations$IfAux2ksmJXT9o9i38WaSEQXJTQ */

            public final void run() {
                ScreenDecorations.lambda$IfAux2ksmJXT9o9i38WaSEQXJTQ(ScreenDecorations.this);
            }
        });
    }

    public Handler startHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("ScreenDecorations");
        handlerThread.start();
        return handlerThread.getThreadHandler();
    }

    /* access modifiers changed from: public */
    private void startOnScreenDecorationsThread() {
        this.mSupportRoundCorner = this.mContext.getResources().getBoolean(C0010R$bool.support_round_corner);
        this.mRotation = this.mContext.getDisplay().getRotation();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService(WindowManager.class);
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mOverlayManager = new OverlayManagerWrapper();
        register();
        setupDecorations();
        setupCameraListener();
        AnonymousClass2 r0 = new DisplayManager.DisplayListener() {
            /* class com.android.systemui.ScreenDecorations.AnonymousClass2 */

            public void onDisplayAdded(int i) {
            }

            public void onDisplayRemoved(int i) {
            }

            public void onDisplayChanged(int i) {
                int rotation = ScreenDecorations.this.mContext.getDisplay().getRotation();
                ScreenDecorations screenDecorations = ScreenDecorations.this;
                if (!(screenDecorations.mOverlays == null || screenDecorations.mRotation == rotation)) {
                    ScreenDecorations.this.mPendingRotationChange = true;
                    Log.i("ScreenDecorations", "Rotation changed, deferring " + rotation + ", staying at " + ScreenDecorations.this.mRotation);
                    for (int i2 = 0; i2 < 4; i2++) {
                        View[] viewArr = ScreenDecorations.this.mOverlays;
                        if (viewArr[i2] != null) {
                            ViewTreeObserver viewTreeObserver = viewArr[i2].getViewTreeObserver();
                            ScreenDecorations screenDecorations2 = ScreenDecorations.this;
                            viewTreeObserver.addOnPreDrawListener(new RestartingPreDrawListener(screenDecorations2.mOverlays[i2], i2, rotation));
                        }
                    }
                }
                ScreenDecorations.this.updateOrientation();
            }
        };
        this.mDisplayListener = r0;
        this.mDisplayManager.registerDisplayListener(r0, this.mHandler);
        updateOrientation();
    }

    private void register() {
        if (CustomizeUtil.HAS_NOTCH) {
            OverlayManagerWrapper overlayManagerWrapper = this.mOverlayManager;
            if (overlayManagerWrapper != null && overlayManagerWrapper.isOverlayEnable("com.android.systemui.notch.overlay", this.mCurrentUserId)) {
                this.mOverlayManager.setEnabled("com.android.systemui.notch.overlay", false, this.mCurrentUserId);
            }
            this.mForceBlackObserver = new ContentObserver(this.mHandler) {
                /* class com.android.systemui.ScreenDecorations.AnonymousClass3 */

                public void onChange(boolean z) {
                    ScreenDecorations screenDecorations = ScreenDecorations.this;
                    screenDecorations.mForceBlack = MiuiSettings.Global.getBoolean(screenDecorations.mContext.getContentResolver(), "force_black");
                    ScreenDecorations.this.postUpdateScreenDecorationsFront();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_black"), false, this.mForceBlackObserver, -1);
            this.mForceBlackObserver.onChange(false);
        }
        if (supportOverlayRoundedCorner()) {
            this.mOverlayDripObserver = new ContentObserver(this.mHandler) {
                /* class com.android.systemui.ScreenDecorations.AnonymousClass4 */

                public void onChange(boolean z) {
                    ScreenDecorations screenDecorations = ScreenDecorations.this;
                    boolean z2 = true;
                    if (Settings.Global.getInt(screenDecorations.mContext.getContentResolver(), "overlay_drip", 1) != 1) {
                        z2 = false;
                    }
                    screenDecorations.mOverlayDrip = z2;
                    ScreenDecorations.this.postUpdateScreenDecorationsFront();
                }
            };
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("overlay_drip"), false, this.mOverlayDripObserver, -1);
            this.mOverlayDripObserver.onChange(false);
        }
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    private boolean supportOverlayRoundedCorner() {
        return this.mContext.getResources().getBoolean(C0010R$bool.support_drip_overlay);
    }

    private void setupDecorations() {
        if (hasRoundedCorners()) {
            for (int i = 0; i < 4; i++) {
                if (shouldShowRoundedCorner(i)) {
                    createOverlay(i);
                } else {
                    removeOverlay(i);
                }
            }
        } else {
            removeAllOverlays();
        }
        setupForceBlackTopView();
        if (!hasOverlays()) {
            this.mMainHandler.post(new Runnable() {
                /* class com.android.systemui.$$Lambda$ScreenDecorations$CTk_RNSSvwUoNV8CfAa6W3y0c0A */

                public final void run() {
                    ScreenDecorations.this.lambda$setupDecorations$1$ScreenDecorations();
                }
            });
            SecureSetting secureSetting = this.mColorInversionSetting;
            if (secureSetting != null) {
                secureSetting.setListening(false);
            }
            this.mBroadcastDispatcher.unregisterReceiver(this.mUserSwitchIntentReceiver);
            this.mIsRegistered = false;
        } else if (!this.mIsRegistered) {
            this.mDisplayManager.getDisplay(0).getMetrics(new DisplayMetrics());
            this.mMainHandler.post(new Runnable() {
                /* class com.android.systemui.$$Lambda$ScreenDecorations$ItnW8ZEHeCqCHue6f8abcXewifU */

                public final void run() {
                    ScreenDecorations.this.lambda$setupDecorations$0$ScreenDecorations();
                }
            });
            if (this.mColorInversionSetting == null) {
                AnonymousClass5 r0 = new SecureSetting(this.mContext, this.mHandler, "accessibility_display_inversion_enabled") {
                    /* class com.android.systemui.ScreenDecorations.AnonymousClass5 */

                    @Override // com.android.systemui.qs.SecureSetting
                    public void handleValueChanged(int i, boolean z) {
                        ScreenDecorations.this.updateColorInversion(i);
                    }
                };
                this.mColorInversionSetting = r0;
                r0.setListening(true);
                this.mColorInversionSetting.onChange(false);
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("miui.action.handymode_change");
            this.mBroadcastDispatcher.registerReceiver(this.mUserSwitchIntentReceiver, intentFilter, new HandlerExecutor(this.mHandler), UserHandle.ALL);
            this.mIsRegistered = true;
        }
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$setupDecorations$0 */
    public /* synthetic */ void lambda$setupDecorations$0$ScreenDecorations() {
        this.mTunerService.addTunable(this, "sysui_rounded_size");
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$setupDecorations$1 */
    public /* synthetic */ void lambda$setupDecorations$1$ScreenDecorations() {
        this.mTunerService.removeTunable(this);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setStatus(int i, String str, Bundle bundle) {
        boolean z;
        if ("upate_specail_mode".equals(str) && this.mEnableForceBlack != (z = bundle.getBoolean("enable_config"))) {
            this.mEnableForceBlack = z;
            this.mHandler.removeCallbacks(this.mUpdateForceBlackTopOverlayVisibility);
            this.mHandler.post(this.mUpdateForceBlackTopOverlayVisibility);
        }
    }

    public DisplayCutout getCutout() {
        return this.mContext.getDisplay().getCutout();
    }

    public boolean hasOverlays() {
        if (this.mOverlays == null) {
            return false;
        }
        if (this.mForceBlackTopOverlay != null) {
            return true;
        }
        for (int i = 0; i < 4; i++) {
            if (this.mOverlays[i] != null) {
                return true;
            }
        }
        this.mOverlays = null;
        return false;
    }

    private void removeAllOverlays() {
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                if (this.mOverlays[i] != null) {
                    removeOverlay(i);
                }
            }
            this.mOverlays = null;
        }
    }

    private void removeOverlay(int i) {
        View[] viewArr = this.mOverlays;
        if (viewArr != null && viewArr[i] != null) {
            this.mWindowManager.removeViewImmediate(viewArr[i]);
            this.mOverlays[i] = null;
        }
    }

    private void createOverlay(final int i) {
        Log.d("ScreenDecorations", "createOverlay: " + i);
        if (this.mOverlays == null) {
            this.mOverlays = new View[4];
        }
        View[] viewArr = this.mOverlays;
        if (viewArr[i] == null) {
            viewArr[i] = new ImageView(this.mContext);
            this.mOverlays[i].setSystemUiVisibility(256);
            this.mOverlays[i].setAlpha(0.0f);
            this.mOverlays[i].setForceDarkAllowed(false);
            updateView(i);
            this.mWindowManager.addView(this.mOverlays[i], getWindowLayoutParams(i));
            this.mOverlays[i].addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                /* class com.android.systemui.ScreenDecorations.AnonymousClass7 */

                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    ScreenDecorations.this.mOverlays[i].removeOnLayoutChangeListener(this);
                    ScreenDecorations.this.mOverlays[i].animate().alpha(1.0f).setDuration(1000).start();
                }
            });
            this.mOverlays[i].getViewTreeObserver().addOnPreDrawListener(new ValidatingPreDrawListener(this.mOverlays[i]));
        }
    }

    private void setupForceBlackTopView() {
        View view;
        if (CustomizeUtil.HAS_NOTCH && this.mForceBlackTopOverlay == null) {
            ImageView imageView = new ImageView(this.mContext);
            this.mForceBlackTopOverlay = imageView;
            imageView.setSystemUiVisibility(256);
            this.mForceBlackTopOverlay.setAlpha(0.0f);
            this.mForceBlackTopOverlay.setForceDarkAllowed(false);
            updateForceBlackTopOverlayView();
            this.mWindowManager.addView(this.mForceBlackTopOverlay, getWindowLayoutParams(1, true));
            updateForceBlackTopOverlayVisibility();
            this.mForceBlackTopOverlay.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                /* class com.android.systemui.ScreenDecorations.AnonymousClass8 */

                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    ScreenDecorations.this.mForceBlackTopOverlay.removeOnLayoutChangeListener(this);
                    ScreenDecorations.this.mForceBlackTopOverlay.animate().alpha(1.0f).setDuration(1000).start();
                }
            });
            this.mForceBlackTopOverlay.getViewTreeObserver().addOnPreDrawListener(new ValidatingPreDrawListener(this.mForceBlackTopOverlay));
        } else if (!CustomizeUtil.HAS_NOTCH && (view = this.mForceBlackTopOverlay) != null) {
            this.mWindowManager.removeViewImmediate(view);
            this.mForceBlackTopOverlay = null;
        }
    }

    private void updateForceBlackTopOverlayView() {
        ((ImageView) this.mForceBlackTopOverlay).setBackground(getForceBlackTopDrawableId(getBoundPositionFromRotation(1, this.mRotation)));
    }

    private void updateOverlayVisibility() {
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                View[] viewArr = this.mOverlays;
                if (viewArr[i] != null) {
                    viewArr[i].setVisibility(this.mHandyMode ? 8 : 0);
                }
            }
        }
    }

    private void updateForceBlackTopOverlayVisibility() {
        View view = this.mForceBlackTopOverlay;
        if (view != null) {
            view.setVisibility(((this.mRotation == 0 || !this.mEnableForceBlack) && this.mForceBlack) ? 0 : 8);
        }
    }

    private void updateView(int i) {
        View[] viewArr = this.mOverlays;
        if (viewArr != null && viewArr[i] != null) {
            if (i == 1) {
                ((ImageView) this.mOverlays[i]).setBackground(getTopDrawableId(getBoundPositionFromRotation(i, this.mRotation)));
            } else if (i == 3) {
                ((ImageView) this.mOverlays[i]).setBackground(getBottomDrawableId(getBoundPositionFromRotation(i, this.mRotation)));
            }
        }
    }

    public Drawable getTopDrawableId(int i) {
        int[] iArr;
        if (this.mForceBlackV2) {
            iArr = this.mForceBlackV2TopDrawables;
        } else if (this.mOverlayDrip) {
            iArr = this.mDripOverlayTopDrawables;
        } else {
            iArr = this.mNormalTopDrawables;
        }
        if (iArr == null || i < 0 || i >= iArr.length) {
            return null;
        }
        return getDrawableForDensity(iArr[i]);
    }

    public Drawable getForceBlackTopDrawableId(int i) {
        if (i >= 0) {
            int[] iArr = this.mForceBlackTopDrawables;
            if (i < iArr.length) {
                return getDrawableForDensity(iArr[i]);
            }
        }
        return null;
    }

    public Drawable getBottomDrawableId(int i) {
        if (i >= 0) {
            int[] iArr = this.mNormalBottomDrawables;
            if (i < iArr.length) {
                return getDrawableForDensity(iArr[i]);
            }
        }
        return null;
    }

    private Drawable getDrawableForDensity(int i) {
        TypedValue typedValue = new TypedValue();
        this.mContext.getResources().getValue(i, typedValue, true);
        return this.mContext.getResources().getDrawableForDensity(i, typedValue.density);
    }

    public WindowManager.LayoutParams getWindowLayoutParams(int i) {
        return getWindowLayoutParams(i, false);
    }

    public WindowManager.LayoutParams getWindowLayoutParams(int i, boolean z) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(getWidthLayoutParamByPos(i), getHeightLayoutParamByPos(i), z ? 2042 : 2024, 562037048, -3);
        int i2 = layoutParams.privateFlags | 80;
        layoutParams.privateFlags = i2;
        if (!DEBUG_SCREENSHOT_ROUNDED_CORNERS && !z) {
            layoutParams.privateFlags = 1048576 | i2;
        }
        layoutParams.setTitle(getWindowTitleByPos(i));
        layoutParams.gravity = getOverlayWindowGravity(i);
        layoutParams.layoutInDisplayCutoutMode = 3;
        layoutParams.setFitInsetsTypes(0);
        layoutParams.privateFlags |= 16777216;
        return layoutParams;
    }

    private int getWidthLayoutParamByPos(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        return (boundPositionFromRotation == 1 || boundPositionFromRotation == 3) ? -1 : -2;
    }

    private int getHeightLayoutParamByPos(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        return (boundPositionFromRotation == 1 || boundPositionFromRotation == 3) ? -2 : -1;
    }

    private int getOverlayWindowGravity(int i) {
        int boundPositionFromRotation = getBoundPositionFromRotation(i, this.mRotation);
        if (boundPositionFromRotation == 0) {
            return 3;
        }
        if (boundPositionFromRotation == 1) {
            return 48;
        }
        if (boundPositionFromRotation == 2) {
            return 5;
        }
        if (boundPositionFromRotation == 3) {
            return 80;
        }
        throw new IllegalArgumentException("unknown bound position: " + i);
    }

    private void setupCameraListener() {
        if (this.mContext.getResources().getBoolean(C0010R$bool.config_enableDisplayCutoutProtection)) {
            CameraAvailabilityListener.Factory factory = CameraAvailabilityListener.Factory;
            Context context = this.mContext;
            Handler handler = this.mHandler;
            Objects.requireNonNull(handler);
            CameraAvailabilityListener build = factory.build(context, new Executor(handler) {
                /* class com.android.systemui.$$Lambda$LfzJt661qZfn2w6SYHFbD3aMy0 */
                public final /* synthetic */ Handler f$0;

                {
                    this.f$0 = r1;
                }

                public final void execute(Runnable runnable) {
                    this.f$0.post(runnable);
                }
            });
            this.mCameraListener = build;
            build.addTransitionCallback(this.mCameraTransitionCallback);
            this.mCameraListener.startListening();
        }
    }

    private void updateColorInversion(int i) {
        int i2 = i != 0 ? -1 : -16777216;
        if (DEBUG_COLOR) {
            i2 = -65536;
        }
        ColorStateList.valueOf(i2);
        if (this.mOverlays != null) {
            for (int i3 = 0; i3 < 4; i3++) {
                View[] viewArr = this.mOverlays;
                if (viewArr[i3] != null) {
                    View view = viewArr[i3];
                    if (view instanceof ImageView) {
                        ((ImageView) view).getBackground().setTint(i2);
                    }
                }
            }
        }
    }

    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.$$Lambda$ScreenDecorations$OAK9dGZ7oEZcFQ_E6ZLqmPf58XA */

            public final void run() {
                ScreenDecorations.this.lambda$onConfigurationChanged$2$ScreenDecorations();
            }
        });
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$onConfigurationChanged$2 */
    public /* synthetic */ void lambda$onConfigurationChanged$2$ScreenDecorations() {
        int i = this.mRotation;
        this.mPendingRotationChange = false;
        updateOrientation();
        Log.i("ScreenDecorations", "onConfigChanged from rot " + i + " to " + this.mRotation);
        setupDecorations();
        if (this.mOverlays != null || this.mForceBlackTopOverlay != null) {
            updateLayoutParams();
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        postUpdateScreenDecorationsFront();
    }

    private void updateOrientation() {
        int rotation;
        Log.d("ScreenDecorations", "updateOrientation: " + this.mPendingRotationChange);
        boolean z = this.mHandler.getLooper().getThread() == Thread.currentThread();
        Preconditions.checkState(z, "must call on " + this.mHandler.getLooper().getThread() + ", but was " + Thread.currentThread());
        if (!this.mPendingRotationChange && (rotation = this.mContext.getDisplay().getRotation()) != this.mRotation) {
            this.mRotation = rotation;
            updateScreenDecorations();
        }
    }

    private boolean hasRoundedCorners() {
        return this.mSupportRoundCorner;
    }

    private boolean shouldShowRoundedCorner(int i) {
        if (!hasRoundedCorners()) {
            return false;
        }
        return i == 1 || i == 3;
    }

    private void postUpdateScreenDecorationsFront() {
        this.mHandler.removeCallbacks(this.mUpdateScreenDecorations);
        this.mHandler.postAtFrontOfQueue(this.mUpdateScreenDecorations);
    }

    private void updateScreenDecorations() {
        updateLayoutParams();
        updateBackground();
        updateOverlayVisibility();
        updateForceBlackTopOverlayVisibility();
    }

    private void updateLayoutParams() {
        View view = this.mForceBlackTopOverlay;
        if (view != null) {
            this.mWindowManager.updateViewLayout(view, getWindowLayoutParams(1, true));
        }
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                View[] viewArr = this.mOverlays;
                if (viewArr[i] != null) {
                    this.mWindowManager.updateViewLayout(viewArr[i], getWindowLayoutParams(i));
                }
            }
        }
    }

    private void updateBackground() {
        if (this.mForceBlackTopOverlay != null) {
            updateForceBlackTopOverlayView();
        }
        if (this.mOverlays != null) {
            for (int i = 0; i < 4; i++) {
                if (this.mOverlays[i] != null) {
                    updateView(i);
                }
            }
            updateColorInversion(this.mColorInversionSetting.getValue());
        }
    }

    public static class DisplayCutoutView extends View implements DisplayManager.DisplayListener, RegionInterceptingFrameLayout.RegionInterceptableView {
        public static void boundsFromDirection(DisplayCutout displayCutout, int i, Rect rect) {
            if (i == 3) {
                rect.set(displayCutout.getBoundingRectLeft());
            } else if (i == 5) {
                rect.set(displayCutout.getBoundingRectRight());
            } else if (i == 48) {
                rect.set(displayCutout.getBoundingRectTop());
            } else if (i != 80) {
                rect.setEmpty();
            } else {
                rect.set(displayCutout.getBoundingRectBottom());
            }
        }
    }

    private class RestartingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final int mPosition;
        private final int mTargetRotation;
        private final View mView;

        private RestartingPreDrawListener(View view, int i, int i2) {
            ScreenDecorations.this = r1;
            this.mView = view;
            this.mTargetRotation = i2;
            this.mPosition = i;
        }

        public boolean onPreDraw() {
            this.mView.getViewTreeObserver().removeOnPreDrawListener(this);
            if (this.mTargetRotation == ScreenDecorations.this.mRotation) {
                Log.i("ScreenDecorations", ScreenDecorations.getWindowTitleByPos(this.mPosition) + " already in target rot " + this.mTargetRotation + ", allow draw without restarting it");
                return true;
            }
            ScreenDecorations.this.mPendingRotationChange = false;
            ScreenDecorations.this.updateOrientation();
            Log.i("ScreenDecorations", ScreenDecorations.getWindowTitleByPos(this.mPosition) + " restarting listener fired, restarting draw for rot " + ScreenDecorations.this.mRotation);
            this.mView.invalidate();
            return false;
        }
    }

    public class ValidatingPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
        private final View mView;

        public ValidatingPreDrawListener(View view) {
            ScreenDecorations.this = r1;
            this.mView = view;
        }

        public boolean onPreDraw() {
            int rotation = ScreenDecorations.this.mContext.getDisplay().getRotation();
            if (rotation == ScreenDecorations.this.mRotation || ScreenDecorations.this.mPendingRotationChange) {
                return true;
            }
            Log.i("ScreenDecorations", "Drawing rot " + ScreenDecorations.this.mRotation + ", but display is at rot " + rotation + ". Restarting draw");
            this.mView.invalidate();
            return false;
        }
    }
}
