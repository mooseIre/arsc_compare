package com.android.systemui.statusbar.phone;

import android.view.View;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.statusbar.phone.-$$Lambda$dFYK0EjGBZUG5FTAJ9pyZPnsifY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$dFYK0EjGBZUG5FTAJ9pyZPnsifY implements Predicate {
    public static final /* synthetic */ $$Lambda$dFYK0EjGBZUG5FTAJ9pyZPnsifY INSTANCE = new $$Lambda$dFYK0EjGBZUG5FTAJ9pyZPnsifY();

    private /* synthetic */ $$Lambda$dFYK0EjGBZUG5FTAJ9pyZPnsifY() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((View) obj).isAttachedToWindow();
    }
}
