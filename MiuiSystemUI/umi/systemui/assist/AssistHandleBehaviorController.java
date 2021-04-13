package com.android.systemui.assist;

import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.app.AssistUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.inject.Provider;

public final class AssistHandleBehaviorController implements AssistHandleCallbacks, Dumpable {
    private static final AssistHandleBehavior DEFAULT_BEHAVIOR = AssistHandleBehavior.REMINDER_EXP;
    private static final long DEFAULT_SHOW_AND_GO_DURATION_MS = TimeUnit.SECONDS.toMillis(3);
    private final Lazy<AccessibilityManager> mA11yManager;
    private final Provider<AssistHandleViewController> mAssistHandleViewController;
    private final AssistUtils mAssistUtils;
    private final Map<AssistHandleBehavior, BehaviorController> mBehaviorMap;
    private final Context mContext;
    private AssistHandleBehavior mCurrentBehavior = AssistHandleBehavior.OFF;
    private final DeviceConfigHelper mDeviceConfigHelper;
    private final Handler mHandler;
    private long mHandlesLastHiddenAt;
    private boolean mHandlesShowing = false;
    private final Runnable mHideHandles = new Runnable() {
        /* class com.android.systemui.assist.$$Lambda$AssistHandleBehaviorController$XubZVLOT9vWCBnLQqZRgbOELVA */

        public final void run() {
            AssistHandleBehaviorController.m8lambda$XubZVLOT9vWCBnLQqZRgbOELVA(AssistHandleBehaviorController.this);
        }
    };
    private boolean mInGesturalMode;
    private final Runnable mShowAndGo = new Runnable() {
        /* class com.android.systemui.assist.$$Lambda$AssistHandleBehaviorController$oeveMWAQo5jd5bG1H5Ci7Dy4X74 */

        public final void run() {
            AssistHandleBehaviorController.lambda$oeveMWAQo5jd5bG1H5Ci7Dy4X74(AssistHandleBehaviorController.this);
        }
    };
    private long mShowAndGoEndsAt;

    public interface BehaviorController {
        default void dump(PrintWriter printWriter, String str) {
        }

        default void onAssistHandlesRequested() {
        }

        default void onAssistantGesturePerformed() {
        }

        void onModeActivated(Context context, AssistHandleCallbacks assistHandleCallbacks);

        default void onModeDeactivated() {
        }
    }

