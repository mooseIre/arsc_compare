package com.android.systemui.controls.controller;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.backup.BackupHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import kotlin.Pair;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AuxiliaryPersistenceWrapper.kt */
public final class AuxiliaryPersistenceWrapper {
    @NotNull
    private List<StructureInfo> favorites;
    private ControlsFavoritePersistenceWrapper persistenceWrapper;

    @VisibleForTesting
    public AuxiliaryPersistenceWrapper(@NotNull ControlsFavoritePersistenceWrapper controlsFavoritePersistenceWrapper) {
        Intrinsics.checkParameterIsNotNull(controlsFavoritePersistenceWrapper, "wrapper");
        this.persistenceWrapper = controlsFavoritePersistenceWrapper;
        this.favorites = CollectionsKt__CollectionsKt.emptyList();
        initialize();
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public AuxiliaryPersistenceWrapper(@NotNull File file, @NotNull Executor executor) {
        this(new ControlsFavoritePersistenceWrapper(file, executor, null, 4, null));
        Intrinsics.checkParameterIsNotNull(file, "file");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
    }

    @NotNull
    public final List<StructureInfo> getFavorites() {
        return this.favorites;
    }

    public final void changeFile(@NotNull File file) {
        Intrinsics.checkParameterIsNotNull(file, "file");
        this.persistenceWrapper.changeFileAndBackupManager(file, null);
        initialize();
    }

    public final void initialize() {
        List<StructureInfo> list;
        if (this.persistenceWrapper.getFileExists()) {
            list = this.persistenceWrapper.readFavorites();
        } else {
            list = CollectionsKt__CollectionsKt.emptyList();
        }
        this.favorites = list;
    }

    @NotNull
    public final List<StructureInfo> getCachedFavoritesAndRemoveFor(@NotNull ComponentName componentName) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        if (!this.persistenceWrapper.getFileExists()) {
            return CollectionsKt__CollectionsKt.emptyList();
        }
        List<StructureInfo> list = this.favorites;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (T t : list) {
            if (Intrinsics.areEqual(t.getComponentName(), componentName)) {
                arrayList.add(t);
            } else {
                arrayList2.add(t);
            }
        }
        Pair pair = new Pair(arrayList, arrayList2);
        List<StructureInfo> list2 = (List) pair.component1();
        List<StructureInfo> list3 = (List) pair.component2();
        this.favorites = list3;
        if (!list3.isEmpty()) {
            this.persistenceWrapper.storeFavorites(list3);
        } else {
            this.persistenceWrapper.deleteFile();
        }
        return list2;
    }

    /* compiled from: AuxiliaryPersistenceWrapper.kt */
    public static final class DeletionJobService extends JobService {
        public static final Companion Companion = new Companion(null);
        private static final int DELETE_FILE_JOB_ID = 1000;
        private static final long WEEK_IN_MILLIS = TimeUnit.DAYS.toMillis(7);

        public boolean onStopJob(@Nullable JobParameters jobParameters) {
            return true;
        }

        /* compiled from: AuxiliaryPersistenceWrapper.kt */
        public static final class Companion {
            @VisibleForTesting
            public static /* synthetic */ void DELETE_FILE_JOB_ID$annotations() {
            }

            private Companion() {
            }

            public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
                this();
            }

            public final int getDELETE_FILE_JOB_ID$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
                return DeletionJobService.DELETE_FILE_JOB_ID;
            }

            @NotNull
            public final JobInfo getJobForContext(@NotNull Context context) {
                Intrinsics.checkParameterIsNotNull(context, "context");
                JobInfo build = new JobInfo.Builder(getDELETE_FILE_JOB_ID$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() + context.getUserId(), new ComponentName(context, DeletionJobService.class)).setMinimumLatency(DeletionJobService.WEEK_IN_MILLIS).setPersisted(true).build();
                Intrinsics.checkExpressionValueIsNotNull(build, "JobInfo.Builder(jobId, c…                 .build()");
                return build;
            }
        }

        @VisibleForTesting
        public final void attachContext(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            attachBaseContext(context);
        }

        public boolean onStartJob(@NotNull JobParameters jobParameters) {
            Intrinsics.checkParameterIsNotNull(jobParameters, "params");
            synchronized (BackupHelper.Companion.getControlsDataLock()) {
                getBaseContext().deleteFile("aux_controls_favorites.xml");
            }
            return false;
        }
    }
}
