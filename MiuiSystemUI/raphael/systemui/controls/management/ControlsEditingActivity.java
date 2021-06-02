package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsControllerImpl;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.util.LifecycleActivity;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

public final class ControlsEditingActivity extends LifecycleActivity {
    private static final int EMPTY_TEXT_ID = C0021R$string.controls_favorite_removed;
    private static final int SUBTITLE_ID = C0021R$string.controls_favorite_rearrange;
    private ComponentName component;
    private final ControlsControllerImpl controller;
    private final ControlsEditingActivity$currentUserTracker$1 currentUserTracker;
    private final ControlsEditingActivity$favoritesModelCallback$1 favoritesModelCallback = new ControlsEditingActivity$favoritesModelCallback$1(this);
    private final GlobalActionsComponent globalActionsComponent;
    private FavoritesModel model;
    private View saveButton;
    private CharSequence structure;
    private TextView subtitle;

    public static final /* synthetic */ View access$getSaveButton$p(ControlsEditingActivity controlsEditingActivity) {
        View view = controlsEditingActivity.saveButton;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("saveButton");
        throw null;
    }

    public static final /* synthetic */ TextView access$getSubtitle$p(ControlsEditingActivity controlsEditingActivity) {
        TextView textView = controlsEditingActivity.subtitle;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("subtitle");
        throw null;
    }

    public ControlsEditingActivity(ControlsControllerImpl controlsControllerImpl, BroadcastDispatcher broadcastDispatcher, GlobalActionsComponent globalActionsComponent2) {
        Intrinsics.checkParameterIsNotNull(controlsControllerImpl, "controller");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent2, "globalActionsComponent");
        this.controller = controlsControllerImpl;
        this.globalActionsComponent = globalActionsComponent2;
        this.currentUserTracker = new ControlsEditingActivity$currentUserTracker$1(this, broadcastDispatcher, broadcastDispatcher);
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ComponentName componentName = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        if (componentName != null) {
            this.component = componentName;
        } else {
            finish();
        }
        CharSequence charSequenceExtra = getIntent().getCharSequenceExtra("extra_structure");
        if (charSequenceExtra != null) {
            this.structure = charSequenceExtra;
        } else {
            finish();
        }
        bindViews();
        bindButtons();
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onStart() {
        super.onStart();
        setUpList();
        this.currentUserTracker.startTracking();
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onStop() {
        super.onStop();
        this.currentUserTracker.stopTracking();
    }

    public void onBackPressed() {
        this.globalActionsComponent.handleShowGlobalActionsMenu();
        animateExitAndFinish();
    }

    /* access modifiers changed from: public */
    private final void animateExitAndFinish() {
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0015R$id.controls_management_root);
        Intrinsics.checkExpressionValueIsNotNull(viewGroup, "rootView");
        ControlsAnimations.exitAnimation(viewGroup, new ControlsEditingActivity$animateExitAndFinish$1(this)).start();
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
        viewStub.setLayoutResource(C0017R$layout.controls_management_editing);
        viewStub.inflate();
        View requireViewById2 = requireViewById(C0015R$id.title);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById<TextView>(R.id.title)");
        TextView textView = (TextView) requireViewById2;
        CharSequence charSequence = this.structure;
        if (charSequence != null) {
            textView.setText(charSequence);
            CharSequence charSequence2 = this.structure;
            if (charSequence2 != null) {
                setTitle(charSequence2);
                View requireViewById3 = requireViewById(C0015R$id.subtitle);
                TextView textView2 = (TextView) requireViewById3;
                textView2.setText(SUBTITLE_ID);
                Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<TextView…xt(SUBTITLE_ID)\n        }");
                this.subtitle = textView2;
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("structure");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("structure");
        throw null;
    }

    private final void bindButtons() {
        ViewGroup viewGroup = (ViewGroup) requireViewById(C0015R$id.controls_management_root);
        View requireViewById = requireViewById(C0015R$id.done);
        Button button = (Button) requireViewById;
        button.setEnabled(false);
        button.setText(C0021R$string.save);
        button.setOnClickListener(new ControlsEditingActivity$bindButtons$$inlined$apply$lambda$1(this));
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<Button>(…)\n            }\n        }");
        this.saveButton = requireViewById;
    }

    /* access modifiers changed from: public */
    private final void saveFavorites() {
        ControlsControllerImpl controlsControllerImpl = this.controller;
        ComponentName componentName = this.component;
        if (componentName != null) {
            CharSequence charSequence = this.structure;
            if (charSequence != null) {
                FavoritesModel favoritesModel = this.model;
                if (favoritesModel != null) {
                    controlsControllerImpl.replaceFavoritesForStructure(new StructureInfo(componentName, charSequence, favoritesModel.getFavorites()));
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("model");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("structure");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("component");
            throw null;
        }
    }

    private final void setUpList() {
        ControlsControllerImpl controlsControllerImpl = this.controller;
        ComponentName componentName = this.component;
        if (componentName != null) {
            CharSequence charSequence = this.structure;
            if (charSequence != null) {
                List<ControlInfo> favoritesForStructure = controlsControllerImpl.getFavoritesForStructure(componentName, charSequence);
                ComponentName componentName2 = this.component;
                if (componentName2 != null) {
                    this.model = new FavoritesModel(componentName2, favoritesForStructure, this.favoritesModelCallback);
                    float f = getResources().getFloat(C0012R$dimen.control_card_elevation);
                    RecyclerView recyclerView = (RecyclerView) requireViewById(C0015R$id.list);
                    Intrinsics.checkExpressionValueIsNotNull(recyclerView, "recyclerView");
                    recyclerView.setAlpha(0.0f);
                    ControlAdapter controlAdapter = new ControlAdapter(f);
                    controlAdapter.registerAdapterDataObserver(new ControlsEditingActivity$setUpList$$inlined$apply$lambda$1(recyclerView));
                    int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.controls_card_margin);
                    MarginItemDecorator marginItemDecorator = new MarginItemDecorator(dimensionPixelSize, dimensionPixelSize);
                    recyclerView.setAdapter(controlAdapter);
                    ControlsEditingActivity$setUpList$$inlined$apply$lambda$2 controlsEditingActivity$setUpList$$inlined$apply$lambda$2 = new ControlsEditingActivity$setUpList$$inlined$apply$lambda$2(recyclerView.getContext(), 2, controlAdapter, recyclerView, marginItemDecorator);
                    controlsEditingActivity$setUpList$$inlined$apply$lambda$2.setSpanSizeLookup(controlAdapter.getSpanSizeLookup());
                    recyclerView.setLayoutManager(controlsEditingActivity$setUpList$$inlined$apply$lambda$2);
                    recyclerView.addItemDecoration(marginItemDecorator);
                    FavoritesModel favoritesModel = this.model;
                    if (favoritesModel != null) {
                        controlAdapter.changeModel(favoritesModel);
                        FavoritesModel favoritesModel2 = this.model;
                        if (favoritesModel2 != null) {
                            favoritesModel2.attachAdapter(controlAdapter);
                            FavoritesModel favoritesModel3 = this.model;
                            if (favoritesModel3 != null) {
                                new ItemTouchHelper(favoritesModel3.getItemTouchHelperCallback()).attachToRecyclerView(recyclerView);
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("model");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("model");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("model");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("component");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("structure");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("component");
            throw null;
        }
    }

    @Override // com.android.systemui.util.LifecycleActivity
    public void onDestroy() {
        this.currentUserTracker.stopTracking();
        super.onDestroy();
    }
}
