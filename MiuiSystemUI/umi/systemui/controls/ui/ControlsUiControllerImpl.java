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
import com.android.systemui.C0008R$color;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
import com.android.systemui.C0019R$style;
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
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl implements ControlsUiController {
    private static final ComponentName EMPTY_COMPONENT;
    /* access modifiers changed from: private */
    public static final StructureInfo EMPTY_STRUCTURE;
    private final ActivityStarter activityStarter;
    private List<StructureInfo> allStructures;
    @NotNull
    private final DelayableExecutor bgExecutor;
    private final Collator collator;
    @NotNull
    private final Context context;
    @NotNull
    private final ControlActionCoordinator controlActionCoordinator;
    /* access modifiers changed from: private */
    public final Map<ControlKey, ControlViewHolder> controlViewsById = new LinkedHashMap();
    /* access modifiers changed from: private */
    public final Map<ControlKey, ControlWithState> controlsById = new LinkedHashMap();
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
    /* access modifiers changed from: private */
    public ListPopupWindow popup;
    /* access modifiers changed from: private */
    public final ContextThemeWrapper popupThemedContext = new ContextThemeWrapper(this.context, C0019R$style.Control_ListPopupWindow);
    /* access modifiers changed from: private */
    public StructureInfo selectedStructure = EMPTY_STRUCTURE;
    /* access modifiers changed from: private */
    public final ShadeController shadeController;
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

    public boolean getAvailable() {
        return this.controlsController.get().getAvailable();
    }

    private final ControlsListingController.ControlsListingCallback createCallback(Function1<? super List<SelectionItem>, Unit> function1) {
        return new ControlsUiControllerImpl$createCallback$1(this, function1);
    }

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
                for (ControlInfo controlWithState : controls) {
                    arrayList.add(new ControlWithState(this.selectedStructure.getComponentName(), controlWithState, (Control) null));
                }
                Map<ControlKey, ControlWithState> map = this.controlsById;
                for (Object next : arrayList) {
                    map.put(new ControlKey(this.selectedStructure.getComponentName(), ((ControlWithState) next).getCi().getControlId()), next);
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
    public final void reload(ViewGroup viewGroup) {
        if (!this.hidden) {
            ControlsListingController controlsListingController2 = this.controlsListingController.get();
            ControlsListingController.ControlsListingCallback controlsListingCallback = this.listingCallback;
            if (controlsListingCallback != null) {
                controlsListingController2.removeCallback(controlsListingCallback);
                this.controlsController.get().unsubscribe();
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(viewGroup, "alpha", new float[]{1.0f, 0.0f});
                ofFloat.setInterpolator(new AccelerateInterpolator(1.0f));
                ofFloat.setDuration(200);
                ofFloat.addListener(new ControlsUiControllerImpl$reload$1(this, viewGroup));
                ofFloat.start();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("listingCallback");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    public final void showSeedingView(List<SelectionItem> list) {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0014R$layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                ((TextView) viewGroup2.requireViewById(C0012R$id.controls_subtitle)).setText(this.context.getResources().getString(C0018R$string.controls_seeding_in_progress));
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
    public final void showInitialSetupView(List<SelectionItem> list) {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0014R$layout.controls_no_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                View requireViewById = viewGroup2.requireViewById(C0012R$id.controls_no_favorites_group);
                if (requireViewById != null) {
                    ViewGroup viewGroup3 = (ViewGroup) requireViewById;
                    viewGroup3.setOnClickListener(new ControlsUiControllerImpl$showInitialSetupView$1(this));
                    ViewGroup viewGroup4 = this.parent;
                    if (viewGroup4 != null) {
                        ((TextView) viewGroup4.requireViewById(C0012R$id.controls_subtitle)).setText(this.context.getResources().getString(C0018R$string.quick_controls_subtitle));
                        ViewGroup viewGroup5 = this.parent;
                        if (viewGroup5 != null) {
                            View requireViewById2 = viewGroup5.requireViewById(C0012R$id.controls_icon_row);
                            if (requireViewById2 != null) {
                                ViewGroup viewGroup6 = (ViewGroup) requireViewById2;
                                for (SelectionItem selectionItem : list) {
                                    View inflate = from.inflate(C0014R$layout.controls_icon, viewGroup3, false);
                                    if (inflate != null) {
                                        ImageView imageView = (ImageView) inflate;
                                        imageView.setContentDescription(selectionItem.getTitle());
                                        imageView.setImageDrawable(selectionItem.getIcon());
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
            SelectionItem selectionItem = (SelectionItem) t;
            if (!Intrinsics.areEqual((Object) selectionItem.getComponentName(), (Object) structureInfo.getComponentName()) || !Intrinsics.areEqual((Object) selectionItem.getStructure(), (Object) structureInfo.getStructure())) {
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
        return (SelectionItem) t;
    }

    private final void putIntentExtras(Intent intent, StructureInfo structureInfo) {
        intent.putExtra("extra_app_label", this.controlsListingController.get().getAppLabel(structureInfo.getComponentName()));
        intent.putExtra("extra_structure", structureInfo.getStructure());
        intent.putExtra("android.intent.extra.COMPONENT_NAME", structureInfo.getComponentName());
    }

    /* access modifiers changed from: private */
    public final void startProviderSelectorActivity(Context context2) {
        Intent intent = new Intent(context2, ControlsProviderSelectorActivity.class);
        intent.addFlags(335544320);
        startActivity(context2, intent);
    }

    private final void startActivity(Context context2, Intent intent) {
        intent.putExtra("extra_animate", true);
        Runnable runnable = this.dismissGlobalActions;
        if (runnable != null) {
            runnable.run();
            this.activityStarter.dismissKeyguardThenExecute(new ControlsUiControllerImpl$startActivity$1(this, context2, intent), (Runnable) null, true);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("dismissGlobalActions");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void showControlsView(List<SelectionItem> list) {
        this.controlViewsById.clear();
        createListView();
        createDropDown(list);
        createMenu();
    }

    private final void createMenu() {
        String[] strArr = {this.context.getResources().getString(C0018R$string.controls_menu_add), this.context.getResources().getString(C0018R$string.controls_menu_edit)};
        Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        ref$ObjectRef.element = new ArrayAdapter(this.context, C0014R$layout.controls_more_item, strArr);
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            ImageView imageView = (ImageView) viewGroup.requireViewById(C0012R$id.controls_more);
            imageView.setOnClickListener(new ControlsUiControllerImpl$createMenu$1(this, imageView, ref$ObjectRef));
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("parent");
        throw null;
    }

    private final void createListView() {
        LayoutInflater from = LayoutInflater.from(this.context);
        int i = C0014R$layout.controls_with_favorites;
        ViewGroup viewGroup = this.parent;
        if (viewGroup != null) {
            from.inflate(i, viewGroup, true);
            int findMaxColumns = findMaxColumns();
            ViewGroup viewGroup2 = this.parent;
            if (viewGroup2 != null) {
                View requireViewById = viewGroup2.requireViewById(C0012R$id.global_actions_controls_list);
                if (requireViewById != null) {
                    ViewGroup viewGroup3 = (ViewGroup) requireViewById;
                    Intrinsics.checkExpressionValueIsNotNull(from, "inflater");
                    ViewGroup createRow = createRow(from, viewGroup3);
                    for (ControlInfo controlId : this.selectedStructure.getControls()) {
                        ControlKey controlKey = new ControlKey(this.selectedStructure.getComponentName(), controlId.getControlId());
                        ControlWithState controlWithState = this.controlsById.get(controlKey);
                        if (controlWithState != null) {
                            if (createRow.getChildCount() == findMaxColumns) {
                                createRow = createRow(from, viewGroup3);
                            }
                            View inflate = from.inflate(C0014R$layout.controls_base_item, createRow, false);
                            if (inflate != null) {
                                ViewGroup viewGroup4 = (ViewGroup) inflate;
                                createRow.addView(viewGroup4);
                                ControlsController controlsController2 = this.controlsController.get();
                                Intrinsics.checkExpressionValueIsNotNull(controlsController2, "controlsController.get()");
                                ControlViewHolder controlViewHolder = new ControlViewHolder(viewGroup4, controlsController2, this.uiExecutor, this.bgExecutor, this.controlActionCoordinator);
                                controlViewHolder.bindData(controlWithState);
                                ControlViewHolder put = this.controlViewsById.put(controlKey, controlViewHolder);
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

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0032, code lost:
        r3 = r5.screenWidthDp;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final int findMaxColumns() {
        /*
            r5 = this;
            android.content.Context r5 = r5.context
            android.content.res.Resources r5 = r5.getResources()
            int r0 = com.android.systemui.C0013R$integer.controls_max_columns
            int r0 = r5.getInteger(r0)
            int r1 = com.android.systemui.C0013R$integer.controls_max_columns_adjust_below_width_dp
            int r1 = r5.getInteger(r1)
            android.util.TypedValue r2 = new android.util.TypedValue
            r2.<init>()
            int r3 = com.android.systemui.C0009R$dimen.controls_max_columns_adjust_above_font_scale
            r4 = 1
            r5.getValue(r3, r2, r4)
            float r2 = r2.getFloat()
            java.lang.String r3 = "res"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r3)
            android.content.res.Configuration r5 = r5.getConfiguration()
            int r3 = r5.orientation
            if (r3 != r4) goto L_0x002f
            goto L_0x0030
        L_0x002f:
            r4 = 0
        L_0x0030:
            if (r4 == 0) goto L_0x0040
            int r3 = r5.screenWidthDp
            if (r3 == 0) goto L_0x0040
            if (r3 > r1) goto L_0x0040
            float r5 = r5.fontScale
            int r5 = (r5 > r2 ? 1 : (r5 == r2 ? 0 : -1))
            if (r5 < 0) goto L_0x0040
            int r0 = r0 + -1
        L_0x0040:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.ui.ControlsUiControllerImpl.findMaxColumns():int");
    }

    private final StructureInfo loadPreference(List<StructureInfo> list) {
        ComponentName componentName;
        boolean z;
        if (list.isEmpty()) {
            return EMPTY_STRUCTURE;
        }
        T t = null;
        String string = this.sharedPreferences.getString("controls_component", (String) null);
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
            StructureInfo structureInfo = (StructureInfo) next;
            if (!Intrinsics.areEqual((Object) componentName, (Object) structureInfo.getComponentName()) || !Intrinsics.areEqual((Object) string2, (Object) structureInfo.getStructure())) {
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
        StructureInfo structureInfo2 = (StructureInfo) t;
        return structureInfo2 != null ? structureInfo2 : list.get(0);
    }

    /* access modifiers changed from: private */
    public final void updatePreferences(StructureInfo structureInfo) {
        if (!Intrinsics.areEqual((Object) structureInfo, (Object) EMPTY_STRUCTURE)) {
            this.sharedPreferences.edit().putString("controls_component", structureInfo.getComponentName().flattenToString()).putString("controls_structure", structureInfo.getStructure().toString()).commit();
        }
    }

    /* access modifiers changed from: private */
    public final void switchAppOrStructure(SelectionItem selectionItem) {
        boolean z;
        List<StructureInfo> list = this.allStructures;
        if (list != null) {
            for (StructureInfo structureInfo : list) {
                if (!Intrinsics.areEqual((Object) structureInfo.getStructure(), (Object) selectionItem.getStructure()) || !Intrinsics.areEqual((Object) structureInfo.getComponentName(), (Object) selectionItem.getComponentName())) {
                    z = false;
                    continue;
                } else {
                    z = true;
                    continue;
                }
                if (z) {
                    if (!Intrinsics.areEqual((Object) structureInfo, (Object) this.selectedStructure)) {
                        this.selectedStructure = structureInfo;
                        updatePreferences(structureInfo);
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
        for (Map.Entry<ControlKey, ControlViewHolder> value : this.controlViewsById.entrySet()) {
            ((ControlViewHolder) value.getValue()).dismiss();
        }
        this.controlActionCoordinator.closeDialogs();
    }

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

    public void onActionResponse(@NotNull ComponentName componentName, @NotNull String str, int i) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.uiExecutor.execute(new ControlsUiControllerImpl$onActionResponse$1(this, new ControlKey(componentName, str), i));
    }

    private final ViewGroup createRow(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        View inflate = layoutInflater.inflate(C0014R$layout.controls_row, viewGroup, false);
        if (inflate != null) {
            ViewGroup viewGroup2 = (ViewGroup) inflate;
            viewGroup.addView(viewGroup2);
            return viewGroup2;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    private final void createDropDown(List<SelectionItem> list) {
        for (SelectionItem selectionItem : list) {
            RenderInfo.Companion.registerComponentIcon(selectionItem.getComponentName(), selectionItem.getIcon());
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(RangesKt___RangesKt.coerceAtLeast(MapsKt__MapsKt.mapCapacity(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10)), 16));
        for (T next : list) {
            linkedHashMap.put(((SelectionItem) next).getComponentName(), next);
        }
        ArrayList arrayList = new ArrayList();
        List<StructureInfo> list2 = this.allStructures;
        if (list2 != null) {
            for (StructureInfo structureInfo : list2) {
                SelectionItem selectionItem2 = (SelectionItem) linkedHashMap.get(structureInfo.getComponentName());
                SelectionItem copy$default = selectionItem2 != null ? SelectionItem.copy$default(selectionItem2, (CharSequence) null, structureInfo.getStructure(), (Drawable) null, (ComponentName) null, 13, (Object) null) : null;
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
            T itemAdapter = new ItemAdapter(this.context, C0014R$layout.controls_spinner_item);
            itemAdapter.addAll(arrayList);
            ref$ObjectRef.element = itemAdapter;
            ViewGroup viewGroup = this.parent;
            if (viewGroup != null) {
                TextView textView = (TextView) viewGroup.requireViewById(C0012R$id.app_or_structure_spinner);
                textView.setText(findSelectionItem.getTitle());
                Drawable background = textView.getBackground();
                if (background != null) {
                    Drawable drawable = ((LayerDrawable) background).getDrawable(0);
                    Context context2 = textView.getContext();
                    Intrinsics.checkExpressionValueIsNotNull(context2, "context");
                    drawable.setTint(context2.getResources().getColor(C0008R$color.control_spinner_dropdown, (Resources.Theme) null));
                    if (arrayList.size() == 1) {
                        textView.setBackground((Drawable) null);
                        return;
                    }
                    ViewGroup viewGroup2 = this.parent;
                    if (viewGroup2 != null) {
                        ViewGroup viewGroup3 = (ViewGroup) viewGroup2.requireViewById(C0012R$id.controls_header);
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

    public void onRefreshState(@NotNull ComponentName componentName, @NotNull List<Control> list) {
        Intrinsics.checkParameterIsNotNull(componentName, "componentName");
        Intrinsics.checkParameterIsNotNull(list, "controls");
        for (Control control : list) {
            Map<ControlKey, ControlWithState> map = this.controlsById;
            String controlId = control.getControlId();
            Intrinsics.checkExpressionValueIsNotNull(controlId, "c.getControlId()");
            ControlWithState controlWithState = map.get(new ControlKey(componentName, controlId));
            if (controlWithState != null) {
                Log.d("ControlsUiController", "onRefreshState() for id: " + control.getControlId());
                ControlWithState controlWithState2 = new ControlWithState(componentName, controlWithState.getCi(), control);
                String controlId2 = control.getControlId();
                Intrinsics.checkExpressionValueIsNotNull(controlId2, "c.getControlId()");
                ControlKey controlKey = new ControlKey(componentName, controlId2);
                this.controlsById.put(controlKey, controlWithState2);
                this.uiExecutor.execute(new ControlsUiControllerImpl$onRefreshState$$inlined$forEach$lambda$1(controlKey, controlWithState2, control, this, componentName));
            }
        }
    }
}
