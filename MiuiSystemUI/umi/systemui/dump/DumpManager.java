package com.android.systemui.dump;

import android.util.ArrayMap;
import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DumpManager {
    private ArrayMap<String, RegisteredDumpable> dumpables = new ArrayMap<>();

    public void registerDumpable(String str, Dumpable dumpable) {
        this.dumpables.put(str, new RegisteredDumpable(this, str, dumpable));
    }

    public void unRegisterDumpable(String str) {
        this.dumpables.remove(str);
    }

    public void dumpTarget(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        for (RegisteredDumpable next : this.dumpables.values()) {
            if (next.name.endsWith(str)) {
                printWriter.println();
                printWriter.println("${dumpable.name}:");
                printWriter.println("----------------------------------------------------------------------------");
                next.dumpable.dump(fileDescriptor, printWriter, strArr);
            }
        }
    }

    private class RegisteredDumpable {
        /* access modifiers changed from: private */
        public Dumpable dumpable;
        /* access modifiers changed from: private */
        public String name;

        public RegisteredDumpable(DumpManager dumpManager, String str, Dumpable dumpable2) {
            this.name = str;
            this.dumpable = dumpable2;
        }
    }
}
