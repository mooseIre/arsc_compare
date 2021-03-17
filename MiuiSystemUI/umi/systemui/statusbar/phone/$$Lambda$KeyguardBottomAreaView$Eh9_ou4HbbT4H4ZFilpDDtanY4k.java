package com.android.systemui.statusbar.phone;

import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.statusbar.policy.ExtensionController;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k implements ExtensionController.PluginConverter {
    public static final /* synthetic */ $$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k INSTANCE = new $$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k();

    private /* synthetic */ $$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k() {
    }

    @Override // com.android.systemui.statusbar.policy.ExtensionController.PluginConverter
    public final Object getInterfaceFromPlugin(Object obj) {
        return ((IntentButtonProvider) obj).getIntentButton();
    }
}
