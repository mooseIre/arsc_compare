package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import android.widget.ImageView;
import com.android.systemui.statusbar.notification.stack.PeopleHubView;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHubView.kt */
final class PeopleHubView$onFinishInflate$1 extends Lambda implements Function1<Integer, PeopleHubView.PersonDataListenerImpl> {
    final /* synthetic */ PeopleHubView this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    PeopleHubView$onFinishInflate$1(PeopleHubView peopleHubView) {
        super(1);
        this.this$0 = peopleHubView;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ PeopleHubView.PersonDataListenerImpl invoke(Integer num) {
        return invoke(num.intValue());
    }

    @Nullable
    public final PeopleHubView.PersonDataListenerImpl invoke(int i) {
        View childAt = PeopleHubView.access$getContents$p(this.this$0).getChildAt(i);
        if (!(childAt instanceof ImageView)) {
            childAt = null;
        }
        ImageView imageView = (ImageView) childAt;
        if (imageView != null) {
            return new PeopleHubView.PersonDataListenerImpl(this.this$0, imageView);
        }
        return null;
    }
}
