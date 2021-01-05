package com.android.systemui.qs.tiles;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRouter;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.lifecycle.LifecycleOwner;
import com.android.internal.app.MediaRouteDialogPresenter;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class CastTile extends QSTileImpl<QSTile.BooleanState> {
    /* access modifiers changed from: private */
    public static final Intent CAST_SETTINGS = new Intent("android.settings.CAST_SETTINGS");
    private final ActivityStarter mActivityStarter;
    private final Callback mCallback = new Callback();
    /* access modifiers changed from: private */
    public final CastController mController;
    private final CastDetailAdapter mDetailAdapter;
    private Dialog mDialog;
    private final KeyguardStateController mKeyguard;
    private final NetworkController mNetworkController;
    private final NetworkController.SignalCallback mSignalCallback = new NetworkController.SignalCallback() {
        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
            boolean z5 = false;
            if (!SystemProperties.getBoolean("persist.debug.wfd.enable", false)) {
                if (z && iconState2.visible) {
                    z5 = true;
                }
                if (z5 != CastTile.this.mWifiConnected) {
                    boolean unused = CastTile.this.mWifiConnected = z5;
                    CastTile.this.refreshState();
                }
            } else if (z != CastTile.this.mWifiConnected) {
                boolean unused2 = CastTile.this.mWifiConnected = z;
                CastTile.this.refreshState();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mWifiConnected;

    public int getMetricsCategory() {
        return 114;
    }

    public boolean isAvailable() {
        return false;
    }

    public CastTile(QSHost qSHost, CastController castController, KeyguardStateController keyguardStateController, NetworkController networkController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = castController;
        this.mDetailAdapter = new CastDetailAdapter();
        this.mKeyguard = keyguardStateController;
        this.mNetworkController = networkController;
        this.mActivityStarter = activityStarter;
        this.mController.observe((LifecycleOwner) this, this.mCallback);
        this.mKeyguard.observe((LifecycleOwner) this, this.mCallback);
        this.mNetworkController.observe((LifecycleOwner) this, this.mSignalCallback);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (QSTileImpl.DEBUG) {
            String str = this.TAG;
            Log.d(str, "handleSetListening " + z);
        }
        if (!z) {
            this.mController.setDiscovering(false);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
        super.handleUserSwitch(i);
        this.mController.setCurrentUserId(i);
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.CAST_SETTINGS");
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        handleClick();
    }

    /* access modifiers changed from: protected */
    public void handleLongClick() {
        handleClick();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (((QSTile.BooleanState) getState()).state != 0) {
            List<CastController.CastDevice> activeDevices = getActiveDevices();
            if (activeDevices.isEmpty() || (activeDevices.get(0).tag instanceof MediaRouter.RouteInfo)) {
                this.mActivityStarter.postQSRunnableDismissingKeyguard(new Runnable() {
                    public final void run() {
                        CastTile.this.lambda$handleClick$0$CastTile();
                    }
                });
            } else {
                this.mController.stopCasting(activeDevices.get(0));
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$CastTile() {
        showDetail(true);
    }

    private List<CastController.CastDevice> getActiveDevices() {
        ArrayList arrayList = new ArrayList();
        for (CastController.CastDevice next : this.mController.getCastDevices()) {
            int i = next.state;
            if (i == 2 || i == 1) {
                arrayList.add(next);
            }
        }
        return arrayList;
    }

    public void showDetail(boolean z) {
        this.mUiHandler.post(new Runnable() {
            public final void run() {
                CastTile.this.lambda$showDetail$3$CastTile();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showDetail$3 */
    public /* synthetic */ void lambda$showDetail$3$CastTile() {
        Dialog createDialog = MediaRouteDialogPresenter.createDialog(this.mContext, 4, new View.OnClickListener() {
            public final void onClick(View view) {
                CastTile.this.lambda$showDetail$1$CastTile(view);
            }
        });
        this.mDialog = createDialog;
        createDialog.getWindow().setType(2009);
        SystemUIDialog.setShowForAllUsers(this.mDialog, true);
        SystemUIDialog.registerDismissListener(this.mDialog);
        SystemUIDialog.setWindowOnTop(this.mDialog);
        this.mUiHandler.post(new Runnable() {
            public final void run() {
                CastTile.this.lambda$showDetail$2$CastTile();
            }
        });
        this.mHost.collapsePanels();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showDetail$1 */
    public /* synthetic */ void lambda$showDetail$1$CastTile(View view) {
        this.mDialog.dismiss();
        this.mActivityStarter.postStartActivityDismissingKeyguard(getLongClickIntent(), 0);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showDetail$2 */
    public /* synthetic */ void lambda$showDetail$2$CastTile() {
        this.mDialog.show();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_cast_title);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        int i2;
        String string = this.mContext.getString(C0021R$string.quick_settings_cast_title);
        booleanState.label = string;
        booleanState.contentDescription = string;
        booleanState.stateDescription = "";
        booleanState.value = false;
        List<CastController.CastDevice> castDevices = this.mController.getCastDevices();
        Iterator<CastController.CastDevice> it = castDevices.iterator();
        boolean z = false;
        while (true) {
            i = 2;
            if (!it.hasNext()) {
                break;
            }
            CastController.CastDevice next = it.next();
            int i3 = next.state;
            if (i3 == 2) {
                booleanState.value = true;
                booleanState.secondaryLabel = getDeviceName(next);
                booleanState.stateDescription += "," + this.mContext.getString(C0021R$string.accessibility_cast_name, new Object[]{booleanState.label});
                z = false;
                break;
            } else if (i3 == 1) {
                z = true;
            }
        }
        if (z && !booleanState.value) {
            booleanState.secondaryLabel = this.mContext.getString(C0021R$string.quick_settings_connecting);
        }
        if (booleanState.value) {
            i2 = C0013R$drawable.ic_cast_connected;
        } else {
            i2 = C0013R$drawable.ic_cast;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i2);
        if (this.mWifiConnected || booleanState.value) {
            if (!booleanState.value) {
                i = 1;
            }
            booleanState.state = i;
            if (!booleanState.value) {
                booleanState.secondaryLabel = "";
            }
            booleanState.contentDescription += "," + this.mContext.getString(C0021R$string.accessibility_quick_settings_open_details);
            booleanState.expandedAccessibilityClassName = Button.class.getName();
        } else {
            booleanState.state = 0;
            booleanState.secondaryLabel = this.mContext.getString(C0021R$string.quick_settings_cast_no_wifi);
        }
        booleanState.stateDescription += ", " + booleanState.secondaryLabel;
        this.mDetailAdapter.updateItems(castDevices);
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (!((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_casting_turned_off);
        }
        return null;
    }

    /* access modifiers changed from: private */
    public String getDeviceName(CastController.CastDevice castDevice) {
        String str = castDevice.name;
        return str != null ? str : this.mContext.getString(C0021R$string.quick_settings_cast_device_default_name);
    }

    private final class Callback implements CastController.Callback, KeyguardStateController.Callback {
        private Callback() {
        }

        public void onCastDevicesChanged() {
            CastTile.this.refreshState();
        }

        public void onKeyguardShowingChanged() {
            CastTile.this.refreshState();
        }
    }

    private final class CastDetailAdapter implements DetailAdapter, MiuiQSDetailItems.Callback {
        private MiuiQSDetailItems mItems;
        /* access modifiers changed from: private */
        public final LinkedHashMap<String, CastController.CastDevice> mVisibleOrder;

        public int getMetricsCategory() {
            return 151;
        }

        public Boolean getToggleState() {
            return null;
        }

        public void setToggleState(boolean z) {
        }

        private CastDetailAdapter() {
            this.mVisibleOrder = new LinkedHashMap<>();
        }

        public CharSequence getTitle() {
            return CastTile.this.mContext.getString(C0021R$string.quick_settings_cast_title);
        }

        public Intent getSettingsIntent() {
            return CastTile.CAST_SETTINGS;
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            MiuiQSDetailItems convertOrInflate = MiuiQSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Cast");
            if (view == null) {
                if (QSTileImpl.DEBUG) {
                    Log.d(CastTile.this.TAG, "addOnAttachStateChangeListener");
                }
                this.mItems.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                    public void onViewAttachedToWindow(View view) {
                        if (QSTileImpl.DEBUG) {
                            Log.d(CastTile.this.TAG, "onViewAttachedToWindow");
                        }
                    }

                    public void onViewDetachedFromWindow(View view) {
                        if (QSTileImpl.DEBUG) {
                            Log.d(CastTile.this.TAG, "onViewDetachedFromWindow");
                        }
                        CastDetailAdapter.this.mVisibleOrder.clear();
                    }
                });
            }
            this.mItems.setEmptyState(C0013R$drawable.ic_qs_cast_detail_empty, C0021R$string.quick_settings_cast_detail_empty_text);
            this.mItems.setCallback(this);
            updateItems(CastTile.this.mController.getCastDevices());
            CastTile.this.mController.setDiscovering(true);
            return this.mItems;
        }

        /* access modifiers changed from: private */
        public void updateItems(List<CastController.CastDevice> list) {
            int i;
            if (this.mItems != null) {
                MiuiQSDetailItems.Item[] itemArr = null;
                if (list != null && !list.isEmpty()) {
                    Iterator<CastController.CastDevice> it = list.iterator();
                    while (true) {
                        i = 0;
                        if (!it.hasNext()) {
                            break;
                        }
                        CastController.CastDevice next = it.next();
                        if (next.state == 2) {
                            MiuiQSDetailItems.Item item = new MiuiQSDetailItems.Item();
                            item.icon = C0013R$drawable.ic_cast_connected;
                            item.line1 = CastTile.this.getDeviceName(next);
                            item.line2 = CastTile.this.mContext.getString(C0021R$string.quick_settings_connected);
                            item.tag = next;
                            item.canDisconnect = true;
                            itemArr = new MiuiQSDetailItems.Item[]{item};
                            break;
                        }
                    }
                    if (itemArr == null) {
                        for (CastController.CastDevice next2 : list) {
                            this.mVisibleOrder.put(next2.id, next2);
                        }
                        itemArr = new MiuiQSDetailItems.Item[list.size()];
                        for (String str : this.mVisibleOrder.keySet()) {
                            CastController.CastDevice castDevice = this.mVisibleOrder.get(str);
                            if (list.contains(castDevice)) {
                                MiuiQSDetailItems.Item item2 = new MiuiQSDetailItems.Item();
                                item2.icon = C0013R$drawable.ic_cast;
                                item2.line1 = CastTile.this.getDeviceName(castDevice);
                                if (castDevice.state == 1) {
                                    item2.line2 = CastTile.this.mContext.getString(C0021R$string.quick_settings_connecting);
                                }
                                item2.tag = castDevice;
                                itemArr[i] = item2;
                                i++;
                            }
                        }
                    }
                }
                this.mItems.setItems(itemArr);
            }
        }

        public void onDetailItemClick(MiuiQSDetailItems.Item item) {
            if (item != null && item.tag != null) {
                MetricsLogger.action(CastTile.this.mContext, 157);
                CastTile.this.mController.startCasting((CastController.CastDevice) item.tag);
            }
        }

        public void onDetailItemDisconnect(MiuiQSDetailItems.Item item) {
            if (item != null && item.tag != null) {
                MetricsLogger.action(CastTile.this.mContext, 158);
                CastTile.this.mController.stopCasting((CastController.CastDevice) item.tag);
            }
        }
    }
}