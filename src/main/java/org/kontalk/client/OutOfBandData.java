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

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;


/**
 * XEP-0066: Out of Band Data
 * http://xmpp.org/extensions/xep-0066.html
 */
public class OutOfBandData implements ExtensionElement {

    public static final String NAMESPACE = "jabber:x:oob";
    public static final String ELEMENT_NAME = "x";

    private final String mUrl;
    private final String mMime;
    private final long mLength;
    private final boolean mEncrypted;

    public OutOfBandData(String url) {
        this(url, null, -1, false);
    }

    public OutOfBandData(String url, String mime, long length, boolean encrypted) {
        mUrl = url;
        mMime = mime;
        mLength = length;
        mEncrypted = encrypted;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getMime() {
        return mMime;
    }

    public long getLength() {
        return mLength;
    }

    public boolean isEncrypted() {
        return mEncrypted;
    }

    @Override
    public StringBuilder toXML(XmlEnvironment xmlEnvironment) {
        /*
  <x xmlns='jabber:x:oob'>
    <url type='image/png' length='2034782'>http://prime.kontalk.net/media/filename_or_hash</url>
  </x>
         */
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<%s xmlns='%s'><url", ELEMENT_NAME, NAMESPACE));
        if (mMime != null)
            xml.append(String.format(" type='%s'", mMime));

        if (mLength >= 0)
            xml.append(String.format(" length='%d'", mLength));

        if (mEncrypted)
            xml.append(" encrypted='true'");

        xml
            .append(">")
            // TODO should we escape this?
            .append(mUrl)
            .append(String.format("</url></%s>", ELEMENT_NAME));
        return xml;
    }

    public static final class Provider extends ExtensionElementProvider<OutOfBandData> {

        @Override
        public OutOfBandData parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException {
            String url = null, mime = null;
            long length = -1;
            boolean encrypted = false;
            boolean in_url = false, done = false;

            while (!done)
            {
                XmlPullParser.Event eventType = parser.next();

                if (eventType == XmlPullParser.Event.START_ELEMENT)
                {
                    if ("url".equals(parser.getName())) {
                        in_url = true;
                        mime = parser.getAttributeValue(null, "type");
                        String _length = parser.getAttributeValue(null, "length");
                        try {
                            length = Long.parseLong(_length);
                        }
                        catch (Exception e) {
                            // ignored
                        }
                        String _encrypted = parser.getAttributeValue(null, "encrypted");
                        encrypted = Boolean.parseBoolean(_encrypted);
                    }

                }
                else if (eventType == XmlPullParser.Event.END_ELEMENT)
                {
                    if ("url".equals(parser.getName())) {
                        done = true;
                    }
                }
                else if (eventType == XmlPullParser.Event.TEXT_CHARACTERS && in_url) {
                    url = parser.getText();
                }
            }

            if (url != null)
                return new OutOfBandData(url, mime, length, encrypted);
            else
                return null;
        }

    }

}
