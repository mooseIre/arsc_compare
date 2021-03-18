package com.android.systemui.controls.controller;

import android.service.controls.Control;
import com.android.systemui.controls.ControlStatus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;

/* access modifiers changed from: package-private */
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$loadForComponent$2$accept$1 implements Runnable {
    final /* synthetic */ List $controls;
    final /* synthetic */ ControlsControllerImpl$loadForComponent$2 this$0;

    ControlsControllerImpl$loadForComponent$2$accept$1(ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2, List list) {
        this.this$0 = controlsControllerImpl$loadForComponent$2;
        this.$controls = list;
    }

    public final void run() {
        List<ControlInfo> controlsForComponent = Favorites.INSTANCE.getControlsForComponent(this.this$0.$componentName);
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controlsForComponent, 10));
        Iterator<T> it = controlsForComponent.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getControlId());
        }
        if (Favorites.INSTANCE.updateControls(this.this$0.$componentName, this.$controls)) {
            this.this$0.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
        }
        Set set = this.this$0.this$0.findRemoved(CollectionsKt___CollectionsKt.toSet(arrayList), this.$controls);
        List<Control> list = this.$controls;
        ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (Control control : list) {
            arrayList2.add(new ControlStatus(control, this.this$0.$componentName, arrayList.contains(control.getControlId()), false, 8, null));
        }
        ArrayList arrayList3 = new ArrayList();
        for (T t : Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName)) {
            for (T t2 : t.getControls()) {
                if (set.contains(t2.getControlId())) {
                    ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2 = this.this$0;
                    arrayList3.add(ControlsControllerImpl.createRemovedStatus$default(controlsControllerImpl$loadForComponent$2.this$0, controlsControllerImpl$loadForComponent$2.$componentName, t2, t.getStructure(), false, 8, null));
                }
            }
        }
        this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject$default(CollectionsKt___CollectionsKt.plus((Collection) arrayList3, (Iterable) arrayList2), arrayList, false, 4, null));
    }
}
