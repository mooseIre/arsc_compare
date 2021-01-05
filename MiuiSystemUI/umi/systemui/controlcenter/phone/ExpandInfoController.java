package com.android.systemui.controlcenter.phone;

import android.graphics.Bitmap;
import android.os.UserHandle;
import java.util.HashMap;

public interface ExpandInfoController {

    public interface Callback {
        void updateInfo(int i, Info info);

        void updateInfosMap() {
        }

        void updateSelectedType(int i) {
        }
    }

    void addCallback(Callback callback);

    ControlPanelContentView getContentView();

    HashMap<Integer, Info> getInfosMap();

    int getSelectedType();

    Info getSuperPowerInfo();

    UserHandle getUserHandle();

    void register();

    void removeCallback(Callback callback);

    void requestData();

    void setContentView(ControlPanelContentView controlPanelContentView);

    void setSelectedType(int i);

    void setSuperPowerMode(boolean z);

    void startActivity(String str);

    void startActivityByUri(String str);

    void unregister();

    void updateInfo(int i, Info info) {
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

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
            r0 = r2.title;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
            r0 = r2.status;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0042, code lost:
            r0 = r2.unit;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0056, code lost:
            r0 = r2.action;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x006a, code lost:
            r2 = r2.uri;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equal(com.android.systemui.controlcenter.phone.ExpandInfoController.Info r3) {
            /*
                r2 = this;
                boolean r0 = r2.available
                boolean r1 = r3.available
                if (r0 != r1) goto L_0x0078
                boolean r0 = r2.initialized
                boolean r1 = r3.initialized
                if (r0 != r1) goto L_0x0078
                android.graphics.Bitmap r0 = r2.icon
                android.graphics.Bitmap r1 = r3.icon
                if (r0 != r1) goto L_0x0078
                java.lang.String r0 = r2.title
                if (r0 != 0) goto L_0x001a
                java.lang.String r0 = r3.title
                if (r0 == 0) goto L_0x0026
            L_0x001a:
                java.lang.String r0 = r2.title
                if (r0 == 0) goto L_0x0078
                java.lang.String r1 = r3.title
                boolean r0 = r0.equals(r1)
                if (r0 == 0) goto L_0x0078
            L_0x0026:
                java.lang.String r0 = r2.status
                if (r0 != 0) goto L_0x002e
                java.lang.String r0 = r3.status
                if (r0 == 0) goto L_0x003a
            L_0x002e:
                java.lang.String r0 = r2.status
                if (r0 == 0) goto L_0x0078
                java.lang.String r1 = r3.status
                boolean r0 = r0.equals(r1)
                if (r0 == 0) goto L_0x0078
            L_0x003a:
                java.lang.String r0 = r2.unit
                if (r0 != 0) goto L_0x0042
                java.lang.String r0 = r3.unit
                if (r0 == 0) goto L_0x004e
            L_0x0042:
                java.lang.String r0 = r2.unit
                if (r0 == 0) goto L_0x0078
                java.lang.String r1 = r3.unit
                boolean r0 = r0.equals(r1)
                if (r0 == 0) goto L_0x0078
            L_0x004e:
                java.lang.String r0 = r2.action
                if (r0 != 0) goto L_0x0056
                java.lang.String r0 = r3.action
                if (r0 == 0) goto L_0x0062
            L_0x0056:
                java.lang.String r0 = r2.action
                if (r0 == 0) goto L_0x0078
                java.lang.String r1 = r3.action
                boolean r0 = r0.equals(r1)
                if (r0 == 0) goto L_0x0078
            L_0x0062:
                java.lang.String r0 = r2.uri
                if (r0 != 0) goto L_0x006a
                java.lang.String r0 = r3.uri
                if (r0 == 0) goto L_0x0076
            L_0x006a:
                java.lang.String r2 = r2.uri
                if (r2 == 0) goto L_0x0078
                java.lang.String r3 = r3.uri
                boolean r2 = r2.equals(r3)
                if (r2 == 0) goto L_0x0078
            L_0x0076:
                r2 = 1
                goto L_0x0079
            L_0x0078:
                r2 = 0
            L_0x0079:
                return r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.ExpandInfoController.Info.equal(com.android.systemui.controlcenter.phone.ExpandInfoController$Info):boolean");
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
