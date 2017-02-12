package com.grabble.grabble;

import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Georgi on 12/5/2016.
 */

public class MarkersXmlParser {

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

    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List placemarks = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "kml");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("Placemark")) {
                placemarks.add(readPlacemark(parser));
            } else {
                skip(parser);
            }
        }
        return placemarks;
    }


    // Parses the contents of a placemark. If it encounters a name, description, or point tag, hands them off
// to their respective "read" methods for processing. Otherwise, skips the tag.
    private Placemark readPlacemark(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Placemark");
        int name = 0;
        String description = null;
        LatLng point = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag_name = parser.getName();
            if (tag_name.equals("name")) {
                name = readName(parser);
            } else if (tag_name.equals("description")) {
                description = readDescription(parser);
            } else if (tag_name.equals("Point")) {
                point = readPoint(parser);
            } else {
                skip(parser);
            }
        }
        return new Placemark(name, description, point);
    }

    // Processes name tags in the feed.
    private int readName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String nameString = readText(parser);
        String[] names =  nameString.split(" ");
        int name = Integer.parseInt(names[1]);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    // Processes description tags in the feed.
    private String readDescription(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    // Processes point tags in the feed.
    private LatLng readPoint(XmlPullParser parser) throws IOException, XmlPullParserException {


        parser.require(XmlPullParser.START_TAG, ns, "Point");
        String coordinates = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("coordinates")) {
                coordinates = readText(parser);
            } else {
                skip(parser);
            }
        }

        String[] latlong =  coordinates.split(",");
        double latitude = Double.parseDouble(latlong[1]);
        double longitude = Double.parseDouble(latlong[0]);
        return new LatLng(latitude,longitude);
    }

    // For the tags name and description, extracts their text values.
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


