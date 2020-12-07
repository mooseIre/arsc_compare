package com.android.keyguard;

import android.content.Context;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.AwesomeLockScreenImp.AwesomeLockScreenView;
import com.android.keyguard.AwesomeLockScreenImp.LockScreenElementFactory;
import com.android.keyguard.AwesomeLockScreenImp.LockScreenResourceLoader;
import com.android.keyguard.AwesomeLockScreenImp.LockScreenRoot;
import com.android.keyguard.AwesomeLockScreenImp.SuperWallpaperLockScreenResourceLoader;
import java.io.File;
import java.util.Stack;
import miui.content.res.ThemeResources;
import miui.maml.LifecycleResourceManager;
import miui.maml.ResourceLoader;
import miui.maml.ScreenContext;

public class RootHolder {
    private ScreenContext mContext;
    private ResourceLoader mLockScreenResourceLoader;
    private LifecycleResourceManager mResourceMgr;
    private LockScreenRoot mRoot;
    private String mTempCachePath;
    private Stack<AwesomeLockScreen> mViewList = new Stack<>();

    public boolean init(Context context, AwesomeLockScreen awesomeLockScreen) {
        if (this.mTempCachePath == null) {
            this.mTempCachePath = context.getCacheDir() + File.separator + "lockscreen_cache";
        }
        if (this.mRoot == null) {
            ThemeResources.getSystem().resetLockscreen();
            if (ThemeResources.getSystem().hasSuperWallpaperLockscreen()) {
                Log.d("RootHolder", "create SuperWallpaperLockScreenResourceLoader");
                this.mLockScreenResourceLoader = new SuperWallpaperLockScreenResourceLoader();
            } else {
                Log.d("RootHolder", "create LockScreenResourceLoader");
                this.mLockScreenResourceLoader = new LockScreenResourceLoader();
            }
            this.mResourceMgr = new LifecycleResourceManager(this.mLockScreenResourceLoader.setLocal(context.getResources().getConfiguration().locale), 86400000, 3600000);
            this.mResourceMgr.setCacheSize(((int) Runtime.getRuntime().maxMemory()) / 2);
            this.mContext = new ScreenContext(context, this.mResourceMgr, new LockScreenElementFactory());
            LockScreenRoot lockScreenRoot = new LockScreenRoot(this.mContext);
            this.mRoot = lockScreenRoot;
            lockScreenRoot.setConfig("/data/system/theme/config.config");
            this.mRoot.setCacheDir(this.mTempCachePath);
            if (!this.mRoot.load()) {
                Slog.e("RootHolder", "fail to load element root");
                this.mRoot = null;
                return false;
            }
            this.mRoot.setAutoDarkenWallpaper(true);
            Log.d("RootHolder", "create root");
        } else {
            this.mResourceMgr.setLocal(context.getResources().getConfiguration().locale);
        }
        this.mViewList.push(awesomeLockScreen);
        Log.d("RootHolder", "init:" + awesomeLockScreen.toString());
        return true;
    }

    public void clear() {
        this.mRoot = null;
        this.mContext = null;
        LifecycleResourceManager lifecycleResourceManager = this.mResourceMgr;
        if (lifecycleResourceManager != null) {
            lifecycleResourceManager.finish(false);
            this.mResourceMgr = null;
        }
        if (this.mTempCachePath != null) {
            new File(this.mTempCachePath).delete();
        }
    }

    public ScreenContext getContext() {
        return this.mContext;
    }

    public LockScreenRoot getRoot() {
        return this.mRoot;
    }

    public AwesomeLockScreenView createView(Context context) {
        if (this.mRoot == null) {
            return null;
        }
        AwesomeLockScreenView awesomeLockScreenView = new AwesomeLockScreenView(context, this.mRoot);
        Log.d("RootHolder", "createView");
        return awesomeLockScreenView;
    }

    public void cleanUp(AwesomeLockScreen awesomeLockScreen) {
        if (this.mRoot != null) {
            this.mViewList.remove(awesomeLockScreen);
            awesomeLockScreen.cleanUpView();
            Log.d("RootHolder", "cleanUp: " + awesomeLockScreen.toString() + " size:" + this.mViewList.size());
            if (this.mViewList.size() == 0) {
                this.mRoot.getContext().mVariables.reset();
                this.mRoot = null;
                Log.d("RootHolder", "cleanUp finish");
                return;
            }
            this.mViewList.peek().rebindView();
        }
    }
}
