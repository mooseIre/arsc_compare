package com.android.systemui.statusbar.phone;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandleCompat;
import android.util.ArraySet;
import android.view.ViewGroup;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.tuner.TunerService;
import com.miui.systemui.annotation.Inject;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class StatusBarIconControllerImpl extends StatusBarIconList implements TunerService.Tunable, ConfigurationController.ConfigurationListener, Dumpable, CommandQueue.Callbacks, StatusBarIconController {
    private Context mContext;
    private final DarkIconDispatcher mDarkIconDispatcher;
    private boolean mDemoMode;
    private final ArraySet<String> mIconBlacklist = new ArraySet<>();
    private final ArrayList<StatusBarIconController.IconManager> mIconGroups = new ArrayList<>();

    private void loadDimens() {
    }

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onConfigChanged(Configuration configuration) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void preloadRecentApps() {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    public StatusBarIconControllerImpl(@Inject Context context) {
        super(context.getResources().getStringArray(17236078));
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        this.mContext = context;
        loadDimens();
        ((CommandQueue) SystemUI.getComponent(context, CommandQueue.class)).addCallbacks(this);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
    }

    public void addIconGroup(StatusBarIconController.IconManager iconManager) {
        this.mIconGroups.add(iconManager);
        for (int i = 0; i < this.mIcons.size(); i++) {
            StatusBarIcon statusBarIcon = this.mIcons.get(i);
            if (statusBarIcon != null) {
                String str = this.mSlots.get(i);
                iconManager.onIconAdded(getViewIndex(getSlotIndex(str)), str, this.mIconBlacklist.contains(str), statusBarIcon);
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
            this.mIconBlacklist.addAll(StatusBarIconControllerHelper.getIconBlacklist(str2));
            ArrayList arrayList = new ArrayList(this.mIcons);
            ArrayList arrayList2 = new ArrayList(this.mSlots);
            for (int size = arrayList.size() - 1; size >= 0; size--) {
                removeIcon((String) arrayList2.get(size));
            }
            for (int i = 0; i < arrayList.size(); i++) {
                setIcon((String) arrayList2.get(i), (StatusBarIcon) arrayList.get(i));
            }
        }
    }

    private void addSystemIcon(int i, StatusBarIcon statusBarIcon) {
        String slot = getSlot(i);
        int viewIndex = getViewIndex(i);
        boolean contains = this.mIconBlacklist.contains(slot);
        Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.iterator();
        while (it.hasNext()) {
            it.next().onIconAdded(viewIndex, slot, contains, statusBarIcon);
        }
    }

    public void setIcon(String str, int i, CharSequence charSequence) {
        if (this.mSlots.contains(str)) {
            int slotIndex = getSlotIndex(str);
            StatusBarIcon icon = getIcon(slotIndex);
            if (icon == null) {
                setIcon(str, new StatusBarIcon(UserHandleCompat.SYSTEM, this.mContext.getPackageName(), Icon.createWithResource(this.mContext, i), 0, 0, charSequence));
                return;
            }
            icon.icon = Icon.createWithResource(this.mContext, i);
            icon.contentDescription = charSequence;
            handleSet(slotIndex, icon);
        }
    }

    public void setExternalIcon(String str) {
        int viewIndex = getViewIndex(getSlotIndex(str));
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_drawing_size);
        Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.iterator();
        while (it.hasNext()) {
            it.next().onIconExternal(viewIndex, dimensionPixelSize);
        }
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        if (this.mSlots.contains(str)) {
            setIcon(getSlotIndex(str), statusBarIcon);
        }
    }

    public void removeIcon(String str) {
        if (this.mSlots.contains(str)) {
            removeIcon(getSlotIndex(str));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r3 = getSlotIndex(r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIconVisibility(java.lang.String r3, boolean r4) {
        /*
            r2 = this;
            java.util.ArrayList<java.lang.String> r0 = r2.mSlots
            boolean r0 = r0.contains(r3)
            if (r0 == 0) goto L_0x001d
            int r3 = r2.getSlotIndex(r3)
            com.android.internal.statusbar.StatusBarIcon r0 = r2.getIcon(r3)
            if (r0 == 0) goto L_0x001d
            boolean r1 = r0.visible
            if (r1 != r4) goto L_0x0017
            goto L_0x001d
        L_0x0017:
            r0.visible = r4
            r2.handleSet(r3, r0)
        L_0x001d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBarIconControllerImpl.setIconVisibility(java.lang.String, boolean):void");
    }

    public void removeIcon(int i) {
        if (getIcon(i) != null) {
            super.removeIcon(i);
            int viewIndex = getViewIndex(i);
            Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.iterator();
            while (it.hasNext()) {
                it.next().onRemoveIcon(viewIndex, getSlot(i));
            }
        }
    }

    public void setIcon(int i, StatusBarIcon statusBarIcon) {
        if (statusBarIcon == null) {
            removeIcon(i);
            return;
        }
        boolean z = getIcon(i) == null;
        super.setIcon(i, statusBarIcon);
        if (z) {
            addSystemIcon(i, statusBarIcon);
        } else {
            handleSet(i, statusBarIcon);
        }
    }

    private void handleSet(int i, StatusBarIcon statusBarIcon) {
        int viewIndex = getViewIndex(i);
        Iterator<StatusBarIconController.IconManager> it = this.mIconGroups.iterator();
        while (it.hasNext()) {
            it.next().onSetIcon(viewIndex, getSlot(i), statusBarIcon);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (this.mIconGroups.size() != 0) {
            ViewGroup viewGroup = this.mIconGroups.get(0).mGroup;
            int childCount = viewGroup.getChildCount();
            printWriter.println("  icon views: " + childCount);
            for (int i = 0; i < childCount; i++) {
                printWriter.println("    [" + i + "] icon=" + ((StatusBarIconView) viewGroup.getChildAt(i)));
            }
            super.dump(printWriter);
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            for (int i = 0; i < this.mIconGroups.size(); i++) {
                this.mIconGroups.get(i).mGroup.setVisibility(8);
            }
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            for (int i2 = 0; i2 < this.mIconGroups.size(); i2++) {
                this.mIconGroups.get(i2).mGroup.setVisibility(0);
            }
        }
    }

    public void onDensityOrFontScaleChanged() {
        loadDimens();
    }
}
