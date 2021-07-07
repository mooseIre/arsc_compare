package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.miui.systemui.BuildConfig;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: FoldManager.kt */
public final class FoldManager {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final Handler handler;
    private static float headerDif = 0.0f;
    private static boolean isShowingUnimportant = false;
    private static boolean isUnimportantAnimating = false;
    private static boolean isUnimportantTransfering = false;
    private static boolean isUsingControlPanel = false;
    @NotNull
    private static final ArrayList<FoldListener> listeners = new ArrayList<>();
    private static float normalTarget = 0.0f;
    private static final int tagId = -140085847;
    @NotNull
    private static final ArrayList<FoldListener> topListeners = new ArrayList<>();
    private static float unimportantTarget;

    /* compiled from: FoldManager.kt */
    public static final class Companion {
        private final boolean needToMarkUnimportantTransfering(int i) {
            return i == 0 || i == 4 || i == 5;
        }

        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final int getTagId() {
            return FoldManager.tagId;
        }

        public final float getNormalTarget() {
            return FoldManager.normalTarget;
        }

        public final void setNormalTarget(float f) {
            FoldManager.normalTarget = f;
        }

        public final float getUnimportantTarget() {
            return FoldManager.unimportantTarget;
        }

        public final void setUnimportantTarget(float f) {
            FoldManager.unimportantTarget = f;
        }

        public final float getHeaderDif() {
            return FoldManager.headerDif;
        }

        public final void setHeaderDif(float f) {
            FoldManager.headerDif = f;
        }

        public final boolean isShowingUnimportant() {
            return FoldManager.isShowingUnimportant;
        }

        public final void setShowingUnimportant(boolean z) {
            FoldManager.isShowingUnimportant = z;
        }

        public final boolean isUnimportantTransfering() {
            return FoldManager.isUnimportantTransfering;
        }

        public final void setUnimportantTransfering(boolean z) {
            FoldManager.isUnimportantTransfering = z;
        }

        public final boolean isUnimportantAnimating() {
            return FoldManager.isUnimportantAnimating;
        }

        public final void setUnimportantAnimating(boolean z) {
            FoldManager.isUnimportantAnimating = z;
        }

        public final boolean isUsingControlPanel() {
            return FoldManager.isUsingControlPanel;
        }

        public final void setUsingControlPanel(boolean z) {
            FoldManager.isUsingControlPanel = z;
        }

        @NotNull
        public final ArrayList<FoldListener> getTopListeners() {
            return FoldManager.topListeners;
        }

        @NotNull
        public final ArrayList<FoldListener> getListeners() {
            return FoldManager.listeners;
        }

        @NotNull
        public final Handler getHandler() {
            return FoldManager.handler;
        }

        public final void notify(int i, @Nullable String str) {
            if (str != null) {
                notifyListeners(i, str);
            }
        }

        public final void addListener(@Nullable FoldListener foldListener) {
            if (!shouldSuppressFold() && foldListener != null) {
                getListeners().add(foldListener);
            }
        }

        public final void removeListener(@Nullable FoldListener foldListener) {
            if (!shouldSuppressFold() && foldListener != null) {
                getListeners().remove(foldListener);
            }
        }

        public final void addTopListeners(@Nullable FoldListener foldListener) {
            if (!shouldSuppressFold() && foldListener != null) {
                getTopListeners().add(foldListener);
            }
        }

        public final void notifyListeners(int i) {
            notifyListenersDelayed(i, 0);
        }

        public final void notifyListenersDelayed(int i, long j) {
            getHandler().sendEmptyMessageDelayed(i, j);
        }

        public final void notifyListeners(int i, @NotNull String str) {
            Intrinsics.checkParameterIsNotNull(str, "packageName");
            if (i == 0 || i == 4 || i == 5) {
                getHandler().removeMessages(0);
                getHandler().removeMessages(4);
                getHandler().removeMessages(5);
            }
            Message obtainMessage = getHandler().obtainMessage();
            obtainMessage.what = i;
            obtainMessage.obj = str;
            getHandler().sendMessage(obtainMessage);
        }

