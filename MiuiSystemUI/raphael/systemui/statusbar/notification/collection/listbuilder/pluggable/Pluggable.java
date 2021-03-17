package com.android.systemui.statusbar.notification.collection.listbuilder.pluggable;

public abstract class Pluggable<This> {
    private PluggableListener<This> mListener;
    private final String mName;

    public interface PluggableListener<T> {
        void onPluggableInvalidated(T t);
    }

    Pluggable(String str) {
        this.mName = str;
    }

    public final String getName() {
        return this.mName;
    }

    public final void invalidateList() {
        PluggableListener<This> pluggableListener = this.mListener;
        if (pluggableListener != null) {
            pluggableListener.onPluggableInvalidated(this);
        }
    }

    public void setInvalidationListener(PluggableListener<This> pluggableListener) {
        this.mListener = pluggableListener;
    }
}
