package com.android.systemui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.FragmentBase;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;

public class PluginFragmentListener implements PluginListener<Plugin> {
    private final Class<? extends Fragment> mDefaultClass;
    private final Class<? extends FragmentBase> mExpectedInterface;
    private final FragmentHostManager mFragmentHostManager;
    private final PluginManager mPluginManager = ((PluginManager) Dependency.get(PluginManager.class));
    private final String mTag;

    public PluginFragmentListener(View view, String str, Class<? extends Fragment> cls, Class<? extends FragmentBase> cls2) {
        this.mTag = str;
        this.mFragmentHostManager = FragmentHostManager.get(view);
        this.mExpectedInterface = cls2;
        this.mDefaultClass = cls;
    }

    public void startListening() {
        this.mPluginManager.addPluginListener(this, (Class<?>) this.mExpectedInterface, false);
    }

    public void onPluginConnected(Plugin plugin, Context context) {
        try {
            this.mExpectedInterface.cast(plugin);
            Fragment.class.cast(plugin);
            this.mFragmentHostManager.getPluginManager().setCurrentPlugin(this.mTag, plugin.getClass().getName(), context);
        } catch (ClassCastException e) {
            Log.e("PluginFragmentListener", plugin.getClass().getName() + " must be a Fragment and implement " + this.mExpectedInterface.getName(), e);
        }
    }

    public void onPluginDisconnected(Plugin plugin) {
        this.mFragmentHostManager.getPluginManager().removePlugin(this.mTag, plugin.getClass().getName(), this.mDefaultClass.getName());
    }
}
