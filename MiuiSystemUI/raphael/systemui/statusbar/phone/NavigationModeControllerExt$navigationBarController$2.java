package com.android.systemui.statusbar.phone;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NavigationBarController;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: NavigationModeControllerExt.kt */
final class NavigationModeControllerExt$navigationBarController$2 extends Lambda implements Function0<NavigationBarController> {
    public static final NavigationModeControllerExt$navigationBarController$2 INSTANCE = new NavigationModeControllerExt$navigationBarController$2();

    NavigationModeControllerExt$navigationBarController$2() {
        super(0);
    }

    @Override // kotlin.jvm.functions.Function0
    public final NavigationBarController invoke() {
        return (NavigationBarController) Dependency.get(NavigationBarController.class);
    }
}
