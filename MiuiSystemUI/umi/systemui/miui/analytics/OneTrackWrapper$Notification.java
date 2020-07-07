package com.android.systemui.miui.analytics;

import android.content.Context;
import com.android.systemui.Constants;
import com.xiaomi.onetrack.Configuration;
import com.xiaomi.onetrack.OneTrack;
import java.util.Map;

public class OneTrackWrapper$Notification {
    private static OneTrack sOneTrack;

    public static void init(Context context) {
        Configuration.Builder builder = new Configuration.Builder();
        builder.setAppId("30000000039");
        builder.setChannel(AnalyticsWrapper.resolveChannelName());
        builder.setMode(OneTrack.Mode.APP);
        builder.setAutoTrackActivityAction(false);
        sOneTrack = OneTrack.createInstance(context, builder.build());
        OneTrack.setDebugMode(Constants.DEBUG);
    }

    public static void track(String str, Map<String, Object> map) {
        sOneTrack.track(str, map);
    }
}