        /* access modifiers changed from: private */
        public final void notifyListenersCore1(int i, String str) {
            if (!shouldSuppressFold()) {
                if (i == 0) {
                    setShowingUnimportant(true);
                } else if (i == 4 || i == 5) {
                    setShowingUnimportant(false);
                }
                notifyListenersCore2(i, str);
            }
        }

        private final void notifyListenersCore2(int i, String str) {
            boolean needToMarkUnimportantTransfering = needToMarkUnimportantTransfering(i);
            if (needToMarkUnimportantTransfering) {
                setUnimportantTransfering(true);
            }
            notifyListenersCore3(i, str, getTopListeners());
            notifyListenersCore3(i, str, getListeners());
            if (needToMarkUnimportantTransfering) {
                setUnimportantTransfering(false);
            }
        }

        private final void notifyListenersCore3(int i, String str, ArrayList<FoldListener> arrayList) {
            Iterator<FoldListener> it = arrayList.iterator();
            while (it.hasNext()) {
                FoldListener next = it.next();
                if (i == 0) {
                    next.showUnimportantNotifications();
                } else if (i == 1) {
                    next.recoverPackageFromUnimportant(str);
                } else if (i == 2) {
                    next.foldPackageAsUnimportant(str);
                } else if (i == 3) {
                    next.cancelAllUnimportantNotifications();
                } else if (i == 4) {
                    next.resetAll(false);
                } else if (i == 5) {
                    next.resetAll(true);
                }
            }
        }

        public final boolean needToSkipHeightLimit() {
            return !isUsingControlPanel() && isShowingUnimportant();
        }

        public final boolean shouldSuppressFold() {
            return BuildConfig.IS_INTERNATIONAL;
        }

        public static /* synthetic */ boolean isSbnFold$default(Companion companion, StatusBarNotification statusBarNotification, boolean z, int i, int i2, Object obj) {
            if ((i2 & 2) != 0) {
                z = false;
            }
            if ((i2 & 4) != 0) {
                i = 0;
            }
            return companion.isSbnFold(statusBarNotification, z, i);
        }

        public final boolean isSbnFold(@NotNull StatusBarNotification statusBarNotification, boolean z, int i) {
            Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
            if (shouldSuppressFold()) {
                return false;
            }
            int canFold = FoldTool.INSTANCE.canFold(statusBarNotification, z, i);
            Log.d("UnimportantNotificationFoldTool", "isSbnFold: sbn=" + statusBarNotification.getKey() + ", fold_reason=" + canFold);
            return FoldTool.INSTANCE.shouldFold(canFold);
        }

        public final boolean canFoldByAnalyze(@NotNull StatusBarNotification statusBarNotification) {
            Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
            if (shouldSuppressFold()) {
                return false;
            }
            return FoldTool.INSTANCE.canFoldByAnalyze(statusBarNotification);
        }

        public final void checkFoldNotification(boolean z, @NotNull UserHandle userHandle) {
            Intrinsics.checkParameterIsNotNull(userHandle, "user");
            if (!shouldSuppressFold()) {
                if (z) {
                    onFoldAddOrUpdate(userHandle);
                } else {
                    onFoldRemoved(userHandle);
                }
            }
        }

        private final void onFoldAddOrUpdate(UserHandle userHandle) {
            ((FoldNotifController) Dependency.get(FoldNotifController.class)).sendFoldNotification(userHandle);
        }

        private final void onFoldRemoved(UserHandle userHandle) {
            ((FoldNotifController) Dependency.get(FoldNotifController.class)).cancelFoldNotification(userHandle);
        }
    }

    static {
        Object obj = Dependency.get(ControlPanelController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(ControlPanelController::class.java)");
        isUsingControlPanel = ((ControlPanelController) obj).isUseControlCenter();
        Context context = SystemUIApplication.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "SystemUIApplication.getContext()");
        handler = new Handler(context.getMainLooper(), FoldManager$Companion$handler$1.INSTANCE);
    }
}
