package com.android.systemui.statusbar.notification.row;

import com.android.systemui.statusbar.notification.row.ChannelEditorDialog;
import dagger.internal.Factory;

public final class ChannelEditorDialog_Builder_Factory implements Factory<ChannelEditorDialog.Builder> {
    private static final ChannelEditorDialog_Builder_Factory INSTANCE = new ChannelEditorDialog_Builder_Factory();

    @Override // javax.inject.Provider
    public ChannelEditorDialog.Builder get() {
        return provideInstance();
    }

    public static ChannelEditorDialog.Builder provideInstance() {
        return new ChannelEditorDialog.Builder();
    }

    public static ChannelEditorDialog_Builder_Factory create() {
        return INSTANCE;
    }
}
