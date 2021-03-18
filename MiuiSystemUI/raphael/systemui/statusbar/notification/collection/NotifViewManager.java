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
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
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
        // Method dump skipped, instructions count: 251
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.collection.NotifViewManager.detachRows(java.util.List):void");
    }

    private final Sequence<NotificationListItem> getListItems(SimpleNotificationListContainer simpleNotificationListContainer) {
        Sequence<NotificationListItem> sequence = SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.map(CollectionsKt___CollectionsKt.asSequence(RangesKt___RangesKt.until(0, simpleNotificationListContainer.getContainerChildCount())), new NotifViewManager$getListItems$1(simpleNotificationListContainer)), NotifViewManager$getListItems$$inlined$filterIsInstance$1.INSTANCE);
        if (sequence != null) {
            return sequence;
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
        for (T t : children) {
            NotifViewBarn notifViewBarn = this.rowRegistry;
            Intrinsics.checkExpressionValueIsNotNull(t, "child");
            arrayList.add(notifViewBarn.requireView(t));
        }
        return CollectionsKt___CollectionsKt.toList(arrayList);
    }

    private final void attachRows(List<? extends ListEntry> list) {
        Iterator<T> it = list.iterator();
        boolean z = false;
        while (true) {
            Class<?> cls = null;
            if (it.hasNext()) {
                T next = it.next();
                NotifViewBarn notifViewBarn = this.rowRegistry;
                NotificationEntry representativeEntry = next.getRepresentativeEntry();
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
                    if (next instanceof GroupEntry) {
                        T t = next;
                        List<NotificationEntry> children = t.getChildren();
                        Intrinsics.checkExpressionValueIsNotNull(children, "entry.children");
                        int i = 0;
                        for (T t2 : children) {
                            NotifViewBarn notifViewBarn2 = this.rowRegistry;
                            Intrinsics.checkExpressionValueIsNotNull(t2, "childEntry");
                            NotificationListItem requireView2 = notifViewBarn2.requireView(t2);
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
                        z = z || requireView.applyChildOrder(getChildListFromParent(next), this.stabilityManager, null);
                        requireView.setUntruncatedChildCount(t.getUntruncatedChildCount());
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
