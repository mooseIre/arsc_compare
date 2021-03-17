package com.android.systemui.controls.controller;

import com.android.systemui.controls.ControlStatus;
import java.util.ArrayList;
import java.util.List;

/* compiled from: ControlsControllerImpl.kt */
final class ControlsControllerImpl$loadForComponent$2$error$1 implements Runnable {
    final /* synthetic */ ControlsControllerImpl$loadForComponent$2 this$0;

    ControlsControllerImpl$loadForComponent$2$error$1(ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2) {
        this.this$0 = controlsControllerImpl$loadForComponent$2;
    }

    public final void run() {
        List<StructureInfo> structuresForComponent = Favorites.INSTANCE.getStructuresForComponent(this.this$0.$componentName);
        ArrayList<ControlStatus> arrayList = new ArrayList<>();
        for (StructureInfo structureInfo : structuresForComponent) {
            List<ControlInfo> controls = structureInfo.getControls();
            ArrayList arrayList2 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
            for (ControlInfo access$createRemovedStatus : controls) {
                ControlsControllerImpl$loadForComponent$2 controlsControllerImpl$loadForComponent$2 = this.this$0;
                arrayList2.add(controlsControllerImpl$loadForComponent$2.this$0.createRemovedStatus(controlsControllerImpl$loadForComponent$2.$componentName, access$createRemovedStatus, structureInfo.getStructure(), false));
            }
            boolean unused = CollectionsKt__MutableCollectionsKt.addAll(arrayList, arrayList2);
        }
        ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList, 10));
        for (ControlStatus control : arrayList) {
            arrayList3.add(control.getControl().getControlId());
        }
        this.this$0.$dataCallback.accept(ControlsControllerKt.createLoadDataObject(arrayList, arrayList3, true));
    }
}
