package com.android.systemui.qs;

import android.content.ComponentName;
import android.content.Context;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsLoggerCompat;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSDetail;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class QSPanel extends LinearLayout implements QSHost.Callback {
    protected final Context mContext;
    /* access modifiers changed from: private */
    public QSCustomizer.QSPanelCallback mCustomizerCallback;
    private QSDetail.QSPanelCallback mDetailCallback;
    /* access modifiers changed from: private */
    public Record mDetailRecord;
    private int mEditTopOffset;
    protected boolean mExpanded;
    protected QSSecurityFooter mFooter;
    /* access modifiers changed from: private */
    public final H mHandler;
    protected QSTileHost mHost;
    protected boolean mListening;
    private final MetricsLogger mMetricsLogger;
    private View mPageIndicator;
    protected final ArrayList<TileRecord> mRecords;
    protected QSTileLayout mTileLayout;

    public interface QSTileLayout {
        void addTile(TileRecord tileRecord);

        int getOffsetTop(TileRecord tileRecord);

        void removeTile(TileRecord tileRecord);

        void restoreInstanceState(Bundle bundle) {
        }

        void saveInstanceState(Bundle bundle) {
        }

        void setListening(boolean z);

        void setOldModeOn(boolean z) {
        }

        boolean updateResources();
    }

    public static class Record {
        public DetailAdapter detailAdapter;
        public View translateView;
        public View wholeView;
        public int x;
        public int y;
    }

    public static final class TileRecord extends Record {
        public QSTile.Callback callback;
        public View expandIndicator;
        public boolean scanState;
        public QSTile tile;
        public QSTileView tileView;
    }

    public QSPanel(Context context) {
        this(context, (AttributeSet) null);
    }

    public QSPanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRecords = new ArrayList<>();
        this.mHandler = new H();
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mContext = context;
        this.mEditTopOffset = getResources().getDimensionPixelSize(R.dimen.qs_detail_margin_top);
        initViews();
    }

    /* access modifiers changed from: protected */
    public void initViews() {
        setOrientation(1);
        setupTileLayout();
        setupPageIndicator();
        setupFooter();
        updateResources(false);
    }

    public View getPageIndicator() {
        return this.mPageIndicator;
    }

    /* access modifiers changed from: protected */
    public void setupTileLayout() {
        this.mTileLayout = (QSTileLayout) LayoutInflater.from(this.mContext).inflate(R.layout.qs_paged_tile_layout, this, false);
        this.mTileLayout.setListening(this.mListening);
        addView((View) this.mTileLayout);
    }

    /* access modifiers changed from: protected */
    public void setupPageIndicator() {
        this.mPageIndicator = LayoutInflater.from(this.mContext).inflate(R.layout.qs_page_indicator, this, false);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mPageIndicator.getLayoutParams();
        layoutParams.bottomMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_page_indicator_dot_bottom_margin);
        addView(this.mPageIndicator, layoutParams);
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            ((PagedTileLayout) qSTileLayout).setPageIndicator((PageIndicator) this.mPageIndicator);
        }
    }

    /* access modifiers changed from: protected */
    public void setupFooter() {
        this.mFooter = new QSSecurityFooter(this, this.mContext);
        addView(this.mFooter.getView());
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.removeCallbacks();
        }
        super.onDetachedFromWindow();
    }

    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    public void openDetails(String str) {
        showDetailAdapter(true, getTile(str).getDetailAdapter(), new int[]{getWidth() / 2, 0});
    }

    private QSTile getTile(String str) {
        for (int i = 0; i < this.mRecords.size(); i++) {
            if (str.equals(this.mRecords.get(i).tile.getTileSpec())) {
                return this.mRecords.get(i).tile;
            }
        }
        return this.mHost.createTile(str);
    }

    public void setQSDetailCallback(QSDetail.QSPanelCallback qSPanelCallback) {
        this.mDetailCallback = qSPanelCallback;
    }

    public void setQSCustomizerCallback(QSCustomizer.QSPanelCallback qSPanelCallback) {
        this.mCustomizerCallback = qSPanelCallback;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mHost.addCallback(this);
        setTiles(this.mHost.getTiles());
        this.mFooter.setHostEnvironment(qSTileHost);
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public void updateResources(boolean z) {
        this.mFooter.onConfigurationChanged();
        this.mEditTopOffset = getResources().getDimensionPixelSize(R.dimen.qs_detail_margin_top);
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            next.tileView.getIcon().updateResources();
            next.tile.clearState();
        }
        if (this.mListening) {
            refreshAllTiles();
        }
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.updateResources();
        }
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mExpanded = z;
            if (!this.mExpanded) {
                QSTileLayout qSTileLayout = this.mTileLayout;
                if (qSTileLayout instanceof PagedTileLayout) {
                    ((PagedTileLayout) qSTileLayout).setCurrentItem(0, false);
                }
            }
            MetricsLogger.visibility(getContext(), R.styleable.AppCompatTheme_toolbarStyle, this.mExpanded);
            if (!this.mExpanded) {
                closeDetail(false);
            } else {
                logTiles();
            }
        }
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            QSTileLayout qSTileLayout = this.mTileLayout;
            if (qSTileLayout != null) {
                qSTileLayout.setListening(z);
            }
            this.mFooter.setListening(this.mListening);
            if (this.mListening) {
                refreshAllTiles();
            }
        }
    }

    public void refreshAllTiles() {
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.refreshState();
        }
        this.mFooter.refreshState();
    }

    public void showDetailAdapter(boolean z, DetailAdapter detailAdapter, int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        ((View) getParent()).getLocationInWindow(iArr);
        Record record = new Record();
        record.detailAdapter = detailAdapter;
        record.x = i - iArr[0];
        record.y = i2 - iArr[1];
        iArr[0] = i;
        iArr[1] = i2;
        showDetail(z, record);
    }

    /* access modifiers changed from: protected */
    public void showDetail(boolean z, Record record) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0, record).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void showEdit(boolean z, Record record) {
        this.mHandler.obtainMessage(4, z ? 1 : 0, 0, record).sendToTarget();
    }

    public void setTiles(Collection<QSTile> collection) {
        setTiles(collection, false);
    }

    public void setTiles(Collection<QSTile> collection, boolean z) {
        ArrayList arrayList = new ArrayList(collection);
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            this.mTileLayout.removeTile(next);
            next.tile.removeCallback(next.callback);
        }
        this.mRecords.clear();
        Iterator it2 = arrayList.iterator();
        int i = 1;
        while (it2.hasNext()) {
            QSTile qSTile = (QSTile) it2.next();
            qSTile.setIndex(i);
            addTile(qSTile, z);
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public void drawTile(TileRecord tileRecord, QSTile.State state) {
        tileRecord.tileView.onStateChanged(state);
    }

    /* access modifiers changed from: protected */
    public QSTileView createTileView(QSTile qSTile, boolean z) {
        return this.mHost.createTileView(qSTile, z);
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowDetail() {
        return this.mExpanded;
    }

    /* access modifiers changed from: protected */
    public TileRecord addTile(QSTile qSTile, boolean z) {
        final TileRecord tileRecord = new TileRecord();
        tileRecord.tile = qSTile;
        tileRecord.tileView = createTileView(qSTile, z);
        AnonymousClass1 r2 = new QSTile.Callback() {
            public void onStateChanged(QSTile.State state) {
                QSPanel.this.drawTile(tileRecord, state);
            }

            public void onShowDetail(boolean z) {
                if (QSPanel.this.shouldShowDetail() || !z) {
                    QSPanel.this.showDetail(z, tileRecord);
                }
            }

            public void onShowEdit(boolean z) {
                QSPanel.this.showEdit(z, tileRecord);
            }

            public void onToggleStateChanged(boolean z) {
                if (QSPanel.this.mDetailRecord == tileRecord) {
                    QSPanel.this.fireToggleStateChanged(z);
                }
            }

            public void onScanStateChanged(boolean z) {
                tileRecord.scanState = z;
                Record access$100 = QSPanel.this.mDetailRecord;
                TileRecord tileRecord = tileRecord;
                if (access$100 == tileRecord) {
                    QSPanel.this.fireScanStateChanged(tileRecord.scanState);
                }
            }

            public void onAnnouncementRequested(CharSequence charSequence) {
                if (charSequence != null) {
                    QSPanel.this.mHandler.obtainMessage(3, charSequence).sendToTarget();
                }
            }
        };
        tileRecord.tile.addCallback(r2);
        tileRecord.callback = r2;
        tileRecord.tileView.init(tileRecord.tile);
        tileRecord.tile.refreshState();
        this.mRecords.add(tileRecord);
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.addTile(tileRecord);
        }
        return tileRecord;
    }

    public void showEdit(final View view) {
        view.post(new Runnable() {
            public void run() {
                if (QSPanel.this.mCustomizerCallback != null) {
                    int[] iArr = new int[2];
                    view.getLocationInWindow(iArr);
                    QSPanel.this.mCustomizerCallback.show(iArr[0] + (view.getWidth() / 2), iArr[1] + (view.getHeight() / 2));
                }
            }
        });
    }

    public void closeDetail(boolean z) {
        QSCustomizer.QSPanelCallback qSPanelCallback = this.mCustomizerCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.hide(0, 0, z);
        }
        Record record = this.mDetailRecord;
        if (record == null || !(record instanceof TileRecord)) {
            showDetail(false, this.mDetailRecord);
            return;
        }
        QSTile qSTile = ((TileRecord) record).tile;
        if (qSTile instanceof QSTileImpl) {
            ((QSTileImpl) qSTile).showDetail(false);
        }
    }

    /* access modifiers changed from: protected */
    public void handleShowDetail(Record record, boolean z) {
        int i;
        if (record instanceof TileRecord) {
            handleShowDetailTile((TileRecord) record, z);
            return;
        }
        int i2 = 0;
        if (record != null) {
            i2 = record.x;
            i = record.y;
        } else {
            i = 0;
        }
        handleShowDetailImpl(record, z, i2, i);
    }

    /* access modifiers changed from: protected */
    public void handleShowEdit(Record record, boolean z) {
        int i;
        if (record instanceof TileRecord) {
            handleShowEditTile((TileRecord) record);
            return;
        }
        int i2 = 0;
        if (record != null) {
            i2 = record.x;
            i = record.y;
        } else {
            i = 0;
        }
        fireShowingEdit(i2, i);
    }

    private void handleShowDetailTile(TileRecord tileRecord, boolean z) {
        if ((this.mDetailRecord != null) != z || this.mDetailRecord != tileRecord) {
            if (z) {
                tileRecord.detailAdapter = tileRecord.tile.getDetailAdapter();
                if (tileRecord.detailAdapter == null) {
                    return;
                }
            }
            tileRecord.tile.setDetailListening(z);
            QSTileView qSTileView = tileRecord.tileView;
            handleShowDetailImpl(tileRecord, z, qSTileView.getLeft() + ((int) qSTileView.getLastX()), ((int) qSTileView.getLastY()) + qSTileView.getTop() + this.mTileLayout.getOffsetTop(tileRecord) + getTop() + this.mEditTopOffset);
        }
    }

    private void handleShowEditTile(TileRecord tileRecord) {
        QSTileView qSTileView = tileRecord.tileView;
        fireShowingEdit(qSTileView.getLeft() + ((int) qSTileView.getLastX()), ((int) qSTileView.getLastY()) + qSTileView.getTop() + this.mTileLayout.getOffsetTop(tileRecord) + getTop() + this.mEditTopOffset);
    }

    private void handleShowDetailImpl(Record record, boolean z, int i, int i2) {
        DetailAdapter detailAdapter = null;
        setDetailRecord(z ? record : null);
        if (z) {
            detailAdapter = record.detailAdapter;
        }
        fireShowingDetail(detailAdapter, i, i2);
    }

    /* access modifiers changed from: protected */
    public void setDetailRecord(Record record) {
        if (record != this.mDetailRecord) {
            this.mDetailRecord = record;
            Record record2 = this.mDetailRecord;
            fireScanStateChanged((record2 instanceof TileRecord) && ((TileRecord) record2).scanState);
        }
    }

    private void logTiles() {
        for (int i = 0; i < this.mRecords.size(); i++) {
            MetricsLoggerCompat.write(this.mContext, this.mMetricsLogger, new LogMaker(this.mRecords.get(i).tile.getMetricsCategory()).setType(1));
        }
    }

    private void fireShowingDetail(DetailAdapter detailAdapter, int i, int i2) {
        QSDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onShowingDetail(detailAdapter, i, i2);
        }
    }

    private void fireShowingEdit(int i, int i2) {
        QSCustomizer.QSPanelCallback qSPanelCallback = this.mCustomizerCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.show(i, i2);
        }
    }

    /* access modifiers changed from: private */
    public void fireToggleStateChanged(boolean z) {
        QSDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onToggleStateChanged(z);
        }
    }

    /* access modifiers changed from: private */
    public void fireScanStateChanged(boolean z) {
        QSDetail.QSPanelCallback qSPanelCallback = this.mDetailCallback;
        if (qSPanelCallback != null) {
            qSPanelCallback.onScanStateChanged(z);
        }
    }

    public void clickTile(ComponentName componentName) {
        String spec = CustomTile.toSpec(componentName);
        int size = this.mRecords.size();
        for (int i = 0; i < size; i++) {
            if (this.mRecords.get(i).tile.getTileSpec().equals(spec)) {
                this.mRecords.get(i).tile.click();
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public QSTileLayout getTileLayout() {
        return this.mTileLayout;
    }

    /* access modifiers changed from: package-private */
    public QSTileView getTileView(QSTile qSTile) {
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            if (next.tile == qSTile) {
                return next.tileView;
            }
        }
        return null;
    }

    public QSSecurityFooter getFooter() {
        return this.mFooter;
    }

    public void showDeviceMonitoringDialog() {
        this.mFooter.showDeviceMonitoringDialog();
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = false;
            if (i == 1) {
                QSPanel qSPanel = QSPanel.this;
                Record record = (Record) message.obj;
                if (message.arg1 != 0) {
                    z = true;
                }
                qSPanel.handleShowDetail(record, z);
            } else if (i == 4) {
                QSPanel qSPanel2 = QSPanel.this;
                Record record2 = (Record) message.obj;
                if (message.arg1 != 0) {
                    z = true;
                }
                qSPanel2.handleShowEdit(record2, z);
            } else if (i == 3) {
                QSPanel.this.announceForAccessibility((CharSequence) message.obj);
            }
        }
    }
}
