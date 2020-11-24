package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.ViewGroup;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.C0005R$array;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarIconList;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public class StatusBarIconControllerImpl extends StatusBarIconList implements TunerService.Tunable, ConfigurationController.ConfigurationListener, Dumpable, CommandQueue.Callbacks, StatusBarIconController {
    private Context mContext;
    private final ArraySet<String> mIconBlacklist = new ArraySet<>();
    private final HashMap<StatusBarIconController.IconManager, List<String>> mIconGroups = new HashMap<>();

    private void loadDimens() {
    }

    /* access modifiers changed from: protected */
    public void dumpForOrderedIconGroup(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public StatusBarIconControllerImpl(Context context, CommandQueue commandQueue) {
        super(context.getResources().getStringArray(C0005R$array.config_statusBarIcons));
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mContext = context;
        loadDimens();
        commandQueue.addCallback((CommandQueue.Callbacks) this);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
    }

    public void addIconGroup(StatusBarIconController.IconManager iconManager) {
        addIconGroup(iconManager, new ArrayList());
    }

    public void addIconGroup(StatusBarIconController.IconManager iconManager, List<String> list) {
        this.mIconGroups.put(iconManager, list);
        ArrayList<StatusBarIconList.Slot> slots = getSlots();
        for (int i = 0; i < slots.size(); i++) {
            StatusBarIconList.Slot slot = slots.get(i);
            List<StatusBarIconHolder> holderListInViewOrder = slot.getHolderListInViewOrder();
            boolean z = this.mIconBlacklist.contains(slot.getName()) || list.contains(slot.getName());
            for (StatusBarIconHolder next : holderListInViewOrder) {
                next.getTag();
                iconManager.onIconAdded(getViewIndex(getSlotIndex(slot.getName()), next.getTag()), slot.getName(), z, next);
            }
        }
    }

    public void removeIconGroup(StatusBarIconController.IconManager iconManager) {
        iconManager.destroy();
        this.mIconGroups.remove(iconManager);
    }

    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            this.mIconBlacklist.clear();
            this.mIconBlacklist.addAll(StatusBarIconController.getIconBlacklist(this.mContext, str2));
            ArrayList<StatusBarIconList.Slot> slots = getSlots();
            ArrayMap arrayMap = new ArrayMap();
            for (int size = slots.size() - 1; size >= 0; size--) {
                StatusBarIconList.Slot slot = slots.get(size);
                arrayMap.put(slot, slot.getHolderList());
                removeAllIconsForSlot(slot.getName());
            }
            for (int i = 0; i < slots.size(); i++) {
                StatusBarIconList.Slot slot2 = slots.get(i);
                List<StatusBarIconHolder> list = (List) arrayMap.get(slot2);
                if (list != null) {
                    for (StatusBarIconHolder icon : list) {
                        setIcon(getSlotIndex(slot2.getName()), icon);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addSystemIcon(int i, StatusBarIconHolder statusBarIconHolder) {
        String slotName = getSlotName(i);
        this.mIconGroups.forEach(new BiConsumer(getViewIndex(i, statusBarIconHolder.getTag()), slotName, this.mIconBlacklist.contains(slotName), statusBarIconHolder) {
            public final /* synthetic */ int f$0;
            public final /* synthetic */ String f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ StatusBarIconHolder f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void accept(Object obj, Object obj2) {
                StatusBarIconControllerImpl.lambda$addSystemIcon$0(this.f$0, this.f$1, this.f$2, this.f$3, (StatusBarIconController.IconManager) obj, (List) obj2);
            }
        });
    }

    static /* synthetic */ void lambda$addSystemIcon$0(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder, StatusBarIconController.IconManager iconManager, List list) {
        iconManager.onIconAdded(i, str, z || list.contains(str), statusBarIconHolder);
    }

    public void setIcon(String str, int i, CharSequence charSequence) {
        int slotIndex = getSlotIndex(str);
        StatusBarIconHolder icon = getIcon(slotIndex, 0);
        if (icon == null) {
            setIcon(slotIndex, StatusBarIconHolder.fromIcon(new StatusBarIcon(UserHandle.SYSTEM, this.mContext.getPackageName(), Icon.createWithResource(this.mContext, i), 0, 0, charSequence)));
            return;
        }
        icon.getIcon().icon = Icon.createWithResource(this.mContext, i);
        icon.getIcon().contentDescription = charSequence;
        handleSet(slotIndex, icon);
    }

    public void setSignalIcon(String str, StatusBarSignalPolicy.WifiIconState wifiIconState) {
        int slotIndex = getSlotIndex(str);
        if (wifiIconState == null) {
            removeIcon(slotIndex, 0);
            return;
        }
        StatusBarIconHolder icon = getIcon(slotIndex, 0);
        if (icon == null) {
            setIcon(slotIndex, StatusBarIconHolder.fromWifiIconState(wifiIconState));
            return;
        }
        icon.setWifiState(wifiIconState);
        handleSet(slotIndex, icon);
    }

    public void setMobileIcons(String str, List<StatusBarSignalPolicy.MobileIconState> list) {
        StatusBarIconList.Slot slot = getSlot(str);
        int slotIndex = getSlotIndex(str);
        Collections.reverse(list);
        for (StatusBarSignalPolicy.MobileIconState next : list) {
            StatusBarIconHolder holderForTag = slot.getHolderForTag(next.subId);
            if (holderForTag == null) {
                setIcon(slotIndex, StatusBarIconHolder.fromMobileIconState(next));
            } else {
                holderForTag.setMobileState(next);
                handleSet(slotIndex, holderForTag);
            }
        }
    }

    public void setExternalIcon(String str) {
        this.mIconGroups.forEach(new BiConsumer(getViewIndex(getSlotIndex(str), 0), this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.status_bar_icon_drawing_size)) {
            public final /* synthetic */ int f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj, Object obj2) {
                ((StatusBarIconController.IconManager) obj).onIconExternal(this.f$0, this.f$1);
            }
        });
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        setIcon(getSlotIndex(str), statusBarIcon);
    }

    private void setIcon(int i, StatusBarIcon statusBarIcon) {
        if (statusBarIcon == null) {
            removeAllIconsForSlot(getSlotName(i));
        } else {
            setIcon(i, StatusBarIconHolder.fromIcon(statusBarIcon));
        }
    }

    public void setIcon(int i, StatusBarIconHolder statusBarIconHolder) {
        boolean z = getIcon(i, statusBarIconHolder.getTag()) == null;
        super.setIcon(i, statusBarIconHolder);
        if (z) {
            addSystemIcon(i, statusBarIconHolder);
        } else {
            handleSet(i, statusBarIconHolder);
        }
    }

    public void setIconVisibility(String str, boolean z) {
        int slotIndex = getSlotIndex(str);
        StatusBarIconHolder icon = getIcon(slotIndex, 0);
        if (icon != null && icon.isVisible() != z) {
            icon.setVisible(z);
            handleSet(slotIndex, icon);
        }
    }

    public void setIconAccessibilityLiveRegion(String str, int i) {
        StatusBarIconList.Slot slot = getSlot(str);
        if (slot.hasIconsInSlot()) {
            int slotIndex = getSlotIndex(str);
            for (StatusBarIconHolder tag : slot.getHolderListInViewOrder()) {
                this.mIconGroups.forEach(new BiConsumer(getViewIndex(slotIndex, tag.getTag()), i) {
                    public final /* synthetic */ int f$0;
                    public final /* synthetic */ int f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj, Object obj2) {
                        ((StatusBarIconController.IconManager) obj).mGroup.getChildAt(this.f$0).setAccessibilityLiveRegion(this.f$1);
                    }
                });
            }
        }
    }

    public void removeIcon(String str) {
        removeAllIconsForSlot(str);
    }

    public void removeAllIconsForSlot(String str) {
        StatusBarIconList.Slot slot = getSlot(str);
        if (slot.hasIconsInSlot()) {
            int slotIndex = getSlotIndex(str);
            for (StatusBarIconHolder next : slot.getHolderListInViewOrder()) {
                int viewIndex = getViewIndex(slotIndex, next.getTag());
                slot.removeForTag(next.getTag());
                this.mIconGroups.forEach(new BiConsumer(viewIndex) {
                    public final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void accept(Object obj, Object obj2) {
                        ((StatusBarIconController.IconManager) obj).onRemoveIcon(this.f$0);
                    }
                });
            }
        }
    }

    public void removeIcon(int i, int i2) {
        if (getIcon(i, i2) != null) {
            super.removeIcon(i, i2);
            this.mIconGroups.forEach(new BiConsumer(getViewIndex(i, 0)) {
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj, Object obj2) {
                    ((StatusBarIconController.IconManager) obj).onRemoveIcon(this.f$0);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void handleSet(int i, StatusBarIconHolder statusBarIconHolder) {
        this.mIconGroups.forEach(new BiConsumer(getViewIndex(i, statusBarIconHolder.getTag()), statusBarIconHolder) {
            public final /* synthetic */ int f$0;
            public final /* synthetic */ StatusBarIconHolder f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj, Object obj2) {
                ((StatusBarIconController.IconManager) obj).onSetIconHolder(this.f$0, this.f$1);
            }
        });
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        int i;
        printWriter.println("StatusBarIconController state:");
        Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.keySet().iterator();
        while (true) {
            i = 0;
            if (!it.hasNext()) {
                break;
            }
            StatusBarIconController.IconManager next = it.next();
            if (next.shouldLog()) {
                ViewGroup viewGroup = next.mGroup;
                int childCount = viewGroup.getChildCount();
                printWriter.println("  icon views: " + childCount);
                while (i < childCount) {
                    printWriter.println("    [" + i + "] icon=" + ((StatusIconDisplayable) viewGroup.getChildAt(i)));
                    i++;
                }
            }
        }
        dumpForOrderedIconGroup(fileDescriptor, printWriter, strArr);
        int size = this.mIconBlacklist.size();
        printWriter.println("  icon black list: " + size);
        while (i < size) {
            printWriter.println("    " + i + ":" + this.mIconBlacklist.valueAt(i));
            i++;
        }
        super.dump(printWriter);
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        for (StatusBarIconController.IconManager next : this.mIconGroups.keySet()) {
            if (next.isDemoable()) {
                next.dispatchDemoCommand(str, bundle);
            }
        }
    }

    public void onDensityOrFontScaleChanged() {
        loadDimens();
    }
}
