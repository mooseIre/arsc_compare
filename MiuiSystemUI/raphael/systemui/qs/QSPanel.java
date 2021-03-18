package com.android.systemui.qs;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.widget.RemeasuringLinearLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.MiuiQSDetail;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.customize.MiuiQSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.settings.ToggleSliderView;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.MiuiBrightnessController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.UniqueObjectHostView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class QSPanel extends LinearLayout implements TunerService.Tunable, QSHost.Callback, BrightnessMirrorController.BrightnessMirrorListener, Dumpable {
    private MiuiBrightnessController mBrightnessController;
    private BrightnessMirrorController mBrightnessMirrorController;
    protected View mBrightnessView;
    private String mCachedSpecs = "";
    private MiuiQSDetail.Callback mCallback;
    private int mContentMarginEnd;
    private int mContentMarginStart;
    protected final Context mContext;
    private MiuiQSCustomizer mCustomizePanel;
    private Record mDetailRecord;
    protected View mDivider;
    private final DumpManager mDumpManager;
    private int mEditTopOffset;
    protected boolean mExpanded;
    protected View mFooter;
    private int mFooterMarginStartHorizontal;
    private MiuiPageIndicator mFooterPageIndicator;
    private boolean mGridContentVisible = true;
    private final H mHandler = new H();
    private ViewGroup mHeaderContainer;
    private LinearLayout mHorizontalContentContainer;
    private LinearLayout mHorizontalLinearLayout;
    private QSTileLayout mHorizontalTileLayout;
    protected QSTileHost mHost;
    private int mLastOrientation = -1;
    protected boolean mListening;
    protected final MediaHost mMediaHost;
    private int mMediaTotalBottomMargin = getResources().getDimensionPixelSize(C0012R$dimen.quick_settings_bottom_margin_media);
    private Consumer<Boolean> mMediaVisibilityChangedListener;
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private final int mMovableContentStartIndex;
    private final QSLogger mQSLogger;
    private QSTileRevealController mQsTileRevealController;
    protected final ArrayList<TileRecord> mRecords = new ArrayList<>();
    protected QSTileLayout mRegularTileLayout;
    protected QSSecurityFooter mSecurityFooter;
    protected QSTileLayout mTileLayout;
    protected final UiEventLogger mUiEventLogger;
    private boolean mUsingHorizontalLayout;
    protected boolean mUsingMediaPlayer = false;
    private int mVisualMarginEnd;
    private int mVisualMarginStart;
    private int mVisualTilePadding;

    public interface QSTileLayout {
        void addTile(TileRecord tileRecord);

        int getNumVisibleTiles();

        int getOffsetTop(TileRecord tileRecord);

        void removeTile(TileRecord tileRecord);

        default void restoreInstanceState(Bundle bundle) {
        }

        default void saveInstanceState(Bundle bundle) {
        }

        default void setExpansion(float f) {
        }

        void setListening(boolean z);

        default boolean setMaxColumns(int i) {
            return false;
        }

        default boolean setMinRows(int i) {
            return false;
        }

        boolean updateResources();
    }

    public static class Record {
        public DetailAdapter detailAdapter;
        public int x;
        public int y;
    }

    public static final class TileRecord extends Record {
        public QSTile.Callback callback;
        public boolean scanState;
        public QSTile tile;
        public QSTileView tileView;
    }

    /* access modifiers changed from: protected */
    public boolean displayMediaMarginsOnMedia() {
        return true;
    }

    /* access modifiers changed from: protected */
    public String getDumpableTag() {
        return "QSPanel";
    }

    /* access modifiers changed from: protected */
    public int getTileCallbackType() {
        return 1;
    }

    /* access modifiers changed from: protected */
    public boolean needsDynamicRowsAndColumns() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void updatePadding() {
    }

    public QSPanel(Context context, AttributeSet attributeSet, DumpManager dumpManager, BroadcastDispatcher broadcastDispatcher, QSLogger qSLogger, MediaHost mediaHost, UiEventLogger uiEventLogger) {
        super(context, attributeSet);
        this.mMediaHost = mediaHost;
        mediaHost.addVisibilityChangeListener(new Function1() {
            /* class com.android.systemui.qs.$$Lambda$QSPanel$eQ8pVxxhUsNJKcJOLQN4uzlXkuA */

            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return QSPanel.this.lambda$new$0$QSPanel((Boolean) obj);
            }
        });
        this.mContext = context;
        this.mQSLogger = qSLogger;
        this.mDumpManager = dumpManager;
        this.mUiEventLogger = uiEventLogger;
        setOrientation(1);
        this.mMovableContentStartIndex = getChildCount();
        this.mRegularTileLayout = createRegularTileLayout();
        if (this.mUsingMediaPlayer) {
            RemeasuringLinearLayout remeasuringLinearLayout = new RemeasuringLinearLayout(this.mContext);
            this.mHorizontalLinearLayout = remeasuringLinearLayout;
            remeasuringLinearLayout.setOrientation(0);
            this.mHorizontalLinearLayout.setClipChildren(false);
            this.mHorizontalLinearLayout.setClipToPadding(false);
            RemeasuringLinearLayout remeasuringLinearLayout2 = new RemeasuringLinearLayout(this.mContext);
            this.mHorizontalContentContainer = remeasuringLinearLayout2;
            remeasuringLinearLayout2.setOrientation(1);
            this.mHorizontalContentContainer.setClipChildren(false);
            this.mHorizontalContentContainer.setClipToPadding(false);
            this.mHorizontalTileLayout = createHorizontalTileLayout();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            layoutParams.setMarginStart(0);
            layoutParams.setMarginEnd((int) this.mContext.getResources().getDimension(C0012R$dimen.qqs_media_spacing));
            layoutParams.gravity = 16;
            this.mHorizontalLinearLayout.addView(this.mHorizontalContentContainer, layoutParams);
            addView(this.mHorizontalLinearLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            initMediaHostState();
        }
        addSecurityFooter();
        QSTileLayout qSTileLayout = this.mRegularTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            this.mQsTileRevealController = new QSTileRevealController(this.mContext, this, (PagedTileLayout) qSTileLayout);
        }
        this.mQSLogger.logAllTilesChangeListening(this.mListening, getDumpableTag(), this.mCachedSpecs);
        updateResources();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ Unit lambda$new$0$QSPanel(Boolean bool) {
        onMediaVisibilityChanged(bool);
        return null;
    }

    /* access modifiers changed from: protected */
    public void onMediaVisibilityChanged(Boolean bool) {
        switchTileLayout();
        Consumer<Boolean> consumer = this.mMediaVisibilityChangedListener;
        if (consumer != null) {
            consumer.accept(bool);
        }
    }

    /* access modifiers changed from: protected */
    public void addSecurityFooter() {
        this.mSecurityFooter = new QSSecurityFooter(this, this.mContext);
    }

    /* access modifiers changed from: protected */
    public QSTileLayout createRegularTileLayout() {
        if (this.mRegularTileLayout == null) {
            this.mRegularTileLayout = (QSTileLayout) LayoutInflater.from(this.mContext).inflate(C0017R$layout.qs_paged_tile_layout, (ViewGroup) this, false);
        }
        return this.mRegularTileLayout;
    }

    /* access modifiers changed from: protected */
    public QSTileLayout createHorizontalTileLayout() {
        return createRegularTileLayout();
    }

    /* access modifiers changed from: protected */
    public void initMediaHostState() {
        this.mMediaHost.setExpansion(1.0f);
        this.mMediaHost.setShowsOnlyActiveMedia(false);
        updateMediaDisappearParameters();
        this.mMediaHost.init(0);
    }

    private void updateMediaDisappearParameters() {
        if (this.mUsingMediaPlayer) {
            DisappearParameters disappearParameters = this.mMediaHost.getDisappearParameters();
            if (this.mUsingHorizontalLayout) {
                disappearParameters.getDisappearSize().set(0.0f, 0.4f);
                disappearParameters.getGonePivot().set(1.0f, 1.0f);
                disappearParameters.getContentTranslationFraction().set(0.25f, 1.0f);
                disappearParameters.setDisappearEnd(0.6f);
            } else {
                disappearParameters.getDisappearSize().set(1.0f, 0.0f);
                disappearParameters.getGonePivot().set(0.0f, 1.0f);
                disappearParameters.getContentTranslationFraction().set(0.0f, 1.05f);
                disappearParameters.setDisappearEnd(0.95f);
            }
            disappearParameters.setFadeStartPosition(0.95f);
            disappearParameters.setDisappearStart(0.0f);
            this.mMediaHost.setDisappearParameters(disappearParameters);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            MiuiPageIndicator miuiPageIndicator = this.mFooterPageIndicator;
            if (miuiPageIndicator != null) {
                miuiPageIndicator.setNumPages(((PagedTileLayout) qSTileLayout).getNumPages());
            }
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(10000, 1073741824);
            ((PagedTileLayout) this.mTileLayout).setExcessHeight(10000 - View.MeasureSpec.getSize(i2));
            i2 = makeMeasureSpec;
        }
        super.onMeasure(i, i2);
        int paddingBottom = getPaddingBottom() + getPaddingTop();
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                paddingBottom = paddingBottom + childAt.getMeasuredHeight() + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), paddingBottom);
    }

    public QSTileRevealController getQsTileRevealController() {
        return this.mQsTileRevealController;
    }

    public boolean isShowingCustomize() {
        MiuiQSCustomizer miuiQSCustomizer = this.mCustomizePanel;
        return miuiQSCustomizer != null && miuiQSCustomizer.isCustomizing();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "qs_show_brightness");
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mDumpManager.registerDumpable(getDumpableTag(), this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.setListening(false);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.removeCallbacksByType(getTileCallbackType());
        }
        this.mRecords.clear();
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mDumpManager.unregisterDumpable(getDumpableTag());
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.qs.QSHost.Callback
    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        View view;
        if ("qs_show_brightness".equals(str) && (view = this.mBrightnessView) != null) {
            updateViewVisibilityForTuningValue(view, str2);
        }
    }

    private void updateViewVisibilityForTuningValue(View view, String str) {
        view.setVisibility(TunerService.parseIntegerSwitch(str, true) ? 0 : 8);
    }

    public void openDetails(String str) {
        QSTile tile = getTile(str);
        if (tile != null) {
            showDetailAdapter(true, tile.getDetailAdapter(), new int[]{getWidth() / 2, 0});
        }
    }

    private QSTile getTile(String str) {
        for (int i = 0; i < this.mRecords.size(); i++) {
            if (str.equals(this.mRecords.get(i).tile.getTileSpec())) {
                return this.mRecords.get(i).tile;
            }
        }
        return this.mHost.createTile(str);
    }

    @Override // com.android.systemui.statusbar.policy.BrightnessMirrorController.BrightnessMirrorListener
    public void onBrightnessMirrorReinflated(View view) {
        updateBrightnessMirror();
    }

    /* access modifiers changed from: package-private */
    public View getBrightnessView() {
        return this.mBrightnessView;
    }

    public void setCallback(MiuiQSDetail.Callback callback) {
        this.mCallback = callback;
    }

    public void setHost(QSTileHost qSTileHost, MiuiQSCustomizer miuiQSCustomizer) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        setTiles(this.mHost.getTiles());
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setHostEnvironment(qSTileHost);
        }
        this.mCustomizePanel = miuiQSCustomizer;
        if (miuiQSCustomizer != null) {
            miuiQSCustomizer.setHost(this.mHost);
        }
    }

    public void setFooterPageIndicator(MiuiPageIndicator miuiPageIndicator) {
        if (this.mRegularTileLayout instanceof PagedTileLayout) {
            this.mFooterPageIndicator = miuiPageIndicator;
            updatePageIndicator();
        }
    }

    private void updatePageIndicator() {
        MiuiPageIndicator miuiPageIndicator;
        QSTileLayout qSTileLayout = this.mRegularTileLayout;
        if ((qSTileLayout instanceof PagedTileLayout) && (miuiPageIndicator = this.mFooterPageIndicator) != null) {
            ((PagedTileLayout) qSTileLayout).setPageIndicator(miuiPageIndicator);
        }
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public void updateResources() {
        this.mEditTopOffset = getResources().getDimensionPixelSize(C0012R$dimen.qs_detail_margin_top);
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.qs_quick_tile_size);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0012R$dimen.qs_tile_background_size);
        this.mFooterMarginStartHorizontal = getResources().getDimensionPixelSize(C0012R$dimen.qs_footer_horizontal_margin);
        this.mVisualTilePadding = (int) (((float) (dimensionPixelSize - dimensionPixelSize2)) / 2.0f);
        updatePadding();
        updatePageIndicator();
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.clearState();
        }
        if (this.mListening) {
            refreshAllTiles();
        }
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.updateResources();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.onConfigurationChanged();
        }
        updateResources();
        updateBrightnessMirror();
        int i = configuration.orientation;
        if (i != this.mLastOrientation) {
            this.mLastOrientation = i;
            switchTileLayout();
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFooter = findViewById(C0015R$id.qs_footer);
        this.mDivider = findViewById(C0015R$id.divider);
        switchTileLayout(true);
    }

    /* access modifiers changed from: package-private */
    public boolean switchTileLayout() {
        return switchTileLayout(false);
    }

    private boolean switchTileLayout(boolean z) {
        QSTileLayout qSTileLayout;
        boolean shouldUseHorizontalLayout = shouldUseHorizontalLayout();
        if (this.mDivider != null) {
            if (shouldUseHorizontalLayout || !this.mUsingMediaPlayer || !this.mMediaHost.getVisible()) {
                this.mDivider.setVisibility(8);
            } else {
                this.mDivider.setVisibility(0);
            }
        }
        if (shouldUseHorizontalLayout == this.mUsingHorizontalLayout && !z) {
            return false;
        }
        this.mUsingHorizontalLayout = shouldUseHorizontalLayout;
        View view = shouldUseHorizontalLayout ? this.mHorizontalLinearLayout : (View) this.mRegularTileLayout;
        View view2 = shouldUseHorizontalLayout ? (View) this.mRegularTileLayout : this.mHorizontalLinearLayout;
        LinearLayout linearLayout = shouldUseHorizontalLayout ? this.mHorizontalContentContainer : this;
        QSTileLayout qSTileLayout2 = shouldUseHorizontalLayout ? this.mHorizontalTileLayout : this.mRegularTileLayout;
        if (!(view2 == null || ((qSTileLayout = this.mRegularTileLayout) == this.mHorizontalTileLayout && view2 == qSTileLayout))) {
            view2.setVisibility(8);
        }
        view.setVisibility(0);
        switchAllContentToParent(linearLayout, qSTileLayout2);
        reAttachMediaHost();
        QSTileLayout qSTileLayout3 = this.mTileLayout;
        if (qSTileLayout3 != null) {
            qSTileLayout3.setListening(false);
            Iterator<TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                TileRecord next = it.next();
                this.mTileLayout.removeTile(next);
                next.tile.removeCallback(next.callback);
            }
        }
        this.mTileLayout = qSTileLayout2;
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
        qSTileLayout2.setListening(this.mListening);
        if (needsDynamicRowsAndColumns()) {
            qSTileLayout2.setMinRows(shouldUseHorizontalLayout ? 2 : 1);
            qSTileLayout2.setMaxColumns(shouldUseHorizontalLayout ? 3 : 100);
        }
        updateTileLayoutMargins();
        updateFooterMargin();
        updateDividerMargin();
        updateMediaDisappearParameters();
        updateMediaHostContentMargins();
        updateHorizontalLinearLayoutMargins();
        updatePadding();
        return true;
    }

    private void updateHorizontalLinearLayoutMargins() {
        if (this.mHorizontalLinearLayout != null && !displayMediaMarginsOnMedia()) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mHorizontalLinearLayout.getLayoutParams();
            layoutParams.bottomMargin = this.mMediaTotalBottomMargin - getPaddingBottom();
            this.mHorizontalLinearLayout.setLayoutParams(layoutParams);
        }
    }

    private void switchAllContentToParent(ViewGroup viewGroup, QSTileLayout qSTileLayout) {
        ViewGroup viewGroup2;
        int i = viewGroup == this ? this.mMovableContentStartIndex : 0;
        switchToParent((View) qSTileLayout, viewGroup, i);
        int i2 = i + 1;
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            View view = qSSecurityFooter.getView();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (!this.mUsingHorizontalLayout || (viewGroup2 = this.mHeaderContainer) == null) {
                layoutParams.width = -2;
                layoutParams.weight = 0.0f;
                switchToParent(view, viewGroup, i2);
                i2++;
            } else {
                layoutParams.width = 0;
                layoutParams.weight = 1.6f;
                switchToParent(view, viewGroup2, 1);
            }
            view.setLayoutParams(layoutParams);
        }
        View view2 = this.mFooter;
        if (view2 != null) {
            switchToParent(view2, viewGroup, i2);
        }
    }

    private void switchToParent(View view, ViewGroup viewGroup, int i) {
        ViewGroup viewGroup2 = (ViewGroup) view.getParent();
        if (viewGroup2 != viewGroup || viewGroup2.indexOfChild(view) != i) {
            if (viewGroup2 != null) {
                viewGroup2.removeView(view);
            }
            viewGroup.addView(view, i);
        }
    }

    private boolean shouldUseHorizontalLayout() {
        return this.mUsingMediaPlayer && this.mMediaHost.getVisible() && getResources().getConfiguration().orientation == 2;
    }

    /* access modifiers changed from: protected */
    public void reAttachMediaHost() {
        if (this.mUsingMediaPlayer) {
            boolean shouldUseHorizontalLayout = shouldUseHorizontalLayout();
            UniqueObjectHostView hostView = this.mMediaHost.getHostView();
            LinearLayout linearLayout = shouldUseHorizontalLayout ? this.mHorizontalLinearLayout : this;
            ViewGroup viewGroup = (ViewGroup) hostView.getParent();
            if (viewGroup != linearLayout) {
                if (viewGroup != null) {
                    viewGroup.removeView(hostView);
                }
                linearLayout.addView(hostView);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) hostView.getLayoutParams();
                layoutParams.height = -2;
                int i = 0;
                layoutParams.width = shouldUseHorizontalLayout ? 0 : -1;
                layoutParams.weight = shouldUseHorizontalLayout ? 1.2f : 0.0f;
                if (!shouldUseHorizontalLayout || displayMediaMarginsOnMedia()) {
                    i = this.mMediaTotalBottomMargin - getPaddingBottom();
                }
                layoutParams.bottomMargin = i;
            }
        }
    }

    public void updateBrightnessMirror() {
        if (this.mBrightnessMirrorController != null) {
            ToggleSliderView toggleSliderView = (ToggleSliderView) findViewById(C0015R$id.brightness_slider);
            toggleSliderView.setMirror((ToggleSliderView) this.mBrightnessMirrorController.getMirror().findViewById(C0015R$id.brightness_slider));
            toggleSliderView.setMirrorController(this.mBrightnessMirrorController);
        }
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded != z) {
            this.mQSLogger.logPanelExpanded(z, getDumpableTag());
            this.mExpanded = z;
            if (!z) {
                QSTileLayout qSTileLayout = this.mTileLayout;
                if (qSTileLayout instanceof PagedTileLayout) {
                    ((PagedTileLayout) qSTileLayout).setCurrentItem(0, false);
                }
            }
            this.mMetricsLogger.visibility(111, this.mExpanded);
            if (!this.mExpanded) {
                this.mUiEventLogger.log(closePanelEvent());
                closeDetail();
                return;
            }
            this.mUiEventLogger.log(openPanelEvent());
            logTiles();
        }
    }

    public void setPageListener(PagedTileLayout.PageListener pageListener) {
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            ((PagedTileLayout) qSTileLayout).setPageListener(pageListener);
        }
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            if (this.mTileLayout != null) {
                this.mQSLogger.logAllTilesChangeListening(z, getDumpableTag(), this.mCachedSpecs);
                this.mTileLayout.setListening(z);
            }
            if (this.mListening) {
                refreshAllTiles();
            }
        }
    }

    private String getTilesSpecs() {
        return (String) this.mRecords.stream().map($$Lambda$QSPanel$EbHBtJlVwGzmqefWXJDEYuyGlcQ.INSTANCE).collect(Collectors.joining(","));
    }

    public void setListening(boolean z, boolean z2) {
        setListening(z && z2);
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setListening(z);
        }
        setBrightnessListening(z);
    }

    public void setBrightnessListening(boolean z) {
        MiuiBrightnessController miuiBrightnessController = this.mBrightnessController;
        if (miuiBrightnessController != null) {
            if (z) {
                miuiBrightnessController.registerCallbacks();
            } else {
                miuiBrightnessController.unregisterCallbacks();
            }
        }
    }

    public void refreshAllTiles() {
        MiuiBrightnessController miuiBrightnessController = this.mBrightnessController;
        if (miuiBrightnessController != null) {
            miuiBrightnessController.checkRestrictionAndSetEnabled();
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.refreshState();
        }
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.refreshState();
        }
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

    public void setTiles(Collection<QSTile> collection) {
        if (!((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter()) {
            setTiles(collection, false);
        }
    }

    public void setTiles(Collection<QSTile> collection, boolean z) {
        if (!z) {
            this.mQsTileRevealController.updateRevealedTiles(collection);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            this.mTileLayout.removeTile(next);
            next.tile.removeCallback(next.callback);
        }
        this.mRecords.clear();
        this.mCachedSpecs = "";
        for (QSTile qSTile : collection) {
            addTile(qSTile, z);
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
    public QSEvent openPanelEvent() {
        return QSEvent.QS_PANEL_EXPANDED;
    }

    /* access modifiers changed from: protected */
    public QSEvent closePanelEvent() {
        return QSEvent.QS_PANEL_COLLAPSED;
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
            /* class com.android.systemui.qs.QSPanel.AnonymousClass1 */

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public int getCallbackType() {
                return 1;
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onStateChanged(QSTile.State state) {
                QSPanel.this.drawTile(tileRecord, state);
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onShowDetail(boolean z) {
                if (QSPanel.this.shouldShowDetail()) {
                    QSPanel.this.showDetail(z, tileRecord);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onShowEdit(boolean z) {
                QSPanel.this.showEdit(z, tileRecord);
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onToggleStateChanged(boolean z) {
                if (QSPanel.this.mDetailRecord == tileRecord) {
                    QSPanel.this.fireToggleStateChanged(z);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onScanStateChanged(boolean z) {
                tileRecord.scanState = z;
                Record record = QSPanel.this.mDetailRecord;
                TileRecord tileRecord = tileRecord;
                if (record == tileRecord) {
                    QSPanel.this.fireScanStateChanged(tileRecord.scanState);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
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
        this.mCachedSpecs = getTilesSpecs();
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.addTile(tileRecord);
        }
        return tileRecord;
    }

    public void showEdit(final View view) {
        view.post(new Runnable() {
            /* class com.android.systemui.qs.QSPanel.AnonymousClass2 */

            public void run() {
                if (QSPanel.this.mCustomizePanel != null && !QSPanel.this.mCustomizePanel.isCustomizing()) {
                    int[] locationOnScreen = view.getLocationOnScreen();
                    QSPanel.this.mCustomizePanel.show(locationOnScreen[0] + (view.getWidth() / 2), locationOnScreen[1] + (view.getHeight() / 2));
                }
            }
        });
    }

    public void closeDetail() {
        MiuiQSCustomizer miuiQSCustomizer = this.mCustomizePanel;
        if (miuiQSCustomizer == null || !miuiQSCustomizer.isShown()) {
            showDetail(false, this.mDetailRecord);
        } else {
            this.mCustomizePanel.hide();
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

    private void handleShowDetailTile(TileRecord tileRecord, boolean z) {
        if ((this.mDetailRecord != null) != z || this.mDetailRecord != tileRecord) {
            if (z) {
                DetailAdapter detailAdapter = tileRecord.tile.getDetailAdapter();
                tileRecord.detailAdapter = detailAdapter;
                if (detailAdapter == null) {
                    return;
                }
            }
            tileRecord.tile.setDetailListening(z);
            handleShowDetailImpl(tileRecord, z, tileRecord.tileView.getLeft() + (tileRecord.tileView.getWidth() / 2), tileRecord.tileView.getDetailY() + this.mTileLayout.getOffsetTop(tileRecord) + getTop());
        }
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
    public void showEdit(boolean z, Record record) {
        this.mHandler.obtainMessage(1001, z ? 1 : 0, 0, record).sendToTarget();
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
        this.mCustomizePanel.show(i2, i);
    }

    private void handleShowEditTile(TileRecord tileRecord) {
        this.mCustomizePanel.show(tileRecord.tileView.getLeft() + (tileRecord.tileView.getWidth() / 2), tileRecord.tileView.getDetailY() + this.mTileLayout.getOffsetTop(tileRecord) + getTop() + this.mEditTopOffset);
    }

    /* access modifiers changed from: protected */
    public void setDetailRecord(Record record) {
        if (record != this.mDetailRecord) {
            this.mDetailRecord = record;
            fireScanStateChanged((record instanceof TileRecord) && ((TileRecord) record).scanState);
        }
    }

    /* access modifiers changed from: package-private */
    public void setGridContentVisibility(boolean z) {
        int i = z ? 0 : 4;
        setVisibility(i);
        if (this.mGridContentVisible != z) {
            this.mMetricsLogger.visibility(111, i);
        }
        this.mGridContentVisible = z;
    }

    private void logTiles() {
        for (int i = 0; i < this.mRecords.size(); i++) {
            QSTile qSTile = this.mRecords.get(i).tile;
            this.mMetricsLogger.write(qSTile.populate(new LogMaker(qSTile.getMetricsCategory()).setType(1)));
        }
    }

    private void fireShowingDetail(DetailAdapter detailAdapter, int i, int i2) {
        MiuiQSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onShowingDetail(detailAdapter, i, i2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireToggleStateChanged(boolean z) {
        MiuiQSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onToggleStateChanged(z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireScanStateChanged(boolean z) {
        MiuiQSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onScanStateChanged(z);
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

    public QSSecurityFooter getSecurityFooter() {
        return this.mSecurityFooter;
    }

    public View getDivider() {
        return this.mDivider;
    }

    public void showDeviceMonitoringDialog() {
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.showDeviceMonitoringDialog();
        }
    }

    public void setContentMargins(int i, int i2) {
        this.mContentMarginStart = i;
        this.mContentMarginEnd = i2;
        int i3 = this.mVisualTilePadding;
        updateTileLayoutMargins(i - i3, i2 - i3);
        updateMediaHostContentMargins();
        updateFooterMargin();
        updateDividerMargin();
    }

    private void updateFooterMargin() {
        int i;
        int i2;
        if (this.mFooter != null) {
            if (this.mUsingHorizontalLayout) {
                i2 = this.mFooterMarginStartHorizontal;
                i = i2 - this.mVisualMarginEnd;
            } else {
                i2 = 0;
                i = 0;
            }
            updateMargins(this.mFooter, i2, 0);
            MiuiPageIndicator miuiPageIndicator = this.mFooterPageIndicator;
            if (miuiPageIndicator != null) {
                updateMargins(miuiPageIndicator, 0, i);
            }
        }
    }

    private void updateTileLayoutMargins(int i, int i2) {
        this.mVisualMarginStart = i;
        this.mVisualMarginEnd = i2;
        updateTileLayoutMargins();
    }

    private void updateTileLayoutMargins() {
        int i = this.mVisualMarginEnd;
        if (this.mUsingHorizontalLayout) {
            i = 0;
        }
        updateMargins((View) this.mTileLayout, this.mVisualMarginStart, i);
    }

    private void updateDividerMargin() {
        View view = this.mDivider;
        if (view != null) {
            updateMargins(view, this.mContentMarginStart, this.mContentMarginEnd);
        }
    }

    /* access modifiers changed from: protected */
    public void updateMediaHostContentMargins() {
        if (this.mUsingMediaPlayer) {
            int i = this.mContentMarginStart;
            if (this.mUsingHorizontalLayout) {
                i = 0;
            }
            updateMargins(this.mMediaHost.getHostView(), i, this.mContentMarginEnd);
        }
    }

    /* access modifiers changed from: protected */
    public void updateMargins(View view, int i, int i2) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMarginStart(i);
        layoutParams.setMarginEnd(i2);
        view.setLayoutParams(layoutParams);
    }

    public void setHeaderContainer(ViewGroup viewGroup) {
        this.mHeaderContainer = viewGroup;
    }

    public void setMediaVisibilityChangedListener(Consumer<Boolean> consumer) {
        this.mMediaVisibilityChangedListener = consumer;
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
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
            } else if (i == 1001) {
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

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(getClass().getSimpleName() + ":");
        printWriter.println("  Tile records:");
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            if (next.tile instanceof Dumpable) {
                printWriter.print("    ");
                ((Dumpable) next.tile).dump(fileDescriptor, printWriter, strArr);
                printWriter.print("    ");
                printWriter.println(next.tileView.toString());
            }
        }
    }
}
