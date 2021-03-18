package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.util.LifecycleActivity;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsProviderSelectorActivity.kt */
public final class ControlsProviderSelectorActivity extends LifecycleActivity {
    private final Executor backExecutor;
    private final ControlsController controlsController;
    private final ControlsProviderSelectorActivity$currentUserTracker$1 currentUserTracker;
    private final Executor executor;
    private final GlobalActionsComponent globalActionsComponent;
    private final ControlsListingController listingController;
    private RecyclerView recyclerView;

    public static final /* synthetic */ RecyclerView access$getRecyclerView$p(ControlsProviderSelectorActivity controlsProviderSelectorActivity) {
        RecyclerView recyclerView2 = controlsProviderSelectorActivity.recyclerView;
        if (recyclerView2 != null) {
            return recyclerView2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        throw null;
    }

    public ControlsProviderSelectorActivity(@NotNull Executor executor2, @NotNull Executor executor3, @NotNull ControlsListingController controlsListingController, @NotNull ControlsController controlsController2, @NotNull GlobalActionsComponent globalActionsComponent2, @NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        Intrinsics.checkParameterIsNotNull(executor3, "backExecutor");
        Intrinsics.checkParameterIsNotNull(controlsListingController, "listingController");
        Intrinsics.checkParameterIsNotNull(controlsController2, "controlsController");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent2, "globalActionsComponent");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        this.executor = executor2;
        this.backExecutor = executor3;
        this.listingController = controlsListingController;
        this.controlsController = controlsController2;
        this.globalActionsComponent = globalActionsComponent2;
        this.currentUserTracker = new ControlsProviderSelectorActivity$currentUserTracker$1(this, broadcastDispatcher, broadcastDispatcher);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
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
        viewStub.setLayoutResource(C0017R$layout.controls_management_apps);
        viewStub.inflate();
        View requireViewById2 = requireViewById(C0015R$id.list);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.list)");
        RecyclerView recyclerView2 = (RecyclerView) requireViewById2;
        this.recyclerView = recyclerView2;
        if (recyclerView2 != null) {
            recyclerView2.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            TextView textView = (TextView) requireViewById(C0015R$id.title);
            textView.setText(textView.getResources().getText(C0021R$string.controls_providers_title));
            Button button = (Button) requireViewById(C0015R$id.other_apps);
            button.setVisibility(0);
            button.setText(17039360);
            button.setOnClickListener(new ControlsProviderSelectorActivity$onCreate$$inlined$apply$lambda$1(this));
            View requireViewById3 = requireViewById(C0015R$id.done);
            Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<View>(R.id.done)");
            requireViewById3.setVisibility(8);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        throw null;
    }

    public void onBackPressed() {
        this.globalActionsComponent.handleShowGlobalActionsMenu();
        animateExitAndFinish();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onStart() {
        super.onStart();
        this.currentUserTracker.startTracking();
        RecyclerView recyclerView2 = this.recyclerView;
        if (recyclerView2 != null) {
            recyclerView2.setAlpha(0.0f);
            RecyclerView recyclerView3 = this.recyclerView;
            if (recyclerView3 != null) {
                Executor executor2 = this.backExecutor;
                Executor executor3 = this.executor;
                Lifecycle lifecycle = getLifecycle();
                ControlsListingController controlsListingController = this.listingController;
                LayoutInflater from = LayoutInflater.from(this);
                Intrinsics.checkExpressionValueIsNotNull(from, "LayoutInflater.from(this)");
                ControlsProviderSelectorActivity$onStart$1 controlsProviderSelectorActivity$onStart$1 = new ControlsProviderSelectorActivity$onStart$1(this);
                Resources resources = getResources();
                Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
                FavoritesRenderer favoritesRenderer = new FavoritesRenderer(resources, new ControlsProviderSelectorActivity$onStart$2(this.controlsController));
                Resources resources2 = getResources();
                Intrinsics.checkExpressionValueIsNotNull(resources2, "resources");
                AppAdapter appAdapter = new AppAdapter(executor2, executor3, lifecycle, controlsListingController, from, controlsProviderSelectorActivity$onStart$1, favoritesRenderer, resources2);
                appAdapter.registerAdapterDataObserver(new ControlsProviderSelectorActivity$onStart$$inlined$apply$lambda$1(this));
                recyclerView3.setAdapter(appAdapter);
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("recyclerView");
        throw null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onStop() {
        super.onStop();
        this.currentUserTracker.stopTracking();
    }

    public final void launchFavoritingActivity(@Nullable ComponentName componentName) {
        this.executor.execute(new ControlsProviderSelectorActivity$launchFavoritingActivity$1(this, componentName));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onDestroy() {
        this.currentUserTracker.stopTracking();
        super.onDestroy();
    }

    private final void animateExitAndFinish() {
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0015R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewGroup, "rootView");
        ControlsAnimations.exitAnimation(viewGroup, new ControlsProviderSelectorActivity$animateExitAndFinish$1(this)).start();
    }
}
