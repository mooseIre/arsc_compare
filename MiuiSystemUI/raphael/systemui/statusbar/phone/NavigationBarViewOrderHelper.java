package com.android.systemui.statusbar.phone;

import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0015R$id;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyOrderObserver.kt */
public final class NavigationBarViewOrderHelper {
    public static final NavigationBarViewOrderHelper INSTANCE = new NavigationBarViewOrderHelper();
    private static final Set<Integer> sKeyIdSet = SetsKt__SetsKt.setOf((Object[]) new Integer[]{Integer.valueOf(C0015R$id.menu), Integer.valueOf(C0015R$id.recent_apps), Integer.valueOf(C0015R$id.back)});

    private NavigationBarViewOrderHelper() {
    }

    public final void reverseOrder(@NotNull ViewGroup viewGroup) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        int childCount = viewGroup.getChildCount();
        LinkedList linkedList = new LinkedList();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ViewGroup) {
                View childAt2 = ((ViewGroup) childAt).getChildAt(0);
                Set<Integer> set = sKeyIdSet;
                Intrinsics.checkExpressionValueIsNotNull(childAt2, "view");
                if (set.contains(Integer.valueOf(childAt2.getId()))) {
                    arrayList.add(childAt);
                    linkedList.add(0, Integer.valueOf(i));
                    viewGroup.removeView(childAt);
                }
            }
        }
        int size = arrayList.size();
        for (int i2 = 0; i2 < size; i2++) {
            Object removeFirst = linkedList.removeFirst();
            Intrinsics.checkExpressionValueIsNotNull(removeFirst, "positions.removeFirst()");
            viewGroup.addView((View) arrayList.get(i2), ((Number) removeFirst).intValue());
        }
    }
}
