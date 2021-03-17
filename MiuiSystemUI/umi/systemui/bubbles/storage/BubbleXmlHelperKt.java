package com.android.systemui.bubbles.storage;

import android.util.Xml;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.XmlUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/* compiled from: BubbleXmlHelper.kt */
public final class BubbleXmlHelperKt {
    public static final void writeXml(@NotNull OutputStream outputStream, @NotNull List<BubbleEntity> list) throws IOException {
        Intrinsics.checkParameterIsNotNull(outputStream, "stream");
        Intrinsics.checkParameterIsNotNull(list, "bubbles");
        FastXmlSerializer fastXmlSerializer = new FastXmlSerializer();
        fastXmlSerializer.setOutput(outputStream, StandardCharsets.UTF_8.name());
        fastXmlSerializer.startDocument(null, Boolean.TRUE);
        fastXmlSerializer.startTag(null, "bs");
        fastXmlSerializer.attribute(null, "v", String.valueOf(1));
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            writeXmlEntry(fastXmlSerializer, it.next());
        }
        fastXmlSerializer.endTag(null, "bs");
        fastXmlSerializer.endDocument();
    }

    private static final void writeXmlEntry(XmlSerializer xmlSerializer, BubbleEntity bubbleEntity) {
        try {
            xmlSerializer.startTag(null, "bb");
            xmlSerializer.attribute(null, "uid", String.valueOf(bubbleEntity.getUserId()));
            xmlSerializer.attribute(null, "pkg", bubbleEntity.getPackageName());
            xmlSerializer.attribute(null, "sid", bubbleEntity.getShortcutId());
            xmlSerializer.attribute(null, "key", bubbleEntity.getKey());
            xmlSerializer.attribute(null, "h", String.valueOf(bubbleEntity.getDesiredHeight()));
            xmlSerializer.attribute(null, "hid", String.valueOf(bubbleEntity.getDesiredHeightResId()));
            String title = bubbleEntity.getTitle();
            if (title != null) {
                xmlSerializer.attribute(null, "t", title);
            }
            xmlSerializer.endTag(null, "bb");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static final List<BubbleEntity> readXml(@NotNull InputStream inputStream) {
        Intrinsics.checkParameterIsNotNull(inputStream, "stream");
        ArrayList arrayList = new ArrayList();
        XmlPullParser newPullParser = Xml.newPullParser();
        Intrinsics.checkExpressionValueIsNotNull(newPullParser, "Xml.newPullParser()");
        newPullParser.setInput(inputStream, StandardCharsets.UTF_8.name());
        XmlUtils.beginDocument(newPullParser, "bs");
        String attributeWithName = getAttributeWithName(newPullParser, "v");
        Integer valueOf = attributeWithName != null ? Integer.valueOf(Integer.parseInt(attributeWithName)) : null;
        if (valueOf != null && valueOf.intValue() == 1) {
            int depth = newPullParser.getDepth();
            while (XmlUtils.nextElementWithin(newPullParser, depth)) {
                BubbleEntity readXmlEntry = readXmlEntry(newPullParser);
                if (readXmlEntry != null) {
                    arrayList.add(readXmlEntry);
                }
            }
        }
        return arrayList;
    }

    private static final BubbleEntity readXmlEntry(XmlPullParser xmlPullParser) {
        String attributeWithName;
        String attributeWithName2;
        String attributeWithName3;
        while (xmlPullParser.getEventType() != 2) {
            xmlPullParser.next();
        }
        String attributeWithName4 = getAttributeWithName(xmlPullParser, "uid");
        if (attributeWithName4 != null) {
            int parseInt = Integer.parseInt(attributeWithName4);
            String attributeWithName5 = getAttributeWithName(xmlPullParser, "pkg");
            if (!(attributeWithName5 == null || (attributeWithName = getAttributeWithName(xmlPullParser, "sid")) == null || (attributeWithName2 = getAttributeWithName(xmlPullParser, "key")) == null || (attributeWithName3 = getAttributeWithName(xmlPullParser, "h")) == null)) {
                int parseInt2 = Integer.parseInt(attributeWithName3);
                String attributeWithName6 = getAttributeWithName(xmlPullParser, "hid");
                if (attributeWithName6 != null) {
                    return new BubbleEntity(parseInt, attributeWithName5, attributeWithName, attributeWithName2, parseInt2, Integer.parseInt(attributeWithName6), getAttributeWithName(xmlPullParser, "t"));
                }
            }
        }
        return null;
    }

    private static final String getAttributeWithName(@NotNull XmlPullParser xmlPullParser, String str) {
        int attributeCount = xmlPullParser.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            if (Intrinsics.areEqual(xmlPullParser.getAttributeName(i), str)) {
                return xmlPullParser.getAttributeValue(i);
            }
        }
        return null;
    }
}
