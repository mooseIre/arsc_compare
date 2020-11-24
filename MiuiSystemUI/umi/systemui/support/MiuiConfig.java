package com.android.systemui.support;

import miui.util.FeatureParser;

public class MiuiConfig {
    public static final boolean IS_QCOM = "qcom".equals(FeatureParser.getString("vendor"));

    static {
        "mediatek".equals(FeatureParser.getString("vendor"));
    }
}
