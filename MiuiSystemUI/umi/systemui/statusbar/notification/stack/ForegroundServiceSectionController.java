package com.android.systemui.statusbar.notification.stack;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.DungeonRow;
import com.android.systemui.util.Assert;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function3;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ForegroundServiceSectionController.kt */
public final class ForegroundServiceSectionController {
    /* access modifiers changed from: private */
    public final Set<NotificationEntry> entries = new LinkedHashSet();
    private View entriesView;
    @NotNull
    private final NotificationEntryManager entryManager;
    @NotNull
    private final ForegroundServiceDismissalFeatureController featureController;

    public ForegroundServiceSectionController(@NotNull NotificationEntryManager notificationEntryManager, @NotNull ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(foregroundServiceDismissalFeatureController, "featureController");
        this.entryManager = notificationEntryManager;
        this.featureController = foregroundServiceDismissalFeatureController;
        if (this.featureController.isForegroundServiceDismissalEnabled()) {
            this.entryManager.addNotificationRemoveInterceptor(new ForegroundServiceSectionController$sam$com_android_systemui_statusbar_NotificationRemoveInterceptor$0(new Function3<String, NotificationEntry, Integer, Boolean>(this) {
                public final String getName() {
                    return "shouldInterceptRemoval";
                }

                public final KDeclarationContainer getOwner() {
                    return Reflection.getOrCreateKotlinClass(ForegroundServiceSectionController.class);
                }

                public final String getSignature() {
                    return "shouldInterceptRemoval(Ljava/lang/String;Lcom/android/systemui/statusbar/notification/collection/NotificationEntry;I)Z";
                }

                public /* bridge */ /* synthetic */ Object invoke(Object obj, Object obj2, Object obj3) {
                    return Boolean.valueOf(invoke((String) obj, (NotificationEntry) obj2, ((Number) obj3).intValue()));
                }

                public final boolean invoke(@NotNull String str, @Nullable NotificationEntry notificationEntry, int i) {
                    Intrinsics.checkParameterIsNotNull(str, "p1");
                    return ((ForegroundServiceSectionController) this.receiver).shouldInterceptRemoval(str, notificationEntry, i);
                }
            }));
            this.entryManager.addNotificationEntryListener(new NotificationEntryListener(this) {
                final /* synthetic */ ForegroundServiceSectionController this$0;

                {
                    this.this$0 = r1;
                }

                public void onPostEntryUpdated(@NotNull NotificationEntry notificationEntry) {
                    Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                    if (this.this$0.entries.contains(notificationEntry)) {
                        this.this$0.removeEntry(notificationEntry);
                        this.this$0.addEntry(notificationEntry);
                        this.this$0.update();
                    }
                }
            });
        }
    }

    @NotNull
    public final NotificationEntryManager getEntryManager() {
        return this.entryManager;
    }

    /* access modifiers changed from: private */
    public final boolean shouldInterceptRemoval(String str, NotificationEntry notificationEntry, int i) {
        Assert.isMainThread();
        boolean z = i == 3;
        boolean z2 = i == 2 || i == 1;
        if (i != 8) {
        }
        boolean z3 = i == 12;
        if (notificationEntry == null) {
            return false;
        }
        if (z2) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            if (!sbn.isClearable()) {
                if (!hasEntry(notificationEntry)) {
                    addEntry(notificationEntry);
                    update();
                }
                this.entryManager.updateNotifications("FgsSectionController.onNotificationRemoveRequested");
                return true;
            }
        }
        if (z || z3) {
            ExpandedNotification sbn2 = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
            if (!sbn2.isClearable()) {
                return true;
            }
        }
        if (hasEntry(notificationEntry)) {
            removeEntry(notificationEntry);
            update();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public final void removeEntry(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.entries.remove(notificationEntry);
    }

    /* access modifiers changed from: private */
    public final void addEntry(NotificationEntry notificationEntry) {
        Assert.isMainThread();
        this.entries.add(notificationEntry);
    }

    public final boolean hasEntry(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Assert.isMainThread();
        return this.entries.contains(notificationEntry);
    }

    @NotNull
    public final View createView(@NotNull LayoutInflater layoutInflater) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "li");
        View inflate = layoutInflater.inflate(C0014R$layout.foreground_service_dungeon, (ViewGroup) null);
        this.entriesView = inflate;
        if (inflate != null) {
            inflate.setVisibility(8);
            View view = this.entriesView;
            if (view != null) {
                return view;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: private */
    public final void update() {
        Assert.isMainThread();
        View view = this.entriesView;
        if (view == null) {
            throw new IllegalStateException("ForegroundServiceSectionController is trying to show dismissed fgs notifications without having been initialized!");
        } else if (view != null) {
            View findViewById = view.findViewById(C0012R$id.entry_list);
            if (findViewById != null) {
                LinearLayout linearLayout = (LinearLayout) findViewById;
                linearLayout.removeAllViews();
                for (T t : CollectionsKt___CollectionsKt.sortedWith(this.entries, new ForegroundServiceSectionController$$special$$inlined$sortedBy$1())) {
                    View inflate = LayoutInflater.from(linearLayout.getContext()).inflate(C0014R$layout.foreground_service_dungeon_row, (ViewGroup) null);
                    if (inflate != null) {
                        DungeonRow dungeonRow = (DungeonRow) inflate;
                        dungeonRow.setEntry(t);
                        dungeonRow.setOnClickListener(new ForegroundServiceSectionController$update$$inlined$apply$lambda$1(dungeonRow, t, linearLayout, this));
                        linearLayout.addView(dungeonRow);
                    } else {
                        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.DungeonRow");
                    }
                }
                if (this.entries.isEmpty()) {
                    View view2 = this.entriesView;
                    if (view2 != null) {
                        view2.setVisibility(8);
                        return;
                    }
                    return;
                }
                View view3 = this.entriesView;
                if (view3 != null) {
                    view3.setVisibility(0);
                    return;
                }
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.widget.LinearLayout");
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }
}
