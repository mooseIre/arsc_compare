package com.android.systemui.statusbar.notification.people;

import com.android.systemui.plugins.ActivityStarter;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleHubViewController.kt */
public final class PeopleHubViewModelFactoryDataSourceImpl implements DataSource<Object> {
    public PeopleHubViewModelFactoryDataSourceImpl(@NotNull ActivityStarter activityStarter, @NotNull DataSource<Object> dataSource) {
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(dataSource, "dataSource");
    }
}
