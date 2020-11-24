package com.android.systemui.statusbar.notification;

import android.view.LayoutInflater;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0014R$layout;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger;
import com.android.systemui.statusbar.notification.stack.NotificationSectionsManager;
import com.android.systemui.statusbar.notification.zen.ZenModeView;
import com.android.systemui.statusbar.notification.zen.ZenModeViewController;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationSectionsManager.kt */
public final class MiuiNotificationSectionsManager extends NotificationSectionsManager {
    @NotNull
    private final NotificationSectionsLogger logger;
    private final MiuiNotificationSectionsFeatureManager sectionsFeatureManager;
    @NotNull
    private final StatusBarStateController statusBarStateController;
    @Nullable
    private ZenModeView zenModeView;
    private final ZenModeViewController zenModeViewController;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MiuiNotificationSectionsManager(@org.jetbrains.annotations.NotNull com.android.systemui.plugins.ActivityStarter r14, @org.jetbrains.annotations.NotNull com.android.systemui.plugins.statusbar.StatusBarStateController r15, @org.jetbrains.annotations.NotNull com.android.systemui.statusbar.policy.ConfigurationController r16, @org.jetbrains.annotations.NotNull com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter r17, @org.jetbrains.annotations.NotNull com.android.systemui.media.KeyguardMediaController r18, @org.jetbrains.annotations.NotNull com.android.systemui.statusbar.notification.zen.ZenModeViewController r19, @org.jetbrains.annotations.NotNull com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager r20, @org.jetbrains.annotations.NotNull com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r21) {
        /*
            r13 = this;
            r8 = r13
            r9 = r15
            r10 = r19
            r11 = r20
            r12 = r21
            java.lang.String r0 = "activityStarter"
            r1 = r14
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r14, r0)
            java.lang.String r0 = "statusBarStateController"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r15, r0)
            java.lang.String r0 = "configurationController"
            r3 = r16
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r3, r0)
            java.lang.String r0 = "peopleHubViewAdapter"
            r4 = r17
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r4, r0)
            java.lang.String r0 = "keyguardMediaController"
            r5 = r18
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r5, r0)
            java.lang.String r0 = "zenModeViewController"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r10, r0)
            java.lang.String r0 = "sectionsFeatureManager"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r11, r0)
            java.lang.String r0 = "logger"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r12, r0)
            r0 = r13
            r2 = r15
            r6 = r20
            r7 = r21
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            r8.statusBarStateController = r9
            r8.zenModeViewController = r10
            r8.sectionsFeatureManager = r11
            r8.logger = r12
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager.<init>(com.android.systemui.plugins.ActivityStarter, com.android.systemui.plugins.statusbar.StatusBarStateController, com.android.systemui.statusbar.policy.ConfigurationController, com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter, com.android.systemui.media.KeyguardMediaController, com.android.systemui.statusbar.notification.zen.ZenModeViewController, com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager, com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger):void");
    }

    @VisibleForTesting
    @Nullable
    public final ZenModeView getZenModeView() {
        return this.zenModeView;
    }

    public void reinflateViews(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        super.reinflateViews(layoutInflater);
        ZenModeView zenModeView2 = (ZenModeView) reinflateView(this.zenModeView, layoutInflater, C0014R$layout.keyguard_zen_header);
        this.zenModeViewController.attach(zenModeView2);
        this.zenModeView = zenModeView2;
    }

    public boolean beginsSection(@NotNull View view, @Nullable View view2) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        ZenModeView zenModeView2 = this.zenModeView;
        if (view != zenModeView2 && view2 != zenModeView2) {
            return super.beginsSection(view, view2);
        }
        ZenModeView zenModeView3 = this.zenModeView;
        if (zenModeView3 != null) {
            return zenModeView3.isVisiable();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Nullable
    public Integer getBucket(@Nullable View view) {
        if (view == this.zenModeView) {
            return 7;
        }
        return super.getBucket(view);
    }

    /* access modifiers changed from: protected */
    public void logShadeChild(int i, @NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "child");
        if (view == this.zenModeView) {
            this.logger.logZenModeView(i);
        } else {
            super.logShadeChild(i, view);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:87:0x019d, code lost:
        if (r20.intValue() != r5.getBucket()) goto L_0x019f;
     */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0202  */
    /* JADX WARNING: Removed duplicated region for block: B:122:0x021b  */
    /* JADX WARNING: Removed duplicated region for block: B:124:0x022d  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x0294  */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x029d  */
    /* JADX WARNING: Removed duplicated region for block: B:168:0x02b9  */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x02be  */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x02cc  */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x02d1  */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x02df  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x02e4  */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x02f2  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x02f7  */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x0305  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x030a  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x0331 A[LOOP:2: B:200:0x032b->B:202:0x0331, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x0352  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x0366 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:231:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0147  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0174  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x01b0  */
    /* JADX WARNING: Removed duplicated region for block: B:97:0x01bc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateSectionBoundaries(@org.jetbrains.annotations.NotNull java.lang.String r27) {
        /*
            r26 = this;
            r8 = r26
            r0 = r27
            java.lang.String r1 = "reason"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r0, r1)
            boolean r1 = r26.isUsingMultipleSections()
            if (r1 != 0) goto L_0x0010
            return
        L_0x0010:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r1 = r8.logger
            r1.logStartSectionUpdate(r0)
            com.android.systemui.plugins.statusbar.StatusBarStateController r0 = r8.statusBarStateController
            int r0 = r0.getState()
            r9 = 0
            r10 = 1
            if (r0 == r10) goto L_0x0021
            r11 = r10
            goto L_0x0022
        L_0x0021:
            r11 = r9
        L_0x0022:
            com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager r0 = r8.sectionsFeatureManager
            boolean r12 = r0.isFilteringEnabled()
            com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager r0 = r8.sectionsFeatureManager
            boolean r13 = r0.isMediaControlsEnabled()
            com.android.systemui.statusbar.notification.stack.MediaHeaderView r0 = r26.getMediaControlsView()
            if (r0 == 0) goto L_0x003a
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r8.expandableViewHeaderState(r0)
            r15 = r0
            goto L_0x003b
        L_0x003a:
            r15 = 0
        L_0x003b:
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r26.getIncomingHeaderView()
            if (r0 == 0) goto L_0x0047
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r8.decorViewHeaderState(r0)
            r7 = r0
            goto L_0x0048
        L_0x0047:
            r7 = 0
        L_0x0048:
            com.android.systemui.statusbar.notification.stack.PeopleHubView r0 = r26.getPeopleHeaderView()
            if (r0 == 0) goto L_0x0054
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r8.decorViewHeaderState(r0)
            r6 = r0
            goto L_0x0055
        L_0x0054:
            r6 = 0
        L_0x0055:
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r26.getAlertingHeaderView()
            if (r0 == 0) goto L_0x0061
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r8.decorViewHeaderState(r0)
            r5 = r0
            goto L_0x0062
        L_0x0061:
            r5 = 0
        L_0x0062:
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r26.getSilentHeaderView()
            if (r0 == 0) goto L_0x006e
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r8.decorViewHeaderState(r0)
            r4 = r0
            goto L_0x006f
        L_0x006e:
            r4 = 0
        L_0x006f:
            com.android.systemui.statusbar.notification.zen.ZenModeView r0 = r8.zenModeView
            if (r0 == 0) goto L_0x0079
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r8.expandableViewHeaderState(r0)
            r3 = r0
            goto L_0x007a
        L_0x0079:
            r3 = 0
        L_0x007a:
            com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager$updateSectionBoundaries$1 r2 = new com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager$updateSectionBoundaries$1
            r0 = r2
            r1 = r26
            r14 = r2
            r2 = r15
            r16 = r3
            r3 = r7
            r17 = r4
            r4 = r6
            r18 = r5
            r19 = r6
            r6 = r17
            r20 = r7
            r7 = r16
            r0.<init>(r1, r2, r3, r4, r5, r6, r7)
            r0 = 6
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState[] r1 = new com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState[r0]
            r2 = r16
            r1[r9] = r2
            r1[r10] = r15
            r3 = 2
            r4 = r20
            r1[r3] = r4
            r5 = 3
            r6 = r19
            r1[r5] = r6
            r5 = 4
            r7 = r18
            r1[r5] = r7
            r9 = 5
            r0 = r17
            r1[r9] = r0
            kotlin.sequences.Sequence r1 = kotlin.sequences.SequencesKt__SequencesKt.sequenceOf(r1)
            kotlin.sequences.Sequence r1 = kotlin.sequences.SequencesKt___SequencesKt.filterNotNull(r1)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r17 = r26.getParent()
            int r17 = r17.getChildCount()
            int r17 = r17 + -1
            r9 = r17
            r17 = 0
            r20 = 0
            r21 = 0
            r22 = 0
        L_0x00cd:
            r5 = -1
            if (r9 < r5) goto L_0x0236
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r5 = r26.getParent()
            android.view.View r5 = r5.getChildAt(r9)
            if (r5 == 0) goto L_0x0127
            r8.logShadeChild(r9, r5)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r3 = r14.invoke((android.view.View) r5)
            if (r3 == 0) goto L_0x0127
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)
            r3.setCurrentPosition(r10)
            com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager$updateSectionBoundaries$2$1$1 r10 = new com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager$updateSectionBoundaries$2$1$1
            r10.<init>(r3)
            kotlin.sequences.Sequence r3 = com.android.systemui.util.ConvenienceExtensionsKt.takeUntil(r1, r10)
            java.util.Iterator r3 = r3.iterator()
        L_0x00f7:
            boolean r10 = r3.hasNext()
            if (r10 == 0) goto L_0x0125
            java.lang.Object r10 = r3.next()
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r10 = (com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState) r10
            java.lang.Integer r24 = r10.getTargetPosition()
            if (r24 == 0) goto L_0x011c
            int r24 = r24.intValue()
            r23 = 1
            int r24 = r24 + -1
            java.lang.Integer r24 = java.lang.Integer.valueOf(r24)
            r25 = r24
            r24 = r3
            r3 = r25
            goto L_0x011f
        L_0x011c:
            r24 = r3
            r3 = 0
        L_0x011f:
            r10.setTargetPosition(r3)
            r3 = r24
            goto L_0x00f7
        L_0x0125:
            kotlin.Unit r3 = kotlin.Unit.INSTANCE
        L_0x0127:
            boolean r3 = r5 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r3 != 0) goto L_0x012d
            r3 = 0
            goto L_0x012e
        L_0x012d:
            r3 = r5
        L_0x012e:
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r3 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r3
            if (r3 == 0) goto L_0x0144
            int r10 = r3.getVisibility()
            r24 = r3
            r3 = 8
            if (r10 != r3) goto L_0x013e
            r3 = 1
            goto L_0x013f
        L_0x013e:
            r3 = 0
        L_0x013f:
            if (r3 != 0) goto L_0x0144
            r3 = r24
            goto L_0x0145
        L_0x0144:
            r3 = 0
        L_0x0145:
            if (r17 != 0) goto L_0x0174
            if (r20 == 0) goto L_0x0165
            int r10 = r20.intValue()
            if (r3 == 0) goto L_0x0165
            com.android.systemui.statusbar.notification.collection.NotificationEntry r17 = r3.getEntry()
            if (r17 == 0) goto L_0x0165
            r24 = r14
            int r14 = r17.getBucket()
            if (r10 >= r14) goto L_0x015f
            r10 = 1
            goto L_0x0160
        L_0x015f:
            r10 = 0
        L_0x0160:
            java.lang.Boolean r10 = java.lang.Boolean.valueOf(r10)
            goto L_0x0168
        L_0x0165:
            r24 = r14
            r10 = 0
        L_0x0168:
            java.lang.Boolean r14 = java.lang.Boolean.TRUE
            boolean r10 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r10, (java.lang.Object) r14)
            if (r10 == 0) goto L_0x0171
            goto L_0x0176
        L_0x0171:
            r17 = 0
            goto L_0x0178
        L_0x0174:
            r24 = r14
        L_0x0176:
            r17 = 1
        L_0x0178:
            if (r17 == 0) goto L_0x0186
            if (r3 == 0) goto L_0x0186
            com.android.systemui.statusbar.notification.collection.NotificationEntry r10 = r3.getEntry()
            if (r10 == 0) goto L_0x0186
            r14 = 2
            r10.setBucket(r14)
        L_0x0186:
            java.lang.String r10 = "row.entry"
            if (r20 == 0) goto L_0x01a1
            if (r5 == 0) goto L_0x019f
            if (r3 == 0) goto L_0x01a1
            com.android.systemui.statusbar.notification.collection.NotificationEntry r5 = r3.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r10)
            int r5 = r5.getBucket()
            int r14 = r20.intValue()
            if (r14 == r5) goto L_0x01a1
        L_0x019f:
            r5 = 1
            goto L_0x01a2
        L_0x01a1:
            r5 = 0
        L_0x01a2:
            if (r5 == 0) goto L_0x01ff
            if (r11 == 0) goto L_0x01ff
            if (r20 != 0) goto L_0x01a9
            goto L_0x01bc
        L_0x01a9:
            int r5 = r20.intValue()
            r14 = 2
            if (r5 != r14) goto L_0x01bc
            if (r4 == 0) goto L_0x01ff
            int r5 = r9 + 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r4.setTargetPosition(r5)
            goto L_0x01ff
        L_0x01bc:
            if (r20 != 0) goto L_0x01bf
            goto L_0x01d2
        L_0x01bf:
            int r5 = r20.intValue()
            r14 = 4
            if (r5 != r14) goto L_0x01d2
            if (r6 == 0) goto L_0x01ff
            int r5 = r9 + 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r6.setTargetPosition(r5)
            goto L_0x01ff
        L_0x01d2:
            if (r20 != 0) goto L_0x01d6
            r14 = 5
            goto L_0x01e9
        L_0x01d6:
            int r5 = r20.intValue()
            r14 = 5
            if (r5 != r14) goto L_0x01e9
            if (r7 == 0) goto L_0x01ff
            int r5 = r9 + 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r7.setTargetPosition(r5)
            goto L_0x01ff
        L_0x01e9:
            if (r20 != 0) goto L_0x01ec
            goto L_0x01ff
        L_0x01ec:
            int r5 = r20.intValue()
            r14 = 6
            if (r5 != r14) goto L_0x0200
            if (r0 == 0) goto L_0x0200
            int r5 = r9 + 1
            java.lang.Integer r5 = java.lang.Integer.valueOf(r5)
            r0.setTargetPosition(r5)
            goto L_0x0200
        L_0x01ff:
            r14 = 6
        L_0x0200:
            if (r3 == 0) goto L_0x022d
            if (r22 != 0) goto L_0x0216
            com.android.systemui.statusbar.notification.collection.NotificationEntry r5 = r3.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r10)
            int r5 = r5.getBucket()
            r14 = 4
            if (r5 != r14) goto L_0x0213
            goto L_0x0217
        L_0x0213:
            r22 = 0
            goto L_0x0219
        L_0x0216:
            r14 = 4
        L_0x0217:
            r22 = 1
        L_0x0219:
            if (r20 != 0) goto L_0x021d
            r21 = r9
        L_0x021d:
            com.android.systemui.statusbar.notification.collection.NotificationEntry r3 = r3.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r3, r10)
            int r3 = r3.getBucket()
            java.lang.Integer r20 = java.lang.Integer.valueOf(r3)
            goto L_0x022e
        L_0x022d:
            r14 = 4
        L_0x022e:
            int r9 = r9 + -1
            r14 = r24
            r3 = 2
            r10 = 1
            goto L_0x00cd
        L_0x0236:
            if (r11 == 0) goto L_0x0290
            if (r12 == 0) goto L_0x0290
            boolean r3 = r26.getPeopleHubVisible()
            if (r3 == 0) goto L_0x0290
            if (r6 == 0) goto L_0x0269
            java.lang.Integer r3 = r6.getTargetPosition()
            if (r3 == 0) goto L_0x0249
            goto L_0x0251
        L_0x0249:
            if (r7 == 0) goto L_0x0250
            java.lang.Integer r3 = r7.getTargetPosition()
            goto L_0x0251
        L_0x0250:
            r3 = 0
        L_0x0251:
            if (r3 == 0) goto L_0x0254
            goto L_0x025c
        L_0x0254:
            if (r0 == 0) goto L_0x025b
            java.lang.Integer r3 = r0.getTargetPosition()
            goto L_0x025c
        L_0x025b:
            r3 = 0
        L_0x025c:
            if (r3 == 0) goto L_0x0262
            int r21 = r3.intValue()
        L_0x0262:
            java.lang.Integer r3 = java.lang.Integer.valueOf(r21)
            r6.setTargetPosition(r3)
        L_0x0269:
            if (r6 == 0) goto L_0x0290
            java.lang.Integer r3 = r6.getCurrentPosition()
            if (r3 == 0) goto L_0x028a
            int r3 = r3.intValue()
            java.lang.Integer r9 = r6.getTargetPosition()
            if (r9 == 0) goto L_0x028a
            int r9 = r9.intValue()
            if (r3 >= r9) goto L_0x0284
            r3 = 1
            int r9 = r9 - r3
            goto L_0x0285
        L_0x0284:
            r3 = 1
        L_0x0285:
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            goto L_0x028c
        L_0x028a:
            r3 = 1
            r9 = 0
        L_0x028c:
            r6.setTargetPosition(r9)
            goto L_0x0291
        L_0x0290:
            r3 = 1
        L_0x0291:
            r9 = 0
            if (r2 == 0) goto L_0x029b
            java.lang.Integer r10 = java.lang.Integer.valueOf(r9)
            r2.setTargetPosition(r10)
        L_0x029b:
            if (r15 == 0) goto L_0x02a8
            if (r13 == 0) goto L_0x02a4
            java.lang.Integer r14 = java.lang.Integer.valueOf(r9)
            goto L_0x02a5
        L_0x02a4:
            r14 = 0
        L_0x02a5:
            r15.setTargetPosition(r14)
        L_0x02a8:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r10 = r8.logger
            java.lang.String r12 = "New header target positions:"
            r10.logStr(r12)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r10 = r8.logger
            if (r2 == 0) goto L_0x02be
            java.lang.Integer r2 = r2.getTargetPosition()
            if (r2 == 0) goto L_0x02be
            int r2 = r2.intValue()
            goto L_0x02bf
        L_0x02be:
            r2 = r5
        L_0x02bf:
            r10.logZenModeView(r2)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r2 = r8.logger
            if (r15 == 0) goto L_0x02d1
            java.lang.Integer r10 = r15.getTargetPosition()
            if (r10 == 0) goto L_0x02d1
            int r10 = r10.intValue()
            goto L_0x02d2
        L_0x02d1:
            r10 = r5
        L_0x02d2:
            r2.logMediaControls(r10)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r2 = r8.logger
            if (r4 == 0) goto L_0x02e4
            java.lang.Integer r4 = r4.getTargetPosition()
            if (r4 == 0) goto L_0x02e4
            int r4 = r4.intValue()
            goto L_0x02e5
        L_0x02e4:
            r4 = r5
        L_0x02e5:
            r2.logIncomingHeader(r4)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r2 = r8.logger
            if (r6 == 0) goto L_0x02f7
            java.lang.Integer r4 = r6.getTargetPosition()
            if (r4 == 0) goto L_0x02f7
            int r4 = r4.intValue()
            goto L_0x02f8
        L_0x02f7:
            r4 = r5
        L_0x02f8:
            r2.logConversationsHeader(r4)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r2 = r8.logger
            if (r7 == 0) goto L_0x030a
            java.lang.Integer r4 = r7.getTargetPosition()
            if (r4 == 0) goto L_0x030a
            int r4 = r4.intValue()
            goto L_0x030b
        L_0x030a:
            r4 = r5
        L_0x030b:
            r2.logAlertingHeader(r4)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r2 = r8.logger
            if (r0 == 0) goto L_0x031c
            java.lang.Integer r0 = r0.getTargetPosition()
            if (r0 == 0) goto L_0x031c
            int r5 = r0.intValue()
        L_0x031c:
            r2.logSilentHeader(r5)
            java.lang.Iterable r0 = kotlin.sequences.SequencesKt___SequencesKt.asIterable(r1)
            java.util.List r0 = kotlin.collections.CollectionsKt___CollectionsKt.reversed(r0)
            java.util.Iterator r0 = r0.iterator()
        L_0x032b:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x033b
            java.lang.Object r1 = r0.next()
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r1 = (com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState) r1
            r1.adjustViewPosition()
            goto L_0x032b
        L_0x033b:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r0 = r8.logger
            java.lang.String r1 = "Final order:"
            r0.logStr(r1)
            r26.logShadeContents()
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r0 = r8.logger
            java.lang.String r1 = "Section boundary update complete"
            r0.logStr(r1)
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r26.getSilentHeaderView()
            if (r0 == 0) goto L_0x0360
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r1 = r26.getParent()
            r2 = 2
            boolean r1 = r1.hasActiveClearableNotifications(r2)
            r0.setAreThereDismissableGentleNotifs(r1)
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
        L_0x0360:
            com.android.systemui.statusbar.notification.stack.PeopleHubView r0 = r26.getPeopleHeaderView()
            if (r0 == 0) goto L_0x0392
            if (r11 == 0) goto L_0x0371
            boolean r1 = r26.getPeopleHubVisible()
            if (r1 == 0) goto L_0x0371
            if (r22 != 0) goto L_0x0371
            r9 = r3
        L_0x0371:
            r0.setCanSwipe(r9)
            if (r6 == 0) goto L_0x0392
            java.lang.Integer r1 = r6.getTargetPosition()
            if (r1 == 0) goto L_0x0392
            int r1 = r1.intValue()
            java.lang.Integer r2 = r6.getCurrentPosition()
            if (r2 != 0) goto L_0x0387
            goto L_0x038d
        L_0x0387:
            int r2 = r2.intValue()
            if (r1 == r2) goto L_0x0390
        L_0x038d:
            r0.resetTranslation()
        L_0x0390:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
        L_0x0392:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager.updateSectionBoundaries(java.lang.String):void");
    }
}