    AssistHandleBehaviorController(Context context, AssistUtils assistUtils, Handler handler, Provider<AssistHandleViewController> provider, DeviceConfigHelper deviceConfigHelper, Map<AssistHandleBehavior, BehaviorController> map, NavigationModeController navigationModeController, Lazy<AccessibilityManager> lazy, DumpManager dumpManager) {
        this.mContext = context;
        this.mAssistUtils = assistUtils;
        this.mHandler = handler;
        this.mAssistHandleViewController = provider;
        this.mDeviceConfigHelper = deviceConfigHelper;
        this.mBehaviorMap = map;
        this.mA11yManager = lazy;
        this.mInGesturalMode = QuickStepContract.isGesturalMode(navigationModeController.addListener(new NavigationModeController.ModeChangedListener() {
            /* class com.android.systemui.assist.$$Lambda$AssistHandleBehaviorController$UX7PPcltnlTgxyL7MxmLbVmQRcI */

            @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
            public final void onNavigationModeChanged(int i) {
                AssistHandleBehaviorController.lambda$UX7PPcltnlTgxyL7MxmLbVmQRcI(AssistHandleBehaviorController.this, i);
            }
        }));
        setBehavior(getBehaviorMode());
        DeviceConfigHelper deviceConfigHelper2 = this.mDeviceConfigHelper;
        Handler handler2 = this.mHandler;
        Objects.requireNonNull(handler2);
        deviceConfigHelper2.addOnPropertiesChangedListener(new Executor(handler2) {
            /* class com.android.systemui.assist.$$Lambda$LfzJt661qZfn2w6SYHFbD3aMy0 */
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }, new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.systemui.assist.$$Lambda$AssistHandleBehaviorController$q1QjkwrdHAyLNN1tG8mZqypuW0 */

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                AssistHandleBehaviorController.this.lambda$new$0$AssistHandleBehaviorController(properties);
            }
        });
        dumpManager.registerDumpable("AssistHandleBehavior", this);
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$AssistHandleBehaviorController(DeviceConfig.Properties properties) {
        if (properties.getKeyset().contains("assist_handles_behavior_mode")) {
            setBehavior(properties.getString("assist_handles_behavior_mode", (String) null));
        }
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void hide() {
        clearPendingCommands();
        this.mHandler.post(this.mHideHandles);
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void showAndGo() {
        clearPendingCommands();
        this.mHandler.post(this.mShowAndGo);
    }

    /* access modifiers changed from: public */
    private void showAndGoInternal() {
        maybeShowHandles(false);
        long showAndGoDuration = getShowAndGoDuration();
        this.mShowAndGoEndsAt = SystemClock.elapsedRealtime() + showAndGoDuration;
        this.mHandler.postDelayed(this.mHideHandles, showAndGoDuration);
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void showAndGoDelayed(long j, boolean z) {
        clearPendingCommands();
        if (z) {
            this.mHandler.post(this.mHideHandles);
        }
        this.mHandler.postDelayed(this.mShowAndGo, j);
    }

    @Override // com.android.systemui.assist.AssistHandleCallbacks
    public void showAndStay() {
        clearPendingCommands();
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.assist.$$Lambda$AssistHandleBehaviorController$jLNVwoO6t8_VWqmD__vvvJFYqA */

            public final void run() {
                AssistHandleBehaviorController.this.lambda$showAndStay$1$AssistHandleBehaviorController();
            }
        });
    }

    /* access modifiers changed from: public */
    /* access modifiers changed from: private */
    /* renamed from: lambda$showAndStay$1 */
    public /* synthetic */ void lambda$showAndStay$1$AssistHandleBehaviorController() {
        maybeShowHandles(true);
    }

    public long getShowAndGoRemainingTimeMs() {
        return Long.max(this.mShowAndGoEndsAt - SystemClock.elapsedRealtime(), 0);
    }

    public boolean areHandlesShowing() {
        return this.mHandlesShowing;
    }

    public void onAssistantGesturePerformed() {
        this.mBehaviorMap.get(this.mCurrentBehavior).onAssistantGesturePerformed();
    }

    public void onAssistHandlesRequested() {
        if (this.mInGesturalMode) {
            this.mBehaviorMap.get(this.mCurrentBehavior).onAssistHandlesRequested();
        }
    }

    public void setBehavior(AssistHandleBehavior assistHandleBehavior) {
        if (this.mCurrentBehavior != assistHandleBehavior) {
            if (!this.mBehaviorMap.containsKey(assistHandleBehavior)) {
                Log.e("AssistHandleBehavior", "Unsupported behavior requested: " + assistHandleBehavior.toString());
                return;
            }
            if (this.mInGesturalMode) {
                this.mBehaviorMap.get(this.mCurrentBehavior).onModeDeactivated();
                this.mBehaviorMap.get(assistHandleBehavior).onModeActivated(this.mContext, this);
            }
            this.mCurrentBehavior = assistHandleBehavior;
        }
    }

    private void setBehavior(String str) {
        try {
            setBehavior(AssistHandleBehavior.valueOf(str));
        } catch (IllegalArgumentException | NullPointerException unused) {
            Log.e("AssistHandleBehavior", "Invalid behavior: " + str);
        }
    }

    private boolean handlesUnblocked(boolean z) {
        if (!isUserSetupComplete()) {
            return false;
        }
        boolean z2 = z || SystemClock.elapsedRealtime() - this.mHandlesLastHiddenAt >= getShownFrequencyThreshold();
        ComponentName assistComponentForUser = this.mAssistUtils.getAssistComponentForUser(KeyguardUpdateMonitor.getCurrentUser());
        if (!z2 || assistComponentForUser == null) {
            return false;
        }
        return true;
    }

    private long getShownFrequencyThreshold() {
        return this.mDeviceConfigHelper.getLong("assist_handles_shown_frequency_threshold_ms", 0);
    }

    private long getShowAndGoDuration() {
        return (long) this.mA11yManager.get().getRecommendedTimeoutMillis((int) this.mDeviceConfigHelper.getLong("assist_handles_show_and_go_duration_ms", DEFAULT_SHOW_AND_GO_DURATION_MS), 1);
    }

    private String getBehaviorMode() {
        return this.mDeviceConfigHelper.getString("assist_handles_behavior_mode", DEFAULT_BEHAVIOR.toString());
    }

    private void maybeShowHandles(boolean z) {
        if (!this.mHandlesShowing && handlesUnblocked(z)) {
            this.mHandlesShowing = true;
            AssistHandleViewController assistHandleViewController = this.mAssistHandleViewController.get();
            if (assistHandleViewController == null) {
                Log.w("AssistHandleBehavior", "Couldn't show handles, AssistHandleViewController unavailable");
            } else {
                assistHandleViewController.lambda$setAssistHintVisible$0(true);
            }
        }
    }

    /* access modifiers changed from: public */
    private void hideHandles() {
        if (this.mHandlesShowing) {
            this.mHandlesShowing = false;
            this.mHandlesLastHiddenAt = SystemClock.elapsedRealtime();
            AssistHandleViewController assistHandleViewController = this.mAssistHandleViewController.get();
            if (assistHandleViewController == null) {
                Log.w("AssistHandleBehavior", "Couldn't show handles, AssistHandleViewController unavailable");
            } else {
                assistHandleViewController.lambda$setAssistHintVisible$0(false);
            }
        }
    }

    /* access modifiers changed from: public */
    private void handleNavigationModeChange(int i) {
        boolean isGesturalMode = QuickStepContract.isGesturalMode(i);
        if (this.mInGesturalMode != isGesturalMode) {
            this.mInGesturalMode = isGesturalMode;
            if (isGesturalMode) {
                this.mBehaviorMap.get(this.mCurrentBehavior).onModeActivated(this.mContext, this);
                return;
            }
            this.mBehaviorMap.get(this.mCurrentBehavior).onModeDeactivated();
            hide();
        }
    }

    private void clearPendingCommands() {
        this.mHandler.removeCallbacks(this.mHideHandles);
        this.mHandler.removeCallbacks(this.mShowAndGo);
        this.mShowAndGoEndsAt = 0;
    }

    private boolean isUserSetupComplete() {
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1) {
            return true;
        }
        return false;
    }

    public void setInGesturalModeForTest(boolean z) {
        this.mInGesturalMode = z;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("Current AssistHandleBehaviorController State:");
        printWriter.println("   mHandlesShowing=" + this.mHandlesShowing);
        printWriter.println("   mHandlesLastHiddenAt=" + this.mHandlesLastHiddenAt);
        printWriter.println("   mInGesturalMode=" + this.mInGesturalMode);
        printWriter.println("   Phenotype Flags:");
        printWriter.println("      assist_handles_show_and_go_duration_ms(a11y modded)=" + getShowAndGoDuration());
        printWriter.println("      assist_handles_shown_frequency_threshold_ms=" + getShownFrequencyThreshold());
        printWriter.println("      assist_handles_behavior_mode=" + getBehaviorMode());
        printWriter.println("   mCurrentBehavior=" + this.mCurrentBehavior.toString());
        this.mBehaviorMap.get(this.mCurrentBehavior).dump(printWriter, "   ");
    }
}
