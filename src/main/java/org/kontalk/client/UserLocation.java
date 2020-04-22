/*
 * Kontalk client common library
 * Copyright (C) 2020 Kontalk Devteam <devteam@kontalk.org>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kontalk.client;

import java.io.IOException;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;


/**
 * XEP-0080: User Location
 * http://xmpp.org/extensions/xep-0080.html
 *
 * @author Andrea Cappelli
 * @author Daniele Ricci
 */

public class UserLocation implements ExtensionElement {
    public static final String NAMESPACE = "http://jabber.org/protocol/geoloc";
    public static final String ELEMENT_NAME = "geoloc";

    private double mLatitude;
    private double mLongitude;
    private String mText;
    private String mStreet;

    public UserLocation(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public UserLocation(double latitude, double longitude, String text, String street) {
        mLatitude = latitude;
        mLongitude = longitude;
        mText = text;
        mStreet = street;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String getText() {
        return mText;
    }

    public String getStreet() {
        return mStreet;
    }

    public void setText(String text) {
        mText = text;
    }

    public void setStreet(String street) {
        mStreet = street;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public StringBuilder toXML(XmlEnvironment xmlEnvironment) {

        StringBuilder builder = new StringBuilder();
        builder.append("<")
            .append(ELEMENT_NAME)
            .append(" xmlns='")
            .append(NAMESPACE)
            .append("'><lat>")
            .append(mLatitude)
            .append("</lat><lon>")
            .append(mLongitude)
            .append("</lon>");

        if (!StringUtils.isNullOrEmpty(mText)) {
            builder.append("<text>")
                .append(mText)
                .append("</text>");
        }

        if (!StringUtils.isNullOrEmpty(mStreet)) {
            builder.append("<street>")
                .append(mStreet)
                .append("</street>");
        }

        builder.append("</")
            .append(ELEMENT_NAME)
            .append('>');

        return builder;
    }

    public static final class Provider extends ExtensionElementProvider<UserLocation> {

        @Override
        public UserLocation parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException, SmackParsingException {
            double lat = 0, lon = 0;
            String text = null, street = null;
            boolean lat_found = false, lon_found = false;
            boolean in_lat = false, in_lon = false;
            boolean text_found = false, street_found = false;
            boolean in_text = false, in_street = false, done = false;

            while (!done) {
                XmlPullParser.Event eventType = parser.next();

                if (eventType == XmlPullParser.Event.START_ELEMENT) {
                    if ("lon".equals(parser.getName())) {
                        in_lon = true;
                    }
                    else if ("lat".equals(parser.getName())) {
                        in_lat = true;
                    }
                    else if ("text".equals(parser.getName())) {
                        in_text = true;
                    }
                    else if ("street".equals(parser.getName())) {
                        in_street = true;
                    }
                }
                else if (eventType == XmlPullParser.Event.END_ELEMENT) {
                    if ("lon".equals(parser.getName())) {
                        in_lon = false;
                    }
                    else if ("lat".equals(parser.getName())) {
                        in_lat = false;
                    }
                    else if ("text".equals(parser.getName())) {
                        in_text = false;
                    }
                    else if ("street".equals(parser.getName())) {
                        in_street = false;
                    }
                    else if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
                else if (eventType == XmlPullParser.Event.TEXT_CHARACTERS) {
                    if (in_lon) {
                        try {
                            lon = Double.parseDouble(parser.getText());
                            lon_found = true;
                        }
                        catch (NumberFormatException ignored) {
                        }
                    }
                    else if (in_lat) {
                        try {
                            lat = Double.parseDouble(parser.getText());
                            lat_found = true;
                        }
                        catch (NumberFormatException ignored) {
                        }
                    }
                    else if (in_text) {
                        text = parser.getText();
                        text_found = true;
                    }
                    else if (in_street) {
                        street = parser.getText();
                        street_found = true;
                    }
                }
            }

            if (lon_found && lat_found) {
                UserLocation userLocation = new UserLocation(lat, lon);
                if (text_found)
                    userLocation.setText(text);
                if (street_found)
                    userLocation.setStreet(street);
                return userLocation;
            }
            else {
                return null;
            }
        }
    }
}
