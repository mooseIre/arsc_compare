package com.android.systemui.controls.controller;

import com.android.systemui.controls.ControlStatus;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$loadForComponent$2$error$1 implements Runnable {
    final /* synthetic */ ControlsControllerImpl$loadForComponent$2 this$0;

    ControlsControllerImpl$loadForComponent$2$error$1(ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2) {
        this.this$0 = controlsControllerImpl$loadForComponent$2;
    }

    public final void run() {
        List<StructureInfo> structuresForComponent = Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName);
        ArrayList<ControlStatus> arrayList = new ArrayList();
        for (T t : structuresForComponent) {
            List<ControlInfo> controls = t.getControls();
            ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
            Iterator<T> it = controls.iterator();
            while (it.hasNext()) {
                ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2 = this.this$0;
                arrayList2.add(controlsControllerImpl$loadForComponent$2.this$0.createRemovedStatus(controlsControllerImpl$loadForComponent$2.$componentName, it.next(), t.getStructure(), false));
            }
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, arrayList2);
        }
        ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList, 10));
        for (ControlStatus controlStatus : arrayList) {
            arrayList3.add(controlStatus.getControl().getControlId());
        }
        this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject(arrayList, arrayList3, true));
    }
}
