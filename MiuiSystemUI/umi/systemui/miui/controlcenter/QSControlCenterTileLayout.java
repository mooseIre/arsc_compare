package com.android.systemui.miui.controlcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSTileView;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSDetail;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;

public class QSControlCenterTileLayout extends ViewGroup implements QSPanel.QSTileLayout, QSHost.Callback, ControlPanelWindowManager.OnExpandChangeListener {
    private int mBaseLineIdx;
    private int mCellHeight;
    private int mCellWidth;
    private int mColumnMarginTop;
    private int mColumns;
    private QSCustomizer.QSPanelCallback mCustomizerCallback;
    private QSDetail.QSPanelCallback mDetailCallback;
    private int mExpandHeightThres = 1;
    private boolean mExpanded;
    private boolean mExpanding;
    /* access modifiers changed from: private */
    public final H mHandler;
    private QSControlTileHost mHost;
    private float mLastHeight = -1.0f;
    private boolean mListening;
    private int mMaxHeight;
    private int mMinCellHeight;
    private int mMinHeight;
    private int mMinShowRows;
    private float mNewHeight = -1.0f;
    private float mOffset;
    private int mOrientation;
    private ControlPanelController mPanelController;
    protected AnimState mPanelHideAnim;
    private float mPanelLandWidth;
    private float mPanelPaddingHorizontal;
    protected AnimState mPanelShowAnim;
    private QSControlCenterPanel mQSControlCenterPanel;
    protected final ArrayList<QSPanel.TileRecord> mRecords;
    private int mRowMarginStart;
    private int mShowLines;

    private void handleShowDetailTile(QSPanel.TileRecord tileRecord, boolean z) {
    }

    private void handleShowEditTile(QSPanel.TileRecord tileRecord) {
    }

    public int getOffsetTop(QSPanel.TileRecord tileRecord) {
        return 0;
    }

    /* access modifiers changed from: protected */
    public void setDetailRecord(QSPanel.Record record) {
    }

