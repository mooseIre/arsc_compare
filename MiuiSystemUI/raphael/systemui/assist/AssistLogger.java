package com.android.systemui.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.android.internal.app.AssistUtils;
import com.android.internal.logging.InstanceId;
import com.android.internal.logging.InstanceIdSequence;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.FrameworkStatsLog;
import com.android.keyguard.KeyguardUpdateMonitor;
import java.util.Set;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.collections.SetsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AssistLogger.kt */
public class AssistLogger {
    private static final Set<AssistantSessionEvent> SESSION_END_EVENTS = SetsKt.setOf((Object[]) new AssistantSessionEvent[]{AssistantSessionEvent.ASSISTANT_SESSION_INVOCATION_CANCELLED, AssistantSessionEvent.ASSISTANT_SESSION_CLOSE});
    private final AssistHandleBehaviorController assistHandleBehaviorController;
    private final AssistUtils assistUtils;
    @NotNull
    private final Context context;
    private InstanceId currentInstanceId;
    private final InstanceIdSequence instanceIdSequence = new InstanceIdSequence(1048576);
    private final PhoneStateMonitor phoneStateMonitor;
    @NotNull
    private final UiEventLogger uiEventLogger;

    /* access modifiers changed from: protected */
    public void reportAssistantInvocationExtraData() {
    }

    public AssistLogger(@NotNull Context context2, @NotNull UiEventLogger uiEventLogger2, @NotNull AssistUtils assistUtils2, @NotNull PhoneStateMonitor phoneStateMonitor2, @NotNull AssistHandleBehaviorController assistHandleBehaviorController2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(uiEventLogger2, "uiEventLogger");
        Intrinsics.checkParameterIsNotNull(assistUtils2, "assistUtils");
        Intrinsics.checkParameterIsNotNull(phoneStateMonitor2, "phoneStateMonitor");
        Intrinsics.checkParameterIsNotNull(assistHandleBehaviorController2, "assistHandleBehaviorController");
        this.context = context2;
        this.uiEventLogger = uiEventLogger2;
        this.assistUtils = assistUtils2;
        this.phoneStateMonitor = phoneStateMonitor2;
        this.assistHandleBehaviorController = assistHandleBehaviorController2;
    }

    public final void reportAssistantInvocationEventFromLegacy(int i, boolean z, @Nullable ComponentName componentName, @Nullable Integer num) {
        reportAssistantInvocationEvent(AssistantInvocationEvent.Companion.eventFromLegacyInvocationType(i, z), componentName, num == null ? null : Integer.valueOf(AssistantInvocationEvent.Companion.deviceStateFromLegacyDeviceState(num.intValue())));
    }

    public final void reportAssistantInvocationEvent(@NotNull UiEventLogger.UiEventEnum uiEventEnum, @Nullable ComponentName componentName, @Nullable Integer num) {
        int i;
        Intrinsics.checkParameterIsNotNull(uiEventEnum, "invocationEvent");
        if (componentName == null) {
            componentName = getAssistantComponentForCurrentUser();
        }
        int assistantUid = getAssistantUid(componentName);
        if (num != null) {
            i = num.intValue();
        } else {
            i = AssistantInvocationEvent.Companion.deviceStateFromLegacyDeviceState(this.phoneStateMonitor.getPhoneState());
        }
        FrameworkStatsLog.write(281, uiEventEnum.getId(), assistantUid, componentName.flattenToString(), getOrCreateInstanceId().getId(), i, this.assistHandleBehaviorController.areHandlesShowing());
        reportAssistantInvocationExtraData();
    }

    public final void reportAssistantSessionEvent(@NotNull UiEventLogger.UiEventEnum uiEventEnum) {
        Intrinsics.checkParameterIsNotNull(uiEventEnum, "sessionEvent");
        ComponentName assistantComponentForCurrentUser = getAssistantComponentForCurrentUser();
        this.uiEventLogger.logWithInstanceId(uiEventEnum, getAssistantUid(assistantComponentForCurrentUser), assistantComponentForCurrentUser.flattenToString(), getOrCreateInstanceId());
        if (CollectionsKt___CollectionsKt.contains(SESSION_END_EVENTS, uiEventEnum)) {
            clearInstanceId();
        }
    }

    /* access modifiers changed from: protected */
    @NotNull
    public final InstanceId getOrCreateInstanceId() {
        InstanceId instanceId = this.currentInstanceId;
        if (instanceId == null) {
            instanceId = this.instanceIdSequence.newInstanceId();
        }
        this.currentInstanceId = instanceId;
        Intrinsics.checkExpressionValueIsNotNull(instanceId, "instanceId");
        return instanceId;
    }

    /* access modifiers changed from: protected */
    public final void clearInstanceId() {
        this.currentInstanceId = null;
    }

    /* access modifiers changed from: protected */
    @NotNull
    public final ComponentName getAssistantComponentForCurrentUser() {
        ComponentName assistComponentForUser = this.assistUtils.getAssistComponentForUser(KeyguardUpdateMonitor.getCurrentUser());
        Intrinsics.checkExpressionValueIsNotNull(assistComponentForUser, "assistUtils.getAssistCom…Monitor.getCurrentUser())");
        return assistComponentForUser;
    }

    /* access modifiers changed from: protected */
    public final int getAssistantUid(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "assistantComponent");
        try {
            return this.context.getPackageManager().getApplicationInfo(componentName.getPackageName(), 0).uid;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("AssistLogger", "Unable to find Assistant UID", e);
            return 0;
        }
    }
}
