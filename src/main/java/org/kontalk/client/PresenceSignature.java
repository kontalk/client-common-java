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
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;


 /**
  * Packet extension for OpenPGP signed stanzas (XEP-0027, obsolete).
  *
  * Only stanza parsing supported, not creation.
  *
  * @author Alexander Bikadorov {@literal <bikaejkb@mail.tu-berlin.de>}
  */
public final class PresenceSignature implements ExtensionElement {
    public static final String ELEMENT_NAME = "x";
    public static final String NAMESPACE = "jabber:x:signed";

    private final String mSignature;

    private PresenceSignature(String signature) {
        mSignature = signature;
    }

    public String getSignature() {
        return mSignature;
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
            .rightAngleBracket().append("[[SIGNATURE DATA; LENGTH=")
            .append(String.valueOf(mSignature.length()))
            .append("]]")
            .closeElement(ELEMENT_NAME);
    }

    public static class Provider extends ExtensionElementProvider<PresenceSignature> {

        @Override
        public PresenceSignature parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment)
                throws XmlPullParserException, IOException, SmackParsingException {
            String signature = null;

            boolean done = false;
            while (!done) {
                XmlPullParser.Event eventType = parser.next();

                if(eventType == XmlPullParser.Event.END_DOCUMENT ||
                        eventType == XmlPullParser.Event.START_ELEMENT)
                    throw new XmlPullParserException("invalid XML schema");

                if (eventType == XmlPullParser.Event.TEXT_CHARACTERS) {
                    signature = parser.getText();
                }

                if (eventType == XmlPullParser.Event.END_ELEMENT &&
                        ELEMENT_NAME.equals(parser.getName())) {
                    done = true;
                }
            }

            return signature != null ? new PresenceSignature(signature) : null;
        }

    }
}
