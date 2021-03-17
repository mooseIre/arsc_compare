package com.android.systemui.statusbar.phone;

import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.statusbar.policy.ExtensionController;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMY-mA_f9J2Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMYmA_f9J2Y implements ExtensionController.PluginConverter {
    public static final /* synthetic */ $$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMYmA_f9J2Y INSTANCE = new $$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMYmA_f9J2Y();

    private /* synthetic */ $$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMYmA_f9J2Y() {
    }

    @Override // com.android.systemui.statusbar.policy.ExtensionController.PluginConverter
    public final Object getInterfaceFromPlugin(Object obj) {
        return ((IntentButtonProvider) obj).getIntentButton();
    }
}
