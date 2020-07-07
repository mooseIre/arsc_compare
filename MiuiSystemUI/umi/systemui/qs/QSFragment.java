package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.app.MiuiStatusBarManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.SystemUI;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.statusbar.policy.OldModeController;
import com.android.systemui.miui.statusbar.policy.SuperSaveModeController;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.util.AutoCleanFloatTransitionListener;
import java.util.Map;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;

public class QSFragment extends Fragment implements QS, CommandQueue.Callbacks, SuperSaveModeController.SuperSaveModeChangeListener, OldModeController.OldModeChangeListener {
    private static final boolean DEBUG = Constants.DEBUG;
    /* access modifiers changed from: private */
    public final Animator.AnimatorListener mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animator) {
            boolean unused = QSFragment.this.mQuickQsAnimating = false;
            QSFragment.this.updateQsState();
        }
    };
    protected View mBackground;
    private Handler mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
    /* access modifiers changed from: private */
    public QSContainerImpl mContainer;
    protected QSContent mContent;
    private int mContentMargin;
    protected View mContentWithoutHeader;
    /* access modifiers changed from: private */
    public long mDelay;
    private int mGutterHeight;
    protected QuickStatusBarHeader mHeader;
    private boolean mKeyguardShowing;
    private float mLastAppearFraction = -1.0f;
    private int mLayoutDirection;
    private boolean mListening;
    private boolean mOldModeOn = false;
    private QS.HeightListener mPanelView;
    private QSAnimator mQSAnimator;
    private QSCustomizer mQSCustomizer;
    /* access modifiers changed from: private */
    public boolean mQSDataUsageEnabled;
    private QSDetail mQSDetail;
    private View mQSFooterBundle;
    protected QSPanel mQSPanel;
    private boolean mQsDisabled;
    private boolean mQsExpanded;
    protected QuickQSPanel mQuickQSPanel;
    /* access modifiers changed from: private */
    public boolean mQuickQsAnimating;
    private ContentResolver mResolver;
    private int mSavedExpandedHeight;
    private ContentObserver mShowDataUsageObserver;
    private boolean mStackScrollerOverscrolling;
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            QSFragment.this.getView().getViewTreeObserver().removeOnPreDrawListener(this);
            QSFragment.this.getView().animate().translationY(0.0f).setStartDelay(QSFragment.this.mDelay).setDuration(448).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(QSFragment.this.mAnimateHeaderSlidingInListener).start();
            QSFragment.this.getView().setY((float) (-QSFragment.this.getQsMinExpansionHeight()));
            return true;
        }
    };
    private int mStatusBarMinHeight;
    /* access modifiers changed from: private */
    public boolean mSuperSaveModeOn = false;
    private float mTopPadding;
    /* access modifiers changed from: private */
    public boolean mUseControlCenter = false;
    private ControlPanelController.UseControlPanelChangeListener mUseControlPanelListener = new ControlPanelController.UseControlPanelChangeListener() {
        public void onUseControlPanelChange(boolean z) {
            boolean unused = QSFragment.this.mUseControlCenter = z;
            QSFragment.this.updateQsState();
        }
    };

    private float getFraction(float f, float f2, float f3) {
        if (f3 <= f) {
            return 0.0f;
        }
        if (f3 >= f2) {
            return 1.0f;
        }
        return (f3 - f) / (f2 - f);
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

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
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

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.qs_panel, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        Resources resources = getResources();
        this.mContainer = (QSContainerImpl) view.findViewById(R.id.quick_settings_container);
        this.mContentWithoutHeader = view.findViewById(R.id.qs_container);
        this.mContent = (QSContent) view.findViewById(R.id.qs_content);
        this.mContent.setQs(this);
        this.mBackground = view.findViewById(R.id.qs_background);
        this.mQuickQSPanel = (QuickQSPanel) view.findViewById(R.id.quick_qs_panel);
        this.mQSPanel = (QSPanel) view.findViewById(R.id.quick_settings_panel);
        this.mQSDetail = (QSDetail) view.findViewById(R.id.qs_detail);
        this.mQSDetail.setQsPanel(this.mQSPanel);
        this.mQSDetail.setQs(this);
        this.mQSCustomizer = (QSCustomizer) view.findViewById(R.id.qs_customize);
        this.mQSCustomizer.setQsPanel(this.mQSPanel);
        this.mQSCustomizer.setQs(this);
        this.mHeader = (QuickStatusBarHeader) view.findViewById(R.id.header);
        this.mQSFooterBundle = view.findViewById(R.id.qs_footer_bundle);
        this.mGutterHeight = resources.getDimensionPixelSize(R.dimen.qs_gutter_height);
        this.mContentMargin = resources.getDimensionPixelSize(R.dimen.panel_content_margin);
        this.mStatusBarMinHeight = resources.getDimensionPixelSize(17105478);
        if (resources.getBoolean(R.bool.config_showQuickSettingsRow)) {
            this.mQSAnimator = new QSAnimator(this, this.mQuickQSPanel, this.mQSPanel);
        }
        if (bundle != null) {
            this.mSavedExpandedHeight = bundle.getInt("savedExpandedHeight");
            setExpanded(bundle.getBoolean("expanded"));
            setListening(bundle.getBoolean("listening"));
            this.mQSCustomizer.restoreInstanceState(bundle);
            if (this.mQsExpanded) {
                this.mQSPanel.getTileLayout().restoreInstanceState(bundle);
            }
        }
        this.mResolver = getContext().getContentResolver();
        this.mShowDataUsageObserver = new ContentObserver(this.mBgHandler) {
            public void onChange(boolean z) {
                Context context = QSFragment.this.getContext();
                if (context != null) {
                    boolean unused = QSFragment.this.mQSDataUsageEnabled = MiuiStatusBarManager.isShowFlowInfoForUser(context, -2) && !QSFragment.this.mSuperSaveModeOn;
                    QSFragment.this.mContainer.post(new Runnable() {
                        public void run() {
                            QSFragment.this.mContainer.updateQSDataUsage(QSFragment.this.mQSDataUsageEnabled);
                        }
                    });
                }
            }
        };
        this.mResolver.registerContentObserver(Settings.System.getUriFor("status_bar_show_network_assistant"), false, this.mShowDataUsageObserver, -1);
        this.mShowDataUsageObserver.onChange(false);
        ((CommandQueue) SystemUI.getComponent(getContext(), CommandQueue.class)).addCallbacks(this);
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).addCallback(this);
        ((OldModeController) Dependency.get(OldModeController.class)).addCallback(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).addCallback(this.mUseControlPanelListener);
    }

    public void onDestroyView() {
        ((OldModeController) Dependency.get(OldModeController.class)).removeCallback(this);
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).removeCallback(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).removeCallback(this.mUseControlPanelListener);
        ((CommandQueue) SystemUI.getComponent(getContext(), CommandQueue.class)).removeCallbacks(this);
        this.mResolver.unregisterContentObserver(this.mShowDataUsageObserver);
        super.onDestroyView();
    }

    public void onDestroy() {
        if (this.mListening) {
            setListening(false);
        }
        this.mQSDetail = null;
        super.onDestroy();
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("expanded", this.mQsExpanded);
        bundle.putBoolean("listening", this.mListening);
        this.mQSCustomizer.saveInstanceState(bundle);
        if (this.mQsExpanded) {
            bundle.putInt("savedExpandedHeight", this.mContent.getMeasuredHeight());
            this.mQSPanel.getTileLayout().saveInstanceState(bundle);
        }
    }

    public boolean isQSFullyCollapsed() {
        return this.mContainer.isQSFullyCollapsed();
    }

    public View getHeader() {
        return this.mHeader;
    }

    public void setHasNotifications(boolean z) {
        this.mContainer.setGutterEnabled(z);
    }

    public void setPanelView(QS.HeightListener heightListener) {
        this.mPanelView = heightListener;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (configuration.getLayoutDirection() != this.mLayoutDirection) {
            this.mLayoutDirection = configuration.getLayoutDirection();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.onRtlChanged();
            }
        }
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSPanel.setHost(qSTileHost);
        this.mQSDetail.setHost(qSTileHost);
        this.mQSCustomizer.setHost(qSTileHost);
        this.mQuickQSPanel.setHost(qSTileHost);
        this.mQuickQSPanel.setQSPanelAndHeader(this.mQSPanel, this.mHeader);
        if (this.mContainer.getQSFooter() != null) {
            this.mContainer.getQSFooter().setQSPanel(this.mQSPanel);
        }
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setHost(qSTileHost);
        }
    }

    public void disable(int i, int i2, boolean z) {
        boolean z2 = true;
        if ((i2 & 1) == 0) {
            z2 = false;
        }
        if (z2 != this.mQsDisabled) {
            this.mQsDisabled = z2;
            updateQsState();
        }
    }

    /* access modifiers changed from: private */
    public void updateQsState() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.mQsExpanded || this.mStackScrollerOverscrolling || this.mQuickQsAnimating;
        int i2 = 4;
        if (this.mUseControlCenter) {
            QuickStatusBarHeader quickStatusBarHeader = this.mHeader;
            if (this.mQsExpanded || !this.mKeyguardShowing || this.mQuickQsAnimating) {
                i2 = 0;
            }
            quickStatusBarHeader.setVisibility(i2);
            QuickStatusBarHeader quickStatusBarHeader2 = this.mHeader;
            if ((!this.mKeyguardShowing || this.mQuickQsAnimating) && (!this.mQsExpanded || this.mStackScrollerOverscrolling)) {
                z = false;
            }
            quickStatusBarHeader2.setExpanded(z);
            this.mQSPanel.setExpanded(false);
            this.mQuickQSPanel.setExpanded(false);
            QSDetail qSDetail = this.mQSDetail;
            if (qSDetail != null) {
                qSDetail.setExpanded(false);
            }
            if (this.mContainer.getQSFooter() != null) {
                this.mContainer.getQSFooter().setExpanded(false);
            }
            this.mContentWithoutHeader.setVisibility(8);
        } else {
            this.mQSPanel.setExpanded(this.mQsExpanded);
            this.mQuickQSPanel.setExpanded(this.mQsExpanded);
            QSDetail qSDetail2 = this.mQSDetail;
            if (qSDetail2 != null) {
                qSDetail2.setExpanded(this.mQsExpanded);
            }
            this.mHeader.setVisibility((this.mQsExpanded || !this.mKeyguardShowing || this.mQuickQsAnimating) ? 0 : 4);
            this.mHeader.setExpanded((this.mKeyguardShowing && !this.mQuickQsAnimating) || (this.mQsExpanded && !this.mStackScrollerOverscrolling));
            if (this.mContainer.getQSFooter() != null) {
                QSFooter qSFooter = this.mContainer.getQSFooter();
                if ((!this.mKeyguardShowing || this.mQuickQsAnimating) && (!this.mQsExpanded || this.mStackScrollerOverscrolling)) {
                    z = false;
                }
                qSFooter.setExpanded(z);
            }
            this.mQSPanel.setVisibility(z2 ? 0 : 4);
            this.mContainer.getBrightnessView().setVisibility((!this.mKeyguardShowing || z2) ? 0 : 4);
            View expandIndicator = this.mContainer.getExpandIndicator();
            if (!this.mKeyguardShowing || z2) {
                i2 = 0;
            }
            expandIndicator.setVisibility(i2);
            this.mContentWithoutHeader.setVisibility(0);
        }
        QSContainerImpl qSContainerImpl = this.mContainer;
        if (this.mQsDisabled) {
            i = 8;
        }
        qSContainerImpl.setVisibility(i);
    }

    public View getQsContent() {
        return this.mContentWithoutHeader;
    }

    public QSPanel getQSPanel() {
        return this.mQSPanel;
    }

    public QuickQSPanel getQuickQSPanel() {
        return this.mQuickQSPanel;
    }

    public void setHeaderClickable(boolean z) {
        if (DEBUG) {
            Log.d("QSFragment", "setHeaderClickable " + z);
        }
        if (this.mContainer.getQSFooter() != null) {
            this.mContainer.getQSFooter().getExpandView().setClickable(z);
        }
    }

    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing() || this.mQSCustomizer.isShown();
    }

    public void setExpanded(boolean z) {
        if (DEBUG) {
            Log.d("QSFragment", "setExpanded " + z);
        }
        this.mQsExpanded = z;
        updateQsState();
    }

    public void setKeyguardShowing(boolean z) {
        if (DEBUG) {
            Log.d("QSFragment", "setKeyguardShowing " + z);
        }
        this.mKeyguardShowing = z;
        if (this.mKeyguardShowing) {
            onPanelDisplayChanged(false, true);
        }
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setOnKeyguard(z);
        }
        if (this.mContainer.getQSFooter() != null) {
            this.mContainer.getQSFooter().setKeyguardShowing(z);
        }
        updateQsState();
    }

    public void setOverscrolling(boolean z) {
        if (DEBUG) {
            Log.d("QSFragment", "setOverscrolling " + z);
        }
        this.mStackScrollerOverscrolling = z;
        updateQsState();
    }

    public void setListening(boolean z) {
        if (DEBUG) {
            Log.d("QSFragment", "setListening " + z);
        }
        if (this.mUseControlCenter) {
            z = false;
        }
        this.mListening = z;
        this.mContainer.setListening(z);
        this.mQSPanel.setListening(this.mListening);
    }

    public boolean isShowingDetail() {
        return this.mQSDetail.isShowingDetail();
    }

    public void setHeaderListening(boolean z) {
        this.mQuickQSPanel.setListening(z);
        if (this.mContainer.getQSFooter() != null) {
            this.mContainer.getQSFooter().setListening(z);
        }
        if (this.mContainer.isDataUsageAvailable()) {
            this.mContainer.updateDataUsageInfo();
        }
    }

    public void notifyCustomizeChanged() {
        this.mPanelView.onQsHeightChanged();
    }

    public void setContainer(ViewGroup viewGroup) {
        this.mQSCustomizer.setContainer(viewGroup);
    }

    public void setDetailAnimatedViews(View... viewArr) {
        this.mContainer.setDetailAnimatedViews(viewArr);
    }

    public void setQsExpansion(float f, float f2, float f3) {
        if (DEBUG) {
            Log.d("QSFragment", "setQSExpansion: expansion: " + f + ", headerTranslation: " + f2 + " appearFraction:" + f3);
        }
        this.mContainer.setExpansion(f);
        float f4 = f - 1.0f;
        if (!this.mQuickQsAnimating) {
            getView().setTranslationY(this.mKeyguardShowing ? ((float) (getQsMinExpansionHeight() + this.mGutterHeight)) * f4 : this.mTopPadding);
        }
        if (this.mLastAppearFraction != f3) {
            float fraction = getFraction(0.05f, 0.3f, f3);
            this.mHeader.setAlpha(fraction);
            QuickStatusBarHeader quickStatusBarHeader = this.mHeader;
            quickStatusBarHeader.setTranslationY((1.0f - fraction) * 0.25f * ((float) (-quickStatusBarHeader.getHeight())));
            float fraction2 = getFraction(0.1f, 1.0f, f3);
            View qsContent = getQsContent();
            qsContent.setTransitionAlpha(fraction2);
            float f5 = 0.88f + (0.12f * f3);
            if (!Float.isFinite(f5)) {
                f5 = 1.0f;
            }
            qsContent.setScaleX(f5);
            qsContent.setScaleY(f5);
            qsContent.setPivotX(((float) qsContent.getWidth()) * 0.5f);
            qsContent.setPivotY(((float) qsContent.getHeight()) * -0.3f);
            this.mLastAppearFraction = f3;
        }
        if (this.mContainer.getQSFooter() != null) {
            this.mContainer.getQSFooter().setExpansion(this.mKeyguardShowing ? 1.0f : f);
        }
        this.mQSPanel.setTranslationY(f4 * ((float) ((this.mQSPanel.getBottom() - this.mQuickQSPanel.getBottom()) + this.mQuickQSPanel.getPaddingBottom())));
        QSDetail qSDetail = this.mQSDetail;
        if (qSDetail != null) {
            qSDetail.setFullyExpanded(f == 1.0f);
        }
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setPosition(f);
        }
    }

    public void animateHeaderSlidingIn(long j) {
        if (DEBUG) {
            Log.d("QSFragment", "animateHeaderSlidingIn mQsExpanded=" + this.mQsExpanded);
        }
        if (!this.mQsExpanded) {
            this.mQuickQsAnimating = true;
            this.mDelay = j;
            getView().getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
        }
    }

    public void animateHeaderSlidingOut() {
        if (DEBUG) {
            Log.d("QSFragment", "animateHeaderSlidingOut");
        }
        this.mQuickQsAnimating = true;
        getView().animate().y((float) ((-getQsMinExpansionHeight()) - (this.mContentMargin * 2))).setStartDelay(0).setDuration(360).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                QSFragment.this.getView().animate().setListener((Animator.AnimatorListener) null);
                boolean unused = QSFragment.this.mQuickQsAnimating = false;
                QSFragment.this.updateQsState();
            }
        }).start();
    }

    public void setExpandClickListener(View.OnClickListener onClickListener) {
        if (this.mContainer.getQSFooter() != null) {
            this.mContainer.getQSFooter().getExpandView().setOnClickListener(onClickListener);
        }
    }

    public void closeDetail() {
        this.mQSPanel.closeDetail(true);
    }

    public int getDesiredHeight() {
        int i;
        int visualBottom;
        int height;
        if (this.mUseControlCenter) {
            return this.mHeader.getHeight();
        }
        if (this.mQSCustomizer.isShown()) {
            visualBottom = this.mQSCustomizer.getVisualBottom();
            height = this.mQSFooterBundle.getHeight();
        } else if (this.mQSDetail.isShowingDetail()) {
            visualBottom = this.mQSDetail.getVisualBottom();
            height = this.mQSFooterBundle.getHeight();
        } else if (!this.mQsExpanded || this.mQsDisabled || (i = this.mSavedExpandedHeight) <= 0) {
            this.mSavedExpandedHeight = 0;
            return this.mQsDisabled ? this.mStatusBarMinHeight : this.mContent.getMeasuredHeight();
        } else {
            this.mSavedExpandedHeight = 0;
            return i;
        }
        return visualBottom + height;
    }

    public void setHeightOverride(int i) {
        this.mContainer.setHeightOverride(i);
    }

    public void setBrightnessMirror(BrightnessMirrorController brightnessMirrorController) {
        this.mContainer.setBrightnessMirror(brightnessMirrorController);
    }

    public int getQsMinExpansionHeight() {
        if (this.mUseControlCenter) {
            return this.mHeader.getHeight();
        }
        return this.mQsDisabled ? this.mStatusBarMinHeight : this.mContainer.getQsMinExpansionHeight();
    }

    public int getQsHeaderHeight() {
        return this.mQsDisabled ? this.mStatusBarMinHeight : this.mHeader.getHeight();
    }

    public void hideImmediately() {
        getView().animate().cancel();
        getView().setY((float) (-getQsMinExpansionHeight()));
    }

    public void onPanelDisplayChanged(boolean z, boolean z2) {
        if (!z2) {
            animateVisibility(z);
            return;
        }
        this.mContainer.setAlpha(1.0f);
        this.mContainer.setScaleX(1.0f);
        this.mContainer.setScaleY(1.0f);
    }

    private void animateVisibility(boolean z) {
        final String str = z ? "QSFragmentAppear" : "QSFragmentDisappear";
        float f = 0.0f;
        float f2 = 1.0f;
        float f3 = z ? 0.0f : 1.0f;
        if (z) {
            f = 1.0f;
        }
        float f4 = z ? 0.8f : 1.0f;
        if (!z) {
            f2 = 0.8f;
        }
        Folme.getValueTarget(str).setMinVisibleChange(0.01f, "alpha", "scale");
        IStateStyle useValue = Folme.useValue(str);
        useValue.setTo("alpha", Float.valueOf(f3), "scale", Float.valueOf(f4));
        useValue.addListener(new AutoCleanFloatTransitionListener(str) {
            public void onUpdate(Map<String, Float> map) {
                float floatValue = map.get("alpha").floatValue();
                float floatValue2 = map.get("scale").floatValue();
                QSFragment.this.mContainer.setAlpha(floatValue);
                QSFragment.this.mContainer.setScaleX(floatValue2);
                QSFragment.this.mContainer.setScaleY(floatValue2);
            }
        });
        useValue.to("alpha", Float.valueOf(f));
        useValue.to("scale", Float.valueOf(f2));
    }

    public void updateTopPadding(float f) {
        this.mTopPadding = Math.max(0.0f, f);
        getView().setTranslationY(this.mTopPadding);
    }

    public void onSuperSaveModeChange(boolean z) {
        if (this.mSuperSaveModeOn != z) {
            this.mSuperSaveModeOn = z;
            this.mShowDataUsageObserver.onChange(false);
            this.mHeader.onSuperSaveModeChange(z);
            updateQsState();
        }
    }

    public void onOldModeChange(boolean z) {
        Log.d("QSFragment", "onOldModeChange: " + z);
        if (this.mOldModeOn != z) {
            this.mOldModeOn = z;
            this.mQSPanel.getTileLayout().setOldModeOn(z);
        }
    }
}
