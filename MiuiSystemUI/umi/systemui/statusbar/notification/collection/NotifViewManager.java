package com.android.systemui.statusbar.notification.collection;

import android.view.View;
import android.view.ViewParent;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import com.android.systemui.util.Assert;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotifViewManager.kt */
public final class NotifViewManager {
    private final FeatureFlags featureFlags;
    private SimpleNotificationListContainer listContainer;
    private final NotifViewBarn rowRegistry;
    private final VisualStabilityManager stabilityManager;

    public final void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
    }

    public NotifViewManager(@NotNull NotifViewBarn notifViewBarn, @NotNull VisualStabilityManager visualStabilityManager, @NotNull FeatureFlags featureFlags2) {
        Intrinsics.checkParameterIsNotNull(notifViewBarn, "rowRegistry");
        Intrinsics.checkParameterIsNotNull(visualStabilityManager, "stabilityManager");
        Intrinsics.checkParameterIsNotNull(featureFlags2, "featureFlags");
        this.rowRegistry = notifViewBarn;
        this.stabilityManager = visualStabilityManager;
        this.featureFlags = featureFlags2;
        List<T> unused = CollectionsKt__CollectionsKt.emptyList();
    }

    public final void attach(@NotNull ShadeListBuilder shadeListBuilder) {
        Intrinsics.checkParameterIsNotNull(shadeListBuilder, "listBuilder");
        if (this.featureFlags.isNewNotifPipelineRenderingEnabled()) {
            shadeListBuilder.setOnRenderListListener(new NotifViewManager$attach$1(this));
        }
    }

    public final void setViewConsumer(@NotNull SimpleNotificationListContainer simpleNotificationListContainer) {
        Intrinsics.checkParameterIsNotNull(simpleNotificationListContainer, "consumer");
        this.listContainer = simpleNotificationListContainer;
    }

    public final void onNotifTreeBuilt(@NotNull List<? extends ListEntry> list) {
        Intrinsics.checkParameterIsNotNull(list, "notifList");
        Assert.isMainThread();
        detachRows(list);
        attachRows(list);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0053  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0081  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void detachRows(java.util.List<? extends com.android.systemui.statusbar.notification.collection.ListEntry> r11) {
        /*
            r10 = this;
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r0 = r10.listContainer
            r1 = 0
            java.lang.String r2 = "listContainer"
            if (r0 == 0) goto L_0x00f7
            kotlin.sequences.Sequence r0 = r10.getListItems(r0)
            com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1 r3 = com.android.systemui.statusbar.notification.collection.NotifViewManager$detachRows$1.INSTANCE
            kotlin.sequences.Sequence r0 = kotlin.sequences.SequencesKt___SequencesKt.filter(r0, r3)
            java.util.Iterator r0 = r0.iterator()
        L_0x0015:
            boolean r3 = r0.hasNext()
            if (r3 == 0) goto L_0x00f6
            java.lang.Object r3 = r0.next()
            com.android.systemui.statusbar.notification.stack.NotificationListItem r3 = (com.android.systemui.statusbar.notification.stack.NotificationListItem) r3
            com.android.systemui.statusbar.notification.collection.NotificationEntry r4 = r3.getEntry()
            java.lang.String r5 = "listItem.entry"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r4, r5)
            com.android.systemui.statusbar.notification.collection.GroupEntry r4 = r4.getParent()
            com.android.systemui.statusbar.notification.collection.GroupEntry r6 = com.android.systemui.statusbar.notification.collection.GroupEntry.ROOT_ENTRY
            boolean r4 = kotlin.jvm.internal.Intrinsics.areEqual((java.lang.Object) r4, (java.lang.Object) r6)
            r6 = 1
            r4 = r4 ^ r6
            r7 = 0
            if (r4 == 0) goto L_0x0048
            com.android.systemui.statusbar.notification.collection.NotificationEntry r8 = r3.getEntry()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r5)
            com.android.systemui.statusbar.notification.collection.GroupEntry r5 = r8.getParent()
            if (r5 == 0) goto L_0x0048
            r5 = r6
            goto L_0x0049
        L_0x0048:
            r5 = r7
        L_0x0049:
            com.android.systemui.statusbar.notification.collection.NotificationEntry r8 = r3.getEntry()
            int r8 = r11.indexOf(r8)
            if (r4 == 0) goto L_0x0081
            boolean r4 = r3.isSummaryWithChildren()
            if (r4 == 0) goto L_0x005c
            r3.removeAllChildren()
        L_0x005c:
            if (r5 == 0) goto L_0x006a
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r4 = r10.listContainer
            if (r4 == 0) goto L_0x0066
            r4.setChildTransferInProgress(r6)
            goto L_0x006a
        L_0x0066:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x006a:
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r4 = r10.listContainer
            if (r4 == 0) goto L_0x007d
            r4.removeListItem(r3)
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r3 = r10.listContainer
            if (r3 == 0) goto L_0x0079
            r3.setChildTransferInProgress(r7)
            goto L_0x0015
        L_0x0079:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x007d:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x0081:
            java.lang.Object r4 = r11.get(r8)
            boolean r4 = r4 instanceof com.android.systemui.statusbar.notification.collection.GroupEntry
            if (r4 == 0) goto L_0x0015
            java.lang.Object r4 = r11.get(r8)
            if (r4 == 0) goto L_0x00ee
            com.android.systemui.statusbar.notification.collection.GroupEntry r4 = (com.android.systemui.statusbar.notification.collection.GroupEntry) r4
            java.util.List r4 = r4.getChildren()
            java.lang.String r5 = "(entries[idx] as GroupEntry).children"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r4, r5)
            java.util.List r5 = r3.getAttachedChildren()
            if (r5 == 0) goto L_0x0015
            java.util.Iterator r5 = r5.iterator()
        L_0x00a4:
            boolean r6 = r5.hasNext()
            if (r6 == 0) goto L_0x0015
            java.lang.Object r6 = r5.next()
            com.android.systemui.statusbar.notification.stack.NotificationListItem r6 = (com.android.systemui.statusbar.notification.stack.NotificationListItem) r6
            java.lang.String r7 = "listChild"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r6, r7)
            com.android.systemui.statusbar.notification.collection.NotificationEntry r7 = r6.getEntry()
            boolean r7 = r4.contains(r7)
            if (r7 != 0) goto L_0x00a4
            r3.removeChildNotification(r6)
            com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer r7 = r10.listContainer
            if (r7 == 0) goto L_0x00ea
            android.view.View r8 = r6.getView()
            java.lang.String r9 = "listChild.view"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r8, r9)
            android.view.View r6 = r6.getView()
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r6, r9)
            android.view.ViewParent r6 = r6.getParent()
            if (r6 == 0) goto L_0x00e2
            android.view.ViewGroup r6 = (android.view.ViewGroup) r6
            r7.notifyGroupChildRemoved(r8, r6)
            goto L_0x00a4
        L_0x00e2:
            kotlin.TypeCastException r10 = new kotlin.TypeCastException
            java.lang.String r11 = "null cannot be cast to non-null type android.view.ViewGroup"
            r10.<init>(r11)
            throw r10
        L_0x00ea:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        L_0x00ee:
            kotlin.TypeCastException r10 = new kotlin.TypeCastException
            java.lang.String r11 = "null cannot be cast to non-null type com.android.systemui.statusbar.notification.collection.GroupEntry"
            r10.<init>(r11)
            throw r10
        L_0x00f6:
            return
        L_0x00f7:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r2)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.detachRows(java.util.List):void");
    }

    private final Sequence<NotificationListItem> getListItems(SimpleNotificationListContainer simpleNotificationListContainer) {
        Sequence<NotificationListItem> filter = SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.map(CollectionsKt___CollectionsKt.asSequence(RangesKt___RangesKt.until(0, simpleNotificationListContainer.getContainerChildCount())), new NotifViewManager$getListItems$1(simpleNotificationListContainer)), NotifViewManager$getListItems$$inlined$filterIsInstance$1.INSTANCE);
        if (filter != null) {
            return filter;
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.sequences.Sequence<R>");
    }

    private final List<NotificationListItem> getChildListFromParent(ListEntry listEntry) {
        if (!(listEntry instanceof GroupEntry)) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
        List<NotificationEntry> children = ((GroupEntry) listEntry).getChildren();
        Intrinsics.checkExpressionValueIsNotNull(children, "parent.children");
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(children, 10));
        for (NotificationEntry notificationEntry : children) {
            NotifViewBarn notifViewBarn = this.rowRegistry;
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "child");
            arrayList.add(notifViewBarn.requireView(notificationEntry));
        }
        return CollectionsKt___CollectionsKt.toList(arrayList);
    }

    private final void attachRows(List<? extends ListEntry> list) {
        Iterator<T> it = list.iterator();
        boolean z = false;
        while (true) {
            Class<?> cls = null;
            if (it.hasNext()) {
                ListEntry listEntry = (ListEntry) it.next();
                NotifViewBarn notifViewBarn = this.rowRegistry;
                NotificationEntry representativeEntry = listEntry.getRepresentativeEntry();
                if (representativeEntry != null) {
                    Intrinsics.checkExpressionValueIsNotNull(representativeEntry, "entry.representativeEntry!!");
                    NotificationListItem requireView = notifViewBarn.requireView(representativeEntry);
                    View view = requireView.getView();
                    Intrinsics.checkExpressionValueIsNotNull(view, "listItem.view");
                    if (view.getParent() == null) {
                        SimpleNotificationListContainer simpleNotificationListContainer = this.listContainer;
                        if (simpleNotificationListContainer != null) {
                            simpleNotificationListContainer.addListItem(requireView);
                            this.stabilityManager.notifyViewAddition(requireView.getView());
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("listContainer");
                            throw null;
                        }
                    }
                    if (listEntry instanceof GroupEntry) {
                        GroupEntry groupEntry = (GroupEntry) listEntry;
                        List<NotificationEntry> children = groupEntry.getChildren();
                        Intrinsics.checkExpressionValueIsNotNull(children, "entry.children");
                        int i = 0;
                        for (NotificationEntry notificationEntry : children) {
                            NotifViewBarn notifViewBarn2 = this.rowRegistry;
                            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "childEntry");
                            NotificationListItem requireView2 = notifViewBarn2.requireView(notificationEntry);
                            if (requireView.getAttachedChildren() != null) {
                                List<? extends NotificationListItem> attachedChildren = requireView.getAttachedChildren();
                                Intrinsics.checkExpressionValueIsNotNull(attachedChildren, "listItem.attachedChildren");
                                if (CollectionsKt___CollectionsKt.contains(attachedChildren, requireView2)) {
                                    continue;
                                    i++;
                                }
                            }
                            View view2 = requireView2.getView();
                            Intrinsics.checkExpressionValueIsNotNull(view2, "childListItem.view");
                            if (view2.getParent() != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("trying to add a notification child that already has a parent. class: ");
                                View view3 = requireView2.getView();
                                Intrinsics.checkExpressionValueIsNotNull(view3, "childListItem.view");
                                ViewParent parent = view3.getParent();
                                if (parent != null) {
                                    cls = parent.getClass();
                                }
                                sb.append(cls);
                                sb.append(' ');
                                sb.append("\n child: ");
                                sb.append(requireView2.getView());
                                throw new IllegalStateException(sb.toString());
                            }
                            requireView.addChildNotification(requireView2, i);
                            this.stabilityManager.notifyViewAddition(requireView2.getView());
                            SimpleNotificationListContainer simpleNotificationListContainer2 = this.listContainer;
                            if (simpleNotificationListContainer2 != null) {
                                View view4 = requireView2.getView();
                                Intrinsics.checkExpressionValueIsNotNull(view4, "childListItem.view");
                                simpleNotificationListContainer2.notifyGroupChildAdded(view4);
                                i++;
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("listContainer");
                                throw null;
                            }
                        }
                        z = z || requireView.applyChildOrder(getChildListFromParent(listEntry), this.stabilityManager, (VisualStabilityManager.Callback) null);
                        requireView.setUntruncatedChildCount(groupEntry.getUntruncatedChildCount());
                    }
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else if (z) {
                SimpleNotificationListContainer simpleNotificationListContainer3 = this.listContainer;
                if (simpleNotificationListContainer3 != null) {
                    simpleNotificationListContainer3.generateChildOrderChangedEvent();
                    return;
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("listContainer");
                    throw null;
                }
            } else {
                return;
            }
        }
    }
}
