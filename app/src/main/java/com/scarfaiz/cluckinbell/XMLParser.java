package com.scarfaiz.cluckinbell;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by USER on 26.10.2017.
 */

public class XMLParser {
    // We don't use namespaces
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private String readCurrency(XmlPullParser parser, String requirement) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, requirement);
        String mTempStr;
        String result = null;
        Log.d("LogDebug", "////////parsing in " + requirement);
        while (parser.getEventType() != XmlPullParser.END_TAG) {
            switch (parser.getEventType()) {
                // если это начало документа
                case XmlPullParser.START_DOCUMENT:
                    Log.d("LogDebug", "START_DOCUMENT");
                    break;
                // если это начало тэга
                case XmlPullParser.START_TAG:
                    if(parser.getName()!=null) if(parser.getName().equals("result")) result = parser.getName();
                    Log.d("LogDebug", "START_TAG: имя тэга = " + parser.getName()
                            + ", глубина = " + parser.getDepth() + ", число атрибутов = "
                            + parser.getAttributeCount());
                    mTempStr  = "";
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        mTempStr = mTempStr + parser.getAttributeName(i) + " = "
                                + parser.getAttributeValue(i) + ", ";
                    }
                    if (!mTempStr.isEmpty())
                        Log.d("LogDebug", "Атрибyты: " + mTempStr);
                    break;
                // если это конец тэга
                case XmlPullParser.END_TAG:
                    Log.d("LogDebug", "END_TAG: имя = " + parser.getName());
                    break;
                // если это содержимое тэга
                case XmlPullParser.TEXT:
                    if(parser.getName()!=null)parser.require(XmlPullParser.START_TAG, ns, parser.getName());
                    Log.d("LogDebug", "текст = " + parser.getText());
                    break;

                default:
                    break;
            }
            // переходим к следующему элементу
            parser.next();
        }
        Log.d("LogDebug", "////////finished parsing in " + requirement);
        return result;
    }

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();
        String mTempStr;
        boolean r = true;
        while (parser.getEventType() != XmlPullParser.END_DOCUMENT && r) {
            switch (parser.getEventType()) {
                // если это начало документа
                case XmlPullParser.START_DOCUMENT:
                    Log.d("LogDebug", "START_DOCUMENT");
                    break;
                // если это начало тэга
                case XmlPullParser.START_TAG:
                    Log.d("LogDebug", "START_TAG: имя тэга = " + parser.getName()
                            + ", глубина = " + parser.getDepth() + ", число атрибутов = "
                            + parser.getAttributeCount());
                    mTempStr  = "";
                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        mTempStr = mTempStr + parser.getAttributeName(i) + " = "
                                + parser.getAttributeValue(i) + ", ";
                    }
                    if (!mTempStr.isEmpty())
                        Log.d("LogDebug", "Атрибyты: " + mTempStr);
                    break;
                // если это конец тэга
                case XmlPullParser.END_TAG:
                    Log.d("LogDebug", "END_TAG: имя = " + parser.getName());
                    break;
                // если это содержимое тэга
                case XmlPullParser.TEXT:
                    Log.d("LogDebug", "текст = " + parser.getText());
                    break;

                default:
                    break;
            }
            if(parser.getName()!=null)
                entries.add(readCurrency(parser, parser.getName()));
            // переходим к следующему элементу
            parser.next();
            if(parser.getName()!=null) if((parser.getEventType() != XmlPullParser.END_TAG && parser.getName().equals("addressparts")))
                r = false;
        }
        return entries;
    }

    public static class Entry {
        public final String city;
        public final String house_number;
        public final String road;

        private Entry(String city, String road, String house_number) {
            this.city = city;
            this.road = road;
            this.house_number = house_number;
        }
    }

    // Parses the contents of an entry. If it encounters a city, road, or house_number tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
}

