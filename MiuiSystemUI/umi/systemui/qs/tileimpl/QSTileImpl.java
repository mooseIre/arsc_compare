package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsLoggerCompat;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsHelper;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Util;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSIconViewImpl;
import com.android.systemui.miui.statusbar.ControlCenterActivityStarter;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTile.State;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.CustomTile;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class QSTileImpl<TState extends QSTile.State> implements QSTile, Dumpable {
    protected static final Object ARG_SHOW_TRANSIENT_ENABLING = new Object();
    /* access modifiers changed from: protected */
    public static final boolean DEBUG = Log.isLoggable("Tile", 3);
    /* access modifiers changed from: protected */
    public final String TAG = ("QSTile." + getClass().getSimpleName());
    private boolean mAnnounceNextStateChange;
    private final ArrayList<QSTile.Callback> mCallbacks = new ArrayList<>();
    /* access modifiers changed from: protected */
    public final Context mContext;
    /* access modifiers changed from: private */
    public RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;
    /* access modifiers changed from: protected */
    public QSTileImpl<TState>.H mHandler = new H((Looper) Dependency.get(Dependency.BG_LOOPER));
    /* access modifiers changed from: protected */
    public final QSHost mHost;
    protected boolean mInControlCenter;
    private int mIndex = -1;
    private int mIsFullQs;
    private final ArraySet<Object> mListeners = new ArraySet<>();
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private boolean mShowingDetail;
    private final Object mStaleListener = new Object();
    /* access modifiers changed from: protected */
    public TState mState = newTileState();
    private String mTileSpec;
    private TState mTmpState = newTileState();
    protected final Handler mUiHandler = new Handler(Looper.getMainLooper());

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        return null;
    }

    public DetailAdapter getDetailAdapter() {
        return null;
    }

    public abstract Intent getLongClickIntent();

    public abstract int getMetricsCategory();

    /* access modifiers changed from: protected */
    public long getStaleTimeout() {
        return 600000;
    }

    /* access modifiers changed from: protected */
    public abstract void handleClick();

    /* access modifiers changed from: protected */
    public abstract void handleSetListening(boolean z);

    /* access modifiers changed from: protected */
    public abstract void handleUpdateState(TState tstate, Object obj);

    /* access modifiers changed from: protected */
    public boolean hideCustomizerAfterClick() {
        return false;
    }

    public boolean isAvailable() {
        return true;
    }

    public abstract TState newTileState();

    public void setDetailListening(boolean z) {
    }

    /* access modifiers changed from: protected */
    public boolean shouldAnnouncementBeDelayed() {
        return false;
    }

    protected QSTileImpl(QSHost qSHost) {
        this.mHost = qSHost;
        this.mContext = qSHost.getContext();
    }

    public final void resetStates() {
        this.mState = newTileState();
        this.mTmpState = newTileState();
    }

    public void setListening(Object obj, boolean z) {
        this.mHandler.obtainMessage(14, z ? 1 : 0, 0, obj).sendToTarget();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void handleStale() {
        setListening(this.mStaleListener, true);
    }

    public int getIndex() {
        return this.mIndex;
    }

    public void setIndex(int i) {
        this.mIndex = i;
    }

    public String getTileSpec() {
        return this.mTileSpec;
    }

    public void setTileSpec(String str) {
        this.mTileSpec = str;
    }

    public void setInControlCenter(boolean z) {
        this.mInControlCenter = z;
    }

    public QSIconView createTileView(Context context) {
        return new QSIconViewImpl(context);
    }

    public QSIconView createControlCenterTileView(Context context) {
        return new CCQSIconViewImpl(context);
    }

    public void addCallback(QSTile.Callback callback) {
        this.mHandler.obtainMessage(1, callback).sendToTarget();
        String str = this.TAG;
        Log.d(str, " callback=" + callback);
    }

    public void removeCallback(QSTile.Callback callback) {
        this.mHandler.obtainMessage(13, callback).sendToTarget();
    }

    public void removeCallbacks() {
        this.mHandler.sendEmptyMessage(12);
    }

    public void click() {
        click(false);
    }

    public void click(boolean z) {
        MetricsLoggerCompat.write(this.mContext, this.mMetricsLogger, populate(new LogMaker(925).setType(4)));
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).onClickQSTile(getTileSpec(), z, getIndex());
        Message obtain = Message.obtain(this.mHandler, 2, Boolean.valueOf(z));
        if (!hideCustomizerAfterClick() || !z) {
            Log.d(this.TAG, "send click msg");
            obtain.sendToTarget();
            return;
        }
        Log.d(this.TAG, "send click msg delayed");
        showEdit(false);
        this.mHandler.sendMessageDelayed(obtain, 420);
    }

    public void secondaryClick() {
        MetricsLoggerCompat.write(this.mContext, this.mMetricsLogger, populate(new LogMaker(926).setType(4)));
        TState tstate = this.mState;
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleTrackQSTileSecondaryClick(getTileSpec(), getIndex(), tstate instanceof QSTile.BooleanState ? ((QSTile.BooleanState) tstate).value : false);
        Log.d(this.TAG, "send secondary click msg");
        this.mHandler.sendEmptyMessage(3);
    }

    public void longClick() {
        MetricsLoggerCompat.write(this.mContext, this.mMetricsLogger, populate(new LogMaker(366).setType(4)));
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleTrackQSTileLongClick(getTileSpec(), getIndex());
        Log.d(this.TAG, "send long click msg");
        this.mHandler.sendEmptyMessage(4);
    }

    public LogMaker populate(LogMaker logMaker) {
        TState tstate = this.mState;
        if (tstate instanceof QSTile.BooleanState) {
            logMaker.addTaggedData(928, Integer.valueOf(((QSTile.BooleanState) tstate).value ? 1 : 0));
        }
        return logMaker.setSubtype(getMetricsCategory()).addTaggedData(833, Integer.valueOf(this.mIsFullQs)).addTaggedData(927, Integer.valueOf(this.mHost.indexOf(this.mTileSpec)));
    }

    public void showDetail(boolean z) {
        this.mHandler.obtainMessage(6, z ? 1 : 0, 0).sendToTarget();
    }

    public void showEdit(boolean z) {
        this.mHandler.obtainMessage(15, z ? 1 : 0, 0).sendToTarget();
    }

    public void refreshState() {
        refreshState((Object) null);
    }

    /* access modifiers changed from: protected */
    public final void refreshState(Object obj) {
        this.mHandler.obtainMessage(5, obj).sendToTarget();
    }

    public void clearState() {
        this.mHandler.sendEmptyMessage(11);
    }

    public void userSwitch(int i) {
        this.mHandler.obtainMessage(7, i, 0).sendToTarget();
    }

    public void fireToggleStateChanged(boolean z) {
        this.mHandler.obtainMessage(8, z ? 1 : 0, 0).sendToTarget();
    }

    public void destroy() {
        this.mHandler.sendEmptyMessage(10);
    }

    public TState getState() {
        return this.mState;
    }

    /* access modifiers changed from: private */
    public void handleAddCallback(QSTile.Callback callback) {
        this.mCallbacks.add(callback);
        callback.onStateChanged(this.mState);
    }

    /* access modifiers changed from: private */
    public void handleRemoveCallback(QSTile.Callback callback) {
        this.mCallbacks.remove(callback);
    }

    /* access modifiers changed from: private */
    public void handleRemoveCallbacks() {
        this.mCallbacks.clear();
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        handleClick();
    }

    /* access modifiers changed from: protected */
    public void handleLongClick() {
        Intent longClickIntent = getLongClickIntent();
        if (longClickIntent != null) {
            postStartActivityDismissingKeyguard(longClickIntent, 0);
        }
    }

    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        if (this.mInControlCenter) {
            ((ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class)).postStartActivityDismissingKeyguard(intent);
        } else {
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(intent, i);
        }
    }

    /* access modifiers changed from: protected */
    public void handleClearState() {
        this.mTmpState = newTileState();
        this.mState = newTileState();
    }

    /* access modifiers changed from: protected */
    public void handleRefreshState(Object obj) {
        handleUpdateState(this.mTmpState, obj);
        if (this.mTmpState.copyTo(this.mState)) {
            handleStateChanged();
        }
        this.mHandler.removeMessages(16);
        this.mHandler.sendEmptyMessageDelayed(16, getStaleTimeout());
        setListening(this.mStaleListener, false);
    }

    private void handleStateChanged() {
        String composeChangeAnnouncement;
        boolean shouldAnnouncementBeDelayed = shouldAnnouncementBeDelayed();
        boolean z = false;
        if (this.mCallbacks.size() != 0) {
            QSTile.State newTileState = newTileState();
            this.mState.copyTo(newTileState);
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                this.mCallbacks.get(i).onStateChanged(newTileState);
            }
            if (this.mAnnounceNextStateChange && !shouldAnnouncementBeDelayed && (composeChangeAnnouncement = composeChangeAnnouncement()) != null) {
                this.mCallbacks.get(0).onAnnouncementRequested(composeChangeAnnouncement);
            }
        }
        if (this.mAnnounceNextStateChange && shouldAnnouncementBeDelayed) {
            z = true;
        }
        this.mAnnounceNextStateChange = z;
    }

    /* access modifiers changed from: private */
    public void handleShowDetail(boolean z) {
        this.mShowingDetail = z;
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onShowDetail(z);
        }
    }

    /* access modifiers changed from: private */
    public void handleShowEdit(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onShowEdit(z);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isShowingDetail() {
        return this.mShowingDetail;
    }

    /* access modifiers changed from: private */
    public void handleToggleStateChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onToggleStateChanged(z);
        }
    }

    /* access modifiers changed from: private */
    public void handleScanStateChanged(boolean z) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onScanStateChanged(z);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
        handleRefreshState((Object) null);
    }

    /* access modifiers changed from: private */
    public void handleSetListeningInternal(Object obj, boolean z) {
        if (z) {
            if (this.mListeners.add(obj) && ((this instanceof CustomTile) || Util.isMiuiOptimizationDisabled() || this.mListeners.size() == 1)) {
                if (DEBUG) {
                    Log.d(this.TAG, "handleSetListening true");
                }
                handleSetListening(z);
                refreshState();
            }
        } else if (this.mListeners.remove(obj) && ((this instanceof CustomTile) || Util.isMiuiOptimizationDisabled() || this.mListeners.size() == 0)) {
            if (DEBUG) {
                Log.d(this.TAG, "handleSetListening false");
            }
            handleSetListening(z);
        }
        updateIsFullQs();
    }

    private void updateIsFullQs() {
        Iterator<Object> it = this.mListeners.iterator();
        while (it.hasNext()) {
            if (PagedTileLayout.TilePage.class.equals(it.next().getClass())) {
                this.mIsFullQs = 1;
                return;
            }
        }
        this.mIsFullQs = 0;
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        if (this.mListeners.size() != 0) {
            handleSetListening(false);
        }
        this.mCallbacks.clear();
    }

    /* access modifiers changed from: protected */
    public void checkIfRestrictionEnforcedByAdminOnly(QSTile.State state, String str) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtilsHelper.checkIfRestrictionEnforced(this.mContext, str, KeyguardUpdateMonitor.getCurrentUser());
        if (checkIfRestrictionEnforced == null || RestrictedLockUtilsHelper.hasBaseUserRestriction(this.mContext, str, KeyguardUpdateMonitor.getCurrentUser())) {
            state.disabledByPolicy = false;
            this.mEnforcedAdmin = null;
            return;
        }
        state.disabledByPolicy = true;
        this.mEnforcedAdmin = checkIfRestrictionEnforced;
    }

    protected final class H extends Handler {
        @VisibleForTesting
        protected H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            try {
                boolean z = true;
                if (message.what == 1) {
                    QSTileImpl.this.handleAddCallback((QSTile.Callback) message.obj);
                } else if (message.what == 12) {
                    QSTileImpl.this.handleRemoveCallbacks();
                } else if (message.what == 13) {
                    QSTileImpl.this.handleRemoveCallback((QSTile.Callback) message.obj);
                } else if (message.what == 2) {
                    if (!(message.obj instanceof Boolean) || !((Boolean) message.obj).booleanValue()) {
                        z = false;
                    }
                    if (QSTileImpl.this.mState.disabledByPolicy) {
                        QSTileImpl.this.postStartActivityDismissingKeyguard(RestrictedLockUtils.getShowAdminSupportDetailsIntent(QSTileImpl.this.mContext, QSTileImpl.this.mEnforcedAdmin), 0);
                    } else if (QSTileImpl.this.getState().state != 0 || Util.isMiuiOptimizationDisabled()) {
                        QSTileImpl.this.handleClick();
                        if (!z && QSTileImpl.this.mHost.collapseAfterClick() && !"edit".equals(QSTileImpl.this.getTileSpec()) && !"autobrightness".equals(QSTileImpl.this.getTileSpec())) {
                            QSTileImpl.this.mHost.collapsePanels();
                        }
                    }
                } else if (message.what == 3) {
                    QSTileImpl.this.handleSecondaryClick();
                } else if (message.what == 4) {
                    QSTileImpl.this.handleLongClick();
                } else if (message.what == 5) {
                    QSTileImpl.this.handleRefreshState(message.obj);
                } else if (message.what == 6) {
                    QSTileImpl qSTileImpl = QSTileImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    qSTileImpl.handleShowDetail(z);
                } else if (message.what == 15) {
                    QSTileImpl qSTileImpl2 = QSTileImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    qSTileImpl2.handleShowEdit(z);
                } else if (message.what == 7) {
                    QSTileImpl.this.handleUserSwitch(message.arg1);
                } else if (message.what == 8) {
                    QSTileImpl qSTileImpl3 = QSTileImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    qSTileImpl3.handleToggleStateChanged(z);
                } else if (message.what == 9) {
                    QSTileImpl qSTileImpl4 = QSTileImpl.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    qSTileImpl4.handleScanStateChanged(z);
                } else if (message.what == 10) {
                    QSTileImpl.this.handleDestroy();
                } else if (message.what == 11) {
                    QSTileImpl.this.handleClearState();
                } else if (message.what == 14) {
                    QSTileImpl qSTileImpl5 = QSTileImpl.this;
                    Object obj = message.obj;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    qSTileImpl5.handleSetListeningInternal(obj, z);
                } else if (message.what == 16) {
                    QSTileImpl.this.handleStale();
                } else {
                    throw new IllegalArgumentException("Unknown msg: " + message.what);
                }
            } catch (Throwable th) {
                String str = "Error in " + null;
                Log.w(QSTileImpl.this.TAG, str, th);
                QSTileImpl.this.mHost.warn(str, th);
            }
        }
    }

    public static class DrawableIcon extends QSTile.Icon {
        protected final Drawable mDrawable;

        public DrawableIcon(Drawable drawable) {
            this.mDrawable = drawable;
        }

        public Drawable getDrawable(Context context) {
            return this.mDrawable;
        }
    }

    public static class ResourceIcon extends QSTile.Icon {
        private static final SparseArray<QSTile.Icon> ICONS = new SparseArray<>();
        protected final int mResId;

        private ResourceIcon(int i) {
            this.mResId = i;
        }

        public static QSTile.Icon get(int i) {
            QSTile.Icon icon = ICONS.get(i);
            if (icon != null) {
                return icon;
            }
            ResourceIcon resourceIcon = new ResourceIcon(i);
            ICONS.put(i, resourceIcon);
            return resourceIcon;
        }

        public Drawable getDrawable(Context context) {
            return context.getDrawable(this.mResId);
        }

        public Drawable getInvisibleDrawable(Context context) {
            return context.getDrawable(this.mResId);
        }

        public boolean equals(Object obj) {
            return (obj instanceof ResourceIcon) && ((ResourceIcon) obj).mResId == this.mResId;
        }

        public String toString() {
            return String.format("ResourceIcon[resId=0x%08x]", new Object[]{Integer.valueOf(this.mResId)});
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(getClass().getSimpleName() + ":");
        printWriter.print("    ");
        printWriter.println(getState().toString());
    }
}
