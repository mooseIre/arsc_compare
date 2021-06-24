package com.android.systemui.plugins.miui.controls;

import android.view.View;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = MiPlayPlugin.ACTION, version = 1)
public interface MiPlayPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_MIPLAY";
    public static final String REF_CONTROLCENTER = "controlcenter";
    public static final String REF_KEYGUARD = "keyguard";
    public static final String REF_NOTIFICATION = "notification";
    public static final int VERSION = 1;

    View createMiPlayDetailView();

    String getCastDescription();

    View getMiPlayView(MiPlayEntranceViewCallback miPlayEntranceViewCallback);

    void hideMiPlayDetailView(View view);

    void hideMiPlayView();

    boolean isAudioCasting();

    void registerCastingCallback(MiPlayCastingCallback miPlayCastingCallback);

    void showMiPlayDetailView(View view, String str);

    boolean supportMiPlayAudio();

    void unregisterCastingCallback(MiPlayCastingCallback miPlayCastingCallback);
}
