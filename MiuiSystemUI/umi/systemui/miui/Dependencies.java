package com.android.systemui.miui;

import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.miui.systemui.annotation.Inject;
import com.miui.systemui.dependencies.BaseResolver;
import com.miui.systemui.dependencies.Resolver;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Dependencies extends BaseResolver {
    private static Dependencies sInstance;

    private Dependencies(List<Resolver> list) {
        for (Resolver addProviders : list) {
            addProviders(addProviders);
        }
    }

    private void addProviders(Resolver resolver) {
        for (Map.Entry next : resolver.getProviders().entrySet()) {
            Map map = getProviders().get(next.getKey());
            if (map == null) {
                map = new HashMap();
                getProviders().put((Class) next.getKey(), map);
            }
            HashSet hashSet = new HashSet(map.keySet());
            if (!hashSet.retainAll(((Map) next.getValue()).keySet())) {
                map.putAll((Map) next.getValue());
            } else {
                throw new IllegalArgumentException("Duplicate dependency found " + hashSet);
            }
        }
    }

    /* access modifiers changed from: protected */
    public <T> T create(Class<T> cls, String str) {
        T create = super.create(cls, str);
        injectDependencies(create);
        return create;
    }

    public void injectDependencies(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            Inject inject = (Inject) field.getAnnotation(Inject.class);
            if (inject != null) {
                injectForField(obj, field, inject);
            }
        }
    }

    private void injectForField(Object obj, Field field, Inject inject) {
        Object obj2;
        String tag = inject.tag();
        if (Constants.DEBUG) {
            Log.i("Dependencies", "injecting " + field.getType() + " for " + obj.getClass() + " with tag: " + tag);
        }
        try {
            field.setAccessible(true);
            if (tag.isEmpty()) {
                obj2 = Dependency.get(field.getType());
            } else {
                obj2 = get(field.getType(), tag);
            }
            if (Constants.DEBUG) {
                Log.i("Dependencies", "dependency found: " + obj2);
            }
            field.set(obj, obj2);
        } catch (IllegalAccessException e) {
            Log.e("Dependencies", "unable to inject " + field, e);
        }
    }

    private static class Builder {
        private List<Resolver> mResolvers;

        private Builder() {
            this.mResolvers = new ArrayList();
        }

        /* access modifiers changed from: package-private */
        public Builder addAll(Resolver... resolverArr) {
            this.mResolvers.addAll(Arrays.asList(resolverArr));
            return this;
        }

        /* access modifiers changed from: package-private */
        public Dependencies build() {
            return new Dependencies(this.mResolvers);
        }
    }

    public static void initialize(Resolver... resolverArr) {
        Builder builder = new Builder();
        builder.addAll(resolverArr);
        sInstance = builder.build();
    }

    public static Dependencies getInstance() {
        return sInstance;
    }
}
