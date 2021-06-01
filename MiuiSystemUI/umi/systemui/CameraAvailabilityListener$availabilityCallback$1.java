package com.android.systemui;

import android.hardware.camera2.CameraManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CameraAvailabilityListener.kt */
public final class CameraAvailabilityListener$availabilityCallback$1 extends CameraManager.AvailabilityCallback {
    final /* synthetic */ CameraAvailabilityListener this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    CameraAvailabilityListener$availabilityCallback$1(CameraAvailabilityListener cameraAvailabilityListener) {
        this.this$0 = cameraAvailabilityListener;
    }

    public void onCameraClosed(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "cameraId");
        if (Intrinsics.areEqual(CameraAvailabilityListener.access$getTargetCameraId$p(this.this$0), str)) {
            CameraAvailabilityListener.access$notifyCameraInactive(this.this$0);
        }
    }

    public void onCameraOpened(@NotNull String str, @NotNull String str2) {
        Intrinsics.checkParameterIsNotNull(str, "cameraId");
        Intrinsics.checkParameterIsNotNull(str2, "packageId");
        if (Intrinsics.areEqual(CameraAvailabilityListener.access$getTargetCameraId$p(this.this$0), str) && !CameraAvailabilityListener.access$isExcluded(this.this$0, str2)) {
            CameraAvailabilityListener.access$notifyCameraActive(this.this$0);
        }
    }
}
