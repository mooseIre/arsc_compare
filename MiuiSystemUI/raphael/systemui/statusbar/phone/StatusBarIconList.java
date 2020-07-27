package com.android.systemui.statusbar.phone;

import com.android.internal.statusbar.StatusBarIcon;
import java.io.PrintWriter;
import java.util.ArrayList;

public class StatusBarIconList {
    protected ArrayList<StatusBarIcon> mIcons = new ArrayList<>();
    protected ArrayList<String> mSlots = new ArrayList<>();

    public StatusBarIconList(String[] strArr) {
        for (String add : strArr) {
            this.mSlots.add(add);
            this.mIcons.add((Object) null);
        }
    }

    public int getSlotIndex(String str) {
        int size = this.mSlots.size();
        for (int i = 0; i < size; i++) {
            if (str.equals(this.mSlots.get(i))) {
                return i;
            }
        }
        this.mSlots.add(0, str);
        this.mIcons.add(0, (Object) null);
        return 0;
    }

    public void setIcon(int i, StatusBarIcon statusBarIcon) {
        this.mIcons.set(i, statusBarIcon);
    }

    public void removeIcon(int i) {
        this.mIcons.set(i, (Object) null);
    }

    public String getSlot(int i) {
        return this.mSlots.get(i);
    }

    public StatusBarIcon getIcon(int i) {
        return this.mIcons.get(i);
    }

    public int getViewIndex(int i) {
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            if (this.mIcons.get(i3) != null) {
                i2++;
            }
        }
        return i2;
    }

    public void dump(PrintWriter printWriter) {
        int size = this.mSlots.size();
        printWriter.println("  icon slots: " + size);
        for (int i = 0; i < size; i++) {
            printWriter.printf("    %2d: (%s) %s\n", new Object[]{Integer.valueOf(i), this.mSlots.get(i), this.mIcons.get(i)});
        }
    }
}
