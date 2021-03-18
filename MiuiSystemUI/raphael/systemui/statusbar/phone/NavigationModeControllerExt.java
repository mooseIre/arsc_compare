package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.NavigationBarController;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: NavigationModeControllerExt.kt */
public final class NavigationModeControllerExt {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    public static final NavigationModeControllerExt INSTANCE = new NavigationModeControllerExt();
    private static Context mContext;
    private static NavigationModeControllerExt$mElderlyModeObserver$1 mElderlyModeObserver = new NavigationModeControllerExt$mElderlyModeObserver$1(null);
    private static final NavigationModeControllerExt$mFullScreenGestureListener$1 mFullScreenGestureListener = new NavigationModeControllerExt$mFullScreenGestureListener$1(null);
    private static boolean mHideGestureLine;
    private static NavigationModeControllerExt$mHideGestureLineObserver$1 mHideGestureLineObserver = new NavigationModeControllerExt$mHideGestureLineObserver$1(null);
    private static boolean mIsFsgMode;
    private static final Lazy mOverlayManager$delegate = LazyKt__LazyJVMKt.lazy(NavigationModeControllerExt$mOverlayManager$2.INSTANCE);
    private static final Lazy navigationBarController$delegate = LazyKt__LazyJVMKt.lazy(NavigationModeControllerExt$navigationBarController$2.INSTANCE);

    private final IOverlayManager getMOverlayManager() {
        Lazy lazy = mOverlayManager$delegate;
        KProperty kProperty = $$delegatedProperties[1];
        return (IOverlayManager) lazy.getValue();
    }

