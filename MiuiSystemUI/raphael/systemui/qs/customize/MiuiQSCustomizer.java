package com.android.systemui.qs.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.controlcenter.phone.widget.AnimatorListenerWrapper;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSAnimation;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.QSEditEvent;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.ArrayList;
import java.util.List;

public class MiuiQSCustomizer extends LinearLayout {
    private boolean isShown;
    private Animator.AnimatorListener mAnimInListener;
    private Animator.AnimatorListener mAnimOutListener;
    private final QSDetailClipper mClipper;
    private final Animator.AnimatorListener mCollapseAnimationListener;
    private boolean mCustomizing;
    protected TextView mDoneButton;
    private final Animator.AnimatorListener mExpandAnimationListener = new AnimatorListenerAdapter() {
        /* class com.android.systemui.qs.customize.MiuiQSCustomizer.AnonymousClass3 */

        public void onAnimationEnd(Animator animator) {
            if (MiuiQSCustomizer.this.isShown) {
                MiuiQSCustomizer.this.setCustomizing(true);
            }
            MiuiQSCustomizer.this.mOpening = false;
            MiuiQSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
        }

        public void onAnimationCancel(Animator animator) {
            MiuiQSCustomizer.this.mOpening = false;
            MiuiQSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
        }
    };
    protected RelativeLayout mHeader;
    private QSTileHost mHost;
    private final KeyguardStateController.Callback mKeyguardCallback = new KeyguardStateController.Callback() {
        /* class com.android.systemui.qs.customize.MiuiQSCustomizer.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            if (MiuiQSCustomizer.this.isAttachedToWindow() && MiuiQSCustomizer.this.mKeyguardStateController.isShowing() && !MiuiQSCustomizer.this.mOpening) {
                MiuiQSCustomizer.this.hide();
            }
        }
    };
    private KeyguardStateController mKeyguardStateController;
    private final LightBarController mLightBarController;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private boolean mOpening;
    protected QSPanelCallback mQsPanelCallback;
    private RecyclerView mRecyclerView;
    protected TextView mResetButton;
    private final ScreenLifecycle mScreenLifecycle;
    private int mSpanCount;
    protected TextView mSubTitle;
    private MiuiTileAdapter mTileAdapter;
    private final TileQueryHelper mTileQueryHelper;
    protected TextView mTitle;
    private UiEventLogger mUiEventLogger = new UiEventLoggerImpl();
    private int mX;
    private int mY;

    public interface QSPanelCallback {
        void hide(int i, int i2, boolean z);
    }

    public void setQs(QS qs) {
    }

    public MiuiQSCustomizer(Context context, AttributeSet attributeSet, LightBarController lightBarController, KeyguardStateController keyguardStateController, ScreenLifecycle screenLifecycle, TileQueryHelper tileQueryHelper, UiEventLogger uiEventLogger) {
        super(new ContextThemeWrapper(context, C0022R$style.edit_theme), attributeSet);
        AnonymousClass4 r3 = new AnimatorListenerAdapter() {
            /* class com.android.systemui.qs.customize.MiuiQSCustomizer.AnonymousClass4 */

            public void onAnimationEnd(Animator animator) {
                if (!MiuiQSCustomizer.this.isShown) {
                    MiuiQSCustomizer.this.setVisibility(8);
                }
                MiuiQSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
                MiuiQSCustomizer.this.mRecyclerView.setAdapter(MiuiQSCustomizer.this.mTileAdapter);
            }

            public void onAnimationCancel(Animator animator) {
                if (!MiuiQSCustomizer.this.isShown) {
                    MiuiQSCustomizer.this.setVisibility(8);
                }
                MiuiQSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
        };
        this.mCollapseAnimationListener = r3;
        this.mAnimInListener = this.mExpandAnimationListener;
        this.mAnimOutListener = r3;
        this.mQsPanelCallback = new QSPanelCallback() {
            /* class com.android.systemui.qs.customize.MiuiQSCustomizer.AnonymousClass5 */

            @Override // com.android.systemui.qs.customize.MiuiQSCustomizer.QSPanelCallback
            public void hide(int i, int i2, boolean z) {
                MiuiQSCustomizer.this.hide(z);
            }
        };
        this.mSpanCount = Math.max(1, ((LinearLayout) this).mContext.getResources().getInteger(C0016R$integer.quick_settings_num_columns));
        LayoutInflater.from(getContext()).inflate(C0017R$layout.qs_customize_panel_content, this);
        this.mClipper = new QSDetailClipper(findViewById(C0015R$id.customize_container));
        this.mRecyclerView = (RecyclerView) findViewById(16908298);
        MiuiTileAdapter miuiTileAdapter = new MiuiTileAdapter(getContext(), uiEventLogger, this.mRecyclerView);
        this.mTileAdapter = miuiTileAdapter;
        this.mTileQueryHelper = tileQueryHelper;
        tileQueryHelper.setListener(miuiTileAdapter);
        this.mTileQueryHelper.setCustomizer(this);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        this.mHeader = (RelativeLayout) findViewById(C0015R$id.header);
        updateLayout();
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setMoveDuration(150);
        this.mRecyclerView.setItemAnimator(defaultItemAnimator);
        this.mLightBarController = lightBarController;
        this.mKeyguardStateController = keyguardStateController;
        this.mScreenLifecycle = screenLifecycle;
        initMiuiAddedView();
    }

