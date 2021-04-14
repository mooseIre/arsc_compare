package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.viewpager2.widget.ViewPager2;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Prefs;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.TooltipManager;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.util.LifecycleActivity;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

public final class ControlsFavoritingActivity extends LifecycleActivity {
    private CharSequence appName;
    private Runnable cancelLoadRunnable;
    private Comparator<StructureContainer> comparator;
    private ComponentName component;
    private final ControlsControllerImpl controller;
    private final ControlsFavoritingActivity$controlsModelCallback$1 controlsModelCallback;
    private final ControlsFavoritingActivity$currentUserTracker$1 currentUserTracker;
    private View doneButton;
    private final Executor executor;
    private boolean fromProviderSelector;
    private final GlobalActionsComponent globalActionsComponent;
    private boolean isPagerLoaded;
    private List<StructureContainer> listOfStructures = CollectionsKt__CollectionsKt.emptyList();
    private final ControlsFavoritingActivity$listingCallback$1 listingCallback;
    private final ControlsListingController listingController;
    private TooltipManager mTooltipManager;
    private View otherAppsButton;
    private ManagementPageIndicator pageIndicator;
    private TextView statusText;
    private CharSequence structureExtra;
    private ViewPager2 structurePager;
    private TextView subtitleView;
    private TextView titleView;

    public static final /* synthetic */ Comparator access$getComparator$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        Comparator<StructureContainer> comparator2 = controlsFavoritingActivity.comparator;
        if (comparator2 != null) {
            return comparator2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("comparator");
        throw null;
    }

