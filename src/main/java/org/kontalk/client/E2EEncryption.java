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
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

import java.io.IOException;


/**
 * Very basic e2e (RFC 3923) extension.
 * http://tools.ietf.org/html/rfc3923
 * @author Daniele Ricci
 */
public class E2EEncryption implements ExtensionElement {
    public static final String ELEMENT_NAME = "e2e";
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:xmpp-e2e";

    private byte[] mData;
    private String mEncoded;

    public E2EEncryption(byte[] data) {
        mData = data;
    }

    public E2EEncryption(String encoded) {
        mEncoded = encoded;
        mData = Base64.decode(encoded);
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public byte[] getData() {
        return mData;
    }

    @Override
    public StringBuilder toXML(XmlEnvironment xmlEnvironment) {
        if (mEncoded == null)
            mEncoded = Base64.encodeToString(mData);

        return new StringBuilder()
            .append('<')
            .append(ELEMENT_NAME)
            .append(" xmlns='")
            .append(NAMESPACE)
            .append("'>")
            .append(mEncoded)
            .append("</")
            .append(ELEMENT_NAME)
            .append('>');
    }

    public static class Provider extends ExtensionElementProvider<E2EEncryption> {

        @Override
        public E2EEncryption parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException, SmackParsingException {
            boolean done = false;
            String data = null;

            while (!done) {
                XmlPullParser.Event eventType = parser.next();

                if (eventType == XmlPullParser.Event.TEXT_CHARACTERS) {
                    data = parser.getText();
                }
                else if (eventType == XmlPullParser.Event.END_ELEMENT) {
                    if (ELEMENT_NAME.equals(parser.getName()))
                        done = true;
                }
            }

            if (data != null)
                return new E2EEncryption(data);
            else
                return null;
        }

    }

}
