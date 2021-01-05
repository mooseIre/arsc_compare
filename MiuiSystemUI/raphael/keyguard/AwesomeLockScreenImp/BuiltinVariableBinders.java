package com.android.keyguard.AwesomeLockScreenImp;

import miui.maml.data.VariableBinderManager;

public class BuiltinVariableBinders {
    public static void fill(VariableBinderManager variableBinderManager) {
        fillMissedCall(variableBinderManager);
        fillUnreadSms(variableBinderManager);
    }

    private static void fillMissedCall(VariableBinderManager variableBinderManager) {
        variableBinderManager.addContentProviderBinder("content://call_log/calls").setColumns(new String[]{"_id", "number"}).setWhere("type=3 AND new=1").setCountName("call_missed_count");
    }

    private static void fillUnreadSms(VariableBinderManager variableBinderManager) {
        variableBinderManager.addContentProviderBinder("content://sms/inbox").setColumns(new String[]{"_id"}).setWhere("seen=0 AND read=0").setCountName("sms_unread_count");
    }
}