    private final NavigationBarController getNavigationBarController() {
        Lazy lazy = navigationBarController$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (NavigationBarController) lazy.getValue();
    }

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(NavigationModeControllerExt.class), "navigationBarController", "getNavigationBarController()Lcom/android/systemui/statusbar/NavigationBarController;");
        Reflection.property1(propertyReference1Impl);
        PropertyReference1Impl propertyReference1Impl2 = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(NavigationModeControllerExt.class), "mOverlayManager", "getMOverlayManager()Landroid/content/om/IOverlayManager;");
        Reflection.property1(propertyReference1Impl2);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl, propertyReference1Impl2};
    }

    private NavigationModeControllerExt() {
    }

    public static final /* synthetic */ Context access$getMContext$p(NavigationModeControllerExt navigationModeControllerExt) {
        Context context = mContext;
        if (context != null) {
            return context;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mContext");
        throw null;
    }

    public final boolean getMIsFsgMode() {
        return mIsFsgMode;
    }

    public final void setMIsFsgMode(boolean z) {
        mIsFsgMode = z;
    }

    private final void init(Context context) {
        Context applicationContext = context.getApplicationContext();
        Intrinsics.checkExpressionValueIsNotNull(applicationContext, "context.applicationContext");
        mContext = applicationContext;
        if (applicationContext != null) {
            mIsFsgMode = MiuiSettings.Global.getBoolean(applicationContext.getContentResolver(), "force_fsg_nav_bar");
            Context context2 = mContext;
            if (context2 != null) {
                boolean z = false;
                if (Settings.Global.getInt(context2.getContentResolver(), "hide_gesture_line", 0) != 0) {
                    z = true;
                }
                mHideGestureLine = z;
                updateOverlayManager();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("mContext");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mContext");
        throw null;
    }

    public final boolean hideNavigationBar() {
        return mIsFsgMode && mHideGestureLine;
    }

    private final void registerHideLineObserver(Context context) {
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("hide_gesture_line"), false, mHideGestureLineObserver);
    }

    private final void registerFullScreenGestureObserver(Context context) {
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, mFullScreenGestureListener);
    }

    private final void registerElderlyModeObserver(Context context) {
        context.getContentResolver().registerContentObserver(Settings.System.getUriFor("elderly_mode"), false, mElderlyModeObserver, -1);
    }

    public final void registerSettingObserver(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        init(context);
        registerFullScreenGestureObserver(context);
        registerHideLineObserver(context);
        registerElderlyModeObserver(context);
    }

    public final void onElderModeChange() {
        int currentUser = ActivityManager.getCurrentUser();
        Context context = mContext;
        if (context != null) {
            boolean z = !MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_fsg_nav_bar") && isElderMode();
            if (z != isOverlay(currentUser, "com.android.systemui.navigation.bar.overlay")) {
                try {
                    Log.d("NavigationModeControllerExt", "needOverlay is" + z);
                    getMOverlayManager().setEnabled("com.android.systemui.navigation.bar.overlay", z, currentUser);
                } catch (Exception e) {
                    Log.e("NavigationModeControllerExt", "Can't apply overlay for user " + currentUser, e);
                }
            }
            if (currentUser != 0) {
                boolean isOverlay = isOverlay(0, "com.android.systemui.navigation.bar.overlay");
                Log.d("NavigationModeControllerExt", "isOverlay is" + isOverlay);
                if (z != isOverlay) {
                    try {
                        Log.d("NavigationModeControllerExt", "mCurrentUserId != UserHandle.USER_OWNER");
                        getMOverlayManager().setEnabled("com.android.systemui.navigation.bar.overlay", z, 0);
                    } catch (Exception e2) {
                        Log.e("NavigationModeControllerExt", "Can't apply overlay for user owner", e2);
                    }
                }
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mContext");
            throw null;
        }
    }

    private final boolean isElderMode() {
        Context context = mContext;
        if (context != null) {
            return MiuiSettings.System.getBooleanForUser(context.getContentResolver(), "elderly_mode", false, ActivityManager.getCurrentUser());
        }
        Intrinsics.throwUninitializedPropertyAccessException("mContext");
        throw null;
    }

    public final void updateElderMode(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        if (!(view instanceof ImageView)) {
            return;
        }
        if (isElderMode()) {
            ((ImageView) view).setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else {
            ((ImageView) view).setScaleType(ImageView.ScaleType.CENTER);
        }
    }

    /* access modifiers changed from: private */
    public final void onGestureLineSettingChange() {
        updateNavigationBarFragment();
    }

    private final void updateNavigationBarFragment() {
        View findViewById;
        View findViewById2;
        if (!mIsFsgMode) {
            getNavigationBarController().addDefaultNavigationBar();
            NavigationBarView defaultNavigationBarView = getNavigationBarController().getDefaultNavigationBarView();
            if (defaultNavigationBarView != null && (findViewById = defaultNavigationBarView.findViewById(C0015R$id.home_handle)) != null) {
                findViewById.setVisibility(8);
            }
        } else if (mHideGestureLine) {
            getNavigationBarController().removeDefaultNavigationBar();
        } else {
            getNavigationBarController().addDefaultNavigationBar();
            NavigationBarView defaultNavigationBarView2 = getNavigationBarController().getDefaultNavigationBarView();
            if (defaultNavigationBarView2 != null && (findViewById2 = defaultNavigationBarView2.findViewById(C0015R$id.home_handle)) != null) {
                findViewById2.setVisibility(0);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void onFsGestureStateChange() {
        updateNavigationBarFragment();
        updateOverlayManager();
    }

    private final void updateOverlayManager() {
        int currentUser = ActivityManager.getCurrentUser();
        boolean z = mIsFsgMode;
        if (z != isOverlay(currentUser, "com.android.systemui.gesture.line.overlay")) {
            if (z) {
                try {
                    getMOverlayManager().setEnabledExclusiveInCategory("com.android.systemui.gesture.line.overlay", currentUser);
                } catch (Exception e) {
                    Log.w("StatusBar", "Can't apply overlay for user " + currentUser, e);
                }
            } else {
                getMOverlayManager().setEnabled("com.android.systemui.gesture.line.overlay", false, currentUser);
            }
        }
        if (currentUser != 0 && z != isOverlay(0, "com.android.systemui.gesture.line.overlay")) {
            if (z) {
                try {
                    getMOverlayManager().setEnabledExclusiveInCategory("com.android.systemui.gesture.line.overlay", 0);
                } catch (Exception e2) {
                    Log.w("StatusBar", "Can't apply overlay for user owner", e2);
                }
            } else {
                getMOverlayManager().setEnabled("com.android.systemui.gesture.line.overlay", false, 0);
            }
        }
    }

    private final boolean isOverlay(int i, String str) {
        OverlayInfo overlayInfo;
        try {
            overlayInfo = getMOverlayManager().getOverlayInfo(str, i);
        } catch (Exception e) {
            Log.w("StatusBar", "Can't get overlay info for user " + i + ' ' + str, e);
            overlayInfo = null;
        }
        return overlayInfo != null && overlayInfo.isEnabled();
    }

    public final void onUserSwitched() {
        Context context = mContext;
        if (context != null) {
            mIsFsgMode = MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_fsg_nav_bar");
            updateOverlayManager();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mContext");
        throw null;
    }
}
