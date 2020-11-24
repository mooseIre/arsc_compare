package com.android.systemui.statusbar.notification.stack;

import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.people.PeopleHubViewAdapter;
import com.android.systemui.statusbar.notification.people.Subscription;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.ConvenienceExtensionsKt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.NoWhenBranchMatchedException;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationSectionsManager.kt */
public class NotificationSectionsManager implements StackScrollAlgorithm.SectionProvider {
    private final ActivityStarter activityStarter;
    @Nullable
    private SectionHeaderView alertingHeaderView;
    private final ConfigurationController configurationController;
    private final NotificationSectionsManager$configurationListener$1 configurationListener = new NotificationSectionsManager$configurationListener$1(this);
    @Nullable
    private SectionHeaderView incomingHeaderView;
    private boolean initialized;
    private final KeyguardMediaController keyguardMediaController;
    private final NotificationSectionsLogger logger;
    @Nullable
    private MediaHeaderView mediaControlsView;
    private View.OnClickListener onClearSilentNotifsClickListener;
    @NotNull
    protected NotificationStackScrollLayout parent;
    @Nullable
    private PeopleHubView peopleHeaderView;
    private Subscription peopleHubSubscription;
    private boolean peopleHubVisible;
    private final NotificationSectionsFeatureManager sectionsFeatureManager;
    @Nullable
    private SectionHeaderView silentHeaderView;
    private final StatusBarStateController statusBarStateController;

    /* compiled from: NotificationSectionsManager.kt */
    protected interface SectionUpdateState<T extends ExpandableView> {
        void adjustViewPosition();

        @Nullable
        Integer getCurrentPosition();

        @Nullable
        Integer getTargetPosition();

        void setCurrentPosition(@Nullable Integer num);

        void setTargetPosition(@Nullable Integer num);
    }

