package com.android.systemui.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.util.Log;
import java.util.Map;
import kotlin.TuplesKt;
import kotlin.Unit;
import kotlin.collections.MapsKt__MapsJVMKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BackupHelper.kt */
public final class BackupHelper extends BackupAgentHelper {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final Object controlsDataLock = new Object();

    /* compiled from: BackupHelper.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final Object getControlsDataLock() {
            return BackupHelper.controlsDataLock;
        }
    }

    public void onCreate() {
        super.onCreate();
        addHelper("systemui.files_no_overwrite", new NoOverwriteFileBackupHelper(controlsDataLock, this, MapsKt__MapsJVMKt.mapOf(TuplesKt.to("controls_favorites.xml", BackupHelperKt.getPPControlsFile(this)))));
    }

    public void onRestoreFinished() {
        super.onRestoreFinished();
        Intent intent = new Intent("com.android.systemui.backup.RESTORE_FINISHED");
        intent.setPackage(getPackageName());
        intent.putExtra("android.intent.extra.USER_ID", getUserId());
        intent.setFlags(1073741824);
        sendBroadcastAsUser(intent, UserHandle.SYSTEM, "com.android.systemui.permission.SELF");
    }

    /* compiled from: BackupHelper.kt */
    private static final class NoOverwriteFileBackupHelper extends FileBackupHelper {
        @NotNull
        private final Context context;
        @NotNull
        private final Map<String, Function0<Unit>> fileNamesAndPostProcess;
        @NotNull
        private final Object lock;

        /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: java.util.Map<java.lang.String, ? extends kotlin.jvm.functions.Function0<kotlin.Unit>> */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARNING: Illegal instructions before constructor call */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public NoOverwriteFileBackupHelper(@org.jetbrains.annotations.NotNull java.lang.Object r3, @org.jetbrains.annotations.NotNull android.content.Context r4, @org.jetbrains.annotations.NotNull java.util.Map<java.lang.String, ? extends kotlin.jvm.functions.Function0<kotlin.Unit>> r5) {
            /*
                r2 = this;
                java.lang.String r0 = "lock"
                kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r3, r0)
                java.lang.String r0 = "context"
                kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r4, r0)
                java.lang.String r0 = "fileNamesAndPostProcess"
                kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r5, r0)
                java.util.Set r0 = r5.keySet()
                r1 = 0
                java.lang.String[] r1 = new java.lang.String[r1]
                java.lang.Object[] r0 = r0.toArray(r1)
                if (r0 == 0) goto L_0x002f
                java.lang.String[] r0 = (java.lang.String[]) r0
                int r1 = r0.length
                java.lang.Object[] r0 = java.util.Arrays.copyOf(r0, r1)
                java.lang.String[] r0 = (java.lang.String[]) r0
                r2.<init>(r4, r0)
                r2.lock = r3
                r2.context = r4
                r2.fileNamesAndPostProcess = r5
                return
            L_0x002f:
                kotlin.TypeCastException r2 = new kotlin.TypeCastException
                java.lang.String r3 = "null cannot be cast to non-null type kotlin.Array<T>"
                r2.<init>(r3)
                throw r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.backup.BackupHelper.NoOverwriteFileBackupHelper.<init>(java.lang.Object, android.content.Context, java.util.Map):void");
        }

        public void restoreEntity(@NotNull BackupDataInputStream backupDataInputStream) {
            Intrinsics.checkParameterIsNotNull(backupDataInputStream, "data");
            if (Environment.buildPath(this.context.getFilesDir(), new String[]{backupDataInputStream.getKey()}).exists()) {
                Log.w("BackupHelper", "File " + backupDataInputStream.getKey() + " already exists. Skipping restore.");
                return;
            }
            synchronized (this.lock) {
                super.restoreEntity(backupDataInputStream);
                Function0<Unit> function0 = this.fileNamesAndPostProcess.get(backupDataInputStream.getKey());
                if (function0 != null) {
                    function0.invoke();
                }
            }
        }

        public void performBackup(@Nullable ParcelFileDescriptor parcelFileDescriptor, @Nullable BackupDataOutput backupDataOutput, @Nullable ParcelFileDescriptor parcelFileDescriptor2) {
            synchronized (this.lock) {
                super.performBackup(parcelFileDescriptor, backupDataOutput, parcelFileDescriptor2);
                Unit unit = Unit.INSTANCE;
            }
        }
    }
}
