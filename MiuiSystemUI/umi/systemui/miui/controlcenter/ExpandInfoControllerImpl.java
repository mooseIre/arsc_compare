package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.statusbar.ControlCenterActivityStarter;
import com.android.systemui.miui.statusbar.phone.ControlPanelContentView;
import com.miui.systemui.annotation.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import miui.view.MiuiHapticFeedbackConstants;

public class ExpandInfoControllerImpl implements ExpandInfoController {
    private ControlCenterActivityStarter mActivityStarter = ((ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class));
    private ArrayList<ExpandInfoController.Callback> mCallbacks = new ArrayList<>();
    private ControlPanelContentView mContentView;
    private Context mContext;
    private DataBillInfo mDataBillInfo;
    private DataUsageInfo mDataUsageInfo;
    private HealthDataInfo mHealthDataInfo;
    private HashMap<Integer, ExpandInfoController.Info> mInfosMap;
    private HashMap<Integer, ExpandInfoController.Info> mInfosMapOld = new HashMap<>();
    private ScreenTimeInfo mScreenTimeInfo;
    private int mSelectedType;
    private SuperPowerInfo mSuperPowerInfo;
    private boolean mSuperPowerMode;
    private UserHandle mUserHandler = new UserHandle(KeyguardUpdateMonitor.getCurrentUser());

    public ExpandInfoControllerImpl(@Inject Context context) {
        this.mContext = context;
        HashMap<Integer, ExpandInfoController.Info> hashMap = new HashMap<>();
        this.mInfosMap = hashMap;
        if (!Constants.IS_INTERNATIONAL) {
            hashMap.put(0, new ExpandInfoController.Info());
            this.mInfosMap.put(1, new ExpandInfoController.Info());
            this.mInfosMap.put(2, new ExpandInfoController.Info());
            this.mInfosMap.put(3, new ExpandInfoController.Info());
            this.mDataUsageInfo = new DataUsageInfo(this.mContext, 0, this);
            this.mDataBillInfo = new DataBillInfo(this.mContext, 1, this);
            this.mHealthDataInfo = new HealthDataInfo(this.mContext, 2, this);
            this.mScreenTimeInfo = new ScreenTimeInfo(this.mContext, 3, this);
            this.mSuperPowerInfo = new SuperPowerInfo(this.mContext, 16, this);
            int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "control_center_expand_info_type", 0, KeyguardUpdateMonitor.getCurrentUser());
            this.mSelectedType = intForUser;
            setSelectedType(intForUser);
        }
    }

    public void onUserSwitched() {
        this.mUserHandler = new UserHandle(KeyguardUpdateMonitor.getCurrentUser());
        requestData();
        int intForUser = Settings.System.getIntForUser(this.mContext.getContentResolver(), "control_center_expand_info_type", 0, KeyguardUpdateMonitor.getCurrentUser());
        if (intForUser != this.mSelectedType) {
            this.mSelectedType = intForUser;
            setSelectedType(intForUser);
        }
    }

    public void setContentView(ControlPanelContentView controlPanelContentView) {
        this.mContentView = controlPanelContentView;
    }

    public ControlPanelContentView getContentView() {
        return this.mContentView;
    }

    public HashMap<Integer, ExpandInfoController.Info> getInfosMap() {
        return this.mInfosMap;
    }

    public void updateInfo(int i, ExpandInfoController.Info info) {
        if (this.mSuperPowerMode && i != 16) {
            return;
        }
        if (this.mSuperPowerMode || i != 16) {
            ExpandInfoController.Info info2 = this.mInfosMap.get(Integer.valueOf(i));
            if (info2 == null) {
                this.mInfosMap.put(Integer.valueOf(i), info);
            } else if (!info2.equal(info)) {
                info2.copy(info);
            } else {
                return;
            }
            Iterator<ExpandInfoController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().updateInfo(i, info);
            }
            if (i == this.mSelectedType && !info.available) {
                setSelectedType(0);
            }
        }
    }

    public void setSelectedType(int i) {
        this.mSelectedType = i;
        if (this.mSuperPowerMode) {
            i = 16;
        }
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "control_center_expand_info_type", i, KeyguardUpdateMonitor.getCurrentUser());
        Iterator<ExpandInfoController.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().updateSelectedType(i);
        }
    }

    public int getSelectedType() {
        if (this.mSuperPowerMode) {
            return 16;
        }
        return this.mSelectedType;
    }

    public ExpandInfoController.Info getSuperPowerInfo() {
        return this.mSuperPowerInfo.getInfo();
    }

    public void addCallback(ExpandInfoController.Callback callback) {
        if (!Constants.IS_INTERNATIONAL) {
            this.mCallbacks.add(callback);
            for (Map.Entry next : this.mInfosMap.entrySet()) {
                callback.updateInfo(((Integer) next.getKey()).intValue(), (ExpandInfoController.Info) next.getValue());
                callback.updateInfosMap();
                callback.updateSelectedType(this.mSelectedType);
            }
        }
    }

    public void requestData() {
        if (!Constants.IS_INTERNATIONAL) {
            if (this.mSuperPowerMode) {
                this.mSuperPowerInfo.requestData(this.mUserHandler);
                return;
            }
            this.mDataUsageInfo.requestData(this.mUserHandler);
            this.mDataBillInfo.requestData(this.mUserHandler);
            this.mHealthDataInfo.requestData(this.mUserHandler);
            this.mScreenTimeInfo.requestData(this.mUserHandler);
        }
    }

    public void setSuperPowerMode(boolean z) {
        this.mSuperPowerMode = z;
        if (z) {
            this.mInfosMapOld.clear();
            this.mInfosMapOld.putAll(this.mInfosMap);
        }
        this.mInfosMap.clear();
        if (!z) {
            this.mInfosMap.putAll(this.mInfosMapOld);
        }
    }

    public void startActivity(String str) {
        if (!TextUtils.isEmpty(str)) {
            Intent intent = new Intent();
            intent.setAction(str);
            intent.putExtra("misettings_from_page", "controller_center");
            intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
            this.mActivityStarter.postStartActivityDismissingKeyguard(intent);
        }
    }

    public void startActivityByUri(String str) {
        if (str != null) {
            try {
                Intent parseUri = Intent.parseUri(str, 0);
                parseUri.putExtra("misettings_from_page", "controller_center");
                parseUri.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                this.mActivityStarter.postStartActivityDismissingKeyguard(parseUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
