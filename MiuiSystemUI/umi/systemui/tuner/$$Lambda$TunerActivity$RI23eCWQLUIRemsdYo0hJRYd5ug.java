package com.android.systemui.tuner;

import com.android.systemui.fragments.FragmentService;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.tuner.-$$Lambda$TunerActivity$RI23eCWQLUIRemsdYo0hJRYd5ug  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TunerActivity$RI23eCWQLUIRemsdYo0hJRYd5ug implements Consumer {
    public static final /* synthetic */ $$Lambda$TunerActivity$RI23eCWQLUIRemsdYo0hJRYd5ug INSTANCE = new $$Lambda$TunerActivity$RI23eCWQLUIRemsdYo0hJRYd5ug();

    private /* synthetic */ $$Lambda$TunerActivity$RI23eCWQLUIRemsdYo0hJRYd5ug() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((FragmentService) obj).destroyAll();
    }
}
