package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.text.TextUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.notification.EnableZenModeDialog;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Prefs;
import com.android.systemui.SysUIToast;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.ZenModePanel;

public class DndTile extends QSTileImpl<QSTile.BooleanState> {
    /* access modifiers changed from: private */
    public static final Intent ZEN_PRIORITY_SETTINGS = new Intent("android.settings.ZEN_MODE_PRIORITY_SETTINGS");
    /* access modifiers changed from: private */
    public static final Intent ZEN_SETTINGS = new Intent("android.settings.ZEN_MODE_SETTINGS");
    /* access modifiers changed from: private */
    public final ActivityStarter mActivityStarter;
    private final BroadcastDispatcher mBroadcastDispatcher;
    /* access modifiers changed from: private */
    public final ZenModeController mController;
    /* access modifiers changed from: private */
    public final DndDetailAdapter mDetailAdapter;
    private boolean mListening;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            if ("DndTileCombinedIcon".equals(str) || "DndTileVisible".equals(str)) {
                DndTile.this.refreshState();
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            DndTile.setVisible(DndTile.this.mContext, intent.getBooleanExtra("visible", false));
            DndTile.this.refreshState();
        }
    };
    private boolean mReceiverRegistered;
    private final SharedPreferences mSharedPreferences;
    /* access modifiers changed from: private */
    public boolean mShowingDetail;
    private final ZenModeController.Callback mZenCallback = new ZenModeController.Callback() {
        public void onConfigChanged(ZenModeConfig zenModeConfig) {
            if (DndTile.this.isShowingDetail()) {
                DndTile.this.mDetailAdapter.updatePanel();
            }
        }
    };
    /* access modifiers changed from: private */
    public final ZenModePanel.Callback mZenModePanelCallback = new ZenModePanel.Callback() {
        public void onExpanded(boolean z) {
        }

        public void onInteraction() {
        }

        public void onPrioritySettings() {
            DndTile.this.mActivityStarter.postStartActivityDismissingKeyguard(DndTile.ZEN_PRIORITY_SETTINGS, 0);
        }
    };

    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowActionBarOverlay;
    }

    public DndTile(QSHost qSHost, ZenModeController zenModeController, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher, SharedPreferences sharedPreferences) {
        super(qSHost);
        this.mController = zenModeController;
        this.mActivityStarter = activityStarter;
        this.mSharedPreferences = sharedPreferences;
        this.mDetailAdapter = new DndDetailAdapter();
        this.mBroadcastDispatcher = broadcastDispatcher;
        broadcastDispatcher.registerReceiver(this.mReceiver, new IntentFilter("com.android.systemui.dndtile.SET_VISIBLE"));
        this.mReceiverRegistered = true;
        this.mController.observe(getLifecycle(), this.mZenCallback);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
        if (this.mReceiverRegistered) {
            this.mBroadcastDispatcher.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    public static void setVisible(Context context, boolean z) {
        Prefs.putBoolean(context, "DndTileVisible", z);
    }

    public static boolean isVisible(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("DndTileVisible", false);
    }

    public static void setCombinedIcon(Context context, boolean z) {
        Prefs.putBoolean(context, "DndTileCombinedIcon", z);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public Intent getLongClickIntent() {
        return ZEN_SETTINGS;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (((QSTile.BooleanState) this.mState).value) {
            this.mController.setZen(0, (Uri) null, this.TAG);
        } else {
            showDetail(true);
        }
    }

    public void showDetail(boolean z) {
        int i = Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_duration", 0);
        if ((Settings.Secure.getInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 0) == 0 || Settings.Secure.getInt(this.mContext.getContentResolver(), "zen_settings_updated", 0) == 1) ? false : true) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), "show_zen_upgrade_notification", 0);
            this.mController.setZen(1, (Uri) null, this.TAG);
            Intent intent = new Intent("android.settings.ZEN_MODE_ONBOARDING");
            intent.addFlags(268468224);
            this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        } else if (i == -1) {
            this.mUiHandler.post(new Runnable() {
                public final void run() {
                    DndTile.this.lambda$showDetail$1$DndTile();
                }
            });
        } else if (i != 0) {
            this.mController.setZen(1, ZenModeConfig.toTimeCondition(this.mContext, i, ActivityManager.getCurrentUser(), true).id, this.TAG);
        } else {
            this.mController.setZen(1, (Uri) null, this.TAG);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showDetail$1 */
    public /* synthetic */ void lambda$showDetail$1$DndTile() {
        Dialog createDialog = new EnableZenModeDialog(this.mContext).createDialog();
        createDialog.getWindow().setType(2009);
        SystemUIDialog.setShowForAllUsers(createDialog, true);
        SystemUIDialog.registerDismissListener(createDialog);
        SystemUIDialog.setWindowOnTop(createDialog);
        this.mUiHandler.post(new Runnable(createDialog) {
            public final /* synthetic */ Dialog f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                this.f$0.show();
            }
        });
        this.mHost.collapsePanels();
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        if (this.mController.isVolumeRestricted()) {
            this.mHost.collapsePanels();
            Context context = this.mContext;
            SysUIToast.makeText(context, (CharSequence) context.getString(17040143), 1).show();
        } else if (!((QSTile.BooleanState) this.mState).value) {
            this.mController.addCallback(new ZenModeController.Callback(this) {
            });
            this.mController.setZen(1, (Uri) null, this.TAG);
        } else {
            showDetail(true);
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_dnd_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        ZenModeController zenModeController = this.mController;
        if (zenModeController != null) {
            int intValue = obj instanceof Integer ? ((Integer) obj).intValue() : zenModeController.getZen();
            boolean z = intValue != 0;
            boolean z2 = booleanState.value != z;
            if (booleanState.slash == null) {
                booleanState.slash = new QSTile.SlashState();
            }
            booleanState.dualTarget = true;
            booleanState.value = z;
            booleanState.state = z ? 2 : 1;
            booleanState.slash.isSlashed = !booleanState.value;
            booleanState.label = getTileLabel();
            booleanState.secondaryLabel = TextUtils.emptyIfNull(ZenModeConfig.getDescription(this.mContext, intValue != 0, this.mController.getConfig(), false));
            booleanState.icon = QSTileImpl.ResourceIcon.get(17302822);
            checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_adjust_volume");
            if (intValue == 1) {
                booleanState.contentDescription = this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd) + ", " + booleanState.secondaryLabel;
            } else if (intValue == 2) {
                booleanState.contentDescription = this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd) + ", " + this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd_none_on) + ", " + booleanState.secondaryLabel;
            } else if (intValue != 3) {
                booleanState.contentDescription = this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd);
            } else {
                booleanState.contentDescription = this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd) + ", " + this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd_alarms_on) + ", " + booleanState.secondaryLabel;
            }
            if (z2) {
                fireToggleStateChanged(booleanState.value);
            }
            booleanState.dualLabelContentDescription = this.mContext.getResources().getString(C0021R$string.accessibility_quick_settings_open_settings, new Object[]{getTileLabel()});
            booleanState.expandedAccessibilityClassName = Switch.class.getName();
        }
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_dnd_changed_off);
    }

    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                Prefs.registerListener(this.mContext, this.mPrefListener);
            } else {
                Prefs.unregisterListener(this.mContext, this.mPrefListener);
            }
        }
    }

    public boolean isAvailable() {
        return isVisible(this.mSharedPreferences);
    }

    private final class DndDetailAdapter implements DetailAdapter, View.OnAttachStateChangeListener {
        private ZenModePanel mZenPanel;

        public int getMetricsCategory() {
            return 149;
        }

        private DndDetailAdapter() {
        }

        public CharSequence getTitle() {
            return DndTile.this.mContext.getString(C0021R$string.quick_settings_dnd_label);
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) DndTile.this.mState).value);
        }

        public Intent getSettingsIntent() {
            return DndTile.ZEN_SETTINGS;
        }

        public void setToggleState(boolean z) {
            MetricsLogger.action(DndTile.this.mContext, 166, z);
            if (!z) {
                DndTile.this.mController.setZen(0, (Uri) null, DndTile.this.TAG);
            } else {
                DndTile.this.mController.setZen(1, (Uri) null, DndTile.this.TAG);
            }
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            ZenModePanel zenModePanel;
            if (view != null) {
                zenModePanel = (ZenModePanel) view;
            } else {
                zenModePanel = (ZenModePanel) LayoutInflater.from(context).inflate(C0017R$layout.zen_mode_panel, viewGroup, false);
            }
            this.mZenPanel = zenModePanel;
            if (view == null) {
                zenModePanel.init(DndTile.this.mController);
                this.mZenPanel.addOnAttachStateChangeListener(this);
                this.mZenPanel.setCallback(DndTile.this.mZenModePanelCallback);
                this.mZenPanel.setEmptyState(C0013R$drawable.ic_qs_dnd_detail_empty, C0021R$string.dnd_is_off);
            }
            updatePanel();
            return this.mZenPanel;
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x0027, code lost:
            r1 = r1.enabler;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updatePanel() {
            /*
                r6 = this;
                com.android.systemui.volume.ZenModePanel r0 = r6.mZenPanel
                if (r0 != 0) goto L_0x0005
                return
            L_0x0005:
                com.android.systemui.qs.tiles.DndTile r0 = com.android.systemui.qs.tiles.DndTile.this
                com.android.systemui.statusbar.policy.ZenModeController r0 = r0.mController
                int r0 = r0.getZen()
                if (r0 != 0) goto L_0x0019
                com.android.systemui.volume.ZenModePanel r6 = r6.mZenPanel
                r0 = 2
                r6.setState(r0)
                goto L_0x008c
            L_0x0019:
                com.android.systemui.qs.tiles.DndTile r0 = com.android.systemui.qs.tiles.DndTile.this
                com.android.systemui.statusbar.policy.ZenModeController r0 = r0.mController
                android.service.notification.ZenModeConfig r0 = r0.getConfig()
                android.service.notification.ZenModeConfig$ZenRule r1 = r0.manualRule
                if (r1 == 0) goto L_0x0030
                java.lang.String r1 = r1.enabler
                if (r1 == 0) goto L_0x0030
                java.lang.String r1 = r6.getOwnerCaption(r1)
                goto L_0x0032
            L_0x0030:
                java.lang.String r1 = ""
            L_0x0032:
                android.util.ArrayMap r0 = r0.automaticRules
                java.util.Collection r0 = r0.values()
                java.util.Iterator r0 = r0.iterator()
            L_0x003c:
                boolean r2 = r0.hasNext()
                r3 = 0
                r4 = 1
                if (r2 == 0) goto L_0x0076
                java.lang.Object r2 = r0.next()
                android.service.notification.ZenModeConfig$ZenRule r2 = (android.service.notification.ZenModeConfig.ZenRule) r2
                boolean r5 = r2.isAutomaticActive()
                if (r5 == 0) goto L_0x003c
                boolean r1 = r1.isEmpty()
                if (r1 == 0) goto L_0x0069
                com.android.systemui.qs.tiles.DndTile r1 = com.android.systemui.qs.tiles.DndTile.this
                android.content.Context r1 = r1.mContext
                int r5 = com.android.systemui.C0021R$string.qs_dnd_prompt_auto_rule
                java.lang.Object[] r4 = new java.lang.Object[r4]
                java.lang.String r2 = r2.name
                r4[r3] = r2
                java.lang.String r1 = r1.getString(r5, r4)
                goto L_0x003c
            L_0x0069:
                com.android.systemui.qs.tiles.DndTile r1 = com.android.systemui.qs.tiles.DndTile.this
                android.content.Context r1 = r1.mContext
                int r2 = com.android.systemui.C0021R$string.qs_dnd_prompt_auto_rule_app
                java.lang.String r1 = r1.getString(r2)
                goto L_0x003c
            L_0x0076:
                boolean r0 = r1.isEmpty()
                if (r0 == 0) goto L_0x0082
                com.android.systemui.volume.ZenModePanel r6 = r6.mZenPanel
                r6.setState(r3)
                goto L_0x008c
            L_0x0082:
                com.android.systemui.volume.ZenModePanel r0 = r6.mZenPanel
                r0.setState(r4)
                com.android.systemui.volume.ZenModePanel r6 = r6.mZenPanel
                r6.setAutoText(r1)
            L_0x008c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.DndTile.DndDetailAdapter.updatePanel():void");
        }

        private String getOwnerCaption(String str) {
            CharSequence loadLabel;
            PackageManager packageManager = DndTile.this.mContext.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 0);
                if (applicationInfo == null || (loadLabel = applicationInfo.loadLabel(packageManager)) == null) {
                    return "";
                }
                String trim = loadLabel.toString().trim();
                return DndTile.this.mContext.getString(C0021R$string.qs_dnd_prompt_app, new Object[]{trim});
            } catch (Throwable th) {
                Slog.w(DndTile.this.TAG, "Error loading owner caption", th);
                return "";
            }
        }

        public void onViewAttachedToWindow(View view) {
            boolean unused = DndTile.this.mShowingDetail = true;
        }

        public void onViewDetachedFromWindow(View view) {
            boolean unused = DndTile.this.mShowingDetail = false;
            this.mZenPanel = null;
        }
    }
}