    public NotificationSectionsManager(@NotNull ActivityStarter activityStarter2, @NotNull StatusBarStateController statusBarStateController2, @NotNull ConfigurationController configurationController2, @NotNull PeopleHubViewAdapter peopleHubViewAdapter, @NotNull KeyguardMediaController keyguardMediaController2, @NotNull NotificationSectionsFeatureManager notificationSectionsFeatureManager, @NotNull NotificationSectionsLogger notificationSectionsLogger) {
        Intrinsics.checkParameterIsNotNull(activityStarter2, "activityStarter");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(configurationController2, "configurationController");
        Intrinsics.checkParameterIsNotNull(peopleHubViewAdapter, "peopleHubViewAdapter");
        Intrinsics.checkParameterIsNotNull(keyguardMediaController2, "keyguardMediaController");
        Intrinsics.checkParameterIsNotNull(notificationSectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(notificationSectionsLogger, "logger");
        this.activityStarter = activityStarter2;
        this.statusBarStateController = statusBarStateController2;
        this.configurationController = configurationController2;
        this.keyguardMediaController = keyguardMediaController2;
        this.sectionsFeatureManager = notificationSectionsFeatureManager;
        this.logger = notificationSectionsLogger;
    }

    /* access modifiers changed from: protected */
    @NotNull
    public final NotificationStackScrollLayout getParent() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
        if (notificationStackScrollLayout != null) {
            return notificationStackScrollLayout;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getSilentHeaderView() {
        return this.silentHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getAlertingHeaderView() {
        return this.alertingHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final SectionHeaderView getIncomingHeaderView() {
        return this.incomingHeaderView;
    }

    @VisibleForTesting
    @Nullable
    public final PeopleHubView getPeopleHeaderView() {
        return this.peopleHeaderView;
    }

    public final boolean getPeopleHubVisible() {
        return this.peopleHubVisible;
    }

    @VisibleForTesting
    public final void setPeopleHubVisible(boolean z) {
        this.peopleHubVisible = z;
    }

    @VisibleForTesting
    @Nullable
    public final MediaHeaderView getMediaControlsView() {
        return this.mediaControlsView;
    }

    public final void initialize(@NotNull NotificationStackScrollLayout notificationStackScrollLayout, @NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "parent");
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        if (!this.initialized) {
            this.initialized = true;
            this.parent = notificationStackScrollLayout;
            reinflateViews(layoutInflater);
            this.configurationController.addCallback(this.configurationListener);
            return;
        }
        throw new IllegalStateException("NotificationSectionsManager already initialized".toString());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x005d  */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final <T extends com.android.systemui.statusbar.notification.row.ExpandableView> T reinflateView(@org.jetbrains.annotations.Nullable T r6, @org.jetbrains.annotations.NotNull android.view.LayoutInflater r7, int r8) {
        /*
            r5 = this;
            java.lang.String r0 = "layoutInflater"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r7, r0)
            r0 = -1
            r1 = 0
            java.lang.String r2 = "parent"
            if (r6 == 0) goto L_0x0038
            android.view.ViewGroup r3 = r6.getTransientContainer()
            if (r3 == 0) goto L_0x0014
            r3.removeView(r6)
        L_0x0014:
            android.view.ViewParent r3 = r6.getParent()
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r5.parent
            if (r4 == 0) goto L_0x0034
            if (r3 != r4) goto L_0x0038
            if (r4 == 0) goto L_0x0030
            int r3 = r4.indexOfChild(r6)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r5.parent
            if (r4 == 0) goto L_0x002c
            r4.removeView(r6)
            goto L_0x0039
        L_0x002c:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x0030:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x0034:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x0038:
            r3 = r0
        L_0x0039:
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r6 = r5.parent
            if (r6 == 0) goto L_0x005d
            r4 = 0
            android.view.View r6 = r7.inflate(r8, r6, r4)
            if (r6 == 0) goto L_0x0055
            com.android.systemui.statusbar.notification.row.ExpandableView r6 = (com.android.systemui.statusbar.notification.row.ExpandableView) r6
            if (r3 == r0) goto L_0x0054
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r5 = r5.parent
            if (r5 == 0) goto L_0x0050
            r5.addView(r6, r3)
            goto L_0x0054
        L_0x0050:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x0054:
            return r6
        L_0x0055:
            kotlin.TypeCastException r5 = new kotlin.TypeCastException
            java.lang.String r6 = "null cannot be cast to non-null type T"
            r5.<init>(r6)
            throw r5
        L_0x005d:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.reinflateView(com.android.systemui.statusbar.notification.row.ExpandableView, android.view.LayoutInflater, int):com.android.systemui.statusbar.notification.row.ExpandableView");
    }

    @NotNull
    public final NotificationSection[] createSectionsForBuckets() {
        int[] notificationBuckets = this.sectionsFeatureManager.getNotificationBuckets();
        ArrayList arrayList = new ArrayList(notificationBuckets.length);
        int length = notificationBuckets.length;
        int i = 0;
        while (i < length) {
            int i2 = notificationBuckets[i];
            NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
            if (notificationStackScrollLayout != null) {
                arrayList.add(new NotificationSection(notificationStackScrollLayout, i2));
                i++;
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("parent");
                throw null;
            }
        }
        Object[] array = arrayList.toArray(new NotificationSection[0]);
        if (array != null) {
            return (NotificationSection[]) array;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<T>");
    }

    public void reinflateViews(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "layoutInflater");
        SectionHeaderView sectionHeaderView = (SectionHeaderView) reinflateView(this.silentHeaderView, layoutInflater, C0014R$layout.status_bar_notification_section_header);
        sectionHeaderView.setHeaderText(C0018R$string.notification_section_header_gentle);
        sectionHeaderView.setOnHeaderClickListener(new NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$1(this));
        sectionHeaderView.setOnClearAllClickListener(new NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$2(this));
        this.silentHeaderView = sectionHeaderView;
        SectionHeaderView sectionHeaderView2 = (SectionHeaderView) reinflateView(this.alertingHeaderView, layoutInflater, C0014R$layout.status_bar_notification_section_header);
        sectionHeaderView2.setHeaderText(C0018R$string.notification_section_header_alerting);
        sectionHeaderView2.setOnHeaderClickListener(new NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$3(this));
        this.alertingHeaderView = sectionHeaderView2;
        Subscription subscription = this.peopleHubSubscription;
        if (subscription != null) {
            subscription.unsubscribe();
        }
        this.peopleHubSubscription = null;
        this.peopleHeaderView = (PeopleHubView) reinflateView(this.peopleHeaderView, layoutInflater, C0014R$layout.people_strip);
        SectionHeaderView sectionHeaderView3 = (SectionHeaderView) reinflateView(this.incomingHeaderView, layoutInflater, C0014R$layout.status_bar_notification_section_header);
        sectionHeaderView3.setHeaderText(C0018R$string.notification_section_header_incoming);
        sectionHeaderView3.setOnHeaderClickListener(new NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$4(this));
        this.incomingHeaderView = sectionHeaderView3;
        MediaHeaderView mediaHeaderView = (MediaHeaderView) reinflateView(this.mediaControlsView, layoutInflater, C0014R$layout.keyguard_media_header);
        this.keyguardMediaController.attach(mediaHeaderView);
        this.mediaControlsView = mediaHeaderView;
    }

    public boolean beginsSection(@NotNull View view, @Nullable View view2) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        return view == this.silentHeaderView || view == this.mediaControlsView || view == this.peopleHeaderView || view == this.alertingHeaderView || view == this.incomingHeaderView || (Intrinsics.areEqual((Object) getBucket(view), (Object) getBucket(view2)) ^ true);
    }

    /* access modifiers changed from: protected */
    @Nullable
    public Integer getBucket(@Nullable View view) {
        if (view == this.silentHeaderView) {
            return 6;
        }
        if (view == this.incomingHeaderView) {
            return 2;
        }
        if (view == this.mediaControlsView) {
            return 1;
        }
        if (view == this.peopleHeaderView) {
            return 4;
        }
        if (view == this.alertingHeaderView) {
            return 5;
        }
        if (!(view instanceof ExpandableNotificationRow)) {
            return null;
        }
        NotificationEntry entry = ((ExpandableNotificationRow) view).getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry, "view.entry");
        return Integer.valueOf(entry.getBucket());
    }

    /* access modifiers changed from: protected */
    public void logShadeChild(int i, @NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "child");
        if (view == this.incomingHeaderView) {
            this.logger.logIncomingHeader(i);
        } else if (view == this.mediaControlsView) {
            this.logger.logMediaControls(i);
        } else if (view == this.peopleHeaderView) {
            this.logger.logConversationsHeader(i);
        } else if (view == this.alertingHeaderView) {
            this.logger.logAlertingHeader(i);
        } else if (view == this.silentHeaderView) {
            this.logger.logSilentHeader(i);
        } else if (!(view instanceof ExpandableNotificationRow)) {
            this.logger.logOther(i, view.getClass());
        } else {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            boolean isHeadsUp = expandableNotificationRow.isHeadsUp();
            NotificationEntry entry = expandableNotificationRow.getEntry();
            Intrinsics.checkExpressionValueIsNotNull(entry, "child.entry");
            int bucket = entry.getBucket();
            if (bucket == 2) {
                this.logger.logHeadsUp(i, isHeadsUp);
            } else if (bucket == 4) {
                this.logger.logConversation(i, isHeadsUp);
            } else if (bucket == 5) {
                this.logger.logAlerting(i, isHeadsUp);
            } else if (bucket == 6) {
                this.logger.logSilent(i, isHeadsUp);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void logShadeContents() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.parent;
        if (notificationStackScrollLayout != null) {
            int i = 0;
            for (View next : ConvenienceExtensionsKt.getChildren(notificationStackScrollLayout)) {
                int i2 = i + 1;
                if (i >= 0) {
                    logShadeChild(i, next);
                    i = i2;
                } else {
                    CollectionsKt.throwIndexOverflow();
                    throw null;
                }
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    /* access modifiers changed from: protected */
    public final boolean isUsingMultipleSections() {
        return this.sectionsFeatureManager.getNumberOfBuckets() > 1;
    }

    @VisibleForTesting
    public final void updateSectionBoundaries() {
        updateSectionBoundaries("test");
    }

    /* access modifiers changed from: protected */
    @NotNull
    public final <T extends ExpandableView> SectionUpdateState<T> expandableViewHeaderState(@NotNull T t) {
        Intrinsics.checkParameterIsNotNull(t, "header");
        return new NotificationSectionsManager$expandableViewHeaderState$1(this, t);
    }

    /* access modifiers changed from: protected */
    @NotNull
    public final <T extends StackScrollerDecorView> SectionUpdateState<T> decorViewHeaderState(@NotNull T t) {
        Intrinsics.checkParameterIsNotNull(t, "header");
        return new NotificationSectionsManager$decorViewHeaderState$1(expandableViewHeaderState(t), t);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0118, code lost:
        if ((r2.getVisibility() == 8) == false) goto L_0x011d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:85:0x016f, code lost:
        if (r8.intValue() != r4.getBucket()) goto L_0x0171;
     */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x01eb  */
    /* JADX WARNING: Removed duplicated region for block: B:158:0x0266  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x0282  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x0287  */
    /* JADX WARNING: Removed duplicated region for block: B:172:0x0295  */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x02a8  */
    /* JADX WARNING: Removed duplicated region for block: B:179:0x02ad  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x02bb  */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x02c0  */
    /* JADX WARNING: Removed duplicated region for block: B:194:0x02e7 A[LOOP:2: B:192:0x02e1->B:194:0x02e7, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x0306  */
    /* JADX WARNING: Removed duplicated region for block: B:204:0x031e  */
    /* JADX WARNING: Removed duplicated region for block: B:231:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0182  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x018e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateSectionBoundaries(@org.jetbrains.annotations.NotNull java.lang.String r26) {
        /*
            r25 = this;
            r7 = r25
            r0 = r26
            java.lang.String r1 = "reason"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r0, r1)
            boolean r1 = r25.isUsingMultipleSections()
            if (r1 != 0) goto L_0x0010
            return
        L_0x0010:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r1 = r7.logger
            r1.logStartSectionUpdate(r0)
            com.android.systemui.plugins.statusbar.StatusBarStateController r0 = r7.statusBarStateController
            int r0 = r0.getState()
            r8 = 0
            r9 = 1
            if (r0 == r9) goto L_0x0021
            r10 = r9
            goto L_0x0022
        L_0x0021:
            r10 = r8
        L_0x0022:
            com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager r0 = r7.sectionsFeatureManager
            boolean r11 = r0.isFilteringEnabled()
            com.android.systemui.statusbar.notification.NotificationSectionsFeatureManager r0 = r7.sectionsFeatureManager
            boolean r12 = r0.isMediaControlsEnabled()
            com.android.systemui.statusbar.notification.stack.MediaHeaderView r0 = r7.mediaControlsView
            if (r0 == 0) goto L_0x0038
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r7.expandableViewHeaderState(r0)
            r14 = r0
            goto L_0x0039
        L_0x0038:
            r14 = 0
        L_0x0039:
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r7.incomingHeaderView
            if (r0 == 0) goto L_0x0043
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r7.decorViewHeaderState(r0)
            r15 = r0
            goto L_0x0044
        L_0x0043:
            r15 = 0
        L_0x0044:
            com.android.systemui.statusbar.notification.stack.PeopleHubView r0 = r7.peopleHeaderView
            if (r0 == 0) goto L_0x004e
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r7.decorViewHeaderState(r0)
            r6 = r0
            goto L_0x004f
        L_0x004e:
            r6 = 0
        L_0x004f:
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r7.alertingHeaderView
            if (r0 == 0) goto L_0x0059
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r7.decorViewHeaderState(r0)
            r5 = r0
            goto L_0x005a
        L_0x0059:
            r5 = 0
        L_0x005a:
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r0 = r7.silentHeaderView
            if (r0 == 0) goto L_0x0064
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r0 = r7.decorViewHeaderState(r0)
            r4 = r0
            goto L_0x0065
        L_0x0064:
            r4 = 0
        L_0x0065:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$updateSectionBoundaries$1 r3 = new com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$updateSectionBoundaries$1
            r0 = r3
            r1 = r25
            r2 = r14
            r13 = r3
            r3 = r15
            r16 = r4
            r4 = r6
            r17 = r5
            r18 = r6
            r6 = r16
            r0.<init>(r1, r2, r3, r4, r5, r6)
            r0 = 5
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState[] r1 = new com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState[r0]
            r1[r8] = r14
            r1[r9] = r15
            r2 = 2
            r3 = r18
            r1[r2] = r3
            r4 = 3
            r1[r4] = r5
            r4 = 4
            r1[r4] = r6
            kotlin.sequences.Sequence r1 = kotlin.sequences.SequencesKt__SequencesKt.sequenceOf(r1)
            kotlin.sequences.Sequence r1 = kotlin.sequences.SequencesKt___SequencesKt.filterNotNull(r1)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r8 = r7.parent
            java.lang.String r17 = "parent"
            if (r8 == 0) goto L_0x034b
            int r8 = r8.getChildCount()
            int r8 = r8 - r9
            r0 = r8
            r8 = 0
            r19 = 0
            r20 = 0
            r21 = 0
        L_0x00a6:
            r4 = -1
            if (r0 < r4) goto L_0x020a
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r7.parent
            if (r4 == 0) goto L_0x0205
            android.view.View r4 = r4.getChildAt(r0)
            if (r4 == 0) goto L_0x0100
            r7.logShadeChild(r0, r4)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r2 = r13.invoke((android.view.View) r4)
            if (r2 == 0) goto L_0x0100
            java.lang.Integer r9 = java.lang.Integer.valueOf(r0)
            r2.setCurrentPosition(r9)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$updateSectionBoundaries$2$1$1 r9 = new com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$updateSectionBoundaries$2$1$1
            r9.<init>(r2)
            kotlin.sequences.Sequence r2 = com.android.systemui.util.ConvenienceExtensionsKt.takeUntil(r1, r9)
            java.util.Iterator r2 = r2.iterator()
        L_0x00d0:
            boolean r9 = r2.hasNext()
            if (r9 == 0) goto L_0x00fe
            java.lang.Object r9 = r2.next()
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r9 = (com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState) r9
            java.lang.Integer r23 = r9.getTargetPosition()
            if (r23 == 0) goto L_0x00f5
            int r23 = r23.intValue()
            r22 = 1
            int r23 = r23 + -1
            java.lang.Integer r23 = java.lang.Integer.valueOf(r23)
            r24 = r23
            r23 = r2
            r2 = r24
            goto L_0x00f8
        L_0x00f5:
            r23 = r2
            r2 = 0
        L_0x00f8:
            r9.setTargetPosition(r2)
            r2 = r23
            goto L_0x00d0
        L_0x00fe:
            kotlin.Unit r2 = kotlin.Unit.INSTANCE
        L_0x0100:
            boolean r2 = r4 instanceof com.android.systemui.statusbar.notification.row.ExpandableNotificationRow
            if (r2 != 0) goto L_0x0106
            r2 = 0
            goto L_0x0107
        L_0x0106:
            r2 = r4
        L_0x0107:
            com.android.systemui.statusbar.notification.row.ExpandableNotificationRow r2 = (com.android.systemui.statusbar.notification.row.ExpandableNotificationRow) r2
            if (r2 == 0) goto L_0x011b
            int r9 = r2.getVisibility()
            r23 = r2
            r2 = 8
            if (r9 != r2) goto L_0x0117
            r2 = 1
            goto L_0x0118
        L_0x0117:
            r2 = 0
        L_0x0118:
            if (r2 != 0) goto L_0x011b
            goto L_0x011d
        L_0x011b:
            r23 = 0
        L_0x011d:
            if (r20 != 0) goto L_0x0148
            if (r8 == 0) goto L_0x013b
            int r2 = r8.intValue()
            if (r23 == 0) goto L_0x013b
            com.android.systemui.statusbar.notification.collection.NotificationEntry r9 = r23.getEntry()
            if (r9 == 0) goto L_0x013b
            int r9 = r9.getBucket()
            if (r2 >= r9) goto L_0x0135
            r2 = 1
            goto L_0x0136
        L_0x0135:
            r2 = 0
        L_0x0136:
            java.lang.Boolean r2 = java.lang.Boolean.valueOf(r2)
            goto L_0x013c
        L_0x013b:
            r2 = 0
        L_0x013c:
            java.lang.Boolean r9 = java.lang.Boolean.TRUE
            boolean r2 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r2, (java.lang.Object) r9)
            if (r2 == 0) goto L_0x0145
            goto L_0x0148
        L_0x0145:
            r20 = 0
            goto L_0x014a
        L_0x0148:
            r20 = 1
        L_0x014a:
            if (r20 == 0) goto L_0x0158
            if (r23 == 0) goto L_0x0158
            com.android.systemui.statusbar.notification.collection.NotificationEntry r2 = r23.getEntry()
            if (r2 == 0) goto L_0x0158
            r9 = 2
            r2.setBucket(r9)
        L_0x0158:
            java.lang.String r2 = "row.entry"
            if (r8 == 0) goto L_0x0173
            if (r4 == 0) goto L_0x0171
            if (r23 == 0) goto L_0x0173
            com.android.systemui.statusbar.notification.collection.NotificationEntry r4 = r23.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r4, r2)
            int r4 = r4.getBucket()
            int r9 = r8.intValue()
            if (r9 == r4) goto L_0x0173
        L_0x0171:
            r4 = 1
            goto L_0x0174
        L_0x0173:
            r4 = 0
        L_0x0174:
            if (r4 == 0) goto L_0x01d0
            if (r10 == 0) goto L_0x01d0
            if (r8 != 0) goto L_0x017b
            goto L_0x018e
        L_0x017b:
            int r4 = r8.intValue()
            r9 = 2
            if (r4 != r9) goto L_0x018e
            if (r15 == 0) goto L_0x01d0
            int r4 = r0 + 1
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r15.setTargetPosition(r4)
            goto L_0x01d0
        L_0x018e:
            if (r8 != 0) goto L_0x0191
            goto L_0x01a4
        L_0x0191:
            int r4 = r8.intValue()
            r9 = 4
            if (r4 != r9) goto L_0x01a4
            if (r3 == 0) goto L_0x01d0
            int r4 = r0 + 1
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r3.setTargetPosition(r4)
            goto L_0x01d0
        L_0x01a4:
            if (r8 != 0) goto L_0x01a8
            r9 = 5
            goto L_0x01bb
        L_0x01a8:
            int r4 = r8.intValue()
            r9 = 5
            if (r4 != r9) goto L_0x01bb
            if (r5 == 0) goto L_0x01d0
            int r4 = r0 + 1
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r5.setTargetPosition(r4)
            goto L_0x01d0
        L_0x01bb:
            r4 = 6
            if (r8 != 0) goto L_0x01bf
            goto L_0x01d0
        L_0x01bf:
            int r9 = r8.intValue()
            if (r9 != r4) goto L_0x01d0
            if (r6 == 0) goto L_0x01d0
            int r4 = r0 + 1
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)
            r6.setTargetPosition(r4)
        L_0x01d0:
            if (r23 == 0) goto L_0x01fe
            if (r21 != 0) goto L_0x01e6
            com.android.systemui.statusbar.notification.collection.NotificationEntry r4 = r23.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r4, r2)
            int r4 = r4.getBucket()
            r9 = 4
            if (r4 != r9) goto L_0x01e3
            goto L_0x01e7
        L_0x01e3:
            r21 = 0
            goto L_0x01e9
        L_0x01e6:
            r9 = 4
        L_0x01e7:
            r21 = 1
        L_0x01e9:
            if (r8 != 0) goto L_0x01ed
            r19 = r0
        L_0x01ed:
            com.android.systemui.statusbar.notification.collection.NotificationEntry r4 = r23.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r4, r2)
            int r2 = r4.getBucket()
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            r8 = r2
            goto L_0x01ff
        L_0x01fe:
            r9 = 4
        L_0x01ff:
            int r0 = r0 + -1
            r2 = 2
            r9 = 1
            goto L_0x00a6
        L_0x0205:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r17)
            r0 = 0
            throw r0
        L_0x020a:
            if (r10 == 0) goto L_0x0262
            if (r11 == 0) goto L_0x0262
            boolean r0 = r7.peopleHubVisible
            if (r0 == 0) goto L_0x0262
            if (r3 == 0) goto L_0x023b
            java.lang.Integer r0 = r3.getTargetPosition()
            if (r0 == 0) goto L_0x021b
            goto L_0x0223
        L_0x021b:
            if (r5 == 0) goto L_0x0222
            java.lang.Integer r0 = r5.getTargetPosition()
            goto L_0x0223
        L_0x0222:
            r0 = 0
        L_0x0223:
            if (r0 == 0) goto L_0x0226
            goto L_0x022e
        L_0x0226:
            if (r6 == 0) goto L_0x022d
            java.lang.Integer r0 = r6.getTargetPosition()
            goto L_0x022e
        L_0x022d:
            r0 = 0
        L_0x022e:
            if (r0 == 0) goto L_0x0234
            int r19 = r0.intValue()
        L_0x0234:
            java.lang.Integer r0 = java.lang.Integer.valueOf(r19)
            r3.setTargetPosition(r0)
        L_0x023b:
            if (r3 == 0) goto L_0x0262
            java.lang.Integer r0 = r3.getCurrentPosition()
            if (r0 == 0) goto L_0x025c
            int r0 = r0.intValue()
            java.lang.Integer r2 = r3.getTargetPosition()
            if (r2 == 0) goto L_0x025c
            int r2 = r2.intValue()
            if (r0 >= r2) goto L_0x0256
            r0 = 1
            int r2 = r2 - r0
            goto L_0x0257
        L_0x0256:
            r0 = 1
        L_0x0257:
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)
            goto L_0x025e
        L_0x025c:
            r0 = 1
            r2 = 0
        L_0x025e:
            r3.setTargetPosition(r2)
            goto L_0x0263
        L_0x0262:
            r0 = 1
        L_0x0263:
            r2 = 0
            if (r14 == 0) goto L_0x0271
            if (r12 == 0) goto L_0x026d
            java.lang.Integer r8 = java.lang.Integer.valueOf(r2)
            goto L_0x026e
        L_0x026d:
            r8 = 0
        L_0x026e:
            r14.setTargetPosition(r8)
        L_0x0271:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r8 = r7.logger
            java.lang.String r9 = "New header target positions:"
            r8.logStr(r9)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r8 = r7.logger
            if (r14 == 0) goto L_0x0287
            java.lang.Integer r9 = r14.getTargetPosition()
            if (r9 == 0) goto L_0x0287
            int r9 = r9.intValue()
            goto L_0x0288
        L_0x0287:
            r9 = r4
        L_0x0288:
            r8.logMediaControls(r9)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r8 = r7.logger
            if (r15 == 0) goto L_0x029a
            java.lang.Integer r9 = r15.getTargetPosition()
            if (r9 == 0) goto L_0x029a
            int r9 = r9.intValue()
            goto L_0x029b
        L_0x029a:
            r9 = r4
        L_0x029b:
            r8.logIncomingHeader(r9)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r8 = r7.logger
            if (r3 == 0) goto L_0x02ad
            java.lang.Integer r9 = r3.getTargetPosition()
            if (r9 == 0) goto L_0x02ad
            int r9 = r9.intValue()
            goto L_0x02ae
        L_0x02ad:
            r9 = r4
        L_0x02ae:
            r8.logConversationsHeader(r9)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r8 = r7.logger
            if (r5 == 0) goto L_0x02c0
            java.lang.Integer r5 = r5.getTargetPosition()
            if (r5 == 0) goto L_0x02c0
            int r5 = r5.intValue()
            goto L_0x02c1
        L_0x02c0:
            r5 = r4
        L_0x02c1:
            r8.logAlertingHeader(r5)
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r5 = r7.logger
            if (r6 == 0) goto L_0x02d2
            java.lang.Integer r6 = r6.getTargetPosition()
            if (r6 == 0) goto L_0x02d2
            int r4 = r6.intValue()
        L_0x02d2:
            r5.logSilentHeader(r4)
            java.lang.Iterable r1 = kotlin.sequences.SequencesKt___SequencesKt.asIterable(r1)
            java.util.List r1 = kotlin.collections.CollectionsKt___CollectionsKt.reversed(r1)
            java.util.Iterator r1 = r1.iterator()
        L_0x02e1:
            boolean r4 = r1.hasNext()
            if (r4 == 0) goto L_0x02f1
            java.lang.Object r4 = r1.next()
            com.android.systemui.statusbar.notification.stack.NotificationSectionsManager$SectionUpdateState r4 = (com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.SectionUpdateState) r4
            r4.adjustViewPosition()
            goto L_0x02e1
        L_0x02f1:
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r1 = r7.logger
            java.lang.String r4 = "Final order:"
            r1.logStr(r4)
            r25.logShadeContents()
            com.android.systemui.statusbar.notification.stack.NotificationSectionsLogger r1 = r7.logger
            java.lang.String r4 = "Section boundary update complete"
            r1.logStr(r4)
            com.android.systemui.statusbar.notification.stack.SectionHeaderView r1 = r7.silentHeaderView
            if (r1 == 0) goto L_0x031a
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r7.parent
            if (r4 == 0) goto L_0x0315
            r5 = 2
            boolean r4 = r4.hasActiveClearableNotifications(r5)
            r1.setAreThereDismissableGentleNotifs(r4)
            kotlin.Unit r1 = kotlin.Unit.INSTANCE
            goto L_0x031a
        L_0x0315:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r17)
            r0 = 0
            throw r0
        L_0x031a:
            com.android.systemui.statusbar.notification.stack.PeopleHubView r1 = r7.peopleHeaderView
            if (r1 == 0) goto L_0x034a
            if (r10 == 0) goto L_0x0328
            boolean r4 = r7.peopleHubVisible
            if (r4 == 0) goto L_0x0328
            if (r21 != 0) goto L_0x0328
            r8 = r0
            goto L_0x0329
        L_0x0328:
            r8 = r2
        L_0x0329:
            r1.setCanSwipe(r8)
            if (r3 == 0) goto L_0x034a
            java.lang.Integer r0 = r3.getTargetPosition()
            if (r0 == 0) goto L_0x034a
            int r0 = r0.intValue()
            java.lang.Integer r2 = r3.getCurrentPosition()
            if (r2 != 0) goto L_0x033f
            goto L_0x0345
        L_0x033f:
            int r2 = r2.intValue()
            if (r0 == r2) goto L_0x0348
        L_0x0345:
            r1.resetTranslation()
        L_0x0348:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
        L_0x034a:
            return
        L_0x034b:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r17)
            r0 = 0
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationSectionsManager.updateSectionBoundaries(java.lang.String):void");
    }

    /* compiled from: NotificationSectionsManager.kt */
    private static abstract class SectionBounds {
        private SectionBounds() {
        }

        public /* synthetic */ SectionBounds(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class Many extends SectionBounds {
            @NotNull
            private final ExpandableView first;
            @NotNull
            private final ExpandableView last;

            public static /* synthetic */ Many copy$default(Many many, ExpandableView expandableView, ExpandableView expandableView2, int i, Object obj) {
                if ((i & 1) != 0) {
                    expandableView = many.first;
                }
                if ((i & 2) != 0) {
                    expandableView2 = many.last;
                }
                return many.copy(expandableView, expandableView2);
            }

            @NotNull
            public final Many copy(@NotNull ExpandableView expandableView, @NotNull ExpandableView expandableView2) {
                Intrinsics.checkParameterIsNotNull(expandableView, "first");
                Intrinsics.checkParameterIsNotNull(expandableView2, "last");
                return new Many(expandableView, expandableView2);
            }

            public boolean equals(@Nullable Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof Many)) {
                    return false;
                }
                Many many = (Many) obj;
                return Intrinsics.areEqual((Object) this.first, (Object) many.first) && Intrinsics.areEqual((Object) this.last, (Object) many.last);
            }

            public int hashCode() {
                ExpandableView expandableView = this.first;
                int i = 0;
                int hashCode = (expandableView != null ? expandableView.hashCode() : 0) * 31;
                ExpandableView expandableView2 = this.last;
                if (expandableView2 != null) {
                    i = expandableView2.hashCode();
                }
                return hashCode + i;
            }

            @NotNull
            public String toString() {
                return "Many(first=" + this.first + ", last=" + this.last + ")";
            }

            @NotNull
            public final ExpandableView getFirst() {
                return this.first;
            }

            @NotNull
            public final ExpandableView getLast() {
                return this.last;
            }

            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            public Many(@NotNull ExpandableView expandableView, @NotNull ExpandableView expandableView2) {
                super((DefaultConstructorMarker) null);
                Intrinsics.checkParameterIsNotNull(expandableView, "first");
                Intrinsics.checkParameterIsNotNull(expandableView2, "last");
                this.first = expandableView;
                this.last = expandableView2;
            }
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class One extends SectionBounds {
            @NotNull
            private final ExpandableView lone;

            public boolean equals(@Nullable Object obj) {
                if (this != obj) {
                    return (obj instanceof One) && Intrinsics.areEqual((Object) this.lone, (Object) ((One) obj).lone);
                }
                return true;
            }

            public int hashCode() {
                ExpandableView expandableView = this.lone;
                if (expandableView != null) {
                    return expandableView.hashCode();
                }
                return 0;
            }

            @NotNull
            public String toString() {
                return "One(lone=" + this.lone + ")";
            }

            /* JADX INFO: super call moved to the top of the method (can break code semantics) */
            public One(@NotNull ExpandableView expandableView) {
                super((DefaultConstructorMarker) null);
                Intrinsics.checkParameterIsNotNull(expandableView, "lone");
                this.lone = expandableView;
            }

            @NotNull
            public final ExpandableView getLone() {
                return this.lone;
            }
        }

        /* compiled from: NotificationSectionsManager.kt */
        public static final class None extends SectionBounds {
            public static final None INSTANCE = new None();

            private None() {
                super((DefaultConstructorMarker) null);
            }
        }

        @NotNull
        public final SectionBounds addNotif(@NotNull ExpandableView expandableView) {
            Intrinsics.checkParameterIsNotNull(expandableView, "notif");
            if (this instanceof None) {
                return new One(expandableView);
            }
            if (this instanceof One) {
                return new Many(((One) this).getLone(), expandableView);
            }
            if (this instanceof Many) {
                return Many.copy$default((Many) this, (ExpandableView) null, expandableView, 1, (Object) null);
            }
            throw new NoWhenBranchMatchedException();
        }

        public final boolean updateSection(@NotNull NotificationSection notificationSection) {
            Intrinsics.checkParameterIsNotNull(notificationSection, "section");
            if (this instanceof None) {
                return setFirstAndLastVisibleChildren(notificationSection, (ExpandableView) null, (ExpandableView) null);
            }
            if (this instanceof One) {
                One one = (One) this;
                return setFirstAndLastVisibleChildren(notificationSection, one.getLone(), one.getLone());
            } else if (this instanceof Many) {
                Many many = (Many) this;
                return setFirstAndLastVisibleChildren(notificationSection, many.getFirst(), many.getLast());
            } else {
                throw new NoWhenBranchMatchedException();
            }
        }

        private final boolean setFirstAndLastVisibleChildren(@NotNull NotificationSection notificationSection, ExpandableView expandableView, ExpandableView expandableView2) {
            return notificationSection.setFirstVisibleChild(expandableView) || notificationSection.setLastVisibleChild(expandableView2);
        }
    }

    public final boolean updateFirstAndLastViewsForAllSections(@NotNull NotificationSection[] notificationSectionArr, @NotNull List<? extends ExpandableView> list) {
        SparseArray sparseArray;
        Intrinsics.checkParameterIsNotNull(notificationSectionArr, "sections");
        Intrinsics.checkParameterIsNotNull(list, "children");
        NotificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1 notificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1 = new NotificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1(CollectionsKt___CollectionsKt.asSequence(list), this);
        SectionBounds.None none = SectionBounds.None.INSTANCE;
        int length = notificationSectionArr.length;
        if (length < 0) {
            sparseArray = new SparseArray();
        } else {
            sparseArray = new SparseArray(length);
        }
        Iterator sourceIterator = notificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1.sourceIterator();
        while (sourceIterator.hasNext()) {
            Object next = sourceIterator.next();
            int intValue = ((Number) notificationSectionsManager$updateFirstAndLastViewsForAllSections$$inlined$groupingBy$1.keyOf(next)).intValue();
            Object obj = sparseArray.get(intValue);
            if (obj == null) {
                obj = none;
            }
            sparseArray.put(intValue, ((SectionBounds) obj).addNotif((ExpandableView) next));
        }
        boolean z = false;
        for (NotificationSection notificationSection : notificationSectionArr) {
            SectionBounds sectionBounds = (SectionBounds) sparseArray.get(notificationSection.getBucket());
            if (sectionBounds == null) {
                sectionBounds = SectionBounds.None.INSTANCE;
            }
            z = sectionBounds.updateSection(notificationSection) || z;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public final void onGentleHeaderClick() {
        this.activityStarter.startActivity(new Intent("android.settings.NOTIFICATION_SETTINGS"), true, true, 536870912);
    }

    /* access modifiers changed from: private */
    public final void onClearGentleNotifsClick(View view) {
        View.OnClickListener onClickListener = this.onClearSilentNotifsClickListener;
        if (onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public final void setOnClearSilentNotifsClickListener(@NotNull View.OnClickListener onClickListener) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "listener");
        this.onClearSilentNotifsClickListener = onClickListener;
    }

    public final void hidePeopleRow() {
        this.peopleHubVisible = false;
        updateSectionBoundaries("PeopleHub dismissed");
    }

    public final void setHeaderForegroundColor(int i) {
        PeopleHubView peopleHubView = this.peopleHeaderView;
        if (peopleHubView != null) {
            peopleHubView.setTextColor(i);
        }
        SectionHeaderView sectionHeaderView = this.silentHeaderView;
        if (sectionHeaderView != null) {
            sectionHeaderView.setForegroundColor(i);
        }
        SectionHeaderView sectionHeaderView2 = this.alertingHeaderView;
        if (sectionHeaderView2 != null) {
            sectionHeaderView2.setForegroundColor(i);
        }
    }
}
