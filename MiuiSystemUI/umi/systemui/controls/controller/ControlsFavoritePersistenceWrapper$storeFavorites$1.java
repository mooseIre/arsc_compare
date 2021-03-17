package com.android.systemui.controls.controller;

import android.app.backup.BackupManager;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import com.android.systemui.backup.BackupHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: ControlsFavoritePersistenceWrapper.kt */
final class ControlsFavoritePersistenceWrapper$storeFavorites$1 implements Runnable {
    final /* synthetic */ List $structures;
    final /* synthetic */ ControlsFavoritePersistenceWrapper this$0;

    ControlsFavoritePersistenceWrapper$storeFavorites$1(ControlsFavoritePersistenceWrapper controlsFavoritePersistenceWrapper, List list) {
        this.this$0 = controlsFavoritePersistenceWrapper;
        this.$structures = list;
    }

    public final void run() {
        boolean z;
        BackupManager access$getBackupManager$p;
        Log.d("ControlsFavoritePersistenceWrapper", "Saving data to file: " + this.this$0.file);
        AtomicFile atomicFile = new AtomicFile(this.this$0.file);
        synchronized (BackupHelper.Companion.getControlsDataLock()) {
            try {
                FileOutputStream startWrite = atomicFile.startWrite();
                z = true;
                try {
                    XmlSerializer newSerializer = Xml.newSerializer();
                    newSerializer.setOutput(startWrite, "utf-8");
                    newSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                    newSerializer.startDocument((String) null, Boolean.TRUE);
                    newSerializer.startTag((String) null, "version");
                    newSerializer.text("1");
                    newSerializer.endTag((String) null, "version");
                    newSerializer.startTag((String) null, "structures");
                    for (StructureInfo structureInfo : this.$structures) {
                        newSerializer.startTag((String) null, "structure");
                        newSerializer.attribute((String) null, "component", structureInfo.getComponentName().flattenToString());
                        newSerializer.attribute((String) null, "structure", structureInfo.getStructure().toString());
                        newSerializer.startTag((String) null, "controls");
                        for (ControlInfo controlInfo : structureInfo.getControls()) {
                            newSerializer.startTag((String) null, "control");
                            newSerializer.attribute((String) null, "id", controlInfo.getControlId());
                            newSerializer.attribute((String) null, "title", controlInfo.getControlTitle().toString());
                            newSerializer.attribute((String) null, "subtitle", controlInfo.getControlSubtitle().toString());
                            newSerializer.attribute((String) null, "type", String.valueOf(controlInfo.getDeviceType()));
                            newSerializer.endTag((String) null, "control");
                        }
                        newSerializer.endTag((String) null, "controls");
                        newSerializer.endTag((String) null, "structure");
                    }
                    newSerializer.endTag((String) null, "structures");
                    newSerializer.endDocument();
                    atomicFile.finishWrite(startWrite);
                } catch (Throwable th) {
                    IoUtils.closeQuietly(startWrite);
                    throw th;
                }
                IoUtils.closeQuietly(startWrite);
            } catch (IOException e) {
                Log.e("ControlsFavoritePersistenceWrapper", "Failed to start write file", e);
                return;
            } catch (Throwable th2) {
                throw th2;
            }
        }
        if (z && (access$getBackupManager$p = this.this$0.backupManager) != null) {
            access$getBackupManager$p.dataChanged();
        }
    }
}
