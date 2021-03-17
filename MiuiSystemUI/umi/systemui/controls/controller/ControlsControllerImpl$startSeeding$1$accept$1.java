package com.android.systemui.controls.controller;

import android.content.ComponentName;
import android.service.controls.Control;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ControlsControllerImpl.kt */
public final class ControlsControllerImpl$startSeeding$1$accept$1 implements Runnable {
    final /* synthetic */ List $controls;
    final /* synthetic */ ControlsControllerImpl$startSeeding$1 this$0;

    ControlsControllerImpl$startSeeding$1$accept$1(ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$1, List list) {
        this.this$0 = controlsControllerImpl$startSeeding$1;
        this.$controls = list;
    }

    public final void run() {
        ArrayMap arrayMap = new ArrayMap();
        for (Control control : this.$controls) {
            Object structure = control.getStructure();
            if (structure == null) {
                structure = "";
            }
            List list = (List) arrayMap.get(structure);
            if (list == null) {
                list = new ArrayList();
            }
            Intrinsics.checkExpressionValueIsNotNull(list, "structureToControls.get(…ableListOf<ControlInfo>()");
            if (list.size() < 6) {
                String controlId = control.getControlId();
                Intrinsics.checkExpressionValueIsNotNull(controlId, "it.controlId");
                CharSequence title = control.getTitle();
                Intrinsics.checkExpressionValueIsNotNull(title, "it.title");
                CharSequence subtitle = control.getSubtitle();
                Intrinsics.checkExpressionValueIsNotNull(subtitle, "it.subtitle");
                list.add(new ControlInfo(controlId, title, subtitle, control.getDeviceType()));
                arrayMap.put(structure, list);
            }
        }
        for (Map.Entry entry : arrayMap.entrySet()) {
            CharSequence charSequence = (CharSequence) entry.getKey();
            List list2 = (List) entry.getValue();
            Favorites favorites = Favorites.INSTANCE;
            ComponentName componentName = this.this$0.$componentName;
            Intrinsics.checkExpressionValueIsNotNull(charSequence, "s");
            Intrinsics.checkExpressionValueIsNotNull(list2, "cs");
            favorites.replaceControls(new StructureInfo(componentName, charSequence, list2));
        }
        this.this$0.this$0.persistenceWrapper.storeFavorites(Favorites.INSTANCE.getAllStructures());
        ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$1 = this.this$0;
        Consumer consumer = controlsControllerImpl$startSeeding$1.$callback;
        String packageName = controlsControllerImpl$startSeeding$1.$componentName.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "componentName.packageName");
        consumer.accept(new SeedResponse(packageName, true));
        ControlsControllerImpl$startSeeding$1 controlsControllerImpl$startSeeding$12 = this.this$0;
        controlsControllerImpl$startSeeding$12.this$0.startSeeding(controlsControllerImpl$startSeeding$12.$remaining, controlsControllerImpl$startSeeding$12.$callback, controlsControllerImpl$startSeeding$12.$didAnyFail);
    }
}
