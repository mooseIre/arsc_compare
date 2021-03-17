package com.android.systemui.shared.tracing;

public interface ProtoTraceable<T> {
    void writeToProto(T t);
}
