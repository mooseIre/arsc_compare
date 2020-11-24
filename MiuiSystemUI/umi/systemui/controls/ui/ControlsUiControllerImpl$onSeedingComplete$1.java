package com.android.systemui.controls.ui;

import com.android.systemui.controls.controller.StructureInfo;
import java.util.Iterator;
import java.util.function.Consumer;

/* compiled from: ControlsUiControllerImpl.kt */
final class ControlsUiControllerImpl$onSeedingComplete$1<T> implements Consumer<Boolean> {
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$onSeedingComplete$1(ControlsUiControllerImpl controlsUiControllerImpl) {
        this.this$0 = controlsUiControllerImpl;
    }

    public /* bridge */ /* synthetic */ void accept(Object obj) {
        accept(((Boolean) obj).booleanValue());
    }

    public final void accept(boolean z) {
        T t;
        if (z) {
            ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
            Iterator<T> it = controlsUiControllerImpl.getControlsController().get().getFavorites().iterator();
            if (!it.hasNext()) {
                t = null;
            } else {
                T next = it.next();
                if (!it.hasNext()) {
                    t = next;
                } else {
                    int size = ((StructureInfo) next).getControls().size();
                    do {
                        T next2 = it.next();
                        int size2 = ((StructureInfo) next2).getControls().size();
                        if (size < size2) {
                            next = next2;
                            size = size2;
                        }
                    } while (it.hasNext());
                }
                t = next;
            }
            StructureInfo structureInfo = (StructureInfo) t;
            if (structureInfo == null) {
                structureInfo = ControlsUiControllerImpl.EMPTY_STRUCTURE;
            }
            controlsUiControllerImpl.selectedStructure = structureInfo;
            ControlsUiControllerImpl controlsUiControllerImpl2 = this.this$0;
            controlsUiControllerImpl2.updatePreferences(controlsUiControllerImpl2.selectedStructure);
        }
        ControlsUiControllerImpl controlsUiControllerImpl3 = this.this$0;
        controlsUiControllerImpl3.reload(ControlsUiControllerImpl.access$getParent$p(controlsUiControllerImpl3));
    }
}
