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
        if (Intrinsics.areEqual(this.this$0.targetCameraId, str)) {
            this.this$0.notifyCameraInactive();
        }
    }

    public void onCameraOpened(@NotNull String str, @NotNull String str2) {
        Intrinsics.checkParameterIsNotNull(str, "cameraId");
        Intrinsics.checkParameterIsNotNull(str2, "packageId");
        if (Intrinsics.areEqual(this.this$0.targetCameraId, str) && !(this.this$0.isExcluded(str2))) {
            this.this$0.notifyCameraActive();
        }
    }
}
