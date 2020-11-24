package com.android.systemui.recents;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0005R$array;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.TaskStackChangeListener;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@TargetApi(28)
public class RecentsOnboarding {
    private final View mArrowView;
    /* access modifiers changed from: private */
    public Set<String> mBlacklistedPackages;
    private final Context mContext;
    private final ImageView mDismissView;
    /* access modifiers changed from: private */
    public boolean mHasDismissedQuickScrubTip;
    /* access modifiers changed from: private */
    public boolean mHasDismissedSwipeUpTip;
    /* access modifiers changed from: private */
    public final View mLayout;
    /* access modifiers changed from: private */
    public boolean mLayoutAttachedToWindow;
    private int mNavBarHeight;
    private int mNavBarMode = 0;
    /* access modifiers changed from: private */
    public int mNumAppsLaunchedSinceSwipeUpTipDismiss;
    private final View.OnAttachStateChangeListener mOnAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        private final BroadcastDispatcher mBroadcastDispatcher = ((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));

        public void onViewAttachedToWindow(View view) {
            if (view == RecentsOnboarding.this.mLayout) {
                this.mBroadcastDispatcher.registerReceiver(RecentsOnboarding.this.mReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
                boolean unused = RecentsOnboarding.this.mLayoutAttachedToWindow = true;
                if (view.getTag().equals(Integer.valueOf(C0018R$string.recents_swipe_up_onboarding))) {
                    boolean unused2 = RecentsOnboarding.this.mHasDismissedSwipeUpTip = false;
                } else {
                    boolean unused3 = RecentsOnboarding.this.mHasDismissedQuickScrubTip = false;
                }
            }
        }

        public void onViewDetachedFromWindow(View view) {
            if (view == RecentsOnboarding.this.mLayout) {
                boolean unused = RecentsOnboarding.this.mLayoutAttachedToWindow = false;
                if (view.getTag().equals(Integer.valueOf(C0018R$string.recents_quick_scrub_onboarding))) {
                    boolean unused2 = RecentsOnboarding.this.mHasDismissedQuickScrubTip = true;
                    if (RecentsOnboarding.this.hasDismissedQuickScrubOnboardingOnce()) {
                        RecentsOnboarding.this.setHasSeenQuickScrubOnboarding(true);
                    } else {
                        RecentsOnboarding.this.setHasDismissedQuickScrubOnboardingOnce(true);
                    }
                    int unused3 = RecentsOnboarding.this.mOverviewOpenedCountSinceQuickScrubTipDismiss = 0;
                }
                this.mBroadcastDispatcher.unregisterReceiver(RecentsOnboarding.this.mReceiver);
            }
        }
    };
    private final int mOnboardingToastArrowRadius;
    private final int mOnboardingToastColor;
    /* access modifiers changed from: private */
    public int mOverviewOpenedCountSinceQuickScrubTipDismiss;
    private OverviewProxyService.OverviewProxyListener mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
        public void onOverviewShown(boolean z) {
            if (!RecentsOnboarding.this.hasSeenSwipeUpOnboarding() && !z) {
                RecentsOnboarding.this.setHasSeenSwipeUpOnboarding(true);
            }
            if (z) {
                RecentsOnboarding.this.incrementOpenedOverviewFromHomeCount();
            }
            RecentsOnboarding.this.incrementOpenedOverviewCount();
            if (RecentsOnboarding.this.getOpenedOverviewCount() >= 10 && RecentsOnboarding.this.mHasDismissedQuickScrubTip) {
                RecentsOnboarding.access$1008(RecentsOnboarding.this);
            }
        }
    };
    private boolean mOverviewProxyListenerRegistered;
    private final OverviewProxyService mOverviewProxyService;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                RecentsOnboarding.this.hide(false);
            }
        }
    };
    private final TaskStackChangeListener mTaskListener = new TaskStackChangeListener() {
        private String mLastPackageName;

        public void onTaskCreated(int i, ComponentName componentName) {
            onAppLaunch();
        }

        public void onTaskMovedToFront(int i) {
            onAppLaunch();
        }

        private void onAppLaunch() {
            boolean z;
            boolean z2;
            ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
            if (runningTask != null) {
                if (RecentsOnboarding.this.mBlacklistedPackages.contains(runningTask.baseActivity.getPackageName())) {
                    RecentsOnboarding.this.hide(true);
                } else if (!runningTask.baseActivity.getPackageName().equals(this.mLastPackageName)) {
                    this.mLastPackageName = runningTask.baseActivity.getPackageName();
                    if (runningTask.configuration.windowConfiguration.getActivityType() == 1) {
                        boolean access$100 = RecentsOnboarding.this.hasSeenSwipeUpOnboarding();
                        boolean access$200 = RecentsOnboarding.this.hasSeenQuickScrubOnboarding();
                        if (access$100 && access$200) {
                            RecentsOnboarding.this.onDisconnectedFromLauncher();
                        } else if (!access$100) {
                            if (RecentsOnboarding.this.getOpenedOverviewFromHomeCount() >= 3) {
                                if (RecentsOnboarding.this.mHasDismissedSwipeUpTip) {
                                    int access$500 = RecentsOnboarding.this.getDismissedSwipeUpOnboardingCount();
                                    if (access$500 <= 2) {
                                        int i = access$500 <= 1 ? 5 : 40;
                                        RecentsOnboarding.access$608(RecentsOnboarding.this);
                                        if (RecentsOnboarding.this.mNumAppsLaunchedSinceSwipeUpTipDismiss >= i) {
                                            int unused = RecentsOnboarding.this.mNumAppsLaunchedSinceSwipeUpTipDismiss = 0;
                                            z2 = RecentsOnboarding.this.show(C0018R$string.recents_swipe_up_onboarding);
                                        } else {
                                            z2 = false;
                                        }
                                    } else {
                                        return;
                                    }
                                } else {
                                    z2 = RecentsOnboarding.this.show(C0018R$string.recents_swipe_up_onboarding);
                                }
                                if (z2) {
                                    RecentsOnboarding.this.notifyOnTip(0, 0);
                                }
                            }
                        } else if (RecentsOnboarding.this.getOpenedOverviewCount() >= 10) {
                            if (!RecentsOnboarding.this.mHasDismissedQuickScrubTip) {
                                z = RecentsOnboarding.this.show(C0018R$string.recents_quick_scrub_onboarding);
                            } else if (RecentsOnboarding.this.mOverviewOpenedCountSinceQuickScrubTipDismiss >= 10) {
                                int unused2 = RecentsOnboarding.this.mOverviewOpenedCountSinceQuickScrubTipDismiss = 0;
                                z = RecentsOnboarding.this.show(C0018R$string.recents_quick_scrub_onboarding);
                            } else {
                                z = false;
                            }
                            if (z) {
                                RecentsOnboarding.this.notifyOnTip(0, 1);
                            }
                        }
                    } else {
                        RecentsOnboarding.this.hide(false);
                    }
                }
            }
        }
    };
    private boolean mTaskListenerRegistered;
    private final TextView mTextView;
    private final WindowManager mWindowManager;

    static /* synthetic */ int access$1008(RecentsOnboarding recentsOnboarding) {
        int i = recentsOnboarding.mOverviewOpenedCountSinceQuickScrubTipDismiss;
        recentsOnboarding.mOverviewOpenedCountSinceQuickScrubTipDismiss = i + 1;
        return i;
    }

    static /* synthetic */ int access$608(RecentsOnboarding recentsOnboarding) {
        int i = recentsOnboarding.mNumAppsLaunchedSinceSwipeUpTipDismiss;
        recentsOnboarding.mNumAppsLaunchedSinceSwipeUpTipDismiss = i + 1;
        return i;
    }

    public RecentsOnboarding(Context context, OverviewProxyService overviewProxyService) {
        this.mContext = context;
        this.mOverviewProxyService = overviewProxyService;
        Resources resources = context.getResources();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        HashSet hashSet = new HashSet();
        this.mBlacklistedPackages = hashSet;
        Collections.addAll(hashSet, resources.getStringArray(C0005R$array.recents_onboarding_blacklisted_packages));
        View inflate = LayoutInflater.from(this.mContext).inflate(C0014R$layout.recents_onboarding, (ViewGroup) null);
        this.mLayout = inflate;
        this.mTextView = (TextView) inflate.findViewById(C0012R$id.onboarding_text);
        this.mDismissView = (ImageView) this.mLayout.findViewById(C0012R$id.dismiss);
        this.mArrowView = this.mLayout.findViewById(C0012R$id.arrow);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(16843829, typedValue, true);
        this.mOnboardingToastColor = resources.getColor(typedValue.resourceId);
        this.mOnboardingToastArrowRadius = resources.getDimensionPixelSize(C0009R$dimen.recents_onboarding_toast_arrow_corner_radius);
        this.mLayout.addOnAttachStateChangeListener(this.mOnAttachStateChangeListener);
        this.mDismissView.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                RecentsOnboarding.this.lambda$new$0$RecentsOnboarding(view);
            }
        });
        ViewGroup.LayoutParams layoutParams = this.mArrowView.getLayoutParams();
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.create((float) layoutParams.width, (float) layoutParams.height, false));
        Paint paint = shapeDrawable.getPaint();
        paint.setColor(this.mOnboardingToastColor);
        paint.setPathEffect(new CornerPathEffect((float) this.mOnboardingToastArrowRadius));
        this.mArrowView.setBackground(shapeDrawable);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$RecentsOnboarding(View view) {
        hide(true);
        if (view.getTag().equals(Integer.valueOf(C0018R$string.recents_swipe_up_onboarding))) {
            this.mHasDismissedSwipeUpTip = true;
            this.mNumAppsLaunchedSinceSwipeUpTipDismiss = 0;
            setDismissedSwipeUpOnboardingCount(getDismissedSwipeUpOnboardingCount() + 1);
            if (getDismissedSwipeUpOnboardingCount() > 2) {
                setHasSeenSwipeUpOnboarding(true);
            }
            notifyOnTip(1, 0);
            return;
        }
        notifyOnTip(1, 1);
    }

    /* access modifiers changed from: private */
    public void notifyOnTip(int i, int i2) {
        try {
            IOverviewProxy proxy = this.mOverviewProxyService.getProxy();
            if (proxy != null) {
                proxy.onTip(i, i2);
            }
        } catch (RemoteException unused) {
        }
    }

    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public void onConnectedToLauncher() {
        if (!QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            if (!hasSeenSwipeUpOnboarding() || !hasSeenQuickScrubOnboarding()) {
                if (!this.mOverviewProxyListenerRegistered) {
                    this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
                    this.mOverviewProxyListenerRegistered = true;
                }
                if (!this.mTaskListenerRegistered) {
                    ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskListener);
                    this.mTaskListenerRegistered = true;
                }
            }
        }
    }

    public void onDisconnectedFromLauncher() {
        if (this.mOverviewProxyListenerRegistered) {
            this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
            this.mOverviewProxyListenerRegistered = false;
        }
        if (this.mTaskListenerRegistered) {
            ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskListener);
            this.mTaskListenerRegistered = false;
        }
        this.mHasDismissedSwipeUpTip = false;
        this.mHasDismissedQuickScrubTip = false;
        this.mNumAppsLaunchedSinceSwipeUpTipDismiss = 0;
        this.mOverviewOpenedCountSinceQuickScrubTipDismiss = 0;
        hide(true);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (configuration.orientation != 1) {
            hide(false);
        }
    }

    public boolean show(int i) {
        int i2;
        int i3 = 0;
        if (!shouldShow()) {
            return false;
        }
        this.mDismissView.setTag(Integer.valueOf(i));
        this.mLayout.setTag(Integer.valueOf(i));
        this.mTextView.setText(i);
        int i4 = this.mContext.getResources().getConfiguration().orientation;
        if (this.mLayoutAttachedToWindow || i4 != 1) {
            return false;
        }
        this.mLayout.setSystemUiVisibility(256);
        if (i == C0018R$string.recents_swipe_up_onboarding) {
            i2 = 81;
        } else {
            i2 = (this.mContext.getResources().getConfiguration().getLayoutDirection() == 0 ? 3 : 5) | 80;
            i3 = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.recents_quick_scrub_onboarding_margin_start);
        }
        this.mWindowManager.addView(this.mLayout, getWindowLayoutParams(i2, i3));
        this.mLayout.setAlpha(0.0f);
        this.mLayout.animate().alpha(1.0f).withLayer().setStartDelay(500).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
        return true;
    }

    private boolean shouldShow() {
        return SystemProperties.getBoolean("persist.quickstep.onboarding.enabled", !((UserManager) this.mContext.getSystemService(UserManager.class)).isDemoUser() && !ActivityManager.isRunningInTestHarness());
    }

    public void hide(boolean z) {
        if (!this.mLayoutAttachedToWindow) {
            return;
        }
        if (z) {
            this.mLayout.animate().alpha(0.0f).withLayer().setStartDelay(0).setDuration(100).setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
                public final void run() {
                    RecentsOnboarding.this.lambda$hide$1$RecentsOnboarding();
                }
            }).start();
            return;
        }
        this.mLayout.animate().cancel();
        this.mWindowManager.removeViewImmediate(this.mLayout);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hide$1 */
    public /* synthetic */ void lambda$hide$1$RecentsOnboarding() {
        this.mWindowManager.removeViewImmediate(this.mLayout);
    }

    public void setNavBarHeight(int i) {
        this.mNavBarHeight = i;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("RecentsOnboarding {");
        printWriter.println("      mTaskListenerRegistered: " + this.mTaskListenerRegistered);
        printWriter.println("      mOverviewProxyListenerRegistered: " + this.mOverviewProxyListenerRegistered);
        printWriter.println("      mLayoutAttachedToWindow: " + this.mLayoutAttachedToWindow);
        printWriter.println("      mHasDismissedSwipeUpTip: " + this.mHasDismissedSwipeUpTip);
        printWriter.println("      mHasDismissedQuickScrubTip: " + this.mHasDismissedQuickScrubTip);
        printWriter.println("      mNumAppsLaunchedSinceSwipeUpTipDismiss: " + this.mNumAppsLaunchedSinceSwipeUpTipDismiss);
        printWriter.println("      hasSeenSwipeUpOnboarding: " + hasSeenSwipeUpOnboarding());
        printWriter.println("      hasSeenQuickScrubOnboarding: " + hasSeenQuickScrubOnboarding());
        printWriter.println("      getDismissedSwipeUpOnboardingCount: " + getDismissedSwipeUpOnboardingCount());
        printWriter.println("      hasDismissedQuickScrubOnboardingOnce: " + hasDismissedQuickScrubOnboardingOnce());
        printWriter.println("      getOpenedOverviewCount: " + getOpenedOverviewCount());
        printWriter.println("      getOpenedOverviewFromHomeCount: " + getOpenedOverviewFromHomeCount());
        printWriter.println("    }");
    }

    private WindowManager.LayoutParams getWindowLayoutParams(int i, int i2) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, i2, (-this.mNavBarHeight) / 2, 2038, 520, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("RecentsOnboarding");
        layoutParams.gravity = i;
        return layoutParams;
    }

    /* access modifiers changed from: private */
    public boolean hasSeenSwipeUpOnboarding() {
        return Prefs.getBoolean(this.mContext, "HasSeenRecentsSwipeUpOnboarding", false);
    }

    /* access modifiers changed from: private */
    public void setHasSeenSwipeUpOnboarding(boolean z) {
        Prefs.putBoolean(this.mContext, "HasSeenRecentsSwipeUpOnboarding", z);
        if (z && hasSeenQuickScrubOnboarding()) {
            onDisconnectedFromLauncher();
        }
    }

    /* access modifiers changed from: private */
    public boolean hasSeenQuickScrubOnboarding() {
        return Prefs.getBoolean(this.mContext, "HasSeenRecentsQuickScrubOnboarding", false);
    }

    /* access modifiers changed from: private */
    public void setHasSeenQuickScrubOnboarding(boolean z) {
        Prefs.putBoolean(this.mContext, "HasSeenRecentsQuickScrubOnboarding", z);
        if (z && hasSeenSwipeUpOnboarding()) {
            onDisconnectedFromLauncher();
        }
    }

    /* access modifiers changed from: private */
    public int getDismissedSwipeUpOnboardingCount() {
        return Prefs.getInt(this.mContext, "DismissedRecentsSwipeUpOnboardingCount", 0);
    }

    private void setDismissedSwipeUpOnboardingCount(int i) {
        Prefs.putInt(this.mContext, "DismissedRecentsSwipeUpOnboardingCount", i);
    }

    /* access modifiers changed from: private */
    public boolean hasDismissedQuickScrubOnboardingOnce() {
        return Prefs.getBoolean(this.mContext, "HasDismissedRecentsQuickScrubOnboardingOnce", false);
    }

    /* access modifiers changed from: private */
    public void setHasDismissedQuickScrubOnboardingOnce(boolean z) {
        Prefs.putBoolean(this.mContext, "HasDismissedRecentsQuickScrubOnboardingOnce", z);
    }

    /* access modifiers changed from: private */
    public int getOpenedOverviewFromHomeCount() {
        return Prefs.getInt(this.mContext, "OverviewOpenedFromHomeCount", 0);
    }

    /* access modifiers changed from: private */
    public void incrementOpenedOverviewFromHomeCount() {
        int openedOverviewFromHomeCount = getOpenedOverviewFromHomeCount();
        if (openedOverviewFromHomeCount < 3) {
            setOpenedOverviewFromHomeCount(openedOverviewFromHomeCount + 1);
        }
    }

    private void setOpenedOverviewFromHomeCount(int i) {
        Prefs.putInt(this.mContext, "OverviewOpenedFromHomeCount", i);
    }

    /* access modifiers changed from: private */
    public int getOpenedOverviewCount() {
        return Prefs.getInt(this.mContext, "OverviewOpenedCount", 0);
    }

    /* access modifiers changed from: private */
    public void incrementOpenedOverviewCount() {
        int openedOverviewCount = getOpenedOverviewCount();
        if (openedOverviewCount < 10) {
            setOpenedOverviewCount(openedOverviewCount + 1);
        }
    }

    private void setOpenedOverviewCount(int i) {
        Prefs.putInt(this.mContext, "OverviewOpenedCount", i);
    }
}
