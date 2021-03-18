package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.MathUtils;
import android.view.SurfaceControl;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: BlurUtils.kt */
public class BlurUtils implements Dumpable {
    private final boolean blurDisabledSysProp = SystemProperties.getBoolean("persist.sys.sf.disable_blurs", false);
    private final boolean blurSupportedSysProp = SystemProperties.getBoolean("ro.surface_flinger.supports_background_blur", false);
    private final int maxBlurRadius = this.resources.getDimensionPixelSize(C0012R$dimen.max_window_blur_radius);
    private final int minBlurRadius;
    private final Resources resources;

    public BlurUtils(@NotNull Resources resources2, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(resources2, "resources");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.resources = resources2;
        this.minBlurRadius = resources2.getDimensionPixelSize(C0012R$dimen.min_window_blur_radius);
        String name = BlurUtils.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager.registerDumpable(name, this);
    }

    public final int getMinBlurRadius() {
        return this.minBlurRadius;
    }

    public final int getMaxBlurRadius() {
        return this.maxBlurRadius;
    }

    public final int blurRadiusOfRatio(float f) {
        if (f == 0.0f) {
            return 0;
        }
        return (int) MathUtils.lerp((float) this.minBlurRadius, (float) this.maxBlurRadius, f);
    }

    public final float ratioOfBlurRadius(int i) {
        if (i == 0) {
            return 0.0f;
        }
        return MathUtils.map((float) this.minBlurRadius, (float) this.maxBlurRadius, 0.0f, 1.0f, (float) i);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0030, code lost:
        kotlin.io.CloseableKt.closeFinally(r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        throw r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void applyBlur(@org.jetbrains.annotations.Nullable android.view.ViewRootImpl r3, int r4) {
        /*
            r2 = this;
            if (r3 == 0) goto L_0x0034
            android.view.SurfaceControl r0 = r3.getSurfaceControl()
            java.lang.String r1 = "viewRootImpl.surfaceControl"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r1)
            boolean r0 = r0.isValid()
            if (r0 == 0) goto L_0x0034
            boolean r0 = r2.supportsBlursOnWindows()
            if (r0 != 0) goto L_0x0018
            goto L_0x0034
        L_0x0018:
            android.view.SurfaceControl$Transaction r2 = r2.createTransaction()
            r0 = 0
            android.view.SurfaceControl r3 = r3.getSurfaceControl()     // Catch:{ all -> 0x002d }
            r2.setBackgroundBlurRadius(r3, r4)     // Catch:{ all -> 0x002d }
            r2.apply()     // Catch:{ all -> 0x002d }
            kotlin.Unit r3 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x002d }
            kotlin.io.CloseableKt.closeFinally(r2, r0)
            return
        L_0x002d:
            r3 = move-exception
            throw r3     // Catch:{ all -> 0x002f }
        L_0x002f:
            r4 = move-exception
            kotlin.io.CloseableKt.closeFinally(r2, r3)
            throw r4
        L_0x0034:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.BlurUtils.applyBlur(android.view.ViewRootImpl, int):void");
    }

    @NotNull
    public SurfaceControl.Transaction createTransaction() {
        return new SurfaceControl.Transaction();
    }

    public boolean supportsBlursOnWindows() {
        return this.blurSupportedSysProp && !this.blurDisabledSysProp && ActivityManager.isHighEndGfx();
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.println("BlurUtils:");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("minBlurRadius: " + this.minBlurRadius);
        indentingPrintWriter.println("maxBlurRadius: " + this.maxBlurRadius);
        indentingPrintWriter.println("blurSupportedSysProp: " + this.blurSupportedSysProp);
        indentingPrintWriter.println("blurDisabledSysProp: " + this.blurDisabledSysProp);
        indentingPrintWriter.println("supportsBlursOnWindows: " + supportsBlursOnWindows());
    }
}
