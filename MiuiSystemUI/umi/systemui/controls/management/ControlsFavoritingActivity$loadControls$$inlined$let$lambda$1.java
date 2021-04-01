package com.android.systemui.controls.management;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Resources;
import android.widget.TextView;
import com.android.systemui.C0021R$string;
import com.android.systemui.controls.ControlStatus;
import com.android.systemui.controls.TooltipManager;
import com.android.systemui.controls.controller.ControlsController;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1<T> implements Consumer<ControlsController.LoadData> {
    final /* synthetic */ CharSequence $emptyZoneString;
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1(CharSequence charSequence, ControlsFavoritingActivity controlsFavoritingActivity) {
        this.$emptyZoneString = charSequence;
        this.this$0 = controlsFavoritingActivity;
    }

    public final void accept(@NotNull ControlsController.LoadData loadData) {
        Intrinsics.checkParameterIsNotNull(loadData, "data");
        List<ControlStatus> allControls = loadData.getAllControls();
        List<String> favoritesIds = loadData.getFavoritesIds();
        final boolean errorOnLoad = loadData.getErrorOnLoad();
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (T t : allControls) {
            Object structure = t.getControl().getStructure();
            if (structure == null) {
                structure = "";
            }
            Object obj = linkedHashMap.get(structure);
            if (obj == null) {
                obj = new ArrayList();
                linkedHashMap.put(structure, obj);
            }
            ((List) obj).add(t);
        }
        ControlsFavoritingActivity controlsFavoritingActivity = this.this$0;
        ArrayList arrayList = new ArrayList(linkedHashMap.size());
        for (Map.Entry entry : linkedHashMap.entrySet()) {
            CharSequence charSequence = this.$emptyZoneString;
            Intrinsics.checkExpressionValueIsNotNull(charSequence, "emptyZoneString");
            arrayList.add(new StructureContainer((CharSequence) entry.getKey(), new AllModel((List) entry.getValue(), favoritesIds, charSequence, this.this$0.controlsModelCallback)));
        }
        controlsFavoritingActivity.listOfStructures = CollectionsKt___CollectionsKt.sortedWith(arrayList, ControlsFavoritingActivity.access$getComparator$p(this.this$0));
        Iterator it = this.this$0.listOfStructures.iterator();
        final int i = 0;
        while (true) {
            if (!it.hasNext()) {
                i = -1;
                break;
            } else if (Intrinsics.areEqual(((StructureContainer) it.next()).getStructureName(), this.this$0.structureExtra)) {
                break;
            } else {
                i++;
            }
        }
        if (i == -1) {
            i = 0;
        }
        if (this.this$0.getIntent().getBooleanExtra("extra_single_structure", false)) {
            ControlsFavoritingActivity controlsFavoritingActivity2 = this.this$0;
            controlsFavoritingActivity2.listOfStructures = CollectionsKt__CollectionsJVMKt.listOf(controlsFavoritingActivity2.listOfStructures.get(i));
        }
        this.this$0.executor.execute(new Runnable(this) {
            /* class com.android.systemui.controls.management.ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.AnonymousClass1 */
            final /* synthetic */ ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1 this$0;

            {
                this.this$0 = r1;
            }

            public final void run() {
                ControlsFavoritingActivity.access$getStructurePager$p(this.this$0.this$0).setAdapter(new StructureAdapter(this.this$0.this$0.listOfStructures));
                ControlsFavoritingActivity.access$getStructurePager$p(this.this$0.this$0).setCurrentItem(i);
                int i = 0;
                if (errorOnLoad) {
                    TextView access$getStatusText$p = ControlsFavoritingActivity.access$getStatusText$p(this.this$0.this$0);
                    Resources resources = this.this$0.this$0.getResources();
                    int i2 = C0021R$string.controls_favorite_load_error;
                    Object[] objArr = new Object[1];
                    Object obj = this.this$0.this$0.appName;
                    if (obj == null) {
                        obj = "";
                    }
                    objArr[0] = obj;
                    access$getStatusText$p.setText(resources.getString(i2, objArr));
                    ControlsFavoritingActivity.access$getSubtitleView$p(this.this$0.this$0).setVisibility(8);
                } else if (this.this$0.this$0.listOfStructures.isEmpty()) {
                    ControlsFavoritingActivity.access$getStatusText$p(this.this$0.this$0).setText(this.this$0.this$0.getResources().getString(C0021R$string.controls_favorite_load_none));
                    ControlsFavoritingActivity.access$getSubtitleView$p(this.this$0.this$0).setVisibility(8);
                } else {
                    ControlsFavoritingActivity.access$getStatusText$p(this.this$0.this$0).setVisibility(8);
                    ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0).setNumPages(this.this$0.this$0.listOfStructures.size());
                    ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0).setLocation(0.0f);
                    ManagementPageIndicator access$getPageIndicator$p = ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0);
                    if (this.this$0.this$0.listOfStructures.size() <= 1) {
                        i = 4;
                    }
                    access$getPageIndicator$p.setVisibility(i);
                    Animator enterAnimation = ControlsAnimations.INSTANCE.enterAnimation(ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0));
                    enterAnimation.addListener(new AnimatorListenerAdapter(this) {
                        /* class com.android.systemui.controls.management.ControlsFavoritingActivity$loadControls$$inlined$let$lambda$1.AnonymousClass1.AnonymousClass1 */
                        final /* synthetic */ AnonymousClass1 this$0;

                        {
                            this.this$0 = r1;
                        }

                        public void onAnimationEnd(@Nullable Animator animator) {
                            if (ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0.this$0).getVisibility() == 0 && this.this$0.this$0.this$0.mTooltipManager != null) {
                                int[] iArr = new int[2];
                                ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0.this$0).getLocationOnScreen(iArr);
                                int width = iArr[0] + (ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0.this$0).getWidth() / 2);
                                int height = iArr[1] + ControlsFavoritingActivity.access$getPageIndicator$p(this.this$0.this$0.this$0).getHeight();
                                TooltipManager tooltipManager = this.this$0.this$0.this$0.mTooltipManager;
                                if (tooltipManager != null) {
                                    tooltipManager.show(C0021R$string.controls_structure_tooltip, width, height);
                                }
                            }
                        }
                    });
                    enterAnimation.start();
                    ControlsAnimations.INSTANCE.enterAnimation(ControlsFavoritingActivity.access$getStructurePager$p(this.this$0.this$0)).start();
                }
            }
        });
    }
}
