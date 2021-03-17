package com.android.systemui.statusbar.phone;

import android.content.om.IOverlayManager;
import android.os.ServiceManager;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: NavigationModeControllerExt.kt */
final class NavigationModeControllerExt$mOverlayManager$2 extends Lambda implements Function0<IOverlayManager> {
    public static final NavigationModeControllerExt$mOverlayManager$2 INSTANCE = new NavigationModeControllerExt$mOverlayManager$2();

    NavigationModeControllerExt$mOverlayManager$2() {
        super(0);
    }

    @Override // kotlin.jvm.functions.Function0
    public final IOverlayManager invoke() {
        return IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay"));
    }
}
