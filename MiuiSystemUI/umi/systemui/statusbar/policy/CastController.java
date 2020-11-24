package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;
import java.util.List;

public interface CastController extends CallbackController<Callback>, Dumpable {

    public interface Callback {
        void onCastDevicesChanged();
    }

    public static final class CastDevice {
        public String id;
        public String name;
        public int state = 0;
        public Object tag;
    }

    List<CastDevice> getCastDevices();

    void setCurrentUserId(int i);

    void setDiscovering(boolean z);

    void startCasting(CastDevice castDevice);

    void stopCasting(CastDevice castDevice);
}
