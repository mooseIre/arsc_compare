package com.android.systemui.pip;

import android.view.SurfaceControl;
import com.android.systemui.pip.PipSurfaceTransactionHelper;

/* renamed from: com.android.systemui.pip.-$$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU implements PipSurfaceTransactionHelper.SurfaceControlTransactionFactory {
    public static final /* synthetic */ $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU INSTANCE = new $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU();

    private /* synthetic */ $$Lambda$0FLZQAxNoOm85ohJ3bgjkYQDWsU() {
    }

    @Override // com.android.systemui.pip.PipSurfaceTransactionHelper.SurfaceControlTransactionFactory
    public final SurfaceControl.Transaction getTransaction() {
        return new SurfaceControl.Transaction();
    }
}
