package com.android.systemui.statusbar.notification.row;

import android.app.INotificationManager;
import android.content.Context;
import com.android.systemui.statusbar.notification.row.ChannelEditorDialog;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ChannelEditorDialogController_Factory implements Factory<ChannelEditorDialogController> {
    private final Provider<Context> cProvider;
    private final Provider<ChannelEditorDialog.Builder> dialogBuilderProvider;
    private final Provider<INotificationManager> noManProvider;

    public ChannelEditorDialogController_Factory(Provider<Context> provider, Provider<INotificationManager> provider2, Provider<ChannelEditorDialog.Builder> provider3) {
        this.cProvider = provider;
        this.noManProvider = provider2;
        this.dialogBuilderProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ChannelEditorDialogController get() {
        return provideInstance(this.cProvider, this.noManProvider, this.dialogBuilderProvider);
    }

    public static ChannelEditorDialogController provideInstance(Provider<Context> provider, Provider<INotificationManager> provider2, Provider<ChannelEditorDialog.Builder> provider3) {
        return new ChannelEditorDialogController(provider.get(), provider2.get(), provider3.get());
    }

    public static ChannelEditorDialogController_Factory create(Provider<Context> provider, Provider<INotificationManager> provider2, Provider<ChannelEditorDialog.Builder> provider3) {
        return new ChannelEditorDialogController_Factory(provider, provider2, provider3);
    }
}
