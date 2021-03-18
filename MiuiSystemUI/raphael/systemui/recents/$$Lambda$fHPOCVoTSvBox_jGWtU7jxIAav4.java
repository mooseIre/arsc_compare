package com.android.systemui.recents;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.recents.-$$Lambda$fHPOCVoTSvBox_jGWtU7jxIAav4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$fHPOCVoTSvBox_jGWtU7jxIAav4 implements Consumer {
    public static final /* synthetic */ $$Lambda$fHPOCVoTSvBox_jGWtU7jxIAav4 INSTANCE = new $$Lambda$fHPOCVoTSvBox_jGWtU7jxIAav4();

    private /* synthetic */ $$Lambda$fHPOCVoTSvBox_jGWtU7jxIAav4() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Divider) obj).onDockedTopTask();
    }
}
