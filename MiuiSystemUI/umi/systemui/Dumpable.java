package com.android.systemui;

import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface Dumpable {
    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);
}
