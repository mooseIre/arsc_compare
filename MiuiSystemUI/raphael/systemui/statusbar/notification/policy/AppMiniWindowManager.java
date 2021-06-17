package com.android.systemui.statusbar.notification.policy;

import android.app.ActivityOptions;
import android.app.ActivityOptionsInjector;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.MiuiMultiWindowAdapter;
import android.util.MiuiMultiWindowUtils;
import com.android.systemui.Dependency;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.miui.systemui.util.MiuiActivityUtil;
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
    private static final boolean HAS_MINI_WINDOW_FEATURE = MiuiMultiWindowAdapter.hasSmallFreeformFeature();
    private final Context context;
    private final Consumer<Boolean> mDockedStackExistsListener = new AppMiniWindowManager$mDockedStackExistsListener$1(this);
    private boolean mHasSmallWindow;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private boolean mInDockedStackMode;
    private boolean mInModalMode;
    private boolean mInPinnedMode;
    private final ArrayList<WindowForegroundListener> mOneshotForegroundListeners = new ArrayList<>();
    private boolean mRegisterForegroundListener;
    private ComponentName mTopActivity;
    private String mTopWindowPackage;
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
        Object obj = Dependency.get(MiuiActivityUtil.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(MiuiActivityUtil::class.java)");
        this.mTopActivity = ((MiuiActivityUtil) obj).getTopActivity();
        ((MiuiActivityUtil) Dependency.get(MiuiActivityUtil.class)).addTopActivityMayChangeListener(new MiuiActivityUtil.TopActivityMayChangeListener(this) {
            /* class com.android.systemui.statusbar.notification.policy.AppMiniWindowManager.AnonymousClass1 */
            final /* synthetic */ AppMiniWindowManager this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.miui.systemui.util.MiuiActivityUtil.TopActivityMayChangeListener
            public void onTopActivityMayChanged(@Nullable ComponentName componentName, boolean z) {
                this.this$0.mTopActivity = componentName;
                this.this$0.mHasSmallWindow = z;
                if (this.this$0.mHeadsUpManager.hasPinnedHeadsUp()) {
                    handler.post(new AppMiniWindowManager$1$onTopActivityMayChanged$1(this));
                }
            }
        });
        divider.registerInSplitScreenListener(this.mDockedStackExistsListener);
        headsUpManagerPhone.addListener(this);
        modalController.addOnModalChangeListener(new ModalController.OnModalChangeListener(this) {
            /* class com.android.systemui.statusbar.notification.policy.AppMiniWindowManager.AnonymousClass2 */
            final /* synthetic */ AppMiniWindowManager this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.statusbar.notification.modal.ModalController.OnModalChangeListener
            public void onChange(boolean z) {
                this.this$0.mInModalMode = z;
                this.this$0.evaluateRegisterListener();
            }
        });
        this.mWindowListener = new AppMiniWindowManager$mWindowListener$1(this, handler);
    }

    public final boolean canNotificationSlide(@Nullable String str, @Nullable PendingIntent pendingIntent) {
        if (HAS_MINI_WINDOW_FEATURE && !this.mInDockedStackMode && pendingIntent != null && pendingIntent.isActivity() && this.notificationSettingsManager.canSlide(str)) {
            return MiniWindowPolicy.INSTANCE.canSlidePackage(str, pendingIntent.getIntent(), this.mTopWindowPackage, this.mTopActivity, this.mHasSmallWindow);
        }
        return false;
    }

    public final void launchMiniWindowActivity(@Nullable String str, @Nullable PendingIntent pendingIntent) {
        if (str != null && pendingIntent != null) {
            ActivityOptions activityOptions = MiuiMultiWindowUtils.getActivityOptions(this.context, str, true, false);
            Intrinsics.checkExpressionValueIsNotNull(activityOptions, "activityOptions");
            ActivityOptionsInjector activityOptionsInjector = activityOptions.getActivityOptionsInjector();
            Intrinsics.checkExpressionValueIsNotNull(activityOptionsInjector, "activityOptions.activityOptionsInjector");
            activityOptionsInjector.setFreeformAnimation(false);
            Intent intent = new Intent();
            MiniWindowPolicy.INSTANCE.initializeMiniWindowIntent(str, intent);
            try {
                pendingIntent.send(this.context, 0, intent, null, null, null, activityOptions.toBundle());
            } catch (Exception e) {
                Log.w("AppMiniWindowManager", "Start freeform failed", e);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpPinnedModeChanged(boolean z) {
        super.onHeadsUpPinnedModeChanged(z);
        this.mInPinnedMode = z;
        evaluateRegisterListener();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void evaluateRegisterListener() {
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
        ArrayList<WindowForegroundListener> arrayList2 = new ArrayList();
        for (T t : arrayList) {
            if (Intrinsics.areEqual(t.getPackageName(), str)) {
                arrayList2.add(t);
            }
        }
        for (WindowForegroundListener windowForegroundListener : arrayList2) {
            windowForegroundListener.getCallback().invoke();
        }
        this.mOneshotForegroundListeners.removeAll(arrayList2);
    }

    /* access modifiers changed from: private */
    public final void updateAllHeadsUpMiniBars() {
        this.mHeadsUpManager.getAllEntries().filter(AppMiniWindowManager$updateAllHeadsUpMiniBars$1.INSTANCE).filter(AppMiniWindowManager$updateAllHeadsUpMiniBars$2.INSTANCE).map(AppMiniWindowManager$updateAllHeadsUpMiniBars$3.INSTANCE).forEach(AppMiniWindowManager$updateAllHeadsUpMiniBars$4.INSTANCE);
    }
}
