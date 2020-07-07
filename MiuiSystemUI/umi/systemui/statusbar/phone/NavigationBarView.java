package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AccessibilityManagerCompat;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.ViewRootImplCompat;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodManagerCompat;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.settingslib.accessibility.SettingsAccessibilityMenuHelper;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.events.AspectClickEvent;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeadZone;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.statusbar.policy.OpaLayout;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class NavigationBarView extends LinearLayout {
    private static int sFilterColor = 0;
    private static HashMap<Integer, Integer> sKeyIdMap;
    private final View.OnClickListener mAccessibilityClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            SettingsAccessibilityMenuHelper.notifySettingsA11yMenuMonitor(NavigationBarView.this.mContext, NavigationBarView.this.mAccessibilityManager);
            AccessibilityManagerCompat.notifyAccessibilityButtonClicked(NavigationBarView.this.mAccessibilityManager, ContextCompat.getDisplayId(NavigationBarView.this.getContext()));
        }
    };
    private final AccessibilityManagerCompat.AccessibilityServicesStateChangeListener mAccessibilityListener = new AccessibilityManagerCompat.AccessibilityServicesStateChangeListener() {
        public void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
            NavigationBarView.this.updateAccessibilityServicesState();
        }
    };
    private final View.OnLongClickListener mAccessibilityLongClickListener = new View.OnLongClickListener() {
        public boolean onLongClick(View view) {
            Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
            intent.addFlags(268468224);
            view.getContext().startActivityAsUser(intent, UserHandle.CURRENT);
            return true;
        }
    };
    /* access modifiers changed from: private */
    public AccessibilityManager mAccessibilityManager = ((AccessibilityManager) getContext().getSystemService(AccessibilityManager.class));
    private final View.OnClickListener mAspectClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            RecentsEventBus.getDefault().send(new AspectClickEvent());
        }
    };
    private Drawable mBackAltIcon;
    private Drawable mBackAltLandIcon;
    private Drawable mBackIcon;
    private Drawable mBackLandIcon;
    private StatusBar mBar;
    private final NavigationBarTransitions mBarTransitions;
    private Handler mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("miui.intent.TAKE_SCREENSHOT".equals(intent.getAction())) {
                boolean booleanExtra = intent.getBooleanExtra("IsFinished", true);
                Log.d("PhoneStatusBar/NavigationBarView", "ACTION_TAKE_SCREENSHOT:" + booleanExtra);
                if (!booleanExtra) {
                    NavigationBarView.this.getRecentsButton().setEnabled(false);
                    NavigationBarView.this.getRecentsButton().setPressed(false);
                    return;
                }
                NavigationBarView.this.getRecentsButton().setEnabled(true);
            }
        }
    };
    private Configuration mConfiguration;
    private ConfigurationController.ConfigurationListener mConfigurationListener;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver = getContext().getContentResolver();
    /* access modifiers changed from: private */
    public int mCurrentUserId = ActivityManager.getCurrentUser();
    View mCurrentView = null;
    private DrawableSuit mDarkSuit;
    private DeadZone mDeadZone;
    int mDisabledFlags = 0;
    final Display mDisplay;
    private DisplayManager.DisplayListener mDisplayListener;
    private DisplayManager mDisplayManager;
    private boolean mForceHide;
    /* access modifiers changed from: private */
    public H mHandler = new H();
    private Drawable mHomeIcon;
    private final View.OnClickListener mImeSwitcherClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            InputMethodManagerCompat.showInputMethodPicker((InputMethodManager) NavigationBarView.this.mContext.getSystemService("input_method"), true, ContextCompat.getDisplayId(NavigationBarView.this.getContext()));
        }
    };
    private boolean mIsLayoutRtl;
    boolean mIsScreenPinningActive;
    private ArrayList<Integer> mKeyOrder = new ArrayList<>();
    private boolean mLayoutTransitionsEnabled = true;
    private DrawableSuit mLightSuit;
    boolean mLongClickableAccessibilityButton;
    private MagnificationContentObserver mMagnificationObserver;
    /* access modifiers changed from: private */
    public ElderlyNavigationBarSettingObserver mNavigationBarSettingObserver;
    private int mNavigationBarWindowState = 0;
    private NavigationHandle mNavigationHandle;
    int mNavigationIconHints = 0;
    private OnVerticalChangedListener mOnVerticalChangedListener;
    private boolean mOpaEnable;
    private OverviewProxyService mOverviewProxyService;
    private Drawable mRecentIcon;
    private Drawable mRecentLandIcon;
    View[] mRotatedViews = new View[4];
    /* access modifiers changed from: private */
    public final ContentObserver mScreenKeyOrderObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            NavigationBarView.this.adjustKeyOrder();
        }
    };
    boolean mShowAccessibilityButton;
    boolean mShowAspect;
    boolean mShowMenu;
    private NavigationBarViewTaskSwitchHelper mTaskSwitchHelper;
    private NavBarTintController mTintController;
    private final NavTransitionListener mTransitionListener = new NavTransitionListener();
    private Runnable mUpdateAccessibilityServicesState = new Runnable() {
        public void run() {
            NavigationBarView.this.mHandler.obtainMessage(8687, AccessibilityManagerCompat.getRequestingServices(NavigationBarView.this.mAccessibilityManager, NavigationBarView.this.mContentResolver), 0).sendToTarget();
        }
    };
    boolean mUseLastScreenPinningActive;
    private final KeyguardUpdateMonitorCallback mUserSwitchCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitching(int i) {
            NavigationBarView.this.mScreenKeyOrderObserver.onChange(false);
            NavigationBarView.this.updateAccessibilityServicesState();
            int unused = NavigationBarView.this.mCurrentUserId = i;
            if (NavigationBarView.this.mNavigationBarSettingObserver != null) {
                NavigationBarView.this.mNavigationBarSettingObserver.setCurrentUserId(i);
            }
        }
    };
    boolean mVertical;
    private boolean mWakeAndUnlocking;

    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean z);
    }

    private static String visibilityToString(int i) {
        return i != 4 ? i != 8 ? "VISIBLE" : "GONE" : "INVISIBLE";
    }

    public boolean isForceImmersive() {
        return false;
    }

    public void startCompositionSampling() {
        this.mTintController.start();
    }

    public void stopCompositionSampling() {
        this.mTintController.stop();
    }

    private static class DrawableSuit {
        Drawable mBack;
        Drawable mBackAlt;
        int mBgColor;
        Drawable mBgLand;
        Drawable mBgLandCTS;
        Drawable mBgPort;
        Drawable mBgPortCTS;
        Drawable mHelp;
        Drawable mHome;
        Drawable mRecent;

        private DrawableSuit() {
        }

        public static class Builder {
            private int mBack = R.drawable.ic_sysbar_back;
            private int mBackAlt = R.drawable.ic_sysbar_back_ime;
            private int mBgColorRes = R.color.nav_bar_background_color;
            private int mBgLand = R.drawable.ic_sysbar_bg_land;
            private int mBgLandCTS = 0;
            private int mBgPort = R.drawable.ic_sysbar_bg;
            private int mBgPortCTS = 0;
            private Context mContext;
            private int mHelp = R.drawable.ic_sysbar_help;
            private int mHome = R.drawable.ic_sysbar_home;
            private int mRecent = R.drawable.ic_sysbar_recent;

            Builder(Context context) {
                this.mContext = context;
            }

            /* access modifiers changed from: package-private */
            public Builder setHelp(int i) {
                this.mHelp = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBack(int i) {
                this.mBack = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBackAlt(int i) {
                this.mBackAlt = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setHome(int i) {
                this.mHome = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setRecent(int i) {
                this.mRecent = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBgPort(int i) {
                this.mBgPort = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBgLand(int i) {
                this.mBgLand = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBgPortCTS(int i) {
                this.mBgPortCTS = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBgLandCTS(int i) {
                this.mBgLandCTS = i;
                return this;
            }

            /* access modifiers changed from: package-private */
            public Builder setBgColorRes(int i) {
                this.mBgColorRes = i;
                return this;
            }

            public DrawableSuit build() {
                DrawableSuit drawableSuit = new DrawableSuit();
                Resources resources = this.mContext.getResources();
                drawableSuit.mHelp = resources.getDrawable(this.mHelp);
                drawableSuit.mBack = resources.getDrawable(this.mBack);
                drawableSuit.mBackAlt = resources.getDrawable(this.mBackAlt);
                drawableSuit.mHome = resources.getDrawable(this.mHome);
                drawableSuit.mRecent = resources.getDrawable(this.mRecent);
                drawableSuit.mBgPort = resources.getDrawable(this.mBgPort);
                drawableSuit.mBgLand = resources.getDrawable(this.mBgLand);
                int i = this.mBgPortCTS;
                if (i != 0) {
                    drawableSuit.mBgPortCTS = resources.getDrawable(i);
                }
                int i2 = this.mBgLandCTS;
                if (i2 != 0) {
                    drawableSuit.mBgLandCTS = resources.getDrawable(i2);
                }
                drawableSuit.mBgColor = resources.getColor(this.mBgColorRes);
                return drawableSuit;
            }
        }
    }

    private class NavTransitionListener implements LayoutTransition.TransitionListener {
        private boolean mBackTransitioning;
        private long mDuration;
        private boolean mHomeAppearing;
        private TimeInterpolator mInterpolator;
        private long mStartDelay;

        private NavTransitionListener() {
        }

        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == R.id.back) {
                this.mBackTransitioning = true;
            } else if (view.getId() == R.id.home && i == 2) {
                this.mHomeAppearing = true;
                this.mStartDelay = layoutTransition.getStartDelay(i);
                this.mDuration = layoutTransition.getDuration(i);
                this.mInterpolator = layoutTransition.getInterpolator(i);
            }
        }

        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == R.id.back) {
                this.mBackTransitioning = false;
            } else if (view.getId() == R.id.home && i == 2) {
                this.mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            if (!this.mBackTransitioning && NavigationBarView.this.getBackButton().getVisibility() == 0 && this.mHomeAppearing && NavigationBarView.this.getHomeButton().getAlpha() == 0.0f) {
                Log.d("PhoneStatusBar/NavigationBarView", "onBackAltCleared");
                NavigationBarView.this.getBackButton().setAlpha(0.0f);
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(NavigationBarView.this.getBackButton(), "alpha", new float[]{0.0f, 1.0f});
                ofFloat.setStartDelay(this.mStartDelay);
                ofFloat.setDuration(this.mDuration);
                ofFloat.setInterpolator(this.mInterpolator);
                ofFloat.start();
            }
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = false;
            if (i == 8686) {
                String str = "" + message.obj;
                int width = NavigationBarView.this.getWidth();
                int height = NavigationBarView.this.getHeight();
                int width2 = NavigationBarView.this.mCurrentView.getWidth();
                int height2 = NavigationBarView.this.mCurrentView.getHeight();
                if (height != height2 || width != width2) {
                    Log.w("PhoneStatusBar/NavigationBarView", String.format("*** Invalid layout in navigation bar (%s this=%dx%d cur=%dx%d)", new Object[]{str, Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(width2), Integer.valueOf(height2)}));
                    NavigationBarView.this.requestLayout();
                }
            } else if (i == 8687) {
                int i2 = message.arg1;
                boolean z2 = i2 >= 1;
                if (i2 >= 2) {
                    z = true;
                }
                NavigationBarView.this.setAccessibilityButtonState(z2, z);
            }
        }
    }

    private class MagnificationContentObserver extends ContentObserver {
        public MagnificationContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z) {
            NavigationBarView.this.updateAccessibilityServicesState();
        }
    }

    public NavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        if (SystemProperties.get("ro.miui.build.region", "").equalsIgnoreCase("eea")) {
            this.mOpaEnable = true;
        }
        this.mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Resources resources = getContext().getResources();
        resources.getDimensionPixelSize(R.dimen.navigation_bar_size);
        this.mVertical = false;
        this.mShowMenu = false;
        this.mShowAccessibilityButton = false;
        this.mLongClickableAccessibilityButton = false;
        this.mTaskSwitchHelper = new NavigationBarViewTaskSwitchHelper(context);
        getIcons(resources);
        this.mBarTransitions = new NavigationBarTransitions(this);
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            public void onDisplayAdded(int i) {
            }

            public void onDisplayChanged(int i) {
            }

            public void onDisplayRemoved(int i) {
            }
        };
        this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
        Configuration configuration = new Configuration();
        this.mConfiguration = configuration;
        configuration.updateFrom(getResources().getConfiguration());
        this.mTintController = new NavBarTintController(this, getLightTransitionsController());
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        updateSystemUiStateFlags();
        this.mNavigationBarSettingObserver = new ElderlyNavigationBarSettingObserver(context, this.mHandler, this);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        StatusBar statusBar;
        super.onAttachedToWindow();
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, this.mHandler);
        ViewRootImpl viewRootImpl = getViewRootImpl();
        if (viewRootImpl != null) {
            ViewRootImplCompat.setDrawDuringWindowsAnimating(viewRootImpl);
        }
        if (!MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar") || (statusBar = this.mBar) == null || statusBar.isHideGestureLine()) {
            stopCompositionSampling();
        } else {
            startCompositionSampling();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.TAKE_SCREENSHOT");
        this.mNavigationBarSettingObserver.register();
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("screen_key_order"), false, this.mScreenKeyOrderObserver, -1);
        this.mScreenKeyOrderObserver.onChange(false);
        if (supportAccessibiliyButton()) {
            this.mMagnificationObserver = new MagnificationContentObserver(this.mHandler);
            this.mContentResolver.registerContentObserver(Settings.Secure.getUriFor("accessibility_display_magnification_navbar_enabled"), false, this.mMagnificationObserver, -1);
            AccessibilityManagerCompat.addAccessibilityServicesStateChangeListener(this.mAccessibilityManager, this.mAccessibilityListener, (Handler) null);
        }
        postInvalidate();
        StatusBar statusBar2 = this.mBar;
        if (statusBar2 != null) {
            statusBar2.updateStatusBarPading();
        }
        this.mConfigurationListener = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
            }

            public void onDensityOrFontScaleChanged() {
                NavigationBarView navigationBarView = NavigationBarView.this;
                navigationBarView.getIcons(navigationBarView.getResources());
                NavigationBarView navigationBarView2 = NavigationBarView.this;
                navigationBarView2.setNavigationIconHints(navigationBarView2.mNavigationIconHints, true);
            }
        };
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUserSwitchCallback);
        processConfigurationChanged(getResources().getConfiguration());
        this.mNavigationBarSettingObserver.onChange(false);
        updateNotTouchable();
    }

    private boolean isElderlyMode() {
        return !MiuiSettings.Global.getBoolean(this.mContentResolver, "force_fsg_nav_bar") && MiuiSettings.System.getBooleanForUser(this.mContentResolver, "elderly_mode", false, this.mCurrentUserId);
    }

    private boolean supportAccessibiliyButton() {
        return Build.VERSION.SDK_INT >= 26;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        if (supportAccessibiliyButton()) {
            this.mContentResolver.unregisterContentObserver(this.mMagnificationObserver);
            AccessibilityManagerCompat.removeAccessibilityServicesStateChangeListener(this.mAccessibilityManager, this.mAccessibilityListener);
            this.mBgHandler.removeCallbacks(this.mUpdateAccessibilityServicesState);
        }
        this.mNavigationBarSettingObserver.unregister();
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mScreenKeyOrderObserver);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this.mConfigurationListener);
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mUserSwitchCallback);
    }

    static {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        sKeyIdMap = hashMap;
        hashMap.put(0, Integer.valueOf(R.id.menu));
        sKeyIdMap.put(1, Integer.valueOf(R.id.home));
        sKeyIdMap.put(2, Integer.valueOf(R.id.recent_apps));
        sKeyIdMap.put(3, Integer.valueOf(R.id.back));
    }

    /* access modifiers changed from: private */
    public void adjustKeyOrder() {
        this.mKeyOrder.clear();
        ArrayList<Integer> screenKeyOrder = getScreenKeyOrder(this.mContext);
        for (int i = 0; i < screenKeyOrder.size(); i++) {
            this.mKeyOrder.add(sKeyIdMap.get(screenKeyOrder.get(i)));
        }
        int i2 = 0;
        while (true) {
            View[] viewArr = this.mRotatedViews;
            if (i2 < viewArr.length) {
                ViewGroup viewGroup = (ViewGroup) viewArr[i2].findViewById(R.id.nav_buttons);
                boolean z = true;
                if (!(i2 == 1 || i2 == 3)) {
                    z = false;
                }
                adjustKeyOrder(viewGroup, z);
                i2++;
            } else {
                return;
            }
        }
    }

    private void adjustKeyOrder(ViewGroup viewGroup, boolean z) {
        int i;
        Log.d("PhoneStatusBar/NavigationBarView", "adjustKeyOrder");
        LinkedList linkedList = new LinkedList();
        HashMap hashMap = new HashMap();
        int childCount = viewGroup.getChildCount();
        while (true) {
            childCount--;
            if (childCount < 0) {
                break;
            }
            View childAt = viewGroup.getChildAt(childCount);
            if (this.mKeyOrder.contains(Integer.valueOf(childAt.getId()))) {
                linkedList.add(0, Integer.valueOf(childCount));
                viewGroup.removeView(childAt);
                hashMap.put(Integer.valueOf(childAt.getId()), childAt);
            }
        }
        int size = this.mKeyOrder.size();
        for (i = 0; i < size; i++) {
            View view = (View) hashMap.get(this.mKeyOrder.get(z ? (size - 1) - i : i));
            if (view != null) {
                viewGroup.addView(view, ((Integer) linkedList.removeFirst()).intValue());
            }
        }
    }

    public static ArrayList<Integer> getScreenKeyOrder(Context context) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        String stringForUser = Settings.System.getStringForUser(context.getContentResolver(), "screen_key_order", -2);
        if (!TextUtils.isEmpty(stringForUser)) {
            String[] split = stringForUser.split(" ");
            int i = 0;
            while (i < split.length) {
                try {
                    int intValue = Integer.valueOf(split[i]).intValue();
                    if (MiuiSettings.System.screenKeys.contains(Integer.valueOf(intValue))) {
                        arrayList.add(Integer.valueOf(intValue));
                    }
                    i++;
                } catch (Exception unused) {
                    arrayList.clear();
                }
            }
        }
        Iterator it = MiuiSettings.System.screenKeys.iterator();
        while (it.hasNext()) {
            Integer num = (Integer) it.next();
            if (!arrayList.contains(num)) {
                arrayList.add(num);
            }
        }
        return arrayList;
    }

    public BarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public LightBarTransitionsController getLightTransitionsController() {
        return this.mBarTransitions.getLightTransitionsController();
    }

    public void setBar(StatusBar statusBar) {
        this.mBar = statusBar;
        this.mTaskSwitchHelper.setBar(statusBar);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Log.d("PhoneStatusBar/NavigationBarView", "onDraw");
        this.mTintController.onDraw();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mTaskSwitchHelper.onTouchEvent(motionEvent)) {
            return true;
        }
        if (this.mDeadZone != null && motionEvent.getAction() == 4) {
            this.mDeadZone.poke(motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        return this.mTaskSwitchHelper.onInterceptTouchEvent(motionEvent);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
            this.mBar.resumeSuspendedNavBarAutohide();
        } else if (motionEvent.getAction() == 0) {
            this.mBar.suspendNavBarAutohide();
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public NavigationHandle getNavigationHandle() {
        return this.mNavigationHandle;
    }

    public View getRecentsButton() {
        return this.mCurrentView.findViewById(R.id.recent_apps);
    }

    public View getMenuButton() {
        return this.mCurrentView.findViewById(R.id.menu);
    }

    public View getBackButton() {
        return this.mCurrentView.findViewById(R.id.back);
    }

    public KeyButtonView getHomeButton() {
        return (KeyButtonView) this.mCurrentView.findViewById(R.id.home);
    }

    public View getImeSwitchButton() {
        return this.mCurrentView.findViewById(R.id.ime_switcher);
    }

    public View getAccessibilityButton() {
        return this.mCurrentView.findViewById(R.id.accessibility_button);
    }

    public View getAspectButton() {
        return this.mCurrentView.findViewById(R.id.aspect);
    }

    public boolean isOverviewEnabled() {
        return (this.mDisabledFlags & 16777216) == 0;
    }

    /* access modifiers changed from: private */
    public void getIcons(Resources resources) {
        Drawable drawable = resources.getDrawable(R.drawable.ic_sysbar_back);
        this.mBackIcon = drawable;
        drawable.setAutoMirrored(true);
        this.mBackLandIcon = this.mBackIcon;
        Drawable drawable2 = resources.getDrawable(R.drawable.ic_sysbar_back_ime);
        this.mBackAltIcon = drawable2;
        this.mBackAltLandIcon = drawable2;
        Drawable drawable3 = resources.getDrawable(R.drawable.ic_sysbar_recent);
        this.mRecentIcon = drawable3;
        this.mRecentLandIcon = drawable3;
        this.mHomeIcon = resources.getDrawable(R.drawable.ic_sysbar_home);
        DrawableSuit.Builder builder = new DrawableSuit.Builder(this.mContext);
        builder.setHelp(R.drawable.ic_sysbar_help_darkmode);
        builder.setBack(R.drawable.ic_sysbar_back_darkmode);
        builder.setBackAlt(R.drawable.ic_sysbar_back_ime_darkmode);
        builder.setHome(R.drawable.ic_sysbar_home_darkmode);
        builder.setRecent(R.drawable.ic_sysbar_recent_darkmode);
        builder.setBgPort(R.drawable.ic_sysbar_bg_darkmode);
        builder.setBgPortCTS(R.drawable.ic_sysbar_bg_darkmode_cts);
        builder.setBgLand(R.drawable.ic_sysbar_bg_land_darkmode);
        builder.setBgLandCTS(R.drawable.ic_sysbar_bg_land_darkmode_cts);
        builder.setBgColorRes(R.color.nav_bar_bakcground_color_darkmode);
        this.mDarkSuit = builder.build();
        this.mLightSuit = new DrawableSuit.Builder(this.mContext).build();
    }

    public void setLayoutDirection(int i) {
        getIcons(getContext().getResources());
        super.setLayoutDirection(i);
        if (getBackButton() != null) {
            getBackButton().invalidate();
        }
    }

    public void setNavigationIconHints(int i, boolean z) {
        Drawable drawable;
        if (z || i != this.mNavigationIconHints) {
            boolean z2 = (i & 1) != 0;
            if ((this.mNavigationIconHints & 1) != 0 && !z2) {
                this.mTransitionListener.onBackAltCleared();
            }
            this.mNavigationIconHints = i;
            ImageView imageView = (ImageView) getBackButton();
            if (z2) {
                drawable = this.mVertical ? this.mBackAltLandIcon : this.mBackAltIcon;
            } else {
                drawable = this.mVertical ? this.mBackLandIcon : this.mBackIcon;
            }
            imageView.setImageDrawable(drawable);
            ((ImageView) getRecentsButton()).setImageDrawable(this.mVertical ? this.mRecentLandIcon : this.mRecentIcon);
            if (!this.mOpaEnable) {
                getHomeButton().setImageDrawable(this.mHomeIcon);
            }
            int i2 = i & 2;
            getImeSwitchButton().setVisibility(4);
            setMenuVisibility(this.mShowMenu, true);
            setAspectVisibility(this.mShowAspect, true);
            setAccessibilityButtonState(this.mShowAccessibilityButton, this.mLongClickableAccessibilityButton);
            setDisabledFlags(this.mDisabledFlags, true);
        }
    }

    public void disableChangeBg(boolean z) {
        this.mBarTransitions.disableChangeBg(z);
    }

    public void setDisabledFlags(int i) {
        setDisabledFlags(i, false);
    }

    public void setDisabledFlags(int i, boolean z) {
        LayoutTransition layoutTransition;
        if (z || this.mDisabledFlags != i) {
            this.mDisabledFlags = i;
            int i2 = 0;
            boolean z2 = (2097152 & i) != 0;
            boolean z3 = !isOverviewEnabled();
            boolean z4 = (4194304 & i) != 0 && (this.mNavigationIconHints & 1) == 0;
            int i3 = 33554432 & i;
            boolean z5 = (i & 512) != 0;
            int mode = this.mBarTransitions.getMode();
            if ((z5 && (mode == 4 || mode == 6)) || mode == 0 || mode == 3) {
                switchSuit(this.mDarkSuit, true);
            } else {
                switchSuit(this.mLightSuit, false);
            }
            setSlippery(z2 && z3 && z4);
            updateNotTouchable();
            ViewGroup viewGroup = (ViewGroup) this.mCurrentView.findViewById(R.id.nav_buttons);
            if (!(viewGroup == null || (layoutTransition = viewGroup.getLayoutTransition()) == null || layoutTransition.getTransitionListeners().contains(this.mTransitionListener))) {
                layoutTransition.addTransitionListener(this.mTransitionListener);
            }
            if (Build.VERSION.SDK_INT < 28 ? !(!inLockTask() || !z3 || z2) : isScreenPinningActive()) {
                z3 = false;
            }
            Log.d("PhoneStatusBar/NavigationBarView", "setDisabledFlags back:" + z4 + " home:" + z2 + " recent:" + z3);
            setLayoutTransitionsEnabled(false);
            getBackButton().setVisibility(z4 ? 4 : 0);
            if (!this.mOpaEnable) {
                getHomeButton().setVisibility(z2 ? 4 : 0);
            } else {
                ((OpaLayout) this.mCurrentView.findViewById(R.id.home_layout)).setVisibility(z2 ? 4 : 0);
            }
            View recentsButton = getRecentsButton();
            if (z3) {
                i2 = 4;
            }
            recentsButton.setVisibility(i2);
            setAlpha((this.mForceHide || (z4 && z2 && z3)) ? 0.0f : 1.0f);
            setLayoutTransitionsEnabled(true);
        }
    }

    public void switchSuit(DrawableSuit drawableSuit, boolean z) {
        Drawable drawable;
        Drawable drawable2 = drawableSuit.mBack;
        this.mBackLandIcon = drawable2;
        this.mBackIcon = drawable2;
        drawable2.setAutoMirrored(true);
        Drawable drawable3 = drawableSuit.mBackAlt;
        this.mBackAltLandIcon = drawable3;
        this.mBackAltIcon = drawable3;
        Drawable drawable4 = drawableSuit.mRecent;
        this.mRecentLandIcon = drawable4;
        this.mRecentIcon = drawable4;
        this.mHomeIcon = drawableSuit.mHome;
        updateIcon((ImageView) getAccessibilityButton(), drawableSuit.mHelp, z);
        boolean z2 = (this.mNavigationIconHints & 1) != 0;
        ImageView imageView = (ImageView) getBackButton();
        if (z2) {
            drawable = this.mVertical ? this.mBackAltLandIcon : this.mBackAltIcon;
        } else {
            drawable = this.mVertical ? this.mBackLandIcon : this.mBackIcon;
        }
        updateIcon(imageView, drawable, z);
        updateIcon((ImageView) getRecentsButton(), this.mVertical ? this.mRecentLandIcon : this.mRecentIcon, z);
        if (!this.mOpaEnable) {
            updateIcon(getHomeButton(), this.mHomeIcon, z);
        } else {
            ((OpaLayout) this.mCurrentView.findViewById(R.id.home_layout)).setDarkIntensity(z ? 1.0f : 0.0f);
        }
        boolean z3 = z && Util.showCtsSpecifiedColor();
        this.mRotatedViews[0].setBackground(z3 ? drawableSuit.mBgPortCTS : drawableSuit.mBgPort);
        this.mRotatedViews[1].setBackground(z3 ? drawableSuit.mBgLandCTS : drawableSuit.mBgLand);
        this.mBarTransitions.setForceBgColor(drawableSuit.mBgColor);
    }

    private void updateIcon(ImageView imageView, Drawable drawable, boolean z) {
        if (imageView != null && drawable != null) {
            if (!z || !Util.showCtsSpecifiedColor()) {
                drawable.setColorFilter((ColorFilter) null);
            } else {
                if (sFilterColor == 0) {
                    sFilterColor = this.mContext.getResources().getColor(R.color.status_bar_icon_text_color_dark_mode_cts);
                }
                drawable.setColorFilter(sFilterColor, PorterDuff.Mode.SRC_IN);
            }
            imageView.setImageDrawable(drawable);
        }
    }

    private boolean inLockTask() {
        if (this.mUseLastScreenPinningActive) {
            return this.mIsScreenPinningActive;
        }
        try {
            boolean isInLockTaskMode = ActivityManagerNative.getDefault().isInLockTaskMode();
            this.mIsScreenPinningActive = isInLockTaskMode;
            return isInLockTaskMode;
        } catch (RemoteException unused) {
            return false;
        }
    }

    private boolean isScreenPinningActive() {
        if (this.mUseLastScreenPinningActive) {
            return this.mIsScreenPinningActive;
        }
        try {
            boolean z = ActivityManagerNative.getDefault().getLockTaskModeState() == 2;
            this.mIsScreenPinningActive = z;
            return z;
        } catch (RemoteException unused) {
            return false;
        }
    }

    public void setLayoutTransitionsEnabled(boolean z) {
        this.mLayoutTransitionsEnabled = z;
        updateLayoutTransitionsEnabled();
    }

    public void setWakeAndUnlocking(boolean z) {
        setUseFadingAnimations(z);
        this.mWakeAndUnlocking = z;
        updateLayoutTransitionsEnabled();
    }

    private void updateLayoutTransitionsEnabled() {
        boolean z = !this.mWakeAndUnlocking && this.mLayoutTransitionsEnabled;
        LayoutTransition layoutTransition = ((ViewGroup) this.mCurrentView.findViewById(R.id.nav_buttons)).getLayoutTransition();
        if (layoutTransition == null) {
            return;
        }
        if (z) {
            layoutTransition.enableTransitionType(2);
            layoutTransition.enableTransitionType(3);
            layoutTransition.enableTransitionType(0);
            layoutTransition.enableTransitionType(1);
            return;
        }
        layoutTransition.disableTransitionType(2);
        layoutTransition.disableTransitionType(3);
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
    }

    private void setUseFadingAnimations(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            boolean z2 = layoutParams.windowAnimations != 0;
            if (!z2 && z) {
                layoutParams.windowAnimations = R.style.Animation_NavigationBarFadeIn;
            } else if (z2 && !z) {
                layoutParams.windowAnimations = 0;
            } else {
                return;
            }
            WindowManager windowManager = (WindowManager) getContext().getSystemService("window");
            if (isAttachedToWindow()) {
                windowManager.updateViewLayout(this, layoutParams);
            }
        }
    }

    public void setSlippery(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
        if (layoutParams != null) {
            boolean z2 = (layoutParams.flags & 536870912) != 0;
            if (!z2 && z) {
                layoutParams.flags |= 536870912;
            } else if (z2 && !z) {
                layoutParams.flags &= -536870913;
            } else {
                return;
            }
            WindowManager windowManager = (WindowManager) getContext().getSystemService("window");
            if (isAttachedToWindow()) {
                windowManager.updateViewLayout(this, layoutParams);
            }
        }
    }

    public void updateNotTouchable() {
        setNotTouchable(isNotTouchable());
    }

    private boolean isNotTouchable() {
        boolean z = (this.mDisabledFlags & 2097152) != 0;
        boolean z2 = !isOverviewEnabled();
        boolean z3 = (this.mDisabledFlags & 4194304) != 0 && (this.mNavigationIconHints & 1) == 0;
        if (this.mNavigationHandle.getVisibility() == 0 || (z && z2 && z3)) {
            return true;
        }
        return false;
    }

    private void setNotTouchable(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
        if (layoutParams != null && isAttachedToWindow()) {
            boolean z2 = (layoutParams.flags & 16) != 0;
            if (!z2 && z) {
                layoutParams.flags |= 16;
            } else if (z2 && !z) {
                layoutParams.flags &= -17;
            } else {
                return;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout(this, layoutParams);
        }
    }

    public void setMenuVisibility(boolean z, boolean z2) {
        if (z2 || this.mShowMenu != z) {
            this.mShowMenu = z;
            if (z) {
                int i = this.mNavigationIconHints & 2;
            }
            getMenuButton().setVisibility(4);
        }
    }

    public void setAccessibilityButtonState(boolean z, boolean z2) {
        this.mShowAccessibilityButton = z;
        this.mLongClickableAccessibilityButton = z2;
        int i = 4;
        if (z) {
            setMenuVisibility(false, true);
            getImeSwitchButton().setVisibility(4);
            setAspectVisibility(this.mShowAspect, true);
        }
        View accessibilityButton = getAccessibilityButton();
        if (z) {
            i = 0;
        }
        accessibilityButton.setVisibility(i);
        getAccessibilityButton().setLongClickable(z2);
    }

    /* access modifiers changed from: private */
    public void updateAccessibilityServicesState() {
        if (supportAccessibiliyButton()) {
            this.mBgHandler.removeCallbacks(this.mUpdateAccessibilityServicesState);
            this.mBgHandler.post(this.mUpdateAccessibilityServicesState);
        }
    }

    public void setAspectVisibility(boolean z) {
        setAspectVisibility(z, false);
    }

    public void setAspectVisibility(boolean z, boolean z2) {
        if (z2 || this.mShowAspect != z) {
            this.mShowAspect = z;
            int i = 0;
            boolean z3 = z && !this.mShowAccessibilityButton;
            View aspectButton = getAspectButton();
            if (!z3) {
                i = 4;
            }
            aspectButton.setVisibility(i);
        }
    }

    public void onFinishInflate() {
        View[] viewArr = this.mRotatedViews;
        View findViewById = findViewById(R.id.rot0);
        viewArr[2] = findViewById;
        viewArr[0] = findViewById;
        this.mRotatedViews[1] = findViewById(R.id.rot90);
        View[] viewArr2 = this.mRotatedViews;
        viewArr2[3] = viewArr2[1];
        this.mCurrentView = viewArr2[0];
        this.mNavigationHandle = (NavigationHandle) findViewById(R.id.home_handle);
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        getAspectButton().setOnClickListener(this.mAspectClickListener);
        getAccessibilityButton().setOnClickListener(this.mAccessibilityClickListener);
        getAccessibilityButton().setOnLongClickListener(this.mAccessibilityLongClickListener);
        updateAccessibilityServicesState();
        updateRTLOrder();
    }

    public void reorient() {
        if (!MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar") || this.mBar.isHideGestureLine()) {
            int rotation = this.mDisplay.getRotation();
            boolean z = false;
            for (int i = 0; i < 4; i++) {
                this.mRotatedViews[i].setVisibility(8);
            }
            View view = this.mRotatedViews[rotation];
            this.mCurrentView = view;
            view.setVisibility(0);
            updateLayoutTransitionsEnabled();
            getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
            getAspectButton().setOnClickListener(this.mAspectClickListener);
            getAccessibilityButton().setOnClickListener(this.mAccessibilityClickListener);
            getAccessibilityButton().setOnLongClickListener(this.mAccessibilityLongClickListener);
            updateAccessibilityServicesState();
            this.mDeadZone = (DeadZone) this.mCurrentView.findViewById(R.id.deadzone);
            this.mBarTransitions.init();
            setMenuVisibility(this.mShowMenu, true);
            setAspectVisibility(this.mShowAspect, true);
            updateTaskSwitchHelper();
            this.mUseLastScreenPinningActive = true;
            setNavigationIconHints(this.mNavigationIconHints, true);
            this.mUseLastScreenPinningActive = false;
            if (this.mOpaEnable && !miui.os.Build.IS_TABLET) {
                OpaLayout opaLayout = (OpaLayout) this.mCurrentView.findViewById(R.id.home_layout);
                if (this.mDisplay.getRotation() == 1 || this.mDisplay.getRotation() == 3) {
                    z = true;
                }
                opaLayout.setVertical(z);
            }
        }
    }

    private void updateTaskSwitchHelper() {
        boolean z = true;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        this.mTaskSwitchHelper.setBarState(this.mVertical, z);
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        boolean z = i2 > 0 && i2 > i;
        if (z != this.mVertical) {
            this.mVertical = z;
            reorient();
            notifyVerticalChangedListener(z);
        }
        postCheckForInvalidLayout("sizeChanged");
        super.onSizeChanged(i, i2, i3, i4);
    }

    private void notifyVerticalChangedListener(boolean z) {
        OnVerticalChangedListener onVerticalChangedListener = this.mOnVerticalChangedListener;
        if (onVerticalChangedListener != null) {
            onVerticalChangedListener.onVerticalChanged(z);
        }
    }

    private void processConfigurationChanged(Configuration configuration) {
        if ((this.mConfiguration.updateFrom(configuration) & 512) == 512) {
            this.mBarTransitions.darkModeChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateRTLOrder();
        updateTaskSwitchHelper();
        updateElderlyMode();
        processConfigurationChanged(configuration);
    }

    public void updateElderlyMode() {
        if (isElderlyMode()) {
            updateImageViewScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            updateImageViewScaleType(ImageView.ScaleType.CENTER);
        }
    }

    public void updateImageViewScaleType(ImageView.ScaleType scaleType) {
        Log.d("PhoneStatusBar/NavigationBarView", "scaleType ".concat(scaleType.name()));
        ((ImageView) getImeSwitchButton()).setScaleType(scaleType);
        ((ImageView) getBackButton()).setScaleType(scaleType);
        ((ImageView) getRecentsButton()).setScaleType(scaleType);
        getHomeButton().setScaleType(scaleType);
    }

    private void updateRTLOrder() {
        boolean z = getResources().getConfiguration().getLayoutDirection() == 1;
        if (this.mIsLayoutRtl != z) {
            View view = this.mRotatedViews[1];
            swapChildrenOrderIfVertical(view.findViewById(R.id.nav_buttons));
            adjustExtraKeyGravity(view, z);
            View view2 = this.mRotatedViews[3];
            if (view != view2) {
                swapChildrenOrderIfVertical(view2.findViewById(R.id.nav_buttons));
                adjustExtraKeyGravity(view2, z);
            }
            this.mIsLayoutRtl = z;
        }
    }

    private void adjustExtraKeyGravity(View view, boolean z) {
        View findViewById = view.findViewById(R.id.menu);
        View findViewById2 = view.findViewById(R.id.ime_switcher);
        int i = 80;
        if (findViewById != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) findViewById.getLayoutParams();
            layoutParams.gravity = z ? 80 : 48;
            findViewById.setLayoutParams(layoutParams);
        }
        if (findViewById2 != null) {
            FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) findViewById2.getLayoutParams();
            if (!z) {
                i = 48;
            }
            layoutParams2.gravity = i;
            findViewById2.setLayoutParams(layoutParams2);
        }
    }

    private void swapChildrenOrderIfVertical(View view) {
        if (view instanceof LinearLayout) {
            LinearLayout linearLayout = (LinearLayout) view;
            if (linearLayout.getOrientation() == 1) {
                int childCount = linearLayout.getChildCount();
                ArrayList arrayList = new ArrayList(childCount);
                for (int i = 0; i < childCount; i++) {
                    arrayList.add(linearLayout.getChildAt(i));
                }
                linearLayout.removeAllViews();
                for (int i2 = childCount - 1; i2 >= 0; i2--) {
                    linearLayout.addView((View) arrayList.get(i2));
                }
            }
        }
    }

    private String getResourceName(int i) {
        if (i == 0) {
            return "(null)";
        }
        try {
            return getContext().getResources().getResourceName(i);
        } catch (Resources.NotFoundException unused) {
            return "(unknown)";
        }
    }

    private void postCheckForInvalidLayout(String str) {
        this.mHandler.obtainMessage(8686, 0, 0, str).sendToTarget();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NavigationBarView {");
        Rect rect = new Rect();
        Point point = new Point();
        this.mDisplay.getRealSize(point);
        printWriter.println(String.format("      this: " + StatusBar.viewInfo(this) + " " + visibilityToString(getVisibility()), new Object[0]));
        getWindowVisibleDisplayFrame(rect);
        boolean z = rect.right > point.x || rect.bottom > point.y;
        StringBuilder sb = new StringBuilder();
        sb.append("      window: ");
        sb.append(rect.toShortString());
        sb.append(" ");
        sb.append(visibilityToString(getWindowVisibility()));
        sb.append(z ? " OFFSCREEN!" : "");
        printWriter.println(sb.toString());
        printWriter.println(String.format("      mCurrentView: id=%s (%dx%d) %s", new Object[]{getResourceName(this.mCurrentView.getId()), Integer.valueOf(this.mCurrentView.getWidth()), Integer.valueOf(this.mCurrentView.getHeight()), visibilityToString(this.mCurrentView.getVisibility())}));
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mDisabledFlags);
        String str = "true";
        objArr[1] = this.mVertical ? str : "false";
        if (!this.mShowMenu) {
            str = "false";
        }
        objArr[2] = str;
        printWriter.println(String.format("      disabled=0x%08x vertical=%s menu=%s", objArr));
        dumpButton(printWriter, "back", getBackButton());
        dumpButton(printWriter, "home", getHomeButton());
        dumpButton(printWriter, "rcnt", getRecentsButton());
        dumpButton(printWriter, "menu", getMenuButton());
        dumpButton(printWriter, "aspect", getAspectButton());
        dumpButton(printWriter, "a11y", getAccessibilityButton());
        printWriter.println("    }");
    }

    private static void dumpButton(PrintWriter printWriter, String str, View view) {
        printWriter.print("      " + str + ": ");
        if (view == null) {
            printWriter.print("null");
        } else {
            printWriter.print(StatusBar.viewInfo(view) + " " + visibilityToString(view.getVisibility()) + " alpha=" + view.getAlpha());
        }
        printWriter.println();
    }

    public void setWindowState(int i, int i2) {
        if (i == 2 && this.mNavigationBarWindowState != i2) {
            this.mNavigationBarWindowState = i2;
            if (StatusBar.DEBUG_WINDOW_STATE) {
                Log.d("PhoneStatusBar/NavigationBarView", "Navigation bar " + StatusBarManager.windowStateToString(i2));
            }
            updateSystemUiStateFlags();
        }
    }

    public void updateSystemUiStateFlags() {
        OverviewProxyService overviewProxyService = this.mOverviewProxyService;
        if (overviewProxyService != null) {
            overviewProxyService.setSystemUiStateFlag(2, !isNavBarWindowVisible());
        }
    }

    public boolean isNavBarWindowVisible() {
        return this.mNavigationBarWindowState == 0;
    }

    public void onGestureLineProgress(float f) {
        NavigationHandle navigationHandle = this.mNavigationHandle;
        if (navigationHandle != null) {
            navigationHandle.onGestureLineProgress(f);
        }
    }

    public void updateBackgroundColor() {
        int i;
        if (this.mNavigationHandle.getVisibility() == 0) {
            i = 0;
        } else {
            i = getContext().getColor(R.color.system_bar_background_semi_transparent);
        }
        this.mBarTransitions.setSemiTransparentColor(i);
    }
}
