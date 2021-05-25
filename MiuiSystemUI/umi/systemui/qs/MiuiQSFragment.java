package com.android.systemui.qs;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import com.android.systemui.C0014R$id;
import com.android.systemui.C0016R$layout;
import com.android.systemui.C0021R$style;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.customize.MiuiQSCustomizer;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.stack.PanelAppearDisappearEvent;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.LifecycleFragment;
import java.util.Arrays;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressLint({"ValidFragment"})
/* compiled from: MiuiQSFragment.kt */
public final class MiuiQSFragment extends LifecycleFragment implements QS, CommandQueue.Callbacks, StatusBarStateController.StateListener, ControlPanelController.UseControlPanelChangeListener {
    private final Animator.AnimatorListener animateHeaderSlidingInListener;
    private final Handler bgHandler;
    private final ControlPanelController controlPanelController;
    private long delay;
    private boolean headerAnimating;
    private final QSTileHost host;
    private final InjectionInflationController injectionInflaterController;
    private boolean lastKeyguardAndExpanded;
    private float lastQSExpansion = -1.0f;
    private int lastViewHeight;
    private int layoutDirection;
    private boolean listening;
    private boolean mAppeared;
    private NotificationsQuickSettingsContainer notificationContainer;
    private final View.OnLayoutChangeListener onLayoutChangeListener = new MiuiQSFragment$onLayoutChangeListener$1(this);
    private QSAnimator qsAnimator;
    private final Rect qsBounds = new Rect();
    @NotNull
    private MiuiQSContainer qsContainer;
    private boolean qsDisabled;
    private boolean qsExpanded;
    private final RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler;
    private final ContentResolver resolver;
    private boolean showCollapsedOnKeyguard;
    private ContentObserver showDataUsageObserver;
    private boolean stackScrollerOverscrolling;
    private final ViewTreeObserver.OnPreDrawListener startHeaderSlidingIn;
    private int statusBarState;
    private final StatusBarStateController statusBarStateController;
    private final Executor uiExecutor;

