package com.android.systemui.statusbar.phone;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: KeyOrderObserver.kt */
public final class KeyOrderObserver {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final HashSet<Integer> sKeyIdSet = new HashSet<>();
    private Function0<Unit> keyOrderCallback;
    private final KeyOrderObserver$observer$1 observer = new KeyOrderObserver$observer$1(this, null);

    public KeyOrderObserver() {
        sKeyIdSet.add(Integer.valueOf(C0015R$id.menu));
        sKeyIdSet.add(Integer.valueOf(C0015R$id.home));
        sKeyIdSet.add(Integer.valueOf(C0015R$id.recent_apps));
        sKeyIdSet.add(Integer.valueOf(C0015R$id.back));
    }

    public static final /* synthetic */ Function0 access$getKeyOrderCallback$p(KeyOrderObserver keyOrderObserver) {
        Function0<Unit> function0 = keyOrderObserver.keyOrderCallback;
        if (function0 != null) {
            return function0;
        }
        Intrinsics.throwUninitializedPropertyAccessException("keyOrderCallback");
        throw null;
    }

    public final void register(@NotNull ContentResolver contentResolver, @NotNull Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(contentResolver, "contentResolver");
        Intrinsics.checkParameterIsNotNull(function0, "c");
        this.keyOrderCallback = function0;
        contentResolver.registerContentObserver(Settings.System.getUriFor("screen_key_order"), false, this.observer, -1);
    }

    public final void unregister(@NotNull ContentResolver contentResolver) {
        Intrinsics.checkParameterIsNotNull(contentResolver, "contentResolver");
        contentResolver.unregisterContentObserver(this.observer);
    }

    /* compiled from: KeyOrderObserver.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private final List<Integer> getScreenKeyOrder(ContentResolver contentResolver) {
            ArrayList arrayList = new ArrayList();
            String stringForUser = Settings.System.getStringForUser(contentResolver, "screen_key_order", -2);
            if (!TextUtils.isEmpty(stringForUser)) {
                Intrinsics.checkExpressionValueIsNotNull(stringForUser, "keyList");
                List<String> list = StringsKt__StringsKt.split$default(stringForUser, new String[]{" "}, false, 0, 6, null);
                if (list != null) {
                    for (String str : list) {
                        if (str != null) {
                            try {
                                if (!TextUtils.isEmpty(StringsKt__StringsKt.trim(str).toString())) {
                                    Integer valueOf = Integer.valueOf(str);
                                    if (MiuiSettings.System.screenKeys.contains(valueOf)) {
                                        arrayList.add(valueOf);
                                    }
                                }
                            } catch (Exception unused) {
                                arrayList.clear();
                            }
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type kotlin.CharSequence");
                        }
                    }
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
            Iterator it = MiuiSettings.System.screenKeys.iterator();
            while (it.hasNext()) {
                Integer num = (Integer) it.next();
                if (!arrayList.contains(num)) {
                    arrayList.add(num);
                }
            }
            return arrayList;
        }

        public final boolean isReversed(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            ContentResolver contentResolver = context.getContentResolver();
            Intrinsics.checkExpressionValueIsNotNull(contentResolver, "context.contentResolver");
            return getScreenKeyOrder(contentResolver).get(0).intValue() == 3;
        }

        public final int getDefaultLayoutResource(@NotNull Context context) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            return isReversed(context) ? C0021R$string.config_navBarLayout_reverse : C0021R$string.config_navBarLayout;
        }
    }
}
