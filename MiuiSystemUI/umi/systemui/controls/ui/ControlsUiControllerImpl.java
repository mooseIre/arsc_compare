package com.android.systemui.controls.ui;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Space;
import android.widget.TextView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.StructureInfo;
import com.android.systemui.controls.management.ControlsEditingActivity;
import com.android.systemui.controls.management.ControlsFavoritingActivity;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.management.ControlsProviderSelectorActivity;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsJVMKt;
import kotlin.collections.MapsKt__MapsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import kotlin.ranges.RangesKt___RangesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl implements ControlsUiController {
    private static final ComponentName EMPTY_COMPONENT;
    private static final StructureInfo EMPTY_STRUCTURE;
    private final ActivityStarter activityStarter;
    private List<StructureInfo> allStructures;
    @NotNull
    private final DelayableExecutor bgExecutor;
    private final Collator collator;
    @NotNull
    private final Context context;
    @NotNull
    private final ControlActionCoordinator controlActionCoordinator;
    private final Map<ControlKey, ControlViewHolder> controlViewsById = new LinkedHashMap();
    private final Map<ControlKey, ControlWithState> controlsById = new LinkedHashMap();
    @NotNull
    private final Lazy<ControlsController> controlsController;
    @NotNull
    private final Lazy<ControlsListingController> controlsListingController;
    private Runnable dismissGlobalActions;
    private boolean hidden = true;
    private ControlsListingController.ControlsListingCallback listingCallback;
    private final Comparator<SelectionItem> localeComparator;
    private final Consumer<Boolean> onSeedingComplete;
    private ViewGroup parent;
    private ListPopupWindow popup;
    private final ContextThemeWrapper popupThemedContext = new ContextThemeWrapper(this.context, C0022R$style.Control_ListPopupWindow);
    private StructureInfo selectedStructure = EMPTY_STRUCTURE;
    private final ShadeController shadeController;
    @NotNull
    private final SharedPreferences sharedPreferences;
    @NotNull
    private final DelayableExecutor uiExecutor;

    public ControlsUiControllerImpl(@NotNull Lazy<ControlsController> lazy, @NotNull Context context2, @NotNull DelayableExecutor delayableExecutor, @NotNull DelayableExecutor delayableExecutor2, @NotNull Lazy<ControlsListingController> lazy2, @NotNull SharedPreferences sharedPreferences2, @NotNull ControlActionCoordinator controlActionCoordinator2, @NotNull ActivityStarter activityStarter2, @NotNull ShadeController shadeController2) {
        Intrinsics.checkParameterIsNotNull(lazy, "controlsController");
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(delayableExecutor2, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(lazy2, "controlsListingController");
        Intrinsics.checkParameterIsNotNull(sharedPreferences2, "sharedPreferences");
        Intrinsics.checkParameterIsNotNull(controlActionCoordinator2, "controlActionCoordinator");
        Intrinsics.checkParameterIsNotNull(activityStarter2, "activityStarter");
        Intrinsics.checkParameterIsNotNull(shadeController2, "shadeController");
        this.controlsController = lazy;
        this.context = context2;
        this.uiExecutor = delayableExecutor;
        this.bgExecutor = delayableExecutor2;
        this.controlsListingController = lazy2;
        this.sharedPreferences = sharedPreferences2;
        this.controlActionCoordinator = controlActionCoordinator2;
        this.activityStarter = activityStarter2;
        this.shadeController = shadeController2;
        Resources resources = this.context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "context.resources.configuration");
        Collator instance = Collator.getInstance(configuration.getLocales().get(0));
        this.collator = instance;
        Intrinsics.checkExpressionValueIsNotNull(instance, "collator");
        this.localeComparator = new ControlsUiControllerImpl$$special$$inlined$compareBy$1(instance);
        this.onSeedingComplete = new ControlsUiControllerImpl$onSeedingComplete$1(this);
    }

    public static final /* synthetic */ Runnable access$getDismissGlobalActions$p(ControlsUiControllerImpl controlsUiControllerImpl) {
        Runnable runnable = controlsUiControllerImpl.dismissGlobalActions;
        if (runnable != null) {
            return runnable;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        throw null;
    }

    public static final /* synthetic */ ViewGroup access$getParent$p(ControlsUiControllerImpl controlsUiControllerImpl) {
        ViewGroup viewGroup = controlsUiControllerImpl.parent;
        if (viewGroup != null) {
            return viewGroup;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    @NotNull
    public final Lazy<ControlsController> getControlsController() {
        return this.controlsController;
    }

    @NotNull
    public final DelayableExecutor getUiExecutor() {
        return this.uiExecutor;
    }

    static {
        ComponentName componentName = new ComponentName("", "");
        EMPTY_COMPONENT = componentName;
        EMPTY_STRUCTURE = new StructureInfo(componentName, "", new ArrayList());
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public boolean getAvailable() {
        return this.controlsController.get().getAvailable();
    }

    private final ControlsListingController.ControlsListingCallback createCallback(Function1<? super List<SelectionItem>, Unit> function1) {
        return new ControlsUiControllerImpl$createCallback$1(this, function1);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v20, resolved type: java.util.Map<com.android.systemui.controls.ui.ControlKey, com.android.systemui.controls.ui.ControlWithState> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void show(@NotNull ViewGroup viewGroup, @NotNull Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        Intrinsics.checkParameterIsNotNull(runnable, "dismissGlobalActions");
        Log.d("ControlsUiController", "show()");
        this.parent = viewGroup;
        this.dismissGlobalActions = runnable;
        this.hidden = false;
        List<StructureInfo> favorites = this.controlsController.get().getFavorites();
        this.allStructures = favorites;
        if (favorites != null) {
            this.selectedStructure = loadPreference(favorites);
            if (this.controlsController.get().addSeedingFavoritesCallback(this.onSeedingComplete)) {
                this.listingCallback = createCallback(new ControlsUiControllerImpl$show$1(this));
            } else {
                if (this.selectedStructure.getControls().isEmpty()) {
                    List<StructureInfo> list = this.allStructures;
                    if (list == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("allStructures");
                        throw null;
                    } else if (list.size() <= 1) {
                        this.listingCallback = createCallback(new ControlsUiControllerImpl$show$2(this));
                    }
                }
                List<ControlInfo> controls = this.selectedStructure.getControls();
                ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(controls, 10));
                Iterator<T> it = controls.iterator();
                while (it.hasNext()) {
                    arrayList.add(new ControlWithState(this.selectedStructure.getComponentName(), it.next(), null));
                }
                Map<ControlKey, ControlWithState> map = this.controlsById;
                for (Object obj : arrayList) {
                    map.put(new ControlKey(this.selectedStructure.getComponentName(), ((ControlWithState) obj).getCi().getControlId()), obj);
                }
                this.listingCallback = createCallback(new ControlsUiControllerImpl$show$5(this));
                this.controlsController.get().subscribeToFavorites(this.selectedStructure);
            }
            ControlsListingController controlsListingController2 = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController2.addCallback(controlsListingCallback);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("allStructures");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void reload(ViewGroup viewGroup) {
        if (!this.hidden) {
            ControlsListingController controlsListingController2 = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController2.removeCallback(controlsListingCallback);
                this.controlsController.get().unsubscribe();
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(viewGroup, "alpha", 1.0f, 0.0f);
                ofFloat.setInterpolator(new AccelerateInterpolator(1.0f));
                ofFloat.setDuration(200L);
                ofFloat.addListener(new ControlsUiControllerImpl$reload$1(this, viewGroup));
                ofFloat.start();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showSeedingView(List<SelectionItem> list) {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0017R$layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                ((TextView) viewGroup2.requireViewById(C0015R$id.controls_subtitle)).setText(this.context.getResources().getString(C0021R$string.controls_seeding_in_progress));
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("parent");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showInitialSetupView(List<SelectionItem> list) {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0017R$layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                View requireViewById = viewGroup2.requireViewById(C0015R$id.controls_no_favorites_group);
                if (requireViewById != null) {
                    ViewGroup viewGroup3 = (ViewGroup) requireViewById;
                    viewGroup3.setOnClickListener(new ControlsUiControllerImpl$showInitialSetupView$1(this));
                    ViewGroup viewGroup4 = this.parent;
                    if (viewGroup4 != null) {
                        ((TextView) viewGroup4.requireViewById(C0015R$id.controls_subtitle)).setText(this.context.getResources().getString(C0021R$string.quick_controls_subtitle));
                        ViewGroup viewGroup5 = this.parent;
                        if (viewGroup5 != null) {
                            View requireViewById2 = viewGroup5.requireViewById(C0015R$id.controls_icon_row);
                            if (requireViewById2 != null) {
                                ViewGroup viewGroup6 = (ViewGroup) requireViewById2;
                                for (T t : list) {
                                    View inflate = from.inflate(C0017R$layout.controls_icon, viewGroup3, false);
                                    if (inflate != null) {
                                        ImageView imageView = (ImageView) inflate;
                                        imageView.setContentDescription(t.getTitle());
                                        imageView.setImageDrawable(t.getIcon());
                                        viewGroup6.addView(imageView);
                                    } else {
                                        throw new TypeCastException("null cannot be cast to non-null type android.widget.ImageView");
                                    }
                                }
                                return;
                            }
                            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("parent");
                        throw null;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("parent");
                    throw null;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
            }
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void startFavoritingActivity(Context context2, StructureInfo structureInfo) {
        startTargetedActivity(context2, structureInfo, ControlsFavoritingActivity.class);
    }

    /* access modifiers changed from: private */
    public final void startEditingActivity(Context context2, StructureInfo structureInfo) {
        startTargetedActivity(context2, structureInfo, ControlsEditingActivity.class);
    }

    private final void startTargetedActivity(Context context2, StructureInfo structureInfo, Class<?> cls) {
        Intent intent = new Intent(context2, cls);
        intent.addFlags(335544320);
        putIntentExtras(intent, structureInfo);
        startActivity(context2, intent);
    }

    private final SelectionItem findSelectionItem(StructureInfo structureInfo, List<SelectionItem> list) {
        T t;
        boolean z;
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                t = null;
                break;
            }
            t = it.next();
            T t2 = t;
            if (!Intrinsics.areEqual(t2.getComponentName(), structureInfo.getComponentName()) || !Intrinsics.areEqual(t2.getStructure(), structureInfo.getStructure())) {
                z = false;
                continue;
            } else {
                z = true;
                continue;
            }
            if (z) {
                break;
            }
        }
        return t;
    }

    private final void putIntentExtras(Intent intent, StructureInfo structureInfo) {
        intent.putExtra("extra_app_label", this.controlsListingController.get().getAppLabel(structureInfo.getComponentName()));
        intent.putExtra("extra_structure", structureInfo.getStructure());
        intent.putExtra("android.intent.extra.COMPONENT_NAME", structureInfo.getComponentName());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void startProviderSelectorActivity(Context context2) {
        Intent intent = new Intent(context2, ControlsProviderSelectorActivity.class);
        intent.addFlags(335544320);
        startActivity(context2, intent);
    }

    private final void startActivity(Context context2, Intent intent) {
        intent.putExtra("extra_animate", true);
        Runnable runnable = this.dismissGlobalActions;
        if (runnable != null) {
            runnable.run();
            this.activityStarter.dismissKeyguardThenExecute(new ControlsUiControllerImpl$startActivity$1(this, context2, intent), null, true);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        throw null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showControlsView(List<SelectionItem> list) {
        this.controlViewsById.clear();
        createListView();
        createDropDown(list);
        createMenu();
    }

    private final void createMenu() {
        String[] strArr = {this.context.getResources().getString(C0021R$string.controls_menu_add), this.context.getResources().getString(C0021R$string.controls_menu_edit)};
        Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        ref$ObjectRef.element = (T) new ArrayAdapter(this.context, C0017R$layout.controls_more_item, strArr);
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            ImageView imageView = (ImageView) viewGroup.requireViewById(C0015R$id.controls_more);
            imageView.setOnClickListener(new ControlsUiControllerImpl$createMenu$1(this, imageView, ref$ObjectRef));
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    private final void createListView() {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0017R$layout.controls_with_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            int findMaxColumns = findMaxColumns();
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                View requireViewById = viewGroup2.requireViewById(C0015R$id.global_actions_controls_list);
                if (requireViewById != null) {
                    ViewGroup viewGroup3 = (ViewGroup) requireViewById;
                    Intrinsics.checkExpressionValueIsNotNull(from, "inflater");
                    ViewGroup createRow = createRow(from, viewGroup3);
                    Iterator<T> it = this.selectedStructure.getControls().iterator();
                    while (it.hasNext()) {
                        ControlKey controlKey = new ControlKey(this.selectedStructure.getComponentName(), it.next().getControlId());
                        ControlWithState controlWithState = this.controlsById.get(controlKey);
                        if (controlWithState != null) {
                            if (createRow.getChildCount() == findMaxColumns) {
                                createRow = createRow(from, viewGroup3);
                            }
                            View inflate = from.inflate(C0017R$layout.controls_base_item, createRow, false);
                            if (inflate != null) {
                                ViewGroup viewGroup4 = (ViewGroup) inflate;
                                createRow.addView(viewGroup4);
                                ControlsController controlsController2 = this.controlsController.get();
                                Intrinsics.checkExpressionValueIsNotNull(controlsController2, "controlsController.get()");
                                ControlViewHolder controlViewHolder = new ControlViewHolder(viewGroup4, controlsController2, this.uiExecutor, this.bgExecutor, this.controlActionCoordinator);
                                controlViewHolder.bindData(controlWithState);
                                this.controlViewsById.put(controlKey, controlViewHolder);
                            } else {
                                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
                            }
                        }
                    }
                    int size = this.selectedStructure.getControls().size() % findMaxColumns;
                    for (int i2 = size == 0 ? 0 : findMaxColumns - size; i2 > 0; i2--) {
                        createRow.addView(new Space(this.context), new LinearLayout.LayoutParams(0, 0, 1.0f));
                    }
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
            }
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    private final int findMaxColumns() {
        int i;
        Resources resources = this.context.getResources();
        int integer = resources.getInteger(C0016R$integer.controls_max_columns);
        int integer2 = resources.getInteger(C0016R$integer.controls_max_columns_adjust_below_width_dp);
        TypedValue typedValue = new TypedValue();
        boolean z = true;
        resources.getValue(C0012R$dimen.controls_max_columns_adjust_above_font_scale, typedValue, true);
        float f = typedValue.getFloat();
        Intrinsics.checkExpressionValueIsNotNull(resources, "res");
        Configuration configuration = resources.getConfiguration();
        if (configuration.orientation != 1) {
            z = false;
        }
        return (!z || (i = configuration.screenWidthDp) == 0 || i > integer2 || configuration.fontScale < f) ? integer : integer - 1;
    }

    private final StructureInfo loadPreference(List<StructureInfo> list) {
        ComponentName componentName;
        boolean z;
        if (list.isEmpty()) {
            return EMPTY_STRUCTURE;
        }
        T t = null;
        String string = this.sharedPreferences.getString("controls_component", null);
        if (string == null || (componentName = ComponentName.unflattenFromString(string)) == null) {
            componentName = EMPTY_COMPONENT;
        }
        String string2 = this.sharedPreferences.getString("controls_structure", "");
        Iterator<T> it = list.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            T next = it.next();
            T t2 = next;
            if (!Intrinsics.areEqual(componentName, t2.getComponentName()) || !Intrinsics.areEqual(string2, t2.getStructure())) {
                z = false;
                continue;
            } else {
                z = true;
                continue;
            }
            if (z) {
                t = next;
                break;
            }
        }
        T t3 = t;
        return t3 != null ? t3 : list.get(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void updatePreferences(StructureInfo structureInfo) {
        if (!Intrinsics.areEqual(structureInfo, EMPTY_STRUCTURE)) {
            this.sharedPreferences.edit().putString("controls_component", structureInfo.getComponentName().flattenToString()).putString("controls_structure", structureInfo.getStructure().toString()).commit();
        }
    }

    /* access modifiers changed from: private */
    public final void switchAppOrStructure(SelectionItem selectionItem) {
        boolean z;
        List<StructureInfo> list = this.allStructures;
        if (list != null) {
            for (T t : list) {
                if (!Intrinsics.areEqual(t.getStructure(), selectionItem.getStructure()) || !Intrinsics.areEqual(t.getComponentName(), selectionItem.getComponentName())) {
                    z = false;
                    continue;
                } else {
                    z = true;
                    continue;
                }
                if (z) {
                    if (!Intrinsics.areEqual(t, this.selectedStructure)) {
                        this.selectedStructure = t;
                        updatePreferences(t);
                        ViewGroup viewGroup = this.parent;
                        if (viewGroup != null) {
                            reload(viewGroup);
                            return;
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("parent");
                            throw null;
                        }
                    } else {
                        return;
                    }
                }
            }
            throw new NoSuchElementException("Collection contains no element matching the predicate.");
        }
        Intrinsics.throwUninitializedPropertyAccessException("allStructures");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void closeDialogs(boolean z) {
        if (z) {
            ListPopupWindow listPopupWindow = this.popup;
            if (listPopupWindow != null) {
                listPopupWindow.dismissImmediate();
            }
        } else {
            ListPopupWindow listPopupWindow2 = this.popup;
            if (listPopupWindow2 != null) {
                listPopupWindow2.dismiss();
            }
        }
        this.popup = null;
        for (Map.Entry<ControlKey, ControlViewHolder> entry : this.controlViewsById.entrySet()) {
            entry.getValue().dismiss();
        }
        this.controlActionCoordinator.closeDialogs();
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void hide() {
        this.hidden = true;
        closeDialogs(true);
        this.controlsController.get().unsubscribe();
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            viewGroup.removeAllViews();
            this.controlsById.clear();
            this.controlViewsById.clear();
            ControlsListingController controlsListingController2 = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController2.removeCallback(controlsListingCallback);
                RenderInfo.Companion.clearCache();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void onActionResponse(@NotNull ComponentName componentName, @NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.uiExecutor.execute(new ControlsUiControllerImpl$onActionResponse$1(this, new ControlKey(componentName, str), i));
    }

    private final ViewGroup createRow(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        View inflate = layoutInflater.inflate(C0017R$layout.controls_row, viewGroup, false);
        if (inflate != null) {
            ViewGroup viewGroup2 = (ViewGroup) inflate;
            viewGroup.addView(viewGroup2);
            return viewGroup2;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    private final void createDropDown(List<SelectionItem> list) {
        for (T t : list) {
            RenderInfo.Companion.registerComponentIcon(t.getComponentName(), t.getIcon());
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt___RangesKt.coerceAtLeast(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10)), 16));
        for (T t2 : list) {
            linkedHashMap.put(t2.getComponentName(), t2);
        }
        ArrayList arrayList = new ArrayList();
        List<StructureInfo> list2 = this.allStructures;
        if (list2 != null) {
            for (T t3 : list2) {
                SelectionItem selectionItem = (SelectionItem) linkedHashMap.get(t3.getComponentName());
                SelectionItem copy$default = selectionItem != null ? SelectionItem.copy$default(selectionItem, null, t3.getStructure(), null, null, 13, null) : null;
                if (copy$default != null) {
                    arrayList.add(copy$default);
                }
            }
            CollectionsKt__MutableCollectionsJVMKt.sortWith(arrayList, this.localeComparator);
            SelectionItem findSelectionItem = findSelectionItem(this.selectedStructure, arrayList);
            if (findSelectionItem == null) {
                findSelectionItem = list.get(0);
            }
            Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
            T t4 = (T) new ItemAdapter(this.context, C0017R$layout.controls_spinner_item);
            t4.addAll(arrayList);
            ref$ObjectRef.element = t4;
            ViewGroup viewGroup = this.parent;
            if (viewGroup != null) {
                TextView textView = (TextView) viewGroup.requireViewById(C0015R$id.app_or_structure_spinner);
                textView.setText(findSelectionItem.getTitle());
                Drawable background = textView.getBackground();
                if (background != null) {
                    Drawable drawable = ((LayerDrawable) background).getDrawable(0);
                    Context context2 = textView.getContext();
                    Intrinsics.checkExpressionValueIsNotNull(context2, "context");
                    drawable.setTint(context2.getResources().getColor(C0011R$color.control_spinner_dropdown, null));
                    if (arrayList.size() == 1) {
                        textView.setBackground(null);
                        return;
                    }
                    ViewGroup viewGroup2 = this.parent;
                    if (viewGroup2 != null) {
                        ViewGroup viewGroup3 = (ViewGroup) viewGroup2.requireViewById(C0015R$id.controls_header);
                        viewGroup3.setOnClickListener(new ControlsUiControllerImpl$createDropDown$3(this, viewGroup3, ref$ObjectRef));
                        return;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("parent");
                    throw null;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
            }
            Intrinsics.throwUninitializedPropertyAccessException("parent");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("allStructures");
        throw null;
    }

    @Override // com.android.systemui.controls.ui.ControlsUiController
    public void onRefreshState(@NotNull ComponentName componentName, @NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(list, "controls");
        for (T t : list) {
            Map<ControlKey, ControlWithState> map = this.controlsById;
            String controlId = t.getControlId();
            Intrinsics.checkExpressionValueIsNotNull(controlId, "c.getControlId()");
            ControlWithState controlWithState = map.get(new ControlKey(componentName, controlId));
            if (controlWithState != null) {
                Log.d("ControlsUiController", "onRefreshState() for id: " + t.getControlId());
                ControlWithState controlWithState2 = new ControlWithState(componentName, controlWithState.getCi(), t);
                String controlId2 = t.getControlId();
                Intrinsics.checkExpressionValueIsNotNull(controlId2, "c.getControlId()");
                ControlKey controlKey = new ControlKey(componentName, controlId2);
                this.controlsById.put(controlKey, controlWithState2);
                this.uiExecutor.execute(new ControlsUiControllerImpl$onRefreshState$$inlined$forEach$lambda$1(controlKey, controlWithState2, t, this, componentName));
            }
        }
    }
}
