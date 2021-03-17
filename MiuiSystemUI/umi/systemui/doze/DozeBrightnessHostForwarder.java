package com.android.systemui.doze;

import com.android.systemui.doze.DozeMachine;

public class DozeBrightnessHostForwarder extends DozeMachine.Service.Delegate {
    private final DozeHost mHost;

    public DozeBrightnessHostForwarder(DozeMachine.Service service, DozeHost dozeHost) {
        super(service);
        this.mHost = dozeHost;
    }

    @Override // com.android.systemui.doze.DozeMachine.Service, com.android.systemui.doze.DozeMachine.Service.Delegate
    public void setDozeScreenBrightness(int i) {
        super.setDozeScreenBrightness(i);
        this.mHost.setDozeScreenBrightness(i);
    }
}
