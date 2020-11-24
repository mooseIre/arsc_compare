package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.policy.RegionController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;

public class RegionController implements Dumpable {
    /* access modifiers changed from: private */
    public ArrayList<Callback> mCallbacks = new ArrayList<>();
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            RegionController.this.updateRegion();
            RegionController.this.mCallbacks.forEach(new Consumer() {
                public final void accept(Object obj) {
                    RegionController.AnonymousClass1.this.lambda$onReceive$0$RegionController$1((RegionController.Callback) obj);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onReceive$0 */
        public /* synthetic */ void lambda$onReceive$0$RegionController$1(Callback callback) {
            callback.onRegionChanged(RegionController.this.mRegion);
        }
    };
    /* access modifiers changed from: private */
    public String mRegion;

    public interface Callback {
        void onRegionChanged(String str);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public RegionController(Context context) {
        updateRegion();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.MIUI_REGION_CHANGED");
        context.registerReceiver(this.mReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public void updateRegion() {
        this.mRegion = SystemProperties.get("ro.miui.region", "");
    }

    public void addCallback(Callback callback) {
        if (callback != null) {
            if (!this.mCallbacks.contains(callback)) {
                this.mCallbacks.add(callback);
            }
            callback.onRegionChanged(this.mRegion);
        }
    }

    public void removeCallback(Callback callback) {
        this.mCallbacks.remove(callback);
    }
}
