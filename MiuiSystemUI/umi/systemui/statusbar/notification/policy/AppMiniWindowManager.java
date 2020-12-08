package com.android.systemui.statusbar.notification.policy;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityOptionsInjector;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.MiuiMultiWindowAdapter;
import android.util.MiuiMultiWindowUtils;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.ArrayList;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import miui.process.ProcessManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager implements OnHeadsUpChangedListener {
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    private static final boolean HAS_MINI_WINDOW_FEATURE = MiuiMultiWindowAdapter.hasSmallFreeformFeature();
    /* access modifiers changed from: private */
    public final Context context;
    /* access modifiers changed from: private */
    public final ActivityManagerWrapper mActivityManager = ActivityManagerWrapper.getInstance();
    /* access modifiers changed from: private */
    public boolean mExpectingTaskStackChanged;
    /* access modifiers changed from: private */
    public boolean mHasSmallWindow;
    /* access modifiers changed from: private */
    public final HeadsUpManagerPhone mHeadsUpManager;
    /* access modifiers changed from: private */
    public boolean mInDockedStackMode;
    /* access modifiers changed from: private */
    public boolean mInModalMode;
    private boolean mInPinnedMode;
    private final ArrayList<WindowForegroundListener> mOneshotForegroundListeners;
    private boolean mRegisterForegroundListener;
    /* access modifiers changed from: private */
    public ComponentName mTopActivity;
    /* access modifiers changed from: private */
    public String mTopWindowPackage;
    private final AppMiniWindowManager$mWindowListener$1 mWindowListener;
    private final NotificationSettingsManager notificationSettingsManager;

    public AppMiniWindowManager(@NotNull Context context2, @NotNull Divider divider, @NotNull HeadsUpManagerPhone headsUpManagerPhone, @NotNull final Handler handler, @NotNull ModalController modalController, @NotNull NotificationSettingsManager notificationSettingsManager2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(divider, "divider");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "headsUpManagerPhone");
        Intrinsics.checkParameterIsNotNull(handler, "handler");
        Intrinsics.checkParameterIsNotNull(modalController, "modalController");
        Intrinsics.checkParameterIsNotNull(notificationSettingsManager2, "notificationSettingsManager");
        this.context = context2;
        this.notificationSettingsManager = notificationSettingsManager2;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mOneshotForegroundListeners = new ArrayList<>();
        ActivityManagerWrapper activityManagerWrapper = this.mActivityManager;
        Intrinsics.checkExpressionValueIsNotNull(activityManagerWrapper, "mActivityManager");
        ActivityManager.RunningTaskInfo runningTask = activityManagerWrapper.getRunningTask();
        this.mTopActivity = runningTask != null ? runningTask.topActivity : null;
        this.mActivityManager.registerTaskStackListener(new TaskStackChangeListener(this) {
            final /* synthetic */ AppMiniWindowManager this$0;

            {
                this.this$0 = r1;
            }

            public void onTaskMovedToFront(@Nullable ActivityManager.RunningTaskInfo runningTaskInfo) {
                super.onTaskMovedToFront(runningTaskInfo);
                this.this$0.mTopActivity = runningTaskInfo != null ? runningTaskInfo.topActivity : null;
                if (this.this$0.mHeadsUpManager.hasPinnedHeadsUp()) {
                    handler.post(new AppMiniWindowManager$1$onTaskMovedToFront$1(this));
                }
                this.this$0.mExpectingTaskStackChanged = true;
            }

            public void onTaskStackChanged() {
                super.onTaskStackChanged();
                if (this.this$0.mExpectingTaskStackChanged) {
                    this.this$0.mExpectingTaskStackChanged = false;
                    return;
                }
                AppMiniWindowManager appMiniWindowManager = this.this$0;
                ActivityManagerWrapper access$getMActivityManager$p = appMiniWindowManager.mActivityManager;
                Intrinsics.checkExpressionValueIsNotNull(access$getMActivityManager$p, "mActivityManager");
                ActivityManager.RunningTaskInfo runningTask = access$getMActivityManager$p.getRunningTask();
                appMiniWindowManager.mTopActivity = runningTask != null ? runningTask.topActivity : null;
                Log.d("AppMiniWindowManager", "topActivity updated in onTaskStackChanged");
            }
        });
        this.context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("freeform_window_state"), false, new ContentObserver(this, new Handler(Looper.getMainLooper())) {
            final /* synthetic */ AppMiniWindowManager this$0;

            {
                this.this$0 = r1;
            }

            public void onChange(boolean z) {
                super.onChange(z);
                boolean isSmallWindowActivated = AppMiniWindowManager.Companion.isSmallWindowActivated(this.this$0.context);
                if (!isSmallWindowActivated && this.this$0.mHasSmallWindow) {
                    AppMiniWindowManager appMiniWindowManager = this.this$0;
                    ActivityManagerWrapper access$getMActivityManager$p = appMiniWindowManager.mActivityManager;
                    Intrinsics.checkExpressionValueIsNotNull(access$getMActivityManager$p, "mActivityManager");
                    ActivityManager.RunningTaskInfo runningTask = access$getMActivityManager$p.getRunningTask();
                    appMiniWindowManager.mTopActivity = runningTask != null ? runningTask.topActivity : null;
                }
                this.this$0.mHasSmallWindow = isSmallWindowActivated;
            }
        });
        divider.registerInSplitScreenListener(new Consumer<Boolean>(this) {
            final /* synthetic */ AppMiniWindowManager this$0;

            {
                this.this$0 = r1;
            }

            public final void accept(Boolean bool) {
                AppMiniWindowManager appMiniWindowManager = this.this$0;
                Intrinsics.checkExpressionValueIsNotNull(bool, "it");
                appMiniWindowManager.mInDockedStackMode = bool.booleanValue();
            }
        });
        headsUpManagerPhone.addListener(this);
        modalController.addOnModalChangeListener(new ModalController.OnModalChangeListener(this) {
            final /* synthetic */ AppMiniWindowManager this$0;

            {
                this.this$0 = r1;
            }

            public void onChange(boolean z) {
                this.this$0.mInModalMode = z;
                this.this$0.evaluateRegisterListener();
            }
        });
        this.mWindowListener = new AppMiniWindowManager$mWindowListener$1(this, handler);
    }

    /* compiled from: AppMiniWindowManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final boolean isSmallWindowActivated(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return Settings.Secure.getInt(context.getContentResolver(), "freeform_window_state", -1) != -1;
        }
    }

    public final boolean canNotificationSlide(@Nullable PendingIntent pendingIntent) {
        if (!HAS_MINI_WINDOW_FEATURE || this.mInDockedStackMode || pendingIntent == null || !pendingIntent.isActivity()) {
            return false;
        }
        String creatorPackage = pendingIntent.getCreatorPackage();
        if (!this.notificationSettingsManager.canSlide(creatorPackage)) {
            return false;
        }
        return MiniWindowPolicy.INSTANCE.canSlidePackage(creatorPackage, pendingIntent.getIntent(), this.mTopWindowPackage, this.mTopActivity, this.mHasSmallWindow);
    }

    public final void launchMiniWindowActivity(@Nullable PendingIntent pendingIntent) {
        String creatorPackage = pendingIntent != null ? pendingIntent.getCreatorPackage() : null;
        if (creatorPackage != null) {
            ActivityOptions activityOptions = MiuiMultiWindowUtils.getActivityOptions(this.context, creatorPackage, true, false);
            Intrinsics.checkExpressionValueIsNotNull(activityOptions, "activityOptions");
            ActivityOptionsInjector activityOptionsInjector = activityOptions.getActivityOptionsInjector();
            Intrinsics.checkExpressionValueIsNotNull(activityOptionsInjector, "activityOptions.activityOptionsInjector");
            activityOptionsInjector.setFreeformAnimation(false);
            Intent intent = new Intent();
            MiniWindowPolicy.INSTANCE.initializeMiniWindowIntent(creatorPackage, intent);
            try {
                pendingIntent.send(this.context, 0, intent, (PendingIntent.OnFinished) null, (Handler) null, (String) null, activityOptions.toBundle());
            } catch (Exception e) {
                Log.w("AppMiniWindowManager", "Start freeform failed", e);
            }
        }
    }

    public void onHeadsUpPinnedModeChanged(boolean z) {
        super.onHeadsUpPinnedModeChanged(z);
        this.mInPinnedMode = z;
        evaluateRegisterListener();
    }

    /* access modifiers changed from: private */
    public final void evaluateRegisterListener() {
        boolean z = this.mInPinnedMode || this.mInModalMode;
        if (this.mRegisterForegroundListener != z) {
            if (z) {
                ProcessManager.registerForegroundWindowListener(this.mWindowListener);
            } else {
                ProcessManager.unregisterForegroundWindowListener(this.mWindowListener);
                this.mTopWindowPackage = null;
            }
        }
        this.mRegisterForegroundListener = z;
    }

    public final void registerOneshotForegroundWindowListener(@NotNull String str, @NotNull Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        Intrinsics.checkParameterIsNotNull(function0, "callback");
        this.mOneshotForegroundListeners.add(new WindowForegroundListener(str, function0));
    }

    /* access modifiers changed from: private */
    public final void fireOneshotListenersForPackages(String str) {
        ArrayList<WindowForegroundListener> arrayList = this.mOneshotForegroundListeners;
        ArrayList<WindowForegroundListener> arrayList2 = new ArrayList<>();
        for (T next : arrayList) {
            if (Intrinsics.areEqual((Object) ((WindowForegroundListener) next).getPackageName(), (Object) str)) {
                arrayList2.add(next);
            }
        }
        for (WindowForegroundListener callback : arrayList2) {
            callback.getCallback().invoke();
        }
        this.mOneshotForegroundListeners.removeAll(arrayList2);
    }

    /* access modifiers changed from: private */
    public final void updateAllHeadsUpMiniBars() {
        this.mHeadsUpManager.getAllEntries().filter(AppMiniWindowManager$updateAllHeadsUpMiniBars$1.INSTANCE).filter(AppMiniWindowManager$updateAllHeadsUpMiniBars$2.INSTANCE).map(AppMiniWindowManager$updateAllHeadsUpMiniBars$3.INSTANCE).forEach(AppMiniWindowManager$updateAllHeadsUpMiniBars$4.INSTANCE);
    }
}
