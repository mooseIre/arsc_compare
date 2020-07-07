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
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Button;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TileQueryHelper {
    private final Handler mBgHandler;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mFinished;
    /* access modifiers changed from: private */
    public final TileStateListener mListener;
    /* access modifiers changed from: private */
    public final HashMap<String, QSTile> mLiveTiles = new HashMap<>();
    /* access modifiers changed from: private */
    public final Handler mMainHandler;
    private final ArraySet<String> mSpecs = new ArraySet<>();
    /* access modifiers changed from: private */
    public final ArrayList<TileInfo> mTiles = new ArrayList<>();
    protected String mTilesStock;

    public static class TileInfo {
        public boolean isSystem;
        public String spec;
        public QSTile.State state;
    }

    public interface TileStateListener {
        void onTileChanged(TileInfo tileInfo);

        void onTilesChanged(List<TileInfo> list, Map<String, QSTile> map);
    }

    public TileQueryHelper(Context context, TileStateListener tileStateListener) {
        this.mContext = context;
        this.mListener = tileStateListener;
        this.mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mMainHandler = (Handler) Dependency.get(Dependency.MAIN_HANDLER);
        this.mTilesStock = this.mContext.getString(R.string.quick_settings_tiles_stock);
    }

    public void queryTiles(QSTileHost qSTileHost) {
        this.mTiles.clear();
        this.mSpecs.clear();
        addStockTiles(qSTileHost);
        addPackageTiles(qSTileHost);
    }

    public void releaseTiles() {
        for (QSTile next : this.mLiveTiles.values()) {
            next.removeCallbacks();
            next.setListening(this, false);
            next.destroy();
        }
        this.mLiveTiles.clear();
    }

    private void addStockTiles(QSTileHost qSTileHost) {
        String[] split = qSTileHost.getQsStockTiles().split(",");
        final ArrayList arrayList = new ArrayList();
        for (String str : split) {
            QSTile createTile = qSTileHost.createTile(str);
            if (createTile != null) {
                if (!createTile.isAvailable()) {
                    createTile.destroy();
                } else {
                    createTile.setListening(this, true);
                    createTile.clearState();
                    createTile.refreshState();
                    createTile.addCallback(new TileCallback(createTile));
                    createTile.setTileSpec(str);
                    arrayList.add(createTile);
                    this.mLiveTiles.put(str, createTile);
                }
            }
        }
        this.mBgHandler.post(new Runnable() {
            public void run() {
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    QSTile qSTile = (QSTile) it.next();
                    QSTile.State copy = qSTile.getState().copy();
                    copy.label = qSTile.getTileLabel();
                    TileQueryHelper.this.addTile(qSTile.getTileSpec(), (CharSequence) null, copy, true);
                }
            }
        });
    }

    private void addPackageTiles(final QSTileHost qSTileHost) {
        this.mBgHandler.post(new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT == 23) {
                    TileQueryHelper.this.notifyTilesChanged(true);
                    return;
                }
                Collection<QSTile> tiles = qSTileHost.getTiles();
                PackageManager packageManager = TileQueryHelper.this.mContext.getPackageManager();
                List<ResolveInfo> queryIntentServicesAsUser = packageManager.queryIntentServicesAsUser(new Intent("android.service.quicksettings.action.QS_TILE"), 0, ActivityManager.getCurrentUser());
                String qsStockTiles = qSTileHost.getQsStockTiles();
                for (ResolveInfo resolveInfo : queryIntentServicesAsUser) {
                    String str = resolveInfo.serviceInfo.packageName;
                    ComponentName componentName = new ComponentName(str, resolveInfo.serviceInfo.name);
                    if (!qsStockTiles.contains(componentName.flattenToString())) {
                        CharSequence loadLabel = resolveInfo.serviceInfo.applicationInfo.loadLabel(packageManager);
                        String spec = CustomTile.toSpec(componentName);
                        QSTile.State access$300 = TileQueryHelper.this.getState(tiles, spec);
                        if (access$300 != null) {
                            TileQueryHelper.this.addTile(spec, loadLabel, access$300, false);
                        } else {
                            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                            if (serviceInfo.icon != 0 || serviceInfo.applicationInfo.icon != 0) {
                                ServiceInfo serviceInfo2 = resolveInfo.serviceInfo;
                                int i = serviceInfo2.icon;
                                if (i == 0) {
                                    i = serviceInfo2.applicationInfo.icon;
                                }
                                Drawable drawable = null;
                                Icon createWithResource = i != 0 ? Icon.createWithResource(str, i) : null;
                                if (createWithResource != null) {
                                    try {
                                        drawable = createWithResource.loadDrawable(TileQueryHelper.this.mContext);
                                    } catch (Exception unused) {
                                        Log.w("TileQueryHelper", "Invalid icon");
                                    }
                                }
                                if ("android.permission.BIND_QUICK_SETTINGS_TILE".equals(resolveInfo.serviceInfo.permission) && drawable != null) {
                                    drawable.mutate();
                                    CharSequence loadLabel2 = resolveInfo.serviceInfo.loadLabel(packageManager);
                                    TileQueryHelper.this.addTile(spec, drawable, (CharSequence) loadLabel2 != null ? loadLabel2.toString() : "null", loadLabel);
                                }
                            }
                        }
                    }
                }
                TileQueryHelper.this.notifyTilesChanged(true);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyTilesChanged(final boolean z) {
        final ArrayList arrayList = new ArrayList(this.mTiles);
        this.mMainHandler.post(new Runnable() {
            public void run() {
                TileQueryHelper.this.mListener.onTilesChanged(arrayList, TileQueryHelper.this.mLiveTiles);
                boolean unused = TileQueryHelper.this.mFinished = z;
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateStateForCustomizer(QSTile.State state) {
        state.dualTarget = false;
        state.expandedAccessibilityClassName = Button.class.getName();
    }

    /* access modifiers changed from: private */
    public QSTile.State getState(Collection<QSTile> collection, String str) {
        for (QSTile next : collection) {
            if (str.equals(next.getTileSpec())) {
                return next.getState().copy();
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public void addTile(String str, CharSequence charSequence, QSTile.State state, boolean z) {
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

    /* access modifiers changed from: private */
    public void addTile(String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2) {
        QSTile.State state = new QSTile.State();
        state.state = 1;
        state.label = charSequence;
        state.contentDescription = charSequence;
        state.icon = new QSTileImpl.DrawableIcon(drawable);
        addTile(str, charSequence2, state, false);
    }

    private class TileCallback implements QSTile.Callback {
        private QSTile mTile;

        public void onAnnouncementRequested(CharSequence charSequence) {
        }

        public void onScanStateChanged(boolean z) {
        }

        public void onShowDetail(boolean z) {
        }

        public void onShowEdit(boolean z) {
        }

        public void onToggleStateChanged(boolean z) {
        }

        TileCallback(QSTile qSTile) {
            this.mTile = qSTile;
        }

        public void onStateChanged(QSTile.State state) {
            QSTile.State copy = this.mTile.getState().copy();
            TileQueryHelper.this.updateStateForCustomizer(copy);
            copy.label = this.mTile.getTileLabel();
            Iterator it = TileQueryHelper.this.mTiles.iterator();
            while (it.hasNext()) {
                final TileInfo tileInfo = (TileInfo) it.next();
                if (TextUtils.equals(this.mTile.getTileSpec(), tileInfo.spec)) {
                    tileInfo.state = copy;
                    TileQueryHelper.this.mMainHandler.post(new Runnable() {
                        public void run() {
                            TileQueryHelper.this.mListener.onTileChanged(tileInfo);
                        }
                    });
                    return;
                }
            }
        }
    }
}
