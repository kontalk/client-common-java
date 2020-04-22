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
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.parsing.SmackParsingException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;

/**
 * Avatar data extension (XEP-0084).
 * To be used as payload of a PubSub item.
 *
 * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
 */
public class AvatarDataExtension implements ExtensionElement {
    public static final String ELEMENT_NAME = "data";
    public static final String NAMESPACE = "urn:xmpp:avatar:data";

    /** Base64 encoded avatar data. */
    private final String mData;

    public AvatarDataExtension(byte[] data) {
        this(Base64.encodeToString(data));
    }

    private AvatarDataExtension(String data) {
        mData = data;
    }

    public byte[] getData() {
        return Base64.decode(mData);
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
    public CharSequence toXML(XmlEnvironment xmlEnvironment) {
        return new XmlStringBuilder()
                .halfOpenElement(ELEMENT_NAME)
                .xmlnsAttribute(NAMESPACE)
                .openElement("data")
                .append(mData)
                .closeElement("data");
    }

    public static final class Provider extends ExtensionElementProvider<AvatarDataExtension> {

        @Override
        public AvatarDataExtension parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException, SmackParsingException {
            boolean done = false;

            String data = "";

            while (!done) {
                XmlPullParser.Event eventType = parser.next();

                if(eventType == XmlPullParser.Event.END_DOCUMENT)
                    throw new XmlPullParserException("invalid XML schema");

                else if (eventType == XmlPullParser.Event.TEXT_CHARACTERS) {
                    data = parser.getText();
                }

                else if (eventType == XmlPullParser.Event.END_ELEMENT) {
                    if (ELEMENT_NAME.equals(parser.getName())) {
                        done = true;
                    }
                }
            }

            return new AvatarDataExtension(data);
        }
    }

}