    public QSControlCenterTileLayout(Context context) {
        super(context, (AttributeSet) null);
        AnimState animState = new AnimState("control_panel_title_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        this.mPanelShowAnim = animState;
        AnimState animState2 = new AnimState("control_panel_title_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState2.add(ViewProperty.SCALE_X, 0.8f, new long[0]);
        animState2.add(ViewProperty.SCALE_Y, 0.8f, new long[0]);
        this.mPanelHideAnim = animState2;
        new ArrayList();
        new ArrayList();
        this.mRecords = new ArrayList<>();
        new ArrayList();
        new ArrayList();
        this.mHandler = new H();
    }

    public QSControlCenterTileLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AnimState animState = new AnimState("control_panel_title_show");
        animState.add(ViewProperty.ALPHA, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        this.mPanelShowAnim = animState;
        AnimState animState2 = new AnimState("control_panel_title_hide");
        animState2.add(ViewProperty.ALPHA, 0.0f, new long[0]);
        animState2.add(ViewProperty.SCALE_X, 0.8f, new long[0]);
        animState2.add(ViewProperty.SCALE_Y, 0.8f, new long[0]);
        this.mPanelHideAnim = animState2;
        new ArrayList();
        new ArrayList();
        this.mRecords = new ArrayList<>();
        new ArrayList();
        new ArrayList();
        this.mHandler = new H();
        this.mContext = context;
        this.mPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        updateResources();
    }

    public void setQSControlCenterPanel(QSControlCenterPanel qSControlCenterPanel) {
        this.mQSControlCenterPanel = qSControlCenterPanel;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).addExpandChangeListener(this);
        post(new Runnable() {
            public void run() {
                QSControlCenterTileLayout.this.updateWidth();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        QSControlTileHost qSControlTileHost = this.mHost;
        if (qSControlTileHost != null) {
            qSControlTileHost.removeCallback(this);
        }
        ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).removeExpandChangeListener(this);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.removeCallbacks();
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        Log.d("QSControlCenterTileLayout", "onConfigurationChanged orientation=" + this.mOrientation + "  newConfig.orientation=" + configuration.orientation);
        super.onConfigurationChanged(configuration);
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            updateWidth();
        }
    }

    /* access modifiers changed from: private */
    public void updateWidth() {
        Log.d("QSControlCenterTileLayout", "updateWidth orientation=" + this.mOrientation);
        if (this.mOrientation == 2) {
            this.mRowMarginStart = ((int) ((this.mPanelLandWidth - (this.mPanelPaddingHorizontal * 2.0f)) - ((float) (this.mCellWidth * 4)))) / 3;
            requestLayout();
            return;
        }
        Point screenSize = Utils.getScreenSize(this.mContext);
        if (screenSize != null) {
            this.mRowMarginStart = ((int) ((((float) screenSize.x) - (this.mPanelPaddingHorizontal * 2.0f)) - ((float) (this.mCellWidth * 4)))) / 3;
            requestLayout();
        }
    }

    public boolean updateResources() {
        this.mPanelPaddingHorizontal = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_panel_margin_horizontal);
        this.mPanelLandWidth = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_width_land);
        this.mColumns = this.mContext.getResources().getInteger(R.dimen.qs_control_tiles_columns);
        this.mMinShowRows = this.mPanelController.isSuperPowerMode() ? 1 : this.mContext.getResources().getInteger(R.dimen.qs_control_tiles_min_rows);
        this.mColumnMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_margin_top);
        this.mCellWidth = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_width);
        this.mCellHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_center_tile_height) + CCQSTileView.getTextHeight(getContext());
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_tile_icon_bg_size);
        this.mMinCellHeight = dimensionPixelSize;
        int i = this.mMinShowRows;
        int i2 = (dimensionPixelSize * i) + ((i - 1) * this.mColumnMarginTop);
        this.mMinHeight = i2;
        if (i2 < 0) {
            float f = (float) i2;
            this.mLastHeight = f;
            this.mNewHeight = f;
        }
        if (this.mListening) {
            refreshAllTiles();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        View.MeasureSpec.getSize(i2);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        View view = this;
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            if (next.tileView.getVisibility() != 8) {
                next.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                view = next.tileView.updateAccessibilityOrder(view);
            }
        }
        this.mShowLines = this.mNewHeight > ((float) this.mMinHeight) ? (int) Math.ceil((double) (((float) this.mRecords.size()) / ((float) this.mColumns))) : this.mMinShowRows;
        float f = this.mNewHeight;
        int i3 = this.mMinHeight;
        if (f > ((float) i3)) {
            i3 = this.mMaxHeight;
        }
        setMeasuredDimension(size, i3);
        StringBuilder sb = new StringBuilder();
        sb.append("onMeasure height:");
        float f2 = this.mNewHeight;
        int i4 = this.mMinHeight;
        if (f2 > ((float) i4)) {
            i4 = this.mMaxHeight;
        }
        sb.append(i4);
        sb.append("  showLines:");
        sb.append(this.mShowLines);
        Log.d("QSControlCenterTileLayout", sb.toString());
    }

    private static int exactly(int i) {
        return View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7 = 0;
        boolean z2 = true;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        int i8 = 0;
        int i9 = 0;
        while (i7 < this.mRecords.size()) {
            int i10 = this.mColumns;
            if (i8 == i10) {
                i9++;
                i8 -= i10;
            }
            QSPanel.TileRecord tileRecord = this.mRecords.get(i7);
            int rowTop = getRowTop(tileRecord, i9);
            int rowBottom = getRowBottom(tileRecord, i9, rowTop);
            if (z2) {
                i6 = getColumnStart(i8);
                i5 = i6 - this.mCellWidth;
            } else {
                i5 = getColumnStart(i8);
                i6 = this.mCellWidth + i5;
            }
            tileRecord.tileView.layout(i5, rowTop, i6, rowBottom);
            i7++;
            i8++;
        }
    }

    private int getRowTop(QSPanel.TileRecord tileRecord, int i) {
        if (i == 0) {
            return 0;
        }
        QSTileView qSTileView = this.mRecords.get(this.mColumns * (i - 1)).tileView;
        if (i >= this.mMinShowRows || i == 0) {
            return qSTileView.getBottom() + this.mColumnMarginTop;
        }
        if (this.mExpanding) {
            float pow = (float) (1.0d - Math.pow((double) (1.0f - (this.mOffset / ((float) this.mExpandHeightThres))), 3.0d));
            int i2 = this.mMinCellHeight;
            return (int) ((((float) (this.mColumnMarginTop + i2)) + (((float) (this.mCellHeight - i2)) * pow)) * ((float) i));
        }
        tileRecord.tileView.getTop();
        return ((this.mExpanded ? this.mCellHeight : this.mMinCellHeight) + this.mColumnMarginTop) * i;
    }

    private int getRowBottom(QSPanel.TileRecord tileRecord, int i, int i2) {
        QSTileView qSTileView = tileRecord.tileView;
        if (i >= this.mMinShowRows) {
            return i2 + this.mCellHeight;
        }
        return i2 + (this.mNewHeight == ((float) this.mMinHeight) ? this.mMinCellHeight : this.mCellHeight);
    }

    private int getColumnStart(int i) {
        return i * (this.mCellWidth + this.mRowMarginStart);
    }

    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    public void addTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.add(tileRecord);
        tileRecord.tile.setListening(this, this.mListening);
        addView(tileRecord.tileView, new ViewGroup.LayoutParams(-2, -2));
        updateViewsLine();
    }

    public void removeTile(QSPanel.TileRecord tileRecord) {
        this.mRecords.remove(tileRecord);
        tileRecord.tile.setListening(this, false);
        removeView(tileRecord.tileView);
        updateViewsLine();
    }

    private void updateViewsLine() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            next.tileView.setTag(R.id.tag_tile_layout, Integer.valueOf(this.mRecords.indexOf(next) / this.mColumns));
        }
        int ceil = (int) Math.ceil((double) (((float) this.mRecords.size()) / ((float) this.mColumns)));
        int i = this.mMinCellHeight;
        int i2 = this.mMinShowRows;
        int i3 = this.mColumnMarginTop;
        this.mMinHeight = (i * i2) + ((i2 - 1) * i3);
        this.mMaxHeight = (this.mCellHeight * ceil) + ((ceil - 1) * i3);
        this.mQSControlCenterPanel.updateExpandHeightThres();
    }

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mHost = qSControlTileHost;
        qSControlTileHost.addCallback(this);
        setTiles(this.mHost.getTiles());
    }

    public void setExpanded(boolean z) {
        if (!this.mPanelController.isSuperPowerMode()) {
            this.mExpanded = z;
            this.mExpanding = false;
            this.mOffset = 0.0f;
            float f = (float) (z ? this.mMaxHeight : this.mMinHeight);
            this.mLastHeight = f;
            this.mNewHeight = f;
            visStartInit();
            Log.d("QSControlCenterTileLayout", "setExpanded:" + z);
            requestLayout();
        }
    }

    public void visStartInit() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            float f = 0.0f;
            if (((Integer) next.tileView.getTag(R.id.tag_tile_layout)).intValue() < this.mMinShowRows) {
                next.tileView.setAlpha(1.0f);
                next.tileView.getIcon().setAlpha(1.0f);
                CCQSTileView cCQSTileView = (CCQSTileView) next.tileView;
                if (this.mExpanded) {
                    f = 1.0f;
                }
                cCQSTileView.setLabelAlpha(f);
            } else {
                next.tileView.setAlpha(1.0f);
                CCQSTileView cCQSTileView2 = (CCQSTileView) next.tileView;
                if (this.mExpanded) {
                    f = 1.0f;
                }
                cCQSTileView2.setChildsAlpha(f);
            }
        }
    }

    public void visAnimOn(boolean z) {
        if (z) {
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                float intValue = (float) (this.mBaseLineIdx + ((Integer) next.tileView.getTag(R.id.tag_tile_layout)).intValue());
                Folme.useAt(next.tileView).state().end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
                IStateStyle state = Folme.useAt(next.tileView).state();
                AnimState animState = this.mPanelHideAnim;
                AnimState animState2 = this.mPanelShowAnim;
                AnimConfig animConfig = new AnimConfig();
                animConfig.setEase(EaseManager.getStyle(-2, ((0.2f * intValue) / 5.0f) + 0.7f, ((intValue * 0.1f) / 5.0f) + 0.5f));
                state.fromTo(animState, animState2, animConfig);
            }
            return;
        }
        Iterator<QSPanel.TileRecord> it2 = this.mRecords.iterator();
        while (it2.hasNext()) {
            QSPanel.TileRecord next2 = it2.next();
            Folme.useAt(next2.tileView).state().end(ViewProperty.ALPHA, ViewProperty.SCALE_X, ViewProperty.SCALE_Y);
            IStateStyle state2 = Folme.useAt(next2.tileView).state();
            AnimState animState3 = this.mPanelShowAnim;
            AnimState animState4 = this.mPanelHideAnim;
            AnimConfig animConfig2 = new AnimConfig();
            animConfig2.setEase(EaseManager.getStyle(-2, 0.99f, 0.2f));
            state2.fromTo(animState3, animState4, animConfig2);
        }
    }

    public void updateTransHeight(float f, int i, int i2) {
        if (f == 0.0f) {
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                QSPanel.TileRecord next = it.next();
                int intValue = this.mBaseLineIdx + ((Integer) next.tileView.getTag(R.id.tag_tile_layout)).intValue();
                AnimState animState = new AnimState("control_panel_trans");
                animState.add(ViewProperty.TRANSLATION_Y, 0.0f, new long[0]);
                float f2 = (float) intValue;
                float f3 = (float) i2;
                AnimConfig animConfig = new AnimConfig();
                animConfig.setEase(EaseManager.getStyle(-2, ((0.2f * f2) / f3) + 0.7f, ((f2 * 0.1f) / f3) + 0.5f));
                Folme.useAt(next.tileView).state().cancel(ViewProperty.TRANSLATION_Y);
                IStateStyle state = Folme.useAt(next.tileView).state();
                animConfig.setFromSpeed(0.0f);
                state.to(animState, animConfig);
            }
            endExpanding();
            return;
        }
        float f4 = (float) i;
        float max = Math.max(0.0f, Math.min(f, f4));
        Iterator<QSPanel.TileRecord> it2 = this.mRecords.iterator();
        while (it2.hasNext()) {
            QSPanel.TileRecord next2 = it2.next();
            float translationY = Utils.getTranslationY(this.mBaseLineIdx + ((Integer) next2.tileView.getTag(R.id.tag_tile_layout)).intValue(), i2, max, f4);
            AnimState animState2 = new AnimState("control_panel_trans");
            animState2.add(ViewProperty.TRANSLATION_Y, translationY, new long[0]);
            Folme.useAt(next2.tileView).state().cancel(ViewProperty.TRANSLATION_Y);
            Folme.useAt(next2.tileView).state().setTo((Object) animState2);
        }
    }

    public void startMove() {
        this.mLastHeight = (float) getHeight();
    }

    public void setExpandHeightThres(int i) {
        if (i <= 0) {
            this.mExpandHeightThres = 1;
        } else {
            this.mExpandHeightThres = i;
        }
    }

    private void endExpanding() {
        this.mExpanding = false;
    }

    public void setExpandRatio(float f) {
        float f2 = ((float) this.mExpandHeightThres) * f;
        this.mExpanded = false;
        this.mExpanding = true;
        this.mOffset = f2;
        this.mNewHeight = Math.max(Math.min(this.mLastHeight + ((float) ((int) f2)), (float) this.mMaxHeight), (float) this.mMinHeight);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            int intValue = ((Integer) next.tileView.getTag(R.id.tag_tile_layout)).intValue();
            CCQSTileView cCQSTileView = (CCQSTileView) next.tileView;
            QSIconView icon = cCQSTileView.getIcon();
            cCQSTileView.getLabel();
            if (intValue < this.mMinShowRows) {
                cCQSTileView.setLabelAlpha(Math.min(1.0f, f));
            } else {
                cCQSTileView.setVisibility(0);
                double d = (double) f;
                icon.setAlpha(Math.min(1.0f, (float) Math.pow(d, (double) ((((intValue - this.mMinShowRows) * 2) + 1) * 2))));
                cCQSTileView.setLabelAlpha(Math.min(1.0f, (float) Math.pow(d, (double) ((((intValue - this.mMinShowRows) * 2) + 2) * 2))));
            }
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.height = (int) this.mNewHeight;
        setLayoutParams(layoutParams);
    }

    public void requestLayout() {
        if (!isInLayout() && getParent() != null && getParent().isLayoutRequested()) {
            for (ViewParent parent = getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof View) {
                    ((View) parent).mPrivateFlags &= -4097;
                }
            }
        }
        super.requestLayout();
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public boolean isExpanding() {
        return this.mExpanding;
    }

    public boolean canScroll() {
        return getMaxHeight() - getMinHeight() > this.mExpandHeightThres;
    }

    public boolean isCollapsed() {
        return getHeight() == this.mMinHeight;
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public int getMaxHeight() {
        return this.mMaxHeight;
    }

    public int caculateExpandHeight() {
        return getMaxHeight() - getMinHeight();
    }

    public int getShowLines() {
        return this.mShowLines;
    }

    public void setBaseLineIdx(int i) {
        this.mBaseLineIdx = i;
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                it.next().tile.setListening(this, z);
            }
        }
    }

    public void refreshAllTiles() {
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            ((CCQSTileView) it.next().tileView).updateResources();
        }
    }

    public void setTiles(Collection<QSTile> collection) {
        setTiles(collection, this.mExpanded);
        setExpanded(this.mExpanded);
    }

    public void setTiles(Collection<QSTile> collection, boolean z) {
        ArrayList arrayList = new ArrayList(collection);
        Iterator<QSPanel.TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            QSPanel.TileRecord next = it.next();
            next.tile.removeCallback(next.callback);
            next.tile.setListening(this, false);
            removeView(next.tileView);
        }
        this.mRecords.clear();
        Iterator it2 = arrayList.iterator();
        int i = 1;
        while (it2.hasNext()) {
            QSTile qSTile = (QSTile) it2.next();
            int i2 = i + 1;
            qSTile.setIndex(i);
            if (!qSTile.getTileSpec().equals("edit")) {
                addTile(qSTile, z);
            }
            i = i2;
        }
        updateViewsLine();
    }

    /* access modifiers changed from: protected */
    public void drawTile(QSPanel.TileRecord tileRecord, QSTile.State state) {
        tileRecord.tileView.onStateChanged(state);
    }

    /* access modifiers changed from: protected */
    public QSTileView createTileView(QSTile qSTile, boolean z) {
        return this.mHost.createControlCenterTileView(qSTile, z);
    }

    /* access modifiers changed from: protected */
    public QSPanel.TileRecord addTile(QSTile qSTile, boolean z) {
        final QSPanel.TileRecord tileRecord = new QSPanel.TileRecord();
        tileRecord.tile = qSTile;
        tileRecord.tileView = createTileView(qSTile, !this.mExpanded && this.mRecords.size() <= this.mMinShowRows * this.mColumns);
        AnonymousClass2 r4 = new QSTile.Callback() {
            public void onScanStateChanged(boolean z) {
            }

            public void onShowDetail(boolean z) {
            }

            public void onShowEdit(boolean z) {
            }

            public void onToggleStateChanged(boolean z) {
            }

            public void onStateChanged(QSTile.State state) {
                QSControlCenterTileLayout.this.drawTile(tileRecord, state);
            }

            public void onAnnouncementRequested(CharSequence charSequence) {
                if (charSequence != null) {
                    QSControlCenterTileLayout.this.mHandler.obtainMessage(3, charSequence).sendToTarget();
                }
            }
        };
        tileRecord.tile.addCallback(r4);
        tileRecord.callback = r4;
        tileRecord.tileView.init(tileRecord.tile);
        tileRecord.tile.refreshState();
        addTile(tileRecord);
        return tileRecord;
    }

    /* access modifiers changed from: protected */
    public void handleShowDetail(QSPanel.Record record, boolean z) {
        int i;
        if (record instanceof QSPanel.TileRecord) {
            handleShowDetailTile((QSPanel.TileRecord) record, z);
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
    public void handleShowEdit(QSPanel.Record record, boolean z) {
        int i;
        if (record instanceof QSPanel.TileRecord) {
            handleShowEditTile((QSPanel.TileRecord) record);
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

    private void handleShowDetailImpl(QSPanel.Record record, boolean z, int i, int i2) {
        DetailAdapter detailAdapter = null;
        setDetailRecord(z ? record : null);
        if (z) {
            detailAdapter = record.detailAdapter;
        }
        fireShowingDetail(detailAdapter, i, i2);
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

    public void onExpandChange(boolean z) {
        setListening(z);
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = false;
            if (i == 1) {
                QSControlCenterTileLayout qSControlCenterTileLayout = QSControlCenterTileLayout.this;
                QSPanel.Record record = (QSPanel.Record) message.obj;
                if (message.arg1 != 0) {
                    z = true;
                }
                qSControlCenterTileLayout.handleShowDetail(record, z);
            } else if (i == 4) {
                QSControlCenterTileLayout qSControlCenterTileLayout2 = QSControlCenterTileLayout.this;
                QSPanel.Record record2 = (QSPanel.Record) message.obj;
                if (message.arg1 != 0) {
                    z = true;
                }
                qSControlCenterTileLayout2.handleShowEdit(record2, z);
            } else if (i == 3) {
                QSControlCenterTileLayout.this.announceForAccessibility((CharSequence) message.obj);
            }
        }
    }
}
