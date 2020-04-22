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
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;


/**
 * Minimal implementation of XEP-0027.
 * http://xmpp.org/extensions/xep-0027.html#signing
 * @author Daniele Ricci
 */
public class OpenPGPSignedMessage implements ExtensionElement {

    public static final String ELEMENT_NAME = "x";
    public static final String NAMESPACE = "jabber:x:signed";

    private byte[] mData;

    public OpenPGPSignedMessage(byte[] data) {
        mData = data;
    }

    public byte[] getData() {
        return mData;
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
        return new StringBuilder()
            .append('<')
            .append(ELEMENT_NAME)
            .append(" xmlns='")
            .append(NAMESPACE)
            .append("'><![CDATA[")
            .append(Base64.encodeToString(mData))
            .append("]]></")
            .append(ELEMENT_NAME)
            .append('>');
    }

    public static final class Provider extends ExtensionElementProvider<OpenPGPSignedMessage> {

        @Override
        public OpenPGPSignedMessage parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException, SmackParsingException {
            String contents = null;
            boolean done = false;

            while (!done)
            {
                XmlPullParser.Event eventType = parser.next();

                if (eventType == XmlPullParser.Event.END_ELEMENT)
                {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
                else if (eventType == XmlPullParser.Event.TEXT_CHARACTERS) {
                    contents = parser.getText();
                }
            }

            if (contents != null)
                return new OpenPGPSignedMessage(Base64.decode(contents));
            else
                return null;
        }

    }

}
