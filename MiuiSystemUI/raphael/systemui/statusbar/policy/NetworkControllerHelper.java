package com.android.systemui.statusbar.policy;

import android.telephony.SubscriptionInfo;
import android.text.TextUtils;

public class NetworkControllerHelper {
    public static boolean equalSubscription(SubscriptionInfo subscriptionInfo, SubscriptionInfo subscriptionInfo2) {
        if (subscriptionInfo == subscriptionInfo2) {
            return true;
        }
        if (subscriptionInfo == null || subscriptionInfo2 == null) {
            return false;
        }
        return TextUtils.equals(subscriptionInfo.getMccString(), subscriptionInfo2.getMccString()) && TextUtils.equals(subscriptionInfo.getMncString(), subscriptionInfo2.getMncString()) && TextUtils.equals(subscriptionInfo.getNumber(), subscriptionInfo2.getNumber()) && TextUtils.equals(subscriptionInfo.getDisplayName(), subscriptionInfo2.getDisplayName());
    }
}