    public static final /* synthetic */ View access$getDoneButton$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        View view = controlsFavoritingActivity.doneButton;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("doneButton");
        throw null;
    }

    public static final /* synthetic */ View access$getOtherAppsButton$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        View view = controlsFavoritingActivity.otherAppsButton;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("otherAppsButton");
        throw null;
    }

    public static final /* synthetic */ ManagementPageIndicator access$getPageIndicator$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        ManagementPageIndicator managementPageIndicator = controlsFavoritingActivity.pageIndicator;
        if (managementPageIndicator != null) {
            return managementPageIndicator;
        }
        Intrinsics.throwUninitializedPropertyAccessException("pageIndicator");
        throw null;
    }

    public static final /* synthetic */ TextView access$getStatusText$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.statusText;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("statusText");
        throw null;
    }

    public static final /* synthetic */ ViewPager2 access$getStructurePager$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        ViewPager2 viewPager2 = controlsFavoritingActivity.structurePager;
        if (viewPager2 != null) {
            return viewPager2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        throw null;
    }

    public static final /* synthetic */ TextView access$getSubtitleView$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.subtitleView;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("subtitleView");
        throw null;
    }

    public static final /* synthetic */ TextView access$getTitleView$p(ControlsFavoritingActivity controlsFavoritingActivity) {
        TextView textView = controlsFavoritingActivity.titleView;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("titleView");
        throw null;
    }

    public ControlsFavoritingActivity(Executor executor2, ControlsControllerImpl controlsControllerImpl, ControlsListingController controlsListingController, BroadcastDispatcher broadcastDispatcher, GlobalActionsComponent globalActionsComponent2) {
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        Intrinsics.checkParameterIsNotNull(controlsControllerImpl, "controller");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "listingController");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent2, "globalActionsComponent");
        this.executor = executor2;
        this.controller = controlsControllerImpl;
        this.listingController = controlsListingController;
        this.globalActionsComponent = globalActionsComponent2;
        this.currentUserTracker = new ControlsFavoritingActivity$currentUserTracker$1(this, broadcastDispatcher, broadcastDispatcher);
        this.listingCallback = new ControlsFavoritingActivity$listingCallback$1(this);
        this.controlsModelCallback = new ControlsFavoritingActivity$controlsModelCallback$1(this);
    }

    public void onBackPressed() {
        if (!this.fromProviderSelector) {
            this.globalActionsComponent.handleShowGlobalActionsMenu();
        }
        animateExitAndFinish();
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Resources resources = getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
        Collator instance = Collator.getInstance(configuration.getLocales().get(0));
        Intrinsics.checkExpressionValueIsNotNull(instance, "collator");
        this.comparator = new ControlsFavoritingActivity$onCreate$$inlined$compareBy$1(instance);
        this.appName = getIntent().getCharSequenceExtra("extra_app_label");
        this.structureExtra = getIntent().getCharSequenceExtra("extra_structure");
        this.component = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        this.fromProviderSelector = getIntent().getBooleanExtra("extra_from_provider_selector", false);
        bindViews();
    }

    private final void loadControls() {
        ComponentName componentName = this.component;
        if (componentName != null) {
            TextView textView = this.statusText;
            if (textView != null) {
                textView.setText(getResources().getText(17040487));
                this.controller.loadForComponent(componentName, new ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1(getResources().getText(C0021R$string.controls_favorite_other_zone_header), this), new ControlsFavoritingActivity$loadControls$$inlined$let$lambda$2(this));
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("statusText");
            throw null;
        }
    }

    private final void setUpPager() {
        ViewPager2 viewPager2 = this.structurePager;
        if (viewPager2 != null) {
            viewPager2.setAlpha(0.0f);
            ManagementPageIndicator managementPageIndicator = this.pageIndicator;
            if (managementPageIndicator != null) {
                managementPageIndicator.setAlpha(0.0f);
                ViewPager2 viewPager22 = this.structurePager;
                if (viewPager22 != null) {
                    viewPager22.setAdapter(new StructureAdapter(CollectionsKt__CollectionsKt.emptyList()));
                    viewPager22.registerOnPageChangeCallback(new ControlsFavoritingActivity$setUpPager$$inlined$apply$lambda$1(this));
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("structurePager");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("pageIndicator");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        throw null;
    }

    private final void bindViews() {
        setContentView(C0017R$layout.controls_management);
        Lifecycle lifecycle = getLifecycle();
        ControlsAnimations controlsAnimations = ControlsAnimations.INSTANCE;
        View requireViewById = requireViewById(C0015R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<ViewGrou…controls_management_root)");
        Window window = getWindow();
        Intrinsics.checkExpressionValueIsNotNull(window, "window");
        Intent intent = getIntent();
        Intrinsics.checkExpressionValueIsNotNull(intent, "intent");
        lifecycle.addObserver(controlsAnimations.observerForAnimations((ViewGroup) requireViewById, window, intent));
        ViewStub viewStub = (ViewStub) requireViewById(C0015R$id.stub);
        viewStub.setLayoutResource(C0017R$layout.controls_management_favorites);
        viewStub.inflate();
        View requireViewById2 = requireViewById(C0015R$id.status_message);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.status_message)");
        this.statusText = (TextView) requireViewById2;
        if (shouldShowTooltip()) {
            TextView textView = this.statusText;
            if (textView != null) {
                Context context = textView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "statusText.context");
                TooltipManager tooltipManager = new TooltipManager(context, "ControlsStructureSwipeTooltipCount", 2, false, 8, null);
                this.mTooltipManager = tooltipManager;
                addContentView(tooltipManager != null ? tooltipManager.getLayout() : null, new FrameLayout.LayoutParams(-2, -2, 51));
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("statusText");
                throw null;
            }
        }
        View requireViewById3 = requireViewById(C0015R$id.structure_page_indicator);
        ManagementPageIndicator managementPageIndicator = (ManagementPageIndicator) requireViewById3;
        managementPageIndicator.setVisibilityListener(new ControlsFavoritingActivity$bindViews$$inlined$apply$lambda$1(this));
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<Manageme…}\n            }\n        }");
        this.pageIndicator = managementPageIndicator;
        CharSequence charSequence = this.structureExtra;
        if (charSequence == null && (charSequence = this.appName) == null) {
            charSequence = getResources().getText(C0021R$string.controls_favorite_default_title);
        }
        View requireViewById4 = requireViewById(C0015R$id.title);
        TextView textView2 = (TextView) requireViewById4;
        textView2.setText(charSequence);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "requireViewById<TextView…   text = title\n        }");
        this.titleView = textView2;
        View requireViewById5 = requireViewById(C0015R$id.subtitle);
        TextView textView3 = (TextView) requireViewById5;
        textView3.setText(textView3.getResources().getText(C0021R$string.controls_favorite_subtitle));
        Intrinsics.checkExpressionValueIsNotNull(requireViewById5, "requireViewById<TextView…orite_subtitle)\n        }");
        this.subtitleView = textView3;
        View requireViewById6 = requireViewById(C0015R$id.structure_pager);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById6, "requireViewById<ViewPager2>(R.id.structure_pager)");
        ViewPager2 viewPager2 = (ViewPager2) requireViewById6;
        this.structurePager = viewPager2;
        if (viewPager2 != null) {
            viewPager2.registerOnPageChangeCallback(new ControlsFavoritingActivity$bindViews$5(this));
            bindButtons();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structurePager");
        throw null;
    }

    /* access modifiers changed from: public */
    private final void animateExitAndFinish() {
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0015R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewGroup, "rootView");
        ControlsAnimations.exitAnimation(viewGroup, new ControlsFavoritingActivity$animateExitAndFinish$1(this)).start();
    }

    private final void bindButtons() {
        View requireViewById = requireViewById(C0015R$id.other_apps);
        Button button = (Button) requireViewById;
        button.setOnClickListener(new ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$1(button, this));
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<Button>(…)\n            }\n        }");
        this.otherAppsButton = requireViewById;
        View requireViewById2 = requireViewById(C0015R$id.done);
        Button button2 = (Button) requireViewById2;
        button2.setEnabled(false);
        button2.setOnClickListener(new ControlsFavoritingActivity$bindButtons$$inlined$apply$lambda$2(this));
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById<Button>(…)\n            }\n        }");
        this.doneButton = requireViewById2;
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onPause() {
        super.onPause();
        TooltipManager tooltipManager = this.mTooltipManager;
        if (tooltipManager != null) {
            tooltipManager.hide(false);
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onStart() {
        super.onStart();
        this.listingController.addCallback(this.listingCallback);
        this.currentUserTracker.startTracking();
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onResume() {
        super.onResume();
        if (!this.isPagerLoaded) {
            setUpPager();
            loadControls();
            this.isPagerLoaded = true;
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onStop() {
        super.onStop();
        this.listingController.removeCallback(this.listingCallback);
        this.currentUserTracker.stopTracking();
    }

    public void onConfigurationChanged(Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        TooltipManager tooltipManager = this.mTooltipManager;
        if (tooltipManager != null) {
            tooltipManager.hide(false);
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onDestroy() {
        Runnable runnable = this.cancelLoadRunnable;
        if (runnable != null) {
            runnable.run();
        }
        super.onDestroy();
    }

    private final boolean shouldShowTooltip() {
        return Prefs.getInt(getApplicationContext(), "ControlsStructureSwipeTooltipCount", 0) < 2;
    }
}
