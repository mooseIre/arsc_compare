package com.android.systemui.controls.controller;

import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.util.Log;
import android.util.Xml;
import com.android.systemui.backup.BackupHelper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import libcore.io.IoUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/* compiled from: ControlsFavoritePersistenceWrapper.kt */
public final class ControlsFavoritePersistenceWrapper {
    private BackupManager backupManager;
    private final Executor executor;
    private File file;

    public ControlsFavoritePersistenceWrapper(@NotNull File file2, @NotNull Executor executor2, @Nullable BackupManager backupManager2) {
        Intrinsics.checkParameterIsNotNull(file2, "file");
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        this.file = file2;
        this.executor = executor2;
        this.backupManager = backupManager2;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ ControlsFavoritePersistenceWrapper(File file2, Executor executor2, BackupManager backupManager2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(file2, executor2, (i & 4) != 0 ? null : backupManager2);
    }

    public final void changeFileAndBackupManager(@NotNull File file2, @Nullable BackupManager backupManager2) {
        Intrinsics.checkParameterIsNotNull(file2, "fileName");
        this.file = file2;
        this.backupManager = backupManager2;
    }

    public final boolean getFileExists() {
        return this.file.exists();
    }

    public final void deleteFile() {
        this.file.delete();
    }

    public final void storeFavorites(@NotNull List<StructureInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "structures");
        this.executor.execute(new ControlsFavoritePersistenceWrapper$storeFavorites$1(this, list));
    }

    @NotNull
    public final List<StructureInfo> readFavorites() {
        List<StructureInfo> parseXml;
        if (!this.file.exists()) {
            Log.d("ControlsFavoritePersistenceWrapper", "No favorites, returning empty list");
            return CollectionsKt__CollectionsKt.emptyList();
        }
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(this.file));
            try {
                Log.d("ControlsFavoritePersistenceWrapper", "Reading data from file: " + this.file);
                synchronized (BackupHelper.Companion.getControlsDataLock()) {
                    XmlPullParser newPullParser = Xml.newPullParser();
                    newPullParser.setInput(bufferedInputStream, null);
                    Intrinsics.checkExpressionValueIsNotNull(newPullParser, "parser");
                    parseXml = parseXml(newPullParser);
                }
                IoUtils.closeQuietly(bufferedInputStream);
                return parseXml;
            } catch (XmlPullParserException e) {
                throw new IllegalStateException("Failed parsing favorites file: " + this.file, e);
            } catch (IOException e2) {
                throw new IllegalStateException("Failed parsing favorites file: " + this.file, e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(bufferedInputStream);
                throw th;
            }
        } catch (FileNotFoundException unused) {
            Log.i("ControlsFavoritePersistenceWrapper", "No file found");
            return CollectionsKt__CollectionsKt.emptyList();
        }
    }

    private final List<StructureInfo> parseXml(XmlPullParser xmlPullParser) {
        Ref$IntRef ref$IntRef = new Ref$IntRef();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ComponentName componentName = null;
        String str = null;
        while (true) {
            int next = xmlPullParser.next();
            ref$IntRef.element = next;
            if (next == 1) {
                return arrayList;
            }
            String name = xmlPullParser.getName();
            String str2 = "";
            if (name == null) {
                name = str2;
            }
            if (ref$IntRef.element == 2 && Intrinsics.areEqual(name, "structure")) {
                componentName = ComponentName.unflattenFromString(xmlPullParser.getAttributeValue(null, "component"));
                str = xmlPullParser.getAttributeValue(null, "structure");
                if (str == null) {
                    str = str2;
                }
            } else if (ref$IntRef.element == 2 && Intrinsics.areEqual(name, "control")) {
                String attributeValue = xmlPullParser.getAttributeValue(null, "id");
                String attributeValue2 = xmlPullParser.getAttributeValue(null, "title");
                String attributeValue3 = xmlPullParser.getAttributeValue(null, "subtitle");
                if (attributeValue3 != null) {
                    str2 = attributeValue3;
                }
                String attributeValue4 = xmlPullParser.getAttributeValue(null, "type");
                Integer valueOf = attributeValue4 != null ? Integer.valueOf(Integer.parseInt(attributeValue4)) : null;
                if (!(attributeValue == null || attributeValue2 == null || valueOf == null)) {
                    arrayList2.add(new ControlInfo(attributeValue, attributeValue2, str2, valueOf.intValue()));
                }
            } else if (ref$IntRef.element == 3 && Intrinsics.areEqual(name, "structure")) {
                if (componentName == null) {
                    Intrinsics.throwNpe();
                    throw null;
                } else if (str != null) {
                    arrayList.add(new StructureInfo(componentName, str, CollectionsKt___CollectionsKt.toList(arrayList2)));
                    arrayList2.clear();
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            }
        }
    }
}
