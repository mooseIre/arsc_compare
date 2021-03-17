package com.android.systemui.assist;

import android.provider.DeviceConfig;
import com.android.systemui.DejankUtils;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class DeviceConfigHelper {
    public long getLong(String str, long j) {
        return ((Long) DejankUtils.whitelistIpcs(new Supplier(str, j) {
            /* class com.android.systemui.assist.$$Lambda$DeviceConfigHelper$3aQUQDpT19LyipkVjVVewd3DuU */
            public final /* synthetic */ String f$0;
            public final /* synthetic */ long f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return Long.valueOf(DeviceConfig.getLong("systemui", this.f$0, this.f$1));
            }
        })).longValue();
    }

    public int getInt(String str, int i) {
        return ((Integer) DejankUtils.whitelistIpcs(new Supplier(str, i) {
            /* class com.android.systemui.assist.$$Lambda$DeviceConfigHelper$Ng8xYHPOvZ_2ultguhmGQJUI2A */
            public final /* synthetic */ String f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return Integer.valueOf(DeviceConfig.getInt("systemui", this.f$0, this.f$1));
            }
        })).intValue();
    }

    public String getString(String str, String str2) {
        return (String) DejankUtils.whitelistIpcs(new Supplier(str, str2) {
            /* class com.android.systemui.assist.$$Lambda$DeviceConfigHelper$3D4OB5zAUMlCtZQpKS6FfDrXEDI */
            public final /* synthetic */ String f$0;
            public final /* synthetic */ String f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return DeviceConfig.getString("systemui", this.f$0, this.f$1);
            }
        });
    }

    public boolean getBoolean(String str, boolean z) {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier(str, z) {
            /* class com.android.systemui.assist.$$Lambda$DeviceConfigHelper$HWniMUF9Jobip6r9UKCXeuOiT4 */
            public final /* synthetic */ String f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Supplier
            public final Object get() {
                return Boolean.valueOf(DeviceConfig.getBoolean("systemui", this.f$0, this.f$1));
            }
        })).booleanValue();
    }

    public void addOnPropertiesChangedListener(Executor executor, DeviceConfig.OnPropertiesChangedListener onPropertiesChangedListener) {
        DeviceConfig.addOnPropertiesChangedListener("systemui", executor, onPropertiesChangedListener);
    }
}
