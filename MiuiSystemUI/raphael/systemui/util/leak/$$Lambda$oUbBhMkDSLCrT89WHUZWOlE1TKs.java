package com.android.systemui.util.leak;

import java.util.Collection;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.util.leak.-$$Lambda$oUbBhMkDSLCrT89WHUZWOlE1TKs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$oUbBhMkDSLCrT89WHUZWOlE1TKs implements Predicate {
    public static final /* synthetic */ $$Lambda$oUbBhMkDSLCrT89WHUZWOlE1TKs INSTANCE = new $$Lambda$oUbBhMkDSLCrT89WHUZWOlE1TKs();

    private /* synthetic */ $$Lambda$oUbBhMkDSLCrT89WHUZWOlE1TKs() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return TrackedObjects.isTrackedObject((Collection) obj);
    }
}
