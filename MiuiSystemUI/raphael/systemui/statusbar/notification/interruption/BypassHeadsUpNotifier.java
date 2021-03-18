package com.android.systemui.statusbar.notification.interruption;

import android.content.Context;
import android.media.MediaMetadata;
import android.provider.Settings;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.tuner.TunerService;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BypassHeadsUpNotifier.kt */
public final class BypassHeadsUpNotifier implements StatusBarStateController.StateListener, NotificationMediaManager.MediaListener {
    private final KeyguardBypassController bypassController;
    private final Context context;
    private NotificationEntry currentMediaEntry;
    private boolean enabled = true;
    private final NotificationEntryManager entryManager;
    private boolean fullyAwake;
    private final HeadsUpManagerPhone headsUpManager;
    private final NotificationMediaManager mediaManager;
    private final NotificationLockscreenUserManager notificationLockscreenUserManager;
    private final StatusBarStateController statusBarStateController;

    public BypassHeadsUpNotifier(@NotNull Context context2, @NotNull KeyguardBypassController keyguardBypassController, @NotNull StatusBarStateController statusBarStateController2, @NotNull HeadsUpManagerPhone headsUpManagerPhone, @NotNull NotificationLockscreenUserManager notificationLockscreenUserManager2, @NotNull NotificationMediaManager notificationMediaManager, @NotNull NotificationEntryManager notificationEntryManager, @NotNull TunerService tunerService) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(keyguardBypassController, "bypassController");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(notificationLockscreenUserManager2, "notificationLockscreenUserManager");
        Intrinsics.checkParameterIsNotNull(notificationMediaManager, "mediaManager");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(tunerService, "tunerService");
        this.context = context2;
        this.bypassController = keyguardBypassController;
        this.statusBarStateController = statusBarStateController2;
        this.headsUpManager = headsUpManagerPhone;
        this.notificationLockscreenUserManager = notificationLockscreenUserManager2;
        this.mediaManager = notificationMediaManager;
        this.entryManager = notificationEntryManager;
        statusBarStateController2.addCallback(this);
        tunerService.addTunable(new TunerService.Tunable(this) {
            /* class com.android.systemui.statusbar.notification.interruption.BypassHeadsUpNotifier.AnonymousClass1 */
            final /* synthetic */ BypassHeadsUpNotifier this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                BypassHeadsUpNotifier bypassHeadsUpNotifier = this.this$0;
                boolean z = false;
                if (Settings.Secure.getIntForUser(bypassHeadsUpNotifier.context.getContentResolver(), "show_media_when_bypassing", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                    z = true;
                }
                bypassHeadsUpNotifier.enabled = z;
            }
        }, "show_media_when_bypassing");
    }

    public final void setFullyAwake(boolean z) {
        this.fullyAwake = z;
        if (z) {
            updateAutoHeadsUp(this.currentMediaEntry);
        }
    }

    public final void setUp() {
        this.mediaManager.addCallback(this);
    }

    @Override // com.android.systemui.statusbar.NotificationMediaManager.MediaListener
    public void onPrimaryMetadataOrStateChanged(@Nullable MediaMetadata mediaMetadata, int i) {
        NotificationEntry notificationEntry = this.currentMediaEntry;
        NotificationEntry activeNotificationUnfiltered = this.entryManager.getActiveNotificationUnfiltered(this.mediaManager.getMediaNotificationKey());
        if (!NotificationMediaManager.isPlayingState(i)) {
            activeNotificationUnfiltered = null;
        }
        this.currentMediaEntry = activeNotificationUnfiltered;
        updateAutoHeadsUp(notificationEntry);
        updateAutoHeadsUp(this.currentMediaEntry);
    }

    private final void updateAutoHeadsUp(NotificationEntry notificationEntry) {
        if (notificationEntry != null) {
            boolean z = Intrinsics.areEqual(notificationEntry, this.currentMediaEntry) && canAutoHeadsUp(notificationEntry);
            notificationEntry.setAutoHeadsUp(z);
            if (z) {
                this.headsUpManager.showNotification(notificationEntry);
            }
        }
    }

    private final boolean canAutoHeadsUp(NotificationEntry notificationEntry) {
        if (isAutoHeadsUpAllowed() && !notificationEntry.isSensitive() && this.notificationLockscreenUserManager.shouldShowOnKeyguard(notificationEntry) && this.entryManager.getActiveNotificationUnfiltered(notificationEntry.getKey()) == null) {
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStatePostChange() {
        updateAutoHeadsUp(this.currentMediaEntry);
    }

    private final boolean isAutoHeadsUpAllowed() {
        if (this.enabled && this.bypassController.getBypassEnabled() && this.statusBarStateController.getState() == 1 && this.fullyAwake) {
            return true;
        }
        return false;
    }
}
