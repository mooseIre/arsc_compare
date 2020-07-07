package com.android.systemui.fragments;

import android.app.Fragment;
import android.app.FragmentController;
import android.app.FragmentHostCallback;
import android.app.FragmentManager;
import android.app.FragmentManagerNonConfig;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.util.InterestingConfigChanges;
import com.android.systemui.util.leak.LeakDetector;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class FragmentHostManager {
    private final InterestingConfigChanges mConfigChanges = new InterestingConfigChanges(-1073741052);
    /* access modifiers changed from: private */
    public final Context mContext;
    private FragmentController mFragments;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler(Looper.getMainLooper());
    private FragmentManager.FragmentLifecycleCallbacks mLifecycleCallbacks;
    private final HashMap<String, ArrayList<FragmentListener>> mListeners = new HashMap<>();
    /* access modifiers changed from: private */
    public final PluginFragmentManager mPlugins = new PluginFragmentManager();
    private final View mRootView;

    public interface FragmentListener {
        void onFragmentViewCreated(String str, Fragment fragment);

        void onFragmentViewDestroyed(String str, Fragment fragment);
    }

    /* access modifiers changed from: private */
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    FragmentHostManager(Context context, FragmentService fragmentService, View view) {
        this.mContext = context;
        this.mRootView = view;
        this.mConfigChanges.applyNewConfig(context.getResources());
        createFragmentHost((Parcelable) null);
    }

    /* access modifiers changed from: private */
    public void createFragmentHost(Parcelable parcelable) {
        FragmentController createController = FragmentController.createController(new HostCallbacks());
        this.mFragments = createController;
        createController.attachHost((Fragment) null);
        this.mLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            public void onFragmentViewCreated(FragmentManager fragmentManager, Fragment fragment, View view, Bundle bundle) {
                FragmentHostManager.this.onFragmentViewCreated(fragment);
            }

            public void onFragmentViewDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                FragmentHostManager.this.onFragmentViewDestroyed(fragment);
            }

            public void onFragmentDestroyed(FragmentManager fragmentManager, Fragment fragment) {
                ((LeakDetector) Dependency.get(LeakDetector.class)).trackGarbage(fragment);
            }
        };
        getFragmentManager().registerFragmentLifecycleCallbacks(this.mLifecycleCallbacks, true);
        if (parcelable != null) {
            this.mFragments.restoreAllState(parcelable, (FragmentManagerNonConfig) null);
        }
        this.mFragments.dispatchCreate();
        this.mFragments.dispatchStart();
        this.mFragments.dispatchResume();
    }

    /* access modifiers changed from: private */
    public Parcelable destroyFragmentHost() {
        this.mFragments.dispatchPause();
        Parcelable saveAllState = this.mFragments.saveAllState();
        this.mFragments.dispatchStop();
        this.mFragments.dispatchDestroy();
        getFragmentManager().unregisterFragmentLifecycleCallbacks(this.mLifecycleCallbacks);
        return saveAllState;
    }

    public FragmentHostManager addTagListener(String str, FragmentListener fragmentListener) {
        ArrayList arrayList = this.mListeners.get(str);
        if (arrayList == null) {
            arrayList = new ArrayList();
            this.mListeners.put(str, arrayList);
        }
        arrayList.add(fragmentListener);
        Fragment findFragmentByTag = getFragmentManager().findFragmentByTag(str);
        if (!(findFragmentByTag == null || findFragmentByTag.getView() == null)) {
            fragmentListener.onFragmentViewCreated(str, findFragmentByTag);
        }
        return this;
    }

    public void removeTagListener(String str, FragmentListener fragmentListener) {
        ArrayList arrayList = this.mListeners.get(str);
        if (arrayList != null && arrayList.remove(fragmentListener) && arrayList.size() == 0) {
            this.mListeners.remove(str);
        }
    }

    /* access modifiers changed from: private */
    public void onFragmentViewCreated(Fragment fragment) {
        String tag = fragment.getTag();
        ArrayList arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((FragmentListener) it.next()).onFragmentViewCreated(tag, fragment);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onFragmentViewDestroyed(Fragment fragment) {
        String tag = fragment.getTag();
        ArrayList arrayList = this.mListeners.get(tag);
        if (arrayList != null) {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ((FragmentListener) it.next()).onFragmentViewDestroyed(tag, fragment);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        if (this.mConfigChanges.applyNewConfig(this.mContext.getResources())) {
            reloadFragment();
        } else {
            this.mFragments.dispatchConfigurationChanged(configuration);
        }
    }

    public void reloadFragment() {
        createFragmentHost(destroyFragmentHost());
    }

    /* access modifiers changed from: private */
    public <T extends View> T findViewById(int i) {
        return this.mRootView.findViewById(i);
    }

    public FragmentManager getFragmentManager() {
        return this.mFragments.getFragmentManager();
    }

    /* access modifiers changed from: package-private */
    public PluginFragmentManager getPluginManager() {
        return this.mPlugins;
    }

    public static FragmentHostManager get(View view) {
        return get(view, false);
    }

    public static FragmentHostManager get(View view, boolean z) {
        try {
            return ((FragmentService) Dependency.get(FragmentService.class)).getFragmentHostManager(view, z);
        } catch (ClassCastException e) {
            throw e;
        }
    }

    class HostCallbacks extends FragmentHostCallback<FragmentHostManager> {
        public int onGetWindowAnimations() {
            return 0;
        }

        public boolean onHasView() {
            return true;
        }

        public boolean onHasWindowAnimations() {
            return false;
        }

        public boolean onShouldSaveFragmentState(Fragment fragment) {
            return true;
        }

        public HostCallbacks() {
            super(FragmentHostManager.this.mContext, FragmentHostManager.this.mHandler, 0);
        }

        public FragmentHostManager onGetHost() {
            return FragmentHostManager.this;
        }

        public void onDump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            FragmentHostManager.this.dump(str, fileDescriptor, printWriter, strArr);
        }

        public Fragment instantiate(Context context, String str, Bundle bundle) {
            return FragmentHostManager.this.mPlugins.instantiate(context, str, bundle);
        }

        public LayoutInflater onGetLayoutInflater() {
            return LayoutInflater.from(FragmentHostManager.this.mContext).cloneInContext(FragmentHostManager.this.mContext);
        }

        public View onFindViewById(int i) {
            return FragmentHostManager.this.findViewById(i);
        }
    }

    class PluginFragmentManager {
        private final ArrayMap<String, Context> mPluginLookup = new ArrayMap<>();

        PluginFragmentManager() {
        }

        public void removePlugin(String str, String str2, String str3) {
            Fragment findFragmentByTag = FragmentHostManager.this.getFragmentManager().findFragmentByTag(str);
            this.mPluginLookup.remove(str2);
            FragmentHostManager.this.getFragmentManager().beginTransaction().replace(((View) findFragmentByTag.getView().getParent()).getId(), instantiate(FragmentHostManager.this.mContext, str3, (Bundle) null), str).commit();
            reloadFragments();
        }

        public void setCurrentPlugin(String str, String str2, Context context) {
            Fragment findFragmentByTag = FragmentHostManager.this.getFragmentManager().findFragmentByTag(str);
            this.mPluginLookup.put(str2, context);
            FragmentHostManager.this.getFragmentManager().beginTransaction().replace(((View) findFragmentByTag.getView().getParent()).getId(), instantiate(context, str2, (Bundle) null), str).commit();
            reloadFragments();
        }

        private void reloadFragments() {
            FragmentHostManager.this.createFragmentHost(FragmentHostManager.this.destroyFragmentHost());
        }

        /* access modifiers changed from: package-private */
        public Fragment instantiate(Context context, String str, Bundle bundle) {
            Context context2 = this.mPluginLookup.get(str);
            if (context2 == null) {
                return Fragment.instantiate(context, str, bundle);
            }
            Fragment instantiate = Fragment.instantiate(context2, str, bundle);
            if (instantiate instanceof Plugin) {
                ((Plugin) instantiate).onCreate(FragmentHostManager.this.mContext, context2);
            }
            return instantiate;
        }
    }
}