    private void updateResources() {
        this.mTitle.setText(C0021R$string.qs_customize_title);
        this.mSubTitle.setText(C0021R$string.drag_to_add_tiles);
        this.mResetButton.setText(17041223);
        this.mDoneButton.setText(C0021R$string.quick_settings_done);
        Resources resources = ((LinearLayout) this).mContext.getResources();
        int max = Math.max(1, resources.getInteger(C0016R$integer.quick_settings_num_columns));
        if (this.mSpanCount != max) {
            if (resources.getConfiguration().orientation == 1) {
                this.mHeader.setVisibility(0);
            } else {
                this.mHeader.setVisibility(4);
            }
            this.mSpanCount = max;
            this.mTileAdapter.setSpanCount(max);
            updateLayout();
        }
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mTileAdapter.setHost(qSTileHost);
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQuickSettingsContainer) {
        this.mNotifQsContainer = notificationsQuickSettingsContainer;
    }

    public void show(int i, int i2) {
        if (!this.isShown) {
            this.mX = i;
            this.mY = i2;
            announceForAccessibility(((LinearLayout) this).mContext.getString(C0021R$string.accessibility_desc_quick_settings_edit));
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_OPEN);
            this.isShown = true;
            this.mOpening = true;
            setTileSpecs();
            setVisibility(0);
            this.mClipper.animateCircularClip(this.mX, this.mY, true, this.mAnimInListener);
            queryTiles();
            this.mNotifQsContainer.setCustomizerAnimating(true);
            this.mNotifQsContainer.setCustomizerShowing(true);
            this.mKeyguardStateController.addCallback(this.mKeyguardCallback);
            this.mLightBarController.setQsCustomizing(this.isShown);
        }
    }

    public void showImmediately() {
        if (!this.isShown) {
            setVisibility(0);
            this.mClipper.cancelAnimator();
            this.mClipper.showBackground();
            this.isShown = true;
            setTileSpecs();
            setCustomizing(true);
            queryTiles();
            this.mNotifQsContainer.setCustomizerAnimating(false);
            this.mNotifQsContainer.setCustomizerShowing(true);
            this.mKeyguardStateController.addCallback(this.mKeyguardCallback);
            this.mLightBarController.setQsCustomizing(this.isShown);
        }
    }

    private void queryTiles() {
        this.mTileQueryHelper.queryTiles(this.mHost);
    }

    private void releaseTiles() {
        this.mTileQueryHelper.releaseTiles();
    }

    public void hide() {
        hide(this.mScreenLifecycle.getScreenState() != 0);
    }

    public boolean isShown() {
        return this.isShown;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCustomizing(boolean z) {
        this.mCustomizing = z;
    }

    public boolean isCustomizing() {
        return this.mCustomizing || this.mOpening;
    }

    private void reset() {
        MiuiTileAdapter miuiTileAdapter = this.mTileAdapter;
        QSTileHost qSTileHost = this.mHost;
        miuiTileAdapter.resetTileSpecs(qSTileHost, qSTileHost.getHostInjector().getMiuiDefaultTiles(((LinearLayout) this).mContext));
    }

    private void setTileSpecs() {
        ArrayList arrayList = new ArrayList();
        for (QSTile qSTile : this.mHost.getTiles()) {
            if (!"edit".equals(qSTile.getTileSpec())) {
                arrayList.add(qSTile.getTileSpec());
            }
        }
        this.mTileAdapter.setTileSpecs(arrayList);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
    }

    private void save() {
        if (this.mTileQueryHelper.isFinished()) {
            this.mTileAdapter.saveSpecs(this.mHost);
            this.mHost.getHostInjector().setMiuiQSTilesEdited();
        }
    }

    public void saveInstanceState(Bundle bundle) {
        if (this.isShown) {
            this.mKeyguardStateController.removeCallback(this.mKeyguardCallback);
        }
        bundle.putBoolean("qs_customizing", this.mCustomizing);
    }

    public void restoreInstanceState(Bundle bundle) {
        if (bundle.getBoolean("qs_customizing")) {
            setVisibility(0);
            addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                /* class com.android.systemui.qs.customize.MiuiQSCustomizer.AnonymousClass1 */

                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    MiuiQSCustomizer.this.removeOnLayoutChangeListener(this);
                    MiuiQSCustomizer.this.showImmediately();
                }
            });
        }
    }

    private void initMiuiAddedView() {
        TextView textView = (TextView) findViewById(16908314);
        this.mResetButton = textView;
        textView.setText(C0021R$string.reset);
        this.mResetButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.qs.customize.$$Lambda$MiuiQSCustomizer$eQKhGYeggWS8XRQpmH9qHZQpl7M */

            public final void onClick(View view) {
                MiuiQSCustomizer.this.lambda$initMiuiAddedView$0$MiuiQSCustomizer(view);
            }
        });
        TextView textView2 = (TextView) findViewById(16908313);
        this.mDoneButton = textView2;
        textView2.setText(C0021R$string.quick_settings_done);
        this.mDoneButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.qs.customize.$$Lambda$MiuiQSCustomizer$3kV5KpVJpOJfqoQ1ScSLLoaadek */

            public final void onClick(View view) {
                MiuiQSCustomizer.this.lambda$initMiuiAddedView$1$MiuiQSCustomizer(view);
            }
        });
        this.mTitle = (TextView) findViewById(C0015R$id.title);
        this.mSubTitle = (TextView) findViewById(C0015R$id.sub_title);
        this.mSpanCount = -1;
        updateResources();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initMiuiAddedView$0 */
    public /* synthetic */ void lambda$initMiuiAddedView$0$MiuiQSCustomizer(View view) {
        MetricsLogger.action(getContext(), 359);
        reset();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initMiuiAddedView$1 */
    public /* synthetic */ void lambda$initMiuiAddedView$1$MiuiQSCustomizer(View view) {
        this.mQsPanelCallback.hide(((int) view.getX()) + (view.getWidth() / 2), ((int) view.getY()) + (view.getHeight() / 2), true);
    }

    private void updateLayout() {
        AnonymousClass6 r0 = new GridLayoutManager(this, getContext(), this.mSpanCount) {
            /* class com.android.systemui.qs.customize.MiuiQSCustomizer.AnonymousClass6 */

            @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
            public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            }
        };
        r0.setSpanSizeLookup(this.mTileAdapter.getSizeLookup());
        this.mRecyclerView.setLayoutManager(r0);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getResources().getDimensionPixelOffset(C0012R$dimen.qs_customize_padding_bottom));
    }

    public void hide(boolean z) {
        if (this.isShown) {
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_CLOSED);
            this.isShown = false;
            this.mClipper.cancelAnimator();
            this.mOpening = false;
            setCustomizing(false);
            save();
            if (z) {
                this.mClipper.animateCircularClip(this.mX, this.mY, false, this.mAnimOutListener);
            } else {
                setVisibility(8);
                this.mAnimOutListener.onAnimationEnd(null);
            }
            this.mNotifQsContainer.setCustomizerAnimating(z);
            this.mNotifQsContainer.setCustomizerShowing(false);
            this.mKeyguardStateController.removeCallback(this.mKeyguardCallback);
            this.mLightBarController.setQsCustomizing(this.isShown);
            releaseTiles();
        }
    }

    public void setMargins(int i, int i2) {
        View findViewById = findViewById(C0015R$id.customize_container);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) findViewById.getLayoutParams();
        marginLayoutParams.rightMargin = i;
        marginLayoutParams.leftMargin = i2;
        findViewById.setLayoutParams(marginLayoutParams);
    }

    public void setAnimatedViews(List<View> list) {
        if (list != null && list.size() != 0) {
            this.mAnimInListener = AnimatorListenerWrapper.of(this.mExpandAnimationListener, new QSAnimation.QsHideBeforeAnimatorListener((View[]) list.toArray(new View[0])));
            this.mAnimOutListener = AnimatorListenerWrapper.of(this.mCollapseAnimationListener, new QSAnimation.QsShowBeforeAnimatorListener((View[]) list.toArray(new View[0])));
            new QSAnimation.QsHideBeforeAnimatorListener((View[]) list.toArray(new View[0])).setAlphaDuration(0);
        }
    }

    public void updateResources(int i) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mHeader.getLayoutParams();
        layoutParams.height = i;
        this.mHeader.setLayoutParams(layoutParams);
        requestLayout();
        updateResources();
    }
}