    @Override // com.android.systemui.plugins.qs.QS
    public void setHasNotifications(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderClickable(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setPanelView(@NotNull QS.HeightListener heightListener) {
        Intrinsics.checkParameterIsNotNull(heightListener, "panelView");
    }

    public MiuiQSFragment(@NotNull RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler2, @NotNull InjectionInflationController injectionInflationController, @NotNull QSTileHost qSTileHost, @NotNull StatusBarStateController statusBarStateController2, @NotNull CommandQueue commandQueue, @NotNull ControlPanelController controlPanelController2, @NotNull Context context, @NotNull Handler handler, @NotNull Executor executor, @NotNull StatusBar statusBar) {
        Intrinsics.checkParameterIsNotNull(remoteInputQuickSettingsDisabler2, "remoteInputQuickSettingsDisabler");
        Intrinsics.checkParameterIsNotNull(injectionInflationController, "injectionInflaterController");
        Intrinsics.checkParameterIsNotNull(qSTileHost, "host");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(commandQueue, "commandQueue");
        Intrinsics.checkParameterIsNotNull(controlPanelController2, "controlPanelController");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(handler, "bgHandler");
        Intrinsics.checkParameterIsNotNull(executor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(statusBar, "statusBar");
        this.remoteInputQuickSettingsDisabler = remoteInputQuickSettingsDisabler2;
        this.injectionInflaterController = injectionInflationController;
        this.host = qSTileHost;
        this.statusBarStateController = statusBarStateController2;
        this.controlPanelController = controlPanelController2;
        this.bgHandler = handler;
        this.uiExecutor = executor;
        commandQueue.observe(getLifecycle(), this);
        ContentResolver contentResolver = context.getContentResolver();
        Intrinsics.checkExpressionValueIsNotNull(contentResolver, "context.contentResolver");
        this.resolver = contentResolver;
        this.startHeaderSlidingIn = new MiuiQSFragment$startHeaderSlidingIn$1(this);
        this.animateHeaderSlidingInListener = new MiuiQSFragment$animateHeaderSlidingInListener$1(this);
        this.mAppeared = true;
    }

    @NotNull
    public final MiuiQSContainer getQsContainer() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            return miuiQSContainer;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Nullable
    public View onCreateView(@NotNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
        Intrinsics.checkParameterIsNotNull(layoutInflater, "inflater");
        return this.injectionInflaterController.injectable(layoutInflater.cloneInContext(new ContextThemeWrapper(getContext(), C0021R$style.qs_theme))).inflate(C0016R$layout.qs_panel, viewGroup, false);
    }

    public void onViewCreated(@NotNull View view, @Nullable Bundle bundle) {
        Intrinsics.checkParameterIsNotNull(view, "view");
        super.onViewCreated(view, bundle);
        View findViewById = view.findViewById(C0014R$id.quick_settings_container);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "view.findViewById(R.id.quick_settings_container)");
        this.qsContainer = (MiuiQSContainer) findViewById;
        this.statusBarStateController.addCallback(this);
        if (this.controlPanelController.isUseControlCenter()) {
            removeQSContent();
        } else {
            addQSContent(bundle);
        }
        this.controlPanelController.addCallback((ControlPanelController.UseControlPanelChangeListener) this);
        view.addOnLayoutChangeListener(new MiuiQSFragment$onViewCreated$1(this));
    }

    private final void addQSContent(Bundle bundle) {
        QSPanel.QSTileLayout tileLayout;
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer == null) {
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        } else if (!miuiQSContainer.getContentAdded()) {
            MiuiQSContainer miuiQSContainer2 = this.qsContainer;
            if (miuiQSContainer2 != null) {
                miuiQSContainer2.addQSContent();
                MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                if (miuiQSContainer3 != null) {
                    View qsPanelScrollView = miuiQSContainer3.getQsPanelScrollView();
                    if (qsPanelScrollView != null) {
                        qsPanelScrollView.addOnLayoutChangeListener(this.onLayoutChangeListener);
                    }
                    MiuiQSContainer miuiQSContainer4 = this.qsContainer;
                    if (miuiQSContainer4 != null) {
                        View qsPanelScrollView2 = miuiQSContainer4.getQsPanelScrollView();
                        if (qsPanelScrollView2 != null) {
                            qsPanelScrollView2.setOnScrollChangeListener(new MiuiQSFragment$addQSContent$1(this));
                        }
                        View view = getView();
                        if (view != null) {
                            ViewGroup viewGroup = (ViewGroup) view.findViewById(C0014R$id.header_text_container);
                            MiuiQSContainer miuiQSContainer5 = this.qsContainer;
                            if (miuiQSContainer5 != null) {
                                QSPanel qsPanel = miuiQSContainer5.getQsPanel();
                                if (qsPanel != null) {
                                    qsPanel.setHeaderContainer(viewGroup);
                                }
                                MiuiQSContainer miuiQSContainer6 = this.qsContainer;
                                if (miuiQSContainer6 != null) {
                                    MiuiQSDetail qsDetail = miuiQSContainer6.getQsDetail();
                                    if (qsDetail != null) {
                                        MiuiQSContainer miuiQSContainer7 = this.qsContainer;
                                        if (miuiQSContainer7 != null) {
                                            QSPanel qsPanel2 = miuiQSContainer7.getQsPanel();
                                            MiuiQSContainer miuiQSContainer8 = this.qsContainer;
                                            if (miuiQSContainer8 != null) {
                                                MiuiNotificationShadeHeader header = miuiQSContainer8.getHeader();
                                                MiuiQSContainer miuiQSContainer9 = this.qsContainer;
                                                if (miuiQSContainer9 != null) {
                                                    QuickQSPanel quickQSPanel = miuiQSContainer9.getQuickQSPanel();
                                                    MiuiQSContainer miuiQSContainer10 = this.qsContainer;
                                                    if (miuiQSContainer10 != null) {
                                                        qsDetail.setQsPanel(qsPanel2, header, quickQSPanel, (View) miuiQSContainer10.getFooter());
                                                    } else {
                                                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                        throw null;
                                                    }
                                                } else {
                                                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                    throw null;
                                                }
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                            throw null;
                                        }
                                    }
                                    MiuiQSContainer miuiQSContainer11 = this.qsContainer;
                                    if (miuiQSContainer11 != null) {
                                        QuickQSPanel quickQSPanel2 = miuiQSContainer11.getQuickQSPanel();
                                        MiuiQSContainer miuiQSContainer12 = this.qsContainer;
                                        if (miuiQSContainer12 != null) {
                                            this.qsAnimator = new QSAnimator(this, quickQSPanel2, miuiQSContainer12.getQsPanel());
                                            MiuiQSContainer miuiQSContainer13 = this.qsContainer;
                                            if (miuiQSContainer13 != null) {
                                                MiuiQSCustomizer qsCustomizer = miuiQSContainer13.getQsCustomizer();
                                                if (qsCustomizer != null) {
                                                    qsCustomizer.setQs(this);
                                                }
                                                if (bundle != null) {
                                                    setExpanded(bundle.getBoolean("expanded"));
                                                    setListening(bundle.getBoolean("listening"));
                                                    MiuiQSContainer miuiQSContainer14 = this.qsContainer;
                                                    if (miuiQSContainer14 != null) {
                                                        MiuiQSCustomizer qsCustomizer2 = miuiQSContainer14.getQsCustomizer();
                                                        if (qsCustomizer2 != null) {
                                                            qsCustomizer2.restoreInstanceState(bundle);
                                                        }
                                                        if (this.qsExpanded) {
                                                            MiuiQSContainer miuiQSContainer15 = this.qsContainer;
                                                            if (miuiQSContainer15 != null) {
                                                                QSPanel qsPanel3 = miuiQSContainer15.getQsPanel();
                                                                if (!(qsPanel3 == null || (tileLayout = qsPanel3.getTileLayout()) == null)) {
                                                                    tileLayout.restoreInstanceState(bundle);
                                                                }
                                                            } else {
                                                                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                                throw null;
                                                            }
                                                        }
                                                    } else {
                                                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                        throw null;
                                                    }
                                                }
                                                setHost(this.host);
                                                onStateChanged(this.statusBarStateController.getState());
                                                this.showDataUsageObserver = new MiuiQSFragment$addQSContent$4(this, this.bgHandler);
                                                updateQSDataUsage();
                                                ContentResolver contentResolver = this.resolver;
                                                Uri uriFor = Settings.System.getUriFor("status_bar_show_network_assistant");
                                                ContentObserver contentObserver = this.showDataUsageObserver;
                                                if (contentObserver != null) {
                                                    contentResolver.registerContentObserver(uriFor, false, contentObserver, -1);
                                                    ContentObserver contentObserver2 = this.showDataUsageObserver;
                                                    if (contentObserver2 != null) {
                                                        contentObserver2.onChange(false);
                                                    }
                                                    NotificationsQuickSettingsContainer notificationsQuickSettingsContainer = this.notificationContainer;
                                                    if (notificationsQuickSettingsContainer != null) {
                                                        MiuiQSContainer miuiQSContainer16 = this.qsContainer;
                                                        if (miuiQSContainer16 != null) {
                                                            MiuiQSCustomizer qsCustomizer3 = miuiQSContainer16.getQsCustomizer();
                                                            if (qsCustomizer3 != null) {
                                                                qsCustomizer3.setContainer(notificationsQuickSettingsContainer);
                                                            }
                                                            MiuiQSContainer miuiQSContainer17 = this.qsContainer;
                                                            if (miuiQSContainer17 != null) {
                                                                MiuiQSDetail qsDetail2 = miuiQSContainer17.getQsDetail();
                                                                if (qsDetail2 != null) {
                                                                    qsDetail2.setContainer(notificationsQuickSettingsContainer);
                                                                    return;
                                                                }
                                                                return;
                                                            }
                                                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                            throw null;
                                                        }
                                                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                                        throw null;
                                                    }
                                                    return;
                                                }
                                                Intrinsics.throwNpe();
                                                throw null;
                                            }
                                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                            throw null;
                                        }
                                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                        throw null;
                                    }
                                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                    throw null;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                            throw null;
                        }
                        Intrinsics.throwNpe();
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
    }

    private final void removeQSContent() {
        QSAnimator qSAnimator = this.qsAnimator;
        if (qSAnimator != null) {
            qSAnimator.onDestroy();
        }
        this.qsAnimator = null;
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            View qsPanelScrollView = miuiQSContainer.getQsPanelScrollView();
            if (qsPanelScrollView != null) {
                qsPanelScrollView.removeOnLayoutChangeListener(this.onLayoutChangeListener);
            }
            MiuiQSContainer miuiQSContainer2 = this.qsContainer;
            if (miuiQSContainer2 != null) {
                View qsPanelScrollView2 = miuiQSContainer2.getQsPanelScrollView();
                if (qsPanelScrollView2 != null) {
                    qsPanelScrollView2.setOnScrollChangeListener(null);
                }
                MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                if (miuiQSContainer3 != null) {
                    MiuiQSCustomizer qsCustomizer = miuiQSContainer3.getQsCustomizer();
                    if (qsCustomizer != null) {
                        qsCustomizer.setQs(null);
                    }
                    ContentObserver contentObserver = this.showDataUsageObserver;
                    if (contentObserver != null) {
                        this.resolver.unregisterContentObserver(contentObserver);
                    }
                    this.showDataUsageObserver = null;
                    MiuiQSContainer miuiQSContainer4 = this.qsContainer;
                    if (miuiQSContainer4 != null) {
                        miuiQSContainer4.removeQSContent();
                        System.gc();
                        return;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.util.LifecycleFragment
    public void onDestroy() {
        super.onDestroy();
        this.statusBarStateController.removeCallback(this);
        this.controlPanelController.removeCallback((ControlPanelController.UseControlPanelChangeListener) this);
        if (this.listening) {
            setListening(false);
        }
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            MiuiQSCustomizer qsCustomizer = miuiQSContainer.getQsCustomizer();
            if (qsCustomizer != null) {
                qsCustomizer.setQs(null);
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    public void onSaveInstanceState(@NotNull Bundle bundle) {
        QSPanel.QSTileLayout tileLayout;
        Intrinsics.checkParameterIsNotNull(bundle, "outState");
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("expanded", this.qsExpanded);
        bundle.putBoolean("listening", this.listening);
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            MiuiQSCustomizer qsCustomizer = miuiQSContainer.getQsCustomizer();
            if (qsCustomizer != null) {
                qsCustomizer.saveInstanceState(bundle);
            }
            if (this.qsExpanded) {
                MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                if (miuiQSContainer2 != null) {
                    QSPanel qsPanel = miuiQSContainer2.getQsPanel();
                    if (qsPanel != null && (tileLayout = qsPanel.getTileLayout()) != null) {
                        tileLayout.saveInstanceState(bundle);
                        return;
                    }
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    public final boolean isListening() {
        return this.listening;
    }

    public final boolean isExpanded() {
        return this.qsExpanded;
    }

    @Override // com.android.systemui.plugins.qs.QS
    @NotNull
    public View getHeader() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            return miuiQSContainer.getHeader();
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        if (configuration.getLayoutDirection() != this.layoutDirection) {
            this.layoutDirection = configuration.getLayoutDirection();
            QSAnimator qSAnimator = this.qsAnimator;
            if (qSAnimator != null) {
                qSAnimator.onRtlChanged();
            }
        }
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            miuiQSContainer.getHeader().onConfigurationChanged(configuration);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setContainer(@Nullable ViewGroup viewGroup) {
        if (viewGroup instanceof NotificationsQuickSettingsContainer) {
            NotificationsQuickSettingsContainer notificationsQuickSettingsContainer = (NotificationsQuickSettingsContainer) viewGroup;
            this.notificationContainer = notificationsQuickSettingsContainer;
            MiuiQSContainer miuiQSContainer = this.qsContainer;
            if (miuiQSContainer != null) {
                MiuiQSCustomizer qsCustomizer = miuiQSContainer.getQsCustomizer();
                if (qsCustomizer != null) {
                    qsCustomizer.setContainer(notificationsQuickSettingsContainer);
                }
                MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                if (miuiQSContainer2 != null) {
                    MiuiQSDetail qsDetail = miuiQSContainer2.getQsDetail();
                    if (qsDetail != null) {
                        qsDetail.setContainer(notificationsQuickSettingsContainer);
                        return;
                    }
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isCustomizing() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            MiuiQSCustomizer qsCustomizer = miuiQSContainer.getQsCustomizer();
            if (qsCustomizer != null) {
                return qsCustomizer.isCustomizing();
            }
            return false;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    public final void setHost(@Nullable QSTileHost qSTileHost) {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSPanel qsPanel = miuiQSContainer.getQsPanel();
            if (qsPanel != null) {
                MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                if (miuiQSContainer2 != null) {
                    qsPanel.setHost(qSTileHost, miuiQSContainer2.getQsCustomizer());
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
            }
            MiuiQSContainer miuiQSContainer3 = this.qsContainer;
            if (miuiQSContainer3 != null) {
                QSFooter footer = miuiQSContainer3.getFooter();
                if (footer != null) {
                    MiuiQSContainer miuiQSContainer4 = this.qsContainer;
                    if (miuiQSContainer4 != null) {
                        footer.setQSPanel(miuiQSContainer4.getQsPanel());
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                }
                MiuiQSContainer miuiQSContainer5 = this.qsContainer;
                if (miuiQSContainer5 != null) {
                    MiuiQSDetail qsDetail = miuiQSContainer5.getQsDetail();
                    if (qsDetail != null) {
                        qsDetail.setHost(qSTileHost);
                    }
                    MiuiQSContainer miuiQSContainer6 = this.qsContainer;
                    if (miuiQSContainer6 != null) {
                        QuickQSPanel quickQSPanel = miuiQSContainer6.getQuickQSPanel();
                        if (quickQSPanel != null) {
                            MiuiQSContainer miuiQSContainer7 = this.qsContainer;
                            if (miuiQSContainer7 != null) {
                                quickQSPanel.setQSPanel(miuiQSContainer7.getQsPanel());
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                throw null;
                            }
                        }
                        MiuiQSContainer miuiQSContainer8 = this.qsContainer;
                        if (miuiQSContainer8 != null) {
                            QuickQSPanel quickQSPanel2 = miuiQSContainer8.getQuickQSPanel();
                            if (quickQSPanel2 != null) {
                                quickQSPanel2.setHost(qSTileHost, null);
                            }
                            QSAnimator qSAnimator = this.qsAnimator;
                            if (qSAnimator != null) {
                                qSAnimator.setHost(qSTileHost);
                                return;
                            }
                            return;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == getContext().getDisplayId()) {
            int adjustDisableFlags = this.remoteInputQuickSettingsDisabler.adjustDisableFlags(i3);
            boolean z2 = (adjustDisableFlags & 1) != 0;
            if (z2 != this.qsDisabled) {
                this.qsDisabled = z2;
                MiuiQSContainer miuiQSContainer = this.qsContainer;
                if (miuiQSContainer != null) {
                    miuiQSContainer.disable(i2, adjustDisableFlags, z);
                    MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                    if (miuiQSContainer2 != null) {
                        QSFooter footer = miuiQSContainer2.getFooter();
                        if (footer != null) {
                            footer.disable(i2, adjustDisableFlags, z);
                        }
                        MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                        if (miuiQSContainer3 != null) {
                            QuickQSPanel quickQSPanel = miuiQSContainer3.getQuickQSPanel();
                            if (quickQSPanel != null) {
                                quickQSPanel.setDisabledByPolicy(z2);
                            }
                            updateQsState();
                            return;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
        }
    }

    /* access modifiers changed from: private */
    public final void updateQsState() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.qsExpanded || this.stackScrollerOverscrolling || this.headerAnimating;
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSPanel qsPanel = miuiQSContainer.getQsPanel();
            if (qsPanel != null) {
                qsPanel.setExpanded(this.qsExpanded);
            }
            MiuiQSContainer miuiQSContainer2 = this.qsContainer;
            if (miuiQSContainer2 != null) {
                MiuiQSDetail qsDetail = miuiQSContainer2.getQsDetail();
                if (qsDetail != null) {
                    qsDetail.setExpanded(this.qsExpanded);
                }
                boolean isKeyguardShowing = isKeyguardShowing();
                MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                if (miuiQSContainer3 != null) {
                    miuiQSContainer3.getHeader().setVisibility((this.qsExpanded || !isKeyguardShowing || this.headerAnimating || this.showCollapsedOnKeyguard) ? 0 : 4);
                    MiuiQSContainer miuiQSContainer4 = this.qsContainer;
                    if (miuiQSContainer4 != null) {
                        miuiQSContainer4.getHeader().setExpanded((isKeyguardShowing && !this.headerAnimating && !this.showCollapsedOnKeyguard) || (this.qsExpanded && !this.stackScrollerOverscrolling));
                        MiuiQSContainer miuiQSContainer5 = this.qsContainer;
                        if (miuiQSContainer5 != null) {
                            QuickQSPanel quickQSPanel = miuiQSContainer5.getQuickQSPanel();
                            if (quickQSPanel != null) {
                                quickQSPanel.setExpanded((isKeyguardShowing && !this.headerAnimating && !this.showCollapsedOnKeyguard) || (this.qsExpanded && !this.stackScrollerOverscrolling));
                            }
                            MiuiQSContainer miuiQSContainer6 = this.qsContainer;
                            if (miuiQSContainer6 != null) {
                                QSFooter footer = miuiQSContainer6.getFooter();
                                if (footer != null) {
                                    footer.setVisibility((this.qsDisabled || (!this.qsExpanded && isKeyguardShowing && !this.headerAnimating && !this.showCollapsedOnKeyguard)) ? 4 : 0);
                                }
                                MiuiQSContainer miuiQSContainer7 = this.qsContainer;
                                if (miuiQSContainer7 != null) {
                                    QSFooter footer2 = miuiQSContainer7.getFooter();
                                    if (footer2 != null) {
                                        if ((!isKeyguardShowing || this.headerAnimating || this.showCollapsedOnKeyguard) && (!this.qsExpanded || this.stackScrollerOverscrolling)) {
                                            z = false;
                                        }
                                        footer2.setExpanded(z);
                                    }
                                    MiuiQSContainer miuiQSContainer8 = this.qsContainer;
                                    if (miuiQSContainer8 != null) {
                                        QSPanel qsPanel2 = miuiQSContainer8.getQsPanel();
                                        if (qsPanel2 != null) {
                                            if (this.qsDisabled || !z2) {
                                                i = 4;
                                            }
                                            qsPanel2.setVisibility(i);
                                            return;
                                        }
                                        return;
                                    }
                                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                    throw null;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    private final boolean isKeyguardShowing() {
        return this.statusBarStateController.getState() == 1;
    }

    private final void setKeyguardShowing(boolean z) {
        this.lastQSExpansion = -1.0f;
        QSAnimator qSAnimator = this.qsAnimator;
        if (qSAnimator != null) {
            qSAnimator.setOnKeyguard(z);
        }
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSFooter footer = miuiQSContainer.getFooter();
            if (footer != null) {
                footer.setKeyguardShowing(z);
            }
            updateQsState();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setShowCollapsedOnKeyguard(boolean z) {
        if (z != this.showCollapsedOnKeyguard) {
            this.showCollapsedOnKeyguard = z;
            updateQsState();
            QSAnimator qSAnimator = this.qsAnimator;
            if (qSAnimator != null) {
                qSAnimator.setShowCollapsedOnKeyguard(z);
            }
            if (!z && isKeyguardShowing()) {
                setQsExpansion(this.lastQSExpansion, 0.0f);
            }
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isShowingDetail() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            MiuiQSDetail qsDetail = miuiQSContainer.getQsDetail();
            if (qsDetail == null) {
                return false;
            }
            if (qsDetail.isShowingDetail()) {
                return true;
            }
            return isCustomizing();
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpanded(boolean z) {
        this.qsExpanded = z;
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSPanel qsPanel = miuiQSContainer.getQsPanel();
            if (qsPanel != null) {
                qsPanel.setListening(this.listening, this.qsExpanded);
            }
            MiuiQSContainer miuiQSContainer2 = this.qsContainer;
            if (miuiQSContainer2 != null) {
                miuiQSContainer2.setBrightnessListening(this.listening);
                updateQsState();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setOverscrolling(boolean z) {
        this.stackScrollerOverscrolling = z;
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setListening(boolean z) {
        Log.d(QS.TAG, "setListening " + z);
        this.listening = !this.controlPanelController.isUseControlCenter() && z;
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSFooter footer = miuiQSContainer.getFooter();
            if (footer != null) {
                footer.setListening(z);
            }
            MiuiQSContainer miuiQSContainer2 = this.qsContainer;
            if (miuiQSContainer2 != null) {
                QSPanel qsPanel = miuiQSContainer2.getQsPanel();
                if (qsPanel != null) {
                    qsPanel.setListening(this.listening, this.qsExpanded);
                }
                MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                if (miuiQSContainer3 != null) {
                    miuiQSContainer3.setBrightnessListening(z);
                    MiuiQSContainer miuiQSContainer4 = this.qsContainer;
                    if (miuiQSContainer4 != null) {
                        QuickQSPanel quickQSPanel = miuiQSContainer4.getQuickQSPanel();
                        if (quickQSPanel != null) {
                            quickQSPanel.setListening(z);
                        }
                        MiuiQSContainer miuiQSContainer5 = this.qsContainer;
                        if (miuiQSContainer5 != null) {
                            QuickQSPanel quickQSPanel2 = miuiQSContainer5.getQuickQSPanel();
                            if (quickQSPanel2 != null) {
                                quickQSPanel2.switchTileLayout();
                                return;
                            }
                            return;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderListening(boolean z) {
        if (!this.controlPanelController.isUseControlCenter()) {
            MiuiQSContainer miuiQSContainer = this.qsContainer;
            if (miuiQSContainer != null) {
                QSFooter footer = miuiQSContainer.getFooter();
                if (footer != null) {
                    footer.setListening(z);
                }
                MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                if (miuiQSContainer2 != null) {
                    QuickQSPanel quickQSPanel = miuiQSContainer2.getQuickQSPanel();
                    if (quickQSPanel != null) {
                        quickQSPanel.setListening(z);
                    }
                    MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                    if (miuiQSContainer3 != null) {
                        miuiQSContainer3.updateDataUsageInfo();
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setQsExpansion(float f, float f2) {
        QSPanel.QSTileLayout tileLayout;
        QSTileRevealController qsTileRevealController;
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            miuiQSContainer.setExpansion(f);
            boolean z = true;
            float f3 = f - ((float) 1);
            boolean z2 = isKeyguardShowing() && !this.showCollapsedOnKeyguard;
            if (!this.headerAnimating && !headerWillBeAnimating()) {
                View view = getView();
                if (view != null) {
                    if (z2) {
                        MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                        if (miuiQSContainer2 != null) {
                            f2 = ((float) miuiQSContainer2.getMinHeight()) * f3;
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                            throw null;
                        }
                    }
                    view.setTranslationY(f2);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
            View view2 = getView();
            if (view2 != null) {
                int height = view2.getHeight();
                if (f != this.lastQSExpansion || this.lastKeyguardAndExpanded != z2 || this.lastViewHeight != height) {
                    this.lastQSExpansion = f;
                    this.lastKeyguardAndExpanded = z2;
                    this.lastViewHeight = height;
                    float f4 = 1.0f;
                    boolean z3 = f == 1.0f;
                    if (f != 0.0f) {
                        z = false;
                    }
                    MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                    if (miuiQSContainer3 != null) {
                        QuickQSPanel quickQSPanel = miuiQSContainer3.getQuickQSPanel();
                        if (quickQSPanel != null) {
                            quickQSPanel.switchTileLayout();
                        }
                        MiuiQSContainer miuiQSContainer4 = this.qsContainer;
                        if (miuiQSContainer4 != null) {
                            QSFooter footer = miuiQSContainer4.getFooter();
                            if (footer != null) {
                                if (!z2) {
                                    f4 = f;
                                }
                                footer.setExpansion(f4);
                            }
                            MiuiQSContainer miuiQSContainer5 = this.qsContainer;
                            if (miuiQSContainer5 != null) {
                                QSPanel qsPanel = miuiQSContainer5.getQsPanel();
                                if (!(qsPanel == null || (qsTileRevealController = qsPanel.getQsTileRevealController()) == null)) {
                                    qsTileRevealController.setExpansion(f);
                                }
                                MiuiQSContainer miuiQSContainer6 = this.qsContainer;
                                if (miuiQSContainer6 != null) {
                                    QSPanel qsPanel2 = miuiQSContainer6.getQsPanel();
                                    if (!(qsPanel2 == null || (tileLayout = qsPanel2.getTileLayout()) == null)) {
                                        tileLayout.setExpansion(f);
                                    }
                                    if (z) {
                                        MiuiQSContainer miuiQSContainer7 = this.qsContainer;
                                        if (miuiQSContainer7 != null) {
                                            View qsPanelScrollView = miuiQSContainer7.getQsPanelScrollView();
                                            if (qsPanelScrollView != null) {
                                                qsPanelScrollView.setScrollY(0);
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                            throw null;
                                        }
                                    }
                                    MiuiQSContainer miuiQSContainer8 = this.qsContainer;
                                    if (miuiQSContainer8 != null) {
                                        MiuiQSDetail qsDetail = miuiQSContainer8.getQsDetail();
                                        if (qsDetail != null) {
                                            qsDetail.setFullyExpanded(z3);
                                        }
                                        MiuiQSContainer miuiQSContainer9 = this.qsContainer;
                                        if (miuiQSContainer9 != null) {
                                            View qsPanelScrollView2 = miuiQSContainer9.getQsPanelScrollView();
                                            if (qsPanelScrollView2 != null && !z3) {
                                                this.qsBounds.top = (int) (-qsPanelScrollView2.getTranslationY());
                                                this.qsBounds.right = qsPanelScrollView2.getWidth();
                                                this.qsBounds.bottom = qsPanelScrollView2.getHeight();
                                            }
                                            updateQsBounds();
                                            QSAnimator qSAnimator = this.qsAnimator;
                                            if (qSAnimator != null) {
                                                qSAnimator.setPosition(f);
                                                return;
                                            }
                                            return;
                                        }
                                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                        throw null;
                                    }
                                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                    throw null;
                                }
                                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                                throw null;
                            }
                            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                            throw null;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    private final void updateQsBounds() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            View qsPanelScrollView = miuiQSContainer.getQsPanelScrollView();
            if (qsPanelScrollView != null) {
                if (this.lastQSExpansion == 1.0f) {
                    this.qsBounds.set(0, 0, qsPanelScrollView.getWidth(), qsPanelScrollView.getHeight());
                }
                qsPanelScrollView.setClipBounds(this.qsBounds);
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    private final boolean headerWillBeAnimating() {
        return this.statusBarState == 1 && this.showCollapsedOnKeyguard && !isKeyguardShowing();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingIn(long j) {
        if (!this.qsExpanded) {
            View view = getView();
            if (view == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (view.getTranslationY() != 0.0f) {
                this.headerAnimating = true;
                this.delay = j;
                View view2 = getView();
                if (view2 != null) {
                    view2.getViewTreeObserver().addOnPreDrawListener(this.startHeaderSlidingIn);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingOut() {
        View view = getView();
        if (view != null) {
            float y = view.getY();
            MiuiQSContainer miuiQSContainer = this.qsContainer;
            if (miuiQSContainer == null) {
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            } else if (y != (-((float) miuiQSContainer.getMinHeight()))) {
                this.headerAnimating = true;
                View view2 = getView();
                if (view2 != null) {
                    ViewPropertyAnimator animate = view2.animate();
                    MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                    if (miuiQSContainer2 != null) {
                        animate.y(-((float) miuiQSContainer2.getMinHeight())).setStartDelay(0).setDuration((long) 300).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new MiuiQSFragment$animateHeaderSlidingOut$1(this)).start();
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                        throw null;
                    }
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpandClickListener(@NotNull View.OnClickListener onClickListener) {
        Intrinsics.checkParameterIsNotNull(onClickListener, "onClickListener");
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSFooter footer = miuiQSContainer.getFooter();
            if (footer != null) {
                footer.setExpandClickListener(onClickListener);
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void closeDetail() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            QSPanel qsPanel = miuiQSContainer.getQsPanel();
            if (qsPanel != null) {
                qsPanel.closeDetail();
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void notifyCustomizeChanged() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            MiuiQSCustomizer qsCustomizer = miuiQSContainer.getQsCustomizer();
            if (qsCustomizer != null) {
                MiuiQSContainer miuiQSContainer2 = this.qsContainer;
                if (miuiQSContainer2 != null) {
                    View qsPanelScrollView = miuiQSContainer2.getQsPanelScrollView();
                    int i = 0;
                    if (qsPanelScrollView != null) {
                        qsPanelScrollView.setVisibility(!qsCustomizer.isCustomizing() ? 0 : 4);
                    }
                    MiuiQSContainer miuiQSContainer3 = this.qsContainer;
                    if (miuiQSContainer3 != null) {
                        QSFooter footer = miuiQSContainer3.getFooter();
                        if (footer != null) {
                            if (qsCustomizer.isCustomizing()) {
                                i = 4;
                            }
                            footer.setVisibility(i);
                            return;
                        }
                        Intrinsics.throwNpe();
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                throw null;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getDesiredHeight() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            MiuiQSCustomizer qsCustomizer = miuiQSContainer.getQsCustomizer();
            if (qsCustomizer != null && qsCustomizer.isCustomizing()) {
                return qsCustomizer.getHeight();
            }
            MiuiQSContainer miuiQSContainer2 = this.qsContainer;
            if (miuiQSContainer2 != null) {
                MiuiQSDetail qsDetail = miuiQSContainer2.getQsDetail();
                if (qsDetail != null && qsDetail.isShowing()) {
                    return qsDetail.getHeight();
                }
                View view = getView();
                if (view != null) {
                    return view.getMeasuredHeight();
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeightOverride(int i) {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            miuiQSContainer.setHeightOverride(i);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getQsMinExpansionHeight() {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            return miuiQSContainer.getMinHeight();
        }
        Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
        throw null;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void hideImmediately() {
        View view = getView();
        if (view != null) {
            view.animate().cancel();
            finishAppearAnimation();
            View view2 = getView();
            if (view2 != null) {
                MiuiQSContainer miuiQSContainer = this.qsContainer;
                if (miuiQSContainer != null) {
                    view2.setY(-((float) miuiQSContainer.getMinHeight()));
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.statusBarState = i;
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        setKeyguardShowing(z);
    }

    /* access modifiers changed from: private */
    public final void updateQSDataUsage() {
        this.uiExecutor.execute(new MiuiQSFragment$updateQSDataUsage$1(this));
    }

    public void onDestroyView() {
        ContentObserver contentObserver = this.showDataUsageObserver;
        if (contentObserver != null) {
            this.resolver.unregisterContentObserver(contentObserver);
        }
        super.onDestroyView();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setDetailAnimatedViews(@NotNull View... viewArr) {
        Intrinsics.checkParameterIsNotNull(viewArr, "views");
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            miuiQSContainer.setDetailAnimatedViews((View[]) Arrays.copyOf(viewArr, viewArr.length));
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
    }

    @Override // com.android.systemui.controlcenter.phone.ControlPanelController.UseControlPanelChangeListener
    public void onUseControlPanelChange(boolean z) {
        MiuiQSContainer miuiQSContainer = this.qsContainer;
        if (miuiQSContainer != null) {
            miuiQSContainer.setShowQSPanel(!z);
            if (z) {
                removeQSContent();
            } else {
                addQSContent(null);
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("qsContainer");
            throw null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateAppearDisappear(boolean z) {
        this.mAppeared = z;
        if (z) {
            setListening(true);
        }
        View view = getView();
        if (view != null) {
            float f = 1.0f;
            ViewPropertyAnimator scaleX = view.animate().setInterpolator(PanelAppearDisappearEvent.Companion.getINTERPOLATOR()).setDuration(450).alpha(this.mAppeared ? 1.0f : 0.0f).scaleX(this.mAppeared ? 1.0f : 0.8f);
            if (!this.mAppeared) {
                f = 0.8f;
            }
            scaleX.scaleY(f).setListener(new MiuiQSFragment$animateAppearDisappear$1(this)).start();
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void finishAppearAnimation() {
        View view = getView();
        if (view != null) {
            float f = 1.0f;
            view.setAlpha(this.mAppeared ? 1.0f : 0.0f);
            View view2 = getView();
            if (view2 != null) {
                view2.setScaleX(this.mAppeared ? 1.0f : 0.8f);
                View view3 = getView();
                if (view3 != null) {
                    if (!this.mAppeared) {
                        f = 0.8f;
                    }
                    view3.setScaleY(f);
                    this.headerAnimating = false;
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
