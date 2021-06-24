package com.android.systemui.controlcenter.phone.controls;

import android.content.Context;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.miui.controls.MiPlayCastingCallback;
import com.android.systemui.plugins.miui.controls.MiPlayEntranceViewCallback;
import com.android.systemui.plugins.miui.controls.MiPlayPlugin;
import com.android.systemui.shared.plugins.PluginManager;
import java.util.ArrayList;
import java.util.Iterator;

public class MiPlayPluginManager implements PluginListener<MiPlayPlugin> {
    private static int STATE_ADDED = 1;
    private static int STATE_ADDING = 0;
    private static int STATE_REMOVED = 3;
    private int currentState = STATE_REMOVED;
    ArrayList<MiPlayCastingCallback> mCastingCallbacks = new ArrayList<>();
    ArrayList<PluginListener<MiPlayPlugin>> mExtraListener = new ArrayList<>();
    private MiPlayPlugin mMiPlayPlugin;

    public void addControlsPluginListener() {
        if (this.mMiPlayPlugin == null) {
            ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener((PluginListener) this, MiPlayPlugin.class, true);
        }
        this.currentState = STATE_ADDING;
    }

    public View createMiPlayView(MiPlayEntranceViewCallback miPlayEntranceViewCallback) {
        MiPlayPlugin miPlayPlugin = this.mMiPlayPlugin;
        if (miPlayPlugin == null) {
            return null;
        }
        try {
            return miPlayPlugin.getMiPlayView(miPlayEntranceViewCallback);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void hideMiPlayView() {
        MiPlayPlugin miPlayPlugin = this.mMiPlayPlugin;
        if (miPlayPlugin != null) {
            miPlayPlugin.hideMiPlayView();
        }
    }

    public View getMiPlayDetailView() {
        MiPlayPlugin miPlayPlugin = this.mMiPlayPlugin;
        if (miPlayPlugin == null) {
            return null;
        }
        try {
            return miPlayPlugin.createMiPlayDetailView();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void showMiPlayDetailView(View view, String str) {
        MiPlayPlugin miPlayPlugin = this.mMiPlayPlugin;
        if (miPlayPlugin != null) {
            miPlayPlugin.showMiPlayDetailView(view, str);
        }
    }

    public void hideMiPlayDetailView(View view) {
        MiPlayPlugin miPlayPlugin = this.mMiPlayPlugin;
        if (miPlayPlugin != null) {
            miPlayPlugin.hideMiPlayDetailView(view);
        }
    }

    public boolean supportMiPlayAudio() {
        MiPlayPlugin miPlayPlugin = this.mMiPlayPlugin;
        if (miPlayPlugin != null) {
            return miPlayPlugin.supportMiPlayAudio();
        }
        return false;
    }

    public void registerCastingCallback(MiPlayCastingCallback miPlayCastingCallback) {
        if (this.mMiPlayPlugin != null) {
            this.mCastingCallbacks.add(miPlayCastingCallback);
            this.mMiPlayPlugin.registerCastingCallback(miPlayCastingCallback);
        }
    }

    public void unregisterCastingCallback(MiPlayCastingCallback miPlayCastingCallback) {
        if (this.mMiPlayPlugin != null) {
            this.mCastingCallbacks.remove(miPlayCastingCallback);
            this.mMiPlayPlugin.unregisterCastingCallback(miPlayCastingCallback);
        }
    }

    public void onPluginConnected(MiPlayPlugin miPlayPlugin, Context context) {
        this.currentState = STATE_ADDED;
        this.mMiPlayPlugin = miPlayPlugin;
        Iterator<PluginListener<MiPlayPlugin>> it = this.mExtraListener.iterator();
        while (it.hasNext()) {
            it.next().onPluginConnected(miPlayPlugin, context);
        }
        Iterator<MiPlayCastingCallback> it2 = this.mCastingCallbacks.iterator();
        while (it2.hasNext()) {
            this.mMiPlayPlugin.registerCastingCallback(it2.next());
        }
    }

    public void onPluginDisconnected(MiPlayPlugin miPlayPlugin) {
        Iterator<PluginListener<MiPlayPlugin>> it = this.mExtraListener.iterator();
        while (it.hasNext()) {
            it.next().onPluginDisconnected(miPlayPlugin);
        }
        this.mMiPlayPlugin = null;
        if (this.currentState == STATE_ADDING) {
            this.currentState = STATE_REMOVED;
            addControlsPluginListener();
            return;
        }
        this.currentState = STATE_REMOVED;
    }

    public void addExtraListener(PluginListener<MiPlayPlugin> pluginListener) {
        this.mExtraListener.add(pluginListener);
    }

    public void removeExtraListener(PluginListener<MiPlayPlugin> pluginListener) {
        this.mExtraListener.remove(pluginListener);
    }
}
