package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiKeyguardMediaController;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.stack.SectionHeaderView;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import com.android.systemui.statusbar.notification.zen.ZenModeViewController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.internal.vip.utils.Utils;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationSectionsManager.kt */
public final class MiuiNotificationSectionsManager extends NotificationSectionsManager {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private final ConfigurationController configurationController;
    private final MiuiNotificationSectionsManager$configurationListener$1 configurationListener = new MiuiNotificationSectionsManager$configurationListener$1(this);
    @Nullable
    private SectionHeaderView importantView;
    @NotNull
    private final NotificationSectionsLogger logger;
    private final MiuiNotificationSectionsFeatureManager sectionsFeatureManager;
    @NotNull
    private final StatusBarStateController statusBarStateController;
    @Nullable
    private ZenModeView zenModeView;
    private final ZenModeViewController zenModeViewController;

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    public void onGentleHeaderClick() {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationSectionsManager(@NotNull ActivityStarter activityStarter, @NotNull StatusBarStateController statusBarStateController2, @NotNull ConfigurationController configurationController2, @NotNull PeopleHubViewAdapter peopleHubViewAdapter, @NotNull MiuiKeyguardMediaController miuiKeyguardMediaController, @NotNull ZenModeViewController zenModeViewController2, @NotNull MiuiNotificationSectionsFeatureManager miuiNotificationSectionsFeatureManager, @NotNull NotificationSectionsLogger notificationSectionsLogger) {
        super(activityStarter, statusBarStateController2, configurationController2, peopleHubViewAdapter, miuiKeyguardMediaController, miuiNotificationSectionsFeatureManager, notificationSectionsLogger);
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(configurationController2, "configurationController");
        Intrinsics.checkParameterIsNotNull(peopleHubViewAdapter, "peopleHubViewAdapter");
        Intrinsics.checkParameterIsNotNull(miuiKeyguardMediaController, "keyguardMediaController");
        Intrinsics.checkParameterIsNotNull(zenModeViewController2, "zenModeViewController");
        Intrinsics.checkParameterIsNotNull(miuiNotificationSectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(notificationSectionsLogger, "logger");
        this.statusBarStateController = statusBarStateController2;
        this.configurationController = configurationController2;
        this.zenModeViewController = zenModeViewController2;
        this.sectionsFeatureManager = miuiNotificationSectionsFeatureManager;
        this.logger = notificationSectionsLogger;
    }

    @VisibleForTesting
    @Nullable
    public final ZenModeView getZenModeView() {
        return this.zenModeView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getImportantView() {
        return this.importantView;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    public void initialize(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, @NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "parent");
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        super.initialize(notificationStackScrollLayout, layoutInflater);
        this.configurationController.addCallback(this.configurationListener);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    public void reinflateViews(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        super.reinflateViews(layoutInflater);
        reinflateZenModeView(layoutInflater);
        reinflateImportantView(layoutInflater);
    }

    /* access modifiers changed from: private */
    public final void reinflateZenModeView(LayoutInflater layoutInflater) {
        ZenModeView zenModeView2 = (ZenModeView) reinflateView(this.zenModeView, layoutInflater, C0017R$layout.keyguard_zen_header);
        this.zenModeViewController.attach(zenModeView2);
        this.zenModeView = zenModeView2;
    }

    private final void reinflateImportantView(LayoutInflater layoutInflater) {
        this.importantView = (SectionHeaderView) reinflateView(this.importantView, layoutInflater, C0017R$layout.status_bar_notification_section_header);
    }

    @Override // com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm.SectionProvider, com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    public boolean beginsSection(@NotNull View view, @Nullable View view2) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        return view == this.zenModeView || view == this.importantView || super.beginsSection(view, view2);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    @Nullable
    public Integer getBucket(@Nullable View view) {
        if (view == this.zenModeView) {
            return 8;
        }
        if (view == this.importantView) {
            return 5;
        }
        return super.getBucket(view);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    public void logShadeChild(int i, @NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "child");
        if (view == this.zenModeView) {
            this.logger.logZenModeView(i);
        } else if (view == this.importantView) {
            this.logger.logImportantView(i);
        } else {
            super.logShadeChild(i, view);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:92:0x01bc, code lost:
        if (r18.intValue() != r5.getBucket()) goto L_0x01be;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x01de  */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01e7  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x01f3  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x01f5  */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x020a  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x020c  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0238  */
    /* JADX WARNING: Removed duplicated region for block: B:133:0x024f  */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0263  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x02cd  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x02d6  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x02ec  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x02ff  */
    /* JADX WARNING: Removed duplicated region for block: B:190:0x0312  */
    /* JADX WARNING: Removed duplicated region for block: B:196:0x0325  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x0338  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x034b  */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x037d A[LOOP:2: B:218:0x0377->B:220:0x037d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:223:0x039e  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x03b2  */
    /* JADX WARNING: Removed duplicated region for block: B:250:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x014a  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x014c  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x0151  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0166  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0190  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0193  */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01a9  */
    /* JADX WARNING: Removed duplicated region for block: B:98:0x01c8  */
    @Override // com.android.systemui.statusbar.notification.stack.NotificationSectionsManager
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateSectionBoundaries(@org.jetbrains.annotations.NotNull java.lang.String r29) {
        /*
        // Method dump skipped, instructions count: 993
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager.updateSectionBoundaries(java.lang.String):void");
    }

    /* compiled from: MiuiNotificationSectionsManager.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final Intent intent4NotificationControlCenterSettings() {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
            intent.putExtra(":settings:show_fragment", "com.android.settings.NotificationControlCenterSettings");
            Context context = Utils.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "Utils.getContext()");
            intent.putExtra(":settings:show_fragment_title", context.getResources().getString(C0021R$string.notification_control_center));
            intent.addFlags(268435456);
            return intent;
        }
    }
}
