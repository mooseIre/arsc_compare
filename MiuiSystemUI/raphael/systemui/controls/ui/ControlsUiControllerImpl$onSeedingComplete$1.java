package com.android.systemui.controls.ui;

import java.util.Iterator;
import java.util.function.Consumer;

/* compiled from: ControlsUiControllerImpl.kt */
final class ControlsUiControllerImpl$onSeedingComplete$1<T> implements Consumer<Boolean> {
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$onSeedingComplete$1(ControlsUiControllerImpl controlsUiControllerImpl) {
        this.this$0 = controlsUiControllerImpl;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // java.util.function.Consumer
    public /* bridge */ /* synthetic */ void accept(Boolean bool) {
        accept(bool.booleanValue());
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
                    int size = next.getControls().size();
                    do {
                        T next2 = it.next();
                        int size2 = next2.getControls().size();
                        if (size < size2) {
                            next = next2;
                            size = size2;
                        }
                    } while (it.hasNext());
                }
                t = next;
            }
            T t2 = t;
            if (t2 == null) {
                t2 = ControlsUiControllerImpl.access$getEMPTY_STRUCTURE$cp();
            }
            ControlsUiControllerImpl.access$setSelectedStructure$p(controlsUiControllerImpl, t2);
            ControlsUiControllerImpl controlsUiControllerImpl2 = this.this$0;
            ControlsUiControllerImpl.access$updatePreferences(controlsUiControllerImpl2, ControlsUiControllerImpl.access$getSelectedStructure$p(controlsUiControllerImpl2));
        }
        ControlsUiControllerImpl controlsUiControllerImpl3 = this.this$0;
        ControlsUiControllerImpl.access$reload(controlsUiControllerImpl3, ControlsUiControllerImpl.access$getParent$p(controlsUiControllerImpl3));
    }
}
