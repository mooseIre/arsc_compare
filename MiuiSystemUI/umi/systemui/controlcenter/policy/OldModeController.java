package com.android.systemui.controlcenter.policy;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.CallbackController;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import java.util.ArrayList;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: OldModeController.kt */
public final class OldModeController implements CallbackController<OldModeChangeListener>, SettingsObserver.Callback {
    public static final Companion Companion = new Companion(null);
    private static final int MSG_ADD_CALLBACK = 1;
    private static final int MSG_NOTIFY = 3;
    private static final int MSG_REMOVE_CALLBACK = 2;
    @NotNull
    private static final String SETTING_OLD_MODE_NAME = "elderly_mode";
    @NotNull
    private static final String TAG = "OldModeController";
    @Nullable
    private CurrentUserTracker mCurrentUserTracker;
    @NotNull
    private final Handler mHandler = new H();
    @Nullable
    private List<OldModeChangeListener> mListeners = new ArrayList();
    private boolean mOldModeOn;
    @NotNull
    private final SettingsObserver mSettingsObserver;

    /* compiled from: OldModeController.kt */
    public interface OldModeChangeListener {
        void onOldModeChange(boolean z);
    }

    public OldModeController(@NotNull BroadcastDispatcher broadcastDispatcher, @NotNull SettingsObserver settingsObserver) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "mBroadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(settingsObserver, "mSettingsObserver");
        this.mSettingsObserver = settingsObserver;
        this.mCurrentUserTracker = new CurrentUserTracker(this, broadcastDispatcher, broadcastDispatcher) {
            /* class com.android.systemui.controlcenter.policy.OldModeController.AnonymousClass1 */
            final /* synthetic */ OldModeController this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                this.this$0.onContentChanged(OldModeController.Companion.getSETTING_OLD_MODE_NAME(), SettingsObserver.getValue$default(this.this$0.getMSettingsObserver(), OldModeController.Companion.getSETTING_OLD_MODE_NAME(), 0, null, 6, null));
            }
        };
    }

    @NotNull
    public final SettingsObserver getMSettingsObserver() {
        return this.mSettingsObserver;
    }

    /* compiled from: OldModeController.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final int getMSG_ADD_CALLBACK() {
            return OldModeController.MSG_ADD_CALLBACK;
        }

        public final int getMSG_REMOVE_CALLBACK() {
            return OldModeController.MSG_REMOVE_CALLBACK;
        }

        public final int getMSG_NOTIFY() {
            return OldModeController.MSG_NOTIFY;
        }

        @NotNull
        public final String getSETTING_OLD_MODE_NAME() {
            return OldModeController.SETTING_OLD_MODE_NAME;
        }
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (Intrinsics.areEqual(str, SETTING_OLD_MODE_NAME)) {
            this.mOldModeOn = MiuiTextUtils.parseBoolean(str2, false);
            String str3 = TAG;
            Log.d(str3, "onChange: mOldModeOn = " + this.mOldModeOn);
            notifyAllListeners();
        }
    }

    public final boolean isActive() {
        return this.mOldModeOn;
    }

    private final void register() {
        CurrentUserTracker currentUserTracker = this.mCurrentUserTracker;
        if (currentUserTracker != null) {
            currentUserTracker.startTracking();
            this.mSettingsObserver.addCallback(this, SETTING_OLD_MODE_NAME);
            String str = SETTING_OLD_MODE_NAME;
            onContentChanged(str, SettingsObserver.getValue$default(this.mSettingsObserver, str, 0, null, 6, null));
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    public void unRegister() {
        this.mSettingsObserver.removeCallback(this);
        CurrentUserTracker currentUserTracker = this.mCurrentUserTracker;
        if (currentUserTracker != null) {
            currentUserTracker.stopTracking();
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void notifyAllListeners() {
        List<OldModeChangeListener> list = this.mListeners;
        if (list != null) {
            for (OldModeChangeListener oldModeChangeListener : list) {
                oldModeChangeListener.onOldModeChange(this.mOldModeOn);
            }
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void addCallbackLocked(OldModeChangeListener oldModeChangeListener) {
        List<OldModeChangeListener> list = this.mListeners;
        if (list != null) {
            if (list.isEmpty()) {
                register();
            }
            List<OldModeChangeListener> list2 = this.mListeners;
            if (list2 != null) {
                if (!list2.contains(oldModeChangeListener)) {
                    List<OldModeChangeListener> list3 = this.mListeners;
                    if (list3 != null) {
                        list3.add(oldModeChangeListener);
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                }
                oldModeChangeListener.onOldModeChange(this.mOldModeOn);
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void removeCallbackLocked(OldModeChangeListener oldModeChangeListener) {
        List<OldModeChangeListener> list = this.mListeners;
        if (list != null) {
            list.remove(oldModeChangeListener);
            List<OldModeChangeListener> list2 = this.mListeners;
            if (list2 == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (list2.size() == 0) {
                unRegister();
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public void addCallback(@Nullable OldModeChangeListener oldModeChangeListener) {
        if (oldModeChangeListener != null) {
            this.mHandler.obtainMessage(MSG_ADD_CALLBACK, oldModeChangeListener).sendToTarget();
        }
    }

    public void removeCallback(@Nullable OldModeChangeListener oldModeChangeListener) {
        if (oldModeChangeListener != null) {
            this.mHandler.obtainMessage(MSG_REMOVE_CALLBACK, oldModeChangeListener).sendToTarget();
        }
    }

    /* compiled from: OldModeController.kt */
    public final class H extends Handler {
        /* JADX WARN: Incorrect args count in method signature: ()V */
        public H() {
        }

        public void handleMessage(@NotNull Message message) {
            Intrinsics.checkParameterIsNotNull(message, "msg");
            int i = message.what;
            if (i == OldModeController.Companion.getMSG_ADD_CALLBACK()) {
                OldModeController oldModeController = OldModeController.this;
                Object obj = message.obj;
                if (obj != null) {
                    oldModeController.addCallbackLocked((OldModeChangeListener) obj);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controlcenter.policy.OldModeController.OldModeChangeListener");
            } else if (i == OldModeController.Companion.getMSG_REMOVE_CALLBACK()) {
                OldModeController oldModeController2 = OldModeController.this;
                Object obj2 = message.obj;
                if (obj2 != null) {
                    oldModeController2.removeCallbackLocked((OldModeChangeListener) obj2);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controlcenter.policy.OldModeController.OldModeChangeListener");
            } else if (i == OldModeController.Companion.getMSG_NOTIFY()) {
                OldModeController.this.notifyAllListeners();
            }
        }
    }
}
