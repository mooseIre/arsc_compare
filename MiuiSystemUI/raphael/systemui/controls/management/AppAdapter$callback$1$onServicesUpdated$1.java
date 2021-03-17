package com.android.systemui.controls.management;

import android.content.res.Configuration;
import java.text.Collator;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;

/* compiled from: AppAdapter.kt */
final class AppAdapter$callback$1$onServicesUpdated$1 implements Runnable {
    final /* synthetic */ List $serviceInfos;
    final /* synthetic */ AppAdapter$callback$1 this$0;

    AppAdapter$callback$1$onServicesUpdated$1(AppAdapter$callback$1 appAdapter$callback$1, List list) {
        this.this$0 = appAdapter$callback$1;
        this.$serviceInfos = list;
    }

    public final void run() {
        Configuration configuration = this.this$0.this$0.resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
        Collator instance = Collator.getInstance(configuration.getLocales().get(0));
        Intrinsics.checkExpressionValueIsNotNull(instance, "collator");
        this.this$0.this$0.listOfServices = CollectionsKt___CollectionsKt.sortedWith(this.$serviceInfos, new AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1(instance));
        this.this$0.$uiExecutor.execute(new AppAdapter$sam$java_lang_Runnable$0(new Function0<Unit>(this.this$0.this$0) {
            public final String getName() {
                return "notifyDataSetChanged";
            }

            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(AppAdapter.class);
            }

            public final String getSignature() {
                return "notifyDataSetChanged()V";
            }

            public final void invoke() {
                ((AppAdapter) this.receiver).notifyDataSetChanged();
            }
        }));
    }
}
