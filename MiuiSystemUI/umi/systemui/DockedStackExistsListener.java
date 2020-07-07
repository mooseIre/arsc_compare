package com.android.systemui;

import android.view.IDockedStackListener;
import java.util.function.Consumer;

public class DockedStackExistsListener extends IDockedStackListener.Stub {
    public static void register(Consumer<Boolean> consumer) {
    }
}
