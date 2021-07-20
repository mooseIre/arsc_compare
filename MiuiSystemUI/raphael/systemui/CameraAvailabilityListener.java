package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraManager;
import android.util.PathParser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.math.MathKt__MathJVMKt;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: CameraAvailabilityListener.kt */
public final class CameraAvailabilityListener {
    public static final Factory Factory = new Factory(null);
    private final CameraManager.AvailabilityCallback availabilityCallback = new CameraAvailabilityListener$availabilityCallback$1(this);
    private final CameraManager cameraManager;
    private Rect cutoutBounds = new Rect();
    private final Path cutoutProtectionPath;
    private final Set<String> excludedPackageIds;
    private final Executor executor;
    private final List<CameraTransitionCallback> listeners = new ArrayList();
    private final String targetCameraId;

    /* compiled from: CameraAvailabilityListener.kt */
    public interface CameraTransitionCallback {
        void onApplyCameraProtection(@NotNull Path path, @NotNull Rect rect);

        void onHideCameraProtection();
    }

    public CameraAvailabilityListener(@NotNull CameraManager cameraManager2, @NotNull Path path, @NotNull String str, @NotNull String str2, @NotNull Executor executor2) {
        Intrinsics.checkParameterIsNotNull(cameraManager2, "cameraManager");
        Intrinsics.checkParameterIsNotNull(path, "cutoutProtectionPath");
        Intrinsics.checkParameterIsNotNull(str, "targetCameraId");
        Intrinsics.checkParameterIsNotNull(str2, "excludedPackages");
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        this.cameraManager = cameraManager2;
        this.cutoutProtectionPath = path;
        this.targetCameraId = str;
        this.executor = executor2;
        RectF rectF = new RectF();
        this.cutoutProtectionPath.computeBounds(rectF, false);
        this.cutoutBounds.set(MathKt__MathJVMKt.roundToInt(rectF.left), MathKt__MathJVMKt.roundToInt(rectF.top), MathKt__MathJVMKt.roundToInt(rectF.right), MathKt__MathJVMKt.roundToInt(rectF.bottom));
        this.excludedPackageIds = CollectionsKt___CollectionsKt.toSet(StringsKt__StringsKt.split$default(str2, new String[]{","}, false, 0, 6, null));
    }

    public final void startListening() {
        registerCameraListener();
    }

    public final void addTransitionCallback(@NotNull CameraTransitionCallback cameraTransitionCallback) {
        Intrinsics.checkParameterIsNotNull(cameraTransitionCallback, "callback");
        this.listeners.add(cameraTransitionCallback);
    }

    /* access modifiers changed from: private */
    public final boolean isExcluded(String str) {
        return this.excludedPackageIds.contains(str);
    }

    private final void registerCameraListener() {
        this.cameraManager.registerAvailabilityCallback(this.executor, this.availabilityCallback);
    }

    /* access modifiers changed from: private */
    public final void notifyCameraActive() {
        Iterator<T> it = this.listeners.iterator();
        while (it.hasNext()) {
            it.next().onApplyCameraProtection(this.cutoutProtectionPath, this.cutoutBounds);
        }
    }

    /* access modifiers changed from: private */
    public final void notifyCameraInactive() {
        Iterator<T> it = this.listeners.iterator();
        while (it.hasNext()) {
            it.next().onHideCameraProtection();
        }
    }

    /* compiled from: CameraAvailabilityListener.kt */
    public static final class Factory {
        private Factory() {
        }

        public /* synthetic */ Factory(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final CameraAvailabilityListener build(@NotNull Context context, @NotNull Executor executor) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(executor, "executor");
            Object systemService = context.getSystemService("camera");
            if (systemService != null) {
                CameraManager cameraManager = (CameraManager) systemService;
                Resources resources = context.getResources();
                String string = resources.getString(C0021R$string.config_frontBuiltInDisplayCutoutProtection);
                String string2 = resources.getString(C0021R$string.config_protectedCameraId);
                String string3 = resources.getString(C0021R$string.config_cameraProtectionExcludedPackages);
                Intrinsics.checkExpressionValueIsNotNull(string, "pathString");
                Path pathFromString = pathFromString(string);
                Intrinsics.checkExpressionValueIsNotNull(string2, "cameraId");
                Intrinsics.checkExpressionValueIsNotNull(string3, "excluded");
                return new CameraAvailabilityListener(cameraManager, pathFromString, string2, string3, executor);
            }
            throw new TypeCastException("null cannot be cast to non-null type android.hardware.camera2.CameraManager");
        }

        private final Path pathFromString(String str) {
            if (str != null) {
                try {
                    Path createPathFromPathData = PathParser.createPathFromPathData(StringsKt__StringsKt.trim(str).toString());
                    Intrinsics.checkExpressionValueIsNotNull(createPathFromPathData, "PathParser.createPathFromPathData(spec)");
                    return createPathFromPathData;
                } catch (Throwable th) {
                    throw new IllegalArgumentException("Invalid protection path", th);
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
            }
        }
    }
}
