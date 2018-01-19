package com.neatherbench.quencher;

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
    public String tag;
    public String subtag;

    public XMLParser(String tag, String subtag)
    {
        this.tag = tag;
        this.subtag = subtag;
    }

    public List<XMLParser.Entry> parse(InputStream in) throws XmlPullParserException, IOException {
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

    private List<XMLParser.Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, tag);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(subtag)) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    public static class Entry {
        public final String city;
        public final String house_number;
        public final String road;
        public final String lat;
        public final String lon;

        private Entry(String city, String road, String house_number, String lat, String lon) {
            this.city = city;
            this.road = road;
            this.house_number = house_number;
            this.lat = lat;
            this.lon = lon;
        }
    }

    // Parses the contents of an entry. If it encounters a city, road, or house_number tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, subtag);
        String city = null;
        String road = null;
        String house_number = null;
        String lat = null;
        String lon = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            Log.d("LogDebug", name);
            if (name.equals("state")) {
                city = readCity(parser);
            } else if (name.equals("road")) {
                road = readRoad(parser);
            } else if (name.equals("house_number")) {
                house_number = readHouseNumber(parser);
            } else if (name.equals("lat")) {
                lat = readlat(parser);
            } else if (name.equals("lon")) {
                lon = readlon(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(city, road, house_number, lat, lon);
    }

    private String readlat(XmlPullParser parser)  throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "lat");
        String lat = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "lat");
        return lat;
    }

    private String readlon(XmlPullParser parser)  throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "lon");
        String lon = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "lon");
        return lon;
    }

    // Processes city tags in the feed.
    private String readCity(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "state");
        String city = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "state");
        return city;
    }

    // Processes House_number tags in the feed.
    private String readHouseNumber(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "house_number");
        String city = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "house_number");
        return city;
    }

    // Processes road tags in the feed.
    private String readRoad(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "road");
        String road = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "road");
        return road;
    }

    // For the tags city and road, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}

