package com.android.systemui.qs.customize;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Button;
import com.android.systemui.C0021R$string;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class TileQueryHelper {
    private final Executor mBgExecutor;
    private final Context mContext;
    private MiuiQSCustomizer mCustomizer;
    private boolean mFinished;
    private TileStateListener mListener;
    private final HashMap<String, QSTile> mLiveTiles = new HashMap<>();
    private final Executor mMainExecutor;
    private final ArraySet<String> mSpecs = new ArraySet<>();
    private final ArrayList<TileInfo> mTiles = new ArrayList<>();
    protected String mTilesStock;

    public static class TileInfo {
        public boolean isSystem;
        public String spec;
        public QSTile.State state;
    }

    public void filterBigTile(ArrayList<String> arrayList) {
    }

    public TileQueryHelper(Context context, Executor executor, Executor executor2) {
        this.mContext = context;
        this.mMainExecutor = executor;
        this.mBgExecutor = executor2;
        this.mTilesStock = context.getString(C0021R$string.miui_quick_settings_tiles_stock);
    }

    public void setListener(TileStateListener tileStateListener) {
        this.mListener = tileStateListener;
    }

    public void queryTiles(final QSTileHost qSTileHost) {
        this.mTiles.clear();
        this.mSpecs.clear();
        this.mFinished = false;
        this.mBgExecutor.execute(new Runnable() {
            /* class com.android.systemui.qs.customize.TileQueryHelper.AnonymousClass1 */

            public void run() {
                TileQueryHelper.this.addCurrentAndStockTiles(qSTileHost);
                TileQueryHelper.this.addPackageTiles(qSTileHost);
            }
        });
    }

    public void releaseTiles() {
        for (QSTile qSTile : this.mLiveTiles.values()) {
            qSTile.removeCallbacksByType(2);
            qSTile.setListening(this, false);
        }
        this.mLiveTiles.clear();
    }

    public boolean isFinished() {
        return this.mFinished;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addCurrentAndStockTiles(QSTileHost qSTileHost) {
        QSTile createTile;
        String string = Settings.Secure.getString(this.mContext.getContentResolver(), "sysui_qs_tiles");
        this.mTilesStock = qSTileHost.getHostInjector().getQsStockTiles();
        ArrayList<String> arrayList = new ArrayList<>();
        if (string != null) {
            arrayList.addAll(Arrays.asList(string.split(",")));
        } else {
            string = "";
        }
        String[] split = this.mTilesStock.split(",");
        for (String str : split) {
            if (!string.contains(str)) {
                arrayList.add(str);
            }
        }
        if (Build.IS_DEBUGGABLE && !string.contains("dbg:mem")) {
            arrayList.add("dbg:mem");
        }
        filterBigTile(arrayList);
        ArrayList arrayList2 = new ArrayList();
        Iterator<String> it = arrayList.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (!next.startsWith("custom(") && (createTile = qSTileHost.createTile(next)) != null) {
                if (!createTile.isAvailable()) {
                    createTile.setTileSpec(next);
                    createTile.destroy();
                } else {
                    createTile.setListening(this, true);
                    createTile.addCallback(new TileCallback(createTile));
                    createTile.setTileSpec(next);
                    arrayList2.add(createTile);
                    this.mLiveTiles.put(next, createTile);
                }
            }
        }
        Iterator it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            QSTile qSTile = (QSTile) it2.next();
            QSTile.State copy = qSTile.getState().copy();
            copy.label = qSTile.getTileLabel();
            addTile(qSTile.getTileSpec(), null, copy, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addPackageTiles(QSTileHost qSTileHost) {
        Collection<QSTile> tiles = qSTileHost.getTiles();
        PackageManager packageManager = this.mContext.getPackageManager();
        for (ResolveInfo resolveInfo : packageManager.queryIntentServicesAsUser(new Intent("android.service.quicksettings.action.QS_TILE"), 0, ActivityManager.getCurrentUser())) {
            String str = resolveInfo.serviceInfo.packageName;
            ComponentName componentName = new ComponentName(str, resolveInfo.serviceInfo.name);
            if (!this.mTilesStock.contains(componentName.flattenToString())) {
                CharSequence loadLabel = resolveInfo.serviceInfo.applicationInfo.loadLabel(packageManager);
                String spec = CustomTile.toSpec(componentName);
                if (!ControlCenterUtils.filterNearby(spec)) {
                    QSTile.State state = getState(tiles, spec);
                    if (state != null) {
                        addTile(spec, loadLabel, state, false);
                    } else {
                        ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                        if (serviceInfo.icon != 0 || serviceInfo.applicationInfo.icon != 0) {
                            ServiceInfo serviceInfo2 = resolveInfo.serviceInfo;
                            int i = serviceInfo2.icon;
                            if (i == 0) {
                                i = serviceInfo2.applicationInfo.icon;
                            }
                            Icon createWithResource = Icon.createWithResource(str, i);
                            Drawable drawable = null;
                            if (createWithResource != null) {
                                try {
                                    drawable = createWithResource.loadDrawable(this.mContext);
                                } catch (Exception unused) {
                                    Log.w("TileQueryHelper", "Invalid icon");
                                }
                            }
                            if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(resolveInfo.serviceInfo.permission) && drawable != null) {
                                drawable.mutate();
                                drawable.setTint(this.mContext.getColor(17170443));
                                CharSequence loadLabel2 = resolveInfo.serviceInfo.loadLabel(packageManager);
                                createStateAndAddTile(spec, drawable, loadLabel2 != null ? loadLabel2.toString() : "null", loadLabel);
                            }
                        }
                    }
                }
            }
        }
        notifyTilesChanged(true);
    }

    private void notifyTilesChanged(boolean z) {
        this.mMainExecutor.execute(new Runnable(new ArrayList(this.mTiles), z) {
            /* class com.android.systemui.qs.customize.$$Lambda$TileQueryHelper$Le1UOp9cit0qDyKlhpo9THBQqcQ */
            public final /* synthetic */ ArrayList f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                TileQueryHelper.this.lambda$notifyTilesChanged$0$TileQueryHelper(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$notifyTilesChanged$0 */
    public /* synthetic */ void lambda$notifyTilesChanged$0$TileQueryHelper(ArrayList arrayList, boolean z) {
        TileStateListener tileStateListener = this.mListener;
        if (tileStateListener != null) {
            tileStateListener.onTilesChanged(arrayList, this.mLiveTiles);
        }
        this.mFinished = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateStateForCustomizer(QSTile.State state) {
        state.dualTarget = false;
        state.expandedAccessibilityClassName = Button.class.getName();
    }

    private QSTile.State getState(Collection<QSTile> collection, String str) {
        for (QSTile qSTile : collection) {
            if (str.equals(qSTile.getTileSpec())) {
                return qSTile.getState().copy();
            }
        }
        return null;
    }

    private void addTile(String str, CharSequence charSequence, QSTile.State state, boolean z) {
        if (!this.mSpecs.contains(str) && !"edit".equals(str)) {
            TileInfo tileInfo = new TileInfo();
            tileInfo.state = state;
            updateStateForCustomizer(state);
            tileInfo.spec = str;
            QSTile.State state2 = tileInfo.state;
            if (z || TextUtils.equals(state.label, charSequence)) {
                charSequence = null;
            }
            state2.secondaryLabel = charSequence;
            tileInfo.isSystem = z;
            this.mTiles.add(tileInfo);
            this.mSpecs.add(str);
        }
    }

    private void createStateAndAddTile(String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2) {
        QSTile.State state = new QSTile.State();
        state.state = 1;
        state.label = charSequence;
        state.contentDescription = charSequence;
        state.icon = new QSTileImpl.DrawableIcon(drawable);
        addTile(str, charSequence2, state, false);
    }

    /* access modifiers changed from: private */
    public class TileCallback implements QSTile.Callback {
        private QSTile mTile;

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public int getCallbackType() {
            return 2;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onAnnouncementRequested(CharSequence charSequence) {
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onScanStateChanged(boolean z) {
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onShowDetail(boolean z) {
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onToggleStateChanged(boolean z) {
        }

        TileCallback(QSTile qSTile) {
            this.mTile = qSTile;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onStateChanged(QSTile.State state) {
            QSTile.State copy = this.mTile.getState().copy();
            TileQueryHelper.this.updateStateForCustomizer(copy);
            copy.label = this.mTile.getTileLabel();
            Iterator it = TileQueryHelper.this.mTiles.iterator();
            while (it.hasNext()) {
                TileInfo tileInfo = (TileInfo) it.next();
                if (TextUtils.equals(this.mTile.getTileSpec(), tileInfo.spec)) {
                    tileInfo.state = copy;
                    TileQueryHelper.this.mMainExecutor.execute(new Runnable(tileInfo) {
                        /* class com.android.systemui.qs.customize.$$Lambda$TileQueryHelper$TileCallback$cigH6rvu6uVvWACtkz00IKk3erk */
                        public final /* synthetic */ TileQueryHelper.TileInfo f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            TileQueryHelper.TileCallback.this.lambda$onStateChanged$0$TileQueryHelper$TileCallback(this.f$1);
                        }
                    });
                    return;
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onStateChanged$0 */
        public /* synthetic */ void lambda$onStateChanged$0$TileQueryHelper$TileCallback(TileInfo tileInfo) {
            TileQueryHelper.this.mListener.onTileChanged(tileInfo);
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Callback
        public void onShowEdit(boolean z) {
            TileQueryHelper.this.mMainExecutor.execute(new Runnable() {
                /* class com.android.systemui.qs.customize.$$Lambda$TileQueryHelper$TileCallback$kw7sHT_TtVudGXAX_r1kNZ61twc */

                public final void run() {
                    TileQueryHelper.TileCallback.this.lambda$onShowEdit$1$TileQueryHelper$TileCallback();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onShowEdit$1 */
        public /* synthetic */ void lambda$onShowEdit$1$TileQueryHelper$TileCallback() {
            if (TileQueryHelper.this.mCustomizer != null) {
                TileQueryHelper.this.mCustomizer.hide();
            }
        }
    }

    public void setCustomizer(MiuiQSCustomizer miuiQSCustomizer) {
        this.mCustomizer = miuiQSCustomizer;
    }

    public interface TileStateListener {
        default void onTileChanged(TileInfo tileInfo) {
        }

        default void onTilesChanged(List<TileInfo> list) {
        }

        default void onTilesChanged(List<TileInfo> list, Map<String, QSTile> map) {
            onTilesChanged(list);
        }
    }
}
