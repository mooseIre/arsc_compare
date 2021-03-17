package com.android.systemui.controlcenter.phone;

import android.graphics.Bitmap;
import android.os.UserHandle;
import java.util.HashMap;

public interface ExpandInfoController {

    public interface Callback {
        void updateInfo(int i, Info info);

        default void updateInfosMap() {
        }

        default void updateSelectedType(int i) {
        }
    }

    void addCallback(Callback callback);

    ControlPanelContentView getContentView();

    HashMap<Integer, Info> getInfosMap();

    int getSelectedType();

    Info getSuperPowerInfo();

    UserHandle getUserHandle();

    void onUserSwitched();

    void register();

    void removeCallback(Callback callback);

    void requestData();

    void setContentView(ControlPanelContentView controlPanelContentView);

    void setSelectedType(int i);

    void setSuperPowerMode(boolean z);

    void startActivity(String str);

    void startActivityByUri(String str);

    void unregister();

    default void updateInfo(int i, Info info) {
    }

    public static class Info {
        public String action = "";
        public boolean available;
        public Bitmap icon = null;
        public boolean initialized;
        public String status = "";
        public String title = "";
        public String unit = "";
        public String uri = "";

        public boolean equal(Info info) {
            String str;
            String str2;
            String str3;
            String str4;
            String str5;
            return this.available == info.available && this.initialized == info.initialized && this.icon == info.icon && ((this.title == null && info.title == null) || ((str5 = this.title) != null && str5.equals(info.title))) && (((this.status == null && info.status == null) || ((str4 = this.status) != null && str4.equals(info.status))) && (((this.unit == null && info.unit == null) || ((str3 = this.unit) != null && str3.equals(info.unit))) && (((this.action == null && info.action == null) || ((str2 = this.action) != null && str2.equals(info.action))) && ((this.uri == null && info.uri == null) || ((str = this.uri) != null && str.equals(info.uri))))));
        }

        public void copy(Info info) {
            this.available = info.available;
            this.initialized = info.initialized;
            this.icon = info.icon;
            this.title = info.title;
            this.status = info.status;
            this.unit = info.unit;
            this.action = info.action;
            this.uri = info.uri;
        }

        public String toString() {
            return "available:" + this.available + " initialized:" + this.initialized + " title:" + this.title + " status:" + this.status + " unit:" + this.unit + " icon:" + this.icon + " action:" + this.action + " uri:" + this.uri;
        }
    }
}
