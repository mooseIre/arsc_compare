package com.android.keyguard.doze;

import android.service.dreams.DreamService;

public class DozeService extends DreamService {
    public void onCreate() {
        super.onCreate();
        finish();
    }

    public void onDreamingStarted() {
        super.onDreamingStarted();
    }

    public void onDreamingStopped() {
        super.onDreamingStopped();
    }
}
