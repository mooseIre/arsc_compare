package com.android.systemui.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;
import com.android.systemui.ConfigurationChangedReceiver;

public class FragmentService implements ConfigurationChangedReceiver {
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private final ArrayMap<View, FragmentHostState> mHosts = new ArrayMap<>();

    public FragmentService(Context context) {
        this.mContext = context;
    }

    public FragmentHostManager getFragmentHostManager(View view, boolean z) {
        if (!z) {
            view = view.getRootView();
        }
        FragmentHostState fragmentHostState = this.mHosts.get(view);
        if (fragmentHostState == null) {
            fragmentHostState = new FragmentHostState(view);
            this.mHosts.put(view, fragmentHostState);
        }
        return fragmentHostState.getFragmentHostManager();
    }

    public void onConfigurationChanged(Configuration configuration) {
        for (FragmentHostState sendConfigurationChange : this.mHosts.values()) {
            sendConfigurationChange.sendConfigurationChange(configuration);
        }
    }

    private class FragmentHostState {
        private FragmentHostManager mFragmentHostManager;
        private final View mView;

        public FragmentHostState(View view) {
            this.mView = view;
            this.mFragmentHostManager = new FragmentHostManager(FragmentService.this.mContext, FragmentService.this, this.mView);
        }

        public void sendConfigurationChange(final Configuration configuration) {
            FragmentService.this.mHandler.post(new Runnable() {
                public void run() {
                    FragmentHostState.this.handleSendConfigurationChange(configuration);
                }
            });
        }

        public FragmentHostManager getFragmentHostManager() {
            return this.mFragmentHostManager;
        }

        /* access modifiers changed from: private */
        public void handleSendConfigurationChange(Configuration configuration) {
            this.mFragmentHostManager.onConfigurationChanged(configuration);
        }
    }
}
