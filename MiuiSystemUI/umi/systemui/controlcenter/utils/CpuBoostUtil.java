package com.android.systemui.controlcenter.utils;

import android.os.Process;
import android.util.Log;
import com.mi.mibridge.MiBridge;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CpuBoostUtil {
    private static final Executor BOOST_CPU_EXECUTOR = Executors.newSingleThreadExecutor();
    private static CpuBoostUtil sInstance;
    private boolean mAllowBoostCpu = false;

    private CpuBoostUtil() {
        checkCPUBooster();
    }

    public static CpuBoostUtil getInstance() {
        if (sInstance == null) {
            synchronized (CpuBoostUtil.class) {
                if (sInstance == null) {
                    sInstance = new CpuBoostUtil();
                }
            }
        }
        return sInstance;
    }

    private void checkCPUBooster() {
        try {
            this.mAllowBoostCpu = MiBridge.checkPermission("com.android.systemui", Process.myUid());
            Log.d("CpuBoostUtil", "allowBoostCpu:" + this.mAllowBoostCpu);
        } catch (Exception e) {
            Log.d("CpuBoostUtil", "checkPermission", e);
        }
    }

    public void boostCpuToMax(int i) {
        if (this.mAllowBoostCpu) {
            BOOST_CPU_EXECUTOR.execute(new Runnable(i) {
                /* class com.android.systemui.controlcenter.utils.$$Lambda$CpuBoostUtil$UGJF9rgQNWBq3nnSNAmgL3_uSc */
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    CpuBoostUtil.lambda$boostCpuToMax$0(this.f$0);
                }
            });
        }
    }

    static /* synthetic */ void lambda$boostCpuToMax$0(int i) {
        try {
            int requestCpuHighFreq = MiBridge.requestCpuHighFreq(Process.myUid(), 1, i);
            Log.d("CpuBoostUtil", "boostCpuToMax:" + requestCpuHighFreq);
        } catch (Exception e) {
            Log.d("CpuBoostUtil", "boostCpuToMax", e);
        }
    }

    public void cancelBoostCpu() {
        if (this.mAllowBoostCpu) {
            BOOST_CPU_EXECUTOR.execute($$Lambda$CpuBoostUtil$kdLURa4k_uFMNVdBwdBL5o2erCQ.INSTANCE);
        }
    }

    static /* synthetic */ void lambda$cancelBoostCpu$1() {
        try {
            int cancelCpuHighFreq = MiBridge.cancelCpuHighFreq(Process.myUid());
            Log.d("CpuBoostUtil", "cancelCpuHighFreq:" + cancelCpuHighFreq);
        } catch (Exception e) {
            Log.d("CpuBoostUtil", "cancelCpuHighFreq", e);
        }
    }
}
