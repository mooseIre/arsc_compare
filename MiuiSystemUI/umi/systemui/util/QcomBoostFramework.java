package com.android.systemui.util;

import android.os.SystemProperties;
import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import miui.util.FeatureParser;

public class QcomBoostFramework {
    private static boolean isQcom = "qcom".equals(FeatureParser.getString("vendor"));
    private static Method mAcquireFunc = null;
    private static Constructor<Class> mConstructor = null;
    private static Method mIOPStart = null;
    private static Method mIOPStop = null;
    private static int mIopv2 = SystemProperties.getInt("iop.enable_uxe", 0);
    private static boolean mIsLoaded = false;
    private static Class<?> mPerfClass = null;
    private static Method mPerfHintFunc = null;
    private static Method mReleaseFunc = null;
    private static Method mReleaseHandlerFunc = null;
    private static Method mUXEngine_events = null;
    private static Method mUXEngine_trigger = null;
    private Object mPerf = null;

    public QcomBoostFramework() {
        if (isQcom) {
            synchronized (QcomBoostFramework.class) {
                if (!mIsLoaded) {
                    try {
                        mPerfClass = Class.forName("com.qualcomm.qti.Performance");
                        mAcquireFunc = mPerfClass.getMethod("perfLockAcquire", new Class[]{Integer.TYPE, int[].class});
                        mPerfHintFunc = mPerfClass.getMethod("perfHint", new Class[]{Integer.TYPE, String.class, Integer.TYPE, Integer.TYPE});
                        mReleaseFunc = mPerfClass.getMethod("perfLockRelease", new Class[0]);
                        mReleaseHandlerFunc = mPerfClass.getDeclaredMethod("perfLockReleaseHandler", new Class[]{Integer.TYPE});
                        mIOPStart = mPerfClass.getDeclaredMethod("perfIOPrefetchStart", new Class[]{Integer.TYPE, String.class, String.class});
                        mIOPStop = mPerfClass.getDeclaredMethod("perfIOPrefetchStop", new Class[0]);
                        if (mIopv2 == 1) {
                            mUXEngine_events = mPerfClass.getDeclaredMethod("perfUXEngine_events", new Class[]{Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE});
                            mUXEngine_trigger = mPerfClass.getDeclaredMethod("perfUXEngine_trigger", new Class[]{Integer.TYPE});
                        }
                        mIsLoaded = true;
                    } catch (Exception e) {
                        Log.e("BoostFramework", "BoostFramework() : Exception_1 = " + e);
                    }
                }
            }
            try {
                if (mPerfClass != null) {
                    this.mPerf = mPerfClass.newInstance();
                }
            } catch (Exception e2) {
                Log.e("BoostFramework", "BoostFramework() : Exception_2 = " + e2);
            }
        }
    }

    public int perfLockRelease() {
        if (!isQcom) {
            return -1;
        }
        try {
            return ((Integer) mReleaseFunc.invoke(this.mPerf, new Object[0])).intValue();
        } catch (Exception e) {
            Log.e("BoostFramework", "Exception " + e);
            return -1;
        }
    }

    public int perfHint(int i, String str) {
        if (!isQcom) {
            return -1;
        }
        return perfHint(i, str, -1, -1);
    }

    public int perfHint(int i, String str, int i2, int i3) {
        if (!isQcom) {
            return -1;
        }
        try {
            return ((Integer) mPerfHintFunc.invoke(this.mPerf, new Object[]{Integer.valueOf(i), str, Integer.valueOf(i2), Integer.valueOf(i3)})).intValue();
        } catch (Exception e) {
            Log.e("BoostFramework", "Exception " + e);
            return -1;
        }
    }
}
